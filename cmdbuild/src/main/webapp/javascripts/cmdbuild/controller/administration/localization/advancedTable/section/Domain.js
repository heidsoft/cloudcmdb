(function () {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.section.Domain', {
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
		sectionId: CMDBuild.core.constants.Proxy.DOMAIN,

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
				title: CMDBuild.Translation.domains
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onLocalizationAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		}
	});

})();
