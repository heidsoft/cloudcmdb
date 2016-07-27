(function() {

	// Model used from Processes -> Task Manager tab
	Ext.define('CMDBuild.model.CMModelTasks.grid.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean'}
		]
	});

	/*
	 * Models for single tasks get proxy calls
	 */
	Ext.define('CMDBuild.model.CMModelTasks.singleTask.connector', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.constants.Proxy.ATTRIBUTE_MAPPING, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.CLASS_MAPPING, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.DATASOURCE_CONFIGURATION, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.DATASOURCE_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE_ERROR, type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.constants.Proxy.ATTACHMENTS_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.constants.Proxy.ATTACHMENTS_CATEGORY, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.FILTER_FROM_ADDRESS, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.FILTER_FUNCTION, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.FILTER_SUBJECT, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.FILTER_TYPE, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.INCOMING_FOLDER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.PARSING_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.constants.Proxy.PARSING_KEY_END, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.PARSING_KEY_INIT, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.PARSING_VALUE_END, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.PARSING_VALUE_INIT, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.PROCESSED_FOLDER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.REJECTED_FOLDER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.REJECT_NOT_MATCHING, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.event_asynchronous', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.event_synchronous', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.GROUPS, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.PHASE, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	/*
	 * Inner tasks models
	 */
	// Connector
		Ext.define('CMDBuild.model.CMModelTasks.connector.classLevel', { // Step 4 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.core.constants.Proxy.SOURCE_NAME, type: 'string' },
				{ name: CMDBuild.core.constants.Proxy.CREATE, type: 'boolean', defaultValue: true },
				{ name: CMDBuild.core.constants.Proxy.UPDATE, type: 'boolean', defaultValue: true },
				{ name: CMDBuild.core.constants.Proxy.DELETE, type: 'boolean', defaultValue: true },
				{ name: CMDBuild.core.constants.Proxy.DELETE_TYPE, type: 'string' }
			]
		});

		Ext.define('CMDBuild.model.CMModelTasks.connector.attributeLevel', { // Step 5 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.core.constants.Proxy.CLASS_ATTRIBUTE, type: 'string' },
				{ name: CMDBuild.core.constants.Proxy.SOURCE_NAME, type: 'string' },
				{ name: CMDBuild.core.constants.Proxy.SOURCE_ATTRIBUTE, type: 'string' },
				{ name: CMDBuild.core.constants.Proxy.IS_KEY, type: 'boolean' }
			]
		});

		Ext.define('CMDBuild.model.CMModelTasks.connector.referenceLevel', { // Step 6 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.core.constants.Proxy.DOMAIN_NAME, type: 'string' }
			]
		});

	// Workflow form
		Ext.define('CMDBuild.model.CMModelTasks.common.workflowForm', {
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
				{ name: CMDBuild.core.constants.Proxy.VALUE, type: 'string' }
			]
		});

})();