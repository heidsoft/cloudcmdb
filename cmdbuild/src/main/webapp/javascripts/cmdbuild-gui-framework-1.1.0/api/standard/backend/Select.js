(function($) {
	var Select = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.processName;
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
			$.Cmdbuild.utilities.proxy.getProcessStatuses(function(response) {
				this.data = response;
				$.Cmdbuild.utilities.proxy.getProcess(this.type, function(process) {
					this.flowStatus = process.defaultStatus;
					onObjectReady();
				}, this);
			}, this);
		};
		this.getData = function() {
			return this.data;
		};
		this.getValue = function() {
			return this.flowStatus;
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
	$.Cmdbuild.standard.backend.Select = Select;
}) (jQuery);
