(function () {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.section.Menu', {
		extend: 'CMDBuild.controller.administration.localization.advancedTable.section.Abstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.localization.Localization'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.constants.Proxy.MENU,

		/**
		 * @property {CMDBuild.view.administration.localization.common.AdvancedTableGrid}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.advancedTable.SectionPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable} configObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.localization.advancedTable.SectionPanel', {
				delegate: this,
				hideActiveOnlyCheckbox: true,
				title: CMDBuild.Translation.menu
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onLocalizationAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		},

		/**
		 * Build children main node (buildAttributesNode)
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 *
		 * @returns {CMDBuild.model.localization.advancedTable.TreeStore}
		 *
		 * @private
		 */
		buildChildrenNode: function (rootNode) {
			if (!Ext.isEmpty(rootNode) && rootNode.getDepth() != 1) {
				var entityAttributesNodeObject = { expandable: true };
				entityAttributesNodeObject[CMDBuild.core.constants.Proxy.LEAF] = false;
				entityAttributesNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
				entityAttributesNodeObject[CMDBuild.core.constants.Proxy.TEXT] = CMDBuild.Translation.children;

				return rootNode.appendChild(entityAttributesNodeObject);
			}

			return rootNode;
		},

		/**
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 * @param {Array} arrayToDecode
		 *
		 * @private
		 */
		decodeStructure: function (rootNode, arrayToDecode) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(arrayToDecode) && Ext.isArray(arrayToDecode)
			) {
				Ext.Array.forEach(arrayToDecode, function (entityObject, i, allEntitiesObjects) {
					if (!Ext.Array.contains(this.entityFilter, entityObject[CMDBuild.core.constants.Proxy.NAME].toLowerCase())) { // Discard unwanted entities
						// Entity main node
						var entityMainNodeObject = { expandable: true };
						entityMainNodeObject[CMDBuild.core.constants.Proxy.IDENTIFIER] = entityObject[CMDBuild.core.constants.Proxy.NAME];
						entityMainNodeObject[CMDBuild.core.constants.Proxy.LEAF] = false;
						entityMainNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
						entityMainNodeObject[CMDBuild.core.constants.Proxy.TEXT] = entityObject[CMDBuild.core.constants.Proxy.NAME];

						var entityMainNode = rootNode.appendChild(entityMainNodeObject);

						// Entity's fields nodes
						if (!Ext.isEmpty(entityObject[CMDBuild.core.constants.Proxy.FIELDS]))
							this.decodeStructureFields(entityMainNode, entityObject[CMDBuild.core.constants.Proxy.FIELDS], entityObject);

						// Entity's children nodes
						if (!Ext.isEmpty(entityObject[CMDBuild.core.constants.Proxy.CHILDREN]))
							this.decodeStructureChildren(entityMainNode, entityObject[CMDBuild.core.constants.Proxy.CHILDREN]);
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructure() wrong parameters', this);
			}
		},

		/**
		 * Entity children nodes (decodeStructureAttributes)
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 * @param {Array} attributesArray
		 *
		 * @private
		 */
		decodeStructureChildren: function (rootNode, attributesArray) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(attributesArray) && Ext.isArray(attributesArray)
			) {
				rootNode = this.buildChildrenNode(rootNode);

				Ext.Array.forEach(attributesArray, function (attributeObject, i, allAttributesObjects) {
					if (!Ext.Array.contains(this.entityAttributeFilter, attributeObject[CMDBuild.core.constants.Proxy.NAME].toLowerCase())) { // Discard unwanted attributes
						var entityAttributeNodeObject = { expandable: true };
						entityAttributeNodeObject[CMDBuild.core.constants.Proxy.IDENTIFIER] = attributeObject[CMDBuild.core.constants.Proxy.NAME];
						entityAttributeNodeObject[CMDBuild.core.constants.Proxy.LEAF] = false;
						entityAttributeNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
						entityAttributeNodeObject[CMDBuild.core.constants.Proxy.TEXT] = attributeObject.fields[0][CMDBuild.core.constants.Proxy.VALUE];
						entityAttributeNodeObject['iconCls'] = 'cmdbuild-tree-' + attributeObject[CMDBuild.core.constants.Proxy.TYPE] + '-icon';

						var entityAttributeNode = rootNode.appendChild(entityAttributeNodeObject);

						// Entity's fields nodes
						if (!Ext.isEmpty(attributeObject[CMDBuild.core.constants.Proxy.FIELDS]))
							this.decodeStructureFields(entityAttributeNode, attributeObject[CMDBuild.core.constants.Proxy.FIELDS]);

						// Entity's children nodes
						if (!Ext.isEmpty(attributeObject[CMDBuild.core.constants.Proxy.CHILDREN]))
							this.decodeStructureChildren(entityAttributeNode, attributeObject[CMDBuild.core.constants.Proxy.CHILDREN]);
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructureChildren() - wrong parameters type', this);
			}
		},

		/**
		 * Menu translatable fields
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 * @param {Array} fieldsArray
		 *
		 * @private
		 * @override
		 */
		decodeStructureFields: function (rootNode, fieldsArray) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(fieldsArray) && Ext.isArray(fieldsArray)
			) {
				Ext.Array.forEach(fieldsArray, function (fieldObject, i, allFields) {
					var entityFieldNodeObject = {};
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.DEFAULT] = fieldObject[CMDBuild.core.constants.Proxy.VALUE];
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.FIELD] = fieldObject[CMDBuild.core.constants.Proxy.NAME];
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.IDENTIFIER] = this.getLevelNode(rootNode, 4).get(CMDBuild.core.constants.Proxy.IDENTIFIER);
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.TEXT] = fieldObject[CMDBuild.core.constants.Proxy.NAME];
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.TYPE] = CMDBuild.core.constants.Proxy.MENU_ITEM;

					this.fillWithTranslations(fieldObject[CMDBuild.core.constants.Proxy.TRANSLATIONS], entityFieldNodeObject);

					rootNode.appendChild(entityFieldNodeObject);
				}, this);
			} else {
				rootNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make rootNode expandable
			}
		}
	});

})();
