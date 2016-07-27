(function($) {
	var ERRORTITLE = "An error is occured!";

	AuthenticationError = function(message) {
		this.name = 'AuthenticationError';
		this.message = message || 'Authentication error';
	};
	AuthenticationError.prototype = new Error();
	AuthenticationError.prototype.constructor = AuthenticationError;

	var errorsManager = {
		isDialogOpen: false,
		CMERROR: "cmError",
		
		ERROREXPLAINED: 1,
		
		EVENTCOMMANDNOTFOUND: 101,
		EVENTMETHODNOTDEFINED: 102,
		INITNOTDEFINED: 103,

		XMLTAGNOTDEFINED: 201,
		MALFORMEDXMLNODE: 202,

		BACKENDMETHODNOTDEFINED: 301,

		FORMFIELDMETHODNOTDEFINED: 401,

		AJAXERROR: 501,
		AJAXERRORNOTFOUND: 502,

		XMLELEMENTNOTDEFINED: 601,
		MALFORMEDVARIABLE: 602,

		SCRIPTNOTDEFINED: 701,

		XMLFILENOTFOUND: 801,
		XMLFILENOTCORRECT: 802,

		PARAMNOTCORRECT: 901,
		AUTHENTICATIONERROR: 902,

		CONTAINERNOTFOUND: 1001,
		CONFIGURATIONDOCUMENTNOTFOUND: 1002,

		CLASSNOTFOUND: 1101,

		CLASSNOTFOUND: 1101,
		UNDEFINEDDEFAULTROLE: 1201,

		AuthenticationError : AuthenticationError,

		strings : {
			0: "Errore {1} non riconosciuto",
			1: "{1}",
			101: "Non è definito l'elemento Command.\nElement Xml: {1}",
			102: "Comando non riconosciuto.\nElement Xml: {1}\nComando:{2}",
			103: "Init [ {1} {2} ] nella form [ {3} ] non definito",
			201: "Elemento {1} non definito",
			202: "Nodo Xml {1} non corretto",
			301: "Backend {1} non definito",
			501: "Ajax: errata richiesta.\nUrl: {1}",
			502: "Ajax: elemento non trovato nel Datatabase.\nUrl: {1}",
			601: "Elemento Xml [ {1} ] non presente nel file di configurazione",
			602: "Formato della variabile [ {1} ]\nerrato in [ {2} ]",
			701: "Script [ {1} ] non definito in {2}",
			801: "Xml file [ {1} ] non trovato",
			802: "Xml file [ {1} ] non corretto",
			901: "Parametro [ {1} {2} ] nella form [ {3} ] non corretto",
			902: "Credenziali di login non trovate: table {1} key {2} value {3}",
			1001: "Container Html non definito",
			1002: "Configuration [ {1} ] non definito",
			1101: "Classe [ {1} ] non trovata",
			1201: "Default role non definito per questo utente"
		},

		log: function(e) {
			
//			if (typeof e == "object") {
			if (typeof e == "object" && e.cmParam) {
				console.log(e, e.cmParam.type, e.stack);
			}
			else if (typeof e == "object") {
				console.log(e, e.stack);
			}
			else {
				console.log(e, e.stack);
			}
		},
		warn: function(message) {
			console.warn(message);
		},
		error : function(message) {
			console.error(message);
		},
		popupOnRequestFields: function(errors) {
			if (errors.length > 0) {
				var strHtml = "";
				for (var i = 0; i < errors.length; i++) {
					var error = errors[i];
					strHtml += "<p>";
					if (error.message) {
						strHtml += "<strong>" + error.field + ":</strong> " + error.message;
					} else {
						strHtml += "Il campo " + error.field + " deve essere valorizzato";
					}
					strHtml += "</p>";
				}
				var theDialog = $("#errorDialog");
				theDialog.html(strHtml);
				theDialog.dialog("option", "dialogClass", $.Cmdbuild.global.getThemeCSSClass());
				theDialog.dialog("option", "title", "Error!").dialog("open");
				theDialog.dialog("option", "height", 400);
				theDialog.dialog("option", "width", 600);
				theDialog.dialog("open");
				return false;
			}
		},
		popup: function(e) {
			if (this.isDialogOpen) {
				return;
			}
			this.isDialogOpen = true;
			this.log(e);
			var htmlStr = "";
			htmlStr += "<h2>" +  ERRORTITLE + "</h2>";
			if (e.message == this.CMERROR) {
				htmlStr += "<p>" +  e.cmParam.type + "</p>";
				htmlStr += "<p>" +  this.getErrorMessage(e) + "</p>";
			}
			else {
				htmlStr += "<p>" +  e.message + "</p>";
			}
			var theDialog = $("#errorDialog");
			theDialog.html(htmlStr);
			theDialog.dialog("option", "dialogClass", $.Cmdbuild.global.getThemeCSSClass());
			theDialog.dialog("option", "title", e.name).dialog("open");
			theDialog.dialog("option", "height", 400);
			theDialog.dialog("option", "width", 600);
			theDialog.dialog("open");
		},
		close: function() {
			this.isDialogOpen = false;
		},
		getErrorMessage: function(e) {
			switch (e.cmParam.type) {
				case this.ERROREXPLAINED :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.EVENTCOMMANDNOTFOUND :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.EVENTMETHODNOTDEFINED :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element, e.cmParam.method);
				case this.INITNOTDEFINED :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.param, e.cmParam.method, e.cmParam.element);
				case this.XMLTAGNOTDEFINED :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.MALFORMEDXMLNODE :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.BACKENDMETHODNOTDEFINED :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.AJAXERROR :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.AJAXERRORNOTFOUND :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.XMLELEMENTNOTDEFINED :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.MALFORMEDVARIABLE :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.variable, e.cmParam.element);
				case this.SCRIPTNOTDEFINED :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.XMLFILENOTFOUND :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.XMLFILENOTCORRECT :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.PARAMNOTCORRECT :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.param, e.cmParam.method, e.cmParam.element);
				case this.AUTHENTICATIONERROR :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.table, e.cmParam.key, e.cmParam.value);
				case this.CONTAINERNOTFOUND :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type]);
				case this.CONFIGURATIONDOCUMENTNOTFOUND :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.CLASSNOTFOUND :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type], e.cmParam.element);
				case this.UNDEFINEDDEFAULTROLE :
					return $.Cmdbuild.utilities.formatVarString(this.strings[e.cmParam.type]);
				default:
					return $.Cmdbuild.utilities.formatVarString(this.strings[0], e.cmParam.type, e.cmParam.element);
			}
		},
		getError: function(param) {
			var error = new Error(param.message);
			error.cmParam = param;
			return error;
		}
	};
	$.Cmdbuild.errorsManager = errorsManager;
}) (jQuery);