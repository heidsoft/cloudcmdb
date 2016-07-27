(function () {

	Ext.define('CMDBuild.view.administration.tasks.generic.CMStep3Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormGenericController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.generic.CMStep3}
		 */
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @overwrite
		 */
		cmOn: function (name, param, callBack) {
			switch (name) {
				case 'onTaskManagerFormTaskGenericStep3DeleteRowButtonClick':
					return this.onTaskManagerFormTaskGenericStep3DeleteRowButtonClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},


		/**
		 * @param {CMDBuild.model.taskManager.generic.Context} record
		 *
		 * @returns {Void}
		 */
		onTaskManagerFormTaskGenericStep3DeleteRowButtonClick: function (record) {
			this.view.grid.getStore().remove(record);
		},

		// GETters functions
			/**
			 * @returns {Object} data
			 */
			getData: function () {
				var data = {};

				// To validate and filter grid rows
				this.view.grid.getStore().each(function (record) {
					if (
						!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.KEY))
						&& !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.VALUE))
					) {
						data[record.get(CMDBuild.core.constants.Proxy.KEY)] = record.get(CMDBuild.core.constants.Proxy.VALUE);
					}
				}, this);

				return data;
			},
		// SETters functions
			/**
			 * @param {Object} data
			 *
			 * @returns {Void}
			 */
			setData: function (data) {
				if (Ext.isObject(data) && !Ext.Object.isEmpty(data)) {
					var storeData = [];

					Ext.Object.each(data, function (key, value, myself) {
						var recordObject = {};
						recordObject[CMDBuild.core.constants.Proxy.KEY] = key;
						recordObject[CMDBuild.core.constants.Proxy.VALUE] = value;

						storeData.push(recordObject);
					}, this);

					if (!Ext.isEmpty(storeData))
						this.view.grid.getStore().loadData(storeData);
				}
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.generic.CMStep3', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.taskManager.generic.Context'
		],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.generic.CMStep3Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.generic.CMStep3Delegate', this);

			Ext.apply(this, {
				items: [
					this.grid = Ext.create('Ext.grid.Panel', {
						title: CMDBuild.Translation.contextVariables,
						considerAsFieldToDisable: true,

						columns: [
							{
								dataIndex: CMDBuild.core.constants.Proxy.KEY,
								text: CMDBuild.Translation.key,
								flex: 1,

								editor: {
									xtype: 'textfield',
									vtype: 'alphanumlines'
								}
							},
							{
								dataIndex: CMDBuild.core.constants.Proxy.VALUE,
								text: CMDBuild.Translation.value,
								flex: 1,

								editor: { xtype: 'textfield' }
							},
							Ext.create('Ext.grid.column.Action', {
								align: 'center',
								width: 30,
								sortable: false,
								hideable: false,
								menuDisabled: true,
								fixed: true,

								items: [
									Ext.create('CMDBuild.core.buttons.iconized.Remove', {
										withSpacer: true,
										tooltip: CMDBuild.Translation.remove,
										scope: this,

										handler: function (view, rowIndex, colIndex, item, e, record) {
											this.delegate.cmOn('onTaskManagerFormTaskGenericStep3DeleteRowButtonClick', record);

										}
									})
								]
							})
						],

						plugins: [
							Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 1 })
						],

						store: Ext.create('Ext.data.Store', {
							model: 'CMDBuild.model.taskManager.generic.Context',
							data: []
						}),

						dockedItems: [
							Ext.create('Ext.toolbar.Toolbar', {
								dock: 'top',
								itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

								items: [
									Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
										scope: this,

										handler: function (buttons, e) {
											this.grid.getStore().insert(0, Ext.create('CMDBuild.model.taskManager.generic.Context'));
										}
									})
								]
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
