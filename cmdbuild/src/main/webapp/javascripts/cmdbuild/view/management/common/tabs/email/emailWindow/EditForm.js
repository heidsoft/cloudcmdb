(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.emailWindow.EditForm', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.EmailWindow}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.HtmlEditor}
		 */
		emailContentField: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		keepSynchronizationCheckbox: undefined,

		frame: false,
		border: false,
		padding: '5',
		flex: 3,
		bodyCls: 'x-panel-body-default-framed',

		layout: {
			type: 'vbox',
			align: 'stretch' // Child items are stretched to full width
		},

		defaults: {
			labelAlign: 'right',
			labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
		},

		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.keepSynchronizationCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION,
						fieldLabel: CMDBuild.Translation.keepSync,
						labelAlign: 'right',
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						disabled: true,
						inputValue: true,
						uncheckedValue: false
					}),
					this.delayField = Ext.create('CMDBuild.view.common.field.delay.Delay', {
						name: CMDBuild.core.constants.Proxy.DELAY,
						fieldLabel: CMDBuild.Translation.delay,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						labelAlign: 'right',
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM
					}),
					this.fromField = Ext.create('Ext.form.field.Display', {
						name: CMDBuild.core.constants.Proxy.FROM,
						fieldLabel: CMDBuild.Translation.from,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						labelAlign: 'right',
						vtype: 'multiemail',
						submitValue: true,

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onTabEmailEmailWindowFieldChange');
							}
						}
					}),
					{
						xtype: 'textfield',
						name: CMDBuild.core.constants.Proxy.TO,
						allowBlank: false,
						fieldLabel: CMDBuild.Translation.to,
						vtype: 'multiemail',

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onTabEmailEmailWindowFieldChange');
							}
						}
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.constants.Proxy.CC,
						fieldLabel: CMDBuild.Translation.cc,
						vtype: 'multiemail',

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onTabEmailEmailWindowFieldChange');
							}
						}
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.constants.Proxy.BCC,
						fieldLabel: CMDBuild.Translation.bcc,
						vtype: 'multiemail',

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onTabEmailEmailWindowFieldChange');
							}
						}
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.constants.Proxy.SUBJECT,
						allowBlank: false,
						fieldLabel: CMDBuild.Translation.subject,

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onTabEmailEmailWindowFieldChange');
							}
						}
					},
					this.emailContentField = Ext.create('CMDBuild.view.common.field.HtmlEditor', {
						name: CMDBuild.core.constants.Proxy.BODY,
						hideLabel: true,
						flex: 1,

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onTabEmailEmailWindowFieldChange');
							}
						}
					})
				]
			});

			this.callParent(arguments);

			this.fixIEFocusIssue();
		},

		/**
		 * Sometimes on IE the HtmlEditor is not able to take the focus after the mouse click. With this call it works. The reason is currently unknown.
		 */
		fixIEFocusIssue: function () {
			if (Ext.isIE || !!navigator.userAgent.match(/Trident.*rv[ :]*11\./)) // Workaround to detect IE 11 witch is not supported from Ext 4.2
				this.mon(this.emailContentField, 'render', function () {
					try {
						this.emailContentField.focus();
					} catch (e) {}
				}, this);
		}
	});

})();
