(function () {

	Ext.define('CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.Attributes', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils',
			'CMDBuild.proxy.common.entryTypeGrid.filter.advanced.filterEditor.Attributes'
		],

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditor}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'entryTypeGridFilterAdvancedFilterEditorAttributesDataGet',
			'filterConditionsGroupsRemove = onEntryTypeGridFilterAdvancedFilterEditorAttributesFieldSetEmptied',
			'onEntryTypeGridFilterAdvancedFilterEditorAttributesAddButtonSelect',
			'onEntryTypeGridFilterAdvancedFilterEditorAttributesViewShow'
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
		 * @property {CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.attributes.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		selectedEntityAttributes: {
			objectsArray: [],
			sortedByName: {}
		},

		/**
		 * @property {CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.attributes.AttributesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditor} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.attributes.AttributesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		// AttributeButton manage methods
			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			attributeButtonBuild: function () {
				var buttonGroups = [];
				var groupedAttributes = CMDBuild.core.Utils.groupAttributesObjects(this.selectedEntityAttributesGet());

				this.attributeButtonReset();

				Ext.Object.each(groupedAttributes, function (group, attributes, myself) {
					var groupItems = [];

					Ext.Array.each(attributes, function (attribute, i, allAttributes) {
						groupItems.push({
							text: attribute[CMDBuild.core.constants.Proxy.DESCRIPTION],
							attribute: attribute,
							scope: this,

							handler: function (item, e) {
								this.cmfg('onEntryTypeGridFilterAdvancedFilterEditorAttributesAddButtonSelect', item[CMDBuild.core.constants.Proxy.ATTRIBUTE]);
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

				if (!Ext.isEmpty(buttonGroups)) {
					this.form.addAttributeButton.menu.add(buttonGroups);
					this.form.addAttributeButton.enable();
				}
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			attributeButtonReset: function () {
				this.form.addAttributeButton.menu.removeAll();
			},

		/**
		 * Recursive method to decode filter object and launch creation of form items
		 *
		 * @param {Object} filterConfigurationObject
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		decodeFilterConfigurationObject: function (filterConfigurationObject) {
			if (Ext.isObject(filterConfigurationObject) && !Ext.Object.isEmpty(filterConfigurationObject)) {
				var filterConfigurationAttribute = filterConfigurationObject.or || filterConfigurationObject.and || filterConfigurationObject;

				if (Ext.isArray(filterConfigurationAttribute)) {
					Ext.Array.forEach(filterConfigurationAttribute, function (objectProperty, i, allObjectProperties) {
						return this.decodeFilterConfigurationObject(objectProperty);
					}, this);
				} else if (
					Ext.isObject(filterConfigurationAttribute)
					&& !Ext.isEmpty(filterConfigurationAttribute[CMDBuild.core.constants.Proxy.SIMPLE])
					&& !Ext.isEmpty(filterConfigurationAttribute[CMDBuild.core.constants.Proxy.SIMPLE][CMDBuild.core.constants.Proxy.ATTRIBUTE])
				) {
					var attribute = this.selectedEntityAttributesFindByName(
						filterConfigurationAttribute[CMDBuild.core.constants.Proxy.SIMPLE][CMDBuild.core.constants.Proxy.ATTRIBUTE]
					);

					if (!Ext.isEmpty(attribute)) {
						return this.filterConditionsConditionAdd(attribute, filterConfigurationAttribute[CMDBuild.core.constants.Proxy.SIMPLE]);
					} else {
						_error('decodeFilterConfigurationObject(): empty attribute name', this, attribute);
					}
				}
			}
		},

		/**
		 * @returns {Object or null}
		 */
		entryTypeGridFilterAdvancedFilterEditorAttributesDataGet: function () {
			var out = {};

			if (!this.filterConditionsIsEmpty()) {
				var data = [];
				var filterObject = {};

				Ext.Object.each(this.filterConditionsGroupGet(), function (attributeName, fieldset, myself) {
					if (!Ext.isEmpty(fieldset) && Ext.isFunction(fieldset.getData))
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

		// FilterConditions manage methods
			/**
			 * @param {Object} attribute
			 * @param {Object} data
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			filterConditionsConditionAdd: function (attribute, data) {
				if (Ext.isObject(attribute) && !Ext.Object.isEmpty(attribute)) {
					this.filterConditionsGroupAdd(attribute);

					if (!this.filterConditionsIsGroupEmpty(attribute[CMDBuild.core.constants.Proxy.NAME])) {
						var filterCondition = Ext.create('CMDBuild.Management.FieldManager.getFieldSetForFilter', attribute); // TODO: implementation with new field manager

						this.filterConditionsGroupGet(attribute[CMDBuild.core.constants.Proxy.NAME]).addCondition(filterCondition);
						filterCondition.setData(data);

						this.view.doLayout(); // Fixes a bug in FieldManager creation methods
					}
				}
			},

			/**
			 * @param {Object} attribute
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			filterConditionsGroupAdd: function (attribute) {
				if (
					Ext.isObject(attribute) && !Ext.Object.isEmpty(attribute)
					&& this.filterConditionsIsGroupEmpty(attribute[CMDBuild.core.constants.Proxy.NAME])
				) {
					var fieldset = Ext.create('CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.attributes.FieldSet', {
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
			 * 		{CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.attributes.FieldSet} if group not empty (single group)
			 * 		{Object} if attributeName is empty (all groups)
			 * 		{null} if group is empty
			 *
			 * @private
			 */
			filterConditionsGroupGet: function (attributeName) {
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
			 *
			 * @private
			 */
			filterConditionsIsEmpty: function (attributeName) {
				return Ext.Object.isEmpty(this.filterConditions);
			},

			/**
			 * @param {String} attributeName
			 *
			 * @returns {Boolean}
			 *
			 * @private
			 */
			filterConditionsIsGroupEmpty: function (attributeName) {
				return !Ext.isEmpty(attributeName) && Ext.isEmpty(this.filterConditions[attributeName]);
			},

			/**
			 * @param {String} attributeName
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			filterConditionsGroupsRemove: function (attributeName) {
				if (!this.filterConditionsIsGroupEmpty(attributeName)) {
					this.form.remove(this.filterConditionsGroupGet(attributeName));

					delete this.filterConditions[attributeName];
				}
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			filterConditionsGroupsReset: function () {
				this.filterConditions = {};

				this.form.removeAll();
			},

		/**
		 * @param {Object} attribute
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorAttributesAddButtonSelect: function (attribute) {
			this.filterConditionsConditionAdd(attribute);

			this.form.doLayout(); // Fixes FieldManager implementation problems
		},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorAttributesViewShow: function () {
			if (!this.cmfg('entryTypeGridFilterAdvancedEntryTypeIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('entryTypeGridFilterAdvancedEntryTypeGet', CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.common.entryTypeGrid.filter.advanced.filterEditor.Attributes.read({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTES];

						this.attributeButtonReset();

						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
							this.selectedEntityAttributesSet(decodedResponse);
							this.attributeButtonBuild();
							this.viewBuild();
						}
					}
				});
			} else {
				_error('onEntryTypeGridFilterAdvancedFilterEditorAttributesViewShow(): entryType is empty', this, this.cmfg('entryTypeGridFilterAdvancedEntryTypeGet'));
			}
		},

		// SelectedEntityAttributes manage methods
			/**
			 * @param {String} name
			 *
			 * @returns {Object or null}
			 *
			 * @private
			 */
			selectedEntityAttributesFindByName: function (name) {
				if (Ext.isString(name) && !Ext.isEmpty(name))
					return this.selectedEntityAttributes.sortedByName[name];

				return null;
			},

			/**
			 * @returns {Array}
			 *
			 * @private
			 */
			selectedEntityAttributesGet: function () {
				return this.selectedEntityAttributes.objectsArray;
			},

			/**
			 * @param {Array} attributes
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			selectedEntityAttributesSet: function (attributes) {
				this.selectedEntityAttributes = { // Buffer variable init
					objectsArray: [],
					sortedByName: {}
				};

				if (!Ext.isEmpty(attributes) && Ext.isArray(attributes)) {
					this.selectedEntityAttributes.objectsArray = attributes;

					Ext.Array.each(attributes, function (attributeObject, i, allAttributeObjects) {
						if (Ext.isObject(attributeObject) && !Ext.Object.isEmpty(attributeObject))
							this.selectedEntityAttributes.sortedByName[attributeObject[CMDBuild.core.constants.Proxy.NAME]] = attributeObject;
					}, this);
				}
			},

		/**
		 * Manages view's filter configuration
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		viewBuild: function () {
			if (!this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty')) {
				var filterConfigurationObject = this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet', CMDBuild.core.constants.Proxy.CONFIGURATION);

				this.filterConditionsGroupsReset();

				if (
					!this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty', CMDBuild.core.constants.Proxy.CONFIGURATION)
					&& !this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty', [CMDBuild.core.constants.Proxy.CONFIGURATION, CMDBuild.core.constants.Proxy.ATTRIBUTE])
				) {
					this.decodeFilterConfigurationObject(
						this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet', [CMDBuild.core.constants.Proxy.CONFIGURATION, CMDBuild.core.constants.Proxy.ATTRIBUTE])
					);
				}
			} else {
				_error('viewBuild(): selected filter is empty', this, this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet'));
			}
		}
	});

})();
