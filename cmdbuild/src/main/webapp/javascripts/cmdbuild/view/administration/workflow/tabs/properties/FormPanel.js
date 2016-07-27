(function () {

	Ext.define('CMDBuild.view.administration.workflow.tabs.properties.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.tabs.Properties}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.workflow.properties.fieldSet.DownloadXpdl}
		 */
		downloadXpdlPanel: undefined,

		/**
		 * @property {CMDBuild.view.administration.workflow.tabs.properties.panel.BaseProperties}
		 */
		propertiesPanel: undefined,

		/**
		 * @property {CMDBuild.view.administration.workflow.properties.fieldSet.UploadXpdl}
		 */
		uploadXpdlPanel: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',
		split: true,

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
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyProcess,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWorkflowTabPropertiesModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								text: CMDBuild.Translation.removeProcess,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWorkflowTabPropertiesRemoveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.split.Print', {
								delegate: this.delegate,
								text: CMDBuild.Translation.printProcess,
								formatList: [
									CMDBuild.core.constants.Proxy.PDF,
									CMDBuild.core.constants.Proxy.ODT
								]
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.FieldSet', {
						layout: 'fit',
						title: CMDBuild.Translation.baseProperties,

						items: [
							this.propertiesPanel = Ext.create('CMDBuild.view.administration.workflow.tabs.properties.panel.BaseProperties', { delegate: this.delegate })
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						layout: 'fit',
						title: CMDBuild.Translation.uploadXpdl,

						items: [
							this.uploadXpdlPanel = Ext.create('CMDBuild.view.administration.workflow.tabs.properties.panel.UploadXpdl', { delegate: this.delegate })
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						layout: 'fit',
						title: CMDBuild.Translation.downloadXpdlTamplete,

						items: [
							this.downloadXpdlPanel = Ext.create('CMDBuild.view.administration.workflow.tabs.properties.panel.DownloadXpdl', { delegate: this.delegate })
						]
					})
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();
