(function () {

	/**
	 * Placeholder class to build empty form if no widget is selected from grid
	 */
	Ext.define('CMDBuild.controller.administration.widget.form.Empty', {
		extend: 'CMDBuild.controller.administration.widget.form.Abstract',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.Widget}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.widget.form.WorkflowPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.widget.Widget} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.widget.form.WorkflowPanel', { delegate: this });
		}
	});

})();
