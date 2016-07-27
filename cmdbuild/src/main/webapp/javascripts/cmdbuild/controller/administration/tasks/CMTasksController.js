(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask',
			'CMDBuild.proxy.taskManager.Connector',
			'CMDBuild.proxy.taskManager.Email',
			'CMDBuild.proxy.taskManager.event.Asynchronous',
			'CMDBuild.proxy.taskManager.event.Event',
			'CMDBuild.proxy.taskManager.event.Synchronous',
			'CMDBuild.proxy.taskManager.Generic',
			'CMDBuild.proxy.taskManager.TaskManager',
			'CMDBuild.proxy.taskManager.Workflow',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.tasks.CMTasksForm}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.tasks.CMTasksGrid}
		 */
		grid: undefined,

		/**
		 * @property {Ext.selection.Model}
		 */
		selectionModel: undefined,

		/**
		 * Used to validate tasks
		 *
		 * @cfg {Array}
		 */
		tasksDatas: [
			'all',
			'connector',
			'email',
			'event',
			'event_asynchronous',
			'event_synchronous',
			'generic',
			'workflow'
		],

		/**
		 * @cfg {"CMDBuild.view.administration.tasks.CMTasks"}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.tasks.CMTasks} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange and controller setup
			this.grid = view.grid;
			this.form = view.form;
			this.formLayout = view.form.getLayout();
			this.view.delegate = this;
			this.grid.delegate = this;

			this.selectionModel = this.grid.getSelectionModel();
		},

		/**
		 * @param {CMDBuild.model.common.Accordion} parameters
		 *
		 * @override
		 */
		onViewOnFront: function(parameters) {
			if (!Ext.isEmpty(parameters)) {
				this.taskType = (this.correctTaskTypeCheck(parameters.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]))
					? parameters.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0] : this.tasksDatas[0];

				this.grid.reconfigure(this.geStoreByTaskType(this.taskType));
				this.grid.getStore().load({
					scope: this,
					callback: function() {
						if (!this.selectionModel.hasSelection()) {
							this.selectionModel.select(0, true);
							this.form.removeAll();
							this.form.disableModify();
						}
					}
				});

				// Fire show event on accordion click
				this.view.fireEvent('show');

				this.setViewTitle(parameters.get(CMDBuild.core.constants.Proxy.TEXT));

				this.callParent(arguments);
			}
		},

		/**
		 * @returns {Mixed}
		 *
		 * @private
		 */
		geStoreByTaskType: function (type) {
			switch (type) {
				case 'all':
					return CMDBuild.proxy.taskManager.TaskManager.getStore();

				case 'connector':
					return CMDBuild.proxy.taskManager.Connector.getStore();

				case 'email':
					return CMDBuild.proxy.taskManager.Email.getStore();

				case 'event':
					return CMDBuild.proxy.taskManager.event.Event.getStore();

				case 'event_asynchronous':
					return CMDBuild.proxy.taskManager.event.Asynchronous.getStore();

				case 'event_synchronous':
					return CMDBuild.proxy.taskManager.event.Synchronous.getStore();

				case 'generic':
					return CMDBuild.proxy.taskManager.Generic.getStore();

				case 'workflow':
					return CMDBuild.proxy.taskManager.Workflow.getStore();

				default:
					throw 'CMProxyTasks error: url type not recognized';
			}
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onCyclicExecutionButtonClick':
					return this.onCyclicExecutionButtonClick(param);

				case 'onItemDoubleClick':
					return this.onItemDoubleClick();

				case 'onNextButtonClick':
					return this.changeItem('next');

				case 'onPreviousButtonClick':
					return this.changeItem('prev');

				case 'onRowSelected':
					return this.onRowSelected(name, param, callBack);

				case 'onSingleExecutionButtonClick':
					return this.onSingleExecutionButtonClick(param);

				case 'onStopButtonClick':
					return this.onStopButtonClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Automated form controller constructor
		 *
		 * @param {String} type
		 */
		buildFormController: function(type) {
			if (this.correctTaskTypeCheck(type)) {
				this.form.delegate = Ext.create(
					'CMDBuild.controller.administration.tasks.CMTasksForm' + CMDBuild.core.Utils.toTitleCase(this.typeSerialize(type, 1)) + 'Controller',
					this.form
				);
				this.form.delegate.parentDelegate = this;
				this.form.delegate.selectionModel = this.selectionModel;
			}
		},

		/**
		 * @override
		 */
		callback: function() {
			this.grid.store.load();

			this.callParent(arguments);
		},

		/**
		 * To change wizard displayed item
		 *
		 * @param (String/Integer) action
		 */
		changeItem: function(action) {
			if (
				typeof action == 'number'
				&& (action >= 0 && action < this.form.items.lenght)
			) {
				this.formLayout.setActiveItem(action);
			} else {
				switch (action) {
					case 'next': {
						if (this.formLayout.getNext())
							this.formLayout.next();
					} break;

					case 'prev': {
						if (this.formLayout.getPrev())
							this.formLayout.prev();
					} break;
				}
			}

			if (this.formLayout.getPrev()) {
				this.form.previousButton.setDisabled(false);
			} else {
				this.form.previousButton.setDisabled(true);
			}

			if (this.formLayout.getNext()) {
				this.form.nextButton.setDisabled(false);
			} else {
				this.form.nextButton.setDisabled(true);
			}

			// Fires activate event on first item
			if (!Ext.isEmpty(this.formLayout.getActiveItem()) && !this.formLayout.getPrev())
				this.formLayout.getActiveItem().fireEvent('activate');
		},

		/**
		 * @param {String} type - form type identifier
		 *
		 * @return {Boolean} type recognition state
		 */
		correctTaskTypeCheck: function(type) {
			return (
				!Ext.isEmpty(type)
				&& Ext.Array.contains(this.tasksDatas, type)
			) ? true : false;
		},

		/**
		 * Form wizard creator
		 *
		 * @param {String} type - form type identifier
		 */
		loadForm: function(type) {
			if (this.correctTaskTypeCheck(type)) {
				this.form.removeAll(true);
				this.form.delegate.delegateStep = [];

				var items = Ext.create('CMDBuild.view.administration.tasks.' + this.typeSerialize(type, 0) + '.CMTaskSteps');

				for (var key in items) {
					items[key].delegate.parentDelegate = this.form.delegate; // Controller relations propagation

					this.form.delegate.delegateStep.push(items[key].delegate);
					this.form.add(items[key]);
				}

				this.changeItem(0);
			}
		},

		/**
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		onAddButtonClick: function(name, param, callBack) {
			this.selectionModel.deselectAll();
			this.buildFormController(param.type);

			return this.form.delegate.cmOn(name, param, callBack);
		},

		/**
		 * On grid item double click to edit double-clicked task
		 */
		onItemDoubleClick: function() {
			this.form.delegate.onModifyButtonClick();
		},

		/**
		 * Check for a right form controller and/or creates it and then calls delegate's onRowSelected function
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		onRowSelected: function(name, param, callBack) {
			var selectedType = this.selectionModel.getSelection()[0].get(CMDBuild.core.constants.Proxy.TYPE);

			if (
				!this.form.delegate
				|| (this.form.delegate.taskType != selectedType)
			) {
				this.buildFormController(selectedType);
			}

			if (this.form.delegate)
				this.form.delegate.cmOn(name, param, callBack);
		},

		/**
		 * @param {Object} record
		 *
		 * @returns {Void}
		 */
		onCyclicExecutionButtonClick: function (record) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = record.get(CMDBuild.core.constants.Proxy.ID);

			CMDBuild.core.LoadMask.show();

			CMDBuild.proxy.taskManager.TaskManager.cyclicExecution({
				params: params,
				loadMask: false,
				scope: this,
				success: this.success,
				callback: this.callback
			});
		},

		/**
		 * @param {Object} record
		 *
		 * @returns {Void}
		 */
		onSingleExecutionButtonClick: function(record) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = record.get(CMDBuild.core.constants.Proxy.ID);

			CMDBuild.proxy.taskManager.TaskManager.singleExecution({
				params: params,
				loadMask: false,
				scope: this,
				success: this.success,
				callback: this.callback
			});
		},

		/**
		 * @param {Object} record
		 *
		 * @returns {Void}
		 */
		onStopButtonClick: function (record) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = record.get(CMDBuild.core.constants.Proxy.ID);

			CMDBuild.core.LoadMask.show();

			CMDBuild.proxy.taskManager.TaskManager.stop({
				params: params,
				loadMask: false,
				scope: this,
				success: this.success,
				callback: this.callback
			});
		},

		/**
		 * Setup view panel title as a breadcrumbs component
		 *
		 * @param {String} titlePart
		 */
		setViewTitle: function(titlePart) {
			if (!Ext.isEmpty(titlePart))
				this.view.setTitle(this.view.baseTitle + CMDBuild.core.constants.Global.getTitleSeparator() + titlePart);
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 */
		success: function(result, options, decodedResult) {
			var me = this;

			this.grid.store.load({
				callback: function() {
					me.form.removeAll();
					me.form.disableModify(true);

					var rowIndex = this.find(
						CMDBuild.core.constants.Proxy.ID,
						options.params[CMDBuild.core.constants.Proxy.ID]
					);

					me.selectionModel.deselectAll();
					me.selectionModel.select(
						(rowIndex < 0) ? 0 : rowIndex,
						true
					);
				}
			});
		},

		/**
		 * Function to serialize type and return as class path string (without header and footer dots)
		 *
		 * @param {String} type
		 * @param {Int} itemsToReturn
		 *
		 * @return {String}
		 */
		typeSerialize: function(type, itemsToReturn) {
			var splittedType = type.split('_');

			if (
				splittedType.length > 1
				&& typeof itemsToReturn == 'number'
				&& itemsToReturn > 0
				&& itemsToReturn <= splittedType.length
			) {
				splittedType = splittedType.slice(0, itemsToReturn);
			} else {
				splittedType = splittedType.slice(0, splittedType.length);
			}

			return splittedType.join('.');
		}
	});

})();