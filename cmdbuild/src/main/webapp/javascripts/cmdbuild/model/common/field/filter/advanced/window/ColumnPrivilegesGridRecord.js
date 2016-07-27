(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.filter.advanced.window.ColumnPrivilegesGridRecord', {
		extend: 'Ext.data.Model',

		/**
		 * @cfg {Array}
		 */
		managedPrivileges: [
			CMDBuild.core.constants.Proxy.NONE,
			CMDBuild.core.constants.Proxy.READ,
			CMDBuild.core.constants.Proxy.WRITE
		],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NONE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.READ, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.WRITE, type: 'boolean' }
		],

		/**
		 * @returns {String}
		 */
		getPrivilege: function() {
			if (this.get(CMDBuild.core.constants.Proxy.NONE))
				return CMDBuild.core.constants.Proxy.NONE;

			if (this.get(CMDBuild.core.constants.Proxy.READ))
				return CMDBuild.core.constants.Proxy.READ;

			if (this.get(CMDBuild.core.constants.Proxy.WRITE))
				return CMDBuild.core.constants.Proxy.WRITE;

			// Default value
			return CMDBuild.core.constants.Proxy.NONE;
		},

		/**
		 * Manages privilege's exclusivity
		 *
		 * @param {String} privilege
		 */
		setPrivilege: function(privilege) {
			// Reset all values
			this.set(CMDBuild.core.constants.Proxy.NONE, false);
			this.set(CMDBuild.core.constants.Proxy.READ, false);
			this.set(CMDBuild.core.constants.Proxy.WRITE, false);

			if (Ext.Array.contains(this.managedPrivileges, privilege)) {
				this.set(privilege, true);
			} else {// Default value
				this.set(CMDBuild.core.constants.Proxy.NONE, true);
			}

			this.commit();
		}
	});

})();