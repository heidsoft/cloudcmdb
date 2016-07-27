package unit.serializers.cardEditMode;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.logic.privileges.CardEditMode.PERSISTENCE_TO_LOGIC;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.cmdbuild.logic.privileges.CardEditMode;
import org.junit.Test;

public class PersistenceToLogic {

	@Test
	public void nullStringProducesDefaultElement() throws Exception {

		// given
		final String persistenceString = null;

		// when
		final CardEditMode cardEditMode = PERSISTENCE_TO_LOGIC.apply(persistenceString);

		// then
		assertThat(cardEditMode, equalTo(CardEditMode.ALLOW_ALL));

	}

	@Test
	public void emptyStringProducesDefaultElement() throws Exception {
		// given
		final String persistenceString = EMPTY;

		// when
		final CardEditMode cardEditMode = PERSISTENCE_TO_LOGIC.apply(persistenceString);

		// then
		assertThat(cardEditMode, equalTo(CardEditMode.ALLOW_ALL));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void badFormattedStringThrowsException() throws Exception {

		// given
		final String persistenceString = "pippo";

		// when
		PERSISTENCE_TO_LOGIC.apply(persistenceString);
	}

	@Test
	public void deserializeDisableClone() throws Exception {
		// given
		final String persistenceString = "create=true,modify=true,clone=false,remove=true";

		// when
		final CardEditMode cardEditMode = PERSISTENCE_TO_LOGIC.apply(persistenceString);

		// then
		assertTrue(!cardEditMode.isAllowClone());
		assertTrue(cardEditMode.isAllowCreate());
		assertTrue(cardEditMode.isAllowUpdate());
		assertTrue(cardEditMode.isAllowRemove());
	}

}
