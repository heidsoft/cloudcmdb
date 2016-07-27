(function() {

	Ext.define('Ext.ux.grid.column.Active', {
		extend: 'Ext.grid.column.Column',

		/**
		 * @cfg {String}
		 */
		iconAltTextActive: 'Active icon',

		/**
		 * @cfg {String}
		 */
		iconAltTextNotActive: 'Not active icon',

		/**
		 * @param {Object} value
		 * @param {Object} metaData
		 * @param {Ext.data.Model} record
		 * @param {Number} rowIndex
		 * @param {Number} colIndex
		 * @param {Ext.data.Store} store
		 * @param {Ext.view.View} view
		 *
		 * @returns {String}
		 */
		renderer: function(value, metaData, record, rowIndex, colIndex, store, view) {
			if (Ext.isBoolean(value))
				return value ? '<img src="images/icons/accept.png" alt="' + this.iconAltTextActive + '" />' : '<img src="images/icons/cancel.png" alt="' + this.iconAltTextNotActive + '" />';

			return value;
		}
	});

})();