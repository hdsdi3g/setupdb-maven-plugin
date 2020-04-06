package tv.hd3g.mvnplugin.setupdb.repos;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Map.entry;
import static tv.hd3g.mvnplugin.setupdb.MojoEnv.extractFromConf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.yaml.snakeyaml.Yaml;

import tv.hd3g.mvnplugin.setupdb.DatasourceConf;
import tv.hd3g.mvnplugin.setupdb.MojoEnv;

public class LocalRepository {

	public static final String DEFAULT_CHANGELOG_XML = "scripts/db/database-changelog.xml";
	private static final String EXCEPTION_IN_APP_CONF = "\" in application configuration";
	private static final String EXCEPTION_MISSING_EMPTY = "Missing/empty \"";

	public static final String CONF_KEY_PASSWORD = "spring.datasource.password";
	public static final String CONF_KEY_USER = "spring.datasource.username";
	public static final String CONF_KEY_URL = "spring.datasource.url";
	public static final String CONF_KEY_DRIVER = "spring.datasource.driver-class-name";

	public static final List<String> CONF_KEY_LIST = List.of(
	        CONF_KEY_URL,
	        CONF_KEY_DRIVER,
	        CONF_KEY_USER,
	        CONF_KEY_PASSWORD);

	/**
	 * No dependencies will be checked
	 */
	private static final Map<String, String> BASE_JDBC_URL_TO_DRIVER_NAME = Map.ofEntries(
	        entry("jdbc:mysql", "com.mysql.cj.jdbc.Driver"),
	        entry("jdbc:oracle:thin", "oracle.jdbc.driver.OracleDriver"),
	        entry("jdbc:sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
	        entry("jdbc:mariadb", "org.mariadb.jdbc.Driver"),
	        entry("jdbc:db2", "com.ibm.db2.jcc.DB2Driver"),
	        entry("jdbc:sap", "com.sap.db.jdbc.Driver"),
	        entry("jdbc:informix-sqli", "com.informix.jdbc.IfxDriver"),
	        entry("jdbc:hsqldb", "org.hsqldb.jdbc.JDBCDriver"),
	        entry("jdbc:h2", "org.h2.Driver"),
	        entry("jdbc:derby", "org.apache.derby.jdbc.EmbeddedDriver"),
	        entry("jdbc:postgresql", "org.postgresql.Driver"));

	private final File projectRepositoryDir;
	private final MavenProject project;
	private final Log log;
	private final Function<String, Stream<Properties>> propFileToPropMapper;
	private final File changeLog;

	public LocalRepository(final File projectRepositoryDir, final MavenProject project,
	                       final Log log) throws MojoExecutionException {
		this.projectRepositoryDir = projectRepositoryDir;
		this.project = project;
		this.log = log;

		propFileToPropMapper = f -> {
			try {
				return Stream.of(parseApplicationConfig(new File(f)));
			} catch (final FileNotFoundException e) {
				return Stream.empty();
			} catch (final IOException e) {
				log.warn("Can't load/read file " + f, e);
				return Stream.empty();
			}
		};

		final var changeLogLocalPath = extractFromConf(project, "changeLog", DEFAULT_CHANGELOG_XML);
		changeLog = new File(projectRepositoryDir.getAbsolutePath() + File.separator
		                     + changeLogLocalPath.replace("/", File.separator));
		if (changeLog.exists() == false || changeLog.isFile() == false) {
			throw new MojoExecutionException("Can't found changeLog file \"" + changeLog + "\"");
		}
	}

	public void mergeChangeLogFile(final XmlMerge xmlMerge) {
		xmlMerge.addChangelog(changeLog);
	}

	public void importChangeLogFile(final XmlMerge xmlMerge) {
		xmlMerge.importChangeLog(changeLog, MojoEnv.fullProjectKey(project));
	}

	public MavenProject getProject() {
		return project;
	}

	public DatasourceConf getDatasourceConfig() {
		final var home = System.getProperty("user.home");
		final var projectPath = projectRepositoryDir.getAbsolutePath();

		final var envConf = new Properties();
		envConf.putAll(System.getenv());

		final Properties globalConfig = new Properties();

		Stream.of(
		        /** System system properties */
		        Stream.of(System.getProperties()),

		        /** System environment var */
		        Stream.of(envConf),

		        /** Json/yaml injected on system property */
		        Optional.ofNullable(System.getProperty("SPRING_APPLICATION_JSON"))
		                .map(json -> {
			                final var result = new Properties();
			                final Yaml yaml = new Yaml();
			                final Map<String, Object> yamlContent = yaml.load(json);
			                walkToYamlVars(result, yamlContent);
			                return Stream.of(result);
		                }).orElse(Stream.empty()),

		        /** Specific conf file(s) from system properties */
		        Optional.ofNullable(System.getProperty("spring.config.location"))
		                .map(location -> Arrays.stream(location.split(",")))
		                .orElse(Stream.empty())
		                .flatMap(propFileToPropMapper),

		        /** Specific conf file(s) from environment vars */
		        Optional.ofNullable(System.getenv("spring.config.location"))
		                .map(location -> Arrays.stream(location.split(",")))
		                .orElse(Stream.empty())
		                .flatMap(propFileToPropMapper),

		        /** Local user dev conf */
		        Stream.of(
		                home + "/.config/spring-boot/spring-boot-devtools.properties",
		                home + "/.config/spring-boot/spring-boot-devtools.yaml",
		                home + "/.config/spring-boot/spring-boot-devtools.yml").flatMap(propFileToPropMapper),

		        /** Project POM plugin config */
		        Optional.ofNullable(extractFromConf(project, "applicationconfig", null)).map(
		                propFileToPropMapper).orElse(Stream.empty()),

		        /** Current project conf files */
		        Stream.of(
		                projectPath + "/config/application.properties",
		                projectPath + "/config/application.yaml",
		                projectPath + "/config/application.yml",
		                projectPath + "/src/test/resources/application.properties",
		                projectPath + "/src/test/resources/application.yaml",
		                projectPath + "/src/test/resources/application.yml").flatMap(propFileToPropMapper))

		        /** Process */
		        .flatMap(s -> s)
		        .takeWhile(current -> {
			        if (CONF_KEY_LIST.stream().noneMatch(current::containsKey)) {
				        /**
				         * This config source have not a wanted conf key
				         */
				        return true;
			        }

			        /**
			         * We only keep new wanted conf key
			         */
			        return CONF_KEY_LIST.stream().anyMatch(confKey -> current.containsKey(confKey)
			                                                          && globalConfig.containsKey(confKey) == false);
		        }).forEach(current -> CONF_KEY_LIST.stream()
		                .filter(current::containsKey)
		                .forEach(confKey -> {
			                final var value = current.getProperty(confKey);
			                log.debug("Found configuration key: " + confKey + "=" + value);
			                globalConfig.put(confKey, value);
		                }));

		final var result = new DatasourceConf();

		/**
		 * URL
		 */
		final var confURL = globalConfig.get(CONF_KEY_URL);
		if (confURL == null || ((String) confURL).isEmpty()) {
			throw new IllegalArgumentException(EXCEPTION_MISSING_EMPTY + CONF_KEY_URL + EXCEPTION_IN_APP_CONF);
		}
		result.setJdbcUrl((String) confURL);

		/**
		 * Driver
		 */
		final var confDriver = globalConfig.get(CONF_KEY_DRIVER);
		if (confDriver == null || ((String) confDriver).isEmpty()) {
			result.setDriver(BASE_JDBC_URL_TO_DRIVER_NAME.keySet().stream()
			        .filter(result.getJdbcUrl()::startsWith)
			        .map(BASE_JDBC_URL_TO_DRIVER_NAME::get)
			        .findFirst()
			        .orElseThrow(() -> new IllegalArgumentException(EXCEPTION_MISSING_EMPTY + CONF_KEY_DRIVER
			                                                        + EXCEPTION_IN_APP_CONF)));
		} else {
			result.setDriver((String) confDriver);
		}

		/**
		 * User
		 */
		final var confUser = globalConfig.get(CONF_KEY_USER);
		if (confUser == null || ((String) confUser).isEmpty()) {
			throw new IllegalArgumentException(EXCEPTION_MISSING_EMPTY + CONF_KEY_USER + EXCEPTION_IN_APP_CONF);
		}
		result.setUsername((String) confUser);

		/**
		 * Password
		 */
		final var confPasswd = globalConfig.get(CONF_KEY_PASSWORD);
		if (confPasswd == null) {
			result.setPassword("");
		} else {
			result.setPassword((String) confPasswd);
		}

		return result;
	}

	static Properties parseApplicationConfig(final File applicationConfig) throws IOException {
		if (applicationConfig.exists() == false) {
			throw new FileNotFoundException(applicationConfig.getAbsolutePath());
		}
		final var result = new Properties();
		if (applicationConfig.getName().endsWith(".properties")) {
			try (FileReader reader = new FileReader(applicationConfig, UTF_8)) {
				result.load(reader);
				return result;
			}
		} else if (applicationConfig.getName().endsWith(".yaml")
		           || applicationConfig.getName().endsWith(".yml")) {
			try (FileReader reader = new FileReader(applicationConfig, UTF_8)) {
				final Yaml yaml = new Yaml();
				final Map<String, Object> yamlContent = yaml.load(reader);
				walkToYamlVars(result, yamlContent);
				return result;
			}
		} else {
			throw new FileNotFoundException("Can't get the type of file \""
			                                + applicationConfig.getAbsolutePath() + "\"");
		}
	}

	static void walkToYamlVars(final Properties result, final Map<String, Object> yamlContent) {
		CONF_KEY_LIST.forEach(k -> Optional.ofNullable(extractFromYamlMap(yamlContent, k))
		        .ifPresent(value -> result.put(k, value)));
	}

	static String extractFromYamlMap(final Map<?, ?> content,
	                                 final String keys) {
		final var keyList = Arrays.asList(keys.split("\\."));
		if (keyList.isEmpty()) {
			throw new IllegalArgumentException("Empty keys list");
		}
		final var value = content.get(keyList.get(0));
		if (value == null) {
			return null;
		} else if (keyList.size() == 1 && value instanceof String) {
			return (String) value;
		} else if (keyList.size() > 1 && value instanceof Map) {
			return extractFromYamlMap((Map<?, ?>) value, keyList.stream().skip(1).collect(Collectors.joining(".")));
		} else if (keyList.size() == 1) {
			throw new IllegalArgumentException("Expect String result for \"" + keyList.get(0)
			                                   + "\" but not get String: " + value);
		} else {
			throw new IllegalArgumentException("Expect Map result for \"" + keys + "\" but not get: " + value);
		}
	}

}
