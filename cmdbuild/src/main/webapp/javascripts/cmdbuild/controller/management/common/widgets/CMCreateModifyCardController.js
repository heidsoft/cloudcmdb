(function() {

	Ext.define("CMDBuild.controller.management.common.widgets.CMCreateModifyCardController", {
		extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",

		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		constructor: function(view, supercontroller, widget, clientForm, card) {
			var widgetControllerManager = new CMDBuild.controller.management.common.CMWidgetManagerController(view.getWidgetManager());
			this.callParent([view, supercontroller, widgetControllerManager]);

			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.templateResolverIsBusy = false;
			this.idClassToAdd = undefined;
			this.savedCardId = undefined;
			this.clientForm = clientForm;

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: widget,
				serverVars: this.getTemplateResolverServerVars()
			});

			this.mon(this.view.addCardButton, "cmClick", this.onAddCardClick, this);
		},

		// override
		onSaveSuccess: function(form, operation) {
			this.callParent(arguments);
			this.savedCardId = operation.result.id || this.cardId;

			if (typeof this.superController.hideWidgetsContainer == "function") {
				this.superController.hideWidgetsContainer();
				updateLocalDepsIfReferenceToModifiedClass(this);
			}
		},

		// override
		loadCardStandardCallBack: function(card) {
			var me = this;
			this.card = card;

			this.loadFields(card.get("IdClass"), function() {
				me.view.loadCard(card, bothpanel = true);
				if (me.isEditable(card)) {
					me.view.editMode();
				}
			});
		},

		getCQLOfTheCardId: function() {
			return this.widgetConf.idcardcqlselector;
		},

		isWidgetEditable: function(controller) {
			return !this.widgetConf.readonly;
		},

		/**
		 * Executed before view activation, loads fields and sets the cardId variable value
		 *
		 * @override
		 */
		beforeActiveView: function() {
			var me = this;
			this.card = null;
			this.targetClassName = this.widgetConf.targetClass;
			this.entryType = _CMCache.getEntryTypeByName(this.targetClassName);

			// Deferred function to avoid bug that won't fill window form first time that window is displayed
			Ext.defer(function() {
				if (this.entryType != null) {
					this.view.initWidget(this.entryType, this.isWidgetEditable());

					this.templateResolver.resolveTemplates({
						attributes: ['idcardcqlselector'],
						callback: function(out, ctx) {
							me.cardId = normalizeIdCard(out['idcardcqlselector']);

							if (me.cardId == null && me.entryType.isSuperClass()) {
								// could not add a card for a superclass
							} else {
								me.loadAndFillFields();
							}
						}
					});
				}
			}, 10, this);
		},

		/**
		 * @override
		 */
		beforeHideView: function() {
			this.unlockCard();
		},

		// override
		getData: function() {
			var out = null;
			if (this.savedCardId) {
				out = {};
				out["output"] = this.savedCardId;
			}

			return out;
		},

		// override
		isEditable: function() {
			return this.callParent(arguments) && this.isWidgetEditable();
		},

		/*
		 * Does not need to listen the
		 * cardModule state events
		 */
		// override
		buildCardModuleStateDelegate: function() {},

		loadAndFillFields: function(classId) {
			classId = classId || this.entryType.getId();

			var me = this;
			var isANewCard = this.cardId == null || this.cardId == 0;

			if (isANewCard) {
				/*
				 * presets is a map like this:
				 * {
				 * 		nameOfActivityAttribute: nameOfCardAttribute,
				 * 		nameOfActivityAttribute: nameOfCardAttribute,
				 * 		...
				 * }
				 */
				var presets = this.widgetConf.attributeMappingForCreation || {};
				var fields = this.clientForm.getFields();

				var values = {
					Id: -1, // to have a new card
					IdClass: classId
				}

				fields.each(function(field) {
					if (field._belongToEditableSubpanel
							&& presets[field.name]) {

						var cardAttributeName = presets[field.name];
						var cardAttributePresetValue = field.getValue();
						if (typeof cardAttributePresetValue != "undefined") {
							values[cardAttributeName] = cardAttributePresetValue;
						}
					}
				});

				this.card = new CMDBuild.DummyModel(values);
				this.loadCard();
			} else {
				this.card = new CMDBuild.DummyModel({
					Id: this.cardId
				});

				this.lockCard(function() {
					me.loadCard(true, {
						cardId: me.cardId,
						className: _CMCache.getEntryTypeNameById(classId)
					});
				});
			}
		},

		onAddCardClick: function() {
			this.cardId = null;

			this.loadAndFillFields(this.entryType.getId());
		}
	});

	/**
	 * Parse idCard from input string witch derivates from templateResolver's idcardcqlselector
	 *
	 * @param (string) idCard
	 *
	 * @return (mixed) idCard - null or cardId parsed from input
	 */
	function normalizeIdCard(idCard) {
		if (typeof idCard == 'string') {
			idCard = parseInt(idCard.replace( /^\D+/g, ''));

			if (!isNaN(idCard))
				return idCard;
		}

		return null;
	}

	function updateLocalDepsIfReferenceToModifiedClass(me) {
		// we will synch the id of the modifyed
		// card with the reference that points to it
		// This is allowed only if the CQL used to get the id
		// of the card to modify is a simple pointer to a form field,
		// es {client:field_name}

		var referenceRX = /^\{client:(\w+)\}$/;
		var cql = me.getCQLOfTheCardId();
		var match = referenceRX.exec(cql);
		if (match != null) {
			var referenceName = match[1];
			if (referenceName) {
				var field = getFieldByName(me, referenceName);
				if (field &&
					field.CMAttribute) {

					field.store.load({
						callback: function() {
							field.setValue(me.savedCardId);
						}
					});
				}
			}
		}
	}

	function getFieldByName(me, name) {
		return me.clientForm.getFields().findBy(
			function findCriteria(f) {
				if (!f.CMAttribute) {
					return false;
				} else {
					return f.CMAttribute.name == name;
				}
			}
		);
	}

})();