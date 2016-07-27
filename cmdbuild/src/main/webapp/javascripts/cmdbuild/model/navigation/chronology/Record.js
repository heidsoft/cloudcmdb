(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.navigation.chronology.Record', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DATE, type: 'date' },
			{ name: CMDBuild.core.constants.Proxy.ENTRY_TYPE, type: 'auto' }, // 2nd level - a.k.a. entityId
			{ name: CMDBuild.core.constants.Proxy.ITEM, type: 'auto' }, // 3rd level - card/workflow/item usually selected from grid
			{ name: CMDBuild.core.constants.Proxy.MODULE_ID, type: 'string' }, // 1st level - module identifier
			{ name: CMDBuild.core.constants.Proxy.SECTION, type: 'auto', defaultValue: {} },  // 4th level
			{ name: CMDBuild.core.constants.Proxy.SUB_SECTION, type: 'auto', defaultValue: {} }  // 5th level
		],

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function (data) {
			data = Ext.clone(data);
			data[CMDBuild.core.constants.Proxy.DATE] = new Date();
			data[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = Ext.create('CMDBuild.model.navigation.chronology.RecordProperty', data[CMDBuild.core.constants.Proxy.ENTRY_TYPE]);
			data[CMDBuild.core.constants.Proxy.ITEM] = Ext.create('CMDBuild.model.navigation.chronology.RecordProperty', data[CMDBuild.core.constants.Proxy.ITEM]);
			data[CMDBuild.core.constants.Proxy.SECTION] = Ext.create('CMDBuild.model.navigation.chronology.RecordProperty', data[CMDBuild.core.constants.Proxy.SECTION]);
			data[CMDBuild.core.constants.Proxy.SUB_SECTION] = Ext.create('CMDBuild.model.navigation.chronology.RecordProperty', data[CMDBuild.core.constants.Proxy.SUB_SECTION]);

			this.callParent(arguments);
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @returns {Boolean}
		 */
		equals: function (record) {
			return (
				Ext.getClassName(record) == Ext.getClassName(this)
				&& this.get(CMDBuild.core.constants.Proxy.MODULE_ID) == record.get(CMDBuild.core.constants.Proxy.MODULE_ID)
				&& this.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE).equals(record.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE))
				&& this.get(CMDBuild.core.constants.Proxy.ITEM).equals(record.get(CMDBuild.core.constants.Proxy.ITEM))
				&& this.get(CMDBuild.core.constants.Proxy.SECTION).equals(record.get(CMDBuild.core.constants.Proxy.SECTION))
				&& this.get(CMDBuild.core.constants.Proxy.SUB_SECTION).equals(record.get(CMDBuild.core.constants.Proxy.SUB_SECTION))
			);
		},

		/**
		 * @param {Array or String} attributePath
		 *
		 * @returns {Mixed}
		 *
		 * @override
		 */
		get: function (attributePath) {
			var requiredAttribute = this;

			if (!Ext.isEmpty(attributePath) && Ext.isArray(attributePath)) {
				Ext.Array.forEach(attributePath, function (attributeName, i, allAttributeNames) {
					if (
						!Ext.isEmpty(attributeName) && Ext.isString(attributeName)
						&& !Ext.isEmpty(requiredAttribute) && Ext.isObject(requiredAttribute) && Ext.isFunction(requiredAttribute.get)
					) {
						requiredAttribute = requiredAttribute.get(attributeName);
					}
				}, this);

				return requiredAttribute;
			}

			return this.callParent(arguments);
		},

		/**
		 * @param {Array or String} attributePath
		 *
		 * @returns {Boolean}
		 */
		isEmpty: function (attributePath) {
			if (!Ext.isEmpty(attributePath)) {
				var requiredValue = this.get(attributePath);

				if (Ext.isObject(requiredValue))
					return Ext.Object.isEmpty(requiredValue);

				return Ext.isEmpty(requiredValue);
			}

			return Ext.Object.isEmpty(this.getData());
		}
	});

})();
