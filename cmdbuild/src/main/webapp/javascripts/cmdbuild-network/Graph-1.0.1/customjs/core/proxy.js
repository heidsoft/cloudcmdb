(function($) {
	var methods = {
		GET : "GET",
		POST : "POST",
		PUT : "PUT",
		DELETE : "DELETE"
	};

	var proxy = {
		getGraphConfiguration : function(callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'configuration/graph';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(
					response) {
				callback.apply(callbackScope, [ response ]);
			});
		},
		
		getDomainTrees: function(config, callback, callbackScope) {
				// params
			var params = $.Cmdbuild.utilities.proxy.prepareParamsForList(config);
			// get url and make request
			callbackObj = {
					success: function(data, metadata){
						callback.apply(callbackScope, [data, metadata]);
					},
					fail: function(response){
						callback.apply(callbackScope, [[], []]);
					}
				};
			var url = $.Cmdbuild.global.getApiUrl() + 'domainTrees/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, callbackObj, params);
		},
		getDomainTree: function(id, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'domainTrees/' + id;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, {});
		},
		
		// transforms processes in classes
		getClassAttributes : function(type, callback, callbackScope) {
			if (type === $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				console.log("ERRORE GUICOMPOUNDNODE", Error().stack);
				callback.apply(callbackScope, []);
			}
			else if ($.Cmdbuild.customvariables.cacheProcess.isProcess(type)) {
				$.Cmdbuild.utilities.proxy.getProcessAttributes(type, callback,
						callbackScope);

			} else {
				$.Cmdbuild.utilities.proxy.getClassAttributes(type, callback,
						callbackScope);
			}
		},
		getCardData : function(type, cardId, config, callback, callbackScope) {
			if (type === $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				console.log("ERRORE GUICOMPOUNDNODE", Error().stack);
				callback.apply(callbackScope, []);
			}
			else if ($.Cmdbuild.customvariables.cacheProcess.isProcess(type)) {
				$.Cmdbuild.utilities.proxy.getCardProcess(type, cardId, config,
						callback, callbackScope);

			} else {
				$.Cmdbuild.utilities.proxy.getCardData(type, cardId, config,
						callback, callbackScope);
			}
		},
		getClass : function(type, callback, callbackScope) {
			if (type === $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				console.log("ERRORE GUICOMPOUNDNODE", Error().stack);
				callback.apply(callbackScope, []);
			}
			else if ($.Cmdbuild.customvariables.cacheProcess.isProcess(type)) {
				$.Cmdbuild.utilities.proxy.getProcess(type, callback,
						callbackScope);

			} else {
				$.Cmdbuild.utilities.proxy.getClass(type, callback,
						callbackScope);
			}
		},
		//here only for this release. It's just committed in the new CORE
		getIcons : function(config, callback, callbackScope) {
			// params
			var params = $.Cmdbuild.utilities.proxy.prepareParamsForList(config);
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'icons/';
			var callbackObj = undefined;
			if (typeof(callback) === "function") {
				callbackObj = {
						success: function(data, metadata){
							callback.apply(callbackScope, [data, metadata]);
						},
						fail: function(response){
							console.log("Error on icons! The icons cannot be load");
							callback.apply(callbackScope, [[], []]);
						}
					};
			} else  {
				callbackObj = callback;					
			}
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, callbackObj, params);
		}

	};
	$.Cmdbuild.g3d.proxy = proxy;
})(jQuery);
