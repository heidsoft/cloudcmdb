(function () {

	Ext.define('CMDBuild.view.administration.report.jasper.form.Step1Panel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		description: undefined,

		/**
		 * @property {Ext.form.field.File}
		 */
		fileField: undefined,

		/**
		 * @property {CMDBuild.view.common.field.CMGroupSelectionList}
		 */
		groups: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		name: undefined,

		/**
		 * @property {Ext.form.field.Hidden}
		 */
		reportId: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
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
				items: [
					this.name = Ext.create('Ext.form.field.Text',{
						name: CMDBuild.core.constants.Proxy.TITLE, // TODO: waiting for refactor (rename "name")
						fieldLabel: CMDBuild.Translation.name,
						allowBlank: false,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						disableEnableFunctions: true
					}),
					this.description = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false,

						translationFieldConfig: {
							type: CMDBuild.core.constants.Proxy.REPORT,
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.TITLE, source: this }, // TODO: waiting for refactor (rename "name")
							field: CMDBuild.core.constants.Proxy.DESCRIPTION
						}
					}),
					this.groups = Ext.create('CMDBuild.view.common.field.CMGroupSelectionList', {
						name: CMDBuild.core.constants.Proxy.GROUPS,
						fieldLabel: CMDBuild.Translation.enabledGroups,
						height: 300,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
					}),
					this.fileField = Ext.create('Ext.form.field.File', {
						name: CMDBuild.core.constants.Proxy.JRXML,
						fieldLabel: CMDBuild.Translation.masterReportJrxml,
						allowBlank: false,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
					}),
					this.reportId = Ext.create('Ext.form.field.Hidden', { name: CMDBuild.core.constants.Proxy.ID })
				]
			});

			this.callParent(arguments);
		}
	});

})();
