(function() {

	Ext.define('CMDBuild.view.administration.lookup.list.ListView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.lookup.List}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.lookup.list.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.lookup.list.GridPanel}
		 */
		grid: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		disabled: true,
		frame: false,
		layout: 'border',
		title: CMDBuild.Translation.lookupList,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.administration.lookup.list.FormPanel', {
						delegate: this.delegate,
						region: 'center'
					}),
					this.grid = Ext.create('CMDBuild.view.administration.lookup.list.GridPanel', {
						delegate: this.delegate,
						region: 'north',
						split: true,
						height: '30%'
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onLookupListTabShow');
			}
		}
	});

})();