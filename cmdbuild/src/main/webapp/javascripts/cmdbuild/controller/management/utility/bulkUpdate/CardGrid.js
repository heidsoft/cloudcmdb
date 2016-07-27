(function () {

	/**
	 * @legacy
	 */
	Ext.define('CMDBuild.controller.management.utility.bulkUpdate.CardGrid', {
		extend: 'CMDBuild.controller.management.common.CMCardGridController',

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.utility.bulkUpdate.BulkUpdate} configurationObject.parentDelegate
		 * @param {CMDBuild.view.management.utility.bulkUpdate.GridPanel} configurationObject.view
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent([configurationObject.view, configurationObject.parentDelegate]);
		},

		/**
		 * Do nothing: this kind of cardGrid is not binded to the Card module State
		 *
		 * @override
		 */
		buildStateDelegate: Ext.emptyFn,

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onEntryTypeSelected: function (entryType) {
			var me = this;
			if (!entryType) {
				return;
			}

			this.entryType = entryType;
			this.unApplyFilter(this);

			this.view.updateStoreForClassId(entryType.get('id'), {
				cb: function cbUpdateStoreForClassId() {
					me.view.loadPage(1);
				}
			});
		},

		/**
		 * @returns {CMDBuild.cache.CMEntryTypeModel}
		 *
		 * @override
		 */
		getEntryType: function () {
			return this.entryType;
		}
	});

})();
