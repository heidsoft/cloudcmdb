(function($) {
	var selectedGrid = function(param) {
		this.param = param;
		this.grid = new $.Cmdbuild.standard.grid();
		this.refreshSelected = function() {
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			$.Cmdbuild.standard.grid.onNavigate(0, this.param.form);
//			$.Cmdbuild.standard.form.onNavigate(0, "formcardoverview");
		};
		this.init = function(param) {
			this.param = param;
			try {
				this.param = param;
				$.Cmdbuild.dataModel.forms[this.param.form] = this.grid;
				this.grid.init(param);
				this.grid.backend.model.observe(this);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.cardsGrid.init");
				throw e;
			}
		};
		this.show = function() {
			this.grid.show();
		};
	};
	$.Cmdbuild.custom.selectedGrid = selectedGrid;
}) (jQuery);
