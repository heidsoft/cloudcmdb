package unit.dao.driver.postgres.query;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.replacePattern;
import static org.cmdbuild.dao.query.clause.OrderByClause.Direction.ASC;
import static org.cmdbuild.dao.query.clause.OrderByClause.Direction.DESC;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.cmdbuild.dao.driver.postgres.query.NumberingAndOrderPartCreator;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.junit.Before;
import org.junit.Test;

public class NumberingAndOrderPartCreatorTest {

	public FromClause fromClause;
	public List<OrderByClause> orderByClauses;
	public WhereClause conditionOnNumberedQuery;

	@Before
	public void setUp() throws Exception {
		fromClause = mock(FromClause.class);
		final CMClass target = mock(CMClass.class);
		doReturn(target) //
				.when(fromClause).getType();
		final NameAlias alias = name("dummy");
		doReturn(alias) //
				.when(fromClause).getAlias();

		orderByClauses = newArrayList();
		orderByClauses.add(new OrderByClause(attribute(alias, "foo"), DESC));
		orderByClauses.add(new OrderByClause(attribute(alias, "bar"), ASC));

		conditionOnNumberedQuery = condition(attribute(alias, "baz"), eq(42));
	}

	@Test
	public void queryWithNothing() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(normalize(part), equalTo("" //
				+ "*** THIS IS THE MAIN ONE *** " //
				+ "ORDER BY \"_dummy_Id\""));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).skipDefaultOrdering();
		verifyNoMoreInteractions(querySpecs);
	}

	@Test
	public void queryWithNothingSkippingOrdering() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).skipDefaultOrdering();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(normalize(part), equalTo("" //
				+ "*** THIS IS THE MAIN ONE ***"));

		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).skipDefaultOrdering();
		verifyNoMoreInteractions(querySpecs);
	}

	@Test
	public void queryWithCount() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).count();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(normalize(part), equalTo("" //
				+ "SELECT *, count(*) over() AS _dummy__RowsCount " //
				+ "FROM (*** THIS IS THE MAIN ONE ***) AS main " //
				+ "ORDER BY \"_dummy_Id\""));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).skipDefaultOrdering();
		verifyNoMoreInteractions(querySpecs);
	}

	@Test
	public void queryWithCountAndCustomOrder() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).count();
		doReturn(orderByClauses) //
				.when(querySpecs).getOrderByClauses();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(normalize(part), equalTo("" //
				+ "SELECT *, count(*) over() AS _dummy__RowsCount " //
				+ "FROM (*** THIS IS THE MAIN ONE ***) AS main " //
				+ "ORDER BY \"dummy#foo\" DESC, \"dummy#bar\" ASC, \"_dummy_Id\""));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verifyNoMoreInteractions(querySpecs);
	}

	@Test
	public void queryWithRowNumbers() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).numbered();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(normalize(part), equalTo("" //
				+ "SELECT *, row_number() OVER (ORDER BY \"_dummy_Id\") AS _dummy__Row " //
				+ "FROM (*** THIS IS THE MAIN ONE ***) AS main"));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).getConditionOnNumberedQuery();
		verify(querySpecs, times(1)).skipDefaultOrdering();
		verifyNoMoreInteractions(querySpecs);
	}

	@Test
	public void queryWithRowNumbersAndCustomOrder() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).numbered();
		doReturn(orderByClauses) //
				.when(querySpecs).getOrderByClauses();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(
				normalize(part),
				equalTo("" //
						+ "SELECT *, row_number() OVER (ORDER BY \"dummy#foo\" DESC, \"dummy#bar\" ASC, \"_dummy_Id\") AS _dummy__Row " //
						+ "FROM (*** THIS IS THE MAIN ONE ***) AS main"));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).getConditionOnNumberedQuery();
		verifyNoMoreInteractions(querySpecs);
	}

	@Test
	public void queryWithRowNumbersAndConditionOnNumeration() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).numbered();
		doReturn(conditionOnNumberedQuery) //
				.when(querySpecs).getConditionOnNumberedQuery();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(normalize(part), equalTo("" //
				+ "SELECT * FROM (SELECT *, row_number() OVER (ORDER BY \"_dummy_Id\") AS _dummy__Row " //
				+ "FROM (*** THIS IS THE MAIN ONE ***) AS main) AS numbered WHERE \"_dummy_Id\" = 42"));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).getConditionOnNumberedQuery();
		verify(querySpecs, times(1)).skipDefaultOrdering();
		verifyNoMoreInteractions(querySpecs);
	}

	@Test
	public void queryWithRowNumbersCustomOrderAndConditionOnNumeration() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).numbered();
		doReturn(orderByClauses) //
				.when(querySpecs).getOrderByClauses();
		doReturn(conditionOnNumberedQuery) //
				.when(querySpecs).getConditionOnNumberedQuery();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(
				normalize(part),
				equalTo("" //
						+ "SELECT * " //
						+ "FROM (" //
						+ "SELECT *, row_number() OVER (ORDER BY \"dummy#foo\" DESC, \"dummy#bar\" ASC, \"_dummy_Id\") AS _dummy__Row " //
						+ "FROM (*** THIS IS THE MAIN ONE ***) AS main" + ") AS numbered " + "WHERE \"_dummy_Id\" = 42"));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).getConditionOnNumberedQuery();
		verifyNoMoreInteractions(querySpecs);
	}

	@Test
	public void queryWithCountAndRowNumbers() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).count();
		doReturn(true) //
				.when(querySpecs).numbered();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(
				normalize(part),
				equalTo("" //
						+ "SELECT *, count(*) over() AS _dummy__RowsCount, row_number() OVER (ORDER BY \"_dummy_Id\") AS _dummy__Row " //
						+ "FROM (*** THIS IS THE MAIN ONE ***) AS main"));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).getConditionOnNumberedQuery();
		verify(querySpecs, times(1)).skipDefaultOrdering();
		verifyNoMoreInteractions(querySpecs);
	}

	@Test
	public void queryWithCountRowNumbersAndCustomOrder() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).count();
		doReturn(true) //
				.when(querySpecs).numbered();
		doReturn(orderByClauses) //
				.when(querySpecs).getOrderByClauses();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(
				normalize(part),
				equalTo("" //
						+ "SELECT *, count(*) over() AS _dummy__RowsCount, row_number() OVER (ORDER BY \"dummy#foo\" DESC, \"dummy#bar\" ASC, \"_dummy_Id\") AS _dummy__Row " //
						+ "FROM (*** THIS IS THE MAIN ONE ***) AS main"));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).getConditionOnNumberedQuery();
		verifyNoMoreInteractions(querySpecs);
	}

	@Test
	public void queryWithCountRowNumbersAndConditionOnNumeration() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).count();
		doReturn(true) //
				.when(querySpecs).numbered();
		doReturn(conditionOnNumberedQuery) //
				.when(querySpecs).getConditionOnNumberedQuery();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(
				normalize(part),
				equalTo("" //
						+ "SELECT * " //
						+ "FROM (" //
						+ "SELECT *, count(*) over() AS _dummy__RowsCount, row_number() OVER (ORDER BY \"_dummy_Id\") AS _dummy__Row " //
						+ "FROM (*** THIS IS THE MAIN ONE ***) AS main" //
						+ ") AS numbered " //
						+ "WHERE \"_dummy_Id\" = 42"));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).getConditionOnNumberedQuery();
		verify(querySpecs, times(1)).skipDefaultOrdering();
		verifyNoMoreInteractions(querySpecs);

	}

	@Test
	public void queryWithCountRowNumbersCustomOrderAndConditionOnNumeration() throws Exception {
		// given
		final QuerySpecs querySpecs = mock(QuerySpecs.class);
		doReturn(fromClause) //
				.when(querySpecs).getFromClause();
		doReturn(true) //
				.when(querySpecs).count();
		doReturn(true) //
				.when(querySpecs).numbered();
		doReturn(orderByClauses) //
				.when(querySpecs).getOrderByClauses();
		doReturn(conditionOnNumberedQuery) //
				.when(querySpecs).getConditionOnNumberedQuery();
		final StringBuilder input = new StringBuilder("*** THIS IS THE MAIN ONE ***");

		// when
		final String part = new NumberingAndOrderPartCreator(querySpecs, input).getPart();

		// then
		assertThat(
				normalize(part),
				equalTo("" //
						+ "SELECT * " //
						+ "FROM (" //
						+ "SELECT *, count(*) over() AS _dummy__RowsCount, row_number() OVER (ORDER BY \"dummy#foo\" DESC, \"dummy#bar\" ASC, \"_dummy_Id\") AS _dummy__Row " //
						+ "FROM (*** THIS IS THE MAIN ONE ***) AS main" //
						+ ") AS numbered " //
						+ "WHERE \"_dummy_Id\" = 42"));

		verify(querySpecs, atLeast(1)).getFromClause();
		verify(querySpecs, atLeast(1)).count();
		verify(querySpecs, atLeast(1)).numbered();
		verify(querySpecs, atLeast(1)).getOrderByClauses();
		verify(querySpecs, times(1)).getConditionOnNumberedQuery();
		verifyNoMoreInteractions(querySpecs);
	}

	/*
	 * Utilities
	 */

	private String normalize(final String value) {
		return replacePattern(value, "[\\s]+", " ");
	}

}
