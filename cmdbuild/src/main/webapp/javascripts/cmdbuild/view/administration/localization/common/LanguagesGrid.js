(function () {

	Ext.define('CMDBuild.view.administration.localization.common.LanguagesGrid', {
		extend: 'Ext.container.Container',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		border: false,
		frame: false,
		layout: 'column',

		initComponent: function () {
			var languagesArray = Ext.Object.getValues(CMDBuild.configuration.localization.get(CMDBuild.core.constants.Proxy.LANGUAGES));
			var languageCheckboxes = [];

			// Sort languages with alphabetical order
			CMDBuild.core.Utils.objectArraySort(languagesArray, CMDBuild.core.constants.Proxy.DESCRIPTION);

			Ext.Array.forEach(languagesArray, function (languageModel, i, allLanguageModels) {
				languageCheckboxes.push(
					Ext.create('Ext.form.field.Checkbox', {
						fieldLabel: languageModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						name: languageModel.get(CMDBuild.core.constants.Proxy.TAG),
						padding: '3 5',
						margin: '0 20 0 0',
						submitValue: false,
						labelClsExtra: 'ux-flag-' + languageModel.get(CMDBuild.core.constants.Proxy.TAG),
						labelStyle: 'background-repeat: no-repeat; background-position: left; padding-left: 22px;'
					})
				);
			}, this);

			Ext.apply(this, {
				items: languageCheckboxes
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Array}
		 */
		getValue: function () {
			var languageArray = [];

			Ext.Array.forEach(this.getItems(), function (languageCheckbox, i, allCheckboxes) {
				if (languageCheckbox.getValue())
					languageArray.push(languageCheckbox.getName());
			}, this);

			return languageArray;
		},

		/**
		 * @param {Array} activeLanguages
		 */
		setValue: function (activeLanguages) {
			if (Ext.isArray(activeLanguages))
				Ext.Array.forEach(this.getItems(), function (languageCheckbox, i, allCheckboxes) {
					languageCheckbox.setValue(Ext.Array.contains(activeLanguages, languageCheckbox.getName()));
				}, this);
		},

		/**
		 * Service function to get all items
		 *
		 * @returns {Array}
		 */
		getItems: function () {
			return this.items.getRange();
		}
	});

})();
