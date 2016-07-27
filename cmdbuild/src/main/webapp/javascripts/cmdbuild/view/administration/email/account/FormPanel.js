(function () {

	Ext.define('CMDBuild.view.administration.email.account.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.email.Account}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Remove}
		 */
		removeButton: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Check}
		 */
		setDefaultButton: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		cls: 'cmdb-border-top',
		frame: false,

		layout: 'hbox',

		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyAccount,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onEmailAccountModifyButtonClick');
								}
							}),
							this.removeButton = Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								text: CMDBuild.Translation.removeAccount,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onEmailAccountRemoveButtonClick');
								}
							}),
							this.setDefaultButton = Ext.create('CMDBuild.core.buttons.iconized.Check', {
								text: CMDBuild.Translation.setAsDefault,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onEmailAccountSetDefaultButtonClick');
								}
							})
						]
					}),
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
									this.delegate.cmfg('onEmailAccountSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onEmailAccountAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.container.Container', {
						flex: 1,
						overflowY: 'auto',

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.account,

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.NAME,
										fieldLabel: CMDBuild.Translation.name,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
										allowBlank: false,
										disableEnableFunctions: true
									}),
									Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.constants.Proxy.IS_DEFAULT,
										hidden: true
									}),
									Ext.create('Ext.form.field.Hidden', { name: CMDBuild.core.constants.Proxy.ID })
								]
							}),
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.credentials,

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.USERNAME,
										fieldLabel: CMDBuild.Translation.username,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
									}),
									Ext.create('Ext.form.field.Text', {
										inputType: 'password',
										name: CMDBuild.core.constants.Proxy.PASSWORD,
										fieldLabel: CMDBuild.Translation.password,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
									})
								]
							})
						]
					}),
					{ xtype: 'splitter' },
					Ext.create('Ext.container.Container', {
						flex: 1,
						overflowY: 'auto',

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.outgoing,

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.ADDRESS,
										fieldLabel: CMDBuild.Translation.address,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
										allowBlank: false,
										vtype: 'email'
									}),
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.SMTP_SERVER,
										fieldLabel: CMDBuild.Translation.smtpServer,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
									}),
									Ext.create('Ext.form.field.Number', {
										name: CMDBuild.core.constants.Proxy.SMTP_PORT,
										fieldLabel: CMDBuild.Translation.smtpPort,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
										minValue: 0,
										maxValue: 65535
									}),
									Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.constants.Proxy.SMTP_SSL,
										fieldLabel: CMDBuild.Translation.enableSsl,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
									}),
									Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.constants.Proxy.SMTP_START_TLS,
										fieldLabel: CMDBuild.Translation.enableStartTls,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										inputValue: true,
										uncheckedValue: false
									}),
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.OUTPUT_FOLDER,
										fieldLabel: CMDBuild.Translation.sentFolder,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
									})
								]
							}),
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.incoming,

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									Ext.create('Ext.form.field.Text', {
										fieldLabel: CMDBuild.Translation.imapServer,
										name: CMDBuild.core.constants.Proxy.IMAP_SERVER,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
									}),
									Ext.create('Ext.form.field.Number', {
										name: CMDBuild.core.constants.Proxy.IMAP_PORT,
										fieldLabel: CMDBuild.Translation.imapPort,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
										minValue: 0,
										maxValue: 65535
									}),
									Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.constants.Proxy.IMAP_SSL,
										fieldLabel: CMDBuild.Translation.enableSsl,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
									}),
									Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.constants.Proxy.IMAP_START_TLS,
										fieldLabel: CMDBuild.Translation.enableStartTls,
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
										maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
										inputValue: true,
										uncheckedValue: false
									})
								]
							})
						]
					})
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();
