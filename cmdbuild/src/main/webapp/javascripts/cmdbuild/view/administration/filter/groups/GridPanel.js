(function() {

	Ext.define('CMDBuild.view.administration.filter.groups.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.filter.Group'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.filter.Groups}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		frame: false,

		initComponent: function() {
			var store = CMDBuild.proxy.filter.Group.getStore();

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Paging', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						store: store,
						displayInfo: true,
						displayMsg: '{0} - {1} ' + CMDBuild.Translation.common.display_topic_of + ' {2}',
						emptyMsg: CMDBuild.Translation.common.display_topic_none
					})
				],
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
						dataIndex: CMDBuild.core.constants.Proxy.ENTRY_TYPE,
						text: CMDBuild.Translation.targetClass,
						flex: 1
					}
				],
				store: store
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onFilterGroupsItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onFilterGroupsRowSelected');
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