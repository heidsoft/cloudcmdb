(function($) {
	var div = function() {
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
				var xmlForm = $.Cmdbuild.elementsManager.getElement(this.param.form);
				$.Cmdbuild.eventsManager.deferEvents();
				var htmlStr = "";
				htmlStr += $.Cmdbuild.elementsManager.insertChildren(xmlForm);
				var customWidgets = $.Cmdbuild.utilities.getWidgetsFromXML(xmlForm);
				var widgets = [];
				if (customWidgets && customWidgets.length) {
					$.each(customWidgets, function(index, widget) {
						widgets.push(widget);
					});
//					this.param.backend.widgets = widgets;
				}
				if (widgets && widgets.length) {
					$.Cmdbuild.widgets.prepareFields(widgets);
					htmlStr += $.Cmdbuild.standard.widgetDiv.getWidgetDiv({
						container: this.param.container,
						form: this.param.form,
						readOnly: this.param.readonly,
						widgets: widgets
					});
				}
				var htmlContainer = $("#" + this.param.container)[0];
				htmlContainer.innerHTML = htmlStr;
				$.Cmdbuild.elementsManager.initialize();
				$.Cmdbuild.eventsManager.unDeferEvents();
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.div.show");
				throw e;
			}
		};		
	};
	$.Cmdbuild.standard.div = div;
}) (jQuery);
