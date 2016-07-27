(function() {

	var tr = CMDBuild.Translation.administration.modDashboard.charts;

	Ext.require([
		'CMDBuild.core.Message',
		'CMDBuild.proxy.dashboard.Chart'
	]);

	Ext.define("CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationPanelControllerDelegate", {
		dashboardChartAreChanged: Ext.emptyFn
	});

	Ext.define("CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationPanelController", {

		alias: "controller.cmdashboardchartconfiguration",

		statics: {
			cmcreate: function(view) {
				var s = buildSubControllers(view);
				return new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationPanelController(view, s.formController, s.gridController);
			}
		},

		mixins: {
			viewDelegate: "CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationPanelDelegate",
			gridControllerDelegate: "CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationGridControllerDelegate"
		},

		constructor : function(view, formController, gridController, proxy, delegate) {
			this.callParent(arguments);

			this.dashboard = null;
			this.chart = null;
			this.view = view;
			this.formController = formController;
			this.gridController = gridController;
			this.proxy = proxy || CMDBuild.proxy.dashboard.Chart;
			this.setDelegate(delegate);

			this.view.setDelegate(this);
			this.gridController.setDelegate(this);
		},

		initComponent : function() {
			this.callParent(arguments);
			this.view.disable();
		},

		dashboardWasSelected: function(d) {
			this.dashboard = d;
			this.view.enable();
			this.view.enableTBarButtons(onlyAdd=true);
			this.view.disableButtons();

			this.formController.initView(d);
			this.gridController.loadCharts(d.getCharts());
		},

		prepareForAdd: function() {
			this.view.disable();
		},

		setDelegate: function(delegate) {
			this.delegate = delegate || new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationPanelControllerDelegate();
		},

		// viewDelegate
		onModifyButtonClick: function() {
			this.view.disableTBarButtons();
			this.view.enableButtons();
			this.formController.prepareForModify();
		},

		onAddButtonClick: function() {
			this.chart = null;
			this.formController.prepareForAdd();
			this.gridController.clearSelection();
			this.view.disableTBarButtons();
			this.view.enableButtons();
		},

		onPreviewButtonClick: function() {
			var formData = CMDBuild.model.CMDashboardChart.build(this.formController.getFormData());
			var store = CMDBuild.controller.common.chart.CMChartPortletController.buildStoreForChart(formData);

			var chartWindow = new CMDBuild.view.management.dashboard.CMChartWindow({
				chartConfiguration: formData,
				store: store,
				title: formData.name
			}).show();

			if (chartWindow.chartPortlet) {
				CMDBuild.controller.common.chart.CMChartPortletController.buildForPreview(
						chartWindow.chartPortlet, formData, store, this.dashboard.getId());
			}
		},

		onRemoveButtonClick: function() {
			this.view.disableButtons();
			this.view.enableTBarButtons(onlyAdd=true);
			this.formController.initView();

			var me = this;
			this.proxy.remove({
				params: {
					dashboardId: this.dashboard.getId(),
					chartId: this.chart.getId()
				},
				loadMask: false,
				scope: this,
				success: function (operation, configuration, decodedResponse) {
					var d = _CMCache.getDashboardById(this.dashboard.getId());
					if (d) {
						d.removeChart(this.chart.getId());

						me.gridController.loadCharts(d.getCharts());
						me.delegate.dashboardChartAreChanged();
					}
				}
			});
		},

		onSaveButtonClick: function() {
			if (!this.formController.isValid()) {
				CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return;
			}

			var formData = this.formController.getFormData(),
				me = this,
				cb =  function(charts, idToSelect) {
					me.gridController.loadCharts(charts, idToSelect);
					me.delegate.dashboardChartAreChanged();
				};

			this.view.disableButtons();
			this.view.disableTBarButtons();
			this.formController.initView();

			if (this.chart) {
				this.proxy.update({
					params: {
						dashboardId: this.dashboard.getId(),
						chartId: this.chart.getId(),
						chartConfiguration: Ext.encode(formData)
					},
					loadMask: false,
					scope: this,
					success: function (operation, configuration, decodedResponse) {
						var d = _CMCache.getDashboardById(this.dashboard.getId());
						if (d) {
							formData.id = this.chart.getId();
							var chart = CMDBuild.model.CMDashboardChart.build(formData);
							d.replaceChart(this.chart.getId(), chart);

							if (typeof cb == "function") {
								cb(d.getCharts(), chart.getId());
							}
						}
					}
				});
			} else {
				this.proxy.create({
					params: {
						dashboardId: this.dashboard.getId(),
						chartConfiguration: Ext.encode(formData)
					},
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						var d = _CMCache.getDashboardById(this.dashboard.getId());
						if (d) {
							formData.id = decodedResponse.response;
							var chart = CMDBuild.model.CMDashboardChart.build(formData);
							d.addChart(chart);

							if (typeof cb == "function") {
								cb(d.getCharts(), chart.getId());
							}
						}
					}
				});
			}
		},

		onAbortButtonClick:function() {
			var enableOnlyAddButton = false;
			if (this.chart) {
				this.formController.prepareForChart(this.chart);
			} else {
				this.formController.initView();
				enableOnlyAddButton = true;
			}
			this.view.disableButtons();
			this.view.enableTBarButtons(enableOnlyAddButton);
		},

		// grid controller delegate
		chartWasSelected: function(chart) {
			this.chart = chart;
			this.formController.prepareForChart(chart);
			this.view.disableButtons();
			this.view.enableTBarButtons();
		}
	});

	function buildSubControllers(view) {
		return {
			formController: CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationFormController.cmcreate(view.getFormPanel()),
			gridController: new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationGridController.cmcreate(view.getGridPanel())
		};
	}

})();