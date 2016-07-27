(function () {

	Ext.define('CMDBuild.core.CookiesManager', {

		requires: ['CMDBuild.core.constants.Proxy'],

		singleton: true,

		// Authorization cookie manage methods
			/**
			 * @returns {Void}
			 *
			 * @public
			 */
			authorizationClear: function () {
				Ext.util.Cookies.set( // Reset cookie value
					CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY,
					null,
					null,
					Ext.Array.slice(window.location.pathname.split('/'), 0, -1).join('/') + '/'
				);

				Ext.util.Cookies.clear(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY); // Sets expiration date to past
			},

			/**
			 * @returns {Void}
			 *
			 * @public
			 */
			authorizationExpirationUpdate: function () {
				if (!CMDBuild.core.CookiesManager.authorizationIsEmpty()) {
					var expirationDate = null;

					if (!Ext.isEmpty(CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.SESSION_TIMEOUT))) {
						expirationDate = new Date();
						expirationDate.setSeconds(expirationDate.getSeconds() + CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.SESSION_TIMEOUT))
					}

					Ext.util.Cookies.set(
						CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY,
						CMDBuild.core.CookiesManager.authorizationGet(),
						expirationDate,
						Ext.Array.slice(window.location.pathname.split('/'), 0, -1).join('/') + '/'
					);
				}
			},

			/**
			 * @returns {String}
			 *
			 * @public
			 */
			authorizationGet: function () {
				return Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY);
			},

			/**
			 * @returns {Boolean}
			 *
			 * @public
			 */
			authorizationIsEmpty: function () {
				return (
					Ext.isEmpty(CMDBuild.core.CookiesManager.authorizationGet())
					|| CMDBuild.core.CookiesManager.authorizationGet() == 'null'
				);
			},

			/**
			 * @param {String} sessionId
			 *
			 * @returns {Void}
			 *
			 * @public
			 */
			authorizationSet: function (sessionId) {
				if (!Ext.isEmpty(sessionId) || sessionId != 'null')
					return Ext.util.Cookies.set(
						CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY,
						sessionId,
						null,
						Ext.Array.slice(window.location.pathname.split('/'), 0, -1).join('/') + '/'
					);

				return _error('empty session id parameter', this, sessionId);
			}
	});

})();
