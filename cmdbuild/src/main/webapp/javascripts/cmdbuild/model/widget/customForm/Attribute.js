(function() {

	Ext.require(['CMDBuild.core.constants.Proxy']);

	/**
	 * Adapter model class to old FieldManager implementation
	 * TODO: delete on full FieldManager implementation
	 */
	Ext.define('CMDBuild.model.widget.customForm.Attribute', {
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
		 * Create a translation layer to adapt old CMDBuild field definition with new one.
		 * To avoid this should be necessary to refactor FieldManager class.
		 *
		 * @returns {Object}
		 */
		getAdaptedData: function() {
			var objectModel = this.getData();

			objectModel['fieldmode'] = this.get(CMDBuild.core.constants.Proxy.WRITABLE) ? 'write' : 'read';
			objectModel['fieldmode'] = this.get(CMDBuild.core.constants.Proxy.HIDDEN) ? CMDBuild.core.constants.Proxy.HIDDEN : objectModel['fieldmode'];
			objectModel['isbasedsp'] = this.get(CMDBuild.core.constants.Proxy.SHOW_COLUMN);
			objectModel['isnotnull'] = this.get(CMDBuild.core.constants.Proxy.MANDATORY);

			switch (objectModel[CMDBuild.core.constants.Proxy.TYPE]) {
				case 'lookup': {
					objectModel['lookup'] = this.get(CMDBuild.core.constants.Proxy.LOOKUP_TYPE);
					objectModel['lookupchain'] = _CMCache.getLookupchainForType(this.get(CMDBuild.core.constants.Proxy.LOOKUP_TYPE));
				} break;

				case 'reference': {
					objectModel['referencedClassName'] = this.get(CMDBuild.core.constants.Proxy.TARGET_CLASS);
					objectModel[CMDBuild.core.constants.Proxy.META] = {};

					// New filter object structure adapter
					if (!Ext.isEmpty(this.get(CMDBuild.core.constants.Proxy.FILTER))) {
						objectModel[CMDBuild.core.constants.Proxy.FILTER] = this.get(CMDBuild.core.constants.Proxy.FILTER)[CMDBuild.core.constants.Proxy.EXPRESSION];

						Ext.Object.each(this.get(CMDBuild.core.constants.Proxy.FILTER)[CMDBuild.core.constants.Proxy.CONTEXT], function(key, value, myself) {
							objectModel[CMDBuild.core.constants.Proxy.META]['system.template.' + key] = value;
						}, this);
					}
				} break;
			}

			objectModel[CMDBuild.core.constants.Proxy.TYPE] = this.get(CMDBuild.core.constants.Proxy.TYPE).toUpperCase();

			return objectModel;
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function() {
			var customValidationValue = false;

			switch (this.get(CMDBuild.core.constants.Proxy.TYPE)) {
				case 'decimal': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.constants.Proxy.SCALE))
						&& !Ext.isEmpty(this.get(CMDBuild.core.constants.Proxy.PRECISION))
						&& this.get(CMDBuild.core.constants.Proxy.SCALE) < this.get(CMDBuild.core.constants.Proxy.PRECISION)
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