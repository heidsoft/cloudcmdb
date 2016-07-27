package org.cmdbuild.workflow.xpdl;

public class SharkStyleXpdlExtendedAttributeMetadataFactory implements XpdlExtendedAttributeMetadataFactory {

	private static final String PREFIX = "Metadata_";

	@Override
	public CMActivityMetadata createMetadata(final XpdlExtendedAttribute xa) {
		final String key = xa.getKey();
		final String value = xa.getValue();
		final CMActivityMetadata output;
		if (key == null || value == null) {
			output = null;
		} else if (isMetadata(key)) {
			output = new CMActivityMetadata(extractKey(key), value);
		} else {
			output = null;
		}
		return output;
	}

	private static boolean isMetadata(final String key) {
		return key.startsWith(PREFIX);
	}

	private static String extractKey(final String key) {
		return key.substring(PREFIX.length());
	}

}
