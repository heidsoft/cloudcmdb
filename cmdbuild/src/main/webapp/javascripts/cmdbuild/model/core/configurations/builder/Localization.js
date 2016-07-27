(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (configurations + translate)
	 */
	Ext.define('CMDBuild.model.core.configurations.builder.Localization', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DEFAULT_LANGUAGE, type: 'string', defaultValue: 'en' },
			{ name: CMDBuild.core.constants.Proxy.ENABLED_LANGUAGES, type: 'auto' }, // CMDBuild enabled languages
			{ name: CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT, type: 'boolean', defaultValue: true }, // Login prompt for language
			{ name: CMDBuild.core.constants.Proxy.LANGUAGES, type: 'auto' } // All CMDBuild languages
		],

		statics: {
			/**
			 * Static function to convert from legacy object to model's one
			 *
			 * @param {Object} data
			 *
			 * @returns {Object} data
			 */
			convertFromLegacy: function (data) { // TODO: remove on refactor
				data = data || {};
				data[CMDBuild.core.constants.Proxy.DEFAULT_LANGUAGE] = data[CMDBuild.core.constants.Proxy.LANGUAGE];

				return data;
			}
		},

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function (data) {
			data = this.statics().convertFromLegacy(data);

			this.callParent(arguments);
		},

		/**
		 * @returns {Object}
		 */
		getAllLanguages: function () {
			return this.get(CMDBuild.core.constants.Proxy.LANGUAGES) || {};
		},

		/**
		 * @returns {Object}
		 */
		getEnabledLanguages: function () {
			var enabledLanguages = {};

			Ext.Array.each(this.get(CMDBuild.core.constants.Proxy.ENABLED_LANGUAGES), function (languageTag, i, allLanguageTag) {
				enabledLanguages[languageTag] = this.getLanguageObject(languageTag);
			},this);

			return enabledLanguages;
		},

		/**
		 * @param {String} languageTag
		 *
		 * @returns {CMDBuild.model.localization.Localization} or null
		 */
		getLanguageObject: function (languageTag) {
			if (this.isManagedLanguage(languageTag))
				return this.get(CMDBuild.core.constants.Proxy.LANGUAGES)[languageTag];

			return null;
		},

		/**
		 * @returns {Boolean}
		 */
		hasEnabledLanguages: function () {
			var enabledLanguages = this.get(CMDBuild.core.constants.Proxy.ENABLED_LANGUAGES);

			return Ext.isArray(enabledLanguages) && enabledLanguages.length > 0;
		},

		/**
		 * @param {String} languageTag
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isManagedLanguage: function (languageTag) {
			return this.get(CMDBuild.core.constants.Proxy.LANGUAGES).hasOwnProperty(languageTag);
		},

		/**
		 * @param {String} fieldName
		 * @param {Object} newValue
		 *
		 * @returns {Array}
		 *
		 * @override
		 */
		set: function (fieldName, newValue) {
			switch (fieldName) {
				case CMDBuild.core.constants.Proxy.ENABLED_LANGUAGES: {
					var decodedArray = [];
					var enabledLanguages = [];

					// TODO: waiting for refactor (saving on server an array not a string)
					if (Ext.isString(newValue)) {
						var splitted = newValue.split(', ');

						if (Ext.isArray(splitted) && splitted.length > 0)
							decodedArray = splitted;
					} else if (Ext.isArray(newValue) && newValue.length > 0) {
						decodedArray = newValue;
					}

					// Build languages with localizations
					Ext.Array.each(decodedArray, function (languageTag, i, allLanguageTag) {
						if (this.isManagedLanguage(languageTag))
							enabledLanguages.push(languageTag);
					}, this);

					return this.callParent([fieldName, enabledLanguages]);
				}

				case CMDBuild.core.constants.Proxy.DEFAULT_LANGUAGE: {
					if (!Ext.isEmpty(newValue) && this.isManagedLanguage(newValue))
						return this.callParent(arguments);

					return _error('empty language tag', this);
				}

				case CMDBuild.core.constants.Proxy.LANGUAGES: {
					var languagesObjectsArray = {};

					if (Ext.isArray(newValue)) {
						Ext.Array.each(newValue, function (language, i, allLanguages) {
							languagesObjectsArray[language[CMDBuild.core.constants.Proxy.TAG]] = Ext.create('CMDBuild.model.localization.Localization', language);
						}, this);

						return this.callParent([fieldName, languagesObjectsArray]);
					} else {
						_error('wrong languages array format', this);
					}
				}

				default:
					return this.callParent(arguments);
			}
		}
	});

})();
