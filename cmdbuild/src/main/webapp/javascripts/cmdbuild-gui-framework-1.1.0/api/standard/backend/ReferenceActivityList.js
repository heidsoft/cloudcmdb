(function($) {
	var ReferenceActivityList = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.type = param.className;
		this.data =  [];
		this.metadata = {};
		this.listData = []; // there is a difference from the cards that comes from list and get card :(
		this.typeFilter = "attribute";
		this.filter = {};
		this.filter["attribute"] = {
			simple: {
				attribute: "_status",
				operator: "in",
				value: [$.Cmdbuild.global.PROCESS_STARTED]
			}
		};
				
		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
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
			onObjectReady();
		};
		this.loadData = function(param, callback, callbackScope) {
			if (this.noValidFilter) {
				this.data = [];
				callback.apply(callbackScope, this.data);
				return;
			}
			var config = {
					page : param.currentPage,
					start : param.firstRow,
					limit : param.nRows,
					sort : param.sort,
					direction : param.direction,
					filter : this.filter,
					active: true,
					filterType : this.typeFilter
			};
			$.Cmdbuild.utilities.proxy.getActivityList(this.type, config,
					function(response, metadata) {
						this.data = response;
						this.metadata = metadata;
						callback.apply(callbackScope, this.data);
					}, this);
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
		 * Custom functions
		 */
		this.getTotalRows = function() {
			var metadata = this.getMetadata();
			return metadata && metadata.total ? metadata.total : this.getData().length;
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.standard.backend.ReferenceActivityList = ReferenceActivityList;
}) (jQuery);