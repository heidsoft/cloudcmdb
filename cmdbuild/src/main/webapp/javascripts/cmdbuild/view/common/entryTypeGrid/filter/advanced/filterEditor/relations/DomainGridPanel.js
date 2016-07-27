(function () {

	/**
	 * @link CMDBuild.view.management.common.filter.CMDomainGrid
	 */
	Ext.define('CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.entryTypeGrid.filter.advanced.filterEditor.Relations'
		],

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.Relations}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		frame: false,
		height: '30%',
		region: 'north',
		split: true,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.DOMAIN_DESCRIPTION,
						text: CMDBuild.Translation.domain,
						flex: 1,
						sortable: false,
						hideable: false,
						menuDisabled: true
					}),
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.ORIENTED_DESCRIPTION,
						text: CMDBuild.Translation.direction,
						flex: 1,
						sortable: false,
						hideable: false,
						menuDisabled: true
					}),
					Ext.create('Ext.grid.column.Column', {
						text: CMDBuild.Translation.destination,
						flex: 1,
						sortable: false,
						hideable: false,
						menuDisabled: true,

						editor: {
							xtype: 'combo',
							displayField: CMDBuild.core.constants.Proxy.TEXT,
							valueField: CMDBuild.core.constants.Proxy.ID,
							editable: false,
							forceSelection: true,

							store: CMDBuild.proxy.common.entryTypeGrid.filter.advanced.filterEditor.Relations.getStoreDestination(),
							queryMode: 'local'
						},

						renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
							return record.get([CMDBuild.core.constants.Proxy.DESTINATION, CMDBuild.core.constants.Proxy.DESCRIPTION]);
						}
					}),
					Ext.create('Ext.grid.column.Column', {
						text: CMDBuild.Translation.relations,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						columns: [
							Ext.create('Ext.grid.column.CheckColumn', {
								dataIndex: 'noone',
								text: CMDBuild.Translation.noOne,
								width: 90,
								align: 'center',
								sortable: false,
								hideable: false,
								menuDisabled: true,
								fixed: true,
								scope: this,

								listeners: {
									scope: this,
									checkchange: function (column, rowIndex, checked, eOpts) {
										this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorRelationsCheckchange', {
											checked: checked,
											propertyName: column.dataIndex,
											record: this.getStore().getAt(rowIndex)
										});
									}
								}
							}),
							Ext.create('Ext.grid.column.CheckColumn', {
								dataIndex: 'any',
								text: CMDBuild.Translation.any,
								width: 90,
								align: 'center',
								sortable: false,
								hideable: false,
								menuDisabled: true,
								fixed: true,
								scope: this,

								listeners: {
									scope: this,
									checkchange: function (column, rowIndex, checked, eOpts) {
										this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorRelationsCheckchange', {
											checked: checked,
											propertyName: column.dataIndex,
											record: this.getStore().getAt(rowIndex)
										});
									}
								}
							}),
							Ext.create('Ext.grid.column.CheckColumn', {
								dataIndex: 'oneof',
								text: CMDBuild.Translation.fromSelection,
								width: 90,
								align: 'center',
								sortable: false,
								hideable: false,
								menuDisabled: true,
								fixed: true,
								scope: this,

								listeners: {
									scope: this,
									checkchange: function (column, rowIndex, checked, eOpts) {
										this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorRelationsCheckchange', {
											checked: checked,
											propertyName: column.dataIndex,
											record: this.getStore().getAt(rowIndex)
										});
									}
								}
							})
						]
					})
				],
				store: CMDBuild.proxy.common.entryTypeGrid.filter.advanced.filterEditor.Relations.getStoreDomain(),
				plugins: [
					Ext.create('Ext.grid.plugin.CellEditing', {
						clicksToEdit: 1,

						listeners: {
							scope: this,
							beforeedit: function (editor, e, eOpts) {
								return this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorRelationsGridDomainBeforeEdit', e);
							}
						}
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			select: function (panel, record, index, eOpts) {
				this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorRelationsDomainSelect', record);
			},
			show: function (panel, eOpts) {
				this.delegate.cmfg('onEntryTypeGridFilterAdvancedFilterEditorRelationsGridDomainViewShow');
			}
		}
	});

})();
