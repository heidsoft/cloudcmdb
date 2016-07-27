(function() {

	Ext.require([
		'CMDBuild.controller.management.classes.StaticsController',
		'CMDBuild.core.constants.Global',
		'CMDBuild.core.Message',
		'CMDBuild.proxy.widget.Workflow',
		'CMDBuild.proxy.workflow.Activity'
	]);

	var ERROR_TEMPLATE = "<p class=\"{0}\">{1}</p>";
	var FILTER_FIELD = "_SystemFieldFilter";

	Ext.define("CMDBuild.controller.management.common.widgets.CMWorkflowControllerWidgetReader",{
		getType: function(w) {return "Activity";},
		getCode: function(w) {return w.workflowName;},
		getPreset: function(w) {return w.preset;},
		getFilter: function(w) {return w.filter;}
	});

	Ext.define("CMDBuild.controller.management.common.widgets.CMWorkflowController", {
		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMWorkflow.WIDGET_NAME
		},
		processId: null,
		processClassName: null,

		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			this.ownerController = ownerController;
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.widgetReader = new CMDBuild.controller.management.common.widgets.CMWorkflowControllerWidgetReader();
			var widgetManager = new CMDBuild.view.management.common.widgets.CMWidgetManagerPopup(this.view);
			this.widgetControllerManager = new CMDBuild.controller.management.common.CMWidgetManagerControllerPopup(widgetManager);
			view.setDelegate(this);
			this.widgetControllerManager.setDelegate(this);

			this.mon(this.view, this.view.CMEVENTS.saveButtonClick, onSaveCardClick, this);
			this.mon(this.view, this.view.CMEVENTS.advanceButtonClick, onAdvanceCardClick, this);

			var me = this;
			var name = this.widgetReader.getCode(this.widgetConf);
			if (name) {
				this.typedWidgetConf = this.widgetConf;
				this.view.hideComboPanel();
				this.widgetType = "name";
				var card = _CMCache.getEntryTypeByName(name);
				if (card && card.data) {
					_CMCache.getAttributeList(card.data.id, function(attributes) {
						me.cardAttributes = attributes;
					});
				}
				this.presets = this.widgetReader.getPreset(this.typedWidgetConf);
			}
			else {
				this.view.showComboPanel();
				this.widgetType = "cql";
			}
		},

		chargeComboFiels: function() {
			var me = this;
			this.filter = this.widgetReader.getFilter(this.widgetConf);
			var filterTemplateResolver = new CMDBuild.Management.TemplateResolver({
				xaVars: {},
				clientForm: this.clientForm,
				serverVars: this.getTemplateResolverServerVars()
			});
			filterTemplateResolver.xaVars[FILTER_FIELD] = this.filter;
			filterTemplateResolver.resolveTemplates({
				attributes: [FILTER_FIELD],
				callback: function(response) {
					var callParams = filterTemplateResolver.buildCQLQueryParameters(response[FILTER_FIELD]);
					var filter = Ext.encode({
						CQL: callParams.CQL
					});

					CMDBuild.proxy.widget.Workflow.readWorkflowByFilter({
						params: {
							className: CMDBuild.core.constants.Global.getRootNameWorkflows(),
							limit: 1000,
							start: 0,
							filter: filter
						},
						loadMask: false,
						scope: this,
						success: function (response, options, decodedResponse){
							var data = decodedResponse.rows;
							me.view.clearComboValues(data);
							me.view.loadComboValues(data);
							// process server response here
						}
					});
				}
			});
		},

		getData: function() {
			var out = null;
			if (!this.readOnly) {
				out = {};
				out["output"] = {
					id : this.processId,
					className: this.processClassName
				};
			}

			return out;
		},

		ensureEditPanel: function() {
		},
		onWidgetButtonClick: function(widget) {
			this.widgetControllerManager.onWidgetButtonClick(widget);
		},
		beforeActiveView: function() {
			if (this.widgetType == "cql") {
				this.chargeComboFiels();
				this.view.comboPanel.clearCombo();
			}
			this.view.clearWindow();
			if (this.widgetType == "name") {
				configureActivityForm(this);
			}
		},
		changeWorkflow: function(name) {
			var me = this;
			this.view.clearWindow();
			this.typedWidgetConf = {
					workflowName : name,
					presets: {}
			};
			var card = _CMCache.getEntryTypeByName(name);
			if (card && card.data) {
				_CMCache.getAttributeList(card.data.id, function(attributes) {
					me.cardAttributes = attributes;
				});
			}
			this.presets = this.widgetReader.getPreset(this.typedWidgetConf);
			configureActivityForm(this);
		}
	});

	function configureActivityForm(me) {
		if (!me.widgetReader) {
			return;
		}

		me.view.setLoading(true);
		var name = me.widgetReader.getCode(me.typedWidgetConf);
		var card = _CMCache.getEntryTypeByName(name);

		CMDBuild.proxy.widget.Workflow.readStartActivity({
			params: {
				classId: card.data.id
			},
			scope: me,
			success: function (response, options, decodedResponse) {
				me.attributes = CMDBuild.controller.management.workflow.StaticsController.filterAttributesInStep(me.cardAttributes, decodedResponse.response.variables);
				me.view.configureForm(me.attributes);
				me.templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: me.clientForm,
					xaVars: me.presets,
					serverVars: me.getTemplateResolverServerVars()
				});

				resolveTemplate(me);
				me.widgetControllerManager.buildControllers(decodedResponse.response.widgets, card);
				me.view.getWidgetButtonsPanel().editMode();
				me.view.setLoading(false);
				me.configured = true;
			}
		});
	}

	function resolveTemplate(me) {
		me.templateResolver.resolveTemplates({
			attributes: Ext.Object.getKeys(me.presets),
			callback: function(o) {
				me.view.fillFormValues(o);
			}
		});
	}

	function onAdvanceCardClick() {
		saveWorkflow(this, true);
	}

	function onSaveCardClick() {
		saveWorkflow(this, false);
	}

	function saveWorkflow(me, advance) {
		var form = me.view.formPanel.getForm();
		var valid = advance ? validate(me) : true;
		if (valid) {
			CMDBuild.core.LoadMask.show();
			var requestParams = {};
			var name = me.widgetReader.getCode(me.typedWidgetConf);
			var card = _CMCache.getEntryTypeByName(name);
			requestParams.classId = card.data.id;
			requestParams.attributes = Ext.JSON.encode(form.getValues());
			requestParams.advance = advance;
			requestParams.activityInstanceId = undefined;
			requestParams.ww = Ext.JSON.encode(me.widgetControllerManager.getData(advance));
			CMDBuild.proxy.workflow.Activity.update({
				params: requestParams,
				scope : me,
				clientValidation: true, //to force the save request
				loadMask: false,
				callback: function(operation, success, response) {
					CMDBuild.core.LoadMask.hide();
				},
				success: function(operation, requestConfiguration, decodedResponse) {
					me.processId = decodedResponse.response.Id;
					var processClassId = decodedResponse.response.IdClass;
					var entity =_CMCache.getEntryTypeById(processClassId);
					me.processClassName = entity.get("name");
				}
			});
			me.ownerController.hideWidgetsContainer();
		} else {
			_debug("There are no processInstance to save");
		}
	}

	function validateForm(me) {
		var form = me.view.formPanel.getForm();
		var invalidAttributes = CMDBuild.controller.management.classes.StaticsController.getInvalidAttributeAsHTML(form);

		if (invalidAttributes != null) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.core.constants.Global.getErrorMsgCss(), CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.core.Message.error(null, msg + invalidAttributes, false);

			return false;
		} else {
			return true;
		}
	}

	function validate(me) {
		var valid = validateForm(me);//,
		wrongWidgets = me.widgetControllerManager.getWrongWFAsHTML();

		if (wrongWidgets != null) {
			valid = false;
			var msg = Ext.String.format(ERROR_TEMPLATE
					, CMDBuild.core.constants.Global.getErrorMsgCss()
					, CMDBuild.Translation.errors.invalid_extended_attributes);
			CMDBuild.core.Message.error(null, msg + wrongWidgets, popup = false);
		}

		return valid;
	}

})();
