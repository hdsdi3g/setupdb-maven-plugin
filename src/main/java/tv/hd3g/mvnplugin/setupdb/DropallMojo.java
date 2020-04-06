package tv.hd3g.mvnplugin.setupdb;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import tv.hd3g.mvnplugin.setupdb.repos.LocalRepository;
import tv.hd3g.mvnplugin.setupdb.repos.ReposUpdater;

@Mojo(name = "dropall")
public class DropallMojo extends BaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		LocalRepository currentRepo;
		if (resolvedChangelog.exists() == false) {
			final var updater = new ReposUpdater(project, projects, cacheRepository, getLog());
			currentRepo = updater.doUpdateChangelogResolved(resolvedChangelog);
		} else {
			currentRepo = new LocalRepository(project.getBasedir(), project, getLog());
		}

		final var liquibase = new LiquibaseActions(this, currentRepo.getDatasourceConfig());
		getLog().info("Liquibase rollback all from " + resolvedChangelog);
		liquibase.rollback(resolvedChangelog, 100_000_000);
	}

}
