package org.cmdbuild.logic.taskmanager.task.connector;

import static com.google.common.collect.Maps.uniqueIndex;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.ClassMapping;
import org.cmdbuild.services.sync.store.Entry;

import com.google.common.base.Function;

class ConnectorTaskPermission implements PermissionBasedStore.Permission {

	private static final Function<ClassMapping, String> TARGET_NAME = new Function<ClassMapping, String>() {

		@Override
		public String apply(final ClassMapping input) {
			return input.getTargetType();
		};

	};

	private static final ClassMapping ALWAYS_TRUE = ClassMapping.newInstance() //
			.withCreateStatus(true) //
			.withUpdateStatus(true) //
			.withDeleteStatus(true) //
			.build();

	private final Map<String, ClassMapping> classMappingByTypeName;

	public ConnectorTaskPermission(final ConnectorTask task) {
		classMappingByTypeName = uniqueIndex(task.getClassMappings(), TARGET_NAME);
	}

	@Override
	public boolean allowsCreate(final Entry entry) {
		return mappingOf(entry).isCreate();
	}

	@Override
	public boolean allowsUpdate(final Entry entry) {
		return mappingOf(entry).isUpdate();
	}

	@Override
	public boolean allowsDelete(final Entry entry) {
		return mappingOf(entry).isDelete();
	}

	private ClassMapping mappingOf(final Entry entry) {
		return defaultIfNull(classMappingByTypeName.get(entry.getType().getName()), ALWAYS_TRUE);
	}

}