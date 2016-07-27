package utils;

import java.util.ArrayDeque;
import java.util.Deque;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.view.DBDataView.DBAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;
import org.cmdbuild.dao.view.DBDataView.DBDomainDefinition;
import org.cmdbuild.workflow.CMProcessClass;

public class GenericRollbackDriver implements DBDriver {

	private interface Undoable {

		public void undo();

	}

	private abstract class Command<T> implements Undoable {

		public T exec() {
			final T out = execCommand();
			undoLog.add(this);
			return out;
		}

		@Override
		public void undo() {
			undoCommand();
			undoLog.remove(this);
		}

		protected abstract T execCommand();

		protected abstract void undoCommand();
	}

	private class CreateClass extends Command<DBClass> {

		private final DBClassDefinition definition;

		private DBClass createdClass;

		private CreateClass(final DBClassDefinition definition) {
			this.definition = definition;
		}

		@Override
		protected DBClass execCommand() {
			createdClass = innerDriver.createClass(definition);
			return createdClass;
		}

		@Override
		public void undoCommand() {
			innerDriver.clear(createdClass);
			innerDriver.deleteClass(createdClass);
		}

		public DBClass getCreatedClass() {
			return createdClass;
		}

	}

	private class UpdateClass extends Command<DBClass> {

		private final DBClassDefinition definition;

		private DBClassDefinition previousDefinition;
		private DBClass updatedClass;

		private UpdateClass(final DBClassDefinition definition) {
			this.definition = definition;
		}

		@Override
		protected DBClass execCommand() {
			storePreviousData();
			updatedClass = innerDriver.updateClass(definition);
			return updatedClass;
		}

		private void storePreviousData() {
			final CMIdentifier identifier = definition.getIdentifier();
			final String localname = identifier.getLocalName();
			final String namespace = identifier.getNameSpace();
			final DBClass existingClass = innerDriver.findClass(localname, namespace);
			previousDefinition = new DBClassDefinition() {

				@Override
				public CMIdentifier getIdentifier() {
					return existingClass.getIdentifier();
				}

				@Override
				public Long getId() {
					return existingClass.getId();
				}

				@Override
				public String getDescription() {
					return existingClass.getDescription();
				}

				@Override
				public DBClass getParent() {
					return existingClass.getParent();
				}

				@Override
				public boolean isSuperClass() {
					return existingClass.isSuperclass();
				}

				@Override
				public boolean isHoldingHistory() {
					return existingClass.holdsHistory();
				}

				@Override
				public boolean isActive() {
					return existingClass.isActive();
				}

				@Override
				public boolean isUserStoppable() {
					final boolean userStoppable;
					if (existingClass instanceof CMProcessClass) {
						userStoppable = CMProcessClass.class.cast(existingClass).isUserStoppable();
					} else {
						userStoppable = false;
					}
					return userStoppable;
				}

				@Override
				public boolean isSystem() {
					return existingClass.isSystem();
				}

			};
		}

		@Override
		public void undoCommand() {
			innerDriver.updateClass(previousDefinition);
		}

	}

	private class DeleteClass extends Command<Void> {

		private final DBClass classToDelete;

		public DeleteClass(final DBClass c) {
			this.classToDelete = c;
		}

		@Override
		protected Void execCommand() {
			innerDriver.deleteClass(classToDelete);
			return null;
		}

		@Override
		public void undoCommand() {
			for (final Undoable undoableCommand : undoLog) {
				if (undoableCommand instanceof CreateClass) {
					final CreateClass createClassCommand = (CreateClass) undoableCommand;
					if (createClassCommand.getCreatedClass().equals(classToDelete)) {
						undoLog.remove(createClassCommand);
						return;
					}
				}
			}
			throw new UnsupportedOperationException(
					"Unsupported deletion of a class that has not been created in the test");
		}
	}

	private class CreateAttribute extends Command<DBAttribute> {

		private final DBAttributeDefinition definition;

		private DBAttribute createdAttribute;

		private CreateAttribute(final DBAttributeDefinition definition) {
			this.definition = definition;
		}

		@Override
		protected DBAttribute execCommand() {
			createdAttribute = innerDriver.createAttribute(definition);
			return createdAttribute;
		}

		@Override
		public void undoCommand() {
			innerDriver.clear(createdAttribute);
			innerDriver.deleteAttribute(createdAttribute);
		}

	}

	private class UpdateAttribute extends Command<DBAttribute> {

		private final DBAttributeDefinition definition;

		private DBAttributeDefinition previousDefinition;
		private DBAttribute updatedAttribute;

		private UpdateAttribute(final DBAttributeDefinition definition) {
			this.definition = definition;
		}

		@Override
		protected DBAttribute execCommand() {
			storePreviousData();
			updatedAttribute = innerDriver.updateAttribute(definition);
			return updatedAttribute;
		}

		private void storePreviousData() {
			final DBClass existingClass = innerDriver.findClass(definition.getOwner().getName());
			final DBAttribute existingAttribute = existingClass.getAttribute(definition.getName());
			previousDefinition = new DBAttributeDefinition() {

				@Override
				public String getName() {
					return existingAttribute.getName();
				}

				@Override
				public DBEntryType getOwner() {
					return existingAttribute.getOwner();
				}

				@Override
				public CMAttributeType<?> getType() {
					return existingAttribute.getType();
				}

				@Override
				public String getDescription() {
					return existingAttribute.getDescription();
				}

				@Override
				public String getDefaultValue() {
					return existingAttribute.getDefaultValue();
				}

				@Override
				public Boolean isDisplayableInList() {
					return existingAttribute.isDisplayableInList();
				}

				@Override
				public boolean isMandatory() {
					return existingAttribute.isMandatory();
				}

				@Override
				public boolean isUnique() {
					return existingAttribute.isUnique();
				}

				@Override
				public Boolean isActive() {
					return existingAttribute.isActive();
				}

				@Override
				public Mode getMode() {
					return existingAttribute.getMode();
				}

				@Override
				public Integer getIndex() {
					return existingAttribute.getIndex();
				}

				@Override
				public String getGroup() {
					return existingAttribute.getGroup();
				}

				@Override
				public Integer getClassOrder() {
					return existingAttribute.getClassOrder();
				}

				@Override
				public String getEditorType() {
					return existingAttribute.getEditorType();
				}

				@Override
				public String getFilter() {
					return existingAttribute.getFilter();
				}

				@Override
				public String getForeignKeyDestinationClassName() {
					return definition.getForeignKeyDestinationClassName();
				}

			};
		}

		@Override
		public void undoCommand() {
			innerDriver.updateAttribute(previousDefinition);
		}

	}

	private class DeleteAttribute extends Command<Void> {

		private final DBAttribute dbAttribute;

		public DeleteAttribute(final DBAttribute dbAttribute) {
			this.dbAttribute = dbAttribute;
		}

		@Override
		protected Void execCommand() {
			innerDriver.deleteAttribute(dbAttribute);
			return null;
		}

		@Override
		public void undoCommand() {
			innerDriver.createAttribute(new DBAttributeDefinition() {

				@Override
				public String getName() {
					return dbAttribute.getName();
				}

				@Override
				public DBEntryType getOwner() {
					return dbAttribute.getOwner();
				}

				@Override
				public CMAttributeType<?> getType() {
					return dbAttribute.getType();
				}

				@Override
				public String getDescription() {
					return dbAttribute.getDescription();
				}

				@Override
				public String getDefaultValue() {
					return dbAttribute.getDefaultValue();
				}

				@Override
				public Boolean isDisplayableInList() {
					return dbAttribute.isDisplayableInList();
				}

				@Override
				public boolean isMandatory() {
					return dbAttribute.isMandatory();
				}

				@Override
				public boolean isUnique() {
					return dbAttribute.isUnique();
				}

				@Override
				public Boolean isActive() {
					return dbAttribute.isActive();
				}

				@Override
				public Mode getMode() {
					return dbAttribute.getMode();
				}

				@Override
				public Integer getIndex() {
					return dbAttribute.getIndex();
				}

				@Override
				public String getGroup() {
					return dbAttribute.getGroup();
				}

				@Override
				public Integer getClassOrder() {
					return dbAttribute.getClassOrder();
				}

				@Override
				public String getEditorType() {
					return dbAttribute.getEditorType();
				}

				@Override
				public String getFilter() {
					return dbAttribute.getFilter();
				}

				@Override
				public String getForeignKeyDestinationClassName() {
					return dbAttribute.getForeignKeyDestinationClassName();
				}

			});
		}

	}

	private class ClearAttribute extends Command<Void> {

		private final DBAttribute dbAttribute;

		public ClearAttribute(final DBAttribute dbAttribute) {
			this.dbAttribute = dbAttribute;
		}

		@Override
		protected Void execCommand() {
			innerDriver.clear(dbAttribute);
			return null;
		}

		@Override
		public void undoCommand() {
			// nothing to do
		}

	}

	private class CreateDomain extends Command<DBDomain> {

		private final DBDomainDefinition domainDefinition;

		private DBDomain newDomain;

		private CreateDomain(final DBDomainDefinition domainDefinition) {
			this.domainDefinition = domainDefinition;
		}

		@Override
		protected DBDomain execCommand() {
			newDomain = innerDriver.createDomain(domainDefinition);
			return newDomain;
		}

		@Override
		public void undoCommand() {
			innerDriver.clear(newDomain);
			innerDriver.deleteDomain(newDomain);
		}

		public DBDomain getCreatedDomain() {
			return newDomain;
		}

	}

	private class UpdateDomain extends Command<DBDomain> {

		private final DBDomainDefinition definition;

		private DBDomainDefinition previousDefinition;
		private DBDomain updatedDomain;

		private UpdateDomain(final DBDomainDefinition definition) {
			this.definition = definition;
		}

		@Override
		protected DBDomain execCommand() {
			storePreviousData();
			updatedDomain = innerDriver.updateDomain(definition);
			return updatedDomain;
		}

		private void storePreviousData() {
			final CMIdentifier identifier = definition.getIdentifier();
			final String localname = identifier.getLocalName();
			final String namespace = identifier.getNameSpace();
			final DBDomain existingDomain = innerDriver.findDomain(localname, namespace);
			previousDefinition = new DBDomainDefinition() {

				@Override
				public CMIdentifier getIdentifier() {
					return existingDomain.getIdentifier();
				}

				@Override
				public Long getId() {
					return existingDomain.getId();
				}

				@Override
				public DBClass getClass1() {
					return existingDomain.getClass1();
				}

				@Override
				public DBClass getClass2() {
					return existingDomain.getClass2();
				}

				@Override
				public String getDescription() {
					return existingDomain.getDescription();
				}

				@Override
				public String getDirectDescription() {
					return existingDomain.getDescription1();
				}

				@Override
				public String getInverseDescription() {
					return existingDomain.getDescription2();
				}

				@Override
				public String getCardinality() {
					return existingDomain.getCardinality();
				}

				@Override
				public boolean isMasterDetail() {
					return existingDomain.isMasterDetail();
				}

				@Override
				public String getMasterDetailDescription() {
					return existingDomain.getMasterDetailDescription();
				}

				@Override
				public boolean isActive() {
					return existingDomain.isActive();
				}

				@Override
				public Iterable<String> getDisabled1() {
					return existingDomain.getDisabled1();
				}

				@Override
				public Iterable<String> getDisabled2() {
					return existingDomain.getDisabled2();
				}

			};
		}

		@Override
		public void undoCommand() {
			innerDriver.updateDomain(previousDefinition);
		}

	}

	private class DeleteDomain extends Command<Void> {

		private final DBDomain domainToDelete;

		public DeleteDomain(final DBDomain d) {
			this.domainToDelete = d;
		}

		@Override
		protected Void execCommand() {
			innerDriver.deleteDomain(domainToDelete);
			return null;
		}

		@Override
		protected void undoCommand() {
			for (final Undoable undoableCommand : undoLog) {
				if (undoableCommand instanceof CreateDomain) {
					final CreateDomain createDomainCommand = (CreateDomain) undoableCommand;
					if (createDomainCommand.getCreatedDomain().equals(domainToDelete)) {
						undoLog.remove(createDomainCommand);
						return;
					}
				}
			}
			throw new UnsupportedOperationException(
					"Unsupported deletion of a class that has not been created in the test");
		}
	}

	private class CreateEntry extends Command<Long> {

		private final DBEntry entry;

		public CreateEntry(final DBEntry entry) {
			this.entry = entry;
		}

		@Override
		protected Long execCommand() {
			return innerDriver.create(entry);
		}

		@Override
		public void undoCommand() {
			innerDriver.delete(entry);
		}
	}

	/*
	 * Driver interface
	 */

	private final DBDriver innerDriver;
	private final Deque<Undoable> undoLog;

	public GenericRollbackDriver(final DBDriver driver) {
		this.innerDriver = driver;
		this.undoLog = new ArrayDeque<Undoable>();
	}

	public DBDriver getInnerDriver() {
		return innerDriver;
	}

	public void rollback() {
		while (!undoLog.isEmpty()) {
			undoLog.getLast().undo();
		}
	}

	@Override
	public Iterable<DBClass> findAllClasses() {
		return innerDriver.findAllClasses();
	}

	@Override
	public DBClass findClass(final Long id) {
		return innerDriver.findClass(id);
	}

	@Override
	public DBClass findClass(final String localname) {
		return innerDriver.findClass(localname);
	}

	@Override
	public DBClass findClass(final String localname, final String namespace) {
		return innerDriver.findClass(localname, namespace);
	}

	@Override
	public DBClass createClass(final DBClassDefinition definition) {
		return new CreateClass(definition).exec();
	}

	@Override
	public DBClass updateClass(final DBClassDefinition definition) {
		return new UpdateClass(definition).exec();
	}

	@Override
	public void deleteClass(final DBClass dbClass) {
		new DeleteClass(dbClass).exec();
	}

	@Override
	public DBAttribute createAttribute(final DBAttributeDefinition definition) {
		return new CreateAttribute(definition).exec();
	}

	@Override
	public DBAttribute updateAttribute(final DBAttributeDefinition definition) {
		return new UpdateAttribute(definition).exec();
	}

	@Override
	public void deleteAttribute(final DBAttribute dbAttribute) {
		new DeleteAttribute(dbAttribute).exec();
	}

	@Override
	public void clear(final DBAttribute dbAttribute) {
		new ClearAttribute(dbAttribute).exec();
	}

	@Override
	public Iterable<DBDomain> findAllDomains() {
		return innerDriver.findAllDomains();
	}

	@Override
	public DBDomain createDomain(final DBDomainDefinition definition) {
		return new CreateDomain(definition).exec();
	}

	@Override
	public DBDomain updateDomain(final DBDomainDefinition definition) {
		return new UpdateDomain(definition).exec();
	}

	@Override
	public void deleteDomain(final DBDomain dbDomain) {
		new DeleteDomain(dbDomain).exec();
	}

	@Override
	public DBDomain findDomain(final Long id) {
		return innerDriver.findDomain(id);
	}

	@Override
	public DBDomain findDomain(final String localname) {
		return innerDriver.findDomain(localname);
	}

	@Override
	public DBDomain findDomain(final String localname, final String namespace) {
		return innerDriver.findDomain(localname, namespace);
	}

	@Override
	public Iterable<DBFunction> findAllFunctions() {
		return innerDriver.findAllFunctions();
	}

	@Override
	public DBFunction findFunction(final String localname) {
		return innerDriver.findFunction(localname);
	}

	@Override
	public Long create(final DBEntry entry) {
		return new CreateEntry(entry).exec();
	}

	@Override
	public void update(final DBEntry entry) {
		// TODO maybe implement this, really needed?
		innerDriver.update(entry);
	}

	@Override
	public void delete(final CMEntry entry) {
		innerDriver.delete(entry);
	}

	@Override
	public CMQueryResult query(final QuerySpecs query) {
		return innerDriver.query(query);
	}

	@Override
	public void clear(final DBEntryType type) {
		innerDriver.clear(type);
	}

}
