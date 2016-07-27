(function () {

	Ext.define('CMDBuild.view.administration.tasks.CMTasksGrid', {
		extend: 'Ext.grid.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Mixed} Task specific controller
		 */
		delegate: undefined,

		border: false,
		cls: 'cmdb-border-bottom',
		frame: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.ID,
						hidden: true
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.TYPE,
						text: CMDBuild.Translation.administration.tasks.type,
						flex: 1,
						scope: this,

						renderer: function (value, metaData, record) {
							return this.typeGridColumnRenderer(value, metaData, record);
						}
					},
					{
						text: CMDBuild.Translation.description_,
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						flex: 4
					},
					Ext.create('Ext.ux.grid.column.Active', {
						dataIndex: CMDBuild.core.constants.Proxy.ACTIVE,
						text: CMDBuild.Translation.active,
						iconAltTextActive: CMDBuild.Translation.administration.tasks.running,
						iconAltTextNotActive: CMDBuild.Translation.administration.tasks.stopped,
						width: 60,
						align: 'center',
						hideable: false,
						menuDisabled: true,
						fixed: true
					}),
					Ext.create('Ext.grid.column.Action', {
						align: 'center',
						width: 75,
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							Ext.create('CMDBuild.core.buttons.taskManager.SingleExecution', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.singleExecution,
								scope: this,

								isDisabled: function (grid, rowIndex, colIndex, item, record) {
									return !record.get(CMDBuild.core.constants.Proxy.EXECUTABLE);
								},

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onSingleExecutionButtonClick', record);
								}
							}),
							Ext.create('CMDBuild.core.buttons.taskManager.CyclicExecution', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.cyclicExecution,
								scope: this,

								isDisabled: function (grid, rowIndex, colIndex, item, record) {
									return record.get(CMDBuild.core.constants.Proxy.ACTIVE);
								},

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onCyclicExecutionButtonClick', record);
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Stop', {
								withSpacer: true,
								tooltip: CMDBuild.Translation.stop,
								scope: this,

								isDisabled: function (grid, rowIndex, colIndex, item, record) {
									return !record.get(CMDBuild.core.constants.Proxy.ACTIVE);
								},

								handler: function (grid, rowIndex, colIndex, node, e, record, rowNode) {
									this.delegate.cmOn('onStopButtonClick', record);
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},


		listeners: {
			itemdblclick: function (grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick');
			},
			select: function (model, record, index, eOpts) {
				this.delegate.cmOn('onRowSelected');
			}
		},

		/**
		 * Rendering task type translating with local language data
		 *
		 * @param {Mixed} value
		 * @param {Object} metaData
		 * @param {Object} record
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		typeGridColumnRenderer: function (value, metaData, record) {
			if (typeof value == 'string') {
				if (this.delegate.correctTaskTypeCheck(value)) {
					var splittedType = value.split('_');
					value = '';

					for (var i = 0; i < splittedType.length; i++) {
						if (i == 0) {
							value += eval('CMDBuild.Translation.administration.tasks.tasksTypes.' + splittedType[i]);
						} else {
							value += ' ' + eval('CMDBuild.Translation.administration.tasks.tasksTypes.' + splittedType[0] + 'Types.' + splittedType[i]).toLowerCase();
						}
					}
				}
			}

			return value;
		}
	});

})();
