(function() {

	/**
	 * Adapter class to use CMCardGrid
	 *
	 * TODO: refactor CMCardGrid class to avoid usage of this adapter
	 */
	Ext.define('CMDBuild.controller.common.field.filter.advanced.window.panels.relations.CardGridAdapter', {
		extend: 'CMDBuild.controller.management.common.CMCardGridController',

		mixins: {
			cardGridDelegate: 'CMDBuild.view.management.common.CMCardGridDelegate'
		},

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.panels.relations.Relations}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.relations.CardGridPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} parameters
		 */
		constructor: function(parameters) {
			Ext.apply(this, parameters); // Apply view and parentDelegate to this class

			this.callParent([parameters.view, parameters.parentDelegate]);

			this.view.addDelegate(this);
		},

		/**
		 * This subclass does not refer to the global state
		 *
		 * @override
		 */
		buildStateDelegate: Ext.emptyFn,

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 *
		 * @override
		 */
		onCMCardGridDeselect: function(grid, record) {
			this.parentDelegate.cmfg('onFieldFilterAdvancedWindowRelationsCardSelectionChange');
		},

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 *
		 * @override
		 */
		onCMCardGridSelect: function(grid, record) {
			this.parentDelegate.cmfg('onFieldFilterAdvancedWindowRelationsCardSelectionChange');
		},

		/**
		 * @override
		 */
		onCardSelected: Ext.emptyFn,

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 *
		 * @override
		 */
		onCMCardGridLoad: function(grid) {
			this.parentDelegate.cmfg('onFieldFilterAdvancedWindowRelationsCardGridLoad');
		}
	});

})();