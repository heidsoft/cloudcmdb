(function($) {
	var DomainList = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.type = param.destinationClass;
		this.data =  [];
		this.listData = []; // there is a difference from the cards that comes from list and get card :(
		this.metadata = {};
		this.param = param;

		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;
		this.conditions = 	$.Cmdbuild.utilities.getConditionsFromParam(param);
		var filterAttributes = $.Cmdbuild.utilities.getAttributesFilterFromConditions(this.conditions);
		this.filter = {};
		if (filterAttributes) {
			this.filter["attribute"] = filterAttributes;
		}
		param.sourceId = '' + param.sourceId;
		var sources = param.sourceId.split(",");
		var cards = [];
		for (var i = 0; i < sources.length; i++) {
			cards.push({
				className: param.sourceClass,
				id: $.trim(sources[i])
			});
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
					cards: cards
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
					filter : this.filter
			};
			this.data = [];
			$.Cmdbuild.utilities.proxy.getCardList(this.type, config,
					function(response, metadata) {
						this.data = response;
						this.metadata = metadata;
						callback.apply(callbackScope, this.data);
//						this.expandData(callback, callbackScope);
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
	$.Cmdbuild.standard.backend.DomainList = DomainList;
}) (jQuery);
