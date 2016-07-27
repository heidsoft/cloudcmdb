package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.logic.translation.converter.LookupConverter;
import org.cmdbuild.logic.translation.object.LookupDescription;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class LookupDescriptionObjectCreationTest {

	private static final String lookupValueUuid = "uuid";
	private static final String field = "Description";
	private static final String lang = "it";
	private static final String translatedValueDescription = "Rosso";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedValueDescription);

	@Test
	public void forDescriptionFieldReturnsValidObject() {
		// given
		final Converter converter = LookupConverter //
				.of(field)//
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter //
				.withIdentifier(lookupValueUuid) //
				.create();

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(LookupDescription.class.cast(translationObject).getName().equals(lookupValueUuid));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedValueDescription));
	}

}
