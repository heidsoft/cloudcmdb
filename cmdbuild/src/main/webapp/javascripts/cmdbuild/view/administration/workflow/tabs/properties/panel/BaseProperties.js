(function () {

	Ext.define('CMDBuild.view.administration.workflow.tabs.properties.panel.BaseProperties', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.workflow.Properties'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.tabs.Properties}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		descriptionField: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		parentCombo: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,

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
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.NAME,
						fieldLabel: CMDBuild.Translation.name,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false,
						vtype: 'alphanum',
						disableEnableFunctions: true,

						enableKeyEvents: true,

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.fieldSynch(this.descriptionField, newValue, oldValue);
							}
						}
					}),
					this.descriptionField = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false,
						vtype: 'commentextended',

						translationFieldConfig: {
							type: CMDBuild.core.constants.Proxy.CLASS,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
							field: CMDBuild.core.constants.Proxy.DESCRIPTION
						}
					}),
					this.parentCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.PARENT,
						fieldLabel: CMDBuild.Translation.inheritsFrom,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						valueField: CMDBuild.core.constants.Proxy.ID,
						displayField: CMDBuild.core.constants.Proxy.TEXT,
						editable: false,
						disableEnableFunctions: true,

						store: CMDBuild.proxy.workflow.Properties.getStoreSuperProcesses(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.Checkbox',{
						name: CMDBuild.core.constants.Proxy.IS_SUPER_CLASS,
						fieldLabel: CMDBuild.Translation.superclass,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						inputValue: true,
						uncheckedValue: false,
						disableEnableFunctions: true
					}),
					Ext.create('Ext.form.field.Checkbox',{
						name: CMDBuild.core.constants.Proxy.ACTIVE,
						fieldLabel: CMDBuild.Translation.active,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						inputValue: true,
						uncheckedValue: false
					}),
					Ext.create('Ext.form.field.Checkbox',{
						name: CMDBuild.core.constants.Proxy.USER_STOPPABLE,
						fieldLabel: CMDBuild.Translation.userStoppable,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
					}),
					{
						xtype: 'hiddenfield',
						name: CMDBuild.core.constants.Proxy.ID
					},
					Ext.create('Ext.container.Container', {
						style: {
							borderTop: '1px solid #d0d0d0'
						},

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								margin: '5',
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWorkflowTabPropertiesSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWorkflowTabPropertiesAbortButtonClick');
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
