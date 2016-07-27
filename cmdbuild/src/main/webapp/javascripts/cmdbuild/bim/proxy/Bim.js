(function () {

	Ext.define('CMDBuild.bim.proxy.Bim', {

		requires: [
			'CMDBuild.bim.data.CMBIMProjectModel',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		activeForClassName: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.activeForClassName });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters);
		},

		/**
		 * FIXME: use common declaration with object parameter
		 */
		create: function (form, params, success, failure) {
			CMDBuild.core.interfaces.FormSubmit.submit({
				form: form,
				url: CMDBuild.proxy.index.Json.bim.create,
				params: params,
				fileUpload: true,
				success: success,
				failure: failure
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		disable: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.disable });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		enable: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.enable });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		fetchCardFromViewewId: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.fetchCardFromViewewId });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters);
		},

		/**
		 * The ROID of the project to download
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		fetchJsonForBimViewer: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.fetchJsonForBimViewer });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.BIM, {
				autoLoad: false,
				model: 'CMDBuild.bim.data.CMBIMProjectModel',
				defaultPageSize: 0, // Disable paging
				pageSize: 0, // Disable paging
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.bim.read,
					actionMethods: 'GET',
					reader: {
						type: 'json',
						root: 'bimProjects'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		roidForCardId: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.roidForCardId });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters);
		},

		/**
		 * FIXME: use common declaration with object parameter
		 */
		update: function (form, params, success, failure) {
			CMDBuild.core.interfaces.FormSubmit.submit({
				form: form,
				url: CMDBuild.proxy.index.Json.bim.update,
				params: params,
				fileUpload: true,
				success: success,
				failure: failure
			});
		}
	});

})();
