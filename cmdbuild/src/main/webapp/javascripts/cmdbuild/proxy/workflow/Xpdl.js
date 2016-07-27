(function () {

	Ext.define('CMDBuild.proxy.workflow.Xpdl', {

		requires: [
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
		download: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				buildRuntimeForm: true,
				url: CMDBuild.proxy.index.Json.workflow.xpdl.download
			});

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		downloadTemplate: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				buildRuntimeForm: true,
				url: CMDBuild.proxy.index.Json.workflow.xpdl.downloadTemplate
			});

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readVersions: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.workflow.xpdl.versions });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.XPDL, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		upload: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.workflow.xpdl.upload });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		}
	});

})();
