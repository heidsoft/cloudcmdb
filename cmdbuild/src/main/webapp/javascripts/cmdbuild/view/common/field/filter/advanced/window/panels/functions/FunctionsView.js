(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.functions.FunctionsView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.panels.Functions}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.functions.FormPanel}
		 */
		form: undefined,

		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.functionLabel,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.functions.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onFieldFilterAdvancedWindowFunctionsShow');
			}
		}
	});

})();