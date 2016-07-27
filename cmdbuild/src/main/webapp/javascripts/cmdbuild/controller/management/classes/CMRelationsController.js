(function() {

	Ext.require([
		'CMDBuild.core.Message',
		'CMDBuild.proxy.Relation'
	]);

	Ext.define('CMDBuild.controller.management.classes.CMCardRelationsController', {
		extend: 'CMDBuild.controller.management.classes.CMModCardSubController',

		/**
		 * @param {CMDBuild.view.management.classes.CMCardRelationsPanel} view
		 * @param {CMDBuild.controller.management.classes.CMModCardController} superController
		 *
		 * @override
		 */
		constructor: function(view, superController) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.callBacks = {
				'action-relation-go': this.onFollowRelationClick,
				'action-relation-edit': this.onEditRelationClick,
				'action-relation-delete': this.onDeleteRelationClick,
				'action-relation-editcard': this.onEditCardClick,
				'action-relation-viewcard': this.onViewCardClick,
				'action-relation-attach': this.onOpenAttachmentClick
			};

			this.view.getStore().getRootNode().on('append', function (node, newNode, index, eOpts) {
				// Nodes with depth == 1 are folders
				if (newNode.getDepth() == 1)
					newNode.on('expand', this.onDomainNodeExpand, this, { single: true });
			}, this);

			this.mon(this.view, this.view.CMEVENTS.openGraphClick, this.onShowGraphClick, this);
			this.mon(this.view, this.view.CMEVENTS.addButtonClick, this.onAddRelationButtonClick, this);
			this.mon(this.view, 'beforeitemclick', cellclickHandler, this);
			this.mon(this.view, 'itemdblclick', onItemDoubleclick, this);
			this.mon(this.view, 'activate', this.loadData, this);

			this.CMEVENTS = { serverOperationSuccess: 'cm-server-success' };

			this.addEvents(this.CMEVENTS.serverOperationSuccess);
		},

		/**
		 * @param {CMRelationPanelModel} node
		 * @param {CMRelationPanelModel} newNode
		 * @param {Number} index
		 * @param {Object} eOpts
		 *
		 * @private
		 */
		onDomainNodeExpand: function (node, newNode, index, eOpts) {
			if (node.get('relations_size') > CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.RELATION_LIMIT)) {
				node.removeAll();

				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.CARD_ID] = this.getCardId();
				parameters[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getClassId());
				parameters[CMDBuild.core.constants.Proxy.DOMAIN_ID] = node.get('dom_id');
				parameters[CMDBuild.core.constants.Proxy.SRC] = node.get('src');

				this.view.setLoading(true);
				CMDBuild.proxy.Relation.readAll({
					params: parameters,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.view.setLoading(false);
						this.view.suspendLayouts();

						this.view.convertRelationInNodes(
							decodedResponse.domains[0].relations,
							node.data.dom_id,
							node.data.src,
							node.data,
							node
						);

						this.view.resumeLayouts(true);
					}
				});
			}
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 *
		 * @override
		 */
		onEntryTypeSelected: function(entryType) {
			this.callParent(arguments);

			this.card = null;

			// Set empty entryType on simple classes
			if (Ext.isEmpty(this.entryType) || this.entryType.get(CMDBuild.core.constants.Proxy.TABLE_TYPE) == 'simpletable')
				this.entryType = null;

			this.view.disable();
			this.view.clearStore();
		},

		/**
		 * @param {Object} entryType - card model
		 *
		 * @override
		 */
		onCardSelected: function(card) {
			this.callParent(arguments);

			this.view.clearStore();
			this.view.disable();

			if (!Ext.isEmpty(card) && !Ext.isEmpty(this.entryType)) { // Don't enable tab on simple classes
				this.updateCurrentClass(card);

				this.loadData();

				this.view.enable();
			}
		},

		/**
		 * @param {Object} entryType - card model
		 */
		updateCurrentClass: function(card) {
			var classId = card.get('IdClass');
			var currentClass = _CMCache.getEntryTypeById(classId);

			if (this.currentClass != currentClass) {
				if (!currentClass || currentClass.get(CMDBuild.core.constants.Proxy.TABLE_TYPE) == 'simpletable')
					currentClass = null;

				this.currentClass = currentClass;
				this.view.addRelationButton.setDomainsForEntryType(currentClass);
			}
		},

		/**
		 * Function to load data to treePanel and edit addRelation button to avoid to violate domains cardinality
		 */
		loadData: function() {
			if (this.card != null && tabIsActive(this.view)) {
				var me = this;
				var el = this.view.getEl();

				if (el)
					el.mask();

				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.CARD_ID] = this.getCardId();
				parameters[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getClassId());
				parameters['domainlimit'] = CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.RELATION_LIMIT);

				CMDBuild.proxy.Relation.readAll({
					params: parameters,
					scope: this,
					success: function(result, options, decodedResult) {
						el.unmask();

						this.view.fillWithData(decodedResult.domains);

						// AddRelation button update
							var toDisableButtons = [];

							// Domains relation cardinality check
							Ext.Array.forEach(decodedResult.domains, function(item, index, allItems) {
								var domainObject = _CMCache.getDomainById(item[CMDBuild.core.constants.Proxy.ID]);

								if ( // Checks when disable add buttons ...
									item[CMDBuild.core.constants.Proxy.RELATIONS_SIZE] == 1 // ... relation size equals 1 ...
									&& !Ext.isEmpty(domainObject) && Ext.isFunction(domainObject.get)
									&& ( // ... and i'm on N side of domain (so i have only one target) ...
										(
											domainObject.get(CMDBuild.core.constants.Proxy.CARDINALITY) == 'N:1'
											&& item[CMDBuild.core.constants.Proxy.DOMAIN_SOURCE] == '_1'
										)
										|| (
											domainObject.get(CMDBuild.core.constants.Proxy.CARDINALITY) == '1:N'
											&& item[CMDBuild.core.constants.Proxy.DOMAIN_SOURCE] == '_2'
										)
										|| domainObject.get(CMDBuild.core.constants.Proxy.CARDINALITY) == '1:1' // ... or i'm on 1:1 relation
									)
								) {
									toDisableButtons.push(domainObject.get(CMDBuild.core.constants.Proxy.ID));
								}
							}, this);

							// Loop trough split button menu items to modify handler if relation is full
							Ext.Array.forEach(this.view.addRelationButton.menu.items.items, function(item, index, allItems) {
								if (Ext.Array.contains(toDisableButtons, item.domain.dom_id)) { // Overwrites button handler to display error popup
									item.setHandler(function() {
										CMDBuild.core.Message.error(
											CMDBuild.Translation.common.failure,
											CMDBuild.Translation.errors.domainCardinalityViolation,
											false
										);
									});
								} else { // Setup standard handler for button
									item.setHandler(function(item, e) {
										me.view.addRelationButton.fireEvent('cmClick', item.domain);
									});
								}
							}, this);
						// END: AddRelation button update
					}
				});
			}
		},

		/**
		 * @return {Int} cardId
		 */
		getCardId: function() {
			return this.card.get('Id');
		},

		/**
		 * @return {Int} classId
		 */
		getClassId: function() {
			return this.card.get('IdClass');
		},

		/**
		 * @param {CMRelationPanelModel} model
		 */
		onFollowRelationClick: function(model) {
			if (model.getDepth() > 1)
				CMDBuild.global.controller.MainViewport.cmfg('mainViewportCardSelect', {
					Id: model.get('dst_id'),
					IdClass: model.get('dst_cid'),
					flowStatus: 'all'
				});
		},

		/**
		 * AddRelation click function to open CMEditRelationWindow filtered excluding already related cards based on relation type
		 *
		 * @param {Object} model - relation grid model
		 */
		onAddRelationButtonClick: function(model) {
			var me = this;
			var masterAndSlave = getMasterAndSlave(model.src);
			var domain = _CMCache.getDomainById(model.dom_id);
			var classData = _CMCache.getEntryTypeById(model.dst_cid);
			var isMany = false;
			var destination = model[CMDBuild.core.constants.Proxy.DOMAIN_SOURCE] == '_1' ? '_2' : '_1'; // Probably tells in witch direction of relation you are looking at

			if (domain)
				isMany = domain.isMany(destination);

			var editRelationWindow = Ext.create('CMDBuild.view.management.classes.relations.CMEditRelationWindow', {
				domain: domain,
				classObject: classData,
				sourceCard: this.card,
				relation: {
					dst_cid: model.dst_cid,
					dom_id: model.dom_id,
					rel_id: -1,
					masterSide: masterAndSlave.masterSide,
					slaveSide: masterAndSlave.slaveSide
				},
				selModel: Ext.create('CMDBuild.selection.CMMultiPageSelectionModel', {
					mode: isMany ? 'MULTI' : 'SINGLE',
					avoidCheckerHeader: true,
					idProperty: 'Id' // required to identify the records for the data and not the id of ext
				}),
				successCb: function() {
					me.onAddRelationSuccess();
				}
			});

			this.mon(editRelationWindow, 'destroy', function() {
				this.loadData();
			}, this, { single: true });

			editRelationWindow.show();

			editRelationWindow.grid.getStore().on('load', function (store, records, successful, eOpts) {
				Ext.Function.createDelayed(function() { // HACK to wait store to be correctly loaded
					var cardsIdArray = [];
					editRelationWindow.grid.getStore().each(function(record) {
						cardsIdArray.push(record.get(CMDBuild.core.constants.Proxy.ID));
					});

					var parameters = {};
					parameters[CMDBuild.core.constants.Proxy.DOMAIN_NAME] = domain.get(CMDBuild.core.constants.Proxy.NAME);
					parameters[CMDBuild.core.constants.Proxy.CLASS_NAME] = classData.get(CMDBuild.core.constants.Proxy.NAME);
					parameters[CMDBuild.core.constants.Proxy.CARDS] = Ext.encode(cardsIdArray);
					parameters[CMDBuild.core.constants.Proxy.DOMAIN_DIRECTION] = destination;

					CMDBuild.proxy.Relation.getAlreadyRelatedCards({
						params: parameters,
						loadMask: false,
						scope: this,
						success: function(result, options, decodedResult) {
							var alreadyRelatedCardsIds = [];

							// Create ids array to use as filter
							Ext.Array.forEach(decodedResult.response, function(alreadyRelatedItem, index, allItems) {
								if (alreadyRelatedItem[CMDBuild.core.constants.Proxy.ID]) {
									var parameters = {};
									parameters[CMDBuild.core.constants.Proxy.CARD_ID] = alreadyRelatedItem[CMDBuild.core.constants.Proxy.ID];
									parameters[CMDBuild.core.constants.Proxy.CLASS_NAME] = alreadyRelatedItem[CMDBuild.core.constants.Proxy.CLASS_NAME];
									parameters['domainlimit'] = CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.RELATION_LIMIT);

									// Get all domains of grid-card to check if it have relation with current-card
									CMDBuild.proxy.Relation.readAll({
										params: parameters,
										loadMask: false,
										scope: this,
										success: function(result, options, decodedResult) {
											// Loop through domains array
											Ext.Array.forEach(decodedResult[CMDBuild.core.constants.Proxy.DOMAINS], function(domainItem, index, allItems) {
												if (domainItem[CMDBuild.core.constants.Proxy.ID] == domain.get(CMDBuild.core.constants.Proxy.ID)) {
													// Loop through domain (witch i'm creating a relation) relations array
													Ext.Array.forEach(domainItem[CMDBuild.core.constants.Proxy.RELATIONS], function(domainRelationItem, index, allItems) {
														if (!Ext.Object.isEmpty(classData)) {
															if (domain.get(CMDBuild.core.constants.Proxy.CARDINALITY) == '1:1') {
																alreadyRelatedCardsIds.push(options.params[CMDBuild.core.constants.Proxy.CARD_ID]);
															} else if (
																(
																	domain.get(CMDBuild.core.constants.Proxy.CARDINALITY) == '1:N'
																	&& model[CMDBuild.core.constants.Proxy.DOMAIN_SOURCE] == '_1' // Direct
																)
																|| (
																	domain.get(CMDBuild.core.constants.Proxy.CARDINALITY) == 'N:1'
																	&& model[CMDBuild.core.constants.Proxy.DOMAIN_SOURCE] == '_2' // Inverse
																)
															) {
																alreadyRelatedCardsIds.push(options.params[CMDBuild.core.constants.Proxy.CARD_ID]);
															} else if (
																(
																	domain.get(CMDBuild.core.constants.Proxy.CARDINALITY) == 'N:1'
																	&& model[CMDBuild.core.constants.Proxy.DOMAIN_SOURCE] == '_1' // Direct
																)
																|| (
																	domain.get(CMDBuild.core.constants.Proxy.CARDINALITY) == '1:N'
																	&& model[CMDBuild.core.constants.Proxy.DOMAIN_SOURCE] == '_2' // Inverse
																)
															) {
																// Here should never enter because you'll be blocked from button pop-up
															} else if (
																domain.get(CMDBuild.core.constants.Proxy.CARDINALITY) == 'N:N'
																&& domainRelationItem['dst_id'] == me.card.get(CMDBuild.core.constants.Proxy.ID)
															) {
																alreadyRelatedCardsIds.push(options.params[CMDBuild.core.constants.Proxy.CARD_ID]);
															} else {
																_warning('onAddRelationButtonClick, domain valutation not catch', this);
															}
														} else {
															_warning('onAddRelationButtonClick, empty class data object', this);
														}
													});
												}
											});

											// Add class to disable rows as user feedback
											editRelationWindow.grid.getView().getRowClass = function(record, rowIndex, rowParams, store) {
												return Ext.Array.contains(alreadyRelatedCardsIds, record.get('Id')) ? 'grid-row-disabled' : null;
											};
											editRelationWindow.grid.getView().refresh();

											// Disable row selection
											editRelationWindow.grid.getSelectionModel().addListener('beforeselect', function(selectionModel, record, index, eOpts) {
												return Ext.Array.contains(alreadyRelatedCardsIds, record.get('Id')) ? false : true;
											});
										}
									});
								}
							});
						}
					});
				}, 100)();
			}, this);
		},

		onAddRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		/**
		 * @param {CMRelationPanelModel} model - relation grid model
		 */
		onEditRelationClick: function(model) {
			var me = this;
			var data = model.raw || model.getData();
			var classData = _CMCache.getEntryTypeById(model.get('dst_cid'));
			var domain = _CMCache.getDomainById(model.get('dom_id'));
			var masterAndSlave = getMasterAndSlave(model.get(CMDBuild.core.constants.Proxy.SRC));

			var editRelationWindow = Ext.create('CMDBuild.view.management.classes.relations.CMEditRelationWindow', {
				domain: domain,
				classObject: classData,
				sourceCard: this.card,
				relation: {
					rel_attr: data.attr_as_obj,
					dst_cid: model.get('dst_cid'),
					dst_id: model.get('dst_id'),
					dom_id: model.get('dom_id'),
					rel_id: model.get('rel_id'),
					masterSide: masterAndSlave.masterSide,
					slaveSide: masterAndSlave.slaveSide
				},
				filterType: this.view.id,
				successCb: function() {
					me.onEditRelationSuccess();
				},
				selModel: Ext.create('CMDBuild.selection.CMMultiPageSelectionModel', {
					mode: 'SINGLE',
					idProperty: 'Id' // required to identify the records for the data and not the id of ext
				})
			});

			this.mon(editRelationWindow, 'destroy', function() {
				this.loadData();
			}, this, { single: true });

			// Model fix to select right row(s) with select()
			model.set({
				Code: model.get('dst_code'),
				Description: model.get('dst_desc'),
				Id: model.get('dst_id'),
				id: model.get('dst_id'),
				IdClass: model.get('dst_cid')
			});

			// Select right cards as a modify routine
			editRelationWindow.grid.getStore().on('load', function (store, records, successful, eOpts) {
				Ext.Function.createDelayed(function() { // HACK to wait store to be correctly loaded
					if (!Ext.isEmpty(model))
						editRelationWindow.grid.getSelectionModel().select(model);
				}, 100)();
			}, this);

			editRelationWindow.show();
		},

		onEditRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		/**
		 * @param {Object} model - relation grid model
		 */
		onDeleteRelationClick: function(model) {
			var me = this;
			var masterAndSlave = getMasterAndSlave(model.get(CMDBuild.core.constants.Proxy.SOURCE));

			Ext.Msg.confirm(
				CMDBuild.Translation.attention,
				CMDBuild.Translation.management.modcard.delete_relation_confirm,
				makeRequest,
				this
			);

			function makeRequest(btn) {
				if (btn == 'yes') {
					var domain = _CMCache.getDomainById(model.get('dom_id'));
					var params = {};
					var attributes = {};

					params[CMDBuild.core.constants.Proxy.DOMAIN_NAME] = domain.getName();
					params[CMDBuild.core.constants.Proxy.RELATION_ID] = model.get('rel_id');
					params['master'] = masterAndSlave.masterSide;

					var masterSide = {};
					masterSide[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.card.get('IdClass'));
					masterSide[CMDBuild.core.constants.Proxy.CARD_ID] = me.card.get('Id');

					attributes[masterAndSlave.masterSide] = [masterSide];

					var slaveSide = {};
					slaveSide[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(model.get('dst_cid'));
					slaveSide[CMDBuild.core.constants.Proxy.CARD_ID] = model.get('dst_id');

					attributes[masterAndSlave.slaveSide] = [slaveSide];

					params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(attributes);

					CMDBuild.core.LoadMask.show();
					CMDBuild.proxy.Relation.remove({
						params: params,
						loadMask: false,
						scope: this,
						success: this.onDeleteRelationSuccess,
						callback: function() {
							CMDBuild.core.LoadMask.hide();
							this.loadData();
						}
					});
				}
			}
		},

		onDeleteRelationSuccess: function() {
			this.defaultOperationSuccess();
		},

		defaultOperationSuccess: function() {
			if (true) { // TODO Check if the modified relation was associated to a reference
				this.fireEvent(this.CMEVENTS.serverOperationSuccess);
			} else {
				this.loadData();
			}
		},

		/**
		 * @param {CMRelationPanelModel} model
		 */
		onEditCardClick: function(model) {
			openCardWindow.call(this, model, true);
		},

		/**
		 * @param {CMRelationPanelModel} model
		 */
		onViewCardClick: function(model) {
			openCardWindow.call(this, model, false);
		},

		/**
		 * @param {CMRelationPanelModel} model
		 */
		onOpenAttachmentClick: function(model) {
			var w = new CMDBuild.view.management.common.CMAttachmentsWindow();

			new CMDBuild.controller.management.common.CMAttachmentsWindowController(w, modelToCardInfo(model));

			w.show();
		}
	});

	Ext.define('CMDBuild.controller.management.workflow.CMActivityRelationsController', {
		extend: 'CMDBuild.controller.management.classes.CMCardRelationsController',

		mixins: {
			wfStateDelegate: 'CMDBuild.state.CMWorkflowStateDelegate'
		},

		constructor: function() {
			this.callParent(arguments);

			_CMWFState.addDelegate(this);
		},

		/**
		 * @param {Object} pi
		 */
		updateForProcessInstance: function(pi) {
			this.card = pi;

			var classId = pi.getClassId();

			if (classId) {
				var entryType = _CMCache.getEntryTypeById(classId);

				if (this.lastEntryType != entryType) {
					if (!entryType || entryType.get(CMDBuild.core.constants.Proxy.TABLE_TYPE) == 'simpletable')
						entryType = null;

					this.lastEntryType = entryType;
					this.view.addRelationButton.setDomainsForEntryType(entryType);
				}
			}
		},

		// override
		loadData: function() {
			var pi = _CMWFState.getProcessInstance();

			if (pi != null && tabIsActive(this.view)) {
				var el = this.view.getEl();

				if (el)
					el.mask();

				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.CARD_ID] =  pi.getId();
				parameters[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(pi.getClassId());
				parameters['domainlimit'] = CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.RELATION_LIMIT);

				CMDBuild.proxy.Relation.readAll({
					params: parameters,
					scope: this,
					success: function(result, options, decodedResult) {
						el.unmask();
						this.view.suspendLayouts();
						this.view.fillWithData(decodedResult.domains);
						this.view.resumeLayouts(true);
					}
				});
			}
		},

		// override
		getCardId: function() {
			return this.card.get('id');
		},

		// override
		getClassId: function() {
			return this.card.get('classId');
		},

		// wfStateDelegate
		onProcessClassRefChange: function(entryType) {
			this.view.disable();
			this.view.clearStore();
		},

		onProcessInstanceChange: function(processInstance) {
			if (processInstance && processInstance.isNew()) {
				this.onProcessClassRefChange();
			} else {
				this.updateForProcessInstance(processInstance);

				this.view.enable();
				this.loadData();
			}
		},

		onActivityInstanceChange: Ext.emptyFn,

		// override
		buildCardModuleStateDelegate: Ext.emptyFn,
		onEntryTypeSelected: Ext.emptyFn,
		onCardSelected: Ext.emptyFn
	});

	function modelToCardInfo(model) {
		return {
			Id: model.get('dst_id'),
			IdClass: model.get('dst_cid'),
			Description: model.get('dst_desc')
		};
	}

	function openCardWindow(model, editable) {
		var w = Ext.create('CMDBuild.view.management.common.CMCardWindow', {
			cmEditMode: editable,
			withButtons: editable,
			title: model.get(CMDBuild.core.constants.Proxy.LABEL) + ' - ' + model.get('dst_desc')
		});

		if (editable) {
			w.on('destroy', function() {
				// cause the reload of the main card-grid, it is needed for the case in which I'm editing the target card
				this.fireEvent(this.CMEVENTS.serverOperationSuccess);
				this.loadData();
			}, this, {single: true});
		}

		new CMDBuild.controller.management.common.CMCardWindowController(w, {
			entryType: model.get('dst_cid'), // classid of the destination
			card: model.get('dst_id'), // id of the card destination
			cmEditMode: editable
		});

		w.show();
	}

	function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className;

		if (this.callBacks[className])
			this.callBacks[className].call(this, model);
	}

	function onItemDoubleclick(grid, model, html, index, e, options) {
		this.onFollowRelationClick(model);
	}

	// Define who is the master
	function getMasterAndSlave(source) {
		var out = {};
		if (source == '_1') {
			out.slaveSide = '_2';
			out.masterSide = '_1';
		} else {
			out.slaveSide = '_1';
			out.masterSide = '_2';
		}

		return out;
	}

})();