(function () {

	Ext.define('CMDBuild.view.common.sessionExpired.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.view.common.sessionExpired.FormPanel}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		password: undefined,

		border: false,
		frame: true,

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
							Ext.create('CMDBuild.core.buttons.text.Login', {
								hidden: !CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.ALLOW_PASSWORD_CHANGE),
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onSessionExpiredLoginButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.ChangeUser', {
								hidden: !CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.ALLOW_PASSWORD_CHANGE),
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onSessionExpiredChangeUserButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Confirm', {
								hidden: CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.ALLOW_PASSWORD_CHANGE),
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onSessionExpiredConfirmButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Display', {
						value: CMDBuild.Translation.sessionExpiredMessage,
						style: {
							padding: '0px 0px 5px 0px'
						}
					}),
					this.password = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.PASSWORD,
						fieldLabel: CMDBuild.Translation.password,
						hidden: !CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.ALLOW_PASSWORD_CHANGE),
						inputType: 'password',
						allowBlank: false,

						listeners: {
							scope: this,
							specialkey: function (field, e, eOpts) {
								if(e.getKey() == e.ENTER)
									this.delegate.cmfg('onSessionExpiredLoginButtonClick');
							}
						}
					}),
					this.group = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.GROUP,
						fieldLabel: CMDBuild.Translation.chooseAGroup,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_LOGIN,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						hidden: true,
						editable: false,
						forceSelection: true,

						store: Ext.create('Ext.data.ArrayStore', {
							fields: [CMDBuild.core.constants.Proxy.NAME, CMDBuild.core.constants.Proxy.DESCRIPTION],
							sorters: [
								{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
							]
						}),
						queryMode: 'local',

						listeners: {
							scope: this,
							specialkey: function (field, e, eOpts) {
								if (e.getKey() == e.ENTER) {
									try {
										field.listKeyNav.selectHighlighted(e);

										this.delegate.cmfg('onSessionExpiredLoginButtonClick');
									} catch (e) {
										_error('error setting the group', this);
									}
								}
							}
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
