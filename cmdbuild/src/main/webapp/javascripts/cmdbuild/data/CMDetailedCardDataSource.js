(function() {

	Ext.require(['CMDBuild.proxy.Card']);

	Ext.define("CMDBuild.data.CMDetailedCardDataSource", {

		extend: "CMDBuild.data.CMMiniCardGridBaseDataSource",

		constructor: function() {
			this.callParent(arguments);

			this.store = new Ext.data.Store ({
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
				model: 'CMDBuild.view.management.CMMiniCardGridModel',
				autoLoad: false
			});
		},

		clearStore: function() {
			this.store.removeAll();
		},

		/**
		 *
		 * @param {object} cardToLoad Object with an Id and an IdClass
		 * attribute. Use it to load the full attributes of the card
		 */
		loadCard: function(cardToLoad) {
			cardToLoad = adaptGetCardCallParams(cardToLoad);

			CMDBuild.proxy.Card.read({
				params: cardToLoad,
				loadMask: false,
				scope: this,
				success: function(a,b, response) {
					var raw = response.card;
					var attributes = response.attributes;

					var r = new CMDBuild.view.management.CMMiniCardGridModel({
						Id: raw.Id,
						IdClass: raw.IdClass,
						Code: raw.Code,
						Description: raw.Description,
						Details: raw,
						Attributes: attributes,
						ClassName: raw.IdClass_value
					});

					this.store.add(r);
				}
			});
		},

		// override
		getLastEntryTypeIdLoaded: function() {
			return null;
		},

		// override
		loadStoreForEntryTypeId: function(entryTypeId, cb) {}
	});

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