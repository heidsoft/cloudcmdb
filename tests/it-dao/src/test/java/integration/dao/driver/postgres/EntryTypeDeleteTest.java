package integration.dao.driver.postgres;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.newSimpleClass;
import static utils.IntegrationTestUtils.newTextAttribute;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.junit.After;
import org.junit.Test;

import utils.IntegrationTestBase;

public class EntryTypeDeleteTest extends IntegrationTestBase {

	private DBClass clazz;

	/**
	 * We don't want default rollback driver here.
	 */
	@Override
	protected DBDriver createTestDriver() {
		return createBaseDriver();
	}

	@Test
	public void cardForStandarClassSuccessfullyDeleted() {
		// given
		final DBClass parent = dbDataView().findClass(Constants.BASE_CLASS_NAME);
		clazz = dbDataView().create(newClass("foo", parent));
		final DBCard card = (DBCard) dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.save();

		// when
		// FIXME use the data view
		dbDriver().delete(card);
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.run();

		// then
		assertThat(result.size(), equalTo(0));
	}

	@Test
	public void cardForSimpleClassSuccessfullyDeleted() {
		// given
		clazz = dbDataView().create(newSimpleClass("foo"));
		dbDataView().createAttribute(newTextAttribute("Code", clazz));
		final DBCard card = (DBCard) dbDataView().createCardFor(clazz) //
				.setCode("foo") //
				.save();

		// when
		// FIXME use the data view
		dbDriver().delete(card);
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.run();

		// then
		assertThat(result.size(), equalTo(0));
	}

	@After
	public void deleteEntryTypes() throws Exception {
		dbDataView().clear(clazz);
		dbDataView().delete(clazz);
	}

}
