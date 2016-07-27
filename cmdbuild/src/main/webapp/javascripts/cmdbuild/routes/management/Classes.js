(function() {

	Ext.define('CMDBuild.routes.management.Classes', {
		extend: 'CMDBuild.routes.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message'
		],

		/**
		 * @cfg {String}
		 */
		classIdentifier: undefined,

		/**
		 * @cfg {String}
		 */
		clientFilter: undefined,

		/**
		 * @cfg {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * @cfg {Array}
		 */
		supportedPrintFormats: [
			CMDBuild.core.constants.Proxy.PDF,
			CMDBuild.core.constants.Proxy.CSV
		],

		/**
		 * Apply clientFilter to grid
		 */
		applyClientFilter: function() {
			if (!Ext.isEmpty(this.clientFilter))
				Ext.Function.createDelayed(function() {
					this.entryType.set(CMDBuild.core.constants.Proxy.FILTER, this.clientFilter); // Inject filter in entryType object

					CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', 'class').onViewOnFront(this.entryType);
				}, 1500, this)();
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.classIden - class name
		 * @param {String} params.clientFilter - advanced filter object serialized
		 * @param {String} path
		 * @param {Object} router
		 */
		detail: function(params, path, router) {
			if (this.paramsValidation(params)) {
				this.entryType = _CMCache.getEntryTypeByName(this.classIdentifier);

				// Use runtime configuration to select class
				CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.STARTING_CLASS_ID, this.entryType.get(CMDBuild.core.constants.Proxy.ID));

				this.applyClientFilter();
			}
		},

		/**
		 * @param {Object} params
		 *
		 * @return  {Boolean}
		 */
		paramsValidation: function(params) {
			this.classIdentifier = params[CMDBuild.core.constants.Proxy.CLASS_IDENTIFIER];
			this.clientFilter = params[CMDBuild.core.constants.Proxy.CLIENT_FILTER];
			this.printFormat = params[CMDBuild.core.constants.Proxy.FORMAT] || CMDBuild.core.constants.Proxy.PDF;

			// Class identifier validation
			if (
				Ext.isEmpty(this.classIdentifier)
				|| !_CMCache.isEntryTypeByName(this.classIdentifier)
			) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidClassIdentifier + ' (' + this.classIdentifier + ')',
					false
				);

				return false;
			}

			// Client filter validation
			if (!Ext.isEmpty(this.clientFilter)) {
				// TODO: validate filter with server side call
			}

			// Print format validation
			if (!Ext.Array.contains(this.supportedPrintFormats, this.printFormat)) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidPrintFormat + ' (' + this.printFormat + ')',
					false
				);

				return false;
			}

			return true;
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.format
		 * @param {String} path
		 * @param {Object} router
		 */
		print: function(params, path, router) {
			this.detail(params, path, router);

			Ext.Function.createDelayed(function() {
				CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', 'class').gridController.onPrintGridMenuClick(this.printFormat);
			}, 500, this)();
		}
	});

})();