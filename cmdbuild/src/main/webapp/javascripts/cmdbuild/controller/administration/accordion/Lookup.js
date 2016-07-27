(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Lookup', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.lookup.Type'
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'accordionBuildId',
			'accordionDeselect',
			'accordionExpand',
			'accordionFirstSelectableNodeSelect',
			'accordionFirtsSelectableNodeGet',
			'accordionIdentifierGet',
			'accordionNodeByIdExists',
			'accordionNodeByIdGet',
			'accordionNodeByIdSelect',
			'accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.Lookup}
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

			this.view = Ext.create('CMDBuild.view.administration.accordion.Lookup', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		accordionUpdateStore: function (nodeIdToSelect) {
			nodeIdToSelect = Ext.isString(nodeIdToSelect) ? nodeIdToSelect : null;

			CMDBuild.proxy.lookup.Type.readAll({
				scope: this,
				success: function (response, options, decodedResponse) {
					if (!Ext.isEmpty(decodedResponse)) {
						var nodes = [];
						var nodesMap = {};

						// Build nodes map
						Ext.Array.forEach(decodedResponse, function (lookupTypeObject, i, allLookupTypeObjects) {
							var nodeObject = {};
							nodeObject['iconCls'] = 'cmdb-tree-lookup-icon';
							nodeObject['cmName'] = this.cmfg('accordionIdentifierGet');
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = lookupTypeObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = lookupTypeObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = lookupTypeObject[CMDBuild.core.constants.Proxy.ID];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', lookupTypeObject[CMDBuild.core.constants.Proxy.ID]);
							nodeObject[CMDBuild.core.constants.Proxy.PARENT] = lookupTypeObject[CMDBuild.core.constants.Proxy.PARENT];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							nodesMap[lookupTypeObject[CMDBuild.core.constants.Proxy.ID]] = nodeObject;
						}, this);

						this.view.getStore().getRootNode().removeAll();

						// Build tree nodes hierarchy
						Ext.Object.each(nodesMap, function (id, node, myself) {
							if (Ext.isEmpty(node[CMDBuild.core.constants.Proxy.PARENT])) {
								nodes.push(node);
							} else {
								var parentNode = nodesMap[node[CMDBuild.core.constants.Proxy.PARENT]];

								if (!Ext.isEmpty(parentNode)) {
									parentNode.children = parentNode.children || [];
									parentNode.children.push(node);

									parentNode.iconCls = 'cmdb-tree-superLookup-icon';
									parentNode.leaf = false;
								}
							}
						}, this);

						if (!Ext.isEmpty(nodes)) {
							this.view.getStore().getRootNode().appendChild(nodes);
							this.view.getStore().sort();
						}
					}

					// Alias of this.callParent(arguments), inside proxy function doesn't work
					this.updateStoreCommonEndpoint(nodeIdToSelect);
				}
			});

			this.callParent(arguments);
		}
	});

})();
