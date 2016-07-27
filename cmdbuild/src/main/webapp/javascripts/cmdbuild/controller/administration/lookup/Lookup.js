(function() {

	Ext.define('CMDBuild.controller.administration.lookup.Lookup', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.lookup.Type',
			'CMDBuild.model.lookup.Type'
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'lookupSelectedLookupTypeGet',
			'lookupSelectedLookupTypeIsEmpty',
			'onLookupAddButtonClick',
			'onLookupModuleInit = onModuleInit',
			'onLookupSelected -> controllerProperties, controllerList'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.controller.administration.lookup.List}
		 */
		controllerList: undefined,

		/**
		 * @property {CMDBuild.controller.administration.lookup.Properties}
		 */
		controllerProperties: undefined,

		/**
		 * @property {CMDBuild.model.lookup.Type} or null
		 *
		 * @private
		 */
		selectedLookupType: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.lookup.LookupView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.lookup.LookupView', { delegate: this });

			this.view.tabPanel.removeAll();

			// Controller build
			this.controllerProperties = Ext.create('CMDBuild.controller.administration.lookup.Properties', { parentDelegate: this });
			this.controllerList = Ext.create('CMDBuild.controller.administration.lookup.List', { parentDelegate: this });

			// Inject tabs
			this.view.tabPanel.add(this.controllerProperties.getView());
			this.view.tabPanel.add(this.controllerList.getView());
		},

		// SelectedLookupType methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			lookupSelectedLookupTypeGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedLookupType';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			lookupSelectedLookupTypeIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedLookupType';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			lookupSelectedLookupTypeReset: function() {
				this.propertyManageReset('selectedLookupType');
			},

			/**
			 * @param {Object} parameters
			 */
			lookupSelectedLookupTypeSet: function(parameters) {
				parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.lookup.Type';
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedLookupType';

				this.propertyManageSet(parameters);
			},

		onLookupAddButtonClick: function() {
			this.lookupSelectedLookupTypeReset();

			this.cmfg('mainViewportAccordionDeselect', CMDBuild.core.constants.ModuleIdentifiers.getLookupType());

			this.view.tabPanel.setActiveTab(0);

			// Forwarding
			this.controllerProperties.cmfg('onLookupPropertiesAddButtonClick');
			this.controllerList.getView().disable();
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.lookup.accordion.Administration} node
		 *
		 * @override
		 */
		onLookupModuleInit: function(node) {
			if (!Ext.isEmpty(node)) {
				CMDBuild.proxy.lookup.Type.read({ // TODO: waiting for refactor (crud + rename)
					scope: this,
					success: function(response, options, decodedResponse) {
						var lookupObject = Ext.Array.findBy(decodedResponse, function(item, i) {
							return node.get(CMDBuild.core.constants.Proxy.ENTITY_ID) == item[CMDBuild.core.constants.Proxy.ID];
						}, this);
						lookupObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = lookupObject[CMDBuild.core.constants.Proxy.TEXT];

						this.lookupSelectedLookupTypeSet({ value: lookupObject });

						this.cmfg('onLookupSelected');

						this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

						if (Ext.isEmpty(this.view.tabPanel.getActiveTab()))
							this.view.tabPanel.setActiveTab(0);

						this.onModuleInit(node); // Custom callParent() implementation
					}
				});
			}
		}
	});

})();