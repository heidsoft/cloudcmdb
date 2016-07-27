(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.attachments.ButtonsContainer', {
		extend: 'Ext.container.Container',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.attachments.Attachments}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		attachmentAddFromDmsButton: undefined,

		/**
		 * @property {Ext.form.field.File}
		 */
		attachmentFileField: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		attachmentUploadForm: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: false,

		layout: {
			type: 'hbox',
			padding: '0 5'
		},

		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.attachmentUploadForm = Ext.create('Ext.form.Panel', {
						frame: false,
						border: false,
						encoding: 'multipart/form-data',
						fileUpload: true,
						monitorValid: true,
						bodyCls: 'x-panel-body-default-framed',

						items: [
							this.attachmentFileField = Ext.create('Ext.form.field.File', {
								name: CMDBuild.core.constants.Proxy.FILE,
								buttonText: CMDBuild.Translation.attachFile,
								buttonOnly: true,
								disabled: this.readOnly,
								scope: this,

								listeners: {
									scope: this,
									change: function (field, value, eOpts) {
										this.delegate.cmfg('onTabEmailAttachmentChangeFile');
									}
								}
							})
						]
					}),
					this.attachmentAddFromDmsButton = Ext.create('Ext.button.Button', {
						margin: '0 0 0 5',
						text: CMDBuild.Translation.addAttachmentFromDms,
						disabled: this.readOnly,
						scope: this,

						handler: function (button, e) {
							this.delegate.cmfg('onTabEmailAttachmentAddFromDmsButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
