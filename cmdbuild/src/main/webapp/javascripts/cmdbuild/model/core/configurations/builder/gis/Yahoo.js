(function () {

	Ext.require([
		'CMDBuild.core.constants.Proxy',
		'CMDBuild.core.Utils'
	]);

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.core.configurations.builder.gis.Yahoo', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ENABLED, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.KEY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ZOOM_MAX, type: 'int', defaultValue: 24 },
			{ name: CMDBuild.core.constants.Proxy.ZOOM_MIN, type: 'int', defaultValue: 0 }
		],

		statics: {
			/**
			 * Static function to create translated properties
			 *
			 * @param {Object} data
			 *
			 * @returns {Object}
			 */
			convertFromLegacy: function (data) {
				data = data || {};
				data[CMDBuild.core.constants.Proxy.ENABLED] = CMDBuild.core.Utils.decodeAsBoolean(data['yahoo']);
				data[CMDBuild.core.constants.Proxy.KEY] = data['yahoo_key'];
				data[CMDBuild.core.constants.Proxy.ZOOM_MAX] = data['yahoo_maxzoom'];
				data[CMDBuild.core.constants.Proxy.ZOOM_MIN] = data['yahoo_minzoom'];

				return data;
			}
		},

		/**
		 * @param {Object} data
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (data) {
			data = CMDBuild.model.core.configurations.builder.gis.Yahoo.convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();
