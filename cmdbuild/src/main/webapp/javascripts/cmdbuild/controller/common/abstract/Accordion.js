(function () {

	// External implementation to avoid overrides
	Ext.require(['CMDBuild.core.constants.Proxy']);

	/**
	 * Common methods
	 *
	 * Required managed functions:
	 * 	- accordionBuildId'
	 * 	- accordionDeselect'
	 * 	- accordionExpand'
	 * 	- accordionFirstSelectableNodeSelect'
	 * 	- accordionFirtsSelectableNodeGet'
	 * 	- accordionIdentifierGet'
	 * 	- accordionNodeByIdExists'
	 * 	- accordionNodeByIdGet'
	 * 	- accordionNodeByIdSelect'
	 * 	- accordionUpdateStore'
	 * 	- onAccordionBeforeSelect'
	 * 	- onAccordionExpand
	 * 	- onAccordionSelectionChange
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.abstract.Accordion', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * Store update callback functions
		 *
		 * @cfg {Function}
		 */
		callback: undefined,

		/**
		 * Flag to disable next selection, will be reset on next store update
		 *
		 * @cfg {Boolean}
		 */
		disableSelection: false,

		/**
		 * Flag to disable next storeLoad, will be reset on next expand
		 *
		 * @cfg {Boolean}
		 */
		disableStoreLoad: false,

		/**
		 * @cfg {Boolean}
		 */
		hideIfEmpty: false,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.model.common.Accordion}
		 */
		lastSelection: undefined,

		/**
		 * Store update scope object
		 *
		 * @cfg {Object}
		 */
		scope: this,

		/**
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * Generates an unique id for the menu accordion, prepend to components array "accordion" string and identifier.
		 *
		 * @param {Array} components
		 *
		 * @returns {String}
		 */
		accordionBuildId: function (components) {
			if (!Ext.isEmpty(components)) {
				components = Ext.isArray(components) ? Ext.Array.clean(components) : [components];
				components = Ext.Array.push([CMDBuild.core.constants.Proxy.ACCORDION, this.cmfg('accordionIdentifierGet')], components);

				Ext.Array.each(components, function (component, i, allComponents) {
					components[i] = Ext.String.trim(String(component));
				}, this);

				return components.join('-');
			}

			return CMDBuild.core.constants.Proxy.ACCORDION + '-' + this.cmfg('accordionIdentifierGet') + '-' + new Date().valueOf(); // Compatibility mode with IE older than IE 9 (Date.now())
		},

		/**
		 * @returns {Void}
		 */
		accordionDeselect: function () {
			this.view.getSelectionModel().deselectAll();

			this.cmfg('onAccordionSelectionChange');
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 */
		accordionExpand: function (parameters) {
			if (!Ext.isEmpty(this.view)) {
				if (
					Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
					&& !Ext.isEmpty(parameters.callback) && Ext.isFunction(parameters.callback)
				) {
					parameters.scope = Ext.isEmpty(parameters.scope) ? this : parameters.scope;

					if (this.view.getCollapsed() === false && this.view.isVisible()) { // Accordion already expanded
						Ext.callback(parameters.callback, parameters.scope);
					} else { // Accordion needs to be expanded
						this.view.on('expand', parameters.callback, parameters.scope, { single: true });
					}
				}

				this.view.expand();
			}
		},

		// First selectable node manage methods
			/**
			 * @returns {CMDBuild.model.common.Accordion or null} node
			 */
			accordionFirtsSelectableNodeGet: function () {
				var node = null;

				if (!this.view.isDisabled()) {
					var inspectedNode = this.view.getRootNode();

					while (inspectedNode) {
						if (this.isNodeSelectable(inspectedNode)) {
							node = inspectedNode;

							break;
						} else {
							inspectedNode = inspectedNode.firstChild;
						}
					}
				}

				return node;
			},

			/**
			 * @returns {Void}
			 */
			accordionFirstSelectableNodeSelect: function () {
				var firstSelectableNode = this.cmfg('accordionFirtsSelectableNodeGet');

				if (!Ext.isEmpty(firstSelectableNode)) {
					this.cmfg('accordionExpand', {
						scope: this,
						callback: function (panel, eOpts) {
							this.cmfg('accordionNodeByIdSelect', { id: firstSelectableNode.get(CMDBuild.core.constants.Proxy.ID) });
						}
					});
				}
			},

		/**
		 * @returns {String or null}
		 */
		accordionIdentifierGet: function () {
			if (!Ext.isEmpty(this.identifier))
				return this.identifier;

			return null;
		},

		// Node by Id manage methods
			/**
			 * @param {Number or String} id
			 *
			 * @returns {Boolean}
			 */
			accordionNodeByIdExists: function (id) {
				return !Ext.isEmpty(this.cmfg('accordionNodeByIdGet', id));
			},

			/**
			 * Search in entityId and id parameter
			 *
			 * @param {Number or String} id
			 *
			 * @returns {CMDBuild.model.common.Accordion}
			 */
			accordionNodeByIdGet: function (id) {
				return (
					this.view.getStore().getRootNode().findChild(CMDBuild.core.constants.Proxy.ID, id, true)
					|| this.view.getStore().getRootNode().findChild(CMDBuild.core.constants.Proxy.ENTITY_ID, id, true)
				);
			},

			/**
			 * @param {Object} parameters
			 * @param {Number or String} parameters.id
			 * @param {String} parameters.mode [normal || silently]
			 *
			 * @returns {Void}
			 */
			accordionNodeByIdSelect: function (parameters) {
				parameters = Ext.isObject(parameters) ? parameters : {};

				if (!Ext.Object.isEmpty(parameters) && !Ext.isEmpty(parameters.id)) {
					var node = this.cmfg('accordionNodeByIdGet', parameters.id);

					if (!Ext.isEmpty(node)) {
						node.bubble(function () {
							this.expand();
						});

						this.view.getSelectionModel().select(
							node,
							false,
							Ext.isString(parameters.mode) && parameters.mode == 'silently' // Silently mode
						);
					} else {
						this.cmfg('accordionFirstSelectableNodeSelect');
					}
				}
			},

		/**
		 * @param {Number or String} nodeIdToSelect
		 *
		 * @returns {Void}
		 *
		 * @abstract
		 */
		accordionUpdateStore: Ext.emptyFn,

		/**
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isEmpty: function () {
			return !this.view.getStore().getRootNode().hasChildNodes();
		},

		/**
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isNodeSelectable: function (node) {
			return (
				!node.isRoot() // Root is hidden by default
				&& node.get(CMDBuild.core.constants.Proxy.SELECTABLE)
				&& !Ext.isEmpty(node.get(CMDBuild.core.constants.Proxy.ID)) // Node without id property are not selectable
			);
		},

		/**
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @returns {Boolean}
		 */
		onAccordionBeforeSelect: function (node) {
			return this.isNodeSelectable(node);
		},

		/**
		 * @returns {Void}
		 */
		onAccordionExpand: function () {
			this.cmfg('mainViewportModuleShow', { identifier: this.cmfg('accordionIdentifierGet') });

			// Update store
			if (!this.disableStoreLoad) {
				if (this.view.getSelectionModel().hasSelection()) {
					var selection = this.view.getSelectionModel().getSelection()[0];

					this.cmfg('accordionDeselect');
					this.cmfg('accordionUpdateStore', selection.get(CMDBuild.core.constants.Proxy.ENTITY_ID));
				} else {
					this.cmfg('accordionUpdateStore');
				}
			}

			// DisableStoreLoad flag reset
			this.disableStoreLoad = false;
		},

		/**
		 * @returns {Void}
		 */
		onAccordionSelectionChange: function () {
			if (this.view.getSelectionModel().hasSelection()) {
				var selection = this.view.getSelectionModel().getSelection()[0];

				if (
					!this.cmfg('mainViewportModuleShow', {
						identifier: selection.get('cmName'),
						parameters: selection
					})
				) {
					// If the panel was not brought to front (report from the navigation menu), select the previous node or deselect the tree
					if (!Ext.isEmpty(this.lastSelection)) {
						this.view.getSelectionModel().select(this.lastSelection);
					} else {
						this.view.getSelectionModel().deselectAll(true);
					}
				} else {
					this.lastSelection = selection;
				}

				// Notify accordion selection event to mainViewport's controller (accordion selection synchronizations)
				this.cmfg('onMainViewportAccordionSelect', {
					sourceAccordionIdentifier: this.cmfg('accordionIdentifierGet'),
					selectedNodeModel: selection
				});
			}
		},

		/**
		 * @param {Number or String} nodeIdToSelect
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		updateStoreCommonEndpoint: function (nodeIdToSelect) {
			if (!this.disableSelection) {
				if (!Ext.isEmpty(nodeIdToSelect))
					this.cmfg('accordionNodeByIdSelect', { id: nodeIdToSelect });

				// Select first selectable item if no selection and expanded
				if (!this.view.getSelectionModel().hasSelection() && this.view.getCollapsed() === false && this.view.isVisible())
					this.cmfg('accordionFirstSelectableNodeSelect');
			}

			// Accordion store update end event fire
			this.view.fireEvent('storeload');

			// Hide if accordion is empty
			if (this.hideIfEmpty && this.isEmpty())
				this.view.hide();

			// Accordion creation callback
			if (!Ext.isEmpty(this.callback) && Ext.isFunction(this.callback))
				Ext.callback(this.callback, this.scope);

			// DisableSelection flag reset
			this.disableSelection = false;
		}
	});

})();
