(function () {

	Ext.define('CMDBuild.controller.common.entryTypeGrid.filter.advanced.Manager', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.entryTypeGrid.filter.advanced.Manager'
		],

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.filter.advanced.Advanced}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'entryTypeGridFilterAdvancedManagerSave',
			'entryTypeGridFilterAdvancedManagerSelectedFilterGet',
			'entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty',
			'entryTypeGridFilterAdvancedManagerSelectedFilterReset',
			'entryTypeGridFilterAdvancedManagerSelectedFilterSet',
			'entryTypeGridFilterAdvancedManagerStoreIsEmpty',
			'entryTypeGridFilterAdvancedManagerViewClose',
			'entryTypeGridFilterAdvancedManagerViewShow',
			'onEntryTypeGridFilterAdvancedManagerAddButtonClick',
			'onEntryTypeGridFilterAdvancedManagerCloneButtonClick',
			'onEntryTypeGridFilterAdvancedManagerModifyButtonClick',
			'onEntryTypeGridFilterAdvancedManagerRemoveButtonClick',
			'onEntryTypeGridFilterAdvancedManagerSaveButtonClick',
			'onEntryTypeGridFilterAdvancedManagerViewShow'
		],

		/**
		 * @property {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditor}
		 */
		controllerFilterEditor: undefined,

		/**
		 * @property {CMDBuild.controller.common.entryTypeGrid.filter.advanced.SaveDialog}
		 */
		controllerSaveDialog: undefined,

		/**
		 * @property {CMDBuild.view.common.entryTypeGrid.filter.advanced.manager.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter}
		 *
		 * @private
		 */
		selectedFilter: undefined,

		/**
		 * @property {CMDBuild.view.common.entryTypeGrid.filter.advanced.manager.ManagerWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.entryTypeGrid.filter.advanced.Advanced} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.entryTypeGrid.filter.advanced.manager.ManagerWindow', { delegate: this });

			// Build sub controllers
			this.controllerFilterEditor = Ext.create('CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditor', { parentDelegate: this });
			this.controllerSaveDialog = Ext.create('CMDBuild.controller.common.entryTypeGrid.filter.advanced.SaveDialog', { parentDelegate: this});

			// Shorthands
			this.grid = this.view.grid;
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.enableApply
		 * @param {Boolean} parameters.enableSaveDialog
		 *
		 * @returns {Void}
		 */
		entryTypeGridFilterAdvancedManagerSave: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};
			parameters.enableApply = Ext.isBoolean(parameters.enableApply) ? parameters.enableApply : false;
			parameters.enableSaveDialog = Ext.isBoolean(parameters.enableSaveDialog) ? parameters.enableSaveDialog : true;

			if (!this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty')) {
				if (parameters.enableSaveDialog)
					return this.controllerSaveDialog.cmfg('entryTypeGridFilterAdvancedSaveDialogShow', parameters.enableApply);

				return this.saveActionManage(parameters.enableApply);
			} else {
				_error('entryTypeGridFilterAdvancedManagerSave(): cannot save empty filter', this, this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet'));
			}
		},

		/**
		 * @returns {Void}
		 */
		entryTypeGridFilterAdvancedManagerStoreIsEmpty: function () {
			return this.grid.getStore().count() == 0;
		},

		/**
		 * @returns {Void}
		 */
		entryTypeGridFilterAdvancedManagerViewClose: function () {
			this.view.close();
		},

		/**
		 * Shows and configures manager or filter window before evaluation after filter grid load (acts like beforeshow event)
		 *
		 * @returns {Void}
		 */
		entryTypeGridFilterAdvancedManagerViewShow: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('entryTypeGridFilterAdvancedEntryTypeGet', CMDBuild.core.constants.Proxy.NAME);

			this.grid.getStore().load({
				params: params,
				scope: this,
				callback: function (records, operation, success) {
					if (success) {
						// Add local cached filters to store
						if (!this.cmfg('entryTypeGridFilterAdvancedLocalFilterIsEmpty')) {
							this.grid.getStore().add(this.cmfg('entryTypeGridFilterAdvancedLocalFilterGet'));
							this.grid.getStore().sort();
						}

						if (this.grid.getStore().count() == 0) {
							this.cmfg('onEntryTypeGridFilterAdvancedManagerAddButtonClick');
						} else {
							this.view.show();
						}
					}
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedManagerAddButtonClick: function () {
			var emptyFilterObject = {};
			emptyFilterObject[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = this.cmfg('entryTypeGridFilterAdvancedEntryTypeGet', CMDBuild.core.constants.Proxy.NAME);
			emptyFilterObject[CMDBuild.core.constants.Proxy.NAME] = CMDBuild.Translation.newSearchFilter;

			this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterSet', { value: emptyFilterObject }); // Manual save call (with empty data)

			this.controllerFilterEditor.getView().show();
		},

		/**
		 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter} filter
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedManagerCloneButtonClick: function (filter) {
			if (Ext.isObject(filter) && !Ext.Object.isEmpty(filter)) {
				var clonedFilterObject = filter.getData();
				clonedFilterObject[CMDBuild.core.constants.Proxy.ID] = null;
				clonedFilterObject[CMDBuild.core.constants.Proxy.NAME] = CMDBuild.Translation.copyOf + ' ' + filter.get(CMDBuild.core.constants.Proxy.NAME);

				this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterSet', { value: clonedFilterObject }); // Manual save call (with cloned data)

				this.controllerFilterEditor.getView().show();
			} else {
				_error('onEntryTypeGridFilterAdvancedManagerCloneButtonClick(): wrong filter parameter', this, filter);
			}
		},

		/**
		 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter} filter
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedManagerModifyButtonClick: function (filter) {
			if (Ext.isObject(filter) && !Ext.Object.isEmpty(filter)) {
				this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterSet', { value: filter }); // Manual save call (with filter data)

				this.controllerFilterEditor.getView().show();
			} else {
				_error('onEntryTypeGridFilterAdvancedManagerModifyButtonClick(): wrong filter parameter', this, filter);
			}
		},

		/**
		 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter} filter
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedManagerRemoveButtonClick: function (filter) {
			Ext.MessageBox.confirm(
				CMDBuild.Translation.attention,
				CMDBuild.Translation.common.confirmpopup.areyousure,
				function (buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem(filter);
				},
				this
			);
		},

		/**
		 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter} filter
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedManagerSaveButtonClick: function (filter) {
			if (Ext.isObject(filter) && !Ext.Object.isEmpty(filter)) {
				// Remove name and description to force save dialog show
				var filterObject = filter.getData();
				delete filterObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
				delete filterObject[CMDBuild.core.constants.Proxy.NAME];

				this.cmfg('entryTypeGridFilterAdvancedLocalFilterRemove', filter);
				this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterSet', { value: filterObject }); // Manual save call (with filter data)
				this.cmfg('entryTypeGridFilterAdvancedManagerSave');
			} else {
				_error('onEntryTypeGridFilterAdvancedManagerSaveButtonClick(): wrong filter parameter', this, filter);
			}
		},

		/**
		 * On show event define window details:
		 * 	- reset selections
		 * 	- set correct position
		 * 	- add event to manage view outside click manage
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedManagerViewShow: function () {
			if (this.grid.getSelectionModel().hasSelection())
				this.grid.getSelectionModel().deselectAll();

			var buttonBox = this.cmfg('entryTypeGridFilterAdvancedViewGet').getBox();

			if (!Ext.isEmpty(buttonBox))
				this.view.setPosition(buttonBox.x, buttonBox.y + buttonBox.height);

			Ext.getBody().on('click', this.onViewOutsideClick, this); // Outside click manage
		},

		/**
		 * Hide on outside click
		 *
		 * @param {Object} e
		 * @param {Object} t
		 * @param {Object} eOpts
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		onViewOutsideClick: function (e, t, eOpts) {
			var el = this.view.getEl();

			if (!(el.dom === t || el.contains(t))) {
				Ext.getBody().un('click', this.onViewOutsideClick, this);

				this.cmfg('entryTypeGridFilterAdvancedManageToggleStateReset');
				this.view.close();
			}
		},

		/**
		 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter} filter
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		removeItem: function (filter) {
			if (Ext.isObject(filter) && !Ext.Object.isEmpty(filter)) {
				this.cmfg('entryTypeGridFilterAdvancedManagerViewClose');

				if (Ext.isEmpty(filter.get(CMDBuild.core.constants.Proxy.ID))) { // Remove from local storage
					if (filter.get(CMDBuild.core.constants.Proxy.ID) == this.cmfg('entryTypeGridFilterAdvancedAppliedFilterGet', CMDBuild.core.constants.Proxy.ID))
						this.cmfg('onEntryTypeGridFilterAdvancedClearButtonClick');

					this.cmfg('entryTypeGridFilterAdvancedLocalFilterRemove', filter);
					this.cmfg('entryTypeGridFilterAdvancedManageToggleButtonLabelSet');
					this.cmfg('entryTypeGridFilterAdvancedManagerViewShow');
				} else { // Remove from server
					var params = {};
					params[CMDBuild.core.constants.Proxy.ID] = filter.get(CMDBuild.core.constants.Proxy.ID);

					CMDBuild.proxy.common.entryTypeGrid.filter.advanced.Manager.remove({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							if (filter.get(CMDBuild.core.constants.Proxy.ID) == this.cmfg('entryTypeGridFilterAdvancedAppliedFilterGet', CMDBuild.core.constants.Proxy.ID))
								this.cmfg('onEntryTypeGridFilterAdvancedClearButtonClick');

							this.cmfg('entryTypeGridFilterAdvancedManageToggleButtonLabelSet');
							this.cmfg('entryTypeGridFilterAdvancedManagerViewShow');
						}
					});
				}
			} else {
				_error('removeItem(): wrong filter parameter', this, filter);
			}
		},

		/**
		 * @param {Boolean} enableApply
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		saveActionManage: function (enableApply) {
			enableApply = Ext.isBoolean(enableApply) ? enableApply : false;

			if (!this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty')) {
				var filter = this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet');
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = filter.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE); // FIXME: i read entryType and write className (rename)
				params[CMDBuild.core.constants.Proxy.CONFIGURATION] = Ext.encode(filter.get(CMDBuild.core.constants.Proxy.CONFIGURATION));
				params[CMDBuild.core.constants.Proxy.DESCRIPTION] = filter.get(CMDBuild.core.constants.Proxy.DESCRIPTION);
				params[CMDBuild.core.constants.Proxy.NAME] = filter.get(CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.TEMPLATE] = filter.get(CMDBuild.core.constants.Proxy.TEMPLATE);

				if (Ext.isEmpty(filter.get(CMDBuild.core.constants.Proxy.ID))) {
					CMDBuild.proxy.common.entryTypeGrid.filter.advanced.Manager.create({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.FILTER];

							if (Ext.isObject(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
								this.controllerSaveDialog.cmfg('onEntryTypeGridFilterAdvancedSaveDialogAbortButtonClick'); // Close save dialog view
								this.controllerFilterEditor.cmfg('onEntryTypeGridFilterAdvancedFilterEditorAbortButtonClick'); // Close filter editor view
								this.cmfg('entryTypeGridFilterAdvancedManagerViewClose'); // Close manager view

								if (enableApply) { // Apply filter to store
									this.cmfg('onEntryTypeGridFilterAdvancedFilterSelect', Ext.create('CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter', decodedResponse));
									this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterReset');
								} else { // Otherwise reopen manager window
									this.cmfg('entryTypeGridFilterAdvancedManagerViewShow');
								}
							}
						}
					});
				} else {
					params[CMDBuild.core.constants.Proxy.ID] = filter.get(CMDBuild.core.constants.Proxy.ID);

					CMDBuild.proxy.common.entryTypeGrid.filter.advanced.Manager.update({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							// FIXME: hack as workaround, should be fixed on server side returning all saved filter object
							decodedResponse = params;
							decodedResponse[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = decodedResponse[CMDBuild.core.constants.Proxy.CLASS_NAME];
							decodedResponse[CMDBuild.core.constants.Proxy.CONFIGURATION] = Ext.decode(decodedResponse[CMDBuild.core.constants.Proxy.CONFIGURATION]);

							if (Ext.isObject(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
								this.controllerSaveDialog.cmfg('onEntryTypeGridFilterAdvancedSaveDialogAbortButtonClick'); // Close save dialog view
								this.controllerFilterEditor.cmfg('onEntryTypeGridFilterAdvancedFilterEditorAbortButtonClick'); // Close filter editor view
								this.cmfg('entryTypeGridFilterAdvancedManagerViewClose'); // Close manager view

								if (enableApply) {// Apply filter to store
									this.cmfg('onEntryTypeGridFilterAdvancedFilterSelect', Ext.create('CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter', decodedResponse));
									this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterReset');
								} else { // Otherwise reopen manager window
									this.cmfg('entryTypeGridFilterAdvancedManagerViewShow');
								}
							}
						}
					});
				}
			} else {
				_error('saveActionManage(): wrong filter parameter', this, this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet'));
			}
		},

		/**
		 * SelectedFilter property methods
		 *
		 * Not real selected filter, is just filter witch FilterEditor will manage.
		 */
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			entryTypeGridFilterAdvancedManagerSelectedFilterGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedFilter';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedFilter';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @returns {Void}
			 */
			entryTypeGridFilterAdvancedManagerSelectedFilterReset: function () {
				this.propertyManageReset('selectedFilter');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			entryTypeGridFilterAdvancedManagerSelectedFilterSet: function (parameters) {
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedFilter';

					this.propertyManageSet(parameters);
				}
			}
	});

})();
