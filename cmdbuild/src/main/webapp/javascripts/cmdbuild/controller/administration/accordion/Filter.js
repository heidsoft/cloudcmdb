(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Filter', {
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
		 * @property {CMDBuild.view.administration.accordion.Filter}
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

			this.view = Ext.create('CMDBuild.view.administration.accordion.Filter', { delegate: this });

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
					cmName: this.cmfg('accordionIdentifierGet'),
					iconCls: 'cmdb-tree-searchFilter-icon',
					text: CMDBuild.Translation.filtersForGroups,
					description: CMDBuild.Translation.filtersForGroups,
					id: this.cmfg('accordionBuildId', 'groups'),
					sectionHierarchy: ['groups'],
					leaf: true
				}
			]);

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		}
	});

})();
