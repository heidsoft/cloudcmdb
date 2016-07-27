(function () {

	Ext.define('CMDBuild.view.management.utility.importCsv.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.utility.ImportCsv'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.utility.importCsv.ImportCsv}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-blue-panel',
		border: false,
		cls: 'cmdb-border-bottom',
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		monitorValid: true,
		overflowY: 'auto',

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
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Upload', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onUtilityImportCsvUploadButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.ComboBox', {
						name: 'idClass',
						fieldLabel: CMDBuild.Translation.selectAClass,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
						displayField: CMDBuild.core.constants.Proxy.TEXT,
						valueField: CMDBuild.core.constants.Proxy.ID,
						editable: false,
						allowBlank: false,

						store: CMDBuild.proxy.utility.ImportCsv.getStoreClasses(),
						queryMode: 'local',

						listeners: {
							scope: this,
							select: function (field, records, eOpts) {
								this.delegate.cmfg('onUtilityImportCsvClassSelected', records[0]);
							}
						}
					}),
					Ext.create('Ext.form.field.File', {
						name: CMDBuild.core.constants.Proxy.FILE,
						fieldLabel: CMDBuild.Translation.csvFile,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.SEPARATOR,
						fieldLabel: CMDBuild.Translation.separator,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.VALUE,
						maxWidth: 200,
						value: ';',
						editable: false,
						allowBlank: false,

						store: CMDBuild.proxy.utility.ImportCsv.getStoreSeparator(),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
