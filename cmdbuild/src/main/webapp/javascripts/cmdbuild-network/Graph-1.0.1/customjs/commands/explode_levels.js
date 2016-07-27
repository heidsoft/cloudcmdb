(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (!$.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var explode_levels = function(model, params) {
		this.model = model;
		this.params = params;
		var batch = params.batch;
		var backend = new $.Cmdbuild.g3d.backend.CmdbuildModel();
		backend.setModel(this.model);
		this.newNodes = [];
		this.newEdges = [];
		this.execute = function(callback, callbackScope) {
			var levels = parseInt(params.levels);
			if ($.Cmdbuild.customvariables.commandsManager.stopped) {
				$.Cmdbuild.customvariables.commandsManager.stopped = false;
				callback.apply(callbackScope, []);
				return;
			}
			this.explodeNode(this.params.id, levels - 1, function() {
				callback.apply(callbackScope, []);
			}, this);
		};
		this.explodeNode = function(id, levels, callback, callbackScope) {
			if ($.Cmdbuild.customvariables.commandsManager.stopped) {
				$.Cmdbuild.customvariables.commandsManager.stopped = false;
				callback.apply(callbackScope, []);
				return;
			}
			var parentNode = this.model.getNode(id);
			var explodedChildren = $.Cmdbuild.g3d.Model.getGraphData(
					parentNode, "exploded_children");
			var oldChildren = $.Cmdbuild.g3d.Model.getGraphData(parentNode,
					"children");
			if (explodedChildren) {
				var emptyBunch = {
					nodes: [],
					edges: []
				};
				this.explodeMyChildren(parentNode, emptyBunch, oldChildren,
						batch, levels, callback, callbackScope);
			} else {
				$.Cmdbuild.g3d.Model.setGraphData(parentNode,
						"exploded_children", true);
				backend.getANodesBunch(id, function(elements) {
					this.explodeMyChildren(parentNode, elements, oldChildren,
							batch, levels, callback, callbackScope);
				}, this);
			}
		};
		this.explodeMyChildren = function(parentNode, elements, oldChildren,
				batch, levels, callback, callbackScope) {
			if ($.Cmdbuild.customvariables.commandsManager.stopped) {
				callback.apply(callbackScope, []);
				return;
			}
			var newChildren = [];
			this.saveForUndo(elements);
			this.model.pushElements(elements, function() {
				for (var i = 0; i < elements.nodes.length; i++) {
					var childId = elements.nodes[i].data.id;
					newChildren.push(childId);
				}
				if (oldChildren) {
					newChildren = newChildren.concat(oldChildren);
				}
				$.Cmdbuild.g3d.Model.setGraphData(parentNode, "children",
						newChildren);
				if (!batch) {
					this.model.changed();
				}
				if (levels > 0) {
					var children = newChildren.slice();
					this.explodeChildren(children, levels, callback, callbackScope);
				} else {
					callback.apply(callbackScope, []);
				}
			}, this);
		};
		this.saveForUndo = function(elements) {
			for (var i = 0; i < elements.nodes.length; i++) {
				var childId = elements.nodes[i].data.id;
				if (this.model.getNode(childId).length === 0) {
					this.newNodes.push(childId);
				}
			}
			for (var i = 0; i < elements.edges.length; i++) {
				var edge = elements.edges[i];
				if (this.model.getEdge({
					source: edge.data.source,
					target: edge.data.target,
					domainId: edge.data.domainId
				}).length === 0) {
					this.newEdges.push(edge);
				}
			}
		};
		this.explodeChildren = function(children, levels, callback,
				callbackScope) {
			if (children.length == 0
					|| $.Cmdbuild.customvariables.commandsManager.stopped) {

				callback.apply(callbackScope, []);
				return;
			}
			var child = children[0];
			children.splice(0, 1);
			this.explodeNode(child, levels - 1,
					function() {
						this.explodeChildren(children, levels, callback,
								callbackScope);
					}, this);
		};
		this.undo = function() {
			for (var i = 0; i < this.newEdges.length; i++) {
				var edge = this.newEdges[i];
				this.model.removeEdge({
					source: edge.data.source,
					target: edge.data.target,
					domainId: edge.data.domainId
				});
			}
			for (var i = 0; i < this.newNodes.length; i++) {
				var id = this.newNodes[i];
				this.model.remove(id);
			}
			this.model.changed(true);
		};
	};
	$.Cmdbuild.g3d.commands.explode_levels = explode_levels;
})(jQuery);
