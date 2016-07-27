(function () {

	Ext.define('CMDBuild.view.administration.localization.importExport.ImportForm', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.localization.Import'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.ImportExport}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		monitorValid: true,
		title: CMDBuild.Translation.import,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

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
							Ext.create('CMDBuild.core.buttons.text.Import', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onLocalizationImportExportImportButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.FORMAT,
						fieldLabel: CMDBuild.Translation.format,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_MEDIUM,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						editable: false,
						allowBlank: false,
						disabled: true,

						value: CMDBuild.core.constants.Proxy.CSV, // Default value

						store: CMDBuild.proxy.localization.Import.getStoreFileFormat(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.File', {
						name: CMDBuild.core.constants.Proxy.FILE,
						fieldLabel: CMDBuild.Translation.file,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
						allowBlank: false
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.SEPARATOR,
						fieldLabel: CMDBuild.Translation.separator,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: 200,
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.VALUE,
						editable: false,
						allowBlank: false,

						value: ';', // Default value

						store: CMDBuild.proxy.localization.Import.getStoreSeparator(),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
