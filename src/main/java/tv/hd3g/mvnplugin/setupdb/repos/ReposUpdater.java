package tv.hd3g.mvnplugin.setupdb.repos;

import static tv.hd3g.mvnplugin.setupdb.MojoEnv.projectKey;
import static tv.hd3g.mvnplugin.setupdb.MojoEnv.Const.MVNPLUGIN_REF;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.util.FileUtils;

public class ReposUpdater {

	private final MavenProject currentProject;
	private final List<MavenProject> externalProjects;
	private final File cacheRepository;
	private final Log log;

	public ReposUpdater(final MavenProject currentProject,
	                    final List<MavenProject> externalProjects,
	                    final File cacheRepository,
	                    final Log log) throws MojoFailureException {
		this.currentProject = currentProject;
		this.externalProjects = externalProjects;
		this.cacheRepository = cacheRepository;
		this.log = log;

		/**
		 * Check cacheRepository dir status
		 */
		try {
			FileUtils.mkdirs(cacheRepository, true);
		} catch (final IOException e) {
			throw new MojoFailureException("Invalid cacheRepository: " + cacheRepository, e);
		}
		if (cacheRepository.isDirectory() == false
		    || cacheRepository.exists() == false
		    || cacheRepository.canRead() == false) {
			throw new MojoFailureException("Invalid cacheRepository: " + cacheRepository);
		}
	}

	/**
	 * Load all LocalRepository from all externalProjects with setupdb
	 */
	private List<LocalRepository> getUpdatedRepositoryList() {
		return Stream.concat(externalProjects.stream()
		        .filter(p -> currentProject.equals(p) == false),
		        Stream.of(currentProject))
		        .filter(p -> p.getPlugin(MVNPLUGIN_REF) != null)
		        .map(p -> {
			        if (currentProject.equals(p)) {
				        return new ProjectSourceCurrent(p, log);
			        } else {
				        return new ProjectSourceExternal(p, cacheRepository, log);
			        }
		        })
		        .map(projectSource -> {
			        try {
				        return projectSource.getLocalRepository();
			        } catch (final MojoExecutionException e1) {
				        throw new IllegalArgumentException("Can't load localRepository for "
				                                           + projectKey(projectSource.project), e1);
			        }
		        })
		        .collect(Collectors.toUnmodifiableList());
	}

	public LocalRepository doUpdateChangelogResolved(final File resolvedChangelog) throws MojoFailureException {
		final var localRepositoryList = getUpdatedRepositoryList();

		/**
		 * Make resolvedChangelog XML file
		 */
		final var xmlMerge = new XmlMerge(log);
		localRepositoryList.forEach(lr -> lr.mergeChangeLogFile(xmlMerge));

		try {
			log.debug("Save XML file: " + resolvedChangelog);
			xmlMerge.save(resolvedChangelog);
		} catch (final IOException e) {
			throw new MojoFailureException("Can't save XML file: " + resolvedChangelog, e);
		}

		return localRepositoryList.stream().filter(r -> currentProject.equals(r.getProject()))
		        .findFirst()
		        .orElseThrow(() -> new MojoFailureException("Can't found current project repo"));
	}

	public void doUpdateChangelogArchive(final File archiveChangelog) throws MojoFailureException {
		final var localRepositoryList = getUpdatedRepositoryList();

		/**
		 * Make archiveChangelog XML file
		 */
		final var xmlMerge = new XmlMerge(log);
		localRepositoryList.forEach(lr -> lr.importChangeLogFile(xmlMerge));

		try {
			log.debug("Save XML file: " + archiveChangelog);
			xmlMerge.save(archiveChangelog);
		} catch (final IOException e) {
			throw new MojoFailureException("Can't save XML file: " + archiveChangelog, e);
		}
	}

}
