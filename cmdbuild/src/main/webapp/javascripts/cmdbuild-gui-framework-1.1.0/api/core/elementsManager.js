(function($) {
	var elementsManager = {
		insertChildren: function(xmlElement) {
			var htmlStr = "";
			for (var i=0; i < xmlElement.childNodes.length;  i++)  {
				htmlStr += this.toHtml(xmlElement.childNodes[i]);
			}
			return htmlStr;
		},
		toHtml: function(xmlElement) {
			try {
				var error = $.Cmdbuild.errorsManager.getError({
					message: $.Cmdbuild.errorsManager.CMERROR,
					type: $.Cmdbuild.errorsManager.MALFORMEDXMLNODE,
					element: xmlElement.nodeName
				});
				if (xmlElement.nodeName == "html") {
					throw error;
				}
				if (xmlElement.nodeName == "ldap") {
					return "";//nop
				}
				if (xmlElement.nodeName == "rowButton") {
					return "";//nop
				}
				if (xmlElement.nodeName == "columnsCommands") {
					return "";//nop
				}
				if (xmlElement.nodeName == "#text") {
					return "";//nop
				}
				if (xmlElement.nodeName == "onInit") {
					return "";//nop
				}
				if (xmlElement.nodeName == "onDblClick") {
					return "";//nop
				}
				if (xmlElement.nodeName == "onClick") {
					return "";//nop
				}
				if (xmlElement.nodeName == "onChange") {
					return "";//nop
				}
				if (xmlElement.nodeName == "params") {
					return "";//nop
				}
				if (xmlElement.nodeName == "form") {
					return "";//nop
				}
				if (xmlElement.nodeName == "openEntry") {
					return "";//nop
				}
				if (xmlElement.nodeName == "#comment") {
					return "";//nop
				}
				if (xmlElement.nodeName == "observe") {
					return "";//nop
				}
				if (xmlElement.nodeName == "fields") {
					return "";//nop
				}
				if (xmlElement.nodeName == "widgets") {
					return "";//nop
				}
				if (xmlElement.nodeName == "buttons") {
					return "";//nop
				}
				if ($.Cmdbuild.custom.elements && $.Cmdbuild.custom.elements[xmlElement.nodeName]) {
					var ret = $.Cmdbuild.custom.elements[xmlElement.nodeName](xmlElement);
					return ret;
				}
				if ($.Cmdbuild.standard.elements[xmlElement.nodeName]) {
					var ret = $.Cmdbuild.standard.elements[xmlElement.nodeName](xmlElement);
					return ret;
				}
				console.log(xmlElement.nodeName + " not found!");
				var error = $.Cmdbuild.errorsManager.getError({
					message: $.Cmdbuild.errorsManager.CMERROR,
					type: $.Cmdbuild.errorsManager.XMLTAGNOTDEFINED,
					element: xmlElement.nodeName
				});
				throw error;
			}
			catch (e) {
				var str = "$.Cmdbuild.elementsManager.toHtml";
				$.Cmdbuild.errorsManager.log(str);
				$.Cmdbuild.errorsManager.log(e);
				throw e;
			}
			return "";//nop
		},
		insertLoadingMask: function(id) {
			var htmlStr = "<div id='" + id + "'><h1>wait wait wait</h1></div>";
			return htmlStr;
		},
		insertDialog: function(id, callback) {
			var htmlStr = "<div id='" + id + "'></div>";
			$.Cmdbuild.scriptsManager.push({
				script: "dialog",
				id: id,
				callback: callback
			});
			return htmlStr;
		},
		getText: function(xmlElement) {
			var i18nText = xmlElement.getAttribute("i18nText");
			var variableText = xmlElement.getAttribute("variableText");
			var text = "";
			text = $.Cmdbuild.utilities.escapeHtml(xmlElement.getAttribute("text"));
			if (variableText) {
				text += " " + $.Cmdbuild.dataModel.resolveVariable(variableText);
			}
			if (i18nText) {
				return $.Cmdbuild.translations.getTranslation(i18nText, text);
			}
			return text;
		},
		getTooltip: function(xmlElement) {
			var i18nTooltip = xmlElement.getAttribute("i18nTooltip");
			var text = "";
			text = $.Cmdbuild.utilities.escapeHtml(xmlElement.getAttribute("tooltip"));
			if (i18nTooltip) {
				return $.Cmdbuild.utilities.escapeHtml($.Cmdbuild.translations.getTranslation(i18nTooltip, text));
			}
			return text;
		},
		getXmlElementId: function(xmlElement) {
			var originalId = xmlElement.getAttribute("id");
			return originalId;
		},
		getCommonAttributes: function(xmlElement) {
			var str = " ";
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			if (id) {
				str += "id='" + id + "' ";
			}
			var className = xmlElement.getAttribute("class");
			if (className) {
				str += "class='" + className + "' ";
			}
			var interactivity = xmlElement.getAttribute("interactivity");
			if (interactivity) {
				str += "interactivity='" + interactivity + "' ";
				str += (interactivity == $.Cmdbuild.global.READ_ONLY) ? " disabled " : "";
			}
			var title = xmlElement.getAttribute("title");
			if (title) {
				str += "title='" + title + "' ";
			}

			var formName = xmlElement.getAttribute("formName");
			if (formName) {
				str += "formName='" + formName + "' ";
			}

			var fieldName = xmlElement.getAttribute("fieldName");
			if (fieldName) {
				str += "fieldName='" + fieldName + "' ";
			}
			var height = xmlElement.getAttribute("height");
			if (height) {
				str += "height='" + height + "' ";
			}
			var width = xmlElement.getAttribute("width");
			if (width) {
				str += "width='" + width + "' ";
			}
			var tooltip = $.Cmdbuild.elementsManager.getTooltip(xmlElement);
			if (tooltip) {
				str += "title='" + tooltip + "' ";
			}

			return str;
		},
		insertReturn: function(xmlElement) {
			var htmlStr = "";
			var withReturn = xmlElement.getAttribute("withReturn");
			if (withReturn == "true") {
				htmlStr += "<br/>";
			}
			return htmlStr;
		},
		isReturn: function(xmlElement) {
			return (xmlElement.getAttribute("withReturn") == "true") ? true : false;
		},
		insertLabel: function(xmlElement) {
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var label = xmlElement.getAttribute("label");
			var i18nLabel = xmlElement.getAttribute("i18nLabel");
			if (i18nLabel) {
				label = $.Cmdbuild.translations.getTranslation(i18nLabel, label);
			}

			var interactivity = xmlElement.getAttribute("interactivity") || $.Cmdbuild.global.READ_WRITE;
			var requiredSuffix = (interactivity == $.Cmdbuild.global.READ_WRITE_REQUIRED) ? "\* " : "";

			var classLabel = xmlElement.getAttribute("classLabel");
			var classLabelHtml = " class='cmdbuildLabel" + (classLabel ? " " + classLabel : "") + "'";

			htmlStr = "<label for='" + id + "'" + classLabelHtml + ">" + requiredSuffix + label + "</label>";

			return label ? htmlStr : "";
		},
		wrapInLayoutContainer: function(xmlElement, inputHtml) {
			var labelPosition = xmlElement.getAttribute("labelPosition");

			if (labelPosition == "over")
				return "<div class='cdmdbuildVboxContainer'>" + inputHtml + "</div>";

			return inputHtml;
		},
		wrapInRowContainer: function(xmlElement, inputHtml) {
			var returnClass = ($.Cmdbuild.elementsManager.isReturn(xmlElement)) ? " cmdbuildBlockFormRow" : "";

			return "<div class='cmdbuildFormRow" + returnClass + "'>" + inputHtml + "</div>";
		},

		wrapInFormRow: function(label, input, type, readonly) {
			var $dl = $("<dl></dl>").addClass("cmdbuildGuiFormRow").addClass("cmdbuildGuiFormRow-" + type);
			if (readonly) {
				$dl.addClass("cmdbuildGuiFormRowReadOnly");
			}
			var $dt = $("<dt></dt>").html(label);
			if (label) {
				$dl.append($dt);
			}
			var $dd = $("<dd></dd>").html(input);
			$dl.append($dd);
			return $dl.prop('outerHTML');
		},

		getEvent: function(eventName, xmlElement) {
			var params = {};
			var $xmlElement = $(xmlElement);
			var $eventItem = $xmlElement.children(eventName);
			if ($eventItem.children().length) {
				params.id = $xmlElement.attr("id");
				$eventItem.children().each(function(index) {
					var key = $(this).prop("tagName");
					var value = $(this).text();
					params[key] = value;
				});
				return params;
			} else {
				return null;
			}
		},

		getGroupElements: function(xmlElement, name, params) {
			params = params || {};
			var $xmlElement = $(xmlElement);
			var $root = $xmlElement;
			var $eventItem = $root.children(name);
			$eventItem.children().each(function(index) {
				var key = $(this).prop("tagName");
				if (!params[key]) {
					var value = $(this).text();
					params[key] = value;
				}
			});
			return (! $.isEmptyObject(params)) ? params : null;
		},

		getRowButtons: function(xmlElement, params) {
			var rowButtons = [];
			var $xmlElement = $(xmlElement);
			$xmlElement.children("rowButton").each(function(index) {
				rowButtons.push({
					id : $(this).attr("id"),
					tooltip : $(this).attr("tooltip"),
					className : $(this).attr("class"),
					onClick: $.Cmdbuild.elementsManager.getEvent("onClick", this),
					onMouseOver: $.Cmdbuild.elementsManager.getEvent("onMouseOver", this),
					params: $.Cmdbuild.elementsManager.getParams(this)
				});
				$.Cmdbuild.dataModel.prepareCallerParameters($(this).attr("id"), $.Cmdbuild.elementsManager.getParams(this));

			});
			params["rowButtons"] = rowButtons;
		},

		getColumnsCommands: function(xmlElement, params) {
			var columnsCommands = [];
			var $xmlElement = $(xmlElement);
			$xmlElement.children("columnsCommands").children("column").each(function(index, column) {
				columnsCommands.push({
					attribute: $(column).children("name").text(),
					icon: $(column).children("icon").text(),
					onClick: $.Cmdbuild.elementsManager.getEvent("onClick", this)
				});
			});
			params["columnsCommands"] = columnsCommands;
		},

		getParams: function(xmlElement, params) {
			return this.getGroupElements(xmlElement, "params", params);
		},

		isObserving: function(xmlElement, formName) {
			var params = this.getGroupElements(xmlElement, "observe");
			if (params) {
				var container = xmlElement.getElementsByTagName('observe')[0].getAttribute("container");
				$.Cmdbuild.dataModel.setObserver({
					container: container,
					form: formName,
					fields:params
				});
				return true;
			}
			return false;
		},

		getElement: function(name) {
			var root = $($.Cmdbuild.global.getConfigurationDocument());
			var element = root.find("#" + name);
			if (! element) {
				var error = $.Cmdbuild.errorsManager.getError({
					message: $.Cmdbuild.errorsManager.CMERROR,
					type: $.Cmdbuild.errorsManager.XMLELEMENTNOTDEFINED,
					element: name,
				});
				throw error;
			}
			return element[0];
		},
		initialize: function() {
			$.Cmdbuild.scriptsManager.execute();
		},
		makeServiceButton: function(classes, id, icon, onclick) {
			var htmlStr = "";
			htmlStr += "<button class='" + classes + "' id='" + id + "' onclick='" + onclick + "'></button>";
			$.Cmdbuild.scriptsManager.push({
				script: "button",
				id: id,
				icon: icon
			});
			return htmlStr;

		},
		loadIconButtons: function(attributes, buttons, position) {
			for (var i = 0; i < buttons.length; i++) {
				var paramActualized = $.Cmdbuild.dataModel.resolveVariables(buttons[i].params);
				if (paramActualized.condition == "false") {
					continue;
				}
				var button = {
						type: "IMAGEBUTTON",
						displayableInList: true,
						active: true,
						name: buttons[i].id,
						className: (buttons[i].className) ? buttons[i].className : buttons[i].id, 
						tooltip: buttons[i].tooltip,
						onClick: JSON.stringify(buttons[i].onClick)
					};
				(position == "left") ? attributes.splice(i, 0, button) : attributes.push(button);
			}
		},
		pushSelectionCheck: function(attributes, position) {
			var button = {
					type: "ROWSELECTIONCHECK",
					displayableInList: true,
					active: true,
					name: "selectionCheck",
					tooltip: $.Cmdbuild.global.SELECTIONTOOLTIP,
				};
			(position == "left") ? attributes.splice(0, 0, button) : attributes.push(button);
		}
	};
	$.Cmdbuild.elementsManager = elementsManager;
}) (jQuery);
