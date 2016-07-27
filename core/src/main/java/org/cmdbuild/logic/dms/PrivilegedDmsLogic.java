package org.cmdbuild.logic.dms;

import static java.lang.String.format;
import static org.cmdbuild.logic.PrivilegeUtils.assure;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.DmsException;

import com.google.common.base.Optional;

public class PrivilegedDmsLogic extends ForwardingDmsLogic {

	public static interface DmsPrivileges {

		boolean readable(String className);

		boolean writable(String className);

	}

	public static class DefaultDmsPrivileges implements DmsPrivileges {

		private final CMDataView dataView;
		private final PrivilegeContext privilegeContext;

		public DefaultDmsPrivileges(final CMDataView dataView, final PrivilegeContext privilegeContext) {
			this.dataView = dataView;
			this.privilegeContext = privilegeContext;
		}

		@Override
		public boolean readable(final String className) {
			final boolean output;
			if (className == null) {
				output = false;
			} else {
				final CMClass element = dataView.findClass(className);
				if (element == null) {
					output = false;
				} else {
					output = privilegeContext.hasReadAccess(element);
				}
			}
			return output;
		}

		@Override
		public boolean writable(final String className) {
			final boolean output;
			if (className == null) {
				output = false;
			} else {
				final CMClass element = dataView.findClass(className);
				if (element == null) {
					output = false;
				} else {
					output = privilegeContext.hasWriteAccess(element)
							|| dataView.getActivityClass().isAncestorOf(element);
				}
			}
			return output;
		}

	}

	private final DmsLogic delegate;
	private final DmsPrivileges privileges;

	public PrivilegedDmsLogic(final DmsLogic delegate, final DmsPrivileges privileges) {
		this.delegate = delegate;
		this.privileges = privileges;
	}

	@Override
	protected DmsLogic delegate() {
		return delegate;
	}

	private void assureReadPrivilege(final String className) {
		assure(privileges.readable(className), format("class '%s' not readable", className));
	}

	private void assureWritePrivilege(final String className) {
		assure(privileges.writable(className), format("class '%s' not writable", className));
	}

	@Override
	public List<StoredDocument> search(final String className, final Long cardId) {
		assureReadPrivilege(className);
		return super.search(className, cardId);
	}

	@Override
	public Optional<StoredDocument> search(final String className, final Long cardId, final String fileName) {
		assureReadPrivilege(className);
		return super.search(className, cardId, fileName);
	}

	@Override
	public void upload(final String author, final String className, final Long cardId, final InputStream inputStream,
			final String fileName, final String category, final String description,
			final Iterable<MetadataGroup> metadataGroups) throws IOException, CMDBException {
		assureWritePrivilege(className);
		super.upload(author, className, cardId, inputStream, fileName, category, description, metadataGroups);
	}

	@Override
	public void delete(final String className, final Long cardId, final String fileName) throws DmsException {
		assureWritePrivilege(className);
		super.delete(className, cardId, fileName);
	}

	@Override
	public void updateDescriptionAndMetadata(final String author, final String className, final Long cardId,
			final String filename, final String category, final String description,
			final Iterable<MetadataGroup> metadataGroups) {
		assureWritePrivilege(className);
		super.updateDescriptionAndMetadata(author, className, cardId, filename, category, description, metadataGroups);
	}

	@Override
	public void copy(final String sourceClassName, final Long sourceId, final String filename,
			final String destinationClassName, final Long destinationId) {
		assureReadPrivilege(sourceClassName);
		assureWritePrivilege(destinationClassName);
		super.copy(sourceClassName, sourceId, filename, destinationClassName, destinationId);
	}

	@Override
	public void move(final String sourceClassName, final Long sourceId, final String filename,
			final String destinationClassName, final Long destinationId) {
		assureReadPrivilege(sourceClassName);
		assureWritePrivilege(destinationClassName);
		super.move(sourceClassName, sourceId, filename, destinationClassName, destinationId);
	}

}
