package integration.logic.data.filter;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.newSuperClass;
import static utils.IntegrationTestUtils.newTextAttribute;

import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.Constants.FilterOperator;
import org.cmdbuild.model.data.Card;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

public class SuperclassFilteredCardsTest extends FilteredCardsFixture {

	private DBClass root;
	private DBClass superNotRoot;
	private DBClass leafOfSuperNotRoot;
	private DBClass leafOfRoot;
	private DBClass anotherLeafOfRoot;

	@Override
	protected void initializeDatabaseData() {
		root = dbDataView().create(newSuperClass("root"));
		dbDataView().createAttribute(newTextAttribute("foo", root));
		superNotRoot = dbDataView().create(newSuperClass("superNotRoot", root));
		leafOfSuperNotRoot = dbDataView().create(newClass("leafOfSuperNotRoot", superNotRoot));
		leafOfRoot = dbDataView().create(newClass("leafOfRoot", root));
		anotherLeafOfRoot = dbDataView().create(newClass("anotherLeafOfRoot", root));

		dbDataView().createCardFor(leafOfSuperNotRoot) //
				.set("foo", leafOfSuperNotRoot.getName()) //
				.save();
		dbDataView().createCardFor(leafOfRoot) //
				.set("foo", leafOfRoot.getName()) //
				.save();
		dbDataView().createCardFor(anotherLeafOfRoot) //
				.set("foo", anotherLeafOfRoot.getName()) //
				.save();
	}

	@Override
	@After
	public void tearDown() {
		dbDataView().clear(anotherLeafOfRoot);
		dbDataView().delete(anotherLeafOfRoot);

		dbDataView().clear(leafOfRoot);
		dbDataView().delete(leafOfRoot);

		dbDataView().clear(leafOfSuperNotRoot);
		dbDataView().delete(leafOfSuperNotRoot);

		dbDataView().clear(superNotRoot);
		dbDataView().delete(superNotRoot);

		dbDataView().clear(root);
		dbDataView().delete(root);
	}

	@Test
	public void cardsFilteredUsingSuperclassAttribute() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter("foo", FilterOperator.EQUAL, "leafOfSuperNotRoot");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> cards = dataAccessLogic.fetchCards(root.getName(), queryOptions).elements();

		// then
		assertThat(size(cards), equalTo(1));
		assertThat(get(cards, 0).getAttribute("foo"), equalTo((Object) "leafOfSuperNotRoot"));
	}

}
