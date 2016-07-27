(function() {

	Ext.define('CMDBuild.controller.administration.dataView.Filter', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.dataView.Filter',
			'CMDBuild.core.Utils',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.dataView.DataView}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onDataViewFilterAbortButtonClick',
			'onDataViewFilterAddButtonClick',
			'onDataViewFilterModifyButtonClick = onDataViewFilterItemDoubleClick',
			'onDataViewFilterRemoveButtonClick',
			'onDataViewFilterRowSelected',
			'onDataViewFilterSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.dataView.filter.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataView.filter.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.dataView.filter.SelectedView}
		 *
		 * @private
		 */
		selectedView: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataView.filter.FilterView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.dataView.DataView} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.dataView.filter.FilterView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		onDataViewFilterAbortButtonClick: function() {
			if (!this.selectedViewIsEmpty()) {
				this.onDataViewFilterRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onDataViewFilterAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.selectedViewReset();

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.dataView.filter.GridStore'));
		},

		onDataViewFilterModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onDataViewFilterRemoveButtonClick: function() {
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

		onDataViewFilterRowSelected: function() {
			var selectedViewId = this.grid.getSelectionModel().getSelection()[0].get(CMDBuild.core.constants.Proxy.ID);

			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = selectedViewId;

			CMDBuild.proxy.dataView.Filter.read({ // TODO: waiting for refactor (CRUD)
				params: params,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.VIEWS];

					this.selectedViewSet({
						value: Ext.Array.findBy(decodedResponse, function(viewObject, i) {
							return selectedViewId == viewObject[CMDBuild.core.constants.Proxy.ID];
						}, this)
					});

					// TODO: waiting for refactor (CRUD with decoded filter data)
					if (CMDBuild.core.Utils.isJsonString(this.selectedViewGet(CMDBuild.core.constants.Proxy.FILTER)))
						this.selectedViewSet({
							propertyName: CMDBuild.core.constants.Proxy.FILTER,
							value: Ext.decode(this.selectedViewGet(CMDBuild.core.constants.Proxy.FILTER))
						});

					this.form.loadRecord(this.selectedViewGet());
					this.form.setDisabledModify(true, true);
				}
			});
		},

		onDataViewFilterSaveButtonClick: function() {
			if (this.validate(this.form)) {
				var formDataModel = Ext.create('CMDBuild.model.dataView.filter.SelectedView', this.form.getData(true));

				var params = formDataModel.getData();
				params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode(params[CMDBuild.core.constants.Proxy.FILTER]);

				if (Ext.isEmpty(formDataModel.get(CMDBuild.core.constants.Proxy.ID))) {
					CMDBuild.proxy.dataView.Filter.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.dataView.Filter.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		/**
		 * @private
		 */
		removeItem: function() {
			if (!this.selectedViewIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.selectedViewGet(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.dataView.Filter.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.form.reset();

						CMDBuild.core.Message.success();

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

		// SelectedView property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			selectedViewGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedView';
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
			selectedViewIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedView';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @private
			 */
			selectedViewReset: function() {
				this.propertyManageReset('selectedView');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			selectedViewSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.dataView.filter.SelectedView';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedView';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 *
		 * @private
		 */
		success: function(response, options, decodedResponse) {
			var me = this;

			CMDBuild.view.common.field.translatable.Utils.commit(this.form);

			CMDBuild.core.Message.success();

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