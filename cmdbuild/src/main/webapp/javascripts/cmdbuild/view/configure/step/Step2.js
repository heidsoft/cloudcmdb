(function () {

	Ext.define('CMDBuild.view.configure.step.Step2',{
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.Configure',
			'CMDBuild.core.Utils'
		],

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		createSharkSchemaCheckbox: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		databaseUserNameField: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		databaseUserPasswordField: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		databaseUserTypeCombobox: undefined,

		border: false,
		bodyCls: 'cmdb-blue-panel-no-padding',
		frame: false,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.dbTypeAndNameFieldSet = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.cmdbuildDatabase,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.DATABASE_TYPE,
								fieldLabel: CMDBuild.Translation.type,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								valueField: CMDBuild.core.constants.Proxy.VALUE,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								editable: false,

								store: CMDBuild.proxy.Configure.getStoreDbTypes(),
								queryMode: 'local',

								listeners: {
									scope: this,
									change: function (combo, newValue, oldValue, eOpts) {
										this.delegate.cmfg('onConfigurationViewportWizardDbTypeChange');
									}
								}
							}),
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.DATABASE_NAME,
								fieldLabel: CMDBuild.Translation.name,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								allowBlank: false
							}),
							this.createSharkSchemaCheckbox = Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.CREATE_SHARK_SCHEMA,
								fieldLabel: CMDBuild.Translation.createSharkSchema,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								inputValue: true,
								uncheckedValue: false
							})
						]
					}),
					this.dbConnectionFieldset = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.databaseConnection
							+ ' (' + CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.JDBC_DRIVER_VERSION) +')',

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.CONNECTION_HOST,
								fieldLabel: CMDBuild.Translation.host,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								allowBlank: false
							}),
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.CONNECTION_PORT,
								fieldLabel: CMDBuild.Translation.port,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								allowBlank: false
							}),
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.CONNECTION_USER,
								fieldLabel: CMDBuild.Translation.superUser,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								allowBlank: false
							}),
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.CONNECTION_PASSWORD,
								fieldLabel: CMDBuild.Translation.password,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								inputType:'password',
								allowBlank: true
							}),
							Ext.create('Ext.container.Container', {
								margin: '0 0 5 ' + (CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE + 5),

								items: [
									Ext.create('CMDBuild.core.buttons.text.TestConnection', {
										scope: this,

										handler: function (button, e) {
											this.delegate.cmfg('onConfigurationViewportWizardConnectionCheckButtonClick');
										}
									})
								]
							})
						]
					}),
					this.userFieldSet = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.createRestrictedDatabaseUser,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							this.databaseUserTypeCombobox = Ext.create('Ext.form.field.ComboBox',{
								name: CMDBuild.core.constants.Proxy.DATABASE_USER_TYPE,
								fieldLabel: CMDBuild.Translation.userType,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								valueField: CMDBuild.core.constants.Proxy.VALUE,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								disablePanelFunctions: true,
								editable: false,

								store: CMDBuild.proxy.Configure.getStoreUserType(),
								queryMode: 'local',

								listeners: {
									scope: this,
									change: function (combo, newValue, oldValue, eOpts) {
										this.delegate.cmfg('onConfigurationViewportWizardUserTypeChange');
									}
								}
							}),
							this.databaseUserNameField = Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.DATABASE_USER_NAME,
								fieldLabel: CMDBuild.Translation.user,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								allowBlank: false
							}),
							this.databaseUserPasswordField = Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.DATABASE_USER_PASSWORD,
								fieldLabel: CMDBuild.Translation.password,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								inputType:'password',
								allowBlank: false
							}),
							this.databaseUserPasswordConfirmationField = Ext.create('Ext.form.field.Text', {
								fieldLabel: CMDBuild.Translation.confirmPassword,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURE,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURE_MEDIUM,
								inputType:'password',
								vtype: 'password',
								twinFieldId: this.databaseUserPasswordField.getId(),
								submitValue: false
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onConfigurationViewportWizardPanelShow', {
					displayNextButton: true,
					displayPreviusButton: true
				});
			}
		}
	});

})();
