(function($) {
	var navigationTreesBtnMenu = function() {
		var control = "navigationTreesBtnMenu_menu";
		this.updateMenu = function(classId) {
			// get menu and empty it
			var $menu = $("#" + control).find("ul");
			$menu.empty();
			// update menu values
			var values = $.Cmdbuild.customvariables.cacheTrees
					.getTreesFromClass(classId);
			$.each(values, function(index, value) {
				var $span = $("<span></span>").text(value.description);
				var tree = $.Cmdbuild.customvariables.cacheTrees
						.getCurrentNavigationTree();

				var $li = $("<li></li>").append($span).click(
						function() {
							tree = $.Cmdbuild.customvariables.cacheTrees
									.getCurrentNavigationTree();
							if (tree && value._id === tree._id) {
								$.Cmdbuild.custom.commands
										.removeNavigationTree({
											treeValue : value._id
										});
								$(this).removeClass("active");

							} else {
								$.Cmdbuild.custom.commands
										.applyNavigationTree({
											treeValue : value._id
										});
							}
						});
				var active = (tree && value._id === tree._id);
				if (active) {
					$li.addClass("active");
				}
				$menu.append($li);
			});

			// enable / disable menu button
			if (values.length) {
				$menu.parent().parent().removeClass("btn-disabled");
			}
		};
		this.refreshSelected = function() {
			var current = $.Cmdbuild.customvariables.selected.getCurrent();
			$("#" + control).parent().addClass("btn-disabled");
			if (current) {
				var currentNode = $.Cmdbuild.customvariables.model
						.getNode(current);
				var currentClassId = $.Cmdbuild.g3d.Model.getGraphData(
						currentNode, "classId");
				this.updateMenu(currentClassId);
			}
		};
		$.Cmdbuild.customvariables.selected.observe(this);
	};
	$.Cmdbuild.g3d.navigationTreesBtnMenu = navigationTreesBtnMenu;
})(jQuery);
