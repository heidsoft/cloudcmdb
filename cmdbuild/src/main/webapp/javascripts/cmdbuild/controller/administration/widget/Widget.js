(function () {

	Ext.define('CMDBuild.controller.administration.widget.Widget', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.Classes',
			'CMDBuild.proxy.widget.Widget'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'classTabWidgetTypeRenderer',
			'onClassTabWidgetAbortButtonClick',
			'onClassTabWidgetAddButtonClick',
			'onClassTabWidgetClassAddButtonClick = onAddClassButtonClick',
			'onClassTabWidgetClassSelected = onClassSelected',
			'onClassTabWidgetItemDrop',
			'onClassTabWidgetModifyButtonClick = onClassTabWidgetItemDoubleClick',
			'onClassTabWidgetPanelShow',
			'onClassTabWidgetRemoveButtonClick',
			'onClassTabWidgetRowSelected',
			'onClassTabWidgetSaveButtonClick',
			'validate = classTabWidgetValidateForm'
		],

		/**
		 * @property {Mixed}
		 */
		controllerWidgetForm: undefined,

		/**
		 * @property {Mixed}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.widget.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.classes.Class}
		 *
		 * @private
		 */
		selectedClass: undefined,

		/**
		 * @property {Mixed}
		 *
		 * @private
		 */
		selectedWidget: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.widget.WidgetView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Object} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.widget.WidgetView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		/**
		 * @private
		 */
		buildForm: function () {
			if (!Ext.isEmpty(this.form))
				this.view.remove(this.form);

			if (!Ext.isEmpty(this.controllerWidgetForm)) {
				// Shorthands
				this.form = this.controllerWidgetForm.getView();
				this.view.form = this.form;

				this.view.add(this.form);
			}
		},

		/**
		 * @param {String} type
		 *
		 * @returns {Mixed}
		 *
		 * @private
		 */
		buildFormController: function (type) {
			switch (type) {
				case '.Calendar':
					return Ext.create('CMDBuild.controller.administration.widget.form.Calendar', { parentDelegate: this });

				case '.CreateModifyCard':
					return Ext.create('CMDBuild.controller.administration.widget.form.CreateModifyCard', { parentDelegate: this });

				case '.OpenReport':
					return Ext.create('CMDBuild.controller.administration.widget.form.OpenReport', { parentDelegate: this });

				case '.Ping':
					return Ext.create('CMDBuild.controller.administration.widget.form.Ping', { parentDelegate: this });

				case '.Workflow':
					return Ext.create('CMDBuild.controller.administration.widget.form.Workflow', { parentDelegate: this });

				default:
					return Ext.create('CMDBuild.controller.administration.widget.form.Empty', { parentDelegate: this });
			}
		},

		/**
		 * Act as renderer for text field
		 *
		 * @param {String} value
		 *
		 * @returns {String}
		 */
		classTabWidgetTypeRenderer: function (value) {
			switch (value) {
				case '.Calendar':
					return CMDBuild.Translation.calendar;

				case '.CreateModifyCard':
					return CMDBuild.Translation.createModifyCard;

				case '.OpenReport':
					return CMDBuild.Translation.createReport;

				case '.Ping':
					return CMDBuild.Translation.ping;

				case '.Workflow':
					return CMDBuild.Translation.startWorkflow;

				default:
					return value;
			}
		},

		onClassTabWidgetAbortButtonClick: function () {
			if (!this.classTabWidgetSelectedWidgetIsEmpty()) {
				this.cmfg('onClassTabWidgetRowSelected');
			} else if (!Ext.isEmpty(this.form)) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		/**
		 * @param {String} type
		 */
		onClassTabWidgetAddButtonClick: function (type) {
			if (!Ext.isEmpty(type) && Ext.isString(type)) {
				this.grid.getSelectionModel().deselectAll();

				this.classTabWidgetSelectedWidgetReset();

				this.controllerWidgetForm = this.buildFormController(type);
				this.buildForm();

				if (!Ext.isEmpty(this.controllerWidgetForm) && Ext.isFunction(this.controllerWidgetForm.cmfg))
					this.controllerWidgetForm.cmfg('classTabWidgetAdd');
			}
		},

		/**
		 * Invoked from parentDelegate
		 */
		onClassTabWidgetClassAddButtonClick: function () {
			this.view.disable();
		},

		/**
		 * Get selected class data and enable/disable tab, invoked from parentDelegate
		 *
		 * @param {Number} classId
		 */
		onClassTabWidgetClassSelected: function (classId) {
			if (!Ext.isEmpty(classId) && Ext.isNumeric(classId)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.proxy.Classes.readAll({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES] || [];

						var selectedClass = Ext.Array.findBy(decodedResponse, function (classObject, i) {
							return classId == classObject[CMDBuild.core.constants.Proxy.ID];
						}, this);

						if (!Ext.isEmpty(selectedClass)) {
							this.classTabWidgetSelectedClassSet({ value: selectedClass });

							// BUSINESS RULE: currently the widgets are not inherited so, deny the definition on superClasses
							this.view.setDisabled(this.classTabWidgetSelectedClassGet(CMDBuild.core.constants.Proxy.IS_SUPER_CLASS));
						} else {
							_error('class with id "' + classId + '" not found', this);
						}
					}
				});
			} else {
				_error('onClassSelected empty or invalid class id parameter', this);
			}
		},

		onClassTabWidgetItemDrop: function () { // TODO: temporary
			if (!Ext.isEmpty(this.grid.getStore().getRange()) && Ext.isArray(this.grid.getStore().getRange())) {
				var params = {};
				params['sortedArray'] = [];

				Ext.Array.forEach(this.grid.getStore().getRange(), function (widgetRowModel, i, allWidgetRowModels) {
					if (!Ext.isEmpty(widgetRowModel))
						params['sortedArray'].push(widgetRowModel.get(CMDBuild.core.constants.Proxy.ID));
				}, this);

				CMDBuild.proxy.widget.Widget.setSorting({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.cmfg('onClassTabWidgetPanelShow');
					}
				});
			}
		},

		onClassTabWidgetModifyButtonClick: function () {
			if (!Ext.isEmpty(this.form))
				this.form.setDisabledModify(false);
		},

		/**
		 * Loads store passing selected class name and selects first or with idToSelect id value card
		 *
		 * @param {Number} idToSelect
		 */
		onClassTabWidgetPanelShow: function (idToSelect) {
			idToSelect = Ext.isNumber(idToSelect) ? idToSelect : 0;

			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.classTabWidgetSelectedClassGet(CMDBuild.core.constants.Proxy.NAME);

			if (!this.grid.getStore().isLoading())
				this.grid.getStore().load({
					params: params,
					scope: this,
					callback: function (records, operation, success) {
						var selectedRecordIndex = this.grid.getStore().find(CMDBuild.core.constants.Proxy.ID, idToSelect);

						this.grid.getSelectionModel().select(
							selectedRecordIndex > 0 ? selectedRecordIndex : idToSelect,
							true
						);

						this.cmfg('onClassTabWidgetRowSelected');
					}
				});
		},

		onClassTabWidgetRemoveButtonClick: function () {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		onClassTabWidgetRowSelected: function () {
			if (this.grid.getSelectionModel().hasSelection()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.classTabWidgetSelectedClassGet(CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.ID] = this.grid.getSelectionModel().getSelection()[0].get(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.widget.Widget.read({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE] || [];

						this.controllerWidgetForm = this.buildFormController(decodedResponse[CMDBuild.core.constants.Proxy.TYPE]);

						this.classTabWidgetSelectedWidgetSet({ value: decodedResponse });

						if (!this.classTabWidgetSelectedWidgetIsEmpty()) {
							this.buildForm();

							if (!Ext.isEmpty(this.controllerWidgetForm))
								this.controllerWidgetForm.cmfg('classTabWidgetLoadRecord', this.classTabWidgetSelectedWidgetGet());

							this.form.setDisabledModify(true, true);
						}
					}
				});
			} else {
				this.controllerWidgetForm = this.buildFormController();

				this.buildForm();

				this.form.setDisabledModify(true, true, true);
			}
		},

		onClassTabWidgetSaveButtonClick: function () {
			if (this.controllerWidgetForm.cmfg('classTabWidgetValidateForm', this.form)) {
				var widgetDefinition = this.controllerWidgetForm.cmfg('classTabWidgetDefinitionGet');

				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.classTabWidgetSelectedClassGet(CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.WIDGET] = Ext.encode(widgetDefinition);

				if (Ext.isEmpty(widgetDefinition[CMDBuild.core.constants.Proxy.ID])) {
					CMDBuild.proxy.widget.Widget.create({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

							this.cmfg('onClassTabWidgetPanelShow', decodedResponse);
						}
					});
				} else {
					CMDBuild.proxy.widget.Widget.update({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							this.cmfg('onClassTabWidgetPanelShow', parseInt(this.form.getForm().findField(CMDBuild.core.constants.Proxy.ID).getValue()));
						}
					});
				}
			}
		},

		/**
		 * @private
		 */
		removeItem: function () {
			if (!this.classTabWidgetSelectedWidgetIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.classTabWidgetSelectedClassGet(CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.ID] = this.classTabWidgetSelectedWidgetGet(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.widget.Widget.remove({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.form.reset();

						this.cmfg('onClassTabWidgetPanelShow');
					}
				});
			}
		},

		// SelectedClass property functions
		// TODO: this functionality should be inside main class controller
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			classTabWidgetSelectedClassGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedClass';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Object} parameters
			 */
			classTabWidgetSelectedClassSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.classes.Class';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedClass';

					this.propertyManageSet(parameters);
				}
			},

		// SelectedWidget property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			classTabWidgetSelectedWidgetGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWidget';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			classTabWidgetSelectedWidgetIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWidget';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			classTabWidgetSelectedWidgetReset: function () {
				this.propertyManageReset('selectedWidget');
			},

			/**
			 * @param {Object} parameters
			 */
			classTabWidgetSelectedWidgetSet: function (parameters) {
				if (
					!Ext.Object.isEmpty(parameters)
					&& !Ext.isEmpty(this.controllerWidgetForm)
				) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = this.controllerWidgetForm.cmfg('classTabWidgetDefinitionModelNameGet');
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedWidget';

					this.propertyManageSet(parameters);
				}
			}
	});

})();
