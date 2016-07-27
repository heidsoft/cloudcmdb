(function () {

	Ext.define('CMDBuild.view.configure.step.Step3',{
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		border: false,
		bodyCls: 'cmdb-blue-panel-no-padding',
		frame: false,
		disabled: true, // Disable this step by default

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function () {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.ADMINISTRATOR_USER_NAME,
						fieldLabel: CMDBuild.Translation.username,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
						allowBlank: false
					}),
					this.adminPassword = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.ADMINISTRATOR_PASSWORD,
						fieldLabel: CMDBuild.Translation.password,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
						inputType: 'password',
						allowBlank: false
					}),
					Ext.create('Ext.form.field.Text', {
						fieldLabel: CMDBuild.Translation.confirmPassword,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
						inputType: 'password',
						vtype: 'password',
						twinFieldId: this.adminPassword.getId(),
						submitValue: false,
						allowBlank: false
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onConfigurationViewportWizardPanelShow', {
					displayPreviusButton: true,
					displayFinishButton: true
				});
			}
		}
	});

})();
