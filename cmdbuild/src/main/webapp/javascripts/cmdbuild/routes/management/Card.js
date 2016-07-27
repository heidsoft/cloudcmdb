(function() {

	Ext.define('CMDBuild.routes.management.Card', {
		extend: 'CMDBuild.routes.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.Card'
		],

		/**
		 * @cfg {String}
		 */
		cardIdentifier: undefined,

		/**
		 * @cfg {Char}
		 */
		cardIdentifierSplitter: '~',

		/**
		 * @cfg {String}
		 */
		classIdentifier: undefined,

		/**
		 * @cfg {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * @cfg {Array}
		 */
		supportedPrintFormats: [
			CMDBuild.core.constants.Proxy.PDF,
			CMDBuild.core.constants.Proxy.ODT
		],

		/**
		 * @param {Object} params - url parameters
		 * @param {String} params.classIdentifier - class name
		 * @param {Int} params.cardIdentifier - card id
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @return  {Boolean}
		 */
		detail: function(params, path, router) {
			if (this.paramsValidation(params)) {
				this.entryType = _CMCache.getEntryTypeByName(this.classIdentifier);

				var splittedIdentifier = this.cardIdentifier.split(this.cardIdentifierSplitter);

				if (!isNaN(parseInt(this.cardIdentifier))) { // Single card selection
					Ext.Function.createDelayed(function() {
						CMDBuild.global.controller.MainViewport.cmfg('mainViewportCardSelect', {
							Id: this.cardIdentifier,
							IdClass: this.entryType.get(CMDBuild.core.constants.Proxy.ID)
						});
					}, 500, this)();
				} else if (
					this.cardIdentifier.indexOf(this.cardIdentifierSplitter) >= 0
					&& (splittedIdentifier.length == 2)
					&& (splittedIdentifier[0].length > 0)
					&& (splittedIdentifier[1].length > 0)
				) { // SimpleFilter
					this.simpleFilter(splittedIdentifier);
				} else {
					CMDBuild.core.Message.error(
						CMDBuild.Translation.common.failure,
						CMDBuild.Translation.errors.routesInvalidCardIdentifier + ' (' + this.cardIdentifier + ')',
						false
					);
				}
			}
		},

		/**
		 * @param {Object} params
		 *
		 * @return  {Boolean}
		 */
		paramsValidation: function(params) {
			this.cardIdentifier = params[CMDBuild.core.constants.Proxy.CARD_IDENTIFIER];
			this.classIdentifier = params[CMDBuild.core.constants.Proxy.CLASS_IDENTIFIER];
			this.clientFilterString = params[CMDBuild.core.constants.Proxy.CLIENT_FILTER];
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

			// Card identifier validation
			if (Ext.isEmpty(this.cardIdentifier)) {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.routesInvalidCardIdentifier + ' (' + this.cardIdentifier + ')',
					false
				);

				return false;
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
		 *
		 * @override
		 */
		print: function(params, path, router) {
			this.detail(params, path, router);

			Ext.Function.createDelayed(function() {
				CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', 'class').cardPanelController.onPrintCardMenuClick(this.printFormat);
			}, 1500, this)();
		},

		/**
		 * @params {Array} splittedIdentifier - ['cardParam', 'value']
		 */
		simpleFilter: function(splittedIdentifier) {
			CMDBuild.proxy.Card.readAll({
				params: {
					className: this.classIdentifier,
					filter: '{"attribute":{"simple":{"attribute":"' + splittedIdentifier[0] + '","operator":"equal","value":["' + splittedIdentifier[1] + '"]}}}'
				},
				loadMask: false,
				scope: this,
				success: function(result, options, decodedResult) {
					if (decodedResult.results == 1) {
						Ext.Router.parse('exec/classes/' + this.classIdentifier + '/cards/' + decodedResult.rows[0]['Id']);
					} else {
						CMDBuild.core.Message.error(
							CMDBuild.Translation.common.failure,
							CMDBuild.Translation.errors.routesInvalidSimpleFilter,
							false
						);
					}
				}
			});
		}
	});

})();