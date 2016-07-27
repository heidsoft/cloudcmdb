package integration.dao;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.ClassHistory.history;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.withIdentifier;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.junit.Ignore;
import org.junit.Test;

import utils.IntegrationTestBase;

public class HistoryQueryTest extends IntegrationTestBase {

	@Ignore
	@Test
	public void shuoldRetrieveOnlyUpdatedCards() {
		// given
		final CMClass foo = dbDataView().create(newClass(withIdentifier("Foo")));
		CMCard card = dbDataView().createCardFor(foo).setCode("foo").save();
		card = dbDataView().update(card).setCode("bar").save();
		card = dbDataView().update(card).setCode("baz").save();

		// when
		final CMQueryResult result = dbDataView().select(anyAttribute(foo)) //
				.from(history(foo)) //
				.where(condition(attribute(foo, "CurrentId"), eq(card.getId()))) //
				.run();

		for (final CMQueryRow row : result) {
			final CMCard c = row.getCard(foo);
		}

		// then
		assertThat(result.size(), equalTo(2));
	}

}
