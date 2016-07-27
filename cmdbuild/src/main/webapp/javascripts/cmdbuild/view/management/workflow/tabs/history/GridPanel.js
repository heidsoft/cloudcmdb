(function () {

	Ext.define('CMDBuild.view.management.workflow.tabs.history.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.configurations.DataFormat',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.workflow.tabs.History'
		],

		/**
		 * @cfg {CMDBuild.controller.management.workflow.tabs.History}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		includeRelationsCheckbox: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		includeSystemActivitiesCheckbox: undefined,

		config: {
			plugins: [
				Ext.create('CMDBuild.view.management.workflow.tabs.history.RowExpander', { id: 'processesHistoryTabRowExpander' })
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
							this.includeSystemActivitiesCheckbox = Ext.create('Ext.form.field.Checkbox', {
								boxLabel: CMDBuild.Translation.includeSystemActivities,
								boxLabelCls: 'cmdb-toolbar-item',
								checked: false, // Default as false
								scope: this,

								handler: function (checkbox, checked) {
									this.delegate.cmfg('onWorkflowTabHistoryIncludeSystemActivitiesCheck');
								}
							}),
							this.includeRelationsCheckbox = Ext.create('Ext.form.field.Checkbox', {
								boxLabel: CMDBuild.Translation.includeRelations,
								boxLabelCls: 'cmdb-toolbar-item',
								checked: false, // Default as false
								scope: this,

								handler: function (checkbox, checked) {
									this.delegate.cmfg('onWorkflowTabHistoryIncludeRelationCheck');
								}
							})
						]
					})
				],
				columns: this.buildColumns(),
				store: CMDBuild.proxy.workflow.tabs.History.getStore()
			});

			this.callParent(arguments);

			// Apply activitiesStore filter
			this.getStore().on('load', function (store, records, successful, eOpts) {
				this.delegate.cmfg('onWorkflowTabHistoryIncludeSystemActivitiesCheck');
			}, this);
		},

		listeners: {
			viewready: function (view, eOpts) {
				this.getView().on('expandbody', function (rowNode, record, expandRow, eOpts) {
					this.doLayout(); // To refresh the scrollbar status and seems to fix also a glitch effect on row collapse

					this.delegate.cmfg('onWorkflowTabHistoryRowExpand', record);
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

			if (!CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.SIMPLE_HISTORY_MODE_FOR_PROCESS)) {
				Ext.Array.push(columns, [
					{
						dataIndex: CMDBuild.core.constants.Proxy.ACTIVITY_NAME,
						text: CMDBuild.Translation.activityName,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.PERFORMERS,
						text: CMDBuild.Translation.activityPerformer,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.STATUS,
						text: CMDBuild.Translation.status,
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
