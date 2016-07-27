(function () {

	Ext.define('CMDBuild.controller.common.field.comboBox.Searchable', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.comboBox.Searchable'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.searchWindow.SearchWindow}
		 */
		controllerSearchWindow: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldComboBoxSearchableNormalizeValue',
			'fieldComboBoxSearchableStoreExceedsLimit',
			'fieldComboBoxSearchableStoreGet = fieldStoreGet',
			'fieldComboBoxSearchableValueFieldGet = fiedlValueFieldGet',
			'fieldComboBoxSearchableValueGet = fieldValueGet',
			'fieldComboBoxSearchableValueSet = fieldValueSet',
			'onFieldComboBoxSearchableKeyUp',
			'onFieldComboBoxSearchableTrigger1Click',
			'onFieldComboBoxSearchableTrigger2Click',
			'onFieldComboBoxSearchableTrigger3Click',
			'onFieldComboBoxSearchableValueSet'
		],

		/**
		 * @property {CMDBuild.model.common.field.comboBox.searchable.Configuration}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Searchable}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.view.common.field.comboBox.Searchable} configurationObject.view
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.configurationSet({ value: this.view.configuration });

			// Controller build
			this.controllerSearchWindow = Ext.create('CMDBuild.controller.common.field.searchWindow.SearchWindow', { parentDelegate: this });
		},

		// Configuration property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			configurationGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			configurationSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.field.comboBox.searchable.Configuration';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @returns {Ext.data.Store}
		 */
		fieldComboBoxSearchableStoreGet: function () {
			return this.view.getStore();
		},

		/**
		 * @returns {Number}
		 */
		fieldComboBoxSearchableValueGet: function () {
			return this.view.getValue();
		},

		/**
		 * Recursive normalization of value
		 *
		 * @param {Mixed} value
		 *
		 * @returns {Mixed}
		 */
		fieldComboBoxSearchableNormalizeValue: function (value) {
			if (!Ext.isEmpty(value)) {
				switch (Ext.typeOf(value)) {
					case 'array':
						return this.cmfg('fieldComboBoxSearchableNormalizeValue', value[0]);

					case 'string':
						return isNaN(parseInt(value)) ? value : parseInt(value);

					case 'object': {
						if (Ext.isFunction(value.get))
							return this.cmfg('fieldComboBoxSearchableNormalizeValue', value.get(this.view.valueField));

						return this.cmfg('fieldComboBoxSearchableNormalizeValue', value[this.view.valueField]);
					}

					default:
						return value;
				}
			}

			return '';
		},

		/**
		 * @param {Ext.data.Model} selectedRecord
		 *
		 * @returns {Void}
		 */
		fieldComboBoxSearchableValueSet: function (selectedRecord) {
			if (!Ext.isEmpty(selectedRecord)) {
				this.view.blur(); // Allow 'change' event that occurs on blur
				this.view.setValue(selectedRecord.get(this.view.valueField));
			}
		},

		/**
		 * @returns {Boolean}
		 */
		fieldComboBoxSearchableStoreExceedsLimit: function () {
			if (!Ext.isEmpty(this.view.getStore()))
				return this.view.getStore().getTotalCount() > CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT);

			return false;
		},

		/**
		 * @returns {String}
		 */
		fieldComboBoxSearchableValueFieldGet: function () {
			return this.view.valueField;
		},

		/**
		 * @returns {Void}
		 */
		onFieldComboBoxSearchableKeyUp: function () {
			this.cmfg('onFieldComboBoxSearchableTrigger3Click', this.view.getRawValue());
		},

		/**
		 * If store has more than configuration limit records, no drop down but opens searchWindow
		 *
		 * @returns {Void}
		 */
		onFieldComboBoxSearchableTrigger1Click: function () {
			if (this.view.getStore().isLoading()) {
				this.view.getStore().on('load', this.trigger1Manager, this, { single: true });
			} else {
				this.trigger1Manager();
			}
		},

		/**
		 * @returns {Void}
		 */
		onFieldComboBoxSearchableTrigger2Click: function () {
			if (!this.view.isDisabled())
				this.view.setValue();
		},

		/**
		 * @param {String} value
		 *
		 * @returns {Void}
		 */
		onFieldComboBoxSearchableTrigger3Click: function (value) {
			value = Ext.isString(value) ? value : '';

			if (!this.view.isDisabled()) {
				// Get class data from server
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.common.field.comboBox.Searchable.readClass({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						var targetClassObject = Ext.Array.findBy(decodedResponse, function (item, i) {
							return item[CMDBuild.core.constants.Proxy.NAME] == this.view.attributeModel.get(CMDBuild.core.constants.Proxy.TARGET_CLASS);
						}, this);

						if (!Ext.isEmpty(targetClassObject)) {
							var configurationObject = {};
							configurationObject[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = Ext.create('CMDBuild.cache.CMEntryTypeModel', targetClassObject);
							configurationObject[CMDBuild.core.constants.Proxy.GRID_CONFIGURATION] = { presets: { quickSearch: value } };
							configurationObject[CMDBuild.core.constants.Proxy.READ_ONLY] = this.configurationGet(CMDBuild.core.constants.Proxy.READ_ONLY_SEARCH_WINDOW);

							this.controllerSearchWindow.cmfg('fieldSearchWindowConfigurationSet', { value: configurationObject });
							this.controllerSearchWindow.getView().show();
						}
					}
				});
			}
		},

		/**
		 * Adds values in store if not already inside
		 *
		 * @param {Mixed} value
		 *
		 * @returns {Void}
		 */
		onFieldComboBoxSearchableValueSet: function (value) {
			if (!Ext.isEmpty(value)) {
				value = this.cmfg('fieldComboBoxSearchableNormalizeValue', value);

				if (this.view.getStore().find(this.view.valueField, value) < 0) {
					var params = {};
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.view.attributeModel.get(CMDBuild.core.constants.Proxy.TARGET_CLASS);
					params[CMDBuild.core.constants.Proxy.CARD_ID] = value;

					CMDBuild.proxy.common.field.ForeignKey.readCard({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CARD];

							if (!Ext.isEmpty(decodedResponse)) {
								if (!Ext.isEmpty(this.view.getStore()))
									this.view.getStore().add(
										Ext.create('CMDBuild.model.common.attributes.ForeignKeyStore', {
											Id: decodedResponse['Id'],
											Description: decodedResponse['Description']
										})
									);

								this.view.setValue(decodedResponse[this.view.valueField]);
							}

							this.view.validate();
						}
					});
				}
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		trigger1Manager: function () {
			if (this.cmfg('fieldComboBoxSearchableStoreExceedsLimit')) {
				this.cmfg('onFieldComboBoxSearchableTrigger3Click');
			} else {
				this.view.onTriggerClick();
			}
		}
	});

})();
