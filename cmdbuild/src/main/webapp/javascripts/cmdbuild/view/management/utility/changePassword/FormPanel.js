(function () {

	Ext.define('CMDBuild.view.management.utility.changePassword.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.utility.changePassword.ChangePassword}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-blue-panel',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUtilityChangePasswordSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUtilityChangePasswordAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.OLD_PASSWORD,
						fieldLabel: CMDBuild.Translation.oldPassword,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
						inputType: 'password',
						allowBlank: false
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.NEW_PASSWORD,
						fieldLabel: CMDBuild.Translation.newPassword,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
						inputType: 'password',
						vtype: 'password',
						id: 'newpassword',
						allowBlank: false
					}),
					Ext.create('Ext.form.field.Text', {
						name: 'confirmNewPassword',
						fieldLabel: CMDBuild.Translation.retypeNewPassword,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
						inputType: 'password',
						vtype: 'password',
						twinFieldId: 'newpassword',
						allowBlank: false
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
