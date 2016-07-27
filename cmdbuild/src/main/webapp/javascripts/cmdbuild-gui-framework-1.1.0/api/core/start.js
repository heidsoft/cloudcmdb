(function($) {
	var start = {
		scriptsToLoad : [
			"libraries/jquery-ui.js",
			"libraries/jquery.dataTables.js",
			"libraries/dataTables.jqueryui.js",
			"libraries/dataTables.fixedColumns.min.js",
			"libraries/js/jquery.validation.js",
			"libraries/js/jstree.min.js",
			"libraries/js/jstree-actions.js",
			"libraries/js/waitMe.min.js",
			"libraries/js/clEditor/jquery.cleditor.js",
			"libraries/js/jquery-ui-timepicker-addon.js",
			"core/errorsManager.js",
			"core/global.js", 
			"core/model.js",
			"core/errorsManager.js",
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
			"standard/backend/Card.js",
			"standard/backend/Lookup.js",
			"standard/backend/Select.js",
			"standard/backend/Process.js",
			"standard/backend/Activity.js",
			"standard/backend/CardList.js",
			"standard/backend/DomainList.js",
			"standard/backend/NewActivity.js",
			"standard/backend/DmsCategory.js",
			"standard/backend/Attachements.js",
			"standard/backend/ActivityHelp.js",
			"standard/backend/ActivityList.js",
			"standard/backend/HelpNewActivity.js",
			"standard/backend/DomainListOnProcesses.js",
			"standard/backend/ReferenceActivityList.js",
			"standard/backend/Report.js",
			"standard/backend/ProcessAttachments.js",
			"standard/backend/Mail.js",
			"standard/backend/MailList.js",
			"standard/backend/DelayTable.js",
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
			"standard/widgets/widgets.js",
			"standard/widgets/LinkCards.js",
			"standard/widgets/ManageEmail.js",
			"standard/widgets/OpenAttachment.js"
		],

		stylesToLoad : [
			"libraries/jquery-ui.css",
			"libraries/jquery-ui.structure.css",
			"libraries/dataTables.jqueryui.css",
			"libraries/cmdbuild-gridlayout.css",
			"libraries/dataTables.fixedColumns.min.css",
			"libraries/css/cmdbuildstyle.css",
			"libraries/css/waitMe.min.css",
			"libraries/js/clEditor/jquery.cleditor.css",
			"libraries/css/jstree.css",
			"libraries/css/jquery-ui-timepicker-addon.css"
		],
		defaultTheme : "libraries/jquery-ui.theme.css",

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
				cache: false,
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

			var output = function(msg) {
				//TODO: Remove?
			};

			// load CQL manager
			var configurator = new Configurator(output);
			$.Cmdbuild.CqlManager = new CqlManager(configurator);

			// set config params
			$.Cmdbuild.customvariables = {};
			$.Cmdbuild.global.setThemeCSSClass(this.configParams.cssClass);
			$.Cmdbuild.global.setApiUrl($.Cmdbuild.apiUrl);
			$.Cmdbuild.global.setAppRootUrl($.Cmdbuild.appRootUrl);
			$.Cmdbuild.global.setAppConfigUrl($.Cmdbuild.appConfigUrl);
			$.Cmdbuild.global.setAuthenticationConfig(this.configParams.authentication);
			$.Cmdbuild.global.setLanguage(this.configParams.language);

			// set container
			$.Cmdbuild.global.setHtmlContainer(this.htmlContainer);

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