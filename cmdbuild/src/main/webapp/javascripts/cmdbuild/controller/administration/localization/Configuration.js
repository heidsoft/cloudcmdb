(function () {

	Ext.define('CMDBuild.controller.administration.localization.Configuration', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.configuration.GeneralOptions',
			'CMDBuild.proxy.localization.Export',
			'CMDBuild.proxy.localization.Import'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.Localization}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLocalizationConfigurationAbortButtonClick',
			'onLocalizationConfigurationSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.localization.ConfigurationPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localization.Localization} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localization.ConfigurationPanel', { delegate: this });

			this.configurationRead();
		},

		/**
		 * Uses configuration module proxy until configurations refactor
		 *
		 * TODO: refactor to use loadData methods
		 *
		 * @private
		 */
		configurationRead: function () {
			CMDBuild.proxy.configuration.GeneralOptions.read({
				scope: this,
				success: function (response, options, decodedResponse) {
					var decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					this.view.languagePromptCheckbox.setValue(decodedResponse['languageprompt']);
					this.view.enabledLanguagesGrid.setValue(decodedResponse['enabled_languages'].split(', ')); // TODO: delete on server configuration refactor
					this.view.defaultLanguageCombobox.setValue(decodedResponse['language']); // Must be before enabledLanguagesGrid to avoid check errors
				}
			});
		},

		onLocalizationConfigurationAbortButtonClick: function () {
			this.configurationRead();
		},

		/**
		 * Uses configuration module proxy until configurations refactor
		 *
		 * TODO: refactor to save directly only language configuration on another endpoint
		 */
		onLocalizationConfigurationSaveButtonClick: function () {
			CMDBuild.proxy.configuration.GeneralOptions.read({
				scope: this,
				success: function (response, options, decodedResponse) {
					var params = decodedResponse[CMDBuild.core.constants.Proxy.DATA];
					params['language'] = this.view.defaultLanguageCombobox.getValue();
					params['languageprompt'] = this.view.languagePromptCheckbox.getValue();
					params['enabled_languages'] = this.view.enabledLanguagesGrid.getValue().join(', ');

					CMDBuild.proxy.configuration.GeneralOptions.update({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							CMDBuild.core.Message.success();

							Ext.create('CMDBuild.core.configurations.builder.Localization'); // Rebuild configuration model
						}
					});
				}
			});
		}
	});

})();
