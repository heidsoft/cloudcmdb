package unit.serializers.translationtable;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.serializers.translations.table.ReportTranslationSerializer;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory.Output;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.TableEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ReportSerializationTest {

	Report report1 = mock(Report.class);
	Report report2 = mock(Report.class);
	Iterable<Report> reports = Lists.newArrayList(report1, report2);

	@Before
	public void setup() {
		doReturn("a").when(report1).getCode();
		doReturn("b").when(report2).getCode();
		doReturn("B").when(report1).getDescription();
		doReturn("A").when(report2).getDescription();
	}

	@Test
	public void typeReportCreatesReportSerializer() throws Exception {

		// given
		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withType("report") //
				.withOutput(Output.TABLE) //
				.build();

		// when
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// then
		assertTrue(serializer instanceof ReportTranslationSerializer);
	}

	@Test
	public void nullSortersSetSortersToDefault() throws Exception {

		// given
		final ReportStore reportStore = mock(ReportStore.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(reports).when(reportStore).findReportsByType(ReportFactory.ReportType.CUSTOM);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withReportStore(reportStore).withTranslationLogic(translationLogic) //
				.withType("report") //
				.withOutput(Output.TABLE) //
				.withSorters(null) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<TableEntry> elements = Lists.newArrayList((Collection<TableEntry>) response);
		assertTrue(elements.size() == 2);
		assertTrue(elements.get(0).getName().equals("b"));
		assertTrue(elements.get(1).getName().equals("a"));
	}

	@Test
	public void serializationHasOnlyDescriptionField() throws Exception {

		// given
		final ReportStore reportStore = mock(ReportStore.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(reports).when(reportStore).findReportsByType(ReportFactory.ReportType.CUSTOM);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withReportStore(reportStore) //
				.withTranslationLogic(translationLogic) //
				.withType("report") //
				.withOutput(Output.TABLE) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<TableEntry> elements = Lists.newArrayList((Collection<TableEntry>) response);
		final TableEntry firstClass = elements.get(0);
		final List<EntryField> fields = Lists.newArrayList(firstClass.getFields());
		assertTrue(fields.size() == 1);
		assertTrue(fields.get(0).getName().equalsIgnoreCase("description"));
	}

	@Test
	public void orderByCodeIsNotYetSupportedHenceIgnored() throws Exception {
		// given
		final ReportStore reportStore = mock(ReportStore.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(reports).when(reportStore).findReportsByType(ReportFactory.ReportType.CUSTOM);

		final JSONArray sorters = new JSONArray();
		final JSONObject classSorter = new JSONObject();
		classSorter.put("element", "report");
		classSorter.put("field", "code");
		classSorter.put("direction", "asc");
		sorters.put(classSorter);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withReportStore(reportStore) //
				.withTranslationLogic(translationLogic) //
				.withType("report") //
				.withOutput(Output.TABLE) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<TableEntry> elements = Lists.newArrayList((Collection<TableEntry>) response);
		assertTrue(elements.size() == 2);
		assertTrue(elements.get(0).getName().equals("b"));
		assertTrue(elements.get(1).getName().equals("a"));
	}

}
