(function() {

	Ext.define('CMDBuild.view.administration.domain.attributes.AttributesView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @property {CMDBuild.view.administration.domain.attributes.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.attributes.GridPanel}
		 */
		grid: undefined,

		layout: 'border',
		title: CMDBuild.Translation.attributes,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.domain.attributes.GridPanel', {
						region: 'north',
						split: true,
						height: '30%'
					}),
					this.form = Ext.create('CMDBuild.view.administration.domain.attributes.FormPanel', {
						region: 'center'
					})
				]
			});

			this.callParent(arguments);

			this.form.disableModify();
		},

		onAddAttributeClick: function() {
			this.form.onAddAttributeClick(params=null, enableAll=true);
			this.grid.getSelectionModel().deselectAll();
		}
	});

})();