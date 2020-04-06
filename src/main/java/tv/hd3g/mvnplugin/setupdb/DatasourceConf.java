package tv.hd3g.mvnplugin.setupdb;

import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;

import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

public class DatasourceConf {

	private String driver;
	private String jdbcUrl;
	private String username;
	private String password;

	public void setDriver(final String driver) {
		this.driver = driver;
	}

	public void setJdbcUrl(final String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public Element getDriverElement() {
		return element(name("driver"), driver);
	}

	public Element getUrlElement() {
		return element(name("url"), jdbcUrl);
	}

	public Element getPasswordElement() {
		if (password == null || password.isEmpty()) {
			return element(name("password"));
		}
		return element(name("password"), password);
	}

	public Element getUsernameElement() {
		return element(name("username"), username);
	}

	public String getDriver() {
		return driver;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
