package unit.serializers.translationtable;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.serializers.translations.table.ProcessTranslationSerializer;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory.Output;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.ParentEntry;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ProcessClassSerializationTest {

	CMClass class1 = mock(CMClass.class);
	CMClass class2 = mock(CMClass.class);
	CMClass class3 = mock(CMClass.class);
	CMClass activity = mock(CMClass.class);
	Collection<CMClass> classes = Lists.newArrayList(class1, class2, class3);
	DataAccessLogic dataLogic = mock(DataAccessLogic.class);
	TranslationLogic translationLogic = mock(TranslationLogic.class);

	@Before
	public void setup() {
		doReturn("a").when(class1).getName();
		doReturn("b").when(class2).getName();
		doReturn("c").when(class3).getName();
		doReturn("B").when(class1).getDescription();
		doReturn("A").when(class2).getDescription();
		doReturn("1").when(class3).getDescription();
		doReturn(activity).when(dataLogic).findClass(Constants.BASE_PROCESS_CLASS_NAME);

		doReturn(classes).when(dataLogic).findAllClasses();
		doReturn(classes).when(dataLogic).findClasses(true);
	}

	@Test
	public void typeProcessCreatesProcessSerializer() throws Exception {
		// given
		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withType("process") //
				.withOutput(Output.TABLE) //
				.build();

		// when
		final TranslationSectionSerializer serializer = factory.createSerializer();

		// then
		assertTrue(serializer instanceof ProcessTranslationSerializer);
	}

	@Test
	public void nullSortersSetSortersToDefault() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withTranslationLogic(translationLogic) //
				.withType("process") //
				.withOutput(Output.TABLE) //
				.withActiveOnly(true) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		doReturn(classes).when(dataLogic).findAllClasses();
		doReturn(activity).when(dataLogic).findClass(Constants.BASE_PROCESS_CLASS_NAME);
		doReturn(true).when(activity).isAncestorOf(class1);
		doReturn(true).when(activity).isAncestorOf(class2);
		doReturn(false).when(activity).isAncestorOf(class3);

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
				.withLookupStore(null) //
				.withTranslationLogic(translationLogic) //
				.withType("process") //
				.withOutput(Output.TABLE) //
				.withActiveOnly(true) //
				.withSorters(null) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		doReturn(classes).when(dataLogic).findAllClasses();
		doReturn(activity).when(dataLogic).findClass(Constants.BASE_PROCESS_CLASS_NAME);
		doReturn(true).when(activity).isAncestorOf(class1);
		doReturn(true).when(activity).isAncestorOf(class2);
		doReturn(false).when(activity).isAncestorOf(class3);

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
	public void ifTheProcessHasNoAttributesThenSerializationHasEmptyAttributesNode() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(classes).when(dataLogic).findClasses(true);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(null) //
				.withTranslationLogic(translationLogic) //
				.withType("process") //
				.withOutput(Output.TABLE) //
				.withActiveOnly(true) //
				.withSorters(null) //
				.build();
		final TranslationSectionSerializer serializer = factory.createSerializer();

		doReturn(classes).when(dataLogic).findAllClasses();
		doReturn(activity).when(dataLogic).findClass(Constants.BASE_PROCESS_CLASS_NAME);
		doReturn(true).when(activity).isAncestorOf(class1);
		doReturn(true).when(activity).isAncestorOf(class2);
		doReturn(false).when(activity).isAncestorOf(class3);

		// when
		final Object response = serializer.serialize();

		// then
		final List<ParentEntry> elements = Lists.newArrayList((Collection<ParentEntry>) response);
		final ParentEntry firstClass = elements.get(0);
		assertTrue(Iterables.isEmpty(firstClass.getChildren()));
	}
}
