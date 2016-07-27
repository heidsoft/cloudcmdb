(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.attachments.picker.AttachmentGrid', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.tabs.email.Attachment'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.attachments.Picker}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-top',
		frame: false,
		split: true,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					{
						text: CMDBuild.Translation.fileName,
						dataIndex: 'Filename', // TODO: waiting for refactor (rename)
						flex: 1
					},
					{
						text: CMDBuild.Translation.descriptionLabel,
						dataIndex: 'Description', // TODO: waiting for refactor (rename)
						flex: 1
					}
				],
				selModel: Ext.create('Ext.selection.CheckboxModel', { injectCheckbox: 'first' }),
				store: CMDBuild.proxy.common.tabs.email.Attachment.getStore()
			});

			this.callParent(arguments);
		}
	});

})();
