package org.cmdbuild.logic.taskmanager.task.email;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.services.email.Email;

interface Action {

	void execute(Email email, Storable storable);

}