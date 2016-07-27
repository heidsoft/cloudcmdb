(function () {

	Ext.define('CMDBuild.view.management.common.widgets.linkCards.map.CMMiniCardGridDelegate', {

		constructor: function (config) {
			Ext.apply(this, config);

			return this.callParent(arguments);
		},
		/**
		 * @param {CMDBuild.view.management.common.widgets.linkCards.map.CMMiniCardGridDelegate} grid This grid
		 */
		miniCardGridDidActivate: Ext.empfyFn,

		/**
		 * @param {CMDBuild.view.management.common.widgets.linkCards.map.CMMiniCardGridDelegate} grid This grid
		 * @param {object} card An object with Id e IdClass for the card
		 */
		miniCardGridWantOpenCard: Ext.emptyFn,

		/**
		 * @param {CMDBuild.view.management.common.widgets.linkCards.map.CMMiniCardGridDelegate} grid This grid
		 * @param {Ext.data.Model} record the recrod that was selected
		 */
		miniCardGridItemSelected: Ext.emptyFn
	});

})();
