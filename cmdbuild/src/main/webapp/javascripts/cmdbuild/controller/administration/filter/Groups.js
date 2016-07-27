(function() {

	Ext.define('CMDBuild.controller.administration.filter.Groups', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.filter.Group',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.filter.Filter}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFilterGroupsAbortButtonClick',
			'onFilterGroupsAddButtonClick',
			'onFilterGroupsModifyButtonClick = onFilterGroupsItemDoubleClick',
			'onFilterGroupsRemoveButtonClick',
			'onFilterGroupsRowSelected',
			'onFilterGroupsSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.filter.groups.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.filter.groups.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.filter.group.SelectedFilter}
		 *
		 * @private
		 */
		selectedFilter: undefined,

		/**
		 * @property {CMDBuild.view.administration.filter.groups.GroupsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.filter.Filter} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.filter.groups.GroupsView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		onFilterGroupsAbortButtonClick: function() {
			if (!this.filterGroupsSelectedFilterIsEmpty()) {
				this.onFilterGroupsRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onFilterGroupsAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.filterGroupsSelectedFilterReset();

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.filter.group.Store'));
		},

		onFilterGroupsModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onFilterGroupsRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function(buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		onFilterGroupsRowSelected: function() {
			var selectedRecord = this.grid.getSelectionModel().getSelection()[0];

			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = selectedRecord.get(CMDBuild.core.constants.Proxy.ID);

			CMDBuild.proxy.filter.Group.read({ // TODO: waiting for refactor (CRUD)
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.FILTERS];

					this.filterGroupsSelectedFilterSet({
						value: Ext.Array.findBy(decodedResponse, function(filterObject, i) {
							return selectedRecord.get(CMDBuild.core.constants.Proxy.ID) == filterObject[CMDBuild.core.constants.Proxy.ID];
						}, this)
					});

					CMDBuild.proxy.filter.Group.readDefaults({
						params: params,
						scope: this,
						success: function(response, options, decodedResponse) {
							decodedResponse = decodedResponse.response.elements;

							this.filterGroupsSelectedFilterSet({
								propertyName: CMDBuild.core.constants.Proxy.DEFAULT_FOR_GROUPS,
								value: decodedResponse
							});

							this.form.loadRecord(this.filterGroupsSelectedFilterGet());
							this.form.setDisabledModify(true, true);
						}
					});
				}
			});
		},

		onFilterGroupsSaveButtonClick: function() {
			if (this.validate(this.form)) {
				var formDataModel = Ext.create('CMDBuild.model.filter.group.Store', this.form.getData(true));

				var params = formDataModel.getData();
				params[CMDBuild.core.constants.Proxy.CONFIGURATION] = Ext.encode(params[CMDBuild.core.constants.Proxy.CONFIGURATION]);
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = params[CMDBuild.core.constants.Proxy.ENTRY_TYPE]; // TODO: waiting for refactor (reads a entryType parameter but i write as className)

				if (Ext.isEmpty(formDataModel.get(CMDBuild.core.constants.Proxy.ID))) {
					CMDBuild.proxy.filter.Group.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.filter.Group.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		removeItem: function() {
			if (!this.filterGroupsSelectedFilterIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.filterGroupsSelectedFilterGet(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.filter.Group.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.form.reset();

						this.grid.getStore().load({
							scope: this,
							callback: function(records, operation, success) {
								this.grid.getSelectionModel().select(0, true);

								// If no selections disable all UI
								if (!this.grid.getSelectionModel().hasSelection())
									this.form.setDisabledModify(true, true, true, true);
							}
						});
					}
				});
			}
		},

		// SelectedFilter property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			filterGroupsSelectedFilterGet: function(attributePath) {
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
			filterGroupsSelectedFilterIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedFilter';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			filterGroupsSelectedFilterReset: function() {
				this.propertyManageReset('selectedFilter');
			},

			/**
			 * @param {Object} parameters
			 */
			filterGroupsSelectedFilterSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.filter.group.SelectedFilter';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedFilter';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 *
		 * TODO: waiting for refactor (save all group attributes in one call)
		 */
		success: function(result, options, decodedResult) {
			var me = this;
			var savedFilterObject = decodedResult[CMDBuild.core.constants.Proxy.FILTER] || options.params;

			CMDBuild.view.common.field.translatable.Utils.commit(this.view.form);

			CMDBuild.core.Message.success();

			var params = {};
			params[CMDBuild.core.constants.Proxy.FILTERS] = Ext.encode([savedFilterObject[CMDBuild.core.constants.Proxy.ID]]);
			params[CMDBuild.core.constants.Proxy.GROUPS] = Ext.encode(this.form.defaultForGroupsField.getValue());

			CMDBuild.proxy.filter.Group.updateDefaults({ params: params });

			this.grid.getStore().load({
				callback: function(records, operation, success) {
					var rowIndex = this.find(
						CMDBuild.core.constants.Proxy.NAME,
						me.form.getForm().findField(CMDBuild.core.constants.Proxy.NAME).getValue()
					);

					me.grid.getSelectionModel().select(rowIndex, true);
					me.form.setDisabledModify(true);
				}
			});
		}
	});

})();