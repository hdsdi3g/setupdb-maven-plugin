package tv.hd3g.mvnplugin.setupdb.repos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tv.hd3g.mvnplugin.setupdb.repos.LocalRepository.CONF_KEY_DRIVER;
import static tv.hd3g.mvnplugin.setupdb.repos.LocalRepository.CONF_KEY_PASSWORD;
import static tv.hd3g.mvnplugin.setupdb.repos.LocalRepository.CONF_KEY_URL;
import static tv.hd3g.mvnplugin.setupdb.repos.LocalRepository.CONF_KEY_USER;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import tv.hd3g.mvnplugin.setupdb.TestLog;

class LocalRepositoryTest {

	private static final String SPRING_CONFIG_LOCATION = "spring.config.location";
	private static final String MYSQL_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	File projectRepositoryDir;
	LocalRepository localRepository;

	@Mock
	MavenProject project;
	@Mock
	XmlMerge xmlMerge;

	@BeforeEach
	void init() throws Exception {
		MockitoAnnotations.openMocks(this).close();

		System.getProperties().remove(CONF_KEY_DRIVER);
		System.getProperties().remove(CONF_KEY_URL);
		System.getProperties().remove(CONF_KEY_USER);
		System.getProperties().remove(CONF_KEY_PASSWORD);
		System.getProperties().remove(SPRING_CONFIG_LOCATION);

		projectRepositoryDir = new File("");
		localRepository = new LocalRepository(projectRepositoryDir, project, new TestLog());
	}

	@Test
	void testGetApplicationConfig_byProperty() {
		final var driverName = System.nanoTime() + "xxx";
		System.setProperty(CONF_KEY_DRIVER, driverName);

		final var url = System.nanoTime() + "xxx";
		System.setProperty(CONF_KEY_URL, url);

		final var user = System.nanoTime() + "xxx";
		System.setProperty(CONF_KEY_USER, user);

		final var pass = System.nanoTime() + "xxx";
		System.setProperty(CONF_KEY_PASSWORD, pass);

		final var dataSource = localRepository.getDatasourceConfig();
		assertNotNull(dataSource);
		assertEquals(driverName, dataSource.getDriver());
		assertEquals(url, dataSource.getJdbcUrl());
		assertEquals(user, dataSource.getUsername());
		assertEquals(pass, dataSource.getPassword());

		System.getProperties().remove(CONF_KEY_DRIVER);
		System.setProperty(CONF_KEY_URL, "jdbc:mysql://");

		assertEquals(MYSQL_JDBC_DRIVER, localRepository.getDatasourceConfig().getDriver());
	}

	@Test
	void testGetApplicationConfig_byLocalProperties() {
		final var dataSource = localRepository.getDatasourceConfig();
		assertNotNull(dataSource);
		assertEquals("jdbc:mysql://localhost:3306/databaseP", dataSource.getJdbcUrl());
		assertEquals("usrnmP", dataSource.getUsername());
		assertEquals("passwdP", dataSource.getPassword());
		assertEquals(MYSQL_JDBC_DRIVER, dataSource.getDriver());
	}

	@Test
	void testGetApplicationConfig_byLocalYaml() {
		System.setProperty(SPRING_CONFIG_LOCATION, "config/application.yml");

		final var dataSource = localRepository.getDatasourceConfig();
		assertNotNull(dataSource);
		assertEquals("jdbc:mysql://localhost:3306/databaseY", dataSource.getJdbcUrl());
		assertEquals("usrnmY", dataSource.getUsername());
		assertEquals("passwdY", dataSource.getPassword());
		assertEquals(MYSQL_JDBC_DRIVER, dataSource.getDriver());
	}

	@Test
	void testMergeChangeLogFile() throws IOException {
		final ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
		localRepository.mergeChangeLogFile(xmlMerge);
		Mockito.verify(xmlMerge).addChangelog(captor.capture());
		assertEquals("database-changelog.xml", captor.getValue().getName());
	}

	@Test
	void testGetProject() {
		assertEquals(project, localRepository.getProject());
	}

	@Test
	void extractFromYamlMap() {
		final var t1 = LocalRepository.extractFromYamlMap(Map.of("k1", "v1", "k2", "v2", "k3", "v3"), "k2");
		assertEquals("v2", t1);
	}

}
