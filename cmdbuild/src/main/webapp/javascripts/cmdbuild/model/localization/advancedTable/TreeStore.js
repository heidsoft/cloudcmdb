(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.localization.advancedTable.TreeStore', {
		extend: 'Ext.data.TreeModel',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DEFAULT, type: 'string'}, // Default translation
			{ name: CMDBuild.core.constants.Proxy.FIELD, type: 'string' }, // Field to translate (description, inverseDescription, ...)
			{ name: CMDBuild.core.constants.Proxy.IDENTIFIER, type: 'string' }, // Entity's attribute/property identifier
			{ name: CMDBuild.core.constants.Proxy.OWNER, type: 'string' }, // Translation owner identifier (className, domainName, ...) used only to translate attribute's entities
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'auto' }, // Parent node
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string'}, // Label to display in grid's tree column (usually name attribute)
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string'} // Entity type identifier (class, attributeclass, domain, attributedomain, filter, instancename, lookupvalue, menuitem, report, view, classwidget)
		],

		/**
		 * Complete fields properties with all configured languages
		 */
		constructor: function() {
			var modelFields = CMDBuild.model.localization.advancedTable.TreeStore.getFields();
			var languages = CMDBuild.configuration.localization.getAllLanguages();

			Ext.Object.each(languages, function(key, value, myself) {
				modelFields.push({ name: value.get(CMDBuild.core.constants.Proxy.TAG), type: 'string' });
			}, this);

			CMDBuild.model.localization.advancedTable.TreeStore.setFields(modelFields);

			this.callParent(arguments);
		},

		/**
		 * @returns {Object} translationsObject
		 */
		getTranslations: function() {
			var translationsObject = {};
			var enabledLanguages = CMDBuild.configuration.localization.getEnabledLanguages();

			Ext.Object.each(enabledLanguages, function(key, value, myself) {
				translationsObject[key] = this.get(key);
			}, this);

			return translationsObject;
		}
	});

})();