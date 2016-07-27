(function($) {
	var authentication = {
		/**
		 * Authentication variables
		 */
		localstorage_key : 'CMDBuild-Authorization',
		types : {
			BASE : 'base',
			SYSTEM : 'system',
			PORTAL : 'portal',
			URL : 'url'
		},

		authenticationToken : null,
		lockedAuth : false,

		/**
		 * Utility methods
		 */
		getAuthenticationUrl : function() {
			return $.Cmdbuild.global.getApiUrl() + 'sessions/';
		},
		setAuthenticationToken : function(token) {
			this.authenticationToken = token;
		},
		getAuthenticationToken : function() {
			var token = $.Cmdbuild.utilities.readCookie($.Cmdbuild.access_token);
			if (token) {
				this.authenticationToken = token;
			}
			else if (!this.authenticationToken && localStorage && localStorage.getItem(this.localstorage_key)) {
				this.authenticationToken = localStorage.getItem(this.localstorage_key);
			}
			return this.authenticationToken;
		},
		lockAuthentication : function() {
			this.lockedAuth = true;
		},
		unlockAuthentication : function() {
			this.lockedAuth = false;
		},
		isAuthLocked : function() {
			return this.lockedAuth;
		},

		/**
		 * Entry point for autentication
		 */
		authenticate : function() {
			// if authentication is not locked
			if (!this.isAuthLocked()) {
				// lock authentication
				this.lockAuthentication();

				// authenticate
				switch ($.Cmdbuild.global.getAuthenticationConfig().type) {
				case this.types.BASE:
					this.baseAuthentication();
					break;
				case this.types.SYSTEM:
					this.systemAuthentication();
					break;
				case this.types.PORTAL:
					this.portalAuthentication();
					break;
				case this.types.URL:
					this.urlAuthentication();
					break;
				default:
					var error = new $.Cmdbuild.errorsManager.AuthenticationError(
							"Please check authentication config.");
					$.Cmdbuild.errorsManager.popup(error);
				}
			}
		},

		/**
		 * Authentication methods
		 */
		baseAuthentication: function() {
			// TODO: show login form
		},
		systemAuthentication: function() {
			var params = {};
			var authconf = $.Cmdbuild.global.getAuthenticationConfig();
			if (authconf.login && authconf.login.username && authconf.login.password) {
				params.username = authconf.login.username;
				params.password = authconf.login.password;
				this.doAuthenticate(params);
			} else {
				var fullUrlFile = $.Cmdbuild.global.getAppConfigUrl() + "auth.xml";
				$.Cmdbuild.utilities.getXmlDoc(fullUrlFile, this.systemAuthenticationXMLCB, this, {});
			}
		},
		systemAuthenticationXMLCB: function($xmlDoc, parameters) {
			if ($xmlDoc) {
				var root = $xmlDoc.documentElement;
				var $root = $(root);
				var params = {};
				params.username = $root.find(this.types.SYSTEM).find("username").text();
				params.password = $root.find(this.types.SYSTEM).find("password").text();
				this.doAuthenticate(params);
			}
		},
		portalAuthentication: function() {
			// TODO: make portal authentication
			var params = {};
			this.doAuthenticate(params);
		},
		urlAuthentication: function() {
			var authconf = $.Cmdbuild.global.getAuthenticationConfig();
			urlconf = authconf.login.url;

			var me = this;
			// show authentication form
			$.ajax({
				type: urlconf.method ? urlconf.method : "GET",
				url: urlconf.base,
				data: urlconf.params,
				cache : false,
				success: function (data) {
					me.onAuthenticationSuccess(data);
				},
				error: function (XMLHttpRequest, textStatus, errorThrown) {
					var error = new $.Cmdbuild.errorsManager.AuthenticationError();
					$.Cmdbuild.errorsManager.popup(error);
					var errorMessage = "A problem occurred while authenticating. Please contact the administrator.";
					// error popup
					$.Cmdbuild.utilities.popupMessage(
							"Authentication error", errorMessage);
				}
			});
		},

		/**
		 * Authenticate to server. On success calls onAuthenticationSuccess
		 * @param {object} with two attributes: username and password
		 */
		doAuthenticate: function(credentials) {
			var me = this;
			var login_params = JSON.stringify(credentials);
			// show authentication form
			$.ajax({
				type: "POST",
				url: me.getAuthenticationUrl(),
				data: login_params,
				dataType : "json",
				contentType : 'application/json',
				success: function (data) {
					if (data) {
						var token = data.data._id;
						me.onAuthenticationSuccess(token);
					}
				},
				error: function (XMLHttpRequest, textStatus, errorThrown) {
					var error = new $.Cmdbuild.errorsManager.AuthenticationError();
					$.Cmdbuild.errorsManager.popup(error);
				}
			});
		},

		/**
		 * Called after authentication request success.
		 * @param {string} token
		 */
		onAuthenticationSuccess : function(token) {
			this.setAuthenticationToken(token);
			this.unlockAuthentication();
			$.Cmdbuild.utilities.startApplication();
		},

		/**
		 * Use local storage to save authentication token
		 */
		getTokenFromLocalStorage : function() {
			if(typeof(Storage) !== "undefined") {
				return localStorage[this.localstorage_key];
			} else {
				return;
			}
		},
		setTokenToLocalStorage : function(token) {
			if(typeof(Storage) !== "undefined") {
				localStorage[this.localstorage_key] = token;
			}
		}
	};
	$.Cmdbuild.authentication = authentication;
})(jQuery);