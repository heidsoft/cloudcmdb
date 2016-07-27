package org.cmdbuild.servlets.json.email;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TIME;

import org.cmdbuild.logic.email.EmailQueueLogic;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;

public class Queue extends JSONBaseWithSpringContext {

	private static class JsonConfiguration implements EmailQueueLogic.Configuration {

		private long time;

		public JsonConfiguration() {
		}

		public JsonConfiguration(final EmailQueueLogic.Configuration configuration) {
			this.time = configuration.time();
		}

		@Override
		@JsonProperty(TIME)
		public long time() {
			return time;
		}

		public void setTime(final long time) {
			this.time = time;
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	@JSONExported
	public JsonResponse running() {
		final boolean running = emailQueueLogic().running();
		return JsonResponse.success(running);
	}

	@JSONExported
	public JsonResponse start() {
		emailQueueLogic().start();
		return JsonResponse.success(true);
	}

	@JSONExported
	public JsonResponse stop() {
		emailQueueLogic().stop();
		return JsonResponse.success(true);
	}

	@JSONExported
	public JsonResponse configuration() {
		final EmailQueueLogic.Configuration configuration = emailQueueLogic().configuration();
		return JsonResponse.success(new JsonConfiguration(configuration));
	}

	@JSONExported
	public JsonResponse configure( //
			@Parameter(value = TIME, required = false) final Long time //
	) {
		final JsonConfiguration configuration = new JsonConfiguration();
		configuration.setTime(defaultIfNull(time, 0L));
		emailQueueLogic().configure(configuration);
		return JsonResponse.success(true);
	}

}
