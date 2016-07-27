(function () {

	Ext.define('CMDBuild.controller.administration.report.SqlWindow', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.report.Jasper}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onReportSqlWindowCloseButtonClick'
		],

		/**
		 * @cfg {CMDBuild.model.report.Grid}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.template.ValuesWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.report.Jasper} configurationObject.parentDelegate
		 * @param {CMDBuild.model.report.Grid} configurationObject.record
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.report.sqlWindow.SqlWindow', { delegate: this });

			this.queryValueSet();

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * @returns {Void}
		 */
		onReportSqlWindowCloseButtonClick: function () {
			this.view.close();
		},

		/**
		 * @param {CMDBuild.model.email.template.Variable} record
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		queryValueSet: function () {
			if (Ext.isObject(this.record) && !Ext.Object.isEmpty(this.record))
				this.view.textArea.setValue(this.record.get(CMDBuild.core.constants.Proxy.QUERY));
		}
	});

})();
