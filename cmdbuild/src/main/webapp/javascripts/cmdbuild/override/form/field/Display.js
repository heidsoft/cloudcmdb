(function() {

	Ext.define('CMDBuild.override.form.field.Display', {
		override: 'Ext.form.field.Display',

		requires: ['CMDBuild.core.Utils'],

		/**
		 * Avoids Display fields to strip \n on contents
		 *
		 * @param {Mixed} rawValue
		 * @param {Mixed} fieldObject
		 *
		 * @return {String}
		 */
		renderer: function(rawValue, fieldObject) {
			if (!CMDBuild.core.Utils.hasHtmlTags(rawValue))
				return rawValue.replace(/(\r\n|\n|\r)/gm, '<br />');

			return rawValue;
		}
	});

})();