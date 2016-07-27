(function () {

	Ext.define('CMDBuild.view.management.widget.customForm.import.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.customForm.Csv'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.widget.customForm.Import}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.multiselect.Multiselect}
		 */
		keyAttributesMultiselect: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		modeCombo: undefined,

		frame: true,
		border: false,
		encoding: 'multipart/form-data',

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
					Ext.create('Ext.form.field.ComboBox', { // Prepared for future implementations
						name: CMDBuild.core.constants.Proxy.FORMAT,
						fieldLabel: CMDBuild.Translation.format,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						labelAlign: 'right',
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						editable: false,
						allowBlank: false,
						disabled: true,

						value: CMDBuild.core.constants.Proxy.CSV, // Default value

						store: CMDBuild.proxy.widget.customForm.Csv.getStoreImportFileFormat(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.File', {
						name: CMDBuild.core.constants.Proxy.FILE,
						fieldLabel: CMDBuild.core.constants.Global.getMandatoryLabelFlag() + CMDBuild.Translation.file,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						labelAlign: 'right',
						allowBlank: false,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.SEPARATOR,
						fieldLabel: CMDBuild.Translation.separator,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						labelAlign: 'right',
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.VALUE,
						maxWidth: 200,
						value: ';',
						editable: false,
						allowBlank: false,

						store: CMDBuild.proxy.widget.customForm.Csv.getStoreSeparator(),
						queryMode: 'local'
					}),
					this.modeCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.MODE,
						fieldLabel: CMDBuild.Translation.mode,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						labelAlign: 'right',
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
						value: 'replace',
						editable: false,
						allowBlank: false,

						store: CMDBuild.proxy.widget.customForm.Csv.getStoreImportMode(
							// Remove add option from store in form layout
							this.delegate.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.LAYOUT) == 'form' ? ['add'] : null
						),
						queryMode: 'local',

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) {
								this.delegate.cmfg('onWidgetCustomFormImportModeChange');
							}
						}
					}),
					this.keyAttributesMultiselect = Ext.create('CMDBuild.view.common.field.multiselect.Multiselect', {
						name: CMDBuild.core.constants.Proxy.KEY_ATTRIBUTES,
						fieldLabel: CMDBuild.core.constants.Global.getMandatoryLabelFlag() + CMDBuild.Translation.keyAttributes,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						labelAlign: 'right',
						valueField: CMDBuild.core.constants.Proxy.NAME,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						maxHeight: 300,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
						considerAsFieldToDisable: true,
						flex: 1, // Stretch vertically
						allowBlank: false,
						disabled: true,

						store: this.delegate.cmfg('widgetCustomFormModelStoreBuilder'),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
