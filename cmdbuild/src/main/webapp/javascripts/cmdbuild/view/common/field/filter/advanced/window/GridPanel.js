(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.filter.advanced.window.Window'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.panels.relations.Relations}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		includeUsersFiltersCheckbox: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				store: CMDBuild.proxy.common.field.filter.advanced.window.Window.getStoreGroup()
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
						items: [
							'->',
							this.includeUsersFiltersCheckbox = Ext.create('Ext.form.field.Checkbox', {
								boxLabel: CMDBuild.Translation.includeUsersFilters,
								boxLabelCls: 'cmdb-toolbar-item',
								inputValue: true,
								uncheckedValue: false,
								checked: false, // Default as false
								scope: this,

								handler: function(checkbox, checked) {
									this.getStore().reload();
								}
							})
						]
					}),
					Ext.create('Ext.toolbar.Paging', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						store: this.getStore(),
						displayInfo: true,
						displayMsg: '{0} - {1} ' + CMDBuild.Translation.common.display_topic_of + ' {2}',
						emptyMsg: CMDBuild.Translation.common.display_topic_none
					})
				],
				columns: [
					{
						text: CMDBuild.Translation.name,
						dataIndex: CMDBuild.core.constants.Proxy.NAME,
						flex: 1
					},
					{
						text: CMDBuild.Translation.descriptionLabel,
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						flex: 1
					}
				]
			});

			this.callParent(arguments);

			this.getStore().on('load', function(store, records, successful, eOpts) {
				this.delegate.cmfg('onFieldFilterAdvancedWindowPresetGridStoreLoad');
			}, this);
		},

		listeners: {
			select: function(grid, record, index, eOpts) {
				this.delegate.cmfg('onFieldFilterAdvancedWindowPresetGridSelect', record);
			}
		}
	});

})();