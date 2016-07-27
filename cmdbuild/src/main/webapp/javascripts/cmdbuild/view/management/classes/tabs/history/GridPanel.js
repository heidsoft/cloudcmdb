(function () {

	Ext.define('CMDBuild.view.management.classes.tabs.history.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.configurations.DataFormat',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.classes.tabs.History'
		],

		/**
		 * @cfg {CMDBuild.controller.management.classes.tabs.History}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		includeRelationsCheckbox: undefined,

		config: {
			plugins: [
				Ext.create('CMDBuild.view.management.classes.tabs.history.RowExpander', { id: 'classesHistoryTabRowExpander' })
			]
		},

		autoScroll: true,
		border: false,
		cls: 'cmdb-history-grid', // Apply right style to grid rows
		disableSelection: true,
		frame: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							'->',
							this.includeRelationsCheckbox = Ext.create('Ext.form.field.Checkbox', {
								boxLabel: CMDBuild.Translation.includeRelations,
								boxLabelCls: 'cmdb-toolbar-item',
								checked: false, // Default as false
								scope: this,

								handler: function (checkbox, checked) {
									this.delegate.cmfg('onClassesTabHistoryIncludeRelationCheck');
								}
							})
						]
					})
				],
				columns: this.buildColumns(),
				store: CMDBuild.proxy.classes.tabs.History.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			viewready: function (view, eOpts) {
				this.getView().on('expandbody', function (rowNode, record, expandRow, eOpts) {
					this.doLayout(); // To refresh the scrollbar status and seems to fix also a glitch effect on row collapse

					this.delegate.cmfg('onClassesTabHistoryRowExpand', record);
				}, this);
			}
		},

		/**
		 * @returns {Array} columns
		 *
		 * @private
		 */
		buildColumns: function () {
			var columns = [
				Ext.create('Ext.grid.column.Date', {
					dataIndex: CMDBuild.core.constants.Proxy.BEGIN_DATE,
					text: CMDBuild.Translation.beginDate,
					width: 140,
					format: CMDBuild.core.configurations.DataFormat.getDateTime(),
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true
				}),
				Ext.create('Ext.grid.column.Date', {
					dataIndex: CMDBuild.core.constants.Proxy.END_DATE,
					text: CMDBuild.Translation.endDate,
					width: 140,
					format: CMDBuild.core.configurations.DataFormat.getDateTime(),
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true
				}),
				{
					dataIndex: CMDBuild.core.constants.Proxy.USER,
					text: CMDBuild.Translation.user,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					flex: 1
				}
			];

			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.SIMPLE_HISTORY_MODE_FOR_CARD)) {
				Ext.Array.push(columns, [
					Ext.create('Ext.ux.grid.column.Tick', {
						dataIndex: CMDBuild.core.constants.Proxy.IS_CARD,
						text: CMDBuild.Translation.attributes,
						iconAltText: CMDBuild.Translation.attributes,
						width: 65,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					}),
					Ext.create('Ext.ux.grid.column.Tick', {
						dataIndex: CMDBuild.core.constants.Proxy.IS_RELATION,
						text: CMDBuild.Translation.relation,
						iconAltText: CMDBuild.Translation.relation,
						width: 65,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					}),
					{
						dataIndex: CMDBuild.core.constants.Proxy.DOMAIN,
						text: CMDBuild.Translation.domain,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.DESTINATION_DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					}
				]);
			}

			return columns;
		}
	});

})();
