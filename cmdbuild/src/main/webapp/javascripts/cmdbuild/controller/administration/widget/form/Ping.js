(function () {

	Ext.define('CMDBuild.controller.administration.widget.form.Ping', {
		extend: 'CMDBuild.controller.administration.widget.form.Abstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.widget.ping.Definition'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.Widget}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'classTabWidgetAdd',
			'classTabWidgetDefinitionModelNameGet',
			'classTabWidgetPingDefinitionGet = classTabWidgetDefinitionGet',
			'classTabWidgetPingLoadRecord = classTabWidgetLoadRecord'
		],

		/**
		 * @cfg {String}
		 *
		 * @private
		 */
		definitionModelName: 'CMDBuild.model.widget.ping.Definition',

		/**
		 * @cfg {CMDBuild.view.administration.widget.form.PingPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.widget.Widget} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.widget.form.PingPanel', { delegate: this });
		},

		/**
		 * @return {Object} widgetDefinition
		 */
		classTabWidgetPingDefinitionGet: function () {
			var widgetDefinition = CMDBuild.model.widget.ping.Definition.convertToLegacy(
				Ext.create(this.classTabWidgetDefinitionModelNameGet(), this.view.getData(true)).getData()
			);
			widgetDefinition[CMDBuild.core.constants.Proxy.TEMPLATES] = this.view.presetGrid.getData(CMDBuild.core.constants.Proxy.DATA);

			return widgetDefinition;
		},

		/**
		 * Fills form with widget data
		 *
		 * @param {CMDBuild.model.widget.ping.Definition} record
		 */
		classTabWidgetPingLoadRecord: function (record) {
			this.view.loadRecord(record);
			this.view.presetGrid.setData(record.get(CMDBuild.core.constants.Proxy.TEMPLATES));
		}
	});

})();
