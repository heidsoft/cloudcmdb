(function () {

	Ext.define('CMDBuild.view.management.widget.openReport.OpenReportView', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.OpenReport'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.management.widget.openReport.OpenReport}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		formatCombo: undefined,

		/**
		 * @property {Ext.container.Container}
		 */
		fieldContainer: undefined,

		bodyCls: 'cmdb-blue-panel',
		border: false,
		frame: false,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.formatCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.EXTENSION,
						fieldLabel: CMDBuild.Translation.format,
						labelAlign: 'right',
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM,
						valueField: CMDBuild.core.constants.Proxy.VALUE,
						displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
						value: CMDBuild.core.constants.Proxy.PDF,
						editable: false,
						forceSelection: true,

						store: CMDBuild.proxy.widget.OpenReport.getStoreFormats(),
						queryMode: 'local'
					}),
					this.fieldContainer = Ext.create('Ext.container.Container', { // To contains all non fixed fields
						frame: false,
						border: false,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						defaults: {
							maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG
						},

						items: []
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		getExtraButtons: function () {
			return [
				Ext.create('CMDBuild.core.buttons.text.Confirm', {
					scope: this,

					handler: function (button, e) {
						this.delegate.cmfg('onWidgetOpenReportSaveButtonClick');
					}
				})
			];
		}
	});

})();
