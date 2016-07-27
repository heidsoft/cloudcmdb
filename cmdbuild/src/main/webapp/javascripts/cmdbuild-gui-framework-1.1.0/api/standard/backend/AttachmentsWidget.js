(function($) {
	var attributes = [{
		type: "string",
		name: "document",
		description: "Documento"
	},
	{
		type: "string",
		name: "description",
		description: "Descrizione"
	},
	{
		type: "string",
		name: "category",
		description: "Category"
	}];
	var AttachmentsWidget = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.type = param.className;
		this.data =  [];
		this.attributes = [];
		onReadyScope.backend = this;

		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.init = function() {
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			for (var i = 0; i < attributes.length; i++) {
				this.attributes.push({
					type: attributes[i].type,
					displayableInList: true,
					active: true,
					name: attributes[i].name,
					description: attributes[i].description
				});
			}
			onObjectReady();
		};
		this.loadData = function(param, callback, callbackScope) {
			var data = $.Cmdbuild.dataModel.getAttachmentsForWidget(param.form).slice();
			if (data) {
				this.data = data;
			}
			else {
				this.data = [];
			}
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
			return this.getData().length;
		};

		this.deleteRow = function(param) {
			var index = $.Cmdbuild.dataModel.getCurrentIndex(param.form);
			this.data.splice(index, 1);
			$.Cmdbuild.dataModel.setAttachmentsForWidget(param.form, this.data);
			$.Cmdbuild.standard.grid.onNavigate("begin", param.form);
		};
		this.addRow = function(param) {
			this.data.splice(0, 0, param.row);
			$.Cmdbuild.dataModel.setAttachmentsForWidget(param.form, this.data);
			$.Cmdbuild.standard.grid.onNavigate("begin", param.form);
		};
		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.standard.backend.AttachmentsWidget = AttachmentsWidget;
}) (jQuery);