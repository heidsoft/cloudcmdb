(function() {

	Ext.require([
		'CMDBuild.core.constants.Global',
		'CMDBuild.core.constants.Proxy',
		'CMDBuild.core.Message',
		'CMDBuild.proxy.workflow.Activity',
		'CMDBuild.proxy.workflow.Workflow'
	]);

	var ERROR_TEMPLATE = "<p class=\"{0}\">{1}</p>";

	Ext.define("CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate", {
		onCardSaved: Ext.emptyFn
	});

	Ext.define("CMDBuild.controller.management.workflow.CMActivityPanelController", {
		extend: "CMDBuild.controller.management.classes.CMCardPanelController",

		mixins: {
			wfStateDelegate: "CMDBuild.state.CMWorkflowStateDelegate"
		},

		/**
		 * @property {Object}
		 */
		lastSelectedActivityInstance: undefined,

		/**
		 * @property {Object}
		 */
		lastSelectedProcessInstance: undefined,

		/**
		 * @param {CMDBuild.view.management.workflow.CMActivityPanel} view
		 * @param {CMDBuild.controller.management.workflow.CMModWorkflowController} supercontroller
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} widgetControllerManager
		 * @param {???} delegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function(view, supercontroller, widgetControllerManager, delegate) {
			this.callParent(arguments);

			// this flag is used to define if the user has click on the
			// save or advance button. The difference is that on save
			// the widgets do nothing and the saved activity goes in display mode.
			// On advance, otherwise, the widgets do the react (save their state) and
			// the saved activity lies in edit mode, to continue the data entry.
			this.isAdvance = false;

			this.mon(this.view, this.view.CMEVENTS.advanceCardButtonClick, this.onAdvanceCardButtonClick, this);
			this.mon(this.view, this.view.CMEVENTS.editModeDidAcitvate, this.onEditMode, this);
			this.mon(this.view, this.view.CMEVENTS.checkEditability, onCheckEditability, this);
			this.mon(this.view, this.view.CMEVENTS.displayModeDidActivate, onDisplayMode, this);

			_CMWFState.addDelegate(this);
			this.setDelegate(delegate || new CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate());
		},

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, "CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate");
			this.delegate = d;
		},

		// wfStateDelegate
		onProcessInstanceChange: function(processInstance) {
			var me = this;

			this.unlock();
			this.clearView();

			this.lastSelectedProcessInstance = processInstance; // Used to unlock last locked card

			if (
				processInstance != null // is null on abort of a new process
				&& processInstance.isStateCompleted()
			) {
				this.loadFields(processInstance.getClassId(), function loadFieldsCb() {
					me.fillFormWithProcessInstanceData(processInstance);
				});
			} else {
				enableStopButtonIfUserCanUseIt(this, processInstance);
			}

			// History record save
			if (!Ext.isEmpty(_CMWFState.getProcessClassRef()) && !Ext.isEmpty( _CMWFState.getProcessInstance()))
				CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
					moduleId: 'workflow',
					entryType: {
						description: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.TEXT),
						id: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.ID),
						object: _CMWFState.getProcessClassRef()
					},
					item: {
						description: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.TEXT),
						id: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.ID),
						object: _CMWFState.getProcessInstance()
					}
				});
		},

		// wfStateDelegate
		/**
		 * @param {Object} activityInstance
		 *
		 * @returns {Void}
		 */
		onActivityInstanceChange: function (activityInstance) {
			var me = this;
			var activityMetadata = activityInstance.data[CMDBuild.core.constants.Proxy.METADATA];
			var processInstance = _CMWFState.getProcessInstance();

			this.unlock();

			this.lastSelectedActivityInstance = activityInstance; // Used to unlock last locked card

			// Reduce the layouts work while fill the panel and build the widgets.
			// Resume it at the end and force a layout update
			Ext.suspendLayouts();

			if (!activityInstance.nullObject && activityInstance.isNew()) {
				/*
				 * I could be in a tab different to the first one, but to edit a new card is necessary to have the editing form.
				 * So I force the view to go on the ActivityTab
				 *
				 * Do it here instead of in the CMModWorkflowController because it must be done before all operation over the form for rendering issues
				 */
				this.superController.view.showActivityPanel();
			}

			this.view.updateInfo(activityInstance.getPerformerName(), activityInstance.getDescription());

			// At first update the widget because they could depends to the form. The Template resolver starts when the form goes in edit mode,
			// so the widgets must be already ready to done them works
			var updateWidget = processInstance.isStateOpen() || activityInstance.isNew();
			if (updateWidget) {
				this.widgetControllerManager.buildControllers(activityInstance);
			} else {
				this.widgetControllerManager.buildControllers(null);
			}

			// Resume the layouts here because the form already suspend the layouts automatically
			Ext.resumeLayouts();

			this.view.doLayout();

			// Load always the fields
			this.loadFields(processInstance.getClassId(), function (attributes) {
				// Fill always the process to trigger the template resolver of filtered references
				me.fillFormWithProcessInstanceData(processInstance);
				manageEditability(me, activityInstance, processInstance);

				// Manage metadata (SelectedAttributesGroup)
				if (Ext.isArray(activityMetadata) && !Ext.isEmpty(activityMetadata)) {
					Ext.Array.each(activityMetadata, function (metadataObject, i, allMetadataObjects) {
						if (Ext.isObject(metadataObject) && !Ext.Object.isEmpty(metadataObject))
							switch (metadataObject[CMDBuild.core.constants.Proxy.NAME]) {
								case CMDBuild.core.constants.Proxy.WORKFLOW_METADATA_SELECTED_ATTRIBUTES_GROUP: {
									var preselectedTab = null;
									var markerAttribute = Ext.Array.findBy(attributes, function (item, index) {
										return item[CMDBuild.core.constants.Proxy.NAME] == metadataObject[CMDBuild.core.constants.Proxy.VALUE];
									}, this);

									if (!Ext.isEmpty(markerAttribute)) {
										this.view.form.tabPanel.items.each(function (item, index, len) {
											if (item.title == markerAttribute[CMDBuild.core.constants.Proxy.GROUP])
												preselectedTab = item;
										}, this);

										if (!Ext.isEmpty(preselectedTab))
											this.view.form.tabPanel.setActiveTab(preselectedTab);
									}
								} break;
							}
					}, me);
				}
			});
		},

		/**
		 * @override
		 */
		onModifyCardClick: function() {
			var processInstance = _CMWFState.getProcessInstance();
			var activityInstance = _CMWFState.getActivityInstance();

			this.isAdvance = false;

			if (
				!Ext.isEmpty(processInstance)
				&& processInstance.isStateOpen()
				&& !Ext.isEmpty(activityInstance)
				&& activityInstance.isWritable()
			) {
				this.lock(function() {
					this.view.editMode();
				}, this);

				this.callParent(arguments);
			}
		},

		/**
		 * @param {Function} success
		 * @param {Object} scope
		 */
		lock: function(success, scope) {
			if (
				CMDBuild.configuration.instance.get('enableCardLock') // TODO: use proxy constants
				&& _CMWFState.getActivityInstance()
				&& _CMWFState.getProcessInstance()
			) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVITY_INSTANCE_ID] = _CMWFState.getActivityInstance().data[CMDBuild.core.constants.Proxy.ID];
				params[CMDBuild.core.constants.Proxy.PROCESS_INSTANCE_ID] = _CMWFState.getProcessInstance().data[CMDBuild.core.constants.Proxy.ID];

				CMDBuild.proxy.workflow.Activity.lock({
					params: params,
					loadMask: false,
					scope: scope,
					success: success
				});
			} else {
				Ext.callback(success, scope);
			}
		},

		unlock: function() {
			if (
				CMDBuild.configuration.instance.get('enableCardLock') // TODO: use proxy constants
				&& !Ext.isEmpty(this.lastSelectedActivityInstance)
				&& this.view.isInEditing()
			) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVITY_INSTANCE_ID] = this.lastSelectedActivityInstance.data[CMDBuild.core.constants.Proxy.ID];
				params[CMDBuild.core.constants.Proxy.PROCESS_INSTANCE_ID] = this.lastSelectedProcessInstance.data[CMDBuild.core.constants.Proxy.ID];

				CMDBuild.proxy.workflow.Activity.unlock({
					params: params,
					loadMask: false
				});
			}
		},

		// override
		onRemoveCardClick: function() {
			var me = this;
			Ext.Msg.confirm(
				CMDBuild.Translation.management.modworkflow.abort_card, // title
				CMDBuild.Translation.management.modworkflow.abort_card_confirm, // message
				confirmCB);

			function confirmCB(btn) {
				if (btn != 'yes') {
					return;
				} else {
					deleteActivity.call(me);
				}
			}
		},

		// override
		onSaveCardClick: function() {
			this.isAdvance = false;

			this.superController.onSaveCardClick(); // Forward save event

			this.widgetControllerManager.waitForBusyWidgets(save, this); // Check for busy widgets also on save
		},

		// override
		onAbortCardClick: function() {
			this.isAdvance = false;
			var activityInstance = _CMWFState.getActivityInstance();

			if (activityInstance && activityInstance.isNew()) {
				this.onProcessInstanceChange();
			} else {
				this.onActivityInstanceChange(activityInstance);
			}

			this.callParent(arguments); // Forward abort event

			_CMUIState.onlyGridIfFullScreen();
		},

		onAdvanceCardButtonClick: function() {
			this.isAdvance = true;
			this.widgetControllerManager.waitForBusyWidgets(save, this); // Check for busy widgets on advance
		},

		clearView: function() {
			this.view.clear();
		},

		// override
		loadFields: function(entryTypeId, cb) {
			var me = this;
			var activityInstance = _CMWFState.getActivityInstance();
			var processInstance = _CMWFState.getProcessInstance();
			var variables = [];

			if (activityInstance) {
				variables = activityInstance.getVariables();
			}

			function onAttributesLoaded(attributes) {
				// Filter attributes to show, if we have a closed process who all attributes
				if (
					activityInstance.isNew()
					|| processInstance.isStateOpen()
					|| processInstance.isStateSuspended()
				) {
					attributes = CMDBuild.controller.management.workflow.StaticsController.filterAttributesInStep(attributes, variables);
				}

				me.view.fillForm(attributes, editMode = false);

				if (cb) {
					cb(attributes);
				}
			}

			if (entryTypeId) {
				_CMCache.getAttributeList(entryTypeId, onAttributesLoaded);
			} else {
				onAttributesLoaded([]);
			}
		},

		fillFormWithProcessInstanceData: function(processInstance) {
			if (processInstance != null) {
				this.view.loadCard(processInstance.asDummyModel());
				this.view.displayModeForNotEditableCard();

				if (!processInstance.isNew() && processInstance.isStateOpen())
					this.ensureEditPanel(); // Creates editPanel with relative form fields
			}
		},

		/**
		 * @override
		 */
		onShowGraphClick: function() {
			var pi = _CMWFState.getProcessInstance();

			Ext.create('CMDBuild.controller.management.common.graph.Graph', {
				parentDelegate: this,
				classId: pi.getClassId(),
				cardId: pi.getId()
			});
		},

		onEditMode: function() {
			this.editMode = true;

			if (this.widgetControllerManager) {
				this.widgetControllerManager.onCardGoesInEdit();
			}
		},

		// TODO: Needs some refactoring
		// override
		doFormSubmit: Ext.emptyFn,
		onSaveSuccess: Ext.emptyFn,
		onEntryTypeSelected: Ext.emptyFn,
		onCardSelected: Ext.emptyFn,
		buildCardModuleStateDelegate: Ext.emptyFn
	});

	function deleteActivity() {
		var me = this;
		var processInstance = _CMWFState.getProcessInstance();

		if (!processInstance && !processInstance.isNew())
			return;

		this.clearView();

		CMDBuild.proxy.workflow.Activity.abort({
			params: {
				classId: processInstance.getClassId(),
				cardId: processInstance.getId()
			},
			loadMask: false,
			scope: this,
			success: function (response, options, decodedResponse) {
				me.fireEvent(me.CMEVENTS.cardRemoved);
			}
		});
	}


	function onCheckEditability(cb) {
		var me = this;
		var pi = _CMWFState.getProcessInstance();

		// for a new process do nothing
		if (pi.isNew()) {
			cb();
			return;
		}

		var requestParams = {
			processInstanceId: pi.getId(),
			className: pi.get("className"),
			beginDate: pi.get("beginDateAsLong")
		}

		CMDBuild.proxy.workflow.Workflow.isPorcessUpdated({
			params: requestParams,
			loadMask: false,
			success: function(operation, requestConfiguration, decodedResponse) {
				var isUpdated = decodedResponse.response.updated;
				if (isUpdated) {
					cb();
				}
			}
		});
	}

	function onDisplayMode() {
		this.editMode = false;
	}

	function save() {
		var me = this,
			requestParams = {},
			pi = _CMWFState.getProcessInstance(),
			ai = _CMWFState.getActivityInstance(),
			valid;

		if (pi) {
			var formValues = this.view.getValues();
			// used server side to be sure to update
			// the last version of the process
			formValues.beginDate = pi.get("beginDateAsLong");

			requestParams = {
				classId: pi.getClassId(),
				attributes: Ext.JSON.encode(formValues),
				advance: me.isAdvance
			};

			if (pi.getId()) {
				requestParams.cardId = pi.getId();
			}

			if (ai && ai.getId) {
				requestParams.activityInstanceId = ai.getId();
			}

			// Business rule: Someone want the validation
			// only if advance and not if save only
			valid = requestParams.advance ? validate(me) : true;

			if (valid) {

				requestParams["ww"] = Ext.JSON.encode(this.widgetControllerManager.getData(me.advance));
				_debug("save the process with params", requestParams);

				CMDBuild.core.LoadMask.show();
				CMDBuild.proxy.workflow.Activity.update({
					params: requestParams,
					scope : this,
					clientValidation: this.isAdvance, //to force the save request
					loadMask: false,
					callback: function(operation, success, response) {
						CMDBuild.core.LoadMask.hide();
					},
					failure: function(response, options, decodedResponse) {
						this.delegate.reload(); // Reload store also on failure
					},
					success: function(operation, requestConfiguration, decodedResponse) {
						var savedCardId = decodedResponse.response.Id;
						this.view.displayMode();

						// to enable the editing for the
						// right processInstance
						if (me.isAdvance) {
							me.idToAdvance = savedCardId;
						} else {
							_CMUIState.onlyGridIfFullScreen();
						}

						this.delegate.onCardSaved(savedCardId, decodedResponse.response.flowStatus);
					}
				});
			}
		} else {
			_error('there are no processInstance to save', this);
		}
	}

	function validateForm(me) {
		var form = me.view.getForm();
		var invalidAttributes = CMDBuild.controller.management.workflow.StaticsController.getInvalidAttributeAsHTML(form);

		if (invalidAttributes != null) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.core.constants.Global.getErrorMsgCss(), CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.core.Message.error(null, msg + invalidAttributes, false);

			return false;
		} else {
			return true;
		}
	}

	function validate(me) {
		var valid = validateForm(me),
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

	function manageEditability(me, activityInstance, processInstance) {
		if (activityInstance.isNew()) {
			me.view.editMode();

			return;
		}

		if (
			!processInstance.isStateOpen()
			|| activityInstance.isNullObject()
			|| !activityInstance.isWritable()
		) {
			me.view.displayModeForNotEditableCard();

			enableStopButtonIfUserCanUseIt(me, processInstance);

			return;
		}

		if (
			me.isAdvance
			&& processInstance.getId() == me.idToAdvance
		) {
			me.view.editMode();
			me.superController.onModifyCardClick(); // Call modify event for email tab

			// Lock card on advance action
			me.lock(function() {
				me.view.editMode();
			});

			me.isAdvance = false;

			return;
		}

		me.view.displayMode(true);

		enableStopButtonIfUserCanUseIt(me, processInstance);
	}

	function enableStopButtonIfUserCanUseIt(me, processInstance) {
		me.view.disableStopButton();
		if (!processInstance) {
			return;
		}

		var processClassId = processInstance.getClassId();
		if (processClassId) {
			var processClass = _CMCache.getEntryTypeById(processClassId);
			if (processClass) {
				var theUserCanStopTheProcess = processClass.isUserStoppable() || CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.IS_ADMINISTRATOR);
				var theProcessIsNotAlreadyTerminated = processInstance.isStateOpen() || processInstance.isStateSuspended();

				if (theUserCanStopTheProcess && theProcessIsNotAlreadyTerminated) {
					me.view.enableStopButton();
				}
			}
		}
	}

})();