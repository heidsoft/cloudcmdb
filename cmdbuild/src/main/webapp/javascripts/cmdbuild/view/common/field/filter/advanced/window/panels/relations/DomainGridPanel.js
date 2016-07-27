(function() {

	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.relations.DomainGridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.filter.advanced.window.Relations'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.panels.relations.Relations}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					Ext.create('Ext.grid.column.Column', {
						text: CMDBuild.Translation.domain,
						flex: 1,

						renderer: function(value, metaData, record, rowIndex, colIndex, store, view) {
							return record.get(CMDBuild.core.constants.Proxy.DOMAIN).getDescription();
						}
					}),
					Ext.create('Ext.grid.column.Column', {
						dataIndex: CMDBuild.core.constants.Proxy.ORIENTED_DESCRIPTION,
						text: CMDBuild.Translation.direction,
						flex: 1
					}),
					Ext.create('Ext.grid.column.Column', {
						text: CMDBuild.Translation.destination,
						flex: 1,

						editor: {
							xtype: 'combo',
							displayField: CMDBuild.core.constants.Proxy.TEXT,
							valueField: CMDBuild.core.constants.Proxy.ID,
							editable: false,
							forceSelection: true,

							store: CMDBuild.proxy.common.field.filter.advanced.window.Relations.getStoreDestination(),
							queryMode: 'local'
						},

						renderer: function(value, metaData, record, rowIndex, colIndex, store, view) {
							return record.get(CMDBuild.core.constants.Proxy.DESTINATION).getDescription();
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
									checkchange: function(column, rowIndex, checked, eOpts) {
										this.delegate.cmfg('onFieldFilterAdvancedWindowRelationsDomainCheckchange', {
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
									checkchange: function(column, rowIndex, checked, eOpts) {
										this.delegate.cmfg('onFieldFilterAdvancedWindowRelationsDomainCheckchange', {
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
									checkchange: function(column, rowIndex, checked, eOpts) {
										this.delegate.cmfg('onFieldFilterAdvancedWindowRelationsDomainCheckchange', {
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
				store: CMDBuild.proxy.common.field.filter.advanced.window.Relations.getStoreDomain(),
				plugins: [
					Ext.create('Ext.grid.plugin.CellEditing', {
						clicksToEdit: 1,

						listeners: {
							scope: this,
							beforeedit: function(editor, e, eOpts) {
								return this.delegate.cmfg('onFieldFilterAdvancedWindowRelationsBeforeEdit', e);
							}
						}
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			select: function(grid, record, index, eOpts) {
				this.delegate.cmfg('onFieldFilterAdvancedWindowRelationsDomainSelect', record);
			}
		}
	});

})();