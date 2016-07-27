(function($) {
	var HelpNewActivity = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.className;
		this.processInstanceId = undefined;//param.cardId;
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
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			$.Cmdbuild.utilities.proxy.getProcessAttributes(this.type,
					this.loadAttributesCallback, this);
		};
		// load Attributes and its callback
		this.loadAttributesCallback = function(attributes) {
			$.Cmdbuild.utilities.proxy.getStartProcessActivities(this.type, this.processInstanceId,
					function(response) {
						if (response.length > 0) {
							this.description = response[0].description;
							var processActivityId = response[0]._id;
							$.Cmdbuild.utilities.proxy.getStartProcessAttributes(this.type, processActivityId,
							function(response) {
								this.instructions = response.instructions;
								this.attributes = [
								   				{
								   					type: "title",
								   					name: "title"
								   				},
								   				{
								   					type: "paragraph",
								   					name: "help"
								   				}
								   			];
								this.loadData(this.param);
							}, this);
						};
			}, this);
		};
		this.loadData = function() {
			this.data = {
					title: this.description,
					help: this.instructions
			};
			onObjectReady();
		};
		this.getAttributes = function() {
			return this.attributes;
		};
		this.getData = function() {
			return this.data;
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
	$.Cmdbuild.standard.backend.HelpNewActivity = HelpNewActivity;
}) (jQuery);
