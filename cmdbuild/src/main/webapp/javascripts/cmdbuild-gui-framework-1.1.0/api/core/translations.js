(function($) {
	var translations = {
		translations : {},

		loadTranslations : function(callback, callbackScope) {
			var me = this;
			var payload = {
				callback : callback,
				callbackScope : callbackScope
			};
			var language = $.Cmdbuild.global.getLanguage();
			if (!language) {
				$.Cmdbuild.errorsManager.warn("No language selected");
				callback.apply(callbackScope, []);
				return;
			}
			var url = $.Cmdbuild.global.getAppConfigUrl() + "translations/"
					+ language + ".xml";

			// check if exists translation
			$.ajax({
				type : 'HEAD',
				url : url
			}).done(function() {
				$.Cmdbuild.utilities.getXmlDoc(url,
						me.loadTranslationsCB, me, payload);
			}).fail(function() {
				callback.apply(callbackScope, []);
				$.Cmdbuild.errorsManager
						.warn("No translation file found for language "
								+ language.toUpperCase());
			});
		},
		loadTranslationsCB : function($xmlDoc, payload) {
			var root = $xmlDoc.documentElement;
			var $root = $(root);
			var me = this;

			$.each($root.children(), function(index, translation) {
				me.translations[translation.tagName] = translation.textContent;
			});

			// callback
			var callback = payload.callback;
			var callbackScope = payload.callbackScope;
			callback.apply(callbackScope, []);
		},

		getTranslations : function() {
			return this.translations;
		},

		getTranslation : function(label, default_value) {
			if (this.getTranslations() && this.getTranslations()[label]) {
				return this.getTranslations()[label];
			}
			// log warning
			var language = $.Cmdbuild.global.getLanguage();
			if (language) {
				$.Cmdbuild.errorsManager.warn("No translation found for label "
						+ label + " into " + language.toUpperCase() + " language file");
			}
			// return default value if set, else label
			return default_value ? default_value : label;
		}
	};

	$.Cmdbuild.translations = translations;
})(jQuery);