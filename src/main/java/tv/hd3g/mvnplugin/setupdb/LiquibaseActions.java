package tv.hd3g.mvnplugin.setupdb;

import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

public class LiquibaseActions {

	private final DatasourceConf dbConf;
	private final MojoEnv mojo;

	public LiquibaseActions(final MojoEnv mojo, final DatasourceConf dbConf) {
		this.mojo = mojo;
		this.dbConf = dbConf;
	}

	public void update(final File resolvedChangelog) throws MojoExecutionException {
		execute("update",
		        element("changeLogFile", resolvedChangelog.getAbsolutePath()));
	}

	public void rollback(final File resolvedChangelog, final int countBack) throws MojoExecutionException {
		execute("rollback",
		        element("changeLogFile", resolvedChangelog.getAbsolutePath()),
		        element("rollbackCount", String.valueOf(countBack)));
	}

	public void execute(final String goal, final Element... configuration) throws MojoExecutionException {
		mojo.getLog().info("Start Liquibase maven plugin v" + mojo.getLiquibasePluginVersion()
		                   + " with " + goal + " goal");

		final Xpp3Dom xmlConfiguration = new Xpp3Dom("configuration");
		for (final Element e : configuration) {
			xmlConfiguration.addChild(e.toDom());
		}

		xmlConfiguration.addChild(element("verbose", String.valueOf(mojo.isLiquibaseVerbose())).toDom());
		xmlConfiguration.addChild(dbConf.getUrlElement().toDom());
		xmlConfiguration.addChild(dbConf.getDriverElement().toDom());
		xmlConfiguration.addChild(dbConf.getUsernameElement().toDom());
		xmlConfiguration.addChild(dbConf.getPasswordElement().toDom());
		xmlConfiguration.addChild(element("promptOnNonLocalDatabase", "false").toDom());

		executeMojo(
		        plugin(
		                "org.liquibase",
		                "liquibase-maven-plugin",
		                mojo.getLiquibasePluginVersion(),
		                /**
		                 * Transfert all "client" deps to this plugin deps
		                 */
		                mojo.getProject().getDependencies()),
		        goal(goal),
		        xmlConfiguration,
		        executionEnvironment(
		                mojo.getProject(),
		                mojo.getSession(),
		                mojo.getPluginManager()));
	}

}
