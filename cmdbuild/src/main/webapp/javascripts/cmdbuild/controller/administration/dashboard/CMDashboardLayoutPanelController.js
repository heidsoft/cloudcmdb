Ext.require([
	'CMDBuild.core.Message',
	'CMDBuild.proxy.dashboard.Dashboard'
]);

Ext.define("CMDBuild.controller.administration.dashboard.CMDashboardLayoutPanelController", {

	alias: ["controller.cmdashboardlayoutconf"],

	mixins: {
		columnController: "CMDBuild.controller.common.CMDashboardColumnController", // the order is important
		viewDelegate: "CMDBuild.view.administration.dashboard.CMDashboardLayoutPanelDelegate"
	},

	constructor: function(view) {
		this.view = view;
		this.view.setDelegate(this);
		this.dashboard = null;
	},

	// called by the super-controller

	dashboardWasSelected: function(dashboard) {
		var me = this;

		me.dashboard = dashboard;
		me.view.enable();

		if (me.view.isTheActiveTab()) {
			me.view.configureForDashboard(me.dashboard);
		} else {
			me.view.mon(me.view, "activate", function() {
				me.view.configureForDashboard(me.dashboard);
			}, me, {
				single: true
			});
		}
	},

	prepareForAdd: function() {
		this.view.clearAll();
		this.view.disable();
	},

	// view delegate

	onAddColumnClick: function() {
		var actualColumnCount = this.view.countColumns();
		var factor = 1/(actualColumnCount + 1);

		this.view.addColumn({
			charts: [],
			width: factor
		});
	},

	onRemoveColumnClick: function() {
		this.view.removeEmptyColumns();
	},

	onColumnWidthSliderChange: function() {
		this.view.syncColumnWidthToSliderThumbs();
	},

	onSaveButtonClick: function() {
		CMDBuild.proxy.dashboard.Dashboard.updateColumns({
			params: {
				dashboardId: this.dashboard.getId(),
				columnsConfiguration: Ext.encode(this.view.getColumnsConfiguration())
			},
			scope: this,
			success: function (response, options, decodedResponse) {
				CMDBuild.core.Message.success();
				var d = _CMCache.getDashboardById(this.dashboard.getId());
				if (d) {
					d.setColumns(this.view.getColumnsConfiguration());
				}
			}
		});
	},

	onAbortButtonClick: function() {
		this.dashboardWasSelected(this.dashboard);
	}
});