package tv.hd3g.mvnplugin.setupdb;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import tv.hd3g.mvnplugin.setupdb.repos.ReposUpdater;

@Mojo(name = "archive")
public class ArchiveMojo extends BaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final var updater = new ReposUpdater(project, projects, cacheRepository, getLog());
		updater.doUpdateChangelogArchive(archiveChangelog);
	}

}
