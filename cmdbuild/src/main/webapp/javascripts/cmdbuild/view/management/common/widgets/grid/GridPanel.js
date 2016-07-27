(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.GridPanel', {
		extend: 'Ext.grid.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.Grid}
		 */
		delegate: undefined,

		/**
		 * @cfg {Array}
		 */
		columns: [],

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		gridEditorPlugin: undefined,

		border: false,

		initComponent: function() {
			this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit: 1
			});

			Ext.apply(this, {
				plugins: [this.gridEditorPlugin]
			});

			this.callParent(arguments);
		}
	});

})();