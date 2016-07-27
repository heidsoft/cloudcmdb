(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.attachments.Attachments', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.tabs.email.Attachment'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.EmailWindow}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTabEmailAttachmentAddFromDmsButtonClick',
			'onTabEmailAttachmentChangeFile',
			'onTabEmailAttachmentDownloadButtonClick',
			'onTabEmailAttachmentRemoveButtonClick',
			'tabEmailAttachmentAddPanel',
			'tabEmailAttachmentNamesGet'
		],

		/**
		 * @cfg {Mixed}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.attachments.MainContainer}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.tabs.email.EmailWindow} configurationObject.parentDelegate
		 * @param {Mixed} configurationObject.record
		 * @param {CMDBuild.view.management.common.tabs.email.attachments.MainContainer} configurationObject.view
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			if (CMDBuild.configuration.dms.get(CMDBuild.core.constants.Proxy.ENABLED)) {
				this.callParent(arguments);

				this.view.delegate = this;
				this.view.attachmentButtonsContainer.delegate = this;
			} else {
				_warning('Alfresco DMS not enabled', this);
			}
		},

		onTabEmailAttachmentAddFromDmsButtonClick: function () {
			Ext.create('CMDBuild.controller.management.common.tabs.email.attachments.Picker', {
				parentDelegate: this,
				record: this.record
			});
		},

		onTabEmailAttachmentChangeFile: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.EMAIL_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.TEMPORARY] = this.record.get(CMDBuild.core.constants.Proxy.TEMPORARY);

			CMDBuild.proxy.common.tabs.email.Attachment.upload({
				form: this.view.attachmentButtonsContainer.attachmentUploadForm.getForm(),
				params: params,
				loadMask: this.cmfg('tabEmailEmailWindowGetView'),
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					this.cmfg('tabEmailAttachmentAddPanel', decodedResponse);
				}
			});
		},

		/**
		 * @param {CMDBuild.view.management.common.tabs.email.attachments.FileAttacchedPanel} attachmentPanel
		 */
		onTabEmailAttachmentDownloadButtonClick: function (attachmentPanel) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.EMAIL_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.FILE_NAME] = attachmentPanel[CMDBuild.core.constants.Proxy.FILE_NAME];
			params[CMDBuild.core.constants.Proxy.TEMPORARY] = this.record.get(CMDBuild.core.constants.Proxy.TEMPORARY);

			CMDBuild.proxy.common.tabs.email.Attachment.download({ params: params });
		},

		/**
		 * @param {CMDBuild.view.management.common.tabs.email.attachments.FileAttacchedPanel} attachmentPanel
		 */
		onTabEmailAttachmentRemoveButtonClick: function (attachmentPanel) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.EMAIL_ID] = this.record.get(CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.FILE_NAME] = attachmentPanel[CMDBuild.core.constants.Proxy.FILE_NAME];
			params[CMDBuild.core.constants.Proxy.TEMPORARY] = this.record.get(CMDBuild.core.constants.Proxy.TEMPORARY);

			CMDBuild.proxy.common.tabs.email.Attachment.remove({
				params: params,
				loadMask: this.cmfg('tabEmailEmailWindowGetView'), // Apply load mask to target
				scope: this,
				success: function (response, options ,decodedResponse) {
					this.view.attachmentPanelsContainer.remove(attachmentPanel);
				}
			});
		},

		/**
		 * @param {String} fileName
		 */
		tabEmailAttachmentAddPanel: function (fileName) {
			this.view.addPanel(
				Ext.create('CMDBuild.view.management.common.tabs.email.attachments.FileAttacchedPanel', {
					delegate: this,
					fileName: fileName,
					readOnly: this.view.readOnly
				})
			);
		},

		/**
		 * @returns {Array} attachmentsNames
		 */
		tabEmailAttachmentNamesGet: function () {
			var attachmentsNames = [];

			this.view.attachmentPanelsContainer.items.each(function (attachmentObject, i, allAttachmentObjects) {
				attachmentsNames.push(attachmentObject[CMDBuild.core.constants.Proxy.FILE_NAME]);
			});

			return attachmentsNames;
		}
	});

})();
