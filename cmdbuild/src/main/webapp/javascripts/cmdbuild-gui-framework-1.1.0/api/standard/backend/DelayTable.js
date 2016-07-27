(function($) {
	var DelayTable = function(param, onReadyFunction, onReadyScope) {
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
			this.data = [
             	{	_id: "Hour_1", description: "1 Hour"	},
         		{	_id: "Hour_2", description: "2 Hours"	},
         		{	_id: "Hour_4", description: "4 Hours"	},
         		{	_id: "Day_1", description: "1 Day"		},
         		{	_id: "Day_2", description: "2 Days"		},
         		{	_id: "Day_4", description: "4 Days"    }
			];
			var me = this;
			this.TM = setTimeout(function() { onObjectReady(); clearTimeout(me.TM); }, 500);
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
	$.Cmdbuild.standard.backend.DelayTable = DelayTable;
}) (jQuery);
