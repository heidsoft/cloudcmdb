(function () {

	/**
	 * @abstract
	 */
	Ext.define('CMDBuild.core.window.AbstractCustomModal', {
		extend: 'Ext.window.Window',

		/**
		 * @cfg {Object}
		 */
		dimensions: {},

		/**
		 * @cfg {String} ['absolute' || 'none' || 'percentage']
		 */
		dimensionsMode: 'none',

		constrain: true,
		layout: 'fit',
		modal: true,
		resizable: true,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			switch (this.dimensionsMode) {
				case 'absolute': {
					if (Ext.Object.isEmpty(this.dimensions)) // Apply defaults
						Ext.apply(this, {
							dimensions: {
								height: 600,
								width: 800
							}
						});

					Ext.apply(this, this.dimensions);
				} break;

				case 'percentage': {
					if (Ext.Object.isEmpty(this.dimensions)) // Apply defaults
						Ext.apply(this, {
							dimensions: {
								height: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.POPUP_HEIGHT_PERCENTAGE),
								width: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.POPUP_WIDTH_PERCENTAGE)
							}
						});

					Ext.apply(this, {
						height: Ext.getBody().getHeight() * (this.dimensions.height / 100),
						width: Ext.getBody().getWidth() * (this.dimensions.width / 100)
					});
				} break;

				case 'none':
				default: {}
			}

			this.callParent(arguments);
		}
	});

})();
