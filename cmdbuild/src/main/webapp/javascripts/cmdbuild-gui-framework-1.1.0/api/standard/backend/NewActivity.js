(function($) {
	var NewActivity = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.className;
		this.processInstanceId = undefined;//param.cardId;
		this.data =  [];
		this.metadata = {};
		this.widgets = undefined;
		this.widgetsData = [];

		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.updateData = function(param, callBack) {
			var data = {};
			for (var i = 0; i < this.attributes.length; i++) {
				data[this.attributes[i]._id] = param.data[this.attributes[i]._id];
			}
			data["_advance"] = (param.advance == "true");
			this.widgetsData = $.Cmdbuild.widgets.getWidgetsData(param.form, this.widgets);
			$.Cmdbuild.widgets.saveOnDataWidgets(data, this.widgetsData);
			var me = this;
			$.Cmdbuild.utilities.proxy.postNewProcess(this.type, //this.processInstanceId,
					data, function(response) {
								me.processInstanceId = response;
								$.Cmdbuild.widgets.savePostponedWidgets(this.widgets, this.widgetsData, {
									type: me.type,
									id: me.processInstanceId
								}, param.form, function() {
									callBack(response);									
								}, this);
							}, this);
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
			this.classAttributes = attributes;
			$.Cmdbuild.utilities.proxy.getStartProcessActivities(this.type, this.processInstanceId,
					function(response) {
						if (response.length > 0) {
							var processActivityId = response[0]._id;
							$.Cmdbuild.utilities.proxy.getStartProcessAttributes(this.type, processActivityId,
							function(response) {
								this.loadProcessStepAttributes(response);
							}, this);
						};
			}, this);
//			$.Cmdbuild.utilities.proxy.getStepProcessAttributes(this.type, this.processInstanceId, this.processActivityId,
//					this.loadProcessStepAttributes, this);
		};
		this.loadProcessStepAttributes = function(response) {
			this.instructions = response.instructions;
			this.activityAttributes = response.attributes;
			this.attributes = [];
			for (var i = 0; i < this.classAttributes.length; i++) {
				for (var j = 0; j < this.activityAttributes.length; j++) {
					if (this.classAttributes[i]._id == this.activityAttributes[j]._id) {
						var attribute = this.activityAttributes[j];
//						var access = $.Cmdbuild.global.fieldInteractivityFromProcess(attribute.writable, attribute.mandatory);
						var classAttribute = this.classAttributes[i];
						classAttribute.mandatory = attribute.mandatory;
						classAttribute.writable = attribute.writable;
//						classAttribute.interactivity = access;
						classAttribute.index = attribute.index;
						this.attributes.push(classAttribute);
					}
				}
			}
			$.Cmdbuild.utilities.sortAttributes(this.attributes);
			this.widgets = response.widgets;
			this.loadData(this.param);
		};
		this.loadDataCallback = function(response, metadata) {
			this.data = response;
			this.metadata = metadata;
			onObjectReady();
		};
		this.loadData = function() {
			for (var i = 0; i < this.attributes.length; i++) {
				this.data[this.attributes[i]._id] = "";
			}
			if (this.param.processDescription != "") {
				this.data["Description"] = this.param.processDescription;
			}
			this.data[this.param.paramUser] = this.param.user;
			this.loadDataCallback(this.data, {});
		};
		this.getAttributes = function() {
			return this.attributes;
		};
		this.getData = function() {
			return this.data;
		};
		this.getInstructions = function() {
			return this.instructions;
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
	$.Cmdbuild.standard.backend.NewActivity = NewActivity;
}) (jQuery);
