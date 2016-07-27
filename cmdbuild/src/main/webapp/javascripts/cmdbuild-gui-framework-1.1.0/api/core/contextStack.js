(function($) {
	var contextStack = {
		context: [],
		push: function(param) {
			this.context.push({
				container: param.container,
				form: param.form
			});
		},
		pop: function(param) {
			for (var i = 0; i < this.context.length; i++) {
				if (param.container == this.context[i].container) {
					this.context.splice(i, this.context.length - i);
				}
			}
		},
		path: function(param) {
			var str = "";
			for (var i = 0; i < this.context.length; i++) {
				str += this.context[i].container + "_";
				if (param.container == this.context[i].container) {
					break;
				}
			}
			return str;
		},
		getFormPath: function(form) {
			for (var i = this.context.length - 1; i >= 0; i--) {
				if (form == this.context[i].form) {
					return this.path({
							container: this.context[i].container
						});
				}
			}
		}
	};
	$.Cmdbuild.standard.contextStack = contextStack;
}) (jQuery);
