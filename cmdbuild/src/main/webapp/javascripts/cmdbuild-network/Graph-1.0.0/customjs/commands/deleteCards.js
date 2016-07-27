(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (! $.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var deleteCards = function(model, selected, deleteSelection) {
		this.model = model;
		this.selected = selected;
		var selectedEles = selected.getData();
		this.saved4Undo = [];
		this.execute = function(callback, callbackScope) {
			var me = this;
			 me.deleteInBackground(deleteSelection, callback, callbackScope);
		};
		this.deleteInBackground = function(deleteSelection, callback, callbackScope) {
			if (deleteSelection == "true") {
				this.deleteSelected(selectedEles);
			}
			else {
				this.deleteUnselected(selectedEles);
			}
			this.model.changed(true);
			callback.apply(callbackScope, []);
			
		};
		this.insertEdges = function(saved) {
			for (var i = 0; i < saved.edges.length; i++) {
				var edge = saved.edges[i];
				this.model.insertEdge({
					data: {
						id: edge.id,
						source: edge.source,
						target: edge.target,
						label: edge.label,
						relationId: edge.relationId,
						domainId: edge.domainId
					}
				});				
			}
		};
		this.insertNode = function(saved) {
			var data = {
				classId: saved.classId,
				id: saved.id,
				label: saved.label,
				color: saved.color,
				faveShape: 'triangle',
				position: saved.position,
				compoundData: {},
				previousPathNode: saved.parentId,
				fromDomain: saved.fromDomain
			};
			var node = {
					data: data
			};
			return this.model.insertNode(node);
		};
		this.childPosition = function(parentId, id) {
			if (! parentId) {
				return -1;
			}
			var parent = this.model.getNode(parentId);
			var children = $.Cmdbuild.g3d.Model.getGraphData(parent, "children");
			if (! children) {
				console.log("A node with parent that have no children!!!!");
				return -1;
			}
			var index = children.indexOf(id);
			if (index == -1) {
				console.log("A node with parent that have no this child!!!!");
				return -1;
			}
			return index;
		};
		this.getNodeEdges = function(id) {
			var edges = [];
			var cyEdges = this.model.connectedEdges(id);
			for (var i = 0; i < cyEdges.length; i++) {
				var cyEdge = cyEdges[i];
				edges.push({
					id: $.Cmdbuild.g3d.Model.getGraphData(cyEdge, "id"),
					label: $.Cmdbuild.g3d.Model.getGraphData(cyEdge, "label"),
					source: $.Cmdbuild.g3d.Model.getGraphData(cyEdge, "source"),
					target: $.Cmdbuild.g3d.Model.getGraphData(cyEdge, "target"),
					relationId: $.Cmdbuild.g3d.Model.getGraphData(cyEdge, "relationId"),
					domainId: $.Cmdbuild.g3d.Model.getGraphData(cyEdge, "domainId")
				});
			}
			return edges;
		};
		this.save4Undo = function(id) {
			var node = this.model.getNode(id);
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(node, "previousPathNode");
			var childPosition = this.childPosition(parentId, id);
			this.saved4Undo.push({
				id: id,
				parentId: parentId,
				children: $.Cmdbuild.g3d.Model.getGraphData(node, "children"),
				classId: $.Cmdbuild.g3d.Model.getGraphData(node, "classId"),
				color: $.Cmdbuild.g3d.Model.getGraphData(node, "color"),
				label: $.Cmdbuild.g3d.Model.getGraphData(node, "label"),
				position: node.position(),
				previousPathNode: $.Cmdbuild.g3d.Model.getGraphData(node, "previousPathNode"),
				fromDomain: $.Cmdbuild.g3d.Model.getGraphData(node, "fromDomain"),
				childPosition: childPosition, 
				edges: this.getNodeEdges(id)
			});
		};
		this.undoSingleSaved = function(saved) {
			var children = saved.children;
			for (var i = 0; children && i < children.length; i++) {
				var child = this.model.getNode(children[i]);
				$.Cmdbuild.g3d.Model.setGraphData(child, "previousPathNode", saved.id);
				$.Cmdbuild.g3d.Model.setGraphData(child, "fromDomain", saved.fromDomain);
			}
			var cyNode = this.insertNode(saved);
			$.Cmdbuild.g3d.Model.setGraphData(cyNode, "children", children);
			this.insertEdges(saved);
		};
		this.undo = function() {
			var saved = this.saved4Undo.pop();
			while (saved) {
				this.undoSingleSaved(saved);
				if (saved.parentId) {
					var parent = this.model.getNode(saved.parentId);
					var parentChildren = $.Cmdbuild.g3d.Model.getGraphData(parent, "children");
					if (! parentChildren) {
						parentChildren = [];
					}
					parentChildren.splice(saved.childPosition, 0, saved.id);
					var node = this.model.getNode(saved.id);
					$.Cmdbuild.g3d.Model.setGraphData(parent, "children", parentChildren);
					$.Cmdbuild.g3d.Model.setGraphData(node, "previousPathNode", saved.parentId);
					$.Cmdbuild.g3d.Model.setGraphData(node, "fromDomain", saved.fromDomain);
				}
				saved = this.saved4Undo.pop();
			}
			this.model.changed();
		};
		this.deleteSelected = function(selectedEles) {
			$.Cmdbuild.customvariables.viewer.clearSelection();
			for (var key in selectedEles) {
				this.save4Undo(key);
				this.model.remove(key);
			}
		};
		this.deleteUnselected = function(selectedEles) {
			var nodes = this.model.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				var key = nodes[i].id();
				if (! selectedEles[key]) {
					this.save4Undo(key);
					this.model.remove(key);
				}
			}
		};
	};
	$.Cmdbuild.g3d.commands.deleteCards = deleteCards;
})(jQuery);
