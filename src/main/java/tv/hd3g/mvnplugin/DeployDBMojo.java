package tv.hd3g.mvnplugin;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

@Mojo(name = "deploy")
public class DeployDBMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
	private List<MavenProject> projects;

	@Override
	public void execute() throws MojoExecutionException {
		for (final MavenProject subProject : projects) {
			extractPlugin(subProject);
		}
		extractPlugin(project);
	}

	private static String projectKey(final MavenProject project) {
		return project.getGroupId() + ":" + project.getArtifactId();
	}

	private void extractPlugin(final MavenProject project) {
		final var plugin = project.getPlugin("tv.hd3g.mvnplugin:setupdb");
		if (plugin == null) {
			getLog().debug("No setupdb plugin for " + projectKey(project));
			return;
		}
		final var conf = plugin.getConfiguration();
		if (conf == null) {
			getLog().info("No configuration for setupdb plugin in " + projectKey(project));
			return;
		}
		if (conf instanceof Xpp3Dom == false) {
			getLog().error("Invalid configuration type (" + conf.getClass() + ") for setupdb plugin in " + projectKey(
			        project));
			return;
		}
		final var xmlConf = (Xpp3Dom) conf;

		Arrays.stream(xmlConf.getChildren()).forEach(c -> getLog()
		        .info("Conf type for " + projectKey(project) + ": " + c.getName() + ">" + c.getValue()));

		// TODO get external deps if needed + git fetch
		// TODO sum all liquibase conf and mergue in a global xml file
		// TODO get and check local database configuration (application.yml)
		// TODO call liquibase and update (or run another command)

		// TODO add an option for drop all tables
		// TODO add an option for copy all external liquibase and regroup in a local directory (usefull for setups scripts)
	}

}
