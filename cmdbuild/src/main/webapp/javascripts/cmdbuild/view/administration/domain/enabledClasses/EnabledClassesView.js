(function() {

	Ext.define('CMDBuild.view.administration.domain.enabledClasses.EnabledClassesView', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.EnabledClasses}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.enabledClasses.TreePanel}
		 */
		destinationTree: undefined,

		/**
		 * @property {CMDBuild.view.administration.domain.enabledClasses.TreePanel}
		 */
		originTree: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		cls: 'cmdb-gray-panel-no-padding',
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.enabledClasses,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyDomain,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainModifyButtonClick');
								}
							})
						]
					}),
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

								handler: function(button, e) {
									this.delegate.cmfg('onDomainSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onDomainAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmdb-gray-panel-no-padding',
						border: false,
						frame: false,

						layout: {
							type: 'hbox',
							align:'stretch'
						},

						items: [
							this.originTree = Ext.create('CMDBuild.view.administration.domain.enabledClasses.TreePanel', {
								delegate: this.delegate,
								title: CMDBuild.Translation.origin
							}),
							{ xtype: 'splitter' },
							this.destinationTree = Ext.create('CMDBuild.view.administration.domain.enabledClasses.TreePanel', {
								delegate: this.delegate,
								title: CMDBuild.Translation.destination
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * Disables isVisible check
		 *
		 * @param {Boolean} state
		 * @param {Boolean} allFields
		 * @param {Boolean} tBarState
		 * @param {Boolean} bBarState
		 *
		 * @override
		 */
		setDisabledModify: function(state, allFields, tBarState, bBarState) {
			this.setDisableFields(state, allFields, true);
			this.setDisabledTopBar(Ext.isBoolean(tBarState) ? tBarState : !state);
			this.setDisabledBottomBar(Ext.isBoolean(bBarState) ? bBarState : state);
		}
	});

})();