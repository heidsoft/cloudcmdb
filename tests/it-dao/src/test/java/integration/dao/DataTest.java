package integration.dao;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.newSuperClass;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Test;

import utils.IntegrationTestBase;

public class DataTest extends IntegrationTestBase {

	private static final Object CODE_VALUE = "foo";
	private static final Object DESCRIPTION_VALUE = "bar";

	@Test
	public void cardsCanBeAdded() {
		final DBClass parent = dbDataView().findClass(Constants.BASE_CLASS_NAME);
		final DBClass newClass = dbDataView().create(newClass("foo", parent));
		final CMCard newCard = dbDataView().createCardFor(newClass) //
				.setCode(CODE_VALUE) //
				.setDescription(DESCRIPTION_VALUE) //
				.save();

		assertThat(newCard.getId(), is(notNullValue()));
		assertThat(newCard.getCode(), equalTo(CODE_VALUE));
		assertThat(newCard.getDescription(), equalTo(DESCRIPTION_VALUE));
	}

	@Test(expected = Exception.class)
	public void cardsCannotBeAddedInSuperclass() {
		final DBClass newClass = dbDataView().create(newSuperClass("foo"));
		DBCard.newInstance(dbDriver(), newClass) //
				.setCode(CODE_VALUE) //
				.setDescription(DESCRIPTION_VALUE) //
				.save();
	}

}
