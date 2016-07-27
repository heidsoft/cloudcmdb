(function () {

	/**
	 * Call sequence: init() -> buildConfiguration() -> buildCache() -> buildUserInterface()
	 */
	Ext.define('CMDBuild.core.Management', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.CookiesManager',
			'CMDBuild.proxy.Classes',
			'CMDBuild.proxy.dashboard.Dashboard',
			'CMDBuild.proxy.domain.Domain',
			'CMDBuild.proxy.lookup.Type',
			'CMDBuild.proxy.Menu',
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
			CMDBuild.core.Splash.show();

			CMDBuild.core.Management.buildConfiguration();
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
				id: 'managementBuildCacheBarrier',
				callback: CMDBuild.core.Management.buildUserInterface
			});

			/**
			 * Class and process
			 */
			params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			var readAllClassesCallback = requestBarrier.getCallback('managementBuildCacheBarrier'); // Avoid to getCallback too late

			CMDBuild.proxy.Classes.readAll({
				params: params,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

					_CMCache.addClasses(decodedResponse);

					/**
					 * Widget
					 *
					 * Widgets must be added to cache only before classes, because widget object is added to class model
					 */
					CMDBuild.proxy.widget.Widget.readAll({
						loadMask: false,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

							// A day I'll can do a request to have only the active, now the cache discards the inactive if the flag onlyActive is true
							_CMCache.addWidgetToEntryTypes(decodedResponse, true);
						},
						callback: readAllClassesCallback
					});
				}
			});

			/**
			 * Dashboard
			 */
			CMDBuild.proxy.dashboard.Dashboard.readAllVisible({
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					_CMCache.addDashboards(decodedResponse[CMDBuild.core.constants.Proxy.DASHBOARDS]);
					_CMCache.setAvailableDataSources(decodedResponse[CMDBuild.core.constants.Proxy.DATA_SOURCES]);
				},
				callback: requestBarrier.getCallback('managementBuildCacheBarrier')
			});

			/**
			 * Domain
			 */
			params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			CMDBuild.proxy.domain.Domain.readAll({
				params: params,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

					_CMCache.addDomains(decodedResponse);
				},
				callback: requestBarrier.getCallback('managementBuildCacheBarrier')
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
				callback: requestBarrier.getCallback('managementBuildCacheBarrier')
			});

			requestBarrier.finalize('managementBuildCacheBarrier', true);
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
				id: 'managementBuildConfigurationBarrier',
				callback: CMDBuild.core.Management.buildCache
			});

			Ext.create('CMDBuild.core.configurations.builder.Instance', { callback: requestBarrier.getCallback('managementBuildConfigurationBarrier') }); // CMDBuild Instance configuration
			Ext.create('CMDBuild.core.configurations.builder.Bim', { callback: requestBarrier.getCallback('managementBuildConfigurationBarrier') }); // CMDBuild BIM configuration
			Ext.create('CMDBuild.core.configurations.builder.Dms', { callback: requestBarrier.getCallback('managementBuildConfigurationBarrier') }); // CMDBuild DMS configuration
			Ext.create('CMDBuild.core.configurations.builder.Gis', { callback: requestBarrier.getCallback('managementBuildConfigurationBarrier') }); // CMDBuild GIS configuration
			Ext.create('CMDBuild.core.configurations.builder.Localization', { callback: requestBarrier.getCallback('managementBuildConfigurationBarrier') }); // CMDBuild Localization configuration
			Ext.create('CMDBuild.core.configurations.builder.RelationGraph', { callback: requestBarrier.getCallback('managementBuildConfigurationBarrier') }); // CMDBuild RelationGraph configuration
			Ext.create('CMDBuild.core.configurations.builder.UserInterface', { callback: requestBarrier.getCallback('managementBuildConfigurationBarrier') }); // CMDBuild UserInterface configuration
			Ext.create('CMDBuild.core.configurations.builder.Workflow', { callback: requestBarrier.getCallback('managementBuildConfigurationBarrier') }); // CMDBuild Workflow configuration

			requestBarrier.finalize('managementBuildConfigurationBarrier', true);
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

				_CMCache.syncAttachmentCategories();

				// Building accordion definitions object array (display order)
				var accordionDefinitionObjectsArray = [{ className: 'CMDBuild.controller.management.accordion.Menu', identifier: CMDBuild.core.constants.ModuleIdentifiers.getNavigation() }];

				if (!CMDBuild.configuration.userInterface.isDisabledModule(CMDBuild.core.constants.Proxy.CLASS))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.management.accordion.Classes', identifier: 'class' });

				if (!CMDBuild.configuration.userInterface.isDisabledModule(CMDBuild.core.constants.Proxy.PROCESS) && CMDBuild.configuration.workflow.get(CMDBuild.core.constants.Proxy.ENABLED))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.management.accordion.Workflow', identifier: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow() });

				if (!CMDBuild.configuration.userInterface.isDisabledModule(CMDBuild.core.constants.Proxy.DATA_VIEW))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.management.accordion.DataView', identifier: CMDBuild.core.constants.ModuleIdentifiers.getDataView() });

				if (!CMDBuild.configuration.userInterface.isDisabledModule(CMDBuild.core.constants.Proxy.DASHBOARD))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.management.accordion.Dashboard', identifier: 'dashboard' });

				if (!CMDBuild.configuration.userInterface.isDisabledModule(CMDBuild.core.constants.Proxy.REPORT))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.management.accordion.Report', identifier: CMDBuild.core.constants.ModuleIdentifiers.getReport() });

				if (!CMDBuild.configuration.userInterface.isDisabledModule(CMDBuild.core.constants.Proxy.CUSTOM_PAGES))
					accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.management.accordion.CustomPage', identifier: CMDBuild.core.constants.ModuleIdentifiers.getCustomPage() });

				accordionDefinitionObjectsArray.push({ className: 'CMDBuild.controller.management.accordion.Utility', identifier: CMDBuild.core.constants.ModuleIdentifiers.getUtility() });

				Ext.ns('CMDBuild.global.controller');
				CMDBuild.global.controller.MainViewport = Ext.create('CMDBuild.controller.common.MainViewport', {
					accordion: accordionDefinitionObjectsArray,
					module: [
						{ className: 'CMDBuild.controller.management.customPage.SinglePage', identifier: CMDBuild.core.constants.ModuleIdentifiers.getCustomPage() },
						{ className: 'CMDBuild.controller.management.dataView.DataView', identifier: CMDBuild.core.constants.ModuleIdentifiers.getDataView() },
						{ className: 'CMDBuild.controller.management.report.Report', identifier: CMDBuild.core.constants.ModuleIdentifiers.getReport() },
						{ className: 'CMDBuild.controller.management.report.Single', identifier: CMDBuild.core.constants.ModuleIdentifiers.getReportSingle() },
						{ className: 'CMDBuild.controller.management.utility.Utility', identifier: CMDBuild.core.constants.ModuleIdentifiers.getUtility() },
						new CMDBuild.view.management.classes.CMModCard({
							cmControllerType: CMDBuild.controller.management.classes.CMModCardController,
							cmName: 'class'
						}),
						new CMDBuild.view.management.workflow.CMModProcess({
							cmControllerType: CMDBuild.controller.management.workflow.CMModWorkflowController,
							cmName: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow()
						}),
						new CMDBuild.view.management.dashboard.CMModDashboard({
							cmControllerType: CMDBuild.controller.management.dashboard.CMModDashboardController,
							cmName: 'dashboard'
						})
					]
				});

				Ext.resumeLayouts(true);

				CMDBuild.core.Splash.hide(function () {
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportInstanceNameSet', CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.INSTANCE_NAME));

					// Execute routes
					CMDBuild.routes.Routes.exec();

					CMDBuild.global.controller.MainViewport.cmfg('mainViewportStartingEntitySelect');
				}, this);

				if (CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.FULL_SCREEN_MODE))
					_CMUIState.onlyGrid();
			}
		}
	});

})();
