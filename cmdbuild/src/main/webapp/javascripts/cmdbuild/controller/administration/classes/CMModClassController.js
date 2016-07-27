(function() {

	Ext.define("CMDBuild.controller.administration.classes.CMModClassController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		requires: ['CMDBuild.core.constants.Proxy'],

		constructor: function() {
			this.callParent(arguments);
			this.buildSubcontrollers();

			this.view.addClassButton.on("click", this.onAddClassButtonClick, this);
			this.view.printSchema.on("click", this.onPrintSchema, this);

			this.registerToCacheEvents();
		},

		//private and overridden in subclasses
		buildSubcontrollers: function() {
			this.classFormController = new CMDBuild.controller.administration.classes.CMClassFormController();
			this.domainTabController = new CMDBuild.controller.administration.classes.CMDomainTabController();
			this.geoAttributesController = new CMDBuild.controller.administration.classes.CMGeoAttributeController();
			this.attributePanelController = new CMDBuild.controller.administration.classes.CMClassAttributeController();
			this.widgetDefinitionController = Ext.create('CMDBuild.controller.administration.widget.Widget', { parentDelegate: this });

			// Views inject
			this.view.tabPanel.add(this.classFormController.getView());
			this.view.classForm = this.classFormController.getView(); // Legacy pointer

			this.view.tabPanel.add(this.attributePanelController.getView());
			this.view.attributesPanel = this.attributePanelController.getView(); // Legacy pointer

			this.view.tabPanel.add(this.domainTabController.getView());
			this.view.domainGrid = this.domainTabController.getView(); // Legacy pointer

			this.view.tabPanel.add(this.widgetDefinitionController.getView());
			this.view.widgetPanel = this.widgetDefinitionController.getView(); // Legacy pointer

			this.view.layerVisibilityGrid = new CMDBuild.Administration.LayerVisibilityGrid({ // Legacy pointer
				title: CMDBuild.Translation.administration.modClass.layers,
				withCheckToHideLayer: true,
				disabled: true
			});
			this.view.tabPanel.add(this.view.layerVisibilityGrid);

			this.view.tabPanel.add(this.geoAttributesController.getView());
			this.view.geoAttributesPanel = this.geoAttributesController.getView(); // Legacy pointer

			this.subControllers = [
				this.classFormController,
				this.domainTabController,
				this.geoAttributesController,
				this.attributePanelController,
				this.widgetDefinitionController
			];

			this.view.tabPanel.setActiveTab(0);
		},

		//private and overridden in subclasses
		registerToCacheEvents: function() {
			_CMCache.on("cm_class_deleted", this.view.onClassDeleted, this.view);
		},

		//private and overridden in subclasses
		onViewOnFront: function(selection) {
			this.view.tabPanel.setActiveTab(0);

			if (selection) {
				selection.data['id'] = selection.data[CMDBuild.core.constants.Proxy.ENTITY_ID]; // New accordion manage

				this.view.onClassSelected(selection.data);
				this.classFormController.onClassSelected(selection.data.id);
				this.domainTabController.onClassSelected(selection.data.id);
				this.geoAttributesController.onClassSelected(selection.data.id);
				this.attributePanelController.onClassSelected(selection.data.id);
				this.widgetDefinitionController.cmfg('onClassSelected', selection.data.id);
			}
		},

		/**
		 * @params {String} format
		 */
		onPrintSchema: function(format) {
			if (!Ext.isEmpty(format)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.FORMAT] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					parentDelegate: this,
					format: format,
					mode: 'schema',
					parameters: params
				});
			}
		},

		onAddClassButtonClick: function () {
			this.classFormController.onAddClassButtonClick();
			this.domainTabController.onAddClassButtonClick();
			this.geoAttributesController.onAddClassButtonClick();
			this.attributePanelController.onAddClassButtonClick();
			this.widgetDefinitionController.cmfg('onAddClassButtonClick');

			this.view.onAddClassButtonClick();
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionDeselect', "class");
		}
	});

})();