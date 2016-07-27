package unit.data.store.task;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.cmdbuild.data.store.task.TaskParameter;
import org.cmdbuild.data.store.task.TaskParameter.Builder;
import org.junit.Test;

public class TaskParameterTest {

	@Test
	public void ownerAndKeyAreTheOnlyRequirements() throws Exception {
		// given
		final Builder builder = TaskParameter.newInstance() //
				.withOwner(456L) //
				.withKey("key");

		// when
		final TaskParameter taskParameter = builder.build();

		// then
		final TaskParameter expected = a(TaskParameter.newInstance() //
				.withId(null) //
				.withOwner(456L) //
				.withKey("key") //
				.withValue(null));
		assertThat(taskParameter, equalTo(expected));
		assertThat(taskParameter.hashCode(), equalTo(expected.hashCode()));
	}

	@Test(expected = NullPointerException.class)
	public void missingOwnerIsFatalAtBuilding() throws Exception {
		// given
		final Builder builder = TaskParameter.newInstance() //
				.withKey("key");

		// when
		builder.build();
	}

	@Test(expected = NullPointerException.class)
	public void nullOwnerIsFatalAtBuilding() throws Exception {
		// given
		final Builder builder = TaskParameter.newInstance() //
				.withOwner(null) //
				.withKey("key");

		// when
		builder.build();
	}

	@Test(expected = NullPointerException.class)
	public void missingKeyIsFatalAtBuilding() throws Exception {
		// given
		final Builder builder = TaskParameter.newInstance() //
				.withOwner(456L);

		// when
		builder.build();
	}

	@Test(expected = NullPointerException.class)
	public void nullKeyIsFatalAtBuilding() throws Exception {
		// given
		final Builder builder = TaskParameter.newInstance() //
				.withOwner(456L) //
				.withKey(null);

		// when
		builder.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyKeyIsFatalAtBuilding() throws Exception {
		// given
		final Builder builder = TaskParameter.newInstance() //
				.withOwner(456L) //
				.withKey(EMPTY);

		// when
		builder.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void blankKeyIsFatalAtBuilding() throws Exception {
		// given
		final Builder builder = TaskParameter.newInstance() //
				.withOwner(456L) //
				.withKey(" \t");

		// when
		builder.build();
	}

	@Test
	public void elementFullyInitialized() throws Exception {
		// given
		final Builder builder = TaskParameter.newInstance() //
				.withId(123L) //
				.withOwner(456L) //
				.withKey("key") //
				.withValue("value");

		// when
		final TaskParameter taskParameter = builder.build();

		// then
		final TaskParameter expected = a(TaskParameter.newInstance() //
				.withId(123L) //
				.withOwner(456L) //
				.withKey("key") //
				.withValue("value"));
		assertThat(taskParameter, equalTo(expected));
		assertThat(taskParameter.hashCode(), equalTo(expected.hashCode()));
	}

	@Test
	public void elementsWithLittleDifferenceIdAreDifferent() throws Exception {
		// given
		final TaskParameter base = a(TaskParameter.newInstance() //
				.withId(1L) //
				.withOwner(456L) //
				.withKey("key") //
				.withValue("value"));
		final TaskParameter differentId = a(TaskParameter.newInstance() //
				.withId(2L) //
				.withOwner(456L) //
				.withKey("key") //
				.withValue("value"));
		final TaskParameter differentOwner = a(TaskParameter.newInstance() //
				.withId(1L) //
				.withOwner(42L) //
				.withKey("key") //
				.withValue("value"));
		final TaskParameter differentKey = a(TaskParameter.newInstance() //
				.withId(1L) //
				.withOwner(456L) //
				.withKey("foo") //
				.withValue("value"));
		final TaskParameter differentValue = a(TaskParameter.newInstance() //
				.withId(1L) //
				.withOwner(456L) //
				.withKey("key") //
				.withValue("foo"));

		// when/then
		assertThat(base, not(equalTo(differentId)));
		assertThat(base, not(equalTo(differentOwner)));
		assertThat(base, not(equalTo(differentKey)));
		assertThat(base, not(equalTo(differentValue)));
		assertThat(base.hashCode(), not(equalTo(differentId.hashCode())));
		assertThat(base.hashCode(), not(equalTo(differentOwner.hashCode())));
		assertThat(base.hashCode(), not(equalTo(differentKey.hashCode())));
		assertThat(base.hashCode(), not(equalTo(differentValue.hashCode())));
	}

}
