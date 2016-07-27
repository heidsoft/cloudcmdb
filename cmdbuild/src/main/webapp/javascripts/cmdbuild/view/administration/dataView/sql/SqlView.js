(function() {

	Ext.define('CMDBuild.view.administration.dataView.sql.SqlView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.dataView.Sql}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataView.sql.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataView.sql.GridPanel}
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
									this.delegate.cmfg('onDataViewSqlAddButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.dataView.sql.GridPanel', {
						delegate: this.delegate,
						region: 'north',
						split: true,
						height: '30%'
					}),
					this.form = Ext.create('CMDBuild.view.administration.dataView.sql.FormPanel', {
						delegate: this.delegate,
						region: 'center'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();