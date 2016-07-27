package integration.logic.data.filter.relations;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static integration.logic.data.DataDefinitionLogicTest.newDomain;
import static integration.logic.data.filter.relations.Utils.anyRelated;
import static integration.logic.data.filter.relations.Utils.anyRelation;
import static integration.logic.data.filter.relations.Utils.card;
import static integration.logic.data.filter.relations.Utils.forClass;
import static integration.logic.data.filter.relations.Utils.notRelated;
import static integration.logic.data.filter.relations.Utils.query;
import static integration.logic.data.filter.relations.Utils.sortBy;
import static integration.logic.data.filter.relations.Utils.withDomain;
import static integration.logic.data.filter.relations.Utils.withSourceClass;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import integration.logic.data.filter.FilteredCardsFixture;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.model.data.Card;
import org.junit.Test;

public class RelationFilterTest extends FilteredCardsFixture {

	private CMClass foo;
	private CMClass bar;
	private CMClass baz;

	private CMDomain foo_bar;
	private CMDomain foo_baz;

	@Override
	protected void createClassesAndDomains() {
		// classes
		foo = dataDefinitionLogic().createOrUpdate(a(newClass("foo") //
				.thatIsActive(true)));
		bar = dataDefinitionLogic().createOrUpdate(a(newClass("bar") //
				.thatIsActive(true)));
		baz = dataDefinitionLogic().createOrUpdate(a(newClass("baz") //
				.thatIsActive(true)));

		// domains
		foo_bar = dataDefinitionLogic().create(a(newDomain("foo_bar") //
				.withIdClass1(foo.getId()) //
				.withIdClass2(bar.getId()) //
				.thatIsActive(true)));
		foo_baz = dataDefinitionLogic().create(a(newDomain("foo_baz") //
				.withIdClass1(foo.getId()) //
				.withIdClass2(baz.getId()) //
				.thatIsActive(true)));
	}

	@Override
	protected void initializeDatabaseData() {
		// nothing to do
	}

	@Override
	protected void clearAndDeleteClassesAndDomains() {
		dbDataView().clear(foo_baz);
		dbDataView().delete(foo_baz);

		dbDataView().clear(foo_bar);
		dbDataView().delete(foo_bar);

		dbDataView().clear(baz);
		dbDataView().delete(baz);

		dbDataView().clear(bar);
		dbDataView().delete(bar);

		dbDataView().clear(foo);
		dbDataView().delete(foo);
	}

	@Test
	public void fetchingCardsWithAnyRelationOverSingleDomainButNothingIsFound() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard bar_1 = dbDataView().createCardFor(bar).setCode("bar_1").save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_1).save();

		// when
		final Iterable<Card> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(anyRelation(withDomain(foo_baz), withSourceClass(foo))));

		// then
		assertThat(isEmpty(cards), equalTo(true));
	}

	@Test
	public void fetchingCardsWithAnyRelationOverSingleDomainOneCardThatHaveTwoRelationsIsFound() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard bar_1 = dbDataView().createCardFor(bar).setCode("bar_1").save();
		final CMCard bar_2 = dbDataView().createCardFor(bar).setCode("bar_2").save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_1).save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_2).save();

		// when
		final Iterable<Card> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(anyRelation(withDomain(foo_bar), withSourceClass(foo))));

		// then
		assertThat(size(cards), equalTo(1));
	}

	@Test
	public void fetchingCardsWithAnyRelationOverSingleDomainTwoCardsAreFoundAndSorted() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard foo_2 = dbDataView().createCardFor(foo).setCode("foo_2").save();
		final CMCard bar_1 = dbDataView().createCardFor(bar).setCode("bar_1").save();
		final CMCard bar_2 = dbDataView().createCardFor(bar).setCode("bar_2").save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_1).save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_2).setCard2(bar_2).save();

		// when
		final Iterable<Card> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(anyRelation(withDomain(foo_bar), withSourceClass(foo)), sortBy("Code", "DESC")));

		// then
		assertThat(size(cards), equalTo(2));
		assertThat((String) get(cards, 0).getAttribute("Code"), equalTo("foo_2"));
		assertThat((String) get(cards, 1).getAttribute("Code"), equalTo("foo_1"));
	}

	@Test
	public void fetchingCardsWithRelationOverSingleDomainLookingForSpecificDestinationCards() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard foo_2 = dbDataView().createCardFor(foo).setCode("foo_2").save();
		final CMCard foo_3 = dbDataView().createCardFor(foo).setCode("foo_3").save();
		final CMCard bar_1 = dbDataView().createCardFor(bar).setCode("bar_1").save();
		final CMCard bar_2 = dbDataView().createCardFor(bar).setCode("bar_2").save();
		final CMCard bar_3 = dbDataView().createCardFor(bar).setCode("bar_3").save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_1).save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_2).setCard2(bar_2).save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_3).setCard2(bar_3).save();

		// when
		final Iterable<Card> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(anyRelated(withDomain(foo_bar), withSourceClass(foo), card(bar_1), card(bar_3))));

		// then
		assertThat(size(cards), equalTo(2));
		assertThat((String) get(cards, 0).getAttribute("Code"), equalTo("foo_1"));
		assertThat((String) get(cards, 1).getAttribute("Code"), equalTo("foo_3"));
	}

	@Test
	public void fetchingCardsWithNoRelationOverSingleDomain() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard foo_2 = dbDataView().createCardFor(foo).setCode("foo_2").save();
		final CMCard bar_1 = dbDataView().createCardFor(bar).setCode("bar_1").save();
		final CMCard baz_1 = dbDataView().createCardFor(baz).setCode("baz_1").save();
		dbDataView().createRelationFor(foo_bar).setCard1(foo_1).setCard2(bar_1).save();
		dbDataView().createRelationFor(foo_baz).setCard1(foo_2).setCard2(baz_1).save();

		// when
		final Iterable<Card> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(notRelated(withDomain(foo_baz), withSourceClass(foo))));

		// then
		assertThat(size(cards), equalTo(1));
		assertThat((String) get(cards, 0).getAttribute("Code"), equalTo("foo_1"));
	}

}
