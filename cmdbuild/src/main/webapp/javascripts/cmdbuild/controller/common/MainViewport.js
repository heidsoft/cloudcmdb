(function () {

	Ext.define('CMDBuild.controller.common.MainViewport', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message'
		],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'mainViewportAccordionControllerExists',
			'mainViewportAccordionControllerExpand',
			'mainViewportAccordionControllerGet',
			'mainViewportAccordionControllerUpdateStore',
			'mainViewportAccordionControllerWithNodeWithIdGet',
			'mainViewportAccordionDeselect',
			'mainViewportAccordionIsCollapsed',
			'mainViewportAccordionSetDisabled',
			'mainViewportCardSelect',
			'mainViewportDanglingCardGet',
			'mainViewportInstanceNameSet',
			'mainViewportModuleControllerExists',
			'mainViewportModuleControllerGet',
			'mainViewportModuleShow',
			'mainViewportSelectFirstExpandedAccordionSelectableNode',
			'mainViewportStartingEntitySelect',
			'onMainViewportAccordionSelect',
			'onMainViewportCreditsClick'
		],

		/**
		 * Accordion definition objects
		 *
		 * @cfg {Array}
		 */
		accordion: [],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		accordionControllers: {},

		/**
		 * @property {Ext.panel.Panel}
		 */
		accordionContainer: undefined,

		/**
		 * The danglig card is used to open a card from a panel to another (something called follow the relations between cards)
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		danglingCard: null,

		/**
		 * @cfg {Array}
		 */
		enableSynchronizationForAccordions: [
			'class',
			CMDBuild.core.constants.ModuleIdentifiers.getWorkflow()
		],

		/**
		 * @cfg {Boolean}
		 */
		isAdministration: false,

		/**
		 * Module definition objects
		 *
		 * @cfg {Array}
		 */
		module: [],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		moduleControllers: [],

		/**
		 * @property {Ext.panel.Panel}
		 */
		moduleContainer: undefined,

		/**
		 * @property {CMDBuild.view.common.MainViewport}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.MainViewport', { delegate: this });

			// Shorthands
			this.accordionContainer = this.view.accordionContainer;
			this.moduleContainer = this.view.moduleContainer;

			this.accordionControllerBuild();
			this.moduleControllerBuild();
		},

		// Accordion manage methods
			/**
			 * Request barrier implementation to synchronize accordion creation
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			accordionControllerBuild: function () {
				if (!Ext.isEmpty(this.accordion) && Ext.isArray(this.accordion)) {
					var accordionViewsBuffer = [];

					var requestBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
						id: 'mainViewportAccordionBarrier',
						scope: this,
						callback: function () {
							if (!Ext.isEmpty(accordionViewsBuffer))
								this.accordionContainer.add(accordionViewsBuffer);
						}
					});

					Ext.Array.forEach(this.accordion, function (accordionControllerObject, i, allAccordionControllerObjects) {
						if (
							Ext.isObject(accordionControllerObject) && !Ext.Object.isEmpty(accordionControllerObject)
							&& !Ext.isEmpty(accordionControllerObject.className) && Ext.isString(accordionControllerObject.className)
							&& !Ext.isEmpty(accordionControllerObject.identifier) && Ext.isString(accordionControllerObject.identifier)
						) {
							var accordionController = Ext.create(accordionControllerObject.className, {
								parentDelegate: this, // Inject as parentDelegate in accordion controllers
								identifier: accordionControllerObject.identifier,
								callback: requestBarrier.getCallback('mainViewportAccordionBarrier')
							});

							this.accordionControllers[accordionControllerObject.identifier] = accordionController;

							accordionViewsBuffer.push(accordionController.getView());
						}
					}, this);

					requestBarrier.finalize('mainViewportAccordionBarrier', true);
				}
			},

			/**
			 * @param {String} identifier
			 *
			 * @returns {Boolean} accordionExists
			 */
			mainViewportAccordionControllerExists: function (identifier) {
				var accordionControllerExists = (
					!Ext.isEmpty(identifier) && Ext.isString(identifier)
					&& !Ext.isEmpty(this.accordionControllers[identifier])
				);

				if (!accordionControllerExists)
					_error('accordion controller with identifier "' + identifier + '" not found', this);

				return accordionControllerExists;
			},

			/**
			 * Forwarder method
			 *
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			mainViewportAccordionControllerExpand: function (identifier) {
				if (this.cmfg('mainViewportAccordionControllerExists', identifier))
					this.cmfg('mainViewportAccordionControllerGet', identifier).cmfg('accordionExpand');
			},

			/**
			 * @param {String} identifier
			 *
			 * @returns {Mixed} or null
			 */
			mainViewportAccordionControllerGet: function (identifier) {
				if (this.cmfg('mainViewportAccordionControllerExists', identifier))
					return this.accordionControllers[identifier];

				return null;
			},

			/**
			 * Returns first accordion witch contains a node with give id
			 *
			 * @param {String} id
			 *
			 * @returns {Mixed or null} searchedAccordionController
			 */
			mainViewportAccordionControllerWithNodeWithIdGet: function (id) {
				var searchedAccordionController = this.accordionControllerExpandedGet();

				// First search in expanded accordion
				if (!Ext.isEmpty(searchedAccordionController) && !Ext.isEmpty(searchedAccordionController.cmfg('accordionNodeByIdGet', id)))
					return searchedAccordionController;

				// Then in other ones
				searchedAccordionController = null;

				Ext.Object.each(this.accordionControllers, function (identifier, accordionController, myself) {
					if (!Ext.isEmpty(accordionController) && !Ext.isEmpty(accordionController.cmfg('accordionNodeByIdGet', id))) {
						searchedAccordionController = accordionController;

						return false;
					}
				}, this);

				return searchedAccordionController;
			},

			/**
			 * Forwarder method
			 *
			 * @param {Object} parameters
			 * @param {String} parameters.identifier
			 * @param {Number} parameters.nodeIdToSelect
			 *
			 * @returns {Void}
			 */
			mainViewportAccordionControllerUpdateStore: function (parameters) {
				if (
					Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
					&& this.cmfg('mainViewportAccordionControllerExists', parameters.identifier)
				) {
					parameters.nodeIdToSelect = Ext.isEmpty(parameters.nodeIdToSelect) ? null : parameters.nodeIdToSelect;

					this.cmfg('mainViewportAccordionControllerGet', parameters.identifier).cmfg('accordionUpdateStore', parameters.nodeIdToSelect);
				}
			},

			/**
			 * Forwarder method
			 *
			 * @param {String} identifier
			 *
			 * @returns {Void}
			 */
			mainViewportAccordionDeselect: function (identifier) {
				if (this.cmfg('mainViewportAccordionControllerExists', identifier))
					this.cmfg('mainViewportAccordionControllerGet', identifier).cmfg('accordionDeselect');
			},

			/**
			 * @returns {Boolean}
			 */
			mainViewportAccordionIsCollapsed: function () {
				return !this.isAdministration && CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.HIDE_SIDE_PANEL);
			},

			/**
			 * Forwarder method
			 *
			 * @param {Object} parameters
			 * @param {String} parameters.identifier
			 * @param {Boolean} parameters.state
			 *
			 * @returns {Void}
			 */
			mainViewportAccordionSetDisabled: function (parameters) {
				if (
					Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
					&& this.cmfg('mainViewportAccordionControllerExists', parameters.identifier)
				) {
					parameters.state = Ext.isBoolean(parameters.state) ? parameters.state : true;

					this.cmfg('mainViewportAccordionControllerGet', parameters.identifier).getView().setDisabled(parameters.state);
				}
			},

			/**
			 * Returns expanded accordion's controller
			 *
			 * @returns {Mixed or null} expandedAccordionController
			 *
			 * @private
			 */
			accordionControllerExpandedGet: function () {
				var expandedAccordionController = null;

				Ext.Object.each(this.accordionControllers, function (identifier, accordionController, myself) {
					if (!Ext.isEmpty(accordionController) && !accordionController.getView().getCollapsed()) {
						expandedAccordionController = accordionController;

						return false;
					}
				}, this);

				return expandedAccordionController;
			},

			/**
			 * @returns {Mixed or null} searchedAccordionController
			 *
			 * @private
			 */
			accordionControllerWithSelectableNodeGet: function () {
				var searchedAccordionController = null;

				Ext.Object.each(this.accordionControllers, function (identifier, accordionController, myself) {
					if (!Ext.isEmpty(accordionController) && !Ext.isEmpty(accordionController.cmfg('accordionFirtsSelectableNodeGet'))) {
						searchedAccordionController = accordionController;

						return false;
					}
				}, this);

				return searchedAccordionController;
			},

		// DanglingCard property methods
			/**
			 * @returns {Object}
			 */
			mainViewportDanglingCardGet: function () {
				var danglingCard = Ext.clone(this.danglingCard);

				this.danglingCardReset();

				return danglingCard;
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			danglingCardReset: function () {
				this.danglingCard = null;
			},

			/**
			 * @param {Object} danglingCard
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			danglingCardSet: function (danglingCard) {
				this.danglingCard = danglingCard;
			},

		/**
		 * @param {Object} parameters
		 * @param {Boolean or Object} parameters.activateFirstTab - if object selects object as tab otherwise selects first one
		 * @param {String} parameters.flowStatus
		 * @param {Number} parameters.Id - card id
		 * @param {Number} parameters.IdClass
		 *
		 * @returns {Void}
		 */
		mainViewportCardSelect: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters['Id'])
				&& !Ext.isEmpty(parameters['IdClass'])
			) {
				parameters.activateFirstTab = Ext.isEmpty(parameters.activateFirstTab) ? true : parameters.activateFirstTab;

				var accordionController = this.cmfg('mainViewportAccordionControllerWithNodeWithIdGet', parameters['IdClass']);

				this.danglingCardSet(parameters);

				if (!Ext.isEmpty(accordionController) && Ext.isFunction(accordionController.cmfg)) {
					accordionController.cmfg('accordionExpand', {
						scope: this,
						callback: function (panel, eOpts) {
							accordionController.cmfg('accordionDeselect'); // Instruction required or selection doesn't work if exists another selection
							accordionController.cmfg('accordionNodeByIdSelect', { id: parameters['IdClass'] });
						}
					});
				} else {
					CMDBuild.core.Message.warning(CMDBuild.Translation.warning, CMDBuild.Translation.warnings.itemNotAvailable);
				}
			} else {
				_error('malformed openCard method parameters', this);
			}
		},

		/**
		 * @param {String} name
		 *
		 * @returns {Void}
		 */
		mainViewportInstanceNameSet: function (name) {
			name = Ext.isString(name) ? name : '';

			var instanceNameContainer = Ext.get('instance-name');

			if (!Ext.isEmpty(instanceNameContainer)) {
				try {
					instanceNameContainer.setHTML(name);
				} catch (e) {
					// Prevents some Explorer error
				}
			}
		},

		/**
		 * @returns {Void}
		 *
		 * @administration
		 */
		mainViewportSelectFirstExpandedAccordionSelectableNode: function () {
			var expandedAccordionController = this.accordionControllerExpandedGet();

			if (!Ext.isEmpty(expandedAccordionController)) {
				this.cmfg('mainViewportModuleShow', { identifier: expandedAccordionController.cmfg('accordionIdentifierGet') });

				expandedAccordionController.cmfg('accordionFirstSelectableNodeSelect');
			}
		},

		/**
		 * Select selected entity at first page load
		 *
		 * @returns {Void}
		 *
		 * @management
		 */
		mainViewportStartingEntitySelect: function () {
			var startingClassId = (
				CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.STARTING_CLASS_ID) // Group's starting class
				|| CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.STARTING_CLASS) // Main configuration's starting class
			);
			var accordionWithNodeController = Ext.isEmpty(startingClassId) ? null : this.cmfg('mainViewportAccordionControllerWithNodeWithIdGet', startingClassId);
			var node = null;

			if (!Ext.isEmpty(accordionWithNodeController)) {
				accordionWithNodeController.disableSelection = true; // Disable first accordion selection to avoid double node selection
				accordionWithNodeController.cmfg('accordionExpand', {
					scope: this,
					callback: function (panel, eOpts) {
						accordionWithNodeController.cmfg('accordionNodeByIdSelect', { id: startingClassId });
					}
				});

				node = accordionWithNodeController.cmfg('accordionNodeByIdGet', startingClassId); // To manage selection if accordion are collapsed
			} else { // If no statingClass to select try to select fist selectable node
				var accordionController = this.accordionControllerWithSelectableNodeGet();

				if (!Ext.isEmpty(accordionController)) {
					accordionController.cmfg('accordionFirstSelectableNodeSelect');

					node = accordionController.cmfg('accordionFirtsSelectableNodeGet'); // To manage selection if accordion are collapsed
				}
			}

			// Manage selection if accordion are collapsed
			if (this.cmfg('mainViewportAccordionIsCollapsed') && !Ext.isEmpty(node))
				this.cmfg('mainViewportModuleShow', {
					identifier: node.get('cmName'),
					parameters: node
				});
		},

		// Module manage methods
			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			moduleControllerBuild: function () {
				if (!Ext.isEmpty(this.module) && Ext.isArray(this.module)) {
					var moduleViewsBuffer = [];

					Ext.Array.forEach(this.module, function (moduleControllerObject, i, allModuleControllerObjects) {
						if (Ext.isObject(moduleControllerObject) && !Ext.Object.isEmpty(moduleControllerObject)) {
							if (
								!Ext.isEmpty(moduleControllerObject.className) && Ext.isString(moduleControllerObject.className)
								&& !Ext.isEmpty(moduleControllerObject.identifier) && Ext.isString(moduleControllerObject.identifier)
							) { // New implementation standard
								var moduleController = Ext.create(moduleControllerObject.className, {
									parentDelegate: this, // Inject as parentDelegate in accordion controllers
									identifier: moduleControllerObject.identifier
								});

								this.moduleControllers[moduleControllerObject.identifier] = moduleController;

								moduleViewsBuffer.push(moduleController.getView());
							} else if (
								!Ext.isEmpty(moduleControllerObject.cmName) && Ext.isString(moduleControllerObject.cmName)
								&& !Ext.isEmpty(moduleControllerObject.cmfg) && Ext.isFunction(moduleControllerObject.cmfg)
							) { // @deprecated
								if (!Ext.isEmpty(moduleControllerObject.cmfg('identifierGet'))) {
									moduleControllerObject.parentDelegate = this; // Inject as parentDelegate in module controllers

									this.moduleControllers[moduleControllerObject.cmfg('identifierGet')] = moduleControllerObject;

									moduleViewsBuffer.push(moduleControllerObject.getView());
								}
							} else if (!Ext.isEmpty(moduleControllerObject.cmName) && Ext.isString(moduleControllerObject.cmName)) { // @deprecated
								this.moduleControllers[moduleControllerObject.cmName] = moduleControllerObject.delegate;

								if (Ext.isFunction(moduleControllerObject.cmControllerType)) {
									// We start to use the cmcreate factory method to have the possibility to inject the sub-controllers in tests
									if (Ext.isFunction(moduleControllerObject.cmControllerType.cmcreate)) {
										this.moduleControllers[moduleControllerObject.cmName] = new moduleControllerObject.cmControllerType.cmcreate(moduleControllerObject);
									} else {
										this.moduleControllers[moduleControllerObject.cmName] = new moduleControllerObject.cmControllerType(moduleControllerObject);
									}
								} else if (Ext.isString(moduleControllerObject.cmControllerType)) { // To use Ext.loader to asynchronous load also controllers
									this.moduleControllers[moduleControllerObject.cmName] = Ext.create(moduleControllerObject.cmControllerType, moduleControllerObject);
								} else {
									this.moduleControllers[moduleControllerObject.cmName] = new CMDBuild.controller.CMBasePanelController(moduleControllerObject);
								}

								moduleViewsBuffer.push(moduleControllerObject);
							}
						}
					}, this);

					if (!Ext.isEmpty(moduleViewsBuffer))
						this.moduleContainer.add(moduleViewsBuffer);
				}
			},

			/**
			 * @param {String} identifier
			 *
			 * @returns {Boolean}
			 */
			mainViewportModuleControllerExists: function (identifier) {
				return (
					!Ext.isEmpty(identifier) && Ext.isString(identifier)
					&& !Ext.isEmpty(this.moduleControllers[identifier])
				);
			},

			/**
			 * @param {String} identifier
			 *
			 * @returns {Mixed} or null
			 */
			mainViewportModuleControllerGet: function (identifier) {
				if (this.cmfg('mainViewportModuleControllerExists', identifier))
					return this.moduleControllers[identifier];

				return null;
			},

			/**
			 * Show module view
			 *
			 * @param {Object} parameters
			 * @param {String} parameters.identifier
			 * @param {Object} parameters.parameters
			 *
			 * @returns {Boolean} toShow
			 */
			mainViewportModuleShow: function (parameters) {
				var toShow = false;

				if (
					!Ext.Object.isEmpty(parameters)
					&& this.cmfg('mainViewportModuleControllerExists', parameters.identifier)
				) {
					parameters.parameters = Ext.isEmpty(parameters.parameters) ? null : parameters.parameters;

					var modulePanel = this.cmfg('mainViewportModuleControllerGet', parameters.identifier);

					if (!Ext.isEmpty(modulePanel) && Ext.isFunction(modulePanel.getView)) {
						modulePanel = modulePanel.getView();
					} else if (!Ext.isEmpty(modulePanel) && !Ext.isEmpty(modulePanel.view)) { // @deprecated
						modulePanel = modulePanel.view;
					}

					toShow = !Ext.isFunction(modulePanel.beforeBringToFront) || modulePanel.beforeBringToFront(parameters.parameters) !== false; // @deprecated

					if (!Ext.isEmpty(modulePanel)) {
						if (toShow)
							this.moduleContainer.layout.setActiveItem(modulePanel.getId());

						/**
						 * Legacy event
						 *
						 * @deprecated
						 */
						modulePanel.fireEvent('CM_iamtofront', parameters.parameters);

						// FireEvent not used because of problems to pass right parameters to cmfg() function
						if (!Ext.isEmpty(modulePanel.delegate) && Ext.isFunction(modulePanel.delegate.cmfg))
							modulePanel.delegate.cmfg('onModuleInit', parameters.parameters);
					}

					return toShow;
				}

				return toShow;
			},

		/**
		 * Synchronize Class and Workflow selection from relative accordions with Navigation accordion, active only in management side
		 *
		 * @param {Object} parameters
		 * @param {String} parameters.sourceAccordionIdentifier
		 * @param {CMDBuild.model.common.Accordion} parameters.selectedNodeModel
		 *
		 * @returns {Void}
		 */
		onMainViewportAccordionSelect: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& Ext.isObject(parameters.selectedNodeModel) && !Ext.Object.isEmpty(parameters.selectedNodeModel)
				&& Ext.isString(parameters.sourceAccordionIdentifier) && !Ext.isEmpty(parameters.sourceAccordionIdentifier)
				&& Ext.Array.contains(this.enableSynchronizationForAccordions, parameters.sourceAccordionIdentifier)
				&& !this.isAdministration
			) {
				var menuAccordionController = this.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getNavigation());
				var selectedNodeModel = parameters.selectedNodeModel;

				if (menuAccordionController.cmfg('accordionNodeByIdExists', selectedNodeModel.get(CMDBuild.core.constants.Proxy.ENTITY_ID)))
					menuAccordionController.cmfg('accordionNodeByIdSelect', {
						id: selectedNodeModel.get(CMDBuild.core.constants.Proxy.ENTITY_ID),
						mode: 'silently'
					});
			}
		},

		/**
		 * Manages footer credits link click action
		 *
		 * @returns {Void}
		 */
		onMainViewportCreditsClick: function () {
			Ext.create('CMDBuild.core.window.Credits').show();
		}
	});

})();
