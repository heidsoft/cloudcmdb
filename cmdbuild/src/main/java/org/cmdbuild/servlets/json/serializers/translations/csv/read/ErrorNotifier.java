package org.cmdbuild.servlets.json.serializers.translations.csv.read;

public interface ErrorNotifier {

	public static final String MSG_UNSUPPORTED_FIELD = "unsupported field '%s'";
	public static final String MSG_UNSUPPORTED_IDENTIFIER = "unsupported identifier '%s'";
	public static final String MSG_UNSUPPORTED_TYPE = "unsupported type '%s'";
	public static final String MSG_INVALID_CONVERTER = "invalid converter";

	void unsupportedIdentifier(String identifier);

	void unsupportedType(String type);

	void unsupportedField(String field);

	void invalidConverter();

	public static ErrorNotifier THROWS_EXCEPTION = new ErrorNotifier() {

		@Override
		public void unsupportedType(final String type) {
			final String message = String.format(MSG_UNSUPPORTED_TYPE, type);
			throw new IllegalArgumentException(message);
		}

		@Override
		public void unsupportedIdentifier(final String identifier) {
			final String message = String.format(MSG_UNSUPPORTED_IDENTIFIER, identifier);
			throw new IllegalArgumentException(message);
		}

		@Override
		public void unsupportedField(final String field) {
			final String message = String.format(MSG_UNSUPPORTED_FIELD, field);
			throw new IllegalArgumentException(message);
		}

		@Override
		public void invalidConverter() {
			final String message = MSG_INVALID_CONVERTER;
			throw new IllegalArgumentException(message);
		}
	};

}
