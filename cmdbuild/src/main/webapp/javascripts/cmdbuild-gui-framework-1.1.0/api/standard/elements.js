(function($) {
	var elements = {
		canvas3d : function(xmlElement) {
			var htmlStr = "";
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			htmlStr += "<div  " + ca + "></div>";
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			$.Cmdbuild.scriptsManager.push({
				script : "canvas3d",
				id : id
			});
			return htmlStr;
		},
		dialog : function(xmlElement) {
			var htmlStr = "";
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			htmlStr += "<div" + ca + "></div>";
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			$.Cmdbuild.scriptsManager.push({
				script : "dialog",
				id : id
			});
			return htmlStr;
		},
		spinner : function(xmlElement) {
			var htmlStr = "";
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var label = $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var change = $.Cmdbuild.elementsManager.getEvent("onChange", xmlElement);
			var param = $.Cmdbuild.elementsManager.getParams(xmlElement);
			var value = param.value;
			var input = "<input  " + ca + " name='value' valueType='spinner' value='" + value + "'></input>";
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			$.Cmdbuild.scriptsManager.push({
				script : "spinner",
				id : id,
				spin: change,
				value: value,
				min: (param.min) ? param.min : 1,
				max: (param.max) ? param.max : 100000
			});
			var htmlStr = $.Cmdbuild.elementsManager.wrapInFormRow(label, input, "input");
			return htmlStr;
		},
		checkbox : function(xmlElement) {
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var text = xmlElement.getAttribute("text");
			text = text ? text : "false";
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var onClick = $.Cmdbuild.utilities.getEventLikeString(xmlElement, "onClick");

			$.Cmdbuild.scriptsManager.push({
				script : "checkbox",
				id : id
			});
			
			var checked = "";
			if (text == "true") {
				checked = " checked ";
			}

			var label = $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var input = "<input type='checkbox'" + checked + ca + onClick + "value='" + text + "' />";

			
			var htmlStr = $.Cmdbuild.elementsManager.wrapInFormRow(label, input, "checkbox");

			return htmlStr;
		},
		button : function(xmlElement) {
			var param = $.Cmdbuild.elementsManager.getParams(xmlElement);
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);

			if (paramActualized.condition !== undefined && ! paramActualized.condition) {
				return "";
			}
			var htmlStr = "";
			htmlStr += $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var onClick = $.Cmdbuild.utilities.getEventLikeString(xmlElement, "onClick");
			var strEnabled = (paramActualized.readOnly == "true") ? " disabled " : "";
			htmlStr += "<input type='button'" + ca + onClick + strEnabled + " value='" + text + "' />";
			htmlStr += $.Cmdbuild.elementsManager.insertReturn(xmlElement);
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			if (param) {
				$.Cmdbuild.dataModel.prepareCallerParameters(id, param);
			}
			$.Cmdbuild.scriptsManager.push({
				script : "button",
				id : id
			});
			return htmlStr;
		},
		upload : function(xmlElement) {
			var label = $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var change = $.Cmdbuild.utilities.getEventLikeString(xmlElement, "onChange");
			var input = text + ": <input type='file' name='filename'" + ca + change + " />";
			input += $.Cmdbuild.elementsManager.insertReturn(xmlElement);

			var htmlStr = $.Cmdbuild.elementsManager.wrapInFormRow(label, input, "upload");
			return htmlStr;
		},
		iframe : function(xmlElement) {
			var src = $.Cmdbuild.utilities.escapeHtml(xmlElement.getAttribute("src"));
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var htmlStr = "<iframe src='" + src + "'" + ca + "/>";
			htmlStr += $.Cmdbuild.elementsManager.insertReturn(xmlElement);
			return htmlStr;
		},
		title : function(xmlElement) {
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var onClick = $.Cmdbuild.utilities.getEventLikeString(xmlElement, "onClick");
			var htmlStr = "<h2 " + onClick + ca + " >" + text + "</h2>";
			htmlStr += $.Cmdbuild.elementsManager.insertReturn(xmlElement);
			return htmlStr;
		},
		p : function(xmlElement) {
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var onClick = $.Cmdbuild.utilities.getEventLikeString(xmlElement, "onClick");
			var htmlStr = "<span " + onClick + ca + " >" + text + "</span>";
			htmlStr += $.Cmdbuild.elementsManager.insertReturn(xmlElement);
			return htmlStr;
		},
		span : function(xmlElement) {
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var onClick = $.Cmdbuild.utilities.getEventLikeString(xmlElement, "onClick");
			var htmlStr = "<span " + onClick + ca + " >" + text + "</span>";
			htmlStr += $.Cmdbuild.elementsManager.insertReturn(xmlElement);
			return htmlStr;
		},
		h1 : function(xmlElement) {
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var htmlStr = "<h1 " + ca + ">" + text + "</h1>";
			return htmlStr;
		},
		h2 : function(xmlElement) {
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var htmlStr = "<h2 " + ca + ">" + text + "</h2>";
			return htmlStr;
		},
		h3 : function(xmlElement) {
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var htmlStr = "<h3 " + ca + ">" + text + "</h3>";
			return htmlStr;
		},
		br : function(br) {
			var htmlStr = "<br />";
			return htmlStr;
		},
		img : function(xmlElement) {
			var htmlStr = "";
			var src = xmlElement.getAttribute("src");
			if (!src) {
				var raw_params = $.Cmdbuild.elementsManager.getParams(xmlElement);
				var params = $.Cmdbuild.dataModel.resolveVariables(raw_params);
				src = params.src;
			}
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			htmlStr += "<img" + ca + "src='" + src + "' />";
			htmlStr += $.Cmdbuild.elementsManager.insertReturn(xmlElement);
			return htmlStr;
		},
		input : function(xmlElement) {
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var label = $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var input = "";

			// Read Only field
			var interactivity = xmlElement.getAttribute("interactivity");
			var readOnly = interactivity && interactivity == $.Cmdbuild.global.READ_ONLY;
			// Max Length
			var maxlength = xmlElement.getAttribute("maxlength");
			if (maxlength !== null) {
				ca += " maxlength='" + maxlength + "' ";
			}

			// create field
			if (readOnly) {
				input = text;
				input += "<input type='hidden'" + ca + "value='" + (text ? text : "") + "' />";
			} else {
				input = "<input type='text'" + ca + "value='" + text + "' />";
			}

			var htmlStr = $.Cmdbuild.elementsManager.wrapInFormRow(label, input, "input", readOnly);
			return htmlStr;
		},
		integer : function(xmlElement) {
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var label = $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var input = "";

			// Read Only field
			var interactivity = xmlElement.getAttribute("interactivity");
			var readOnly = interactivity && interactivity == $.Cmdbuild.global.READ_ONLY;
			if (readOnly) {
				input += text;
				input += "<input type='hidden'" + ca + "value='" + (text ? text : "") + "' />";
			} else {
				input += "<input type='text'" + ca + "value='" + text + "' />";
			}

			var htmlStr = $.Cmdbuild.elementsManager.wrapInFormRow(label, input, "integer", readOnly);
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			$.Cmdbuild.scriptsManager.push({
				script : "integer",
				id : id
			});

			return htmlStr;
		},
		date : function(xmlElement) {
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var param = $.Cmdbuild.elementsManager.getParams(xmlElement);

			var text = xmlElement.getAttribute("text");
			text = text || "";
			text = $.Cmdbuild.utilities.convertDateDB2GUI(text, param.type);

			$.Cmdbuild.scriptsManager.push({
				script : "date",
				id : id,
				type: param.type
			});

			var label = $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var input;

			// Read Only field
			var interactivity = xmlElement.getAttribute("interactivity");
			var readOnly = interactivity && interactivity == $.Cmdbuild.global.READ_ONLY;
			if (readOnly) {
				input = text;
				input += "<input isDate='" + param.type + "' type='hidden'" + ca + "value='" + (text ? text : "") + "' />";
			} else {
				input = "<input isDate='" + param.type + "' type='text'" + ca + "value='" + text + "' />";
			}
			var htmlStr = $.Cmdbuild.elementsManager.wrapInFormRow(label, input, "date", readOnly);

			return htmlStr;
		},
		textarea : function(xmlElement) {
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var param = $.Cmdbuild.elementsManager.getParams(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);

			var label = $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var input = "";

			var isHtml = (param && param.isHtml) ? true : false;
			var rows = xmlElement.getAttribute("rows");
			var cols = xmlElement.getAttribute("cols");

			var text = "";
			if (!isHtml && param && param.rawText) {
				text = param.rawText.trim();
			} else {
				text = $.Cmdbuild.elementsManager.getText(xmlElement);
			}
			
			// Read Only field
			var interactivity = xmlElement.getAttribute("interactivity");
			var readOnly = interactivity && interactivity == $.Cmdbuild.global.READ_ONLY;
			
			// Max Length
			var maxlength = xmlElement.getAttribute("maxlength");
			if (maxlength !== null) {
				ca += " maxlength='" + maxlength + "' ";
			}

			if (readOnly) {
				if (isHtml) {
					// interpretare html
					var e = document.createElement('div');
					e.innerHTML = text;
					if (e.childNodes.length) {
						input = '<div class="textareaReadOnly">' + e.childNodes[0].nodeValue + '</div>';
					}
				} else {
					input = text.replace(/(?:\r\n|\r|\n)/g, '<br />');
				}
				input += "<textarea class='hiddentextarea'>" + (text ?  text : "") + "</textarea>";
			} else {
				input = "<textarea  cols='" + cols + "' rows='" + rows + "' type='text'" + ca + "value='" + text
					+ "'>" + text + "</textarea>";
			}

			var htmlStr = $.Cmdbuild.elementsManager.wrapInFormRow(label, input, "textarea", readOnly);
			if (isHtml) {
				param = {
						script : "htmlText",
						id : id
				};
				$.Cmdbuild.scriptsManager.push(param);
			}

			return htmlStr;
		},
		lookup : function(xmlElement) {
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var param = $.Cmdbuild.elementsManager.getParams(xmlElement);
			var change = $.Cmdbuild.utilities.getEventLikeString(xmlElement, "onChange");

			var label = $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var interactivity = xmlElement.getAttribute("interactivity");
			var readOnly = interactivity && interactivity == $.Cmdbuild.global.READ_ONLY;
			var input;

			if (readOnly) {
				input = "<input type='hidden'" + ca + "value='" + (param.value | "") + "' />";
			} else {
				input = ""; //"<div class='lookupContainer'>";
				input += "<select " + ca + change + ">";
				input += "</select>";
				input += "";//"</div>";
			}

			var htmlStr = $.Cmdbuild.elementsManager.wrapInFormRow(label, input, "lookup", readOnly);

			param = {
					script : "lookup",
					id : id,
					backend: param.backend,
					lookupName: param.lookupName,
					value: param.value,
					readOnly: readOnly
			};
			$.Cmdbuild.scriptsManager.push(param);

			return htmlStr;
		},
		select : function(xmlElement) {
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var param = $.Cmdbuild.elementsManager.getParams(xmlElement);
			var change = $.Cmdbuild.utilities.getEventLikeString(xmlElement, "onChange");
			param = $.extend({
				script : "select",
				id : id
			}, param);
			$.Cmdbuild.scriptsManager.push(param);

			var label = $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var input = "<select " + ca + change + ">";
			input += "</select>";

			var interactivity = xmlElement.getAttribute("interactivity");
			var readOnly = interactivity && interactivity == $.Cmdbuild.global.READ_ONLY;

			var htmlStr = $.Cmdbuild.elementsManager.wrapInFormRow(label, input, "select", readOnly);

			return htmlStr;
		},
		reference : function(xmlElement) {
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var param = $.Cmdbuild.elementsManager.getParams(xmlElement);
			var backend = param.backend;

			var label = $.Cmdbuild.elementsManager.insertLabel(xmlElement);

			var interactivity = xmlElement.getAttribute("interactivity");
			var readOnly = interactivity && interactivity == $.Cmdbuild.global.READ_ONLY;

			var input;
			var eventSearch;
			if (!readOnly) {
				eventSearch = "$.Cmdbuild.standard.referenceField.onSearchLookup(\"" + id + "\", \"" + param.container
				+ "\")";
				var eventClear = "$.Cmdbuild.standard.referenceField.onClearLookup(\"" + id + "\")";
				param.toExecCommand = id;
				input = "<select " + ca + " backend='" + backend + "' className='" + param.className + 
				"' eventSearch = '" + eventSearch + "'>";
				input += "<option selected></option>";// datatables want an option;
				input += "</select>";
				var formName = xmlElement.getAttribute("formName");
				var fieldName = xmlElement.getAttribute("fieldName");
				param.fieldName = fieldName;
				param.formName = formName;
				
				input += this.lookupButtons(id, param.container, eventSearch, eventClear);
				this.lookupDialog(id, param);
			} else {
				input = "<input type='hidden'" + ca + "value='" + (param.value | "") + "' />";
			}

			var htmlStr = $.Cmdbuild.elementsManager.wrapInFormRow(label, input, "reference", readOnly);

			param = {
					script : "reference",
					id : id,
					eventSearch : eventSearch,
					backend: param.backend,
					className: param.className,
					value: param.value,
					readOnly: readOnly,
					formNRows: param.formNRows
			};
			$.Cmdbuild.scriptsManager.push(param);
			return htmlStr;
		},
		openEntry : function(xmlElement, idMenu) {
			var htmlStr = "";
			var strTitle = "";
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);

			// get params
			var raw_params = $.Cmdbuild.elementsManager.getParams(xmlElement);
			var params = $.Cmdbuild.dataModel.resolveVariables(raw_params);
			if (params) {
				$.Cmdbuild.dataModel.prepareCallerParameters(id, params);
			}

			// if params.show is false, or params.hide is true
			// not show this element
			if (params.show == false || params.hide == true) {
				return "";
			}
			var onClick = $.Cmdbuild.utilities.getEventLikeString(xmlElement, "onClick", 
					"$.Cmdbuild.utilities.currentMenu(\"" + id + "\", \"" + idMenu + "\");return false;");
			htmlStr += "<li " + ca + "><span class='ui-icon ui-icon-carat-1-e'></span><a id='" + id + "_aElement' title='" + strTitle + "' href='#'" + onClick
					+ ">" + text + "</a>";
			if (xmlElement.childNodes.length > 0) { // To build sub-menu
				htmlStr += "<ul class='ui-menu'>";
				for (var i = 0; i < xmlElement.childNodes.length; i++) {
					if (xmlElement.childNodes[i].nodeName == "openEntry") {
						htmlStr += this.openEntry(xmlElement.childNodes[i], idMenu);
					}
				}
				htmlStr += "</ul>";
			}
			htmlStr += "</li>";
			$.Cmdbuild.scriptsManager.push({
				script : "openMenu",
				id : id + "_aElement"
			});
			return htmlStr;
		},
		openMenu : function(xmlElement) {
			var htmlStr = "";
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			htmlStr += "<ul id='" + id + "' class='ui-menu'>";
			for (var i = 0; i < xmlElement.childNodes.length; i++) {
				if (xmlElement.childNodes[i].nodeName == "openEntry") {
					htmlStr += this.openEntry(xmlElement.childNodes[i], id);
				}
			}
			htmlStr += "</ul>";
			return htmlStr;
		},
		text : function(xmlElement) {
			var htmlStr = "";
			htmlStr += $.Cmdbuild.elementsManager.insertLabel(xmlElement);
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			htmlStr += "<span" + ca + ">" + text + "</span>";
			htmlStr += $.Cmdbuild.elementsManager.insertReturn(xmlElement);
			return htmlStr;
		},
		entry : function(xmlElement) {
			var htmlStr = "";
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			var onClick = $.Cmdbuild.utilities.getEventLikeString(xmlElement, "onClick");
			htmlStr += "<li" + ca + "><a href='#'" + onClick + ">" + text + "</a>";
			var subEntries = $.Cmdbuild.elementsManager.insertChildren(xmlElement);
			if (subEntries != "") {
				htmlStr += "<ul>" + subEntries + "</ul>";
			}
			htmlStr += "</li>";
			return htmlStr;
		},
		menu : function(xmlElement) {
			var htmlStr = "";
			var text = $.Cmdbuild.elementsManager.getText(xmlElement);
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			htmlStr += "<ul" + ca + ">";
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			if (text != null) {
				htmlStr += "<a href='#'>" + text + "</a>";
			}
			htmlStr += $.Cmdbuild.elementsManager.insertChildren(xmlElement);
			htmlStr += "</ul>";
			htmlStr += $.Cmdbuild.elementsManager.insertReturn(xmlElement);
			$.Cmdbuild.scriptsManager.push({
				script : "menu",
				id : id
			});
			return htmlStr;
		},
		divform : function(xmlElement) {
			var htmlStr = "";
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			htmlStr += "<div" + ca + "/>";
			return htmlStr;
		},
		div : function(xmlElement) {
			var htmlStr = "";
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			htmlStr += "<div" + ca + ">";
			htmlStr += $.Cmdbuild.elementsManager.insertChildren(xmlElement);
			htmlStr += "</div>";
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var param = $.Cmdbuild.elementsManager.getEvent("onInit", xmlElement);
			var parameters = $.Cmdbuild.elementsManager.getParams(xmlElement);
			$.Cmdbuild.dataModel.prepareCallerParameters(id, parameters);
			if (param) {
				param.script = 'init';
				param.id = id;
				$.Cmdbuild.scriptsManager.push(param);
			}
			return htmlStr;
		},
		dl : function(xmlElement) {
			var htmlStr = "";
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			htmlStr += "<dl" + ca + ">";
			htmlStr += $.Cmdbuild.elementsManager.insertChildren(xmlElement);
			htmlStr += "</dl>";
			return htmlStr;
		},
		dd : function(xmlElement) {
			var htmlStr = "";
			var ca = $.Cmdbuild.elementsManager.getCommonAttributes(xmlElement);
			htmlStr += "<dd" + ca + ">";
			htmlStr += $.Cmdbuild.elementsManager.insertChildren(xmlElement);
			htmlStr += "</dd>";
			return htmlStr;
		},
		override : function(xmlElement) {
			var container = xmlElement.getAttribute("container");
			var htmlContainer = $("#" + container)[0];
			var htmlStr = $.Cmdbuild.elementsManager.insertChildren(xmlElement);
			htmlContainer.innerHTML = htmlStr;
			return ""; //NB: this element does not have a Htlm shape
		},
		gridNavigationBar : function(xmlElement) {
			var htmlStr = "";
			var param = {};
			$.Cmdbuild.elementsManager.getParams(xmlElement, param);
			var id = param.form;
			htmlStr += "<input type='text' id='" + id + "_filtertext' />";
			htmlStr += $.Cmdbuild.elementsManager.makeServiceButton("cmdbuildButton", id + "_filter", 
					"ui-icon-search", "$.Cmdbuild.standard.gridMenu.onFilter(\"" + id + "\", \"\")");
			htmlStr += $.Cmdbuild.elementsManager.makeServiceButton("cmdbuildButton", id + "_clearFilter", 
					"ui-icon-closethick", "$.Cmdbuild.standard.gridMenu.onClearFilter(\"" + id + "\")");
			htmlStr += "<span id='" + id + "_pageCount' class='cmdbuildGridNavigationCount'></span>";
			htmlStr += $.Cmdbuild.elementsManager.makeServiceButton("cmdbuildButton", id + "_init", "ui-icon-arrowthickstop-1-w",
					"$.Cmdbuild.standard.gridMenu.onNavigate(\"begin\", \"" + id + "\")");
			htmlStr += $.Cmdbuild.elementsManager.makeServiceButton("cmdbuildButton", id + "_previous", "ui-icon-arrowthick-1-w",
					"$.Cmdbuild.standard.gridMenu.onNavigate(\"previous\", \"" + id + "\")");
			htmlStr += $.Cmdbuild.elementsManager.makeServiceButton("cmdbuildButton", id + "_next", "ui-icon-arrowthick-1-e",
					"$.Cmdbuild.standard.gridMenu.onNavigate(\"next\", \"" + id + "\")");
			htmlStr += $.Cmdbuild.elementsManager.makeServiceButton("cmdbuildButton", id + "_end", "ui-icon-arrowthickstop-1-e",
					"$.Cmdbuild.standard.gridMenu.onNavigate(\"end\", \"" + id + "\")");
			return htmlStr;
		},
		grid : function(xmlElement) {
			var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlElement);
			var htmlStr = "<table id='" + id + "' class='display' style='width: 100%;'></table>";
			var param = {
				script : "grid",
				id : id
			};
			$.Cmdbuild.elementsManager.getParams(xmlElement, param);
			param.onClick = $.Cmdbuild.elementsManager.getEvent("onClick", xmlElement);
			$.Cmdbuild.scriptsManager.push(param);
			return htmlStr;
		},
		lookupButtons : function(id, container, eventSearch, eventClear) {
			var htmlStr = "";
			htmlStr += $.Cmdbuild.elementsManager.makeServiceButton("cmdbuildButton", id + "_search", "ui-icon-search",
					eventSearch);
			htmlStr += $.Cmdbuild.elementsManager.makeServiceButton("cmdbuildButton", id + "_clearFilter",
					"ui-icon-closethick", eventClear);
			return htmlStr;
		},
		lookupDialog : function(id, param) {
			var xmlStr = "<form" + " id='" + id + "_dialog' >";
			xmlStr += "<params>";
			xmlStr += "<type>popup</type>";
			xmlStr += "</params>";
			xmlStr += "<div id='" + id + "_dialogDiv' class='cmdbuildDialogContentWrapper'>";
			xmlStr += "<onInit>";
			xmlStr += "<command>navigate</command>";
			xmlStr += "<container>" + id + "_dialogDiv</container>";
			xmlStr += "<form>" + id + "_dialogGrid</form>";
			xmlStr += "<formName>" + param.formName + "</formName>";
			xmlStr += "<fieldName>" + param.fieldName + "</fieldName>";
			xmlStr += "</onInit>";
			xmlStr += "<form id='" + id + "_dialogGrid'>";
			xmlStr += "<params>";
			xmlStr += "<type>grid</type>";
			xmlStr += "<className>" + param.className + "</className>";
			xmlStr += "<backend>" + param.backend + "</backend>";
			xmlStr += "<nRows>" + $.Cmdbuild.global.configurationValue("NUMROWGRIDREFERENCE") + "</nRows>";
			xmlStr += "<lookup>true</lookup>";
			xmlStr += "<navigation>false</navigation>";
			xmlStr += "<sort>Description</sort>";
			xmlStr += "<direction>asc</direction>";
			xmlStr += "</params>";
			xmlStr += "<onDblClick>";
			xmlStr += "<command>dialogExec</command>";
			xmlStr += "<toExecCommand>" + param.toExecCommand + "</toExecCommand>";
			xmlStr += "<bReference>" + param.bReference + "</bReference>";
			xmlStr += "<dialog>" + id + "_lookupDialog</dialog>";
			xmlStr += "</onDblClick>";
			xmlStr += "</form>";
			xmlStr += "</div>";
			xmlStr += "<div class='cmdbuildFooterButtonsWrapper'>";
			xmlStr += "<button text='Ok' id='" + id + "_dialogOk'>";
			xmlStr += "<onClick>";
			xmlStr += "<command>dialogExec</command>";
			xmlStr += "<toExecCommand>" + param.toExecCommand + "</toExecCommand>";
			xmlStr += "<bReference>" + param.bReference + "</bReference>";
			xmlStr += "<dialog>" + id + "_lookupDialog</dialog>";
			xmlStr += "</onClick>";
			xmlStr += "</button>";
			xmlStr += "<button text='Cancel' id='" + id + "_dialogCancel'>";
			xmlStr += "<onClick>";
			xmlStr += "<command>dialogClose</command>";
			xmlStr += "<dialog>" + id + "_lookupDialog</dialog>";
			xmlStr += "</onClick>";
			xmlStr += "</button>";
			xmlStr += "</div>";
			xmlStr += "</form>";
			var dp = new DOMParser();
			xDoc = dp.parseFromString(xmlStr, "text/xml");
			var xmlElement = xDoc.documentElement;
			var xmlContainer = $.Cmdbuild.elementsManager.getElement(param.container);
			xmlContainer.appendChild(xmlElement);
		}
	};
	$.Cmdbuild.standard.elements = elements;
})(jQuery);
