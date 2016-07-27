(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.Window', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.filter.CMFilterAttributes}
		 */
		attributePanel: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.buildFilter,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.ColumnPrivilegesView}
		 */
		columnPrivileges: undefined,

		/**
		 * @property {CMDBuild.view.management.common.filter.CMFunctions}
		 */
		functionPanel: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.management.common.filter.CMRelations}
		 */
		relationPanel: undefined,

		/**
		 * @property {Ext.panel.Panel}
		 */
		rowPrivileges: undefined,

		/**
		 * @property {Ext.tab.Panel}
		 */
		tabPanel: undefined,

		/**
		 * @property {Ext.tab.Panel}
		 */
		windowTabPanel: undefined,

		closeAction: 'hide',

		initComponent: function() {
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
							Ext.create('CMDBuild.core.buttons.text.Confirm', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFieldFilterAdvancedWindowConfirmButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onFieldFilterAdvancedWindowAbortButtonClick');
								}
							})
						]
					})
				]
			});

			// Items property evaluation
			if (this.delegate.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'columnPrivileges')) {
				Ext.apply(this, {
					layout: 'fit',
					items: [
						this.windowTabPanel = Ext.create('Ext.tab.Panel', {
							border: false,
							frame: false,

							items: [
								this.rowPrivileges = Ext.create('Ext.panel.Panel', {
									title: CMDBuild.Translation.rowsPrivileges,
									layout: 'border',
									border: false,
									frame: false,

									items: [
										this.grid = Ext.create('CMDBuild.view.common.field.filter.advanced.window.GridPanel', {
											delegate: this.delegate,
											region: 'north',
											height: '30%',
											split: true
										}),
										this.tabPanel = Ext.create('Ext.tab.Panel', {
											region: 'center',
											bodyCls: 'cmdb-gray-panel-no-padding',
											border: false,
											cls: 'x-panel-body-default-framed cmdb-border-top',

											items: []
										})
									]
								}),
								this.columnPrivileges = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.ColumnPrivilegesView')
							]
						})
					]
				});
			} else {
				Ext.apply(this, {
					layout: 'border',
					items: [
						this.grid = Ext.create('CMDBuild.view.common.field.filter.advanced.window.GridPanel', {
							delegate: this.delegate,
							region: 'north',
							height: '30%',
							split: true
						}),
						this.tabPanel = Ext.create('Ext.tab.Panel', {
							region: 'center',
							bodyCls: 'cmdb-gray-panel-no-padding',
							border: false,
							cls: 'x-panel-body-default-framed cmdb-border-top',

							items: []
						})
					]
				});
			}

			this.callParent(arguments);
		},

		listeners: {
			beforeshow: function(window, eOpts) {
				return this.delegate.cmfg('onFieldFilterAdvancedWindowBeforeShow');
			},
			show: function(window, eOpts) {
				return this.delegate.cmfg('onFieldFilterAdvancedWindowShow');
			}
		}
	});

})();