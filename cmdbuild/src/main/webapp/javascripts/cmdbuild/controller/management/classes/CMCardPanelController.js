(function() {

	Ext.require(['CMDBuild.proxy.Card']);

	Ext.define("CMDBuild.controller.management.classes.CMCardPanelController", {

		mixins : {
			observable : "Ext.util.Observable"
		},

		extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",

		constructor: function(view, supercontroller, widgetControllerManager) {

			this.callParent(arguments);

			this.mixins.observable.constructor.call(this, arguments);

			this.CMEVENTS = Ext.apply(this.CMEVENTS,  {
				cardRemoved: "cm-card-removed",
				cloneCard: "cm-card-clone"
			});

			this.addEvents(
				this.CMEVENTS.cardRemoved,
				this.CMEVENTS.cloneCard,
				this.CMEVENTS.cardSaved,
				this.CMEVENTS.editModeDidAcitvate,
				this.CMEVENTS.displayModeDidActivate
			);

			var ev = this.view.CMEVENTS;
			this.mon(this.view, ev.removeCardButtonClick, this.onRemoveCardClick, this);
			this.mon(this.view, ev.cloneCardButtonClick, this.onCloneCardClick, this);
			this.mon(this.view, ev.printCardButtonClick, this.onPrintCardMenuClick, this);
			this.mon(this.view, ev.openGraphButtonClick, this.onShowGraphClick, this);
		},

		onEntryTypeSelected: function() {
			this.cloneCard = false;
			this.callParent(arguments);
		},

		onCardSelected: function() {
			this.cloneCard = false;
			this.callParent(arguments);
		},

		onRemoveCardClick: function() {
			var me = this,
				idCard = me.card.get("Id"),
				idClass = me.entryType.get("id");

			function makeRequest(btn) {
				if (btn != 'yes') {
					return;
				}

				CMDBuild.core.LoadMask.show();
				CMDBuild.proxy.Card.remove({
					params : {
						IdClass: idClass,
						Id: idCard
					},
					loadMask: false,
					success : function() {
						me.fireEvent(me.CMEVENTS.cardRemoved, idCard, idClass);
					},
					callback : function() {
						CMDBuild.core.LoadMask.hide();
					}
				});
			};

			Ext.Msg.confirm(
				CMDBuild.Translation.attention,
				CMDBuild.Translation.management.modcard.delete_card_confirm,
				makeRequest,
				this
			);
		},

		onCloneCardClick: function() {
			this.cloneCard = true;
			this.onModifyCardClick();
			this.fireEvent(this.CMEVENTS.cloneCard);
		},

		changeClassUIConfigurationForGroup: function(disabledModify, disabledClone, disabledRemove) {
			this.view.form.modifyCardButton.disabledForGroup = disabledModify;
			this.view.form.cloneCardButton.disabledForGroup = disabledClone;
			this.view.form.deleteCardButton.disabledForGroup = disabledRemove;
			if (this.view.form.modifyCardButton.disabledForGroup)
				this.view.form.modifyCardButton.disable();
			else
				this.view.form.modifyCardButton.enable();
			if (this.view.form.cloneCardButton.disabledForGroup)
				this.view.form.cloneCardButton.disable();
			else
				this.view.form.cloneCardButton.enable();
			if (this.view.form.deleteCardButton.disabledForGroup)
				this.view.form.deleteCardButton.disable();
			else
				this.view.form.deleteCardButton.enable();
		},

		onModifyCardClick: function() {
			var me = this;

			// If wanna clone the card skip the locking
			if (this.cloneCard && this.isEditable(this.card)) {
				me.loadCard(true, null, function() { // Force card load before entering in edit mode
					me.view.editMode();
				});
			} else {
				this.callParent(arguments);
			}
		},

		onAbortCardClick: function() {
			if (this.cloneCard) {
				// Set the current card to null
				// like if wanna add a new card
				// Than is possible select again
				// the card that you are try to clone
				_CMCardModuleState.setCard(null);
			} else {
				this.callParent(arguments);
			}

			_CMUIState.onlyGridIfFullScreen();
		},

		onSaveSuccess: function() {
			this.cloneCard = false;
			this.callParent(arguments);
			_CMUIState.onlyGridIfFullScreen();
		},

		/**
		 * @param {String} format
		 */
		onPrintCardMenuClick: function(format) {
			if (!Ext.isEmpty(format)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.entryType.get(CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.CARD_ID] = this.card.get(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.FORMAT] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					parentDelegate: this,
					format: format,
					mode: 'cardDetails',
					parameters: params
				});
			}
		}
	});

})();