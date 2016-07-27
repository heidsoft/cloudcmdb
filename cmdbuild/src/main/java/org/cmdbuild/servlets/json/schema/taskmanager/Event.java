package org.cmdbuild.servlets.json.schema.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Sets.newHashSet;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;

import java.util.Collection;

import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.task.event.asynchronous.AsynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;

public class Event extends JSONBaseWithSpringContext {

	@Admin
	@JSONExported
	public JsonResponse readAll() {
		final Collection<Task> allTasks = newHashSet();
		addAll(allTasks, taskManagerLogic().read(AsynchronousEventTask.class));
		addAll(allTasks, taskManagerLogic().read(SynchronousEventTask.class));
		return JsonResponse.success(JsonElements.of(from(allTasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

}
