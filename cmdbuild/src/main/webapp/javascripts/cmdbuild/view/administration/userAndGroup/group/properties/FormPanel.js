(function () {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.properties.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.userAndGroup.group.Group'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.Properties}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeCheckbox: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.state.Double}
		 */
		activeStateToggleButton: undefined,

		/**
		 * @property {CMDBuild.view.common.field.CMErasableCombo}
		 */
		startingClassCombo: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',
		split: true,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyGroup,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupTabPropertiesModifyButtonClick');
								}
							}),
							this.activeStateToggleButton = Ext.create('CMDBuild.core.buttons.iconized.state.Double', {
								state1text: CMDBuild.Translation.disableGroup,
								state2text: CMDBuild.Translation.enableGroup,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupTabPropertiesActiveStateToggleButtonClick', button.getActiveState());
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

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupTabPropertiesSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupTabPropertiesAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.NAME,
						fieldLabel: CMDBuild.Translation.name,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false,
						disableEnableFunctions: true,
						vtype: 'alphanumextended'
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.TYPE,
						fieldLabel: CMDBuild.Translation.type,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						editable: false,
						forceSelection: true,

						value: CMDBuild.core.constants.Proxy.NORMAL,

						store: CMDBuild.proxy.userAndGroup.group.Group.getStoreType(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.EMAIL,
						fieldLabel: CMDBuild.Translation.email,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: true,
						vtype: 'emailOrBlank'
					}),
					this.startingClassCombo = Ext.create('CMDBuild.view.common.field.CMErasableCombo', {
						name: CMDBuild.core.constants.Proxy.STARTING_CLASS,
						fieldLabel: CMDBuild.Translation.startingPageAt,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						valueField: CMDBuild.core.constants.Proxy.ID,
						displayField: CMDBuild.core.constants.Proxy.TEXT, // TODO: waiting for refactor (rename description)
						editable: false,
						forceSelection: true,

						store: CMDBuild.proxy.userAndGroup.group.Group.getStoreStartingClass(),
						queryMode: 'local'
					}),
					this.activeCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.IS_ACTIVE,
						fieldLabel: CMDBuild.Translation.active,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						inputValue: true,
						uncheckedValue: false,
						checked: true
					}),
					{
						xtype: 'hiddenfield',
						name: CMDBuild.core.constants.Proxy.ID
					}
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();
