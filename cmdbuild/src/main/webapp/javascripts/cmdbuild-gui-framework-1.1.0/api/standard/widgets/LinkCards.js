(function($) {
	var LinkCards =  {
		saveMethod: $.Cmdbuild.widgets.SAVEONCARD,
		save: function(form, widget) {
			var formName = this.formName(form, widget);
			var formObject = $.Cmdbuild.dataModel.forms[formName];
			if (! formObject) {
				return {};
			}
			var output = {};
			for(var key in formObject.checked) {
				var checked = formObject.checked[key];
			    if (checked) {
			    	output[key] = {};
			    }
			}		
			return output;
		},
		formName: function(form, widget) {
			var name = form + "_" + widget._id + "grid";
			return name;
		},
		cleanData: function(form, widget) {
			var name = this.formName(form, widget);
			$.Cmdbuild.dataModel.cleanForm(name);
		}
	};
	$.Cmdbuild.widgets.LinkCards = LinkCards;
}) (jQuery);