(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.taskManager.common.WorkflowForm'
		],

		/**
		 * @property {CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo}
		 */
		comboField: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormGrid}
		 */
		gridField: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		gridEditorPlugin: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm}
		 */
		view: undefined,

		/**
		 * @property {Ext.data.Store}
		 */
		workflowAttributesStore: undefined,

		/**
		 * @param {CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm}
		 */
		constructor: function(view) {
			this.view = view;
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
				case 'onBeforeEdit':
					return this.onBeforeEdit(param.fieldName, param.rowData);

				case 'onSelectAttributeCombo':
					return this.onSelectAttributeCombo(param);

				case 'onSelectWorkflow':
					return this.onSelectWorkflow(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Build real editor with generated store
		 */
		buildWorkflowAtributesComboEditor: function() {
			if (!Ext.isEmpty(this.workflowAttributesStore)) {
				var me = this;

				this.gridField.columns[0].setEditor({
					xtype: 'combo',
					valueField: CMDBuild.core.constants.Proxy.VALUE,
					displayField: CMDBuild.core.constants.Proxy.VALUE,
					forceSelection: true,
					editable: false,
					allowBlank: false,

					store: me.workflowAttributesStore,
					queryMode: 'local',

					listeners: {
						select: function(combo, records, eOpts) {
							me.cmOn('onSelectAttributeCombo', me.gridField.store.indexOf(me.gridField.getSelectionModel().getSelection()[0]));
						}
					}
				});
			}
		},

		/**
		 * Workflow attribute store builder for onWorkflowSelected event
		 *
		 * @param {Object} attributes
		 *
		 * @return {Object} store
		 */
		buildWorkflowAttributesStore: function(attributes) {
			if (!Ext.isEmpty(attributes)) {
				var store = Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: [CMDBuild.core.constants.Proxy.VALUE],
					data: []
				});

				for (var key in attributes)
					store.add({ value: key });

				return store;
			}
		},

		/**
		 * @param {Object} attributes
		 *
		 * @return {Object} out
		 */
		cleanServerAttributes: function(attributes) {
			var out = {};

			for (var item in attributes)
				out[attributes[item].name] = '';

			return out;
		},

		/**
		 * An ExtJs fix to have a correct fields label and field width in FieldSet - 08/04/2014
		 */
		fieldWidthsFix: function() {
			if (!Ext.isEmpty(this.view))
				this.view.labelWidth = this.view.labelWidth - 10;

			if (!Ext.isEmpty(this.comboField))
				this.comboField.maxWidth = this.comboField.maxWidth - 10;

		},

		// GETters functions
			/**
			 * @return {String}
			 */
			getValueCombo: function() {
				return this.comboField.getValue();
			},

			/**
			 * @return {Object} data
			 *
			 * 	Example:
			 * 		{
			 * 			name1: value1,
			 * 			name2: value2
			 * 		}
			 */
			getValueGrid: function() {
				var data = {};

				// To validate and filter grid rows
				this.gridField.getStore().each(function(record) {
					if (
						!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.NAME))
						&& !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.VALUE))
					) {
						data[record.get(CMDBuild.core.constants.Proxy.NAME)] = record.get(CMDBuild.core.constants.Proxy.VALUE);
					}
				});

				return data;
			},

		/**
		 * @return {Boolean}
		 */
		isEmptyCombo: function() {
			return Ext.isEmpty(this.comboField.getValue());
		},

		/**
		 * Actions for addButtonClick event to erase workflowForm
		 */
		eraseWorkflowForm: function() {
			this.gridField.store.removeAll();
			this.setDisabledAttributesGrid(true);
		},

		/**
		 * Function to update rows stores/editors on beforeEdit event
		 *
		 * @param {String} fieldName
		 * @param {Object} rowData
		 */
		onBeforeEdit: function(fieldName, rowData) {
			switch (fieldName) {
				case CMDBuild.core.constants.Proxy.NAME: {
					if (!this.isEmptyCombo()) {
						this.buildWorkflowAtributesComboEditor();
					} else {
						this.setDisabledAttributesGrid(true);
					}
				} break;
			}
		},

		/**
		 * @param {Int} rowIndex
		 */
		onSelectAttributeCombo: function(rowIndex) {
			this.gridEditorPlugin.startEditByPosition({ row: rowIndex, column: 1 });
		},

		/**
		 * To build combo editors store
		 *
		 * @param {String} className
		 * @param {Boolean} erase
		 */
		onSelectWorkflow: function(erase) {
			var className = this.getValueCombo();

			if (!Ext.isEmpty(className)) {
				var me = this;

				if (Ext.isEmpty(erase))
					erase = false;

				CMDBuild.proxy.taskManager.common.WorkflowForm.readAllAttributes({
					params: {
						className: className
					},
					success: function(response) {
						var decodedResponse = Ext.JSON.decode(response.responseText);

						me.workflowAttributesStore = me.buildWorkflowAttributesStore(me.cleanServerAttributes(decodedResponse.attributes));

						if (erase) {
							me.gridField.store.removeAll();
							me.gridField.store.insert(0, Ext.create('CMDBuild.model.CMModelTasks.common.workflowForm'));
							me.gridEditorPlugin.startEditByPosition({ row: 0, column: 0 });
							me.setDisabledAttributesGrid(false);
						}
					}
				});
			}
		},

		// SETters functions
			/**
			 * Set combo as required/unrequired
			 *
			 * @param {Boolean} state
			 */
			setAllowBlankCombo: function(state) {
				this.comboField.allowBlank = state;
			},

			/**
			 * @param {Boolean} state
			 */
			setDisabledAttributesGrid: function(state) {
				this.gridField.setDisabled(state);
			},

			/**
			 * @param {String} value
			 */
			setValueCombo: function(value) {
				if (!Ext.isEmpty(value)) {
					this.comboField.setValue(value);
					this.onSelectWorkflow(false);
				}
			},

			/**
			 * Rewrite of loadData
			 *
			 * @param {Object} value
			 */
			setValueGrid: function(value) {
				var store = this.gridField.getStore();
				store.removeAll();

				if (!Ext.isEmpty(value)) {
					for (var key in value) {
						var recordConf = {};

						recordConf[CMDBuild.core.constants.Proxy.NAME] = key;
						recordConf[CMDBuild.core.constants.Proxy.VALUE] = value[key] || '';

						store.add(recordConf);
					}
				}
			},

		/**
		 * Workflow form validation
		 *
		 * @param {Boolean} enable
		 */
		validate: function(enable) {
			this.setAllowBlankCombo(
				!(this.isEmptyCombo() && enable)
			);
		}
	});

})();