(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.linkCards.LinkCardsMapController', {
		extend: 'CMDBuild.controller.management.common.widgets.linkCards.map.CMMapController',

		requires: [
			'CMDBuild.controller.management.common.widgets.linkCards.map.CMCardBrowserTreeDataSource',
			'CMDBuild.controller.management.common.widgets.linkCards.map.CMMapLayerSwitcherController',
			'CMDBuild.controller.management.common.widgets.linkCards.map.CMSelectFeatureController'
		],

		/**
		 * @property {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		cmIsInEditing: false,

		/**
		 * @property {Int}
		 */
		currentCardId: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.widgets.linkCards.map.CMMapEditingWindowDelegate}
		 */
		editingWindowDelegate: undefined,

		/**
		 * @property {CMDBuild.Management.CMMap}
		 */
		map: undefined,

		/**
		 * @property {CMDBuild.state.CMMapState}
		 */
		mapState: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.widgets.linkCards.map.CMMiniCardGridWindowFeaturesController}
		 */
		miniCardGridWindowController: undefined,

		/**
		 * @property {CMDBuild.model.widget.linkCards.Selection}
		 */
		model: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.widgets.linkCards.map.CMSelectFeatureController}
		 */
		selectControl: undefined,

		/**
		 * @property {Object}
		 */
		targetClass: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.map.CMMapPanel}
		 */
		view: undefined,

		mapPanel: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {Object} configuration
		 *	{
		 *		{CMDBuild.view.management.common.widgets.linkCards.map.CMMapPanel} view
		 *		{CMDBuild.model.widget.linkCards.Selection} model
		 *		{CMDBuild.controller.management.common.widgets.linkCards.LinkCardsController} parentDelegate,
		 *		{Object} widgetConf
		 *	}
		 */
		constructor: function (configuration) {
			var me = this;

			this.view = this.mapPanel = configuration.view; // Use mapPanel for compatibility mode with extended class
			this.model = configuration.model;
			this.parentDelegate = configuration.parentDelegate;
			this.widgetConf = configuration.widgetConf;

			this.targetClass = this.parentDelegate.getTargetClass();

			this.mon(
				this.view,
				'afterrender',
				function () {
					this.view.addDelegate(this);

					// Set me as delegate of the OpenLayers.Map (pimped in CMMap)
					this.map = this.view.getMap();
					this.map.delegate = this;

					this.cmIsInEditing = false;

					// Init the map state
					this.mapState = new CMDBuild.state.CMMapState(this);
					_CMMapState = this.mapState;

					// Set the switcher controller as a map delegate
					var layerSwitcher = this.view.getLayerSwitcherPanel();
					this.view.addDelegate(
						// TODO: use Ext.create with object as parameter
						new CMDBuild.controller.management.common.widgets.linkCards.map.CMMapLayerSwitcherController(layerSwitcher, this.map)
					);

					// Set me as a delegate of the switcher
					layerSwitcher.addDelegate(this);

					// Set me as a delegate of the cardBrowser
					var cardBrowserPanel = this.view.getCardBrowserPanel();
					if (cardBrowserPanel) {
						// TODO: use Ext.create with object as parameter
						new CMDBuild.controller.management.common.widgets.linkCards.map.CMCardBrowserTreeDataSource(cardBrowserPanel, this.mapState);
						cardBrowserPanel.addDelegate(Ext.create('CMDBuild.controller.management.common.widgets.linkCards.map.CMCardBrowserDelegate', this));
					}

					// Set me as delegate of the mini card grid
					this.view.getMiniCardGrid().addDelegate(this);

					// Init the miniCardGridWindowController
					this.miniCardGridWindowController = Ext.create('CMDBuild.controller.management.common.widgets.linkCards.map.CMMiniCardGridWindowFeaturesController');

					// Initialize editing control
					this.editingWindowDelegate = Ext.create('CMDBuild.controller.management.common.widgets.linkCards.map.CMMapEditingWindowDelegate', this);
					this.view.editingWindow.addDelegate(this.editingWindowDelegate);
					this.selectControl = new CMDBuild.controller.management.common.widgets.linkCards.map.CMSelectFeatureController([], {
						hover: false,
						renderIntent: 'default',
						eventListeners: {
							featurehighlighted: function (e) {
								me.onFeatureSelect(e);
							}
						}
					});

					this.map.addControl(this.selectControl);
					this.selectControl.activate();

					// Add me to the CMCardModuleStateDelegates
					_CMCardModuleState.addDelegate(this);
					_CMCardModuleState.setEntryType(this.targetClass, null, null, false);

					this.map.events.register('zoomend', this, this.onZoomEnd);
				},
				this,
				{ single: true }
			);
		},

		/**
		 * @param {Object} e
		 * @param {OpenLayers.Feature.Vector} e.feature
		 * @param {CMDBuild.controller.management.common.widgets.linkCards.map.CMSelectFeatureController} e.object
		 */
		onFeatureSelect: function (e) {
			var attributes = e.feature.attributes;
			var layer = e.feature.layer;
			var selectedCoordinates = this.map.getLonLatFromPixel(e.object.handlers.feature.evt.xy);

			var selectedCoordinatesObject = {}
			selectedCoordinatesObject['lat'] = selectedCoordinates['lat'];
			selectedCoordinatesObject['lon'] = selectedCoordinates['lon'];

			this.model.reset();
			this.model.select(attributes.master_card, selectedCoordinatesObject);

			if (!Ext.isEmpty(layer.editLayer)) { // The feature selected is in a cmdbLayer with an associated editLayer
				_CMCardModuleState.setCard(
					{
						Id: attributes.master_card,
						IdClass: attributes.master_class
					},
					null,
					false
				);
			}
		},

		/**
		 * @param {Object} or {Int} card
		 */
		onCardSelected: function (card) {
			if (this.view.cmVisible) {
				var id = card;

				if (card && typeof card.get == 'function')
					id = card.get('Id');

				if (id != this.currentCardId) {
					this.currentCardId = id;
					var layers = this.view.getMap().getCmdbLayers();

					for (var i = 0; i < layers.length; i++) {
						layers[i].clearSelection();
						layers[i].selectFeatureByMasterCard(this.currentCardId);
					}
				}

				// To sync the cardBrowserPanelSelection
				if (this.view.getCardBrowserPanel()) {
					this.view.getCardBrowserPanel().checkCardNodeAncestors(card);
					this.view.getCardBrowserPanel().selectCardNodePath(card);
				}

				// To sync the miniCardGrid
				this.view.getMiniCardGrid().selectCardSilently(card);

				if (card)
					this.centerMapOnFeature(card.data);

				// To sync selected feature with grid card
				this.model.select(id);
			}
		},

		/**
		 * Executed after zoomEvent to update mapState object and manually redraw all map's layers
		 */
		onZoomEnd: function () {
			var zoom = this.map.getZoom();

			this.mapState.updateForZoom(zoom);

			var baseLayers = this.map.cmBaseLayers;
			var haveABaseLayer = false;

			// Manually force redraw of all layers to fix a problem with GoogleMaps
			Ext.Array.each(this.map.layers, function (item, index, allItems) {
				item.redraw();
			});

			for (var i = 0; i < baseLayers.length; ++i) {
				var layer = baseLayers[i];

				if (!layer || typeof layer.isInZoomRange != 'function')
					continue;

				if (layer.isInZoomRange(zoom)) {
					this.map.setBaseLayer(layer);
					haveABaseLayer = true;

					break;
				}
			}

			if (!haveABaseLayer)
				this.map.setBaseLayer(this.map.cmFakeBaseLayer);
		}
	});

})();
