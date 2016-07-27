(function () {

	Ext.define('CMDBuild.controller.common.entryTypeGrid.filter.advanced.Advanced', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter}
		 *
		 * @private
		 */
		appliedFilter: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'entryTypeGridFilterAdvancedAppliedFilterGet',
			'entryTypeGridFilterAdvancedAppliedFilterIsEmpty',
			'entryTypeGridFilterAdvancedAppliedFilterReset',
			'entryTypeGridFilterAdvancedAppliedFilterSet',
			'entryTypeGridFilterAdvancedEntryTypeGet',
			'entryTypeGridFilterAdvancedEntryTypeIsEmpty',
			'entryTypeGridFilterAdvancedEntryTypeSet = entryTypeSet',
			'entryTypeGridFilterAdvancedLocalFilterAdd',
			'entryTypeGridFilterAdvancedLocalFilterGet',
			'entryTypeGridFilterAdvancedLocalFilterIsEmpty',
			'entryTypeGridFilterAdvancedLocalFilterRemove',
			'entryTypeGridFilterAdvancedManageToggleButtonLabelSet',
			'entryTypeGridFilterAdvancedManageToggleStateReset',
			'entryTypeGridFilterAdvancedMasterGridGet',
			'getView = entryTypeGridFilterAdvancedViewGet',
			'onEntryTypeGridFilterAdvancedClearButtonClick',
			'onEntryTypeGridFilterAdvancedDisable',
			'onEntryTypeGridFilterAdvancedEnable',
			'onEntryTypeGridFilterAdvancedFilterSelect',
			'onEntryTypeGridFilterAdvancedManageToggleButtonClick'
		],

		/**
		 * @property {CMDBuild.controller.common.entryTypeGrid.filter.advanced.Manager}
		 */
		controllerManager: undefined,

		/**
		 * @property {Object}
		 *
		 * @private
		 */
		localFilterCache: {},

		/**
		 * @cfg {Ext.grid.Panel}
		 *
		 * @legacy
		 *
		 * FIXME: waiting for refactor (move to parent)
		 */
		masterGrid: undefined,

		/**
		 * @property {CMDBuild.model.common.entryTypeGrid.filter.advanced.SelectedEntryType}
		 *
		 * @private
		 */
		selectedEntryType: undefined,

		/**
		 * @property {CMDBuild.view.common.entryTypeGrid.filter.advanced.AdvancedView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.entryTypeGrid.filter.advanced.AdvancedView', { delegate: this });

			// Build sub controllers
			this.controllerManager = Ext.create('CMDBuild.controller.common.entryTypeGrid.filter.advanced.Manager', { parentDelegate: this });
		},

		/**
		 * AppliedFilter property functions
		 *
		 * @legacy
		 *
		 * FIXME: waiting for refactor (move to parent, grid controller)
		 */
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			entryTypeGridFilterAdvancedAppliedFilterGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'appliedFilter';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			entryTypeGridFilterAdvancedAppliedFilterIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'appliedFilter';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			entryTypeGridFilterAdvancedAppliedFilterReset: function () {
				this.propertyManageReset('appliedFilter');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			entryTypeGridFilterAdvancedAppliedFilterSet: function (parameters) {
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'appliedFilter';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * EntryType property functions
		 *
		 * @legacy
		 *
		 * FIXME: waiting for refactor (move to parent, grid controller)
		 */
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			entryTypeGridFilterAdvancedEntryTypeGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedEntryType';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			entryTypeGridFilterAdvancedEntryTypeIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedEntryType';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			entryTypeGridFilterAdvancedEntryTypeSet: function (parameters) {
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.entryTypeGrid.filter.advanced.SelectedEntryType';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedEntryType';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {String} label
		 *
		 * @returns {Void}
		 */
		entryTypeGridFilterAdvancedManageToggleButtonLabelSet: function (label) {
			this.view.manageToggleButton.setText(Ext.isEmpty(label) ? CMDBuild.Translation.searchFilter : Ext.String.ellipsis(label, 20));
			this.view.manageToggleButton.setTooltip(Ext.isEmpty(label) ? '' : label);
		},

		/**
		 * @returns {Void}
		 */
		entryTypeGridFilterAdvancedManageToggleStateReset: function () {
			this.view.manageToggleButton.toggle(false);
		},

		/**
		 * @returns {Ext.grid.Panel}
		 *
		 * @legacy
		 *
		 * FIXME: waiting for refactor (move to parent)
		 */
		entryTypeGridFilterAdvancedMasterGridGet: function () {
			return this.masterGrid;
		},

		// LocalFilterCache property functions
			/**
			 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter} filterModel
			 *
			 * @returns {Void}
			 */
			entryTypeGridFilterAdvancedLocalFilterAdd: function (filterModel) {
				if (Ext.isObject(filterModel) && !Ext.Object.isEmpty(filterModel)) {
					var filterIdentifier = filterModel.get(CMDBuild.core.constants.Proxy.ID);

					if (Ext.isEmpty(filterIdentifier))
						filterIdentifier = new Date().valueOf(); // Compatibility mode with IE older than IE 9 (Date.now())

					this.localFilterCache[filterIdentifier] = filterModel;
				}
			},

			/**
			 * @returns {Array}
			 */
			entryTypeGridFilterAdvancedLocalFilterGet: function () {
				return Ext.Object.getValues(this.localFilterCache);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			entryTypeGridFilterAdvancedLocalFilterIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'localFilterCache';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter} filterModel
			 *
			 * @returns {Void}
			 */
			entryTypeGridFilterAdvancedLocalFilterRemove: function (filterModel) {
				if (Ext.isObject(filterModel) && !Ext.Object.isEmpty(filterModel)) {
					var identifierToDelete = null;

					// Search for filter
					Ext.Object.each(this.localFilterCache, function (id, object, myself) {
						var filterModelObject = filterModel.getData();
						var localFilterObject = object.getData();

						if (Ext.Object.equals(filterModelObject, localFilterObject))
							identifierToDelete = id;
					}, this);

					if (!Ext.isEmpty(identifierToDelete))
						delete this.localFilterCache[identifierToDelete];
				}
			},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedClearButtonClick: function () {
			var masterGrid = this.cmfg('entryTypeGridFilterAdvancedMasterGridGet');

			if (!Ext.isEmpty(masterGrid)) {
				if (masterGrid.getSelectionModel().hasSelection())
					masterGrid.getSelectionModel().deselectAll();

				if (!this.cmfg('entryTypeGridFilterAdvancedAppliedFilterIsEmpty'))
					this.cmfg('entryTypeGridFilterAdvancedAppliedFilterReset');

				this.cmfg('entryTypeGridFilterAdvancedManageToggleButtonLabelSet');

				this.view.clearButton.disable();

				/**
				 * @legacy
				 *
				 * FIXME: waiting for refactor (use cmfg)
				 */
				masterGrid.applyFilterToStore({});
				masterGrid.reload();
			} else {
				_error('onEntryTypeGridFilterAdvancedClearButtonClick(): empty master grid', this, masterGrid);
			}
		},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedDisable: function () {
			this.view.clearButton.disable();
			this.view.manageToggleButton.disable();
		},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedEnable: function () {
			this.view.clearButton.enable();
			this.view.manageToggleButton.enable();
		},

		/**
		 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter} filter
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterSelect: function (filter) {
			var masterGrid = this.cmfg('entryTypeGridFilterAdvancedMasterGridGet');

			if (!Ext.isEmpty(masterGrid)) {
				this.controllerManager.cmfg('entryTypeGridFilterAdvancedManagerViewClose');

				this.cmfg('entryTypeGridFilterAdvancedAppliedFilterSet', { value: filter });
				this.cmfg(
					'entryTypeGridFilterAdvancedManageToggleButtonLabelSet',
					this.cmfg('entryTypeGridFilterAdvancedAppliedFilterGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
				);

				this.view.clearButton.enable();

				/**
				 * @legacy
				 *
				 * FIXME: waiting for refactor (use cmfg)
				 */
				masterGrid.delegate.onFilterMenuButtonApplyActionClick(Ext.create('CMDBuild.model.CMFilterModel', filter.getData()));
			} else {
				_error('onEntryTypeGridFilterAdvancedFilterSelect(): empty master grid', this, masterGrid);
			}
		},

		/**
		 * @param {Boolean} buttonState
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedManageToggleButtonClick: function (buttonState) {
			if (buttonState) {
				this.controllerManager.cmfg('entryTypeGridFilterAdvancedManagerViewShow');
			} else {
				this.controllerManager.cmfg('entryTypeGridFilterAdvancedManagerViewClose');
			}
		}
	});

})();
