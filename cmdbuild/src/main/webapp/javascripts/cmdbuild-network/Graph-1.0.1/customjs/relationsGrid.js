(function($) {
	var oldId = -1;
	var relationsGrid = function(param) {
		this.param = param;
		this.grid = new $.Cmdbuild.standard.grid();
		this.init = function(param) {
			if (this.loading === true) {
				this.buffer = param;
				return;
			}
			this.buffer = null;
			if ($.Cmdbuild.customvariables.commandInExecution === true) {
				return;
			}
			var cardId = $.Cmdbuild.dataModel.getValue("selectedForm", "id");
			if (oldId == cardId) {
				return;
			}
			oldId = cardId;
			param.parent = this;
			this.param = param;
			try {
				$.Cmdbuild.dataModel.forms[this.param.form] = this.grid;
				this.loading = true;
				this.grid.init(param);
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.cardsGrid.init");
				throw e;
			}
			this.gridDrawCallback = this.grid.drawCallback;
			this.grid.drawCallback = this.drawCallback;
		};
		this.show = function() {
			this.grid.show();
		};
		this.onLoad = function() {
			this.loading = false;
			if (this.buffer !== null) {
				this.init(this.buffer);
			}
		};
		this.drawCallback = function(table, grid, param, settings) {
			// call parent function
			if (this.gridDrawCallback) {
				this.gridDrawCallback(table, grid, param, settings);
			}
			// add class to hide attributes column if row has not attributes
			var api = table.api();
			$.each(grid.paginationSettings.backend.getData(), function(index, item) {
				if (!item.attributes || $.isEmptyObject(item.attributes)) {
					api.row(index).node().className += " hideAttributes";
				}
			});
		};
	};
	$.Cmdbuild.custom.relationsGrid = relationsGrid;
}) (jQuery);
