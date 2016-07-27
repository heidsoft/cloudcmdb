package integration.dao;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;
import static org.cmdbuild.dao.entrytype.DBIdentifier.fromName;
import static org.cmdbuild.dao.entrytype.DBIdentifier.fromNameAndNamespace;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.junit.Ignore;
import org.junit.Test;

import utils.IntegrationTestBase;

import com.google.common.base.Function;

public class NamespaceTest extends IntegrationTestBase {

	@Test
	public void classesWithSameNameCanBeCreatedInDifferentNamespaces() throws Exception {
		// given

		// when
		dbDataView().create(newClass(fromName("Foo")));
		dbDataView().create(newClass(fromNameAndNamespace("Foo", "Bar Baz")));

		// then
		// ... no error
	}

	@Test
	public void classCreatedAndDeleted() throws Exception {
		// given
		final int size = size(dbDataView().findClasses());
		final DBClass clazz = dbDataView().create(newClass(fromNameAndNamespace("Foo", "Bar")));

		// when
		dbDataView().delete(clazz);

		// then
		assertThat(size(dbDataView().findClasses()), equalTo(size));
	}

	@Test
	public void allClassesInAllNamespacesAreFound() throws Exception {
		// given
		dbDataView().create(newClass(fromName("Foo")));
		dbDataView().create(newClass(fromNameAndNamespace("Foo", "Bar Baz")));

		// when
		final Iterable<DBClass> classes = dbDataView().findClasses();
		final Iterable<CMIdentifier> identifiers = transform(classes, new Function<CMClass, CMIdentifier>() {
			@Override
			public CMIdentifier apply(final CMClass input) {
				return input.getIdentifier();
			}
		});

		// then
		assertThat(identifiers, hasItems(fromName("Foo"), fromNameAndNamespace("Foo", "Bar Baz")));
	}

	@Ignore
	@Test
	public void successfullSimpleQuery() throws Exception {
		// given
		final DBClass clazz = dbDataView().create(newClass(fromNameAndNamespace("Foo", "Bar")));
		dbDataView().createCardFor(clazz) //
				.setCode("test") //
				.save();

		// when
		final CMQueryResult result = dbDataView().select(anyAttribute(clazz)) //
				.from(clazz) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run();

		// then
		assertThat(result.size(), equalTo(1));
		assertThat(result.getOnlyRow().getCard(clazz).getCode(), equalTo((Object) "test"));
	}

}
