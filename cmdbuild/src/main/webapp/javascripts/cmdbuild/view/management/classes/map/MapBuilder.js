CMDBuild.Management.MapBuilder = (function() {

	var bounds = new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34);
	var projection = new OpenLayers.Projection('EPSG:900913');
	var displayProjection = new OpenLayers.Projection('EPSG:4326');

	/**
	 * @param {String} divId
	 */
	function buildMap(divId) {
		var map = new CMDBuild.Management.CMMap({
			projection: projection,
			displayProjection: displayProjection,
			units: 'm',
			numZoomLevels: 25,
			maxResolution: 156543.0339,
			maxExtent: bounds,
			div: divId,
			initBaseLayers: initBaseLayers,
			size: new OpenLayers.Size(0,0) // Set starting size object to avoid null pointer exception on Firefox
		});

		map.cmBaseLayers = [];

		map.addControl(new OpenLayers.Control.ScaleLine());
		map.addControl(new CMDBuild.Management.CMZoomAndMousePositionControl({
			zoomLabel: CMDBuild.Translation.management.modcard.gis.zoom,
			positionLabel: CMDBuild.Translation.management.modcard.gis.position
		}));

		addFakeLayer(map);

		return map;
	};

	function initBaseLayers() {
		var DEFAULT_MIN_ZOOM = 0;
		var DEFAULT_MAX_ZOOM = 18;
		var map = this;

		// add OSM if configured
		if (CMDBuild.configuration.gis.get(['osm', 'enabled'])) { // TODO: use proxy constants
			var osm = new OpenLayers.Layer.OSM(
				'Open Street Map',
				null,
				{
					numZoomLevels: 25,
					cmdb_minZoom: CMDBuild.configuration.gis.get(['osm', 'zoomMin']) || DEFAULT_MIN_ZOOM, // TODO: use proxy constants
					cmdb_maxZoom: CMDBuild.configuration.gis.get(['osm', 'zoomMax']) || DEFAULT_MAX_ZOOM, // TODO: use proxy constants

					isInZoomRange: function(zoom) {
						var max = this.cmdb_maxZoom <= DEFAULT_MAX_ZOOM ? this.cmdb_maxZoom : DEFAULT_MAX_ZOOM;
						return (zoom >= this.cmdb_minZoom && zoom <= max);
					},

					setVisibilityByZoom: function(zoom) {
						this.setVisibility(this.isInZoomRange(zoom));
					}
				}
			);

			map.addLayers([osm]);
			map.cmBaseLayers.push(osm);
			map.setBaseLayer(osm);
		}

		// add GOOGLE if configured
		if (CMDBuild.configuration.gis.get(['google', 'enabled'])) { // TODO: use proxy constants
			var googleLayer = new OpenLayers.Layer.Google(
				'Google Maps',
				{
					sphericalMercator: true,
					cmdb_minZoom: CMDBuild.configuration.gis.get(['google', 'zoomMin']) || DEFAULT_MIN_ZOOM, // TODO: use proxy constants
					cmdb_maxZoom: CMDBuild.configuration.gis.get(['google', 'zoomMax']) || DEFAULT_MAX_ZOOM, // TODO: use proxy constants

					setVisibilityByZoom: function(zoom) {
						var max = this.cmdb_maxZoom <= DEFAULT_MAX_ZOOM ? this.cmdb_maxZoom : DEFAULT_MAX_ZOOM;
						var isInRange = (zoom >= this.cmdb_minZoom && zoom <= max);

						this.setVisibility(isInRange);
					}
				}
			);

			map.addLayers([googleLayer]);
			map.setBaseLayer(googleLayer);

			googleLayer.setVisibility(true); // FIX To display map, by default visibility is set to false
		}

		// add YAHOO if configured
		if (CMDBuild.configuration.gis.get(['yahoo', 'enabled'])) { // TODO: use proxy constants
			var yahooLayer = new OpenLayers.Layer.Yahoo(
				'Yahoo',
				{
					sphericalMercator: true,
					cmdb_minZoom: CMDBuild.configuration.gis.get(['yahoo', 'zoomMin']) || DEFAULT_MIN_ZOOM, // TODO: use proxy constants
					cmdb_maxZoom: CMDBuild.configuration.gis.get(['yahoo', 'zoomMax']) || DEFAULT_MAX_ZOOM, // TODO: use proxy constants

					setVisibilityByZoom: function(zoom) {
						var max = this.cmdb_maxZoom <= DEFAULT_MAX_ZOOM ? this.cmdb_maxZoom : DEFAULT_MAX_ZOOM;
						var isInRange = (zoom >= this.cmdb_minZoom && zoom <= max);

						this.setVisibility(isInRange);
					}
				}
			);

			map.addLayers([yahooLayer]);
			map.setBaseLayer(yahooLayer);
		}

	};

	/**
	 * @param {CMDBuild.Management.CMMap} map
	 */
	function addFakeLayer(map) {
		// Add a fake base layer to set as base layer when the real base layers are out of range.
		// Without this, the continue to ask the tails
		var fakeBaseLayer = new OpenLayers.Layer.Vector('', {
			displayInLayerSwitcher: false,
			isBaseLayer: true
		});

		map.cmFakeBaseLayer = fakeBaseLayer;
		map.addLayers([fakeBaseLayer]);
	}

	return {
		buildMap: buildMap
	};

})();