package integration.dao;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue.beginsWith;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue.endsWith;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue.gt;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue.lt;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.not;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.isNull;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.codeAttribute;
import static utils.IntegrationTestUtils.descriptionAttribute;
import static utils.IntegrationTestUtils.newClass;

import java.util.Iterator;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

public class WhereQueryTest extends IntegrationTestBase {

	private static final String CLASS_NAME = "foo";

	private DBClass clazz;

	@Before
	public void createData() throws Exception {
		clazz = dbDataView().create(newClass(CLASS_NAME));
	}

	@Test
	public void singleCardRespectingSimpleCondition() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("baz") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(codeAttribute(clazz), eq("foo"))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.getCode(), equalTo("foo"));
	}

	@Test
	public void singleCardRespectingBothConditions() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("bar") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("baz") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(and( //
						condition(codeAttribute(clazz), eq("foo")), //
						condition(descriptionAttribute(clazz), eq("bar")))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		final CMCard card = row.getCard(clazz);
		assertThat((String) card.getCode(), equalTo("foo"));
		assertThat((String) card.getDescription(), equalTo("bar"));
	}

	@Test
	public void moreCardsRespectingOrConditions() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("baz") //
				.setDescription("baz") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(or( //
						condition(codeAttribute(clazz), eq("foo")), //
						condition(codeAttribute(clazz), eq("bar")))) //
				.run();

		// then
		assertThat(result.size(), equalTo(2));
		final Iterator<CMQueryRow> rows = result.iterator();
		assertThat((String) rows.next().getCard(clazz).getCode(), equalTo("foo"));
		assertThat((String) rows.next().getCard(clazz).getCode(), equalTo("bar"));
	}

	@Test
	public void singleCardRespectingNotCondition() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(not( //
				condition(codeAttribute(clazz), eq("foo")))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("bar"));
	}

	@Test
	public void noResultWithMoreThanTwoAndConditions() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(and( //
						condition(codeAttribute(clazz), eq("foo")), //
						condition(descriptionAttribute(clazz), eq("foo")), //
						condition(codeAttribute(clazz), eq("bar")))) //
				.run();

		// then
		assertThat(result.size(), equalTo(0));
	}

	@Test
	public void singleResultWithCompositeConditions() throws Exception {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(and(condition(codeAttribute(clazz), eq("foo")), //
						not(condition(descriptionAttribute(clazz), eq("fake"))))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("foo"));
	}

	/**
	 * GREATHER THAN
	 */
	@Test
	public void whereClausesWithGreatherThanOperatorWork() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(codeAttribute(clazz), gt("f"))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("foo"));
	}

	/**
	 * LESS THAN
	 */
	@Test
	public void whereClausesWithLessThanOperatorWork() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(codeAttribute(clazz), lt("e"))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("bar"));
	}

	/**
	 * CONTAINS
	 */
	@Test
	public void shouldRetrieveCardsWhoseDescriptionContainsAValue() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("description_for_foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(descriptionAttribute(clazz), contains("PTioN"))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("foo"));
	}

	/**
	 * DOES NOT CONTAIN
	 */
	@Test
	public void shouldRetrieveCardsWhoseDescriptionDoesNotContainAValue() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("description_for_foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(not(condition(descriptionAttribute(clazz), contains("PTioN")))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("bar"));
	}

	/**
	 * BEGINS WITH
	 */
	@Test
	public void shouldRetrieveCardsWhoseDescriptionBeginsWithAValue() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("description_for_foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(descriptionAttribute(clazz), beginsWith("dESc"))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("foo"));
	}

	/**
	 * DOES NOT BEGIN WITH
	 */
	@Test
	public void shouldRetrieveCardsWhoseDescriptionDoesNotBeginWithAValue() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("description_for_foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(not(condition(descriptionAttribute(clazz), beginsWith("dESc")))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("bar"));
	}

	/**
	 * ENDS WITH
	 */
	@Test
	public void shouldRetrieveCardsWhoseDescriptionEndsWithAValue() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("description_for_foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(descriptionAttribute(clazz), endsWith("_fOo"))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("foo"));
	}

	/**
	 * DOES NOT END WITH
	 */
	@Test
	public void shouldRetrieveCardsWhoseDescriptionDoesNotEndWithAValue() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("description_for_foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(not(condition(descriptionAttribute(clazz), endsWith("_FOO")))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("bar"));
	}

	/**
	 * IS NULL
	 */
	@Test
	public void shouldRetrieveCardsWhoseCodeIsNull() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode(null) //
				.setDescription("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(codeAttribute(clazz), isNull())) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getDescription(), equalTo("foo"));
	}

	/**
	 * IS NOT NULL
	 */
	@Test
	public void shouldRetrieveCardsWhoseCodeIsNotNull() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode(null) //
				.setDescription("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(not(condition(codeAttribute(clazz), isNull()))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getDescription(), equalTo("bar"));
	}

	/**
	 * IN
	 */
	@Test
	public void shouldRetrieveCardsWhoseCodeIsContainedInASetOfValues() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(codeAttribute(clazz), in("foo", "loo", "moo", "poo"))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("foo"));
	}

	/**
	 * NOT IN
	 */
	@Test
	public void shouldRetrieveCardsWhoseCodeIsNotContainedInASetOfValues() {
		// given
		dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.setDescription("foo") //
				.save();
		dbDataView().createCardFor(clazz) //
				.setCode("bar") //
				.setDescription("bar") //
				.save();

		// when
		final CMQueryRow row = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(not(condition(codeAttribute(clazz), in("foo", "loo", "goo", "poo")))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		// then
		assertThat((String) row.getCard(clazz).getCode(), equalTo("bar"));
	}

}
