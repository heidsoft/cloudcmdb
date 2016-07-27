(function() {

	Ext.define('CMDBuild.routes.Base', {
		extend: 'Ext.app.Controller',

		requires: ['CMDBuild.routes.Routes'],

		/**
		 * @param {Object} params
		 *
		 * @return {Boolean}
		 *
		 * @abstract
		 */
		paramsValidation: function(params) {
			_error('unimplemented paramsValidation method', this);
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} path
		 * @param {Object} router
		 */
		saveRoute: function(params, path, router) {
			CMDBuild.routes.Routes.setRoutePath(path);
		}
	});

})();