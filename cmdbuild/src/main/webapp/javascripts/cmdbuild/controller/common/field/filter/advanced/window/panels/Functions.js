(function() {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.window.panels.Functions', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldFilterAdvancedWindowFunctionsGetData',
			'onFieldFilterAdvancedWindowFunctionsSetData = onFieldFilterAdvancedWindowSetData',
			'onFieldFilterAdvancedWindowFunctionsShow',
			'onFieldFilterAdvancedWindowFunctionsTabBuild'
		],

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.functions.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.functions.FunctionsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.window.Window} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.buildView();
		},

		/**
		 * @returns {CMDBuild.view.common.field.filter.advanced.window.panels.functions.FunctionsView}
		 */
		buildView: function() {
			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.functions.FunctionsView', { delegate: this });

			// Shorthands
			this.form = this.view.form;

			return this.view;
		},

		/**
		 * @returns {Object} out
		 */
		onFieldFilterAdvancedWindowFunctionsGetData: function() {
			var out = {};

			if (!Ext.isEmpty(this.form.functionComboBox.getValue())) {
				out[CMDBuild.core.constants.Proxy.FUNCTIONS] = [];

				var filterObject = {};
				filterObject[CMDBuild.core.constants.Proxy.NAME] = this.form.functionComboBox.getValue();

				out[CMDBuild.core.constants.Proxy.FUNCTIONS].push(filterObject);
			}

			return out;
		},

		/**
		 * @param {CMDBuild.model.common.field.filter.advanced.Filter} filter
		 */
		onFieldFilterAdvancedWindowFunctionsSetData: function(filter) {
			var filterConfigurationObject = filter.get(CMDBuild.core.constants.Proxy.CONFIGURATION);

			this.viewReset();

			if (
				!Ext.isEmpty(filterConfigurationObject)
				&& !Ext.Object.isEmpty(filterConfigurationObject[CMDBuild.core.constants.Proxy.FUNCTIONS])
			) {
				var functionObject = filterConfigurationObject[CMDBuild.core.constants.Proxy.FUNCTIONS];

				if (Ext.isArray(functionObject) && !Ext.isEmpty(functionObject))
					this.form.functionComboBox.setValue(functionObject[0][CMDBuild.core.constants.Proxy.NAME]);
			}
		},

		onFieldFilterAdvancedWindowFunctionsShow: function() {
			if (!this.cmfg('fieldFilterAdvancedFilterIsEmpty')) {
				this.form.functionComboBox.getStore().load({
					scope: this,
					callback: function(records, operation, success) {
						this.onFieldFilterAdvancedWindowFunctionsSetData(this.cmfg('fieldFilterAdvancedFilterGet'));
					}
				});
			}
		},

		/**
		 * Builds tab from filter value (preset values and add)
		 */
		onFieldFilterAdvancedWindowFunctionsTabBuild: function() {
			if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'function'))
				this.cmfg('fieldFilterAdvancedWindowAddTab', this.buildView());
		},

		viewReset: function() {
			this.form.functionComboBox.setValue();
		}
	});

})();