(function () {

	Ext.define('CMDBuild.controller.administration.workflow.tabs.Properties', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.workflow.Workflow',
			'CMDBuild.proxy.workflow.Xpdl',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.Workflow}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWorkflowTabPropertiesAbortButtonClick',
			'onWorkflowTabPropertiesAddWorkflowButtonClick',
			'onWorkflowTabPropertiesDownloadXpdlPanelDownloadButtonClick',
			'onWorkflowTabPropertiesModifyButtonClick',
			'onWorkflowTabPropertiesPrintButtonClick = onButtonPrintClick',
			'onWorkflowTabPropertiesRemoveButtonClick',
			'onWorkflowTabPropertiesSaveButtonClick',
			'onWorkflowTabPropertiesShow',
			'onWorkflowTabPropertiesUploadXpdlPanelUploadButtonClick',
			'workflowTabPropertiesInit = workflowTabInit'
		],

		/**
		 * @property {CMDBuild.view.administration.workflow.tabs.properties.panel.DownloadXpdl}
		 */
		downloadXpdlPanel: undefined,

		/**
		 * @property {CMDBuild.view.administration.workflow.tabs.properties.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.workflow.tabs.properties.panel.BaseProperties}
		 */
		propertiesPanel: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.workflow.tabs.properties.PropertiesView}
		 */
		view: undefined,

		/**
		 * @property {CMDBuild.view.administration.workflow.tabs.properties.panel.UploadXpdl}
		 */
		uploadXpdlPanel: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.workflow.Workflow} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.workflow.tabs.properties.PropertiesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.propertiesPanel = this.view.form.propertiesPanel;
			this.downloadXpdlPanel = this.view.form.downloadXpdlPanel;
			this.uploadXpdlPanel = this.view.form.uploadXpdlPanel;
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabPropertiesAbortButtonClick: function () {
			if (!this.cmfg('workflowSelectedWorkflowIsEmpty')) {
				this.cmfg('onWorkflowTabPropertiesShow');
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabPropertiesAddWorkflowButtonClick: function () {
			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.workflow.Workflow'));

			// Set parent default selection
			this.propertiesPanel.parentCombo.getStore().load({
				scope: this,
				callback: function (records, operation, success) {
					this.propertiesPanel.parentCombo.getStore().each(function (record) {
						if (record.get(CMDBuild.core.constants.Proxy.NAME) == 'Activity') {
							this.propertiesPanel.parentCombo.setValue(record);

							return false;
						}
					}, this);
				}
			});

			this.downloadXpdlPanel.setDisabledModify(true);
			this.uploadXpdlPanel.setDisabledModify(true);
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabPropertiesDownloadXpdlPanelDownloadButtonClick: function () {
			var formData = this.downloadXpdlPanel.getData();

			if (Ext.isEmpty(formData[CMDBuild.core.constants.Proxy.VERSION])) {
				var params = {};
				params['idClass'] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.workflow.Xpdl.downloadTemplate({ params: params });
			} else {
				var params = {};
				params['idClass'] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.VERSION] = formData[CMDBuild.core.constants.Proxy.VERSION];

				CMDBuild.proxy.workflow.Xpdl.download({ params: params });
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabPropertiesModifyButtonClick: function () {
			this.form.setDisabledModify(false);
			this.downloadXpdlPanel.setDisabledModify(true);
			this.uploadXpdlPanel.setDisabledModify(true);
		},

		/**
		 * @param {String} format
		 *
		 * @returns {Void}
		 */
		onWorkflowTabPropertiesPrintButtonClick: function (format) {
			if (!Ext.isEmpty(format)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.FORMAT] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					parentDelegate: this,
					format: format,
					mode: 'classSchema',
					parameters: params
				});
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabPropertiesRemoveButtonClick: function () {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabPropertiesSaveButtonClick: function () {
			if (this.validate(this.propertiesPanel)) {
				var formDataModel = Ext.create('CMDBuild.model.workflow.Workflow', this.propertiesPanel.getData(true));

				var params = formDataModel.getData();
				params['inherits'] = params[CMDBuild.core.constants.Proxy.PARENT];
				params['isprocess'] = false;
				params['superclass'] = params[CMDBuild.core.constants.Proxy.IS_SUPER_CLASS];
				params['userstoppable'] = params[CMDBuild.core.constants.Proxy.USER_STOPPABLE];

				if (Ext.isEmpty(formDataModel.get(CMDBuild.core.constants.Proxy.ID))) {
					params[CMDBuild.core.constants.Proxy.FORCE_CREATION] = true;

					CMDBuild.proxy.workflow.Workflow.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.workflow.Workflow.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabPropertiesShow: function () {
			this.form.reset();
			this.form.setDisabledModify(true, true);
			this.form.loadRecord(this.cmfg('workflowSelectedWorkflowGet'));

			// Disable the XPDL fields if the process is a superclass
			this.downloadXpdlPanel.setDisabledModify(this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.IS_SUPER_CLASS));
			this.uploadXpdlPanel.setDisabledModify(this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.IS_SUPER_CLASS));

			if (!this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.IS_SUPER_CLASS)) {
				var params = {};
				params['idClass'] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.workflow.Xpdl.readVersions({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						var storeData = [];

						// Add original template version
						var dataObject = {};
						dataObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = CMDBuild.core.constants.Proxy.TEMPLATE;
						dataObject[CMDBuild.core.constants.Proxy.ID] = null;
						dataObject[CMDBuild.core.constants.Proxy.INDEX] = 0;

						storeData.push(dataObject);

						// Add XPDL versions to the begin of array
						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse))
							Ext.Array.forEach(decodedResponse, function (version, i, allValues) {
								var dataObject = {};
								dataObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = version;
								dataObject[CMDBuild.core.constants.Proxy.ID] = version;
								dataObject[CMDBuild.core.constants.Proxy.INDEX] = version;

								storeData.unshift(dataObject); // Add to the begin of array
							}, this);


						this.downloadXpdlPanel.versionCombo.getStore().removeAll();
						this.downloadXpdlPanel.versionCombo.getStore().loadData(storeData);
						this.downloadXpdlPanel.versionCombo.select( // FIX: autoSelect seems to have some problems probably because it's a fake store
							this.downloadXpdlPanel.versionCombo.getStore().getAt(0)
						);
					}
				});
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabPropertiesUploadXpdlPanelUploadButtonClick: function () {
			var params = {};
			params['idClass'] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.ID);

			CMDBuild.proxy.workflow.Xpdl.upload({
				form: this.uploadXpdlPanel.getForm(),
				params: params,
				scope: this,
				failure: function (response, options, decodedResponse) {
					CMDBuild.core.Message.error(
						CMDBuild.Translation.common.failure,
						CMDBuild.Translation.errorWhileUploadingXpdlFile,
						true
					);
				},
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse))
						CMDBuild.core.Message.info(
							CMDBuild.Translation.common.success,
							CMDBuild.Translation.xpdlFileUploaded
						);
				}
			});
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		removeItem: function () {
			if (!this.cmfg('workflowSelectedWorkflowIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.workflow.Workflow.remove({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.cmfg('workflowSelectedWorkflowReset');

						this.form.reset();
						this.form.setDisabledModify(true, true, true, true);

						this.cmfg('mainViewportAccordionDeselect', CMDBuild.core.constants.ModuleIdentifiers.getWorkflow());
						this.cmfg('mainViewportAccordionControllerUpdateStore', { identifier: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow() });

						CMDBuild.core.Message.success();
					}
				});
			}
		},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		success: function (response, options, decodedResponse) {
			decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.TABLE] || [];

			var formDataModel = Ext.create('CMDBuild.model.workflow.Workflow', this.propertiesPanel.getData(true));

			CMDBuild.view.common.field.translatable.Utils.commit(this.propertiesPanel);

			this.cmfg('mainViewportAccordionControllerUpdateStore', {
				identifier: CMDBuild.core.constants.ModuleIdentifiers.getWorkflow(),
				nodeIdToSelect: formDataModel.get(CMDBuild.core.constants.Proxy.ID) || decodedResponse[CMDBuild.core.constants.Proxy.ID]
			});

			this.cmfg('onWorkflowTabPropertiesShow');

			CMDBuild.core.Message.success();
		},

		/**
		 * Enable/Disable tab on workflow selection
		 *
		 * @returns {Void}
		 */
		workflowTabPropertiesInit: function () {
			this.view.setDisabled(this.cmfg('workflowSelectedWorkflowIsEmpty'));
		}
	});

})();
