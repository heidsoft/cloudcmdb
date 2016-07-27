package integration.logic.data.filter.relations;

import static com.google.common.collect.Iterables.size;
import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static integration.logic.data.DataDefinitionLogicTest.newDomain;
import static integration.logic.data.filter.relations.Utils.anyRelated;
import static integration.logic.data.filter.relations.Utils.anyRelation;
import static integration.logic.data.filter.relations.Utils.card;
import static integration.logic.data.filter.relations.Utils.forClass;
import static integration.logic.data.filter.relations.Utils.query;
import static integration.logic.data.filter.relations.Utils.withDomain;
import static integration.logic.data.filter.relations.Utils.withSourceClass;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import integration.logic.data.filter.FilteredCardsFixture;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.model.data.Card;
import org.junit.Ignore;
import org.junit.Test;

public class RelationFilterOverReflexiveDomainTest extends FilteredCardsFixture {

	private CMClass foo;
	private CMDomain foo_foo;

	@Override
	protected void createClassesAndDomains() {
		// classes
		foo = dataDefinitionLogic().createOrUpdate(a(newClass("foo") //
				.thatIsActive(true)));

		// domains
		foo_foo = dataDefinitionLogic().create(a(newDomain("foo_foo") //
				.withIdClass1(foo.getId()) //
				.withIdClass2(foo.getId()) //
				.thatIsActive(true)));
	}

	@Override
	protected void initializeDatabaseData() {
		// nothing to do
	}

	@Override
	protected void clearAndDeleteClassesAndDomains() {
		dbDataView().clear(foo_foo);
		dbDataView().delete(foo_foo);

		dbDataView().clear(foo);
		dbDataView().delete(foo);
	}

	@Test
	@Ignore("TODO")
	public void fetchingCardsWithAnyRelation() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard foo_2 = dbDataView().createCardFor(foo).setCode("foo_2").save();
		dbDataView().createRelationFor(foo_foo).setCard1(foo_1).setCard2(foo_2).save();

		// when
		final Iterable<Card> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(anyRelation(withDomain(foo_foo), withSourceClass(foo))));

		// then
		assertThat(size(cards), equalTo(1));
	}

	@Test
	@Ignore("TODO")
	public void fetchingCardsWithAnyRelationLookingForSpecificDestinationCards() throws Exception {
		// given
		final CMCard foo_1 = dbDataView().createCardFor(foo).setCode("foo_1").save();
		final CMCard foo_2 = dbDataView().createCardFor(foo).setCode("foo_2").save();
		final CMCard foo_3 = dbDataView().createCardFor(foo).setCode("foo_3").save();
		final CMCard foo_4 = dbDataView().createCardFor(foo).setCode("foo_4").save();
		dbDataView().createRelationFor(foo_foo).setCard1(foo_1).setCard2(foo_2).save();
		dbDataView().createRelationFor(foo_foo).setCard1(foo_1).setCard2(foo_3).save();
		dbDataView().createRelationFor(foo_foo).setCard1(foo_1).setCard2(foo_4).save();

		// when
		final Iterable<Card> cards = dataAccessLogic.fetchCards( //
				forClass(foo), //
				query(anyRelated(withDomain(foo_foo), withSourceClass(foo), card(foo_3), card(foo_4))));

		// then
		assertThat(size(cards), equalTo(2));
	}

}
