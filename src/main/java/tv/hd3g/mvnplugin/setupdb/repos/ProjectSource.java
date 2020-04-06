package tv.hd3g.mvnplugin.setupdb.repos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public abstract class ProjectSource {

	protected final MavenProject project;
	protected final Log log;

	public ProjectSource(final MavenProject project, final Log log) {
		this.project = project;
		this.log = log;
	}

	public abstract LocalRepository getLocalRepository() throws MojoExecutionException;

}