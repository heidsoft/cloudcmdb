(function() {

	Ext.require(['CMDBuild.proxy.Card']);

	Ext.define('CMDBuild.state.CMCardModuleStateDelegate', {
		/**
		 * @param {CMDBuild.state.CMCardModuleState} state The state that calls the delegate
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType The new entryType
		 * @param {Object} danglingCard, the configuration to open a card.
		 * @see CMDBuild.controller.management.common.CMCardGridController
		 */
		onEntryTypeDidChange: function(state, entryType, danglingCard, filter) {},

		/**
		 * @param {CMDBuild.state.CMCardModuleState} state The state that calls the delegate
		 * @param {object} card The data of the new selected card
		 */
		onCardDidChange: function(state, card) {}
	});

	Ext.define('CMDBuild.state.CMCardModuleState', {

		mixins: {
			delegable: 'CMDBuild.core.CMDelegable'
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this, 'CMDBuild.state.CMCardModuleStateDelegate');

			this.entryType = null;
			this.card = null;
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 * @param {Object} danglingCard
		 * @param {Object} filter
		 * @param {Boolean} enableDelegatesCall
		 *
		 * TODO manage dangling card
		 */
		setEntryType: function(entryType, danglingCard, filter, enableDelegatesCall) {
			enableDelegatesCall = (!Ext.isEmpty(enableDelegatesCall)) ? enableDelegatesCall : true;

			if (
				(entryType === this.entryType && this.filter)
				|| danglingCard
				|| filter
				|| this.entryType !== entryType
			) {
				this.entryType = entryType;
				this.filter = filter || null;

				this.setCard(null, null, enableDelegatesCall); // reset the stored card because it could not be of the new entry type

				if (enableDelegatesCall)
					this.callDelegates('onEntryTypeDidChange', [this, entryType, danglingCard, filter]);
			}
		},

		/**
		 * To check/get card datas and set complete card object
		 *
		 * @param {Object} card
		 * @param {Function} cb
		 * @param {Boolean} enableDelegatesCall
		 */
		setCard: function(card, cb, enableDelegatesCall) {
			enableDelegatesCall = (!Ext.isEmpty(enableDelegatesCall)) ? enableDelegatesCall : true;

			if (card != null && typeof card.data == 'undefined') {
				card = adaptGetCardCallParams(card);

				CMDBuild.proxy.Card.read({
					params: card,
					loadMask: false,
					scope: this,
					success: function(a, b, response) {
						var raw = response.card;

						if (raw) {
							var c = new CMDBuild.DummyModel(response.card);

							c.raw = raw;
							this.setCard(c, cb, enableDelegatesCall);
						}
					}
				});
			} else {
				this.card = card;

				if (enableDelegatesCall)
					this.callDelegates('onCardDidChange', [this, card]);

				if (typeof cb == 'function')
					cb(card);
			}
		}

	});

	// Define a global variable
	_CMCardModuleState = new CMDBuild.state.CMCardModuleState();

	function adaptGetCardCallParams(p) {
		if (p.Id && p.IdClass) {
			_deprecated('adaptGetCardCallParams', this);

			var parameters = {};
			parameters[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(p.IdClass);
			parameters[CMDBuild.core.constants.Proxy.CARD_ID] = p.Id;

			p = parameters;
		}

		return p;
	}

})();