(function () {

	Ext.define('CMDBuild.proxy.Menu', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: Ext.emptyFn,

		/**
		 * Read the menu designed for this group. If there are no menu, a default menu is returned. If the configuration of the menu contains some node
		 * but the group has not the privileges to use it this method does not add it to the menu
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.menu.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.MENU, parameters);
		},

		/**
		 * Read the items that are not added to the current menu configuration
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAvailableItems: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.menu.readAvailableItems });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.MENU, parameters);
		},

		/**
		 * Read the full configuration designed for the given group.
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readConfiguration: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.menu.readConfiguration });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.MENU, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.menu.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.MENU, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		save: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.menu.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.MENU, parameters, true);
		}
	});

})();
