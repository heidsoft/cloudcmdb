(function($) {
	var widgets = {
		// methods for save data. 
		SAVEONCARD: "SAVEONCARD",
		SAVEAFTER: "SAVEAFTER",
		// there are widgets that save data directly in the card on which they are. (LinkCards)
		// there are widgets that save data in separated calls at server (OpenAttachment)
		getWidgetsData: function(form, widgets) {
			var widgetsData = {};
			if (! widgets) {
				return widgetsData;
			}
			for (var i = 0; i < widgets.length; i++) {
				var widget = $.Cmdbuild.standard.widgetDiv.widgetName(widgets[i].type); 
				if ($.Cmdbuild.widgets[widget]) {
					widgetsData[widgets[i]._id] = {
						data: $.Cmdbuild.widgets[widget].save(form, widgets[i]),
						saveType: $.Cmdbuild.widgets[widget].saveMethod,
						widgetType: widgets[i].type
					};
				}
				else {
					console.log("Widget " + widget + " not found!");
				}
			}	
			return widgetsData;
		},
		saveOnDataWidgets: function(card, widgetData) {
			var _widgets = [];
			for(var key in widgetData) {
				var widget = widgetData[key];
				var output = {};
				if (widget.saveType == $.Cmdbuild.widgets.SAVEONCARD) {
					for(var dkey in widget.data) {
						output[dkey] = {};
					}
					_widgets.push({
						_id: key,
						output: output
					});
				}
			}	
			card["_widgets"] = _widgets;
		},
		savePostponedWidgets: function(widgets, widgetData, param, parent_form, callback, callbackScope) {
			var widgetsArray = widgets.slice();
			this.savePostponedWidgetsRecursive(widgetsArray, widgetData, param, parent_form, function() {
				callback.apply(callbackScope, []);
			}, this);
			
		},
		savePostponedWidgetsRecursive: function(widgets, widgetData, param, parent_form, callback, callbackScope) {
			if (widgets.length <= 0) {
				callback.apply(callbackScope, []);
				return;
			}
			var widget = widgets[0];
			widgets.splice(0, 1);
			var data = widgetData[widget._id];
			if (! data) {
				callback.apply(callbackScope, []);
				return;
			}
			if (data.saveType == $.Cmdbuild.widgets.SAVEAFTER) {
				var widgetName = $.Cmdbuild.standard.widgetDiv.widgetName(data.widgetType);
				$.Cmdbuild.widgets[widgetName].flush(param, widgetData[widget._id].data, widget._id, parent_form, function() {
					this.savePostponedWidgetsRecursive(widgets, widgetData, param, parent_form, callback, callbackScope);
				}, this);
			} else {
				this.savePostponedWidgetsRecursive(widgets, widgetData, param, parent_form, callback, callbackScope);
			}
		},
		evaluateCqlFields: function(form, widgets) {
			for (var i = 0; i < widgets.length; i++) {
				var widget = widgets[i];
				var widgetName = $.Cmdbuild.standard.widgetDiv.widgetName(widget.type);
				if ($.Cmdbuild.widgets[widgetName] && $.Cmdbuild.widgets[widgetName].evaluateCql) {
					$.Cmdbuild.widgets[widgetName].evaluateCql(form, widget);
				}
			}
		},
		initialize: function(form, widgets) {
			for (var i = 0; i < widgets.length; i++) {
				var widget = widgets[i];
				var widgetName = $.Cmdbuild.standard.widgetDiv.widgetName(widget.type);
				if ($.Cmdbuild.widgets[widgetName] && $.Cmdbuild.widgets[widgetName].initialize) {
					$.Cmdbuild.widgets[widgetName].initialize(form, widget);
				}
			}
		},
		refreshCqlField: function(form, widgets) {
			if (widgets === undefined) {
				widgets = [];
			}
			for (var i = 0; i < widgets.length; i++) {
				var widget = widgets[i];
				var widgetName = $.Cmdbuild.standard.widgetDiv.widgetName(widget.type);
				if ($.Cmdbuild.widgets[widgetName] && $.Cmdbuild.widgets[widgetName].refreshCqlField) {
					$.Cmdbuild.widgets[widgetName].refreshCqlField(form, widget);
				}
			}
		},
		refreshTemplate: function(form, widgets, templateName) {
			for (var i = 0; i < widgets.length; i++) {
				var widget = widgets[i];
				var widgetName = $.Cmdbuild.standard.widgetDiv.widgetName(widget.type);
				if ($.Cmdbuild.widgets[widgetName] && $.Cmdbuild.widgets[widgetName].refreshTemplate) {
					$.Cmdbuild.widgets[widgetName].refreshTemplate(form, widgets[i], templateName);
				}
			}
		},
		prepareFields: function(widgets) {
			for (var i = 0; i < widgets.length; i++) {
				var widget = widgets[i];
				var widgetName = $.Cmdbuild.standard.widgetDiv.widgetName(widget.type);
				if ($.Cmdbuild.widgets[widgetName] && $.Cmdbuild.widgets[widgetName].prepareFields) {
					$.Cmdbuild.widgets[widgetName].prepareFields(widget);
				}
			}
		}
	};
	$.Cmdbuild.widgets = widgets;
}) (jQuery);
