(function () {

	Ext.require(['CMDBuild.bim.proxy.Ifc']);

	Ext.define('CMDBuild.view.administration.bim.CMBimGrid', {
		extend: 'CMDBuild.view.administration.common.basepanel.CMGrid',

		withPagingBar: false,

		initComponent: function() {
			var me = this;

			this.callParent(arguments);

			this.on('beforeitemclick', function (grid, model, htmlelement, rowIndex, event, opt) {
				var eventName = event.target.className;
				if (eventName == 'action-download-ifc') {
					var id = model.get('id');
					var basicFormPanel = Ext.create('Ext.form.FormPanel', {
						hidden: true,
						fileUpload: true,
						items: [
							{
								xtype: 'textfield',
								fieldLabel: 'Field',
								name: 'projectId',
								value: id
							}
						]
					});

					var basicForm = basicFormPanel.getForm();
					basicForm.standardSubmit = true;

					// FIXME: should be used fake form submit feature
					CMDBuild.bim.proxy.Ifc.download({
						form: basicForm,
						target: '_self'
					});
				}
			}, this);
		}
	});

})();
