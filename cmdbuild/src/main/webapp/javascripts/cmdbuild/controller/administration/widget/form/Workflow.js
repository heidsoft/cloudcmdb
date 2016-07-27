(function () {

	Ext.define('CMDBuild.controller.administration.widget.form.Workflow', {
		extend: 'CMDBuild.controller.administration.widget.form.Abstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.Workflow',
			'CMDBuild.model.widget.workflow.Definition'
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
			'classTabWidgetWorkflowDefinitionGet = classTabWidgetDefinitionGet',
			'classTabWidgetWorkflowLoadRecord = classTabWidgetLoadRecord',
			'onClassTabWidgetWorkflowFilterTypeChange',
			'onClassTabWidgetWorkflowSelectedWorkflowChange'
		],

		/**
		 * @cfg {String}
		 *
		 * @private
		 */
		definitionModelName: 'CMDBuild.model.widget.workflow.Definition',

		/**
		 * @cfg {CMDBuild.view.administration.widget.form.WorkflowPanel}
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

			this.view = Ext.create('CMDBuild.view.administration.widget.form.WorkflowPanel', { delegate: this });
		},

		/**
		 * @return {Object} widgetDefinition
		 */
		classTabWidgetWorkflowDefinitionGet: function () {
			var widgetDefinition = CMDBuild.model.widget.workflow.Definition.convertToLegacy(
				Ext.create(this.classTabWidgetDefinitionModelNameGet(), this.view.getData(true)).getData()
			);

			switch (widgetDefinition[CMDBuild.core.constants.Proxy.FILTER_TYPE]) {
				case CMDBuild.core.constants.Proxy.NAME: {
					widgetDefinition[CMDBuild.core.constants.Proxy.PRESET] = this.view.presetGrid.getData(CMDBuild.core.constants.Proxy.DATA);
				}
			}

			return widgetDefinition;
		},

		/**
		 * Fills form with widget data
		 *
		 * @param {CMDBuild.model.widget.workflow.Definition} record
		 */
		classTabWidgetWorkflowLoadRecord: function (record) {
			this.view.loadRecord(record);

			switch (record.get(CMDBuild.core.constants.Proxy.FILTER_TYPE)) {
				case CMDBuild.core.constants.Proxy.CQL: {
					this.view.filter.setValue(record.get(CMDBuild.core.constants.Proxy.FILTER));
				} break;

				case CMDBuild.core.constants.Proxy.NAME: {
					this.view.workflow.setValue(record.get(CMDBuild.core.constants.Proxy.WORKFLOW_NAME));
					this.view.presetGrid.setData(record.get(CMDBuild.core.constants.Proxy.PRESET));
				}
			}
		},

		/**
		 * @param {String} selectedType
		 */
		onClassTabWidgetWorkflowFilterTypeChange: function (selectedType) {
			switch (selectedType) {
				case CMDBuild.core.constants.Proxy.CQL: {
					this.view.additionalProperties.removeAll();
					this.view.additionalProperties.add(this.view.widgetDefinitionFormAdditionalPropertiesByCqlGet());
				} break;

				case CMDBuild.core.constants.Proxy.NAME: {
					this.view.additionalProperties.removeAll();
					this.view.additionalProperties.add(this.view.widgetDefinitionFormAdditionalPropertiesByNameGet());
				}
			}
		},

		/**
		 * @param {CMDBuild.model.widget.workflow.TargetWorkflow} selectedRecord
		 */
		onClassTabWidgetWorkflowSelectedWorkflowChange: function (selectedRecord) {
			if (!Ext.Object.isEmpty(selectedRecord)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_ID] = selectedRecord.get(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.widget.Workflow.readStartActivity({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE][CMDBuild.core.constants.Proxy.VARIABLES];

						var data = {};

						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse))
							Ext.Array.forEach(decodedResponse, function (valueObject, i, allValueObjects) {
								if (!Ext.Object.isEmpty(valueObject))
									data[valueObject[CMDBuild.core.constants.Proxy.NAME]] = '';
							}, this);

						this.view.presetGrid.setData(data);
					}
				});
			}
		}
	});

})();
