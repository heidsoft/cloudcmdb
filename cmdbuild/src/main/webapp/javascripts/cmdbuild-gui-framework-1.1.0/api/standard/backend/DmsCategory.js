(function($) {
	var DmsCategory = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.lookupName;
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
		this.loadDataCallback = function(response, metadata) {
			this.data = response;
			this.metadata = metadata;
			onObjectReady();
		};
		this.loadAttributes = function() {
			//$.Cmdbuild.utilities.proxy.getLookupType(this.type, {}, this.loadData, this);
			this.loadData();
		};
		this.loadData = function(response, metadata) {
			$.Cmdbuild.utilities.proxy.getAttachmentsCategories(this.loadDataCallback, this);
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
	$.Cmdbuild.standard.backend.DmsCategory = DmsCategory;
}) (jQuery);
