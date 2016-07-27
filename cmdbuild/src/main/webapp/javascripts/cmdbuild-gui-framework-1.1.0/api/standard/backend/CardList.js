(function($) {
	var CardList = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.type = param.className;
		this.data =  [];
		this.metadata = {};
		this.filter = {};

		this.positionOf = param.positionOf;
		this.positions = {};

		this.defaultSort = param.sort;
		this.defaultDirection = param.direction;

		this.conditions = 	$.Cmdbuild.utilities.getConditionsFromParam(param);
		this.filter = {};
		this.cqlFilter = param.cqlFilter;
		this.cqlFilterFn = param.cqlFilterFn;

		this.updateFilter = function() {
			var queryFilter = this.filter["query"];
			var oldCqlFilter = this.filter["CQL"];
			this.filter = {};
			var filterAttributes = $.Cmdbuild.utilities.getAttributesFilterFromConditions(this.conditions);
			if (filterAttributes) {
				this.filter["attribute"] = filterAttributes;
			}
			if (queryFilter) {
				this.filter["query"] = queryFilter;
			}
			// CQL Filter
			if (this.cqlFilter) {
				this.filter["CQL"] = this.cqlFilter;
				this.noValidFilter = false;
			} else if (this.cqlFilterFn) {
				this.filter["CQL"] = this.cqlFilterFn.apply();
				this.noValidFilter = false;
			}
			else {
				this.filter["CQL"] = oldCqlFilter;				
			}
		};
		if (param.widgetId) {
			//ATTENZIONE: va inserita una callback
			//var str = cqlEvaluate($.Cmdbuild.dataModel.getWidgetWindow(param.widgetId));
			//this.filter["CQL"] = str;
		}

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
		this.deleteRow = function(param, callback, callbackScope) {
			var data = $.Cmdbuild.dataModel.getValues(param.form);
			$.Cmdbuild.utilities.proxy.deleteCard(data._type, data._id, function() {
				callback.apply(callbackScope, []);
			}, this);
		};
		this.loadAttributes = function() {
			if (!this.type) {
				var msg = "No _type specified for form: " + param.form;
				$.Cmdbuild.errorsManager.warn(msg);
				this.attributes = [];
				return;
			}

			$.Cmdbuild.utilities.proxy.getClassAttributes(this.type,
					this.loadAttributesCallback, this);
		};
		// load Attributes and its callback
		this.loadAttributesCallback = function(attributes) {
			this.attributes = attributes;
			this.attributes.sort($.Cmdbuild.utilities.attributesSorter);

			if (this.positionOf) {
				this.loadPositions();
			} else {
				onObjectReady();
			}
		};
		this.loadPositions = function() {
			var me = this;
			this.updateFilter();
			var config = {
					sort : this.defaultSort,
					direction : this.defaultDirection || "ASC",
					positionOf : this.positionOf,
					filter : this.filter
			};
			this.makeAjaxRequest(config, function() {
				me.positions = me.metadata.positions;

				// clear data and metadata
				me.data = [];
				me.metadata = {};

				// call callback
				onObjectReady();
			}, this);
		};
		this.loadData = function(param, callback, callbackScope) {
			if (this.noValidFilter) {
				this.data = [];
				callback.apply(callbackScope, this.data);
				return;
			}
			this.updateFilter();
			var config = {
					page : param.currentPage,
					start : param.firstRow,
					limit : param.nRows,
					sort : param.sort,
					direction : param.direction,
					filter : this.filter
			};
			this.makeAjaxRequest(config, callback, callbackScope);
		};
		this.makeAjaxRequest = function(requestParams, callback, callbackScope) {
			$.Cmdbuild.utilities.proxy.getCardList(this.type, requestParams,
					function(response, metadata) {
				this.data = response;
				this.metadata = metadata;
				callback.apply(callbackScope, this.data);
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
	$.Cmdbuild.standard.backend.CardList = CardList;
}) (jQuery);
