(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.core.configurations.builder.Instance', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CARD_FORM_RATIO, type: 'int', defaultValue: 50 },
			{ name: CMDBuild.core.constants.Proxy.CARD_LOCK_TIMEOUT, type: 'int', defaultValue: 300 },
			{ name: CMDBuild.core.constants.Proxy.CARD_TABS_POSITION, type: 'string', defaultValue: 'bottom' },
			{ name: CMDBuild.core.constants.Proxy.DISPLAY_CARD_LOCKER_NAME, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ENABLE_CARD_LOCK, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.INSTANCE_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.POPUP_HEIGHT_PERCENTAGE, type: 'int', defaultValue: 80 },
			{ name: CMDBuild.core.constants.Proxy.POPUP_WIDTH_PERCENTAGE, type: 'int', defaultValue: 80 },
			{ name: CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT, type: 'int', defaultValue: 500 }, // Limit of dropDown displayed relations before force pop-up window show
			{ name: CMDBuild.core.constants.Proxy.RELATION_LIMIT, type: 'int', defaultValue: 20 },
			{ name: CMDBuild.core.constants.Proxy.ROW_LIMIT, type: 'int', defaultValue: 20 }, // Rows per page to display in card's grid
			{ name: CMDBuild.core.constants.Proxy.SESSION_TIMEOUT, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.STARTING_CLASS, type: 'int', useNull: true } // Default selected class on UI display
		],

		statics: {
			/**
			 * Static function to convert from legacy object to model's one
			 *
			 * @param {Object} data
			 *
			 * @returns {Object} data
			 */
			convertFromLegacy: function (data) {
				data = data || {};
				data[CMDBuild.core.constants.Proxy.CARD_FORM_RATIO] = data['grid_card_ratio'];
				data[CMDBuild.core.constants.Proxy.CARD_LOCK_TIMEOUT] = data['lockcardtimeout'];
				data[CMDBuild.core.constants.Proxy.CARD_TABS_POSITION] = data['card_tab_position'];
				data[CMDBuild.core.constants.Proxy.DISPLAY_CARD_LOCKER_NAME] = data['lockcarduservisible'];
				data[CMDBuild.core.constants.Proxy.ENABLE_CARD_LOCK] = data['lockcardenabled'];
				data[CMDBuild.core.constants.Proxy.INSTANCE_NAME] = data['instance_name'];
				data[CMDBuild.core.constants.Proxy.POPUP_HEIGHT_PERCENTAGE] = data['popuppercentageheight'];
				data[CMDBuild.core.constants.Proxy.POPUP_WIDTH_PERCENTAGE] = data['popuppercentagewidth'];
				data[CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT] = data['referencecombolimit'];
				data[CMDBuild.core.constants.Proxy.RELATION_LIMIT] = data['relationlimit'];
				data[CMDBuild.core.constants.Proxy.ROW_LIMIT] = data['rowlimit'];
				data[CMDBuild.core.constants.Proxy.SESSION_TIMEOUT] = data['session.timeout'];
				data[CMDBuild.core.constants.Proxy.STARTING_CLASS] = data['startingclass'];

				return data;
			}
		},

		/**
		 * @param {Object} data
		 *
		 * @override
		 */
		constructor: function (data) {
			data = CMDBuild.model.core.configurations.builder.Instance.convertFromLegacy(data);

			this.callParent(arguments);
		}
	});

})();
