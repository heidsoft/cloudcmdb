package org.cmdbuild.workflow.widget;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.model.widget.Grid.DEFAULT_DISABLE_ADD_ROW;
import static org.cmdbuild.model.widget.Grid.DEFAULT_DISABLE_DELETE_ROW;
import static org.cmdbuild.model.widget.Grid.DEFAULT_DISABLE_IMPORT_FROM_CSV;
import static org.cmdbuild.model.widget.Grid.DEFAULT_ENTRY_SEPARATOR;
import static org.cmdbuild.model.widget.Grid.DEFAULT_KEYVALUE_SEPARATOR;
import static org.cmdbuild.model.widget.Grid.DEFAULT_MAP_SEPARATOR;
import static org.cmdbuild.model.widget.Grid.DEFAULT_PRESETS_TYPE;
import static org.cmdbuild.model.widget.Grid.DEFAULT_READONLY;
import static org.cmdbuild.model.widget.Grid.DEFAULT_REQUIRED;
import static org.cmdbuild.model.widget.Grid.DEFAULT_SERIALIZATION;
import static org.cmdbuild.model.widget.Grid.DEFAULT_WRITE_ON_ADVANCE;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.model.widget.Grid;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class GridWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "grid";

	public static final String CLASS_NAME = "ClassName";
	public static final String CARD_SEPARATOR = "CardSeparator";
	public static final String ATTRIBUTE_SEPARATOR = "AttributeSeparator";
	public static final String KEY_VALUE_SEPARATOR = "KeyValueSeparator";
	public static final String SERIALIZATION_TYPE = "SerializationType";
	public static final String WRITE_ON_ADVANCE = "WriteOnAdvance";
	public static final String PRESETS = "Presets";
	public static final String PRESETS_TYPE = "PresetsType";
	public static final String DISABLE_ADD_ROW = "DisableAddRow";
	public static final String DISABLE_IMPORT_FROM_CSV = "DisableImportFromCsv";
	public static final String DISABLE_DELETE_ROW = "DisableDeleteRow";
	public static final String READ_ONLY = "ReadOnly";
	public static final String REQUIRED = "Required";

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, CLASS_NAME, CARD_SEPARATOR, ATTRIBUTE_SEPARATOR,
			KEY_VALUE_SEPARATOR, SERIALIZATION_TYPE, WRITE_ON_ADVANCE, PRESETS, PRESETS_TYPE, DISABLE_ADD_ROW,
			DISABLE_IMPORT_FROM_CSV, DISABLE_DELETE_ROW, READ_ONLY, REQUIRED };

	public GridWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final WidgetDefinition definition) {
		final String className = readString(definition.get(CLASS_NAME));
		Validate.notEmpty(className, "{} is required", CLASS_NAME);
		final Grid widget = new Grid();
		widget.setClassName(className);
		widget.setOutputName(readString(definition.get(OUTPUT_KEY)));
		widget.setCardSeparator(defaultIfBlank(readString(definition.get(CARD_SEPARATOR)), DEFAULT_MAP_SEPARATOR));
		widget.setAttributeSeparator(defaultIfBlank(readString(definition.get(ATTRIBUTE_SEPARATOR)),
				DEFAULT_ENTRY_SEPARATOR));
		widget.setKeyValueSeparator(defaultIfBlank(readString(definition.get(KEY_VALUE_SEPARATOR)),
				DEFAULT_KEYVALUE_SEPARATOR));
		widget.setSerializationType(defaultIfBlank(readString(definition.get(SERIALIZATION_TYPE)),
				DEFAULT_SERIALIZATION));
		widget.setWriteOnAdvance(readBoolean(definition.get(WRITE_ON_ADVANCE), DEFAULT_WRITE_ON_ADVANCE));
		widget.setPresets(readString(definition.get(PRESETS)));
		widget.setPresetsType(defaultIfBlank(readString(definition.get(PRESETS_TYPE)), DEFAULT_PRESETS_TYPE));
		widget.setDisableAddRow(readBoolean(definition.get(DISABLE_ADD_ROW), DEFAULT_DISABLE_ADD_ROW));
		widget.setDisableImportFromCsv(readBoolean(definition.get(DISABLE_IMPORT_FROM_CSV),
				DEFAULT_DISABLE_IMPORT_FROM_CSV));
		widget.setDisableDeleteRow(readBoolean(definition.get(DISABLE_DELETE_ROW), DEFAULT_DISABLE_DELETE_ROW));
		widget.setReadOnly(readBoolean(definition.get(READ_ONLY), DEFAULT_READONLY));
		widget.setRequired(readBoolean(definition.get(REQUIRED), DEFAULT_REQUIRED));
		widget.setVariables(extractUnmanagedParameters(definition, KNOWN_PARAMETERS));
		return widget;
	}

}
