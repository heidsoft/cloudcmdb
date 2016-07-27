(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.relations.RelationsView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.panels.relations.Relations}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.relations.CardGridPanel}
		 */
		gridCard: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.relations.DomainGridPanel}
		 */
		gridDomain: undefined,

		border: false,
		frame: false,
		layout: 'border',
		title: CMDBuild.Translation.relations,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.gridDomain = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.relations.DomainGridPanel', {
						delegate: this.delegate,
						region: 'north',
						split: true,
						height: '30%'
					}),
					this.gridCard = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.relations.CardGridPanel', {
						delegate: this.delegate,
						cls: 'cmdb-border-top',
						disabled: true,
						region: 'center'
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onFieldFilterAdvancedWindowRelationsShow');
			}
		}
	});

})();