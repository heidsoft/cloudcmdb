(function () {

	Ext.define('CMDBuild.controller.management.widget.ManageEmail', {
		extend: 'CMDBuild.controller.common.abstract.Widget',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		parentDelegate: undefined,

		/**
		 * Widget before save callback loop object
		 *
		 * @property {Object}
		 */
		beforeSaveCallbackObject: undefined,

		/**
		 * @property {CMDBuild.model.CMActivityInstance or Ext.data.Model}
		 */
		card: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetManageEmailBeforeSave = onBeforeSave',
			'widgetConfigurationGet = widgetManageEmailConfigurationGet',
			'widgetManageEmailBeforeActiveView = beforeActiveView',
			'widgetManageEmailEditMode = onEditMode',
			'widgetManageEmailGetData = getData',
			'widgetManageEmailIsValid = isValid'
		],

		/**
		 * Disable delegate apply to avoid to set this class as delegate of email tab
		 *
		 * @cfg {Boolean}
		 *
		 * @override
		 */
		enableDelegateApply: false,

		/**
		 * @property {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		tabDelegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.EmailView}
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: 'CMDBuild.model.widget.manageEmail.Configuration',

		/**
		 * @param {CMDBuild.view.management.workflow.tabs.Email} configurationObject.view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} configurationObject.parentDelegate
		 * @param {Object} configurationObject.widgetConfiguration
		 * @param {Ext.form.Basic} configurationObject.clientForm
		 * @param {CMDBuild.model.CMActivityInstance or Ext.data.Model} configurationObject.card
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			// Shorthands
			this.tabDelegate = this.view.delegate;

			this.tabDelegate.cmfg('tabEmailConfigurationSet', { value: this.cmfg('widgetManageEmailConfigurationGet').getData() });
			this.tabDelegate.cmfg('tabEmailConfigurationSet', {
				propertyName: CMDBuild.core.constants.Proxy.TEMPLATES,
				value: this.cmfg('widgetManageEmailConfigurationGet', CMDBuild.core.constants.Proxy.TEMPLATES)
			});

			// Build bottom toolbar
			this.buildBottomToolbar();
		},

		/**
		 * Create event manager and show toolbar
		 *
		 * @private
		 */
		buildBottomToolbar: function () {
			this.tabDelegate.getView().on('show', this.showEventManager, this);

			// Border manage
			if (!this.tabDelegate.grid.hasCls('cmdb-border-bottom'))
				this.tabDelegate.grid.addCls('cmdb-border-bottom');

			this.tabDelegate.getView().getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).removeAll();
			this.tabDelegate.getView().getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).add(
				Ext.create('CMDBuild.core.buttons.text.Back', {
					scope: this,

					handler: function (button, e) {
						this.parentDelegate.activateFirstTab();
					}
				})
			);
			this.tabDelegate.getView().getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).show();
		},

		/**
		 * Delete event and hide toolbar on widget destroy
		 */
		destroy: function () {
			this.tabDelegate.getView().un('show', this.showEventManager, this);
			this.tabDelegate.getView().getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).hide();

			// Border manage
			if (this.tabDelegate.grid.hasCls('cmdb-border-bottom'))
				this.tabDelegate.grid.removeCls('cmdb-border-bottom');
		},

		/**
		 * @param {CMDBuild.view.management.common.tabs.email.EmailView} panel
		 * @param {Object} eOpts
		 *
		 * @private
		 */
		showEventManager: function (panel, eOpts) {
			var cardWidgetTypes = [];

			if (Ext.isArray(this.parentDelegate.takeWidgetFromCard(this.card)))
				Ext.Array.forEach(this.parentDelegate.takeWidgetFromCard(this.card), function (widgetObject, i, allWidgetObjects) {
					if (!Ext.isEmpty(widgetObject))
						cardWidgetTypes.push(widgetObject[CMDBuild.core.constants.Proxy.TYPE]);
				}, this);

			if (Ext.Array.contains(cardWidgetTypes, this.cmfg('widgetManageEmailConfigurationGet', CMDBuild.core.constants.Proxy.TYPE)))
				this.tabDelegate.getView().getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).show();
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onWidgetManageEmailBeforeSave: function (parameters) {
			this.tabDelegate.cmfg('tabEmailGlobalLoadMaskSet', false);

			// Setup end-point callback to close widget save callback loop
			var callbackDefinitionObject = {};
			callbackDefinitionObject[CMDBuild.core.constants.Proxy.SCOPE] = this;
			callbackDefinitionObject[CMDBuild.core.constants.Proxy.FUNCTION] =  function () {
				this.onBeforeSave(parameters); // CallParent alias

				this.tabDelegate.cmfg('tabEmailRegenerationEndPointCallbackReset'); // Reset callback function
			};

			this.tabDelegate.cmfg('tabEmailRegenerationEndPointCallbackSet', { value: callbackDefinitionObject });

			this.tabDelegate.cmfg('tabEmailRegenerateAllEmailsSet', true);
			this.tabDelegate.controllerGrid.cmfg('tabEmailGridStoreLoad');
		},

		/**
		 * @override
		 */
		widgetManageEmailBeforeActiveView: function () {
			this.beforeActiveView(); // Custom callParent
		},

		/**
		 * @override
		 */
		widgetManageEmailEditMode: function () {
			this.tabDelegate.cmfg('tabEmailEditModeSet', true);

			this.onEditMode(); // Custom callParent
		},

		/**
		 * @returns {Object} output
		 *
		 * @override
		 */
		widgetManageEmailGetData: function () {
			var output = {};
			output[CMDBuild.core.constants.Proxy.OUTPUT] = this.tabDelegate.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ID);

			return output;
		},

		/**
		 * @returns {Boolean}
		 */
		widgetManageEmailIsValid: function () {
			if (
				Ext.isBoolean(this.cmfg('widgetManageEmailConfigurationGet', CMDBuild.core.constants.Proxy.REQUIRED))
				&& this.cmfg('widgetManageEmailConfigurationGet', CMDBuild.core.constants.Proxy.REQUIRED)
			) {
				return this.tabDelegate.controllerGrid.cmfg('tabEmailGridDraftEmailsIsEmpty');
			}

			return true;
		}
	});

})();
