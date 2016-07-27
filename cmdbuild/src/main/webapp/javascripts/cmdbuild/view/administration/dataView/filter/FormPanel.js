(function() {

	Ext.define('CMDBuild.view.administration.dataView.filter.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.dataView.Filter'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.dataView.Filter}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.Advanced}
		 */
		advancedFilterField: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		cls: 'cmdb-border-top',
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			var classesCombobox = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.constants.Proxy.SOURCE_CLASS_NAME,
				fieldLabel: CMDBuild.Translation.targetClass,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				valueField: CMDBuild.core.constants.Proxy.NAME,
				displayField: CMDBuild.core.constants.Proxy.TEXT,
				forceSelection: true,
				editable: false,
				allowBlank: false,

				store: CMDBuild.proxy.dataView.Filter.getStoreSourceClass(),
				queryMode: 'local'
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyView,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewFilterModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								text: CMDBuild.Translation.removeView,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewFilterRemoveButtonClick');
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
									this.delegate.cmfg('onDataViewFilterSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDataViewFilterAbortButtonClick');
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
							type: CMDBuild.core.constants.Proxy.VIEW,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
							field: CMDBuild.core.constants.Proxy.DESCRIPTION
						}
					}),
					classesCombobox,
					this.advancedFilterField = Ext.create('CMDBuild.view.common.field.filter.advanced.Advanced', {
						name: CMDBuild.core.constants.Proxy.FILTER,
						fieldLabel: CMDBuild.Translation.filter,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						fieldConfiguration: {
							targetClassField: classesCombobox,
							enabledPanels: ['attribute', 'relation']
						}
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

			this.advancedFilterField.setValue(record.get(CMDBuild.core.constants.Proxy.FILTER));
		}
	});

})();