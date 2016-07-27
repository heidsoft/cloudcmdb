(function($) {
	var MailList = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.data =  [];
		this.metadata = {};
		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.init = function() {
			this.loadAttributes(this.param);
		};
		this.loadAttributes = function() {
			this.attributes = [
				{
					type: "string",
					name: "status",
					description: "Status",
					displayableInList: true
				},
				{
					type: "string",
					name: "to",
					description: "Addresses",
					displayableInList: true
				},
				{
					type: "text",
					name: "subject",
					description: "Subject",
					displayableInList: true
				}
			];
			var me = this;
			this.TM = setTimeout(function() { onObjectReady(); clearTimeout(me.TM); }, 500);
		};
		this.loadData = function(param, callback, callbackScope) {
			var formObject = $.Cmdbuild.dataModel.forms[this.param.formData];
			var backendData = formObject.getBackendData().mails;
			if (this.param.onlyTemplates === "true") {
				this.data = [];
				for (var i = 0; i < backendData.length; i++) {
					if (backendData[i].fromTemplate) {
						this.data.push(backendData[i]);
					}
				}
			} else {
				this.data = backendData;
			}
			this.data.sort(function(a, b) {
				return (a.status >= b.status) ? 1 : -1;
			});
			callback.apply(callbackScope);
		};
		this.disableRowButtons = function(index) {
			switch (this.data[index].status) {
				case $.Cmdbuild.global.MAILSTATUSDRAFT:
					if (! this.data[index].fromTemplate) {
						return [$.Cmdbuild.widgets.ManageEmail.ROWBUTTONREGENERATE];
					} else {
						return [];
					}
				case $.Cmdbuild.global.MAILSTATUSNEW:
					return [];
				case $.Cmdbuild.global.MAILSTATUSOUTGOING:
					return [$.Cmdbuild.widgets.ManageEmail.ROWBUTTONREGENERATE, 
					        $.Cmdbuild.widgets.ManageEmail.ROWBUTTONREPLY];
				case $.Cmdbuild.global.MAILSTATUSRECEIVED:
					return [$.Cmdbuild.widgets.ManageEmail.ROWBUTTONREGENERATE,
					        $.Cmdbuild.widgets.ManageEmail.ROWBUTTONMODIFY, 
					        $.Cmdbuild.widgets.ManageEmail.ROWBUTTONDELETE];
				default:
					return [$.Cmdbuild.widgets.ManageEmail.ROWBUTTONREGENERATE, 
					        $.Cmdbuild.widgets.ManageEmail.ROWBUTTONREPLY, 
					        $.Cmdbuild.widgets.ManageEmail.ROWBUTTONMODIFY, 
					        $.Cmdbuild.widgets.ManageEmail.ROWBUTTONDELETE];
				
			} 
		};
		this.getAttributes = function() {
			return this.attributes;
		};
		this.getData = function() {
			return this.data;
		};
		this.getMetadata = function() {
			return this.metadata;
		};

		this.getTotalRows = function() {
			return this.getData().length;
		};

		/**
		 * Private functions
		 */
		var onObjectReady = function() {
			onReadyFunction.apply(onReadyScope);
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.standard.backend.MailList = MailList;
}) (jQuery);
