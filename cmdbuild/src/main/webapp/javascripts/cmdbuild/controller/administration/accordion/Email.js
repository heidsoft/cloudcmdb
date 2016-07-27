(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Email', {
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
		 * @property {CMDBuild.view.administration.accordion.Email}
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

			this.view = Ext.create('CMDBuild.view.administration.accordion.Email', { delegate: this });

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
					iconCls: 'cmdb-tree-email-icon',
					text: CMDBuild.Translation.accounts,
					description: CMDBuild.Translation.accounts,
					id: this.cmfg('accordionBuildId', 'accounts'),
					sectionHierarchy: ['accounts'],
					leaf: true
				},
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					iconCls: 'cmdb-tree-email-icon',
					text: CMDBuild.Translation.templates,
					description: CMDBuild.Translation.templates,
					id: this.cmfg('accordionBuildId', 'templates'),
					sectionHierarchy: ['templates'],
					leaf: true
				},
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					iconCls: 'cmdb-tree-email-icon',
					text: CMDBuild.Translation.queue,
					description: CMDBuild.Translation.queue,
					id: this.cmfg('accordionBuildId', 'queue'),
					sectionHierarchy: ['queue'],
					leaf: true
				}
			]);

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		}
	});

})();
