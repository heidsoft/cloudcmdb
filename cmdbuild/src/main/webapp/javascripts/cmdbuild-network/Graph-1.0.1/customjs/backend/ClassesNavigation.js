(function($) {
	var NO_OPEN_NODE = "__NoOpenNode";
	var ClassesNavigation = function(param, onReadyFunction, onReadyScope) {
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
			this.data = data.rows;
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			this.attributes = [{
				type: "string",
				name: "classId",
				description: "classId",
				displayableInList: false
			}, {
				type: "string",
				name: "classDescription",
				description: $.Cmdbuild.translations.getTranslation("COLUMNHEADER_CLASS", "Class"),
				displayableInList: true
			}, {
				type: "integer",
				name: "qt",
				description: $.Cmdbuild.translations.getTranslation("COLUMNHEADER_QUANTITY", "Qt"),
				displayableInList: true
			}];
			setTimeout(function() {
				onObjectReady();
			}, 100);
		};
		this.loadSuperClasses = function(rows) {
			var originalRows = rows.slice();
			var l = rows.length;
			for (var i = 0; i < originalRows.length; i++) {
				var row = originalRows[i];
				var parents = $.Cmdbuild.customvariables.cacheClasses
						.getAllParents(row.classId);
				for (var j = 0; j < parents.length; j++) {
					if (!this.isJustHere(rows, parents[j])) {
						rows
								.push({
									_id : parents[j],
									id : parents[j],
									classId : parents[j],
									classDescription : $.Cmdbuild.customvariables.cacheClasses
											.getDescription(parents[j]),
									qt : this.getSuperClassQuantity(parents[j], originalRows)
								});
					}
					
				}
			}
		};
		this.inSuperClasses = function(classId, superClasses) {
			for (var i = 0; i < superClasses.length; i++) {
				if (superClasses[i] === classId) {
					return true;
				}
			}
			return false;
		};
		this.getSuperClassQuantity = function(classId, classesOnGraph) {
			var qt = 0;
			for (var i = 0; i < classesOnGraph.length; i++) {
				if (this.inSuperClasses(classId, classesOnGraph[i].superClasses)) {
					qt += classesOnGraph[i].qt;
				}
			}
			return qt;
		};
		this.loadData = function(param, callback, callbackScope) {
			var data = this.model.getDistinctClasses(0, 10);
			for (var i = 0; i < data.rows.length; i++) {
				data.rows[i]._id = data.rows[i].classId;
				data.rows[i].id = data.rows[i].classId;
			}
			data = this.filterData(this.filter, data.rows);
			if (this.param.superClasses === "true") {
				this.loadSuperClasses(data);
			}
			param.nRows = parseInt(param.nRows);
			if (param.sort) {
				var sortingColumn = param.sort;
				data.sort(function(a, b) {
					if (param.direction === "ASC")
						return (a[sortingColumn] > b[sortingColumn]) ? 1  : -1;
					else
						return (a[sortingColumn] < b[sortingColumn]) ? 1  : -1;
				});
			}
			this.total = data.length;
			this.data = [];
			for (var i = param.firstRow; i < param.nRows + param.firstRow && i < data.length; i++) {
				this.data.push(data[i]);
			}
			callback.apply(callbackScope, this.data);
		};
		this.isJustHere = function(rows, id) {
			for (var i = 0; i < rows.length; i++) {
				if (id === rows[i].classId) {
					return true;
				}
			}
			return false;
		};
		this.filterData = function(filter, data) {
			if (! (filter && filter.query)) {
				return data;
			}
			var retData = [];
			for (var i = 0; i < data.length; i++) {
				if (filter.query) {
					filter.query = filter.query.toLowerCase();
					var label = data[i].classDescription;
					if (label.toLowerCase().indexOf(filter.query) < 0) {
						continue;
					}
				}
				retData.push(data[i]);
			}
			return retData;
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
	$.Cmdbuild.custom.backend.ClassesNavigation = ClassesNavigation;

})(jQuery);
