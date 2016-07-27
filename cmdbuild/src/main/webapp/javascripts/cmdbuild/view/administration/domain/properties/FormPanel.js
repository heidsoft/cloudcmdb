(function() {

	Ext.define('CMDBuild.view.administration.domain.properties.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.domain.Properties'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.Properties}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeCheckbox: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		cardinalityCombo: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		directDescription: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		domainDescription: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		inverseDescription: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		masterDetailCheckbox: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		masterDetailLabel: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		defaults: {
			maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyDomain,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								text: CMDBuild.Translation.removeDomain,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainRemoveButtonClick');
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
									this.delegate.cmfg('onDomainSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.TextField', {
						name: CMDBuild.core.constants.Proxy.NAME,
						fieldLabel: CMDBuild.Translation.name,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						allowBlank: false,
						vtype: 'alphanum',
						disableEnableFunctions: true,

						listeners: {
							scope: this,
							change: function(field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onDomainPropertiesNameChange', {
									newValue: newValue,
									oldValue: oldValue
								});
							}
						}
					}),
					this.domainDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						allowBlank: false,
						vtype: 'commentextended',

						translationFieldConfig: {
							type: CMDBuild.core.constants.Proxy.DOMAIN,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
							field: CMDBuild.core.constants.Proxy.DESCRIPTION
						}
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID,
						fieldLabel: CMDBuild.Translation.origin,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						valueField: CMDBuild.core.constants.Proxy.ID,
						displayField: CMDBuild.core.constants.Proxy.TEXT,
						allowBlank: false,
						disableEnableFunctions: true,
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.domain.Properties.getStoreClasses(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.DESTINATION_CLASS_ID,
						fieldLabel: CMDBuild.Translation.destination,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						valueField: CMDBuild.core.constants.Proxy.ID,
						displayField: CMDBuild.core.constants.Proxy.TEXT,
						allowBlank: false,
						disableEnableFunctions: true,
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.domain.Properties.getStoreClasses(),
						queryMode: 'local'
					}),
					this.directDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION,
						fieldLabel: CMDBuild.Translation.directDescription,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						allowBlank: false,
						vtype: 'commentextended',

						translationFieldConfig: {
							type: CMDBuild.core.constants.Proxy.DOMAIN,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
							field: CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION
						}
					}),
					this.inverseDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION,
						fieldLabel: CMDBuild.Translation.inverseDescription,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						allowBlank: false,
						vtype: 'commentextended',

						translationFieldConfig: {
							type: CMDBuild.core.constants.Proxy.DOMAIN,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
							field: CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION
						}
					}),
					this.cardinalityCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.CARDINALITY,
						fieldLabel: CMDBuild.Translation.cardinality,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						displayField: CMDBuild.core.constants.Proxy.VALUE,
						allowBlank: false,
						disableEnableFunctions: true,
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.domain.Properties.getStoreCardinality(),
						queryMode: 'local',

						listeners: {
							scope: this,
							select:	function(combo, records, eOpts) {
								this.delegate.cmfg('onDomainPropertiesCardinalitySelect');
							}
						}
					}),
					this.masterDetailCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.IS_MASTER_DETAIL,
						fieldLabel: CMDBuild.Translation.masterDetail,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						inputValue: true,
						uncheckedValue: false,

						listeners: {
							scope: this,
							change: function(field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onDomainPropertiesMasterDetailCheckboxChange');
							}
						}
					}),
					this.masterDetailLabel = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.constants.Proxy.MASTER_DETAIL_LABEL,
						fieldLabel: CMDBuild.Translation.masterDetailLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						hidden: true, // Hidden by default

						translationFieldConfig: {
							type: CMDBuild.core.constants.Proxy.DOMAIN,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
							field: CMDBuild.core.constants.Proxy.MASTER_DETAIL
						}
					}),
					this.activeCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.ACTIVE,
						fieldLabel: CMDBuild.Translation.active,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						inputValue: true,
						uncheckedValue: false
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