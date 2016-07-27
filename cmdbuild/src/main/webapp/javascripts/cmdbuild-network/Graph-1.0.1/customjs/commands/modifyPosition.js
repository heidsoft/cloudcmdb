(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (! $.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var modifyPosition = function(model, id, position) {
		this.model = model;
		this.id = id;
		var node = this.model.getNode(this.id);
		this.oldValue = $.Cmdbuild.utilities.clone(node.position());
		this.execute = function(callback, callbackScope) {
			var node = this.model.getNode(this.id);
			$.Cmdbuild.g3d.Model.setGraphData(node, $.Cmdbuild.g3d.constants.OBJECT_STATUS_MOVED, true);
			this.model.modifyPosition(this.id, position);
			callback.apply(callbackScope, []);
			this.model.changed();
		};
		this.undo = function() {
			this.model.modifyPosition(this.id, this.oldValue);
			this.model.changed();
		};
	};
	$.Cmdbuild.g3d.commands.modifyPosition = modifyPosition;
})(jQuery);
