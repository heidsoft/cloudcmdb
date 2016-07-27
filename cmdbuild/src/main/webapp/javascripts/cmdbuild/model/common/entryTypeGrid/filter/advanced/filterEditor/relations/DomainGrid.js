(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGrid', { // TODO: waiting for refactor (rename)
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'noone', type: 'boolean', defaultValue: false },
			{ name: 'oneof', type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.ANY, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.CHECKED_CARDS, type: 'auto', defaultValue: [] }, // {Array} [{Id: ..., ClassName: ''}, ...]
			{ name: CMDBuild.core.constants.Proxy.DESTINATION, type: 'auto' }, // {CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.EntryType}
			{ name: CMDBuild.core.constants.Proxy.DIRECTION, type: 'string' }, // '_1' if the source class is the first of the domain configuration, '_2' if the source class is the second of the domain configuration
			{ name: CMDBuild.core.constants.Proxy.DOMAIN, type: 'auto' }, // {CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.Domain}
			{ name: CMDBuild.core.constants.Proxy.DOMAIN_DESCRIPTION, type: 'string', persist: false }, // Only to sort store
			{ name: CMDBuild.core.constants.Proxy.ORIENTED_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SOURCE, type: 'auto' } // {CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.EntryType}
		],

		/**
		 * Override to permits multilevel get with a single function
		 *
		 * @param {Array or String} propertyName
		 *
		 * @returns {Mixed}
		 *
		 * @override
		 */
		get: function (propertyName) {
			if (Ext.isArray(propertyName) && !Ext.isEmpty(propertyName)) {
				var returnValue = this;

				Ext.Array.each(propertyName, function (name, i, allPropertyNames) {
					if (!Ext.isEmpty(returnValue) && Ext.isFunction(returnValue.get))
						returnValue = returnValue.get(name);
				}, this);

				return returnValue;
			}

			return this.callParent(arguments);
		},

		/**
		 * @returns {String or null}
		 */
		getType: function () {
			if (this.get(CMDBuild.core.constants.Proxy.ANY)) {
				return CMDBuild.core.constants.Proxy.ANY;
			} else if (this.get('noone')) {
				return 'noone';
			} else if (this.get('oneof')) {
				return 'oneof';
			}

			return null;
		},

		/**
		 * Fields properties 'any', 'noone' and 'oneof' are mutual exclusive
		 *
		 * @param {String} type
		 *
		 * @returns {Void}
		 */
		setType: function (type) {
			this.set('noone', type == 'noone');
			this.set('oneof', type == 'oneof');
			this.set(CMDBuild.core.constants.Proxy.ANY, type == CMDBuild.core.constants.Proxy.ANY);
		}
	});

})();
