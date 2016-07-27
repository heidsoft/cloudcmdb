(function($) {
	var graph = function() {
		this.param = undefined;
		this.init = function(param) {
			try {
				this.param = param;
				this.show();
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.div.init");
				throw e;
			}
		};
		this.show = function() {
			try {
				var xmlForm = $.Cmdbuild.elementsManager.getElement(this.param.form);
				$.Cmdbuild.eventsManager.deferEvents();
				var htmlStr = "";
				htmlStr += $.Cmdbuild.elementsManager.insertChildren(xmlForm);
				var htmlContainer = $("#" + this.param.container)[0];
				htmlStr += "<div id='container'>";
				htmlStr += "<style> #graph-container { top: 0; bottom: 0; left: 0; right: 0; position: absolute;} </style>";
				htmlStr += "<div id='graph-container'></div></div>";
				htmlContainer.innerHTML = htmlStr;
				$.Cmdbuild.scriptsManager.push({
					script : "graph",
					id : "graph-container",
					className: this.param.className,
					cardId: this.param.cardId
				});
				$.Cmdbuild.elementsManager.initialize();
				$.Cmdbuild.eventsManager.unDeferEvents();
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.div.show");
				throw e;
			}
		};		
	};
	$.Cmdbuild.standard.graph = graph;
}) (jQuery);