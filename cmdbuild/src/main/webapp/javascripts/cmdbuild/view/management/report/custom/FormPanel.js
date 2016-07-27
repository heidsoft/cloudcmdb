(function () {

	Ext.define('CMDBuild.view.administration.userAndGroup.user.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.userAndGroup.user.User'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.user.User}
		 */
		delegate: undefined,

		/**
		 * @param {CMDBuild.core.buttons.iconized.state.Double}
		 */
		toggleEnableDisableButton: undefined,

		/**
		 * @param {CMDBuild.view.common.field.CMErasableCombo}
		 */
		defaultGroupCombo: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		privilegedCheckbox: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		serviceCheckbox: undefined,

		/**
		 * @param {Ext.form.FieldSet}
		 */
		userInfoFieldSet: undefined,

		/**
		 * @param {Ext.form.FieldSet}
		 */
		userPasswordFieldSet: undefined,

		/**
		 * @param {Ext.form.Panel}
		 */
		wrapper: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		cls: 'x-panel-body-default-framed',
		frame: false,
		overflowY: 'auto',
		split: true,

		layout: {
			type: 'hbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyUser,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserAndGroupUserModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Password', {
								text: CMDBuild.Translation.changePassword,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserAndGroupUserChangePasswordButtonClick');
								}
							}),
							this.toggleEnableDisableButton = Ext.create('CMDBuild.core.buttons.iconized.state.Double', {
								state1text: CMDBuild.Translation.disableUser,
								state2text: CMDBuild.Translation.enableUser,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserAndGroupUserDisableButtonClick', button.getActiveState());
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

								handler: function(button, e) {
									this.delegate.cmfg('onUserAndGroupUserSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserAndGroupUserAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.userInfoFieldSet = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.userInformations,
						overflowY: 'auto',
						flex: 1,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.USERNAME,
								fieldLabel: CMDBuild.Translation.username,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
								allowBlank: false,
								disableEnableFunctions: true,
								vtype: 'alphanumextended'
							}),
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.DESCRIPTION,
								fieldLabel: CMDBuild.Translation.descriptionLabel,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
								allowBlank: false
							}),
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.EMAIL,
								fieldLabel: CMDBuild.Translation.email,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
								allowBlank: true,
								vtype: 'emailOrBlank'
							}),
							this.defaultGroupCombo = Ext.create('CMDBuild.view.common.field.CMErasableCombo', {
								name: 'defaultgroup',
								fieldLabel: CMDBuild.Translation.defaultGroup,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
								valueField: CMDBuild.core.constants.Proxy.ID,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								editable: false,
								allowBlank: true,

								store: CMDBuild.proxy.userAndGroup.user.User.getStoreDefaultGroup(),
								queryMode: 'local'
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.IS_ACTIVE,
								fieldLabel: CMDBuild.Translation.active,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
							}),
							this.serviceCheckbox = Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.SERVICE,
								fieldLabel: CMDBuild.Translation.service,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,

								listeners: {
									scope: this,
									change: function(field, newValue, oldValue, eOpts) {
										this.delegate.cmfg('onUserAndGroupUserServiceChange');
									}
								}
							}),
							this.privilegedCheckbox = Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.PRIVILEGED,
								fieldLabel: CMDBuild.Translation.privileged,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,

								listeners: {
									scope: this,
									change: function(field, newValue, oldValue, eOpts) {
										this.delegate.cmfg('onUserAndGroupUserPrivilegedChange');
									}
								}
							}),
							{
								xtype: 'hiddenfield',
								name: 'userid'
							}
						]
					}),
					{ xtype: 'splitter' },
					this.userPasswordFieldSet = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.password,
						overflowY: 'auto',
						flex: 1,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.PASSWORD,
								id: 'user_password',
								inputType: 'password',
								fieldLabel: CMDBuild.Translation.password,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
								allowBlank: false
							}),
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.CONFIRMATION,
								inputType: 'password',
								fieldLabel: CMDBuild.Translation.confirmation,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
								allowBlank: false,
								twinFieldId: 'user_password',
								vtype: 'password',
								submitValue: false
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