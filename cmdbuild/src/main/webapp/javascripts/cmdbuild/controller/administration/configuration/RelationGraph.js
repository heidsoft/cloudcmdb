(function () {

	Ext.define('CMDBuild.controller.administration.configuration.RelationGraph', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.configuration.RelationGraph'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Configuration}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onConfigurationRelationGraphSaveButtonClick',
			'onConfigurationRelationGraphTabShow = onConfigurationRelationGraphAbortButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.RelationGraphPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.configuration.RelationGraphPanel', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationRelationGraphSaveButtonClick: function () {
			CMDBuild.proxy.configuration.RelationGraph.update({
				params: Ext.create('CMDBuild.model.configuration.RelationGraph', this.view.getData(true)).getData(),
				scope: this,
				success: function (response, options, decodedResponse) {
					this.cmfg('onConfigurationRelationGraphTabShow');

					CMDBuild.core.Message.success();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationRelationGraphTabShow: function () {
			CMDBuild.proxy.configuration.RelationGraph.read({
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					if (!Ext.isEmpty(decodedResponse)) {
						this.view.loadRecord(Ext.create('CMDBuild.model.configuration.RelationGraph', decodedResponse));

						Ext.create('CMDBuild.core.configurations.builder.RelationGraph'); // Rebuild configuration model
					}
				}
			});
		}
	});

})();
