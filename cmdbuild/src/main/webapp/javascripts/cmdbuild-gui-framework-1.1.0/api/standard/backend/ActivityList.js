(function($) {
	var ActivityList = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.type = param.className;
		this.data =  [];
		this.metadata = {};
		this.listData = []; // there is a difference from the cards that comes from list and getCard :(
		this.typeFilter = "attribute";
		this.conditions = 	$.Cmdbuild.utilities.getConditionsFromParam(param);

		this.positionOf = param.positionOf;
		this.positions = {};

		this.defaultSort = param.sort;
		this.defaultDirection = param.direction;

		if (! param.noUser) {
			this.conditions.push({
				field: param.paramUser,
				value: param.user
			});
		}
		this.cqlfilter = param.cqlFilter;
		this.cqlfilterFn = param.cqlFilterFn;

		this.filter = {};
		this.setFilter = function(param) {
			var oldCqlFilter = this.filter["CQL"];
			var attributes = this.conditions.slice();
			if (param.flowStatus) {
				if (param.flowStatus !== undefined) {
					attributes.push({
						field: "_status",
						value: param.flowStatus
					});
				}
			}
			var filterAttributes = $.Cmdbuild.utilities.getAttributesFilterFromConditions(attributes);
			this.filter["attribute"] = filterAttributes;

			// CQL Filter
			if (this.cqlfilter) {
				this.filter["CQL"] = this.cqlfilter;
				this.noValidFilter = false;
			} else if (this.cqlfilterFn) {
				this.noValidFilter = false;
				this.filter["CQL"] = this.cqlfilterFn.apply();
			} else {
				this.filter["CQL"] = oldCqlFilter;				
			}
		};

		this.updateFilter = function() {
			if (this.cqlfilterFn) {
				this.filter["CQL"] = this.cqlfilterFn.apply();
			}
		};

		this.getStatus = function() {
			return this.flowStatus;
		};
				
		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.deleteRow = function(param, callback) {
			var data = $.Cmdbuild.dataModel.getValues(param.form);
			$.Cmdbuild.utilities.proxy.abortProcess(data._type, data._id, {}, function(response) {
					callback(response);
				}
			, this);
		};
		this.init = function() {
			$.Cmdbuild.utilities.proxy.getProcessStatuses(function(response) {
				this.flowStatuses = response;
				$.Cmdbuild.utilities.proxy.getProcess(this.type, function(process) {
					if (param.flowStatus) {
						this.setFilter({
							flowStatus: process.defaultStatus
						});
					}
					else {
						this.setFilter({});
					}
					this.loadAttributes();
				}, this);
			}, this);
		};
		this.loadAttributes = function() {
			if (!this.type) {
				var msg = "No _type specified for form: " + param.form;
				$.Cmdbuild.errorsManager.warn(msg);
				return;
			}

			$.Cmdbuild.utilities.proxy.getProcessAttributes(this.type,
					this.loadAttributesCallback, this);
		};
		// load Attributes and its callback
		this.loadAttributesCallback = function(attributes) {
			this.attributes = attributes;

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
					filter : this.filter,
					active: true
			};

			var expandDataCallabck = function() {
				this.expandData(callback, callbackScope);
			};

			this.makeAjaxRequest(config, expandDataCallabck, this);
		};
		/* private make ajax request */
		this.makeAjaxRequest = function(config, callback, callbackScope) {
			$.Cmdbuild.utilities.proxy.getActivityList(this.type, config,
				function(response, metadata) {
					this.data = [];
					this.listData = response;
					this.metadata = metadata;
					callback.apply(callbackScope, this.data);
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
					card["writable"] = false;
				this.data.push(card);
				}
				else {
					for (var i = 0; i < response.length; i++) {
						var cardActivity = $.Cmdbuild.utilities.clone(card);
						cardActivity["ActivityInstanceId"] = response[i]._id;
						cardActivity["activityTitle"] = response[i].description;
						cardActivity["writable"] = response[i].writable;
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
		this.getStatus = function() {
			return this.flowStatus;
		};
		this.getTotalRows = function() {
			var metadata = this.getMetadata();
			return metadata && metadata.total ? metadata.total : this.getData().length;
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.standard.backend.ActivityList = ActivityList;
}) (jQuery);