(function () {

	Ext.define('CMDBuild.view.administration.configuration.DmsPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.configuration.Dms'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Dms}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		fieldSetAlfresco: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		fieldSetCmis: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

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
									this.delegate.cmfg('onConfigurationDmsSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onConfigurationDmsAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.ENABLED,
						fieldLabel: CMDBuild.Translation.enabled,
						labelAlign: 'left',
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION,
						inputValue: true,
						uncheckedValue: false
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.ALFRESCO_LOOKUP_CATEGORY,
						fieldLabel: CMDBuild.Translation.cmdbuildCategory,
						labelAlign: 'left',
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION,
						maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_MEDIUM,
						valueField: CMDBuild.core.constants.Proxy.ID,
						displayField: CMDBuild.core.constants.Proxy.TEXT, // TODO: waiting for refactor (rename)

						store: CMDBuild.proxy.configuration.Dms.getStoreLookups(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.Display', {
						fieldLabel: CMDBuild.Translation.serviceType,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION,
						disablePanelFunctions: true
					}),
					this.fieldSetAlfresco = Ext.create('Ext.form.FieldSet', {// Alfresco configuration
						title: CMDBuild.Translation.alfresco,
						checkboxName: CMDBuild.core.constants.Proxy.TYPE,
						checkboxToggle: true,
						checkboxValue: CMDBuild.core.constants.Proxy.ALFRESCO,
						collapsed: true,
						collapsible: true,
						toggleOnTitleClick: true,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.credentials,

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.ALFRESCO_USER,
										fieldLabel: CMDBuild.Translation.username,
										labelAlign: 'left',
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 20,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_MEDIUM
									}),
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.ALFRESCO_PASSWORD,
										fieldLabel: CMDBuild.Translation.password,
										labelAlign: 'left',
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 20,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_MEDIUM,
										inputType: 'password'
									})
								]
							}),
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.ftp,

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.ALFRESCO_FILE_SERVER_URL,
										fieldLabel: CMDBuild.Translation.host,
										labelAlign: 'left',
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 20,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
									}),
									Ext.create('Ext.form.field.Number', {
										name: CMDBuild.core.constants.Proxy.ALFRESCO_FILE_SERVER_PORT,
										fieldLabel: CMDBuild.Translation.port,
										labelAlign: 'left',
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 20,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_MEDIUM,
										minValue: 0,
										maxValue: 65535
									}),
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.ALFRESCO_REPOSITORY_FILE_SERVER_PATH,
										fieldLabel: CMDBuild.Translation.fileServerPath,
										labelAlign: 'left',
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 20,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
									}),
									Ext.create('Ext.form.field.Number', {
										name: CMDBuild.core.constants.Proxy.ALFRESCO_DELAY,
										fieldLabel: CMDBuild.Translation.settleTimeMs,
										labelAlign: 'left',
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 20,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_MEDIUM,
										minValue: 0
									})
								]
							}),
							Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.webServices,

								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: [
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.ALFRESCO_HOST,
										fieldLabel: CMDBuild.Translation.host,
										labelAlign: 'left',
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 20,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
									}),
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.ALFRESCO_REPOSITORY_WEB_SERVICE_PATH,
										fieldLabel: CMDBuild.Translation.webServicePath,
										labelAlign: 'left',
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 20,
										maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
									}),
									Ext.create('Ext.form.field.Text', {
										name: CMDBuild.core.constants.Proxy.ALFRESCO_REPOSITORY_APPLICATION,
										fieldLabel: CMDBuild.Translation.application,
										labelAlign: 'left',
										labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 20
									})
								]
							})
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.delegate.cmfg('onConfigurationDmsFieldSetExpand', field.checkboxValue);
							}
						}
					}),
					this.fieldSetCmis = Ext.create('Ext.form.FieldSet', { // CMIS configuration
						title: CMDBuild.Translation.cmis,
						checkboxName: CMDBuild.core.constants.Proxy.TYPE,
						checkboxToggle: true,
						checkboxValue: CMDBuild.core.constants.Proxy.CMIS,
						collapsed: true,
						collapsible: true,
						toggleOnTitleClick: true,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.CMIS_HOST,
								fieldLabel: CMDBuild.Translation.host,
								labelAlign: 'left',
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 10,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
							}),
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.CMIS_PATH,
								fieldLabel: CMDBuild.Translation.webServicePath,
								labelAlign: 'left',
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 10,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
							}),
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.CMIS_USER,
								fieldLabel: CMDBuild.Translation.username,
								labelAlign: 'left',
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 10,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_MEDIUM
							}),
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.CMIS_PASSWORD,
								fieldLabel: CMDBuild.Translation.password,
								labelAlign: 'left',
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 10,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_MEDIUM,
								inputType: 'password'
							}),
							Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.CMIS_MODEL,
								fieldLabel: CMDBuild.Translation.presets,
								labelAlign: 'left',
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL_CONFIGURATION - 10,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_MEDIUM,
								valueField: CMDBuild.core.constants.Proxy.ID,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,

								store: CMDBuild.proxy.configuration.Dms.getStorePresets(),
								queryMode: 'local'
							})
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.delegate.cmfg('onConfigurationDmsFieldSetExpand', field.checkboxValue);
							}
						}
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onConfigurationDmsTabShow');
			}
		},

		/**
		 * Custom fields set value manage
		 *
		 * @param {CMDBuild.model.configuration.dms.Dms} record
		 *
		 * @override
		 */
		loadRecord: function (record) {
			this.callParent(arguments);

			// FieldSet state manage
			this.fieldSetCmis.collapse();
			this.fieldSetAlfresco.collapse();

			switch (record.get(CMDBuild.core.constants.Proxy.TYPE)) {
				case CMDBuild.core.constants.Proxy.CMIS:
					return this.fieldSetCmis.expand();

				case CMDBuild.core.constants.Proxy.ALFRESCO:
				default:
					return this.fieldSetAlfresco.expand();
			}
		}
	});

})();
