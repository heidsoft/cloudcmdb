(function () {

	/**
	 * @link CMDBuild.controller.common.field.filter.advanced.window.Window
	 */
	Ext.define('CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditor', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.filter.advanced.Manager}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onEntryTypeGridFilterAdvancedFilterEditorAbortButtonClick',
			'onEntryTypeGridFilterAdvancedFilterEditorApplyButtonClick',
			'onEntryTypeGridFilterAdvancedFilterEditorSaveAndApplyButtonClick',
			'onEntryTypeGridFilterAdvancedFilterEditorViewHide',
			'onEntryTypeGridFilterAdvancedFilterEditorViewShow'
		],

		/**
		 * @property {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.Attributes}
		 */
		controllerAttributes: undefined,

		/**
		 * @property {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.Relations}
		 */
		controllerRelations: undefined,

		/**
		 * @property {CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditorWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.entryTypeGrid.filter.advanced.Manager} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditorWindow', { delegate: this });

			// Build sub controllers
			this.controllerAttributes = Ext.create('CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.Attributes', { parentDelegate: this });
			this.controllerRelations = Ext.create('CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.Relations', { parentDelegate: this });

			this.view.wrapper.removeAll();
			this.view.wrapper.add([
				this.controllerAttributes.getView(),
				this.controllerRelations.getView()
			]);

			this.manageActiveTabSet(true);
		},

		/**
		 * @param {Boolean} disableFireEvent
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		manageActiveTabSet: function (disableFireEvent) {
			if (!this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty', [CMDBuild.core.constants.Proxy.CONFIGURATION, CMDBuild.core.constants.Proxy.RELATION]))
				return this.view.wrapper.setActiveTab(1);

			this.view.wrapper.setActiveTab(0);

			return disableFireEvent ? null : this.view.wrapper.getActiveTab().fireEvent('show'); // Manual show event fire
		},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorAbortButtonClick: function () {
			this.view.close();
		},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorApplyButtonClick: function () {
			var filterModelObject = this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet').getData();
			filterModelObject[CMDBuild.core.constants.Proxy.CONFIGURATION] = Ext.Object.merge(
				this.controllerAttributes.cmfg('entryTypeGridFilterAdvancedFilterEditorAttributesDataGet'),
				this.controllerRelations.cmfg('entryTypeGridFilterAdvancedFilterEditorRelationsDataGet')
			);

			// If new filter model
			if (Ext.isEmpty(filterModelObject[CMDBuild.core.constants.Proxy.ID])) {
				filterModelObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = CMDBuild.Translation.newSearchFilter;
				filterModelObject[CMDBuild.core.constants.Proxy.NAME] = CMDBuild.Translation.newSearchFilter;
			}

			this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterSet', { value: filterModelObject });

			// Save filter in local storage
			if (this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty', CMDBuild.core.constants.Proxy.ID))
				this.cmfg('entryTypeGridFilterAdvancedLocalFilterAdd', this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet'));

			this.cmfg('onEntryTypeGridFilterAdvancedFilterEditorAbortButtonClick'); // Close filter editor view
			this.cmfg('entryTypeGridFilterAdvancedManagerViewClose'); // Close manager view
			this.cmfg('onEntryTypeGridFilterAdvancedFilterSelect', this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet')); // Apply filter to store
		},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorSaveAndApplyButtonClick: function () {
			var filterModelObject = this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet').getData();
			filterModelObject[CMDBuild.core.constants.Proxy.CONFIGURATION] = Ext.Object.merge(
				this.controllerAttributes.cmfg('entryTypeGridFilterAdvancedFilterEditorAttributesDataGet'),
				this.controllerRelations.cmfg('entryTypeGridFilterAdvancedFilterEditorRelationsDataGet')
			);

			this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterSet', { value: filterModelObject });
			this.cmfg('entryTypeGridFilterAdvancedManagerSave', { enableApply: true });
		},

		/**
		 * Reset manage toggle button on window hide with no filters in manager store
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorViewHide: function () {
			if (this.cmfg('entryTypeGridFilterAdvancedManagerStoreIsEmpty'))
				this.cmfg('entryTypeGridFilterAdvancedManageToggleStateReset');
		},

		/**
		 * Show event forwarder method
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorViewShow: function () {
			this.setViewTitle([
				this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet', CMDBuild.core.constants.Proxy.NAME),
				this.cmfg('entryTypeGridFilterAdvancedEntryTypeGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
			]);

			this.manageActiveTabSet();
		}
	});

})();
