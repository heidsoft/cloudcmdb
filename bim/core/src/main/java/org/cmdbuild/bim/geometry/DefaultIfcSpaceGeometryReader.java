package org.cmdbuild.bim.geometry;

import java.util.List;

import javax.vecmath.Vector3d;

import org.cmdbuild.bim.logging.LoggingSupport;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.Position3d;
import org.cmdbuild.bim.model.SpaceGeometry;
import org.cmdbuild.bim.model.implementation.DefaultSpaceGeometry;
import org.cmdbuild.bim.model.implementation.IfcPosition3d;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ListAttribute;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.SimpleAttribute;
import org.slf4j.Logger;
import org.slf4j.Marker;

import com.google.common.collect.Lists;

public class DefaultIfcSpaceGeometryReader implements IfcSpaceGeometryReader {

	private static final Logger logger = LoggingSupport.logger;
	private static final Marker marker = LoggingSupport.geometry;

	private final String revisionId;
	private final BimService service;
	private final IfcGeometryHelper geomHelper;
	private String key;

	public DefaultIfcSpaceGeometryReader(final BimService service, final String revisionId) {
		this.service = service;
		this.revisionId = revisionId;
		geomHelper = new DefaultIfcGeometryHelper(service, revisionId);
	}

	@Override
	public SpaceGeometry fetchGeometry(final String spaceIdentifier) {
		SpaceGeometry geometry = new DefaultSpaceGeometry();
		key = spaceIdentifier;
		final Entity space = service.getEntityByGuid(revisionId, key, null);
		logger.info(marker, "Space: {} Name: {}", key, space.getAttributeByName("Name").getValue());
		if (!space.isValid()) {
			throw new BimError("No space found with given identifier");
		}
		final IfcGeometryHelper geometryHelper = new DefaultIfcGeometryHelper(service, revisionId);

		// 1. Compute absolute coordinates of the space
		final Position3d spacePosition = geometryHelper.getAbsoluteObjectPlacement(space);
		logger.debug(spacePosition.toString());

		// 2. Read representation
		final Attribute representationAttribute = space.getAttributeByName("Representation");
		if (representationAttribute.isValid()) {
			final Entity shape = service.getReferencedEntity((ReferenceAttribute) representationAttribute, revisionId);
			if (!shape.isValid()) {
				throw new BimError("No shape found for given space");
			}
			final Attribute representationList = shape.getAttributeByName("Representations");
			if (!representationList.isValid()) {
				throw new BimError("No valid representation found in shape");
			}
			final int indexOfBB = getIndexOfBB(representationList);
			final boolean isThereABB = indexOfBB != -1;
			logger.debug("Is there a Bounding Box? '{}'", isThereABB);
			if (isThereABB) {
				logger.debug("Index of BoundingBox : '{}'", indexOfBB);

				final Entity representation = service.getReferencedEntity(
						(ReferenceAttribute) ((ListAttribute) representationList).getValues().get(indexOfBB),
						revisionId);

				geometry = getGeometryFromBoundingBox(representation);
				logger.debug("Relative coordinates of centroid: '{}'", geometry.getCentroid());

				final Position3d absolutePlacement = geomHelper.getAbsoluteObjectPlacement(space);
				logger.debug("Absolute placement of space: '{}'", absolutePlacement);

				convertCoordinates(geometry.getCentroid(), absolutePlacement);
				logger.debug("Absolute coordinates of centroid: '{}'", geometry.getCentroid());
			} else {
				final Entity representation = service.getReferencedEntity(
						(ReferenceAttribute) ((ListAttribute) representationList).getValues().get(0), revisionId);
				final Attribute typeAttribute = representation.getAttributeByName("RepresentationType");
				if (typeAttribute.isValid()) {
					final SimpleAttribute type = (SimpleAttribute) typeAttribute;
					if (type.getValue().equals("SweptSolid")) {
						geometry = getGeometryFromSweptSolid(representation);
					} else {
						logger.debug("Import of '{}' geometries not managed yet", type);
						return null;
					}
				}
				logger.debug("Base profile vertices: '{}'", geometry.getVertexList());
				logger.debug("Absolute placement of space: '{}'", spacePosition);

				// 5. Convert base profile vertices to the global reference
				// system
				convertCoordinatesAsVectors(geometry.getVertexList(), spacePosition);
				logger.debug("Absolute coordinates of base profile vertices: '{}'", geometry.getVertexList());
			}
		}
		return geometry;
	}

	private SpaceGeometry getGeometryFromBoundingBox(final Entity representation) {
		if (!representation.isValid()) {
			throw new BimError(
					"Unable to retrieve the Bounding Box. This should never occur, there should be some problem in the algorithm.");
		}
		final Attribute items = representation.getAttributeByName("Items");
		if (!items.isValid()) {
			throw new BimError("Unable to retrieve Item attribute of Bounding Box representation");
		}
		final ListAttribute itemList = (ListAttribute) items;
		if (itemList.getValues().size() != 1) {
			throw new BimError("More than one item: I do not know which one to pick up");
		}
		final ReferenceAttribute item = (ReferenceAttribute) itemList.getValues().get(0);
		final Entity boundingBox = service.getReferencedEntity(item, revisionId);
		if (!boundingBox.getTypeName().equals("IfcBoundingBox")) {
			throw new BimError(
					"This is not an IfcBoundingBox. This is an unexpected problem, I do not know what to do.");
		}
		final Attribute xDim = boundingBox.getAttributeByName("XDim");
		final Attribute yDim = boundingBox.getAttributeByName("YDim");
		final Attribute zDim = boundingBox.getAttributeByName("ZDim");
		final Attribute cornerAttribute = boundingBox.getAttributeByName("Corner");
		if (!xDim.isValid() || !yDim.isValid() || !zDim.isValid() || !cornerAttribute.isValid()) {
			throw new BimError("Some attribute of the Bounding Box is not filled. I do not know what to do.");
		}
		final Double dx = Double.parseDouble(xDim.getValue());
		final Double dy = Double.parseDouble(yDim.getValue());
		final Double dz = Double.parseDouble(zDim.getValue());

		final Entity cornerPoint = service.getReferencedEntity((ReferenceAttribute) cornerAttribute, revisionId);
		final Vector3d corner = geomHelper.getCoordinatesOfIfcCartesianPoint(cornerPoint);

		final double[] centroidAsArray = { corner.x + dx / 2, corner.y + dy / 2, corner.z + dz / 2 };

		final Vector3d centroid = new Vector3d(centroidAsArray);
		return new DefaultSpaceGeometry(centroid, dx, dy, dz);
	}

	private SpaceGeometry getGeometryFromSweptSolid(final Entity representation) {
		logger.debug("Get geometry from Swept Solid...");
		if (!representation.isValid()) {
			throw new BimError(
					"Unable to retrieve the Bounding Box. This should never occur, there should be some problem in the algorithm.");
		}
		final Attribute items = representation.getAttributeByName("Items");
		if (!items.isValid()) {
			throw new BimError("Unable to retrieve Item attribute of Bounding Box representation");
		}
		final ListAttribute itemList = (ListAttribute) items;
		if (itemList.getValues().size() != 1) {
			throw new BimError("More than one item: I do not know which one to pick up");
		}
		final ReferenceAttribute item = (ReferenceAttribute) itemList.getValues().get(0);
		final Entity sweptSolid = service.getReferencedEntity(item, revisionId);
		if (!sweptSolid.getTypeName().equals("IfcExtrudedAreaSolid")) {
			throw new BimError("This is not an IfcExtrudedAreaSolid. I do not know what to do.");
		}
		final Attribute positionAttribute = sweptSolid.getAttributeByName("Position");
		if (!positionAttribute.isValid()) {
			throw new BimError("Position attribute not found");
		}
		final Entity position = service.getReferencedEntity((ReferenceAttribute) positionAttribute, revisionId);

		// **** 1. SWEPT SOLID POSITION - It is an IfcPosition3D of the form
		// [O,[M]]
		final Position3d sweptSolidPosition = geomHelper.getPositionFromIfcPlacement(position);
		logger.debug("IfcExtrudedAreaSolid.Position '{}'", sweptSolidPosition);

		// **** 2. DEPTH - It is a number
		final Attribute depth = sweptSolid.getAttributeByName("Depth");
		Double dz = new Double(0);
		if (depth.isValid()) {
			dz = Double.parseDouble(depth.getValue());
		}

		// **** 3. BASE PROFILE OF THE SOLID - It is a polyline
		final Attribute sweptAreaAttribute = sweptSolid.getAttributeByName("SweptArea");
		if (!sweptAreaAttribute.isValid()) {
			throw new BimError("Solid attribute not found");
		}
		final Entity sweptArea = service.getReferencedEntity((ReferenceAttribute) sweptAreaAttribute, revisionId);

		Double dx = new Double(0);
		Double dy = new Double(0);
		Vector3d baseProfileCentroid = new Vector3d(0, 0, 0);
		final List<Vector3d> polylineVerticesAsVectors = Lists.newArrayList();
		final List<Position3d> polylineVertices = Lists.newArrayList();

		if (sweptArea.getTypeName().equals("IfcRectangleProfileDef")) {
			final Entity rectangleProfile = sweptArea;
			final Attribute rectangleProfilePositionAttribute = rectangleProfile.getAttributeByName("Position");
			if (!rectangleProfilePositionAttribute.isValid()) {
				throw new BimError("Position attribute not found");
			}
			final Entity rectangleProfilePositionEntity = service.getReferencedEntity(
					(ReferenceAttribute) rectangleProfilePositionAttribute, revisionId);

			// **** 3A. CENTRE OF RECTANGLE PROFILE with origin.z forced to 0
			final Position3d rectangleProfileCentre = geomHelper
					.getPositionFromIfcPlacement(rectangleProfilePositionEntity);
			baseProfileCentroid = rectangleProfileCentre.getOrigin(); // We do
																		// not
																		// care
			// about its
			// reference system.

			// **** 3B. DIMENSIONS OF RECTANGLE PROFILE
			final Attribute xDimAttribute = rectangleProfile.getAttributeByName("XDim");
			final Attribute yDimAttribute = rectangleProfile.getAttributeByName("YDim");
			if (!xDimAttribute.isValid() || !yDimAttribute.isValid()) {
				throw new BimError("Dimension attribute not found");
			}
			dx = Double.parseDouble(xDimAttribute.getValue());
			dy = Double.parseDouble(yDimAttribute.getValue());

			// **** 3C. PROFILE VERTICES
			final Vector3d v1 = new Vector3d(baseProfileCentroid.x - dx / 2, baseProfileCentroid.y - dy / 2,
					baseProfileCentroid.z);
			polylineVertices.add(new IfcPosition3d(v1));
			final Vector3d v2 = new Vector3d(baseProfileCentroid.x + dx / 2, baseProfileCentroid.y - dy / 2,
					baseProfileCentroid.z);
			polylineVertices.add(new IfcPosition3d(v2));
			final Vector3d v3 = new Vector3d(baseProfileCentroid.x + dx / 2, baseProfileCentroid.y + dy / 2,
					baseProfileCentroid.z);
			polylineVertices.add(new IfcPosition3d(v3));
			final Vector3d v4 = new Vector3d(baseProfileCentroid.x - dx / 2, baseProfileCentroid.y + dy / 2,
					baseProfileCentroid.z);
			polylineVertices.add(new IfcPosition3d(v4));
			polylineVerticesAsVectors.add(v1);
			polylineVerticesAsVectors.add(v2);
			polylineVerticesAsVectors.add(v3);
			polylineVerticesAsVectors.add(v4);

		} else if (sweptArea.getTypeName().equals("IfcArbitraryClosedProfileDef")
				|| sweptArea.getTypeName().equals("IfcArbitraryProfileDefWithVoids")) {
			final Attribute outerCurveAttribute = sweptArea.getAttributeByName("OuterCurve");
			if (!outerCurveAttribute.isValid()) {
				throw new BimError("Outer Curve attribute not found");
			}
			final Entity outerCurve = service.getReferencedEntity((ReferenceAttribute) outerCurveAttribute, revisionId);
			if (!outerCurve.getTypeName().equals("IfcPolyline")) {
				throw new BimError("Curve of type " + outerCurve.getTypeName() + " not handled");
			}
			final Attribute pointsAttribute = outerCurve.getAttributeByName("Points");
			if (!pointsAttribute.isValid()) {
				throw new BimError("Points attribute not found");
			}
			final ListAttribute edgesOfPolylineList = (ListAttribute) pointsAttribute;
			// **** 3C. PROFILE VERTICES
			for (final Attribute pointAttribute : edgesOfPolylineList.getValues()) {
				final Entity edge = service.getReferencedEntity((ReferenceAttribute) pointAttribute, revisionId);
				final Vector3d polylinePointCoordinates = geomHelper.getCoordinatesOfIfcCartesianPoint(edge);
				polylineVertices.add(new IfcPosition3d(polylinePointCoordinates));
				polylineVerticesAsVectors.add(polylinePointCoordinates);
			}
		} else {
			logger.debug("IfcProfileDef of type '{}' not managed", sweptArea.getTypeName());
			return null;
		}
		// **** 4. CONVERT VERTICES COORDINATES FROM THE SOLID REFERENCE SYSTEM
		convertCoordinatesAsVectors(polylineVerticesAsVectors, sweptSolidPosition);
		return new DefaultSpaceGeometry(polylineVerticesAsVectors, dz);
	}

	private void convertCoordinatesAsVectors(final List<Vector3d> polylineVerticesAsVectors,
			final Position3d sweptSolidPosition) {
		for (final Vector3d vector : polylineVerticesAsVectors) {
			convertCoordinates(vector, sweptSolidPosition);
		}
	}

	private void convertCoordinates(final Vector3d P, final Position3d referenceSystem) {
		referenceSystem.getVersorsMatrix().transform(P);
		P.add(referenceSystem.getOrigin());
	}

	private int getIndexOfBB(final Attribute representationList) {
		int index = -1;
		for (final Attribute value : ((ListAttribute) representationList).getValues()) {
			index++;
			final Entity representation = service.getReferencedEntity((ReferenceAttribute) value, revisionId);
			final Attribute typeAttribute = representation.getAttributeByName("RepresentationType");
			final String representationType = typeAttribute.getValue();
			if (representationType.equals("BoundingBox")) {
				return index;
			}
		}
		return -1;
	}

}
