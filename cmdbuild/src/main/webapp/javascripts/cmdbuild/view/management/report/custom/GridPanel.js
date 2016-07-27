(function() {

	Ext.define('CMDBuild.view.management.report.custom.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.report.Report'
		],

		/**
		 * @cfg {CMDBuild.controller.management.report.Custom}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			// Apply first store to use it in paging bar
			Ext.apply(this, {
				store: CMDBuild.proxy.report.Report.getStore('CUSTOM')
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Paging', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						store: this.getStore(),
						displayInfo: true,
						displayMsg: '{0} - {1} ' + CMDBuild.Translation.common.display_topic_of + ' {2}',
						emptyMsg: CMDBuild.Translation.common.display_topic_none
					})
				],
				columns: [
					{
						text: CMDBuild.Translation.name,
						dataIndex: CMDBuild.core.constants.Proxy.TITLE,
						flex: 1
					},
					{
						text: CMDBuild.Translation.descriptionLabel,
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						flex: 1
					},
					Ext.create('Ext.grid.column.Action', {
						text: CMDBuild.Translation.report,
						align: 'center',
						width: 120,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							Ext.create('CMDBuild.core.buttons.fileTypes.Pdf', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.pdf,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onReportCustomGenerateButtonClick', {
										record: record,
										type: CMDBuild.core.constants.Proxy.PDF
									});
								}
							}),
							Ext.create('CMDBuild.core.buttons.fileTypes.Odt', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.odt,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onReportCustomGenerateButtonClick', {
										record: record,
										type: CMDBuild.core.constants.Proxy.ODT
									});
								}
							}),
							Ext.create('CMDBuild.core.buttons.fileTypes.Rtf', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.rtf,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onReportCustomGenerateButtonClick', {
										record: record,
										type: CMDBuild.core.constants.Proxy.RTF
									});
								}
							}),
							Ext.create('CMDBuild.core.buttons.fileTypes.Csv', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.csv,
								scope: this,

								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmfg('onReportCustomGenerateButtonClick', {
										record: record,
										type: CMDBuild.core.constants.Proxy.CSV
									});
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			beforeselect: function(grid, record, index, eOpts) {
				return false; // Disable row selection
			}
		}
	});

})();