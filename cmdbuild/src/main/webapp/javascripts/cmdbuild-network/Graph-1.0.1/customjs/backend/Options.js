(function($) {
	var Options = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.type;
		this.data = [];
		this.metadata = {};

		var layouts = [{
			_id: "Hierarchical",
			description: "Hierarchical"
		}, {
			_id: "Attractive",
			description: "Attractive"
		}];

		var projections = [{
			_id: "Projection",
			description: "Projection"
		}, {
			_id: "Orthogonal",
			description: "Orthogonal"
		}];
		var labelsVisibility = [{
			_id: "none",
			description: "No abels on entities"
		},{
			_id: "all",
			description: "Labels on all entities"
		}, {
			_id: "selected",
			description: "Labels on all selected entities"
		}];

		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.init = function() {
			this.loadData(this.param);
		};
		this.loadData = function(response, metadata) {
			switch (this.type) {
				case "projectionType" :
					this.data = projections;
					break;
				case "layoutType" :
					this.data = layouts;
					break;
//				case "baseLevel" :
//					this.data = [];
//					break;
				case "displayLabel" :
					this.data = labelsVisibility;
					break;
			}
			setTimeout(function() {
				onObjectReady();
			}, 10);

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
		 * Call init function and return object
		 */
		this.init();
		$.Cmdbuild.standard.backend.Options.labelsVisibility = function(code) {
			for (var i = 0; i < labelsVisibility.length; i++) {
				if (code == labelsVisibility[i]._id) {
					return labelsVisibility[i].description;
				}
			}
			return "";
		};
		$.Cmdbuild.standard.backend.Options.getSelectValue = function(type, code) {
			switch (type) {
				case "projectionType" :
				case "layoutType" :
				case "baseLevel" :
				case "clusteringThreshold" :
					return "";
				case "displayLabel" :
					return "";
			}
		};
	};
	$.Cmdbuild.standard.backend.Options = Options;

})(jQuery);