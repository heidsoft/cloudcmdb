(function () {

	Ext.define('CMDBuild.controller.common.sessionExpired.SessionExpired', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.CookiesManager',
			'CMDBuild.proxy.Session'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		ajaxParameters: {},

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onSessionExpiredChangeUserButtonClick = onSessionExpiredConfirmButtonClick',
			'onSessionExpiredLoginButtonClick'
		],

		/**
		 * @cfg {Boolean}
		 */
		passwordFieldEnable: true,

		/**
		 * @property {CMDBuild.view.common.sessionExpired.SessionExpiredWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.sessionExpired.SessionExpiredWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;

			this.form.password.setDisabled(!this.passwordFieldEnable);

			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * @returns {Void}
		 */
		onSessionExpiredChangeUserButtonClick: function () {
			window.location = '.';
		},

		/**
		 * @returns {Void}
		 */
		onSessionExpiredLoginButtonClick: function () {
			this.view.hide();

			if (this.form.group.isHidden()) {
				this.sessionCreate();
			} else {
				this.sessionUpdate();
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		sessionCreate: function () {
			if (this.validate(this.form)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.PASSWORD] = this.form.password.getValue();
				params[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME);

				CMDBuild.proxy.Session.create({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						CMDBuild.core.CookiesManager.authorizationSet(decodedResponse[CMDBuild.core.constants.Proxy.SESSION_ID]);

						Ext.create('CMDBuild.core.Data'); // Reset connections header setup

						if (Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.GROUP])) { // Group to be selected
							var group = Ext.Array.findBy(decodedResponse[CMDBuild.core.constants.Proxy.GROUPS], function (item, index) {
								return item[CMDBuild.core.constants.Proxy.NAME] == CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME);
							}, this);

							if (!Ext.isEmpty(group)) { // Group is available
								this.sessionUpdate();
							} else { // Previous logged groups is no more available, enable group selection combo and show window
								this.form.group.getStore().loadData(decodedResponse[CMDBuild.core.constants.Proxy.GROUPS]);

								this.form.password.hide();
								this.form.group.show();

								this.view.show();
							}
						} else { // Successfully logged
							this.view.hide();

							if (Ext.Object.isEmpty(this.ajaxParameters)) {
								window.location.reload();
							} else {
								CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, this.ajaxParameters);
							}
						}
					}
				});
			}
		},

		/**
		 * Update session with selected group
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		sessionUpdate: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.SESSION] = CMDBuild.core.CookiesManager.authorizationGet();

			if (this.form.group.isHidden()) {
				params[CMDBuild.core.constants.Proxy.GROUP] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME);
			} else if (!Ext.isEmpty(this.form.group.getValue())) {
				params[CMDBuild.core.constants.Proxy.GROUP] = this.form.group.getValue();
			} else {
				return this.onSessionExpiredChangeUserButtonClick();
			}

			CMDBuild.proxy.Session.update({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					window.location.reload();
				}
			});
		}
	});

})();
