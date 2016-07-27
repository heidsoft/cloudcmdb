(function () {

	Ext.define('CMDBuild.controller.management.accordion.Menu', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.CustomPage',
			'CMDBuild.proxy.Menu'
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
		 * Used as a hack to get all customPages data from server
		 *
		 * @property {Array}
		 */
		customPagesResponse: [],

		/**
		 * @cfg {Boolean}
		 */
		hideIfEmpty: true,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.management.accordion.Menu}
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

			this.view = Ext.create('CMDBuild.view.management.accordion.Menu', { delegate: this });

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
			params[CMDBuild.core.constants.Proxy.GROUP_NAME] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME);
			params[CMDBuild.core.constants.Proxy.LOCALIZED] = true;

			CMDBuild.proxy.Menu.read({
				params: params,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					var menuItemsResponse = decodedResponse[CMDBuild.core.constants.Proxy.MENU];

					CMDBuild.proxy.CustomPage.readForCurrentUser({
						loadMask: false,
						scope: this,
						success: function (response, options, decodedResponse) {
							this.customPagesResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

							if (
								!Ext.isEmpty(menuItemsResponse)
								&& Ext.isArray(menuItemsResponse[CMDBuild.core.constants.Proxy.CHILDREN]) && !Ext.isEmpty(menuItemsResponse[CMDBuild.core.constants.Proxy.CHILDREN])
								&& menuItemsResponse[CMDBuild.core.constants.Proxy.TYPE] == 'root'
							) {
								this.view.getStore().getRootNode().removeAll();
								this.view.getStore().getRootNode().appendChild(this.menuStructureChildrenBuilder(menuItemsResponse));
								this.view.getStore().sort();
							}

							// Alias of this.callParent(arguments), inside proxy function doesn't work
							this.updateStoreCommonEndpoint(nodeIdToSelect);
						}
					});
				}
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Object} menuObject - menu root node object
		 *
		 * @returns {Array} nodeStructure
		 *
		 * @private
		 */
		menuStructureChildrenBuilder: function (menuObject) {
			var nodeStructure = [];

			if (!Ext.isEmpty(menuObject[CMDBuild.core.constants.Proxy.CHILDREN]) && Ext.isArray(menuObject[CMDBuild.core.constants.Proxy.CHILDREN]))
				Ext.Array.each(menuObject[CMDBuild.core.constants.Proxy.CHILDREN], function (childObject, i, allChildNodes) {
					nodeStructure.push(this.menuStructureNodeBuilder(childObject));
				}, this);

			return nodeStructure;
		},

		/**
		 * Children nodes IDs contains also index property to avoid problems of duplicate IDs
		 *
		 * @param {Object} menuNodeObject
		 * 	Ex. {
		 * 		{String} description
		 * 		{Number} index
		 * 		{String} referencedClassName
		 * 		{Number} referencedElementId
		 * 		{String} type - [class | processclass | dashboard | reportcsv | reportpdf | view]
		 * 	}
		 *
		 * @returns {Object} nodeStructure
		 *
		 * @private
		 */
		menuStructureNodeBuilder: function (menuNodeObject) {
			var nodeStructure = {};

			if (!Ext.Object.isEmpty(menuNodeObject)) {
				// Common attributes
				nodeStructure['cmIndex'] = menuNodeObject[CMDBuild.core.constants.Proxy.INDEX];
				nodeStructure[CMDBuild.core.constants.Proxy.DESCRIPTION] = menuNodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
				nodeStructure[CMDBuild.core.constants.Proxy.TEXT] = menuNodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
				nodeStructure[CMDBuild.core.constants.Proxy.LEAF] = true;

				switch (menuNodeObject[CMDBuild.core.constants.Proxy.TYPE]) {
					case 'class': {
						var entryType = _CMCache.getEntryTypeByName(menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME]);

						if (!Ext.isEmpty(entryType)) {
							nodeStructure['cmName'] = menuNodeObject[CMDBuild.core.constants.Proxy.TYPE];
							nodeStructure['iconCls'] = 'cmdb-tree-' + (entryType.isSuperClass() ? 'super' : '') + 'class-icon';
							nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = entryType.getId();
							nodeStructure[CMDBuild.core.constants.Proxy.FILTER] = menuNodeObject[CMDBuild.core.constants.Proxy.FILTER];
							nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', [
								menuNodeObject[CMDBuild.core.constants.Proxy.TYPE],
								entryType.getId(),
								menuNodeObject[CMDBuild.core.constants.Proxy.INDEX],
								menuNodeObject['uuid']
							]);
							nodeStructure[CMDBuild.core.constants.Proxy.NAME] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME];
						}
					} break;

					/**
					 * Uses readForCurrentUser() server call response to get CustomPage name. Should be fixed with a server getAssignedMenu() call refactor.
					 */
					case CMDBuild.core.constants.ModuleIdentifiers.getCustomPage(): {
						var customPageDataObject = Ext.Array.findBy(this.customPagesResponse, function (item, i) {
							return menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID] == item[CMDBuild.core.constants.Proxy.ID];
						}, this);

						nodeStructure['cmName'] = menuNodeObject[CMDBuild.core.constants.Proxy.TYPE];
						nodeStructure['iconCls'] = 'cmdb-tree-custompage-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', [
							menuNodeObject[CMDBuild.core.constants.Proxy.TYPE],
							menuNodeObject[CMDBuild.core.constants.Proxy.INDEX],
							menuNodeObject['uuid']
						]);
						nodeStructure[CMDBuild.core.constants.Proxy.NAME] = customPageDataObject[CMDBuild.core.constants.Proxy.NAME];
					} break;

					case 'dashboard': {
						nodeStructure['cmName'] = 'dashboard';
						nodeStructure['iconCls'] = 'cmdb-tree-dashboard-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', [
							menuNodeObject[CMDBuild.core.constants.Proxy.TYPE],
							menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID],
							menuNodeObject[CMDBuild.core.constants.Proxy.INDEX],
							menuNodeObject['uuid']
						]);
					} break;

					case 'folder': {
						nodeStructure['cmName'] = 'folder';
						nodeStructure['expandable'] = false;
						nodeStructure[CMDBuild.core.constants.Proxy.SELECTABLE] = false;
						nodeStructure[CMDBuild.core.constants.Proxy.LEAF] = false;
					} break;

					case 'processclass': {
						var entryType = _CMCache.getEntryTypeByName(menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME]);

						if (!Ext.isEmpty(entryType)) {
							nodeStructure['cmName'] = CMDBuild.core.constants.ModuleIdentifiers.getWorkflow();
							nodeStructure['iconCls'] = 'cmdb-tree-' + (entryType.isSuperClass() ? 'super' : '') + 'processclass-icon';
							nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = entryType.getId();
							nodeStructure[CMDBuild.core.constants.Proxy.FILTER] = menuNodeObject[CMDBuild.core.constants.Proxy.FILTER];
							nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', [
								menuNodeObject[CMDBuild.core.constants.Proxy.TYPE],
								entryType.getId(),
								menuNodeObject[CMDBuild.core.constants.Proxy.INDEX],
								menuNodeObject['uuid']
							]);
							nodeStructure[CMDBuild.core.constants.Proxy.NAME] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_CLASS_NAME];
						}
					} break;

					case 'reportcsv': {
						nodeStructure['cmName'] = CMDBuild.core.constants.ModuleIdentifiers.getReportSingle();
						nodeStructure['iconCls'] = 'cmdb-tree-reportcsv-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', [
							CMDBuild.core.constants.ModuleIdentifiers.getReportSingle(),
							menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID],
							menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
						]);
						nodeStructure[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = [CMDBuild.core.constants.Proxy.CSV];
					} break;

					case 'reportpdf': {
						nodeStructure['cmName'] = CMDBuild.core.constants.ModuleIdentifiers.getReportSingle();
						nodeStructure['iconCls'] = 'cmdb-tree-reportpdf-icon';
						nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID];
						nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', [
							CMDBuild.core.constants.ModuleIdentifiers.getReportSingle(),
							menuNodeObject[CMDBuild.core.constants.Proxy.REFERENCED_ELEMENT_ID],
							menuNodeObject[CMDBuild.core.constants.Proxy.INDEX]
						]);
						nodeStructure[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = [CMDBuild.core.constants.Proxy.PDF];
					} break;

					case 'view': {
						switch (menuNodeObject[CMDBuild.core.constants.Proxy.SPECIFIC_TYPE_VALUES][CMDBuild.core.constants.Proxy.TYPE]) {
							case 'FILTER': {
								var entryType = _CMCache.getEntryTypeByName(
									menuNodeObject[CMDBuild.core.constants.Proxy.SPECIFIC_TYPE_VALUES][CMDBuild.core.constants.Proxy.SOURCE_CLASS_NAME]
								);

								if (!Ext.isEmpty(entryType)) {
									nodeStructure['cmName'] = 'class';
									nodeStructure[CMDBuild.core.constants.Proxy.ENTITY_ID] = entryType.getId();
									nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', [
										'dataview-filter',
										entryType.getId(),
										menuNodeObject[CMDBuild.core.constants.Proxy.INDEX],
										menuNodeObject['uuid']
									]);
									nodeStructure[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['filter'];
									nodeStructure[CMDBuild.core.constants.Proxy.FILTER] = menuNodeObject[CMDBuild.core.constants.Proxy.SPECIFIC_TYPE_VALUES][CMDBuild.core.constants.Proxy.FILTER];
								}
							} break;

							case 'SQL': { // TODO: check if fill with SQL or do something else
								nodeStructure['cmName'] = CMDBuild.core.constants.ModuleIdentifiers.getDataView();
								nodeStructure[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', [
									'dataview-sql',
									menuNodeObject[CMDBuild.core.constants.Proxy.INDEX],
									menuNodeObject['uuid']
								]);
								nodeStructure[CMDBuild.core.constants.Proxy.SECTION_HIERARCHY] = ['sql'];
								nodeStructure[CMDBuild.core.constants.Proxy.SOURCE_FUNCTION] = menuNodeObject[CMDBuild.core.constants.Proxy.SPECIFIC_TYPE_VALUES][CMDBuild.core.constants.Proxy.SOURCE_FUNCTION];
							} break;

							default: {
								_error(
									'specificTypeValues.type "'
									+ menuNodeObject[CMDBuild.core.constants.Proxy.SPECIFIC_TYPE_VALUES][CMDBuild.core.constants.Proxy.TYPE]
									+ '" not managed',
									this
								);

								nodeStructure = {};
							}
						}
					} break;

					default: {
						_error('menu item type "' + menuNodeObject[CMDBuild.core.constants.Proxy.TYPE] + '" not managed', this);

						nodeStructure = {};
					}
				}

				// Build children nodes
				if (Ext.isArray(menuNodeObject[CMDBuild.core.constants.Proxy.CHILDREN]) && !Ext.isEmpty(menuNodeObject[CMDBuild.core.constants.Proxy.CHILDREN])) {
					nodeStructure['expandable'] = true;
					nodeStructure[CMDBuild.core.constants.Proxy.CHILDREN] = this.menuStructureChildrenBuilder(menuNodeObject);
					nodeStructure[CMDBuild.core.constants.Proxy.LEAF] = false;
				}
			}

			return nodeStructure;
		}
	});

})();
