package tv.hd3g.mvnplugin.setupdb;

import org.apache.maven.plugin.logging.Log;

public final class TestLog implements Log {

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public void debug(final CharSequence content) {
	}

	@Override
	public void debug(final CharSequence content, final Throwable error) {
	}

	@Override
	public void debug(final Throwable error) {
	}

	@Override
	public boolean isInfoEnabled() {
		return false;
	}

	@Override
	public void info(final CharSequence content) {
	}

	@Override
	public void info(final CharSequence content, final Throwable error) {
	}

	@Override
	public void info(final Throwable error) {
	}

	@Override
	public boolean isWarnEnabled() {
		return false;
	}

	@Override
	public void warn(final CharSequence content) {

	}

	@Override
	public void warn(final CharSequence content, final Throwable error) {
	}

	@Override
	public void warn(final Throwable error) {
	}

	@Override
	public boolean isErrorEnabled() {
		return false;
	}

	@Override
	public void error(final CharSequence content) {
	}

	@Override
	public void error(final CharSequence content, final Throwable error) {
	}

	@Override
	public void error(final Throwable error) {
	}

}