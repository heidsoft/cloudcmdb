(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.attributes.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.common.field.filter.advanced.window.panels.Attributes}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.split.Add}
		 */
		addAttributeButton: undefined,

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
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							this.addAttributeButton = Ext.create('CMDBuild.core.buttons.iconized.split.Add', {
								text: CMDBuild.Translation.chooseAnAttribute
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();