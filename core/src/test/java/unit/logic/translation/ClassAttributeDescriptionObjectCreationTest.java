package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.logic.translation.object.ClassAttributeDescription;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ClassAttributeDescriptionObjectCreationTest {

	private static final String entryType = "class";
	private static final String classname = "Building";
	private static final String attributename = "Name";
	private static final String field = "Description";
	private static final String lang = "it";
	private static final String translatedAttributename = "Descrizione";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedAttributename);

	@Test
	public void forDescriptionFieldReturnsValidObject() {
		// given
		final Converter converter = AttributeConverter //
				.of(entryType, field)//
				.withTranslations(map).withIdentifier(attributename).withOwner(classname);

		// when
		final TranslationObject translationObject = converter.create();

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeDescription.class.cast(translationObject).getClassName().equals(classname));
		assertTrue(ClassAttributeDescription.class.cast(translationObject).getName().equals(attributename));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedAttributename));
	}

	@Test
	public void converterIsCaseInsensitiveForTheField() {
		// given
		final Converter converter = AttributeConverter//
				.of(entryType, "dEscRiptION") //
				.withIdentifier(attributename) //
				.withOwner(classname) //
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create();

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeDescription.class.cast(translationObject).getClassName().equals(classname));
		assertTrue(ClassAttributeDescription.class.cast(translationObject).getName().equals(attributename));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedAttributename));
	}

	@Test
	public void unsupportedFieldGeneratesNotValidConverter() {
		// given
		final String invalidfield = "invalidfield";

		// when
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, invalidfield);

		// then
		assertTrue(!converter.isValid());
	}

	@Test
	public void invalidConverterThrowsException() {
		// given
		final String invalidfield = "invalidfield";
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, invalidfield);
		Exception thrown = null;

		// when
		try {
			converter.withIdentifier(attributename) //
					.withOwner(classname).create();
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
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, field);

		// when
		final TranslationObject translationObject = converter //
				.withIdentifier(attributename) //
				.withOwner(classname) //
				.create();

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeDescription.class.cast(translationObject).getClassName().equals(classname));
	}
}
