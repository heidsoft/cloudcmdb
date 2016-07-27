package utils;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.profiler.Profiler;

public class ProfilerRule extends TestWatcher {

	private Profiler profiler;

	@Override
	protected void starting(final Description description) {
		super.starting(description);
		profiler = new Profiler(description.getClassName());
		profiler.setLogger(LoggingSupport.logger);
		profiler.start(description.getMethodName());
	}

	@Override
	protected void succeeded(final Description description) {
		super.succeeded(description);
		profiler.stop().log();
	}

	@Override
	protected void finished(final Description description) {
		super.finished(description);
		profiler.stop();
	}

}
