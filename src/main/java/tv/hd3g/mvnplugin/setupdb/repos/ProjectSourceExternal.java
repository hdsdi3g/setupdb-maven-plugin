package tv.hd3g.mvnplugin.setupdb.repos;

import static tv.hd3g.mvnplugin.setupdb.MojoEnv.fullProjectKey;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.apache.maven.model.Scm;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

public class ProjectSourceExternal extends ProjectSource {

	private final File cacheRepository;
	private final URL gitScmProvider;

	public ProjectSourceExternal(final MavenProject project,
	                             final File cacheRepository,
	                             final Log log) {
		super(project, log);
		this.cacheRepository = cacheRepository;

		final var connection = Optional.ofNullable(project.getScm())
		        .map(Scm::getConnection)
		        .orElseThrow(() -> new SourceExtractionException(project, "No SCM declared in POM"));
		if (connection == null) {
			throw new SourceExtractionException(project, "No SCM connection declared in POM");
		} else if (connection.startsWith("scm:git:") == false && connection.startsWith("scm:git|") == false) {
			throw new SourceExtractionException(project, "Invalid SCM connection declared in POM: " + connection);
		}

		try {
			gitScmProvider = new URL(connection.substring("scm:git:".length()));
		} catch (final MalformedURLException e) {
			throw new SourceExtractionException(project,
			        "Invalid SCM connection URL declared in POM: " + e.getMessage());
		}
		if ("git".equals(gitScmProvider.getProtocol()) == false
		    && "https".equals(gitScmProvider.getProtocol()) == false
		    && "http".equals(gitScmProvider.getProtocol()) == false) {
			throw new SourceExtractionException(project,
			        "Invalid SCM connection URL protocol  declared in POM: " + gitScmProvider);
		} else if (gitScmProvider.getPath() == null || gitScmProvider.getPath().endsWith(".git") == false) {
			throw new SourceExtractionException(project,
			        "Invalid SCM connection URL path declared in POM: " + gitScmProvider);
		}
	}

	@Override
	public LocalRepository getLocalRepository() throws MojoExecutionException {
		final var projectRepositoryDir = new File(cacheRepository.getAbsolutePath() + File.separator
		                                          + project.getGroupId() + "_" + project.getArtifactId());
		Git git = null;
		try {
			if (projectRepositoryDir.exists()) {
				log.info(fullProjectKey(project)
				         + ": try to update repo \"" + projectRepositoryDir + "\" from \"" + gitScmProvider + "\"");

				git = Git.open(projectRepositoryDir);
				git.fetch().call();
				git.checkout().setName("master").call();
				git.pull().call();
			} else {
				log.info(fullProjectKey(project)
				         + ": try to clone to \"" + projectRepositoryDir + "\" from \"" + gitScmProvider + "\"");

				git = Git.cloneRepository()
				        .setURI(gitScmProvider.toString())
				        .setDirectory(projectRepositoryDir)
				        .setBare(false)
				        .setCloneAllBranches(false)
				        .setCloneSubmodules(false)
				        .call();
			}

			final List<Ref> list = git.tagList().call();
			final var currentVersionIsTagged = list.stream()
			        .anyMatch(ref -> ref.getName().equals("refs/tags/" + project.getVersion()));
			if (currentVersionIsTagged) {
				log.debug(fullProjectKey(project) + ": get tag " + project.getVersion());
				git.checkout().setName("refs/tags/" + project.getVersion()).call();
			}
		} catch (final GitAPIException | IOException e) {
			throw new SourceExtractionException(project, "Can't access/manage local git repository: "
			                                             + gitScmProvider + " in "
			                                             + projectRepositoryDir, e);
		} finally {
			if (git != null) {
				git.close();
			}
		}
		return new LocalRepository(projectRepositoryDir, project, log);
	}

	public static class SourceExtractionException extends RuntimeException {

		private SourceExtractionException(final MavenProject project, final String reason) {
			super(fullProjectKey(project) + " cause: " + reason);
		}

		private SourceExtractionException(final MavenProject project, final String reason, final Exception cause) {
			super(fullProjectKey(project) + " cause: " + reason, cause);
		}

	}

}
