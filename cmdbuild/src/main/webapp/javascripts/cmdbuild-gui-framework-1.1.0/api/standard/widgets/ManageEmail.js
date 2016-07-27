(function($) {
	var ManageEmail = {
		ROWBUTTONREGENERATE: "refresh",
		ROWBUTTONREPLY: "btnIconReply",
		ROWBUTTONDELETE: "deleteIconButton",
		ROWBUTTONMODIFY: "pencilGoIcon",
		mailAttributes: ["account", "bcc", "body", "cc", "date", "delay",
				"from", "_id", "keepSynchronization", "noSubjectPrefix",
				"notifyWith", "promptSynchronization", "status", "subject",
				"template", "to"],
		template2MailTable: {
			account: "account",
			bccAddresses: "bcc",
			content: "body",
			toAddresses: "to",
			ccAddresses: "cc",
			fromAddress: "from",
			subject: "subject"
		},
		saveMethod: $.Cmdbuild.widgets.SAVEAFTER,
		save: function(form, widget) {
			var formName = this.formName(form, widget);
			var formObject = $.Cmdbuild.dataModel.forms[form];
			if (!formObject) {
				return {};
			}
			var data = {
				data: formObject.getBackendData().mails,
				bkData: formObject.getBackendData().bkMails,
				name: widget._id
			};
			return data;
		},
		formName: function(form, widget) {
			var name = form + "_" + widget._id + "formattachements";
			return name;
		},
		cleanData: function(form, widget) {
			var name = this.formName(form, widget);
			$.Cmdbuild.dataModel.cleanForm(name);
		},
		prepareFields: function(widget) {
			for (var i = 0; i < widget.data.templates.length; i++) {
				widget.data.templates[i] = this
						.template2Mail(widget.data.templates[i]);
			}
		},
		evaluateCql: function(form, widget) {
			for (var i = 0; i < widget.data.templates.length; i++) {
				widget.data.templates[i] = widget.data.templates[i];
				var template = widget.data.templates[i];
				var attributes = this.getAttributes(form, widget, template, i);
				this.compileAttributes(form, attributes);
				template.attributes = attributes;

			}
			$.Cmdbuild.CqlManager.variablesTable.generate(form,
					$.Cmdbuild.CqlManager.commandsTable);
		},
		createField: function(name, widgetId, template, templateNumber,
				attributes) {
			attributes.push({
				name: name,
				filter: {
					text: template[name],
					params: template.variables
				}
			});
		},
		getAttributes: function(form, widget, template, templateNumber) {
			var attributes = [];
			this.createField("subject", widget._id, template, templateNumber,
					attributes);
			this.createField("body", widget._id, template, templateNumber,
					attributes);
			this.createField("to", widget._id, template, templateNumber,
					attributes);
			this.createField("from", widget._id, template, templateNumber,
					attributes);
			this.createField("cc", widget._id, template, templateNumber,
					attributes);
			this.createField("bcc", widget._id, template, templateNumber,
					attributes);
			return attributes;
		},
		setSynchronization: function(templates, checked, keepSynchronization,
				promptSynchronization) {
			for (var i = 0; i < templates.length; i++) {
				var template = templates[i];
				template.promptSynchronization = false;
				if (!keepSynchronization) {
					template.keepSynchronization = false;
				} else {
					if (checked[template._id]) {
						template.keepSynchronization = true;
					} else {
						template.keepSynchronization = false;
					}
				}
			}
		},
		compileAttributes: function(form, attributes) {
			for (var i = 0; i < attributes.length; i++) {
				$.Cmdbuild.CqlManager.compileAttribute(form, attributes[i]);
			}
		},
		initialize: function(form, widget) {
			var formObject = $.Cmdbuild.dataModel.forms[form];
			var backendData = formObject.getBackendData();
			backendData.mails = [];
			backendData.bkMails = [];
			for (var i = 0; i < widget.data.templates.length; i++) {
				var template = widget.data.templates[i];
				var _id = "guiTmpMail" + "_" + i;
				template._id = _id;
				backendData.mails.push({
					_id: _id,
					status: "draft"
				});
				$.Cmdbuild.widgets.ManageEmail.cqlResolve(form, template,
						function() {
						}, this);
			}
			this.loadMails(formObject.config, formObject.getBackend(),
					function() {
					}, this);
		},
		refreshCqlField: function(form, widget) {
			var formObject = $.Cmdbuild.dataModel.forms[form];
			if (formObject.initializationPhase == true) {
				return;
			}
			if (this.busy === true) {
				return;
			}
			this.busy = true;
			try {
				for (var i = 0; i < widget.data.templates.length; i++) {
					var template = widget.data.templates[i];
					var keepSynchronization = template.keepSynchronization;
					var promptSynchronization = template.promptSynchronization;
					if (!keepSynchronization) {
					} else if (!promptSynchronization) {
						$.Cmdbuild.widgets.ManageEmail.cqlResolve(form,
								template, function() {

								}, this);
					} else if (formObject.initializationPhase === false) {
						var id = form + "_" + widget._id;
						$.Cmdbuild.dataModel.pushSingleParameterOnStack(
								"widgetName", id);
						$.Cmdbuild.dataModel.pushSingleParameterOnStack(
								"formData", form);
						$.Cmdbuild.standard.commands.navigate({
							command: "navigate",
							form: "mailSynchronization",
							dialog: "syncMailDialog"
						});
						template.promptSynchronization = false;
					}
				}
			} catch (e) {
				console
						.log(
								"Warning: Cmdbuild.widgets.ManageEmail.refreshCqlField ",
								form);
				me.busy = false;
			}
			var me = this;
			this.TM = setTimeout(function() {
				me.busy = false;
			}, 500);
		},
		loadMails: function(param, backend, callback, callbackScope) {
			if (!param.cardId) {
				callback.apply(callbackScope, []);
			} else {
				$.Cmdbuild.utilities.proxy.getProcessMails(param.className,
						param.cardId, function(response) {
							this.callbackLoadMails(backend, param.className,
									param.cardId, response, callback,
									callbackScope);
						}, this);
			}
		},
		callbackLoadMails: function(backend, processType, processInstanceId,
				mails, callback, callbackScope) {
			if (mails.length <= 0) {
				callback.apply(callbackScope);
				return;
			}
			var mail = mails[0];
			mails.splice(0, 1);
			$.Cmdbuild.utilities.proxy.getProcessSingleMail(processType,
					processInstanceId, mail["_id"], function(response) {
						backend.data.mails.push(response);
						backend.data.bkMails.push($.Cmdbuild.utilities
								.clone(response));
						this.callbackLoadMails(backend, processType,
								processInstanceId, mails, callback,
								callbackScope);
					}, this);

		},
		template2Mail: function(template) {
			var ret = {};
			for ( var key in template) {
				if (this.template2MailTable[key]) {
					ret[this.template2MailTable[key]] = template[key];
				} else {
					ret[key] = template[key];
				}
			}
			return ret;
		}
	};
	$.Cmdbuild.widgets.ManageEmail = ManageEmail;
	// statics
	$.Cmdbuild.widgets.ManageEmail.cqlResolveSingleField = function(form,
			nameField, template, callback, callbackScope) {
		var formObject = $.Cmdbuild.dataModel.forms[form];
		var backendData = formObject.getBackendData();
		$.Cmdbuild.CqlManager.resolve(form, nameField, function(response) {
			var mail = $.Cmdbuild.widgets.ManageEmail.getMail(
					backendData.mails, template._id);
			mail[nameField] = response;
			mail.fromTemplate = template._id;
			if (callback) {
				callback.apply(callbackScope, [response]);
			}
		}, this);
	};
	$.Cmdbuild.widgets.ManageEmail.cqlResolve = function(form, template,
			callback, callbackScope) {
		var attributes = template.attributes.slice();
		$.Cmdbuild.widgets.ManageEmail.cqlResolveCallback(form, template,
				attributes, function() {
					callback.apply(callbackScope, []);
				}, this);
	};
	$.Cmdbuild.widgets.ManageEmail.cqlResolveCallback = function(form,
			template, attributes, callback, callbackScope) {
		if (attributes.length <= 0) {
			callback.apply(callbackScope, []);
			return;
		}
		var attribute = attributes[0];
		attributes.splice(0, 1);
		$.Cmdbuild.widgets.ManageEmail.cqlResolveSingleField(form,
				attribute.name, template, function() {
					$.Cmdbuild.widgets.ManageEmail.cqlResolveCallback(form,
							template, attributes, callback, callbackScope);
				}, this);
	};
	$.Cmdbuild.widgets.ManageEmail.refreshTemplate = function(form, widget,
			templateName) {
		for (var i = 0; i < widget.data.templates.length; i++) {
			var template = widget.data.templates[i];
			if (template._id == templateName) {
				$.Cmdbuild.widgets.ManageEmail.cqlResolve(form, template,
						function() {
						}, this);
				break;
			}
		}
	};
	$.Cmdbuild.widgets.ManageEmail.getMail = function(mails, id) {
		for (var i = 0; i < mails.length; i++) {
			if (mails[i]._id == id) {
				return mails[i];
			}
		}
	};
	$.Cmdbuild.widgets.ManageEmail.isDifferent = function(bkMail, mail) {
		for (var i = 0; i < $.Cmdbuild.widgets.ManageEmail.mailAttributes.length; i++) {
			var attribute = $.Cmdbuild.widgets.ManageEmail.mailAttributes[i];
			if (mail[attribute] != bkMail[attribute]) {
				return true;
			}
		}
		return false;
	};
	$.Cmdbuild.widgets.ManageEmail.copyMail = function(mail) {
		var mailOut = {};
		for (var i = 0; i < $.Cmdbuild.widgets.ManageEmail.mailAttributes.length; i++) {
			var attribute = $.Cmdbuild.widgets.ManageEmail.mailAttributes[i];
			mailOut[attribute] = mail[attribute];
		}
		return mailOut;
	};
	$.Cmdbuild.widgets.ManageEmail.deleteMails = function(param, data) {
		for (var i = 0; i < data.bkData.length; i++) {
			var found = false;
			var bkMail = data.bkData[i];
			for (var j = 0; j < data.data.length; j++) {
				var mail = data.data[j];
				if (bkMail._id == mail._id) {
					found = true;
					break;
				}
			}
			if (found) {
				continue;
			} else {
				$.Cmdbuild.utilities.proxy.deleteProcessMail(param.type,
						param.id, bkMail._id, function() {
						}, this);
			}
		}
	};
	$.Cmdbuild.widgets.ManageEmail.flush = function(param, data, widgetId,
			parentForm, callback, callbackScope) {
		if (!data.data) {
			if (!callback) {
				console.log(Error().stack);
			}
			callback.apply(callbackScope, []);
			return;
		}
		var arrMails = data.data.slice();
		$.Cmdbuild.widgets.ManageEmail.flushRecursive(param, arrMails,
				data.bkData, function() {
					this.deleteMails(param, data);
					callback.apply(callbackScope, []);
				}, this);
	};
	$.Cmdbuild.widgets.ManageEmail.flushRecursive = function(param, mails,
			bkMails, callback, callbackScope) {
		if (mails.length <= 0) {
			callback.apply(callbackScope, []);
			return;
		}
		var mail = mails[0];
		mails.splice(0, 1);
		var found = false;
		var present = $.grep(bkMails, function(el) {
			return el._id == mail._id;
		});
		if (present.length > 0) {// the mail was there
			var bkMail = present[0];
			if ($.Cmdbuild.widgets.ManageEmail.isDifferent(bkMail, mail)) {
				var mailOut = $.Cmdbuild.widgets.ManageEmail.copyMail(mail);
				$.Cmdbuild.utilities.proxy.putProcessMail(param.type, param.id,
						mailOut._id, mailOut, function() {
							$.Cmdbuild.widgets.ManageEmail.flushRecursive(
									param, mails, bkMails, callback,
									callbackScope);
						}, this);
			} else {
				$.Cmdbuild.widgets.ManageEmail.flushRecursive(param, mails,
						bkMails, callback, callbackScope);
			}
		} else {// a new mail
			var mailOut = $.Cmdbuild.widgets.ManageEmail.copyMail(mail);
			var mailId = mailOut["_id"];
			delete mailOut["_id"];
			$.Cmdbuild.utilities.proxy.postProcessMail(param.type, param.id,
					mailOut, function() {
						$.Cmdbuild.widgets.ManageEmail.flushRecursive(param,
								mails, bkMails, callback, callbackScope);
					}, this);
		}
	};
})(jQuery);