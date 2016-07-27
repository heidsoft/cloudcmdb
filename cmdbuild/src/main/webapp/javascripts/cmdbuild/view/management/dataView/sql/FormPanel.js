(function() {

	Ext.define('CMDBuild.view.management.dataView.sql.FormPanel', {
		extend: 'Ext.tab.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.dataView.Sql}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.dataView.sql.tabs.CardPanel}
		 */
		cardPanel: undefined,

		bodyCls: 'cmdb-blue-panel-no-padding',
		border: false,
		cls: 'cmdb-border-top',
		frame: false,
		split: true,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.cardPanel = Ext.create('CMDBuild.view.management.dataView.sql.tabs.CardPanel', { delegate: this.delegate }),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.detail,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.notes,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.relations,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.history,
						border: false,
						disabled: true
					}),
					Ext.create('Ext.panel.Panel', {
						title: CMDBuild.Translation.attachments,
						border: false,
						disabled: true
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();