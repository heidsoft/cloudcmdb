(function() {

	Ext.require([
		'CMDBuild.core.constants.Global',
		'CMDBuild.core.Message'
	]);

	Ext.define('CMDBuild.controller.management.common.widgets.linkCards.cardWindow.CMCardWindowController', {
		extend: 'CMDBuild.controller.management.common.widgets.linkCards.cardWindow.CMBaseCardPanelController',

		requires: ['CMDBuild.controller.management.classes.StaticsController'],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		/**
		 * @param {CMDBuild.view.management.common.CMCardWindow} view
		 * @param {Object} configuration
		 * 	{
		 * 		{Int} entryType - classTypeId
		 * 		{Int} card - cardId
		 * 		{Boolean} cmEditMode
		 * 	}
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			Ext.apply(this, configurationObject); // Apply configurations

			if (!Ext.isEmpty(this.configuration.entryType)) {
				var me = this;

				this.callParent([this.view, this.configuration]);

				this.mixins.observable.constructor.call(this, arguments);

				this.onEntryTypeSelected(_CMCache.getEntryTypeById(this.configuration.entryType));
				this.cmEditMode = this.configuration.cmEditMode;

				this.mon(this.view, 'show', function() {
					this.loadFields(this.configuration.entryType, function() {
						if (me.configuration.card) {
							var params = {};
							params[CMDBuild.core.constants.Proxy.CARD_ID] = me.configuration.card;
							params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.configuration.entryType);

							me.loadCard(true, params, function(card) {
								me.onCardLoaded(me, card);
							});
						} else {
							me.editModeIfPossible();
						}
					});
				}, this);

				this.mon(this.view, 'destroy', function() {
					this.unlockCard();
				}, this);
			}
		},

		/**
		 * @return {Ext.form.Basic}
		 */
		getForm: function() {
			return this.view.cardPanel.getForm();
		},

		/**
		 * @override
		 */
		onSaveCardClick: function() {
			var form = this.getForm();
			var params = this.buildSaveParams();

			this.beforeRequest(form);

			// Check form fields validity
			if (form.isValid()) {
				this.doFormSubmit(params);
			} else {
				CMDBuild.core.Message.error(
					null,
					Ext.String.format(
						'<p class="{0}">{1}</p>',
						CMDBuild.core.constants.Global.getErrorMsgCss(), CMDBuild.Translation.errors.invalid_attributes
					) + CMDBuild.controller.management.classes.StaticsController.getInvalidAttributeAsHTML(form),
					false
				);
			}
		},

		/**
		 * @override
		 */
		onAbortCardClick: function() {
			this.view.destroy();
		},

		/**
		 * @param {Object} entryType
		 *
		 * @override
		 */
		onEntryTypeSelected: function(entryType) {
			this.callParent(arguments);

			this.view.setTitle(this.entryType.get(CMDBuild.core.constants.Proxy.TEXT));
		},

		/**
		 * @return {Object} params
		 *
		 * @protected
		 */
		buildSaveParams: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.entryType.getName();
			params[CMDBuild.core.constants.Proxy.CARD_ID] = this.card ? this.card.get('Id') : -1;

			return params;
		},

		/**
		 * @param {Ext.form.Basic} form
		 * @param {Ext.form.action.Submit} action
		 *
		 * @protected
		 * @override
		 */
		onSaveSuccess: function(form, action) {
			CMDBuild.core.LoadMask.hide();

			_CMCache.onClassContentChanged(this.entryType.get(CMDBuild.core.constants.Proxy.ID));

			this.view.destroy();
		},

		/**
		 * @param {Object} me
		 * @param {Object} card
		 */
		onCardLoaded: function(me, card) {
			me.card = card;
			me.view.loadCard(card);

			if (me.widgetControllerManager)
				me.widgetControllerManager.buildControllers(card);

			me.editModeIfPossible();
		},

		/**
		 * Template to override in subclass
		 */
		beforeRequest: Ext.emptyFn,

		editModeIfPossible: function() {
			var me = this;

			if (!me.card) { // Here add a new card, so there is nothing to lock
				me.onAddCardButtonClick(this.configuration.entryType);
			} else if (me.cmEditMode) {
				me.lockCard(function() {
					me.view.editMode();
				});
			} else {
				me.view.displayMode();
			}
		}
	});

})();