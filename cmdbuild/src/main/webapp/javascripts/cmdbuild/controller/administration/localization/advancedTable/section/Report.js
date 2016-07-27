(function () {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.section.Report', {
		extend: 'CMDBuild.controller.administration.localization.advancedTable.section.Abstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.localization.Localization'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.constants.Proxy.REPORT,

		/**
		 * @property {CMDBuild.view.administration.localization.common.AdvancedTableGrid}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.advancedTable.SectionPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localization.advancedTable.SectionPanel', {
				delegate: this,
				hideActiveOnlyCheckbox: true,
				title: CMDBuild.Translation.report
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onLocalizationAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		}
	});

})();
