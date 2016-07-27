(function () {

	Ext.define('CMDBuild.controller.administration.configuration.Gis', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.configuration.Gis',
			'CMDBuild.model.configuration.Gis'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationGisSaveButtonClick',
			'onConfigurationGisTabShow = onConfigurationGisAbortButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.GisPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.configuration.Configuration} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.GisPanel', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationGisSaveButtonClick: function () {
			CMDBuild.proxy.configuration.Gis.update({
				params: CMDBuild.model.configuration.Gis.convertToLegacy(this.view.getData(true)),
				scope: this,
				callback: function (options, success, response) {
					this.cmfg('onConfigurationGisTabShow');
				},
				success: function (response, options, decodedResponse) {
					CMDBuild.core.Message.success();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationGisTabShow: function () {
			CMDBuild.proxy.configuration.Gis.read({
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					if (!Ext.isEmpty(decodedResponse)) {
						this.view.loadRecord(Ext.create('CMDBuild.model.configuration.Gis', CMDBuild.model.configuration.Gis.convertFromLegacy(decodedResponse)));

						Ext.create('CMDBuild.core.configurations.builder.Gis', { // Rebuild configuration model
							scope: this,
							callback: function (options, success, response) {
								this.cmfg('mainViewportAccordionSetDisabled', {
									identifier: 'gis',
									state: !CMDBuild.configuration.gis.get(CMDBuild.core.constants.Proxy.ENABLED)
								});
							}
						});
					}
				}
			});
		}
	});

})();
