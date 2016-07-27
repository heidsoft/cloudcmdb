package org.cmdbuild.servlets.json;

import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVITY_INSTANCE_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.PROCESS_INSTANCE_ID;

import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBase.Admin;
import org.cmdbuild.servlets.json.JSONBase.JSONExported;
import org.cmdbuild.servlets.utils.Parameter;

public class Lock extends JSONBaseWithSpringContext {

	@JSONExported
	public JsonResponse lockCard( //
			@Parameter(value = ID) final Long cardId //
	) {
		lockLogic().lockCard(cardId);
		return success();
	}

	@JSONExported
	public JsonResponse unlockCard( //
			@Parameter(value = ID) final Long cardId //
	) {
		lockLogic().unlockCard(cardId);
		return success();
	}

	@JSONExported
	public JsonResponse lockActivity( //
			@Parameter(value = PROCESS_INSTANCE_ID) final Long instanceId, //
			@Parameter(value = ACTIVITY_INSTANCE_ID) final String activityId //
	) {
		lockLogic().lockActivity(instanceId, activityId);
		return success();
	}

	@JSONExported
	public JsonResponse unlockActivity( //
			@Parameter(value = PROCESS_INSTANCE_ID) final Long instanceId, //
			@Parameter(value = ACTIVITY_INSTANCE_ID) final String activityId //
	) {
		lockLogic().unlockActivity(instanceId, activityId);
		return success();
	}

	@Admin
	@JSONExported
	public JsonResponse unlockAll() {
		lockLogic().unlockAll();
		return success();
	}

}
