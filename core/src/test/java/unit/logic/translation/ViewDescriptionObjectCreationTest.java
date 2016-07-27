package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.logic.translation.object.ViewDescription;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ViewDescriptionObjectCreationTest {

	private static final String viewName = "viewName";
	private static final String field = "Description";
	private static final String lang = "it";
	private static final String translatedValueDescription = "nomeVista";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedValueDescription);

	@Test
	public void forDescriptionFieldReturnsValidObject() {
		// given
		final Converter converter = ViewConverter //
				.of(field)//
				.withIdentifier(viewName).withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create();

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ViewDescription.class.cast(translationObject).getName().equals(viewName));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedValueDescription));
	}

}
