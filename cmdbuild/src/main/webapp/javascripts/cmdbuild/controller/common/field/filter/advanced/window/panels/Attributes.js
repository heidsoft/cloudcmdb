(function() {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.window.panels.Attributes', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.tabs.attribute.Attribute',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'filterConditionsGroupsRemove = onFieldFilterAdvancedWindowAttributesFieldSetEmptied',
			'onFieldFilterAdvancedWindowAttributesAddButtonSelect',
			'onFieldFilterAdvancedWindowAttributesGetData',
			'onFieldFilterAdvancedWindowAttributesSetData = onFieldFilterAdvancedWindowSetData',
			'onFieldFilterAdvancedWindowAttributesShow',
			'onFieldFilterAdvancedWindowAttributesTabBuild'
		],

		/**
		 * Filter conditions buffer grouped by attribute name
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		filterConditions: {},

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.attributes.FormPanel}
		 */
		form: undefined,

		/**
		 * Flag to override selectAtRuntime attribute parameter
		 *
		 * @cfg {Boolean}
		 */
		selectAtRuntimeCheckDisabled: false,

		/**
		 * @cfg {Array}
		 *
		 * @private
		 */
		selectedEntityAttributes: [],

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.attributes.AttributesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.window.Window} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.buildView();
		},

		buildMenuButton: function() {
			var buttonGroups = [];
			var groupedAttributes = CMDBuild.core.Utils.groupAttributesObjects(this.selectedEntityAttributesGet());

			this.form.addAttributeButton.menu.removeAll();

			Ext.Object.each(groupedAttributes, function(group, attributes, myself) {
				var groupItems = [];

				Ext.Array.forEach(attributes, function(attribute, i, allAttributes) {
					groupItems.push({
						text: attribute[CMDBuild.core.constants.Proxy.DESCRIPTION],
						attribute: attribute,
						scope: this,

						handler: function(item, e) {
							this.cmfg('onFieldFilterAdvancedWindowAttributesAddButtonSelect', item[CMDBuild.core.constants.Proxy.ATTRIBUTE]);
						}
					});
				}, this);

				buttonGroups.push({
					text: group,
					menu: groupItems
				});
			}, this);

			// If no groups display just attributes
			buttonGroups = (Ext.Object.getKeys(groupedAttributes).length == 1) ? buttonGroups[0].menu : buttonGroups;

			this.form.addAttributeButton.menu.add(buttonGroups);
		},

		/**
		 * @returns {CMDBuild.view.common.field.filter.advanced.window.panels.attributes.AttributesView}
		 */
		buildView: function() {
			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.attributes.AttributesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;

			return this.view;
		},

		/**
		 * Decode filter object and launch creation of fields and fieldsets
		 *
		 * @param {Object} filterConfigurationObject
		 *
		 * @returns {Mixed}
		 */
		decodeFilterConfigurationObject: function(filterConfigurationObject) {
			if (!Ext.Object.isEmpty(filterConfigurationObject)) {
				var filterConfigurationAttribute = filterConfigurationObject.or || filterConfigurationObject.and || filterConfigurationObject;

				if (Ext.isArray(filterConfigurationAttribute)) {
					Ext.Array.forEach(filterConfigurationAttribute, function(objectProperty, i, allObjectProperties) {
						return this.decodeFilterConfigurationObject(objectProperty);
					}, this);
				} else if (Ext.isObject(filterConfigurationAttribute)) {
					var attribute = this.selectedEntityAttributesFindByName(
						filterConfigurationAttribute[CMDBuild.core.constants.Proxy.SIMPLE][CMDBuild.core.constants.Proxy.ATTRIBUTE]
					);

					if (!Ext.isEmpty(attribute))
						return this.filterConditionsConditionAdd(attribute, filterConfigurationAttribute[CMDBuild.core.constants.Proxy.SIMPLE]);
				}
			}
		},

		// FilterConditions methods
			/**
			 * @param {Object} attribute
			 * @param {Object} data
			 */
			filterConditionsConditionAdd: function(attribute, data) {
				if (
					!Ext.isEmpty(attribute)
					&& Ext.isObject(attribute)
				) {
					this.filterConditionsGroupAdd(attribute);

					if (!this.filterConditionsIsGroupEmpty(attribute[CMDBuild.core.constants.Proxy.NAME])) {
						var filterCondition = Ext.create('CMDBuild.Management.FieldManager.getFieldSetForFilter', attribute);

						this.filterConditionsGroupGet(attribute[CMDBuild.core.constants.Proxy.NAME]).addCondition(filterCondition);
						filterCondition.setData(data);

						this.view.doLayout(); // Fixes a bug in FieldManager creation methods
					}
				}
			},

			/**
			 * @param {Object} attribute
			 */
			filterConditionsGroupAdd: function(attribute) {
				if (
					!Ext.isEmpty(attribute)
					&& Ext.isObject(attribute)
					&& this.filterConditionsIsGroupEmpty(attribute[CMDBuild.core.constants.Proxy.NAME])
				) {
					var fieldset = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.attributes.FieldSet', {
						delegate: this,
						attributeName: attribute[CMDBuild.core.constants.Proxy.NAME],
						title: attribute[CMDBuild.core.constants.Proxy.DESCRIPTION]
					});

					this.filterConditions[attribute[CMDBuild.core.constants.Proxy.NAME]] = fieldset;

					this.form.add(fieldset);
				}
			},

			/**
			 * @param {String} attributeName
			 *
			 * @returns
			 * 		{CMDBuild.view.common.field.filter.advanced.window.panels.attributes.FieldSet} if group not empty (single group)
			 * 		{Object} if attributeName is empty (all groups)
			 * 		{null} if group is empty
			 */
			filterConditionsGroupGet: function(attributeName) {
				if (!Ext.isEmpty(attributeName))
					if (this.filterConditionsIsGroupEmpty(attributeName)) {
						return null;
					} else {
						return this.filterConditions[attributeName];
					}

				return this.filterConditions;
			},

			/**
			 * @param {String} attributeName
			 *
			 * @returns {Boolean}
			 */
			filterConditionsIsEmpty: function(attributeName) {
				return Ext.Object.isEmpty(this.filterConditions);
			},

			/**
			 * @param {String} attributeName
			 *
			 * @returns {Boolean}
			 */
			filterConditionsIsGroupEmpty: function(attributeName) {
				return Ext.isEmpty(this.filterConditions[attributeName]);
			},

			/**
			 * @param {String} attributeName
			 */
			filterConditionsGroupsRemove: function(attributeName) {
				if (!this.filterConditionsIsGroupEmpty(attributeName)) {
					this.form.remove(this.filterConditionsGroupGet(attributeName));

					delete this.filterConditions[attributeName];
				}
			},

			filterConditionsGroupsReset: function() {
				this.filterConditions = {};

				this.form.removeAll();
			},

		/**
		 * @param {Object} attribute
		 */
		onFieldFilterAdvancedWindowAttributesAddButtonSelect: function(attribute) {
			attribute.selectAtRuntimeCheckDisabled = Ext.isBoolean(this.selectAtRuntimeCheckDisabled) ? this.selectAtRuntimeCheckDisabled : false;

			this.filterConditionsConditionAdd(attribute);

			this.form.doLayout(); // Fixes FieldManager implementation problems
		},

		/**
		 * @returns {Object} or null
		 */
		onFieldFilterAdvancedWindowAttributesGetData: function() {
			var out = {};

			if (!this.filterConditionsIsEmpty()) {
				var data = [];
				var filterObject = {};

				Ext.Object.each(this.filterConditionsGroupGet(), function(attributeName, fieldset, myself) {
					if (Ext.isFunction(fieldset.getData))
						data.push(fieldset.getData());
				}, this);

				if (data.length == 1) {
					filterObject = data[0];
				} else if (data.length > 1) {
					filterObject[CMDBuild.core.constants.Proxy.AND] = data;
				}

				out[CMDBuild.core.constants.Proxy.ATTRIBUTE] = filterObject;
			}

			return out;
		},

		/**
		 * Apply data only if filter entryType equals to form's selectedClass name
		 *
		 * @param {CMDBuild.model.common.field.filter.advanced.Filter} filter
		 */
		onFieldFilterAdvancedWindowAttributesSetData: function(filter) {
			if (filter.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE) == this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.NAME)) {
				var filterConfigurationObject = filter.get(CMDBuild.core.constants.Proxy.CONFIGURATION);

				this.viewReset();

				if (
					!Ext.isEmpty(filterConfigurationObject)
					&& !Ext.Object.isEmpty(filterConfigurationObject[CMDBuild.core.constants.Proxy.ATTRIBUTE])
				) {
					this.decodeFilterConfigurationObject(filterConfigurationObject[CMDBuild.core.constants.Proxy.ATTRIBUTE]);
				}
			}
		},

		onFieldFilterAdvancedWindowAttributesShow: function() {
			if (!this.cmfg('fieldFilterAdvancedFilterIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.NAME);


				CMDBuild.proxy.common.tabs.attribute.Attribute.read({
					params: params,
					loadMask: true,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTES];

						this.selectedEntityAttributesSet(decodedResponse);
						this.buildMenuButton();
						this.onFieldFilterAdvancedWindowAttributesSetData(this.cmfg('fieldFilterAdvancedFilterGet'));
					}
				});
			}
		},

		/**
		 * Builds tab from filter value (preset values and add)
		 */
		onFieldFilterAdvancedWindowAttributesTabBuild: function() {
			if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'attribute'))
				this.cmfg('fieldFilterAdvancedWindowAddTab', this.buildView());
		},

		// SelectedEntityAttributes property methods
			/**
			 * @param {String} name
			 *
			 * @returns {Object} or null
			 */
			selectedEntityAttributesFindByName: function(name) {
				if (!Ext.isEmpty(name) && Ext.isString(name))
					return Ext.Array.findBy(this.selectedEntityAttributes, function(attribute) {
						return attribute[CMDBuild.core.constants.Proxy.NAME] == name;
					}, this);

				return null;
			},
			/**
			 * @returns {Array}
			 */
			selectedEntityAttributesGet: function() {
				return this.selectedEntityAttributes;
			},

			/**
			 * @param {Array} attributes
			 */
			selectedEntityAttributesSet: function(attributes) {
				this.selectedEntityAttributes = [];

				if (!Ext.isEmpty(attributes) && Ext.isArray(attributes))
					this.selectedEntityAttributes = attributes;
			},

		viewReset: function() {
			this.filterConditionsGroupsReset();
		}
	});

})();