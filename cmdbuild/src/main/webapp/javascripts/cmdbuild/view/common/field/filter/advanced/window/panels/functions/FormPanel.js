(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.functions.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.filter.advanced.window.Functions'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.panels.Functions}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.CMErasableCombo}
		 */
		functionComboBox: undefined,

		bodyCls: 'x-panel-default-framed',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.functionComboBox = Ext.create('CMDBuild.view.common.field.CMErasableCombo', {
						name: CMDBuild.core.constants.Proxy.FUNCTION,
						fieldLabel: CMDBuild.Translation.functionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						displayField: CMDBuild.core.constants.Proxy.NAME,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						editable: false,
						forceSelection: true,

						store: CMDBuild.proxy.common.field.filter.advanced.window.Functions.getStore(),
						queryMode: 'local'
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();