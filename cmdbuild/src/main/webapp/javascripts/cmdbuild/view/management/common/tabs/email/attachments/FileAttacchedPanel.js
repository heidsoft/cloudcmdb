(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.attachments.FileAttacchedPanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.attachments.Attachments}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		fileName: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: false,

		bodyPadding: '0 15 0 0',
		frame: true,
		margin: 5,

		layout: {
			type: 'hbox',
			align: 'middle'
		},

		initComponent: function () {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.field.Display', {
						value: this.fileName,
						flex: 1
					}),
					Ext.create('CMDBuild.core.buttons.iconized.Download', {
						tooltip: CMDBuild.Translation.download,
						scope: this,

						handler: function (button, e) {
							this.delegate.cmfg('onTabEmailAttachmentDownloadButtonClick', this);
						}
					}),
					Ext.create('CMDBuild.core.buttons.iconized.Remove', {
						tooltip: CMDBuild.Translation.remove,
						disabled: this.readOnly,
						scope: this,

						handler: function (button, e) {
							this.delegate.cmfg('onTabEmailAttachmentRemoveButtonClick', this);
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
