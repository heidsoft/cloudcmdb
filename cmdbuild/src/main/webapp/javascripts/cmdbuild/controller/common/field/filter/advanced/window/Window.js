(function() {

	/**
	 * To get the result of filter window you need to implement "onFieldFilterAdvancedWindowgetEndpoint" in cmfg structure
	 */
	Ext.define('CMDBuild.controller.common.field.filter.advanced.window.Window', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.filter.User'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.Advanced}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedWindowAddTab',
			'fieldFilterAdvancedWindowSelectedRecordGet',
			'fieldFilterAdvancedWindowSelectedRecordIsEmpty',
			'onFieldFilterAdvancedWindowAbortButtonClick',
			'onFieldFilterAdvancedWindowBeforeShow',
			'onFieldFilterAdvancedWindowConfirmButtonClick',
			'onFieldFilterAdvancedWindowPresetGridSelect',
			'onFieldFilterAdvancedWindowPresetGridStoreLoad',
			'onFieldFilterAdvancedWindowSetData -> controllerTabAttributes, controllerTabFunctions, controllerTabRelations',
			'onFieldFilterAdvancedWindowShow'
		],

		/**
		 * @property {CMDBuild.model.common.field.filter.advanced.window.Configuration}
		 *
		 * @private
		 */
		configuration: {},

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.panels.Attributes}
		 */
		controllerTabAttributes: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.panels.ColumnPrivileges}
		 */
		controllerTabColumnPrivileges: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.panels.Functions}
		 */
		controllerTabFunctions: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.panels.relations.Relations}
		 */
		controllerTabRelations: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {Object}
		 *
		 * @private
		 */
		selectedRecord: undefined,

		/**
		 * @property {Ext.tab.Panel}
		 */
		tabPanel: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.Window}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.Advanced} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.fieldFilterAdvancedWindowConfigurationSet(this.configuration);

			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.window.Window', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;
			this.tabPanel = this.view.tabPanel;

			// Build sub controllers
			this.controllerTabAttributes = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.panels.Attributes', {
				parentDelegate: this,
				selectAtRuntimeCheckDisabled: this.fieldFilterAdvancedWindowConfigurationGet([
					CMDBuild.core.constants.Proxy.TABS,
					CMDBuild.core.constants.Proxy.ATTRIBUTES,
					'selectAtRuntimeCheckDisabled'
				])
			});
			this.controllerTabColumnPrivileges = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.panels.ColumnPrivileges', {
				parentDelegate: this,
				view: this.view.columnPrivileges || {}
			});
			this.controllerTabFunctions = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.panels.Functions', { parentDelegate: this });
			this.controllerTabRelations = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.panels.relations.Relations', { parentDelegate: this });
		},

		/**
		 * @property {Mixed} panel
		 */
		fieldFilterAdvancedWindowAddTab: function(panel) {
			if (!Ext.isEmpty(panel))
				this.tabPanel.add(panel);
		},

		// Configuration property methods
			/**
			 * Attribute could be a single string (attribute name) or an array of strings that declares path to required attribute through model object's properties
			 *
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed}
			 */
			fieldFilterAdvancedWindowConfigurationGet: function(attributePath) {
				attributePath = Ext.isArray(attributePath) ? attributePath : [attributePath];

				var requiredAttribute = this.configuration;

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
			fieldFilterAdvancedWindowConfigurationIsEmpty: function(attributePath) {
				if (!Ext.isEmpty(attributePath))
					if (Ext.isObject(this.fieldFilterAdvancedWindowConfigurationGet(attributePath))) {
						return Ext.Object.isEmpty(this.fieldFilterAdvancedWindowConfigurationGet(attributePath));
					} else {
						return Ext.isEmpty(this.fieldFilterAdvancedWindowConfigurationGet(attributePath));
					}

				return Ext.isEmpty(this.configuration);
			},

			/**
			 * @param {Object} configurationObject
			 */
			fieldFilterAdvancedWindowConfigurationSet: function(configurationObject) {
				if (
					Ext.isObject(configurationObject)
					&& Ext.getClassName(configurationObject) == 'CMDBuild.model.common.field.filter.advanced.window.Configuration'
				) {
					this.configuration = configurationObject;
				} else {
					this.configuration = Ext.create('CMDBuild.model.common.field.filter.advanced.window.Configuration', configurationObject);
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
			fieldFilterAdvancedWindowSelectedRecordGet: function(attributePath) {
				attributePath = Ext.isArray(attributePath) ? attributePath : [attributePath];

				var requiredAttribute = this.selectedRecord;

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
			fieldFilterAdvancedWindowSelectedRecordIsEmpty: function(attributePath) {
				if (!Ext.isEmpty(attributePath))
					if (Ext.isObject(this.fieldFilterAdvancedWindowSelectedRecordGet(attributePath))) {
						return Ext.Object.isEmpty(this.fieldFilterAdvancedWindowSelectedRecordGet(attributePath));
					} else {
						return Ext.isEmpty(this.fieldFilterAdvancedWindowSelectedRecordGet(attributePath));
					}

				return Ext.isEmpty(this.selectedRecord);
			},

			/**
			 * Setup full record or only one model property
			 *
			 * @param {Object} parameters
			 * @param {Object} parameters.value
			 * @param {String} parameters.name
			 *
			 * @returns {Mixed}
			 */
			fieldFilterAdvancedWindowSelectedRecordSet: function(parameters) {
				if (Ext.isEmpty(parameters)) { // Reset filter property
					this.selectedRecord = undefined;
				} else {
					var value = parameters.value;
					var name = parameters.name;

					// Single property management
					if (!Ext.isEmpty(name) && Ext.isString(name)) {
						if (
							!Ext.isEmpty(this.selectedRecord)
							&& Ext.isObject(this.selectedRecord)
							&& Ext.isFunction(this.selectedRecord.set)
						) { // Model management
							this.selectedRecord.set(name, value);
						} else if (
							!Ext.isEmpty(this.selectedRecord)
							&& Ext.isObject(this.selectedRecord)
						) { // Simple object management
							this.selectedRecord[name] = value;
						}
					} else if (!Ext.isEmpty(value)) { // Full model setup management
						this.selectedRecord = value;
					}
				}
			},

		onFieldFilterAdvancedWindowAbortButtonClick: function() {
			this.view.hide();
		},

		/**
		 * @returns {Boolean} returnValue
		 */
		onFieldFilterAdvancedWindowBeforeShow: function() {
			var returnValue = false;

			switch (this.fieldFilterAdvancedWindowConfigurationGet(CMDBuild.core.constants.Proxy.MODE)) {
				case 'grid': {
					if (this.fieldFilterAdvancedWindowSelectedRecordIsEmpty())
						CMDBuild.core.Message.warning(null, CMDBuild.Translation.warnings.toSetAFilterYouMustBeforeSelectAClass, false);

					returnValue = returnValue && !this.fieldFilterAdvancedWindowSelectedRecordIsEmpty();
				}

				case 'field': {
					if (this.cmfg('fieldFilterAdvancedFilterIsEmpty'))
						CMDBuild.core.Message.warning(null, CMDBuild.Translation.warnings.toSetAFilterYouMustBeforeSelectAClass, false);

					returnValue = !this.cmfg('fieldFilterAdvancedFilterIsEmpty');
				}
			}

			return returnValue;
		},

		/**
		 * Fill filter model with tab's data
		 */
		onFieldFilterAdvancedWindowConfirmButtonClick: function() {
			if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'columnPrivileges')) {
				var activeTab = this.view.windowTabPanel.getActiveTab();
				var columnPrivilegesArray = [];
				var filterObject = {};

				if (Ext.getClassName(activeTab) == 'CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.ColumnPrivilegesView') {
					if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'columnPrivileges'))
						columnPrivilegesArray = this.controllerTabColumnPrivileges.cmfg('onFieldFilterAdvancedWindowColumnPrivilegesGetData');

					this.cmfg('onFieldFilterAdvancedWindowgetEndpoint', { columnPrivileges: columnPrivilegesArray });
				} else if (Ext.getClassName(activeTab) == 'Ext.panel.Panel') {
					if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'attribute'))
						Ext.apply(filterObject, this.controllerTabAttributes.cmfg('onFieldFilterAdvancedWindowAttributesGetData'));

					if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'function'))
						Ext.apply(filterObject, this.controllerTabFunctions.cmfg('onFieldFilterAdvancedWindowFunctionsGetData'));

					if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'relation'))
						Ext.apply(filterObject, this.controllerTabRelations.cmfg('onFieldFilterAdvancedWindowRelationsGetData'));

					this.cmfg('onFieldFilterAdvancedWindowgetEndpoint', { filter: filterObject });
				}
			} else {
				var columnPrivilegesArray = [];
				var filterObject = {};

				if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'attribute'))
					Ext.apply(filterObject, this.controllerTabAttributes.cmfg('onFieldFilterAdvancedWindowAttributesGetData'));

				if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'function'))
					Ext.apply(filterObject, this.controllerTabFunctions.cmfg('onFieldFilterAdvancedWindowFunctionsGetData'));

				if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'relation'))
					Ext.apply(filterObject, this.controllerTabRelations.cmfg('onFieldFilterAdvancedWindowRelationsGetData'));

				this.cmfg('onFieldFilterAdvancedWindowgetEndpoint', { filter: filterObject });
			}

			this.onFieldFilterAdvancedWindowAbortButtonClick();
		},

		/**
		 * @param {CMDBuild.model.common.field.filter.advanced.Filter} filter
		 */
		onFieldFilterAdvancedWindowPresetGridSelect: function(filter) {
			if (!Ext.isEmpty(filter)) {
				this.grid.getSelectionModel().deselectAll();

				this.cmfg('onFieldFilterAdvancedWindowSetData', filter);
			}
		},

		/**
		 * Include in store also Users filters to be consistent with checkbox state
		 */
		onFieldFilterAdvancedWindowPresetGridStoreLoad: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.NAME);

			if (this.grid.includeUsersFiltersCheckbox.getValue())
				CMDBuild.proxy.filter.User.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.FILTERS];

						this.grid.getStore().loadData(decodedResponse, true);
					}
				});
		},

		/**
		 * Setup tab visibility based on field configuration
		 */
		onFieldFilterAdvancedWindowShow: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.NAME);

			this.grid.getStore().load({ params: params });

			this.setViewTitle(this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.TEXT)); // TODO: waiting for refactor (description)

			// On window show rebuild all tab configuration (sorted)
			this.tabPanel.removeAll(true);

			this.controllerTabAttributes.cmfg('onFieldFilterAdvancedWindowAttributesTabBuild');
			this.controllerTabColumnPrivileges.cmfg('onFieldFilterAdvancedWindowColumnPrivilegesTabBuild');
			this.controllerTabRelations.cmfg('onFieldFilterAdvancedWindowRelationsTabBuild');
			this.controllerTabFunctions.cmfg('onFieldFilterAdvancedWindowFunctionsTabBuild');

			if (Ext.isEmpty(this.view.tabPanel.getActiveTab()))
				this.tabPanel.setActiveTab(0); // Configuration parameter doesn't work because panels are added

			this.view.tabPanel.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
		},

		/**
		 * Forward method
		 */
		show: function() {
			if (!Ext.isEmpty(this.view))
				this.view.show();
		}
	});

})();