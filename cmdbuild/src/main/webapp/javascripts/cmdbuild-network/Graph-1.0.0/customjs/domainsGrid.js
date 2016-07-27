(function($) {
	var domainsGrid = function(param) {
		this.param = param;
		this.grid = new $.Cmdbuild.standard.grid();
		this.prepareChecked = function() {
			var domains = $.Cmdbuild.customvariables.cacheDomains.getData();
			this.grid.checked = {};
			for (var i = 0; i < domains.length; i++) {
				this.grid.checked[domains[i]._id] = domains[i].active;
			}
		};
		this.refresh = function() {
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			this.prepareChecked();
			$.Cmdbuild.standard.grid.onNavigate(0, this.param.form);
		};
		this.init = function(param) {
			if (this.loading === true) {
				this.buffer = param;
				return;
			}
			this.buffer = null;
			this.param = param;
			if (this.param.selection === "true") {
				this.prepareChecked();
			}
			try {
				this.param = param;
				$.Cmdbuild.dataModel.forms[this.param.form] = this.grid;
				this.grid.init(param);
				$.Cmdbuild.customvariables.model.observe(this);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.custom.domainsGrid.init");
				throw e;
			}
		};
	};
	$.Cmdbuild.custom.domainsGrid = domainsGrid;
}) (jQuery);
