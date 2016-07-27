(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * Configuration model used in main CMDBuild configuration procedure
	 */
	Ext.define('CMDBuild.model.configure.Configure', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ADMINISTRATOR_PASSWORD, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ADMINISTRATOR_USER_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CONNECTION_HOST, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CONNECTION_PASSWORD, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CONNECTION_PORT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CONNECTION_USER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CREATE_SHARK_SCHEMA, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DATABASE_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DATABASE_TYPE, type: 'string', defaultValue: 'empty' },
			{ name: CMDBuild.core.constants.Proxy.DATABASE_USER_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DATABASE_USER_PASSWORD, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DATABASE_USER_PASSWORD, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DATABASE_USER_TYPE, type: 'string', defaultValue: 'superuser' },
			{ name: CMDBuild.core.constants.Proxy.LANGUAGE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT, type: 'boolean' }
		],

		/**
		 * Returns data for submit based on database type property (converts to legacy names)
		 *
		 * @returns {Object}
		 */
		getDataSubmit: function () {
			switch (this.get(CMDBuild.core.constants.Proxy.DATABASE_TYPE)) {
				case CMDBuild.core.constants.Proxy.DEMO:
					return this.getDataSubmitDemo();

				case CMDBuild.core.constants.Proxy.EMPTY:
					return this.getDataSubmitEmpty();

				case CMDBuild.core.constants.Proxy.EXISTING:
					return this.getDataSubmitExisting();

				default:
					_error('unmanaged database type "' + formDataModel.get(CMDBuild.core.constants.Proxy.DATABASE_TYPE) + '"', this);
			}
		},

		/**
		 * @returns {Object} data
		 *
		 * @private
		 */
		getDataSubmitDemo: function () {
			var data = {
				db_name: this.get(CMDBuild.core.constants.Proxy.DATABASE_NAME),
				db_type: this.get(CMDBuild.core.constants.Proxy.DATABASE_TYPE),
				host: this.get(CMDBuild.core.constants.Proxy.CONNECTION_HOST),
				language: this.get(CMDBuild.core.constants.Proxy.LANGUAGE),
				language_prompt: this.get(CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT),
				password: this.get(CMDBuild.core.constants.Proxy.CONNECTION_PASSWORD),
				port: this.get(CMDBuild.core.constants.Proxy.CONNECTION_PORT),
				shark_schema: this.get(CMDBuild.core.constants.Proxy.CREATE_SHARK_SCHEMA),
				user: this.get(CMDBuild.core.constants.Proxy.CONNECTION_USER),
				user_type: this.get(CMDBuild.core.constants.Proxy.DATABASE_USER_TYPE)
			};

			if (this.get(CMDBuild.core.constants.Proxy.DATABASE_USER_TYPE) != 'superuser') {
				data = Ext.Object.merge(data, {
					lim_password: this.get(CMDBuild.core.constants.Proxy.DATABASE_USER_PASSWORD),
					lim_user: this.get(CMDBuild.core.constants.Proxy.DATABASE_USER_NAME)
				});
			}

			return data;
		},

		/**
		 * @returns {Object} data
		 *
		 * @private
		 */
		getDataSubmitEmpty: function () {
			var data = {
				admin_password: this.get(CMDBuild.core.constants.Proxy.ADMINISTRATOR_PASSWORD),
				admin_user: this.get(CMDBuild.core.constants.Proxy.ADMINISTRATOR_USER_NAME),
				db_name: this.get(CMDBuild.core.constants.Proxy.DATABASE_NAME),
				db_type: this.get(CMDBuild.core.constants.Proxy.DATABASE_TYPE),
				host: this.get(CMDBuild.core.constants.Proxy.CONNECTION_HOST),
				language: this.get(CMDBuild.core.constants.Proxy.LANGUAGE),
				language_prompt: this.get(CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT),
				password: this.get(CMDBuild.core.constants.Proxy.CONNECTION_PASSWORD),
				port: this.get(CMDBuild.core.constants.Proxy.CONNECTION_PORT),
				shark_schema: this.get(CMDBuild.core.constants.Proxy.CREATE_SHARK_SCHEMA),
				user: this.get(CMDBuild.core.constants.Proxy.CONNECTION_USER),
				user_type: this.get(CMDBuild.core.constants.Proxy.DATABASE_USER_TYPE)
			};

			if (this.get(CMDBuild.core.constants.Proxy.DATABASE_USER_TYPE) != 'superuser') {
				data = Ext.Object.merge(data, {
					lim_password: this.get(CMDBuild.core.constants.Proxy.DATABASE_USER_PASSWORD),
					lim_user: this.get(CMDBuild.core.constants.Proxy.DATABASE_USER_NAME)
				});
			}

			return data;
		},

		/**
		 * @returns {Object}
		 *
		 * @private
		 */
		getDataSubmitExisting: function () {
			return {
				db_name: this.get(CMDBuild.core.constants.Proxy.DATABASE_NAME),
				db_type: this.get(CMDBuild.core.constants.Proxy.DATABASE_TYPE),
				host: this.get(CMDBuild.core.constants.Proxy.CONNECTION_HOST),
				language: this.get(CMDBuild.core.constants.Proxy.LANGUAGE),
				language_prompt: this.get(CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT),
				password: this.get(CMDBuild.core.constants.Proxy.CONNECTION_PASSWORD),
				port: this.get(CMDBuild.core.constants.Proxy.CONNECTION_PORT),
				user: this.get(CMDBuild.core.constants.Proxy.CONNECTION_USER)
			};
		},

		/**
		 * @returns {Object} data
		 */
		getDataDBConnection: function () {
			var data = {};
			data[CMDBuild.core.constants.Proxy.HOST] = this.get(CMDBuild.core.constants.Proxy.CONNECTION_HOST);
			data[CMDBuild.core.constants.Proxy.PASSWORD] = this.get(CMDBuild.core.constants.Proxy.CONNECTION_PASSWORD);
			data[CMDBuild.core.constants.Proxy.PORT] = this.get(CMDBuild.core.constants.Proxy.CONNECTION_PORT);
			data[CMDBuild.core.constants.Proxy.USER] = this.get(CMDBuild.core.constants.Proxy.CONNECTION_USER);

			return data;
		}
	});

})();
