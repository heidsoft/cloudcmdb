(function () {

	Ext.define('CMDBuild.controller.administration.navigationTree.NavigationTree', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.NavigationTree'
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'navigationTreeSelectedTreeGet',
			'navigationTreeSelectedTreeIsEmpty',
			'onNavigationTreeAbortButtonClick',
			'onNavigationTreeAddButtonClick',
			'onNavigationTreeModifyButtonClick',
			'onNavigationTreeModuleInit = onModuleInit',
			'onNavigationTreeRemoveButtonClick',
			'onNavigationTreeSaveButtonClick',
			'onNavigationTreeSelected -> controllerProperties, controllerTree'
		],

		/**
		 * @property {CMDBuild.controller.administration.navigationTree.Properties}
		 */
		controllerProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.navigationTree.Tree}
		 */
		controllerTree: undefined,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.model.navigationTree.NavigationTree}
		 *
		 * @private
		 */
		selectedTree: undefined,

		/**
		 * @property {Ext.tab.Panel}
		 */
		tabPanel: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.navigationTree.NavigationTreeView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.navigationTree.NavigationTreeView', { delegate: this });

			// Shorthands
			this.tabPanel = this.view.tabPanel;

			this.tabPanel.removeAll();

			// Controller build
			this.controllerProperties = Ext.create('CMDBuild.controller.administration.navigationTree.Properties', { parentDelegate: this });
			this.controllerTree = Ext.create('CMDBuild.controller.administration.navigationTree.Tree', { parentDelegate: this });

			// Inject tabs
			this.tabPanel.add(this.controllerProperties.getView());
			this.tabPanel.add(this.controllerTree.getView());
		},

		// SelectedTree property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			navigationTreeSelectedTreeGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedTree';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			navigationTreeSelectedTreeIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedTree';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			navigationTreeSelectedTreeReset: function () {
				this.propertyManageReset('selectedTree');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			navigationTreeSelectedTreeSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.navigationTree.NavigationTree';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedTree';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onNavigationTreeAbortButtonClick: function () {
			this.controllerProperties.cmfg('onNavigationTreeTabPropertiesAbortButtonClick');
			this.controllerTree.cmfg('onNavigationTreeTabTreeAbortButtonClick');
		},

		/**
		 * @returns {Void}
		 */
		onNavigationTreeAddButtonClick: function () {
			this.navigationTreeSelectedTreeReset();

			this.cmfg('mainViewportAccordionDeselect', this.cmfg('identifierGet'));

			this.tabPanel.setActiveTab(0);

			// Forwarding
			this.controllerProperties.cmfg('onNavigationTreeTabPropertiesAddButtonClick');
			this.controllerTree.cmfg('onNavigationTreeTabTreeAddButtonClick');
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 */
		onNavigationTreeModifyButtonClick: function () {
			this.controllerProperties.cmfg('onNavigationTreeTabPropertiesModifyButtonClick');
			this.controllerTree.cmfg('onNavigationTreeTabTreeModifyButtonClick');
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.navigationTree.accordion.Administration} node
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onNavigationTreeModuleInit: function (node) {
			if (!Ext.isEmpty(node)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = node.get(CMDBuild.core.constants.Proxy.ENTITY_ID);

				CMDBuild.proxy.NavigationTree.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						this.navigationTreeSelectedTreeSet({ value: Ext.decode(decodedResponse) }); // TODO: waiting for refactor (remove double JSON encode)

						this.cmfg('onNavigationTreeSelected');

						this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

						if (Ext.isEmpty(this.tabPanel.getActiveTab()))
							this.tabPanel.setActiveTab(0);

						this.onModuleInit(node); // Custom callParent() implementation
					}
				});
			} else { // Display title if no nodes are selected
				this.setViewTitle();

				this.cmfg('onNavigationTreeSelected');
			}
		},

		/**
		 * @returns {Void}
		 */
		onNavigationTreeRemoveButtonClick: function () {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onNavigationTreeSaveButtonClick: function () {
			var propertiesForm = this.controllerProperties.cmfg('navigationTreeTabPropertiesFormGet');

			if (this.validate(propertiesForm)) {
				var params = propertiesForm.getData(true);

				if (Ext.isEmpty(params[CMDBuild.core.constants.Proxy.ID])) {
					var structure = {};
					structure[CMDBuild.core.constants.Proxy.CHILD_NODES] = [];
					structure[CMDBuild.core.constants.Proxy.TARGET_CLASS_DESCRIPTION] = params[CMDBuild.core.constants.Proxy.DESCRIPTION]; // TODO: tree description
					structure[CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME] = params[CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME];

					params[CMDBuild.core.constants.Proxy.STRUCTURE] = Ext.encode(structure);

					CMDBuild.proxy.NavigationTree.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					var structure = {};
					structure[CMDBuild.core.constants.Proxy.CHILD_NODES] = this.controllerTree.cmfg('navigationTreeTabTreeDataGet', 'childNodes');
					structure[CMDBuild.core.constants.Proxy.FILTER] = this.controllerTree.cmfg('navigationTreeTabTreeDataGet', 'rootFilter');
					structure[CMDBuild.core.constants.Proxy.TARGET_CLASS_DESCRIPTION] = params[CMDBuild.core.constants.Proxy.DESCRIPTION]; // TODO: tree description
					structure[CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME] = params[CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME];

					params[CMDBuild.core.constants.Proxy.STRUCTURE] = Ext.encode(structure);

					CMDBuild.proxy.NavigationTree.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		removeItem: function () {
			if (!this.cmfg('navigationTreeSelectedTreeIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.cmfg('navigationTreeSelectedTreeGet', CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.NavigationTree.remove({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.navigationTreeSelectedTreeReset();

						this.cmfg('onNavigationTreeSelected'); // Fake selection to correctly sets tabs display state

						this.cmfg('mainViewportAccordionDeselect', this.cmfg('identifierGet'));
						this.cmfg('mainViewportAccordionControllerUpdateStore', { identifier: this.cmfg('identifierGet') });
					}
				});
			}
		},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		success: function (response, options, decodedResponse) {
			this.cmfg('mainViewportAccordionControllerUpdateStore', {
				identifier: this.cmfg('identifierGet'),
//				nodeIdToSelect: decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE][CMDBuild.core.constants.Proxy.ID] // TODO: waiting for refactor
			});
		}
	});

})();
