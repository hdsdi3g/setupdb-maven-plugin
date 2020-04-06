package tv.hd3g.mvnplugin.setupdb;

import static tv.hd3g.mvnplugin.setupdb.MojoEnv.Const.MVNPLUGIN_REF;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public interface MojoEnv {

	public final class Const {
		private Const() {
		}

		public static final String MVNPLUGIN_REF = "tv.hd3g.mvnplugin:setupdb";
	}

	BuildPluginManager getPluginManager();

	MavenSession getSession();

	MavenProject getProject();

	String getLiquibasePluginVersion();

	boolean isLiquibaseVerbose();

	Log getLog();

	/**
	 * Internal XML conf can return a list (not implemented here)
	 */
	static String extractFromConf(final MavenProject project, final String key, final String defaultResult) {
		final var plugin = project.getPlugin(MVNPLUGIN_REF);
		if (plugin == null) {
			return defaultResult;
		}

		final var conf = plugin.getConfiguration();
		if (conf == null) {
			return defaultResult;
		}
		if (conf instanceof Xpp3Dom == false) {
			return defaultResult;
		}
		final var xmlConf = (Xpp3Dom) conf;
		final var result = xmlConf.getChild(key);
		if (result == null || result.getValue() == null || result.getValue().isEmpty()) {
			return defaultResult;
		}
		return result.getValue();
	}

	static String projectKey(final MavenProject project) {
		return project.getGroupId() + ":" + project.getArtifactId();
	}

	static String fullProjectKey(final MavenProject project) {
		return project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
	}

}