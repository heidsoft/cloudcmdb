package org.cmdbuild.model.widget;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.AbstractReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.WorkflowTypesConverter.Reference;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class LinkCards extends Widget {

	public static class Submission {

		private List<Object> output;
		private List<Map<Object, Object>> metadata;

		public List<Object> getOutput() {
			return output;
		}

		public void setOutput(final List<Object> output) {
			this.output = output;
		}

		public List<Map<Object, Object>> getMetadata() {
			return metadata;
		}

		public void setMetadata(final List<Map<Object, Object>> metadata) {
			this.metadata = metadata;
		}

	}

	public static final String METADATA_GROUP_SEPARATOR = "|";
	public static final String METADATA_SEPARATOR = ";";
	public static final String NAME_TYPE_SEPARATOR = ":";

	private static final Map<String, String> NO_METADATA = Collections.emptyMap();

	/**
	 * A CQL query to fill the linkCard grid Use it or the className
	 */
	private String filter;

	/**
	 * Fill the linkCard grid with the cards of this class. Use it or the filter
	 */
	private String className;

	/**
	 * A CQL query to define the starting selection
	 */
	private String defaultSelection;

	/**
	 * If true, the grid is in read-only mode so you can not select its rows
	 */
	private boolean readOnly;

	/**
	 * To allow the selection of only a row
	 */
	private boolean singleSelect;

	/**
	 * Add an icon at the right of each row to edit the referred card
	 */
	private boolean allowCardEditing;

	/**
	 * If true, the user must select a card on this widget before to can advance
	 * with the process
	 */
	private boolean required;

	/**
	 * If true, enable the map module for this widget
	 */
	private boolean enableMap;

	/**
	 * The latitude to use as default for the map module
	 */
	private Integer mapLatitude;

	/**
	 * The longitude to use as default for the map module
	 */
	private Integer mapLongitude;

	/**
	 * The zoom level to use as default for the map module
	 */
	private Integer mapZoom;

	/**
	 * Default status of grid filter toggler button
	 */
	private boolean disableGridFilterToggler;

	/**
	 * The name of the variable where to put the selections of the widget during
	 * the save operation
	 */
	private String outputName;

	/**
	 * Templates to use for the CQL filters
	 */
	private Map<String, String> templates;

	private Map<String, String> metadata;
	private String metadataOutput;

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public String getDefaultSelection() {
		return defaultSelection;
	}

	public void setDefaultSelection(final String defaultSelection) {
		this.defaultSelection = defaultSelection;
	}

	public boolean isSingleSelect() {
		return singleSelect;
	}

	public void setSingleSelect(final boolean singleSelect) {
		this.singleSelect = singleSelect;
	}

	public boolean isAllowCardEditing() {
		return allowCardEditing;
	}

	public void setAllowCardEditing(final boolean allowCardEditing) {
		this.allowCardEditing = allowCardEditing;
	}

	public boolean isDisableGridFilterToggler() {
		return disableGridFilterToggler;
	}

	public void setDisableGridFilterToggler(final boolean disableGridFilterToggler) {
		this.disableGridFilterToggler = disableGridFilterToggler;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(final boolean required) {
		this.required = required;
	}

	public boolean isEnableMap() {
		return enableMap;
	}

	public void setEnableMap(final boolean enableMap) {
		this.enableMap = enableMap;
	}

	public Integer getMapLatitude() {
		return mapLatitude;
	}

	public void setMapLatitude(final Integer mapLatitude) {
		this.mapLatitude = mapLatitude;
	}

	public Integer getMapLongitude() {
		return mapLongitude;
	}

	public void setMapLongitude(final Integer mapLongitude) {
		this.mapLongitude = mapLongitude;
	}

	public Integer getMapZoom() {
		return mapZoom;
	}

	public void setMapZoom(final Integer mapZoom) {
		this.mapZoom = mapZoom;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	public Map<String, String> getTemplates() {
		return templates;
	}

	public void setTemplates(final Map<String, String> templates) {
		this.templates = templates;
	}

	public Map<String, String> getMetadata() {
		return defaultIfNull(metadata, NO_METADATA);
	}

	public void setMetadata(final Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getMetadataOutput() {
		return metadataOutput;
	}

	public void setMetadataOutput(final String metadataOutput) {
		this.metadataOutput = metadataOutput;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		if (outputName != null) {
			final Submission submission = decodeInput(input);
			output.put(outputName, outputValue(submission));
			if (isNotBlank(metadataOutput)) {
				output.put(metadataOutput, metadataValue(submission));
			}
		}
	}

	private Submission decodeInput(final Object input) {
		if (input instanceof Submission) {
			return (Submission) input;
		} else {
			@SuppressWarnings("unchecked")
			final Map<Object, Object> selectedCards = Map.class.cast(input);
			final List<Object> selectedIds = Lists.newArrayList(selectedCards.keySet());
			final List<Map<Object, Object>> metadata = Lists.newArrayList();
			for (final Object selectedId : selectedIds) {
				final Map<Object, Object> _metadata = (Map<Object, Object>) selectedCards.get(selectedId);
				metadata.add(_metadata);
			}
			final Submission submission = new Submission();
			submission.setOutput(selectedIds);
			submission.setMetadata(metadata);
			return submission;
		}
	}

	private Reference[] outputValue(final Submission submission) {
		final List<Object> selectedCardIds = submission.getOutput();
		final List<Reference> selectedCards = newArrayListWithExpectedSize(selectedCardIds.size());
		for (final Object cardId : selectedCardIds) {
			final Long cardIdLong = toLong(cardId);
			final Reference reference = new Reference() {

				@Override
				public Long getId() {
					return cardIdLong;
				}

				@Override
				public String getClassName() {
					return null;
				}

			};
			selectedCards.add(reference);
		}
		return selectedCards.toArray(new Reference[selectedCards.size()]);
	}

	private Long toLong(final Object cardId) {
		return new AbstractReferenceAttributeType() {

			@Override
			public void accept(final CMAttributeTypeVisitor visitor) {
				throw new UnsupportedOperationException();
			}

		}.convertValue(cardId).getId();
	}

	private Object metadataValue(final Submission submission) {
		return Joiner.on(METADATA_GROUP_SEPARATOR) //
				.join(from(submission.getMetadata()) //
						.transform(new Function<Map<Object, Object>, String>() {

							@Override
							public String apply(final Map<Object, Object> input) {
								return Joiner.on(METADATA_SEPARATOR) //
										.withKeyValueSeparator(NAME_TYPE_SEPARATOR) //
										.join(input);
							}

						}) //
				);
	}
}
