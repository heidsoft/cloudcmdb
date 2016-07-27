(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Gis', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'accordionBuildId',
			'accordionDeselect',
			'accordionExpand',
			'accordionFirstSelectableNodeSelect',
			'accordionFirtsSelectableNodeGet',
			'accordionIdentifierGet',
			'accordionNodeByIdExists',
			'accordionNodeByIdGet',
			'accordionNodeByIdSelect',
			'accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.Gis}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Gis', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		accordionUpdateStore: function (nodeIdToSelect) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: 'gis-icons',
					iconCls: 'cmdb-tree-gis-icon',
					text: CMDBuild.Translation.manageIcons,
					description: CMDBuild.Translation.manageIcons,
					id: this.cmfg('accordionBuildId', 'gis-icons'),
					sectionHierarchy: ['gis-icons'],
					leaf: true
				},
				{
					cmName: 'gis-external-services',
					iconCls: 'cmdb-tree-gis-icon',
					text: CMDBuild.Translation.externalServices,
					description: CMDBuild.Translation.externalServices,
					id: this.cmfg('accordionBuildId', 'gis-external-services'),
					sectionHierarchy: ['gis-external-services'],
					leaf: true
				},
				{
					cmName: 'gis-layers-order',
					iconCls: 'cmdb-tree-gis-icon',
					text: CMDBuild.Translation.layersOrder,
					description: CMDBuild.Translation.layersOrder,
					id: this.cmfg('accordionBuildId', 'gis-layers-order'),
					sectionHierarchy: ['gis-layers-order'],
					leaf: true
				},
				{
					cmName: 'gis-geoserver',
					iconCls: 'cmdb-tree-gis-icon',
					text: CMDBuild.Translation.geoserverLayers,
					description: CMDBuild.Translation.geoserverLayers,
					id: this.cmfg('accordionBuildId', 'gis-geoserver'),
					sectionHierarchy: ['gis-geoserver'],
					leaf: true
				},
				{
					cmName: 'gis-filter-configuration',
					iconCls: 'cmdb-tree-gis-icon',
					text: CMDBuild.Translation.gisNavigation,
					description: CMDBuild.Translation.gisNavigation,
					id: this.cmfg('accordionBuildId', 'gis-filter-configuration'),
					sectionHierarchy: ['gis-filter-configuration'],
					leaf: true
				}
			]);

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		}
	});

})();
