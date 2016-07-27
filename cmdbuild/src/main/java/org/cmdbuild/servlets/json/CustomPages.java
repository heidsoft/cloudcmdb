package org.cmdbuild.servlets.json;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;

import org.cmdbuild.logic.custompages.CustomPage;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Function;

public class CustomPages extends JSONBaseWithSpringContext {

	private static interface JsonCustomPage {

		@JsonProperty(ID)
		Long getId();

		@JsonProperty(NAME)
		String getName();

		@JsonProperty(DESCRIPTION)
		String getDescription();

	}

	private static class CustomPageAdapter implements JsonCustomPage {

		private final CustomPage delegate;

		public CustomPageAdapter(final CustomPage delegate) {
			this.delegate = delegate;
		}

		@Override
		public Long getId() {
			return delegate.getId();
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public String getDescription() {
			return delegate.getDescription();
		}

	}

	private static final Function<CustomPage, JsonCustomPage> TO_JSON = new Function<CustomPage, JsonCustomPage>() {

		@Override
		public JsonCustomPage apply(final CustomPage input) {
			return new CustomPageAdapter(input);
		}

	};

	@JSONExported
	public JsonResponse readForCurrentUser() {
		final Iterable<CustomPage> filters = customPagesLogic().readForCurrentUser();
		return JsonResponse.success(from(filters) //
				.transform(TO_JSON) //
				.toList());
	}

	@JSONExported
	@Admin
	public JsonResponse readAll() {
		final Iterable<CustomPage> filters = customPagesLogic().read();
		return JsonResponse.success(from(filters) //
				.transform(TO_JSON) //
				.toList());
	}

}
