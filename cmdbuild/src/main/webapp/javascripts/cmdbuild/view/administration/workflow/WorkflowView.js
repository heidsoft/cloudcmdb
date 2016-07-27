(function () {

	Ext.define('CMDBuild.view.administration.workflow.WorkflowView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.Workflow}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.processes,

		/**
		 * @property {Ext.tab.Panel}
		 */
		tabPanel: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: true,
		frame: false,
		layout: 'fit',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				// TODO: legacy waiting for refactor (attribute module refactor)
				attributesPanel: Ext.create('CMDBuild.view.administration.workflow.CMProcessAttributes', {
					title: CMDBuild.Translation.attributes,
					border: false,
					disabled: true
				}),
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.addProcess,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onWorkflowAddButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.split.Print', {
								delegate: this.delegate,
								text: CMDBuild.Translation.printSchema,
								formatList: [
									CMDBuild.core.constants.Proxy.PDF,
									CMDBuild.core.constants.Proxy.ODT
								]
							})
						]
					})
				],
				items: [
					this.tabPanel = Ext.create('Ext.tab.Panel', {
						frame: false,
						border: false,

						items: []
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
