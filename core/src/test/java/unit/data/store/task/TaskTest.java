package unit.data.store.task;

import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.cmdbuild.common.utils.BuilderUtils.build;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Map;

import org.cmdbuild.data.store.task.Task;
import org.cmdbuild.data.store.task.Task.Builder;
import org.cmdbuild.data.store.task.TaskVisitor;
import org.junit.Test;

public class TaskTest {

	private static class DummyTask extends Task {

		public static Builder<DummyTask> newInstance() {
			return new Builder<DummyTask>() {

				@Override
				protected DummyTask doBuild() {
					return new DummyTask(this);
				}

			};
		}

		private DummyTask(final Builder<? extends Task> builder) {
			super(builder);
		}

		@Override
		public void accept(final TaskVisitor visitor) {
			// nothing to do
		}

		@Override
		protected Builder<? extends Task> builder() {
			return newInstance();
		}

	}

	private static final Map<String, ? extends String> NO_PARAMETERS = Collections.emptyMap();

	@Test
	public void taskCanBeCreatedEmpty() throws Exception {
		// given
		final Builder<DummyTask> builder = DummyTask.newInstance();

		// when
		final Task task = build(builder);

		// then
		final Task expected = a(DummyTask.newInstance() //
				.withId(null) //
				.withDescription(null) //
				.withRunningStatus(null) //
				.withCronExpression(null) //
				.withParameters(NO_PARAMETERS));
		assertThat(task, equalTo(expected));
		assertThat(task.hashCode(), equalTo(expected.hashCode()));
	}

	@Test
	public void ifNotSpecifiedIsNotRunning() throws Exception {
		// given
		final Task notSpecified = a(DummyTask.newInstance());
		final Task specifiedNotRunning = a(DummyTask.newInstance().withRunningStatus(false));
		final Task specifiedRunning = a(DummyTask.newInstance().withRunningStatus(true));

		// when/then
		assertThat(notSpecified.isRunning(), is(false));
		assertThat(specifiedNotRunning.isRunning(), is(false));
		assertThat(specifiedRunning.isRunning(), is(true));
	}

	@Test
	public void elementsWithLittleDifferenceIdAreDifferent() throws Exception {
		// given
		final Task base = a(DummyTask.newInstance() //
				.withId(1L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter("foo", "bar") //
				.withParameter("bar", "baz") //
				.withParameter("baz", "foo"));
		final Task differentId = a(DummyTask.newInstance() //
				.withId(2L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter("foo", "bar") //
				.withParameter("bar", "baz") //
				.withParameter("baz", "foo"));
		final Task differentDescription = a(DummyTask.newInstance() //
				.withId(1L) //
				.withDescription("another description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter("foo", "bar") //
				.withParameter("bar", "baz") //
				.withParameter("baz", "foo"));
		final Task differentRunningStatus = a(DummyTask.newInstance() //
				.withId(1L) //
				.withDescription("description") //
				.withRunningStatus(false) //
				.withCronExpression("cron expression") //
				.withParameter("foo", "bar") //
				.withParameter("bar", "baz") //
				.withParameter("baz", "foo"));
		final Task differentCronExpression = a(DummyTask.newInstance() //
				.withId(1L) //
				.withDescription("description") //
				.withRunningStatus(false) //
				.withCronExpression("cron expression") //
				.withParameter("foo", "bar") //
				.withParameter("bar", "baz") //
				.withParameter("baz", "foo"));
		final Task differentSingleParameter = a(DummyTask.newInstance() //
				.withId(1L) //
				.withDescription("description") //
				.withRunningStatus(false) //
				.withCronExpression("cron expression") //
				.withParameter("foo", "FOO") //
				.withParameter("bar", "baz") //
				.withParameter("baz", "foo"));
		final Task differentMissingParameter = a(DummyTask.newInstance() //
				.withId(1L) //
				.withDescription("description") //
				.withRunningStatus(false) //
				.withCronExpression("cron expression") //
				.withParameter("bar", "baz") //
				.withParameter("baz", "foo"));
		final Task differentAdditionalParameter = a(DummyTask.newInstance() //
				.withId(1L) //
				.withDescription("description") //
				.withRunningStatus(false) //
				.withCronExpression("cron expression") //
				.withParameter("lol", "lol") //
				.withParameter("foo", "bar") //
				.withParameter("bar", "baz") //
				.withParameter("baz", "foo"));

		// when/then
		assertThat(base, not(equalTo(differentId)));
		assertThat(base, not(equalTo(differentDescription)));
		assertThat(base, not(equalTo(differentRunningStatus)));
		assertThat(base, not(equalTo(differentCronExpression)));
		assertThat(base, not(equalTo(differentSingleParameter)));
		assertThat(base, not(equalTo(differentMissingParameter)));
		assertThat(base, not(equalTo(differentAdditionalParameter)));
		assertThat(base.hashCode(), not(equalTo(differentId.hashCode())));
		assertThat(base.hashCode(), not(equalTo(differentDescription.hashCode())));
		assertThat(base.hashCode(), not(equalTo(differentRunningStatus.hashCode())));
		assertThat(base.hashCode(), not(equalTo(differentCronExpression.hashCode())));
		assertThat(base.hashCode(), not(equalTo(differentSingleParameter.hashCode())));
		assertThat(base.hashCode(), not(equalTo(differentMissingParameter.hashCode())));
		assertThat(base.hashCode(), not(equalTo(differentAdditionalParameter.hashCode())));
	}

	@Test
	public void parametersInDifferentOrderAreNotImportantForEquality() throws Exception {
		// given
		final Task lhs = a(DummyTask.newInstance() //
				.withId(1L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter("foo", "bar") //
				.withParameter("bar", "baz") //
				.withParameter("baz", "foo"));
		final Task rhs = a(DummyTask.newInstance() //
				.withId(1L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter("baz", "foo") //
				.withParameter("foo", "bar") //
				.withParameter("bar", "baz"));

		// when/then
		assertThat(lhs, equalTo(rhs));
		assertThat(lhs.hashCode(), equalTo(rhs.hashCode()));
	}

}
