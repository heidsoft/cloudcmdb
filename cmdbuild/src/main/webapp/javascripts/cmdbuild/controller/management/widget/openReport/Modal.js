(function () {

	Ext.define('CMDBuild.controller.management.widget.openReport.Modal', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		/**
		 * @cfg {CMDBuild.controller.management.widget.openReport.OpenReport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		browserManagedFormats: [
			CMDBuild.core.constants.Proxy.PDF,
			CMDBuild.core.constants.Proxy.CSV
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetOpenReportModalWindowShow'
		],

		/**
		 * @cfg {String}
		 */
		extension: CMDBuild.core.constants.Proxy.PDF,

		/**
		 * @property {CMDBuild.view.management.widget.openReport.ModalWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.widget.openReport.OpenReport} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.widget.openReport.ModalWindow', { delegate: this });

			this.setViewTitle(this.cmfg('widgetOpenReportConfigurationGet', CMDBuild.core.constants.Proxy.LABEL));

			if (!Ext.isEmpty(this.view) && Ext.isString(this.extension) && Ext.Array.contains(this.browserManagedFormats, this.extension))
				this.view.show();
		},

		/**
		 * @returns {Void}
		 */
		onWidgetOpenReportModalWindowShow: function () {
			if (!Ext.isEmpty(this.extension)) {
				this.view.removeAll();

				this.view.add({
					xtype: 'component',

					autoEl: {
						tag: 'iframe',
						src: CMDBuild.proxy.index.Json.report.factory.print + '?donotdelete=true' // Add parameter to avoid report delete
					}
				});
			}
		}
	});

})();
