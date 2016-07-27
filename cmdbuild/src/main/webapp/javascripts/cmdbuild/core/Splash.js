(function () {

	Ext.define('CMDBuild.core.Splash', {

		singleton: true,

		/**
		 * @property {Ext.window.Window}
		 *
		 * @private
		 */
		instance: undefined,

		/**
		 * @param {Boolean} message
		 *
		 * @returns {Ext.window.Window}
		 */
		build: function (isAdministration) {
			isAdministration = Ext.isBoolean(isAdministration) ? isAdministration : false;

			if (Ext.isEmpty(CMDBuild.core.Splash.instance))
				CMDBuild.core.Splash.instance = Ext.create('Ext.window.Window', {
					closable: false,
					draggable: false,
					modal: true,
					renderTo: Ext.getBody(),
					resizable: false,

					contentEl: Ext.create('Ext.dom.Element', {
						html: '<div class="splash-screen' + (isAdministration ? '-administration' : '') + '">'
								+ '<div class="text-container">'
									+ '<div class="description">Open Source Configuration and Management Database</div>'
									+ '<span class="copyright">Copyright &copy; Tecnoteca srl</span>'
								+ '</div>'
								+ '<div class="version">' + CMDBuild.Translation.release + '</div>'
							+ '</div>'
					})
				});

			return CMDBuild.core.Splash.instance;
		},

		/**
		 * Shows the header and the footer, that are initially hidden
		 *
		 * @param {Function} callback
		 * @param {Object} scope
		 */
		hide: function (callback, scope) {
			callback = Ext.isFunction(callback) ? callback : Ext.emptyFn;

			CMDBuild.core.Splash.build().hide();

			// Remove class from HTML dom elements
			Ext.Array.forEach(Ext.DomQuery.select('div[class="display-none"]'), function (div, i, allDivs) {
				new Ext.Element(div).removeCls('display-none');
			}, this);

			Ext.callback(callback, scope, [], 500);
		},

		/**
		 * @param {Boolean} isAdministration
		 */
		show: function (isAdministration) {
			CMDBuild.core.Splash.build(isAdministration).show();
		}
	});

})();
