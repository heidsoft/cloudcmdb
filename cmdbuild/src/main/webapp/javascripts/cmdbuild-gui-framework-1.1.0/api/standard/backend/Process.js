(function($) {
	var Process = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.className;
		this.processInstanceId = param.cardId;
		this.data =  [];
		this.metadata = {};
		this.widgets = undefined;

		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.updateData = function(param, callBack) {
//			var data = {};
//			for (var i = 0; i < this.attributes.length; i++) {
//				data[this.attributes[i]._id] = param.data[this.attributes[i]._id];
//			}
//			data["_advance"] = (param.advance == "true");
//			data["_activity"] = this.processActivityId;
//			$.Cmdbuild.utilities.proxy.putStepProcess(this.type, this.processInstanceId,
//					data, callBack, this);
		};
		this.init = function() {
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			$.Cmdbuild.utilities.proxy.getProcessAttributes(this.type,
					this.loadAttributesCallback, this);
		};
		// load Attributes and its callback
		this.loadAttributesCallback = function(attributes) {
			this.attributes = attributes;
			this.loadData(this.param);
		};
		this.loadDataCallback = function(response, metadata) {
			this.data = response;
			this.data["activityTitle"] = this.activityTitle;
			this.metadata = metadata;
			onObjectReady();
		};
		this.loadData = function() {
			$.Cmdbuild.utilities.proxy.getCardProcess(this.type, this.param.cardId, {}, this.loadDataCallback, this);
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
	$.Cmdbuild.standard.backend.Process = Process;
}) (jQuery);
