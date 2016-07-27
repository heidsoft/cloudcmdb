package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.logic.translation.object.ClassDescription;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ClassDescriptionObjectCreationTest {

	private static final String classname = "Building";
	private static final String field = "Description";
	private static final String lang = "it";
	private static final String translatedClassname = "Edificio";
	private static final String invalidfield = "invalidfield";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedClassname);

	@Test
	public void forDescriptionFieldReturnsValidObject() {
		// given
		final Converter converter = ClassConverter //
				.of(field).withTranslations(map);

		// when
		final TranslationObject translationObject = converter.withIdentifier(classname).create();

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassDescription.class.cast(translationObject).getName().equals(classname));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedClassname));
	}

	@Test
	public void converterIsCaseInsensitiveForTheField() {
		// given
		final Converter converter = ClassConverter //
				.of(field).withTranslations(map);

		// when
		final TranslationObject translationObject = converter.withIdentifier(classname).create();

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassDescription.class.cast(translationObject).getName().equals(classname));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedClassname));
	}

	@Test
	public void unsupportedFieldGeneratesNotValidConverter() {
		// given

		// when
		final Converter converter = ClassConverter //
				.of(invalidfield);

		// then
		assertTrue(!converter.isValid());
	}

	@Test
	public void invalidConverterThrowsException() {
		// given
		final Converter converter = ClassConverter.of(invalidfield);
		Exception thrown = null;

		// when
		try {
			converter.withIdentifier(classname).create();
		} catch (final Exception e) {
			thrown = e;
		}

		// then
		assertNotNull(thrown);
		assertTrue(thrown instanceof UnsupportedOperationException);
	}

	@Test
	public void createConverterForReading() {
		// given
		final Converter converter = ClassConverter.of(field);

		// when
		final TranslationObject translationObject = converter.withIdentifier(classname).create();
		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassDescription.class.cast(translationObject).getName().equals(classname));
	}

}
