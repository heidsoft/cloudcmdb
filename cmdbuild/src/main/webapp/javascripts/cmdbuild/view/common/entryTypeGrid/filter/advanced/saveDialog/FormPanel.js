(function () {

	/**
	 * @link CMDBuild.view.management.common.filter.CMSaveFilterWindow
	 */
	Ext.define('CMDBuild.view.common.entryTypeGrid.filter.advanced.saveDialog.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.filter.advanced.SaveDialog}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-blue-panel',
		border: false,
		frame: false,
		overflowY: 'auto',

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
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onEntryTypeGridFilterAdvancedSaveDialogSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onEntryTypeGridFilterAdvancedSaveDialogAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.NAME,
						fieldLabel: CMDBuild.core.Utils.prependMandatoryLabel(CMDBuild.Translation.name),
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
						allowBlank: false
					}),
					Ext.create('Ext.form.field.TextArea', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.core.Utils.prependMandatoryLabel(CMDBuild.Translation.descriptionLabel),
						maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
						allowBlank: false
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
