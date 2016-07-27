(function () {

	Ext.define('CMDBuild.view.configure.Wizard', {
		extend: 'Ext.form.Panel',

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.configure.Configure}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.text.Previous}
		 */
		previousButton: undefined,

		/**
		 * @property {CMDBuild.core.buttons.text.Next}
		 */
		nextButton: undefined,

		/**
		 * @property {CMDBuild.core.buttons.text.Finish}
		 */
		finishButton: undefined,

		activeItem: 0,
		border: false,
		cls: 'cmdb-blue-panel',
		frame: false,
		layout: 'card',
		padding: '5',

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
							pack: 'end'
						},

						items: [
							this.previousButton = Ext.create('CMDBuild.core.buttons.text.Previous', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onConfigurationViewportWizardNavigationButtonClick', 'previous');
								}
							}),
							this.nextButton = Ext.create('CMDBuild.core.buttons.text.Next', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onConfigurationViewportWizardNavigationButtonClick', 'next');
								}
							}),
							this.finishButton = Ext.create('CMDBuild.core.buttons.text.Finish', {
								hidden: true,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onConfigurationViewportWizardFinishButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.step1 = Ext.create('CMDBuild.view.configure.step.Step1', { delegate: this.delegate }),
					this.step2 = Ext.create('CMDBuild.view.configure.step.Step2', { delegate: this.delegate }),
					this.step3 = Ext.create('CMDBuild.view.configure.step.Step3', { delegate: this.delegate })
				]
			});

			this.callParent(arguments);
		}
	});

})();
