package unit.serializers.translationtable;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory.Output;
import org.cmdbuild.servlets.json.serializers.translations.table.ViewTranslationSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.TableEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ViewSerializationTest {

	View view1 = mock(View.class);
	View view2 = mock(View.class);
	Collection<View> views = Lists.newArrayList(view1, view2);

	@Before
	public void setup() {
		doReturn("a").when(view1).getName();
		doReturn("b").when(view2).getName();
		doReturn("B").when(view1).getDescription();
		doReturn("A").when(view2).getDescription();
	}

	@Test
	public void typeViewCreatesViewSerializer() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withType("view") //
				.withOutput(Output.TABLE) //
				.build();

		// when
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// then
		assertTrue(serializer instanceof ViewTranslationSerializer);
	}

	@Test
	public void nullSortersSetSortersToDefault() throws Exception {

		// given
		final ViewLogic viewLogic = mock(ViewLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(views).when(viewLogic).fetchViewsOfAllTypes();

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withViewLogic(viewLogic) //
				.withTranslationLogic(translationLogic) //
				.withType("view") //
				.withOutput(Output.TABLE) //
				.withActiveOnly(true) //
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
		final ViewLogic viewLogic = mock(ViewLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(views).when(viewLogic).fetchViewsOfAllTypes();

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withViewLogic(viewLogic) //
				.withLookupStore(null) //
				.withTranslationLogic(translationLogic) //
				.withType("view") //
				.withOutput(Output.TABLE) //
				.withActiveOnly(true) //
				.withSorters(null) //
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
	public void orderViewsByNameIsNotYetSupported() throws Exception {
		// given
		final ViewLogic viewLogic = mock(ViewLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(views).when(viewLogic).fetchViewsOfAllTypes();

		final JSONArray sorters = new JSONArray();
		final JSONObject classSorter = new JSONObject();
		classSorter.put("element", "view");
		classSorter.put("field", "name");
		classSorter.put("direction", "asc");
		sorters.put(classSorter);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withViewLogic(viewLogic) //
				.withLookupStore(null) //
				.withTranslationLogic(translationLogic) //
				.withType("view") //
				.withOutput(Output.TABLE) //
				.withActiveOnly(true) //
				.withSorters(sorters) //
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
