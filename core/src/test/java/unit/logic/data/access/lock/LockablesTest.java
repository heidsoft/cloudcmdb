package unit.logic.data.access.lock;

import static org.cmdbuild.logic.data.access.lock.Lockables.card;
import static org.cmdbuild.logic.data.access.lock.Lockables.instanceActivity;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.cmdbuild.logic.data.access.lock.Lockable;
import org.junit.Test;

public class LockablesTest {

	@Test
	public void lockableCards() {
		// given
		final Lockable first = card(1L);
		final Lockable second = card(2L);
		final Lockable sameAsFirst = card(1L);

		// then
		assertThat(first, equalTo(sameAsFirst));
		assertThat(first.hashCode(), equalTo(sameAsFirst.hashCode()));
		assertThat(first, not(equalTo(second)));
		assertThat(first.hashCode(), not(equalTo(second.hashCode())));
	}

	@Test
	public void lockableInstanceActivities() {
		// given
		final Lockable first = instanceActivity(1L, "foo");
		final Lockable second = instanceActivity(2L, "bar");
		final Lockable third = instanceActivity(1L, "baz");
		final Lockable fourth = instanceActivity(2L, "foo");
		final Lockable sameAsFirst = instanceActivity(1L, "foo");

		// then
		assertThat(first, equalTo(sameAsFirst));
		assertThat(first.hashCode(), equalTo(sameAsFirst.hashCode()));
		assertThat(first, not(equalTo(second)));
		assertThat(first.hashCode(), not(equalTo(second.hashCode())));
		assertThat(first, not(equalTo(third)));
		assertThat(first.hashCode(), not(equalTo(third.hashCode())));
		assertThat(first, not(equalTo(fourth)));
		assertThat(first.hashCode(), not(equalTo(fourth.hashCode())));
	}

}
