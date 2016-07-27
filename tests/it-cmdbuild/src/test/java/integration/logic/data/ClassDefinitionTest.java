package integration.logic.data;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.cmdbuild.dao.entrytype.CMClass;
import org.junit.Ignore;
import org.junit.Test;

public class ClassDefinitionTest extends DataDefinitionLogicTest {

	private static final String SUPER_CLASS_NAME = "super";
	private static final String CLASS_NAME = "foo";
	private static final String DESCRIPTION = "description of foo";

	@Test
	public void createStandardClassHasSomeDefaults() {
		// given
		dataDefinitionLogic().createOrUpdate(a(newClass(CLASS_NAME)));

		// when
		final CMClass createdClass = dataView().findClass(CLASS_NAME);

		// then
		assertThat(createdClass.getName(), equalTo(CLASS_NAME));
		assertThat(createdClass.getDescription(), equalTo(CLASS_NAME));
		assertThat(createdClass.isSuperclass(), equalTo(false));
		assertThat(createdClass.isActive(), equalTo(true));
		assertThat(createdClass.holdsHistory(), equalTo(true));
		assertThat(createdClass.getParent(), is(nullValue(CMClass.class)));
	}

	@Test
	public void createClassWithNoDescription() {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newClass(CLASS_NAME)));

		// when
		final CMClass createdClass = dataView().findClass(CLASS_NAME);

		// then
		assertThat(createdClass.getDescription(), equalTo(CLASS_NAME));
	}

	@Test
	public void createClassWithDescriptionDifferentFromName() {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newClass(CLASS_NAME).withDescription(DESCRIPTION)));

		// when
		final CMClass createdClass = dataView().findClass(CLASS_NAME);

		// then
		assertThat(createdClass.getDescription(), equalTo(DESCRIPTION));
	}

	@Test
	public void createNonActiveClass() {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newClass(CLASS_NAME).thatIsActive(false)));

		// when
		final CMClass createdClass = dataView().findClass(CLASS_NAME);

		// then
		assertThat(createdClass.isActive(), equalTo(false));
	}

	@Test
	public void createClassWithNoParent() {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newClass(CLASS_NAME)));

		// when
		final CMClass createdClass = dataView().findClass(CLASS_NAME);

		// then
		assertThat(createdClass.getParent(), is(nullValue()));
	}

	@Test
	public void createClassWithParent() {
		// given
		final CMClass parent = dataDefinitionLogic().createOrUpdate(a(newClass(SUPER_CLASS_NAME)));
		dataDefinitionLogic().createOrUpdate( //
				a(newClass(CLASS_NAME).withParent(parent.getId())));

		// when
		final CMClass createdClass = dataView().findClass(CLASS_NAME);

		// then
		assertThat(createdClass.getParent().getName(), is(SUPER_CLASS_NAME));
	}

	@Test
	public void createClassWithNoHistoryAndNoParentAKASimpleClass() {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newClass(CLASS_NAME).withTableType(org.cmdbuild.model.data.EntryType.TableType.simpletable)));

		// when
		final CMClass createdClass = dataView().findClass(CLASS_NAME);

		// then
		assertThat(createdClass.holdsHistory(), equalTo(false));
		assertThat(createdClass.getParent(), is(nullValue()));
	}

	@Test
	public void onlyDescriptionAndActiveConditionCanBeChangedDuringUpdate() {
		// given
		final CMClass parent = dataDefinitionLogic().createOrUpdate(a(newClass(SUPER_CLASS_NAME)));
		dataDefinitionLogic().createOrUpdate( //
				a(newClass(CLASS_NAME).withParent(parent.getId())));
		dataDefinitionLogic().createOrUpdate(a(newClass(CLASS_NAME) //
				.withDescription(DESCRIPTION) //
				.withParent(null) //
				.thatIsActive(false)));

		// when
		final CMClass updatedClass = dataView().findClass(CLASS_NAME);

		// then
		assertThat(updatedClass.holdsHistory(), equalTo(true));
		assertThat(updatedClass.getParent().getName(), equalTo(parent.getName()));
		assertThat(updatedClass.getDescription(), equalTo(DESCRIPTION));
		assertThat(updatedClass.isActive(), equalTo(false));
	}

	@Test
	public void deletingUnexistingClassDoesNothing() throws Exception {
		// given
		// nothing

		// when
		dataDefinitionLogic().deleteOrDeactivate(CLASS_NAME);

		// then
		// nothing happens, but at least no errors
	}

	@Test
	public void deletingExistingClassWithNoData() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate(a(newClass(CLASS_NAME)));

		// when
		dataDefinitionLogic().deleteOrDeactivate(CLASS_NAME);

		// then
		assertThat(dataView().findClass(CLASS_NAME), is(nullValue()));
	}

	@Ignore
	@Test
	public void deletingExistingClassWithDataSetsTheClassAsNoActiveAndThrowsException() throws Exception {
		fail("TODO");
	}

}
