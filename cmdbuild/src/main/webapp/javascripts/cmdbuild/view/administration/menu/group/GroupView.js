(function () {

	Ext.define('CMDBuild.view.administration.menu.group.GroupView', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.model.menu.TreeStore'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.menu.Groups}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Trigger}
		 */
		addFolderField: undefined,

		/**
		 * @property {Ext.tree.Panel}
		 */
		availableItemsTreePanel: undefined,

		/**
		 * @property {Ext.tree.Panel}
		 */
		menuTreePanel: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.MoveRight}
		 */
		removeItemButton: undefined,

		/**
		 * @property {Object}
		 */
		translatableAttributesConfigurationsBuffer: {},

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,

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
			var me = this;

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							this.addFolderField = Ext.create('Ext.form.field.Trigger', {
								fieldLabel: CMDBuild.Translation.newFolder,
								triggerCls: 'trigger-add',
								margin: '0 0 0 5',
								allowBlank: true,

								onTriggerClick: function (e) {
									me.delegate.cmfg('onMenuGroupAddFolderButtonClick', this.getValue());
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Remove', {
								text: CMDBuild.Translation.removeMenu,
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onMenuGroupRemoveMenuButtonClick');
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

								handler: function (button, e) {
									this.delegate.cmfg('onMenuGroupSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onMenuGroupAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.menuTreePanel = Ext.create('Ext.tree.Panel', {
						border: true,
						flex: 1,
						hideHeaders: true,
						rootVisible: true,
						title: CMDBuild.Translation.customMenu,

						viewConfig: {
							plugins: [
								{ ptype: 'treeviewdragdrop' }
							]
						},

						columns: [
							{
								xtype: 'treecolumn',
								dataIndex: CMDBuild.core.constants.Proxy.TEXT,
								flex: 1,

								editor: {
									xtype: 'textfield',
									allowBlank: false
								}
							},
							Ext.create('Ext.grid.column.Action', {
								align: 'center',
								width: 30,
								sortable: false,
								hideable: false,
								menuDisabled: true,
								fixed: true,

								items: [
									Ext.create('CMDBuild.core.buttons.FieldTranslation', {
										scope: this,

										getClass: function (value, metadata, record, rowIndex, colIndex, store) { // Hides icon in root node or if no translations enabled
											return (record.isRoot() || !CMDBuild.configuration.localization.hasEnabledLanguages()) ? '' : 'translate';
										},

										handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
											if (Ext.isEmpty(record.get('uuid'))) {
												CMDBuild.core.Message.warning(null, CMDBuild.Translation.warnings.saveMenuBeforeAccess, false);
											} else {
												Ext.create('CMDBuild.controller.common.field.translatable.NoFieldWindow', {
													buffer: this.translatableAttributesConfigurationsBuffer,
													translationFieldConfig: {
														type: CMDBuild.core.constants.Proxy.MENU_ITEM,
														identifier: record.get('uuid'),
														field: CMDBuild.core.constants.Proxy.DESCRIPTION
													}
												});
											}
										},

										isDisabled: function (grid, rowIndex, colIndex, item, record) { // Disable icons in root node or if no translations enabled to avoid click action
											return record.isRoot() || !CMDBuild.configuration.localization.hasEnabledLanguages();
										}
									})
								]
							})
						],

						plugins: [
							this.cellEditor = Ext.create('Ext.grid.plugin.CellEditing', {
								clicksToEdit: 2,
								autoCancel: false,

								listeners: {
									// Prevent to edit root node
									beforeedit: function (editor, context, eOpts) {
										if (context.record.isRoot())
											return false;
									}
								}
							})
						],

						store: Ext.create('Ext.data.TreeStore', {
							model: 'CMDBuild.model.menu.TreeStore',

							root: {
								text: '',
								expanded: true,
								children: []
							}
						}),

						listeners: {
							scope: this,
							beforeselect: function (treePanel, record, index, eOpts) {
								return this.delegate.cmfg('onMenuGroupMenuTreeBeforeselect', record);
							},
							selectionchange: function (treePanel, selected, eOpts) {
								this.delegate.cmfg('onMenuGroupMenuTreeSelectionchange');
							}
						}
					}),
					{
						xtype: 'panel',
						bodyCls: 'x-panel-body-default-framed',
						border: false,
						frame: false,
						margin: '5',

						layout: {
							type: 'vbox',
							pack: 'center',
							align: 'center'
						},

						items: [
							this.removeItemButton = Ext.create('CMDBuild.core.buttons.iconized.MoveRight', {
								tooltip: CMDBuild.Translation.remove,
								scope: this,
								disabled: true,

								handler: function (button, e) {
									this.delegate.cmfg('onMenuGroupRemoveItemButtonClick');
								}
							})
						]
					},
					this.availableItemsTreePanel = Ext.create('Ext.tree.Panel', {
						border: true,
						flex: 1,
						rootVisible: false,
						title: CMDBuild.Translation.availableElements,

						viewConfig: {
							plugins: [
								{ ptype: 'treeviewdragdrop', enableDrop: false }
							]
						},

						store: Ext.create('Ext.data.TreeStore', {
							model: 'CMDBuild.model.menu.TreeStore',

							root: {
								text: '',
								expanded: true,
								children: []
							}
						})
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
