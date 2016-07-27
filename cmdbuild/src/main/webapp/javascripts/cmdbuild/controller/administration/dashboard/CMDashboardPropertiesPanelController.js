(function() {

	Ext.require([
		'CMDBuild.core.Message',
		'CMDBuild.proxy.dashboard.Dashboard'
	]);

	Ext.define("CMDBuild.controller.administration.dashboard.CMDashboardPropertiesPanelController", {

		alias: "controller.cmdashboardproperties",

		mixins: {
			viewDelegate: "CMDBuild.view.administration.dashboard.CMDashboardPropertiesDelegate"
		},

		constructor : function(view) {
			this.callParent(arguments);

			this.view = view;
			this.view.setDelegate(this);

			this.dashboard = null;
		},

		dashboardWasSelected: function(dashboard) {
			this.dashboard = dashboard;
			this.onAbortButtonClick();
		},

		prepareForAdd: function() {
			this.dashboard = null;

			this.view.disableTBarButtons();
			this.view.enableButtons();
			this.view.enableFields(all=true);
			this.view.cleanFields();
		},

		// CMDashboardPropertiesDelegate

		onModifyButtonClick: function() {
			this.view.enableFields(all=false);
			this.view.enableButtons();
			this.view.disableTBarButtons();
		},

		onAbortButtonClick: function() {
			this.view.disableFields();
			this.view.disableButtons();

			if (this.dashboard) {
				fillFormWithDashboardData(this);
				this.view.enableTBarButtons();
			} else {
				this.view.cleanFields();
				this.view.disableTBarButtons();
			}
		},

		onSaveButtonClick: function() {
			var data = this.view.getFieldsValue();
			if (!data.name || !data.description) {
				CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return;
			}

			if (this.dashboard) {
				CMDBuild.proxy.dashboard.Dashboard.update({
					params: {
						dashboardId: this.dashboard.getId(),
						dashboardConfiguration: Ext.encode(data)
					},
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.dashboard = null;
						this.onAbortButtonClick();

						CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerUpdateStore', {
							identifier: 'dashboard',
							nodeIdToSelect: data.id
						});

						/**
						 * @deprecated
						 */
						_CMCache.modifyDashboard(data, data.id);
					}
				});
			} else {
				CMDBuild.proxy.dashboard.Dashboard.create({
					params: {
						dashboardConfiguration: Ext.encode(data)
					},
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						data.id = decodedResponse.response;

						this.dashboard = null;
						this.onAbortButtonClick();

						CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerUpdateStore', {
							identifier: 'dashboard',
							nodeIdToSelect: data.id
						});

						/**
						 * @deprecated
						 */
						_CMCache.addDashboard(data);
					}
				});
			}
		},

		onRemoveButtonClick: function () {
			CMDBuild.proxy.dashboard.Dashboard.remove({
				params: {
					dashboardId: this.dashboard.getId()
				},
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionDeselect', 'dashboard');
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerUpdateStore', { identifier: 'dashboard' });

					/**
					 * @deprecated
					 */
					_CMCache.removeDashboardWithId(this.dashboard.getId());

					this.dashboard = null;
					this.onAbortButtonClick();
				}
			});
		}
	});

	function fillFormWithDashboardData(me) {
		me.view.fillFieldsWith({
			name: me.dashboard.getName(),
			description: me.dashboard.getDescription(),
			groups: me.dashboard.getGroups()
		});
	}

})();
