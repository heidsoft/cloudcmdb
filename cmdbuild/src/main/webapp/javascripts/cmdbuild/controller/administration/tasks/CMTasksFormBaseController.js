(function() {

	/**
	 * Base class to extends to create form controller implementation
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormBaseController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask'
		],

		/**
		 * @cfg {Array} array of all step delegates
		 */
		delegateStep: undefined,

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Int}
		 */
		selectedId: undefined,

		/**
		 * @property {Ext.selection.Model}
		 */
		selectionModel: undefined,

		/**
		 * @cfg {String}
		 */
		taskType: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.CMTasksForm}
		 */
		view: undefined,

		/**
		 * @abstract
		 */
		cmOn: function() {
			_debug('CMTasksFormBaseController: cmOn() unimplemented method');
		},

		disableTypeField: function() {
			this.delegateStep[0].setDisabledTypeField(true);
		},

		onAbortButtonClick: function() {
			if (this.selectedId != null) {
				this.onRowSelected();
			} else {
				this.view.reset();
				this.view.disableModify();
				this.parentDelegate.changeItem(0);
			}
		},

		/**
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		onAddButtonClick: function(name, param, callBack) {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.parentDelegate.loadForm(param.type);
			this.view.reset();
			this.view.enableTabbedModify();
			this.disableTypeField();
			this.parentDelegate.changeItem(0);
		},

		onCloneButtonClick: function() {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.resetIdField();
			this.view.disableCMTbar();
			this.view.enableCMButtons();
			this.view.enableTabbedModify(true);
			this.disableTypeField();
			this.parentDelegate.changeItem(0);
		},

		onModifyButtonClick: function() {
			this.view.disableCMTbar();
			this.view.enableCMButtons();
			this.view.enableTabbedModify(true);
			this.disableTypeField();
			this.parentDelegate.changeItem(0);
		},

		onRemoveButtonClick: function() {
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

		/**
		 * @abstract
		 */
		onRowSelected: function() {
			_debug('CMTasksFormBaseController: onRowSelected() unimplemented method');
		},

		/**
		 * @abstract
		 */
		onSaveButtonClick: function() {
			_debug('CMTasksFormBaseController: onSaveButtonClick() unimplemented method');
		},

		resetIdField: function() {
			this.delegateStep[0].setValueId();
		},

		/**
		 * @param {Boolean} state
		 */
		setDisabledButtonNext: function(state) {
			this.view.nextButton.setDisabled(state);
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 */
		success: function(result, options, decodedResult) {
			var me = this;
			var taskId = this.delegateStep[0].getValueId();

			this.parentDelegate.grid.store.load({
				callback: function() {
					me.view.removeAll();

					var rowIndex = this.find(
						CMDBuild.core.constants.Proxy.ID,
						(decodedResult.response) ? decodedResult.response : taskId
					);

					me.selectionModel.deselectAll();
					me.selectionModel.select(
						(rowIndex < 0) ? 0 : rowIndex,
						true
					);

					if (!me.selectionModel.hasSelection())
						me.view.disableModify();
				}
			});

			this.view.disableModify(true);
			this.parentDelegate.changeItem(0);
		},

		/**
		 * Task validation
		 *
		 * @param {Boolean} enable
		 *
		 * @return {Boolean}
		 *
		 * @override
		 */
		validate: function(enable, type) {
			return this.callParent([this.view]);
		}
	});

})();