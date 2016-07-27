(function () {

	/**
	 * Call sequence: init() -> buildConfiguration() -> buildCache() -> buildUserInterface()
	 */
	Ext.define('CMDBuild.core.Administration', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.CookiesManager',
			'CMDBuild.proxy.Classes',
			'CMDBuild.proxy.dashboard.Dashboard',
			'CMDBuild.proxy.domain.Domain',
			'CMDBuild.proxy.lookup.Type',
			'CMDBuild.proxy.userAndGroup.group.Group',
			'CMDBuild.proxy.widget.Widget',
			'CMDBuild.core.Splash'
		],

		singleton: true,

		/**
		 * Entry-point
		 *
		 * @returns {Void}
		 *
		 * @public
		 */
		init: function () {
			CMDBuild.core.Splash.show(true);

			CMDBuild.core.Administration.buildConfiguration();
		},

		/**
		 * Builds all entities cache objects
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildCache: function () {
			var params = {};
			var requestBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
				id: 'administrationBuildCacheBarrier',
				callback: CMDBuild.core.Administration.buildUserInterface
			});

			/**
			 * Class and process
			 */
			params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

			CMDBuild.proxy.Classes.readAll({
				params: params,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

					_CMCache.addClasses(decodedResponse);
				},
				callback: requestBarrier.getCallback('administrationBuildCacheBarrier')
			});

			/**
			 * Domain
			 */
			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
				CMDBuild.proxy.domain.Domain.readAll({
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

						_CMCache.addDomains(decodedResponse);
					},
					callback: requestBarrier.getCallback('administrationBuildCacheBarrier')
				});

			/**
			 * Groups
			 */
			CMDBuild.proxy.userAndGroup.group.Group.readAll({
				loadMask: false,
				scope: this,
				callback: requestBarrier.getCallback('administrationBuildCacheBarrier')
			});

			/**
			 * Lookup
			 */
			CMDBuild.proxy.lookup.Type.readAll({
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					_CMCache.addLookupTypes(decodedResponse);
				},
				callback: requestBarrier.getCallback('administrationBuildCacheBarrier')
			});

			/**
			 * Dashboard
			 */
			CMDBuild.proxy.dashboard.Dashboard.readAll({
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					_CMCache.addDashboards(decodedResponse[CMDBuild.core.constants.Proxy.DASHBOARDS]);
					_CMCache.setAvailableDataSources(decodedResponse[CMDBuild.core.constants.Proxy.DATA_SOURCES]);
				},
				callback: requestBarrier.getCallback('administrationBuildCacheBarrier')
			});

			/**
			 * Widget
			 */
			CMDBuild.proxy.widget.Widget.readAll({
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					_CMCache.addWidgetToEntryTypes(decodedResponse);
				},
				callback: requestBarrier.getCallback('administrationBuildCacheBarrier')
			});

			requestBarrier.finalize('administrationBuildCacheBarrier', true);
		},

		/**
		 * Builds CMDBuild configurations objects
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildConfiguration: function () {
			var requestBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
				id: 'administrationBuildConfigurationBarrier',
				callback: CMDBuild.core.Administration.buildCache
			});

			Ext.create('CMDBuild.core.configurations.builder.Instance', { callback: requestBarrier.getCallback('administrationBuildConfigurationBarrier') }); // CMDBuild Instance configuration
			Ext.create('CMDBuild.core.configurations.builder.Bim', { callback: requestBarrier.getCallback('administrationBuildConfigurationBarrier') }); // CMDBuild BIM configuration
			Ext.create('CMDBuild.core.configurations.builder.Dms', { callback: requestBarrier.getCallback('administrationBuildConfigurationBarrier') }); // CMDBuild DMS configuration
			Ext.create('CMDBuild.core.configurations.builder.Gis', { callback: requestBarrier.getCallback('administrationBuildConfigurationBarrier') }); // CMDBuild GIS configuration
			Ext.create('CMDBuild.core.configurations.builder.Localization', { callback: requestBarrier.getCallback('administrationBuildConfigurationBarrier') }); // CMDBuild Localization configuration
			Ext.create('CMDBuild.core.configurations.builder.RelationGraph', { callback: requestBarrier.getCallback('administrationBuildConfigurationBarrier') }); // CMDBuild RelationGraph configuration
			Ext.create('CMDBuild.core.configurations.builder.UserInterface', { callback: requestBarrier.getCallback('administrationBuildConfigurationBarrier') }); // CMDBuild UserInterface configuration
			Ext.create('CMDBuild.core.configurations.builder.Workflow', { callback: requestBarrier.getCallback('administrationBuildConfigurationBarrier') }); // CMDBuild Workflow configuration

			requestBarrier.finalize('administrationBuildConfigurationBarrier', true);
		},

		/**
		 * Build all UI modules if runtime sessionId property isn't empty
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buildUserInterface: function () {
			if (!CMDBuild.core.CookiesManager.authorizationIsEmpty()) {
				Ext.suspendLayouts();

				// Building accordion definitions object array (display order)
				var accordionDefinitionObjectsArray = [];

				if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.administration.accordion.Classes', identifier: 'class' });

				if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.administration.accordion.Workflow', identifier: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow() });

				if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.administration.accordion.Domain', identifier: CMDBuild.core.constants.ModuleIdentifiers.getDomain() });

				if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.administration.accordion.DataView', identifier: CMDBuild.core.constants.ModuleIdentifiers.getDataView() });

				accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.administration.accordion.Filter', identifier: CMDBuild.core.constants.ModuleIdentifiers.getFilter() });

				if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.administration.accordion.NavigationTree', identifier: CMDBuild.core.constants.ModuleIdentifiers.getNavigationTree() });

				accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.administration.accordion.Lookup', identifier: CMDBuild.core.constants.ModuleIdentifiers.getLookupType() });

				if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.CLOUD_ADMIN))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.administration.accordion.Dashboard', identifier: 'dashboard' });

				Ext.Array.push(accordionDefinitionObjectsArray, [
					{ className: 'CMDBuild.controller.administration.accordion.Report', identifier: CMDBuild.core.constants.ModuleIdentifiers.getReport() },
					{ className: 'CMDBuild.controller.administration.accordion.Menu', identifier: CMDBuild.core.constants.ModuleIdentifiers.getMenu() },
					{ className: 'CMDBuild.controller.administration.accordion.UserAndGroup', identifier: CMDBuild.core.constants.ModuleIdentifiers.getUserAndGroup() },
					{ className: 'CMDBuild.controller.administration.accordion.Task', identifier: 'task' },
					{ className: 'CMDBuild.controller.administration.accordion.Email', identifier: CMDBuild.core.constants.ModuleIdentifiers.getEmail() },
					{ className: 'CMDBuild.controller.administration.accordion.Gis', identifier: 'gis' },
					{ className: 'CMDBuild.controller.administration.accordion.Bim', identifier: 'bim' },
					{ className: 'CMDBuild.controller.administration.accordion.Localization', identifier: CMDBuild.core.constants.ModuleIdentifiers.getLocalization() },
					{ className: 'CMDBuild.controller.administration.accordion.Configuration', identifier: CMDBuild.core.constants.ModuleIdentifiers.getConfiguration() }
				]);

				Ext.ns('CMDBuild.global.controller');
				CMDBuild.global.controller.MainViewport = Ext.create('CMDBuild.controller.common.MainViewport', {
					isAdministration: true,
					accordion: accordionDefinitionObjectsArray,
					module: [
						{ className: 'CMDBuild.controller.administration.configuration.Configuration', identifier: CMDBuild.core.constants.ModuleIdentifiers.getConfiguration() },
						{ className: 'CMDBuild.controller.administration.dataView.DataView', identifier: CMDBuild.core.constants.ModuleIdentifiers.getDataView() },
						{ className: 'CMDBuild.controller.administration.domain.Domain', identifier: CMDBuild.core.constants.ModuleIdentifiers.getDomain() },
						{ className: 'CMDBuild.controller.administration.email.Email', identifier: CMDBuild.core.constants.ModuleIdentifiers.getEmail() },
						{ className: 'CMDBuild.controller.administration.filter.Filter', identifier: CMDBuild.core.constants.ModuleIdentifiers.getFilter() },
						{ className: 'CMDBuild.controller.administration.localization.Localization', identifier: CMDBuild.core.constants.ModuleIdentifiers.getLocalization() },
						{ className: 'CMDBuild.controller.administration.lookup.Lookup', identifier: CMDBuild.core.constants.ModuleIdentifiers.getLookupType() },
						{ className: 'CMDBuild.controller.administration.menu.Menu', identifier: CMDBuild.core.constants.ModuleIdentifiers.getMenu() },
						{ className: 'CMDBuild.controller.administration.navigationTree.NavigationTree', identifier: CMDBuild.core.constants.ModuleIdentifiers.getNavigationTree() },
						{ className: 'CMDBuild.controller.administration.report.Report', identifier: CMDBuild.core.constants.ModuleIdentifiers.getReport() },
						{ className: 'CMDBuild.controller.administration.userAndGroup.UserAndGroup', identifier: CMDBuild.core.constants.ModuleIdentifiers.getUserAndGroup() },
						{ className: 'CMDBuild.controller.administration.workflow.Workflow', identifier: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow() },
						Ext.create('CMDBuild.view.administration.gis.CMModGeoServer', {
							cmControllerType: 'CMDBuild.controller.administration.gis.CMModGeoServerController',
							cmName: 'gis-geoserver'
						}),
						Ext.create('CMDBuild.view.administration.gis.ExternalServices', {
							cmControllerType: 'CMDBuild.controller.administration.gis.ExternalServicesController',
							cmName: 'gis-external-services'
						}),
						Ext.create('CMDBuild.view.administration.tasks.CMTasks', {
							cmControllerType: 'CMDBuild.controller.administration.tasks.CMTasksController',
							cmName: 'task'
						}),
						new CMDBuild.view.administration.bim.CMBIMPanel({
							cmControllerType: CMDBuild.controller.administration.filter.CMBIMPanelController,
							cmName: 'bim-project'
						}),
						new CMDBuild.bim.administration.view.CMBimLayers({
							cmControllerType: CMDBuild.controller.administration.filter.CMBimLayerController,
							cmName: 'bim-layers'
						}),
						new CMDBuild.view.common.CMUnconfiguredModPanel({
							cmControllerType: CMDBuild.controller.common.CMUnconfiguredModPanelController,
							cmName: 'notconfiguredpanel'
						}),
						new CMDBuild.view.administration.classes.CMModClass({
							cmControllerType: CMDBuild.controller.administration.classes.CMModClassController,
							cmName: 'class'
						}),
						new CMDBuild.Administration.ModIcons({
							cmName: 'gis-icons'
						}),
						new CMDBuild.view.administration.gis.CMModGISNavigationConfiguration({
							cmControllerType: CMDBuild.controller.administration.gis.CMModGISNavigationConfigurationController,
							cmName: 'gis-filter-configuration'
						}),
						new CMDBuild.Administration.ModLayerOrder({
							cmControllerType: CMDBuild.controller.administration.gis.CMModLayerOrderController,
							cmName: 'gis-layers-order'
						}),
						new CMDBuild.view.administration.dashboard.CMModDashboard({
							cmControllerType: CMDBuild.controller.administration.dashboard.CMModDashboardController,
							cmName: 'dashboard'
						})
					]
				});

				Ext.resumeLayouts(true);

				CMDBuild.core.Splash.hide(function () {
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportInstanceNameSet', CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.INSTANCE_NAME));
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportSelectFirstExpandedAccordionSelectableNode');
				}, this);
			}
		}
	});

})();
