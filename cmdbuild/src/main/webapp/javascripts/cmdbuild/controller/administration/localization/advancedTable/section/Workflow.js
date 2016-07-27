(function () {

	Ext.define('CMDBuild.controller.administration.localization.advancedTable.section.Workflow', {
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
		 * @cfg {Array}
		 */
		entityAttributeFilter: ['notes'],

		/**
		 * @cfg {String}
		 */
		sectionId: CMDBuild.core.constants.Proxy.PROCESS,

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
				title: CMDBuild.Translation.processes
			});

			// Shorthand
			this.grid = this.view.grid;

			this.cmfg('onLocalizationAdvancedTableTabCreation', this.view); // Add panel to parent tab panel
		},

		/**
		 * Process translatable fields
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
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.IDENTIFIER] = rootNode.get(CMDBuild.core.constants.Proxy.IDENTIFIER);
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.PARENT] = rootNode;
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.TEXT] = fieldObject[CMDBuild.core.constants.Proxy.NAME];
					entityFieldNodeObject[CMDBuild.core.constants.Proxy.TYPE] = CMDBuild.core.constants.Proxy.CLASS;

					// Fields adapter for attributes nodes
					if (rootNode.getDepth() != 1) {
						entityFieldNodeObject[CMDBuild.core.constants.Proxy.OWNER] = this.getLevelNode(rootNode, 1).get(CMDBuild.core.constants.Proxy.IDENTIFIER);
						entityFieldNodeObject[CMDBuild.core.constants.Proxy.TYPE] = CMDBuild.core.constants.Proxy.ATTRIBUTE + CMDBuild.core.Utils.toTitleCase(CMDBuild.core.constants.Proxy.CLASS);
					}

					this.fillWithTranslations(fieldObject[CMDBuild.core.constants.Proxy.TRANSLATIONS], entityFieldNodeObject);

					rootNode.appendChild(entityFieldNodeObject);
				}, this);
			} else {
				rootNode.appendChild({}); // FIX: expandable property is bugged so i must build a fake node to make rootNode expandable
			}
		}
	});

})();
