package org.cmdbuild.workflow.xpdl;

public class SharkStyleXpdlExtendedAttributeVariableFactory implements XpdlExtendedAttributeVariableFactory {

	public enum VariableSuffix {
		VIEW(false, false, "READ_ONLY"), //
		UPDATE(true, false, "READ_WRITE"), //
		UPDATEREQUIRED(true, true, "READ_WRITE_REQUIRED"), //
		;

		private final boolean writable;
		private final boolean mandatory;
		private final String legacy;

		private VariableSuffix(final boolean writable, final boolean mandatory, final String legacy) {
			this.writable = writable;
			this.mandatory = mandatory;
			this.legacy = legacy;
		}

		public boolean isWritable() {
			return writable;
		}

		public boolean isMandatory() {
			return mandatory;
		}

		@Deprecated
		public String getLegacy() {
			return legacy;
		}

	}

	public static final String VARIABLE_PREFIX = "VariableToProcess_";

	@Override
	public CMActivityVariableToProcess createVariable(final XpdlExtendedAttribute xa) {
		final String key = xa.getKey();
		final String name = xa.getValue();
		if (key == null || name == null) {
			return null;
		}
		final CMActivityVariableToProcess output;
		if (isVariableKey(key)) {
			final VariableSuffix type = extractType(key);
			output = new CMActivityVariableToProcess(name, type.isWritable(), type.isMandatory());
		} else {
			output = null;
		}
		return output;
	}

	private final boolean isVariableKey(final String key) {
		return key.startsWith(VARIABLE_PREFIX);
	}

	private final VariableSuffix extractType(final String key) {
		final String suffix = key.substring(VARIABLE_PREFIX.length());
		return VariableSuffix.valueOf(suffix);
	}

}
