(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.userAndGroup.group.Group', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DISABLED_MODULES, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.EMAIL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.IS_ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.IS_CLOUD_ADMINISTRATOR, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.STARTING_CLASS, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' }, // 'Name' alias (waiting server refactor)
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', defaultValue: 'normal' } // Evaluated on model creation
		],

		/**
		 * @param {Object} data
		 */
		constructor: function (data) {
			this.callParent(arguments);

			// StartingClass filter (waiting server refactor)
			if (this.get(CMDBuild.core.constants.Proxy.STARTING_CLASS) == 0)
				this.set(CMDBuild.core.constants.Proxy.STARTING_CLASS, null);

			// Evaluate type attribute
			if (this.get(CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR))
				this.set(
					CMDBuild.core.constants.Proxy.TYPE,
					this.get(CMDBuild.core.constants.Proxy.IS_CLOUD_ADMINISTRATOR) ? CMDBuild.core.constants.Proxy.RESTRICTED_ADMIN
						: CMDBuild.core.constants.Proxy.ADMIN
				);

			this.commit();
		}
	});

})();
