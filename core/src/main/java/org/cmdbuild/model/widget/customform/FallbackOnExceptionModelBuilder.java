package org.cmdbuild.model.widget.customform;

class FallbackOnExceptionModelBuilder implements ModelBuilder {

	private final ModelBuilder delegate;
	private final ModelBuilder fallback;

	public FallbackOnExceptionModelBuilder(final ModelBuilder delegate, final ModelBuilder fallback) {
		this.delegate = delegate;
		this.fallback = fallback;
	}

	@Override
	public String build() {
		try {
			return delegate.build();
		} catch (final Exception e) {
			return fallback.build();
		}
	}

}