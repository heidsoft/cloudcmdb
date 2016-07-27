(function() {

	Ext.define('CMDBuild.view.administration.email.template.TemplateView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.email.template.Template}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.template.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.template.GridPanel}
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
								text: CMDBuild.Translation.addTemplate,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplateAddButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.grid = Ext.create('CMDBuild.view.administration.email.template.GridPanel', {
						delegate: this.delegate,
						region: 'north',
						height: '30%'
					}),
					this.form = Ext.create('CMDBuild.view.administration.email.template.FormPanel', {
						delegate: this.delegate,
						region: 'center'
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onEmailTemplateShow');
			}
		}
	});

})();
