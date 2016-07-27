(function() {

	Ext.define('CMDBuild.view.management.dataView.sql.tabs.CardPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.dataView.Sql}
		 */
		delegate: undefined,

		bodyCls: 'x-panel-body-default-framed cmdb-border-top',
		bodyPadding: '5 5 0 5',
		border: false,
		frame: false,
		overflowY: 'auto',
		title: CMDBuild.Translation.card,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyCard,
								disabled: true
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								text: CMDBuild.Translation.deleteCard,
								disabled: true
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Clone', {
								text: CMDBuild.Translation.cloneCard,
								disabled: true
							}),
							Ext.create('CMDBuild.core.buttons.iconized.RelationGraph', { disabled: true }),
							Ext.create('CMDBuild.core.buttons.iconized.split.Print', {
								text: CMDBuild.Translation.print + ' ' + CMDBuild.Translation.card.toLowerCase(),
								disabled: true
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',
						cls: 'x-panel-body-default-framed',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', { disabled: true }),
							Ext.create('CMDBuild.core.buttons.text.Abort', { disabled: true })
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();