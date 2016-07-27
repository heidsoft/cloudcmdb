(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var CommandsManager = function(model) {
		this.model = model;
		this.commandsStack = [];
		this.execute = function(command, params, callback, callbackScope) {
			if ($.Cmdbuild.customvariables.commandInExecution
					&& params.batch !== true) {
				if (callback) {
					callback.apply(callbackScope, []);
				}
				return;
			}
			var me = this;
			$.Cmdbuild.authProxy.maskRequest();
			// this for give control at the mask to start
			setTimeout(function() {
				command.execute(function(response) {
					command.batch = (params.batch === true);
					me.commandsStack.push(command);
					$.Cmdbuild.authProxy.unmaskRequest();
					if (callback) {
						callback.apply(callbackScope, [response]);
					}
				});
			}, 10);
		};
		this.undo = function() {
			if (this.commandsStack.length == 1 || $.Cmdbuild.customvariables.commandInExecution) {
				return;
			}
			var command = this.commandsStack.pop();
			var me = this;
			$.Cmdbuild.authProxy.maskRequest();
			// this for give control at the mask to start
			$.Cmdbuild.customvariables.selected.erase();
			setTimeout(function() {
				command.undo(me.model);
				$.Cmdbuild.customvariables.selected.changed({});
				$.Cmdbuild.authProxy.unmaskRequest();
			}, 10);
		};
	};
	$.Cmdbuild.g3d.CommandsManager = CommandsManager;
})(jQuery);
