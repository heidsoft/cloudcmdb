(function() {

	Ext.define('CMDBuild.controller.management.dataView.Sql', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.dataView.Sql'
		],

		/**
		 * @cfg {CMDBuild.controller.management.dataView.DataView}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'dataViewSqlBuildColumns',
			'dataViewSqlBuildStore',
			'onButtonPrintClick',
			'onDataViewSqlGridSelect',
			'onDataViewSqlPanelShow = onDataViewPanelShow',
			'onDataViewSqlViewSelected = onDataViewViewSelected'
		],

		/**
		 * @property {CMDBuild.view.management.dataView.sql.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.dataView.sql.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.management.dataView.sql.SqlView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.dataView.DataView} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.dataView.sql.SqlView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;

			if (!Ext.isEmpty(_CMUIState))
				_CMUIState.addDelegate(this);
		},

		/**
		 * @returns {Array} columns
		 */
		dataViewSqlBuildColumns: function() {
			var columns = [];

			Ext.Array.forEach(this.cmfg('dataViewSelectedGet', CMDBuild.core.constants.Proxy.OUTPUT), function(columnObject, i, allColumnObjects) {
				columns.push({
					text: columnObject[CMDBuild.core.constants.Proxy.NAME],
					dataIndex: columnObject[CMDBuild.core.constants.Proxy.NAME],
					renderer: 'stripTags',
					flex: 1
				});
			}, this);

			return columns;
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		dataViewSqlBuildStore: function() {
			var extraParams = {};
			extraParams[CMDBuild.core.constants.Proxy.FUNCTION] = this.cmfg('dataViewSelectedGet', CMDBuild.core.constants.Proxy.SOURCE_FUNCTION);

			return CMDBuild.proxy.dataView.Sql.getStoreFromSql({
				fields: this.cmfg('dataViewSelectedGet', CMDBuild.core.constants.Proxy.OUTPUT),
				extraParams: extraParams
			});
		},

		/**
		 * @returns {Array} visibleColumns
		 */
		getVisibleColumns: function() {
			var visibleColumns = [];

			Ext.Array.forEach(this.grid.columns, function(column, i, allColumns) {
				if (!column.isHidden() && !Ext.isEmpty(column.dataIndex))
					visibleColumns.push(column.dataIndex);
			}, this);

			return visibleColumns;
		},

		/**
		 * @param {String} format
		 */
		onButtonPrintClick: function(format) {
			if (!Ext.isEmpty(format)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(this.getVisibleColumns());
				params[CMDBuild.core.constants.Proxy.FUNCTION] = this.cmfg('dataViewSelectedGet', CMDBuild.core.constants.Proxy.SOURCE_FUNCTION);
				params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(this.grid.getStore().getSorters());
				params[CMDBuild.core.constants.Proxy.TYPE] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					format: format,
					mode: 'dataViewSql',
					parameters: params
				});
			}
		},

		onDataViewSqlGridSelect: function() {
			var record = this.grid.getSelectionModel().getSelection()[0];

			this.form.cardPanel.removeAll();

			record.fields.each(function(field) {
				var name = field.name;

				if (!Ext.Array.contains([CMDBuild.core.constants.Proxy.ID], name)) { // Filters id attribute
					var value = record.get(name);

					if (
						!Ext.isEmpty(value)
						&& Ext.isObject(value)
						&& Ext.isFunction(value.toString)
					) {
						value = value.toString();
					}

					this.form.cardPanel.add(
						Ext.create('CMDBuild.view.common.field.CMDisplayField', {
							disabled: false,
							fieldLabel: field.name,
							labelAlign: 'right',
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							name: field.name,
							submitValue: false,
							style: {
								overflow: 'hidden'
							},
							value: value,
							maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG
						})
					);
				}
			}, this);
		},

		onDataViewSqlPanelShow: function() {
			this.view.grid.getStore().on('load', function() {
				if (!this.grid.getSelectionModel().hasSelection())
					this.grid.getSelectionModel().select(0, true);
			}, this);
		},

		// _CMUIState methods
			onFullScreenChangeToFormOnly: function() {
				Ext.suspendLayouts();

				this.grid.hide();
				this.grid.region = '';

				this.form.show();
				this.form.region = 'center';

				Ext.resumeLayouts(true);
			},

			onFullScreenChangeToGridOnly: function() {
				Ext.suspendLayouts();

				this.form.hide();
				this.form.region = '';

				this.grid.show();
				this.grid.region = 'center';

				Ext.resumeLayouts(true);
			},

			onFullScreenChangeToOff: function() {
				Ext.suspendLayouts();
				this.form.show();
				this.form.region = 'south';

				this.grid.show();
				this.grid.region = 'center';

				Ext.resumeLayouts(true);
			}
	});

})();