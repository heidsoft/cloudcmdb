(function() {

	/**
	 * Custom VTypes:
	 * 	- alphanumextended: to validate user names (alphanum and _ - .)
	 * 	- alphanumlines: alphanumeric and lines ("_" and "-")
	 * 	- comment: all except pipe (all excluded |)
	 * 	- commentextended: all except pipe and apostrophe (all excluded | ')
	 * 	- ipv4: ipv4 validation (CIDR support)
	 * 	- ipv6: ipv6 validation (CIDR support)
	 * 	- multimail: to validate a field with multiple email addresses separated by commas (,)
	 * 	- password: to validate password fields with confirmation capabilities
	 */
	Ext.apply(Ext.form.field.VTypes, {
		// Alpha-numeric extended (alphanumextended)
			/**
			 * @param {String} value
			 *
			 * @returns {Boolean}
			 */
			alphanumextended: function (value) {
				return this.alphanumextendedMask.test(value);
			},

			/**
			 * @type {String}
			 */
			alphanumextendedText: 'This field should only contain letters, numbers, underscore (_), hyphen (-). dot (.), hash (#) and at (@)',

			/**
			 * @type {RegExp}
			 */
			alphanumextendedMask: /^[a-zA-Z0-9_.+#@-]+$/i,

		// Alpha-numeric lines (alphanumlines)
			/**
			 * @param {String} value
			 *
			 * @returns {Boolean}
			 */
			alphanumlines: function (value) {
				return this.alphanumlinesMask.test(value);
			},

			/**
			 * @type {String}
			 */
			alphanumlinesText: 'This field should only contain letters, numbers, underscore (_) and hyphen (-)',

			/**
			 * @type {RegExp}
			 */
			alphanumlinesMask: /^[a-zA-Z0-9_-]+$/i,

		// Comment (comment)
			/**
			 * @param {String} value
			 *
			 * @returns {Boolean}
			 */
			comment: function (value) {
				return this.commentMask.test(value);
			},

			/**
			 * @type {String}
			 */
			commentText: CMDBuild.Translation.vtypeCommentText,

			/**
			 * @type {RegExp}
			 */
			commentMask: /^[^|]*$/i,

		// Comment extended (commentextended)
			/**
			 * @param {String} value
			 *
			 * @returns {Boolean}
			 */
			commentextended: function (value) {
				return this.commentextendedMask.test(value);
			},

			/**
			 * @type {String}
			 */
			commentextendedText: CMDBuild.Translation.vtypeCommentExtendedText,

			/**
			 * @type {RegExp}
			 */
			commentextendedMask: /^[^'|]*$/i,

		// IPv4 (ipv4)
			/**
			 * @param {String} value
			 *
			 * @return {Boolean}
			 */
			ipv4: function (value) {
				return this.ipv4RegExp.test(value);
			},

			/**
			 * @type {RegExp}
			 */
			ipv4RegExp: /^([0-9]{1,3}\.){3}[0-9]{1,3}(\/([0-9]|[1-2][0-9]|3[0-2]))?$/,

			/**
			 * @type {RegExp}
			 */
			ipv4Mask: /[0-9.\/]/i,

			/**
			 * @type {String}
			 */
			ipv4Text: CMDBuild.Translation.vtypeIpText,

		// IPv6 (ipv6)
			/**
			 * @param {String} value
			 *
			 * @return {Boolean}
			 */
			ipv6: function (value) {
				return this.ipv6RegExp.test(value);
			},

			/**
			 * @type {RegExp}
			 */
			ipv6Mask: /[0-9a-fA-F:\/]/i,

			/**
			 * @type {RegExp}
			 */
			ipv6RegExp: /^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*(\/([0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8]))?$/,

			/**
			 * @type {String}
			 */
			ipv6Text: CMDBuild.Translation.vtypeIpText,

		// Multiple email addresses (multimail)
			/**
			 * The function used to validated multiple email addresses on a single line
			 *
			 * @param {String} value - The email addresses separated by a comma or semicolon
			 *
			 * @returns {Boolean}
			 */
			multiemail: function (value) {
				var array = value.split(',');
				var valid = true;

				Ext.Array.each(array, function (value) {
					if (!this.email(value)) {
						valid = false;

						return false;
					}
				}, this);

				return valid;
			},

			/**
			 * The error text to display when the multi email validation function returns false
			 *
			 * @type {String}
			 */
			multiemailText: 'This field should be an e-mail address, or a list of email addresses separated by commas (,) in the format "user@domain.com,test@test.com"',

			/**
			 * The keystroke filter mask to be applied on multi email input
			 *
			 * @type {RegExp}
			 */
			multiemailMask: /[\w.\-@'"!#$%&'*+/=?^_`{|}~,]/i,

		// Password (password)
			/**
			 * @param {String} value
			 * @param {Object} field
			 *
			 * @returns {Boolean}
			 */
			password: function (value, field) {
				if (
					!Ext.isEmpty(field.twinFieldId) && Ext.isString(field.twinFieldId)
					&& !Ext.isEmpty(Ext.getCmp(field.twinFieldId)) && Ext.isFunction(Ext.getCmp(field.twinFieldId).getValue)
				) {
					return value == Ext.getCmp(field.twinFieldId).getValue();
				}

				return true;
			},

			/**
			 * The error text to display when the password validation function returns false
			 *
			 * @type {String}
			 */
			passwordText: CMDBuild.Translation.passwordsDoNotMatch,

		// Time fields (time)
			/**
			 * @param {String} value
			 * @param {Object} field
			 *
			 * @returns {Boolean}
			 */
			time: function (value, field) {
				return Ext.Date.parse(value, field.format);
			},

			/**
			 * The error text to display when the time validation function returns false
			 *
			 * @type {String}
			 */
			timeText: CMDBuild.Translation.vtypeTimeText
	});

})();
