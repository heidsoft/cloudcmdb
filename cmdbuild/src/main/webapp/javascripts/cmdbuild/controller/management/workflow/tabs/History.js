(function () {

	Ext.define('CMDBuild.controller.management.workflow.tabs.History', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.workflow.tabs.History'
		],

		mixins: {
			observable: 'Ext.util.Observable',
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.management.workflow.CMModWorkflowController}
		 */
		parentDelegate: undefined,

		/**
		 * Attributes to hide from selectedEntity object
		 *
		 * @cfg {Array}
		 *
		 * @private
		 */
		attributesKeysToFilter: [
			'Id',
			'IdClass',
			'IdClass_value',
			CMDBuild.core.constants.Proxy.BEGIN_DATE,
			CMDBuild.core.constants.Proxy.CLASS_NAME,
			CMDBuild.core.constants.Proxy.USER
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWorkflowTabHistoryIncludeSystemActivitiesCheck',
			'onWorkflowTabHistoryPanelShow = onWorkflowTabHistoryIncludeRelationCheck', // Reloads store to be consistent with includeRelationsCheckbox state
			'onWorkflowTabHistoryRowExpand',
			'workflowTabHistorySelectedEntityGet',
			'workflowHistorySelectedEntityIsEmpty',
			'workflowTabHistorySelectedEntitySet'
		],

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 *
		 * @private
		 */
		entryType: undefined,

		/**
		 * @property {Object}
		 *
		 * @private
		 */
		entryTypeAttributes: {},

		/**
		 * @property {CMDBuild.view.management.workflow.tabs.history.GridPanel}
		 */
		grid: undefined,

		/**
		 * @cfg {Array}
		 *
		 * @private
		 */
		managedStatuses: ['ABORTED', 'COMPLETED', 'OPEN', 'SUSPENDED', 'TERMINATED'],

		/**
		 * @cfg {Object}
		 *
		 * Ex. {
		 *		ABORTED: '...',
		 *		COMPLETED: '...',
		 *		OPEN: '...',
		 *		SUSPENDED: '...',
		 *		TERMINATED: '...'
		 *	}
		 *
		 * @private
		 */
		statusTranslationObject: {},

		/**
		 * @property {CMDBuild.model.CMProcessInstance}
		 *
		 * @private
		 */
		selectedEntity: undefined,

		/**
		 * @property {CMDBuild.view.management.workflow.tabs.history.HistoryView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.workflow.CMModWorkflowController} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.workflow.tabs.history.HistoryView', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;

			this.statusBuildTranslationObject( ); // Build status translation object from lookup

			_CMWFState.addDelegate(this);
		},

		/**
		 * Adds current card to history store for a better visualization of differences from last history record and current one. As last function called on store build
		 * collapses all rows on store load.
		 *
		 * It's implemented with ugly workarounds because of server side ugly code.
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		addCurrentCardToStore: function () {
			var selectedEntityAttributes = {};
			var selectedEntityValues = this.selectedEntity.get(CMDBuild.core.constants.Proxy.VALUES);

			// Filter selectedEntity's attributes values to avoid the display of incorrect data
			Ext.Object.each(selectedEntityValues, function (key, value, myself) {
				if (!Ext.Array.contains(this.attributesKeysToFilter, key) && key.indexOf('_') != 0)
					selectedEntityAttributes[key] = value;
			}, this);

			selectedEntityValues[CMDBuild.core.constants.Proxy.USER] = this.selectedEntity.get(CMDBuild.core.constants.Proxy.VALUES)[CMDBuild.core.constants.Proxy.USER];

			this.valuesFormattingAndCompare(selectedEntityAttributes); // Formats values only

			this.clearStoreAdd(this.buildCurrentEntityModel(selectedEntityAttributes));

			this.getRowExpanderPlugin().collapseAll();
		},

		/**
		 * @param {Object} entityAttributeData
		 *
		 * @returns {CMDBuild.model.workflow.tabs.history.CardRecord} currentEntityModel
		 *
		 * @private
		 */
		buildCurrentEntityModel: function (entityAttributeData) {
			var performers = [];

			// Build performers array
			Ext.Array.forEach(this.selectedEntity.get(CMDBuild.core.constants.Proxy.ACTIVITY_INSTANCE_INFO_LIST), function (activityObject, i, array) {
				if (!Ext.isEmpty(activityObject[CMDBuild.core.constants.Proxy.PERFORMER_NAME]))
					performers.push(activityObject[CMDBuild.core.constants.Proxy.PERFORMER_NAME]);
			}, this);

			var currentEntityModel = Ext.create('CMDBuild.model.workflow.tabs.history.CardRecord', this.selectedEntity.getData());
			currentEntityModel.set(CMDBuild.core.constants.Proxy.ACTIVITY_NAME, this.selectedEntity.get(CMDBuild.core.constants.Proxy.VALUES)['Code']);
			currentEntityModel.set(CMDBuild.core.constants.Proxy.PERFORMERS, performers);
			currentEntityModel.set(CMDBuild.core.constants.Proxy.STATUS, this.statusTranslationGet(this.selectedEntity.get(CMDBuild.core.constants.Proxy.FLOW_STATUS)));
			currentEntityModel.set(CMDBuild.core.constants.Proxy.VALUES, entityAttributeData);
			currentEntityModel.commit();

			return currentEntityModel;
		},

		/**
		 * Adds clear and re-apply filters functionalities
		 *
		 * @param {Array or Object} itemsToAdd
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		clearStoreAdd: function (itemsToAdd) {
			this.grid.getStore().clearFilter();

			var oldStoreDatas = this.grid.getStore().getRange();

			this.grid.getStore().loadData(Ext.Array.merge(oldStoreDatas, itemsToAdd));

			this.cmfg('onWorkflowTabHistoryIncludeSystemActivitiesCheck');
		},

		/**
		 * @param {CMDBuild.model.classes.tabs.history.CardRecord} record
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		currentCardRowExpand: function (record) {
			var predecessorRecord = this.grid.getStore().getAt(1); // Get expanded record predecessor record
			var selectedEntityAttributes = {};
			var selectedEntityValues = this.selectedEntity.get(CMDBuild.core.constants.Proxy.VALUES);

			// Filter selectedEntity's attributes values to avoid the display of incorrect data
			Ext.Object.each(selectedEntityValues, function (key, value, myself) {
				if (!Ext.Array.contains(this.attributesKeysToFilter, key) && key.indexOf('_') != 0)
					selectedEntityAttributes[key] = value;
			}, this);

			selectedEntityValues[CMDBuild.core.constants.Proxy.USER] = this.selectedEntity.get(CMDBuild.core.constants.Proxy.VALUES)[CMDBuild.core.constants.Proxy.USER];

			if (!Ext.isEmpty(predecessorRecord)) {
				var predecessorParams = {};
				predecessorParams[CMDBuild.core.constants.Proxy.CARD_ID] = predecessorRecord.get(CMDBuild.core.constants.Proxy.ID); // Historic card ID
				predecessorParams[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.selectedEntity.get(CMDBuild.core.constants.Proxy.CLASS_NAME);

				CMDBuild.proxy.workflow.tabs.History.readHistoric({
					params: predecessorParams,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.valuesFormattingAndCompare(selectedEntityAttributes, decodedResponse.response[CMDBuild.core.constants.Proxy.VALUES]);

						// Setup record property with historic card details to use XTemplate functionalities to render
						record.set(CMDBuild.core.constants.Proxy.VALUES, selectedEntityAttributes);
					}
				});
			}
		},

		/**
		 * Finds same type (card or relation) current record predecessor
		 *
		 * @param {CMDBuild.model.classes.tabs.history.CardRecord or CMDBuild.model.classes.tabs.history.RelationRecord} record
		 *
		 * @returns {CMDBuild.model.classes.tabs.history.CardRecord or CMDBuild.model.classes.tabs.history.RelationRecord} predecessor or null
		 *
		 * @private
		 */
		getRecordPredecessor: function (record) {
			var i = this.grid.getStore().indexOf(record) + 1;
			var predecessor = null;

			if (!Ext.isEmpty(record) && !Ext.isEmpty(this.grid.getStore())) {
				while (i < this.grid.getStore().getCount() && Ext.isEmpty(predecessor)) {
					var inspectedRecord = this.grid.getStore().getAt(i);

					if (
						!Ext.isEmpty(inspectedRecord)
						&& record.get(CMDBuild.core.constants.Proxy.IS_CARD) == inspectedRecord.get(CMDBuild.core.constants.Proxy.IS_CARD)
						&& record.get(CMDBuild.core.constants.Proxy.IS_RELATION) == inspectedRecord.get(CMDBuild.core.constants.Proxy.IS_RELATION)
					) {
						predecessor = inspectedRecord;
					}

					i = i + 1;
				}
			}

			return predecessor;
		},

		/**
		 * @returns {CMDBuild.view.management.common.tabs.history.RowExpander} or null
		 *
		 * @private
		 */
		getRowExpanderPlugin: function () {
			var rowExpanderPlugin = null;

			if (
				!Ext.isEmpty(this.grid)
				&& !Ext.isEmpty(this.grid.plugins) && Ext.isArray(this.grid.plugins)
			) {
				Ext.Array.forEach(this.grid.plugins, function (plugin, i, allPlugins) {
					if (plugin instanceof Ext.grid.plugin.RowExpander)
						rowExpanderPlugin = plugin;
				});
			}

			return rowExpanderPlugin;
		},

		/**
		 * @returns {Void}
		 *
		 * @public
		 *
		 * FIXME: use cmfg
		 */
		onAddCardButtonClick: function () {
			this.view.disable();
		},

		/**
		 * @returns {Void}
		 *
		 * @public
		 *
		 * FIXME: use cmfg
		 */
		onCloneCard: function () {
			this.view.disable();
		},

		/**
		 * Equals to onEntryTypeSelected in classes
		 *
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 *
		 * @returns {Void}
		 *
		 * @public
		 *
		 * FIXME: use cmfg
		 */
		onProcessClassRefChange: function (entryType) {
			this.entryType = entryType;

			this.view.disable();
		},

		/**
		 * Equals to onCardSelected in classes
		 *
		 * @param {CMDBuild.model.CMProcessInstance} processInstance
		 *
		 * @returns {Void}
		 *
		 * @public
		 *
		 * FIXME: use cmfg
		 */
		onProcessInstanceChange: function (processInstance) {
			this.cmfg('workflowTabHistorySelectedEntitySet', processInstance);

			this.view.setDisabled(processInstance.isNew());

			this.cmfg('onWorkflowTabHistoryPanelShow');
		},

		/**
		 * Include or not System activities rows in history grid
		 *
		 * @returns {Void}
		 */
		onWorkflowTabHistoryIncludeSystemActivitiesCheck: function () {
			this.getRowExpanderPlugin().collapseAll();

			if (this.grid.includeSystemActivitiesCheckbox.getValue()) { // Checked: Remove any filter from store
				if (this.grid.getStore().isFiltered()) {
					this.grid.getStore().clearFilter();
					this.grid.getStore().sort(); // Resort store because clearFilter() breaks it
				}
			} else { // Unchecked: Apply filter to hide 'System' activities rows
				this.grid.getStore().filterBy(function (record, id) {
					return record.get(CMDBuild.core.constants.Proxy.USER).indexOf('system') < 0; // System user name
				}, this);
			}
		},

		/**
		 * Loads store and if includeRelationsCheckbox is checked fills store with relations rows
		 *
		 * @returns {Void}
		 */
		onWorkflowTabHistoryPanelShow: function () {
			if (this.view.isVisible()) {
				// History record save
				if (!Ext.isEmpty(_CMWFState.getProcessClassRef()) && !Ext.isEmpty( _CMWFState.getProcessInstance()))
					CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
						moduleId: 'workflow',
						entryType: {
							description: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.TEXT),
							id: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.ID),
							object: _CMWFState.getProcessClassRef()
						},
						item: {
							description: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.TEXT),
							id: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.ID),
							object: _CMWFState.getProcessInstance()
						},
						section: {
							description: this.view.title,
							object: this.view
						}
					});

				this.grid.getStore().removeAll(); // Clear store before load new one

				if (!Ext.isEmpty(this.selectedEntity)) {
					var params = {};
					params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selectedEntity.get('IdClass'));

					// Request all class attributes
					CMDBuild.proxy.workflow.tabs.History.readAttributes({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTES];

							Ext.Array.forEach(decodedResponse, function (attribute, i, allAttributes) {
								if (attribute['fieldmode'] != 'hidden')
									this.entryTypeAttributes[attribute[CMDBuild.core.constants.Proxy.NAME]] = attribute;
							}, this);

							params = {};
							params[CMDBuild.core.constants.Proxy.CARD_ID] = this.selectedEntity.get(CMDBuild.core.constants.Proxy.ID);
							params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selectedEntity.get('IdClass'));

							this.grid.getStore().load({
								params: params,
								scope: this,
								callback: function (records, operation, success) {
									this.getRowExpanderPlugin().collapseAll();

									if (this.grid.includeRelationsCheckbox.getValue()) {
										CMDBuild.proxy.workflow.tabs.History.readRelations({
											params: params,
											loadMask: false,
											scope: this,
											success: function (response, options, decodedResponse) {
												decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];
												decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ELEMENTS];

												var referenceElementsModels = [];

												// Build reference models
												Ext.Array.forEach(decodedResponse, function (element, i, allElements) {
													referenceElementsModels.push(Ext.create('CMDBuild.model.classes.tabs.history.RelationRecord', element));
												});

												this.clearStoreAdd(referenceElementsModels);

												this.addCurrentCardToStore();
											}
										});
									} else {
										this.addCurrentCardToStore();
									}
								}
							});
						}
					});
				}
			}
		},

		/**
		 * @param {CMDBuild.model.classes.tabs.history.CardRecord or CMDBuild.model.classes.tabs.history.RelationRecord} record
		 *
		 * @returns {Void}
		 */
		onWorkflowTabHistoryRowExpand: function (record) {
			if (!Ext.isEmpty(record)) {
				var params = {};

				if (record.get(CMDBuild.core.constants.Proxy.IS_CARD)) { // Card row expand
					if (this.selectedEntity.get(CMDBuild.core.constants.Proxy.ID) == record.get(CMDBuild.core.constants.Proxy.ID)) { // Expanding current card
						this.currentCardRowExpand(record);
					} else {
						params[CMDBuild.core.constants.Proxy.CARD_ID] = record.get(CMDBuild.core.constants.Proxy.ID); // Historic card ID
						params[CMDBuild.core.constants.Proxy.CLASS_NAME] = record.get(CMDBuild.core.constants.Proxy.CLASS_NAME);

						CMDBuild.proxy.workflow.tabs.History.readHistoric({ // Get expanded card data
							params: params,
							scope: this,
							success: function (response, options, decodedResponse) {
								var cardValuesObject = decodedResponse.response[CMDBuild.core.constants.Proxy.VALUES];
								var predecessorRecord = this.getRecordPredecessor(record);

								if (!Ext.isEmpty(predecessorRecord)) {
									var predecessorParams = {};
									predecessorParams[CMDBuild.core.constants.Proxy.CARD_ID] = predecessorRecord.get(CMDBuild.core.constants.Proxy.ID); // Historic card ID
									predecessorParams[CMDBuild.core.constants.Proxy.CLASS_NAME] = record.get(CMDBuild.core.constants.Proxy.CLASS_NAME);

									CMDBuild.proxy.workflow.tabs.History.readHistoric({ // Get expanded predecessor's card data
										params: predecessorParams,
										scope: this,
										success: function (response, options, decodedResponse) {
											decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

											this.valuesFormattingAndCompare(cardValuesObject, decodedResponse[CMDBuild.core.constants.Proxy.VALUES]);

											// Setup record property with historic card details to use XTemplate functionalities to render
											record.set(CMDBuild.core.constants.Proxy.VALUES, cardValuesObject);
										}
									});
								} else {
									this.valuesFormattingAndCompare(cardValuesObject);

									// Setup record property with historic card details to use XTemplate functionalities to render
									record.set(CMDBuild.core.constants.Proxy.VALUES, cardValuesObject);
								}
							}
						});
					}
				} else { // Relation row expand
					params[CMDBuild.core.constants.Proxy.ID] = record.get(CMDBuild.core.constants.Proxy.ID); // Historic relation ID
					params[CMDBuild.core.constants.Proxy.DOMAIN] = record.get(CMDBuild.core.constants.Proxy.DOMAIN);

					CMDBuild.proxy.workflow.tabs.History.readHistoricRelation({
						params: params,
						scope: this,
						success: function (response, options, decodedResponse) {
							var cardValuesObject = decodedResponse.response[CMDBuild.core.constants.Proxy.VALUES];

							this.valuesFormattingAndCompare(cardValuesObject);

							// Setup record property with historic relation details to use XTemplate functionalities to render
							record.set(CMDBuild.core.constants.Proxy.VALUES, cardValuesObject);
						}
					});
				}
			}
		},

		// Status translation management
			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			statusBuildTranslationObject: function () {
				var params = {};
				params[CMDBuild.core.constants.Proxy.TYPE] = 'FlowStatus';
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
				params[CMDBuild.core.constants.Proxy.SHORT] = false;

				CMDBuild.proxy.workflow.tabs.History.readLookups({
					params: params,
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ROWS];

						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse))
							Ext.Array.forEach(decodedResponse, function (lookup, i, array) {
								switch (lookup['Code']) {
									case 'closed.aborted': {
										this.statusTranslationObject['ABORTED'] = lookup['Description'];
									} break;

									case 'closed.completed': {
										this.statusTranslationObject['COMPLETED'] = lookup['Description'];
									} break;

									case 'closed.terminated': {
										this.statusTranslationObject['TERMINATED'] = lookup['Description'];
									} break;

									case 'open.running': {
										this.statusTranslationObject['OPEN'] = lookup['Description'];
									} break;

									case 'open.not_running.suspended': {
										this.statusTranslationObject['SUSPENDED'] = lookup['Description'];
									} break;
								}
							}, this);
					}
				});
			},

			/**
			 * @param {String} status
			 *
			 * @returns {String or null}
			 *
			 * @private
			 */
			statusTranslationGet: function (status) {
				if (Ext.Array.contains(this.managedStatuses, status))
					return this.statusTranslationObject[status];

				return null;
			},

		// SelectedEntity property functions
			/**
			 * @returns {Mixed}
			 */
			workflowTabHistorySelectedEntityGet: function () {
				return this.selectedEntity;
			},

			/**
			 * @returns {Mixed}
			 */
			workflowHistorySelectedEntityIsEmpty: function () {
				return Ext.isEmpty(this.selectedEntity);
			},

			/**
			 * @param {Mixed} selectedEntity
			 *
			 * @returns {Void}
			 */
			workflowTabHistorySelectedEntitySet: function (selectedEntity) {
				this.selectedEntity = Ext.isEmpty(selectedEntity) ? undefined : selectedEntity;
			},

		/**
		 * Formats all object1 values as objects:
		 * 	{
		 * 		{Boolean} changed
		 * 		{Mixed} description
		 * 	}
		 *
		 * If value1 is different than value2
		 * modified is true, false otherwise. Strips also HTML tags from "description".
		 *
		 * @param {Object} object1 - currently expanded record
		 * @param {Object} object2 - predecessor record
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		valuesFormattingAndCompare: function (object1, object2) {
			object1 = object1 || {};
			object2 = object2 || {};

			if (!Ext.isEmpty(object1) && Ext.isObject(object1)) {
				Ext.Object.each(object1, function (key, value, myself) {
					var changed = false;

					// Get attribute's index and description
					var attributeDescription = Ext.isEmpty(this.entryTypeAttributes[key]) ? null : this.entryTypeAttributes[key][CMDBuild.core.constants.Proxy.DESCRIPTION];
					var attributeIndex = Ext.isEmpty(this.entryTypeAttributes[key]) ? 0 : this.entryTypeAttributes[key][CMDBuild.core.constants.Proxy.INDEX];

					// Build object1 properties models
					var attributeValues = Ext.isObject(value) ? value : { description: value };
					attributeValues[CMDBuild.core.constants.Proxy.ATTRIBUTE_DESCRIPTION] = attributeDescription;
					attributeValues[CMDBuild.core.constants.Proxy.INDEX] = attributeIndex;

					object1[key] = Ext.create('CMDBuild.model.common.tabs.history.Attribute', attributeValues);

					// Build object2 properties models
					if (!Ext.Object.isEmpty(object2)) {
						if (!object2.hasOwnProperty(key))
							object2[key] = null;

						attributeValues = Ext.isObject(object2[key]) ? object2[key] : { description: object2[key] };
						attributeValues[CMDBuild.core.constants.Proxy.ATTRIBUTE_DESCRIPTION] = attributeDescription;
						attributeValues[CMDBuild.core.constants.Proxy.INDEX] = attributeIndex;

						object2[key] = Ext.create('CMDBuild.model.common.tabs.history.Attribute', attributeValues);
					}

					changed = Ext.Object.isEmpty(object2) ? false : !Ext.Object.equals(object1[key].getData(), object2[key].getData());

					object1[key].set(CMDBuild.core.constants.Proxy.CHANGED, changed);
				}, this);
			}
		}
	});

})();
