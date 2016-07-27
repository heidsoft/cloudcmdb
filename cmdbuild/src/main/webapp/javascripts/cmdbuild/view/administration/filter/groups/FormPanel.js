(function() {

	Ext.define('CMDBuild.view.administration.filter.groups.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.filter.Groups}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.Advanced}
		 */
		advancedFilterField: undefined,

		/**
		 * @property {CMDBuild.view.common.field.multiselect.Group}
		 */
		defaultForGroupsField: undefined,

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
								text: CMDBuild.Translation.modifyFilter,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFilterGroupsModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								text: CMDBuild.Translation.removeFilter,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFilterGroupsRemoveButtonClick');
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
									this.delegate.cmfg('onFilterGroupsSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFilterGroupsAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.NAME,
						itemId: CMDBuild.core.constants.Proxy.NAME,
						fieldLabel: CMDBuild.Translation.name,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false,
						disableEnableFunctions: true
					}),
					Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false,
						vtype: 'commentextended',

						translationFieldConfig: {
							type: CMDBuild.core.constants.Proxy.FILTER,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
							field: CMDBuild.core.constants.Proxy.DESCRIPTION
						}
					}),
					this.targetClassCombobox = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.ENTRY_TYPE,
						fieldLabel: CMDBuild.Translation.targetClass,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						displayField: CMDBuild.core.constants.Proxy.TEXT, // TODO: waiting for refactor (rename description)
						forceSelection: true,
						editable: false,
						allowBlank: false,

						store: CMDBuild.proxy.filter.Group.getStoreTargetClass(),
						queryMode: 'local'
					}),
					this.advancedFilterField = Ext.create('CMDBuild.view.common.field.filter.advanced.Advanced', {
						name: CMDBuild.core.constants.Proxy.CONFIGURATION,
						fieldLabel: CMDBuild.Translation.filter,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						fieldConfiguration: {
							targetClassField: this.targetClassCombobox,
							enabledPanels: ['attribute', 'relation']
						}
					}),
					this.defaultForGroupsField = Ext.create('CMDBuild.view.common.field.multiselect.Group', {
						name: CMDBuild.core.constants.Proxy.DEFAULT_FOR_GROUPS,
						fieldLabel: CMDBuild.Translation.defaultForGroups,
						height: 300,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						valueField: CMDBuild.core.constants.Proxy.NAME
					}),
					{
						xtype: 'hiddenfield',
						name: CMDBuild.core.constants.Proxy.ID
					}
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		},

		/**
		 * LoadRecord override to implement setValue of custom fields (witch don't extends Ext.form.field.Base)
		 *
		 * @param {Ext.data.Model} record
		 *
		 * @override
		 */
		loadRecord: function(record) {
			this.callParent(arguments);

			this.advancedFilterField.setValue(record.get(CMDBuild.core.constants.Proxy.CONFIGURATION));
			this.defaultForGroupsField.setValue(record.get(CMDBuild.core.constants.Proxy.DEFAULT_FOR_GROUPS));
		}
	});

})();