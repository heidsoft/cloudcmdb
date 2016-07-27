(function($) {
	/* La chiamata ai passi successivi del processo:
	 * 	http://localhost:8080/cmdbuild/services/json/workflow/getactivityinstance?_dc=1410793789724&
		classId=380000&cardId=1259891&
		activityInstanceId=151051_17606_Package_gestionerichieste_Process_gestionerichieste_E03f-Chiusuravar 
	 */
	var methods = {
		GET : "GET",
		POST : "POST",
		PUT : "PUT",
		DELETE : "DELETE"
	};
	
	var proxyGuiServerSide = {
		/**
		 * Getters for base URLs
		 */
		getBaseUrlCard : function() {
			return $.Cmdbuild.guiJavaUrlKey + "services/gui/ldapmanager/";
		},

		/**
		 * Utilities
		 */
		/**
		 * Getters for resources
		 */
		postLdap: function(params, callback, callbackScope) {
			var url = this.getBaseUrlCard();
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.POST, function(response) {
				callback.apply(callbackScope, [response]);
			}, params);
		}
	};
	$.Cmdbuild.utilities.proxyGuiServerSide = proxyGuiServerSide;
}) (jQuery);
