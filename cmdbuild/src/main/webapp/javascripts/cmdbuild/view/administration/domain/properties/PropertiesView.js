(function() {

	Ext.define('CMDBuild.view.administration.domain.properties.PropertiesView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.domain.Properties}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.properties.FormPanel}
		 */
		form: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.properties,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.administration.domain.properties.FormPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();