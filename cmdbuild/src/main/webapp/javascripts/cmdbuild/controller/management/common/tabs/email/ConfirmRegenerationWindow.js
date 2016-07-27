(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.ConfirmRegenerationWindow', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.controller.management.common.tabs.email.Email',
			'CMDBuild.controller.management.common.widgets.CMWidgetController',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Grid}
		 */
		gridDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfirmRegenerationWindowClearStore',
			'onConfirmRegenerationWindowConfirmButtonClick',
			'onConfirmRegenerationWindowShow'
		],

		/**
		 * @property {Ext.data.Store}
		 */
		gridStore: undefined,

		/**
		 * @property {Array}
		 */
		recordsCouldBeRegenerated: undefined,

		/**
		 * @property {Array}
		 */
		templatesCouldBeRegenerated: undefined,

		/**
		 * @property {Mixed} emailWindows
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.tabs.email.Email} configurationObject.parentDelegate
		 * @param {CMDBuild.controller.management.common.tabs.email.Grid} configurationObject.gridDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.ConfirmRegenerationWindow', {
				delegate: this
			});

			this.gridStore = this.view.grid.getStore();
		},

		/**
		 * @param {Mixed} record
		 */
		addRecordToArray: function (record) {
			this.recordsCouldBeRegenerated.push(record);
		},

		/**
		 * @param {CMDBuild.model.common.tabs.email.Template} template
		 */
		addTemplateToArray: function (template) {
			this.templatesCouldBeRegenerated.push(template);
		},

		beforeShow: function () {
			this.gridStore.loadData(this.recordsCouldBeRegenerated);

			this.regenerateAndAddTemplateToStore(this.templatesCouldBeRegenerated);

			this.view.grid.getSelectionModel().deselectAll();

			this.show();
		},

		onConfirmRegenerationWindowClearStore: function () {
			this.gridStore.removeAll();
		},

		/**
		 * Regenerates only selected records
		 */
		onConfirmRegenerationWindowConfirmButtonClick: function () {
			this.cmfg('tabEmailRegenerateSelectedEmails', this.view.grid.getSelectionModel().getSelection());

			this.view.hide();
		},

		onConfirmRegenerationWindowShow: function () {
			this.reset();
		},

		/**
		 * Evaluates conditions and adds template to store
		 *
		 * @param {Array} templatesToAdd
		 *
		 * {conditionEvalTrafficLightArray} Implements a trafficLight functionality to manage multiple asynchronous calls and have a global callback
		 * to hide loadMask only at real end of calls.
		 */
		regenerateAndAddTemplateToStore: function (templatesToAdd) {
			var me = this;
			var conditionEvalTrafficLightArray = [];

			if (Ext.isArray(templatesToAdd) && !Ext.isEmpty(templatesToAdd)) {
				CMDBuild.core.LoadMask.show();
				Ext.Array.forEach(templatesToAdd, function (template, i, allTemplates) {

					if (!Ext.Object.isEmpty(template)) {
						CMDBuild.controller.management.common.tabs.email.Email.trafficLightSlotBuild(template, conditionEvalTrafficLightArray);

						var xaVars = Ext.apply({}, template.getData(), template.get(CMDBuild.core.constants.Proxy.VARIABLES));

						var templateResolver = new CMDBuild.Management.TemplateResolver({
							clientForm: me.parentDelegate.clientForm,
							xaVars: xaVars,
							serverVars: CMDBuild.controller.management.common.widgets.CMWidgetController.getTemplateResolverServerVars()
						});

						templateResolver.resolveTemplates({
							attributes: Ext.Object.getKeys(xaVars),
							callback: function (values, ctx) {
								emailObject = Ext.create('CMDBuild.model.common.tabs.email.Email', values);
								emailObject.set(CMDBuild.core.constants.Proxy.REFERENCE, me.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ID));
								emailObject.set(CMDBuild.core.constants.Proxy.TEMPLATE, template.get(CMDBuild.core.constants.Proxy.KEY));

								me.gridStore.add(emailObject);

								if (
									CMDBuild.controller.management.common.tabs.email.Email.trafficLightArrayCheck(template, conditionEvalTrafficLightArray)
									|| Ext.isEmpty(conditionEvalTrafficLightArray)
								) {
									CMDBuild.core.LoadMask.hide();
									me.show();
								}
							}
						});
					}
				}, this);
			}
		},

		reset: function () {
			this.recordsCouldBeRegenerated = [];
			this.templatesCouldBeRegenerated = [];
		},

		show: function () {
			if(
				!Ext.isEmpty(this.view)
				&& (
					this.gridStore.count() > 0
					|| !Ext.isEmpty(this.templatesCouldBeRegenerated)
				)
			) {
				this.view.show();
			}
		}
	});

})();
