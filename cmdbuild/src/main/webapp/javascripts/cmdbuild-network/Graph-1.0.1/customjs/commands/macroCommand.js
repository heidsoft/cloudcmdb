(function($) {
	if (! $.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (! $.Cmdbuild.g3d.commands) {
		$.Cmdbuild.g3d.commands = {};
	}
	var macroCommand = function(model, arCommands) {
		this.model = model;
		this.arCommands = arCommands;
		var backend = new $.Cmdbuild.g3d.backend.CmdbuildModel();
		this.newElements = [];
		backend.setModel(this.model);
		this.numberUndoes = 0;
		this.singleCommand = function(arCommands, callback, callbackScope) {
			if (arCommands.length == 0 || $.Cmdbuild.customvariables.commandsManager.stopped) {
				$.Cmdbuild.customvariables.commandsManager.stopped = false;
				callback.apply(callbackScope, []);
				return;
			}
			this.numberUndoes++;
			var singleCommand = arCommands[0];
			arCommands.splice(0, 1);
			var command = new $.Cmdbuild.g3d.commands[singleCommand.command](this.model, singleCommand);
			$.Cmdbuild.customvariables.commandsManager.execute(command, {
				batch: true
			}, function() {
				var me = this;
				//this timeout for return the dom at the browsers
				setTimeout(function() { me.singleCommand(arCommands, callback, callbackScope); }, 10);
			}, this);
		};
		this.execute = function(callback, callbackScope) {
			$.Cmdbuild.customvariables.commandInExecution = true;
			this.singleCommand(this.arCommands, function() {
				$.Cmdbuild.customvariables.commandInExecution = false;
				backend.model.changed(false);
				callback.apply(callbackScope, []);
			}, this);
		};
		this.undo = function() {
			for (var i = 0; i < this.numberUndoes; i++) {
				$.Cmdbuild.customvariables.commandsManager.undo();
			}
			this.model.changed(true);
		};
	};
	$.Cmdbuild.g3d.commands.macroCommand = macroCommand;
})(jQuery);
