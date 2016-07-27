(function() {

	Ext.define('CMDBuild.core.interfaces.service.LoadMask', {

		requires: ['CMDBuild.core.LoadMask'],

		singleton: true,

		/**
		 * Manage parameter to display global loadMask or specific panel's one
		 *
		 * @param {Object or Boolean} param
		 * @param {Boolean} show
		 */
		manage: function(param, show) {
			show = Ext.isBoolean(show) ? show : false;

			if (!Ext.isEmpty(param)) {
				switch (Ext.typeOf(param)) {
					case 'object':
						return CMDBuild.core.interfaces.service.LoadMask.manageObject(param, show);

					case 'boolean':
					default:
						return CMDBuild.core.interfaces.service.LoadMask.manageBoolean(param, show);
				}
			}
		},

		/**
		 * @param {Object or Boolean} param
		 * @param {Boolean} show
		 *
		 * @private
		 */
		manageBoolean: function(param, show) {
			show = Ext.isBoolean(show) ? show : false;

			if (param)
				if (show) {
					CMDBuild.core.LoadMask.show();
				} else {
					CMDBuild.core.LoadMask.hide();
				}
		},

		/**
		 * @param {Object or Boolean} param
		 * @param {Boolean} show
		 *
		 * @private
		 */
		manageObject: function(param, show) {
			show = Ext.isBoolean(show) ? show : false;

			if (Ext.isFunction(param.setLoading))
				param.setLoading(show);
		}
	});

})();