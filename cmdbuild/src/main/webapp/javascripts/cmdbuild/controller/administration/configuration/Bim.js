(function () {

	Ext.define('CMDBuild.controller.administration.configuration.Bim', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.configuration.Bim'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationBimSaveButtonClick',
			'onConfigurationBimTabShow = onConfigurationBimAbortButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.BimPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.administration.configuration.Configuration} configObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.BimPanel', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationBimSaveButtonClick: function () {
			CMDBuild.proxy.configuration.Bim.update({
				params: this.view.getData(true),
				scope: this,
				success: function (response, options, decodedResponse) {
					this.cmfg('onConfigurationBimTabShow');

					CMDBuild.core.Message.success();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationBimTabShow: function () {
			CMDBuild.proxy.configuration.Bim.read({
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					if (!Ext.isEmpty(decodedResponse)) {
						this.view.loadRecord(Ext.create('CMDBuild.model.configuration.Bim', decodedResponse));

						Ext.create('CMDBuild.core.configurations.builder.Bim', { // Rebuild configuration model
							scope: this,
							callback: function (options, success, response) {
								this.cmfg('mainViewportAccordionSetDisabled', {
									identifier: 'bim',
									state: !CMDBuild.configuration.bim.get(CMDBuild.core.constants.Proxy.ENABLED)
								});
							}
						});
					}
				}
			});
		}
	});

})();
