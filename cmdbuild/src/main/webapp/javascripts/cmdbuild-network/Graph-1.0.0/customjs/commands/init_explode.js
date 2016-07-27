(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (!$.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var init_explode = function(model, params) {
		this.model = model;
		this.params = params;
		this.domainList = (params.domainList) ? params.domainsList : null;
		var batch = params.batch;
		var backend = new $.Cmdbuild.g3d.backend.CmdbuildModel();
		this.newElements = [];
		backend.setModel(this.model);
		var justExploded = {};
		this.execute = function(callback, callbackScope) {
			$.Cmdbuild.customvariables.commandInExecution = true;
			if ($.Cmdbuild.customvariables.commandsManager.stopped) {
				$.Cmdbuild.customvariables.commandInExecution = false;
				callback.apply(callbackScope, []);
				return;
			}
			backend.getInitModel(params, function(elements) {
				this.model.pushElements(elements, function() {
				var me = this;
				setTimeout(function() {
						var parentId = elements.nodes[0].data.id;
						me.explodeNode(parentId, $.Cmdbuild.customvariables.options["baseLevel"] - 1, function(elements) {
							if (!batch) {
								$.Cmdbuild.customvariables.commandsManager.stopped = false;
								$.Cmdbuild.customvariables.commandInExecution = false;
								me.model.changed();
							}
							callback.apply(callbackScope, [elements]);
						}, me);
					}, 100);
				}, this);
			}, this);
		};
		this.explodeNode = function(id, levels, callback, callbackScope) {
			if ($.Cmdbuild.customvariables.commandsManager.stopped) {
				$.Cmdbuild.customvariables.commandsManager.stopped = false;
				callback.apply(callbackScope, []);
				return;
			}
			if (justExploded[id] && justExploded[id] >= levels) {
				callback.apply(callbackScope, []);
				return;
			}
			else {
				justExploded[id] = levels;
			}
			var parentNode = this.model.getNode(id);
			var oldChildren = $.Cmdbuild.g3d.Model.getGraphData(parentNode, "children");
			backend.getANodesBunch(id, this.domainList, function(elements) {
				var newElements = [];
				this.model.pushElements(elements, function() {
					for (var i = 0; i < elements.nodes.length; i++) {
						var childId = elements.nodes[i].data.id;
						newElements.push(childId);
					}
					if (oldChildren) {
						newElements = newElements.concat(oldChildren);
					}
					$.Cmdbuild.g3d.Model.setGraphData(parentNode, "children", newElements);
					if (! batch) {
						this.model.changed();	
					}
					this.newElements = this.newElements.concat(newElements);
					if (levels > 0) {
						var children = this.newElements.slice();
						this.explodeChildren(children, levels - 1, function() {
							callback.apply(callbackScope, []);
						}, this);
					} else {
						callback.apply(callbackScope, [this.newElements]);
					}
				}, this);
			}, this);			
		};
		this.explodeChildren = function(children, levels, callback, callbackScope) {
			if (children.length == 0 || $.Cmdbuild.customvariables.commandsManager.stopped) {
				callback.apply(callbackScope, []);
				return;
			}
			var child = children[0];
			children.splice(0, 1);
			this.explodeNode(child, levels, function() {
				this.explodeChildren(children, levels - 1, callback, callbackScope);				
			}, this);
		};
		this.undo = function() {
			console.log("Error! undo the init");
		};
	};
	$.Cmdbuild.g3d.commands.init_explode = init_explode;
})(jQuery);
