(function() {

	Ext.define('CMDBuild.view.administration.lookup.list.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.lookup.Lookup'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.lookup.List}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeCheckbox: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.state.Double}
		 */
		enableDisableButton: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		parentCombobox: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		cls: 'cmdb-border-top',
		frame: false,
		overflowY: 'auto',
		split: true,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyLookup,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLookupListModifyButtonClick');
								}
							}),
							this.enableDisableButton = Ext.create('CMDBuild.core.buttons.iconized.state.Double', {
								state1text: CMDBuild.Translation.disableLookup,
								state2text: CMDBuild.Translation.enableLookup,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLookupListEnableDisableButtonClick', button.getActiveState());
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
									this.delegate.cmfg('onLookupListSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLookupListAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.CODE,
						fieldLabel: CMDBuild.Translation.code,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
					}),
					Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false,

						translationFieldConfig: {
							type: CMDBuild.core.constants.Proxy.LOOKUP_VALUE,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.TRANSLATION_UUID, source: this },
							field: CMDBuild.core.constants.Proxy.DESCRIPTION
						}
					}),
					this.parentCombobox = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.PARENT_ID,
						fieldLabel: CMDBuild.Translation.parentDescription,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						displayField: 'ParentDescription',
						valueField: 'ParentId',
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.lookup.Lookup.getStoreParents(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.TextArea', {
						name: CMDBuild.core.constants.Proxy.NOTES,
						fieldLabel: CMDBuild.Translation.notes,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
					}),
					this.activeCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.ACTIVE,
						fieldLabel: CMDBuild.Translation.active,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						inputValue: true,
						uncheckedValue: false,
						checked: true
					}),
					{
						xtype: 'hiddenfield',
						name: CMDBuild.core.constants.Proxy.ID
					},
					{ // Used for translations
						xtype: 'hiddenfield',
						name: CMDBuild.core.constants.Proxy.TRANSLATION_UUID
					}
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();