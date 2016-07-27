(function () {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.administration.localization.advancedTable.section.Abstract', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.localization.Localization'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.advancedTable.AdvancedTable}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLocalizationAdvancedTableRowUpdateButtonClick',
			'onLocalizationAdvancedTableShow = onLocalizationAdvancedTableOnlyEnabledEntitiesCheck'
		],

		/**
		 * @cfg {Array}
		 */
		entityFilter: [],

		/**
		 * @cfg {Array}
		 */
		entityAttributeFilter: [],

		/**
		 * @cfg {String}
		 *
		 * @abstract
		 */
		sectionId: undefined,

		/**
		 * @property {CMDBuild.view.administration.localization.advancedTable.SectionPanel}
		 */
		view: undefined,

		/**
		 * Build attributes main node
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 *
		 * @returns {CMDBuild.model.localization.advancedTable.TreeStore}
		 *
		 * @private
		 */
		buildAttributesNode: function (rootNode) {
			if (!Ext.isEmpty(rootNode)) {
				var entityAttributesNodeObject = { expandable: true };
				entityAttributesNodeObject[CMDBuild.core.constants.Proxy.LEAF] = false;
				entityAttributesNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
				entityAttributesNodeObject[CMDBuild.core.constants.Proxy.TEXT] = CMDBuild.Translation.attributes;

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
				&& Ext.isArray(arrayToDecode)
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
							this.decodeStructureFields(entityMainNode, entityObject[CMDBuild.core.constants.Proxy.FIELDS]);

						// Entity's attributes nodes
						if (!Ext.isEmpty(entityObject[CMDBuild.core.constants.Proxy.CHILDREN]))
							this.decodeStructureAttributes(entityMainNode, entityObject[CMDBuild.core.constants.Proxy.CHILDREN]);
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructure() wrong parameters', this);
			}
		},

		/**
		 * Entity attribute nodes
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 * @param {Array} attributesArray
		 *
		 * @private
		 */
		decodeStructureAttributes: function (rootNode, attributesArray) {
			if (
				!Ext.isEmpty(rootNode)
				&& !Ext.isEmpty(attributesArray) && Ext.isArray(attributesArray)
			) {
				rootNode = this.buildAttributesNode(rootNode);

				Ext.Array.forEach(attributesArray, function (attributeObject, i, allAttributesObjects) {
					if (!Ext.Array.contains(this.entityAttributeFilter, attributeObject[CMDBuild.core.constants.Proxy.NAME].toLowerCase())) { // Discard unwanted attributes
						var entityAttributeNodeObject = { expandable: true };
						entityAttributeNodeObject[CMDBuild.core.constants.Proxy.IDENTIFIER] = attributeObject[CMDBuild.core.constants.Proxy.NAME];
						entityAttributeNodeObject[CMDBuild.core.constants.Proxy.LEAF] = false;
						entityAttributeNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
						entityAttributeNodeObject[CMDBuild.core.constants.Proxy.TEXT] = attributeObject[CMDBuild.core.constants.Proxy.NAME];

						var entityAttributeNode = rootNode.appendChild(entityAttributeNodeObject);

						this.decodeStructureFields(entityAttributeNode, attributeObject[CMDBuild.core.constants.Proxy.FIELDS]);
					}
				}, this);
			} else {
				_error('[' + this.getSectionId() + '] decodeStructureAttributes() - wrong parameters type', this);
			}
		},

		/**
		 * Entity translatable fields
		 *
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} rootNode
		 * @param {Array} fieldsArray
		 *
		 * @private
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
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.IDENTIFIER] = rootNode.get(CMDBuild.core.constants.Proxy.IDENTIFIER);
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.TEXT] = fieldObject[CMDBuild.core.constants.Proxy.NAME];
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.TYPE] = this.getSectionId();

					// Fields adapter for attributes nodes
					if (rootNode.getDepth() != 1) {
						entityFieldNodeObject[CMDBuild.core.constants.Proxy.OWNER] = this.getLevelNode(rootNode, 1).get(CMDBuild.core.constants.Proxy.IDENTIFIER);
						entityFieldNodeObject[CMDBuild.core.constants.Proxy.TYPE] = CMDBuild.core.constants.Proxy.ATTRIBUTE + CMDBuild.core.Utils.toTitleCase(this.getSectionId());
					}

					this.fillWithTranslations(fieldObject[CMDBuild.core.constants.Proxy.TRANSLATIONS], entityFieldNodeObject);

					rootNode.appendChild(entityFieldNodeObject);
				}, this);
			} else {
				rootNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make rootNode expandable
			}
		},

		/**
		 * @param {Object} translationsSourceObject
		 * @param {Object} targetObject
		 *
		 * @private
		 */
		fillWithTranslations: function (translationsSourceObject, targetObject) {
			if (
				Ext.isObject(translationsSourceObject)
				&& Ext.isObject(targetObject)
			) {
				Ext.Object.each(translationsSourceObject, function (tag, translation, myself) {
					targetObject[tag] = translation;
				});
			} else {
				_error('[' + this.getSectionId() + '] fillWithTranslations() - wrong parameters type', this);
			}
		},

		/**
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} startNode
		 * @param {Number} levelToReach
		 *
		 * @returns {CMDBuild.model.localization.advancedTable.TreeStore} requestedNode or null
		 *
		 * @private
		 */
		getLevelNode: function (startNode, levelToReach) {
			var requestedNode = startNode;

			if (!Ext.isEmpty(requestedNode) && Ext.isNumber(levelToReach)) {
				while (requestedNode.getDepth() > levelToReach) {
					requestedNode = requestedNode.get(CMDBuild.core.constants.Proxy.PARENT);
				}

				return requestedNode;
			}

			return null;
		},

		/**
		 * @returns {String}
		 *
		 * @private
		 */
		getSectionId: function () {
			return this.sectionId;
		},

		/**
		 * Fill grid store with entities data
		 */
		onLocalizationAdvancedTableShow: function () {
			var root = this.grid.getStore().getRootNode();
			root.removeAll();

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = this.view.activeOnlyCheckbox.getValue();
			params[CMDBuild.core.constants.Proxy.TYPE] = this.getSectionId();

			CMDBuild.proxy.localization.Localization.readAll({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
						Ext.suspendLayouts();

						this.decodeStructure(root, decodedResponse);

						Ext.resumeLayouts(true);
					}
				}
			});
		},

		/**
		 * @param {CMDBuild.model.localization.advancedTable.TreeStore} node
		 */
		onLocalizationAdvancedTableRowUpdateButtonClick: function (node) {
			if (!Ext.isEmpty(node)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.TYPE] = node.get(CMDBuild.core.constants.Proxy.TYPE);
				params[CMDBuild.core.constants.Proxy.IDENTIFIER] = node.get(CMDBuild.core.constants.Proxy.IDENTIFIER);
				params[CMDBuild.core.constants.Proxy.FIELD] = node.get(CMDBuild.core.constants.Proxy.FIELD);
				params[CMDBuild.core.constants.Proxy.TRANSLATIONS] = Ext.encode(node.getTranslations());

				if (!Ext.isEmpty(node.get(CMDBuild.core.constants.Proxy.OWNER)))
					params[CMDBuild.core.constants.Proxy.OWNER] = node.get(CMDBuild.core.constants.Proxy.OWNER);

				CMDBuild.proxy.localization.Localization.update({
					params: params,
					success: function (response, options, decodedResponse) {
						CMDBuild.core.Message.success();
					}
				});
			} else {
				_error('empty node on update action', this);
			}
		}
	});

})();
