(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.ImportCSVWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.grid.Csv'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.ImportCSV}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Hidden}
		 */
		classIdField: undefined,

		/**
		 * @property {Ext.form.field.File}
		 */
		csvFileField: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		csvImportModeCombo: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		csvSeparatorCombo: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		csvUploadForm: undefined,

		border: false,
		defaultSizeW: 0.90,
		autoHeight: true,
		title: CMDBuild.Translation.importFromCSV,

		initComponent: function() {
			this.classIdField = Ext.create('Ext.form.field.Hidden', {
				name: 'idClass'
			});

			this.csvFileField = Ext.create('Ext.form.field.File', {
				name: CMDBuild.core.constants.Proxy.FILE,
				fieldLabel: CMDBuild.Translation.csvFile,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				labelAlign: 'right',
				allowBlank: true,
				width: CMDBuild.core.constants.FieldWidths.STANDARD_BIG
			});

			this.csvSeparatorCombo = Ext.create('Ext.form.field.ComboBox', {
				name: 'separator',
				fieldLabel: CMDBuild.Translation.separator,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				labelAlign: 'right',
				valueField: CMDBuild.core.constants.Proxy.VALUE,
				displayField: CMDBuild.core.constants.Proxy.VALUE,
				width: 200,
				value: ';',
				editable: false,
				allowBlank: false,

				store: CMDBuild.proxy.grid.Csv.getStoreSeparator(),
				queryMode: 'local'
			});

			this.csvImportModeCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.constants.Proxy.MODE,
				fieldLabel: CMDBuild.Translation.mode,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				labelAlign: 'right',
				valueField: CMDBuild.core.constants.Proxy.VALUE,
				displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
				width: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
				value: 'replace',
				editable: false,
				allowBlank: false,

				store: CMDBuild.proxy.grid.Csv.getStoreImportMode(),
				queryMode: 'local'
			});

			this.csvUploadForm = Ext.create('Ext.form.Panel', {
				frame: true,
				border: false,
				encoding: 'multipart/form-data',
				fileUpload: true,
				monitorValid: true,

				items: [this.csvFileField, this.csvSeparatorCombo, this.csvImportModeCombo, this.classIdField]
			});

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

								handler: function(button, e) {
									this.delegate.cmfg('onImportCSVUploadButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onImportCSVAbortButtonClick');
								}
							})
						]
					})
				],
				items: [this.csvUploadForm]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.width = this.width * this.defaultSizeW;
		}
	});

})();