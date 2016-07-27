package integration.dao;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;

import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.junit.Test;

import utils.IntegrationTestBase;

public class NumberedQueryTest extends IntegrationTestBase {

	@Test
	public void numberedSimple() {
		// given
		final DBClass foo = dbDataView().create(newClass("foo"));
		dbDataView().createCardFor(foo) //
				.setCode("foo") //
				.save();
		dbDataView().createCardFor(foo) //
				.setCode("bar") //
				.save();
		dbDataView().createCardFor(foo) //
				.setCode("baz") //
				.save();

		// when
		final Iterable<CMQueryRow> rows = dbDataView() //
				.select(anyAttribute(foo)) //
				.from(foo) //
				.numbered() //
				.run();

		// then
		assertThat(size(rows), equalTo(3));
		assertThat(get(rows, 0).getCard(foo).getCode(), equalTo((Object) "foo"));
		assertThat(get(rows, 0).getNumber(), equalTo(1L));
		assertThat(get(rows, 1).getCard(foo).getCode(), equalTo((Object) "bar"));
		assertThat(get(rows, 1).getNumber(), equalTo(2L));
		assertThat(get(rows, 2).getCard(foo).getCode(), equalTo((Object) "baz"));
		assertThat(get(rows, 2).getNumber(), equalTo(3L));
	}

	@Test
	public void numberedWithOrdering() {
		// given
		final DBClass foo = dbDataView().create(newClass("foo"));
		dbDataView().createCardFor(foo) //
				.setCode("foo") //
				.save();
		dbDataView().createCardFor(foo) //
				.setCode("bar") //
				.save();
		dbDataView().createCardFor(foo) //
				.setCode("baz") //
				.save();

		// when
		final Iterable<CMQueryRow> rows = dbDataView() //
				.select(anyAttribute(foo)) //
				.from(foo) //
				.orderBy(foo.getCodeAttributeName(), Direction.DESC) //
				.numbered() //
				.run();

		// then
		assertThat(size(rows), equalTo(3));
		assertThat(get(rows, 0).getCard(foo).getCode(), equalTo((Object) "foo"));
		assertThat(get(rows, 0).getNumber(), equalTo(1L));
		assertThat(get(rows, 1).getCard(foo).getCode(), equalTo((Object) "baz"));
		assertThat(get(rows, 1).getNumber(), equalTo(2L));
		assertThat(get(rows, 2).getCard(foo).getCode(), equalTo((Object) "bar"));
		assertThat(get(rows, 2).getNumber(), equalTo(3L));
	}

	@Test
	public void numberedWithFiltering() {
		// given
		final DBClass foo = dbDataView().create(newClass("foo"));
		dbDataView().createCardFor(foo) //
				.setCode("foo") //
				.save();
		dbDataView().createCardFor(foo) //
				.setCode("bar") //
				.save();
		dbDataView().createCardFor(foo) //
				.setCode("baz") //
				.save();

		// when
		final Iterable<CMQueryRow> rows = dbDataView() //
				.select(anyAttribute(foo)) //
				.from(foo) //
				.where(condition(attribute(foo, foo.getCodeAttributeName()), eq("bar"))) //
				.numbered() //
				.run();

		// then
		assertThat(size(rows), equalTo(1));
		assertThat(get(rows, 0).getCard(foo).getCode(), equalTo((Object) "bar"));
		assertThat(get(rows, 0).getNumber(), equalTo(1L));
	}

}
