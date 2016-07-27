(function() {

	Ext.define('CMDBuild.view.administration.filter.groups.GroupsView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.filter.Groups}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.filter.groups.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.filter.groups.GridPanel}
		 */
		grid: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addFilter,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFilterGroupsAddButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.filter.groups.GridPanel', {
						delegate: this.delegate,
						region: 'north',
						split: true,
						height: '30%'
					}),
					this.form = Ext.create('CMDBuild.view.administration.filter.groups.FormPanel', {
						delegate: this.delegate,
						region: 'center'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();