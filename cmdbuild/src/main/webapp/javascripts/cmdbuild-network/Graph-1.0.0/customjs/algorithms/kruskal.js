(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (!$.Cmdbuild.g3d.algorithms) {
		$.Cmdbuild.g3d.algorithms = {};
	}
	var kruskal = function(model, selected) {
		this.model = model;
		var selectedEles = selected.getData();
		this.execute = function() {
			var nodes = this.model.getNodesFromIdsArray(selectedEles);
			if (nodes.length < 2) {
				alert("Kruskal works on two or more parameters");
				return;
			}
			var path = this.model.kruskal();
			selected.erase();
			for (var i = 0; i < path.length; i++) {
				if (path[i].group() === 'nodes') {
					selected.select(path[i].id());
				}
			}
			selected.changed();
		};
		this.execute();
	};
	$.Cmdbuild.g3d.algorithms.kruskal = kruskal;
})(jQuery);
