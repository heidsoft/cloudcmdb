(function() {

	Ext.define('CMDBuild.view.common.field.grid.KeyValue', {
		extend: 'Ext.grid.Panel',

		requres: ['CMDBuild.model.common.field.grid.KeyValue'],

		/**
		 * @cfg {CMDBuild.controller.common.field.grid.KeyValue}
		 */
		delegate: undefined,

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		actionColumns: [],

		/**
		 * @cfg {Array}
		 */
		additionalColumns: [],

		/**
		 * @cfg {Boolean}
		 */
		enableCellEditing: false,

		/**
		 * @cfg {Boolean}
		 */
		enableRowAdd: false,

		/**
		 * @cfg {Boolean}
		 */
		enableRowDelete: false,

		/**
		 * @cfg {String}
		 */
		keyAttributeName: CMDBuild.core.constants.Proxy.KEY,

		/**
		 * @cfg {Object}
		 */
		keyEditor: { xtype: 'textfield' },

		/**
		 * @cfg {String}
		 */
		keyLabel: CMDBuild.Translation.key,

		/**
		 * @cfg {String}
		 */
		modelName: 'CMDBuild.model.common.field.grid.KeyValue',

		/**
		 * @cfg {Object}
		 *
		 * @private
		 */
		pluginCellEditing: undefined,

		/**
		 * @cfg {String}
		 */
		valueAttributeName: CMDBuild.core.constants.Proxy.VALUE,

		/**
		 * @cfg {Object}
		 */
		valueEditor: { xtype: 'textfield' },

		/**
		 * @cfg {String}
		 */
		valueLabel: CMDBuild.Translation.value,

		considerAsFieldToDisable: true,
		flex: 1,
		frame: false,

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.controller.common.field.grid.KeyValue', { view: this });

			if (this.enableCellEditing)
				Ext.apply(this, {
					plugins: [
						this.pluginCellEditing = Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })
					]
				});

			if (this.enableRowAdd)
				Ext.apply(this, {
					dockedItems: [
						Ext.create('Ext.toolbar.Toolbar', {
							dock: 'top',
							itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
							items: [
								Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
									scope: this,

									handler: function(button, e) {
										this.delegate.cmfg('onFieldGridKeyValueAddButtonClick');
									}
								})
							]
						})
					]
				});

			Ext.apply(this, {
				columns: this.getColumns(),
				store: Ext.create('Ext.data.ArrayStore', {
					model: this.modelName,
					data: [],
					sorters: [
						{ property: this.keyAttributeName, direction: 'ASC' }
					]
				})
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Array}
		 */
		getColumns: function() {
			return this.delegate.cmfg('fieldGridKeyValueColumnsGet');
		},

		/**
		 * @param {Boolean} validatedData
		 *
		 * @returns {Object} data
		 */
		getData: function(validatedData) {
			validatedData = Ext.isBoolean(validatedData) ? validatedData : true;

			return this.delegate.cmfg('fieldGridKeyValueDataGet', validatedData);
		},

		/**
		 * @param {Object} data
		 */
		setData: function(data) {
			this.delegate.cmfg('fieldGridKeyValueDataSet', data);
		}
	});

})();