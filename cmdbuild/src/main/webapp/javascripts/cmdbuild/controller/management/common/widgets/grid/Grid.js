(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.grid.Grid', {
		extend: 'CMDBuild.controller.common.abstract.Widget',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.widget.grid.Grid',
			'CMDBuild.model.widget.grid.Grid'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Array}
		 */
		cardAttributes: undefined,

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		classType: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'getCardAttributes',
			'onAddRowButtonClick' ,
			'onBeforeSave',
			'onCSVImportButtonClick',
			'onDeleteRowButtonClick',
			'onEditRowButtonClick',
			'onWidgetGridBeforeActiveView = beforeActiveView',
			'onWidgetGridBeforeHideView = beforeHideView',
			'onWidgetGridEditMode = onEditMode',
			'setGridDataFromCsv',
			'widgetGridGetData = getData',
			'widgetGridIsValid = isValid'
		],

		/**
		 * Grid column configuration variable
		 *
		 * @proeprty {Array}
		 */
		columns: [],

		/**
		 * @cfg {Boolean}
		 *
		 * @override
		 */
		enableWidgetConfigurationSetup: false,

		/**
		 * Array of attributes names to hide from grid visualization
		 *
		 * @cfg {Array}
		 */
		filteredAttributes: ['Notes'],

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.GridPanel}
		 */
		grid: undefined,

		/**
		 * Widget instances data storage
		 *
		 * @cfg {Object}
		 *
		 * @private
		 */
		instancesDataStorage: {},

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.GridView}
		 */
		view: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: 'CMDBuild.model.widget.grid.Configuration',

		/**
		 * @param {CMDBuild.view.management.common.widgets.grid.GridView} configurationObject.view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} configurationObject.parentDelegate
		 * @param {Object} configurationObject.widgetConfiguration
		 * @param {Ext.form.Basic} configurationObject.clientForm
		 * @param {CMDBuild.model.CMActivityInstance} configurationObject.card
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.widgetConf = this.widgetConfiguration; // FIXME: Alias for compatibility mode with new AbstractBaseWidgetController

			this.classType = _CMCache.getEntryTypeByName(this.widgetConf[CMDBuild.core.constants.Proxy.CLASS_NAME]);

			this.grid = Ext.create('CMDBuild.view.management.common.widgets.grid.GridPanel', {
				delegate: this
			});

			if (!Ext.isEmpty(this.grid))
				this.view.add(this.grid);

			this.configureGridPanel();
		},

		/**
		 * @param {Object} header
		 * @param {Object} attribute
		 *
		 * @return {String} value
		 */
		addRendererToHeader: function(header, attribute) {
			var me = this;

			if (Ext.isEmpty(header.renderer))
				header.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
					value = value || record.get(header.dataIndex);

					if (!Ext.isEmpty(value)) {
						if (!Ext.isEmpty(header.editor.store)) {
							var comboRecord = header.editor.store.findRecord('Id', value);

							value = !Ext.isEmpty(comboRecord) ? comboRecord.get('Description') : '';
						} else if (attribute.type == 'DATE') {
							value = me.formatDate(value);
						}

						if (Ext.isEmpty(Ext.String.trim(String(value))) && attribute['isnotnull'])
							value = '<div style="width: 100%; height: 100%; border: 1px dotted red;">';

						return value;
					}

					return null;
				}
		},

		/**
		 * @return {Array}
		 */
		buildActionColumns: function() {
			return [
				Ext.create('Ext.grid.column.Action', {
					align: 'center',
					width: 50,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,

					items: [
						Ext.create('CMDBuild.core.buttons.iconized.Modify', {
							withSpacer: true,
							tooltip: CMDBuild.Translation.editRow,
							scope: this,

							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return this.widgetConf[CMDBuild.core.constants.Proxy.READ_ONLY];
							},

							handler: function(grid, rowIndex, colIndex) {
								var record = grid.getStore().getAt(rowIndex);

								this.cmfg('onEditRowButtonClick', record);
							}
						}),
						Ext.create('CMDBuild.core.buttons.iconized.Remove', {
							withSpacer: true,
							tooltip: CMDBuild.Translation.deleteRow,
							scope: this,

							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return (
									this.widgetConf[CMDBuild.core.constants.Proxy.READ_ONLY]
									|| this.widgetConf[CMDBuild.core.constants.Proxy.DISABLE_DELETE_ROW]
								);
							},

							handler: function(grid, rowIndex, colIndex) {
								this.cmfg('onDeleteRowButtonClick', rowIndex);
							}
						})
					]
				})
			];
		},

		/**
		 * Builds columns for grid with cell editors.
		 * Builds attributesToTranslate array where stores all attributes with needs translations from Id to Description.
		 *
		 * @return {Object}
		 */
		buildColumnsForAttributes: function() {
			var columns = [];
			var classId = this.classType.get(CMDBuild.core.constants.Proxy.ID);

			if (_CMUtils.isSuperclass(classId))
				columns.push(this.buildClassColumn());

			Ext.Array.forEach(this.getCardAttributes(), function(attribute, i, allAttributes) {
				if (!Ext.Array.contains(this.filteredAttributes, attribute[CMDBuild.core.constants.Proxy.NAME])) { // Attributes filter
					var attributesMap = CMDBuild.Management.FieldManager.getAttributesMap();

					// TODO: hack to bypass CMDBuild.Management.FieldManager.getFieldForAttr() control to check if return DisplayField
					// (correct way "var editor = CMDBuild.Management.FieldManager.getCellEditorForAttribute(attribute);")
					var editor = attributesMap[attribute.type].buildField(attribute, false, false);

					var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);

					if (attribute.type == 'REFERENCE') { // TODO: hack to force a templateResolver build for editor that haven't a form associated like other fields types
						var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, 'system.template.');
						xaVars['_SystemFieldFilter'] = attribute.filter;

						var templateResolver = new CMDBuild.Management.TemplateResolver({
							clientForm: this.clientForm,
							xaVars: xaVars,
							serverVars: this.getTemplateResolverServerVars()
						});

						editor = CMDBuild.Management.ReferenceField.buildEditor(attribute, templateResolver);

						// Avoids to resolve field templates when form is in editMode (when you click on abort button)
						if (!this.clientForm.owner._isInEditMode && !Ext.Object.isEmpty(editor) && Ext.isFunction(editor.resolveTemplate))
							editor.resolveTemplate();
					}

					editor.hideLabel = true;

					if (!Ext.isEmpty(header)) {
						editor.disabled = attribute[CMDBuild.core.constants.Proxy.FIELD_MODE] == CMDBuild.core.constants.Proxy.READ;

						if (attribute['isnotnull']) {
							header.header = '* ' + header.header; // TODO: header property is deprecated, should use "text" but FieldManager uses header so ...

							header[CMDBuild.core.constants.Proxy.REQUIRED] = true;
							editor[CMDBuild.core.constants.Proxy.REQUIRED] = true;
						} else {
							header[CMDBuild.core.constants.Proxy.REQUIRED] = false;
							editor[CMDBuild.core.constants.Proxy.REQUIRED] = false;
						}

						// Do not override renderer, add editor on checkbox columns and make it editable
						if (attribute[CMDBuild.core.constants.Proxy.TYPE] != 'BOOLEAN') {
							header.editor = editor;

							this.addRendererToHeader(header, attribute);
						} else {
							header.cmReadOnly = false;
						}

						// Read only attributes header setup
						header.disabled = attribute[CMDBuild.core.constants.Proxy.FIELD_MODE] == CMDBuild.core.constants.Proxy.READ;

						columns.push(header);
					}

					// Force editor fields store load (must be done because FieldManager field don't works properly)
					if (!Ext.isEmpty(editor) && !Ext.isEmpty(editor.store) && editor.store.count() == 0)
						editor.store.load();
				}
			}, this);

			return columns;
		},

		/**
		 * Configure grid and fill store with data from source
		 */
		configureGridPanel: function() {
			var me = this;

			if (!Ext.isEmpty(this.grid))
				this.grid.getStore().removeAll();

			if (!Ext.isEmpty(this.instancesDataStorage[this.getWidgetId()])) {
				this.grid.getStore().loadRecords(this.instancesDataStorage[this.getWidgetId()]);

				// Fixes reference field renderer to avoid blank cell content render
				Ext.Function.createDelayed(function() {
					if (this.grid.getView().isVisible())
						this.grid.getView().refresh();
				}, 100, this)();
			} else if (!Ext.isEmpty(this.classType)) {
				CMDBuild.Management.FieldManager.loadAttributes(
					this.classType.get(CMDBuild.core.constants.Proxy.ID),
					function(attributes) {
						me.cardAttributes = attributes;
						me.setColumnsForClass();
						me.loadPresets();
					}
				);
			} else {
				_warning('classType error with className ' + this.widgetConf[CMDBuild.core.constants.Proxy.CLASS_NAME], this);
			}
		},

		/**
		 * To decode "function" presetsType and fill grid store
		 *
		 * @param {String} presetsString
		 */
		decodeFunctionPresets: function(presetsString) {
			// Validate presetsString
			CMDBuild.proxy.widget.grid.Grid.getFunctions({
				scope: this,
				success: function(result, options, decodedResult) {
					var isPresetsStringValid = false;

					Ext.Array.each(decodedResult.response, function(record) {
						if (record[CMDBuild.core.constants.Proxy.NAME] == presetsString)
							isPresetsStringValid = true;
					});

					if (isPresetsStringValid) {
						var functionParamsNames = [];
						var params = {};
						var widgetUnmanagedVariables = this.widgetConf[CMDBuild.core.constants.Proxy.VARIABLES];

						// Instantiate model to transform attributes in fields
						Ext.create('CMDBuild.model.widget.grid.Grid', this.cardAttributes);

						// Resolve templates for widget configuration "function" type
						new CMDBuild.Management.TemplateResolver({
							clientForm: this.clientForm,
							xaVars: widgetUnmanagedVariables,
							serverVars: this.getTemplateResolverServerVars()
						}).resolveTemplates({
							attributes: Ext.Object.getKeys(widgetUnmanagedVariables),
							callback: function(out, ctx) {
								widgetUnmanagedVariables = out;
							}
						});

						// Builds functionParams with all param names
						for (var index in _CMCache.getDataSourceInput(presetsString)) {
							var functionParamDefinitionObject = _CMCache.getDataSourceInput(presetsString)[index];

							functionParamsNames.push(functionParamDefinitionObject[CMDBuild.core.constants.Proxy.NAME]);
						}

						var functionParams = Ext.Array.intersect(functionParamsNames, Object.keys(widgetUnmanagedVariables));

						for (var index in functionParams)
							params[functionParams[index]] = widgetUnmanagedVariables[functionParams[index]];

						this.grid.reconfigure(
							CMDBuild.proxy.widget.grid.Grid.getStoreFromFunction({
								fields: CMDBuild.model.widget.grid.Grid.getFields(),
								extraParams: {
									'function': presetsString,
									params: Ext.encode(params)
								}
							})
						);
					} else {
						CMDBuild.core.Message.error(
							CMDBuild.Translation.error,
							'GridController decodeFunctionPresets: SQL function not found',
							true
						);
					}
				}
			});
		},

		/**
		 * To decode "text" presetsType strings, uses widget separators
		 *
		 * @param {String} presetsString
		 *
		 * @return {Array} decodedArray
		 */
		decodeTextPresets: function(presetsString) {
			var cardsArray = presetsString.split(this.widgetConf[CMDBuild.core.constants.Proxy.CARD_SEPARATOR]);
			var decodedArray = [];

			// Decode cards
			Ext.Array.forEach(cardsArray, function(card, i, allCards) {
				if (!Ext.isEmpty(card)) {
					var buffer = {};
					var cardAttributes = card.split(this.widgetConf[CMDBuild.core.constants.Proxy.ATTRIBUTE_SEPARATOR]);

					Ext.Array.forEach(cardAttributes, function(attribute, i, allAttributes) {
						var keyValueArray = attribute.split(this.widgetConf[CMDBuild.core.constants.Proxy.KEY_VALUE_SEPARATOR]);

						if (!Ext.isEmpty(keyValueArray[0]))
							buffer[keyValueArray[0]] = keyValueArray[1];
					}, this);

					decodedArray.push(buffer);
				}
			}, this);

			return decodedArray;
		},

		/**
		 * @param {Mixed} value
		 *
		 * @return {String}
		 */
		formatDate: function(value) {
			if(Ext.isDate(value))
				return Ext.Date.format(value, 'd/m/Y');

			return Ext.isEmpty(value) ? '' : value;
		},

		/**
		 * @return {Array} cardAttributes
		 */
		getCardAttributes: function() {
			return this.cardAttributes;
		},

		/**
		 * @return {Number}
		 */
		getWidgetId: function() {
			return this.widgetConfiguration[CMDBuild.core.constants.Proxy.ID];
		},

		/**
		 * Read presets and loads data to grid store
		 */
		loadPresets: function() {
			if (!Ext.isEmpty(this.widgetConf[CMDBuild.core.constants.Proxy.PRESETS])) {
				switch (this.widgetConf[CMDBuild.core.constants.Proxy.PRESETS_TYPE]) {
					case 'text':
						return this.setGridDataFromTextPresets(
							this.decodeTextPresets(
								this.widgetConf[CMDBuild.core.constants.Proxy.PRESETS]
							)
						);

					case 'function':
						return this.decodeFunctionPresets(
							this.widgetConf[CMDBuild.core.constants.Proxy.PRESETS]
						);

					default:
						CMDBuild.core.Message.error(
							CMDBuild.Translation.error,
							'GridController: wrong serializationType (' + this.widgetConf[CMDBuild.core.constants.Proxy.SERIALIZATION_TYPE] + ') format or value',
							true
						);
				}
			}
		},

		/**
		 * Add empty row to grid store
		 */
		onAddRowButtonClick: function() {
			this.grid.getStore().insert(0, Ext.create('CMDBuild.model.widget.grid.Grid', this.cardAttributes));
		},

		/**
		 * Opens importCSV configuration pop-up window
		 */
		onCSVImportButtonClick: function() {
			Ext.create('CMDBuild.controller.management.common.widgets.grid.ImportCSV', {
				parentDelegate: this,
				classId: this.classType.get(CMDBuild.core.constants.Proxy.ID)
			});
		},

		/**
		 * @param {Int} rowIndex
		 */
		onDeleteRowButtonClick: function(rowIndex) {
			this.grid.getStore().removeAt(rowIndex);
		},

		/**
		 * Edit row data in new pop-up window
		 *
		 * @param {Object} record
		 */
		onEditRowButtonClick: function(record) {
			Ext.create('CMDBuild.controller.management.common.widgets.grid.RowEdit', {
				parentDelegate: this,
				record: record
			});
		},

		/**
		 * @override
		 */
		onWidgetGridBeforeActiveView: function() {
			this.beforeActiveView(arguments); // CallParent alias

			// Disable add button
			this.view.addButton.setDisabled(
				this.widgetConf.hasOwnProperty(CMDBuild.core.constants.Proxy.DISABLE_ADD_ROW)
				&& this.widgetConf[CMDBuild.core.constants.Proxy.DISABLE_ADD_ROW]
			);

			// Disable import from CSV button
			this.view.importFromCSVButton.setDisabled(
				this.widgetConf.hasOwnProperty(CMDBuild.core.constants.Proxy.DISABLE_IMPORT_FROM_CSV)
				&& this.widgetConf[CMDBuild.core.constants.Proxy.DISABLE_IMPORT_FROM_CSV]
			);

			// Disable buttons for readOnly mode
			if (
				this.widgetConf.hasOwnProperty(CMDBuild.core.constants.Proxy.READ_ONLY)
				&& this.widgetConf[CMDBuild.core.constants.Proxy.READ_ONLY]
			) {
				this.view.addButton.setDisabled(true);
				this.view.importFromCSVButton.setDisabled(true);

				this.grid.on('beforeedit', function(plugin, edit) {
					return false;
				});
			}

			this.configureGridPanel();
		},

		/**
		 * Save data in storage attribute
		 *
		 * @override
		 */
		onWidgetGridBeforeHideView: function() {
			this.instancesDataStorage[this.getWidgetId()] = this.grid.getStore().getRange();

			this.beforeHideView(arguments); // CallParent alias
		},

		/**
		 * Reset instance storage property
		 */
		onWidgetGridEditMode: function() {
			this.instancesDataStorage = {};
		},

		/**
		 * Build columns for class in view's grid
		 */
		setColumnsForClass: function() {
			this.columns = Ext.Array.push(this.buildColumnsForAttributes(), this.buildActionColumns());

			this.grid.reconfigure(
				this.grid.getStore(),
				this.columns
			);
		},

		/**
		 * Adapter for grid's loarRecords function
		 *
		 * @param {Object} parameters
		 * @param {Array} parameters.rawData - Ex. [{ card: {...}, not_valid_fields: {...} }, {...}]
		 * @param {String} parameters.mode
		 */
		setGridDataFromCsv: function(parameters) {
			if (
				!Ext.isEmpty(parameters)
				&& !Ext.isEmpty(parameters.rawData)
			) {
				// To clear all grid data if mode = 'replace'
				if (!Ext.isEmpty(parameters.mode) && parameters.mode == 'replace')
					this.grid.getStore().removeAll();

				Ext.Array.forEach(parameters.rawData, function(rowData, i, allRowsData) {
					var cardData = rowData[CMDBuild.core.constants.Proxy.CARD];

					// Resolve objects returned for reference fields, just rewrite with object's id
					for (var item in cardData)
						if (typeof cardData[item] == 'object' && !Ext.isEmpty(cardData[item][CMDBuild.core.constants.Proxy.ID]))
							cardData[item] = cardData[item][CMDBuild.core.constants.Proxy.ID];

					this.grid.getStore().add(Ext.create('CMDBuild.DummyModel', cardData));
				}, this);
			}
		},

		/**
		 * @param {Array} data
		 */
		setGridDataFromTextPresets: function(data) {
			this.grid.getStore().removeAll();

			Ext.Array.forEach(data, function(recordObject, i, allRecordsObjects) {
				this.grid.getStore().add(Ext.create('CMDBuild.DummyModel', recordObject));
			}, this);
		},

		/**
		 * @return {Object} out
		 *
		 * @override
		 */
		widgetGridGetData: function() {
			var me = this;
			var out = {};
			var data = [];

			this.grid.getStore().each(function(record) {
				var xaVars = record.data;

				// Resolve templates for widget configuration "text" type
				new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: xaVars,
					serverVars: this.getTemplateResolverServerVars()
				}).resolveTemplates({
					attributes: Ext.Object.getKeys(xaVars),
					callback: function(out, ctx) {
						// Date field format fix: date field gives wrong formatted value used as cell editor.
						// To delete when FieldManager will be refactored
						Ext.Object.each(out, function(key, value, object) {
							out[key] = me.formatDate(value);
						});

						data.push(
							Ext.encode(
								Ext.Object.merge(record.data, out)
							)
						);
					}
				});
			}, this);

			if (!this.readOnly)
				out[CMDBuild.core.constants.Proxy.OUTPUT] = data;

			return out;
		},

		/**
		 * Check required field value of grid store records
		 *
		 * @return {Boolean}
		 */
		widgetGridIsValid: function() {
			var returnValue = true;
			var requiredAttributes = [];

			// If widget is flagged as required must return at least 1 row
			if (
				Ext.isBoolean(this.widgetConf[CMDBuild.core.constants.Proxy.REQUIRED])
				&& this.widgetConf[CMDBuild.core.constants.Proxy.REQUIRED]
				&& this.grid.getStore().getCount() == 0
			) {
				returnValue = false;
			}

			// Build columns required array
			Ext.Array.forEach(this.columns, function(column, i, allColumns) {
				if (column[CMDBuild.core.constants.Proxy.REQUIRED])
					requiredAttributes.push(column[CMDBuild.core.constants.Proxy.DATA_INDEX]);
			}, this);

			// Check grid store records empty required fields
			this.grid.getStore().each(function(record) {
				for (var y in requiredAttributes)
					if (Ext.isEmpty(record.get(requiredAttributes[y]))) {
						returnValue = false;

						return false;
					}

				return true;
			}, this);

			return returnValue;
		}
	});

})();