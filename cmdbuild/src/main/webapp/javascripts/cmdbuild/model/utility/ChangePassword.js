(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.utility.ChangePassword', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.NEW_PASSWORD, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.OLD_PASSWORD, type: 'string' }
		],

		/**
		 * @returns {Object}
		 */
		getDataLegacy: function (data) {
			return {
				newpassword: this.get(CMDBuild.core.constants.Proxy.NEW_PASSWORD),
				oldpassword: this.get(CMDBuild.core.constants.Proxy.OLD_PASSWORD)
			};
		}
	});

})();
