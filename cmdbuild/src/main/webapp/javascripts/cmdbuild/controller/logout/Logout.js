(function () {

	Ext.define('CMDBuild.controller.logout.Logout', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.CookiesManager',
			'CMDBuild.proxy.Session'
		],

		/**
		 * @param {Object} configurationObject
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.sessionRemove();
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		sessionRemove: function () {
			if (!CMDBuild.core.CookiesManager.authorizationIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.SESSION] = CMDBuild.core.CookiesManager.authorizationGet();

				CMDBuild.proxy.Session.remove({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						CMDBuild.core.CookiesManager.authorizationClear();

						window.location = 'index.jsp';
					}
				});
			} else {
				window.location = 'index.jsp';
			}
		}
	});

})();
