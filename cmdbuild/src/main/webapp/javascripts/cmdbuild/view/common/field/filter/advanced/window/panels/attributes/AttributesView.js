(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.attributes.AttributesView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.view.common.field.filter.advanced.window.panels.Attributes}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.attributes.FormPanel}
		 */
		form: undefined,

		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.attributes,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.attributes.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onFieldFilterAdvancedWindowAttributesShow');
			}
		}
	});

})();