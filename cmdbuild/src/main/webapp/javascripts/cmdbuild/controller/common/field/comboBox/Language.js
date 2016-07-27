(function () {

	Ext.define('CMDBuild.controller.common.field.comboBox.Language', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldComboBoxLanguageSelect',
			'onFieldComboBoxLanguageStoreLoad',
			'onFieldComboBoxLanguageValueSet'
		],

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Language}
		 */
		view: undefined,

		/**
		 * @param {String} language
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		changeLanguage: function (language) {
			language = !Ext.isEmpty(language) && Ext.isString(language) ? language : CMDBuild.configuration.localization.get(CMDBuild.core.constants.Proxy.DEFAULT_LANGUAGE);

			window.location = '?' + CMDBuild.core.constants.Proxy.LANGUAGE + '=' + language;
		},

		/**
		 * @returns {String}
		 *
		 * @private
		 */
		getCurrentLanguage: function () {
			// Step 1: check URL
			if (!Ext.isEmpty(window.location.search))
				return Ext.Object.fromQueryString(window.location.search)[CMDBuild.core.constants.Proxy.LANGUAGE];

			// Step 2: check CMDBuild configuration (server call for default language configuration)
			if (!Ext.isEmpty(CMDBuild) && !Ext.isEmpty(CMDBuild.configuration) && !Ext.isEmpty(CMDBuild.configuration.localization))
				return CMDBuild.configuration.localization.get(CMDBuild.core.constants.Proxy.DEFAULT_LANGUAGE);
		},

		/**
		 * @param {CMDBuild.model.localization.Localization} selectedRecord
		 *
		 * @returns {Void}
		 */
		onFieldComboBoxLanguageSelect: function (selectedRecord) {
			this.view.setValue(this.view.getValue()); // Fixes flag image render error

			if (this.view.enableChangeLanguage)
				this.changeLanguage(selectedRecord.get(CMDBuild.core.constants.Proxy.TAG));
		},

		/**
		 * @returns {Void}
		 */
		onFieldComboBoxLanguageStoreLoad: function () {
			this.view.setValue(this.getCurrentLanguage());
		},

		/**
		 * @param {String} value
		 *
		 * @returns {Void}
		 */
		onFieldComboBoxLanguageValueSet: function (value) {
			if (this.view.lastFlagCls && !Ext.isEmpty(this.view.inputEl))
				this.view.inputEl.removeCls(this.view.lastFlagCls);

			this.view.lastFlagCls = this.view.iconClsPrefix + value;

			if (!Ext.isEmpty(this.view.inputEl))
				this.view.inputEl.addCls(this.view.lastFlagCls);
		}
	});

})();
