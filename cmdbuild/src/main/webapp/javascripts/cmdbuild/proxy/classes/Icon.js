(function () {

	/**
	 * REST proxy
	 *
	 * FIXME: future refactor for a correct implementation
	 */
	Ext.define('CMDBuild.proxy.classes.Icon', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.interfaces.Rest',
			'CMDBuild.proxy.index.Rest'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		createImage: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: 'services/json/file/upload?'
					+ CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY
					+ '=' + Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY) // Headers not supported in form submit
			});

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		getFolders: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				method: 'GET',
				url: CMDBuild.proxy.index.Rest.fileStores + '/images/folders/' // TODO: images is custom fileStore
			});

			CMDBuild.core.interfaces.Rest.request(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAllIcons: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				method: 'GET',
				url: CMDBuild.proxy.index.Rest.icons + '/'
			});

			CMDBuild.core.interfaces.Rest.request(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			// Icon delete
			CMDBuild.core.interfaces.Rest.request({
				method: 'DELETE',
				url: CMDBuild.proxy.index.Rest.icons + '/' + parameters.restUrlParams['iconId'] + '/',
				scope: Ext.isEmpty(parameters.scope) ? this : parameters.scope,
				success: function (response, options, decodedResponse) {
					parameters = Ext.isEmpty(parameters) ? {} : parameters;

					// Image delete
					Ext.apply(parameters, {
						method: 'DELETE',
						url: CMDBuild.proxy.index.Rest.fileStores + '/images/folders/' // TODO: images is custom fileStore
							+ parameters.restUrlParams['folderId']
							+ '/files/'
							+ parameters.restUrlParams['imageId'] + '/'
					});

					CMDBuild.core.interfaces.Rest.request(parameters);
				}
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				method: 'POST',
				url: CMDBuild.proxy.index.Rest.icons + '/'
			});

			CMDBuild.core.interfaces.Rest.request(parameters);
		}
	});

})();
