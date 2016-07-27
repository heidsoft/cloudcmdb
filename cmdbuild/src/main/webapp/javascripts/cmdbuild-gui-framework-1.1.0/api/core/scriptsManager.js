(function($) {
	var scriptsManager = {
		arScripts: [],
		execute: function() {
			try {
				while (this.arScripts.length > 0) {
					var script = this.arScripts[0];
					this.arScripts.splice(0, 1);
					this.executeScript(script);
				}
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.scriptsManager.execute " + e.message, e.stack);
				throw e;
			}
		},
		executeScript: function(param) {
				if ($.Cmdbuild.custom.scripts && $.Cmdbuild.custom.scripts[param.script]) {
					$.Cmdbuild.custom.scripts[param.script](param);
				}
				else if ($.Cmdbuild.standard.scripts[param.script]) {
					$.Cmdbuild.standard.scripts[param.script](param);
				}
				else {
					var error = $.Cmdbuild.errorsManager.getError({
						message: $.Cmdbuild.errorsManager.CMERROR,
						type: $.Cmdbuild.errorsManager.SCRIPTNOTDEFINED,
						element: param.id,
						method: param.type
					});
					throw error;
				}
		},
		push: function(param) {
			this.arScripts.push(param);
		}
	};
	$.Cmdbuild.scriptsManager = scriptsManager;
}) (jQuery);