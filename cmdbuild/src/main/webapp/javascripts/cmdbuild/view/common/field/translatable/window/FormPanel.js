(function() {

	Ext.define('CMDBuild.view.common.field.translatable.window.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.FieldWidths'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.common.field.translatable.Window}
		 */
		delegate: undefined,

		/**
		 * @property {Object}
		 */
		oldValues: {},

		frame: true,
		border: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		defaults: {
			labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
			maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG
		}
	});

})();