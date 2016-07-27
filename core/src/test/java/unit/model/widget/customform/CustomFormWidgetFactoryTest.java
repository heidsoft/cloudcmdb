package unit.model.widget.customform;

import static java.util.Arrays.asList;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.ADD_DISABLED;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.ATTRIBUTES_SEPARATOR;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.CLASS_ATTRIBUTES;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.CLASS_MODEL;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.CLONE_DISABLED;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.DATA_TYPE;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.DEFAULT_ATTRIBUTES_SEPARATOR;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.DEFAULT_KEY_VALUE_SEPARATOR;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.DEFAULT_ROWS_SEPARATOR;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.DELETE_DISABLED;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.EXPORT_DISABLED;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.FORM_MODEL;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.FUNCTION_ATTRIBUTES;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.FUNCTION_DATA;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.FUNCTION_MODEL;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.IMPORT_DISABLED;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.KEY_VALUE_SEPARATOR;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.MODEL_TYPE;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.MODIFY_DISABLED;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.RAW_DATA;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.READ_ONLY;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.ROWS_SEPARATOR;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.SERIALIZATION_TYPE;
import static org.cmdbuild.model.widget.customform.CustomFormWidgetFactory.TEMPLATE_RESOLVER;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;
import java.util.List;

import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.model.widget.customform.Attribute;
import org.cmdbuild.model.widget.customform.Attribute.Filter;
import org.cmdbuild.model.widget.customform.CustomForm;
import org.cmdbuild.model.widget.customform.CustomForm.Capabilities;
import org.cmdbuild.model.widget.customform.CustomForm.Serialization;
import org.cmdbuild.model.widget.customform.CustomForm.TextConfiguration;
import org.cmdbuild.model.widget.customform.CustomFormWidgetFactory;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;

public class CustomFormWidgetFactoryTest {

	private TemplateRepository templateRespository;
	private Notifier notifier;
	private CMDataView dataView;
	private MetadataStoreFactory metadataStoreFactory;
	private CustomFormWidgetFactory widgetFactory;

	@Before
	public void setUp() throws Exception {
		templateRespository = mock(TemplateRepository.class);
		notifier = mock(Notifier.class);
		dataView = mock(CMDataView.class);
		metadataStoreFactory = mock(MetadataStoreFactory.class);
		widgetFactory = new CustomFormWidgetFactory(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void undefinedModelProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = MODEL_TYPE + "=\"foo\"";

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void formModelAndMissingDefinitionProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = MODEL_TYPE + "=\"form\"";

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void formModelAndEmptyDefinitionProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void formModelAndBlankDefinitionProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\" \"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void formModelAndInvalidJsonDefinitionProducesWidgetWithDefinitionAsIsAndNoNotification() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo {form:bar} baz\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created.getModel(), equalTo("foo {form:bar} baz"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void formModelAndValidDefinitionProducesAttributesAndNoNotification() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"[{\"name\": \"foo\"},{\"name\": \"bar\"}]\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final List<Attribute> form = readJsonString(created.getModel());
		assertThat(form, not(empty()));
		assertThat(form, hasSize(2));
		assertThat(form.get(0).getName(), equalTo("foo"));
		assertThat(form.get(1).getName(), equalTo("bar"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void definitionForFormConfigurationSuccessfullyParsed() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=" //
				+ "    \"[" //
				+ "        {" //
				+ "            \"type\": \"text\"," //
				+ "            \"name\": \"foo\"," //
				+ "            \"description\": \"this is foo\"," //
				+ "            \"unique\": true," //
				+ "            \"mandatory\": true," //
				+ "            \"writable\": true," //
				+ "            \"precision\": 1," //
				+ "            \"scale\": 2," //
				+ "            \"length\": 3," //
				+ "            \"editorType\": \"lol\"," //
				+ "            \"targetClass\": \"some class\"," //
				+ "            \"lookupType\": \"some lookup type\"," //
				+ "            \"filter\": {" //
				+ "                \"expression\": \"the expression\"," //
				+ "                \"context\": {" //
				+ "                    \"foo\": \"bar\"," //
				+ "                    \"bar\": \"baz\"" //
				+ "                }" //
				+ "            }" //
				+ "        }" //
				+ "    ]\"" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final List<Attribute> form = readJsonString(created.getModel());
		assertThat(form, not(empty()));
		assertThat(form, hasSize(1));
		final Attribute attribute = form.get(0);
		assertThat(attribute.getType(), equalTo("text"));
		assertThat(attribute.getName(), equalTo("foo"));
		assertThat(attribute.getDescription(), equalTo("this is foo"));
		assertThat(attribute.isUnique(), equalTo(true));
		assertThat(attribute.isMandatory(), equalTo(true));
		assertThat(attribute.isWritable(), equalTo(true));
		assertThat(attribute.getPrecision(), equalTo(1L));
		assertThat(attribute.getScale(), equalTo(2L));
		assertThat(attribute.getLength(), equalTo(3L));
		assertThat(attribute.getEditorType(), equalTo("lol"));
		assertThat(attribute.getTargetClass(), equalTo("some class"));
		assertThat(attribute.getTarget().getName(), equalTo("some class"));
		assertThat(attribute.getTarget().getType(), nullValue());
		assertThat(attribute.getLookupType(), equalTo("some lookup type"));
		assertThat(attribute.getFilter(), equalTo(Filter.class.cast(new Filter() {
			{
				setExpression("the expression"); //
				setContext(ChainablePutMap.of(new HashMap<String, String>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz"));
			}
		})));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void definitionForFormConfigurationWithNewTargetSuccessfullyParsed() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=" //
				+ "    \"[" //
				+ "        {" //
				+ "            \"target\": {" //
				+ "                \"name\": \"this is the name\"," //
				+ "                \"type\": \"this is the type\"" //
				+ "            }" //
				+ "        }" //
				+ "    ]\"" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final List<Attribute> form = readJsonString(created.getModel());
		assertThat(form, not(empty()));
		assertThat(form, hasSize(1));
		final Attribute attribute = form.get(0);
		assertThat(attribute.getTargetClass(), equalTo("this is the name"));
		assertThat(attribute.getTarget().getName(), equalTo("this is the name"));
		assertThat(attribute.getTarget().getType(), equalTo("this is the type"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void definitionForFormConfigurationLeavedAsIsWhenContainsTemplate() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo {form:bar} baz\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created.getModel(), equalTo("foo {form:bar} baz"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void classModelAndMissingClassProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"class\"\n" //
				+ CLASS_MODEL + "=\"foo\"\n" //
		;
		doReturn(null) //
				.when(dataView).findClass(any(String.class));

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(dataView).findClass(eq("foo"));
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void attributesForClassSuccessfullyConverted() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"class\"\n" //
				+ CLASS_MODEL + "=\"foo\"\n" //
		;
		final CMClass target = mock(CMClass.class);
		doReturn(target) //
				.when(dataView).findClass(any(String.class));
		final CMAttribute first = attribute(new TextAttributeType(), "bar");
		final CMAttribute second = attribute(new TextAttributeType(), "baz");
		doReturn(asList(first, second)) //
				.when(target).getAttributes();

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final List<Attribute> form = readJsonString(created.getModel());
		assertThat(form, not(empty()));
		assertThat(form, hasSize(2));
		assertThat(form.get(0).getName(), equalTo("bar"));
		// TODO test all attribute conversion
		assertThat(form.get(1).getName(), equalTo("baz"));
		verify(dataView).findClass(eq("foo"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void attributesForClassSuccessfullyFiltered() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"class\"\n" //
				+ CLASS_MODEL + "=\"foo\"\n" //
				+ CLASS_ATTRIBUTES + "=\" bar , baz\"\n" //
		;
		final CMClass target = mock(CMClass.class);
		doReturn(target) //
				.when(dataView).findClass(any(String.class));
		final CMAttribute first = attribute(new TextAttributeType(), "bar");
		final CMAttribute second = attribute(new TextAttributeType(), "lol");
		final CMAttribute third = attribute(new TextAttributeType(), "baz");
		doReturn(asList(first, second, third)) //
				.when(target).getAttributes();

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final List<Attribute> form = readJsonString(created.getModel());
		assertThat(form, not(empty()));
		assertThat(form, hasSize(2));
		assertThat(form.get(0).getName(), equalTo("bar"));
		// TODO test all attribute conversion
		assertThat(form.get(1).getName(), equalTo("baz"));
		verify(dataView).findClass(eq("foo"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void functionModelAndMissingFunctionProducesNoWidgetAndNotification() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"function\"\n" //
				+ FUNCTION_MODEL + "=\"foo\"\n" //
		;
		doReturn(null) //
				.when(dataView).findFunctionByName(any(String.class));

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(dataView).findFunctionByName(eq("foo"));
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void attributesForFunctionSuccessfullyConverted() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"function\"\n" //
				+ FUNCTION_MODEL + "=\"foo\"\n" //
		;
		final CMFunction target = mock(CMFunction.class);
		doReturn(target) //
				.when(dataView).findFunctionByName(any(String.class));
		final CMFunctionParameter first = parameter(new TextAttributeType(), "bar");
		final CMFunctionParameter second = parameter(new TextAttributeType(), "baz");
		doReturn(asList(first, second)) //
				.when(target).getInputParameters();

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final List<Attribute> form = readJsonString(created.getModel());
		assertThat(form, not(empty()));
		assertThat(form, hasSize(2));
		assertThat(form.get(0).getName(), equalTo("bar"));
		// TODO test all attribute conversion
		assertThat(form.get(1).getName(), equalTo("baz"));
		verify(dataView).findFunctionByName(eq("foo"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void attributesForFunctionSuccessfullyFiltered() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"function\"\n" //
				+ FUNCTION_MODEL + "=\"foo\"\n" //
				+ FUNCTION_ATTRIBUTES + "=\" bar , baz\"\n" //
		;
		final CMFunction target = mock(CMFunction.class);
		doReturn(target) //
				.when(dataView).findFunctionByName(any(String.class));
		final CMFunctionParameter first = parameter(new TextAttributeType(), "bar");
		final CMFunctionParameter second = parameter(new TextAttributeType(), "lol");
		final CMFunctionParameter third = parameter(new TextAttributeType(), "baz");
		doReturn(asList(first, second, third)) //
				.when(target).getInputParameters();

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final List<Attribute> form = readJsonString(created.getModel());
		assertThat(form, not(empty()));
		assertThat(form, hasSize(2));
		assertThat(form.get(0).getName(), equalTo("bar"));
		// TODO test all attribute conversion
		assertThat(form.get(1).getName(), equalTo("baz"));
		verify(dataView).findFunctionByName(eq("foo"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void capabilitiesAreNotDisabledByDefault() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final Capabilities capabilities = created.getCapabilities();
		assertThat(capabilities.isReadOnly(), equalTo(false));
		assertThat(capabilities.isAddDisabled(), equalTo(false));
		assertThat(capabilities.isDeleteDisabled(), equalTo(false));
		assertThat(capabilities.isExportDisabled(), equalTo(false));
		assertThat(capabilities.isImportDisabled(), equalTo(false));
		assertThat(capabilities.isModifyDisabled(), equalTo(false));
		assertThat(capabilities.isCloneDisabled(), equalTo(false));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void capabilitiesAreNotDisabledOnlyIfKeyIsPresent() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ READ_ONLY + "\n" //
				+ ADD_DISABLED + "\n" //
				+ DELETE_DISABLED + "\n" //
				+ EXPORT_DISABLED + "\n" //
				+ IMPORT_DISABLED + "\n" //
				+ MODIFY_DISABLED + "\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final Capabilities capabilities = created.getCapabilities();
		assertThat(capabilities.isReadOnly(), equalTo(false));
		assertThat(capabilities.isAddDisabled(), equalTo(false));
		assertThat(capabilities.isDeleteDisabled(), equalTo(false));
		assertThat(capabilities.isExportDisabled(), equalTo(false));
		assertThat(capabilities.isImportDisabled(), equalTo(false));
		assertThat(capabilities.isModifyDisabled(), equalTo(false));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void capabilitiesAreAlwaysDisabledWhenSpecifiedWithoutQuotes() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ READ_ONLY + "=true\n" //
				+ ADD_DISABLED + "=true\n" //
				+ DELETE_DISABLED + "=true\n" //
				+ EXPORT_DISABLED + "=true\n" //
				+ IMPORT_DISABLED + "=true\n" //
				+ MODIFY_DISABLED + "=true\n" //
				+ CLONE_DISABLED + "=true\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final Capabilities capabilities = created.getCapabilities();
		assertThat(capabilities.isReadOnly(), equalTo(false));
		assertThat(capabilities.isAddDisabled(), equalTo(false));
		assertThat(capabilities.isDeleteDisabled(), equalTo(false));
		assertThat(capabilities.isExportDisabled(), equalTo(false));
		assertThat(capabilities.isImportDisabled(), equalTo(false));
		assertThat(capabilities.isModifyDisabled(), equalTo(false));
		assertThat(capabilities.isCloneDisabled(), equalTo(false));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void capabilitiesAreDisabledOnlyWhenSpecified() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ READ_ONLY + "=\"true\"\n" //
				+ ADD_DISABLED + "=\"true\"\n" //
				+ DELETE_DISABLED + "=\"true\"\n" //
				+ EXPORT_DISABLED + "=\"true\"\n" //
				+ IMPORT_DISABLED + "=\"true\"\n" //
				+ MODIFY_DISABLED + "=\"true\"\n" //
				+ CLONE_DISABLED + "=\"true\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final Capabilities capabilities = created.getCapabilities();
		assertThat(capabilities.isReadOnly(), equalTo(true));
		assertThat(capabilities.isAddDisabled(), equalTo(true));
		assertThat(capabilities.isDeleteDisabled(), equalTo(true));
		assertThat(capabilities.isExportDisabled(), equalTo(true));
		assertThat(capabilities.isImportDisabled(), equalTo(true));
		assertThat(capabilities.isModifyDisabled(), equalTo(true));
		assertThat(capabilities.isCloneDisabled(), equalTo(true));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void noSerializationSpecifiedSoTextIsAssumedWithDefaultValues() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final Serialization _serialization = created.getSerialization();
		assertThat(_serialization.getType(), equalTo("text"));
		assertThat(_serialization.getConfiguration(), instanceOf(TextConfiguration.class));
		final TextConfiguration configuration = TextConfiguration.class.cast(_serialization.getConfiguration());
		assertThat(configuration.getKeyValueSeparator(), equalTo(DEFAULT_KEY_VALUE_SEPARATOR));
		assertThat(configuration.getAttributesSeparator(), equalTo(DEFAULT_ATTRIBUTES_SEPARATOR));
		assertThat(configuration.getRowsSeparator(), equalTo(DEFAULT_ROWS_SEPARATOR));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void textSerializationSpecifiedWithNoDetailsReturnsDefaultValues() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ SERIALIZATION_TYPE + "=\"text\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final Serialization _serialization = created.getSerialization();
		assertThat(_serialization.getType(), equalTo("text"));
		assertThat(_serialization.getConfiguration(), instanceOf(TextConfiguration.class));
		final TextConfiguration configuration = TextConfiguration.class.cast(_serialization.getConfiguration());
		assertThat(configuration.getKeyValueSeparator(), equalTo(DEFAULT_KEY_VALUE_SEPARATOR));
		assertThat(configuration.getAttributesSeparator(), equalTo(DEFAULT_ATTRIBUTES_SEPARATOR));
		assertThat(configuration.getRowsSeparator(), equalTo(DEFAULT_ROWS_SEPARATOR));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void textSerializationSpecifiedWithDetails() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ SERIALIZATION_TYPE + "=\"text\"\n" //
				+ KEY_VALUE_SEPARATOR + "=\"1\"\n" //
				+ ATTRIBUTES_SEPARATOR + "=\"2\"\n" //
				+ ROWS_SEPARATOR + "=\"3\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final Serialization _serialization = created.getSerialization();
		assertThat(_serialization.getType(), equalTo("text"));
		assertThat(_serialization.getConfiguration(), instanceOf(TextConfiguration.class));
		final TextConfiguration configuration = TextConfiguration.class.cast(_serialization.getConfiguration());
		assertThat(configuration.getKeyValueSeparator(), equalTo("1"));
		assertThat(configuration.getAttributesSeparator(), equalTo("2"));
		assertThat(configuration.getRowsSeparator(), equalTo("3"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void jsonSerializationDoesNotRequireConfiguration() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ SERIALIZATION_TYPE + "=\"json\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		final Serialization _serialization = created.getSerialization();
		assertThat(_serialization.getType(), equalTo("json"));
		assertThat(_serialization.getConfiguration(), nullValue());
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void dataFromRawSourceReturnsExpressionAsIs() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ DATA_TYPE + "=\"raw\"\n" //
				+ RAW_DATA + "=\"foo bar baz\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created.getData(), equalTo("foo bar baz"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void dataFromJsonRawSourceReturnsExpressionAsIs() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ DATA_TYPE + "=\"raw_json\"\n" //
				+ RAW_DATA + "=\"foo bar baz\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created.getData(), equalTo("foo bar baz"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void dataFromFunctionWithNoTemplateResolverAndMissingFunctionNameProducesNoWidget() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ DATA_TYPE + "=\"function\"\n" //
				+ TEMPLATE_RESOLVER + "=\"false\"\n" //
		;

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void dataFromFunctionWithNoTemplateResolverAndMissingFunctionProducesNoWidget() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ DATA_TYPE + "=\"function\"\n" //
				+ FUNCTION_DATA + "=\"missing\"\n" //
				+ TEMPLATE_RESOLVER + "=\"false\"\n" //
		;
		doReturn(null) //
				.when(dataView).findFunctionByName(any(String.class));

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(dataView).findFunctionByName(eq("missing"));
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void dataFromFunctionWithTemplateResolverAndMissingFunctionNameProducesNoWidget() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ DATA_TYPE + "=\"function\"\n" //
		;
		doReturn(null) //
				.when(dataView).findFunctionByName(any(String.class));

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void dataFromFunctionWithTemplateResolverAndEmptyFunctionNameProducesNoWidget() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ DATA_TYPE + "=\"function\"\n" //
				+ FUNCTION_DATA + "=\"\"\n" //
		;
		doReturn(null) //
				.when(dataView).findFunctionByName(any(String.class));

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created, nullValue());
		verify(notifier).warn(
				eq(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR, widgetFactory
						.getWidgetName())));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	@Test
	public void dataFromFunctionWithTemplateResolver() throws Exception {
		// given
		final String serialization = "" //
				+ MODEL_TYPE + "=\"form\"\n" //
				+ FORM_MODEL + "=\"foo\"\n" //
				+ DATA_TYPE + "=\"function\"\n" //
				+ FUNCTION_DATA + "=\"some function\"\n" //
		;
		doReturn(null) //
				.when(dataView).findFunctionByName(any(String.class));

		// when
		final CustomForm created = (CustomForm) widgetFactory.createWidget(serialization, mock(CMValueSet.class));

		// then
		assertThat(created.getData(), nullValue());
		assertThat(created.getFunctionData(), equalTo("some function"));
		verifyNoMoreInteractions(templateRespository, notifier, dataView, metadataStoreFactory);
	}

	/*
	 * utilities
	 */

	private static CMAttribute attribute(final CMAttributeType<?> type, final String name) {
		final CMAttribute output = mock(CMAttribute.class);
		doReturn(type) //
				.when(output).getType();
		doReturn(name) //
				.when(output).getName();
		return output;
	}

	private static CMFunctionParameter parameter(final CMAttributeType<?> type, final String name) {
		final CMFunctionParameter output = mock(CMFunctionParameter.class);
		doReturn(type) //
				.when(output).getType();
		doReturn(name) //
				.when(output).getName();
		return output;
	}

	private static List<Attribute> readJsonString(final String value) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(value, new TypeReference<List<Attribute>>() {
			});
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
