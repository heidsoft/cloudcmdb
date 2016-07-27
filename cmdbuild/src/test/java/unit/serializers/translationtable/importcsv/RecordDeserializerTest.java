package unit.serializers.translationtable.importcsv;

import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.ClassDescription;
import org.cmdbuild.servlets.json.serializers.translations.csv.read.DefaultRecordDeserializer;
import org.cmdbuild.servlets.json.serializers.translations.csv.read.ErrorListener;
import org.cmdbuild.servlets.json.serializers.translations.csv.read.ErrorNotifier;
import org.cmdbuild.servlets.json.serializers.translations.csv.read.SafeRecordDeserializer;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;
import org.junit.Test;

import com.google.common.collect.Maps;

public class RecordDeserializerTest {

	@Test
	public void validRecordForClassReturnsTranslationObject() throws Exception {
		// given
		final Map<String, Object> map = Maps.newHashMap();
		map.put("identifier", "class.Application.description");
		map.put("description", "description of the class 'Application'");
		map.put("default", "Application");
		map.put("it", "Applicazione");
		map.put("ja", "アプリケーション");
		final CsvTranslationRecord record = new CsvTranslationRecord(map);

		// when
		final TranslationObject object = SafeRecordDeserializer.of(a(DefaultRecordDeserializer.newInstance() //
				.withRecord(record) //
				.withNotifier(ErrorNotifier.THROWS_EXCEPTION))) //
				.deserialize();

		// then
		assertTrue(object instanceof ClassDescription);
		assertTrue(ClassDescription.class.cast(object).getName().equals("Application"));
		assertTrue(ClassDescription.class.cast(object).getTranslations().size() == 2);
	}

	@Test
	public void extraColumnsAreInterpretedAsLanguages() throws Exception {
		// given
		final Map<String, Object> map = Maps.newHashMap();
		map.put("identifier", "class.Application.description");
		map.put("description", "description of the class 'Application'");
		map.put("default", "Application");
		map.put("foo", "bar");
		map.put("it", "Applicazione");
		map.put("ja", "アプリケーション");
		final CsvTranslationRecord record = new CsvTranslationRecord(map);

		// when
		final TranslationObject object = SafeRecordDeserializer.of(a(DefaultRecordDeserializer.newInstance() //
				.withRecord(record) //
				.withNotifier(ErrorNotifier.THROWS_EXCEPTION))) //
				.deserialize();

		// then
		assertTrue(object instanceof ClassDescription);
		assertTrue(ClassDescription.class.cast(object).getName().equals("Application"));
		assertTrue(ClassDescription.class.cast(object).getTranslations().size() == 3);
		assertTrue(ClassDescription.class.cast(object).getTranslations().get("foo").equals("bar"));
	}

	@Test
	public void missingColumnsReturnsInvalidObjectAndOneFailure() throws Exception {
		// given
		final Map<String, Object> map = Maps.newHashMap();
		map.put("description", "description of the class 'Application'");
		map.put("default", "Application");
		map.put("it", "Applicazione");
		map.put("ja", "アプリケーション");
		final CsvTranslationRecord record = new CsvTranslationRecord(map);
		final ErrorListener listener = new ErrorListener() {

			Map<TranslationSerialization, Throwable> failures = Maps.newHashMap();

			@Override
			public void handleError(final TranslationSerialization input, final Throwable throwable) {
				failures.put(input, throwable);
			}

			@Override
			public Map<TranslationSerialization, Throwable> getFailures() {
				return failures;
			}
		};

		// when
		final TranslationObject translationObject = SafeRecordDeserializer.of(a(DefaultRecordDeserializer.newInstance() //
				.withRecord(record) //
				.withNotifier(ErrorNotifier.THROWS_EXCEPTION))) //
				.withErrorListener(listener) //
				.deserialize();
		// then
		assertTrue(!translationObject.isValid());
		assertTrue(listener.getFailures().size() == 1);
		assertTrue(listener.getFailures().get(record).getLocalizedMessage().equals("unsupported identifier 'null'"));
	}

	@Test
	public void failureMessageWhenWrongIdentifierSyntax() throws Exception {
		// given
		final Map<String, Object> map = Maps.newHashMap();
		map.put("description", "description of the class 'Application'");
		map.put("default", "Application");
		map.put("it", "Applicazione");
		map.put("ja", "アプリケーション");
		map.put("identifier", "bbb.ccc");
		final CsvTranslationRecord record = new CsvTranslationRecord(map);
		final ErrorListener listener = new ErrorListener() {

			Map<TranslationSerialization, Throwable> failures = Maps.newHashMap();

			@Override
			public void handleError(final TranslationSerialization input, final Throwable throwable) {
				failures.put(input, throwable);
			}

			@Override
			public Map<TranslationSerialization, Throwable> getFailures() {
				return failures;
			}
		};

		// when
		SafeRecordDeserializer.of(a(DefaultRecordDeserializer.newInstance() //
				.withRecord(record) //
				.withNotifier(ErrorNotifier.THROWS_EXCEPTION))) //
				.withErrorListener(listener) //
				.deserialize();
		// then
		assertTrue(listener.getFailures().get(record).getLocalizedMessage().equals("unsupported identifier 'bbb.ccc'"));
	}

	@Test
	public void failureMessageWhenUnsupportedTypeFieldPair() throws Exception {
		// given
		final Map<String, Object> map = Maps.newHashMap();
		map.put("description", "description of the class 'Application'");
		map.put("default", "Application");
		map.put("it", "Applicazione");
		map.put("ja", "アプリケーション");
		map.put("identifier", "class.Application.wrongfield");
		final CsvTranslationRecord record = new CsvTranslationRecord(map);
		final ErrorListener listener = new ErrorListener() {

			Map<TranslationSerialization, Throwable> failures = Maps.newHashMap();

			@Override
			public void handleError(final TranslationSerialization input, final Throwable throwable) {
				failures.put(input, throwable);
			}

			@Override
			public Map<TranslationSerialization, Throwable> getFailures() {
				return failures;
			}
		};

		// when
		SafeRecordDeserializer.of(a(DefaultRecordDeserializer.newInstance() //
				.withRecord(record) //
				.withNotifier(ErrorNotifier.THROWS_EXCEPTION))) //
				.withErrorListener(listener) //
				.deserialize();
		// then
		assertTrue(listener.getFailures().get(record).getLocalizedMessage().equals("unsupported field 'wrongfield'"));
	}

}
