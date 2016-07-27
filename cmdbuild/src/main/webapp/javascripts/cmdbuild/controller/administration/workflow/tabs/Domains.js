(function () {

	Ext.define('CMDBuild.controller.administration.workflow.tabs.Domains', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.domain.Domain'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.Workflow}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWorkflowTabDomainsAddButtonClick',
			'onWorkflowTabDomainsAddWorkflowButtonClick',
			'onWorkflowTabDomainsIncludeInheritedCheck',
			'onWorkflowTabDomainsItemDoubleClick',
			'onWorkflowTabDomainsModifyButtonClick',
			'onWorkflowTabDomainsRemoveButtonClick',
			'onWorkflowTabDomainsRowSelect',
			'onWorkflowTabDomainsShow',
			'onWorkflowTabDomainsStoreLoad',
			'workflowTabDomainsInit = workflowTabInit'
		],

		/**
		 * @property {CMDBuild.view.administration.workflow.tabs.domains.GridPanel}
		 */
		grid: undefined,

		/**
		 * Just the grid subset of domain properties, not a full domain object
		 *
		 * @property {CMDBuild.model.workflow.tabs.domains.Grid}
		 *
		 * @private
		 */
		selectedDomain: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.workflow.tabs.domains.DomainsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.workflow.Workflow} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.workflow.tabs.domains.DomainsView', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;

			this.grid.getStore().on('load', function (store, records, successful, eOpts) {
				this.cmfg('onWorkflowTabDomainsIncludeInheritedCheck');
			}, this);
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsAddButtonClick: function () {
			this.cmfg('mainViewportAccordionDeselect', CMDBuild.core.constants.ModuleIdentifiers.getDomain());

			this.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain()).getView().on('storeload', function (accordion, eOpts) {
				this.cmfg('mainViewportModuleControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain()).cmfg('onDomainAddButtonClick');
			}, this, { single: true });

			this.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain()).disableSelection = true;
			this.cmfg('mainViewportAccordionControllerExpand', CMDBuild.core.constants.ModuleIdentifiers.getDomain());
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsAddWorkflowButtonClick: function () {
			this.view.disable();
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsIncludeInheritedCheck: function () {
			if (this.view.includeInheritedCheckbox.getValue()) {
				this.grid.getStore().clearFilter();
			} else {
				this.grid.getStore().filterBy(function (record) {
					return !record.get(CMDBuild.core.constants.Proxy.INHERITED);
				});
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsItemDoubleClick: function () {
			if (!this.selectedDomainIsEmpty()) {
				this.cmfg('mainViewportAccordionDeselect', CMDBuild.core.constants.ModuleIdentifiers.getDomain());
				this.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain()).disableStoreLoad = true;
				this.cmfg('mainViewportAccordionControllerExpand', CMDBuild.core.constants.ModuleIdentifiers.getDomain());
				this.cmfg('mainViewportAccordionControllerUpdateStore', {
					identifier: CMDBuild.core.constants.ModuleIdentifiers.getDomain(),
					nodeIdToSelect: this.selectedDomainGet(CMDBuild.core.constants.Proxy.ID_DOMAIN)
				});
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsModifyButtonClick: function () {
			if (!this.selectedDomainIsEmpty()) {
				this.cmfg('mainViewportAccordionDeselect', CMDBuild.core.constants.ModuleIdentifiers.getDomain());
				this.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain()).disableStoreLoad = true;
				this.cmfg('mainViewportAccordionControllerExpand', CMDBuild.core.constants.ModuleIdentifiers.getDomain());

				this.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain()).getView().on('storeload', function (accordion, eOpts) {
					Ext.Function.createDelayed(function () {
						this.cmfg('mainViewportModuleControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain()).cmfg('onDomainModifyButtonClick');
					}, 100, this)();
				}, this, { single: true });

				this.cmfg('mainViewportAccordionControllerUpdateStore', {
					identifier: CMDBuild.core.constants.ModuleIdentifiers.getDomain(),
					nodeIdToSelect: this.selectedDomainGet(CMDBuild.core.constants.Proxy.ID_DOMAIN)
				});
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsRemoveButtonClick: function () {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsRowSelect: function () {
			if (this.grid.getSelectionModel().hasSelection())
				this.selectedDomainSet({ value: this.grid.getSelectionModel().getSelection()[0] });

			this.view.setDisabledTopBar(!this.grid.getSelectionModel().hasSelection());
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsShow: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);

			this.grid.getStore().load({
				params: params,
				scope: this,
				callback: function (records, operation, success) {
					if (!this.grid.getSelectionModel().hasSelection())
						this.grid.getSelectionModel().select(0, true);

					this.cmfg('onWorkflowTabDomainsRowSelect');
				}
			});
		},

		/**
		 * Translations of grid records domain's class name to description
		 *
		 * @returns {Void}
		 */
		onWorkflowTabDomainsStoreLoad: function () {
			if (!Ext.isEmpty(this.grid.getStore().getRange()) && Ext.isArray(this.grid.getStore().getRange())) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.Classes.readAll({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES] || [];

						Ext.Array.forEach(this.grid.getStore().getRange(), function (gridRecord, i, allGridRecords) {
							var foundClassObject = undefined;

							// Translate class1 name to description
							foundClassObject = Ext.Array.findBy(decodedResponse, function (record, i) {
								return gridRecord.get('class1') == record[CMDBuild.core.constants.Proxy.NAME];
							}, this);

							if (!Ext.isEmpty(foundClassObject))
								gridRecord.set('class1', foundClassObject[CMDBuild.core.constants.Proxy.TEXT]);

							// Translate class2 name to description
							foundClassObject = Ext.Array.findBy(decodedResponse, function (record, i) {
								return gridRecord.get('class2') == record[CMDBuild.core.constants.Proxy.NAME];
							}, this);

							if (!Ext.isEmpty(foundClassObject))
								gridRecord.set('class2', foundClassObject[CMDBuild.core.constants.Proxy.TEXT]);

							gridRecord.commit();
						}, this);
					}
				});
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		removeItem: function () {
			if (!this.selectedDomainIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.DOMAIN_NAME] = this.selectedDomainGet(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.domain.Domain.remove({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.selectedDomainReset();

						this.cmfg('onWorkflowTabDomainsShow');
					}
				});
			}
		},

		// SelectedDomain property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			selectedDomainGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 *
			 * @private
			 */
			selectedDomainIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			selectedDomainReset: function (parameters) {
				this.propertyManageReset('selectedDomain');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			selectedDomainSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.workflow.tabs.domains.Grid';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * Enable/Disable tab on workflow selection
		 *
		 * @returns {Void}
		 */
		workflowTabDomainsInit: function () {
			this.view.setDisabled(
				this.cmfg('workflowSelectedWorkflowIsEmpty')
				|| this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.TABLE_TYPE) == CMDBuild.core.constants.Global.getTableTypeSimpleTable()
			);
		}
	});

})();
