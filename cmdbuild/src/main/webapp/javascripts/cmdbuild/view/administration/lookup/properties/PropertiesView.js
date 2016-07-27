(function() {

	Ext.define('CMDBuild.view.administration.lookup.properties.PropertiesView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.lookup.Properties}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.lookup.properties.FormPanel}
		 */
		form: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		disabled: true,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.properties,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.administration.lookup.properties.FormPanel', {
						delegate: this.delegate
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();