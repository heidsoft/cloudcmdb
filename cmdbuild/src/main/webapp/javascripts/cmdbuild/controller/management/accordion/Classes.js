(function () {

	Ext.define('CMDBuild.controller.management.accordion.Classes', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.Classes'
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
			'onAccordionClassesCollapse',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * @cfg {Boolean}
		 */
		hideIfEmpty: true,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.management.accordion.Classes}
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

			this.view = Ext.create('CMDBuild.view.management.accordion.Classes', { delegate: this });

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
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			CMDBuild.proxy.Classes.readAll({
				params: params,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES] || [];

					var nodes = [];
					var standard = [];
					var simple = [];
					var standardNodesMap = {};

					// Removes all processes and root class from response
					decodedResponse = Ext.Array.filter(decodedResponse, function (item, i, array) {
						return (
							item[CMDBuild.core.constants.Proxy.TYPE] != CMDBuild.core.constants.Global.getTableTypeProcessClass() // Discard processes
							&& item[CMDBuild.core.constants.Proxy.NAME] != CMDBuild.core.constants.Global.getRootNameClasses() // Discard root class of all classes
							&& !item[CMDBuild.core.constants.Proxy.SYSTEM] // Discard system classes
						);
					}, this);

					this.view.getStore().getRootNode().removeAll();

					if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
						Ext.Array.each(decodedResponse, function (classObject, i, allClassObjects) {
							var nodeObject = {};
							nodeObject['cmName'] = this.cmfg('accordionIdentifierGet');
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = classObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = classObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = classObject[CMDBuild.core.constants.Proxy.ID];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', classObject[CMDBuild.core.constants.Proxy.ID]);
							nodeObject[CMDBuild.core.constants.Proxy.NAME] = classObject[CMDBuild.core.constants.Proxy.NAME];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							if (classObject[CMDBuild.core.constants.Proxy.TABLE_TYPE] == CMDBuild.core.constants.Global.getTableTypeSimpleTable()) {
								nodeObject['iconCls'] = 'cmdb-tree-class-icon';

								simple.push(nodeObject);
							} else { // Standard nodes map build
								nodeObject['iconCls'] = classObject['superclass'] ? 'cmdb-tree-superclass-icon' : 'cmdb-tree-class-icon';
								nodeObject[CMDBuild.core.constants.Proxy.PARENT] = classObject[CMDBuild.core.constants.Proxy.PARENT];

								standardNodesMap[nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID]] = nodeObject;
							}
						}, this);

						// Builds full standard/simple classes trees
						for (var id in standardNodesMap) {
							var node = standardNodesMap[id];

							if (
								!Ext.isEmpty(node[CMDBuild.core.constants.Proxy.PARENT])
								&& !Ext.isEmpty(standardNodesMap[node[CMDBuild.core.constants.Proxy.PARENT]])
							) {
								var parentNode = standardNodesMap[node[CMDBuild.core.constants.Proxy.PARENT]];
								parentNode.children = parentNode.children || [];
								parentNode.children.push(node);
								parentNode[CMDBuild.core.constants.Proxy.LEAF] = false;
							} else {
								standard.push(node);
							}
						}

						// Manually sorting to avoid main classes group sorting
						CMDBuild.core.Utils.objectArraySort(standard, CMDBuild.core.constants.Proxy.TEXT);
						CMDBuild.core.Utils.objectArraySort(simple, CMDBuild.core.constants.Proxy.TEXT);

						if (Ext.isEmpty(simple)) {
							nodes = standard;
						} else {
							nodes = [
								{
									iconCls: 'cmdb-tree-superclass-icon',
									text: CMDBuild.Translation.standard,
									description: CMDBuild.Translation.standard,
									children: standard,
									expanded: true,
									selectable: false,
									leaf: false
								},
								{
									iconCls: 'cmdb-tree-superclass-icon',
									text: CMDBuild.Translation.simple,
									description: CMDBuild.Translation.simple,
									children: simple,
									expanded: true,
									selectable: false,
									leaf: false
								}
							];
						}

						if (!Ext.isEmpty(nodes))
							this.view.getStore().getRootNode().appendChild(nodes);
					}

					// Alias of this.callParent(arguments), inside proxy function doesn't work
					this.updateStoreCommonEndpoint(nodeIdToSelect);
				}
			});

			this.callParent(arguments);
		},

		/**
		 * Manage accordion collapse to exit from card edit mode
		 *
		 * @returns {Void}
		 *
		 * @legacy
		 */
		onAccordionClassesCollapse: function () {
			if (this.cmfg('mainViewportModuleControllerExists', 'class')) {
				var controller = this.cmfg('mainViewportModuleControllerGet', 'class');
				var controllerCardPanel = controller.cardPanelController;

				if (!Ext.isEmpty(controllerCardPanel) && Ext.isFunction(controllerCardPanel.onAbortCardClick))
					controllerCardPanel.onAbortCardClick();
			} else {
				_error('onAccordionClassesCollapse(): non-existent module controller', this);
			}
		}
	});

})();
