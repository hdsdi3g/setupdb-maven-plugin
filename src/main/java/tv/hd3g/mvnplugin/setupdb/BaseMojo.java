package tv.hd3g.mvnplugin.setupdb;

import java.io.File;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class BaseMojo extends AbstractMojo implements MojoEnv {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	protected MavenProject project;

	@Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
	protected List<MavenProject> projects;

	@Parameter(defaultValue = "${user.home}/.mvn-setupdb-cache", readonly = true, required = true)
	protected File cacheRepository;

	@Parameter(property = "resolvedChangelog",
	           defaultValue = "${project.build.directory}/database-changelog-resolved.xml",
	           readonly = true, required = true)
	protected File resolvedChangelog;

	@Parameter(property = "archiveChangelog",
	           defaultValue = "${project.build.directory}/database-full-archive-changelog.xml",
	           readonly = true, required = true)
	protected File archiveChangelog;

	@Parameter(property = "liquibasePluginVersion",
	           defaultValue = "3.8.9",
	           readonly = true, required = true)
	protected String liquibasePluginVersion;

	@Parameter(property = "liquibaseVerbose",
	           defaultValue = "false",
	           readonly = true, required = true)
	protected boolean liquibaseVerbose;

	@Parameter(defaultValue = "${session}", readonly = true)
	protected MavenSession session;

	@Component
	protected BuildPluginManager pluginManager;

	@Override
	public BuildPluginManager getPluginManager() {
		return pluginManager;
	}

	@Override
	public MavenSession getSession() {
		return session;
	}

	@Override
	public MavenProject getProject() {
		return project;
	}

	@Override
	public String getLiquibasePluginVersion() {
		return liquibasePluginVersion;
	}

	@Override
	public boolean isLiquibaseVerbose() {
		return liquibaseVerbose;
	}

}
