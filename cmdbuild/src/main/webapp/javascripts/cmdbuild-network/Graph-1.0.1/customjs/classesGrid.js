(function($) {
	var classesGrid = function(param) {
		this.param = param;
		this.grid = new $.Cmdbuild.standard.grid();
		this.refreshSelected = function() {};
		this.onLoad = function() {};
		this.refresh = function() {
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			this.prepareChecked();
			$.Cmdbuild.standard.grid.onNavigate(0, this.param.form);
		};
		this.prepareChecked = function() {
			var classes = $.Cmdbuild.customvariables.model.getDistinctClasses();
			this.grid.checked = {};
			for (var i = 0; i < classes.rows.length; i++) {
				this.grid.checked[classes.rows[i].classId] = true;
			}
			var filterClasses = $.Cmdbuild.custom.configuration.filterClasses;
			if (filterClasses) {
				for (i = 0; i < filterClasses.length; i++) {
					this.grid.checked[filterClasses[i]] = false;
				}
			}
		};
		this.getSelection = function() {
			return $.Cmdbuild.custom.classesGrid.getAllSelected();
		};
		this.init = function(param) {
			this.param = param;
			if (this.param.selection === "true") {
				this.prepareChecked();
			}
			try {
				this.param = param;
				$.Cmdbuild.dataModel.forms[this.param.form] = this.grid;
				param.parent = this;
				this.grid.init(param);
				$.Cmdbuild.customvariables.model.observe(this);
				$.Cmdbuild.customvariables.selected.observe(this);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.classesGrid.init");
				throw e;
			}
		};
	};
	$.Cmdbuild.custom.classesGrid = classesGrid;
	$.Cmdbuild.custom.classesGrid.getAllSelected =  function(classId) {
		var classes = {};
		classes[classId] = true;
		return classes;
	};
}) (jQuery);
