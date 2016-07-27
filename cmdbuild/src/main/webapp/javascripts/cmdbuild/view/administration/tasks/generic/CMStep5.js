(function () {

	Ext.define('CMDBuild.view.administration.tasks.generic.CMStep5Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormGenericController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.generic.CMStep5}
		 */
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @overwrite
		 */
		cmOn: function (name, param, callBack) {
			switch (name) {
				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			/**
			 * @return {CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController} delegate
			 */
			getReportDelegate: function () {
				return this.view.reportForm.delegate;
			},

			/**
			 * @return {Object}
			 */
			getValueReportAttributeGrid: function () {
				return this.getReportDelegate().getValueGrid();
			},

			/**
			 * @return {Boolean}
			 */
			getValueReportFieldsetCheckbox: function () {
				return this.view.reportFieldset.checkboxCmp.getValue();
			},

		// SETters functions
			/**
			 * @param {Object} value
			 */
			setValueReportAttributesGrid: function (value) {
				this.getReportDelegate().setValueGrid(value);
			},

			/**
			 * @param {String} value
			 */
			setValueReportCombo: function (value) {
				this.getReportDelegate().setValueCombo(value);
			},

			/**
			 * @param {String} value
			 */
			setValueReportExtension: function (value) {
				this.getReportDelegate().setValueExtension(value);
			},

			/**
			 * @param {Boolean} state
			 */
			setValueReportFieldsetCheckbox: function (state) {
				if (state) {
					this.view.reportFieldset.expand();
				} else {
					this.view.reportFieldset.collapse();
				}
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.generic.CMStep5', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.generic.CMStep5Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.FieldSet}
		 */
		reportFieldset: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.reportForm.CMReportForm}
		 */
		reportForm: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.generic.CMStep5Delegate', this);

			Ext.apply(this, {
				items: [
					this.reportFieldset = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.report,
						checkboxName: CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVE,
						checkboxToggle: true,
						collapsed: true,
						collapsible: true,
						toggleOnTitleClick: true,
						overflowY: 'auto',

						items: [
							this.reportForm = Ext.create('CMDBuild.view.administration.tasks.common.reportForm.ReportFormView')
						],

						listeners: {
							scope: this,
							expand: function (field, eOpts) {
								this.reportForm.enable();
							}
						}
					})
				]
			});

			this.reportFieldset.fieldWidthsFix();

			this.callParent(arguments);
		}
	});

})();
