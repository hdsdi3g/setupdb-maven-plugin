package tv.hd3g.mvnplugin.setupdb;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import tv.hd3g.mvnplugin.setupdb.repos.ReposUpdater;

@Mojo(name = "deploy")
public class DeployMojo extends BaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final var updater = new ReposUpdater(project, projects, cacheRepository, getLog());
		final var currentRepo = updater.doUpdateChangelogResolved(resolvedChangelog);

		final var liquibase = new LiquibaseActions(this, currentRepo.getDatasourceConfig());
		getLog().info("Liquibase update from " + resolvedChangelog);
		liquibase.update(resolvedChangelog);
	}

}
