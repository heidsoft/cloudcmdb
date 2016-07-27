(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var Selected = function(model) {
		this.current = undefined;
		this.observers = [];
		this.model = model;
		this.selected = {};
		this.getData = function() {
			return this.selected;
		};
		this.observe = function(observer) {
			this.observers.push(observer);
		};
		this.isEmpty = function() {
			return $.isEmptyObject(this.selected);
		};
		this.changed = function(params) {
			for (var i = 0; i < this.observers.length; i++) {
				this.observers[i].refreshSelected(params);
			}
			$.Cmdbuild.dataModel.dispatchChange("selected");
		};
		this.isSelect = function(nodeId) {
			return (this.selected[nodeId] === true) ? true : false;
		};
		this.getCurrent = function() {
			return this.current;
		};
		this.length = function() {
			return Object.keys(this.selected).length;
		};
		this.select = function(nodeId, noRefresh) {
			this.current = nodeId;
			if (! this.selected[nodeId] === true) {
				this.selected[nodeId] = true;
				if (! noRefresh) {
					this.changed({});
				}
			}
		};
		this.unSelect = function(nodeId) {
			if (this.selected[nodeId] === true) {
				delete this.selected[nodeId];
				this.changed({});
			}
		};
		this.erase = function() {
			this.selected = {};
		};
		this.selectByClassName =  function(classId, bAddSelection, canBeASuperClass) {
			if (! bAddSelection) {
				this.erase();
			}
			var nodes = this.model.getNodesByClassName(classId, canBeASuperClass);
			for (var i = 0; i < nodes.length; i++) {
				if (this.selected[nodes[i].id()] === true && bAddSelection) {
					delete this.selected[nodes[i].id()];
				} else {
					this.selected[nodes[i].id()] = true;
				}
			}
			this.changed({});
		};
		this.getCards = function(first, rows, filter) {
			first = parseInt(first);
			rows = parseInt(rows);
			var arClasses = [];
			for (var key in this.selected) {
				var node = this.model.getNode(key);
				var classId = $.Cmdbuild.g3d.Model.getGraphData(node, "classId");
				var classDescription = $.Cmdbuild.customvariables.cacheClasses.getDescription(classId);
				var label = $.Cmdbuild.g3d.Model.getGraphData(node, "label");
				if (filter && filter.query && label.toLowerCase().indexOf(filter.query) < 0) {
					continue;
				}
				var id = node.id();
				arClasses.push({
					id: id,
					classId: classId,
					classDescription: classDescription,
					label: label
				});
			}
			if (rows === -1) {
				return {
					total: arClasses.length,
					rows: arClasses
				};
				
			}
			var retCards = [];
			for (var i = first; i < first + rows && i < arClasses.length; i++) {
				retCards.push(arClasses[i]);
			}
			return {
				total: retCards.length,
				rows: retCards
			};
		};
	};
	$.Cmdbuild.g3d.Selected = Selected;
	
})(jQuery);