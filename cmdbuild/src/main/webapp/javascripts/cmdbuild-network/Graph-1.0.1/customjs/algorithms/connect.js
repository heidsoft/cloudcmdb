(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (!$.Cmdbuild.g3d.algorithms) {
		$.Cmdbuild.g3d.algorithms = {};
	}
	var connect = function(model, selected) {
		this.model = model;
		var selectedEles = selected.getData();
		this.execute = function() {
			var arrSelected = Object.keys(selectedEles);
			var path = this.model.bellmanFord(arrSelected[0]);
			selected.erase();
			for (var i = 1; i < arrSelected.length; i++) {
				var pathTo = path.pathTo("#" + arrSelected[i]);
				for (var j = 0; j < pathTo.length; j++) {
					if (pathTo[j].group() === 'nodes') {
						selected.select(pathTo[j].id(), true);
					}
				}
			}
			selected.changed();
		};
		this.execute();
	};
	$.Cmdbuild.g3d.algorithms.connect = connect;
})(jQuery);
