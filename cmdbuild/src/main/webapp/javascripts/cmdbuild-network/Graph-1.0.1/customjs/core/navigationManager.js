(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var navigationManager = function() {
		this.tree = undefined;
		this.setServerTree = function(tree) {
			this.serverTree = tree;
			this.tree = this.transformInTree(tree.nodes[0]);

			this.root = tree.nodes[0];
		};
		this.getCurrentTree = function() {
			return this.serverTree;
		};
		this.getRoot = function() {
			return this.root;
		};
		this.cleanCurrentTree = function() {
			this.serverTree = null;
		};
		this.setCurrentTree = function(node, callback, callbackScope) {
			var classId = $.Cmdbuild.g3d.Model.getGraphData(node, "classId");
			this.loadDomains(this.tree, function() {
				chooseTreePrompt(classId, this.tree, this, function(value) {
					if (value) {
						this.root = this.getNode(value, this.tree);
					} else {
						this.serverTree = null;
						this.tree = null;
						this.root = null;
					}
					callback.apply(callbackScope, [ value ]);
				}, this);
			}, this);
		};
		this.getNode = function(id, tree) {
			if (tree._id == id) {
				return tree;
			} else {
				for (var i = 0; i < tree.children.length; i++) {
					var node = this.getNode(id, tree.children[i]);
					if (node !== null) {
						return node;
					}
				}
			}
			return null;
		};
		this.transformInTree = function(from) {
			var myChildren = [];
			var node = {
				_id : from._id,
				domainId : from.metadata.domain,
				recursionEnabled : from.metadata.recursionEnabled,
				targetClass : from.metadata.targetClass,
				parent : from.parent

			};
			var children = this.getChildrenServerTree(from._id);
			for (var i = 0; i < children.length; i++) {
				myChildren.push(this.transformInTree(children[i]));
			}
			node.children = myChildren;
			return node;
		};
		this.loadDomainChildren = function(nodes, index, callback,
				callbackScope) {
			if (index >= nodes.length) {
				callback.apply(callbackScope, []);
				return;
			}
			var node = nodes[index];
			this.loadDomains(node, function() {
				this.loadDomainChildren(nodes, ++index, callback,
						callbackScope);
			}, this);
		};
		this.loadDomains = function(tree, callback, callbackScope) {
			if (tree.domainId === null) {
				this.loadDomainChildren(tree.children, 0, callback,
						callbackScope);
			} else {
				$.Cmdbuild.customvariables.cacheDomains.loadSingleDomain(
						tree.domainId, function() {
							this.loadDomainChildren(tree.children, 0, callback,
									callbackScope);
						}, this);
			}
		};
		this.getChildrenServerTree = function(id) {
			var nodes = [];
			for (var i = 0; i < this.serverTree.nodes.length; i++) {
				var node = this.serverTree.nodes[i];
				if (node.parent === id) {
					nodes.push(node);
				}
			}
			return nodes;
		};
		this.searchNode = function(tree, originalPath, domains) {
			if ((tree.domainId === originalPath.fromDomain || originalPath.fromDomain === undefined) && this.sameClass(tree.targetClass, originalPath.classId)) {
				var ar = this.transformInNodes(tree);
				domains = $.merge(ar, domains);
			} 
			for (var i = 0; i < tree.children.length; i++) {
				this.searchNode(tree.children[i], originalPath, domains);
			}
		};
		this.calculatePathFromNode_NEW = function(node, originalPath, index, parent) {
			var domains = [];
			this.searchNode(node, originalPath[0], domains);
			
			return domains;
		};
		this.calculatePathFromNode = function(node, originalPath, index, parent) {
			if (!node) {
				return null;
			}
			var domain = $.Cmdbuild.customvariables.cacheDomains
					.getDomain(node.domainId);
			var path = originalPath[originalPath.length - index];
			if (domain === null
					&& !this.sameClass(node.targetClass, path.classId)) {
				return null;
			} else if (domain !== null
					&& !(this.sameClass(domain.destinationId, path.classId) || this
							.sameClass(domain.sourceId, path.classId))) {
				return null;
			}
			if (!(node.domainId === undefined || path.fromDomain === undefined || node.domainId == path.fromDomain)) {
				return null;
			}
			if (originalPath.length - index <= 0) {
				return node;
			}
			if (node.recursionEnabled) {
				return this.calculatePathFromNode(node, originalPath,
						index + 1, node);
			} else {
				var children = this.getChildren(node, parent);
				for (var i = 0; i < children.length; i++) {
					var retNode = this.calculatePathFromNode(children[i],
							originalPath, index + 1, node);
					if (retNode !== null) {
						return retNode;
					}
				}
				return null;
			}
		};

		this.sameClass = function(superClass, currentClass) {
			return $.Cmdbuild.customvariables.cacheClasses.sameClass(
					superClass, currentClass);
		};
		this.getChildren = function(node, parent) {
			var children = [];
			if (!node.children) {
				console.log("WARNING Navigation Manager getChildren", node);
			}
			for (var i = 0; i < node.children.length; i++) {
				var child = node.children[i];
				if ((!parent) || (child._id !== parent._id)) {
					children.push(child);
				}
			}
			if (!parent || (node.parent !== null && node.parent !== parent._id)) {
				children.push(this.getNode(node.parent, this.tree));
			}
			return children;
		};
		this.searchMyRoot = function(classId, node, parent) {
			var domain = $.Cmdbuild.customvariables.cacheDomains
					.getDomain(node.domainId);
			if (domain === null && this.sameClass(node.targetClass, classId)) {
				return node;
			}
			if (domain !== null && this.sameClass(domain.sourceId, classId)) {
				return node;
			}
			if (domain !== null
					&& this.sameClass(domain.destinationId, classId)) {
				return node;
			}
			var children = this.getChildren(node, parent);
			for (var i = 0; i < children.length; i++) {
				var retNode = this.searchMyRoot(classId, children[i], node);
				if (retNode !== null) {
					return retNode;
				}
			}
			return null;
		};
		this.copyTreeNode = function(from) {
			return {
				_id : from._id,
				children : from.children,
				domainId : from.domainId,
				recursionEnabled : from.recursionEnabled,
				targetClass : from.targetClass
				
			};
		}
		this.transformInNodes = function(node) {
			var domains = [];
			for (var i = 0; node && i < node.children.length; i++) {
				var child = node.children[i];
				if (child.domain !== null) {
					domains.push(child);
				}
			}
			if (node && node.recursionEnabled) {
				domains.push(node);
			}
			if (node && node.parent) {
				var parent = this.getNode(node.parent, this.tree);
				if (parent) { 
					var nodeParent = this.copyTreeNode(parent);
					nodeParent.domainId = node.domainId;
					nodeParent.parent = node._id;
					domains.push(nodeParent);
				}
			}
			return domains;
		};
		this.getClassPathInTree = function(node) {
			var nodeOnNavigationTreeId = $.Cmdbuild.g3d.Model.getGraphData(node, "nodeOnNavigationTree");
			var nodeOnNavigationTree = this.getNode(nodeOnNavigationTreeId, this.tree);
			var nodes = this.transformInNodes(nodeOnNavigationTree);
		return (nodes) ? nodes : null;
		};
	};
	$.Cmdbuild.g3d.navigationManager = navigationManager;

	function chooseTreePrompt(classId, tree, parentWindow, callback,
			callbackScope) {
		this.returnValue = null;
		this.tree = tree;
		var me = this;
		this.space = 10;
		this.currentClass = classId;
		this.possibleNodes = [];
		this.createRadio = function(value, code, disabled) {
			var strHtml = '';
			var strCode = (!disabled) ? 'code="' + code + '"' : '';
			var strClass = (!disabled) ? 'radio selectable' : 'radio';
			strHtml += '<text  name="chooseTreePrompt"  class="' + strClass + '" '
					+ strCode + '>';
			strHtml += "<span class=\"icon\"></span>";
			strHtml += value;
			strHtml += '</text>';
			return strHtml;
		};
		this.showDomain = function(indent, node, parentClassId) {
			var strHtml = "";
			var str;
			var disabled;
			if (!node.domainId) {
				strHtml += '<div style="margin-left: ' + (indent * this.space) + 'px">';
				disabled = !parentWindow.sameClass(node.targetClass,
						this.currentClass);
				str = node.targetClass + " - " + "Root";
				if (!disabled) {
					str = "<b>" + str + "</b>";
				}
				if (!disabled) {
					this.possibleNodes.push(node._id);
				}
				strHtml += this.createRadio(str, node._id, disabled);
				strHtml += '</div>';
				return strHtml;

			}
			var domain = $.Cmdbuild.customvariables.cacheDomains
					.getDomain(node.domainId);
			if (domain === null) {
				console.log("WARNING: " + node.domainId + " domain ", domain);
				return "";
			}
			var parent = parentWindow.getNode(node.parent, parentWindow.tree);
			var direction = this.getDomainDirection(domain, node.targetClass,
					parentClassId);
			var description = "";
			var classTarget = "";
			var classSource = "";
			if (direction === 1) {
				description = domain.descriptionDirect;
				classTarget = domain.destinationId;
				classSource = domain.sourceId;
			} else {
				description = domain.descriptionInverse;
				classTarget = domain.sourceId;
				classSource = domain.destinationId;
			}
			var classDestinationDescription = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(classTarget);
			var classSourceDescription = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(classSource);
			strHtml += '<div style="margin-left: ' + (indent * this.space)+ 'px">';
			disabled = !parentWindow.sameClass(node.targetClass, this.currentClass);
			if (!disabled) {
				this.possibleNodes.push(node._id);
			}
			str = classSourceDescription + " - " + description + " - "
					+ classDestinationDescription;
			if (!disabled) {
				str = "<b>" + str + "</b>";
			}
			strHtml += this.createRadio(str, node._id, disabled);
			strHtml += '</div>';
			return strHtml;
		};
		this.getDomainDirection = function(domain, targetClass, originClass) {
			var direction = 3;
			if (parentWindow.sameClass(targetClass/* originClass */,
					domain.sourceId)) {
				direction = 2;
			} else if (parentWindow.sameClass(targetClass/* originClass */,
					domain.destinationId)) {
				direction = 1;
			}
			return direction;
		};
		this.loadTree = function(indent, node, parentClassId) {
			var strHtml = "";
			strHtml += this.showDomain(indent, node, parentClassId);
			for (var i = 0; i < node.children.length; i++) {
				var nodeParent = parentWindow.getNode(node.children[i].parent,
						parentWindow.tree);
				strHtml += this.loadTree(indent + 1, node.children[i],
						nodeParent.targetClass);
			}
			return strHtml;
		};
		this.loadForm = function(tree) {
			var cancelButton = $.Cmdbuild.translations.getTranslation("BUTTON_CANCEL", "Cancel");
			var form = {
				state0 : {
					title : $.Cmdbuild.translations.getTranslation("TITLEPOPUP_TREES", "Possible positions in the tree"),
					html : tree,
					buttons : {
					},
					focus : 1,
					submit : function(e, v, m, f) {
						if (!v) {
							me.returnValue = null;
							$.prompt.close();
						}
						return false;
					}
				}
			};
			form.state0.buttons[cancelButton] = false;
			return form;
		};
		this.prompt = function(form) {
			var me = this;
			$.prompt(form, {
				close : function(e, v, m, f) {
					callback.apply(callbackScope, [ me.returnValue ])
				},
				loaded : function() {
					var button = $('[name="chooseTreePrompt"]');
					button.bind("click", function() {
						var code = $(this).attr("code");
						if (code !== undefined) {
							me.returnValue = code;
							$.prompt.close();
						}
					});
				},
				classes : {
					box : '',
					fade : '',
					prompt : 'ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-draggable ui-resizable cmdbuild-graph',
					close : 'ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close',
					title : 'ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix ui-draggable-handle',
					message : 'ui-dialog-content ui-widget-content',
					buttons : 'ui-dialog-content ui-widget-content',
					button : 'cmdbuildButton ui-button ui-widget ui-state-default ui-corner-all',
					defaultButton : 'pure-button-primary'
				},
				promptspeed : "fast",
			});
		};
		this.init = function() {
			var tree = this.loadTree(0, this.tree, null);
			var form = this.loadForm(tree);
			if (this.possibleNodes.length > 1) {
				this.prompt(form);
				
			}
			else {
				var possibleNode =  (this.possibleNodes.length > 0) ? this.possibleNodes[0] : null;
				callback.apply(callbackScope, [ possibleNode]);
			}
		};
		init();
	}
})(jQuery);
