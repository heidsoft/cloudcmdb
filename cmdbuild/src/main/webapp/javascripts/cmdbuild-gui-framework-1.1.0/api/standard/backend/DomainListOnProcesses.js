(function($) {
	var DomainListOnProcesses = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.type = param.destinationClass;
		this.data =  [];
		this.listData = []; // there is a difference from the cards that comes from list and get card :(
		this.metadata = {};

		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;
		this.conditions = [];
		if (param.condition) {
			var params = param.condition.split(",");
			for (var i = 0; i < params.length; i++) {
				var fields = params[i].split("=");
				var field = {
					field: fields[0],
					value: fields[1]
				};
				this.conditions.push(field);
			}
		}
		this.conditions = 	$.Cmdbuild.utilities.getConditionsFromParam(param);
		var filterAttributes = $.Cmdbuild.utilities.getAttributesFilterFromConditions(this.conditions);
		this.filter = {};
		if (filterAttributes) {
			this.filter["attribute"] = filterAttributes;
		}
		this.filter["relation"] = 
				[{
					domain: param.domainName,
					// searching the destination class so i have to invert the direction of the domain
					// and the order of classes
					source: param.destinationClass,
					destination: param.sourceClass,
					type: "oneof",
					direction: (param.domainDirection == "_2") ? "_1" : "_2",
					cards: [{
							className: param.sourceClass,
							id: param.sourceId
					}]
				}];
		

		/**
		 * Base functions
		 */
		this.init = function() {
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			$.Cmdbuild.utilities.proxy.getClassAttributes(this.type,
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
					filterType : this.typeFilter
			};
			this.data = [];
			$.Cmdbuild.utilities.proxy.getActivityList(this.type, config,
					function(response, metadata) {
						this.listData = response;
						this.metadata = metadata;
						this.expandData(callback, callbackScope);
					}, this);
		};
		this.expandData = function(callback, callbackScope) {
			if (this.listData.length == 0) {
				callback.apply(callbackScope, this.data);
				return;
			}
			var card = this.listData[0];
			this.listData.splice(0, 1);
			$.Cmdbuild.utilities.proxy.getActivity(card._type, card._id, function(response) {
				if(response.length == 0) {
					this.data.push(card);
				}
				else {
					for (var i = 0; i < response.length; i++) {
						var cardActivity = $.Cmdbuild.utilities.clone(card);
						cardActivity["ActivityInstanceId"] = response[i]._id;
						cardActivity["activityTitle"] = response[i].description;
						this.data.push(cardActivity);
					}
				}
				this.expandData(callback, callbackScope);
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
	$.Cmdbuild.standard.backend.DomainListOnProcesses = DomainListOnProcesses;
}) (jQuery);
