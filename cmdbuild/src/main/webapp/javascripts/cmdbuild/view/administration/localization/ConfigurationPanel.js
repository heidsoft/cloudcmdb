(function () {

	Ext.define('CMDBuild.view.administration.localization.ConfigurationPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.Configuration}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Language}
		 */
		defaultLanguageCombobox: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.common.LanguagesGrid}
		 */
		enabledLanguagesGrid: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		languagePromptCheckbox: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onLocalizationConfigurationSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onLocalizationConfigurationAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.languageConfiguration,
						overflowY: 'auto',

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						defaults: {
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_MEDIUM
						},

						items: [
							this.defaultLanguageCombobox = Ext.create('CMDBuild.view.common.field.comboBox.Language', {
								name: CMDBuild.core.constants.Proxy.DEFAULT_LANGUAGE,
								fieldLabel: CMDBuild.Translation.defaultLanguage,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								enableChangeLanguage: false
							}),
							this.languagePromptCheckbox = Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.constants.Proxy.LANGUAGE_PROMPT,
								fieldLabel: CMDBuild.Translation.showLanguageChoice,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
								inputValue: true,
								uncheckedValue: false
							})
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.enabledLanguages,
						overflowY: 'auto',

						items: [
							this.enabledLanguagesGrid = Ext.create('CMDBuild.view.administration.localization.common.LanguagesGrid')
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
