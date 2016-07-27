(function () {

	Ext.define('CMDBuild.controller.management.utility.Utility', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUtilityModuleInit = onModuleInit'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @cfg {CMDBuild.view.management.utility.UtilityView}
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

			this.view = Ext.create('CMDBuild.view.management.utility.UtilityView', { delegate: this });
		},

		/**
		 * @param {String} sectionId
		 *
		 * @returns {Object}
		 *
		 * @private
		 */
		buildController: function (sectionId) {
			switch (sectionId) {
				case 'changepassword':
					return Ext.create('CMDBuild.controller.management.utility.changePassword.ChangePassword', { parentDelegate: this });

				case 'exportcsv':
					return Ext.create('CMDBuild.controller.management.utility.exportCsv.ExportCsv', { parentDelegate: this });

				case 'importcsv':
					return Ext.create('CMDBuild.controller.management.utility.importCsv.ImportCsv', { parentDelegate: this });

				case 'bulkcardupdate':
				default:
					return Ext.create('CMDBuild.controller.management.utility.bulkUpdate.BulkUpdate', { parentDelegate: this });
			}
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onUtilityModuleInit: function (node) {
			if (!Ext.Object.isEmpty(node)) {
				this.view.removeAll(false);

				this.sectionController = this.buildController(node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]);

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

				this.view.add(this.sectionController.getView());

				this.sectionController.getView().fireEvent('show');

				this.onModuleInit(node); // Custom callParent() implementation
			}
		}
	});

})();
