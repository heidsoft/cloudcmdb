(function($) {

	var popup = function() {
		this.param = undefined;
		this.init = function(param) {
			try {
				this.param = param;
				this.show();
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.div.init");
				throw e;
			}
		};
		this.show = function() {
			try {
				$.Cmdbuild.eventsManager.deferEvents();
				var theDialog = $("#" + this.param.dialog);
				var xmlForm = $.Cmdbuild.elementsManager.getElement(this.param.form);
				var htmlStr = $.Cmdbuild.elementsManager.insertChildren(xmlForm);
				theDialog.html(htmlStr);
				
				theDialog.dialog( "option", "dialogClass", $.Cmdbuild.global.getThemeCSSClass());

				var w = this.param.width;
				var h = this.param.height;
				theDialog.dialog( "option", "height", h || $.Cmdbuild.global.modalMaxHeight);
				theDialog.dialog( "option", "width", w || $.Cmdbuild.global.modalMaxWidth);
				
				var title = this.param.title;
				var i18nTitle = this.param.i18nTitle;
				if (i18nTitle) {
					title = $.Cmdbuild.translations.getTranslation(i18nTitle, title);
				}
				theDialog.dialog("option", "title", title).dialog("open");

				$.Cmdbuild.elementsManager.initialize();
				$.Cmdbuild.eventsManager.unDeferEvents();
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.div.show");
				throw e;
			}
		};
	};

	$.Cmdbuild.standard.popup = popup;

}) (jQuery);