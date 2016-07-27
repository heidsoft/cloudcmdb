(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep3Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormConnectorController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.connector.CMStep3}
		 */
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @override
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onDbFieldsetExpand':
					return this.onDbFieldsetExpand();

				case 'onLdapFieldsetExpand':
					return this.onLdapFieldsetExpand();

				case 'onSelectDbType':
					return this.onSelectDbType(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			/**
			 * @return {Mixed} dataSourceType or false
			 */
			getTypeDataSource: function() {
				if (this.view.dbFieldset.checkboxCmp.getValue())
					return CMDBuild.core.constants.Proxy.DB;

				if (this.view.ldapFieldset.checkboxCmp.getValue())
					return CMDBuild.core.constants.Proxy.LDAP;

				return false;
			},

		onDbFieldsetExpand: function() {
			this.view.ldapFieldset.collapse();
			this.view.ldapFieldset.reset();
		},

		onLdapFieldsetExpand: function() {
			this.view.dbFieldset.collapse();
			this.view.dbFieldset.reset();
		},

		/**
		 * To enable/disable dbInstanceNameField
		 *
		 * @param {String} selectedValue
		 */
		onSelectDbType: function(selectedValue) {
			this.view.dbInstanceNameField.setDisabled(
				!(selectedValue == CMDBuild.core.constants.Proxy.MYSQL)
			);
		},

		// SETters functions
			/**
			 * @param {String} dataSourceType
			 * @param {Object} configurationObject
			 */
			setValueDataSourceConfiguration: function(dataSourceType, configurationObject) {
				if (!Ext.Object.isEmpty(configurationObject))
					switch (dataSourceType) {
						case CMDBuild.core.constants.Proxy.DB: {
							this.view.dbFieldset.expand();

							this.view.dbTypeCombo.setValue(configurationObject[CMDBuild.core.constants.Proxy.DATASOURCE_DB_TYPE]);
							this.view.dbAddressField.setValue(configurationObject[CMDBuild.core.constants.Proxy.DATASOURCE_ADDRESS]);
							this.view.dbPortField.setValue(configurationObject[CMDBuild.core.constants.Proxy.DATASOURCE_DB_PORT]);
							this.view.dbNameField.setValue(configurationObject[CMDBuild.core.constants.Proxy.DATASOURCE_DB_NAME]);
							this.view.dbInstanceNameField.setValue(configurationObject[CMDBuild.core.constants.Proxy.DATASOURCE_DB_INSATANCE_NAME]);
							this.view.dbUsernameField.setValue(configurationObject[CMDBuild.core.constants.Proxy.DATASOURCE_DB_USERNAME]);
							this.view.dbPasswordField.setValue(configurationObject[CMDBuild.core.constants.Proxy.DATASOURCE_DB_PASSWORD]);
							this.view.dbSourceFilterField.setValue(configurationObject[CMDBuild.core.constants.Proxy.DATASOURCE_TABLE_VIEW_PREFIX]);
						} break;

						default:
							_error('CMTasksFormConnectorController: onSaveButtonClick() datasource type not recognized');
					}
			},

		/**
		 * Set dataSource configuration fields as required/unrequired
		 *
		 * @param {Boolean} enable
		 */
		validate: function(enable) {
			this.view.dbTypeCombo.allowBlank = !enable;
			this.view.dbAddressField.allowBlank = !enable;

			this.view.dbPortField.allowBlank = !enable;
			this.view.dbPortField.setMinValue(enable ? 1 : 0);

			this.view.dbNameField.allowBlank = !enable;
			this.view.dbUsernameField.allowBlank = !enable;
			this.view.dbPasswordField.allowBlank = !enable;
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep3', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.taskManager.Connector'
		],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.connector.CMStep3Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		dbAddressField: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		dbFieldset: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		dbInstanceNameField: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		dbNameField: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		dbPasswordField: undefined,

		/**
		 * @property {Ext.form.field.Number}
		 */
		dbPortField: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		dbSourceFilterField: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		dbTypeCombo: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		dbUsernameField: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep3Delegate', this);

			// DataSource: relationa databases configuration
				this.dbTypeCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_TYPE,
					fieldLabel: CMDBuild.Translation.administration.tasks.type,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					displayField: CMDBuild.core.constants.Proxy.VALUE,
					valueField: CMDBuild.core.constants.Proxy.KEY,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					forceSelection: true,
					editable: false,
					anchor: '100%',

					store: CMDBuild.proxy.taskManager.Connector.getStoreDbTypes(),
					queryMode: 'local',

					listeners: {
						select: function(combo, records, options) {
							me.delegate.cmOn('onSelectDbType', this.getValue());
						}
					}
				});

				this.dbAddressField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.DATASOURCE_ADDRESS,
					fieldLabel: CMDBuild.Translation.address,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
					anchor: '100%'
				});

				this.dbPortField = Ext.create('Ext.form.field.Number', {
					name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_PORT,
					fieldLabel: CMDBuild.Translation.port,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					minValue: 1,
					maxValue: 65535,
					maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
					anchor: '100%'
				});

				this.dbNameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_NAME,
					fieldLabel: tr.dbName,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
					anchor: '100%'
				});

				this.dbInstanceNameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_INSATANCE_NAME,
					fieldLabel: tr.instanceName,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
					anchor: '100%'
				});

				this.dbUsernameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_USERNAME,
					fieldLabel: CMDBuild.Translation.username,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
					anchor: '100%'
				});

				this.dbPasswordField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.DATASOURCE_DB_PASSWORD,
					inputType: 'password',
					fieldLabel: CMDBuild.Translation.password,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
					anchor: '100%'
				});

				this.dbSourceFilterField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.DATASOURCE_TABLE_VIEW_PREFIX,
					fieldLabel: tr.sourceFilter,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
					anchor: '100%'
				});

				this.dbFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.dataSourceDbFieldset,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,

					items: [
						this.dbTypeCombo,
						this.dbAddressField,
						this.dbPortField,
						this.dbNameField,
						this.dbInstanceNameField,
						this.dbUsernameField,
						this.dbPasswordField,
						this.dbSourceFilterField
					],

					listeners: {
						beforeexpand: function(fieldset, eOpts) {
							me.delegate.cmOn('onDbFieldsetExpand');
						}
					}
				});

				this.dbFieldset.fieldWidthsFix();
			// END - DataSource: relationa databases configuration

			// DataSource: LDAP configuration
				this.ldapAddressField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.ADDRESS,
					fieldLabel: CMDBuild.Translation.address,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					width: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
					allowBlank: false
				});

				this.ldapUsernameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.USERNAME,
					fieldLabel: CMDBuild.Translation.username,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					width: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
					allowBlank: false
				});

				this.ldapPasswordField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.PASSWORD,
					inputType: 'password',
					fieldLabel: CMDBuild.Translation.password,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					width: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
					allowBlank: false
				});

				this.ldapFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.dataSourceLdapFieldset,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,

					items: [
						this.ldapAddressField,
						this.ldapUsernameField,
						this.ldapPasswordField
					],

					listeners: {
						beforeexpand: function(fieldset, eOpts) {
							me.delegate.cmOn('onLdapFieldsetExpand');
						}
					}
				});

				this.ldapFieldset.fieldWidthsFix();
			// END - DataSource: LDAP configuration

			Ext.apply(this, {
				items: [
					this.dbFieldset
// TODO: future implementation
//					,
//					this.ldapFieldset
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Disable instanceNameField
			 *
			 * @param {Object} view
			 * @param {Object} eOpts
			 */
			activate: function(view, eOpts) {
				this.dbInstanceNameField.setDisabled(true);
			}
		}
	});

})();