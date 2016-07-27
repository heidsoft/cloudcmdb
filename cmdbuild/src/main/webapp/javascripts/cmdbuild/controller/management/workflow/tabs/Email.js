(function () {

	/**
	 * Workflow specific email tab controller
	 */
	Ext.define('CMDBuild.controller.management.workflow.tabs.Email', {
		extend: 'CMDBuild.controller.management.common.tabs.email.Email',

		mixins: {
			observable: 'Ext.util.Observable',
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.management.workflow.CMModWorkflowController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.tabs.Email}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.workflow.CMModWorkflowController} configObject.parentDelegate
		 */
		constructor: function(configObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.workflow.tabs.Email', { delegate: this });
			this.view.add(this.grid);

			_CMWFState.addDelegate(this);
		},

		/**
		 * It's the change of processIstance step (called activity)
		 *
		 * @param {CMDBuild.model.CMActivityInstance} activityIstance
		 */
		onActivityInstanceChange: Ext.emptyFn,

		onAbortCardClick: function() {
			this.cmfg('tabEmailEditModeSet', false);
			this.cmfg('tabEmailConfigurationReset');
		},

		/**
		 * Enable action shouldn't be needed but on addCardButtoClick is fired also onProcessInstanceChange event
		 *
		 * @override
		 */
		onAddCardButtonClick: function() {
			this.callParent(arguments);

			// Reset selected entity, regenerate email and load store
			this.cmfg('tabEmailSelectedEntityInit', {
				scope: this,
				callbackFunction: function(options, success, response) {
					this.cmfg('tabEmailRegenerateAllEmailsSet', true);
					this.forceRegenerationSet(true);
					this.cmfg('onTabEmailPanelShow');
				}
			});
		},

		/**
		 * Equals to onEntryTypeSelected in classes
		 *
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		onProcessClassRefChange: function(entryType) {
			this.cmfg('tabEmailEditModeSet', false);
		},

		/**
		 * Equals to onCardSelected in classes.
		 * N.B. Enable/Disable email tab is done by widget configurationSet
		 *
		 * @param {CMDBuild.model.CMProcessInstance} processInstance
		 */
		onProcessInstanceChange: function(processInstance) {
			if (!Ext.isEmpty(processInstance) && processInstance.isStateOpen()) {
				if (!processInstance.isNew())
					this.parentDelegate.activityPanelController.ensureEditPanel(); // Creates editPanel with relative form fields

				this.cmfg('tabEmailConfigurationReset');
				this.cmfg('tabEmailSelectedEntitySet', {
					selectedEntity: processInstance,
					scope: this,
					callbackFunction: function(options, success, response) {
						this.cmfg('tabEmailRegenerateAllEmailsSet', processInstance.isNew());
						this.forceRegenerationSet(processInstance.isNew());
						this.cmfg('onTabEmailPanelShow');
					}
				});
				this.cmfg('tabEmailEditModeSet', processInstance.isNew()); // Enable/Disable tab based on model new state to separate create/view mode
				this.controllerGrid.cmfg('tabEmailGridUiStateSet');
			} else { // We have a closed process instance
				this.cmfg('tabEmailSelectedEntitySet', {
					selectedEntity: processInstance,
					scope: this,
					callbackFunction: function(options, success, response) {
						this.cmfg('onTabEmailPanelShow');
					}
				});
			}
		},

		/**
		 * Launch regeneration on save button click and send all draft emails
		 */
		onSaveCardClick: function() {
			if (!this.grid.getStore().isLoading()) {
				this.cmfg('tabEmailRegenerateAllEmailsSet', true);
				this.cmfg('onTabEmailPanelShow');
			}
		},

		/**
		 * @override
		 */
		onTabEmailPanelShow: function() {
			if (this.view.isVisible()) {
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
						},
						section: {
							description: this.view.title,
							object: this.view
						}
					});
			}

			this.callParent(arguments);
		}
	});

})();