(function() {

	Ext.require('CMDBuild.proxy.workflow.Workflow');

	Ext.define("CMDBuild.view.management.workflow.CMModProcess", {
		extend: "CMDBuild.view.management.classes.CMModCard",

		whitMap: false,

		initComponent: function() {
			this.callParent(arguments);
			_CMUtils.forwardMethods(this, this.cardTabPanel, [
				"getActivityPanel",
				"getRelationsPanel",
				"getHistoryPanel",
				"getAttachmentsPanel",
				"getNotesPanel",
				"buildWidgets",
				"updateDocPanel",
				"getWFWidgets",
				"showActivityPanel",
				"reset"
			]);
		},

		buildComponents: function() {
			var gridratio = CMDBuild.configuration.instance.get('cardFormRatio') || 50; // TODO: use proxy constants

			this.cardGrid = new CMDBuild.view.management.workflow.CMActivityGrid({
				hideMode: "offsets",
				filterCategory: this.cmName,
				border: false,
				columns: [],
				forceSelectionOfFirst: true
			});

			this.cardTabPanel = new CMDBuild.view.management.workflow.CMActivityTabPanel({
				cls: "cmdb-border-top",
				region: "south",
				hideMode: "offsets",
				split: true,
				border: false,
				height: gridratio + "%"
			});

			var widgetManager = new CMDBuild.view.management.common.widgets.CMWidgetManager(
				this.cardTabPanel.getActivityPanel(), // as CMWidgetManagerDelegate
				this.cardTabPanel // as CMTabbedWidgetDelegate
			);

			this.mon(this.cardGrid.addCardButton, "cmClick", function(p) {
				this.fireEvent(this.CMEVENTS.addButtonClick, p);
			}, this);

			this.getWidgetManager = function() {
				return widgetManager;
			};
		}
	});

})();