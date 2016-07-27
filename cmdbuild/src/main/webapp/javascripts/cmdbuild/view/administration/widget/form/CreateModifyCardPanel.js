(function () {

	Ext.define('CMDBuild.view.administration.widget.form.CreateModifyCardPanel', {
		extend: 'CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionPanel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.CreateModifyCard'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.form.CreateModifyCard}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.TextArea}
		 */
		filter: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		targetClass: undefined,

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
						Ext.create('Ext.form.field.Checkbox', {
							name: CMDBuild.core.constants.Proxy.READ_ONLY,
							fieldLabel: CMDBuild.Translation.readOnly,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
						}),
						this.targetClass = Ext.create('Ext.form.field.ComboBox', {
							name: CMDBuild.core.constants.Proxy.TARGET_CLASS,
							fieldLabel: CMDBuild.Translation.targetClass,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							valueField: CMDBuild.core.constants.Proxy.NAME,
							displayField: CMDBuild.core.constants.Proxy.TEXT, // TODO: waiting for refactor (rename description)
							editable: false,
							forceSelection: true,

							store: CMDBuild.proxy.widget.CreateModifyCard.getStoreTargetClass(),
							queryMode: 'local'
						}),
						this.filter = Ext.create('Ext.form.field.TextArea', {
							name: CMDBuild.core.constants.Proxy.FILTER,
							fieldLabel: CMDBuild.Translation.cardCqlSelector,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
						})
					]
				})
			];
		}
	});

})();
