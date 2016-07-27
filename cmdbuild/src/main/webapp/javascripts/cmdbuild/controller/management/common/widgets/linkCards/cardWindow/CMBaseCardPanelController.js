(function() {

	Ext.require([
		'CMDBuild.controller.management.classes.StaticsController',
		'CMDBuild.core.constants.Global',
		'CMDBuild.core.Message'
	]);

	Ext.define("CMDBuild.controller.management.common.widgets.linkCards.cardWindow.CMBaseCardPanelController", {
		extend: "CMDBuild.controller.management.common.widgets.linkCards.cardWindow.CMModCardSubController",

		mixins: {
			observable : "Ext.util.Observable"
		},

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.Card'
		],

		cardDataProviders: [],

		constructor: function(view, supercontroller, widgetControllerManager) {

			this.callParent(arguments);

			this.mixins.observable.constructor.call(this, arguments);

			var ev = this.view.CMEVENTS;

			if (widgetControllerManager) {
				this.widgetControllerManager = widgetControllerManager;
			} else {
				var widgetManager = Ext.create('CMDBuild.view.management.common.widgets.linkCards.cardWindow.CMWidgetManager', {
					mainView: this.view
				});
				this.widgetControllerManager = Ext.create('CMDBuild.controller.management.common.widgets.linkCards.cardWindow.CMWidgetManagerController', widgetManager);
			}

			this.widgetControllerManager.setDelegate(this);

			this.CMEVENTS = {
				cardSaved: "cm-card-saved",
				abortedModify: "cm-card-modify-abort",
				editModeDidAcitvate: ev.editModeDidAcitvate,
				displayModeDidActivate: ev.displayModeDidActivate
			};

			this.addEvents(this.CMEVENTS.cardSaved, this.CMEVENTS.abortedModify, ev.editModeDidAcitvate, ev.displayModeDidActivate);
			this.relayEvents(this.view, [ev.editModeDidAcitvate, ev.displayModeDidActivate]);

			this.mon(this.view, ev.modifyCardButtonClick, function() { this.onModifyCardClick.apply(this, arguments); }, this);
			this.mon(this.view, ev.saveCardButtonClick, function() { this.onSaveCardClick.apply(this, arguments); }, this);
			this.mon(this.view, ev.abortButtonClick, function() { this.onAbortCardClick.apply(this, arguments); }, this);
			this.mon(this.view, ev.widgetButtonClick, this.onWidgetButtonClick, this);
			this.mon(this.view, ev.editModeDidAcitvate, this.onCardGoesInEdit, this);
		},

		onEntryTypeSelected: function() {
			this.unlockCard();

			if (this.view.isInEditing()) {
				this.view.displayMode();
			}

			this.callParent(arguments);
			this.loadFields(this.entryType.get("id"));

			if (this.widgetControllerManager) {
				this.widgetControllerManager.removeAll();
			}
		},

		/**
		 * @param {Object} card
		 */
		onCardSelected: function(card) {
			var me = this;

			this.unlockCard();

			this.callParent(arguments);

			if (this.view.isInEditing())
				this.view.displayMode();

			this.view.reset();

			if (!this.entryType || !this.card)
				return;

			// The right way it should work is to execute the getCard query to the server every time i select a new card in grid
			var loadRemoteData = true;

			// If the entryType id and the id of the card are different the fields are not right, refill the form before the loadCard
			var reloadFields = this.entryType.get(CMDBuild.core.constants.Proxy.ID) != this.card.get("IdClass");

			// Defer this call to release the UI event manage
			Ext.defer(buildWidgetControllers, 1, this, [card]);

			if (reloadFields) {
				this.loadFields(this.card.get("IdClass"), function() {
					me.loadCard(loadRemoteData);
				});
			} else {
				me.loadCard(loadRemoteData);
			}

			// History record save
			if (!Ext.isEmpty(_CMCardModuleState.entryType) && !Ext.isEmpty(card))
				CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
					moduleId: 'class',
					entryType: {
						description: _CMCardModuleState.entryType.get(CMDBuild.core.constants.Proxy.TEXT),
						id: _CMCardModuleState.entryType.get(CMDBuild.core.constants.Proxy.ID),
						object: _CMCardModuleState.entryType
					},
					item: {
						description: card.get('Description') || card.raw['Description'] || card.get('Code') || card.raw['Code'],
						id: card.get(CMDBuild.core.constants.Proxy.ID),
						object: card
					}
				});
		},

		onModifyCardClick: function() {
			if (this.isEditable(this.card)) {
				var me = this;

				this.lockCard(function() {
					me.loadCard(true, null, function() { // Force card load before entering in edit mode
						me.view.editMode();
					});
				});
			}

			this.callParent(arguments);
		},

		onSaveCardClick: function() {
			var me = this;

			var params = {};
			params[CMDBuild.core.constants.Proxy.CARD_ID] = this.cloneCard ? -1 : this.card.get("Id");
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.card.get("IdClass"));

			addDataFromCardDataPoviders(me, params);

			if (thereAraNotWrongAttributes(me)) {
				this.doFormSubmit(params);
			}
		},

		/**
		 * @param {Object} params
		 */
		doFormSubmit: function (params) {
			CMDBuild.proxy.Card.update({
				params: Ext.Object.merge(params, this.view.getForm().getValues()),
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					// Adapter to old method behaviour for classes witch extends this one
					var fakeOperation = {};
					fakeOperation['result'] = decodedResponse;
					fakeOperation['params'] = options.params;

					this.onSaveSuccess(this.view.getForm(), fakeOperation);
				}
			});
		},

		/**
		 * @param {Ext.form.Basic} form
		 * @param {Object} operation
		 */
		onSaveSuccess: function(form, operation) {
			this.view.displayMode();

			var cardData = {
				Id: operation.result[CMDBuild.core.constants.Proxy.ID] || this.card.get("Id"), // if is a new card, the id is given by the request
				IdClass: this.entryType.get(CMDBuild.core.constants.Proxy.ID)
			};

			this.fireEvent(this.CMEVENTS.cardSaved, cardData);
		},

		onAbortCardClick: function() {
			if (this.card && this.card.get("Id") == -1) {
				this.onCardSelected(null);
			} else {
				this.onCardSelected(this.card);
			}

			this.callParent(arguments); // Forward abort event

			this.fireEvent(this.CMEVENTS.abortedModify);
		},

		onAddCardButtonClick: function(classIdOfNewCard) {
			if (!classIdOfNewCard) {
				return;
			}

			this.onCardSelected(new CMDBuild.DummyModel({
				IdClass: classIdOfNewCard,
				Id: -1
			}));

			this.view.editMode();
		},

		addCardDataProviders: function(dataProvider) {
			this.cardDataProviders.push(dataProvider);
		},

		loadFields: function(entryTypeId, cb) {
			var me = this;
			_CMCache.getAttributeList(entryTypeId, function(attributes) {
				me.view.fillForm(attributes, editMode = false);
				if (cb) {
					cb();
				}
			});
		},

		/**
		 * @param {Boolean} loadRemoteData
		 * @param {Object} params
		 * @param {Function} cb
		 */
		loadCard: function(loadRemoteData, params, cb) {
			var me = this;
			var cardId;

			if (params) {
				cardId = params.Id || params.cardId;
			} else {
				cardId = me.card.get('Id');
			}

			if (cardId && cardId != '-1' && (loadRemoteData || me.view.hasDomainAttributes())) {
				if (!params) {
					var params = {};
					params[CMDBuild.core.constants.Proxy.CARD_ID] = me.card.get('Id');
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.card.get('IdClass'));
				}

				CMDBuild.proxy.Card.read({
					params: params,
					loadMask: false,
					success: function(result, options, decodedResult) {
						var data = decodedResult.card;

						if (me.card) {
							// Merge the data of the selected card with the remote data loaded from the server. The reason is that in the activity list
							// the card have data that are not returned from the server, so use the data already in the record. For activities, the privileges
							// returned from the server are of the class and not of the activity
							data = Ext.Object.merge((me.card.raw || me.card.data), data);
						}

						addRefenceAttributesToDataIfNeeded(decodedResult.referenceAttributes, data);
						var card = Ext.create('CMDBuild.DummyModel', data);

						(typeof cb == 'function') ? cb(card) : me.loadCardStandardCallBack(card);
					}
				});
			} else {
				me.loadCardStandardCallBack(me.card);
			}
		},

		loadCardStandardCallBack: function(card) {
			var me = this;
			me.view.loadCard(card);
			if (card) {
				if (me.isEditable(card)) {
					if (card.get("Id") == -1 || me.cmForceEditing) {
						me.view.editMode();
						me.cmForceEditing = false;
					} else {
						me.view.displayMode(enableTBar = true);
					}
				} else {
					me.view.displayModeForNotEditableCard();
				}
			}
		},

		isEditable: function(card) {
			var privileges = _CMUtils.getEntryTypePrivilegesByCard(card);
			return (privileges.create);
		},

		setWidgetManager: function(wm) {
			this.widgetManager = wm;
		},

		onWidgetButtonClick: function(w) {
			if (this.widgetControllerManager) {
				this.widgetControllerManager.onWidgetButtonClick(w);
			}
		},

		onCardGoesInEdit: function() {
			if (this.widgetControllerManager) {
				this.widgetControllerManager.onCardGoesInEdit();
			}
		},

		/**
		 * @param {Function} success
		 */
		lockCard: function(success) {
			if (CMDBuild.configuration.instance.get('enableCardLock')) { // TODO: use proxy constants
				if (
					this.card
					&& this.card.get("Id") >= 0 // Avoid lock on card create
				) {
					CMDBuild.proxy.Card.lock({
						params: {
							id: this.card.get("Id")
						},
						loadMask: false,
						success: success
					});
				}
			} else {
				success();
			}
		},

		unlockCard: function() {
			if (CMDBuild.configuration.instance.get('enableCardLock')) { // TODO: use proxy constants
				if (
					this.card
					&& this.view.isInEditing()
					&& this.card.get("Id") >= 0 // Avoid unlock on card create
				) {
					CMDBuild.proxy.Card.unlock({
						params: {
							id: this.card.get("Id")
						},
						loadMask: false
					});
				}
			}
		},

		// override
		onCloneCard: Ext.emptyFn,

		// widgetManager delegate
		ensureEditPanel: function() {
			this.view.ensureEditPanel();
		}
	});

	Ext.define("CMDBuild.controller.management.classes.CMCardDataProvider", {
		/*
		 * Extending this class you have to
		 * get a value to the cardDataName attribute
		 */
		cardDataName: null,

		getCardDataName: function() {
			return this.cardDataName;
		},

		/*
		 * Implement it on subclasses
		 */
		getCardData: function() {
			throw "You have to implement the getCardData method in " + this.$className;
		}
	});

	function buildWidgetControllers(card) {
		if (this.widgetControllerManager) {
			this.widgetControllerManager.buildControllers(card);
		}
	}

	function addDataFromCardDataPoviders(me, params) {
		for (var provider in me.cardDataProviders) {
			provider = me.cardDataProviders[provider];
			if (typeof provider.getCardData == "function") {
				var values = provider.getCardData();
				if (values) {
					params[provider.getCardDataName()] = values;
				}
			}
		}

		return params;
	}

	function thereAraNotWrongAttributes(me) {
		var form = me.view.getForm();
		var invalidAttributes = CMDBuild.controller.management.classes.StaticsController.getInvalidAttributeAsHTML(form);
		if (invalidAttributes != null) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.core.constants.Global.getErrorMsgCss(), CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.core.Message.error(null, msg + invalidAttributes, false);
			return false;
		} else {
			return true;
		}
	}

	function addRefenceAttributesToDataIfNeeded(referenceAttributes, data) {
		// the referenceAttributes are like this:
		//	referenceAttributes: {
		//		referenceName: {
		//			firstAttr: 32,
		//			secondAttr: "Foo"
		//		},
		//		secondReference: {...}
		//	}
		var ra = referenceAttributes;
		if (ra) {
			for (var referenceName in ra) {
				var attrs = ra[referenceName];
				for (var attribute in attrs) {
					data["_" + referenceName + "_" + attribute] = attrs[attribute];
				}
			}
		}
	}

})();
