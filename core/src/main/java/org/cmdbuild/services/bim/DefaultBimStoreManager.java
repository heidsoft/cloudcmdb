package org.cmdbuild.services.bim;

import static org.cmdbuild.data.store.Storables.storableOf;

import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.model.bim.StorableLayer;
import org.cmdbuild.model.bim.StorableProject;

public class DefaultBimStoreManager implements BimStoreManager {

	private final Store<StorableProject> projectStore;
	private final Store<StorableLayer> layerStore;

	public DefaultBimStoreManager(Store<StorableProject> projectInfoStore, Store<StorableLayer> layerStore) {
		this.projectStore = projectInfoStore;
		this.layerStore = layerStore;
	}

	@Override
	public Iterable<StorableProject> readAll() {
		return projectStore.readAll();
	}

	@Override
	public StorableProject read(final String identifier) {
		return projectStore.read(storableOf(identifier));
	}

	@Override
	public StorableLayer readLayer(final String className) {
		return layerStore.read(storableOf(className));
	}

	@Override
	public void write(StorableProject project) {
		StorableProject projectAlreadyStored = projectStore.read(storableOf(project.getProjectId()));
		if (projectAlreadyStored != null) {
			project.setName(projectAlreadyStored.getName());
			if (project.getExportProjectId() == null) {
				project.setExportProjectId(projectAlreadyStored.getExportProjectId());
			}
			if (project.getLastCheckin() == null) {
				project.setLastCheckin(projectAlreadyStored.getLastCheckin());
			}
			projectStore.update(project);
		} else {
			projectStore.create(project).getIdentifier();
		}

	}

	@Override
	public void disableProject(String identifier) {
		StorableProject projectToEnable = read(identifier);
		projectToEnable.setActive(false);
		write(projectToEnable);
	}

	@Override
	public void enableProject(String identifier) {
		StorableProject projectToEnable = read(identifier);
		projectToEnable.setActive(true);
		write(projectToEnable);
	}

	@Override
	public Iterable<StorableLayer> readAllLayers() {
		return layerStore.readAll();
	}

	@Override
	public void saveActiveStatus(String className, String value) {
		StorableLayer layerForClass = layerStore.read(storableOf(className));
		if (layerForClass == null) {
			layerForClass = new StorableLayer(className);
			layerForClass.setActive(Boolean.parseBoolean(value));
			layerStore.create(layerForClass);
		} else {
			layerForClass.setActive(Boolean.parseBoolean(value));
			layerStore.update(layerForClass);
		}

	}

	@Override
	public void saveRoot(String className, boolean value) {
		StorableLayer layer = layerStore.read(storableOf(className));
		if (layer == null) {
			layer = new StorableLayer(className);
			layer.setRoot(value);
			layerStore.create(layer);
		} else {
			layer.setRoot(value);
			layerStore.update(layer);
		}
	}

	@Override
	public void saveRootReference(String className, String value) {
		StorableLayer layer = layerStore.read(storableOf(className));
		if (layer == null) {
			layer = new StorableLayer(className);
			layer.setRootReference(value);
			layerStore.create(layer);
		} else {
			layer.setRootReference(value);
			layerStore.update(layer);
		}
	}

	@Override
	public void saveExportStatus(String className, String value) {
		StorableLayer layerForClass = layerStore.read(storableOf(className));
		boolean exportValue = Boolean.parseBoolean(value);
		if (layerForClass == null) {
			layerForClass = new StorableLayer(className);
			layerForClass.setExport(exportValue);
			layerStore.create(layerForClass);
		} else {
			layerForClass.setExport(exportValue);
			layerStore.update(layerForClass);
		}
	}

	@Override
	public void saveContainerStatus(String className, String value) {
		StorableLayer layerForClass = layerStore.read(storableOf(className));
		boolean containerValue = Boolean.parseBoolean(value);
		if (layerForClass == null) {
			layerForClass = new StorableLayer(className);
			layerForClass.setContainer(containerValue);
			layerStore.create(layerForClass);
		} else {
			layerForClass.setContainer(containerValue);
			layerStore.update(layerForClass);
		}

	}

	@Override
	public StorableLayer findRoot() {
		for (StorableLayer layer : layerStore.readAll()) {
			if (layer.isRoot()) {
				return layer;
			}
		}
		return StorableLayer.NULL_LAYER;
	}

	@Override
	public StorableLayer findContainer() {
		for (StorableLayer layer : layerStore.readAll()) {
			if (layer.isContainer()) {
				return layer;
			}
		}
		return StorableLayer.NULL_LAYER;
	}

	@Override
	public boolean isActive(final String className) {
		boolean response = false;
		StorableLayer layer = layerStore.read(storableOf(className));
		if (layer != null) {
			response = layer.isActive();
		}
		return response;
	}

	@Override
	public String getContainerClassName() {
		StorableLayer containerLayer = findContainer();
		if (containerLayer == null) {
			throw new BimError("Container layer not configured");
		} else {
			return containerLayer.getClassName();
		}
	}

}
