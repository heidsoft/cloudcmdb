(function () {

	Ext.define('CMDBuild.view.administration.workflow.tabs.properties.panel.UploadXpdl', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.tabs.Properties}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		monitorValid: true,

		layout: {
			type: 'hbox',
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
					Ext.create('Ext.form.field.File', {
						name: CMDBuild.core.constants.Proxy.XPDL, // TODO: waiting for refactor (rename in "file")
						fieldLabel: CMDBuild.Translation.xpdlFile,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						flex: 1,
						margin: '0 5 5 0',
						allowBlank: true
					}),
					Ext.create('Ext.container.Container', {
						flex: 1,

						items: [
							Ext.create('CMDBuild.core.buttons.text.Upload', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWorkflowTabPropertiesUploadXpdlPanelUploadButtonClick');
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
