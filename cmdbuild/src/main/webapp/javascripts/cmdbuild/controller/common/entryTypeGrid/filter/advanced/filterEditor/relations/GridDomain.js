(function () {

	Ext.define('CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.GridDomain', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.entryTypeGrid.filter.advanced.filterEditor.Relations'
		],

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.Relations}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onEntryTypeGridFilterAdvancedFilterEditorRelationsGridDomainBeforeEdit',
			'onEntryTypeGridFilterAdvancedFilterEditorRelationsGridDomainCheckchange',
			'onEntryTypeGridFilterAdvancedFilterEditorRelationsGridDomainViewShow'
		],

		/**
		 * @property {CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGridPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.Relations} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGridPanel', { delegate: this })
		},

		/**
		 * Apply filter to classes store to display only related items (no simple classes)
		 *
		 * @param {Object} parameters
		 * @param {Number} parameters.colIdx
		 * @param {Object} parameters.column
		 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGrid} parameters.record
		 *
		 * @returns {Boolean}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorRelationsGridDomainBeforeEdit: function (parameters) {
			if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
				var colIdx = parameters.colIdx;
				var column = parameters.column;
				var recordDestinationId = !Ext.isEmpty(parameters.record.get) && Ext.isFunction(parameters.record.get)
					? parameters.record.get([CMDBuild.core.constants.Proxy.DESTINATION, CMDBuild.core.constants.Proxy.ID]) : null;

				if (
					!Ext.isEmpty(column)
					&& !Ext.isEmpty(recordDestinationId)
					&& colIdx == 2 // Avoid to go in edit of unwanted columns
				) {
					column.getEditor().getStore().clearFilter();
					column.getEditor().getStore().filterBy(function (storeRecord, id) {
						return (
							storeRecord.get(CMDBuild.core.constants.Proxy.TABLE_TYPE) != CMDBuild.core.constants.Global.getTableTypeSimpleTable()
							&& (
								storeRecord.get(CMDBuild.core.constants.Proxy.PARENT) == recordDestinationId
								|| storeRecord.get(CMDBuild.core.constants.Proxy.ID) == recordDestinationId
							)
						);
					}, this);

					return true;
				}
			}

			return false;
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.checked
		 * @param {String} parameters.propertyName
		 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGrid} parameters.record
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorRelationsGridDomainCheckchange: function (parameters) {
			if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
				var checked = Ext.isBoolean(parameters.checked) ? parameters.checked : false;
				var propertyName = parameters.propertyName;
				var record = parameters.record;

				if (
					Ext.isString(propertyName) && !Ext.isEmpty(propertyName)
					&& Ext.isObject(record) && !Ext.Object.isEmpty(record)
				) {
					this.view.getSelectionModel().select(record); // Autoselect on checkchange

					// Makes properties mutual exclusive only on check action
					if (checked)
						record.setType(propertyName);
				}
			}
		},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorRelationsGridDomainViewShow: function () {
			this.view.getStore().removeAll();
			this.view.getSelectionModel().clearSelections();

			if (!this.cmfg('entryTypeGridFilterAdvancedEntryTypeIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.common.entryTypeGrid.filter.advanced.filterEditor.Relations.readAllEntryTypes({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
							var localCacheClasses = {};

							Ext.Array.each(decodedResponse, function (classObject, i, allClassObjects) {
								if (Ext.isObject(classObject) && !Ext.Object.isEmpty(classObject))
									localCacheClasses[classObject[CMDBuild.core.constants.Proxy.ID]] = classObject;
							}, this);

							// CMCache.getDirectedDomainsByEntryType alias code
							var anchestorsId = CMDBuild.core.Utils.getEntryTypeAncestorsId(this.cmfg('entryTypeGridFilterAdvancedEntryTypeGet'));

							params = {};
							params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('entryTypeGridFilterAdvancedEntryTypeGet', CMDBuild.core.constants.Proxy.NAME);
							params[CMDBuild.core.constants.Proxy.SKIP_DISABLED_CLASSES] = true;

							CMDBuild.proxy.common.entryTypeGrid.filter.advanced.filterEditor.Relations.readAllDomainsByClass({
								params: params,
								scope: this,
								callback: function (options, success, decodedResponse) {
									this.cmfg('entryTypeGridFilterAdvancedFilterEditorRelationsSelectionManage');
								},
								success: function (response, options, decodedResponse) {
									decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

									var domainGridStoreRecords = [];

									if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
										Ext.Array.forEach(decodedResponse, function (domainObject, i, allDomainObjects) {
											var domainCMObject = {};

											// Also filters EntryTypes with no read permission
											if (Ext.Array.contains(anchestorsId, domainObject['class1id']) && !Ext.isEmpty(localCacheClasses[domainObject['class2id']]))
												domainGridStoreRecords.push(
													Ext.create('CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGrid', {
														destination: Ext.create(
															'CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.EntryType',
															localCacheClasses[domainObject['class2id']]
														),
														direction: '_1',
														domain: Ext.create('CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.Domain', domainObject),
														domainDescription: domainObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
														orientedDescription: domainObject['descrdir'],
														source: Ext.create(
															'CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.EntryType',
															localCacheClasses[domainObject['class1id']]
														)
													})
												);

											// Also filters EntryTypes with no read permission
											if (Ext.Array.contains(anchestorsId, domainObject['class2id']) && !Ext.isEmpty(localCacheClasses[domainObject['class2id']]))
												domainGridStoreRecords.push(
													Ext.create('CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGrid', {
														destination: Ext.create(
															'CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.EntryType',
															localCacheClasses[domainObject['class1id']]
														),
														direction: '_2',
														domain: Ext.create('CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.Domain', domainObject),
														domainDescription: domainObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
														orientedDescription: domainObject['descrinv'],
														source: Ext.create(
															'CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.EntryType',
															localCacheClasses[domainObject['class2id']]
														)
													})
												);
										}, this);

										if (!Ext.isEmpty(domainGridStoreRecords))
											this.view.getStore().add(domainGridStoreRecords);
									}
								}
							});
						}
					}
				});
			}
		}
	});

})();
