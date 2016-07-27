(function () {

	Ext.define('CMDBuild.view.administration.widget.form.PingPanel', {
		extend: 'CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionPanel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.widget.ping.PresetGrid'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.form.OpenReport}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.grid.KeyValue}
		 */
		presetGrid: undefined,

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		widgetDefinitionFormAdditionalPropertiesGet: function () {
			return [
				Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.additionalProperties,
					flex: 1,

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [
						Ext.create('Ext.form.field.Text', {
							name: CMDBuild.core.constants.Proxy.ADDRESS,
							fieldLabel: CMDBuild.Translation.addressToPing,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
						}),
						Ext.create('Ext.form.field.Number', {
							name: CMDBuild.core.constants.Proxy.COUNT,
							fieldLabel: CMDBuild.Translation.numberOfPings,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
							maxValue: 10,
							minValue: 1
						}),
						this.presetGrid = Ext.create('CMDBuild.view.common.field.grid.KeyValue', {
							enableRowAdd: true,
							enableRowDelete: true,
							enableCellEditing: true,
							keyAttributeName: CMDBuild.core.constants.Proxy.NAME,
							keyLabel: CMDBuild.Translation.attribute,
							margin: '8 0 9 0',
							modelName: 'CMDBuild.model.widget.ping.PresetGrid',
							title: CMDBuild.Translation.templates
						})
					]
				})
			];
		}
	});

})();
