(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.core.configurations.builder.userInterface.UserInterface', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CLOUD_ADMIN, type: 'boolean', defaulValue: false },
			{ name: CMDBuild.core.constants.Proxy.DISABLED_CARD_TABS, type: 'auto', defaultValue: [] }, // {CMDBuild.model.core.configurations.builder.userInterface.DisabledCardTabs}
			{ name: CMDBuild.core.constants.Proxy.DISABLED_MODULES, type: 'auto', defaultValue: [] }, // {CMDBuild.model.core.configurations.builder.userInterface.DisabledModules}
			{ name: CMDBuild.core.constants.Proxy.DISABLED_PROCESS_TABS, type: 'auto', defaultValue: [] }, // {CMDBuild.model.core.configurations.builder.userInterface.DisabledProcessTabs}
			{ name: CMDBuild.core.constants.Proxy.FULL_SCREEN_MODE, type: 'boolean', defaulValue: false },
			{ name: CMDBuild.core.constants.Proxy.HIDE_SIDE_PANEL, type: 'boolean', defaulValue: false },
			{ name: CMDBuild.core.constants.Proxy.PROCESS_WIDGET_ALWAYS_ENABLED, type: 'boolean', defaulValue: false },
			{ name: CMDBuild.core.constants.Proxy.SIMPLE_HISTORY_MODE_FOR_CARD, type: 'boolean', defaulValue: false },
			{ name: CMDBuild.core.constants.Proxy.SIMPLE_HISTORY_MODE_FOR_PROCESS, type: 'boolean', defaulValue: false }
		],

		/**
		 * @param {Object} data
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (data) {
			this.callParent(arguments);

			// DisabledCardTabs to model conversion
			this.toModel(CMDBuild.core.constants.Proxy.DISABLED_CARD_TABS, 'CMDBuild.model.core.configurations.builder.userInterface.DisabledCardTabs');

			// DisabledModules to model conversion
			this.toModel(CMDBuild.core.constants.Proxy.DISABLED_MODULES, 'CMDBuild.model.core.configurations.builder.userInterface.DisabledModules');

			// DisabledProcessTabs to model conversion
			this.toModel(CMDBuild.core.constants.Proxy.DISABLED_PROCESS_TABS, 'CMDBuild.model.core.configurations.builder.userInterface.DisabledProcessTabs');
		},

		/**
		 * @param {String} name
		 *
		 * @returns {Boolean}
		 */
		isDisabledCardTab: function (name) {
			return this.get(CMDBuild.core.constants.Proxy.DISABLED_CARD_TABS).get(name);
		},

		/**
		 * @param {String} name
		 *
		 * @returns {Boolean}
		 */
		isDisabledModule: function (name) {
			return this.get(CMDBuild.core.constants.Proxy.DISABLED_MODULES).get(name);
		},

		/**
		 * @param {String} name
		 *
		 * @returns {Boolean}
		 */
		isDisabledProcessTab: function (name) {
			return this.get(CMDBuild.core.constants.Proxy.DISABLED_PROCESS_TABS).get(name);
		},

		/**
		 * @param {String} propertyName
		 * @param {String} modelName
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		toModel: function (propertyName, modelName) {
			if (Ext.isArray(this.get(propertyName))) {
				var modelObject = {};

				Ext.Array.forEach(this.get(propertyName), function (property, i, allProperties) {
					modelObject[property] = true;
				}, this);

				return this.set(propertyName, Ext.create(modelName, modelObject));
			}

			return this.set(propertyName, Ext.create(modelName, this.get(propertyName)));
		}
	});

})();
