(function() {

	Ext.define('CMDBuild.controller.administration.lookup.List', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.lookup.Lookup',
			'CMDBuild.model.lookup.Lookup',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.lookup.Lookup}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLookupListAbortButtonClick',
			'onLookupListAddButtonClick',
			'onLookupListDrop',
			'onLookupListEnableDisableButtonClick',
			'onLookupListLookupSelected = onLookupSelected',
			'onLookupListModifyButtonClick = onLookupListItemDoubleClick',
			'onLookupListRowSelected',
			'onLookupListSaveButtonClick',
			'onLookupListTabShow'
		],

		/**
		 * @property {CMDBuild.view.administration.lookup.list.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.lookup.list.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.lookup.Lookup or null}
		 */
		selectedLookup: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.lookup.list.ListView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.lookup.Lookup} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.lookup.list.ListView', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;
			this.form = this.view.form;
		},

		// SelectedLookup methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			lookupListSelectedLookupGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedLookup';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			lookupListSelectedLookupIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedLookup';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},


			lookupListSelectedLookupReset: function() {
				this.propertyManageReset('selectedLookup');
			},

			/**
			 * @param {Object} parameters
			 */
			lookupListSelectedLookupSet: function(parameters) {
				parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.lookup.Lookup';
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedLookup';

				this.propertyManageSet(parameters);
			},

		onLookupListAbortButtonClick: function() {
			if (this.lookupListSelectedLookupIsEmpty()) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.onLookupListRowSelected();
			}
		},

		onLookupListAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.lookupListSelectedLookupReset();

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.lookup.Lookup'));
		},

		onLookupListDrop: function() {
			var gridRowsObjects = [];

			Ext.Array.forEach(this.grid.getStore().getRange(), function(row, i, allRows) {
				var rowObject = {};
				rowObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = row.get('Description');
				rowObject[CMDBuild.core.constants.Proxy.ID] = row.get('Id');
				rowObject[CMDBuild.core.constants.Proxy.INDEX] = i + 1;

				gridRowsObjects.push(rowObject);
			}, this);

			var params = {};
			params[CMDBuild.core.constants.Proxy.TYPE] = this.cmfg('lookupSelectedLookupTypeGet', CMDBuild.core.constants.Proxy.ID);
			params['lookuplist'] = Ext.encode(gridRowsObjects);

			CMDBuild.proxy.lookup.Lookup.setOrder({
				params: params,
				scope: this,
				success: function(result, options, decodedResult) {
					this.onLookupListLookupSelected();
				}
			});
		},

		onLookupListEnableDisableButtonClick: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = this.lookupListSelectedLookupGet(CMDBuild.core.constants.Proxy.ID);

			if (this.form.activeCheckbox.getValue()) {
				CMDBuild.proxy.lookup.Lookup.disable({
					params: params,
					scope: this,
					success: function(result, options, decodedResult) {
						this.onLookupListLookupSelected();
					}
				});
			} else {
				CMDBuild.proxy.lookup.Lookup.enable({
					params: params,
					scope: this,
					success: function(result, options, decodedResult) {
						this.onLookupListLookupSelected();
					}
				});
			}
		},

		onLookupListLookupSelected: function() {
			this.view.setDisabled(this.cmfg('lookupSelectedLookupTypeIsEmpty'));

			this.lookupListSelectedLookupReset();

			if (!this.cmfg('lookupSelectedLookupTypeIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.TYPE] = this.cmfg('lookupSelectedLookupTypeGet', CMDBuild.core.constants.Proxy.ID);

				this.grid.getStore().load({
					params: params,
					scope: this,
					callback: function(records, operation, success) {
						var rowIndex = 0;

						if (!this.lookupListSelectedLookupIsEmpty())
							rowIndex = this.grid.getStore().find('Id', this.lookupListSelectedLookupGet(CMDBuild.core.constants.Proxy.ID));

						this.grid.getSelectionModel().select(rowIndex, true);
						this.form.setDisabledModify(true);
					}
				});
			}
		},

		onLookupListModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		/**
		 * TODO: waiting for refactor (crud + rename)
		 */
		onLookupListRowSelected: function() {
			if (this.grid.getSelectionModel().hasSelection()) {
				var gridSelection = this.grid.getSelectionModel().getSelection()[0];
				var params = {};
				params[CMDBuild.core.constants.Proxy.TYPE] = this.cmfg('lookupSelectedLookupTypeGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.lookup.Lookup.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ROWS];

						this.lookupListSelectedLookupSet({
							value: Ext.Array.findBy(decodedResponse, function(item, i) {
								return gridSelection.get('Id') == item['Id'];
							}, this)
						});

						this.form.loadRecord(this.lookupListSelectedLookupGet());
						this.form.parentCombobox.getStore().load({ params: params }); // Refresh store
						this.form.enableDisableButton.setActiveState(this.lookupListSelectedLookupGet(CMDBuild.core.constants.Proxy.ACTIVE));
						this.form.setDisabledModify(true, true);
					}
				});
			}
		},

		onLookupListSaveButtonClick: function() {
			if (this.validate(this.form)) {
				var params = CMDBuild.model.lookup.Lookup.convertToLegacy(this.form.getData(true)); // Use model to convert form values in legacy object
				params['Type'] = this.cmfg('lookupSelectedLookupTypeGet', CMDBuild.core.constants.Proxy.ID); // TODO: wrong server implementation to fix

				if (Ext.isEmpty(params['Id'])) {
					CMDBuild.proxy.lookup.Lookup.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.lookup.Lookup.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		onLookupListTabShow: function() {
			if (!this.grid.getSelectionModel().hasSelection())
				this.grid.getSelectionModel().select(0, true);
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 */
		success: function(result, options, decodedResult) {
			if (!Ext.isEmpty(decodedResult[CMDBuild.core.constants.Proxy.LOOKUP]))
				this.lookupListSelectedLookupSet({ value: decodedResult[CMDBuild.core.constants.Proxy.LOOKUP] });

			// HACK to apply TranslationUuid to form to be able to save translations ... because lookups doesn't have an identifier like a name
			this.form.loadRecord(this.lookupListSelectedLookupGet());

			CMDBuild.view.common.field.translatable.Utils.commit(this.form);

			CMDBuild.core.Message.success();

			this.onLookupListLookupSelected();
		}
	});

})();