(function () {

	/**
	 * @link CMDBuild.view.management.common.CMCardGridDelegate
	 */
	Ext.define("CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.relations.CardGridPanelDelegate", {

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 */
		onCMCardGridSelect: function (grid, record) {},

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 */
		onCMCardGridDeselect: function (grid, record) {},

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridBeforeLoad: function (grid) {},

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridLoad: function (grid) {},

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridColumnsReconfigured: function (grid) {},

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridIconRowClick: function (grid, action, model) {}
	});

})();
