(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.userAndGroup.group.userInterface.UserInterface', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CLOUD_ADMIN, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DISABLED_CARD_TABS, type: 'auto', defaultValue: [] }, // {CMDBuild.model.userAndGroup.group.userInterface.DisabledCardTabs}
			{ name: CMDBuild.core.constants.Proxy.DISABLED_MODULES, type: 'auto', defaultValue: [] }, // {CMDBuild.model.userAndGroup.group.userInterface.DisabledModules}
			{ name: CMDBuild.core.constants.Proxy.DISABLED_PROCESS_TABS, type: 'auto', defaultValue: [] }, // {CMDBuild.model.userAndGroup.group.userInterface.DisabledProcessTabs}
			{ name: CMDBuild.core.constants.Proxy.FULL_SCREEN_MODE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.HIDE_SIDE_PANEL, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PROCESS_WIDGET_ALWAYS_ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.SIMPLE_HISTORY_MODE_FOR_CARD, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.SIMPLE_HISTORY_MODE_FOR_PROCESS, type: 'boolean' }
		],

		/**
		 * @param {Object} data
		 */
		constructor: function (data) {
			this.propertiesAdapter(data);

			data[CMDBuild.core.constants.Proxy.DISABLED_CARD_TABS] = Ext.create('CMDBuild.model.core.configurations.builder.userInterface.DisabledCardTabs', Ext.clone(data));
			data[CMDBuild.core.constants.Proxy.DISABLED_MODULES] = Ext.create('CMDBuild.model.core.configurations.builder.userInterface.DisabledModules', Ext.clone(data));
			data[CMDBuild.core.constants.Proxy.DISABLED_PROCESS_TABS] = Ext.create('CMDBuild.model.core.configurations.builder.userInterface.DisabledProcessTabs', Ext.clone(data));

			this.callParent(arguments);
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		getData: function () {
			var returnedObject = this.callParent(arguments);
			returnedObject[CMDBuild.core.constants.Proxy.DISABLED_CARD_TABS] = this.toArray(CMDBuild.core.constants.Proxy.DISABLED_CARD_TABS);
			returnedObject[CMDBuild.core.constants.Proxy.DISABLED_MODULES] = this.toArray(CMDBuild.core.constants.Proxy.DISABLED_MODULES);
			returnedObject[CMDBuild.core.constants.Proxy.DISABLED_PROCESS_TABS] = this.toArray(CMDBuild.core.constants.Proxy.DISABLED_PROCESS_TABS);

			delete returnedObject[CMDBuild.core.constants.Proxy.ID];

			return returnedObject;
		},

		/**
		 * Linearize object injecting array items in main object
		 *
		 * @param {Object} data
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		propertiesAdapter: function (data) {
			Ext.Object.each(data, function (key, value, myself) {
				if (Ext.isArray(value))
					Ext.Array.forEach(value, function (property, i, allProperties) {
						data[property] = true;
					}, this);
			}, this);
		},

		/**
		 * @param {String} propertyName
		 *
		 * @returns {Array} arrayBuffer
		 *
		 * @private
		 */
		toArray: function (propertyName) {
			var arrayBuffer = [];

			Ext.Object.each(this.get(propertyName).getData(), function (key, value, myself) {
				if (value)
					arrayBuffer.push(key);
			}, this);

			return arrayBuffer;
		}
	});

})();
