(function() {

	Ext.require(['CMDBuild.proxy.Card']);

	var MD = "detail";
	var FK = "foreignkey";

	Ext.define("CMDBuild.controller.management.classes.masterDetails.CMMasterDetailsController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",
		constructor: function(v, sc) {

			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.view = v;
			this.superController = sc;

			this.entryType = null;
			this.card = null;

			this.currentForeignKey = null;
			this.currentDetail = null;

			this.mon(this.view.tabs,"click", onTabClick, this);
			this.mon(this.view.detailGrid, 'beforeitemclick', cellclickHandler, this);
			this.mon(this.view.detailGrid, "itemdblclick", onDetailDoubleClick, this);
			this.mon(this.view.addDetailButton, "cmClick", this.onAddDetailButtonClick, this);

			this.addEvents(["empty"]);

			this.mon(this.view, "empty", function() {
				this.fireEvent("empty", this.view.isVisible());
			}, this);

			this.callBacks = {
				'action-masterdetail-edit': this.onEditDetailClick,
				'action-masterdetail-show': this.onShowDetailClick,
				'action-masterdetail-delete': this.onDeleteDetailClick,
				'action-masterdetail-graph': this.onOpenGraphClick,
				'action-masterdetail-note': this.onOpenNoteClick,
				'action-masterdetail-attach': this.onOpenAttachmentClick
			};
		},

		/**
		 * @param {Object} configObj
		 * 	{
		 * 		{Boolean} editable
		 * 	}
		 *
		 * @return {CMDBuild.view.management.common.CMCardWindow}
		 */
		buildWindow: function(configObj) {
			return Ext.create('CMDBuild.view.management.common.CMCardWindow', {
				referencedIdClass: this.card.get('IdClass'),
				fkAttribute: this.currentForeignKey,
				masterData: this.card,
				detail: this.currentDetail,
				cmEditMode: configObj.editable,
				withButtons: configObj.editable
			});
		},

		/**
		 * @params {Object} params
		 */
		onAddDetailButtonClick: function(params) {
			var window = this.buildWindow({
				entryType: params.classId,
				editable: true
			});

			new CMDBuild.controller.management.common.CMAddDetailWindowController(window, {
				entryType: params.classId,
				cmEditMode: true
			});

			window.show();

			window.mon(window, 'destroy', function() {
				this.view.reload();
			}, this, { single: true });
		},

		onEntryTypeSelected: function(entryType) {
			this.callParent(arguments);

			this.currentTab = null;
			this.currentForeignKey = null;
			this.currentDetail = null;

			this.view.loadDetailsAndFKThenBuildSideTabs(this.entryType.get("id"));
			this.view.resetDetailGrid();
		},

		onCardSelected: function(card) {
			this.callParent(arguments);

			this.view.setDisabled(this.view.empty);
			// given the tab is not active but enabled
			// and we change card
			// when the tab is activated
			// then the grid should be updated
			if (tabIsActive(this.view)) {
				updateDetailGrid.call(this);
			} else {
				this.view.on('activate', updateDetailGrid, this, {single: true});
			}
		},

		onAddCardButtonClick: function(classIdOfNewCard) {
			this.view.disable();
		},

		/**
		 * @param {Object} model - card grid model
		 */
		onEditDetailClick: function(model) {
			var window = this.buildWindow({
				editable: true
			});

			var windowController = new CMDBuild.controller.management.common.CMDetailWindowController(window, {
				entryType: model.get('IdClass'),
				card: model.get('Id'),
				cmEditMode: true
			});

			window.mon(window, 'destroy', function() {
				this.view.reload();
				delete windowController;
				delete window;
			}, this, { single: true });

			window.show();
		},

		onShowDetailClick: function(model) {
			var w = this.buildWindow({
				editable: false
			});

			var c = new CMDBuild.controller.management.common.CMDetailWindowController(w, {
				entryType: model.get("IdClass"),
				card: model.get("Id"),
				cmEditMode: false
			});

			w.mon(w, "destroy", function() {
				delete c;
				delete w;
			}, this, {single: true});

			w.show();
		},

		onDeleteDetailClick: function(model) {
			Ext.Msg.confirm(CMDBuild.Translation.attention,
				CMDBuild.Translation.management.modcard.delete_card_confirm,
				makeRequest, this);

			var me = this;
			var detailCard = model;
			var masterCard = this.card;

			function makeRequest(btn) {
				if (btn != 'yes') {
					return;
				}

				var params = {};

				if (this.currentDetail) {
					params.domainName = me.currentDetail.get("name");
				};

				params.masterClassName = _CMCache.getEntryTypeNameById(masterCard.get("IdClass"));
				params.masterCardId = masterCard.get("Id");
				params.detailClassName = _CMCache.getEntryTypeNameById(detailCard.get("IdClass"));
				params.detailCardId = detailCard.get("Id");

				CMDBuild.core.LoadMask.show();
				CMDBuild.proxy.Relation.removeDetail({
					params : params,
					loadMask: false,
					callback: function() {
						CMDBuild.core.LoadMask.hide();
						me.view.reload();
					}
				});
			};
		},

		onOpenGraphClick: function(model) {
			Ext.create('CMDBuild.controller.management.common.graph.Graph', {
				parentDelegate: this,
				classId: model.get('IdClass'),
				cardId: model.get('id')
			});
		},

		onOpenNoteClick: function(model) {
			var editable = (model && model.raw && model.raw.priv_write);

			var noteWindow = new CMDBuild.view.management.common.CMNoteWindow({
				withButtons: editable,
				withTbar: false
			}).show();

			var noteWindowController = new CMDBuild.view.management.common.CMNoteWindowController(noteWindow);
			noteWindowController.onCardSelected(model);

			noteWindow.mon(
				noteWindow,
				'destroy',
				function() {
					this.view.reload();
				},
				this,
				{ single: true }
			);
		},

		onOpenAttachmentClick: function(model) {
			var w = new CMDBuild.view.management.common.CMAttachmentsWindow();
			new CMDBuild.controller.management.common.CMAttachmentsWindowController(w,modelToCardInfo(model));
			w.show();
		},

		onTabClick: onTabClick,

		/**
		 * @param {Mixed} tab
		 */
		activeTabSet: function(tab) {
			return this.view.tabs.setActiveTab(tab);
		}
	});

	function masterSide(domainRecord) {
		return {
			"1:N" : 1,
			"N:1" : 2
		}[domainRecord.get("cardinality")];
	}

	function detailSide(domainRecord) {
		return 3-masterSide(domainRecord);
	}

	function modelToCardInfo(model) {
		return {
			Id: model.get("Id"),
			IdClass: model.get("IdClass"),
			Description: model.get("Description")
		};
	}

	function onDetailDoubleClick(grid, model, html, index, e, options) {
		CMDBuild.global.controller.MainViewport.cmfg('mainViewportCardSelect', {
			Id: model.get("Id"),
			IdClass: model.get("IdClass"),
			activateFirstTab: true
		});
	}

	function removeCard(model) {
		CMDBuild.core.LoadMask.show();
		CMDBuild.proxy.Card.remove({
			params : {
				"IdClass": model.get("IdClass"),
				"Id": model.get("Id")
			},
			important: true,
			loadMask: false,
			scope : this,
			success : updateDetailGrid,
			callback : function() {
				CMDBuild.core.LoadMask.hide();
			}
		});
	}

	function updateDetailGrid() {
		if (this.card != null) {
			var p = {
				masterCard: this.card
			};

			if (this.currentDetail != null) {
				p["detail"] = this.currentDetail;
				this.view.updateGrid(MD, p);
			} else if (this.currentForeignKey != null) {
				p["detail"] = this.currentForeignKey;
				this.view.updateGrid(FK, p);
			} else {
				this.view.activateFirstTab();
			}
		}
	}

	function onTabClick(tab) {
		// if building the details tables cannot select the detail on the grid
		// anyway the onTabClick is called at end of buildTabs
		if (this.view.buildingTabsDetails == true) {
			return;
		}
		if (this.currentTab === tab || !tabIsActive(this.view)) {
			return;
		}

		var targetPanel = tab.targetPanel,
			type = targetPanel.detailType,
			detail = this.view.details[type][targetPanel.detailId];
		this.view.addDetailButton.enable();
		this.currentTab = tab;

// TODO: future implementation
//		// History record save
//		if (!Ext.isEmpty(_CMCardModuleState.entryType) && !Ext.isEmpty(_CMCardModuleState.card))
//			CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
//				moduleId: 'class',
//				entryType: {
//					description: _CMCardModuleState.entryType.get(CMDBuild.core.constants.Proxy.TEXT),
//					id: _CMCardModuleState.entryType.get(CMDBuild.core.constants.Proxy.ID),
//					object: _CMCardModuleState.entryType
//				},
//				item: {
//					description: _CMCardModuleState.card.get('Description') || _CMCardModuleState.card.get('Code'),
//					id: _CMCardModuleState.card.get(CMDBuild.core.constants.Proxy.ID),
//					object: _CMCardModuleState.card
//				},
//				section: {
//					description: this.view.title,
//					object: this.view
//				},
//				subSection: {
//					description: this.currentTab[CMDBuild.core.constants.Proxy.TEXT],
//					object: this.currentTab.targetPanel
//				}
//			});

		if (type == MD) {
			selectDetail.call(this, detail);
		} else {
			selectFK.call(this, detail);
		}

		updateDetailGrid.call(this);
	}

	function selectDetail(detail) {
		this.currentForeignKey = undefined;
		this.currentDetail = detail;
		this.view.selectDetail(detail);
	}

	function selectFK(fk) {
		this.currentDetail = undefined;
		this.currentForeignKey = fk;
		this.view.selectForeignKey(fk);
	}

	function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className;

		if (this.callBacks[className]) {
			this.callBacks[className].call(this, model);
		}
	}

})();