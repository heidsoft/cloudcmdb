(function() {

	Ext.define('CMDBuild.view.management.dataView.sql.SqlView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.dataView.Sql}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.dataView.sql.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.dataView.sql.GridPanel}
		 */
		grid: undefined,

		/**
		 * @cfg {Boolean}
		 */
		whitMap: false,

		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.grid = Ext.create('CMDBuild.view.management.dataView.sql.GridPanel', {
						delegate: this.delegate,
						region: 'center'
					}),
					this.form = Ext.create('CMDBuild.view.management.dataView.sql.FormPanel', {
						delegate: this.delegate,
						region: 'south',
						height: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.CARD_FORM_RATIO) + '%'
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onDataViewSqlPanelShow');
			}
		}
	});

})();