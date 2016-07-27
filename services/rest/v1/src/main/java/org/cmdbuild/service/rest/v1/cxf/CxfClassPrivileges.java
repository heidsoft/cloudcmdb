package org.cmdbuild.service.rest.v1.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.service.rest.v1.model.Models.newClassPrivilege;
import static org.cmdbuild.service.rest.v1.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.privileges.PrivilegeInfo;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.service.rest.v1.ClassPrivileges;
import org.cmdbuild.service.rest.v1.model.ClassPrivilege;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CxfClassPrivileges implements ClassPrivileges {

	private final ErrorHandler errorHandler;
	private final AuthenticationLogic authenticationLogic;
	private final SecurityLogic securityLogic;
	private final DataAccessLogic dataAccessLogic;

	public CxfClassPrivileges(final ErrorHandler errorHandler, final AuthenticationLogic authenticationLogic,
			final SecurityLogic securityLogic, final DataAccessLogic dataAccessLogic) {
		this.errorHandler = errorHandler;
		this.authenticationLogic = authenticationLogic;
		this.securityLogic = securityLogic;
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public ResponseMultiple<ClassPrivilege> read(final String roleId) {
		final CMGroup role = authenticationLogic.getGroupWithName(roleId);
		if (role instanceof NullGroup) {
			errorHandler.roleNotFound(roleId);
		}
		final Iterable<ClassPrivilege> elements = from(classPrivileges(role)).toList();
		return newResponseMultiple(ClassPrivilege.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<ClassPrivilege> read(final String roleId, final String classId) {
		final CMGroup role = authenticationLogic.getGroupWithName(roleId);
		if (role instanceof NullGroup) {
			errorHandler.roleNotFound(roleId);
		}
		final Optional<ClassPrivilege> element = from(classPrivileges(role)) //
				.filter(new Predicate<ClassPrivilege>() {

					@Override
					public boolean apply(final ClassPrivilege input) {
						return input.getId().equals(classId);
					}

				}) //
				.first();
		if (!element.isPresent()) {
			errorHandler.classNotFound(classId);
		}
		return newResponseSingle(ClassPrivilege.class) //
				.withElement(element.get()) //
				.build();
	}

	private Iterable<ClassPrivilege> classPrivileges(final CMGroup role) {
		final Iterable<ClassPrivilege> elements;
		if (role.isAdmin()) {
			elements = from(dataAccessLogic.findClasses(true)) //
					.transform(classPrivilegeWithWriteMode());
		} else {
			elements = from(securityLogic.fetchClassPrivilegesForGroup(role.getId())) //
					.filter(privileged()) //
					.transform(classPrivilege());
		}
		return elements;
	}

	private Predicate<PrivilegeInfo> privileged() {
		return new Predicate<PrivilegeInfo>() {

			@Override
			public boolean apply(final PrivilegeInfo input) {
				final SerializablePrivilege privileged = input.getPrivilegedObject();
				final CMClass privilegedClass = CMClass.class.cast(privileged);
				return dataAccessLogic.hasClass(privilegedClass.getId());
			}

		};
	}

	private Function<PrivilegeInfo, ClassPrivilege> classPrivilege() {
		return new Function<PrivilegeInfo, ClassPrivilege>() {

			@Override
			public ClassPrivilege apply(final PrivilegeInfo input) {
				final SerializablePrivilege privilege = input.getPrivilegedObject();
				return newClassPrivilege() //
						.withId(privilege.getName()) //
						.withName(privilege.getName()) //
						.withDescription(privilege.getDescription()) //
						.withMode(input.getMode().getValue()) //
						.build();
			}

		};
	}

	private Function<CMClass, ClassPrivilege> classPrivilegeWithWriteMode() {
		return new Function<CMClass, ClassPrivilege>() {

			@Override
			public ClassPrivilege apply(final CMClass input) {
				return newClassPrivilege() //
						.withId(input.getName()) //
						.withName(input.getName()) //
						.withDescription(input.getDescription()) //
						.withMode(PrivilegeMode.WRITE.getValue()) //
						.build();
			}

		};
	}

}
