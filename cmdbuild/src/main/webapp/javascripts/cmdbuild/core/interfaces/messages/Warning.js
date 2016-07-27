(function() {

	Ext.define('CMDBuild.core.interfaces.messages.Warning', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message'
		],

		singleton: true,

		/**
		 * @param {Object} decodedResponse
		 */
		display: function(decodedResponse) {
			if (
				!Ext.isEmpty(decodedResponse)
				&& !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.WARNINGS]) && Ext.isArray(decodedResponse[CMDBuild.core.constants.Proxy.WARNINGS])
			) {
				Ext.Array.forEach(decodedResponse[CMDBuild.core.constants.Proxy.WARNINGS], function(message, i, allMessages) {
					if (!Ext.Object.isEmpty(message))
						CMDBuild.core.interfaces.messages.Warning.showPopup(message);
				}, this);
			}
		},

		/**
		 * @param {String} reasonName
		 * @param {Object} reasonParameters
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		formatMessage: function(reasonName, reasonParameters) {
			if (
				!Ext.isEmpty(CMDBuild.Translation.errors.reasons)
				&& !Ext.isEmpty(CMDBuild.Translation.errors.reasons[reasonName])
			) {
				return Ext.String.format.apply(null, [].concat(CMDBuild.Translation.errors.reasons[reasonName]).concat(reasonParameters));
			} else {
				_error('"' + reasonName + '" translation not found', 'CMDBuild.core.interfaces.messages.Warning');
			}

			return '';
		},

		/**
		 * @param {Object} message
		 *
		 * @private
		 */
		showPopup: function(message) {
			if (!Ext.Object.isEmpty(message)) {
				var formattedMessage = CMDBuild.core.interfaces.messages.Warning.formatMessage(message.reason, message.reasonParameters);

				if (Ext.isEmpty(formattedMessage)) {
					_error('cannot format warning message from "' + message + '"', 'CMDBuild.core.interfaces.messages.Warning');
				} else {
					CMDBuild.core.Message.warning(null, formattedMessage);
				}
			}
		}
	});

})();