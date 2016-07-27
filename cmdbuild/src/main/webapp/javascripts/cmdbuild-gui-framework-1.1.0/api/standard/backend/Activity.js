(function($) {
	var Activity = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.className;
		this.processInstanceId = param.cardId;
		this.processActivityId = param.activityInstanceId;
		this.displayFirstAvailableActivity = param.displayFirstAvailableActivity;
		this.hideActivityTitle = param.hideActivityTitle;
		this.data =  [];
		this.metadata = {};
		this.widgets = undefined;
		this.widgetsData = [];
		this.originalAttributes = [];
		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.deleteMail = function(param) {
			for (var i = 0; i < this.data.mails.length; i++) {
				var mail = this.data.mails[i];
				if (mail._id === param.cardId) {
					this.data.mails.splice(i, 1);
				}
			}
		};
		this.updateData = function(param, callback) {
			var data = {};
			for (var i = 0; i < this.attributes.length; i++) {
				data[this.attributes[i]._id] = param.data[this.attributes[i]._id];
			}
			data["_advance"] = (param.advance == "true");
			if (typeof this.processActivityId === 'object') {
				this.processActivityId = this.processActivityId[0];
			}
			data["_activity"] = this.processActivityId;
			this.widgetsData = $.Cmdbuild.widgets.getWidgetsData(param.form, this.widgets);
			$.Cmdbuild.widgets.saveOnDataWidgets(data, this.widgetsData);
			$.Cmdbuild.utilities.proxy.putStepProcess(this.type, this.processInstanceId,
					data, function(response) {
				$.Cmdbuild.utilities.proxy.getCardProcess(this.type, this.processInstanceId, {}, function(processData) {
					$.Cmdbuild.widgets.savePostponedWidgets(this.widgets, this.widgetsData, {
						type: this.type,
						id: this.processInstanceId
					}, param.form, function() {
						callback(processData);
					}, this);
				}, this);
				// TODO:
				// ATTENTION: (if in the first step) the process's id is undefined 
				// and the response have to be the new id
			}, this);
		};
		this.init = function() {
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			if (!this.type) {
				var msg = "No _type specified for form: " + param.form;
				$.Cmdbuild.errorsManager.warn(msg);
				this.attributes = [];
				return;
			}

			$.Cmdbuild.utilities.proxy.getProcessAttributes(this.type,
					this.loadAttributesCallback, this);
		};
		// load Attributes and its callback
		this.loadAttributesCallback = function(attributes) {
			this.originalAttributes = attributes.slice();
			this.processWithActivity = true;
			if (this.processActivityId) {
				this.classAttributes = attributes;
				$.Cmdbuild.utilities.proxy.getStepProcessAttributes(this.type,
						this.processInstanceId, this.processActivityId,
						this.loadProcessStepAttributes, this);
			
			} else if (this.displayFirstAvailableActivity) {
				// show process instance with attributes of first available
				// activity
				this.classAttributes = attributes;
				$.Cmdbuild.utilities.proxy.getActivity(this.type,
						this.processInstanceId, this.onActivitiesLoaded, this);
			} else {
				this.processWithActivity = false;
				this.attributes = attributes;
				this.loadData();
			}
		};
		this.onActivitiesLoaded = function(activities) {
			if (activities.length) {
				this.processActivityId = activities[0]._id;
				$.Cmdbuild.utilities.proxy.getStepProcessAttributes(this.type,
						this.processInstanceId, this.processActivityId,
						this.loadProcessStepAttributes, this);
			} else {
				this.attributes = this.classAttributes;
				this.loadData();
			}
		};
		this.loadProcessStepAttributes = function(response) {
			this.activityTitle = response.description;
			this.activityAttributes = response.attributes;
			this.originalAttributes.concat(response.attributes.slice());
			this.attributes = [];
			if (!this.hideActivityTitle) {
				this.attributes.push({
					type: "title",
					displayableInList: false,
					active: true,
					name: "activityTitle",
					index: -1
				});
			}
			for (var i = 0; i < this.classAttributes.length; i++) {
				for (var j = 0; j <  + this.activityAttributes.length; j++) {
					if (this.classAttributes[i]._id == this.activityAttributes[j]._id) {
						var attribute = this.activityAttributes[j];
//						var access = $.Cmdbuild.global.fieldInteractivityFromProcess(attribute.writable, attribute.mandatory);
//						this.classAttributes[i].interactivity = access;
						this.classAttributes[i].mandatory = attribute.mandatory;
						this.classAttributes[i].writable = attribute.writable;
						this.classAttributes[i].index = attribute.index;
						this.attributes.push(this.classAttributes[i]);
					}
				}
			}
			$.Cmdbuild.utilities.changeAttributeType(this.attributes, "Notes", "text");
			this.widgets = response.widgets;
			this.loadData();
		};
		this.loadDataCallback = function(response, metadata) {
			this.data = response;
			this.data["activityTitle"] = this.activityTitle;
			this.data["advanceEnabled"] = this.processWithActivity;
			this.data["readOnly"] = ! this.processWithActivity;
			this.metadata = metadata;
			onObjectReady();
		};
		this.loadData = function() {
			$.Cmdbuild.utilities.proxy.getCardProcess(this.type, this.param.cardId, {}, this.loadDataCallback, this);
		};
		this.getAttributes = function() {
			function compare(a,b) {
				if (a.index < b.index)
					return -1;
				if (a.index > b.index)
					return 1;
				return 0;
			}

			this.attributes.sort(compare);
			return this.attributes;
		};
		this.getData = function() {
			return this.data;
		};
		this.getMetadata = function() {
			return this.metadata;
		};

		this.getOriginalAttributes = function() {
			return this.originalAttributes;
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
	$.Cmdbuild.standard.backend.Activity = Activity;
}) (jQuery);
