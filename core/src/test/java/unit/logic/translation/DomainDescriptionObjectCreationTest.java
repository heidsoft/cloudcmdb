package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.logic.translation.converter.DomainConverter;
import org.cmdbuild.logic.translation.object.DomainDescription;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class DomainDescriptionObjectCreationTest {

	private static final String domainName = "BuildingFloor";
	private static final String field = "Description";
	private static final String lang = "it";
	private static final String translatedDomainName = "EdificioPiano";
	private static final String invalidfield = "invalidfield";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedDomainName);

	@Test
	public void forDescriptionFieldReturnsValidObject() {
		// given
		final Converter converter = DomainConverter //
				.of(field) //
				.withIdentifier(domainName) //
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create();

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(DomainDescription.class.cast(translationObject).getName().equals(domainName));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedDomainName));
	}

	@Test
	public void converterIsCaseInsensitiveForTheField() {
		// given
		final Converter converter = DomainConverter //
				.of(field) //
				.withIdentifier(domainName) //
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create();

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(DomainDescription.class.cast(translationObject).getName().equals(domainName));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedDomainName));
	}

	@Test
	public void unsupportedFieldGeneratesNotValidConverter() {
		// given

		// when
		final Converter converter = DomainConverter //
				.of(invalidfield);

		// then
		assertTrue(!converter.isValid());
	}

	@Test
	public void invalidConverterThrowsException() {
		// given
		final Converter converter = DomainConverter.of(invalidfield);
		Exception thrown = null;

		// when
		try {
			converter.create();
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
		final Converter converter = DomainConverter.of(field);

		// when
		final TranslationObject translationObject = converter //
				.withIdentifier(domainName) //
				.create();

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(DomainDescription.class.cast(translationObject).getName().equals(domainName));
	}

}
