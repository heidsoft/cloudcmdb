(function () {

	Ext.define('CMDBuild.view.management.utility.exportCsv.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.utility.ExportCsv'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.utility.exportCsv.ExportCsv}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-blue-panel',
		border: false,
		frame: false,
		overflowY: 'auto',
		standardSubmit: true,

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
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.CLASS_NAME,
						fieldLabel: CMDBuild.Translation.selectAClass,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
						displayField: CMDBuild.core.constants.Proxy.TEXT,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						allowBlank: false,

						store: CMDBuild.proxy.utility.ExportCsv.getStoreClasses(),
						queryMode: 'local'
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

						store: CMDBuild.proxy.utility.ExportCsv.getStoreSeparator(),
						queryMode: 'local'
					}),
					Ext.create('Ext.container.Container', { // TODO: until bottom toolbar implementation to avoid button stretch
						items: [
							Ext.create('CMDBuild.core.buttons.text.Export', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUtilityExportCsvExportButtonClick');
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
