(function () {

	/**
	 * @management
	 */
	Ext.define('CMDBuild.proxy.classes.tabs.Attachment', {

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
		confirm: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		download: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& Ext.isObject(parameters.params) && !Ext.Object.isEmpty(parameters.params)
			) {
				window.open(
					CMDBuild.proxy.index.Json.attachment.download + '?' + Ext.urlEncode(parameters.params),
					'_blank'
				);
			}
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.ATTACHMENT, {
				autoLoad: false,
				model: 'CMDBuild.model.CMAttachment', // TODO: waiting for refactor
				groupField: 'Category',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.attachment.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: 'Category', direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.attachment.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTACHMENT, parameters, true);
		}
	});

})();
