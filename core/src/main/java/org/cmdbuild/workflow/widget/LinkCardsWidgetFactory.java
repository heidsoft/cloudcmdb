package org.cmdbuild.workflow.widget;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.model.widget.LinkCards.METADATA_SEPARATOR;
import static org.cmdbuild.model.widget.LinkCards.NAME_TYPE_SEPARATOR;

import java.util.Collections;
import java.util.Map;

import org.cmdbuild.model.widget.LinkCards;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

import com.google.common.base.Splitter;

public class LinkCardsWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "linkCards";

	public static final String FILTER = "Filter";
	public static final String CLASS_NAME = "ClassName";
	public static final String DEFAULT_SELECTION = "DefaultSelection";
	public static final String READ_ONLY = "NoSelect";
	public static final String SINGLE_SELECT = "SingleSelect";
	private static final String ALLOW_CARD_EDITING = "AllowCardEditing";
	private static final String WITH_MAP = "Map";
	private static final String MAP_LATITUDE = "StartMapWithLatitude";
	private static final String MAP_LONGITUDE = "StartMapWithLongitude";
	private static final String MAP_ZOOM = "StartMapWithZoom";
	public static final String REQUIRED = "Required";
	private static final String METADATA = "Metadata";
	private static final String METADATA_OUTPUT = "MetadataOutput";
	public static final String DISABLE_GRID_FILTER_TOGGLER = "DisableGridFilterToggler";

	public LinkCardsWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final WidgetDefinition definition) {
		final LinkCards widget = new LinkCards();

		setFilterAndClassName(definition, widget);
		widget.setOutputName(readString(definition.get(OUTPUT_KEY)));
		widget.setDefaultSelection(readString(definition.get(DEFAULT_SELECTION)));
		widget.setReadOnly(definition.containsKey(READ_ONLY));
		widget.setSingleSelect(definition.containsKey(SINGLE_SELECT));
		widget.setAllowCardEditing(definition.containsKey(ALLOW_CARD_EDITING));
		widget.setEnableMap(definition.containsKey(WITH_MAP));
		widget.setMapLatitude(readInteger(definition.get(MAP_LATITUDE)));
		widget.setMapLongitude(readInteger(definition.get(MAP_LONGITUDE)));
		widget.setMapZoom(readInteger(definition.get(MAP_ZOOM)));
		widget.setRequired(definition.containsKey(REQUIRED));
		widget.setMetadata(toMap(readString(definition.get(METADATA))));
		widget.setMetadataOutput(readString(definition.get(METADATA_OUTPUT)));
		widget.setDisableGridFilterToggler(readBooleanFalseIfMissing(definition.get(DISABLE_GRID_FILTER_TOGGLER)));
		widget.setTemplates(extractUnmanagedStringParameters(definition, FILTER, CLASS_NAME, DEFAULT_SELECTION,
				READ_ONLY, SINGLE_SELECT, ALLOW_CARD_EDITING, WITH_MAP, MAP_LATITUDE, MAP_LONGITUDE, MAP_ZOOM,
				REQUIRED, BUTTON_LABEL, DISABLE_GRID_FILTER_TOGGLER));

		return widget;
	}

	/*
	 * If the filter is set the given ClassName is ignored and is used the
	 * filter
	 */
	private void setFilterAndClassName(final Map<String, Object> valueMap, final LinkCards widget) {
		final String filter = readString(valueMap.get(FILTER));
		if (filter != null) {
			widget.setFilter(filter);
			widget.setClassName(readClassNameFromCQLFilter(filter));
		} else {
			widget.setClassName(readString(valueMap.get(CLASS_NAME)));
		}
	}

	private Map<String, String> toMap(final String value) {
		Map<String, String> map;
		if (isBlank(value)) {
			map = Collections.emptyMap();
		} else {
			map = Splitter.on(METADATA_SEPARATOR) //
					.withKeyValueSeparator(NAME_TYPE_SEPARATOR) //
					.split(value);
		}
		return map;
	}

}
