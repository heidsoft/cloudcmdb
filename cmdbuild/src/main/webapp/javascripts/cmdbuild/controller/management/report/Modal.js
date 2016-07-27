(function() {

	Ext.define('CMDBuild.controller.management.report.Modal', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		/**
		 * @cfg {Mixed}
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
			'onReportModalWindowDownloadButtonClick',
			'onReportModalWindowShow'
		],

		/**
		 * @cfg {String}
		 */
		extension: CMDBuild.core.constants.Proxy.PDF,

		/**
		 * @property {CMDBuild.view.management.report.ModalWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.report.ModalWindow', { delegate: this });

			this.setViewTitle(this.cmfg('selectedReportRecordGet', CMDBuild.core.constants.Proxy.DESCRIPTION));

			if (!Ext.isEmpty(this.view) && Ext.isString(this.extension) && Ext.Array.contains(this.browserManagedFormats, this.extension))
				this.view.show();
		},

		onReportModalWindowDownloadButtonClick: function() {
			this.cmfg('showReport', true);
		},

		onReportModalWindowShow: function() {
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