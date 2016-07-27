package unit.services.startup;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.alwaysTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.services.startup.DefaultStartupManager;
import org.cmdbuild.services.startup.StartupManager;
import org.cmdbuild.services.startup.StartupManager.Startable;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;

public class DefaultStartupManagerTest {

	private static final Predicate<Void> ALWAYS = alwaysTrue();
	private static final Predicate<Void> NEVER = alwaysFalse();

	private StartupManager startupManager;

	@Before
	public void setUp() throws Exception {
		startupManager = new DefaultStartupManager();
	}

	@Test
	public void serviceNotStartedIfConditionIsNotSatisfied() throws Exception {
		// given
		final Startable startable = mock(Startable.class);
		startupManager = new DefaultStartupManager();
		startupManager.add(startable, NEVER);

		// when
		startupManager.start();

		// then
		verifyNoMoreInteractions(startable);
	}

	@Test
	public void serviceStartedIfConditionIsSatisfied() throws Exception {
		final Startable startable = mock(Startable.class);
		startupManager = new DefaultStartupManager();
		startupManager.add(startable, ALWAYS);

		// when
		startupManager.start();

		// then
		verify(startable).start();
		verifyNoMoreInteractions(startable);
	}

	@Test
	public void serviceStartedWhenConditionIsSatisfied() throws Exception {
		// given
		final Startable startable = mock(Startable.class);
		startupManager = new DefaultStartupManager();
		startupManager.add(startable, condition(false, true));

		// when
		startupManager.start();

		// then
		verifyZeroInteractions(startable);

		// when
		startupManager.start();

		// then
		verify(startable).start();
		verifyNoMoreInteractions(startable);
	}

	@Test
	public void serviceNoMoreStartedAfterTheFirstStart() throws Exception {
		final Startable startable = mock(Startable.class);
		startupManager = new DefaultStartupManager();
		startupManager.add(startable, ALWAYS);

		// when
		startupManager.start();

		// then
		verify(startable).start();

		// when
		startupManager.start();

		// then
		verifyNoMoreInteractions(startable);
	}

	@Test
	public void multipleServicesStartedAtDifferentTimes() throws Exception {
		// given
		final Startable firstStartable = mock(Startable.class);
		final Startable secondStartable = mock(Startable.class);
		startupManager = new DefaultStartupManager();
		startupManager.add(firstStartable, ALWAYS);
		startupManager.add(secondStartable, condition(false, true));

		// when
		startupManager.start();

		// then
		verify(firstStartable).start();
		verifyZeroInteractions(secondStartable);

		// when
		startupManager.start();

		// then
		verify(secondStartable).start();
		verifyZeroInteractions(firstStartable);
	}

	/*
	 * Utilities
	 */

	private Predicate<Void> condition(final Boolean value, final Boolean... values) {
		final Predicate<Void> condition = mock(Predicate.class);
		when(condition.apply(null)) //
				.thenReturn(value, values);
		return condition;
	}

}
