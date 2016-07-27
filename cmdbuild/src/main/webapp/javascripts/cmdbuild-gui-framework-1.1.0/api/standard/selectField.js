(function($) {
	var selectField = function(param) {
		this.param = param;
		this.backend = undefined;
		this.loadLookup = function() {
			var options = [];
			var selectMenu = $("#" + this.param.id);
			var value = (this.param.value == "$fromBackend") ? this.backend.getValue() : this.param.value;
			if (!this.param.hideEmptyOption){
				options.push("<option entryValue='' " + ((! this.param.value) ? "selected" : "") + "></option>");
			}
			for (var i = 0; i < this.backend.data.length; i++) {
				var entry = this.backend.data[i];
				var selected = (value && value == entry._id) ? "selected" : "";
//				var selected = "";
				var entryValue = " entryValue='" + entry._id + "' ";
				var optionValue = " value='" + entry._id + "' ";
				options.push("<option " + entryValue + selected + optionValue +">" + entry.description
						+ "</option>");
			}
			selectMenu.empty();
			selectMenu.append(options.join("")).selectmenu();
			selectMenu.selectmenu("refresh");
			selectMenu.trigger("change");
			// call init complete
			this.onInitComplete();
		};
		this.init = function() {
			try {
				var backendFn = $.Cmdbuild.utilities.getBackend(param.backend);
				this.backend = new backendFn(this.param, this.loadLookup, this);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log(e);
			}
		};
		this.onInitComplete = function() {
			if (this.param.refreshGridAfterInit) {
				$.Cmdbuild.standard.commands.refreshGrid({
					form: this.param.refreshGridAfterInit
				});
			}
		};
		
		this.init();
	};
	$.Cmdbuild.standard.selectField = selectField;
	// statics
	$.Cmdbuild.standard.selectField.setValue = function(id, value) {
		var selectMenu = $("#" + id);
		selectMenu.val("");
		selectMenu.selectmenu("refresh");
	};
	$.Cmdbuild.standard.selectField.clearValue = function(id) {
		var selectMenu = $("#" + id);
		selectMenu.val("");
		selectMenu.selectmenu("refresh");
	};
}) (jQuery);
