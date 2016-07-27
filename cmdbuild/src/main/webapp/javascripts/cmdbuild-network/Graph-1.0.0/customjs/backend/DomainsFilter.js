(function($) {
	var DomainsFilter = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Private attributes
		 */
		this.data = [];
		this.metadata = {};
		this.filter = {};
		this.param = param;
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;
		var backend = this;

		/**
		 * Base functions
		 */
		this.init = function() {
			this.model = $.Cmdbuild.customvariables.model;
			var data = this.model.getDistinctClasses(0, 10);
			this.total = data.total;
			this.data = [];
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			this.attributes = [{
				type: "string",
				name: "_id",
				description: "_id",
				displayableInList: false
			}, {
				type: "string",
				name: "sourceDescription",
				description: $.Cmdbuild.translations.getTranslation("COLUMNHEADER_CLASS", "Class"),
				displayableInList: true
			}, {
				type: "string",
				name: "sourceId",
				description: "sourceId",
				displayableInList: false
			}, {
				type: "string",
				name: "relationDescription",
				description: $.Cmdbuild.translations.getTranslation("COLUMNHEADER_RELATION", "Relation"),
				displayableInList: true
			}, {
				type: "string",
				name: "destinationDescription",
				description: $.Cmdbuild.translations.getTranslation("COLUMNHEADER_CLASS", "Class"),
				displayableInList: true
			}, {
				type: "string",
				name: "destinationId",
				description: "destinationId",
				displayableInList: false
			}];
			setTimeout(function() {
				onObjectReady();
			}, 100);
		};
		this.selectByClass = function(key, data) {
			var ret = [];
			for (var i = 0; i < data.length; i++) {
				if (data[i].sourceDescription === key || data[i].destinationDescription === key) {
					ret.push(data[i]);
				}
			}
			return ret;
		};
		this.loadData = function(param, callback, callbackScope) {
			this.data = [];
			param.nRows = parseInt(param.nRows);
			var data = $.Cmdbuild.customvariables.cacheDomains.getData();
			if (this.param.classDescription !== "-1") {
				data = this.selectByClass(this.param.classDescription, data);
			}
			var sortRow = (param.sort) ? param.sort : "_id";
			data.sort(function(a, b) {
				if (param.direction.toLowerCase() === "asc")
					return (a[sortRow] + a._id > b[sortRow] + b._id) ? 1 : -1;
				else
					return (a[sortRow] + a._id >= b[sortRow] + b._id) ? -1 : 1;
			});
			var rowsFilteredByQuery = [];
			for (i = 0; i < data.length; i++) {
				var sourceDescription = data[i].sourceDescription.toLowerCase();
				var destinationDescription = data[i].destinationDescription.toLowerCase();
				if (this.filter && this.filter.query) {
					var query = this.filter.query.toLowerCase();
					if (sourceDescription.indexOf(query) === -1 &&
							destinationDescription.indexOf(query) === -1) {
						continue;
					}
				}
				rowsFilteredByQuery.push(data[i]);
			}
			this.data = [];
			for (var i = param.firstRow; i < param.nRows + param.firstRow
					&& i < rowsFilteredByQuery.length; i++) {
				this.data.push(rowsFilteredByQuery[i]);
			}
			updateRelDescription(this.data);
			this.total = rowsFilteredByQuery.length;
			setTimeout(function() {
				callback.apply(callbackScope, [this.data]);
			}, 100);
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
			onReadyFunction.apply(onReadyScope, [backend]);
		};

		/**
		 * Custom functions
		 */
		this.getTotalRows = function() {
			return this.total;
		};
		this.getNodesByClassName = function(classId) {
			return this.model.getNodesByClassName(classId);
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.custom.backend.DomainsFilter = DomainsFilter;

	function updateRelDescription (items) {
		$.each(items, function(index, item) {
			var desc = "";
			if (item.descriptionDirect) {
				desc = "<span class=\"desc_direct\"><span class=\"icon\"></span>" + item.descriptionDirect + "</span>";
			}
			if (item.descriptionInverse) {
				desc += "<span class=\"desc_inverse\"><span class=\"icon\"></span>" + item.descriptionInverse + "</span>";
			}
			item.relationDescription = desc;
		});
	}

})(jQuery);
