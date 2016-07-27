(function () {

	Ext.define('CMDBuild.controller.management.widget.manageRelation.CMManageRelationController', {

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.ManageRelation'
		],

		mixins: {
			observable: 'Ext.util.Observable',
			widgetcontroller: 'CMDBuild.controller.management.common.widgets.CMWidgetController'
		},

		constructor: function (view, ownerController, widgetDef, clientForm, card) {
			this.mixins.observable.constructor.call(this, arguments);

			if (typeof view == 'undefined') {
				throw ('OOO snap, you have not passed a view to me');
			} else {
				this.view = view;
				this.view.delegate = this; // Delegate injection
			}

			this.superController = ownerController;
			this.card = null;
			this.entryType = null;

			this.buildCardModuleStateDelegate();

			this.hasDomains = false;

			this.callBacks = {
				'action-relation-delete': this.onDeleteRelationClick,
				'action-relation-deletecard': this.onDeleteCard,
				'action-relation-edit': this.onEditRelationClick,
				'action-relation-editcard': this.onEditCardClick,
				'action-relation-viewcard': this.onViewCardClick
			};

			this.view.store.getRootNode().on('append', function (root, newNode) {
				// the nodes with depth == 1 are the folders
				if (newNode.get('depth') == 1) {
					newNode.on('expand', onDomainNodeExpand, this, {single: true});
				}
			}, this);

			this.mon(this.view, this.view.CMEVENTS.openGraphClick, this.onShowGraphClick, this);
			this.mon(this.view, this.view.CMEVENTS.addButtonClick, this.onAddRelationButtonClick, this);
			this.mon(this.view, 'beforeitemclick', cellclickHandler, this);
			this.mon(this.view, 'activate', this.loadData, this);

			this.CMEVENTS = {
				serverOperationSuccess: 'cm-server-success'
			};

			this.addEvents(this.CMEVENTS.serverOperationSuccess);

			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			ensureEntryType(this);

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: this.widgetConf, // TODO: pass only the 'objId'??
				serverVars: this.getTemplateResolverServerVars()
			});

			this.templateResolverIsBusy = false;

			this.readOnly = !(this.widgetConf['singleSelection'] || this.widgetConf['multiSelection']);

			this.idClass = this.targetEntryType.getId();
			this.domain = _CMCache.getDomainByName(this.widgetConf['domainName']);
		},

		buildCardModuleStateDelegate: function () {
			var me = this;

			this.cardStateDelegate = new CMDBuild.state.CMCardModuleStateDelegate();

			this.cardStateDelegate.onEntryTypeDidChange = function (state, entryType) {
				me.onEntryTypeSelected(entryType);
			};

			this.cardStateDelegate.onModifyCardClick = function (state) {
				me.onModifyCardClick();
			};

			this.cardStateDelegate.onCardDidChange = function (state, card) {
				Ext.suspendLayouts();
				me.onCardSelected(card);
				Ext.resumeLayouts();
			};

			_CMCardModuleState.addDelegate(this.cardStateDelegate);

			if (this.view) {
				var me = this;
				this.mon(me.view, 'destroy', function (view) {
					_CMCardModuleState.removeDelegate(me.cardStateDelegate);
					delete me.cardStateDelegate;
				});
			}

		},

		/**
		 * Forward onAbortCardClick event to superController
		 */
		onAbortCardClick: function () {
			this.superController.onAbortCardClick(); // Forward abort event
		},

		onAddCardButtonClick: Ext.emptyFn,

		onAddRelationButtonClick: function (d) {
			var domain = _CMCache.getDomainById(d.dom_id);
			var isMany = false;
			var destination = d.src == '_1' ? '_2' : '_1';

			if (domain) {
				isMany = domain.isMany(destination);
			};

			var me = this;
			var masterAndSlave = getMasterAndSlave(d.src);

			var editRelationWindow = Ext.create('CMDBuild.view.management.widget.manageRelation.CMEditRelationWindow', {
				sourceCard: this.card,
				relation: {
					dst_cid: d.dst_cid,
					dom_id: d.dom_id,
					rel_id: -1,
					masterSide: masterAndSlave.masterSide,
					slaveSide: masterAndSlave.slaveSide
				},
				selModel: new CMDBuild.selection.CMMultiPageSelectionModel({
					mode: isMany ? 'MULTI' : 'SINGLE',
					avoidCheckerHeader: true,
					idProperty: 'Id' // required to identify the records for the data and not the id of ext
				}),
				filterType: this.view.id,
				successCb: function () {
					me.onAddRelationSuccess();
				}
			});

			this.mon(editRelationWindow, 'destroy', function () {
				this.loadData();
			}, this, {single: true});

			editRelationWindow.show();
		},

		onAddRelationSuccess: function () {
			this.defaultOperationSuccess();
		},

		onCardSelected: function (card) {
			this.card = card;
			this.view.clearStore();
			this.view.disable();

			if (card) {
				this.updateCurrentClass(card);

				if (this.hasDomains) {
					this.view.enable();
					this.loadData();
				}
			}
		},

		onCloneCard: function () {
			if (this.view) {
				this.view.disable();
			}
		},

		onDeleteCard: function (model) {
			this.cardToDelete = model;
			this.onDeleteRelationClick(model);
		},

		onDeleteRelationClick: function (model) {
			Ext.Msg.confirm(CMDBuild.Translation.attention,
				CMDBuild.Translation.management.modcard.delete_relation_confirm, makeRequest, this);

			var masterAndSlave = getMasterAndSlave(model.get('src'));
			var me = this;
			function makeRequest(btn) {
				if (btn != 'yes') {
					return;
				}

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

				CMDBuild.proxy.widget.ManageRelation.removeRelation({
					params: params,
					scope: this,
					success: this.onDeleteRelationSuccess,
					callback: function () {
						this.loadData();
					}
				});
			};
		},

		onEditCardClick: function (model) {
			openCardWindow.call(this, model, true);
		},

		onEditMode: function () {
			resolveTemplate.call(this);
		},

		onEditRelationClick: function (model) {
			var me = this;
			var data = model.raw || model.data;
			var masterAndSlave = getMasterAndSlave(model.get('src'));
			var editRelationWindow = Ext.create('CMDBuild.view.management.widget.manageRelation.CMEditRelationWindow', {
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
				successCb: function () {
					me.onEditRelationSuccess();
				},
				selModel: new CMDBuild.selection.CMMultiPageSelectionModel({
					mode: 'SINGLE',
					idProperty: 'Id' // required to identify the records for the data and not the id of ext
				})
			});

			this.mon(editRelationWindow, 'destroy', function () {
				this.loadData();
			}, this, {single: true});

			editRelationWindow.show();
		},

		onEditRelationSuccess: function () {
			this.defaultOperationSuccess();
		},

		onEntryTypeSelected: function (entryType) {
			this.entryType = entryType;

			this.card = null;

			if (!this.entryType || this.entryType.get('tableType') == CMDBuild.core.constants.Global.getTableTypeSimpleTable()) {
				this.entryType = null;
			}

			this.view.disable();
			this.view.clearStore();
		},

		onDeleteRelationSuccess: function () {
			if (this.cardToDelete) {
				removeCard.call(this);
			} else {
				this.defaultOperationSuccess();
			}
		},

		/**
		 * Forward onModifyCardClick event to superController
		 */
		onModifyCardClick: function () {
			this.superController.onModifyCardClick(); // Forward modify event
		},

		onShowGraphClick: function () {
			Ext.create('CMDBuild.controller.management.common.graph.Graph', {
				parentDelegate: this,
				classId: parseInt(this.idClass),
				cardId: parseInt(this.cardId)
			});
		},

		onViewCardClick: function (model) {
			openCardWindow.call(this, model, false);
		},

		defaultOperationSuccess: function () {
			this.loadData();
		},

		getCardId: function (cb) {
			// remember that -1 is the id for a new card
			var idCard = this.getVariable('xa:id');
			if (!idCard) {
				return -1;
			}

			if (typeof idCard == 'string') {
				idCard = parseInt(idCard);
				if (isNaN(idCard)) {
					idCard = -1;
				}
			}

			return idCard;
		},

		// override
		beforeActiveView: function () {
			this.view.addRelationButton.setDomainsForEntryType(this.targetEntryType, this.domain.getId());

			var me = this;
			this.templateResolver.resolveTemplates({
				attributes: ['objId'],
				callback: function (o) {
					me.cardId = o['objId'];
					me.card = getFakeCard(me);

					if (me.cardId > 0) {
						me.loadData();
						me.view.addRelationButton.enable();
					} else {
						me.view.fillWithData();
						me.view.addRelationButton.disable();
					}
				},
				scope: this
			});

		},

		getData: function () {
			var out = null;
			out = {};
			var data = [],
				nodes = Ext.query('input[name='+this.view.CHECK_NAME+']');

			for (var i=0, l=nodes.length, item=null; i<l; ++i) {
				item = nodes[i];

				if(item && item.checked) {
					data.push(item.value);
				}
			}

			out['output'] = data;

			return out;
		},

		isValid: function () {
			if (this.widgetConf['required'] && !this.readOnly) {
				try {
					return this.getData()['output'].length > 0;
				} catch (e) {
					// if here, data is null or data has not selections,
					// so the ww is not valid
					return false;
				}

			} else {
				return true;
			}
		},

		// override
		loadData: function () {
			var domain = this.domain;

			if (domain == null) {
				_debug('It is not possible to lad data for null domain');
				return;
			}

			buildAdapterForExpandNode.call(this);

			var parameters = {};
			parameters[CMDBuild.core.constants.Proxy.CARD_ID] =  this.cardId;
			parameters[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.idClass);
			parameters[CMDBuild.core.constants.Proxy.DOMAIN_ID] = domain.getId();
			parameters[CMDBuild.core.constants.Proxy.DOMAIN_SOURCE] = getSrc(this);

			CMDBuild.proxy.widget.ManageRelation.readAllRelations({
				params: parameters,
				scope: this,
				success: function (a,b, response) {
					this.view.fillWithData(response.domains);
				}
			});
		},

		getClassId: function () {
			return this.card.get('IdClass');
		},

		getLabel: function () {
			var label = '';
			if (this.widgetConf) {
				label = this.widgetConf['label'];
			}

			return label;
		},

		updateCurrentClass: function (card) {
			var classId = card.get('IdClass'),
				currentClass = _CMCache.getEntryTypeById(classId);

			if (this.currentClass != currentClass) {
				if (!currentClass || currentClass.get('tableType') == CMDBuild.core.constants.Global.getTableTypeSimpleTable()) {
					currentClass = null;
				}
				this.currentClass = currentClass;
				this.hasDomains = this.view.addRelationButton.setDomainsForEntryType(currentClass);
			}
		}
	});

	function modelToCardInfo(model) {
		return {
			Id: model.get('dst_id'),
			IdClass: model.get('dst_cid'),
			Description: model.get('dst_desc')
		};
	}

	function openCardWindow(model, editable) {
		var w = new CMDBuild.view.management.common.CMCardWindow({
			cmEditMode: editable,
			withButtons: editable,
			title: model.get('label') + ' - ' + model.get('dst_desc')
		});

		if (editable) {
			w.on('destroy', function () {
				// cause the reload of the main card-grid, it is needed
				// for the case in which I'm editing the target card
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

	function ensureEntryType(me) {
		me.targetEntryType = _CMCache.getEntryTypeByName(me.widgetConf['className']);

		if (me.targetEntryType == null) {
			throw {error: 'There is no entry type for this widget', widget: me.widgetConf};
		}
	}

	function removeCard() {
		if (this.cardToDelete) {
			CMDBuild.proxy.widget.ManageRelation.removeCard({
				params: {
					'IdClass': this.cardToDelete.get('dst_cid'),
					'Id': this.cardToDelete.get('dst_id')
				},
				scope: this,
				callback: function (options, success, response) {
					delete this.cardToDelete;

					this.loadData();
				}
			});
		}
	}

	function buildAdapterForExpandNode() {
		var data = {
			Id: this.cardId,
			IdClass: this.idClass
		};
		this.currentCard = {
			get: function (k) {
				return data[k];
			}
		};
	}

	function getSrc(me) {
		var src = me.widgetConf['source'];
		if (src == null) {
			// Same code of CMDBuild.core.Utils.getEntryTypeAncestorsId()
			var entryType = _CMCache.getEntryTypeById(me.targetEntryType.getId());
			var targetClassId = [];

			if (entryType) {
				targetClassId.push(entryType.get("id"));

				while (entryType.get("parent") != "") {
					entryType = _CMCache.getEntryTypeById(entryType.get("parent"));
					targetClassId.push(entryType.get("id"));
				}
			}
			// END: Same code of CMDBuild.core.Utils.getEntryTypeAncestorsId()

			if (Ext.Array.contains(targetClassId, me.domain.get('idClass1'))) {
				src = '_1';
			} else {
				src = '_2';
			}
		}

		return src;
	}

	function resolveTemplate() {
		resolve.call(this);

		function resolve() {
			this.templateResolverIsBusy = true;

			this.templateResolver.resolveTemplates({
				attributes: ['objId'],
				callback: onTemplateResolved,
				scope: this
			});
		}

		function onTemplateResolved(out, ctx) {
			this.templateResolverIsBusy = false;

			this.templateResolver.bindLocalDepsChange(resolveTemplate, this);
		}
	}

	// a object that fake a card,
	// is passed at the ModifyRelationWindow
	function getFakeCard(me) {
		var data = {
			IdClass: me.idClass,
			Id: me.cardId
		};

		return {
			get: function (k) {
				return data[k];
			}
		};
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className;

		if (this.callBacks[className]) {
			this.callBacks[className].call(this, model);
		}
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

	function onDomainNodeExpand(node) {
		if (node.get('relations_size') > CMDBuild.configuration.instance.get('relationLimit')) {
			node.removeAll();

			var parameters = {};
			parameters[CMDBuild.core.constants.Proxy.CARD_ID] = this.getCardId();
			parameters[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getClassId());
			parameters[CMDBuild.core.constants.Proxy.DOMAIN_ID] = node.get('dom_id');
			parameters[CMDBuild.core.constants.Proxy.DOMAIN_SOURCE] = node.get('src');

			CMDBuild.proxy.widget.ManageRelation.readAllRelations({
				params: parameters,
				scope: this,
				success: function (a,b, response) {
					this.view.suspendLayouts();
					var cc = this.view.convertRelationInNodes( //
						response.domains[0].relations, //
							node.data.dom_id, //
							node.data.src, //
							node.data, //
							node //
							);
					this.view.resumeLayouts(true);
				}
			});
		}
	}

})();
