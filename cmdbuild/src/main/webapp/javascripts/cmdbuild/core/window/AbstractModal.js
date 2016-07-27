(function () {

	/**
	 * Reads the size in percentage in the configuration file and create a modal popup-window
	 *
	 * @deprecated (CMDBuild.core.window.AbstractCustomModal)
	 * @abstract
	 */
	Ext.define('CMDBuild.core.window.AbstractModal', {
		extend: 'Ext.window.Window',

		/**
		 * @cfg {Boolean}
		 */
		autoHeight: false,

		/**
		 * @cfg {Boolean}
		 */
		autoWidth: false,

		/**
		 * @cfg {Number}
		 */
		defaultSize: 0.80,

		buttonAlign: 'center',
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
			if (!this.autoHeight) {
				var percentualHeight;
				var configHeight = CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.POPUP_HEIGHT_PERCENTAGE);

				if (configHeight) {
					percentualHeight = configHeight/100;
				} else {
					percentualHeight = this.defaultSize;
				}

				this.height = Ext.getBody().getHeight() * percentualHeight;
			}

			if (!this.autoWidth) {
				var percentualWidth;
				var configWidth = CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.POPUP_WIDTH_PERCENTAGE);

				if (configWidth) {
					percentualWidth = configWidth/100;
				} else {
					percentualWidth = this.defaultSize;
				}

				this.width = Ext.getBody().getWidth() * percentualWidth;
			} else {
				this.width = 660; // Default width setup based on text field default width inside window
			}

			this.callParent(arguments);
		}
	});

})();
