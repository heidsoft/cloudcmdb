package integration.dao;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.namesOf;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.newSimpleClass;
import static utils.IntegrationTestUtils.newSuperClass;

import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Test;

import utils.IntegrationTestBase;

public class ClassesStructureTest extends IntegrationTestBase {

	@Test
	public void classesReturnedFromCreationMethodHaveAllData() {
		// given

		// when
		final DBClass superClass = dbDataView().create(newSuperClass("super"));
		final DBClass classWithNoSuperClass = dbDataView().create(newClass("foo"));
		final DBClass classWithSuperClass = dbDataView().create(newClass("bar", superClass));
		final DBClass simpleClass = dbDataView().create(newSimpleClass("baz"));

		// then
		assertThat(superClass.getIdentifier().getLocalName(), equalTo("super"));
		assertThat(superClass.isSuperclass(), equalTo(true));
		assertThat(superClass.getParent(), nullValue());
		assertThat(superClass.getLeaves(), hasItems(classWithSuperClass));

		assertThat(classWithNoSuperClass.getIdentifier().getLocalName(), equalTo("foo"));
		assertThat(classWithNoSuperClass.isSuperclass(), equalTo(false));
		assertThat(classWithNoSuperClass.getParent(), nullValue());
		assertThat(classWithNoSuperClass.holdsHistory(), equalTo(true));

		assertThat(classWithSuperClass.getIdentifier().getLocalName(), equalTo("bar"));
		assertThat(classWithSuperClass.isSuperclass(), equalTo(false));
		assertThat(classWithSuperClass.getParent(), equalTo(superClass));
		assertThat(classWithSuperClass.holdsHistory(), equalTo(true));

		assertThat(simpleClass.getIdentifier().getLocalName(), equalTo("baz"));
		assertThat(simpleClass.isSuperclass(), equalTo(false));
		assertThat(simpleClass.getParent(), nullValue());
		assertThat(simpleClass.holdsHistory(), equalTo(false));
	}

	@Test
	public void classesHierarchyBuildedWithoutReloadingClasses() {
		// given

		// when
		final DBClass superClass = dbDataView().create(newSuperClass("superClass"));
		final DBClass subClassA = dbDataView().create(newClass("subClassA", superClass));
		final DBClass subClassB = dbDataView().create(newClass("subClassB", superClass));

		// then
		assertThat(superClass.isSuperclass(), is(true));
		assertThat(superClass.isAncestorOf(subClassA), is(true));
		assertThat(superClass.isAncestorOf(subClassB), is(true));
		assertThat(namesOf(superClass.getChildren()), hasItem("subClassA"));
		assertThat(namesOf(superClass.getChildren()), hasItem("subClassB"));
		assertThat(subClassA.getParent().getIdentifier().getLocalName(), equalTo("superClass"));
		assertThat(subClassB.getParent().getIdentifier().getLocalName(), equalTo("superClass"));
	}

	@Test
	public void newlyCreatedClassesCanBeFoundBetweenAllClasses() {
		// given
		final DBClass superClass = dbDataView().create(newSuperClass("super"));
		dbDataView().create(newClass("foo"));
		dbDataView().create(newClass("bar", superClass));
		dbDataView().create(newSimpleClass("baz"));

		// when
		final Iterable<String> classNames = namesOf(dbDataView().findClasses());

		// then
		assertThat(classNames, hasItems("super", "foo", "bar", "baz"));
	}

	@Test
	public void classCreatedAndRead() {
		// given
		dbDataView().create(newClass("foo"));

		// when
		final DBClass clazz = dbDataView().findClass("foo");

		// then
		assertThat(clazz.getIdentifier().getLocalName(), equalTo("foo"));
		assertThat(clazz.isSuperclass(), equalTo(false));
		assertThat(clazz.getParent(), nullValue());
		assertThat(clazz.holdsHistory(), equalTo(true));
	}

	@Test
	public void superClassCreatedAndRead() {
		// given
		dbDataView().create(newSuperClass("foo"));

		// when
		final DBClass superClass = dbDataView().findClass("foo");

		// then
		assertThat(superClass.getIdentifier().getLocalName(), equalTo("foo"));
		assertThat(superClass.isSuperclass(), equalTo(true));
		assertThat(superClass.getParent(), nullValue());
		assertThat(isEmpty(superClass.getLeaves()), equalTo(true));
	}

	@Test
	public void simpleClassCreatedAndRead() {
		// given
		dbDataView().create(newSimpleClass("foo"));

		// when
		final DBClass clazz = dbDataView().findClass("foo");

		// then
		assertThat(clazz.getIdentifier().getLocalName(), equalTo("foo"));
		assertThat(clazz.isSuperclass(), equalTo(false));
		assertThat(clazz.getParent(), nullValue());
		assertThat(clazz.holdsHistory(), equalTo(false));
	}

	@Test
	public void classesHierarchyAfterReloadingClasses() {
		// given
		DBClass superClass = dbDataView().create(newSuperClass("superClass"));
		DBClass subClassA = dbDataView().create(newClass("subClassA", superClass));
		DBClass subClassB = dbDataView().create(newClass("subClassB", superClass));

		// when
		superClass = dbDataView().findClass(superClass.getId());
		subClassA = dbDataView().findClass(subClassA.getId());
		subClassB = dbDataView().findClass(subClassB.getId());

		assertThat(superClass.isSuperclass(), is(true));
		assertThat(superClass.isAncestorOf(subClassA), is(true));
		assertThat(superClass.isAncestorOf(subClassB), is(true));
		assertThat(namesOf(superClass.getChildren()), hasItem("subClassA"));
		assertThat(namesOf(superClass.getChildren()), hasItem("subClassB"));
		assertThat(subClassA.getParent().getIdentifier().getLocalName(), equalTo("superClass"));
		assertThat(subClassB.getParent().getIdentifier().getLocalName(), equalTo("superClass"));
	}

	@Test
	public void classesHierarchyUpdatedAfterDeletingClassButWithoutReloadingClasses() {
		// given
		final DBClass root = dbDataView().create(newClass("root"));
		final DBClass sub = dbDataView().create(newClass("sub", root));

		// when
		dbDataView().delete(sub);

		// then
		assertThat(namesOf(root.getChildren()), not(hasItem(sub.getIdentifier().getLocalName())));
	}

	@Test
	public void classesHierarchyUpdatedAfterDeletingClassAndAfterReloadingClasses() {
		// given
		final DBClass _root = dbDataView().create(newSuperClass("root"));
		final DBClass _sub = dbDataView().create(newClass("sub", _root));

		// when
		dbDataView().delete(_sub);
		final DBClass root = dbDataView().findClass(_root.getIdentifier().getLocalName());

		// then
		assertThat(namesOf(root.getChildren()), not(hasItem(_sub.getIdentifier().getLocalName())));
	}

	@Test(expected = Exception.class)
	public void cannotCreateTwoClassesWithSameName() {
		// given
		dbDataView().create(newClass("foo"));

		// when
		dbDataView().create(newClass("foo"));

		// then
		// ...
	}

	@Test
	public void leafClassesCanBeFetchedFromRootClass() {
		// given
		final DBClass root = dbDataView().create(newSuperClass("root"));
		final DBClass subClassA = dbDataView().create(newSuperClass("subClassA", root));
		final DBClass subClassB = dbDataView().create(newSuperClass("subClassB", root));
		final DBClass leafA = dbDataView().create(newClass("leafA", subClassA));
		final DBClass leafB = dbDataView().create(newClass("leafB", subClassA));

		// when
		final Iterable<DBClass> leaves = dbDataView().findClass(root.getIdentifier().getLocalName()).getLeaves();

		// then
		assertThat(size(leaves), is(equalTo(2)));
		assertThat(leaves, hasItems(leafA, leafB));
	}

	@Test
	public void leafClassesCanBeFetchedFromAnyNonRootSuperClass() {
		// given
		final DBClass root = dbDataView().create(newSuperClass("root"));
		final DBClass subClassA = dbDataView().create(newSuperClass("subClassA", root));
		final DBClass subClassB = dbDataView().create(newSuperClass("subClassB", root));
		final DBClass leafA = dbDataView().create(newClass("leafA", subClassA));
		final DBClass leafB = dbDataView().create(newClass("leafB", subClassA));

		// when
		final Iterable<DBClass> leaves = dbDataView().findClass(subClassA.getIdentifier().getLocalName()).getLeaves();

		// then
		assertThat(size(leaves), is(equalTo(2)));
		assertThat(leaves, hasItems(leafA, leafB));
	}

	@Test
	public void superClassShouldNotBeALeaf() {
		// given
		dbDataView().create(newSuperClass("root"));

		// when
		final Iterable<DBClass> items = dbDataView().findClass("root").getLeaves();

		// then
		assertThat(isEmpty(items), is(true));
	}

}
