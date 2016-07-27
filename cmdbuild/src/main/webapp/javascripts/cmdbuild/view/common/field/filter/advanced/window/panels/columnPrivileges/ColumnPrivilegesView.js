(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.ColumnPrivilegesView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.panels.ColumnPrivileges}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.GridPanel}
		 */
		grid: undefined,

		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.columnsPrivileges,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.grid = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.GridPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();