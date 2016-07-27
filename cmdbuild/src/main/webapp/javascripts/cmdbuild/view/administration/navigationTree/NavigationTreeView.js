(function () {

	Ext.define('CMDBuild.view.administration.navigationTree.NavigationTreeView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.navigationTree.NavigationTree}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.navigationTrees,

		/**
		 * @property {Ext.tab.Panel}
		 */
		tabPanel: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: true,
		frame: false,
		layout: 'fit',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addNavigationTree,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onNavigationTreeAddButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.tabPanel = Ext.create('Ext.tab.Panel', {
						frame: false,
						border: false,
						activeTab: 0,

						items: []
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
