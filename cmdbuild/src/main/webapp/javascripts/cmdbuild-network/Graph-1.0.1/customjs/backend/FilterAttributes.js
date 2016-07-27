(function($) {
	var FilterAttributes = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.classId;
		this.data = [];
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
			this.loadData(this.param);
		};
		this.push = function(obj) {
			for (var i = 0; i < this.data.length; i++) {
				if (obj._id === this.data[i]._id) {
					return;
				}
			}
			this.data.push(obj);
		};
		this.loadData = function(response, metadata) {
			if (!this.type) {
				this.attributes = [];
				this.data = [];
				setTimeout(function() {
					onObjectReady();
				}, 100);
				return;
			}
			if (this.type == $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				this.attributes = $.Cmdbuild.g3d.constants.COMPOUND_ATTRIBUTES;
				this.data = this.dataFromAttributes(this.attributes);
				setTimeout(function() {
					onObjectReady();
				}, 100);
			} else {
				$.Cmdbuild.g3d.proxy.getClassAttributes(this.type,
						this.loadAttributesCallback, this);
			}
		};
		this.loadAttributesCallback = function(attributes) {
			this.originalAttributes = attributes.slice();
			if (this.param.displayableInList === "true") {
				attributes = this.removeIfNotGridAttribute(attributes);
			}
			if (this.param.withNotes != "true") {
				this.attributes = $.Cmdbuild.utilities.removeAttribute(
						attributes, "Notes");
			} else {
				this.attributes = attributes;
			}
			$.Cmdbuild.utilities.sortAttributes(attributes);
			$.Cmdbuild.utilities.changeAttributeType(this.attributes, "Notes",
					"text");
			this.dataFromAttributes(this.attributes);
			setTimeout(function() {
				onObjectReady();
			}, 10);

		};
		this.dataFromAttributes = function(attributes) {
			for (var i = 0; i < attributes.length; i++) {
				var attribute = attributes[i];
				this.push(attribute);				
			}
		};
		this.getData = function() {
			return	 this.data;
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
	$.Cmdbuild.standard.backend.FilterAttributes = FilterAttributes;

})(jQuery);

