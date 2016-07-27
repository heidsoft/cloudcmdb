(function($) {
	var NO_OPEN_NODE = "__NoOpenNode";
	var oldRow = 0;
	var ClassesFilter = function(param, onReadyFunction, onReadyScope) {
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
			this.attributes = [
					{
						type : "string",
						name : "_id",
						description : "_id",
						displayableInList : false
					},
					{
						type : "string",
						name : "classDescription",
						description : $.Cmdbuild.translations.getTranslation(
								"COLUMNHEADER_CLASS", "Class"),
						displayableInList : true
					},
					{
						type : "string",
						name : "filterStatus",
						description : $.Cmdbuild.translations.getTranslation(
								"COLUMNHEADER_FILTER_ATTRIBUTES",
								"Attributes filter"),
						displayableInList : true
					}, {
						type : "string",
						name : "classId",
						description : "Class",
						displayableInList : false
					} ];
			setTimeout(function() {
				onObjectReady();
			}, 100);
		};
		this.isJustHere = function(rows, id) {
			for (var i = 0; i < rows.length; i++) {
				if (id === rows[i].classId) {
					return true;
				}
			}
			return false;
		};
		this.loadSuperClasses = function(rows) {
			var l = rows.length;
			for (var i = 0; i < l; i++) {
				var row = rows[i];
				var parents = $.Cmdbuild.customvariables.cacheClasses
						.getAllParents(row.classId);
				for (var j = 0; j < parents.length; j++) {
					if (!this.isJustHere(rows, parents[j])) {
						rows
								.push({
									_id : parents[j],
									id : parents[j],
									classId : parents[j],
									filterStatus : this
									.getFilterStatus(parents[j]),
									classDescription : $.Cmdbuild.customvariables.cacheClasses
											.getDescription(parents[j])
								});
					}
				}
			}
		};
		this.getFilterStatus = function(classId) {
			var fAttributes = $.Cmdbuild.custom.configuration.filterByAttributes;
			var on = $.Cmdbuild.translations.getTranslation(
					"LABEL_FILTERON", "Attributes filter");
			var off = $.Cmdbuild.translations.getTranslation(
					"LABEL_FILTEROFF", "Empty");
			return (!(fAttributes && fAttributes[classId])) ? off : on;
		};
		this.loadData = function(param, callback, callbackScope) {
			var data = this.model.getDistinctClasses(0, 10);
			for (var i = 0; i < data.rows.length; i++) {
				data.rows[i]._id = data.rows[i].classId;
				data.rows[i].filterStatus = this
						.getFilterStatus(data.rows[i].classId);
				data.rows[i].id = data.rows[i].classId;
			}
			this.total = data.total;
			var filterClasses = $.Cmdbuild.custom.configuration.filterClasses;
			if (filterClasses) {
				for (var i = 0; i < filterClasses.length; i++) {
					data.rows
							.push({
								_id : filterClasses[i],
								id : filterClasses[i],
								classId : filterClasses[i],
								filterStatus : this
										.getFilterStatus(filterClasses[i]),
								classDescription : $.Cmdbuild.customvariables.cacheClasses
										.getDescription(filterClasses[i])
							});
				}
				this.total += i;
			}
			if ($.Cmdbuild.customvariables.filterClassSuperclasses) {
				this.loadSuperClasses(data.rows);
				this.total = data.rows.length;
			}

			var fAttributes = $.Cmdbuild.custom.configuration.filterByAttributes;
			for ( var key in fAttributes) {
				if (!data.rows.find(function(el) {
					return el.classId === key;
				})) {
					data.rows
							.push({
								classDescription : $.Cmdbuild.customvariables.cacheClasses
										.getDescription(key),
								filterStatus : this.getFilterStatus(key),
								classId : key,
								qt : 1
							});
					data.total += 1;
				}
			}
			data.rows.sort(function(a, b) {
				if (param.direction === "ASC")
					return (a.classId > b.classId) ? 1 : -1;
				else
					return (a.classId < b.classId) ? 1 : -1;
			});
			var rowsFilteredByQuery = [];
			for (i = 0; i < data.rows.length; i++) {
				var desc = data.rows[i].classDescription.toLowerCase();
				if (this.filter && this.filter.query
						&& desc.indexOf(this.filter.query.toLowerCase()) === -1) {
					continue;
				}
				rowsFilteredByQuery.push(data.rows[i]);
			}

			this.data = [];
			param.nRows = parseInt(param.nRows);
			if (this.param.stayOnRow === "true" && oldRow < rowsFilteredByQuery.length) {
				param.firstRow = oldRow;
			}
			else {
				oldRow = param.firstRow;
			}
			this.param.stayOnRow = "false";
			for (var i = param.firstRow; i < param.nRows + param.firstRow
					&& i < rowsFilteredByQuery.length; i++) {
				this.data.push(rowsFilteredByQuery[i]);
			}
			callback.apply(callbackScope, this.data);
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
			onReadyFunction.apply(onReadyScope, [ backend ]);
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
	$.Cmdbuild.custom.backend.ClassesFilter = ClassesFilter;

})(jQuery);
