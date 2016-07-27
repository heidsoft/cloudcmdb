(function () {

	Ext.define('CMDBuild.controller.common.field.searchWindow.SearchWindow', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.comboBox.Searchable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldSearchWindowConfigurationGet',
			'fieldSearchWindowConfigurationSet',
			'onFieldSearchWindowSaveButtonClick = onFieldSearchWindowItemDoubleClick',
			'onFieldSearchWindowSelectionChange',
			'onFieldSearchWindowShow',
			'onFieldSearchWindowStoreLoad'
		],

		/**
		 * @property {CMDBuild.model.common.field.searchWindow.Configuration}
		 *
		 * @private
		 */
		configuration: undefined,

		/**
		 * @property {CMDBuild.view.common.field.searchWindow.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.common.field.searchWindow.SearchWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.comboBox.Searchable} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.field.searchWindow.SearchWindow', { delegate: this });
		},

		// Configuration property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			fieldSearchWindowConfigurationGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 *
			 * @private
			 */
			fieldSearchWindowConfigurationIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			fieldSearchWindowConfigurationSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.field.searchWindow.Configuration';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @returns {Void}
		 */
		onFieldSearchWindowSaveButtonClick: function () {
			if (this.grid.getSelectionModel().hasSelection())
				this.cmfg('fieldValueSet', this.grid.getSelectionModel().getSelection()[0]);

			this.view.hide();
		},

		/**
		 * @returns {Void}
		 */
		onFieldSearchWindowSelectionChange: function () {
			this.view.saveButton.setDisabled(!this.grid.getSelectionModel().hasSelection());
		},

		/**
		 * After window show setup presets (title, quick search filter, selection)
		 *
		 * @returns {Void}
		 */
		onFieldSearchWindowShow: function () {
			if (this.fieldSearchWindowConfigurationIsEmpty()) {
				_error('search window configuration empty', this);
			} else {
				this.setupViewGrid();

				// Set window title
				this.setViewTitle(this.cmfg('fieldSearchWindowConfigurationGet', [CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.TEXT]));

				this.setupViewAddCardButton();

				// Setup save button
				this.cmfg('onFieldSearchWindowSelectionChange');
			}
		},

		/**
		 * Selected value setup
		 *
		 * @returns {Void}
		 */
		onFieldSearchWindowStoreLoad: function () {
			if (!Ext.isEmpty(this.cmfg('fieldValueGet')))
				this.grid.getSelectionModel().select(
					this.grid.getStore().find(this.cmfg('fiedlValueFieldGet'), this.cmfg('fieldValueGet'))
				);
		},

		/**
		 * Adapter function
		 *
		 * @returns {Void}
		 *
		 * @private
		 *
		 * TODO: waiting for refactor (CMDBuild.view.common.field.searchWindow.GridPanel)
		 */
		setupViewAddCardButton: function () {
			this.view.addCardButton.setDisabled(this.cmfg('fieldSearchWindowConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY));

			if (!this.cmfg('fieldSearchWindowConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)) {
				this.view.addCardButton.updateForEntry(this.cmfg('fieldSearchWindowConfigurationGet', CMDBuild.core.constants.Proxy.ENTRY_TYPE));

				this.view.mon(this.view.addCardButton, 'cmClick', function (p) {
					var w = new CMDBuild.view.management.common.CMCardWindow({
						withButtons: true,
						title: p.className
					});

					new CMDBuild.controller.management.common.CMCardWindowController(w, {
						cmEditMode: true,
						card: null,
						entryType: p.classId
					});
					w.show();

					this.view.mon(w, 'destroy', function () {
						this.grid.reload();
					}, this);

				}, this);
			}
		},

		/**
		 * Adapter function
		 *
		 * @returns {Void}
		 *
		 * @private
		 *
		 * TODO: waiting for refactor (CMDBuild.view.common.field.searchWindow.GridPanel)
		 */
		setupViewGrid: function () {
			this.view.removeAll(false);
			this.view.add(
				this.grid = Ext.create('CMDBuild.view.common.field.searchWindow.GridPanel', {
					delegate: this,
					CQL: Ext.isEmpty(this.cmfg('fieldStoreGet')) ? null : this.cmfg('fieldStoreGet').getProxy().extraParams,
					selModel: Ext.create('CMDBuild.selection.CMMultiPageSelectionModel', {
						mode: 'SINGLE',
						idProperty: 'Id' // Required to identify the records for the data and not the id of Ext
					})
				})
			);

			this.grid.updateStoreForClassId(
				this.cmfg('fieldSearchWindowConfigurationGet', [CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID]),
				{
					scope: this,
					cb: function (grid) {
						this.grid.getStore().loadPage(1);

						// Setup quick search value
						this.grid.gridSearchField.focus();
						this.grid.gridSearchField.setValue(
							this.cmfg('fieldSearchWindowConfigurationGet', [
								CMDBuild.core.constants.Proxy.GRID_CONFIGURATION,
								CMDBuild.core.constants.Proxy.PRESETS,
								CMDBuild.core.constants.Proxy.QUICK_SEARCH
							])
						);

						this.grid.getStore().on('load', function (store, records, successful, eOpts) {
							this.cmfg('onFieldSearchWindowStoreLoad');
						}, this);
					}
				}
			);
		}
	});

})();
