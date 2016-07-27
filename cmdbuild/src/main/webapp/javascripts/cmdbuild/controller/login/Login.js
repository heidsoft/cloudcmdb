(function () {

	Ext.define('CMDBuild.controller.login.Login', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.CookiesManager',
			'CMDBuild.proxy.Session'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLoginViewportLoginButtonClick',
			'onLoginViewportUserChange'
		],

		/**
		 * @property {CMDBuild.view.login.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.login.LoginViewport}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.login.LoginViewport', { delegate: this });

			// Shorthands
			this.form = this.view.formContainer.form;

			this.setupFields();
		},

		/**
		 * @returns {Void}
		 */
		onLoginViewportLoginButtonClick: function () {
			if (this.form.group.isHidden()) {
				this.sessionCreate();
			} else {
				this.sessionUpdate();
			}
		},

		/**
		 * @returns {Void}
		 */
		onLoginViewportUserChange: function () {
			this.setupFieldsGroup();
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		sectionRedirect: function () {
			if (/administration.jsp$/.test(window.location)) {
				window.location = 'administration.jsp' + window.location.hash;
			} else {
				window.location = 'management.jsp' + window.location.hash;
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
				params[CMDBuild.core.constants.Proxy.USERNAME] = this.form.user.getValue();

				CMDBuild.proxy.Session.create({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						CMDBuild.core.CookiesManager.authorizationSet(decodedResponse[CMDBuild.core.constants.Proxy.SESSION_ID]);

						if (Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.GROUP])) { // Group to be selected
							CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.USERNAME, this.form.user.getValue());
							CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.GROUPS, decodedResponse[CMDBuild.core.constants.Proxy.GROUPS]);

							this.setupFields();
						} else { // Succesfully logged
							this.sectionRedirect();
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
			if (this.validate(this.form)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.GROUP] = this.form.group.getValue();
				params[CMDBuild.core.constants.Proxy.SESSION] = CMDBuild.core.CookiesManager.authorizationGet();

				CMDBuild.proxy.Session.update({
					params: params,
					scope: this,
					success: this.sectionRedirect
				});
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		setupFields: function () {
			if (!Ext.isEmpty(CMDBuild.configuration.runtime) && Ext.isEmpty(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME))) {
				this.form.user.focus();
			} else {
				this.form.user.setValue(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME));
				this.form.user.disable();

				this.form.password.hide();
				this.form.password.disable();
			}

			this.setupFieldsGroup();
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		setupFieldsGroup: function () {
			if (
				!Ext.isEmpty(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.GROUPS))
				&& Ext.isArray(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.GROUPS))
			) {
				this.form.group.getStore().loadData(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.GROUPS));

				this.form.group.show();
				this.form.group.focus();
			} else {
				this.form.group.hide();
			}
		}
	});

})();
