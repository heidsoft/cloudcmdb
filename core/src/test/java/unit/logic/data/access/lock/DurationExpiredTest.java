package unit.logic.data.access.lock;

import static java.lang.Thread.currentThread;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.cmdbuild.logic.data.access.lock.DefaultLockManager.DurationExpired;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.DurationExpired.Configuration;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.Lock;
import org.junit.Before;
import org.junit.Test;

public class DurationExpiredTest {

	private static final long TIMEOUT = 100;
	private static final long TIMEOUT_FOR_TESTS = 100 * 3;

	private DurationExpired predicate;

	@Before
	public void setUp() throws Exception {
		predicate = new DurationExpired(new Configuration() {

			@Override
			public long getExpirationTimeInMilliseconds() {
				return TIMEOUT;
			}

		});
	}

	@Test(timeout = TIMEOUT_FOR_TESTS)
	public void durationNotExpired() throws Exception {
		// given
		final Lock lock = Lock.newInstance().withTime(new Date()).build();

		// when
		final boolean output = predicate.apply(lock);

		// then
		assertThat(output, equalTo(false));
	}

	@Test(timeout = TIMEOUT_FOR_TESTS)
	public void durationExpired() throws Exception {
		// given
		final Lock lock = Lock.newInstance().withTime(new Date()).build();

		currentThread();
		// when
		Thread.sleep(TIMEOUT * 2);
		final boolean output = predicate.apply(lock);

		// then
		assertThat(output, equalTo(true));
	}

}
