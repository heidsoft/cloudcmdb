(function($) {
	var dataModel = {
		model: [],
		observers: {},
		forms: {},
		formFields: {},
		callerParameters: {},
		parameters: {},
		widgets: {},
		attachments : {},
		classes: [],
		processes: [],
		observing: true,
		isAProcess: function(name) {
			return $.grep(this.processes, function(e){ return e._id == name; }).length > 0;
		},
		isAClass: function(name) {
			return $.grep(this.classes, function(e){ return e._id == name; }).length > 0;
		},
		setClasses : function(items) {
			this.classes = items;
		},
		setProcesses : function(items) {
			this.processes = items;
		},
		cleanForm: function(name) {
			if (this.forms[name]) {
				delete this.forms[name];
			}
		},
		substituteWidgetWindow: function(id, object) {
			this.widgets[id] = object;
		},
		putWidgetWindow: function(id, object) {
			this.widgets[id] = object;
		},
		getWidgetWindow: function(id) {
			return this.widgets[id];
		},
		prepareCallerParameters: function(id, param) {
			$.Cmdbuild.dataModel.callerParameters[id] = param;
		},
		pushSingleParameterOnStack: function(key, value) {
			$.Cmdbuild.dataModel.parameters[key] = value;
		},
		pushParametersOnStack: function(id) {
			var parameters = $.Cmdbuild.dataModel.callerParameters[id];
			for (var key in parameters) {
				var parameter = parameters[key];
				$.Cmdbuild.dataModel.parameters[key] = parameter;
			}
		},
		getParameter: function(name) {
			return $.Cmdbuild.dataModel.parameters[name];
		},
		deleteParameter: function(name) {
			delete $.Cmdbuild.dataModel.parameters[name];
		},
		addFields: function(fields, backendAttributes) {
			var attributes = [];
			for (var key in fields) {
				for (var i = 0; i < backendAttributes.length; i++) {
					var attribute = backendAttributes[i];
					if (key == attribute._id) {
						attribute.displayableInList = true;
						for (k in fields[key]) {
							attribute[k] = fields[key][k];
						}
//						if (fields[key].interactivity) {
//							attribute.interactivity = fields[key].interactivity;
//						}
						attributes.push(attribute);
						break;
					}
				}
			}
			return attributes;
		},
		evaluateFields: function(fields, backendAttributes) {
			var attributes = [], me = this;
			for (var i = 0; i < backendAttributes.length; i++) {
				var attribute = backendAttributes[i];
				for (var key in fields) {
					if (key == attribute._id) {
						var field = fields[key];
						if (jQuery.isEmptyObject(field)) {
							attribute = null;
							break;
						}
						if (field) {
							for(var k in field) {
								if (k === "interactivity") {
									$.Cmdbuild.errorsManager.warn("Interactivity is deprecated. Use hidden or writable properties");
									if (field[k] === $.Cmdbuild.global.READ_ONLY) {
										attribute["writable"] = false;
										attribute["mandatory"] = false;
									} else if (field[k] === $.Cmdbuild.global.READ_WRITE_REQUIRED) {
										attribute["writable"] = true;
										attribute["mandatory"] = true;
									} else if (field[k] === $.Cmdbuild.global.READ_WRITE) {
										attribute["writable"] = true;
										attribute["mandatory"] = false;
									}
								} else {
									attribute[k]= me.resolveVariable(field[k]);
								}
							}
						}
						break;
					}
				}
				if (attribute) {
					attributes.push(attribute);
				}
			}
			return attributes;
		},
		evaluateXmlAttributes: function(form, backendAttributes) {
			// this routine get the backend's attributes reference and select attributes from the Xml <Fields> Tag
			// it has also to reorder the attributes following the order indicate on the Xml
			// if there is no a Xml fields tag it does nothing
			if (! $.Cmdbuild.dataModel.formFields[form]) {
				return;
			}
			var formFields = $.Cmdbuild.dataModel.getFormFields(form);
			var fields = formFields.fields;
			var attributes = (formFields.method == "evaluate") ? 
					this.evaluateFields(fields, backendAttributes) :
					this.addFields(fields, backendAttributes);
						
			// the reference backendAttributes have to be respected (the attibutes have to stay on the backend)
			// the alteranative is put a substitute method on backends but i find it verbose
			// because in that case every backend has to implement the method
			backendAttributes.splice(0, backendAttributes.length); // delete all
			for (var i = 0; i < attributes.length; i++) {
				backendAttributes.push(attributes[i]);
			}
		},
		evaluateJSONAttributes : function(backendAttributes, customAttributes) {
			if (customAttributes) {
				var attributes = (customAttributes.method == "evaluate") ? 
						this.evaluateFields(customAttributes.fields, backendAttributes) :
							this.addFields(customAttributes.fields, backendAttributes);
				// the reference backendAttributes have to be respected (the attibutes have to stay on the backend)
				// the alteranative is put a substitute method on backends but i find it verbose
				// because in that case every backend has to implement the method
				backendAttributes.splice(0, backendAttributes.length); // delete all
				for (var i = 0; i < attributes.length; i++) {
					backendAttributes.push(attributes[i]);
				}
			} 
		},
		getFormFields: function(form) {
			if (! $.Cmdbuild.dataModel.formFields[form]) {
				return undefined;
			}
			return $.Cmdbuild.dataModel.formFields[form];
		},
		putFormFields: function(fields) {
			if (! fields.fields) {
				delete $.Cmdbuild.dataModel.formFields[fields.form];
			}
			else {
				$.Cmdbuild.dataModel.formFields[fields.form] = fields;
			}
		},
		getCurrentIndex: function(form) {
			return $.Cmdbuild.dataModel.model[form].currentIndex;
		},
		setCurrentIndex: function(form, index) {
			$.Cmdbuild.dataModel.model[form].currentIndex = index;
			$.Cmdbuild.dataModel.dispatchChange(form);
		},
		setValues: function(form, data) {
			if (! $.Cmdbuild.dataModel.model[form]) {
				return {};
			}
			var index = $.Cmdbuild.dataModel.model[form].currentIndex;
			if (index == -1) {
				return {};
			}
			else if (index !== undefined) {
				$.extend($.Cmdbuild.dataModel.model[form].data[index], data);
			}
			else {
				$.extend($.Cmdbuild.dataModel.model[form].data, data);
			}
		},
		getData: function(form) {
			if (! ($.Cmdbuild.dataModel.model[form] && $.Cmdbuild.dataModel.model[form].data)) {
				return [];
			}
			return $.Cmdbuild.dataModel.model[form].data;
		},
		getValues: function(form) {
			$.Cmdbuild.standard.contextStack.getFormPath({
				form: form
			});
			if (! $.Cmdbuild.dataModel.model[form]) {
				return "";
			}
			var index = $.Cmdbuild.dataModel.model[form].currentIndex;
			if (index == -1) {
				return "";
			}
			else if (index !== undefined) {
				return $.Cmdbuild.dataModel.model[form].data[index];
			}
			else {
				return $.Cmdbuild.dataModel.model[form].data;
			}
			
		},
		getValue: function(form, name) {
			if (! $.Cmdbuild.dataModel.model[form]) {
				return "";
			}
			var index = $.Cmdbuild.dataModel.model[form].currentIndex;
			if (index == -1) {
				return "";
			}
			else if (index !== undefined) {
				return $.Cmdbuild.dataModel.model[form].data[index][name];
			}
			else {
				return $.Cmdbuild.dataModel.model[form].data[name];
			}
			
		},
		push: function(param) {
			try {
				$.Cmdbuild.dataModel.model[param.form] = {
					type: param.type,
					currentIndex: param.currentIndex,
					data: param.data,
					backup : $.Cmdbuild.utilities.clone(param.data)
				};
			}
			catch (e) {
				e.message += "\n" +"$.Cmdbuild.dataModel.push";
				$.Cmdbuild.errorsManager.log(e);
			}
		},
		flush: function(param, callback, callbackScope) {
			try {
				// try to save data. data are to be changed first
				param.data = $.Cmdbuild.dataModel.model[param.form].data;
				param.formObject.flush(param, function(response) {
					$.Cmdbuild.dataModel.model[param.form].backup = $.Cmdbuild.utilities.clone($.Cmdbuild.dataModel.model[param.form].data);
					$.Cmdbuild.dataModel.dispatchChange(param.form);
					// success message
					if (param.successMessage || param.i18nSuccessMessage) {
						var message = param.successMessage;
						if (param.i18nSuccessMessage) {
							message = $.Cmdbuild.translations.getTranslation(
											param.i18nSuccessMessage,
											param.successMessage
													? param.successMessage
													: param.i18nSuccessMessage);
						}
						$('<div />').html(message).dialog({
							dialogClass : "no-close",
							buttons : [{
								text : "OK",
								click : function() {
									$(this).dialog("destroy");
								}
							}]
						});
					}
					callback.apply(callbackScope, [response]);
				}, this);
			}
			catch (e) {
				$.Cmdbuild.dataModel.model[param.form].data = $.Cmdbuild.utilities.clone($.Cmdbuild.dataModel.model[param.form].backup);
				$.Cmdbuild.dataModel.reset(param);
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.dataModel.flush");
				throw e;
			}
		
		},
		reset: function(param) {
			try {
				param.data = $.Cmdbuild.dataModel.model[param.form].data;
				param.formObject.reset(param);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.dataModel.reset");
				throw e;
			}
		},
		change: function(param) {
			try {
				param.data = $.Cmdbuild.dataModel.model[param.form].data;
				var errors = param.formObject.change(param);
				if (errors.length > 0) {
					$.Cmdbuild.errorsManager.popupOnRequestFields(errors);
					return false;
				}
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.dataModel.change");
				throw e;
			}
			return true;
		},
		resolveParametersVariable: function(field) {
			var variable = $.Cmdbuild.dataModel.parameters[field];
			var val = $.Cmdbuild.dataModel.resolveVariable(variable);
			return val;
		},
		resolveInterfaceVariable: function(field) {
			var val = $.Cmdbuild.utilities.getHtmlFieldValue("#" + field);
			return val;
		},
		resolveWindowVariable: function(field) {
			return eval("window." + field);
		},
		resolveCMDBuildVariable: function(field) {
			return $.Cmdbuild.customvariables[field];
		},
		resolveModelVariable: function(form, field) {
			var value = $.Cmdbuild.dataModel.getValue(form, field);
			return value;
		},
		resolveAnd: function(text) {
			text = $.trim(text);
			text = text.substr(1);
			text = text.substr(0, text.length - 1);
			var ar = text.split(",");
			for (var i = 0; i < ar.length; i++) {
				if (! $.Cmdbuild.dataModel.resolveVariable($.trim(ar[i]))) {
					return false;
				}
			}
			return true;
		},
		resolveAnd: function(text) {
			text = $.trim(text);
			text = text.substr(1);
			text = text.substr(0, text.length - 1);
			var ar = text.split(",");
			for (var i = 0; i < ar.length; i++) {
				if (! $.Cmdbuild.dataModel.resolveVariable($.trim(ar[i]))) {
					return false;
				}
			}
			return true;
		},
		resolveOr: function(text) {
			text = $.trim(text);
			text = text.substr(1);
			text = text.substr(0, text.length - 1);
			var ar = text.split(",");
			for (var i = 0; i < ar.length; i++) {
				if ($.Cmdbuild.dataModel.resolveVariable($.trim(ar[i]))) {
					return true;
				}
			}
			return false;
		},
		resolveNot: function(text) {
			text = $.trim(text);
			text = text.substr(1);
			text = text.substr(0, text.length - 1);
			return ! $.Cmdbuild.dataModel.resolveVariable($.trim(text));
		},
		resolveVariable: function(variable) {
 			var ret = "";
 
			if (("" + variable).substr(0, 4) == "$and") {
 				ret = $.Cmdbuild.dataModel.resolveAnd(variable.substr(4));
 			}
			else if (("" + variable).substr(0, 3) == "$or") {
 				ret = $.Cmdbuild.dataModel.resolveOr(variable.substr(3));
 			}
			else if (("" + variable).substr(0, 4) == "$not") {
 				ret = $.Cmdbuild.dataModel.resolveNot(variable.substr(4));
 			}
 			else if (("" + variable).substr(0, 1) == "$") {
       	 		var str = variable.substr(1);
       	 		var arStr = str.split(".");
  	 		    if (arStr.length == 1) {
       	 			ret = $.Cmdbuild.dataModel.resolveInterfaceVariable(arStr[0]);
       	 		}
       	 		else if (arStr[0] == "parameters"){
   	 				ret = $.Cmdbuild.dataModel.resolveParametersVariable(arStr[1]);
       	 		}
       	 		else if (arStr[0] == "window"){
   	 				ret = $.Cmdbuild.dataModel.resolveWindowVariable(arStr[1]);
       	 		}
       	 		else if (arStr[0] == "cmdbuildvariables"){
   	 				ret = $.Cmdbuild.dataModel.resolveCMDBuildVariable(arStr[1]);
       	 		}
       	 		else {
   	 				ret = $.Cmdbuild.dataModel.resolveModelVariable(arStr[0], arStr[1]);
       	 		}
       	 	}
       	 	else {
       	 		ret = variable;
       	 	}
 			return ret;
		},
		resolveVariables: function(param) {
 			var retParam = {};
			for (var key in param) {
				retParam[key] = $.Cmdbuild.dataModel.resolveVariable(param[key]);
			}
			return retParam;
		},
		detachObserving: function() {
			this.observing = false;
		},
		attachObserving: function() {
			this.observing = true;
		},
		detachedObserving: function() {
			return ! this.observing;
		},
		dispatchChange: function(nameDispatcher) {
			if ($.Cmdbuild.dataModel.detachedObserving()) {
				return;
			}
			try {
				var events = {};
				for (var nameObserver in $.Cmdbuild.dataModel.observers) {
					for (var i = 0; i < $.Cmdbuild.dataModel.observers[nameObserver].fields.length; i++) {
						var name = $.Cmdbuild.dataModel.observers[nameObserver].fields[i].form;
						if (nameDispatcher == name) {
							if (! events[nameObserver]) { 
								events[nameObserver] = {};
								events[nameObserver].fields = [];
								events[nameObserver]["container"] = $.Cmdbuild.dataModel.observers[nameObserver].container;
							}
							events[nameObserver].fields.push({
								tag: $.Cmdbuild.dataModel.observers[nameObserver].fields[i].tag,
								variable: "$" + nameDispatcher + "." + $.Cmdbuild.dataModel.observers[nameObserver].fields[i].field
							});
						}
					}
				}
				for (var form in events) {
					var obj = {};
					obj["form"] = form;
					obj["container"] = events[form].container;
					obj["fromObserving"] = "true";
					for (var i = 0; i < events[form].fields.length; i++) {
						obj[events[form].fields[i].tag] = events[form].fields[i].variable;
					}
					$.Cmdbuild.standard.commands.navigate(obj);
				}
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.dataModel.setObserver");
				throw e;
			}
		},
		setObserver: function(param) {
			var fields = [];
			try {
				for (var key in param.fields) {
		       	 	if (("" + param.fields[key]).substr(0, 1) == "$") {
		       	 		var str = param.fields[key].substr(1);
		       	 		var arStr = str.split(".");
						fields.push({
							form: arStr[0], 
							field: arStr[1],
							tag: key
						});
		       	 	}
		       	 	else {
						var error = $.Cmdbuild.errorsManager.getError({
							message: $.Cmdbuild.errorsManager.CMERROR,
							type: $.Cmdbuild.errorsManager.MALFORMEDVARIABLE,
							element: key,
							variable: param.fields[key]
						});
		       	 		throw(error);
		       	 	}
				}
				$.Cmdbuild.dataModel.observers[param.form] = {};
				$.Cmdbuild.dataModel.observers[param.form]["fields"] = fields;
				$.Cmdbuild.dataModel.observers[param.form]["container"] = param.container;
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.dataModel.setObserver");
				throw e;
			}
		},

		/*
		 * @param {String} widget - widget form id
		 * @param {Object[]} attachments
		 */
		setAttachmentsForWidget : function(widget, attachments) {
			this.attachments[widget] = attachments.slice();
		},
		/*
		 * @param {String} widget - widget form id
		 * @param {Object[]} attachments
		 */
		addAttachmentForWidget : function(widget, attachment) {
			if (!(widget in this.attachments)) {
				this.attachments[widget] = [];
			}
			this.attachments[widget].push(attachment);
		},
		/*
		 * @param {String} widget - widget form id
		 */
		getAttachmentsForWidget : function(widget) {
			if (widget in this.attachments) {
				return this.attachments[widget];
			}
			return [];
		}
	};
	$.Cmdbuild.dataModel = dataModel;
}) (jQuery);
