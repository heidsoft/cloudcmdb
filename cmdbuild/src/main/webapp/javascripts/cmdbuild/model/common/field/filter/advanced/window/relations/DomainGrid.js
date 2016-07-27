(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'noone', type: 'boolean', defaultValue: false },
			{ name: 'oneof', type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.ANY, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.CHECKED_CARDS, type: 'auto', defaultValue: [] }, // {Array} [{Id: ..., ClassName: ''}, ...]
			{ name: CMDBuild.core.constants.Proxy.DESTINATION, type: 'auto' }, // {CMDBuild.cache.CMEntryTypeModel}
			{ name: CMDBuild.core.constants.Proxy.DIRECTION, type: 'string' }, // '_1' if the source class is the first of the domain configuration, '_2' if the source class is the second of the domain configuration
			{ name: CMDBuild.core.constants.Proxy.DOMAIN, type: 'auto' }, // {CMDBuild.cache.CMDomainModel}
			{ name: CMDBuild.core.constants.Proxy.ORIENTED_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SOURCE, type: 'auto' } // {CMDBuild.cache.CMEntryTypeModel}
		],

		/**
		 * @returns {String} or null
		 */
		getType: function() {
			var type = null;

			if (this.get(CMDBuild.core.constants.Proxy.ANY)) {
				type = CMDBuild.core.constants.Proxy.ANY;
			} else if (this.get('noone')) {
				type = 'noone';
			} else if (
				this.get('oneof')
			) {
				type = 'oneof';
			}

			return type;
		},

		/**
		 * Fields properties 'any', 'noone' and 'oneof' are mutual exclusive
		 *
		 * @param {String} type
		 */
		setType: function(type) {
			this.set('noone', type == 'noone');
			this.set('oneof', type == 'oneof');
			this.set(CMDBuild.core.constants.Proxy.ANY, type == CMDBuild.core.constants.Proxy.ANY);
		}
	});

})();