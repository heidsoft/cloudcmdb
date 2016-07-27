(function($) {
	var utilities = {
		getXmlDoc: function(file, callback, scope, params) {
			$.support.cors = true;
			$.ajax({ 
				// custom params
				type: "GET",
				url: file,
				data: {},
				cache: false,
				// default params
				dataType : "xml",
				crossDomain : true,
			}).done(function(data) {
				callback.call(scope, data, params);
			}).fail(function(XMLHttpRequest, textStatus, errorThrown) {
				// failure method
				console.log(XMLHttpRequest, textStatus, errorThrown);
			}).always(function() {
				// callback
			});
		},
		getLookupIdFromCode: function(lookupTypeId, code, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'lookup_types/' + lookupTypeId + '/values/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, "GET", function(data, metadata){
				for (var i = 0; i < data.length; i++) {
					if (data[i].code == code) {
						callback.apply(callbackScope, [data[i]._id]);
						break;
					}
				}
			}, {});
		},
		removeAllChildren: function(xmlElement) {
			var last;
			while (last = xmlElement.lastChild) {
				xmlElement.removeChild(last);
			}
		},
		include: function(xmlElement, callback, callbackParams, callbackScope) {
			var file = "";
			file = xmlElement.getAttribute("include");
			if (!file) {
/*				if (callbackScope) {
					pcallback.apply(callbackScope, [callbackParams]);
				}
				else {
					callback(callbackParams);
				}
					
*/				callback.apply(callbackScope, [callbackParams]);
				return;
			}
			var withId = xmlElement.getAttribute("withId");
			var fileType = xmlElement.getAttribute("fileType");
			this.removeAllChildren(xmlElement);
			var fullUrlFile = "";
			if (fileType && fileType === "core") {
				fullUrlFile = $.Cmdbuild.global.getAppRootUrl() + "templates/"+ file;
			}
			else {
				fullUrlFile = $.Cmdbuild.global.getAppConfigUrl() + file;
			}
			this.getXmlDoc(fullUrlFile, this.includeCB, this, {
				withId : withId,
				xmlElement : xmlElement,
				callback : callback,
				callbackParams : callbackParams,
				callbackScope : callbackScope
			});
		},
		includeCB : function(data, params) {
			var root = data.documentElement;
			if (params.withId) {//sum withId at every id
				var me = this;
				$(root).find("*[id]").each(function() {
					me.updateElementIds(root, this, params.withId);
				});
				this.updateAttributeIds(root, params.withId);
			}
			for (var i = 0; i < root.childNodes.length;  i++) {
				if (root.childNodes[i].nodeName == "#text") {
					continue;//nop
				}
				if (root.childNodes[i].nodeName == "#comment") {
					continue;//nop
				}
				params.xmlElement.appendChild(root.childNodes[i]);
			}
			if (params.callback) {
				params.callback.apply(params.callbackScope, [params.callbackParams]);
			}
		},
		updateElementIds : function(root, element, baseId) {
			var id = $(element).attr('id');
			$(element).attr('id', baseId + id);
			for (var i = 0; i < $.Cmdbuild.global.ID_TAGS.length; i++) {
				var tag = $.Cmdbuild.global.ID_TAGS[i];
				$(root).find(tag + ":contains(" + id + ")").each(
						function() {
							var text = $(this).text();
							if (text.substr(0, 1) == "$"
									&& $.trim(text.substr(1)) == id) {
								$(this).text("$" + baseId + id);
								var ar = text.split(".");
								if (ar.length > 1) {
									$(this).text($(this).text() + ar[1]);
								}
							} else if (text.substr(0, 1) == "$") {
								var ar = text.split(".");
								if ($.trim(ar[0].substr(1)) == id && ar.length > 1) {
									$(this).text("$" + baseId + id + "." + ar[1]);
								}
							} else if ($.trim(text) == id) {
								$(this).text(baseId + id);
							} else {
								var ar = text.split(".");
								if (ar.length > 1 && $.trim(ar[i]) == id) {
									$(this).text(ar[0] + "." + baseId + id);
								}
							}
						});
			}
		},
		updateAttributeIds : function(root, baseId) {
			for (var i = 0; i < $.Cmdbuild.global.ID_TAGSWITHVARIABLEATTRIBUTES.length; i++) {
				var tag = $.Cmdbuild.global.ID_TAGSWITHVARIABLEATTRIBUTES[i][0];
				var tagAttribute = $.Cmdbuild.global.ID_TAGSWITHVARIABLEATTRIBUTES[i][1];
				$(root).find(tag).each(
					function() {
						var value = $(this).attr(tagAttribute);
						$(this).attr(tagAttribute, baseId + value);
					});
			}
		},
		getBackend: function(name) {
			var backend = $.Cmdbuild.utilities.getPathObject(name, $.Cmdbuild.custom.backend);
			if (! backend) {
				backend = $.Cmdbuild.utilities.getPathObject(name, $.Cmdbuild.standard.backend);
			}
			if (! backend) {
				var error = $.Cmdbuild.errorsManager.getError({
					message: $.Cmdbuild.errorsManager.CMERROR,
					type: $.Cmdbuild.errorsManager.BACKENDMETHODNOTDEFINED,
					element: name,
				});
				throw error;
			}
			return backend;
		},
		getPathObject: function(name, caller) {
			if (! name) {
				return null;
			}
			var arGerarchy = name.split(".");
			for (var i = 0; i < arGerarchy.length; i++) {
				if (! caller[arGerarchy[i]]) {
					return null;
				}
				caller = caller[arGerarchy[i]];
			}
			return caller;
		},
		formatVarString: function() {
			var args = [].slice.call(arguments);
			if(this.toString() != '[object Object]') {
				args.unshift(this.toString());
			}
			var pattern = new RegExp('{([1-' + args.length + '])}','g');
			return String(args[0]).replace(pattern, function(match, index) { return args[index]; });
		},
		getFieldDescription: function(attribute, value, callback, callbackScope) {
			if (attribute.type == "reference") {
				$.Cmdbuild.utilities.proxy.getCardData(attribute.targetClass, value, {}, function(response, metadata) {
					callback.apply(callbackScope, [response.Description]);
				}, this);
			} else if (attribute.type == "lookup") {
				$.Cmdbuild.utilities.proxy.getLookupValue(attribute.lookupType, value, {}, function(response, metadata) {
					callback.apply(callbackScope, [response.description]);				
				}, this);
			} else if (attribute.type == "decimal") {
				try {
					callback.apply(callbackScope, [value.toFixed(attribute.scale)]);
				} catch (e) {
					callback.apply(callbackScope, [undefined]);
				}
			} else {
				callback.apply(callbackScope, [undefined]);
			}
		},
		getHtmlFieldValue: function(name) {
			var field = $(name);
			if (field) {
				if (field.prop("tagName") == "SELECT") {
					var entryValue = field.val();
					return entryValue;
				}
				else if (field.attr("isDate")) {
					var entryValue = field.val();
					var dateType = field.attr("date-input-type");
					entryValue = $.Cmdbuild.utilities.convertDateGUI2DB(entryValue, dateType);
					return entryValue;
				}
				else if (field.attr("valueType") == "spinner") {
					var entryValue = field.spinner("value");
					return entryValue;
				}

				else if (field.attr("type") == "checkbox") {
					return (field.prop("checked")) ? true : false;
				}
				else {
					return field.val();
				}
			}
			return null;
		},
		convertDateGUI2DB: function(entryValue, dateType) {
			if (dateType == "date") {
				var ar = entryValue.split("/");
				if (ar.length == 3) {
					entryValue = ar[2] + "-" + ar[1] + "-" + ar[0] +"T" + "00:00:00";
					return entryValue;
				}
				else
					return "";
			} else if (dateType == "time") {
				return "1970-01-01T"+ entryValue;
			} else if (dateType == "dateTime") {
				var dt = entryValue.split(" ");
				if (dt.length === 2) {
					ar = dt[0].split("/");
					entryValue = ar[2] + "-" + ar[1] + "-" + ar[0] +"T" + dt[1];
					return entryValue;
				}
				return entryValue.replace("/", "-");
			}
			return entryValue;
		},
		convertDateDB2GUI: function(entryValue, dateType) {
			if (!entryValue) {
				return "";
			} else if (dateType == "date") {
				var ar = entryValue.split("T");
				ar = ar[0].split("-");
				if (ar.length == 3) {
					entryValue = ar[2] + "/" + ar[1] + "/" + ar[0];
				}
				return entryValue;
			} else if (dateType == "time") {
				var ar = entryValue.split("T");
				return ar[1];
			} else if (dateType == "dateTime") {
				return this.convertDateDB2GUI(entryValue, "date") + " " + this.convertDateDB2GUI(entryValue, "time");
			}
			return entryValue;
		},
		getHtmlFieldInteractivity: function(name) {
			var field = $(name);
			if (field && field.attr("interactivity")) {
				return field.attr("interactivity");
			}
			return null;
		},
		setAttributesInteractivity: function(attributes) {
			for (var i = 0; i < attributes.length; i++) {
				attributes[i].interactivity = $.Cmdbuild.global.fieldInteractivityFromCard(attributes[i].isnotnull);
			}
		},
		currentMenu: function(id, idMenu) {
			$("#" + idMenu + " .ui-menu-item.selected").removeClass("selected");
			$("#" + id).addClass("selected");
		},
		getEventLikeString: function(xmlElement, strEvent, otherCommands) {
			var htmlStr = "";
			otherCommand = otherCommands | "";
			var params = $.Cmdbuild.elementsManager.getEvent(strEvent, xmlElement);
			if (! params)
				return "";
			htmlStr = " " + strEvent + "=\'$.Cmdbuild.eventsManager.onEvent(" + JSON.stringify(params) + ");" + otherCommands + "\' ";
			return htmlStr;
		},
		clone: function(data) {
			return JSON.parse(JSON.stringify(data));
		},
		escapeHtml: function (string) {
			if (! string) {
				return "";
			}
			var entityMap = {
				"&": "&amp;",
				"<": "&lt;",
				">": "&gt;",
				'"': '&quot;',
				"'": '&apos;',
				"/": '/'//'&#x2F;'
			};
			var retStr = String(string).replace(/[&<>"'\/]/g, function (s) {
				return entityMap[s];
			});
			return retStr;
		},
		removeAttribute: function (ar, name) {
			for (var i = 0; i < ar.length; i++) {
				if (ar[i].name == name) {
					ar.splice(i, 1);
					break;
				}
			}
			return ar;
		},
		changeAttributeType: function  (ar, name, type) {
			for (var i = 0; i < ar.length; i++) {
				if (ar[i].name == name) {
					ar[i].type = type;
					break;
				}
			}
			return ar;
		},
		sortAttributes: function (attributes) {
			attributes.sort(function(a, b) { return a.index - b.index; });
		},
		insertOtherGroup: function (ar, name) {
			var bGrouped = false;
			for (var i = 0; i < ar.length; i++) {
				if (ar[i].group) {
					bGrouped = true;
					break;
				}
			}
			if (bGrouped) {
				for (var i = 0; i < ar.length; i++) {
					if (! ar[i].group) {
						ar[i].group = "Others";
					}
				}
			}
			return ar;
		},
		startApplication: function() {
			var me = this;
			// load classes
			$.Cmdbuild.utilities.proxy.getClasses(function(data) {
				$.Cmdbuild.dataModel.setClasses(data);
				// load processes
				$.Cmdbuild.utilities.proxy.getProcesses(function(data) {
					$.Cmdbuild.dataModel.setProcesses(data);
					// start application
					if ($.Cmdbuild.custom.commands && $.Cmdbuild.custom.commands.initialize) {
						$.Cmdbuild.custom.commands.initialize(me.startApplicationCallBack);
					}
					else {
						me.startApplicationCallBack();
					}
				}, me);
			}, me);
		},
		startApplicationCallBack: function() {
			$.Cmdbuild.global.getHtmlContainer().innerHTML = "";
			var root = $.Cmdbuild.global.getConfigurationDocument().documentElement;
			var htmlStr = $.Cmdbuild.elementsManager.insertDialog("errorDialog", $.Cmdbuild.errorsManager.close);
			htmlStr += $.Cmdbuild.elementsManager.insertChildren(root);
			$($.Cmdbuild.global.getHtmlContainer()).attr("class", $.Cmdbuild.global.getThemeCSSClass());
			$.Cmdbuild.global.getHtmlContainer().innerHTML = htmlStr;

			$.Cmdbuild.elementsManager.initialize();
			$(document).tooltip();

		},
		getFields : function(xmlElement) {
			var $xml = $(xmlElement);
			var $fields = $xml.find("fields");
			if (! $fields.length) {
				return undefined;
			}
			var response = {};
			var xmlfields = $($fields[0]);
			var form = xmlfields.children("form").text();
			response["form"] = form;
			var method = xmlfields.children("method") .text();
			response["method"] = method;
			// method can be "evaluate" in this case the GUI accept the fields from cmbdbuil but
			//	- evaluate attributes if there are attributes
			//  - remove field if there are not attributes
			if (xmlfields.children("field").length == 0) {
				// response["fields"] remain undefined this clear the model
				// so for this form the fields are again the original values from the server
				return response;
			}
			response["fields"] = {};
			for(var i = 0; i < xmlfields.children("field").length; i++) {
				var $field = $(xmlfields.children("field")[i]);
				var fieldname = $field.children("name").text();
				var attributes = {};
				if (fieldname) {
					var $attributes = $field.children("attributes");
					for(var j = 0; j < $attributes.children("attribute").length; j++) {
						var $attribute = $($attributes.children("attribute")[j]);
						var key = $attribute.children("name").text();
						var value = $attribute.children("value").text();
						if (key) {
							attributes[key] = value;
						}
					}
					response.fields[fieldname] = attributes;
				}
			}
			return response;
		},
		getFormFields : function(xmlElement) {
			var $xml = $(xmlElement);
			var $fields = $xml.find("formFields");
			if (! $fields.length) {
				return undefined;
			}
			var response = {};
			var xmlfields = $($fields[0]);
			var form = xmlfields.children("form").text();
			response["form"] = form;
			if (xmlfields.children("field").length == 0) {
				// response["fields"] remain undefined this clear the model
				// so for this form the fields are again the original values from the server
				return response;
			}
			response["fields"] = {};
			for(var i = 0; i < xmlfields.children("field").length; i++) {
				var $field = $(xmlfields.children("field")[i]);
				
				var fieldname = $field.children("name").text();
				var attributes = {};
				if (fieldname) {
					var $attributes = $field.children("attributes");
					for(var j = 0; j < $attributes.children("attribute").length; j++) {
						var $attribute = $($attributes.children("attribute")[j]);
						var key = $attribute.children("name").text();
						var value = $attribute.children("value").text();
						if (key) {
							attributes[key] = value;
						}
					}
					response.fields[fieldname] = attributes;
				}
			}
			return response;
		},
		addFields: function() {
		},
		getToday: function() {
		    var currentDate = new Date();
		    var day = currentDate.getDate();
		    var month = currentDate.getMonth() + 1;
		    var year = currentDate.getFullYear();
			return day + "/" + month + "/" + year;
		},
		getConditionsFromParam: function(param) {
			var conditions = [];
			if (param.condition) {
				var params = param.condition.split(",");
				for (var i = 0; i < params.length; i++) {
					var fields = params[i].split("=");
					var field = {
						field: fields[0],
						value: $.Cmdbuild.dataModel.resolveVariable(fields[1])
					};
					conditions.push(field);
				}
			}
			return conditions;
		},
		getAttributesFilterFromConditions: function(conditions) {
			if (conditions.length == 0) {
				return undefined;
			}
			var attributeFilter = {};
			var filterBlocks = [];
			for (var i = 0; i < conditions.length; i++) {
				var filterBlock = {
					simple: {
						attribute: conditions[i].field,
						operator: "equal",
						value: [conditions[i].value],
						parameterType: "fixed"
					}
				};
				filterBlocks.push(filterBlock);
			}
			if (conditions.length == 1) {
				attributeFilter = filterBlocks[0];
			}
			else {
				attributeFilter = {
					and: filterBlocks
				};
			}
			return attributeFilter;
		},

		/*
		 * @param {String} xmlElement
		 */
		getWidgetsFromXML : function(xmlElement) {
			var widgets = [];
			var $xml = $(xmlElement);
			var $widgets = $xml.children("widgets");
			if (! $widgets.length || $widgets.children("widget").length == 0) {
				return undefined;
			}

			$.each($widgets.children("widget"), function(index, item) {
				var data = {};
				$.each(item.children, function(i, node) {
					data[node.nodeName] = node.textContent;
				});
				widgets.push({
					_id: data.id,
					active: data.active,
					label: data.label,
					required: data.required,
					type: data.type,
					data: data
				});
			});
			return widgets;
		},

		/*
		 * @param {String} xmlElement
		 */
		getButtonsFromXML : function(xmlElement) {
			var htmlStr = "";
			var $xml = $(xmlElement);
			var $buttons = $xml.children("buttons");
			var hasButtons = $buttons.length  > 0 && $buttons.children("button").length > 0;

			if (hasButtons){
				htmlStr += "<div class=\"formButtons\">";
			}

			$.each($buttons.children("button"), function(index, button) {
				var dp = new DOMParser();
				xDoc = dp.parseFromString(button.outerHTML, "text/xml");
				htmlStr += $.Cmdbuild.elementsManager.toHtml(xDoc.documentElement);
			});

			if (hasButtons){
				htmlStr += "</div>";
			}

			return htmlStr;
		},

		/*
		 * @param {string} title - popup title
		 * @param {string} message - popup message
		 */
		popupMessage : function(title, message) {
			$('<div />').html(message).dialog({
				title : title,
				dialogClass : "no-close",
				dialogClass: $.Cmdbuild.global.getThemeCSSClass(),
				buttons : [ {
					text : "OK",
					click : function() {
						$(this).dialog("destroy");
					}
				} ]
			});
		},
		uniqueId: function() {
			  return Math.round(new Date().getTime() + (Math.random() * 100));
		},
        readCookie : function(name) {
            var nameEQ = name + "=";
            var ca = document.cookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0) == ' ') c = c.substring(1, c.length);
                if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
            }
            return null;
        },

		attributesSorter : function(a, b) {
			if (a.index < b.index)
				return -1;
			if (a.index > b.index)
				return 1;
			return 0;
		}

	};
	$.Cmdbuild.utilities = $.extend($.Cmdbuild.utilities, utilities);
}) (jQuery);
