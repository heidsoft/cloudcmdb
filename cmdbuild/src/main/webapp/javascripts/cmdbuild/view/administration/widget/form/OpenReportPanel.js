(function () {

	Ext.define('CMDBuild.view.administration.widget.form.OpenReportPanel', {
		extend: 'CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionPanel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.OpenReport',
			'CMDBuild.model.widget.openReport.PresetGrid'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.form.OpenReport}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.DrivedCheckbox}
		 */
		forceFormat: undefined,

		/**
		 * @property {CMDBuild.view.common.field.grid.KeyValue}
		 */
		presetGrid: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		reportCode: undefined,

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		widgetDefinitionFormAdditionalPropertiesGet: function () {
			return [
				Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.additionalProperties,
					flex: 1,

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [
						this.reportCode = Ext.create('Ext.form.field.ComboBox', {
							name: CMDBuild.core.constants.Proxy.REPORT_CODE,
							fieldLabel: CMDBuild.Translation.report,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							valueField: CMDBuild.core.constants.Proxy.TITLE,
							displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
							forceSelection: true,
							editable: false,

							store: CMDBuild.proxy.widget.OpenReport.getStore(),
							queryMode: 'local',

							listeners: {
								scope: this,
								select: function (combo, records, eOpts) {
									this.delegate.cmfg('onClassTabWidgetOpenReportReportSelect', { selectedReport: records[0] });
								}
							}
						}),
						this.forceFormat = Ext.create('CMDBuild.view.common.field.comboBox.DrivedCheckbox', {
							name: CMDBuild.core.constants.Proxy.FORCE_FORMAT,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							fieldLabel: CMDBuild.Translation.forceFormat,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
							valueField: CMDBuild.core.constants.Proxy.VALUE,

							store: CMDBuild.proxy.widget.OpenReport.getStoreForceFormat()
						}),
						this.presetGrid = Ext.create('CMDBuild.view.common.field.grid.KeyValue', {
							additionalColumns: [
								Ext.create('Ext.grid.column.CheckColumn', {
									dataIndex: CMDBuild.core.constants.Proxy.READ_ONLY,
									text: CMDBuild.Translation.readOnly,
									width: 60,
									align: 'center',
									hideable: false,
									menuDisabled: true,
									fixed: true
								})
							],
							enableCellEditing: true,
							keyAttributeName: CMDBuild.core.constants.Proxy.NAME,
							keyLabel: CMDBuild.Translation.attribute,
							margin: '8 0 9 0',
							modelName: 'CMDBuild.model.widget.openReport.PresetGrid',
							title: CMDBuild.Translation.reportAttributes
						})
					]
				})
			];
		}
	});

})();
