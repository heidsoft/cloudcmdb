(function () {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.AdvancedTable', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask',
			'CMDBuild.model.localization.advancedTable.TreeStore'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.Localization}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLocalizationAdvancedTableBuildColumns',
			'onLocalizationAdvancedTableBuildStore',
			'onLocalizationAdvancedTableCollapseAll',
			'onLocalizationAdvancedTableExpandAll',
			'onLocalizationAdvancedTableShow',
			'onLocalizationAdvancedTableTabCreation'
		],

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.section.Class}
		 */
		sectionControllerClass: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.section.Domain}
		 */
		sectionControllerDomain: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.section.Filter}
		 */
		sectionControllerFilter: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.section.Lookup}
		 */
		sectionControllerLookup: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.section.Menu}
		 */
		sectionControllerMenu: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.section.Report}
		 */
		sectionControllerReport: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.section.View}
		 */
		sectionControllerView: undefined,

		/**
		 * @property {CMDBuild.controller.administration.localization.advancedTable.section.Workflow}
		 */
		sectionControllerWorkflow: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.advancedTable.AdvancedTableView}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localization.Localization} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localization.advancedTable.AdvancedTableView', { delegate: this });

			// Build tabs (in display order)
			this.sectionControllerClass = Ext.create('CMDBuild.controller.administration.localization.advancedTable.section.Class', { parentDelegate: this });
			this.sectionControllerWorkflow = Ext.create('CMDBuild.controller.administration.localization.advancedTable.section.Workflow', { parentDelegate: this });
			this.sectionControllerDomain = Ext.create('CMDBuild.controller.administration.localization.advancedTable.section.Domain', { parentDelegate: this });
			this.sectionControllerView = Ext.create('CMDBuild.controller.administration.localization.advancedTable.section.View', { parentDelegate: this });
			this.sectionControllerFilter = Ext.create('CMDBuild.controller.administration.localization.advancedTable.section.Filter', { parentDelegate: this });
			this.sectionControllerLookup = Ext.create('CMDBuild.controller.administration.localization.advancedTable.section.Lookup', { parentDelegate: this });
			this.sectionControllerReport = Ext.create('CMDBuild.controller.administration.localization.advancedTable.section.Report', { parentDelegate: this });
			this.sectionControllerMenu = Ext.create('CMDBuild.controller.administration.localization.advancedTable.section.Menu', { parentDelegate: this });

			this.view.setActiveTab(0);
			this.view.getActiveTab().fireEvent('show'); // Manual show event fire
		},

		/**
		 * @param {CMDBuild.model.localization.Localization} languageObject
		 *
		 * @returns {Ext.grid.column.Column} or null
		 *
		 * @private
		 */
		buildColumn: function (languageObject) {
			if (!Ext.isEmpty(languageObject))
				return Ext.create('Ext.grid.column.Column', {
					dataIndex: languageObject.get(CMDBuild.core.constants.Proxy.TAG),
					languageDescription: languageObject.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
					text: '<img style="margin: 0px 5px 0px 0px;" src="images/icons/flags/'
						+ languageObject.get(CMDBuild.core.constants.Proxy.TAG) + '.png" /> '
						+ languageObject.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
					width: 300,
					sortable: false,
					draggable: false,

					editor: { xtype: 'textfield' }
				});

			return null;
		},

		/**
		 * Build TreePanel columns only with languages with translations
		 *
		 * @returns {Array} columnsArray
		 */
		onLocalizationAdvancedTableBuildColumns: function () {
			var columnsArray = [
				{
					xtype: 'treecolumn',
					dataIndex: CMDBuild.core.constants.Proxy.TEXT,
					text: CMDBuild.Translation.translationObject,
					width: 300,
//					locked: true, // There is a performance issue in ExtJs 4.2.0 without locked columns all is fine
					sortable: false,
					draggable: false,
					hideable: false
				},
				{
					dataIndex: CMDBuild.core.constants.Proxy.DEFAULT,
					text: CMDBuild.Translation.defaultTranslation,
					width: 300,
					sortable: false,
					draggable: false,
					hideable: false
				}
			];
			var languagesColumnsArray = [];

			Ext.Object.each(CMDBuild.configuration.localization.getEnabledLanguages(), function (key, value, myself) {
				languagesColumnsArray.push(this.buildColumn(value));
			}, this);

			// Sort languages columns with alphabetical sort order
			CMDBuild.core.Utils.objectArraySort(languagesColumnsArray, 'languageDescription');

			return Ext.Array.push(columnsArray, languagesColumnsArray);
		},

		/**
		 * @returns {Ext.data.TreeStore}
		 */
		onLocalizationAdvancedTableBuildStore: function () {
			return Ext.create('Ext.data.TreeStore', {
				model: 'CMDBuild.model.localization.advancedTable.TreeStore',

				root: {
					text: 'ROOT',
					expanded: true,
					children: []
				}
			});
		},

		/**
		 * @param {CMDBuild.view.administration.localization.common.AdvancedTableGrid}
		 */
		onLocalizationAdvancedTableCollapseAll: function (gridPanel) {
			CMDBuild.core.LoadMask.show();

			Ext.Function.defer(function () { // HACK: to fix collapseAll bug that don't displays loeadMask
				gridPanel.collapseAll(function () {
					CMDBuild.core.LoadMask.hide();
				});
			}, 100, this);
		},

		/**
		 * @param {CMDBuild.view.administration.localization.common.AdvancedTableGrid}
		 */
		onLocalizationAdvancedTableExpandAll: function (gridPanel) {
			CMDBuild.core.LoadMask.show();

			Ext.Function.defer(function () { // HACK: to fix expandAll bug that don't displays loeadMask
				gridPanel.expandAll(function () {
					CMDBuild.core.LoadMask.hide();
				});
			}, 100, this);
		},

		/**
		 * Check if there are enabled languages and show warning popup
		 */
		onLocalizationAdvancedTableShow: function () {
			if (Ext.isEmpty(CMDBuild.configuration.localization.get(CMDBuild.core.constants.Proxy.ENABLED_LANGUAGES)))
				CMDBuild.core.Message.warning(null, CMDBuild.Translation.warnings.thereAreNoEnabledLanguages);
		},

		/**
		 * @param {Mixed} panel
		 */
		onLocalizationAdvancedTableTabCreation: function (panel) {
			if (!Ext.isEmpty(panel))
				this.view.add(panel);
		}
	});

})();
