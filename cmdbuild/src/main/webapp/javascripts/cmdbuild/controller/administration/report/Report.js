(function () {

	Ext.define('CMDBuild.controller.administration.report.Report', {
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
			'onReportModuleInit = onModuleInit'
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
		 * @cfg {CMDBuild.view.administration.report.ReportView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.report.ReportView', { delegate: this });
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @override
		 */
		onReportModuleInit: function (node) {
			if (!Ext.Object.isEmpty(node)) {
				this.view.removeAll(true);

				switch(node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'jasper':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.report.Jasper', { parentDelegate: this });
					}
				}

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.TEXT));

				this.view.add(this.sectionController.getView());

				this.sectionController.getView().fireEvent('show');

				this.onModuleInit(node); // Custom callParent() implementation
			}
		}
	});

})();
