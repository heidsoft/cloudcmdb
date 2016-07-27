(function() {

	Ext.require([
		'CMDBuild.proxy.Card',
		'CMDBuild.proxy.classes.tabs.Note'
	]);

	Ext.define("CMDBuild.controller.management.classes.CMNoteController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",

		constructor: function(view, supercontroller) {

			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.CMEVENTS = {
				noteWasSaved: "cm-note-saved"
			};

			this.mon(this.view, this.view.CMEVENTS.modifyNoteButtonClick, this.onModifyNoteClick, this);
			this.mon(this.view, this.view.CMEVENTS.saveNoteButtonClick, this.onSaveNoteClick, this);
			this.mon(this.view, this.view.CMEVENTS.cancelNoteButtonClick, this.onCancelNoteClick, this);

			this.addEvents(this.addEvents.noteWasSaved);
		},

		onEntryTypeSelected: function() {
			this.unlockCard();
			this.callParent(arguments);
			this.view.disable();
		},

		onCardSelected: function(card) {
			this.unlockCard();
			this.callParent(arguments);
			this.updateView(card);

			if (this.disableTheTabBeforeCardSelection(card)) {
				this.view.disable();
			} else {
				this.view.enable();
				this.view.loadCard(card);
			}
		},

		disableTheTabBeforeCardSelection: function(card) {
			return !card || CMDBuild.Utils.isSimpleTable(card.get("IdClass"));
		},

		updateView: function(card) {
			this.updateViewPrivilegesForCard(card);
			this.view.reset();
			this.view.disableModify();
		},

		updateViewPrivilegesForCard: function(card) {
			var privileges = _CMUtils.getEntryTypePrivilegesByCard(card);
			this.view.updateWritePrivileges(privileges.write && ! privileges.crudDisabled.modify);
		},

		/**
		 * @returns {Void}
		 */
		onSaveNoteClick: function () {
			var params = this._getSaveParams();

			if (this.view.getForm().isValid() && this.beforeSave(this.card)) {
				CMDBuild.proxy.classes.tabs.Note.update({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.view.disableModify(enableToolbar = true);

						var val = this.view.syncForms();
						this.syncSavedNoteWithModel(this.card, val);
						this.fireEvent(this.CMEVENTS.noteWasSaved, this.card);
					}
				});
			}
		},

		onCancelNoteClick: function() {
			this.onCardSelected(this.card);
			this.view.disableModify(couldModify = isEditable(this.card));
		},

		onModifyNoteClick: function() {
			if (isEditable(this.card)) {
				var me = this;

				this.lockCard(function() {
					me.view.enableModify();
				});
			}
		},

		// called before the save request
		// override in subclass, return false to avoid the save
		beforeSave: function(card) {
			return true;
		},

		_getSaveParams: function() {
			var params = {};
			params['Notes'] = this.view.getForm().getValues()['Notes'];

			if (this.card) {
				params[CMDBuild.core.constants.Proxy.CARD_ID] = this.card.get("Id");
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.card.get("IdClass"));
			}

			return params;
		},

		syncSavedNoteWithModel: function(card, val) {
			card.set("Notes", val);
			card.commit();
			if (card.raw) {
				card.raw["Notes"] = val;
			}
		},

		lockCard: function(success) {
			if (CMDBuild.configuration.instance.get('enableCardLock')) { // TODO: use proxy constants
				if (this.card) {
					var id = this.card.get("Id");
					CMDBuild.proxy.Card.lock({
						params: {
							id: id
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
				if (this.card && this.view.isInEditing()) {
					var id = this.card.get("Id");
					CMDBuild.proxy.Card.unlock({
						params: {
							id: id
						},
						loadMask: false
					});
				}
			}
		}
	});

	function isEditable(card) {
		return _CMUtils.getEntryTypePrivilegesByCard(card).write;
	}

	Ext.define("CMDBuild.view.management.common.CMNoteWindowController", {
		extend: "CMDBuild.controller.management.classes.CMNoteController",
		constructor: function() {
			this.callParent(arguments);
		},

		onCardSelected: function(card) {
			this.callParent(arguments);
			var title = "";

			if (this.card) {
				title = Ext.String.format("{0} - {1}"
					, CMDBuild.Translation.management.modcard.tabs.notes
					, this.card.get("Description"));
			}

			this.view.setTitle(title);
		}
	});

})();