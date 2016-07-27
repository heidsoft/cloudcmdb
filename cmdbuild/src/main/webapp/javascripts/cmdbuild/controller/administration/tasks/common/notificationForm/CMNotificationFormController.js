(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController', {

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @property {Array}
		 * 		ex. [
		 * 			'internalId': { Input object },
		 * 			...
		 * 		]
		 */
		inputFields: [],

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {Boolean} returnBoolean
		 */
		isEmpty: function() {
			var returnBoolean = true;

			for (var field in this.inputFields)
				if (!Ext.isEmpty(this.inputFields[field]))
					returnBoolean = Ext.isEmpty(this.inputFields[field].getValue());

			return returnBoolean;
		},

		// SETters functions
			/**
			 * Set fields as required/unrequired
			 *
			 * @param {Boolean} state
			 */
			setAllowBlankFields: function(state) {
				for (var field in this.inputFields)
					if (!Ext.isEmpty(this.inputFields[field]))
						this.inputFields[field].allowBlank = state;
			},

			/**
			 * @param {String} internalId
			 * @param {String} value
			 */
			setValue: function(internalId, value) {
				var inputField = this.inputFields[internalId];

				if (!Ext.isEmpty(inputField) && !Ext.isEmpty(value))
					inputField.setValue(value);
			},

		/**
		 * Notification form validation
		 *
		 * @param {Boolean} enable
		 */
		validate: function(enable) {
			this.setAllowBlankFields(
				!(this.isEmpty() && enable)
			);
		}
	});

})();