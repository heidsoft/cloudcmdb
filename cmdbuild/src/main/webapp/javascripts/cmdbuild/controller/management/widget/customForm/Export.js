(function () {

	Ext.define('CMDBuild.controller.management.widget.customForm.Export', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.customForm.Csv'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetCustomFormExportAbortButtonClick',
			'onWidgetCustomFormExportExportButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.widget.customForm.export.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.widget.customForm.export.ExportWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.widget.customForm.export.ExportWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * @returns {Void}
		 */
		onWidgetCustomFormExportAbortButtonClick: function () {
			this.view.destroy();
		},

		/**
		 * Uses exportCSV calls to build and download file
		 *
		 * @returns {Void}
		 */
		onWidgetCustomFormExportExportButtonClick: function () {
			if (this.validate(this.form)) {
				var gridData = [];

				// Uses direct data property access to avoid a get problem because of generic model
				Ext.Array.each(this.cmfg('widgetCustomFormLayoutDataGet'), function (rowObject, i, allRowObjects) {
					var dataObject = Ext.isEmpty(rowObject.data) ? rowObject : rowObject.data; // Model/Objects management

					new CMDBuild.Management.TemplateResolver({
						clientForm: this.cmfg('widgetCustomFormControllerPropertyGet', 'clientForm'),
						xaVars: dataObject,
						serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
					}).resolveTemplates({
						attributes: Ext.Object.getKeys(dataObject),
						scope: this,
						callback: function (out, ctx) {
							if (Ext.isObject(out))
								gridData.push(out);
						}
					});
				}, this);

				var params = this.form.getData();
				params[CMDBuild.core.constants.Proxy.DATA] = Ext.encode(gridData);
				params[CMDBuild.core.constants.Proxy.HEADERS] = Ext.encode(params[CMDBuild.core.constants.Proxy.HEADERS].split(','));

				CMDBuild.proxy.widget.customForm.Csv.exports({ params: params });

				this.cmfg('onWidgetCustomFormExportAbortButtonClick');
			}
		}
	});

})();
