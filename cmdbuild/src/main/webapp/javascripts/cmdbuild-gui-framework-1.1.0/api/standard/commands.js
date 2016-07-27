(function($) {
	var commands = {
		tab: function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			$('#' + paramActualized.form).tabs("option", "active", param.activeTab);
		},
		navigate : function(param) {
			try {
				var xmlForm = $.Cmdbuild.elementsManager.getElement(param.form);
				var fields = $.Cmdbuild.utilities.getFields(xmlForm);
				if (fields && fields.fields) {
					fields.form = param.form;
					$.Cmdbuild.dataModel.putFormFields(fields);
				}
				$.Cmdbuild.utilities.include(xmlForm, this.navigateIncludeCB, {
					xmlForm : xmlForm,
					param : param
				}, undefined);// include if is definite an attribute include
			}
			catch (e) {
				console.log("Commands navigate " + e.message, param, e.stack);
			}
		},
		navigateIncludeCB : function(params) {
			var xmlForm = params.xmlForm;
			var param = params.param;
			$.Cmdbuild.elementsManager.getParams(xmlForm, param);
			var isObserving = $.Cmdbuild.elementsManager.isObserving(xmlForm,
					param.form);
			var navigationType = $.Cmdbuild.standard[param.type] || $.Cmdbuild.custom[param.type];
			if (!(param.type && navigationType)) {
				throw new $.Cmdbuild.errorsManager.getError({
					message : $.Cmdbuild.errorsManager.CMERROR,
					type : $.Cmdbuild.errorsManager.PARAMNOTCORRECT,
					param : "type",
					method : "navigate",
					element : param.form
				});
			}
			if (isObserving && param.fromObserving !== "true") {
				return;
			}
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			try {
				var formFn = navigationType;//$.Cmdbuild.standard[param.type];
				var form = undefined;
				if ($.Cmdbuild.dataModel.forms[param.form]) {
					form = $.Cmdbuild.dataModel.forms[param.form];
				} else {
					form = new formFn();
					$.Cmdbuild.dataModel.forms[param.form] = form;
				}
				$.Cmdbuild.standard.contextStack.pop(paramActualized);
				$.Cmdbuild.standard.contextStack.push(paramActualized);
				form.init(paramActualized);
			} catch (e) {
				console.log(e, e.stack);
			}
		},
		advance: function(param) {
			try {
				param.formObject = $.Cmdbuild.dataModel.forms[param.form];
				if (! $.Cmdbuild.dataModel.change(param)) {
					return;
				}
				$.Cmdbuild.dataModel.flush(param, function(response) {
					if (param.dialog) {
						$.Cmdbuild.standard.commands.dialogClose(param);
						return;
					}
					var data = $.Cmdbuild.dataModel.model[param.form].data;
					if (! data._id) {
						data._id = response;
						data._type = param.formObject.config.className;
					}
					if (param.navigationForm) {
						$.Cmdbuild.standard.commands.navigate({
							form: param.navigationForm,
							container: param.navigationContainer
						});
						return;
					}
					$.Cmdbuild.utilities.proxy.getActivity(data._type, data._id,
							function(response) {
								if (response.length == 0) {
									if (param.tab) {
										var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
										$('#' + paramActualized.tabbedForm).tabs("option", "active", param.tab);
									}
									return;
								}
								$.Cmdbuild.standard.commands.navigate({
									form: param.form,
									container: param.container,
									className: data._type,
									cardId: data._id,
									activityInstanceId: response[0]._id 
								});
					}, this);
				});
			}
			catch (e) {
				$.Cmdbuild.errorsManager.popup(e);
			}
		},
		save: function(param) {
			try {
				if (param.ldap) {
					$.Cmdbuild.ldap.write({
						form: param.form,
						ldapDescriptor: param.ldap
					});
				}
				param.formObject = $.Cmdbuild.dataModel.forms[param.form];
				if (! $.Cmdbuild.dataModel.change(param)) {
					return;
				}
				$.Cmdbuild.dataModel.flush(param, function(response) {
					if (param.dialog) {
						$.Cmdbuild.standard.commands.dialogClose(param);
						return;
					}
				});
				if (param.navigationForm) {
					$.Cmdbuild.standard.commands.navigate({
						form: param.navigationForm,
						container: param.navigationContainer
					});
					return;
				}
			}
			catch (e) {
				$.Cmdbuild.errorsManager.popup(e);
			}
		},
		cancel: function(param) {
			try {
				var xmlForm = $.Cmdbuild.elementsManager.getElement(param.form);
				$.Cmdbuild.elementsManager.getParams(xmlForm, param);
				param.backend = $.Cmdbuild.utilities.getBackend(param.backend);
				param.formObject = $.Cmdbuild.dataModel.forms[param.form];
				$.Cmdbuild.dataModel.reset(param);
				if (param.navigationForm) {
					$.Cmdbuild.standard.commands.navigate({
						form: param.navigationForm,
						container: param.navigationContainer
					});
					return;
				}
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.commands.cancel");
				throw e;
			}
		},
		dialogExec: function(param) {
			$select = $("#" + param.toExecCommand);
			$select.trigger("itemselectedfromgrid");
			$("#" + param.dialog).dialog( "close" );
		},
		dialogClose: function(param) {
			if (param.dialog) {
				var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
				$("#" + paramActualized.dialog).dialog( "close" );
			}
		},
		dialogCancel: function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			if (paramActualized.form && paramActualized.selection) {
				$.Cmdbuild.standard.grid.cancelSelection(paramActualized.form);
			}
			$("#" + paramActualized.dialog).dialog( "close" );
		},
		fieldChanged: function(param) {
			var $input = $('#' + param.id);
			var fieldName = $input.attr("name");
			if (!fieldName || fieldName == "") {
				// @deprecated
				fieldName = $input.attr("fieldName");
				$.Cmdbuild.errorsManager.warn("Attribute fieldName is deprecated. Use name.");
			}
			var formId = $input.attr("form");
			if (!formId || formId == "") {
				// @deprecated
				formId = $input.attr("formName");
				$.Cmdbuild.errorsManager.warn("Attribute formName is deprecated. Use form.");
			}
			$.Cmdbuild.CqlManager.fieldChanged(formId, fieldName);
		},
		deleteRow: function(param) {
			var formObject = $.Cmdbuild.dataModel.forms[param.form];
			var backend = formObject.getBackend();
			backend.deleteRow({
				form: param.form
			});
		},
		attach: function(params) {
			var formObject = $.Cmdbuild.dataModel.forms[params.form];
			var backend = formObject.getBackend();
			var errors = [];

			// values
			var description = $("#" + params.description).val();
			var category = $("#" + params.category).val();
			var file = document.getElementById(params.id).files[0];
			var filepath = $('#' + params.id).val();

			// validate
			if (!file) {
				errors.push({
					field : $("#" + params.id).attr("fieldName"),
					errorType : $.Cmdbuild.global.NOVALUEONREQUIREDFIELD
				});
			}
			if ($.trim(description) == "") {
				errors.push({
					field : $("#" + params.description).attr("fieldName"),
					errorType : $.Cmdbuild.global.NOVALUEONREQUIREDFIELD
				});
			}
			if ($.trim(category) == "") {
				errors.push({
					field : $("#" + params.category).attr("fieldName"),
					errorType : $.Cmdbuild.global.NOVALUEONREQUIREDFIELD
				});
			}
			if (errors.length > 0) {
				$.Cmdbuild.errorsManager.popupOnRequestFields(errors);
				return;
			}

			// create reader
			var reader = new FileReader();
			reader.readAsText(file);
			reader.onload = function(e) {
				var formData = new FormData();
				formData.append("file", file);
				formData.append("attachment", new Blob([JSON.stringify({
					_description : description,
					_category : category
				})], {
					type : "application/json"
				}));
				$("#" + params.description).val("");
				$("#" + params.category).val("");
				$("#" + params.id).val("");
				backend.addRow({
					form : param.form,
					row : {
						description : description,
						category : category,
						document: filepath,
						formdata : formData
					}
				});
			};
		},
		fieldRefresh: function(param) {
			var form = $.Cmdbuild.dataModel.forms[param.form];

			form.refreshField(param);
		},
		reportParamsSend: function(param) {
			try {
				param.type = "form";
				if (! $.Cmdbuild.dataModel.change(param)) {
					return;
				}
				var reportParams = $.Cmdbuild.dataModel.getValues(param.form);
				var p = {};
				for (var key in reportParams) {
					var name = key.replace(/_PT_/g, ".");
					name = name.replace(/_SP_/g, " ");
					p[name] = reportParams[key];
				}
				var backend = $.Cmdbuild.utilities.getBackend(param.backend);
				backend.showReport(p);
			}
			catch (e) {
				$("#" + param.dialog).dialog("close");
				$.Cmdbuild.errorsManager.log(e);
				$.Cmdbuild.errorsManager.popup(e);
			}
			$("#" + param.dialog).dialog("close");
		},
		requestReportParameters: function(param) {
			var backend = $.Cmdbuild.utilities.getBackend(param.backend);
			$.Cmdbuild.utilities.setAttributesInteractivity(param.attributes);
			for (var i = 0; i < param.attributes.length; i++) {
				param.attributes[i].name = param.attributes[i].name.replace(/[.]/g, "_PT_");
				param.attributes[i].name = param.attributes[i].name.replace(/[ ]/g, "_SP_");
			}
			backend.data.attributes = param.attributes;
			$.Cmdbuild.standard.commands.navigate(param);
		},
		printReport: function(param) {
			var xmlForm = $.Cmdbuild.elementsManager.getElement(param.form);
			var p = {};
			$.Cmdbuild.elementsManager.getParams(xmlForm, p);
			var backend = $.Cmdbuild.utilities.getBackend(p.backend);
			var id = $.Cmdbuild.dataModel.getValue(param.form, "id");
			var type = $.Cmdbuild.dataModel.getValue(param.form, "type");
			param.typeReport = type;
			param.id = id;
			param.form = param.formDialog;
			backend.requestReport(param);
		},
		saveattachments : function(param) {
			this.dialogClose(param);
		},

		/*
		 * @param {Object} params - command configuration
		 * @param {string} params.form - id of the grid to refresh
		 */
		deleteMail: function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			var formObject = $.Cmdbuild.dataModel.forms[paramActualized.formData];
			var backend = formObject.getBackend();
			backend.deleteMail(paramActualized);
			this.refreshGrid(paramActualized, function() {});
		},
		replyMail : function(params) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(params);
			$.Cmdbuild.standard.commands.navigate({
				form: paramActualized.navigationForm,
				dialog: paramActualized.navigationDialog
			});				
		},
		refreshMail : function(params) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(params);
			var formDataObject = $.Cmdbuild.dataModel.forms[paramActualized.formData];
			var data = $.Cmdbuild.dataModel.getValues(paramActualized.form);
			var widgets = formDataObject.getBackend().widgets;
			$.Cmdbuild.dataModel.detachObserving();
			$.Cmdbuild.widgets.refreshTemplate(paramActualized.formData, widgets, data.fromTemplate);
			$.Cmdbuild.dataModel.attachObserving();
			var me = this;
			this.TM = setTimeout(function() { me.refreshGrid(paramActualized, function() {
			}); }, 500);
		},
		/*
		 * @param {Object} params - command configuration
		 * @param {string} params.form - id of the grid to refresh
		 */
		refreshGrid : function(params, callback, callbackScope) {
			var table = $('#' + params.form).DataTable();
			table.ajax.reload(function(json) {
				if (callback) {
					callback.apply(callbackScope);
				}
			});
		},
		
		/*
		 * @param {Object} params - command configuration
		 * @param {string} params.widgetName - Email widget Name
		 */
		synchronizeMails : function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			var objectForm = $.Cmdbuild.dataModel.getWidgetWindow(paramActualized.widgetName);
			var checked = $.Cmdbuild.standard.grid.getChecked(paramActualized.form);
			var type = objectForm.data.type;
			var templates = objectForm.data.templates;
			var widgetName = $.Cmdbuild.standard.widgetDiv.widgetName(type);
			if ($.Cmdbuild.widgets[widgetName] && $.Cmdbuild.widgets[widgetName].setSynchronization) {
				$.Cmdbuild.widgets[widgetName].setSynchronization(templates, checked, true, false);
			}
			for (var i = 0; i < templates.length; i++) {
				var template = templates[i];
				if (checked[template._id] == "true") {
					$.Cmdbuild.widgets.ManageEmail.cqlResolve(paramActualized.formData, template, function() {
						
					}, this);
				}
			}
			$("#" + paramActualized.dialog).dialog( "close" );
		},		
		/*
		 * @param {Object} params - command configuration
		 * @param {string} params.widgetName - Email widget Name
		 */
		noSynchronizeMails : function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			var objectForm = $.Cmdbuild.dataModel.getWidgetWindow(paramActualized.widgetName);
			var templates = objectForm.data.templates;
			var type = objectForm.data.type;
			var widgetName = $.Cmdbuild.standard.widgetDiv.widgetName(type);
			if ($.Cmdbuild.widgets[widgetName] && $.Cmdbuild.widgets[widgetName].setSynchronization) {
				$.Cmdbuild.widgets[widgetName].setSynchronization(templates, {}, false, false);
			}
			$("#" + paramActualized.dialog).dialog( "close" );
		},		
	
		// generate attachment URL for download
		downloadAttachment : function(params) {
			var base_url;
			var _type = $.Cmdbuild.dataModel.resolveVariable(params.className);
			var _id = $.Cmdbuild.dataModel.resolveVariable(params.cardId);
			var attachment_name = $.Cmdbuild.dataModel
					.resolveVariable(params.attachmentName);
			var attachment_id = $.Cmdbuild.dataModel
					.resolveVariable(params.attachmentId);

			if ($.Cmdbuild.dataModel.isAClass(_type)) {
				base_url = $.Cmdbuild.global.getApiUrl() + "classes/" + _type
						+ "/cards/" + _id;
			} else if ($.Cmdbuild.dataModel.isAProcess(_type)) {
				base_url = $.Cmdbuild.global.getApiUrl() + "processes/" + _type
						+ "/instances/" + _id;
			}

			var url = base_url + "/attachments/" + attachment_id + "/"
					+ encodeURIComponent(attachment_name) + "?CMDBuild-Authorization="
					+ $.Cmdbuild.authentication.getAuthenticationToken();

			window.location.replace(url);
		}
	};
	$.Cmdbuild.standard.commands = commands;
}) (jQuery);