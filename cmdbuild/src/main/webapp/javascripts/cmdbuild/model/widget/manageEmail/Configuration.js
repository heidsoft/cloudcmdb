(function() {

	Ext.require(['CMDBuild.core.constants.Proxy']);

	Ext.define('CMDBuild.model.widget.manageEmail.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'alwaysenabled', type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.READ_ONLY, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.TEMPLATES, type: 'auto', defaultValue: [] }, // Array of CMDBuild.model.common.tabs.email.Template models
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' }
		],

		/**
		 * @param {Object} data
		 */
		constructor: function(data) {
			this.callParent(arguments);

			// Apply templates model
			this.set(CMDBuild.core.constants.Proxy.TEMPLATES, data[CMDBuild.core.constants.Proxy.TEMPLATES]);
		},

		/**
		 * @param {String} fieldName
		 * @param {Object} newValue
		 *
		 * @returns {String}
		 *
		 * @override
		 */
		set: function(fieldName, newValue) {
			if (!Ext.isEmpty(newValue)) {
				switch (fieldName) {
					case CMDBuild.core.constants.Proxy.TEMPLATES: {
						newValue = Ext.isString(newValue) ? Ext.decode(newValue) : newValue;

						var templatesArray = [];

						Ext.Array.forEach(newValue, function(attributeObject, i, allAttributeObjects) {
							if (Ext.isObject(attributeObject) && !Ext.Object.isEmpty(attributeObject)) {
								attributeObject[CMDBuild.core.constants.Proxy.BCC] = attributeObject[CMDBuild.core.constants.Proxy.BCC_ADDRESSES];
								attributeObject[CMDBuild.core.constants.Proxy.BODY] = attributeObject[CMDBuild.core.constants.Proxy.CONTENT];
								attributeObject[CMDBuild.core.constants.Proxy.CC] = attributeObject[CMDBuild.core.constants.Proxy.CC_ADDRESSES];
								attributeObject[CMDBuild.core.constants.Proxy.FROM] = attributeObject[CMDBuild.core.constants.Proxy.FROM_ADDRESS];
								attributeObject[CMDBuild.core.constants.Proxy.TO] = attributeObject[CMDBuild.core.constants.Proxy.TO_ADDRESSES];

								templatesArray.push(Ext.create('CMDBuild.model.common.tabs.email.Template', attributeObject));
							}
						}, this);

						newValue = templatesArray;
					} break;

					default: {
						if (Ext.isString(newValue))
							newValue = Ext.decode(newValue);
					}
				}
			}

			this.callParent(arguments);
		}
	});

})();