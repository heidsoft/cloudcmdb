(function($) {
	var DomainsFilterClasses = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.data = [];
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
		this.push = function(obj) {
			for (var i = 0; i < this.data.length; i++) {
				if (obj._id === this.data[i]._id) {
					return;
				}
			}
			this.data.push(obj);
		};
		this.loadData = function(response, metadata) {
			var domains = $.Cmdbuild.customvariables.cacheDomains.getData();
			this.data = [];
			for (var i = 0; i < domains.length; i++) {
				var domain = domains[i];
				this.push({
					_id: domain.sourceDescription,
					description: domain.sourceDescription
				});
				this.push({
					_id: domain.destinationDescription,
					description: domain.destinationDescription
				});
			}
			this.data.sort(function(a, b) {
				return (a.description > b.description) ? 1  : -1;
			});
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
	};
	$.Cmdbuild.standard.backend.DomainsFilterClasses = DomainsFilterClasses;

})(jQuery);