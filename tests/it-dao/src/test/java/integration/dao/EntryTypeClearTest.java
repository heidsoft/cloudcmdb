package integration.dao;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.newDomain;
import static utils.IntegrationTestUtils.withIdentifier;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.junit.After;
import org.junit.Test;

import utils.IntegrationTestBase;

public class EntryTypeClearTest extends IntegrationTestBase {

	private DBClass clazz;
	private DBDomain domain;

	/**
	 * We don't want default rollback driver here.
	 */
	@Override
	protected DBDriver createTestDriver() {
		return createBaseDriver();
	}

	@Test
	public void allCardsCleared() throws Exception {
		// given
		clazz = dbDataView().create(newClass("foo"));
		dbDataView().createCardFor(clazz).setCode("this").save();
		dbDataView().createCardFor(clazz).setCode("is").save();
		dbDataView().createCardFor(clazz).setCode("a").save();
		dbDataView().createCardFor(clazz).setCode("test").save();

		// when
		dbDataView().clear(clazz);
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.run();

		// then
		assertThat(result.size(), equalTo(0));
	}

	@Test
	public void allRelationsCleared() throws Exception {
		// given
		clazz = dbDataView().create(newClass("foo"));
		domain = dbDataView().create(newDomain(withIdentifier("bar"), clazz, clazz));
		final CMCard card0 = dbDataView().createCardFor(clazz).setCode("baz").save();
		final CMCard card1 = dbDataView().createCardFor(clazz).setCode("baz").save();
		final CMCard card2 = dbDataView().createCardFor(clazz).setCode("baz").save();
		dbDataView().createRelationFor(domain).setCard1(card0).setCard2(card1).save();
		dbDataView().createRelationFor(domain).setCard1(card0).setCard2(card2).save();

		// when
		dbDataView().clear(domain);
		final Alias DST_ALIAS = NameAlias.as("DST");
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(domain)) //
				.from(clazz) //
				.join(anyClass(), as(DST_ALIAS), over(domain)) //
				.run();

		// then
		assertThat(result.size(), equalTo(0));
	}

	@After
	public void deleteEntryTypes() throws Exception {
		if (domain != null) {
			dbDataView().clear(domain);
			dbDataView().delete(domain);
		}
		if (clazz != null) {
			dbDataView().clear(clazz);
			dbDataView().delete(clazz);
		}
	}

}
