package unit.serializers.translationtable;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.serializers.translations.table.ClassTranslationSerializer;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory.Output;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.ParentEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ClassSerializationTest {

	CMClass class1 = mock(CMClass.class);
	CMClass class2 = mock(CMClass.class);
	Collection<CMClass> classes = Lists.newArrayList(class1, class2);

	@Before
	public void setup() {
		doReturn("a").when(class1).getName();
		doReturn("b").when(class2).getName();
		doReturn("B").when(class1).getDescription();
		doReturn("A").when(class2).getDescription();
	}

	@Test
	public void typeClassCreatesClassSerializer() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(null) //
				.withOutput(Output.TABLE) //
				.withTranslationLogic(translationLogic) //
				.withType("class") //
				.withActiveOnly(true) //
				.withSorters(null) //
				.build();

		// when
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// then
		assertTrue(serializer instanceof ClassTranslationSerializer);
	}

	@Test
	public void nullSortersSetSortersToDefault() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(classes).when(dataLogic).findClasses(true);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(null) //
				.withOutput(Output.TABLE) //
				.withTranslationLogic(translationLogic).withType("class") //
				.withActiveOnly(true) //
				.withSorters(null) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<ParentEntry> elements = Lists.newArrayList((Collection<ParentEntry>) response);
		assertTrue(elements.size() == 2);
		assertTrue(elements.get(0).getName().equals("b"));
		assertTrue(elements.get(1).getName().equals("a"));
	}

	@Test
	public void serializationHasOnlyDescriptionField() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(classes).when(dataLogic).findClasses(true);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withOutput(Output.TABLE) //
				.withLookupStore(null) //
				.withTranslationLogic(translationLogic).withType("class") //
				.withActiveOnly(true) //
				.withSorters(null) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<ParentEntry> elements = Lists.newArrayList((Collection<ParentEntry>) response);
		final ParentEntry firstClass = elements.get(0);
		final List<EntryField> fields = Lists.newArrayList(firstClass.getFields());
		assertTrue(fields.size() == 1);
		assertTrue(fields.get(0).getName().equalsIgnoreCase("description"));
	}

	@Test
	public void ifTheClassHasNoAttributesThenSerializationHasEmptyAttributesNode() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(classes).when(dataLogic).findClasses(true);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withOutput(Output.TABLE) //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(null) //
				.withTranslationLogic(translationLogic).withType("class") //
				.withActiveOnly(true) //
				.withSorters(null) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<ParentEntry> elements = Lists.newArrayList((Collection<ParentEntry>) response);
		final ParentEntry firstClass = elements.get(0);
		assertTrue(Iterables.isEmpty(firstClass.getChildren()));
	}

	@Test
	public void orderClassesByName() throws Exception {
		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(classes).when(dataLogic).findClasses(true);

		final JSONArray sorters = new JSONArray();
		final JSONObject classSorter = new JSONObject();
		classSorter.put("element", "class");
		classSorter.put("field", "name");
		classSorter.put("direction", "asc");
		sorters.put(classSorter);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(null) //
				.withOutput(Output.TABLE) //
				.withTranslationLogic(translationLogic).withType("class") //
				.withActiveOnly(true) //
				.withSorters(sorters) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<ParentEntry> elements = Lists.newArrayList((Collection<ParentEntry>) response);
		assertTrue(elements.size() == 2);
		assertTrue(elements.get(0).getName().equals("a"));
		assertTrue(elements.get(1).getName().equals("b"));
	}

	@Test
	public void wrongSortersSyntaxGetIgnored() throws Exception {
		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(classes).when(dataLogic).findClasses(true);

		final JSONArray sorters = new JSONArray();
		final JSONObject classSorter = new JSONObject();
		classSorter.put("foo", "bar");
		sorters.put(classSorter);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(null) //
				.withOutput(Output.TABLE) //
				.withTranslationLogic(translationLogic).withType("class") //
				.withActiveOnly(true) //
				.withSorters(sorters) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize();

		// then
		final List<ParentEntry> elements = Lists.newArrayList((Collection<ParentEntry>) response);
		assertTrue(elements.size() == 2);
		assertTrue(elements.get(0).getName().equals("b"));
		assertTrue(elements.get(1).getName().equals("a"));
	}

}
