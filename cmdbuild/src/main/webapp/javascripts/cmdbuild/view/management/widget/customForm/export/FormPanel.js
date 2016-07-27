(function () {

	Ext.define('CMDBuild.view.management.widget.customForm.export.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.customForm.Csv'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.widget.customForm.Export}
		 */
		delegate: undefined,

		frame: true,
		border: false,

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

						store: CMDBuild.proxy.widget.customForm.Csv.getStoreExportFileFormat(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.FILE_NAME,
						fieldLabel: CMDBuild.core.constants.Global.getMandatoryLabelFlag() + CMDBuild.Translation.fileName,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						labelAlign: 'right',
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
						allowBlank: false,
						vtype: 'alphanumextended'
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
					Ext.create('CMDBuild.view.common.field.multiselect.Multiselect', {
						name: CMDBuild.core.constants.Proxy.HEADERS,
						fieldLabel: CMDBuild.core.constants.Global.getMandatoryLabelFlag() + CMDBuild.Translation.dataToExport,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						labelAlign: 'right',
						valueField: CMDBuild.core.constants.Proxy.NAME,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						maxHeight: 300,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
						considerAsFieldToDisable: true,
						defaultSelection: 'all',
						flex: 1, // Stretch vertically
						allowBlank: false,

						store: this.delegate.cmfg('widgetCustomFormModelStoreBuilder'),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
