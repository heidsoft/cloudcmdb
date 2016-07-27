(function () {

	Ext.define('CMDBuild.view.management.common.widgets.linkCards.LinkCards', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.linkCards.LinkCardsController}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Reload}
		 */
		applyDefaultSelectionButton: undefined,

		/**
		 * @property {Boolean}
		 *
		 * @private
		 */
		gisMapEnabled: false,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.state.Map}
		 */
		mapButton: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.map.CMMapPanel}
		 */
		mapPanel: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.state.Double}
		 */
		toggleGridFilterButton: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		hideMode: 'offsets',
		border: false,
		frame: false,

		layout: 'card',

		initComponent: function () {
			this.gisMapEnabled = this.widgetConf[CMDBuild.core.constants.Proxy.ENABLE_MAP] && CMDBuild.configuration.gis.get(CMDBuild.core.constants.Proxy.ENABLED);

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							this.toggleGridFilterButton = Ext.create('CMDBuild.core.buttons.iconized.state.Double', {
								state1text: CMDBuild.Translation.disableGridFilter,
								state1icon: 'searchFilterClear',
								state2text: CMDBuild.Translation.enableGridFilter,
								state2icon: 'searchFilter',
								disabled: true,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmOn('onToggleGridFilterButtonClick', button.getActiveState());
								}
							}),
							this.applyDefaultSelectionButton = Ext.create('CMDBuild.core.buttons.iconized.Reload', {
								text: CMDBuild.Translation.applyDefaultSelection,
								disabled: Ext.isEmpty(this.widgetConf[CMDBuild.core.constants.Proxy.DEFAULT_SELECTION]),
								scope: this,

								handler: function (button, e) {
									this.delegate.cmOn('onLinkCardApplyDefaultSelectionButtonClick');
								}
							}),
							'->',
							this.mapButton = Ext.create('CMDBuild.core.buttons.iconized.state.Map', {
								hidden: !this.gisMapEnabled,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmOn('onToggleMapButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid', {
						autoScroll: true,
						selModel: this.getSelectionModel(),
						hideMode: 'offsets',
						border: false
					}),
					this.gisMapEnabled ? this.mapPanel = Ext.create('CMDBuild.view.management.common.widgets.linkCards.map.CMMapPanel', {
						frame: false,
						border: false,

						lon: this.widgetConf['StartMapWithLongitude'] || this.widgetConf[CMDBuild.core.constants.Proxy.MAP_LONGITUDE],
						lat: this.widgetConf['StartMapWithLatitude'] || this.widgetConf[CMDBuild.core.constants.Proxy.MAP_LATITATUDE],
						initialZoomLevel: this.widgetConf['StartMapWithZoom'] || this.widgetConf[CMDBuild.core.constants.Proxy.MAP_ZOOM]
					}) : null
				]
			});

			this.callParent(arguments);

			// To listener to select right cards on pageChange
			this.grid.pagingBar.on('change', function (pagingBar, options) {
				this.delegate.cmOn('onGridPageChange');
			}, this);
		},

		/**
		 * @returns {Ext.selection.RowModel}
		 * @returns {CMDBuild.selection.CMMultiPageSelectionModel} single select or multi select
		 */
		getSelectionModel: function () {
			if (this.widgetConf[CMDBuild.core.constants.Proxy.READ_ONLY])
				return Ext.create('Ext.selection.RowModel');

			return Ext.create('CMDBuild.selection.CMMultiPageSelectionModel', {
				avoidCheckerHeader: true,
				mode: this.widgetConf[CMDBuild.core.constants.Proxy.SINGLE_SELECT] ? 'SINGLE' : 'MULTI',
				idProperty: 'Id' // Required to identify the records for the data and not the id of ext
			});
		},

		// Map methods
			/**
			 * @returns {CMDBuild.view.management.common.widgets.linkCards.map.CMMapPanel}
			 */
			getMapPanel: function () {
				if (this.gisMapEnabled)
					return this.mapPanel;
			},

			/**
			 * @return {Boolean}
			 */
			hasMap: function () {
				return !Ext.isEmpty(this.mapPanel);
			},

			/**
			 * @returns {Void}
			 */
			showGrid: function () {
				if (this.gisMapEnabled) {
					this.getLayout().setActiveItem(this.grid.id);

					this.grid.setCmVisible(true);
					this.mapPanel.setCmVisible(false);
				}
			},

			/**
			 * @returns {Void}
			 */
			showMap: function () {
				if (this.gisMapEnabled) {
					this.getLayout().setActiveItem(this.mapPanel.id);

					this.mapPanel.updateSize();

					this.mapPanel.setCmVisible(true);
					this.grid.setCmVisible(false);
				}
			}
	});

})();
