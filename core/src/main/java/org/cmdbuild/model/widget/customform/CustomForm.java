package org.cmdbuild.model.widget.customform;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.WidgetVisitor;
import org.cmdbuild.workflow.CMActivityInstance;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Optional;

public class CustomForm extends Widget {

	private static final Marker MARKER = MarkerFactory.getMarker(CustomForm.class.getName());

	public static class Capabilities {

		private boolean readOnly;
		private boolean addDisabled;
		private boolean deleteDisabled;
		private boolean exportDisabled;
		private boolean importDisabled;
		private boolean modifyDisabled;
		private boolean cloneDisabled;
		private String refreshBehaviour;

		public boolean isReadOnly() {
			return readOnly;
		}

		public void setReadOnly(final boolean readOnly) {
			this.readOnly = readOnly;
		}

		public boolean isAddDisabled() {
			return addDisabled;
		}

		public void setAddDisabled(final boolean addDisabled) {
			this.addDisabled = addDisabled;
		}

		public boolean isDeleteDisabled() {
			return deleteDisabled;
		}

		public void setDeleteDisabled(final boolean deleteDisabled) {
			this.deleteDisabled = deleteDisabled;
		}

		public boolean isExportDisabled() {
			return exportDisabled;
		}

		public void setExportDisabled(final boolean exportDisabled) {
			this.exportDisabled = exportDisabled;
		}

		public boolean isImportDisabled() {
			return importDisabled;
		}

		public void setImportDisabled(final boolean importDisabled) {
			this.importDisabled = importDisabled;
		}

		public Boolean isModifyDisabled() {
			return modifyDisabled;
		}

		public void setModifyDisabled(final boolean modifyDisabled) {
			this.modifyDisabled = modifyDisabled;
		}

		public boolean isCloneDisabled() {
			return cloneDisabled;
		}

		public void setCloneDisabled(final boolean cloneDisabled) {
			this.cloneDisabled = cloneDisabled;
		}

		public String getRefreshBehaviour() {
			return refreshBehaviour;
		}

		public void setRefreshBehaviour(final String refreshBehaviour) {
			this.refreshBehaviour = refreshBehaviour;
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static class Serialization {

		@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "__type__")
		public static interface Configuration {

		}

		private String type;
		private Configuration configuration;

		public String getType() {
			return type;
		}

		public void setType(final String type) {
			this.type = type;
		}

		public Configuration getConfiguration() {
			return configuration;
		}

		public void setConfiguration(final Configuration configuration) {
			this.configuration = configuration;
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static class TextConfiguration implements Serialization.Configuration {

		private String keyValueSeparator;
		private String attributesSeparator;
		private String rowsSeparator;

		public String getKeyValueSeparator() {
			return keyValueSeparator;
		}

		public void setKeyValueSeparator(final String keyValueSeparator) {
			this.keyValueSeparator = keyValueSeparator;
		}

		public String getAttributesSeparator() {
			return attributesSeparator;
		}

		public void setAttributesSeparator(final String attributesSeparator) {
			this.attributesSeparator = attributesSeparator;
		}

		public String getRowsSeparator() {
			return rowsSeparator;
		}

		public void setRowsSeparator(final String rowsSeparator) {
			this.rowsSeparator = rowsSeparator;
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private String outputName;
	private boolean required;
	private String model;
	private String data;
	private String functionData;
	private String layout;
	private Capabilities capabilities;
	private Serialization serialization;
	private Map<String, Object> variables;

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		if (outputName != null) {
			final Optional<String> submission = decodeInput(input);
			output.put(outputName, submission.or(of(EMPTY)).get());
		}
	}

	private Optional<String> decodeInput(final Object input) {
		final Optional<String> output;
		if (serialization.configuration instanceof TextConfiguration) {
			final TextConfiguration configuration = TextConfiguration.class.cast(serialization.configuration);
			final StringBuilder outputBuilder = new StringBuilder();
			@SuppressWarnings("unchecked")
			final Iterable<String> inputElements = (Iterable<String>) input;
			for (final String element : inputElements) {
				if (outputBuilder.length() != 0) {
					outputBuilder.append(configuration.rowsSeparator);
				}
				try {
					final StringBuilder entryString = new StringBuilder();
					@SuppressWarnings("unchecked")
					final Map<String, Object> elementAsMap = new ObjectMapper().readValue(element, HashMap.class);
					for (final Entry<String, Object> entry : elementAsMap.entrySet()) {
						logger.debug(MARKER, "serializing entry '{}'", element);
						final String key = entry.getKey();
						if (key.equals("Id") || key.equals("IdClass")) {
							continue;
						}
						final Object value = entry.getValue();
						if (value == null) {
							continue;
						}
						if (entryString.length() != 0) {
							entryString.append(configuration.attributesSeparator);
						}
						final String keyValueString = format("%s%s%s", key, configuration.keyValueSeparator, value);
						entryString.append(keyValueString);
					}
					outputBuilder.append(entryString);
				} catch (final Exception e) {
					logger.warn(MARKER, format("error serializing entry '%s'", element), e);
				}
			}
			output = of(outputBuilder.toString());
		} else {
			output = absent();
		}
		return output;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(final boolean required) {
		this.required = required;
	}

	public String getModel() {
		return model;
	}

	public void setModel(final String model) {
		this.model = model;
	}

	public String getData() {
		return data;
	}

	public void setData(final String data) {
		this.data = data;
	}

	public String getFunctionData() {
		return functionData;
	}

	public void setFunctionData(final String functionData) {
		this.functionData = functionData;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(final String layout) {
		this.layout = layout;
	}

	public Capabilities getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(final Capabilities capabilities) {
		this.capabilities = capabilities;
	}

	public Serialization getSerialization() {
		return serialization;
	}

	public void setSerialization(final Serialization serialization) {
		this.serialization = serialization;
	}

	public Map<String, Object> getVariables() {
		return variables;
	}

	public void setVariables(final Map<String, Object> variables) {
		this.variables = variables;
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}