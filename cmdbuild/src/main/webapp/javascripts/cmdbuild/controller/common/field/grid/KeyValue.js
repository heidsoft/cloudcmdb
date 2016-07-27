(function() {

	Ext.define('CMDBuild.controller.common.field.grid.KeyValue', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldGridKeyValueColumnsGet',
			'fieldGridKeyValueDataGet',
			'fieldGridKeyValueDataSet',
			'onFieldGridKeyValueAddButtonClick',
			'onFieldGridKeyValueDeleteRowButtonClick'
		],

		/**
		 * @property {CMDBuild.view.common.field.grid.KeyValue}
		 */
		view: undefined,

		/**
		 * @return {Array}
		 */
		buildActionColumn: function() {
			var actionButtons = [];

			if (this.view.enableRowDelete)
				actionButtons.push(
					Ext.create('CMDBuild.core.buttons.iconized.Remove', {
						withSpacer: true,
						tooltip: CMDBuild.Translation.deleteRow,
						scope: this,

						handler: function(view, rowIndex, colIndex, item, e, record) {
							this.cmfg('onFieldGridKeyValueDeleteRowButtonClick', rowIndex);
						}
					})
				);

			return [
				Ext.create('Ext.grid.column.Action', {
					align: 'center',
					width: 30,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,

					items: actionButtons
				})
			];
		},

		/**
		 * @returns {Array} columnDefinitions
		 */
		fieldGridKeyValueColumnsGet: function() {
			var columnDefinitions = [
				{
					dataIndex: this.view.keyAttributeName,
					text: this.view.keyLabel,
					editor: this.view.enableCellEditing ? this.view.keyEditor : {},
					flex: 1
				},
				{
					dataIndex: this.view.valueAttributeName,
					text: this.view.valueLabel,
					editor: this.view.enableCellEditing ? this.view.valueEditor : {},
					flex: 1
				}
			];

			// Add additionalColumns to generated column definition array
			if (!Ext.isEmpty(this.view.additionalColumns) && Ext.isArray(this.view.additionalColumns))
				columnDefinitions = Ext.Array.push(columnDefinitions, this.view.additionalColumns);

			// Add action columns to generated column definition array
			if (this.view.enableRowDelete)
				columnDefinitions = Ext.Array.push(columnDefinitions, this.buildActionColumn());

			return columnDefinitions;
		},

		/**
		 * If validatedData is true returns all validated row (non empty key and value values) otherwise just keys are required
		 *
		 * @param {Boolean} validatedData
		 *
		 * @returns {Object}
		 */
		fieldGridKeyValueDataGet: function(validatedData) {
			var data = {};

			Ext.Array.forEach(this.view.getStore().getRange(), function(record, i, allRecords) {
				if (validatedData) {
					if (
						!Ext.isEmpty(record.get(this.view.keyAttributeName))
						&& !Ext.isEmpty(record.get(this.view.valueAttributeName))
					) {
						data[record.get(this.view.keyAttributeName)] = record.get(this.view.valueAttributeName);
					}
				} else {
					if (!Ext.isEmpty(record.get(this.view.keyAttributeName)))
						data[record.get(this.view.keyAttributeName)] = record.get(this.view.valueAttributeName);
				}
			}, this);

			return data;
		},

		/**
		 * Decodes data object, build records models and adds them in store
		 *
		 * @param {Object} data
		 */
		fieldGridKeyValueDataSet: function(data) {
			if (!Ext.Object.isEmpty(data)) {
				var formattedDataObject = [];

				this.view.getStore().removeAll();

				Ext.Object.each(data, function(key, value, myself) {
					// Remove already existing rows
					var storeReportIndex = this.view.getStore().find(this.view.keyAttributeName, key);

					if (storeReportIndex >= 0)
						this.view.getStore().removeAt(storeReportIndex);

					// Add data objects to store
					var modelObject = {};
					modelObject[this.view.keyAttributeName] = key;
					modelObject[this.view.valueAttributeName] = value;

					formattedDataObject.push(Ext.create(this.view.modelName, modelObject));
				}, this);

				this.view.getStore().loadRecords(formattedDataObject, { addRecords: true });
			}
		},

		onFieldGridKeyValueAddButtonClick: function() {
			this.view.getStore().insert(0, Ext.create(this.view.modelName));

			if (this.view.enableCellEditing)
				this.view.pluginCellEditing.startEditByPosition({
					row: 0,
					column: 0
				});
		},

		/**
		 * @param {Number} rowIndex
		 */
		onFieldGridKeyValueDeleteRowButtonClick: function(rowIndex) {
			this.view.getStore().removeAt(rowIndex);
		}
	});

})();