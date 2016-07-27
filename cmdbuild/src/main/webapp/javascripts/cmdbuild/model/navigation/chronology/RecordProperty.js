(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.navigation.chronology.RecordProperty', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string' }, // Accordion item's IDs are strings
			{ name: CMDBuild.core.constants.Proxy.OBJECT, type: 'auto', defaultValue: {} }
		],

		/**
		 * @param {CMDBuild.model.navigation.chronology.RecordProperty} record
		 *
		 * @returns {Boolean}
		 */
		equals: function (record) {
			return (
				Ext.getClassName(record) == Ext.getClassName(this)
				&& this.get(CMDBuild.core.constants.Proxy.DESCRIPTION) == record.get(CMDBuild.core.constants.Proxy.DESCRIPTION)
				&& this.get(CMDBuild.core.constants.Proxy.ID) == record.get(CMDBuild.core.constants.Proxy.ID)
			);
		},

		/**
		 * @param {Array or String} attributePath
		 *
		 * @returns {Boolean}
		 */
		isEmpty: function (attributeName) {
			if (!Ext.isEmpty(attributeName)) {
				var requiredValue = this.get(attributeName);

				if (Ext.isObject(requiredValue))
					return Ext.Object.isEmpty(requiredValue);

				return Ext.isEmpty(requiredValue);
			}

			return Ext.Object.isEmpty(this.getData());
		}
	});

})();
