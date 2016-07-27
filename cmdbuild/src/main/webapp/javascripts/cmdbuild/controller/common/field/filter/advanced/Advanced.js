(function() {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.Advanced', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		controllerFilterWindow: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedConfigurationGet',
			'fieldFilterAdvancedConfigurationIsPanelEnabled',
			'fieldFilterAdvancedFilterGet',
			'fieldFilterAdvancedFilterIsEmpty',
			'fieldFilterAdvancedFilterSet',
			'fieldFilterAdvancedSelectedClassGet',
			'fieldFilterAdvancedSelectedClassIsEmpty',
			'onFieldFilterAdvancedFilterClearButtonClick',
			'onFieldFilterAdvancedFilterSetButtonClick',
			'onFieldFilterAdvancedReset',
			'onFieldFilterAdvancedSetValue',
			'onFieldFilterAdvancedWindowgetEndpoint'
		],

		/**
		 * @cfg {CMDBuild.model.common.field.filter.advanced.FieldConfiguration}
		 *
		 * @private
		 */
		fieldConfiguration: undefined,

		/**
		 * Selected filter
		 *
		 * @property {CMDBuild.model.common.field.filter.advanced.Filter}
		 *
		 * @private
		 */
		filter: undefined,

		/**
		 * Used to loads the right attributes when click to the button to add a new filter
		 *
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		selectedClass: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.Advanced}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.fieldFilterAdvancedConfigurationSet(this.view.fieldConfiguration);

			this.setTargetClassFieldListener();

			this.onFieldFilterAdvancedReset();

			// Build sub controller
			this.controllerFilterWindow = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.Window', { parentDelegate: this });
		},

		// FieldConfiguration property methods
			/**
			 * @param {String} parameterName
			 *
			 * @returns {Mixed}
			 */
			fieldFilterAdvancedConfigurationGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName) && Ext.isObject(this.fieldConfiguration))
					return this.fieldConfiguration.get(parameterName);

				return this.fieldConfiguration;
			},

			/**
			 * @param {String} parameterName
			 *
			 * @returns {Mixed}
			 */
			fieldFilterAdvancedConfigurationIsEmpty: function(parameterName) {
				if (!Ext.isEmpty(parameterName) && Ext.isObject(this.fieldConfiguration))
					if (Ext.isObject(this.fieldConfiguration.get(parameterName))) {
						return Ext.Object.isEmpty(this.fieldConfiguration.get(parameterName));
					} else {
						return Ext.isEmpty(this.fieldConfiguration.get(parameterName));
					}

				return Ext.isEmpty(this.fieldConfiguration);
			},

			/**
			 * @param {String} panelIdentifier
			 *
			 * @returns {Boolean}
			 */
			fieldFilterAdvancedConfigurationIsPanelEnabled: function(panelIdentifier) {
				if (!Ext.isEmpty(panelIdentifier) && Ext.isString(panelIdentifier))
					return Ext.Array.contains(this.fieldFilterAdvancedConfigurationGet(CMDBuild.core.constants.Proxy.ENABLED_PANELS), panelIdentifier);

				return false;
			},

			/**
			 * @param {Object} fieldConfiguration
			 */
			fieldFilterAdvancedConfigurationSet: function(fieldConfiguration) {
				this.fieldConfiguration = undefined;

				if (!Ext.isEmpty(fieldConfiguration) && Ext.isObject(fieldConfiguration)) {
					if (Ext.getClassName(fieldConfiguration) == 'CMDBuild.model.common.field.filter.advanced.FieldConfiguration') {
						this.fieldConfiguration = fieldConfiguration;
					} else {
						this.fieldConfiguration = Ext.create('CMDBuild.model.common.field.filter.advanced.FieldConfiguration', fieldConfiguration);
					}
				}
			},

		// Filter property methods
			/**
			 * Attribute could be a single string (attribute name) or an array of strings that declares path to required attribute through model object's properties
			 *
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed}
			 */
			fieldFilterAdvancedFilterGet: function(attributePath) {
				attributePath = Ext.isArray(attributePath) ? attributePath : [attributePath];

				var requiredAttribute = this.filter;

				if (!Ext.isEmpty(attributePath))
					Ext.Array.forEach(attributePath, function(attributeName, i, allAttributeNames) {
						if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName))
							if (
								!Ext.isEmpty(requiredAttribute)
								&& Ext.isObject(requiredAttribute)
								&& Ext.isFunction(requiredAttribute.get)
							) { // Model management
								requiredAttribute = requiredAttribute.get(attributeName);
							} else if (
								!Ext.isEmpty(requiredAttribute)
								&& Ext.isObject(requiredAttribute)
							) { // Simple object management
								requiredAttribute = requiredAttribute[attributeName];
							}
					}, this);

				return requiredAttribute;
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			fieldFilterAdvancedFilterIsEmpty: function(attributePath) {
				if (!Ext.isEmpty(attributePath))
					if (Ext.isObject(this.fieldFilterAdvancedFilterGet(attributePath))) {
						return Ext.Object.isEmpty(this.fieldFilterAdvancedFilterGet(attributePath));
					} else {
						return Ext.isEmpty(this.fieldFilterAdvancedFilterGet(attributePath));
					}

				return Ext.isEmpty(this.filter);
			},

			/**
			 * Setup full filter or only one model property
			 *
			 * @param {Object} parameters
			 * @param {Object} parameters.filterObject
			 * @param {String} parameters.propertyName
			 *
			 * @returns {Mixed}
			 */
			fieldFilterAdvancedFilterSet: function(parameters) {
				if (Ext.isEmpty(parameters)) { // Reset filter property
					this.filter = undefined;
				} else {
					var filterObject = parameters.filterObject;
					var propertyName = parameters.propertyName;

					// Single property management
					if (!Ext.isEmpty(propertyName) && Ext.isString(propertyName)) {
						if (
							!Ext.isEmpty(this.filter)
							&& Ext.isObject(this.filter)
							&& Ext.isFunction(this.filter.set)
						) { // Model management
							this.filter.set(propertyName, filterObject);
						} else if (
							!Ext.isEmpty(this.filter)
							&& Ext.isObject(this.filter)
						) { // Simple object management
							this.filter[propertyName] = filterObject;
						}
					} else if (!Ext.isEmpty(filterObject)) { // Full model setup management
						if (Ext.getClassName(filterObject) == 'CMDBuild.model.common.field.filter.advanced.Filter') {
							this.filter = filterObject;
						} else {
							this.filter = Ext.create('CMDBuild.model.common.field.filter.advanced.Filter', filterObject);
						}
					}

					// Field label setup
					this.view.label.setValue(this.fieldFilterAdvancedFilterIsEmpty() ? CMDBuild.Translation.notSet : CMDBuild.Translation.set);
				}

				// Field buttons setup
				this.setButtonState();
			},

		// SelectedClass property methods
			/**
			 * @param {String} parameterName
			 *
			 * @returns {Mixed}
			 */
			fieldFilterAdvancedSelectedClassGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName) && Ext.isObject(this.selectedClass))
					return this.selectedClass.get(parameterName);

				return this.selectedClass;
			},

			/**
			 * @returns {Boolean}
			 */
			fieldFilterAdvancedSelectedClassIsEmpty: function() {
				return Ext.isEmpty(this.selectedClass);
			},

			/**
			 * @param {String} className
			 */
			fieldFilterAdvancedSelectedClassSet: function(className) {
				this.selectedClass = null;

				if (
					!Ext.isEmpty(className)
					&& Ext.isString(className)
					&& _CMCache.isEntryTypeByName(className)
				) {
					this.selectedClass = _CMCache.getEntryTypeByName(className);
				}

				this.fieldFilterAdvancedFilterSet();
			},

		onFieldFilterAdvancedFilterClearButtonClick: function() {
			this.fieldFilterAdvancedFilterSet({
				filterObject: Ext.create('CMDBuild.model.common.field.filter.advanced.Filter', {
					entryType: this.fieldFilterAdvancedSelectedClassGet(CMDBuild.core.constants.Proxy.NAME)
				})
			});

			this.view.filterClearButton.setDisabled(true);
		},

		onFieldFilterAdvancedFilterSetButtonClick: function() {
			this.controllerFilterWindow.show();
		},

		onFieldFilterAdvancedReset: function() {
			this.fieldFilterAdvancedFilterSet();
			this.fieldFilterAdvancedSelectedClassSet();
		},

		/**
		 * @param {Object} value
		 */
		onFieldFilterAdvancedSetValue: function(value) {
			if (
				!Ext.isEmpty(value)
				&& Ext.isObject(value)
				&& !this.fieldFilterAdvancedConfigurationIsEmpty(CMDBuild.core.constants.Proxy.TARGET_CLASS_FIELD)
			) {
				this.fieldFilterAdvancedSelectedClassSet(this.fieldFilterAdvancedConfigurationGet(CMDBuild.core.constants.Proxy.TARGET_CLASS_FIELD).getValue());

				this.fieldFilterAdvancedFilterSet({
					filterObject: Ext.create('CMDBuild.model.common.field.filter.advanced.Filter', {
						configuration: value,
						entryType: this.fieldFilterAdvancedSelectedClassGet(CMDBuild.core.constants.Proxy.NAME)
					})
				});
			}

			this.setButtonState();
		},

		/**
		 * @param {Object} resultObject
		 * @param {Object} resultObject.columnPrivileges
		 * @param {Object} resultObject.filter
		 */
		onFieldFilterAdvancedWindowgetEndpoint: function(resultObject) {
			this.fieldFilterAdvancedFilterSet({
				filterObject: resultObject.filter,
				propertyName: CMDBuild.core.constants.Proxy.CONFIGURATION
			});
		},

		/**
		 * Setup buttons enabled state besed on context
		 */
		setButtonState: function() {
			if (!this.view.isDisabled()) {
				this.view.filterSetButton.setDisabled(this.fieldFilterAdvancedSelectedClassIsEmpty());
				this.view.filterClearButton.setDisabled(this.fieldFilterAdvancedFilterIsEmpty([CMDBuild.core.constants.Proxy.CONFIGURATION]));
			}
		},

		setTargetClassFieldListener: function() {
			if (!this.fieldFilterAdvancedConfigurationIsEmpty(CMDBuild.core.constants.Proxy.TARGET_CLASS_FIELD))
				this.fieldFilterAdvancedConfigurationGet(CMDBuild.core.constants.Proxy.TARGET_CLASS_FIELD).on('select', function(combo, records, eOpts) {
					this.fieldFilterAdvancedSelectedClassSet(combo.getValue());

					// Apply empty filter on class select
					if (!this.fieldFilterAdvancedSelectedClassIsEmpty() && this.fieldFilterAdvancedFilterIsEmpty())
						this.fieldFilterAdvancedFilterSet({
							filterObject: Ext.create('CMDBuild.model.common.field.filter.advanced.Filter', {
								entryType: this.fieldFilterAdvancedSelectedClassGet(CMDBuild.core.constants.Proxy.NAME)
							})
						});

					this.setButtonState();
				}, this);
		}
	});

})();