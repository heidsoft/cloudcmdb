(function () {

	Ext.define('CMDBuild.view.management.utility.bulkUpdate.ClassesTree', {
		extend: 'Ext.tree.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.utility.BulkUpdate'
		],

		/**
		 * @cfg {CMDBuild.controller.management.utility.bulkUpdate.BulkUpdate}
		 */
		delegate: undefined,

		autoRender: true,
		border: true,
		floatable: false,
		layout: 'border',
		rootVisible: false,

		bodyStyle: {
			background: '#ffffff'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				store: CMDBuild.proxy.utility.BulkUpdate.getStoreClassesTree()
			});

			this.callParent(arguments);

			this.getSelectionModel().on('selectionchange', function (selectionModel, selected, eOpts) {
				if (!Ext.isEmpty(this.delegate))
					this.delegate.cmfg('onUtilityBulkUpdateClassSelected', selected[0]);
			}, this);
		}
	});

})();
