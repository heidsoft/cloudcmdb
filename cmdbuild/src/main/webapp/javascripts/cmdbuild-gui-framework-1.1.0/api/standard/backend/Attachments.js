(function($) {
	
	var attributes = [
	{
		type: "string",
		name: "_description",
		description: "Descrizione",
		displayableInList: true,
		active: true
	},
	{
		type: "string",
		name: "_name",
		description: "Nome file",
		displayableInList: true,
		active: true
	},
	{
		type: "string",
		name: "_author",
		description: "Autore",
		displayableInList: true,
		active: true
	},
	{
		type: "string",
		name: "_category",
		description: "Categoria",
		displayableInList: true,
		active: true
	},
	{
		type: "string",
		name: "_version",
		description: "Versione",
		displayableInList: true,
		active: true
	}
	];
	
	var Attachments = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.className;
		this.procId = param.cardId; 
		this.data =  [];
		this.metadata = {};
		this.attributes = [];
		
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
			this.attributes = attributes.slice();
			setTimeout(function(){
				onObjectReady();
			}, 500);
		};
		this.loadData = function(param, callback, callbackScope) {
			var config = {
					page : param.currentPage,
					start : param.firstRow,
					limit : param.nRows,
					sort : param.sort,
					direction : param.direction,
					filter : this.filter
			};
			var callback_fn = function(response, metadata) {
				this.data = response;
				this.metadata = metadata;
				callback.apply(callbackScope, this.data);
			};
			if ($.Cmdbuild.dataModel.isAClass(this.type)) {
				$.Cmdbuild.utilities.proxy.getCardAttachments(this.type,
						this.procId, config, callback_fn, this);
			} else if ($.Cmdbuild.dataModel.isAProcess(this.type)) {
				$.Cmdbuild.utilities.proxy.getInstanceAttachment(this.type,
						this.procId, config, callback_fn, this);
			}
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
			var metadata = this.getMetadata();
			return metadata && metadata.total ? metadata.total : this.getData().length;
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.standard.backend.Attachments = Attachments;

	/**
	 * @deprecated
	 */
	$.Cmdbuild.standard.backend.ProcessAttachments = function() {
		$.Cmdbuild.errorsManager.error("ProcessAttachments backend was removed. Please use Attachments backend.")
	}
}) (jQuery);