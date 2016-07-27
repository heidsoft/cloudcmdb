(function() {

	Ext.define('Ext.ux.grid.column.Tick', {
		extend: 'Ext.grid.column.Column',

		/**
		 * @cfg {String}
		 */
		iconAltText: 'Tick icon',

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
				return value ? '<img src="images/icons/tick.png" alt="' + this.iconAltText + '" />' : null;

			return value;
		}
	});

})();