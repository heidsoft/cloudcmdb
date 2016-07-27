(function () {

	Ext.define('CMDBuild.view.administration.userAndGroup.group.users.UsersView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.Users}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.group.users.GridPanel}
		 */
		availableGrid: undefined,

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.group.users.GridPanel}
		 */
		selectedGrid: undefined,

		bodyCls: 'cmdb-gray-panel',
		cls: 'cmdb-gray-panel-no-padding',
		border: false,
		frame: false,
		overflowY: 'auto',
		split: true,
		title: CMDBuild.Translation.users,

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

								handler: function (button, e) {
									this.delegate.cmfg('onUserAndGroupGroupTabUsersSaveButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.availableGrid = Ext.create('CMDBuild.view.administration.userAndGroup.group.users.GridPanel', {
						delegate: this.delegate,
						title: CMDBuild.Translation.availableUsers,

						viewConfig: {
							plugins: {
								ptype: 'gridviewdragdrop',
								dragGroup: 'firstGridDDGroup',
								dropGroup: 'secondGridDDGroup'
							}
						}
					}),
					{ xtype: 'splitter' },
					this.selectedGrid = Ext.create('CMDBuild.view.administration.userAndGroup.group.users.GridPanel', {
						delegate: this.delegate,
						title: CMDBuild.Translation.selectedUsers,

						viewConfig: {
							plugins: {
								ptype: 'gridviewdragdrop',
								dragGroup: 'secondGridDDGroup',
								dropGroup: 'firstGridDDGroup'
							}
						}
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function (panel, eOpts) {
				this.delegate.cmfg('onUserAndGroupGroupTabUsersShow');
			}
		}
	});

})();
