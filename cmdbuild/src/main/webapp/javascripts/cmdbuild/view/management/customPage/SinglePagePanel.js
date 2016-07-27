(function() {

	Ext.define('CMDBuild.view.management.customPage.SinglePagePanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.customPage.SinglePage}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.customPages,

		border: true,
		frame: false,
		layout: 'fit',

		initComponent: function () {
			Ext.apply(this, {
				tools: [
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Properties', {
						style: {} // Reset margin setup
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
