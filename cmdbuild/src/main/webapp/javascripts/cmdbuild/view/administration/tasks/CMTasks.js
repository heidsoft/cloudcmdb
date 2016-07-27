(function() {

	Ext.define('CMDBuild.view.administration.tasks.CMTasks', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.administration.tasks.title,

		/**
		 * @property {CMDBuild.view.administration.tasks.CMTasksForm}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.CMTasksGrid}
		 */
		grid: undefined,

		frame: false,
		border: true,
		layout: 'border',

		initComponent: function() {
			this.form = Ext.create('CMDBuild.view.administration.tasks.CMTasksForm', {
				region: 'center'
			});

			this.grid = Ext.create('CMDBuild.view.administration.tasks.CMTasksGrid', {
				region: 'north',
				split: true,
				height: '30%'
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
						items: []
					})
				],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * To show correct button in top toolbar
			 *
			 * @param {Ext.panel.Panel} panel
			 * @param {Object} eOpts
			 */
			show: function(panel, eOpts) {
				this.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_TOP).removeAll();

				switch (this.delegate.taskType) {
					case 'all': {
						this.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_TOP).add(
							Ext.create('Ext.button.Split', {
								iconCls: 'add',
								text: CMDBuild.Translation.administration.tasks.add,

								handler: function() {
									this.showMenu();
								},

								menu: Ext.create('Ext.menu.Menu', { // Rendered as dropdown menu on button click
									items: [
										{
											text: CMDBuild.Translation.administration.tasks.tasksTypes.connector,
											scope: this,

											handler: function(button, e) {
												this.delegate.cmOn('onAddButtonClick', { type: 'connector' });
											}
										},
										{
											text: CMDBuild.Translation.administration.tasks.tasksTypes.email,
											scope: this,

											handler: function(button, e) {
												this.delegate.cmOn('onAddButtonClick', { type: 'email' });
											}
										},
										{
											text: CMDBuild.Translation.administration.tasks.tasksTypes.event,
											menu: [
												{
													text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
													scope: this,

													handler: function(button, e) {
														this.delegate.cmOn('onAddButtonClick', { type: 'event_asynchronous' });
													}
												},
												{
													text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
													scope: this,

													handler: function(button, e) {
														this.delegate.cmOn('onAddButtonClick', { type: 'event_synchronous' });
													}
												}
											]
										},
										{
											text: CMDBuild.Translation.administration.tasks.tasksTypes.workflow,
											scope: this,

											handler: function(button, e) {
												this.delegate.cmOn('onAddButtonClick', { type: 'workflow' });
											}
										},
										{
											text: CMDBuild.Translation.others,
											menu: [
												{
													text: CMDBuild.Translation.sendEmail,
													scope: this,

													handler: function(button, e) {
														this.delegate.cmOn('onAddButtonClick', { type: 'generic' });
													}
												}
											]
										}
									]
								})
							})
						);
					} break;

					case 'event': {
						this.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_TOP).add(
							Ext.create('Ext.button.Split', {
								iconCls: 'add',
								text: CMDBuild.Translation.administration.tasks.add,

								handler: function() {
									this.showMenu();
								},

								menu: Ext.create('Ext.menu.Menu', { // Rendered as dropdown menu on button click
									items: [
										{
											text: CMDBuild.Translation.administration.tasks.tasksTypes.event,
											menu: [
												{
													text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
													scope: this,
													handler: function() {
														this.delegate.cmOn('onAddButtonClick', { type: 'event_asynchronous' });
													}
												},
												{
													text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
													scope: this,
													handler: function() {
														this.delegate.cmOn('onAddButtonClick', { type: 'event_synchronous' });
													}
												}
											]
										}
									]
								})
							})
						);
					} break;

					default: {
						this.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_TOP).add(
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
								text: CMDBuild.Translation.administration.tasks.add,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmOn('onAddButtonClick', { type: this.delegate.taskType });
								}
							})
						);
					}
				}
			}
		}
	});

})();