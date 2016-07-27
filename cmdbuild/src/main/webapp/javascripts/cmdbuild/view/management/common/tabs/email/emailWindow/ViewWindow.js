(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.emailWindow.ViewWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.EmailWindow}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.attachments.MainContainer}
		 */
		attachmentContainer: undefined,

		/**
		 * @property {Ext.button.Split}
		 */
		fillFromTemplateButton: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		title: CMDBuild.Translation.viewEmail,

		layout: 'border',

		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
						items: [
							this.fillFromTemplateButton = Ext.create('Ext.button.Split', {
								iconCls: 'clone',
								text: CMDBuild.Translation.composeFromTemplate,
								disabled: true,

								menu: Ext.create('Ext.menu.Menu', {
									items: []
								})
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Close', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onTabEmailEmailWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('Ext.form.Panel', { // Used Ext.form.Panel to be able to use loadRecord() function to load fields values
						region: 'center',
						frame: false,
						border: false,
						padding: '5',
						bodyCls: 'x-panel-body-default-framed',

						layout: {
							type: 'vbox',
							align: 'stretch' // Child items are stretched to full width
						},

						defaults: {
							labelAlign: 'right',
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
						},

						items: [
							{
								xtype: 'checkbox',
								fieldLabel: CMDBuild.Translation.keepSync,
								readOnly: true,
								name: CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION
							},
							Ext.create('CMDBuild.view.common.field.delay.Display', {
								name: CMDBuild.core.constants.Proxy.DELAY,
								fieldLabel: CMDBuild.Translation.delay,
								labelAlign: 'right',
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								readOnly: true
							}),
							{
								xtype: 'displayfield',
								name: CMDBuild.core.constants.Proxy.FROM,
								fieldLabel: CMDBuild.Translation.from
							},
							{
								xtype: 'displayfield',
								name: CMDBuild.core.constants.Proxy.TO,
								fieldLabel: CMDBuild.Translation.to
							},
							{
								xtype: 'displayfield',
								name: CMDBuild.core.constants.Proxy.CC,
								fieldLabel: CMDBuild.Translation.cc
							},
							{
								xtype: 'displayfield',
								name: CMDBuild.core.constants.Proxy.BCC,
								fieldLabel: CMDBuild.Translation.bcc
							},
							{
								xtype: 'displayfield',
								name: CMDBuild.core.constants.Proxy.SUBJECT,
								fieldLabel: CMDBuild.Translation.subject
							},
							{ // Thisn't a good way to display email content, but i don't know better one
								xtype: 'panel',
								autoScroll: true,
								frame: true,
								border: true,
								margin: '1 0', // Fixes a bug that hides bottom border
								flex: 1,
								html: this.delegate.record.get(CMDBuild.core.constants.Proxy.BODY)
							}
						]
					}),
					this.attachmentContainer = Ext.create('CMDBuild.view.management.common.tabs.email.attachments.MainContainer', {
						height: '30%',
						region: 'south',
						readOnly: true
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			beforedestroy: function (window, eOpts) {
				return this.delegate.cmfg('onTabEmailEmailWindowBeforeDestroy');
			}
		}
	});

})();
