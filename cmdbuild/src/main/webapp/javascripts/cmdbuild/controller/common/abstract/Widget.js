(function () {

	// External implementation to avoid overrides
	Ext.require(['CMDBuild.core.constants.Proxy']);

	/**
	 * Class to be extended in widget controllers to adapt CMDBuild.controller.common.abstract.Base functionalities
	 *
	 * @requires Mandatory managed methods:
	 * 	- beforeActiveView
	 * 	- beforeHideView
	 * 	- isValid
	 * 	- onBeforeSave
	 * 	- onEditMode
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.abstract.Widget', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Ext.data.Model or CMDBuild.model.CMActivityInstance}
		 */
		card: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Boolean}
		 */
		enableDelegateApply: true,

		/**
		 * @cfg {Boolean}
		 */
		enableWidgetConfigurationSetup: true,

		/**
		 * Multiple widget instances data storage buffer
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		instancesDataStorage: {},

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * Plain widget configuration object
		 *
		 * @property {Object}
		 */
		widgetConfiguration: undefined,

		/**
		 * Widget configuration model built with WidgetConfiguration methods
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		widgetConfigurationModel: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: undefined,

		statics: {
			/**
			 * Old implementation to be used in new widgets
			 *
			 * @param {Object} model
			 *
			 * @returns {Object} out
			 */
			getTemplateResolverServerVars: function (model) {
				var out = {};
				var pi = null;

				if (!Ext.isEmpty(model)) {
					if (Ext.getClassName(model) == 'CMDBuild.model.CMActivityInstance') {
						// Retrieve the process instance because it stores the data. this.card has only the varibles to show in this step (is the activity instance)
						pi = _CMWFState.getProcessInstance();
					} else if (Ext.getClassName(model) == 'CMDBuild.model.CMProcessInstance') {
						pi = model;
					}

					if (!Ext.isEmpty(pi) && Ext.isFunction(pi.getValues)) { // The processes use a new serialization. Add backward compatibility attributes to the card values
						out = Ext.apply({
							'Id': pi.get('Id'),
							'IdClass': pi.get('IdClass'),
							'IdClass_value': pi.get('IdClass_value')
						}, pi.getValues());
					} else {
						out = model.raw || model.data;
					}
				}

				return out;
			}
		},

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} configurationObject.parentDelegate
		 * @param {CMDBuild.model.CMActivityInstance} configurationObject.card
		 * @param {Ext.form.Basic} configurationObject.clientForm
		 * @param {Mixed} configurationObject.view
		 * @param {Object} configurationObject.widgetConfiguration
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			if (
				!Ext.Object.isEmpty(configurationObject)
				&& !Ext.isEmpty(configurationObject.view)
				&& !Ext.Object.isEmpty(configurationObject.widgetConfiguration)
			) {
				// Add default managed functions
				this.cmfgCatchedFunctions.push('getLabel');

				this.callParent(arguments);

				// Setup widget configuration
				if (this.enableWidgetConfigurationSetup)
					this.widgetConfigurationSet({ value: this.widgetConfiguration }); // Setup widget configuration model

				// Inject delegate to view
				if (this.enableDelegateApply)
					this.view.delegate = this;
			} else {
				_error('wrong configuration object or empty widget view', this, configurationObject);
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @abstract
		 */
		beforeActiveView: function () {
			// Setup widgetConfiguration on widget view activation to switch configuration on multiple instances
			if (!Ext.isEmpty(this.widgetConfiguration))
				this.widgetConfigurationSet({ value: this.widgetConfiguration });
		},

		/**
		 * Executed before window hide perform
		 *
		 * @returns {Void}
		 *
		 * @abstract
		 */
		beforeHideView: Ext.emptyFn,

		/**
		 * @returns {Object or null}
		 *
		 * @abstract
		 */
		getData: function () {
			return null;
		},

		/**
		 * @param {String} mode
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		getId: function (mode) {
			switch (mode) {
				// Generates a unique ID for widget related to card data. This mode is mainly used from InstancesDataStorage methods.
				case 'unique':
					return this.card.data[CMDBuild.core.constants.Proxy.ID] + '-' + this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.ID);

				// Original widget ID generated from server
				case 'strict':
				default:
					return this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.ID);
			}
		},

		/**
		 * @returns {String}
		 */
		getLabel: function () {
			return this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.LABEL);
		},

		/**
		 * @returns {Object}
		 */
		getTemplateResolverServerVars: function () {
			if (!Ext.isEmpty(this.card))
				return this.statics().getTemplateResolverServerVars(this.card);

			return {};
		},

		// InstancesDataStorage methods (multiple widget instances support)
			/**
			 * @returns {Boolean}
			 */
			instancesDataStorageExists: function () {
				if (!Ext.isEmpty(this.getId('unique')))
					return this.instancesDataStorage.hasOwnProperty(this.getId('unique'));

				return false;
			},

			/**
			 * @returns {Mixed} or null
			 */
			instancesDataStorageGet: function () {
				if (!Ext.isEmpty(this.getId('unique')) && !Ext.isEmpty(this.instancesDataStorage[this.getId('unique')]))
					return this.instancesDataStorage[this.getId('unique')];

				return null;
			},

			/**
			 * @returns {Boolean}
			 */
			instancesDataStorageIsEmpty: function () {
				if (!Ext.isEmpty(this.getId('unique')))
					return Ext.isEmpty(this.instancesDataStorage[this.getId('unique')]);

				return true;
			},

			/**
			 * @param {String} mode ['full' || 'single']
			 *
			 * @returns {Void}
			 */
			instancesDataStorageReset: function (mode) {
				mode = !Ext.isEmpty(mode) && Ext.isString(mode) ? mode : 'full';

				switch (mode) {
					case 'single':
						return this.instancesDataStorage[this.getId('unique')] = null;

					case 'full':
					default:
						return this.instancesDataStorage = {};
				}
			},

			/**
			 * @param {Mixed} instanceData
			 *
			 * @returns {Void}
			 */
			instancesDataStorageSet: function (instanceData) {
				if (!Ext.isEmpty(this.getId('unique')))
					this.instancesDataStorage[this.getId('unique')] = instanceData;
			},

		/**
		 * @returns {Boolean}
		 *
		 * @abstract
		 */
		isValid: function () {
			return true;
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 */
		onBeforeSave: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& Ext.isFunction(parameters.callback)
			) {
				Ext.callback(
					parameters.callback,
					Ext.isEmpty(parameters.scope) ? this : parameters.scope
				);
			} else {
				_error('[' + this.getLabel() + '] onBeforeSave invalid parameters', this, parameters);
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @abstract
		 */
		onEditMode: Ext.emptyFn,

		// WidgetConfiguration methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed}
			 */
			widgetConfigurationGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'widgetConfigurationModel';

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			widgetConfigurationIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'widgetConfigurationModel';

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 * @param {String} parameters.modelName
			 * @param {Object} parameters.value
			 * @param {String} parameters.propertyName
			 *
			 * @returns {Mixed}
			 *
			 * @abstract
			 */
			widgetConfigurationSet: function (parameters) {
				if (Ext.isEmpty(this.widgetConfigurationModelClassName) || !Ext.isString(this.widgetConfigurationModelClassName))
					return _error('widgetConfigurationModelClassName parameter not configured', this);

				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'widgetConfigurationModel';
				parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = this.widgetConfigurationModelClassName;
				parameters[CMDBuild.core.constants.Proxy.VALUE] = Ext.clone(parameters[CMDBuild.core.constants.Proxy.VALUE]);

				return this.propertyManageSet(parameters);
			},

		// WidgetCntroller methods
			/**
			 * @param {String} propertyName
			 *
			 * @returns {Mixed}
			 */
			widgetControllerPropertyGet: function (propertyName) {
				if (!Ext.isEmpty(this[propertyName]))
					return this[propertyName];

				return null;
			}
	});

})();
