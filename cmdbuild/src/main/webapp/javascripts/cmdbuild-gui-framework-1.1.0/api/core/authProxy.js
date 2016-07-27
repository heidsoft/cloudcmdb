(function($) {
	var authProxy = {
		AUTHENTICATION_HEADER : "CMDBuild-Authorization",
		contentType : 'application/json',
		dataType : "json",
		loaderrequests : 0,

		/**
		 * @desc This function make ajax request to the server
		 * @param {String} request URL
		 * @param {String} HTTP method. Allowed methods are GET, POST, PUT and DELETE
		 * @param {function} callback
		 * @param {Object} custom parameters for the request
		 */
		makeAjaxRequest: function(url, method, callback, params, noJsonRequest) {
			var token = $.Cmdbuild.authentication.getAuthenticationToken();
			var language = $.Cmdbuild.global.getLanguage();
			if (!token) {
				$.Cmdbuild.authentication.authenticate();
			} else  {
				var headers = {};
				headers[this.AUTHENTICATION_HEADER] = token;
				if (language) {
					headers["CMDBuild-Localized"] = true;
					headers["CMDBuild-Localization"] = language;
				}
				if (noJsonRequest && noJsonRequest===true) {
					this.noJSONAjaxRequest(url, method, callback, params, headers);
				} else {
					this.directMakeAjaxRequest(url, method, callback, params, headers);
				}
			}
		},
		directMakeAjaxRequest: function(url, method, callback, params, headers) {
			// loading overlay
			var me = this;
			this.maskRequest();
			// ajax request
			$.ajax({ 
				// custom params
				type: method,
				url: url,
				data: params,
				cache : false,
				// default params
				dataType : this.dataType,
				crossDomain : true,
				contentType : this.contentType,
				headers : headers
			}).done(function(data) {
				// success method
				var returnData = (data) ? data.data : undefined;
				var returnMeta = (data) ? data.meta : undefined;
				if (typeof(callback) === "function") {
					callback(returnData, returnMeta);					
				} else if (callback.success) {
					callback.success(returnData, returnMeta);					
				}
			}).fail(function(XMLHttpRequest, textStatus, errorThrown) {
				// failure method
				if (typeof(callback) === "function") {
					console.log("Ajax error = " + url + " -- ", params, Error().stack);					
				} else if (callback.fail) {
					callback.fail();					
				}
				
			}).always(function() {
				// callback
				// remove loading overlay
				if (typeof(callback) !== "function" && callback.callback) {
					callback.callback();					
				}
				
				me.unmaskRequest();
			});
		},

		noJSONAjaxRequest : function(url, method, callback, params, headers) {
			// loading overlay
			var me = this;
			this.maskRequest();

			$.ajax({ 
				// custom params
				type: method,
				url: url,
				data: params,
				cache : false,
				// default params
				contentType: false,
				processData: false,
				headers : headers,
			}).done(function(data) {
				// success method
				if (typeof(callback) === "function") {
					callback(data);					
				} else if (callback.success) {
					callback.success(data);					
				}
			}).fail(function(XMLHttpRequest, textStatus, errorThrown) {
				// failure method
				if (typeof(callback) === "function") {
					console.log("Ajax error = " + url + " -- ", params, Error().stack);					
				} else if (callback.fail) {
					callback.fail();					
				}
			}).always(function() {
				if (typeof(callback) !== "function" && callback.callback) {
					callback.callback();					
				}
				me.unmaskRequest();
			});
		},

		/*
		 * Add mask overlay
		 */
		maskRequest : function() {
			var containerId = $.Cmdbuild.global.getHtmlContainer().getAttribute("id");
			if (this.loaderrequests === 0) {
				$("#" + containerId).waitMe({
					effect : 'stretch',
					color : "#1c94c4"
				});
			}
			this.loaderrequests++;
		},
		/*
		 * Remove mask overlay
		 */
		unmaskRequest : function() {
			var containerId = $.Cmdbuild.global.getHtmlContainer().getAttribute("id");
			this.loaderrequests--;
			if (this.loaderrequests === 0) {
				$("#" + containerId).delay(500).waitMe("hide");
			}
		}
	};
	$.Cmdbuild.authProxy = authProxy;
})(jQuery);