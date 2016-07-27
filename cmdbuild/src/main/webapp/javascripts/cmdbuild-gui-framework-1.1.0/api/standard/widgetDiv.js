(function($) {
	var widgetDiv = {
		getWidgetDiv: function(param) {
			try {
				htmlStr = "";
				var xmlElement = this.createXmlDiv(param.form, param.widgets, param.container, param.readOnly);
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
				htmlStr += this.createAllPopups(param.form, param);
				return htmlStr;
			}
			catch (e) {
				$.Cmdbuild.errorsManager.popup(e);
			}
		},
		createXmlDiv: function(form, widgets, container, readOnly) {
			var xmlStr = "<div";
			xmlStr += " class='ui-widget-content ui-corner-all cmdbuild-widget-container' ";
			xmlStr += " withReturn='true' ";
			xmlStr += ">";
//			xmlStr += this.createXmlTitle("Widgets");
			for (var i = 0; i < widgets.length; i++) {
				xmlStr += this.createXmlButton(form, widgets[i], container, readOnly);
			}
			xmlStr += "</div>";
			for (var i = 0; i < widgets.length; i++) {
				var widget = $.Cmdbuild.standard.widgetDiv.widgetName(widgets[i].type); 
				if ($.Cmdbuild.widgets[widget]) {
					$.Cmdbuild.widgets[widget].cleanData(form, widgets[i]);
				}
			}
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		},
//		createXmlTitle: function(title) {
//			return "<span text='" + title + "' class='page-title'/>";
//		},
		createXmlButton: function(form, widget, container, readOnly) {
			var id = form + "_" + widget._id;
			var xmlStr = "<button ";
			xmlStr += " id='" + id + "' ";
			xmlStr += " text='" + widget.label + "' ";
			xmlStr += ">";
			xmlStr += "<onClick>";
			xmlStr += "<command>navigate</command>";
			xmlStr += "<dialog>" + id + "_widgetDialog</dialog>";
			xmlStr += "<container>" + container + "</container>";
			xmlStr += "<form>" + id + "_dialogForm" + "</form>";
			xmlStr += "<title>" + widget.label + "</title>";
			xmlStr += "</onClick>";
			xmlStr += "<params>";
			xmlStr += "<dialog>" + id + "_widgetDialog</dialog>";
			xmlStr += "<widgetId>" + id + "</widgetId>";
			xmlStr += "<widgetClassName>" + widget.data.className + "</widgetClassName>";
			xmlStr += "<singleSelect>" + widget.data.singleSelect + "</singleSelect>";
			xmlStr += "<formData>" + form + "</formData>";
			xmlStr += "<selection>" + (widget.data.noSelect == 1 ? "false" : "true") + "</selection>";
			xmlStr += "<editable>" + widget.data.allowCardEditing + "</editable>";
			xmlStr += "<readOnly>" + ((readOnly == "true") ? "true" : "false") + "</readOnly>";			
			xmlStr += "<widgetName>" + id + "</widgetName>";			
			xmlStr += "</params>";
			xmlStr += "</button>";
			return xmlStr;
		},
		createXmlDialog: function(id) {
			var xmlStr = "<dialog id='" + id + "_widgetDialog'></dialog>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			return xDoc.documentElement;
		},
		createAllPopups: function(form, param) {
			var htmlStr = "";
			for (var i = 0; i < param.widgets.length; i++) {
				var id = form + "_" + param.widgets[i]._id;
				var widgetObj = {
						data: param.widgets[i].data, //.templates,
						form: form
				};
				// initialization grid (selections have to be reset)
				// default selection have to be set up
				if (! $.Cmdbuild.dataModel.getWidgetWindow(id)) {
					var xmlElement = this.createXmlDialog(id);
					htmlStr += $.Cmdbuild.elementsManager.toHtml(xmlElement);
					$.Cmdbuild.dataModel.putWidgetWindow(id, widgetObj);
				} else {
					$.Cmdbuild.dataModel.substituteWidgetWindow(id, widgetObj);
				}
				this.widgetForm(id, param, param.widgets[i]);
			}
			return htmlStr;
		},
		widgetForm : function(id, param, widget) {
			var xmlStr = "<form title='Example' id='" + id + "_dialogForm" + "'";
			if (widget.data && widget.data.template) {
				xmlStr += " include=\"" + widget.data.template + "\"";
			} else {
				var file = $.Cmdbuild.standard.widgetDiv.widgetName(widget.type) + ".xml";
				xmlStr += " include='widgets/" + file + "'";
				xmlStr += " fileType='core'";
			}
			xmlStr += " withId='" + id + "'";
			xmlStr += " class='cmdbuildTabbedForm'/>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			var xmlElement = xDoc.documentElement;
			var xmlContainer = $.Cmdbuild.elementsManager.getElement(param.container);
			xmlContainer.appendChild(xmlElement);
		},
		getFiter: function(widgetObj) {
			var filter = "";
			return filter;
		}
	};
	$.Cmdbuild.standard.widgetDiv = widgetDiv;
	// statics
	$.Cmdbuild.standard.widgetDiv.widgetName = function(type) {
		var values = type.split(".");
		// widgets are in the form ".name" but we want remove the point
		return values[values.length - 1];
	};

}) (jQuery);
