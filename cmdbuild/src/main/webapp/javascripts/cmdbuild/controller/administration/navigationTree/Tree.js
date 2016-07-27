(function () {

	Ext.define('CMDBuild.controller.administration.navigationTree.Tree', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.Classes',
			'CMDBuild.proxy.domain.Domain',
			'CMDBuild.core.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.navigationTree.NavigationTree}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'navigationTreeTabTreeDataGet',
			'onNavigationTreeTabTreeAbortButtonClick',
			'onNavigationTreeTabTreeAddButtonClick',
			'onNavigationTreeTabTreeCheckChange',
			'onNavigationTreeTabTreeModifyButtonClick',
			'onNavigationTreeTabTreeNodeExpand',
			'onNavigationTreeTabTreeSelected = onNavigationTreeSelected'
		],

		/**
		 * @property {CMDBuild.view.administration.navigationTree.tree.FormPanel}
		 */
		form: undefined,

		/**
		 * Local cache used to avoid too many server calls
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		localCache: {
			entryTypes: {}, // NAME as key
			domains: {} // ID_DOMAIN as key
		},

		/**
		 * @property {CMDBuild.view.administration.navigationTree.tree.TreePanel}
		 */
		tree: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.navigationTree.tree.TreeView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.navigationTree.NavigationTree} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.navigationTree.tree.TreeView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.tree = this.form.tree;
		},

		/**
		 * Build tree nodes searching from ancestors domain and excluding one
		 *
		 * @param {CMDBuild.model.navigationTree.Class} entryType
		 *
		 * @returns {Array} nodes
		 *
		 * @private
		 */
		buildNodesArray: function (entryType) {
			var nodes = [];

			if (Ext.isObject(entryType) && !Ext.Object.isEmpty(entryType)) {
				var ancestorsDomains = this.getDomainsWithEntryType(entryType);

				if (!Ext.isEmpty(ancestorsDomains) && Ext.isArray(ancestorsDomains)) {
					var entryTypeName = entryType.get(CMDBuild.core.constants.Proxy.NAME);

					Ext.Array.each(ancestorsDomains, function (anchestorDomainModel, i, allAnchestorDomainModels) {
						if (!Ext.isEmpty(anchestorDomainModel)) {
							var oppositeEntryTypeModel = anchestorDomainModel.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME);
							var domainDescription = anchestorDomainModel.get(CMDBuild.core.constants.Proxy.INVERSE_DESCRIPTION);

							if (entryTypeName == anchestorDomainModel.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME)) {
								oppositeEntryTypeModel = anchestorDomainModel.get(CMDBuild.core.constants.Proxy.DESTINATION_CLASS_NAME);
								domainDescription = anchestorDomainModel.get(CMDBuild.core.constants.Proxy.DIRECT_DESCRIPTION);
							}

							oppositeEntryTypeModel = this.localCacheEntryTypeGet(oppositeEntryTypeModel); // Get full model

							// Build node object
							var childObject = {
								children: [{}], // Fake node to enable node expand
								checked: false, // Enables checkbox
								expandable: true,
								leaf: false
							};
							childObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = anchestorDomainModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION)
								+ ' (' + domainDescription + ' ' + oppositeEntryTypeModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION) + ')';
							childObject[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = oppositeEntryTypeModel;
							childObject[CMDBuild.core.constants.Proxy.DOMAIN] = anchestorDomainModel;

							nodes.push(childObject);
						}
					}, this);
				}
			} else {
				_error('wrong or malformed buildNodesArray method parameters', this);
			}

			return nodes;
		},

		/**
		 * @param {CMDBuild.model.navigationTree.TreeNode} node
		 *
		 * @returns {Array} checkedChildren
		 *
		 * @private
		 */
		getCheckedChild: function (node) {
			var checkedChildren = [];

			if (!Ext.isEmpty(node) && node.hasChildNodes())
				node.eachChild(function (childNode) {
					if (!Ext.isEmpty(childNode) && childNode.get('checked')) {
						var domainModel = childNode.get(CMDBuild.core.constants.Proxy.DOMAIN);
						var entryTypeModel = childNode.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE);

						var childObject = { direct: true };
						childObject[CMDBuild.core.constants.Proxy.BASE_NODE] = childNode.isRoot();
						childObject[CMDBuild.core.constants.Proxy.CHILD_NODES] = this.getCheckedChild(childNode);
						childObject[CMDBuild.core.constants.Proxy.DOMAIN_NAME] = domainModel.get(CMDBuild.core.constants.Proxy.NAME);
						childObject[CMDBuild.core.constants.Proxy.ENABLE_RECURSION] = childNode.get(CMDBuild.core.constants.Proxy.ENABLE_RECURSION);
						childObject[CMDBuild.core.constants.Proxy.FILTER] = childNode.get(CMDBuild.core.constants.Proxy.FILTER);
						childObject[CMDBuild.core.constants.Proxy.TARGET_CLASS_DESCRIPTION] = entryTypeModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION);
						childObject[CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME] = entryTypeModel.get(CMDBuild.core.constants.Proxy.NAME);

						checkedChildren.push(childObject);
					}
				}, this);

			return checkedChildren;
		},

		/**
		 * Get all domains where origin or destinations is entryTypeId (also filters excludeDomainId)
		 *
		 * @param {Number} entryType
		 *
		 * @returns {Array} ancestorsDomains
		 *
		 * @private
		 */
		getDomainsWithEntryType: function (entryType) {
			var ancestorsDomains = [];

			if (Ext.isObject(entryType) && !Ext.Object.isEmpty(entryType)) {
				var ancestorsId = CMDBuild.core.Utils.getEntryTypeAncestorsId(entryType);

				// Retrieve entryType related domains
				if (!Ext.isEmpty(ancestorsId) && Ext.isArray(ancestorsId))
					this.localCacheDomainEach(function (id, model, myself) {
						if (
							Ext.Array.contains(ancestorsId, model.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_ID))
							|| Ext.Array.contains(ancestorsId, model.get(CMDBuild.core.constants.Proxy.DESTINATION_CLASS_ID))
						) {
							ancestorsDomains.push(model);
						}
					}, this);

			} else {
				_error('wrong or malformed getDomainsWithEntryType method parameters', this);
			}

			return ancestorsDomains;
		},

		/**
		 * Walk through tree and stateObject to fill tree nodes with check values and filters
		 *
		 * @param {CMDBuild.model.navigationTree.TreeNode} node
		 * @param {Array} stateObjectArray
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		loadNodesCheckState: function (node, stateObjectArray) {
			if (
				!Ext.isEmpty(node)
				&& !Ext.isEmpty(stateObjectArray) && Ext.isArray(stateObjectArray)
			) {
				node.expand();

				Ext.Array.each(stateObjectArray, function (stateObject, i, allStateObjects) {
					if (Ext.isObject(stateObject) && !Ext.Object.isEmpty(stateObject)) {
						var soughtNode = node.findChildBy(function (childNode) {
							var domainModel = childNode.get(CMDBuild.core.constants.Proxy.DOMAIN);
							var entryTypeModel = childNode.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE);

							return (
								domainModel.get(CMDBuild.core.constants.Proxy.NAME) == stateObject[CMDBuild.core.constants.Proxy.DOMAIN_NAME]
								&& entryTypeModel.get(CMDBuild.core.constants.Proxy.NAME) == stateObject[CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME]
								&& entryTypeModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION) == stateObject[CMDBuild.core.constants.Proxy.TARGET_CLASS_DESCRIPTION]
							);
						}, this);

						if (!Ext.isEmpty(soughtNode)) {
							soughtNode.set('checked', true);
							soughtNode.set(CMDBuild.core.constants.Proxy.ENABLE_RECURSION, stateObject[CMDBuild.core.constants.Proxy.ENABLE_RECURSION]);
							soughtNode.set(CMDBuild.core.constants.Proxy.FILTER, stateObject[CMDBuild.core.constants.Proxy.FILTER]);

							this.loadNodesCheckState(soughtNode, stateObject[CMDBuild.core.constants.Proxy.CHILD_NODES]);
						}
					}
				}, this);
			}
		},

		// Domain local cache methods
			/**
			 * @param {Function} callback
			 * @param {Object} scope
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			localCacheDomainEach: function (callback, scope) {
				if (Ext.isFunction(callback))
					Ext.Object.each(this.localCache.domains, callback, scope);
			},

			/**
			 * @param {Array} domainsArray
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			localCacheDomainSet: function (domainsArray) {
				if (!Ext.isEmpty(domainsArray) && Ext.isArray(domainsArray))
					Ext.Array.each(domainsArray, function (domainObject, i, allDomainObjects) {
						if (Ext.isObject(domainObject) && !Ext.Object.isEmpty(domainObject))
							this.localCache.domains[domainObject[CMDBuild.core.constants.Proxy.ID_DOMAIN]] = Ext.create('CMDBuild.model.navigationTree.Domain', domainObject);
					}, this);
			},

		// EntryType local cache methods
			/**
			 * @param {String} entryTypeName
			 *
			 * @returns {Mixed}
			 *
			 * @private
			 */
			localCacheEntryTypeGet: function (entryTypeName, attributeName) {
				if (!Ext.isEmpty(entryTypeName) && Ext.isString(entryTypeName))
					if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName)) {
						return this.localCache.entryTypes[entryTypeName].get(attributeName);
					} else {
						return this.localCache.entryTypes[entryTypeName];
					}

				return null;
			},

			/**
			 * @param {Array} classesArray
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			localCacheEntryTypeSet: function (classesArray) {
				if (!Ext.isEmpty(classesArray) && Ext.isArray(classesArray))
					Ext.Array.each(classesArray, function (entryTypeObject, i, allEntryTyopeObjects) {
						if (Ext.isObject(entryTypeObject) && !Ext.Object.isEmpty(entryTypeObject))
							this.localCache.entryTypes[entryTypeObject[CMDBuild.core.constants.Proxy.NAME]] = Ext.create('CMDBuild.model.navigationTree.Class', entryTypeObject);
					}, this);
			},

		/**
		 * @param {string} type
		 *
		 * @returns {Mixed}
		 */
		navigationTreeTabTreeDataGet: function (type) {
			switch (type) {
				case 'childNodes': // Returns root's child nodes array
					return this.getCheckedChild(this.tree.getStore().getRootNode());

				case 'rootFilter': // Returns root's data
					return this.tree.getStore().getRootNode().get(CMDBuild.core.constants.Proxy.FILTER);

				default:
					return null;
			}
		},

		/**
		 * @returns {Void}
		 */
		onNavigationTreeTabTreeAbortButtonClick: function () {
			if (this.cmfg('navigationTreeSelectedTreeIsEmpty')) {
				this.tree.getStore().getRootNode().removeAll();
				this.tree.getSelectionModel().deselectAll();

				this.form.setDisabledModify(true, true, true);
			} else {
				this.cmfg('onNavigationTreeTabTreeSelected');
			}
		},

		/**
		 * @returns {Void}
		 */
		onNavigationTreeTabTreeAddButtonClick: function () {
			this.view.setDisabled(true);

			this.tree.getStore().getRootNode().removeAll();
		},

		/**
		 * @param {Object} parameters
		 * @param {CMDBuild.model.navigationTree.TreeNode} parameters.node
		 * @param {Boolean} parameters.checked
		 *
		 * @returns {Void}
		 */
		onNavigationTreeTabTreeCheckChange: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters.node)
			) {
				parameters.checked = Ext.isBoolean(parameters.checked) ? parameters.checked : false;

				var node = parameters.node;

				if (parameters.checked) {
					while (!Ext.isEmpty(node.parentNode)) {
						node.set('checked', true);
						node = node.parentNode;
					}
				} else {
					this.unCheckChildNodes(node);
				}
			} else {
				_error('wrong or malformed onNavigationTreeTabTreeCheckChange method parameters', this);
			}
		},

		/**
		 * @returns {Void}
		 */
		onNavigationTreeTabTreeModifyButtonClick: function () {
			this.form.setDisabledModify(false);
			this.form.setDisableFields(false, false, true); // To enable also if not visible

			this.tree.getView().refresh(); // Fixes enable/disable checkcolumn problems
		},

		/**
		 * @param {CMDBuild.model.navigationTree.TreeNode} node
		 *
		 * @returns {Void}
		 */
		onNavigationTreeTabTreeNodeExpand: function (node) {
			if (!Ext.isEmpty(node)) {
				var nodeEntryType = node.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE);
				var nodes = this.buildNodesArray(nodeEntryType);

				if (!Ext.isEmpty(nodes)) {
					CMDBuild.core.Utils.objectArraySort(nodes, CMDBuild.core.constants.Proxy.DESCRIPTION);

					node.removeAll();
					node.appendChild(nodes);
				}
			} else {
				_error('wrong or malformed onNavigationTreeTabTreeNodeExpand method parameters', this);
			}
		},

		/**
		 * Build root node and Classes/Domains local cache to avoid too many server calls
		 *
		 * @returns {Void}
		 */
		onNavigationTreeTabTreeSelected: function () {
			this.view.setDisabled(this.cmfg('navigationTreeSelectedTreeIsEmpty'));

			this.tree.getStore().getRootNode().removeAll();

			if (this.cmfg('navigationTreeSelectedTreeIsEmpty')) {
				this.form.setDisabledModify(true, true, true, true);
			} else {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false; // Also inactive to get all processes if shark isn't on

				CMDBuild.proxy.Classes.read({ // TODO: waiting for refactor (CRUD)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (!Ext.isEmpty(decodedResponse)) {
							this.localCacheEntryTypeSet(decodedResponse);

							var targetClassModel = this.localCacheEntryTypeGet(this.cmfg('navigationTreeSelectedTreeGet', CMDBuild.core.constants.Proxy.TARGET_CLASS_NAME));

							// Build root node
							var rootNodeObject = {
								children: [{}],
								checked: true,
								expandable: true,
								expanded: false,
								leaf: false
							};
							rootNodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = targetClassModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION);
							rootNodeObject[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = targetClassModel;

							this.tree.getStore().setRootNode(rootNodeObject);

							CMDBuild.proxy.domain.Domain.readAll({
								scope: this,
								success: function (response, options, decodedResponse) {
									decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

									if (!Ext.isEmpty(decodedResponse)) {
										this.localCacheDomainSet(decodedResponse);

										this.form.setDisabledModify(true);

										if (!this.cmfg('navigationTreeSelectedTreeIsEmpty', CMDBuild.core.constants.Proxy.CHILD_NODES))
											this.tree.getStore().getRootNode().set( // Setup root node filter
												CMDBuild.core.constants.Proxy.FILTER,
												this.cmfg('navigationTreeSelectedTreeGet', CMDBuild.core.constants.Proxy.FILTER)
											);

										this.loadNodesCheckState(
											this.tree.getStore().getRootNode(),
											this.cmfg('navigationTreeSelectedTreeGet', CMDBuild.core.constants.Proxy.CHILD_NODES)
										);
									}
								}
							});
						}
					}
				});
			}
		},

		/**
		 * @param {CMDBuild.model.navigationTree.TreeNode} node
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		unCheckChildNodes: function (node) {
			if (!Ext.isEmpty(node) && Ext.isFunction(node.hasChildNodes) && node.hasChildNodes())
				node.eachChild(function (childNode) {
					childNode.set('checked', false);

					this.unCheckChildNodes(childNode);
				}, this);
		}
	});

})();
