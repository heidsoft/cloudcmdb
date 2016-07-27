(function() {

	Ext.define("CMDBuild.controller.management.dashboard.CMModDashboardController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: {
			columnController: "CMDBuild.controller.common.CMDashboardColumnController"
		},

		constructor: function() {
			this.callParent(arguments);
			if (this.view) {
				this.view.setDelegate(this);
			}

			this.dashboard = null;
		},

		onViewOnFront: function(selection) {
			if (selection && typeof selection.get == "function") {
				var idPropertyName = Ext.isEmpty(selection.get(CMDBuild.core.constants.Proxy.ENTITY_ID)) ? CMDBuild.core.constants.Proxy.ID : CMDBuild.core.constants.Proxy.ENTITY_ID;

				this.dashboard = _CMCache.getDashboardById(selection.get(idPropertyName));
				this.view.buildDashboardColumns(this.dashboard);

				// History record save
				if (!Ext.isEmpty(selection))
					CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
						moduleId: this.view.cmName,
						entryType: {
							description: selection.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
							id: selection.get(CMDBuild.core.constants.Proxy.ID),
							object: selection
						}
					});
			}
		}
	});
})();