(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CONFIGURATION, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ENTRY_TYPE, type: 'string' }, // Entry type name
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEMPLATE, type: 'boolean' }
		],

		/**
		 * Implementation of model get custom routines:
		 * - on get description if description is empty return name property
		 *
		 * @param {String} propertyName
		 *
		 * @returns {Mixed}
		 *
		 * @override
		 */
		get: function (propertyName) {
			switch (propertyName) {
				case CMDBuild.core.constants.Proxy.DESCRIPTION:
					return this.callParent(arguments) || this.get(CMDBuild.core.constants.Proxy.NAME) || '';

				default:
					return this.callParent(arguments);
			}
		},

		/**
		 * @returns {Array}
		 */
		getRuntimeParameters: function () {
			var configuration = this.get(CMDBuild.core.constants.Proxy.CONFIGURATION);

			return this.runtimeParameterSearch(configuration[CMDBuild.core.constants.Proxy.ATTRIBUTE] || {}, []);
		},

		/**
		 * Recursive method to find all filter runtime parameters
		 *
		 * @param {Object} configuration
		 * @param {Array} runtimeParameters
		 *
		 * @returns {Array} runtimeParameters
		 *
		 * @private
		 */
		runtimeParameterSearch: function (configuration, runtimeParameters) {
			if (Ext.isObject(configuration.simple)) {
				var conf = configuration.simple;

				if (conf.parameterType == CMDBuild.core.constants.Proxy.RUNTIME)
					runtimeParameters.push(conf);
			} else if (Ext.isArray(configuration.and) || Ext.isArray(configuration.or)) {
				var attributes = configuration.and || configuration.or;

				if (Ext.isArray(attributes) && !Ext.isEmpty(attributes))
					Ext.Array.each(attributes, function (attributeObject, i, allAttributeObjects) {
						this.runtimeParameterSearch(attributeObject, runtimeParameters);
					}, this);
			}

			return runtimeParameters;
		}
	});

})();
