(function () {

	Ext.define('CMDBuild.core.interfaces.messages.Error', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message'
		],

		singleton: true,

		/**
		 * @param {Object} decodedResponse
		 * @param {Object} options
		 */
		display: function (decodedResponse, options) {
			if (
				!Ext.isEmpty(decodedResponse)
				&& !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.ERRORS]) && Ext.isArray(decodedResponse[CMDBuild.core.constants.Proxy.ERRORS])
			) {
				Ext.Array.forEach(decodedResponse[CMDBuild.core.constants.Proxy.ERRORS], function (message, i, allMessages) {
					if (!Ext.Object.isEmpty(message))
						CMDBuild.core.interfaces.messages.Error.showPopup(message, options);
				}, this);
			}
		},

		/**
		 * Custom error messages implementation
		 *
		 * @param {String} reasonName
		 * @param {Object} reasonParameters
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		formatMessage: function (reasonName, reasonParameters) {
			if (
				!Ext.isEmpty(CMDBuild.Translation.errors.reasons)
				&& !Ext.isEmpty(CMDBuild.Translation.errors.reasons[reasonName])
			) {
				return Ext.String.format.apply(null, [].concat(CMDBuild.Translation.errors.reasons[reasonName]).concat(reasonParameters));
			} else {
				_error('"' + reasonName + '" translation not found', 'CMDBuild.core.interfaces.messages.Error');
			}

			return '';
		},

		/**
		 * @param {Object} message
		 * @param {Object} options
		 *
		 * @private
		 */
		showPopup: function (message, options) {
			var errorTitle = null;
			var errorBody = {
				text: CMDBuild.Translation.errors.anErrorHasOccurred,
				detail: undefined
			};

			if (!Ext.Object.isEmpty(message)) {
				var detail = '';
				var reason = message.reason;

				// Add URL that generate the error
				if (
					!Ext.Object.isEmpty(options)
					&& !Ext.isEmpty(options.url)
				) {
					detail = 'Call: ' + options.url + '\n';

					var line = '';

					for (var i = 0; i < detail.length; ++i)
						line += '-';

					detail += line + '\n';
				}

				detail += 'Error: ' + message.stacktrace; // Add to the details the server stacktrace

				errorBody.detail = detail;

				if (!Ext.isEmpty(reason)) {
					if (reason == 'AUTH_NOT_LOGGED_IN' || reason == 'AUTH_MULTIPLE_GROUPS') {
						Ext.create('CMDBuild.controller.common.sessionExpired.SessionExpired', {
							ajaxParameters: options,
							passwordFieldEnable: reason == 'AUTH_NOT_LOGGED_IN'
						});

						return;
					}

					var errorString = CMDBuild.core.interfaces.messages.Error.formatMessage(reason, message.reasonParameters);

					if (Ext.isEmpty(errorString)) {
						_error('cannot format error message from "' + message + '"', 'CMDBuild.core.interfaces.messages.Error');
					} else {
						errorBody.text = errorString;
					}
				}
			} else {
				if (
					Ext.isEmpty(response)
					|| response.status == 200
					|| response.status == 0
				) {
					errorTitle = CMDBuild.Translation.errors.error_message;
					errorBody.text = CMDBuild.Translation.errors.anErrorHasOccurred;
				} else if (response.status) {
					errorTitle = CMDBuild.Translation.errors.error_message;
					errorBody.text = CMDBuild.Translation.errors.server_error_code + response.status;
				}
			}

			CMDBuild.core.Message.error(
				errorTitle,
				errorBody,
				options.form
			);
		}
	});

})();
