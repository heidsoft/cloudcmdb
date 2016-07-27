(function($) {
	// var INCLUDED_FILE = "NetworkConfigurationFile";
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var Options = function() {
		this.observers = [];
		this.init = function(name) {
			return this[name];
		};

		if (!$.Cmdbuild.customvariables.options) {
			$.Cmdbuild.customvariables.options = {};
		}
		for ( var key in $.Cmdbuild.custom.configuration) {
			this[key] = $.Cmdbuild.custom.configuration[key];
		}
		this.data = function(name) {
			return this[name];
		};
		this.observe = function(observer) {
			this.observers.push(observer);
		};
		this.changed = function(params) {
		};
		this.init();
	};
	$.Cmdbuild.g3d.Options = Options;
	$.Cmdbuild.g3d.Options.initVariables = function() {// from saved to session
		$.Cmdbuild.customvariables.options.nodeTooltipEnabled = $.Cmdbuild.custom.configuration.nodeTooltipEnabled;
		$.Cmdbuild.customvariables.options.edgeTooltipEnabled = $.Cmdbuild.custom.configuration.edgeTooltipEnabled;
		$.Cmdbuild.customvariables.options.displayLabel = $.Cmdbuild.custom.configuration.displayLabel;
		$.Cmdbuild.customvariables.options.clusteringThreshold = $.Cmdbuild.custom.configuration.clusteringThreshold;
		$.Cmdbuild.customvariables.options.spriteDimension = $.Cmdbuild.custom.configuration.spriteDimension;
		$.Cmdbuild.customvariables.options.stepRadius = $.Cmdbuild.custom.configuration.stepRadius;
		$.Cmdbuild.customvariables.options.baseLevel = $.Cmdbuild.custom.configuration.baseLevel;
		$.Cmdbuild.customvariables.options.filterEnabled = true;
	};
	$.Cmdbuild.g3d.Options.initFields = function() {
		$("#baseLevel").spinner("value",
				$.Cmdbuild.customvariables.options.baseLevel);
		$("#" + $.Cmdbuild.customvariables.options.displayLabel).attr(
				"checked", "checked").button('refresh');
	};
	$.Cmdbuild.g3d.Options.loadConfiguration = function(callback, callbackScope) {
		$.Cmdbuild.g3d.proxy.getGraphConfiguration(
				function(graphConfiguration) {
					callback.apply(callbackScope, [ graphConfiguration ]);
				}, this);
	};
	$.Cmdbuild.g3d.Options.getFileFromServer = function(url, doneCallback) {
		$.ajax({
			url : url,
			cache : false,
			success : function(data) {
				doneCallback(data);
			}
		});
	};
})(jQuery);