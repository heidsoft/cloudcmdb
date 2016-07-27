(function () {

	/**
	 * @deprecated (old cache system is dismissed)
	 */
	Ext.define('CMDBuild.proxy.Cache', {

		requires: [
			'CMDBuild.cache.CMReferenceStoreModel',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.cache.LookupFieldStore'
		],

		singleton: true,

		/**
		 * @param {Object} baseParams
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreForeignKey: function (baseParams) {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: true,
				model: 'CMDBuild.cache.CMReferenceStoreModel',
				baseParams: baseParams, // Retro-compatibility
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT),
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.card.getListShort,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					},
					extraParams: baseParams
				},
				sorters: [
					{ property: 'Description', direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {String} type
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreLookup: function (type) {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: true,
				model: 'CMDBuild.model.cache.LookupFieldStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.lookup.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					},
					extraParams: {
						type: type,
						active: true,
						short: true
					},
					actionMethods: 'POST' // Lookup types can have UTF-8 names  not handled correctly
				},
				sorters: [
					{ property: 'Number', direction: 'ASC' },
					{ property: 'Description', direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Boolean} isOneTime
		 * @param {Object} baseParams
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store} store
		 */
		getStoreReference: function (isOneTime, baseParams) {
			var store = CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: !isOneTime,
				model: 'CMDBuild.cache.CMReferenceStoreModel',
				isOneTime: isOneTime,
				baseParams: baseParams, // Retro-compatibility,
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT),
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.card.readAllShort,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					},
					extraParams: baseParams
				},
				sorters: [
					{ property: 'Description', direction: 'ASC' }
				]
			});

			// Disable filter method to avoid filter parameter stack witch drive to get url overflow error (reason is unknown)
			if (baseParams.NoFilter)
				store.filter = Ext.emptyFn;

			return store;
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAttachmentDefinitions: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.attachment.getContext });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTACHMENT, parameters);
		}
	});

})();
