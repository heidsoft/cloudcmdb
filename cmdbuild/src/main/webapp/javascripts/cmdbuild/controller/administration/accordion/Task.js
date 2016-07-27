(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Task', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'accordionBuildId',
			'accordionDeselect',
			'accordionExpand',
			'accordionFirstSelectableNodeSelect',
			'accordionFirtsSelectableNodeGet',
			'accordionIdentifierGet',
			'accordionNodeByIdExists',
			'accordionNodeByIdGet',
			'accordionNodeByIdSelect',
			'accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.Task}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Task', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		accordionUpdateStore: function (nodeIdToSelect) {
			this.view.getStore().getRootNode().removeAll();
			this.view.getStore().getRootNode().appendChild([
				{
					cmName: this.cmfg('accordionIdentifierGet'),
					iconCls: 'cmdb-tree-taskGroup-icon',
					text: CMDBuild.Translation.administration.tasks.all,
					description: CMDBuild.Translation.administration.tasks.all,
					id: this.cmfg('accordionBuildId', 'all'),
					sectionHierarchy: ['all'],
					leaf: false,

					children: [
						{
							cmName: this.cmfg('accordionIdentifierGet'),
							iconCls: 'cmdb-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.connector,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.connector,
							id: this.cmfg('accordionBuildId', 'connector'),
							sectionHierarchy: ['connector'],
							leaf: true
						},
						{
							cmName: this.cmfg('accordionIdentifierGet'),
							iconCls: 'cmdb-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.email,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.email,
							id: this.cmfg('accordionBuildId', 'email'),
							sectionHierarchy: ['email'],
							leaf: true
						},
						{
							cmName: this.cmfg('accordionIdentifierGet'),
							iconCls: 'cmdb-tree-taskGroup-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.event,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.event,
							expanded: true,
							id: this.cmfg('accordionBuildId', 'event'),
							sectionHierarchy: ['event'],
							leaf: false,

							children: [
								{
									cmName: this.cmfg('accordionIdentifierGet'),
									iconCls: 'cmdb-tree-tasks-icon',
									text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
									description: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.asynchronous,
									id: this.cmfg('accordionBuildId', 'event_asynchronous'),
									sectionHierarchy: ['event_asynchronous'], // TODO: use double level (event, asynchronous)
									leaf: true
								},
								{
									cmName: this.cmfg('accordionIdentifierGet'),
									iconCls: 'cmdb-tree-tasks-icon',
									text: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
									description: CMDBuild.Translation.administration.tasks.tasksTypes.eventTypes.synchronous,
									id: this.cmfg('accordionBuildId', 'event_synchronous'),
									sectionHierarchy: ['event_synchronous'], // TODO: use double level (event, synchronous)
									leaf: true
								}
							]
						},
						{
							cmName: this.cmfg('accordionIdentifierGet'),
							iconCls: 'cmdb-tree-tasks-icon',
							text: CMDBuild.Translation.administration.tasks.tasksTypes.workflow,
							description: CMDBuild.Translation.administration.tasks.tasksTypes.workflow,
							id: this.cmfg('accordionBuildId', 'workflow'),
							sectionHierarchy: ['workflow'],
							leaf: true
						},
						{
							cmName: this.cmfg('accordionIdentifierGet'),
							iconCls: 'cmdb-tree-taskGroup-icon',
							text: CMDBuild.Translation.others,
							description: CMDBuild.Translation.others,
							expanded: true,
							id: this.cmfg('accordionBuildId', 'others'),
							sectionHierarchy: ['generic'],
							leaf: false,

							children: [
								{
									cmName: this.cmfg('accordionIdentifierGet'),
									iconCls: 'cmdb-tree-tasks-icon',
									text: CMDBuild.Translation.sendEmail,
									description: CMDBuild.Translation.sendEmail,
									id: this.cmfg('accordionBuildId', 'generic'),
									sectionHierarchy: ['generic'],
									leaf: true
								}
							]
						}
					]
				}
			]);

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		}
	});

})();
