(function() {

	Ext.define('CMDBuild.view.administration.email.template.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.email.Account'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.email.template.Template}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.delay.Delay}
		 */
		delayField: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		cls: 'cmdb-border-top',
		frame: false,

		layout: {
			type: 'hbox',
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
								text: CMDBuild.Translation.modifyTemplate,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplateModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								text: CMDBuild.Translation.removeTemplate,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplateRemoveButtonClick');
								}
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
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplateSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplateAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.baseProperties,
						overflowY: 'auto',
						flex: 1,

						defaults: {
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
						},

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.NAME,
								fieldLabel: CMDBuild.Translation.name,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								allowBlank: false,
								disableEnableFunctions: true
							}),
							{
								xtype: 'textareafield',
								name: CMDBuild.core.constants.Proxy.DESCRIPTION,
								fieldLabel: CMDBuild.Translation.descriptionLabel,
								allowBlank: false
							},
							{
								xtype: 'checkbox',
								fieldLabel: CMDBuild.Translation.keepSync,
								inputValue: true,
								uncheckedValue: false,
								name: CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION
							},
							{
								xtype: 'checkbox',
								fieldLabel: CMDBuild.Translation.promptSync,
								inputValue: true,
								uncheckedValue: false,
								name: CMDBuild.core.constants.Proxy.PROMPT_SYNCHRONIZATION
							},
							this.delayField = Ext.create('CMDBuild.view.common.field.delay.Delay', {
								fieldLabel: CMDBuild.Translation.delay,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								name: CMDBuild.core.constants.Proxy.DELAY
							}),
							Ext.create('Ext.form.field.Hidden', { name: CMDBuild.core.constants.Proxy.ID })
						]
					}),
					{ xtype: 'splitter' },
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.template,
						overflowY: 'auto',
						flex: 1,

						style: {
							paddingBottom: '5px'
						},

						defaults: {
							xtype: 'textfield',
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
						},

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							Ext.create('CMDBuild.view.common.field.CMErasableCombo', {
								name: CMDBuild.core.constants.Proxy.DEFAULT_ACCOUNT,
								fieldLabel: CMDBuild.Translation.defaultAccount,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								displayField: CMDBuild.core.constants.Proxy.NAME,
								valueField: CMDBuild.core.constants.Proxy.NAME,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
								forceSelection: true,
								editable: false,

								store: CMDBuild.proxy.email.Account.getStore(true),
								queryMode: 'local'
							}),
							{
								name: CMDBuild.core.constants.Proxy.FROM,
								fieldLabel: CMDBuild.Translation.from,
								vtype: 'email'
							},
							{
								name: CMDBuild.core.constants.Proxy.TO,
								fieldLabel: CMDBuild.Translation.to
							},
							{
								name: CMDBuild.core.constants.Proxy.CC,
								fieldLabel: CMDBuild.Translation.cc
							},
							{
								name: CMDBuild.core.constants.Proxy.BCC,
								fieldLabel: CMDBuild.Translation.bcc
							},
							{
								name: CMDBuild.core.constants.Proxy.SUBJECT,
								fieldLabel: CMDBuild.Translation.subject
							},
							Ext.create('CMDBuild.view.common.field.HtmlEditor', {
								name: CMDBuild.core.constants.Proxy.BODY,
								fieldLabel: CMDBuild.Translation.body,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.editValues,
								margin: '0 0 0 ' + (CMDBuild.core.constants.FieldWidths.LABEL + 5),
								maxWidth: 100,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplateValuesButtonClick');
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();
