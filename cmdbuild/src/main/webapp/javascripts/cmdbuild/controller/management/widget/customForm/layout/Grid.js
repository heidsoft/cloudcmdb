(function () {

	Ext.define('CMDBuild.controller.management.widget.customForm.layout.Grid', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.management.widget.customForm.CustomForm}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetCustomFormLayoutGridAddRowButtonClick',
			'onWidgetCustomFormLayoutGridCloneRowButtonClick',
			'onWidgetCustomFormLayoutGridDeleteRowButtonClick' ,
			'onWidgetCustomFormLayoutGridEditRowButtonClick',
			'onWidgetCustomFormLayoutGridExportButtonClick',
			'onWidgetCustomFormLayoutGridImportButtonClick',
			'onWidgetCustomFormLayoutGridResetButtonClick',
			'onWidgetCustomFormLayoutGridShow = onWidgetCustomFormShow',
			'widgetCustomFormLayoutGridDataGet = widgetCustomFormLayoutDataGet',
			'widgetCustomFormLayoutGridDataSet = widgetCustomFormLayoutDataSet',
			'widgetCustomFormLayoutGridIsValid = widgetCustomFormLayoutIsValid'
		],

		/**
		 * @property {CMDBuild.view.management.widget.customForm.layout.GridPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.widget.customForm.CustomForm} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Barrier to load data after reference field store's load end
			var barrierId = 'widgetCustomFormLayoutGridBarrier-' + this.cmfg('widgetCustomFormIdGet'); // Unique widget barrier id
			var requestBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
				id: barrierId,
				scope: this,
				callback: function () {
					if (this.cmfg('instancesDataStorageExists'))
						this.cmfg('widgetCustomFormLayoutGridDataSet', this.cmfg('widgetCustomFormInstancesDataStorageGet'));

					this.cmfg('widgetCustomFormViewSetLoading', false);
				}
			});

			this.view = Ext.create('CMDBuild.view.management.widget.customForm.layout.GridPanel', {
				delegate: this,
				columns: this.buildColumns(requestBarrier, barrierId),
				store: this.buildDataStore()
			});

			this.cmfg('widgetCustomFormViewSetLoading', true);

			requestBarrier.finalize(barrierId, true);
		},

		/**
		 * @param {Object} header
		 * @param {Object} attribute
		 *
		 * @returns {String} value
		 *
		 * TODO: delete when old FieldManager will be replaced
		 *
		 * @private
		 */
		addRendererToHeader: function (header, attribute) {
			var me = this;

			if (Ext.isEmpty(header.renderer))
				header.renderer = function (value, metadata, record, rowIndex, colIndex, store, view) {
					value = value || record.get(header.dataIndex);

					if (!Ext.isEmpty(value)) {
						if (!Ext.isEmpty(header.editor.store)) {
							var comboRecord = header.editor.store.findRecord('Id', value);

							header.editor.setValue(value);

							return Ext.isEmpty(comboRecord) ? '' : comboRecord.get('Description');
						} else if (attribute.type == 'DATE') {
							return me.formatDate(value);
						}
					}

					if (Ext.isEmpty(Ext.String.trim(String(value))) && attribute['isnotnull'])
						metadata.tdCls += ' x-grid-invalid-cell-error';

					return null;
				}
		},

		/**
		 * @returns {Ext.grid.column.Action}
		 *
		 * @private
		 */
		buildActionColumns: function () {
			return Ext.create('Ext.grid.column.Action', {
				align: 'center',
				width: 75,
				sortable: false,
				hideable: false,
				menuDisabled: true,
				fixed: true,

				items: [
					Ext.create('CMDBuild.core.buttons.iconized.Clone', {
						withSpacer: true,
						tooltip: CMDBuild.Translation.cloneRow,
						scope: this,

						isDisabled: function (grid, rowIndex, colIndex, item, record) {
							return (
								this.cmfg('widgetCustomFormConfigurationGet', [
									CMDBuild.core.constants.Proxy.CAPABILITIES,
									CMDBuild.core.constants.Proxy.READ_ONLY
								])
								|| this.cmfg('widgetCustomFormConfigurationGet', [
									CMDBuild.core.constants.Proxy.CAPABILITIES,
									CMDBuild.core.constants.Proxy.CLONE_DISABLED
								])
							);
						},

						handler: function (view, rowIndex, colIndex, item, e, record) {
							this.cmfg('onWidgetCustomFormLayoutGridCloneRowButtonClick', {
								record: record,
								index: rowIndex
							});
						}
					}),
					Ext.create('CMDBuild.core.buttons.iconized.Modify', {
						withSpacer: true,
						tooltip: CMDBuild.Translation.editRow,
						scope: this,

						isDisabled: function (grid, rowIndex, colIndex, item, record) {
							return (
								this.cmfg('widgetCustomFormConfigurationGet', [
									CMDBuild.core.constants.Proxy.CAPABILITIES,
									CMDBuild.core.constants.Proxy.READ_ONLY
								])
								|| this.cmfg('widgetCustomFormConfigurationGet', [
									CMDBuild.core.constants.Proxy.CAPABILITIES,
									CMDBuild.core.constants.Proxy.MODIFY_DISABLED
								])
							);
						},

						handler: function (view, rowIndex, colIndex, item, e, record) {
							this.cmfg('onWidgetCustomFormLayoutGridEditRowButtonClick', record);
						}
					}),
					Ext.create('CMDBuild.core.buttons.iconized.Remove', {
						withSpacer: true,
						tooltip: CMDBuild.Translation.deleteRow,
						scope: this,

						isDisabled: function (grid, rowIndex, colIndex, item, record) {
							return (
								this.cmfg('widgetCustomFormConfigurationGet', [
									CMDBuild.core.constants.Proxy.CAPABILITIES,
									CMDBuild.core.constants.Proxy.READ_ONLY
								])
								|| this.cmfg('widgetCustomFormConfigurationGet', [
									CMDBuild.core.constants.Proxy.CAPABILITIES,
									CMDBuild.core.constants.Proxy.DELETE_DISABLED
								])
							);
						},

						handler: function (view, rowIndex, colIndex, item, e, record) {
							this.cmfg('onWidgetCustomFormLayoutGridDeleteRowButtonClick', rowIndex);
						}
					})
				]
			});
		},

		/**
		 * @param {CMDBuild.core.RequestBarrier} requestBarrier
		 * @param {String} barrierId
		 *
		 * @returns {Array} columns definitions
		 *
		 * TODO: this implementation should be refactored with FieldManager class
		 *
		 * @private
		 */
		buildColumns: function (requestBarrier, barrierId) {
			var columns = [];

			if (!this.cmfg('widgetCustomFormConfigurationIsEmpty',  CMDBuild.core.constants.Proxy.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function (attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.constants.Proxy.TYPE))) {
						fieldManager.attributeModelSet(Ext.create('CMDBuild.model.common.attributes.Attribute', attribute.getData()));
						fieldManager.push(columns, fieldManager.buildColumn(true));
					} else if (attribute.get(CMDBuild.core.constants.Proxy.TYPE) != 'ipaddress') { // TODO: future implementation - @deprecated - Old field manager
						var attribute = attribute.getAdaptedData();
						var attributesMap = CMDBuild.Management.FieldManager.getAttributesMap();

						// TODO: hack to bypass CMDBuild.Management.FieldManager.getFieldForAttr() control to check if return DisplayField
						// (correct way "var editor = CMDBuild.Management.FieldManager.getCellEditorForAttribute(attribute);")
						var editor = attributesMap[attribute.type].buildField(attribute, false, false);

						var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);

						header.flex = 1; // Apply flex 1 by default to avoid unused empty space on grids

						if (attribute.type == 'REFERENCE') { // TODO: hack to force a templateResolver build for editor that haven't a form associated like other fields types
							var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, 'system.template.');
							xaVars['_SystemFieldFilter'] = attribute.filter;

							var templateResolver = new CMDBuild.Management.TemplateResolver({ // TODO: implementation of serverside template resolver
								clientForm: this.cmfg('widgetCustomFormControllerPropertyGet', 'clientForm'),
								xaVars: xaVars,
								serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
							});

							editor = CMDBuild.Management.ReferenceField.buildEditor(attribute, templateResolver);

							// Force execution of template resolver
							if (!Ext.isEmpty(editor) && Ext.isFunction(editor.resolveTemplate))
								editor.resolveTemplate();

							// Manage reference selection from window
							editor.on('cmdbuild-reference-selected', function (selectedRecord, field) {
								selectedRecord = Ext.isArray(selectedRecord) ? selectedValue[0] : selectedRecord;

								var record = this.view.getSelectionModel().getSelection()[0];
								record.set(field.getName(), selectedRecord.get('Id'));
							}, this);
						}

						if (!Ext.isEmpty(header)) {
							editor.hideLabel = true;

							// Read only attributes header/editor setup
							header[CMDBuild.core.constants.Proxy.DISABLED] = !attribute[CMDBuild.core.constants.Proxy.WRITABLE];
							header[CMDBuild.core.constants.Proxy.REQUIRED] = false;
							editor[CMDBuild.core.constants.Proxy.DISABLED] = !attribute[CMDBuild.core.constants.Proxy.WRITABLE];
							editor[CMDBuild.core.constants.Proxy.REQUIRED] = false;

							if (attribute[CMDBuild.core.constants.Proxy.MANDATORY] || attribute['isnotnull']) {
								header.header = '* ' + header.header; // TODO: header property is deprecated, should use "text" but FieldManager uses header so ...

								header[CMDBuild.core.constants.Proxy.REQUIRED] = true;
								editor[CMDBuild.core.constants.Proxy.REQUIRED] = true;
							}

							// Do not override renderer, add editor on checkbox columns and make it editable
							if (attribute[CMDBuild.core.constants.Proxy.TYPE] != 'BOOLEAN') {
								header.editor = editor;

								this.addRendererToHeader(header, attribute);
							}

							columns.push(header);
						}

						// Force editor fields store load (must be done because FieldManager field don't works properly)
						if (!Ext.isEmpty(editor) && !Ext.isEmpty(editor.store) && editor.store.count() == 0)
							editor.store.load({ callback: requestBarrier.getCallback(barrierId) });
					}
				}, this);
			}

			columns.push(this.buildActionColumns());

			return columns;
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 *
		 * @private
		 */
		buildDataStore: function () {
			var storeFields = [];

			if (!this.cmfg('widgetCustomFormConfigurationIsEmpty',  CMDBuild.core.constants.Proxy.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function (attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.constants.Proxy.TYPE))) {
						fieldManager.attributeModelSet(Ext.create('CMDBuild.model.common.attributes.Attribute', attribute.getData()));
						fieldManager.push(storeFields, fieldManager.buildStoreField());
					} else {
						storeFields.push({ name: attribute.get(CMDBuild.core.constants.Proxy.NAME), type: 'string' });
					}
				}, this);
			}

			return Ext.create('Ext.data.ArrayStore', {
				fields: storeFields,
				data: []
			});
		},

		/**
		 * Add empty row to grid store
		 */
		onWidgetCustomFormLayoutGridAddRowButtonClick: function () {
			this.view.getStore().insert(0, {});
		},

		/**
		 * @param {Object} parameters
		 * @param {Ext.data.Model} parameters.record
		 * @param {Number} parameters.index
		 */
		onWidgetCustomFormLayoutGridCloneRowButtonClick: function (parameters) {
			if (
				!Ext.isEmpty(parameters) && Ext.isObject(parameters)
				&& !Ext.isEmpty(parameters.index) && Ext.isNumber(parameters.index)
				&& !Ext.isEmpty(parameters.record) && Ext.isFunction(parameters.record.getData)
			) {
				this.view.getStore().insert(parameters.index, parameters.record.getData());
			}
		},

		/**
		 * @param {Number} rowIndex
		 */
		onWidgetCustomFormLayoutGridDeleteRowButtonClick: function (rowIndex) {
			this.view.getStore().removeAt(rowIndex);
		},

		/**
		 * @param {Ext.data.Model} record
		 */
		onWidgetCustomFormLayoutGridEditRowButtonClick: function (record) {
			Ext.create('CMDBuild.controller.management.widget.customForm.RowEdit', {
				parentDelegate: this,
				record: record
			});
		},

		/**
		 * Opens export configuration pop-up window
		 */
		onWidgetCustomFormLayoutGridExportButtonClick: function () {
			Ext.create('CMDBuild.controller.management.widget.customForm.Export', { parentDelegate: this });
		},

		/**
		 * Opens import configuration pop-up window
		 */
		onWidgetCustomFormLayoutGridImportButtonClick: function () {
			Ext.create('CMDBuild.controller.management.widget.customForm.Import', { parentDelegate: this });
		},

		/**
		 * Setup form items disabled state, disable topToolBar only if is readOnly
		 * Load grid data
		 */
		onWidgetCustomFormLayoutGridShow: function () {
			this.updateUiState();

			if (this.cmfg('instancesDataStorageExists'))
				this.cmfg('widgetCustomFormLayoutGridDataSet', this.cmfg('widgetCustomFormInstancesDataStorageGet'));

			// Fixes reference field renderer to avoid blank cell content render
			Ext.Function.createDelayed(function () {
				if (this.view.getView().isVisible())
					this.view.getView().refresh();
			}, 100, this)();
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		updateUiState: function () {
			var isWidgetReadOnly = this.cmfg('widgetCustomFormConfigurationGet', [
				CMDBuild.core.constants.Proxy.CAPABILITIES,
				CMDBuild.core.constants.Proxy.READ_ONLY
			]);

			// Setup toolbar buttons
			this.view.addButton.setDisabled(
				isWidgetReadOnly
				|| this.cmfg('widgetCustomFormConfigurationGet', [
					CMDBuild.core.constants.Proxy.CAPABILITIES,
					CMDBuild.core.constants.Proxy.ADD_DISABLED
				])
			);
			this.view.exportButton.setDisabled(
				isWidgetReadOnly
				|| this.cmfg('widgetCustomFormConfigurationGet', [
					CMDBuild.core.constants.Proxy.CAPABILITIES,
					CMDBuild.core.constants.Proxy.IMPORT_DISABLED
				])
			);
			this.view.importButton.setDisabled(
				isWidgetReadOnly
				|| this.cmfg('widgetCustomFormConfigurationGet', [
					CMDBuild.core.constants.Proxy.CAPABILITIES,
					CMDBuild.core.constants.Proxy.EXPORT_DISABLED
				])
			);
		},

		/**
		 * @returns {Array} storeRecordsData
		 */
		widgetCustomFormLayoutGridDataGet: function () {
			var storeRecordsData = [];

			Ext.Array.forEach(this.view.getStore().getRange(), function (record, i, allRecords) {
				if (!Ext.isEmpty(record) && Ext.isFunction(record.getData))
					storeRecordsData.push(record.getData());
			}, this);

			return storeRecordsData;
		},

		/**
		 * @param {Array} data
		 *
		 * @returns {Void}
		 */
		widgetCustomFormLayoutGridDataSet: function (data) {
			this.view.getStore().removeAll();

			if (!Ext.isEmpty(data))
				this.view.getStore().loadData(data);

			this.cmfg('widgetCustomFormLayoutGridIsValid');
		},

		/**
		 * Check required field value of grid store records
		 *
		 * @returns {Boolean}
		 */
		widgetCustomFormLayoutGridIsValid: function () {
			var returnValue = true;
			var requiredAttributes = [];

			// If widget is flagged as required must return at least 1 row
			returnValue = !(
				this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.REQUIRED)
				&& this.view.getStore().getCount() == 0
			);

			// Build required attributes names array
			Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function (attributeModel, i, allAttributeModels) {
				if (attributeModel.get(CMDBuild.core.constants.Proxy.MANDATORY))
					requiredAttributes.push(attributeModel.get(CMDBuild.core.constants.Proxy.NAME));
			}, this);

			// Check grid store records empty required fields
			this.view.getStore().each(function (record) {
				Ext.Array.forEach(requiredAttributes, function (attributeName, i, allAttributeNames) {
					if (Ext.isEmpty(record.get(attributeName))) {
						returnValue = false;

						return false;
					}
				}, this);
			}, this);

			return returnValue;
		}
	});

})();
