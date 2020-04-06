package tv.hd3g.mvnplugin.setupdb.repos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class ProjectSourceCurrent extends ProjectSource {

	public ProjectSourceCurrent(final MavenProject project, final Log log) {
		super(project, log);
	}

	@Override
	public LocalRepository getLocalRepository() throws MojoExecutionException {
		return new LocalRepository(project.getBasedir(), project, log);
	}

}
