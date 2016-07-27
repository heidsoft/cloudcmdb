(function () {

	Ext.define('CMDBuild.view.administration.tasks.common.reportForm.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.taskManager.common.reportForm.Grid'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.reportForm.ReportForm}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		cellEditingPlugin: undefined,

		considerAsFieldToDisable: true,
		disabled: true,
		margin: '0 0 0 ' + (CMDBuild.core.constants.FieldWidths.LABEL - 5),
		maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
		title: CMDBuild.Translation.parameters,

		/**
		 * @returns {View}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					},
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: CMDBuild.core.constants.Proxy.EDITING_MODE,
						text: CMDBuild.Translation.cqlExpression,
						width: 100,
						align: 'center',
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						listeners: {
							scope: this,
							checkchange: function (column, rowIndex, checked, eOpts) {
								this.delegate.cmfg('onTaskManagerReportFormEditingModeCheckChange', rowIndex);
							}
						}
					}),
					{
						dataIndex: CMDBuild.core.constants.Proxy.VALUE,
						text: CMDBuild.Translation.value,
						flex: 1,

						editor: { xtype: 'textfield' }
					}
				],
				plugins: [
					this.cellEditingPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
						clicksToEdit: 1,

						listeners: {
							scope: this,
							beforeedit: function (editor, e, eOpts) {
								this.delegate.cmfg('onTaskManagerReportFormBeforeEdit', {
									column: e.column,
									columnName: e.field,
									record: e.record
								});
							}
						}
					})
				],
				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.taskManager.common.reportForm.Grid',
					data: []
				})
			});

			this.callParent(arguments);
		}
	});

})();
