(function () {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.section.Lookup', {
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
		sectionId: CMDBuild.core.constants.Proxy.LOOKUP,

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
				title: CMDBuild.Translation.lookupTypes
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onLocalizationAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		},

		/**
		 * Build value main node (buildAttributesNode)
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 *
		 * @returns {CMDBuild.model.localization.advancedTable.TreeStore}
		 *
		 * @private
		 * @override
		 */
		buildValuesNode: function (rootNode) {
			if (!Ext.isEmpty(rootNode)) {
				var entityAttributesNodeObject = { expandable: true };
				entityAttributesNodeObject[CMDBuild.core.constants.Proxy.LEAF] = false;
				entityAttributesNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
				entityAttributesNodeObject[CMDBuild.core.constants.Proxy.TEXT] = CMDBuild.Translation.lookup;

				return rootNode.appendChild(entityAttributesNodeObject);
			}

			return rootNode;
		},

		/**
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 * @param {Array} arrayToDecode
		 *
		 * @private
		 * @override
		 */
		decodeStructure: function (rootNode, arrayToDecode) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(arrayToDecode) && Ext.isArray(arrayToDecode)
			) {
				Ext.Array.forEach(arrayToDecode, function (lookupTypeObject, i, allLookupTypesObjects) {
					// LookupType main node
					var lookupTypeNodeObject = { expandable: true };
					lookupTypeNodeObject[CMDBuild.core.constants.Proxy.IDENTIFIER] = lookupTypeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
					lookupTypeNodeObject[CMDBuild.core.constants.Proxy.LEAF] = false;
					lookupTypeNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
					lookupTypeNodeObject[CMDBuild.core.constants.Proxy.TEXT] = lookupTypeObject[CMDBuild.core.constants.Proxy.DESCRIPTION];

					var lookupTypeNode = rootNode.appendChild(lookupTypeNodeObject);

					// Lookup's values nodes
					if (!Ext.isEmpty(lookupTypeObject[CMDBuild.core.constants.Proxy.VALUES]))
						this.decodeStructureValues(lookupTypeNode, lookupTypeObject[CMDBuild.core.constants.Proxy.VALUES]);
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructure() wrong parameters', this);
			}
		},

		/**
		 * Entity translatable fields
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
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.IDENTIFIER] = this.getLevelNode(rootNode, 3).get(CMDBuild.core.constants.Proxy.IDENTIFIER);
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.TEXT] = fieldObject[CMDBuild.core.constants.Proxy.NAME];
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.TYPE] = this.getSectionId();

					// Fields adapter for attributes nodes
					if (rootNode.getDepth() != 1) {
						entityFieldNodeObject[CMDBuild.core.constants.Proxy.OWNER] = this.getLevelNode(rootNode, 1).get(CMDBuild.core.constants.Proxy.IDENTIFIER);
						entityFieldNodeObject[CMDBuild.core.constants.Proxy.TYPE] = this.getSectionId() + CMDBuild.core.Utils.toTitleCase(CMDBuild.core.constants.Proxy.VALUE);
					}

					this.fillWithTranslations(fieldObject[CMDBuild.core.constants.Proxy.TRANSLATIONS], entityFieldNodeObject);

					rootNode.appendChild(entityFieldNodeObject);
				}, this);
			} else {
				rootNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make rootNode expandable
			}
		},

		/**
		 * Entity values nodes (decodeStructureAttributes)
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 * @param {Array} valuesArray
		 *
		 * @private
		 */
		decodeStructureValues: function (rootNode, valuesArray) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(valuesArray) && Ext.isArray(valuesArray)
			) {
				rootNode = this.buildValuesNode(rootNode);

				Ext.Array.forEach(valuesArray, function (valueObject, i, allValuesObjects) {
					var lookupValueNodeObject = { expandable: true };
					lookupValueNodeObject[CMDBuild.core.constants.Proxy.IDENTIFIER] = valueObject[CMDBuild.core.constants.Proxy.TRANSLATION_UUID];
					lookupValueNodeObject[CMDBuild.core.constants.Proxy.LEAF] = false;
					lookupValueNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
					lookupValueNodeObject[CMDBuild.core.constants.Proxy.TEXT] = valueObject[CMDBuild.core.constants.Proxy.CODE];

					var lookupValueNode = rootNode.appendChild(lookupValueNodeObject);

					this.decodeStructureFields(lookupValueNode, valueObject[CMDBuild.core.constants.Proxy.FIELDS]);
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructureValues() - wrong parameters type', this);
			}
		}
	});

})();
