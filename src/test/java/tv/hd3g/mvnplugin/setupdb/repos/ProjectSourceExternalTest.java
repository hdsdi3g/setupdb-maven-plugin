package tv.hd3g.mvnplugin.setupdb.repos;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import tv.hd3g.mvnplugin.setupdb.TestLog;

class ProjectSourceExternalTest {

	@Mock
	MavenProject project;
	TestLog log;
	File cacheRepository;

	@BeforeEach
	void init() throws Exception {
		MockitoAnnotations.openMocks(this).close();
		Mockito.when(project.getGroupId()).thenReturn("tv.hd3g.internaltest");
		Mockito.when(project.getArtifactId()).thenReturn(getClass().getSimpleName().toLowerCase());
		final var scm = Mockito.mock(Scm.class);
		Mockito.when(project.getScm()).thenReturn(scm);
		Mockito.when(scm.getConnection()).thenReturn(
		        "scm:git:https://github.com/hdsdi3g/test-setupdb-maven-plugin.git");

		cacheRepository = new File("target" + File.separator + "internal-test-" + getClass().getSimpleName());
		cacheRepository.mkdirs();

		log = new TestLog();
	}

	@Test
	void testFetchUpdateGitRepository() throws Exception {
		Mockito.when(project.getVersion()).thenReturn("0.2.0");
		final ProjectSource projectSource = new ProjectSourceExternal(project, cacheRepository, log);
		final var result = projectSource.getLocalRepository();
		assertNotNull(result);
	}

}
