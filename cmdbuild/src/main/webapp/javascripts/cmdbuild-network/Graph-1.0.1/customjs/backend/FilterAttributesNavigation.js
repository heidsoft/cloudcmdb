(function($) {
	var FilterAttributesNavigation = function(param, onReadyFunction,
			onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.classId;
		this.data = [];
		this.metadata = {};
		var configuration = $.Cmdbuild.custom.configuration;
		if (!$.Cmdbuild.custom.configuration.temporaryFilterByAttributes) {
			$.Cmdbuild.custom.configuration.temporaryFilterByAttributes = [];
		}
		if (!$.Cmdbuild.custom.configuration.temporaryFilterByAttributes[param.classId]) {
			$.Cmdbuild.custom.configuration.temporaryFilterByAttributes[param.classId] = {};
		}
		this.temporaryFilterByAttributes = $.Cmdbuild.custom.configuration.temporaryFilterByAttributes[param.classId];
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
			this.loadData(this.param);
		};
		this.loadAttributes = function() {
			this.attributes = [];
			for ( var key in this.temporaryFilterByAttributes) {
				var attribute = this.temporaryFilterByAttributes[key].attribute;
				this.attributes.push(attribute);
			}
		};
		this.loadData = function(response, metadata) {
			this.data = {};
			for (var i = 0; i < this.attributes.length; i++) {
				var attribute = this.attributes[i];
				this.data[attribute._id] = this.temporaryFilterByAttributes[attribute._id].data;
			}
			setTimeout(function() {
				onObjectReady();
			}, 100);
		};
		this.getData = function() {
			return this.data;
		};
		this.getAttributes = function() {
			return this.attributes;
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
	$.Cmdbuild.standard.backend.FilterAttributesNavigation = FilterAttributesNavigation;

})(jQuery);
