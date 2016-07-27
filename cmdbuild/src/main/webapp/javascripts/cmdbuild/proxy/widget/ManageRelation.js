(function () {

	Ext.define('CMDBuild.proxy.widget.ManageRelation', {

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
		readAllRelations: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.relation.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		removeCard: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;
			parameters['important'] = true;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.card.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CARD, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		removeRelation: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.relation.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters, true);
		}
	});

})();
