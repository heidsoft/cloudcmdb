package org.cmdbuild.logic.report;

import static com.google.common.collect.Sets.intersection;
import static com.google.common.collect.Sets.newHashSet;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.services.store.report.Report;

import com.google.common.base.Predicate;

public class Predicates {

	private static class CurrentGroupAllowed implements Predicate<Report> {

		private final UserStore userStore;

		public CurrentGroupAllowed(final UserStore userStore) {
			this.userStore = userStore;
		}

		@Override
		public boolean apply(final Report input) {
			final OperationUser operationUser = userStore.getUser();
			if (operationUser.hasAdministratorPrivileges()) {
				return true;
			}
			return !intersection(newHashSet(input.getGroups()),
					newHashSet(operationUser.getAuthenticatedUser().getGroupNames())).isEmpty();
		}

	}

	public static Predicate<Report> currentGroupAllowed(final UserStore userStore) {
		return new CurrentGroupAllowed(userStore);
	}

	private Predicates() {
		// prevents instantiation
	}

}
