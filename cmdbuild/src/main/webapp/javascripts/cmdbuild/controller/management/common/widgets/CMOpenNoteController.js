(function() {

	Ext.require([
		'CMDBuild.core.Message',
		'CMDBuild.proxy.widget.OpenNote'
	]);

	Ext.define("CMDBuild.controller.management.workflow.CMNoteController", {
		extend: "CMDBuild.controller.management.classes.CMNoteController",

		mixins: {
			wfStateDelegate: "CMDBuild.state.CMWorkflowStateDelegate"
		},

		constructor: function() {
			this.callParent(arguments);
			_CMWFState.addDelegate(this);
		},

		// override to deny to add a note to a new process
		disableTheTabBeforeCardSelection: function(processInstance) {
			if (!processInstance
					|| processInstance.isNew()) {
				return true;
			} else {
				return CMDBuild.Utils.isSimpleTable(processInstance.getClassId());
			}
		},

		// override: return always false because we want that
		// in process the user could modify the notes only if
		// there is an openNote extended attribute defined.
		updateViewPrivilegesForCard: function(card) {
			this.view.updateWritePrivileges(false);
		},

		// is not possible to save the note if the
		// activity is not already saved
		beforeSave: function(card) {
			var isNew = isANewActivity(card);

			if (isNew) {
				CMDBuild.core.Message.error(CMDBuild.Translation.common.failure,
					CMDBuild.Translation.management.modworkflow.extattrs.notes.must_save_to_modify,
					popup = false);
			}

			return !isNew;
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onSaveNoteClick: function () {
			var form = this.view.getForm();
			var params = this._getSaveParams();

			if (form.isValid() && this.beforeSave(this.card)) {
				CMDBuild.proxy.widget.OpenNote.update({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse['response'];

						this.view.disableModify(enableToolbar = true);

						this.syncSavedNoteWithModel(
							this.card,
							this.view.syncForms(),
							decodedResponse
						);

						this.fireEvent(this.CMEVENTS.noteWasSaved);
					}
				});
			}
		},

		// override to retrieve the activityInstance
		// from the WorkflowState
		_getSaveParams: function() {
			var params = {};
			var form = this.view.getForm();
 			var pi = _CMWFState.getProcessInstance();
			var ai = _CMWFState.getActivityInstance();

			if (pi && ai) {
				params['Notes'] = this.view.getForm().getValues()['Notes'];
				params.classId = pi.getClassId();
				params.cardId = pi.getId();
				params.activityInstanceId = ai.getId();
				params.advance = false;
				params.ww = "{}";
				params.attributes = Ext.encode(form.getValues());
 			}

			return params;
		},

		// override
		// don't use the card passed by superclass success
		// after save request. Use the processInstance instead
		syncSavedNoteWithModel: function(card, noteValue, processData) {
			var pi = _CMWFState.getProcessInstance();

			if (pi) {
				pi.setNotes(noteValue);
				pi.updateBeginDate(processData);
			}
		},

		// override
		// use the process instance
		onCancelNoteClick: function() {
			this.onProcessInstanceChange(_CMWFState.getProcessInstance());
		},

		// wfStateDelegate
		onProcessClassRefChange: function() {
			this.view.disable();
		},

		onProcessInstanceChange: function(pi) {
			this.updateView(pi);
			this.view.loadCard(new CMDBuild.DummyModel(pi.getValues()));

			if (this.disableTheTabBeforeCardSelection(pi)) {
				this.view.disable();
			} else {
				this.view.enable();
			}
		},

		onActivityInstanceChange: Ext.emptyFn,

		// override
		buildCardModuleStateDelegate: Ext.emptyFn,
		onEntryTypeSelected: Ext.emptyFn,
		onCardSelected: Ext.emptyFn
	});

	Ext.define("CMDBuild.controller.management.common.widgets.CMOpenNoteController", {

		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		constructor: function(view, supercontroller, widget, templateResolver, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			try {
				this.view.updateWritePrivileges(this.card.hasWritePrivileges());
			} catch (e) {
				this.view.updateWritePrivileges(false);
			}

			this.view.disableModify();

			this.mon(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		destroy: function() {
			this.mun(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		onBackToActivityButtonClick: function() {
			try {
				this.view.hideBackButton();
				this.view.disableModify();
				this.ownerController.activateFirstTab();
			} catch (e) {
				CMDBuild.log.error("Something went wrong displaying the Activity panel");
			}
		},

		// override
		// if set the value to the html field when is disabled
		// it display null, so set again the value to it the first time that is shown
		beforeActiveView: function() {
			if (!this._alreadyOpened) {
				this.view.loadCard(new CMDBuild.DummyModel(_CMWFState.getProcessInstance().getValues()));
				this._alreadyOpened = true;
			}
		}
	});

	function isANewActivity() {
		var ai = _CMWFState.getActivityInstance();

		if (ai) {
			return  ai.isNew();
		} else {
			return false;
		}
	}

})();