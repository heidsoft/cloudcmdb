(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.widget.linkCards.Selection', {
		extend: 'Ext.util.Observable',

		/**
		 * @property {Boolean}
		 */
		_freezed: {},

		/**
		 * @property {Int}
		 */
		lastSelection: undefined,

		/**
		 * @property {Object}
		 * 	{
		 * 		cardId: {
		 * 			metadata1: value1,
		 * 			metadata2: value2,
		 * 			...
		 * 		},
		 * 		...
		 * 	}
		 */
		selections: {},

		/**
		 * @cfg {Boolean}
		 */
		singleSelect: undefined,

		/**
		 * @param {Object} configuration - { singleSelect: true/false }
		 *
		 * @override
		 */
		constructor: function (configuration) {
			configuration = configuration || {};

			this.singleSelect = configuration[CMDBuild.core.constants.Proxy.SINGLE_SELECT];

			this.addEvents({
				'select': true,
				'deselect': true
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Int} selection - card id
		 */
		deselect: function (selection) {
			if (!this._silent)
				if (this.isSelected(selection)) {
					delete this.selections[selection];

					this.fireEvent('deselect', selection);
				}

			if (Ext.Object.isEmpty(this.selections))
				this.lastSelection = undefined;
		},

		defreeze: function () {
			this.selections = Ext.apply({}, this._freezed);
		},

		freeze: function () {
			this._freezed = Ext.apply({}, this.selections);
		},

		/**
		 * @return {Int} cardId of last selection
		 */
		getLastSelection: function () {
			return this.lastSelection || null;
		},

		/**
		 * @return {Array} selections - each element is a cardId
		 */
		getSelections: function () {
			return this.selections;
		},

		/**
		 * @return {Boolean}
		 */
		hasSelection: function () {
			return Ext.Object.getKeys(this.getSelections()).length > 0;
		},

		/**
		 * @param {Int} selection - card id
		 */
		isSelected: function (selection) {
			return this.selections.hasOwnProperty(selection);
		},

		reset: function () {
			for (var selection in this.selections)
				this.deselect(selection);

			this.selections = {}; // TODO: find right way - Hack to fix problems with this.selections object
		},

		/**
		 * @param {Int} selection - card id
		 * @param {Object} metadata
		 */
		select: function (selection, metadata) {
			metadata = metadata || {};

			if (!this._silent && !this.isSelected(selection)) {
				if (this.singleSelect)
					this.reset();

				this.selections[selection] = metadata;
				this.lastSelection = selection;

				this.fireEvent('select', selection);
			}
		}
	});

})();
