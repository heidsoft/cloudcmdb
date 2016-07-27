package org.cmdbuild.model.widget;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.workflow.CMActivityInstance;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Grid extends Widget {

	public static final String DEFAULT_MAP_SEPARATOR = "\n";
	public static final String DEFAULT_ENTRY_SEPARATOR = ",";
	public static final String DEFAULT_KEYVALUE_SEPARATOR = "=";
	public static final String TEXT_SERIALIZATION = "text";
	public static final String DEFAULT_SERIALIZATION = TEXT_SERIALIZATION;
	public static final boolean DEFAULT_WRITE_ON_ADVANCE = false;
	public static final String DEFAULT_PRESETS_TYPE = "text";
	public static final boolean DEFAULT_DISABLE_ADD_ROW = false;
	public static final boolean DEFAULT_DISABLE_IMPORT_FROM_CSV = false;
	public static final boolean DEFAULT_DISABLE_DELETE_ROW = false;
	public static final boolean DEFAULT_READONLY = false;
	public static final boolean DEFAULT_REQUIRED = false;

	private String className;
	private String outputName;
	private Map<String, Object> variables;
	private String cardSeparator;
	private String attributeSeparator;
	private String keyValueSeparator;
	private String serializationType;
	private boolean writeOnAdvance;
	private String presets;
	private String presetsType;
	private boolean disableAddRow;
	private boolean disableImportFromCsv;
	private boolean disableDeleteRow;
	private boolean readOnly;
	private boolean required;

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public void setOutputName(final String outputName) {
		this.outputName = outputName;
	}

	public String getOutputName() {
		return outputName;
	}

	public String getSerializationType() {
		return serializationType;
	}

	public void setSerializationType(final String serializationType) {
		this.serializationType = serializationType;
	}

	public boolean isWriteOnAdvance() {
		return writeOnAdvance;
	}

	public void setWriteOnAdvance(final boolean writeOnAdvance) {
		this.writeOnAdvance = writeOnAdvance;
	}

	public void setCardSeparator(final String cardSeparator) {
		this.cardSeparator = defaultIfBlank(cardSeparator, DEFAULT_MAP_SEPARATOR);
	}

	public String getCardSeparator() {
		return cardSeparator;
	}

	public void setAttributeSeparator(final String attributeSeparator) {
		this.attributeSeparator = defaultIfBlank(attributeSeparator, DEFAULT_ENTRY_SEPARATOR);
	}

	public String getAttributeSeparator() {
		return attributeSeparator;
	}

	public void setKeyValueSeparator(final String keyValueSeparator) {
		this.keyValueSeparator = defaultIfBlank(keyValueSeparator, DEFAULT_KEYVALUE_SEPARATOR);
	}

	public String getKeyValueSeparator() {
		return keyValueSeparator;
	}

	public String getPresets() {
		return presets;
	}

	public void setPresets(final String presets) {
		this.presets = presets;
	}

	public String getPresetsType() {
		return presetsType;
	}

	public void setPresetsType(final String presetsType) {
		this.presetsType = presetsType;
	}

	public boolean isDisableAddRow() {
		return disableAddRow;
	}

	public void setDisableAddRow(final boolean disableAddRow) {
		this.disableAddRow = disableAddRow;
	}

	public boolean isDisableImportFromCsv() {
		return disableImportFromCsv;
	}

	public void setDisableImportFromCsv(final boolean disableImportFromCsv) {
		this.disableImportFromCsv = disableImportFromCsv;
	}

	public boolean isDisableDeleteRow() {
		return disableDeleteRow;
	}

	public void setDisableDeleteRow(final boolean disableDeleteRow) {
		this.disableDeleteRow = disableDeleteRow;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(final boolean required) {
		this.required = required;
	}

	public Map<String, Object> getVariables() {
		return variables;
	}

	public void setVariables(final Map<String, Object> variables) {
		this.variables = variables;
	}

	@Override
	public void save(final CMActivityInstance activityInstance, final Object input, final Map<String, Object> output)
			throws Exception {
		if (outputName != null) {
			final Submission submission = decodeInput(input);
			output.put(outputName, outputValue(submission));
		}
	}

	private Object outputValue(final Submission submission) {
		String output = EMPTY;
		if (submission != null) {
			output = submission.getOutput();
		}
		return output;
	}

	private Submission decodeInput(final Object input) {
		if (writeOnAdvance) {
			throw new UnsupportedOperationException("'WriteOnAdvance ' not yet supported");
		}
		if (!serializationType.equals(TEXT_SERIALIZATION)) {
			throw new UnsupportedOperationException("Only " + TEXT_SERIALIZATION + " serialization is supported");
		}
		String output = EMPTY;
		final StringBuilder outputBuilder = new StringBuilder();
		@SuppressWarnings("unchecked")
		final Iterable<String> inputElements = (Iterable<String>) input;
		for (final String entry : inputElements) {
			HashMap<String, Object> props;
			try {
				props = new ObjectMapper().readValue(entry, HashMap.class);
				for (final Entry<String, Object> attribute : props.entrySet()) {
					final String key = attribute.getKey();
					if (attribute.getValue() == null) {
						continue;
					}
					if (key.equals("Id") || key.equals("IdClass")) {
						continue;
					}
					final String format = "%s" + keyValueSeparator + "%s";
					final String attributeString = String.format(format, key, attribute.getValue());
					outputBuilder.append(attributeString);
					outputBuilder.append(attributeSeparator);
				}
			} catch (final JsonParseException e) {
				e.printStackTrace();
			} catch (final JsonMappingException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			outputBuilder.append(cardSeparator);
		}
		output = outputBuilder.toString();
		final Submission submission = new Submission();
		submission.setOutput(output);
		return submission;
	}

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public static class Submission {
		private String output;

		public String getOutput() {
			return output;
		}

		public void setOutput(final String output) {
			this.output = output;
		}
	}

}