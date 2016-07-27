(function($) {
	var start = {
		scriptsToLoadDebug : [
			"libraries/plugin/jqueryui/jquery-ui.js",
			"libraries/plugin/datatables/jquery.dataTables.js",
			"libraries/plugin/datatables/dataTables.jqueryui.js",
			"libraries/plugin/datatables/dataTables.fixedColumns.min.js",
			"libraries/plugin/clEditor/jquery.cleditor.js",
			"libraries/plugin/jstree/jstree.min.js",
			"libraries/plugin/jstree/jstree-actions.js",
			"libraries/plugin/mask/mask.js",
			"libraries/plugin/timepicker/jquery-ui-timepicker-addon.js",
			"libraries/plugin/waitme/waitMe.js",
			"libraries/plugin/validation/jquery.validation.js",
			"core/errorsManager.js",
			"core/global.js", 
			"core/model.js",
			"core/loadingDialog.js",
			"core/eventsManager.js",
			"core/elementsManager.js",
			"core/scriptsManager.js",
			"core/utilities.js", 
			"core/ldap.js", 
			"core/proxy.js", 
			"core/proxyGuiServerSide.js", 
			"core/sprintf.js", 
			"core/authProxy.js",
			"core/contextStack.js",
			"core/authentication.js",
			"core/translations.js",
			"core/cookie.js",
			"core/fieldsManager.js",
			"cql/CqlManager.js",
			"cql/Configurator.js",
			"standard/backend/Activity.js",
			"standard/backend/ActivityHelp.js",
			"standard/backend/ActivityList.js",
			"standard/backend/Attachments.js",
			"standard/backend/AttachmentsWidget.js",
			"standard/backend/Card.js",
			"standard/backend/CardList.js",
			"standard/backend/DelayTable.js",
			"standard/backend/DmsCategory.js",
			"standard/backend/DomainList.js",
			"standard/backend/DomainListOnProcesses.js",
			"standard/backend/HelpNewActivity.js",
			"standard/backend/Lookup.js",
			"standard/backend/Mail.js",
			"standard/backend/MailList.js",
			"standard/backend/NewActivity.js",
			"standard/backend/Process.js",
			"standard/backend/ReferenceActivityList.js",
			"standard/backend/Report.js",
			"standard/backend/Select.js",
			"standard/config.js",
			"standard/form.js",
			"standard/grid.js",
			"standard/navigation.js",
			"standard/tabbed.js",
			"standard/div.js",
			"standard/widgetDiv.js",
			"standard/elements.js",
			"standard/scripts.js",
			"standard/commands.js",
			"standard/popup.js",
			"standard/lookupField.js",
			"standard/graph.js",
			"standard/selectField.js",
			"standard/referenceField.js",
			"standard/widgets.js",
			"standard/widgets/LinkCards.js",
			"standard/widgets/ManageEmail.js",
			"standard/widgets/OpenAttachment.js"
		],
		scriptsToLoad : [
			"plugin.min.js",
			"core.min.js",
			"cql.min.js",
			"standard.min.js",
			"widgets.min.js",
			"backends.min.js"
		],

		stylesToLoad : [
			"libraries/plugin/jqueryui/jquery-ui.css",
			"libraries/plugin/jqueryui/jquery-ui.structure.css",
			"libraries/plugin/datatables/dataTables.jqueryui.css",
			"libraries/plugin/datatables/dataTables.fixedColumns.min.css",
			"libraries/plugin/waitme/waitMe.min.css",
			"libraries/plugin/clEditor/jquery.cleditor.css",
			"libraries/plugin/jstree/jstree.css",
			"libraries/plugin/timepicker/jquery-ui-timepicker-addon.css",
			"libraries/css/cmdbuild-gridlayout.css",
			"libraries/css/cmdbuildstyle.css"
		],
		defaultTheme : "libraries/plugin/jqueryui/jquery-ui.theme.css",

		loadAndStart : function(configParams, container) {
			this.configParams = configParams;
			this.htmlContainer = container;
			// get full url scripts
			var staticScripts = this.getStatisScripts();
			var customScripts = this.getCustomScripts(configParams.customjs);
			var allScripts = $.merge(staticScripts, customScripts);

			// load javascripts
			var startIndex = 0;
			this.loadScript(allScripts, startIndex);
			// load all stylesheets
			this.loadStaticStylesheets();
			this.loadThemeStylesheet(configParams.theme);
			this.loadHttpCallParameters(this.configParams.httpCallParameters);
		},
		loadHttpCallParameters: function(httpCallParameters) {
			this.httpCallParameters = httpCallParameters;
		},

		/*
		 * $.ajax doesn't allow async option for cross-domain requests,
		 * then we need to load all scripts with this recursive method.
		 */
		loadScript: function(allScripts, currentIndex) {
			var me = this;
			var script = allScripts[currentIndex];
			$.ajax({
				async: false,
				url: script,
				crossDomain: true,
				dataType: "script",
				cache: !this.configParams.debug,
				success : function(data, status) {
					var nextIndex = currentIndex +1;
					if (nextIndex == allScripts.length) {
						me.onLoadCMDBuildLibraries();
					} else {
						me.loadScript(allScripts, nextIndex);
					}
				}
			});
		},

		onLoadCMDBuildLibraries: function() {
			// FIX problem in IE9 with console.log
			if (typeof console==='undefined') {
				var f = function() {};
				console = {'log':f, 'info':f, 'warn':f, 'error':f, 'debug':f};
			};


			// set config params
			$.Cmdbuild.customvariables = {};
			$.Cmdbuild.global.setThemeCSSClass(this.configParams.cssClass);
			$.Cmdbuild.global.setApiUrl($.Cmdbuild.apiUrl);
			$.Cmdbuild.global.setAppRootUrl($.Cmdbuild.appRootUrl);
			$.Cmdbuild.global.setAppConfigUrl($.Cmdbuild.appConfigUrl);
			$.Cmdbuild.global.setAuthenticationConfig(this.configParams.authentication);
			$.Cmdbuild.global.setLanguage(this.configParams.language);
			$.Cmdbuild.global.setDebugMode(this.configParams.debug);

			// set container
			$.Cmdbuild.global.setHtmlContainer(this.htmlContainer);

			// load CQL manager
			var output = function(msg) {
				if ($.Cmdbuild.global.isDebugMode()) {
					// if in debug mode the cql writes here
					console.log(msg.msg);
				}
			};
			var configurator = new Configurator(output);
			$.Cmdbuild.CqlManager = new CqlManager(configurator);

			// start xml file
			var configDocument = $.Cmdbuild.appConfigUrl + this.configParams.start;
			$.Cmdbuild.global.setConfigurationDocument(configDocument, this.onConfigurationReady);
		},

		onConfigurationReady: function() {
			// load translations
			$.Cmdbuild.translations.loadTranslations(function() {
				// make authentication before start
				var token = $.Cmdbuild.authentication.getAuthenticationToken();
				if (!token) {
					$.Cmdbuild.authentication.authenticate();
				} else {
					$.Cmdbuild.utilities.startApplication();
				}
			}, this);
		},

		getStatisScripts: function() {
			var fullUrlScripts = [];
			var scripts = this.scriptsToLoad;
			if (this.configParams.debug) {
				scripts = this.scriptsToLoadDebug;
			}
			for (var i = 0; i < scripts.length; i++) {
				fullUrlScripts.push($.Cmdbuild.appRootUrl + scripts[i]);
			}
			return fullUrlScripts;
		},

		getCustomScripts: function(scripts) {
			var fullUrlScripts = [];
			var baseUrl = $.Cmdbuild.appConfigUrl + 'customjs/';
			for (var i = 0; i < scripts.length; i++) {
				fullUrlScripts.push(baseUrl + scripts[i]);
			}
			return fullUrlScripts;
		},

		loadStylesheet : function(url) {
			var cssLink = $("<link rel='stylesheet' type='text/css' href='" + url + "'>");
			$("head").append(cssLink); 
		},

		loadStaticStylesheets: function() {
			var me = this;
			var stylesheets = this.stylesToLoad;
			for (var i = 0; i < stylesheets.length; i++) {
				var href = $.Cmdbuild.appRootUrl + stylesheets[i];
				me.loadStylesheet(href);
			}
		},

		loadThemeStylesheet: function(theme) {
			if (theme) {
				var base_url = $.Cmdbuild.appConfigUrl + 'theme/';
				if ($.isArray(theme)) {
					for (var i = 0; i < theme.length; i++) {
						this.loadStylesheet(base_url + theme[i]);
					}
				} else {
					this.loadStylesheet(base_url + theme);
				}
			} else {
				this.loadStylesheet($.Cmdbuild.appRootUrl + this.defaultTheme);
			}
		}
	};
	$.Cmdbuild.start = start;
}) (jQuery);