(function($) {
	var cardsGrid = function(param) {
		this.param = param;
		this.grid = new $.Cmdbuild.standard.grid();
		this.refreshSelected = function() {
			var form2Hook = $.Cmdbuild.dataModel.forms.cardsForm;
			form2Hook.selectRows($.Cmdbuild.customvariables.selected.getData());
		};
		this.refresh = function() {
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			$.Cmdbuild.standard.grid.onNavigate(0, this.param.form);
		};
		this.getSelection = function() {
			return $.Cmdbuild.customvariables.selected.getData();
		};
		this.init = function(param) {
			this.param = param;
			try {
				this.param = param;
				$.Cmdbuild.dataModel.forms[this.param.form] = this.grid;
				param.parent = this;
				this.grid.init(param);
				$.Cmdbuild.customvariables.model.observe(this);
				$.Cmdbuild.customvariables.selected.observe(this);
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
	$.Cmdbuild.custom.cardsGrid = cardsGrid;
}) (jQuery);
