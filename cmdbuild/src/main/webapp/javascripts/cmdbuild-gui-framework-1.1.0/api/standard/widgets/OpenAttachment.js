(function($) {
	var OpenAttachment =  {
		data: {},
		saveMethod: $.Cmdbuild.widgets.SAVEAFTER,
		save: function(form, widget) {
			return {
				data : $.Cmdbuild.dataModel.getAttachmentsForWidget(form)
						.slice(),
				name : widget._id
			};
		},
		formName: function(form, widget) {
			widget.formname = form + "_" + widget._id + "formattachements";
			return widget.formname;
		},
		cleanData: function(form, widget) {
			var name = this.formName(form, widget);
			$.Cmdbuild.dataModel.cleanForm(name);
		}
	};
	$.Cmdbuild.widgets.OpenAttachment = OpenAttachment;

	//statics
	$.Cmdbuild.widgets.OpenAttachment.flush = function(param, data, widgetid, parentform, callback, callbackScope) {
		var formname = $.Cmdbuild.widgets.OpenAttachment.formName(parentform, {_id : widgetid});

		var attachments = $.Cmdbuild.dataModel.getAttachmentsForWidget(formname);
		if (attachments.length <= 0) {
			callback.apply(callbackScope, []);
			return;
		}
		var attachment = attachments.splice(0, 1)[0];
		$.Cmdbuild.dataModel.setAttachmentsForWidget(param.form, attachments);
		$.Cmdbuild.utilities.proxy.postInstanceAttachment(param.type, param.id, attachment.formdata, function(response) {
			$.Cmdbuild.widgets.OpenAttachment.flush(param, data, widgetid, parentform, callback, callbackScope);
		}, this);
	};
}) (jQuery);