(function($) {
	var global = {
		CQLUNDEFINEDVALUE: "CMDBUILDUNDEFINEDFIELD",

		MAILSTATUSDRAFT: "draft",
		MAILSTATUSNEW: "new",
		MAILSTATUSOUTGOING: "outgoing",
		MAILSTATUSRECEIVED: "received",
		// errors on a field
		NOVALUEONREQUIREDFIELD : "NOVALUEONREQUIREDFIELD",
		// Process statuses
		LOOKUPPROCESSSTATUSES: "FlowStatus",
		STATE_VALUE_OPEN: "",
		STATE_VALUE_SUSPENDED: "open.not_running.suspended",
		STATE_VALUE_COMPLETED: "closed.completed",
		STATE_VALUE_ABORTED: "closed.aborted",
		STATE_VALUE_ALL: "all", // Not existent
		PROCESS_STARTED: 1,
		PROCESS_SUSPENDED: 2,
		PROCESS_COMPLETED: 3,
		PROCESS_STOPPED: 4,
		PROCESS_INTERRUPTED: 5,
		// type of interactivity on a field
		READ_WRITE_REQUIRED : "READ_WRITE_REQUIRED",
		READ_WRITE : "READ_WRITE",
		READ_ONLY : "READ_ONLY",
		htmlContainer : null,
		themeCSSClass : null,
		debugMode : undefined,
		// tags that can be influenced by a withId tag
		ID_TAGS: ["onClick > dialog", "onClick > form", "onClick > container",  "onClick > *[variableId='true']",
		          "onInit > dialog", "onInit > form", "onInit > container",  "onInit > *[variableId='true']",
		          "onChange > dialog", "onChange > form", "onChange > container",  "onInit > *[variableId='true']",
		          "params > *[variableId='true']", "observe > *[variableId='true']"],
		ID_TAGSWITHVARIABLEATTRIBUTES: [["observe", "container"]],
		// configuration values
		// TODO values that have to be in a configuration file
		WIDTH_REFERENCE : 400,
		GRIDROWSCOUNT : "Page {1} of {2} (records {3})",
		GROUPOTHER : "Others",
		SELECTIONTOOLTIP : "Select element",
		maxLookupEntries : 50,
		modalMaxHeight : 600,
		modalMaxWidth : 800,

		// unify the possible interaction on a field between processes ad cards
		fieldInteractivityFromProcess : function(writable, mandatory) {
			if (writable && mandatory) {
				return this.READ_WRITE_REQUIRED;
			}
			if (writable) {
				return this.READ_WRITE;
			}
			return this.READ_ONLY;
		},
		fieldInteractivityFromCard : function(type) {
			if (type === true) {
				return this.READ_WRITE_REQUIRED;
			}
			return this.READ_WRITE;
		},
		getMaxLookupEntries : function() {
			return this.maxLookupEntries;
		},

		/**
		 * HTML container
		 */
		setHtmlContainer : function(htmlContainer) {
			this.htmlContainer = htmlContainer[0];
		},
		getHtmlContainer : function() {
			try {
				if (this.htmlContainer) {
					return this.htmlContainer;
				} else {
					var error = $.Cmdbuild.errorsManager.getError({
						message : $.Cmdbuild.errorsManager.CMERROR,
						type : $.Cmdbuild.errorsManager.CONTAINERNOTFOUND
					});
					throw error;
				}
			} catch (e) {
				$.Cmdbuild.errorsManager.log(e);
				throw e;
			}
		},
		/**
		 * Configuration document
		 */
		setConfigurationDocument : function(nameConfigurationDocument, callback) {
			var params = {
				callback : callback
			};
			// make request with callback
			$.Cmdbuild.utilities.getXmlDoc(nameConfigurationDocument,
					this.setConfigurationDocumentCB, this, params);
		},
		setConfigurationDocumentCB : function(data, params) {
			this.configurationDocument = data;
			params.callback();
		},
		getConfigurationDocument : function() {
			try {
				if (this.configurationDocument) {
					return this.configurationDocument;
				} else {
					var error = $.Cmdbuild.errorsManager
							.getError({
								message : $.Cmdbuild.errorsManager.CMERROR,
								type : $.Cmdbuild.errorsManager.CONFIGURATIONDOCUMENTNOTFOUND,
								element : ""
							});
					throw error;
				}
			} catch (e) {
				$.Cmdbuild.errorsManager.log(e);
				throw e;
			}
		},
		
		/**
		 * Getters and setters for config params
		 */
		// css class
		setThemeCSSClass : function(cssClass) {
			this.themeCSSClass = cssClass;
		},
		getThemeCSSClass : function() {
			return this.themeCSSClass;
		},

		// Api url
		setApiUrl : function(apiUrl) {
			this.apiUrl = apiUrl;
		},
		getApiUrl : function() {
			return this.apiUrl;
		},

		// App root url 
		setAppRootUrl : function(appRootUrl) {
			this.appRootUrl = appRootUrl;
		},
		getAppRootUrl : function() {
			return this.appRootUrl;
		},

		// App config url
		setAppConfigUrl : function(appConfigUrl) {
			this.appConfigUrl = appConfigUrl;
		},
		getAppConfigUrl : function() {
			return this.appConfigUrl;
		},

		// App CQL url
		setCqlUrl : function(cqlUrl) {
			this.cqlUrl = cqlUrl;
		},
		getCqlUrl : function() {
			return this.cqlUrl;
		},

		// Language
		setLanguage : function(language) {
			this.language = language;
		},
		getLanguage : function() {
			return this.language;
		},

		// Debug mode
		setDebugMode : function(debug) {
			this.debugMode = debug;
		},
		isDebugMode : function() {
			return this.debugMode;
		},

		// Authentication config
		setAuthenticationConfig : function(authConf) {
			var conf = {};

			if (typeof(authConf) === 'string') {
				conf.type = authConf;
			} else if (typeof(authConf) === 'object' && authConf && authConf.type) {
				conf = authConf;
			} else {
				conf.type = $.Cmdbuild.authentication.types.BASE;
			}

			this.authConf = conf;
		},
		getAuthenticationConfig : function() {
			return this.authConf;
		},
		configurationValue: function(name) {
			var c = $.Cmdbuild.custom.configuration;
			if (c && c[name]) {
				return c[name];
			}
			else {
				var c = $.Cmdbuild.standard.configuration;
				if (! (c && c[name])) {
					console.log("Not found config variable " + name);
					return undefined;
				}
				return c[name];
			}
		}
	};
	$.Cmdbuild.global = global;
})(jQuery);
