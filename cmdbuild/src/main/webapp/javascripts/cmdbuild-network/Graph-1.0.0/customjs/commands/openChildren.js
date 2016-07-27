(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (!$.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var openChildren = function(model, params) {
		this.model = model;
		this.params = params;
		this.elements = params.elements;
		this.parentNode = undefined;
		this.compoundNode = undefined;
		var backend = new $.Cmdbuild.g3d.backend.CmdbuildModel();
		this.newElements = [];
		backend.setModel(this.model);
		this.newNodes = [];
		this.newEdges = [];
		this.compoundSavedNode = {
			node : {},
			edge : {}
		};
		this.execute = function(callback, callbackScope) {
			this.compoundNode = this.model.getNode(this.params.id);
			this.compoundEdge = this.model.connectedEdges(this.params.id)[0];
			this.saveForUndoCompoundNode(this.compoundNode, this.compoundEdge);
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(this.compoundNode,
					"previousPathNode");
			this.parentNode = this.model.getNode(parentId);
			this.oldChildren = $.Cmdbuild.g3d.Model.getGraphData(
					this.parentNode, "children");
			var domainId = $.Cmdbuild.g3d.Model.getGraphData(this.compoundNode,
					"domainId");
			backend.openCompoundNode(this.params.id, this.elements, domainId,
					function(elements) {
						this.newElements = [];
						for (var i = 0; i < elements.nodes.length; i++) {
							var childId = elements.nodes[i].data.id;
							if (this.model.getNode(childId).length === 0) {
								this.newElements.push(childId);
							}
						}
						this.saveForUndo(elements);
						this.model.pushElements(elements, function() {
							var allChildren = [];
							if (this.oldChildren) {
								allChildren = this.newElements
										.concat(this.oldChildren);
							}
							$.Cmdbuild.g3d.Model.setGraphData(this.parentNode,
									"children", allChildren);
							this.model.changed();
							callback.apply(callbackScope, []);
						}, this);
					}, this);
		};
		this.undo = function() {
			if (this.compoundSavedNode.node.id === undefined) {
				return;
			}
			for (var i = 0; i < this.newEdges.length; i++) {
				var edge = this.newEdges[i];
				this.model.removeEdge({
					source : edge.data.source,
					target : edge.data.target,
					domainId : edge.data.domainId
				});
			}
			for (i = 0; i < this.newNodes.length; i++) {
				var id = this.newNodes[i];
				this.model.remove(id);
			}
			var compoundNode = this.model
					.getNode(this.compoundSavedNode.node.id);
			if (compoundNode.length === 0) {
				this.model.pushElements({
					nodes : [ {
						data : this.compoundSavedNode.node
					} ],
					edges : [ {
						data : this.compoundSavedNode.edge
					} ]
				}, function() {
				}, this);
			} else {
				$.Cmdbuild.g3d.Model.setGraphData(compoundNode, "compoundData",
						this.compoundSavedNode.node.compoundData);
			}
			this.model.changed(true);
		};
		this.saveForUndoCompoundNode = function(node, edge) {
			var compoundData = $.Cmdbuild.g3d.Model.getGraphData(node,
					"compoundData");
			var data = {
				classId : $.Cmdbuild.g3d.Model.getGraphData(node, "classId"),
				id : node.id(),
				label : $.Cmdbuild.g3d.Model.getGraphData(node, "label"),
				color : "#ff0000",
				faveShape : 'triangle',
				position : {
					x : Math.random() * 1000 - 500,
					y : Math.random() * 600 - 300,
					z : 200
				},
				domainId : $.Cmdbuild.g3d.Model.getGraphData(node, "domainId"),
				compoundData : compoundData,
				previousPathNode : $.Cmdbuild.g3d.Model.getGraphData(node,
						"previousPathNode"),
				fromDomain : $.Cmdbuild.g3d.Model.getGraphData(node,
						"fromDomain")
			};
			node = data;

			data = {};
			if (edge) {
				data = {
					source : edge.source().id(),
					target : edge.target().id(),
					label : $.Cmdbuild.g3d.Model.getGraphData(edge, "label"),
					domainId : $.Cmdbuild.g3d.Model.getGraphData(edge,
							"domainId"),
					relationId : $.Cmdbuild.g3d.Model.getGraphData(edge,
							"relationId"),
					color : $.Cmdbuild.custom.configuration.edgeColor,
					strength : 90
				};
			}
			edge = data;
			this.compoundSavedNode = {
				node : node,
				edge : edge
			};
		};
		this.saveForUndo = function(elements) {
			for (var i = 0; i < elements.nodes.length; i++) {
				var childId = elements.nodes[i].data.id;
				if (this.model.getNode(childId).length === 0) {
					this.newNodes.push(childId);
				}
			}
			for (i = 0; i < elements.edges.length; i++) {
				var edge = elements.edges[i];
				if (this.model.getEdge({
					source : edge.data.source,
					target : edge.data.target,
					domainId : edge.data.domainId
				}).length === 0) {
					this.newEdges.push(edge);
				}
			}
		};
	};
	$.Cmdbuild.g3d.commands.openChildren = openChildren;
})(jQuery);
