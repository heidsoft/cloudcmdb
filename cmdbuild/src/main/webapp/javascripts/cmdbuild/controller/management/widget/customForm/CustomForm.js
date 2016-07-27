(function () {

	Ext.define('CMDBuild.controller.management.widget.customForm.CustomForm', {
		extend: 'CMDBuild.controller.common.abstract.Widget',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.Utils',
			'CMDBuild.proxy.widget.customForm.CustomForm'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Boolean}
		 *
		 * @private
		 */
		alreadyDisplayed: false,

		/**
		 * @property {CMDBuild.model.CMActivityInstance or Ext.data.Model}
		 */
		card: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @property {Mixed}
		 */
		controllerLayout: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'getId = widgetCustomFormIdGet',
			'getTemplateResolverServerVars = widgetCustomFormGetTemplateResolverServerVars',
			'instancesDataStorageExists = widgetCustomFormInstancesDataStorageExists',
			'instancesDataStorageGet = widgetCustomFormInstancesDataStorageGet',
			'instancesDataStorageIsEmpty = widgetCustomFormInstancesDataStorageIsEmpty',
			'onWidgetCustomFormBeforeActiveView = beforeActiveView',
			'onWidgetCustomFormBeforeHideView = beforeHideView',
			'onWidgetCustomFormBeforeSave = onBeforeSave',
			'onWidgetCustomFormEditMode = onEditMode',
			'onWidgetCustomFormResetButtonClick',
			'widgetConfigurationGet = widgetCustomFormConfigurationGet',
			'widgetConfigurationIsEmpty = widgetCustomFormConfigurationIsEmpty',
			'widgetControllerPropertyGet = widgetCustomFormControllerPropertyGet',
			'widgetCustomFormAlreadyDisplayedGet',
			'widgetCustomFormGetData = getData',
			'widgetCustomFormIsValid = isValid',
			'widgetCustomFormLayoutDataGet -> controllerLayout',
			'widgetCustomFormLayoutIsValid -> controllerLayout',
			'widgetCustomFormModelStoreBuilder',
			'widgetCustomFormViewSetLoading'
		],

		/**
		 * @property {CMDBuild.view.management.widget.customForm.CustomFormView}
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: 'CMDBuild.model.widget.customForm.Configuration',

		// AlreadyDisplayed property methods
			/**
			 * @returns {Boolean} alreadyDisplayed
			 *
			 * @private
			 */
			isAlreadyDisplayed: function () {
				return this.alreadyDisplayed;
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			alreadyDisplayedSet: function () {
				this.alreadyDisplayed = true;
			},

		/**
		 * @param {Array or String} target
		 *
		 * @returns {Array} decodedOutput
		 *
		 * @private
		 */
		applyTemplateResolverToArray: function (target) {
			target = CMDBuild.core.Utils.isJsonString(target) ? Ext.decode(target) : target;
			target = Ext.isArray(target) ? target : [target];

			var decodedOutput = [];

			Ext.Array.forEach(target, function (object, i, allObjects) {
				var templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: object,
					serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
				});

				templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(object),
					scope: this,
					callback: function (out, ctx) {
						decodedOutput.push(out);

						Ext.Object.each(templateResolver.getLocalDepsAsField(), function (name, field, myself) {
							if (Ext.isEmpty(field.observerControllers))
								field.observerControllers = [];

							field.observerControllers = Ext.Array.merge(field.observerControllers, [this]); // Alias of add if unique
						}, this);

						// Apply change event to reset data property in widgetConfiguration to enable SQL function server call
						templateResolver.bindLocalDepsChange(function (field) {
							if (!Ext.isEmpty(field.observerControllers) && Ext.isArray(field.observerControllers))
								Ext.Array.each(field.observerControllers, function (observer, i, allObservers) {
									observer.instancesDataStorageReset('single'); // Reset widget instance data storage
								}, this);
						}, this);
					}
				});
			}, this);

			return decodedOutput;
		},

		/**
		 * @param {Object or String} target
		 *
		 * @returns {Object} decodedOutput
		 *
		 * @private
		 */
		applyTemplateResolverToObject: function (target) {
			target = CMDBuild.core.Utils.isJsonString(target) ? Ext.decode(target) : target;

			var decodedOutput = {};

			if (Ext.isObject(target)) {
				var templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: target,
					serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
				});

				templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(target),
					scope: this,
					callback: function (out, ctx) {
						decodedOutput = out;

						Ext.Object.each(templateResolver.getLocalDepsAsField(), function (name, field, myself) {
							if (Ext.isEmpty(field.observerControllers))
								field.observerControllers = [];

							field.observerControllers = Ext.Array.merge(field.observerControllers, [this]); // Alias of add if unique
						}, this);

						// Apply change event to reset data property in widgetConfiguration to enable SQL function server call
						templateResolver.bindLocalDepsChange(function (field) {
							if (!Ext.isEmpty(field.observerControllers) && Ext.isArray(field.observerControllers))
								Ext.Array.each(field.observerControllers, function (observer, i, allObservers) {
									observer.instancesDataStorageReset('single'); // Reset widget instance data storage
								}, this);
						}, this);
					}
				});
			}

			return decodedOutput;
		},

		/**
		 * @param {String} property
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		applyTemplateResolverToConfigurationProperty: function (property) {
			if (!Ext.isEmpty(property) && Ext.isString(property)) {
				switch (Ext.typeOf(this.widgetConfiguration[property])) {
					case 'array':
						return this.widgetConfigurationSet({
							propertyName: property,
							value: this.applyTemplateResolverToArray(this.widgetConfiguration[property])
						});

					case 'object':
						return this.widgetConfigurationSet({
							propertyName: property,
							value: this.applyTemplateResolverToObject(this.widgetConfiguration[property])
						});
				}
			} else {
				_error('empty property parameter name', this);
			}
		},

		/**
		 * Builds layout controller and inject view
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildLayout: function () {
			if (!this.cmfg('widgetCustomFormConfigurationIsEmpty', CMDBuild.core.constants.Proxy.MODEL)) {
				switch (this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.LAYOUT)) {
					case 'form': {
						this.controllerLayout = Ext.create('CMDBuild.controller.management.widget.customForm.layout.Form', { parentDelegate: this });
					} break;

					case 'grid':
					default: {
						this.controllerLayout = Ext.create('CMDBuild.controller.management.widget.customForm.layout.Grid', { parentDelegate: this });
					}
				}

				// Add related layout panel
				if (!Ext.isEmpty(this.view)) {
					this.view.removeAll();
					this.view.add(this.controllerLayout.getView());
				}

				this.controllerLayout.cmfg('onWidgetCustomFormShow');

				this.alreadyDisplayedSet();
			} else {
				_error('empty model configuration parameter', this);
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 * @param {Function} parameters.success
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		executeConfigurationSqlFunction: function (parameters) {
			if (
				!this.cmfg('widgetCustomFormConfigurationIsEmpty', CMDBuild.core.constants.Proxy.FUNCTION_DATA)
				&& this.isRefreshNeeded()
			) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.FUNCTION] = this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.FUNCTION_DATA);
				params[CMDBuild.core.constants.Proxy.PARAMS] = Ext.encode(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.VARIABLES));

				CMDBuild.proxy.widget.customForm.CustomForm.readFromFunctions({
					params: params,
					scope: Ext.isEmpty(parameters.scope) ? this : parameters.scope,
					callback: Ext.isFunction(parameters.callback) ? parameters.callback : Ext.emptyFn,
					success: Ext.isFunction(parameters.success) ? parameters.success : Ext.emptyFn
				});
			}
		},

		/**
		 * Refresh behaviour manage method
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isRefreshNeeded: function () {
			switch (
				this.cmfg('widgetCustomFormConfigurationGet', [
					CMDBuild.core.constants.Proxy.CAPABILITIES,
					CMDBuild.core.constants.Proxy.REFRESH_BEHAVIOUR
				])
			) {
				case 'firstTime':
					return !this.isAlreadyDisplayed();

				case 'everyTime':
				default:
					return true;
			}
		},

		/**
		 * Execute template resolver on variables property and SQL function to get data
		 *
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		manageSqlFunctionAsDataSource: function (parameters) {
			if (
				this.cmfg('widgetCustomFormInstancesDataStorageIsEmpty') // Local store buffer is empty
				&& !this.cmfg('widgetCustomFormConfigurationIsEmpty', CMDBuild.core.constants.Proxy.FUNCTION_DATA)
			) {
				this.applyTemplateResolverToConfigurationProperty(CMDBuild.core.constants.Proxy.VARIABLES);

				// Build data configurations from function definition
				this.executeConfigurationSqlFunction({
					scope: Ext.isEmpty(parameters.scope) ? this : parameters.scope,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CARDS];

						// Save function response to instance data storage
						this.instancesDataStorageSet(decodedResponse);
					},
					callback: Ext.isFunction(parameters.callback) ? parameters.callback : Ext.emptyFn
				});
			} else {
				Ext.callback(
					Ext.isFunction(parameters.callback) ? parameters.callback : Ext.emptyFn,
					Ext.isEmpty(parameters.scope) ? this : parameters.scope
				);
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onWidgetCustomFormBeforeActiveView: function () {
			this.beforeActiveView(arguments); // CallParent alias

			// Create buffer with data configuration parameter if exists
			if (!this.instancesDataStorageExists())
				this.instancesDataStorageSet(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.DATA));

			// Execute template resolver on model properties
			this.applyTemplateResolverToConfigurationProperty(CMDBuild.core.constants.Proxy.MODEL);
			this.applyTemplateResolverToConfigurationProperty(CMDBuild.core.constants.Proxy.VARIABLES);

			// Manage SQL function as data source
			this.manageSqlFunctionAsDataSource({
				scope: this,
				callback: this.buildLayout
			});
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onWidgetCustomFormBeforeSave: function (parameters) {
			// Manage SQL function as data source
			this.manageSqlFunctionAsDataSource({
				scope: this,
				callback: function () {
					this.controllerLayout.cmfg('onWidgetCustomFormShow');

					this.onBeforeSave(parameters); // CallParent alias
				}
			});
		},

		/**
		 * Preset data in instanceDataStorage variable
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onWidgetCustomFormEditMode: function () {
			this.instancesDataStorageSet(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.DATA));

			this.cmfg('onWidgetCustomFormBeforeActiveView');

			this.onEditMode(arguments); // CallParent alias
		},

		/**
		 * @returns {Void}
		 */
		onWidgetCustomFormResetButtonClick: function () {
			if (!this.cmfg('widgetCustomFormConfigurationIsEmpty', CMDBuild.core.constants.Proxy.DATA)) { // Refill widget with data configuration
				this.instancesDataStorageSet(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.DATA));

				this.controllerLayout.cmfg('onWidgetCustomFormShow');
			} else if (!this.cmfg('widgetCustomFormConfigurationIsEmpty', CMDBuild.core.constants.Proxy.FUNCTION_DATA)) { // Get data from function
				this.applyTemplateResolverToConfigurationProperty(CMDBuild.core.constants.Proxy.VARIABLES);

				this.executeConfigurationSqlFunction({
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CARDS];

						this.instancesDataStorageSet(decodedResponse);

						this.controllerLayout.cmfg('onWidgetCustomFormShow');
					}
				});
			}
		},

		/**
		 * Save data in storage attribute
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onWidgetCustomFormBeforeHideView: function () {
			this.instancesDataStorageSet(this.cmfg('widgetCustomFormLayoutDataGet'));

			this.beforeHideView(arguments); // CallParent alias
		},

		/**
		 * @returns {Object} output
		 *
		 * @override
		 */
		widgetCustomFormGetData: function () {
			var output = {};
			output[CMDBuild.core.constants.Proxy.OUTPUT] = [];

			this.controllerLayout.cmfg('onWidgetCustomFormShow'); // Force widget layout build

			// Uses direct data property access to avoid a get problem because of generic model
			Ext.Array.each(this.cmfg('widgetCustomFormLayoutDataGet'), function (rowObject, i, allRowObjects) {
				var dataObject = Ext.isEmpty(rowObject.data) ? rowObject : rowObject.data; // Model/Objects management

				new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: dataObject,
					serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
				}).resolveTemplates({
					attributes: Ext.Object.getKeys(dataObject),
					scope: this,
					callback: function (out, ctx) {
						if (Ext.isObject(out))
							output[CMDBuild.core.constants.Proxy.OUTPUT].push(Ext.encode(out));
					}
				});
			}, this);

			return output;
		},

		/**
		 * @returns {Boolean}
		 *
		 * @override
		 */
		widgetCustomFormIsValid: function () {
			return Ext.isEmpty(this.controllerLayout) ? this.isValid() : this.cmfg('widgetCustomFormLayoutIsValid');
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		widgetCustomFormModelStoreBuilder: function () {
			var columnsData = [];

			Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function (attributeModel, i, allAttributeModels) {
				if (!Ext.isEmpty(attributeModel))
					columnsData.push([
						attributeModel.get(CMDBuild.core.constants.Proxy.NAME),
						attributeModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION)
					]);
			}, this);

			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.NAME, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: columnsData,

				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 */
		widgetCustomFormViewSetLoading: function (state) {
			state = Ext.isBoolean(state) ? state : false;

			this.view.setLoading(state);
		}
	});

})();
