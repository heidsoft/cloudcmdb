(function () {

	Ext.define('CMDBuild.controller.management.accordion.Utility', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		requires: ['CMDBuild.core.constants.Proxy'],

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
		 * @cfg {Boolean}
		 */
		hideIfEmpty: true,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.management.accordion.Utility}
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

			this.view = Ext.create('CMDBuild.view.management.accordion.Utility', { delegate: this });

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
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			var nodes = [];

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.CHANGE_PASSWORD))
				nodes.push({
					cmName: 'utility',
					iconCls: 'cmdb-tree-utility-icon',
					text: CMDBuild.Translation.changePassword,
					description: CMDBuild.Translation.changePassword,
					id: this.cmfg('accordionBuildId', 'changepassword'),
					sectionHierarchy: ['changepassword'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.BULK_UPDATE))
				nodes.push({
					cmName: 'utility',
					iconCls: 'cmdb-tree-utility-icon',
					text: CMDBuild.Translation.multipleUpdate,
					description: CMDBuild.Translation.multipleUpdate,
					id: this.cmfg('accordionBuildId', 'bulkcardupdate'),
					sectionHierarchy: ['bulkcardupdate'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.IMPORT_CSV))
				nodes.push({
					cmName: 'utility',
					iconCls: 'cmdb-tree-utility-icon',
					text: CMDBuild.Translation.importCsvFile,
					description: CMDBuild.Translation.importCsvFile,
					id: this.cmfg('accordionBuildId', 'importcsv'),
					sectionHierarchy: ['importcsv'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.EXPORT_CSV))
				nodes.push({
					cmName: 'utility',
					iconCls: 'cmdb-tree-utility-icon',
					text: CMDBuild.Translation.exportCsvFile,
					description: CMDBuild.Translation.exportCsvFile,
					id: this.cmfg('accordionBuildId', 'exportcsv'),
					sectionHierarchy: ['exportcsv'],
					leaf: true
				});

			if (!Ext.isEmpty(nodes)) {
				this.view.getStore().getRootNode().removeAll();
				this.view.getStore().getRootNode().appendChild(nodes);
			}

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		},

		/**
		 * @param {String} moduleName
		 *
		 * @returns {Boolean}
		 */
		isSectionDisabled: function (moduleName) {
			switch (moduleName) {
				case CMDBuild.core.constants.Proxy.CHANGE_PASSWORD:
					return !CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.ALLOW_PASSWORD_CHANGE);

				default:
					return CMDBuild.configuration.userInterface.isDisabledModule(moduleName);
			}
		}
	});

})();
