(function () {

	Ext.define('CMDBuild.view.administration.tasks.common.reportForm.ReportFormView', {
		extend: 'Ext.form.FieldContainer',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.taskManager.common.ReportForm'
		],

		mixins: ['Ext.form.field.Field'], // To enable functionalities restricted to Ext.form.field.Field classes (loadRecord, etc.)

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.reportForm.ReportForm}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		combo: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		extension: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.reportForm.GridPanel}
		 */
		grid: undefined,

		border: false,
		considerAsFieldToDisable: true,
		frame: false,

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
			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.reportForm.ReportForm', { view: this });

			Ext.apply(this, {
				items: [
					this.combo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.REPORT_NAME,
						fieldLabel: CMDBuild.Translation.report,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						valueField: CMDBuild.core.constants.Proxy.TITLE,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.taskManager.common.ReportForm.getStore(),
						queryMode: 'local',

						listeners: {
							scope: this,
							change: function (field, newValue, oldValue, eOpts) { // Selected by setValue()
								field.getStore().on('load', function (store, records, successful, eOpts) {
									this.delegate.cmfg('onTaskManagerReportFormReportSelect',  {
										merge: true,
										record: store.findRecord(CMDBuild.core.constants.Proxy.TITLE, newValue)
									});
								}, this, { single: true });
							},
							select: function (field, records, eOpts) { // Selected by user
								this.delegate.cmfg('onTaskManagerReportFormReportSelect', { record: records[0] });
							}
						}
					}),
					this.extension = Ext.create('Ext.form.field.ComboBox', { // Prepared for future implementations
						name: CMDBuild.core.constants.Proxy.REPORT_EXTENSION,
						fieldLabel: CMDBuild.Translation.format,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.taskManager.common.ReportForm.getStoreExtension(),
						queryMode: 'local'
					}),
					this.grid = Ext.create('CMDBuild.view.administration.tasks.common.reportForm.GridPanel', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Void}
		 */
		disable: function () {
			this.delegate.cmfg('onTaskManagerReportFormDisable');
		},

		/**
		 * @returns {Void}
		 */
		enable: function () {
			this.delegate.cmfg('onTaskManagerReportFormEnable');
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function () {
			return this.delegate.cmfg('onTaskManagerReportFormIsValid');
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 */
		setValidate: function (state) {
			return this.delegate.cmfg('onTaskManagerReportFormValidateStateSet', state);
		}
	});

})();
