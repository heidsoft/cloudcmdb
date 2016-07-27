(function () {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.privileges.UiConfigurationWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.privileges.UiConfiguration}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		autoHeight: true,
		closeAction: 'hide',
		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.buttonsToDisable,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
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
							Ext.create('CMDBuild.core.buttons.text.Confirm', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridUIConfigurationSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridUIConfigurationAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.form = Ext.create('Ext.form.Panel', {
						frame: true,
						border: false,

						items: [
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.CREATE,
								fieldLabel: CMDBuild.Translation.add,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.REMOVE,
								fieldLabel: CMDBuild.Translation.remove,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.MODIFY,
								fieldLabel: CMDBuild.Translation.modify,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								inputValue: true,
								uncheckedValue: false
							}),
							Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.CLONE,
								fieldLabel: CMDBuild.Translation.clone,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								inputValue: true,
								uncheckedValue: false
							})
						]
					})
				]
			});

			this.callParent(arguments);

			// Custom window width
			this.width = 200;
		},

		listeners: {
			show: function (window, eOpts) {
				this.delegate.cmfg('onUserAndGroupGroupPrivilegesGridUIConfigurationShow');
			}
		}
	});

})();
