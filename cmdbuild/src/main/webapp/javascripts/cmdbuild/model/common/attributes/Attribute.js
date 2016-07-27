(function() {

	Ext.require(['CMDBuild.core.constants.Proxy']);

	Ext.define('CMDBuild.model.common.attributes.Attribute', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EDITOR_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.HIDDEN, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.LENGTH, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.constants.Proxy.LOOKUP_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.MANDATORY, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PRECISION, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.SCALE, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.constants.Proxy.SHOW_COLUMN, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.TARGET_CLASS, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', convert: toLowerCase }, // Case insensitive types
			{ name: CMDBuild.core.constants.Proxy.UNIQUE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.WRITABLE, type: 'boolean', defaultValue: true }
		],

		/**
		 * Function to translate old CMDBuild attributes configuration objects to new one used from new FieldManager
		 *
		 * @param {Object} data
		 */
		setAdaptedData: function(data) {
			if (!Ext.isEmpty(data) && Ext.isObject(data)) {
				this.set(CMDBuild.core.constants.Proxy.LENGTH, data['len']);
				this.set(CMDBuild.core.constants.Proxy.LOOKUP_TYPE, data[CMDBuild.core.constants.Proxy.LOOKUP]);
				this.set(CMDBuild.core.constants.Proxy.MANDATORY, data['isnotnull']);
				this.set(CMDBuild.core.constants.Proxy.SHOW_COLUMN, data['isbasedsp']);
				this.set(CMDBuild.core.constants.Proxy.UNIQUE, data['isunique']);

				if (!Ext.isEmpty(data['fieldmode']))
					if (data['fieldmode'] == CMDBuild.core.constants.Proxy.WRITE) {
						this.set(CMDBuild.core.constants.Proxy.WRITABLE, true);
					} else if (data['fieldmode'] == CMDBuild.core.constants.Proxy.HIDDEN) {
						this.set(CMDBuild.core.constants.Proxy.HIDDEN, true);
					}

				// ForeignKey's specific
				if (!Ext.isEmpty(data['fkDestination']))
					this.set(CMDBuild.core.constants.Proxy.TARGET_CLASS, data['fkDestination']);
			}
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function() {
			var customValidationValue = true;

			switch (this.get(CMDBuild.core.constants.Proxy.TYPE)) {
				case 'decimal': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.constants.Proxy.SCALE))
						&& !Ext.isEmpty(this.get(CMDBuild.core.constants.Proxy.PRECISION))
						&& this.get(CMDBuild.core.constants.Proxy.SCALE) <= this.get(CMDBuild.core.constants.Proxy.PRECISION)
					);
				} break;

				case 'foreignkey': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.constants.Proxy.TARGET_CLASS))
					);
				} break;

				case 'string': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.constants.Proxy.LENGTH))
						&& this.get(CMDBuild.core.constants.Proxy.LENGTH) > 0
					);
				} break;
			}

			return this.callParent(arguments) && customValidationValue;
		}
	});

	/**
	 * @param {String} value
	 * @param {Object} record
	 *
	 * @returns {String}
	 *
	 * @private
	 */
	function toLowerCase(value, record) {
		return value.toLowerCase();
	}

})();