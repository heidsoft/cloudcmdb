(function() {

	Ext.define('CMDBuild.view.management.report.custom.CustomView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.report.Custom}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.report.custom.GridPanel}
		 */
		grid: undefined,

		border: false,
		frame: false,
		layout: 'fit',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.grid = Ext.create('CMDBuild.view.management.report.custom.GridPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();