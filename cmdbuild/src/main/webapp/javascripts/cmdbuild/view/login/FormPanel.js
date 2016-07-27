(function () {

	Ext.define('CMDBuild.view.login.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.login.Login}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		group: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		password: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		user: undefined,

		border: true,
		frame: true,
		padding: 10,
		title: CMDBuild.Translation.loginTitle,

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
							Ext.create('CMDBuild.core.buttons.text.Login', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onLoginViewportLoginButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('CMDBuild.view.common.field.comboBox.Language', {
						fieldLabel: CMDBuild.Translation.language,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_LOGIN,
						hidden: !CMDBuild.configuration.localization.get(CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT),
						submitValue: false
					}),
					this.user = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.USERNAME,
						fieldLabel: CMDBuild.Translation.username,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_LOGIN,
						allowBlank: false,

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onLoginViewportUserChange');
							},
							specialkey: function (field, e, eOpts) {
								if (e.getKey() == e.ENTER)
									this.delegate.cmfg('onLoginViewportLoginButtonClick');
							}
						}
					}),
					this.password = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.PASSWORD,
						fieldLabel: CMDBuild.Translation.password,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_LOGIN,
						inputType: 'password',
						allowBlank: false,

						listeners: {
							scope: this,
							specialkey: function (field, e, eOpts) {
								if (e.getKey() == e.ENTER)
									this.delegate.cmfg('onLoginViewportLoginButtonClick');
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

										this.delegate.cmfg('onLoginViewportLoginButtonClick');
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
