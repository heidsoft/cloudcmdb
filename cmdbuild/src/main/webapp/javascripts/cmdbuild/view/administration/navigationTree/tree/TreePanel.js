(function () {

	Ext.define('CMDBuild.view.administration.navigationTree.tree.TreePanel', {
		extend: 'Ext.tree.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.navigationTree.TreeNode'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.navigationTree.Tree}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.plugin.CellEditing}
		 */
		gridEditorPlugin: undefined,

		autoScroll: true,
		border: false,
		cls: 'cmdb-border-bottom',
		collapsible: true,
		considerAsFieldToDisable: true,
		disableSelection: true,
		enableColumnHide: false,
		frame: false,
		header: false,
		hideCollapseTool: true,
		sortableColumns: false, // BUGGED in ExtJs 4.2, workaround setting sortable: false to columns

		viewConfig: {
			markDirty: false // Workaround to avoid dirty mark on hidden checkColumn cells
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					{
						xtype: 'treecolumn',
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.navigationTree,
						flex: 2,
						sortable: false,
						draggable: false
					},
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.FILTER,
						text: CMDBuild.Translation.cqlFilter,
						flex: 1,
						align: 'left',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						editor: { xtype: 'textfield' }
					}),
					Ext.create('Ext.grid.column.CheckColumn', {
						dataIndex: CMDBuild.core.constants.Proxy.ENABLE_RECURSION,
						text: CMDBuild.Translation.enableRecursion,
						width: 100,
						align: 'center',
						hideable: false,
						menuDisabled: true,
						fixed: true,
						scope: this,

						renderer: function (value, meta, record, rowIndex, colIndex, store, view) {
							var recordDomain = record.get(CMDBuild.core.constants.Proxy.DOMAIN);

							// HACK: to recreate original renderer method behaviour, callParent doesn't work
							if (
								!record.isRoot()
								&& recordDomain.get(CMDBuild.core.constants.Proxy.DESTINATION_CLASS_NAME) == recordDomain.get(CMDBuild.core.constants.Proxy.ORIGIN_CLASS_NAME)
							) {
								var cssPrefix = Ext.baseCSSPrefix;
								var cls = [cssPrefix + 'grid-checkcolumn'];

								if (this.disabled)
									meta.tdCls += ' ' + this.disabledCls;

								if (value)
									cls.push(cssPrefix + 'grid-checkcolumn-checked');

								return '<img class="' + cls.join(' ') + '" src="' + Ext.BLANK_IMAGE_URL + '"/>';
							} else {
								return '';
							}
						}
					})
				],
				store: Ext.create('Ext.data.TreeStore', {
					model: 'CMDBuild.model.navigationTree.TreeNode',

					root: {
						text: 'ROOT',
						checked: true,
						expanded: false,
						children: []
					}
				}),
				plugins: [
					this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			checkchange: function (node, checked, eOpts) {
				this.delegate.cmfg('onNavigationTreeTabTreeCheckChange', {
					node: node,
					checked: checked
				});
			},
			itemexpand: function (node, eOpts) {
				this.delegate.cmfg('onNavigationTreeTabTreeNodeExpand', node);
			}
		}
	});

})();
