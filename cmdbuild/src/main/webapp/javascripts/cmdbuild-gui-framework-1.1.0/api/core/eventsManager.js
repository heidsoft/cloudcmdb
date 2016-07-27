(function($) {
	var eventsManager = {
		events: [],
		deferred: 0,
		onEvent: function(param) {
			if (param) {
				this.events.push(param);
				this.executeAllEvents();
			}
		},
		deferEvents: function() {
			this.deferred++;
		},
		unDeferEvents: function() {
			this.deferred--;
			this.executeAllEvents();
		},
		executeAllEvents: function() {
			if (this.deferred > 0) {
				return;
			}
			while (this.events.length > 0) {
				var event = this.events[0];
				this.events.splice(0, 1);
				this.executeEvent(event);
			}
		},
		executeEvent: function(param) {
			try {
				if (! param.command) {
					var error = $.Cmdbuild.errorsManager.getError({
						message: $.Cmdbuild.errorsManager.CMERROR,
						type: $.Cmdbuild.errorsManager.EVENTCOMMANDNOTFOUND,
						element: param.id
					});
					throw error;
				}
				if ($.Cmdbuild.dataModel.callerParameters[param.id]) {
					$.Cmdbuild.dataModel.pushParametersOnStack(param.id);
				}
				if ($.Cmdbuild.custom.commands && $.Cmdbuild.custom.commands[param.command]) {
					$.Cmdbuild.custom.commands[param.command](param);
				}
				else if ($.Cmdbuild.standard.commands[param.command]) {
					$.Cmdbuild.standard.commands[param.command](param);
				}
				else  {
					var error = $.Cmdbuild.errorsManager.getError({
						message: $.Cmdbuild.errorsManager.CMERROR,
						type: $.Cmdbuild.errorsManager.EVENTMETHODNOTDEFINED,
						element: param.id,
						method: param.command
					});
					throw error;
				}
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.eventsManager.executeEvent " + param.command);
				throw e;
			}
		}
	};
	$.Cmdbuild.eventsManager = eventsManager;
}) (jQuery);