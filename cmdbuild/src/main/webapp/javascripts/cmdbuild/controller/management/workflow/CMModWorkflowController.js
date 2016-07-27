(function () {

	var FLOW_STATUS_CODE = 'FlowStatus_code';
	var STATE_VALUE_OPEN = 'open.running';

	Ext.define('CMDBuild.controller.management.workflow.CMModWorkflowController', {
		extend: 'CMDBuild.controller.management.common.CMModController',

		mixins: {
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @property {CMDBuild.controller.management.workflow.CMActivityPanelController}
		 */
		activityPanelController: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.CMActivityAttachmentsController}
		 */
		attachmentsController: undefined,

		/**
		 * @property {Ext.data.Model}
		 */
		card: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.tabs.Email}
		 */
		controllerTabEmail: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.CMWorkflowHistoryPanelController}
		 */
		controllerTabHistory: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.CMNoteController}
		 */
		noteController: undefined,

		/**
		 * @property {CMDBuild.controller.management.workflow.CMActivityRelationsController}
		 */
		relationsController: undefined,

		/**
		 * @property {Array}
		 */
		subControllers: [],

		/**
		 * @property {CMDBuild.view.management.workflow.CMModProcess}
		 */
		view: undefined,

		/**
		 * @property {Object}
		 */
		widgetsController: {},

		constructor: function() {
			this.callParent(arguments);

			_CMWFState.addDelegate(this);

			this.mon(this.view, this.view.CMEVENTS.addButtonClick, this.onAddCardButtonClick, this);
		},

		/**
		 * Build all controllers and adds view in tab panel with controller declaration order
		 *
		 * @override
		 */
		buildSubControllers: function() {
			// Tabs controllers
			this.buildTabControllerActivity();
			this.buildTabControllerNote();
			this.buildTabControllerRelations();
			this.buildTabControllerHistory();
			this.buildTabControllerEmail();
			this.buildTabControllerAttachments();

			// Generic controllers
			buildGridController(this);
		},

		buildTabControllerActivity: function() {
			var view = this.view.getActivityPanel();
			var widgetControllerManager = new CMDBuild.controller.management.common.CMWidgetManagerController(this.view.getWidgetManager());

			this.activityPanelController = new CMDBuild.controller.management.workflow.CMActivityPanelController(view, this, widgetControllerManager);

			this.mon(this.activityPanelController, this.activityPanelController.CMEVENTS.cardRemoved, function() {
				this.gridController.onCardDeleted();
			}, this);

			this.subControllers.push(this.activityPanelController);

			this.view.cardTabPanel.acutalPanel.add(view); // Add panel to view
		},

		buildTabControllerAttachments: function() {
			var view = this.view.getAttachmentsPanel();

			if (!Ext.isEmpty(view)) {
				this.attachmentsController = new CMDBuild.controller.management.workflow.CMActivityAttachmentsController(view, this);

				this.subControllers.push(this.attachmentsController);

				this.view.cardTabPanel.acutalPanel.add(view);
			}
		},

		buildTabControllerEmail: function() {
			if (!CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_EMAIL_TAB)) {
				this.controllerTabEmail = Ext.create('CMDBuild.controller.management.workflow.tabs.Email', { parentDelegate: this });

				this.subControllers.push(this.controllerTabEmail);

				this.view.cardTabPanel.emailPanel = this.controllerTabEmail.getView(); // Creates tabPanel object

				this.view.cardTabPanel.acutalPanel.add(this.controllerTabEmail.getView());
			}
		},

		buildTabControllerHistory: function() {
			if (!CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_HISTORY_TAB)) {
				this.controllerTabHistory = Ext.create('CMDBuild.controller.management.workflow.tabs.History', { parentDelegate: this });

				this.subControllers.push(this.controllerTabHistory);

				this.view.cardTabPanel.cardHistoryPanel = this.controllerTabHistory.getView(); // Creates tabPanel object

				this.view.cardTabPanel.acutalPanel.add(this.controllerTabHistory.getView());
			}
		},

		buildTabControllerNote: function() {
			var view = this.view.getNotesPanel();

			if (!Ext.isEmpty(view)) {
				this.noteController = new CMDBuild.controller.management.workflow.CMNoteController(view, this);

				this.subControllers.push(this.noteController);

				this.view.cardTabPanel.acutalPanel.add(view);
			}
		},

		buildTabControllerRelations: function() {
			var view = this.view.getRelationsPanel();

			if (!Ext.isEmpty(view)) {
				this.relationsController = new CMDBuild.controller.management.workflow.CMActivityRelationsController(view, this);

				this.subControllers.push(this.relationsController);

				this.view.cardTabPanel.acutalPanel.add(view);
			}
		},

		// wfStateDelegate
		onActivityInstanceChange: function(activityInstance) {
			this.view.updateDocPanel(activityInstance.getInstructions());

			if (!activityInstance.nullObject
					&& activityInstance.isNew()) {
				_CMUIState.onlyFormIfFullScreen();
			}
		},

		/**
		 * Forward onAbortCardClick event to email tab controller
		 */
		onAbortCardClick: function() {
			if (!Ext.isEmpty(this.controllerTabEmail) && Ext.isFunction(this.controllerTabEmail.onAbortCardClick))
				this.controllerTabEmail.onAbortCardClick();
		},

		/**
		 * Forward onAddCardButtonClick event to email tab controller
		 */
		onAddCardButtonClick: function() {
			if (!Ext.isEmpty(this.controllerTabEmail) && Ext.isFunction(this.controllerTabEmail.onAddCardButtonClick))
				this.controllerTabEmail.onAddCardButtonClick();
		},

		/**
		 * @deprecated
		 */
		onCardChanged: function(card) {
			_deprecated('onCardChanged', this);

			var me = this;

			if (card == null) {
				return;
			}

			if (isStateOpen(card) || card._cmNew) {
				me.view.updateDocPanel(card);
				if (card._cmNew) {
					// I could be in a tab different to the first one,
					// but to edit a new card is necessary to have the editing form.
					// So I force the view to go on the ActivityTab

					me.view.showActivityPanel();
				}
			} else {
				me.view.updateDocPanel(null);
			}

			me.callParent(arguments);
		},

		/**
		 * Forward onModifyCardClick event to email tab controller
		 */
		onModifyCardClick: function() {
			if (!Ext.isEmpty(this.controllerTabEmail) && Ext.isFunction(this.controllerTabEmail.onModifyCardClick))
				this.controllerTabEmail.onModifyCardClick();
		},

		/**
		 * Forward onSaveCardClick event to email tab controller
		 */
		onSaveCardClick: function() {
			if (!Ext.isEmpty(this.controllerTabEmail) && Ext.isFunction(this.controllerTabEmail.onSaveCardClick))
				this.controllerTabEmail.onSaveCardClick();
		},

		/**
		 * Is called when the view is bring to front from the main viewport.
		 * Set the entry type of the _CMWFState instead to store it inside this controller
		 *
		 * @param {Number} entryTypeId
		 * @param {Object} danglingCard
		 * @param {String} filter
		 *
		 * @override
		 */
		setEntryType: function(entryTypeId, danglingCard, filter) {
			var entryType = _CMCache.getEntryTypeById(entryTypeId);

			if (!Ext.isEmpty(danglingCard)) {
				if (!Ext.isEmpty(danglingCard.flowStatus))
					this.gridController.view.setStatus(danglingCard.flowStatus);

				if (!Ext.isEmpty(danglingCard.activateFirstTab))
					this.view.cardTabPanel.activeTabSet(danglingCard.activateFirstTab);
			}

			_CMWFState.setProcessClassRef(entryType, danglingCard, false, filter);

			this.view.updateTitleForEntry(entryType);

			_CMUIState.onlyGridIfFullScreen();
		}
	});

	function isStateOpen(card) {
		var data = card.raw;
		return data[FLOW_STATUS_CODE] == STATE_VALUE_OPEN;
	}

	function buildGridController(me) {
		me.grid = me.view.cardGrid;

		me.gridController = new CMDBuild.controller.management.workflow.CMActivityGridController(me.view.cardGrid);
		me.mon(me.gridController, me.gridController.CMEVENTS.cardSelected, me.onCardSelected, me);
		me.mon(me.gridController, me.gridController.CMEVENTS.load, onGridLoad, me);

		me.grid.mon(me.gridController, 'itemdblclick', function() {
			me.activityPanelController.onModifyCardClick();
			_CMUIState.onlyFormIfFullScreen();
		}, me);

		me.activityPanelController.setDelegate(me.gridController);

		me.subControllers.push(me.gridController);
	}

	function onGridLoad(args) {
		// args[1] is the array with the loaded records
		// so, if there are no records clear the view
		if (args[1] && args[1].length == 0) {
			this.activityPanelController.clearView();
		}
	}

})();