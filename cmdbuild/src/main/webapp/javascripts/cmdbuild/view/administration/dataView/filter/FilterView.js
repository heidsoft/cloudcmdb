(function() {

	Ext.define('CMDBuild.view.administration.dataView.filter.FilterView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.dataView.Filter}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataView.filter.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataView.filter.GridPanel}
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
								text: CMDBuild.Translation.addView,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewFilterAddButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.dataView.filter.GridPanel', {
						delegate: this.delegate,
						region: 'north',
						height: '30%'
					}),
					this.form = Ext.create('CMDBuild.view.administration.dataView.filter.FormPanel', {
						delegate: this.delegate,
						region: 'center'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();