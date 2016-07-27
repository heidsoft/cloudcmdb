(function($) {
	var CardsNavigation = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Private attributes
		 */
		this.data =  [];
		this.metadata = {};
		this.param = param;
		this.filter = {};
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.init = function(onlyRefresh) {
			this.onlyRefresh = onlyRefresh;
			this.model = $.Cmdbuild.customvariables.model; 
			if (onlyRefresh) {
				setTimeout(function() { onObjectReady(); }, 100);
			}
			else {
				this.loadAttributes();
			}
		};
		this.loadAttributes = function() {
			this.attributes = [
   				{
   					type: "string",
   					name: "label",
   					description: $.Cmdbuild.translations.getTranslation("COLUMNHEADER_CARD", "Card"),
   					displayableInList: true
   				},
   				{
   					type: "string",
   					name: "classDescription",
   					description: $.Cmdbuild.translations.getTranslation("COLUMNHEADER_CLASS", "Class"),
   					displayableInList: true
   				},
   				{
   					type: "string",
   					name: "classId",
   					description: "Class",
   					displayableInList: false
   				}
   			];
			setTimeout(function() { onObjectReady(); }, 100);
		};
		this.loadData = function(param, callback, callbackScope) {
			var data = $.Cmdbuild.customvariables.model.getCards(this.filter);
			this.total = data.total;
			if (param.sort) {
				var sortingColumn = param.sort;
				data.rows.sort(function(a, b) {
					if (param.direction === "ASC")
						return (a[sortingColumn] > b[sortingColumn]) ? 1  : -1;
					else
						return (a[sortingColumn] < b[sortingColumn]) ? 1  : -1;
				});
			}
			this.data = data.rows.splice(param.firstRow, param.nRows);
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
			onReadyFunction.apply(onReadyScope);
		};

		/**
		 * Custom functions
		 */
		this.getTotalRows = function() {
			return this.total;
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.custom.backend.CardsNavigation = CardsNavigation;
	
})(jQuery);
