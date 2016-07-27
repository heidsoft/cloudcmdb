(function() {

	Ext.define('CMDBuild.view.administration.dataView.filter.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.dataView.Filter'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.dataView.Filter}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		frame: false,
		split: true,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.SOURCE_CLASS_NAME,
						text: CMDBuild.Translation.targetClass,
						flex: 1
					}
				],
				store: CMDBuild.proxy.dataView.Filter.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onDataViewFilterItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onDataViewFilterRowSelected');
			},

			// Event to load store on view display and first row selection as CMDBuild standard
			viewready: function() {
				this.getStore().load({
					scope: this,
					callback: function(records, operation, success) {
						if (!this.getSelectionModel().hasSelection())
							this.getSelectionModel().select(0, true);
					}
				});
			}
		}
	});

})();