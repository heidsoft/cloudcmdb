(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var Model = function() {
		this.observers = [];
		this.erase = function() {
			cy.remove("*");
			cy.destroy();

		};
		this.erase();
		this.doLayout = function(options) {
			this.layout.clean();
			this.changed();
			// this.layout.layoutPositions(cy.nodes());
		};
		this.changeLayout = function(options) {
			this.layout = cy.elements().makeLayout(options);
			this.layout.run();
		};
		this.observe = function(observer) {
			if ($.inArray(observer, this.observers) === -1) {
				this.observers.push(observer);
			}
		};
		this.changed = function(params) {
			for (var i = 0; i < this.observers.length; i++) {
				this.observers[i].refresh(params);
			}
			$.Cmdbuild.dataModel.dispatchChange("viewer");
		};
		this.nodesLength = function() {
			return cy.nodes().length;
		};
		this.edgesLength = function() {
			return cy.edges().length;
		};
		this.getNodes = function() {
			return cy.nodes();
		};
		this.getNodesFromIdsArray = function(idsArray) {
			var nodes = [];
			for ( var key in idsArray) {
				var node = this.getNode(key);
				nodes.push(node);
			}
			return nodes;
		};
		this.getEdges = function() {
			return cy.edges();
		};
		this.connectedEdges = function(idNode) {
			var source = this.getNode(idNode);
			if (source.length === 0) {
				return cy.collection();
			}
			var nodes = cy.collection([ source ]).connectedEdges();
			return nodes;
		};
		this.getNode = function(id) {
			return cy.getElementById(id);
		};
		this.getEdge = function(edge) {
			var filter = "";
			filter += "[target='" + edge.target + "']";
			filter += "[source='" + edge.source + "']";
			filter += "[domainId='" + edge.domainId + "']";
			return cy.edges(filter);
		};
		this.copyNode = function(from) {
			var to = {
					id : from.id,
					classId : from.classId,
					label : from.label,
					color : from.color,
					compoundData : from.compoundData,
					domainId : from.domainId,
					children : from.children,
					previousPathNode : from.previousPathNode,
					nodeOnNavigationTree : from.nodeOnNavigationTree,
					fromDomain : from.fromDomain
			};
			return to;
		};
		this.insertNode = function(node) {
			var isHere = this.getNode(node.data.id);
			if (isHere.length !== 0) {
				return;
			}
			var cyNode = cy.add({
				group : "nodes",
				data : this.copyNode(node.data),
				nodeOnNavigationTree : node.data.nodeOnNavigationTree,
				position : {
					x : node.data.position.x,
					y : node.data.position.y,
					z : node.data.position.z
				}
			});
			return cyNode;
		};
		this.insertEdge = function(edge) {
			var exist = cy.edges("[source='" + edge.data.source + "'][target='"
					+ edge.data.target + "']");
			if (exist.length > 0) {
				return;
			}
			exist = cy.edges("[target='" + edge.data.source + "'][source='"
					+ edge.data.target + "']");
			if (exist.length > 0) {
				return;
			}
			cy.add({
				group : "edges",
				data : {
					source : edge.data.source,
					target : edge.data.target,
					label : edge.data.label,
					domainId : edge.data.domainId,
					relationId : edge.data.relationId
				},
			});
		};
		this.modifyPosition = function(id, value) {
			var node = this.getNode(id);
			node.position({
				x : value.x,
				y : value.y,
				z : value.z
			});
		};
		this.removeParentFromOpenChildren = function(children) {
			if (!children) {
				return;
			}
			for (var i = 0; i < children.length; i++) {
				var node = this.getNode(children[i]);
				$.Cmdbuild.g3d.Model.removeGraphData([ node ],
						"previousPathNode");
			}
		};
		this.removeOpenChildren = function(parentId, id) {
			var node = this.getNode(parentId);
			var children = $.Cmdbuild.g3d.Model.getGraphData(node, "children");// node.data("children");
			var newChildren = [];
			if (!children) {
				return;
			}
			for (var j = 0; j < children.length; j++) {
				var childId = children[j];
				if (childId != id) {
					newChildren.push(childId);
				}
			}
			$.Cmdbuild.g3d.Model.setGraphData(node, "children", newChildren);
		};
		this.removeEdge = function(edge) {
			var filter = "";
			filter += (edge.target) ? "[target='" + edge.target + "']" : "";
			filter += (edge.source) ? "[source='" + edge.source + "']" : "";
			filter += (edge.domainId) ? "[domainId='" + edge.domainId + "']"
					: "";
			cy.remove(cy.edges(filter));
		};
		this.remove = function(id) {
			var node = this.getNode(id);
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(node,
					"previousPathNode");
			var children = $.Cmdbuild.g3d.Model.getGraphData(node, "children");
			this.removeParentFromOpenChildren(children);
			if (parentId) {
				this.removeOpenChildren(parentId, id);
			}
			cy.remove("node#" + id);
		};
		this.pushElements = function(elements, callback, callbackScope) {
			$.Cmdbuild.customvariables.cacheClasses.pushClasses(elements,
					function() {
						for (var i = 0; i < elements.nodes.length; i++) {
							var node = elements.nodes[i];
							this.insertNode(node);
						}
						for (i = 0; i < elements.edges.length; i++) {
							var edge = elements.edges[i];
							this.insertEdge(edge);
						}
						callback.apply(callbackScope, []);
					}, this);
		};
		this.getDistinctClasses = function() {
			this.classes = {};
			var nodes = this.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				var node = nodes[i];
				var classId = $.Cmdbuild.g3d.Model
						.getGraphData(node, "classId");
				if (!this.classes[classId]) {
					var classDescription = $.Cmdbuild.customvariables.cacheClasses
							.getDescription(classId);
					this.classes[classId] = {
						classId : classId,
						classDescription : classDescription,
						qt : 1
					};
				} else {
					this.classes[classId].qt += 1;
				}
			}
			var arClasses = [];
			for ( var key in this.classes) {
				arClasses.push(this.classes[key]);
			}
			var retCards = [];
			for (i = 0; i < arClasses.length; i++) {
				retCards.push(arClasses[i]);
			}
			return {
				total : retCards.length,
				rows : retCards
			};
		};
		this.getCards = function(first, rows, filter) {
			first = parseInt(first);
			rows = parseInt(rows);
			var arClasses = [];
			var nodes = this.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				var node = nodes[i];
				var classId = $.Cmdbuild.g3d.Model
						.getGraphData(node, "classId");
				var classDescription = $.Cmdbuild.customvariables.cacheClasses
						.getDescription(classId);
				var label = $.Cmdbuild.g3d.Model.getGraphData(node, "label");
				if (filter.query) {
					filter.query = filter.query.toLowerCase();
					if (label.toLowerCase().indexOf(filter.query) < 0) {
						continue;
					}
				}
				var id = node.id();
				arClasses.push({
					id : id,
					classId : classId,
					classDescription : classDescription,
					label : label
				});
			}
			var retCards = [];
			if (first >= arClasses.length) {
				first = 0;
			}
			for (i = first; i < first + rows && i < arClasses.length; i++) {
				retCards.push(arClasses[i]);
			}
			return {
				total : arClasses.length,
				rows : retCards
			};
		};
		this.pathClasses = function(path, node) {
			if (!node) {
				return path;
			}
			var classId = $.Cmdbuild.g3d.Model.getGraphData(node, "classId");
			var fromDomain = $.Cmdbuild.g3d.Model.getGraphData(node,
					"fromDomain");
			path.push({
				classId : classId,
				cardId : node.id(),
				fromDomain : fromDomain
			});
			var previousPathNodeId = $.Cmdbuild.g3d.Model.getGraphData(node,
					"previousPathNode");
			if (!previousPathNodeId) {
				return path;
			}
			var previousPathNode = this.getNode(previousPathNodeId);
			return this.pathClasses(path, previousPathNode);
		};

		this.getNodesByClassName = function(classId) {
			return cy.filter(function(i, element) {
				if (element.isNode()
						&& $.Cmdbuild.g3d.Model
								.getGraphData(element, "classId") == classId) {
					return true;
				}
				return false;
			});
		};
		this.getChildrenByClassName = function(node, classId) {
			return $.Cmdbuild.g3d.Model.getChildrenByFunct(node,
					function(element) {
						return ($.Cmdbuild.g3d.Model.getGraphData(element,
								"classId") == classId);
					});
		};
		this.collection = function() {
			return cy.collection();
		};
		this.bellmanFord = function(id) {
			return cy.elements().bellmanFord({
				root : "#" + id
			});
			// return nodes.kruskal();
		};
	};
	$.Cmdbuild.g3d.Model = Model;

	$.Cmdbuild.g3d.Model.getChildrenByFunct = function(node, f) {
		var nodes = node.children();
		var children2Return = [];
		for (var i = 0; i < nodes.length; i++) {
			var element = nodes[i];
			if (f(element)) {
				children2Return.push(element);
			}
		}
		return children2Return;

	};
	$.Cmdbuild.g3d.Model.removeGraphData = function(nodes, key) {
		for (var i = 0; i < nodes.length; i++) {
			delete nodes[i]._private.data[key];
		}
	};
	$.Cmdbuild.g3d.Model.getGraphData = function(node, key) {
		return (!(node._private && node._private.data)) ? undefined
				: node._private.data[key];
	};
	$.Cmdbuild.g3d.Model.setGraphData = function(node, key, value) {
		if (!(node._private && node._private.data)) {
			node.data(key, value);
		} else {
			node._private.data[key] = value;
		}
	};
})(jQuery);
