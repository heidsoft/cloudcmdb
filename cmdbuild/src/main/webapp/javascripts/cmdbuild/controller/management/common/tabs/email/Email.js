(function () {

	/**
	 * Main controller which manage email regeneration methods
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.management.common.tabs.email.Email', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.controller.management.common.widgets.CMWidgetController',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.Utils',
			'CMDBuild.proxy.email.Template',
			'CMDBuild.core.Message'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Boolean}
		 *
		 * @private
		 */
		busyState: false,

		/**
		 * Form where to get fields data
		 *
		 * @cfg {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTabEmailGlobalRegenerationButtonClick',
			'onModifyCardClick',
			'onTabEmailPanelShow',
			'tabEmailBindLocalDepsChangeEvent',
			'tabEmailBusyStateGet',
			'tabEmailBusyStateSet',
			'tabEmailConfigurationGet',
			'tabEmailConfigurationReset',
			'tabEmailConfigurationSet',
			'tabEmailEditModeGet',
			'tabEmailEditModeSet',
			'tabEmailGetAllTemplatesData',
			'tabEmailGetFormForTemplateResolver',
			'tabEmailGlobalLoadMaskGet',
			'tabEmailGlobalLoadMaskSet',
			'tabEmailRegenerateAllEmailsSet',
			'tabEmailRegenerateSelectedEmails',
			'tabEmailRegenerationEndPointCallbackReset',
			'tabEmailRegenerationEndPointCallbackSet',
			'tabEmailSelectedEntityGet',
			'tabEmailSelectedEntityInit',
			'tabEmailSelectedEntityIsEmpty',
			'tabEmailSelectedEntitySet',
			'tabEmailSendAllOnSaveSet'
		],

		/**
		 * @property {CMDBuild.model.common.tabs.email.Configuration}
		 *
		 * @private
		 */
		configuration: {},

		/**
		 * @property {CMDBuild.controller.management.common.tabs.email.Grid}
		 */
		controllerGrid: undefined,

		/**
		 * @property {Boolean}
		 *
		 * @private
		 */
		editMode: false,

		/**
		 * All templates I have in configuration and grid
		 *
		 * @property {Array}
		 */
		emailTemplatesObjects: [],

		/**
		 * All templates identifiers I have in configuration and grid
		 *
		 * @property {Array}
		 */
		emailTemplatesIdentifiers: [],

		/**
		 * @cfg {Boolean}
		 */
		flagForceRegeneration: false,

		/**
		 * @cfg {Boolean}
		 */
		flagRegenerateAllEmails: false,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.GridPanel}
		 */
		grid: undefined,

		/**
		 * @cfg {Boolean}
		 *
		 * @private
		 */
		globalLoadMask: true,

		/**
		 * Defines a function executed on regeneration end-point, works also as flagSave
		 *
		 * @cfg {CMDBuild.model.common.tabs.email.RegenerationEndPointCallback}
		 *
		 * @private
		 */
		regenerationEndPointCallback: undefined,

		/**
		 * Global attribute change flag
		 *
		 * @cfg {Boolean}
		 */
		relatedAttributeChanged: false,

		/**
		 * Actually selected Card/Activity
		 *
		 * @cfg {CMDBuild.model.common.tabs.email.SelectedEntity}
		 *
		 * @private
		 */
		selectedEntity: undefined,

		/**
		 * If true send all draft emails on save action
		 *
		 * @cfg {Boolean}
		 *
		 * @private
		 */
		sendAllOnSave: false,

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.EmailView}
		 */
		view: undefined,

		statics: {
			/**
			 * Searches for CQL variables resolved by client
			 *
			 * @param {String} inspectingVariable - variable where to check presence of CQL variables
			 * @param {Mixed} inspectingVariableKey - identifier of inspecting variable
			 * @param {Array} searchedVariablesNames - searched variables names
			 * @param {Array} foundedKeysArray - where to push keys of variables witch contains CQL
			 *
			 * @returns {Boolean} found
			 */
			searchForCqlClientVariables: function (inspectingVariable, inspectingVariableKey, searchedVariablesNames, foundedKeysArray) {
				var found = false;
				var cqlTags = ['{client:', '{cql:', '{xa:', '{js:'];

				for (var y in searchedVariablesNames) {
					for (var i in cqlTags) {
						if (
							inspectingVariable.indexOf(cqlTags[i] + searchedVariablesNames[y]) > -1
							&& !Ext.Array.contains(foundedKeysArray, inspectingVariableKey)
						) {
							foundedKeysArray.push(inspectingVariableKey);
							found = true;
						}
					}
				}

				return found;
			},

			/**
			 * @param {Mixed} record
			 * @param {Array} regenerationTrafficLightArray
			 *
			 * @returns {Boolean} storeLoadEnabled
			 */
			trafficLightArrayCheck: function (record, regenerationTrafficLightArray) {
				if (!Ext.isEmpty(regenerationTrafficLightArray) && Ext.isArray(regenerationTrafficLightArray)) {
					var storeLoadEnabled = true;

					Ext.Array.forEach(regenerationTrafficLightArray, function (item, i, allItems) {
						if (Ext.Object.equals(item[CMDBuild.core.constants.Proxy.RECORD], record))
							item[CMDBuild.core.constants.Proxy.STATUS] = true;

						if (!item[CMDBuild.core.constants.Proxy.STATUS])
							storeLoadEnabled = false;
					}, this);

					// Array reset on store load
					if (storeLoadEnabled)
						regenerationTrafficLightArray = [];

					return storeLoadEnabled;
				}

				return true;
			},

			/**
			 * @param {Mixed} record
			 * @param {Array} trafficLightArray
			 */
			trafficLightSlotBuild: function (record, trafficLightArray) {
				if (!Ext.isEmpty(record) && Ext.isArray(trafficLightArray)) {
					var trafficLight = {};
					trafficLight[CMDBuild.core.constants.Proxy.STATUS] = false;
					trafficLight[CMDBuild.core.constants.Proxy.RECORD] = record; // Reference to record

					trafficLightArray.push(trafficLight);
				}
			}
		},

		/**
		 * Abstract constructor that must be extended implementing creation and setup of view
		 *
		 * @param {Object} configurationObject
		 * @param {Object} configurationObject.parentDelegate - CMModCardController or CMModWorkflowController
		 *
		 * @abstract
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.cmfg('tabEmailConfigurationReset');

			// Build controllers
			this.controllerGrid = Ext.create('CMDBuild.controller.management.common.tabs.email.Grid', { parentDelegate: this });
			this.grid = this.controllerGrid.getView();

			this.controllerConfirmRegenerationWindow = Ext.create('CMDBuild.controller.management.common.tabs.email.ConfirmRegenerationWindow', {
				parentDelegate: this,
				gridDelegate: this.controllerGrid
			});

			this.cmfg('tabEmailSelectedEntityInit'); // Setup empty object by default

			// Extends to create view
		},

		/**
		 * @param {Array} data
		 * @param {CMDBuild.Management.TemplateResolver} templateResolver
		 *
		 * @returns {Boolean}
		 */
		checkCondition: function (data, templateResolver) {
			var conditionExpr = data[CMDBuild.core.constants.Proxy.CONDITION];

			return Ext.isEmpty(conditionExpr) || templateResolver.safeJSEval(conditionExpr);
		},

		/**
		 * Builds templatesToRegenerate array in relation of dirty fields
		 *
		 * @returns {Array} templatesToRegenerate
		 */
		checkTemplatesToRegenerate: function () {
			var templatesToRegenerate = [];
			var clientForm = this.cmfg('tabEmailGetFormForTemplateResolver');
			var dirtyVariables = Ext.Object.getKeys(clientForm.getValues(false, true));
			var xaVars = this.extractVariablesForTemplateResolver();

			clientForm.owner.initValues(); // Clear form fields dirty state to reset state after regeneration

			// Complete dirtyVariables array also with multilevel variables (ex. var1 = '... {client:var2} ...')
			for (var i in xaVars) {
				var variable = xaVars[i] || [];

				if (
					!Ext.isEmpty(variable)
					&& !Ext.isObject(variable)
					&& Ext.isString(variable)
				) {
					CMDBuild.controller.management.common.tabs.email.Email.searchForCqlClientVariables(
						variable,
						i,
						dirtyVariables,
						dirtyVariables
					);
				}
			}

			// Check templates attributes looking for dirtyVariables as client variables (ex. {client:varName})
			Ext.Array.forEach(this.emailTemplatesObjects, function (template, templateIndex, allTemplatesItems) {
				if (!Ext.Object.isEmpty(template))
					var mergedTemplate = Ext.apply(template.getData(), template.get(CMDBuild.core.constants.Proxy.VARIABLES));

					Ext.Object.each(mergedTemplate, function (key, value, myself) {
						if (Ext.isString(value)) { // Check all types of CQL variables that can contains client variables
							CMDBuild.controller.management.common.tabs.email.Email.searchForCqlClientVariables(
								value,
								mergedTemplate[CMDBuild.core.constants.Proxy.KEY] || mergedTemplate[CMDBuild.core.constants.Proxy.NAME],
								dirtyVariables,
								templatesToRegenerate
							);
						}
					}, this);
			}, this);

			return templatesToRegenerate;
		},

		/**
		 * Extract the variables of each EmailTemplate object, add a suffix to them with the index, and put them all in the templates map.
		 * This is needed to be passed as a unique map to the template resolver.
		 *
		 * @returns {Object} variables
		 */
		extractVariablesForTemplateResolver: function () {
			var variables = {};

			Ext.Array.forEach(this.emailTemplatesObjects, function (item, i, allItems) {
				var templateObject = item.getData();
				var templateVariables = item.get(CMDBuild.core.constants.Proxy.VARIABLES);

				for (var key in templateVariables)
					variables[key] = templateVariables[key];

				for (var key in templateObject)
					variables[key + (i + 1)] = templateObject[key];
			}, this);

			return variables;
		},

		// ForceRegeneration property functions
			/**
			 * @returns {Boolean}
			 */
			forceRegenerationGet: function () {
				return this.flagForceRegeneration;
			},

			/**
			 * @param {Boolean} mode
			 */
			forceRegenerationSet: function (mode) {
				this.flagForceRegeneration = Ext.isBoolean(mode) ? mode : false;
			},

		/**
		 * @abstract
		 */
		onAbortCardClick: Ext.emptyFn,

		/**
		 * Called from parent super controller
		 */
		onAddCardButtonClick: function () {
			this.cmfg('tabEmailEditModeSet', false);
		},

		onTabEmailGlobalRegenerationButtonClick: function () {
			this.cmfg('tabEmailRegenerateAllEmailsSet', true);
			this.forceRegenerationSet(true);
			this.controllerGrid.cmfg('tabEmailGridStoreLoad');
		},

		/**
		 * Base implementation to force email regeneration and editMode setup
		 */
		onModifyCardClick: function () {
			this.cmfg('tabEmailEditModeSet', false);

			if (!this.grid.getStore().isLoading())
				this.cmfg('onTabEmailGlobalRegenerationButtonClick');
		},

		/**
		 * Reload store every time panel is showed
		 */
		onTabEmailPanelShow: function () {
			this.view.setDisabled(
				this.cmfg('tabEmailSelectedEntityIsEmpty', CMDBuild.core.constants.Proxy.ENTITY)
				&& !this.cmfg('tabEmailEditModeGet') // Evaluate also editMode to enable onAddCardButtonClick
			);

			if (this.view.isVisible()) {
				this.controllerGrid.cmfg('tabEmailGridUiStateSet');

				// Regenerate all email only if editMode otherwise simple store load
				this.cmfg('tabEmailRegenerateAllEmailsSet', this.cmfg('tabEmailEditModeGet'));
				this.controllerGrid.cmfg('tabEmailGridStoreLoad');

				// Fire show event to manage buttons setup
				this.grid.buttonAdd.fireEvent('show');
				this.grid.buttonRegenerate.fireEvent('show');
			}
		},

		/**
		 * @param {Mixed} record
		 * @param {Array} regenerationTrafficLightArray
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		regenerateEmail: function (record, regenerationTrafficLightArray) {
			if (
				!Ext.Object.isEmpty(record)
				&& Ext.isArray(regenerationTrafficLightArray)
				&& !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.TEMPLATE))
				&& record.get(CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION)
			) {
				// Find record template in emailTemplatesObjects
				var recordTemplate = record.get(CMDBuild.core.constants.Proxy.TEMPLATE);
				recordTemplate = Ext.Array.findBy(this.emailTemplatesObjects, function (item, i) {
					if (
						recordTemplate == item.get(CMDBuild.core.constants.Proxy.KEY)
						|| recordTemplate == item.get(CMDBuild.core.constants.Proxy.NAME)
					) {
						return true;
					}

					return false;
				}, this);

				if (!Ext.isEmpty(recordTemplate)) {
					var templateData = Ext.apply({}, recordTemplate.getData(), recordTemplate.get(CMDBuild.core.constants.Proxy.VARIABLES));
					var xaVars = Ext.apply({}, templateData, record.getData());

					var templateResolver = new CMDBuild.Management.TemplateResolver({
						clientForm: this.cmfg('tabEmailGetFormForTemplateResolver'),
						xaVars: xaVars,
						serverVars: CMDBuild.controller.management.common.widgets.CMWidgetController.getTemplateResolverServerVars(
							this.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ENTITY)
						)
					});

					templateResolver.resolveTemplates({
						attributes: Ext.Object.getKeys(xaVars),
						scope: this,
						callback: function (values, ctx) {
							for (var key in values)
								record.set(key, values[key]);

							if (this.checkCondition(values, templateResolver)) {
								_msg('Email with subject "' + values[CMDBuild.core.constants.Proxy.SUBJECT] + '" regenerated');

								CMDBuild.controller.management.common.tabs.email.Email.trafficLightSlotBuild(record, regenerationTrafficLightArray);

								this.controllerGrid.cmfg('tabEmailGridRecordEdit', {
									record: record,
									regenerationTrafficLightArray: regenerationTrafficLightArray
								});
							} else {
								this.controllerGrid.cmfg('tabEmailGridRecordRemove', record);
							}

							this.cmfg('tabEmailBindLocalDepsChangeEvent',{
								record: record,
								templateResolver: templateResolver,
								scope: this
							});
						}
					});
				}
			}
		},

		/**
		 * @param {CMDBuild.model.common.tabs.email.Template} template
		 * @param {Array} regenerationTrafficLightArray
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		regenerateTemplate: function (template, regenerationTrafficLightArray) {
			if (
				!Ext.Object.isEmpty(template)
				&& Ext.isArray(regenerationTrafficLightArray)
			) {
				var xaVars = Ext.apply({}, template.getData(), template.get(CMDBuild.core.constants.Proxy.VARIABLES));

				var templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: this.cmfg('tabEmailGetFormForTemplateResolver'),
					xaVars: xaVars,
					serverVars: CMDBuild.controller.management.common.widgets.CMWidgetController.getTemplateResolverServerVars(
						this.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ENTITY)
					)
				});

				templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(xaVars),
					scope: this,
					callback: function (values, ctx) {
						var emailObject = null;

						// Find record witch has been created from this template
						var record = Ext.Array.findBy(this.controllerGrid.cmfg('tabEmailGridDraftEmailsGet'), function (item, i) {
							if (item.get(CMDBuild.core.constants.Proxy.TEMPLATE) == template.get(CMDBuild.core.constants.Proxy.KEY))
								return true;

							return false;
						});

						// Update record data with values
						if (!Ext.Object.isEmpty(record))
							values = Ext.Object.merge(record.getData(), values);

						emailObject = Ext.create('CMDBuild.model.common.tabs.email.Email', values);
						emailObject.set(CMDBuild.core.constants.Proxy.REFERENCE, this.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ID));
						emailObject.set(CMDBuild.core.constants.Proxy.TEMPLATE, template.get(CMDBuild.core.constants.Proxy.KEY));
						emailObject.set(CMDBuild.core.constants.Proxy.TEMPORARY, this.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ID) < 0); // Setup temporary parameter

						if (this.checkCondition(values, templateResolver)) {
							_msg('Template with subject "' + values[CMDBuild.core.constants.Proxy.SUBJECT] + '" regenerated');

							CMDBuild.controller.management.common.tabs.email.Email.trafficLightSlotBuild(emailObject, regenerationTrafficLightArray);

							if (Ext.isEmpty(record)) {
								this.controllerGrid.cmfg('tabEmailGridRecordAdd', {
									record: emailObject,
									regenerationTrafficLightArray: regenerationTrafficLightArray
								});
							} else {
								this.controllerGrid.cmfg('tabEmailGridRecordEdit', {
									record: emailObject,
									regenerationTrafficLightArray: regenerationTrafficLightArray
								});
							}
						} else {
							this.controllerGrid.cmfg('tabEmailGridRecordRemove', record);
						}

						this.cmfg('tabEmailBindLocalDepsChangeEvent',{
							record: emailObject,
							templateResolver: templateResolver,
							scope: this
						});
					}
				});
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {Mixed} parameters.record
		 * @param {CMDBuild.Management.TemplateResolver} parameters.templateResolver
		 * @param {Object} parameters.scope
		 */
		tabEmailBindLocalDepsChangeEvent: function (parameters) {
			if (
				!Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters.record)
				&& !Ext.isEmpty(parameters.templateResolver)
			) {
				parameters.scope = Ext.isEmpty(parameters.scope) ? this : parameters.scope;

				parameters.templateResolver.bindLocalDepsChange(function (field) {
					if (
						Ext.isObject(parameters.record) && !Ext.Object.isEmpty(parameters.record)
						&& !this.relatedAttributeChanged
					) {
						this.relatedAttributeChanged = true;

						// FIXME: wrong conditions check, no sense to display a change warning message when keepSynch flag is disabled and you don't wanna synch email
						if (
							!parameters.record.get(CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION)
							&& !parameters.record.get(CMDBuild.core.constants.Proxy.PROMPT_SYNCHRONIZATION)
						) {
							CMDBuild.core.Message.warning(null, CMDBuild.Translation.warnings.emailTemplateRelatedAttributeEdited);
						}
					}
				}, parameters.scope);
			} else {
				_error('error on tabEmailBindLocalDepsChangeEvent() parameters', this, parameters);
			}
		},

		// BusyState property functions
			/**
			 * @returns {Boolean}
			 */
			tabEmailBusyStateGet: function () {
				return this.busyState;
			},

			/**
			 * @param {Boolean} state
			 */
			tabEmailBusyStateSet: function (state) {
				state = Ext.isBoolean(state) ? state : false;

				this.busyState = state;
			},

		// Configuration property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			tabEmailConfigurationGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			tabEmailConfigurationReset: function () {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.tabs.email.Configuration';
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';
				parameters[CMDBuild.core.constants.Proxy.VALUE] = {};

				this.propertyManageSet(parameters);
			},

			/**
			 * @param {Object} parameters
			 */
			tabEmailConfigurationSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.tabs.email.Configuration';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'configuration';

					this.propertyManageSet(parameters);
				}
			},

		// EditMode property functions
			/**
			 * @returns {Boolean}
			 */
			tabEmailEditModeGet: function () {
				return this.editMode;
			},

			/**
			 * @param {Boolean} state
			 */
			tabEmailEditModeSet: function (state) {
				state = Ext.isBoolean(state) ? state : false;

				this.editMode = state;
			},

		/**
		 * Adapter function waiting for widgetController refactor
		 */
		tabEmailGetFormForTemplateResolver: function () {
			if (!Ext.isEmpty(this.parentDelegate) && Ext.isFunction(this.parentDelegate.getFormForTemplateResolver))
				return this.parentDelegate.getFormForTemplateResolver();
		},

		tabEmailGetAllTemplatesData: function () {
			var templatesFromConfiguration = this.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.TEMPLATES);
			var gridsDraftEmail = this.controllerGrid.cmfg('tabEmailGridDraftEmailsGet');

			// Reset local storage arrays
			this.emailTemplatesObjects = [];
			this.emailTemplatesIdentifiers = [];

			// Loads configuration templates to local array and push key in emailTemplatesIdentifiers array
			if (!Ext.isEmpty(templatesFromConfiguration) && Ext.isArray(templatesFromConfiguration))
				Ext.Array.each(templatesFromConfiguration, function (template, i, allItems) {
					if (!Ext.isEmpty(template) && !Ext.Array.contains(this.emailTemplatesIdentifiers, template.get(CMDBuild.core.constants.Proxy.KEY))) {
						this.emailTemplatesObjects.push(template);
						this.emailTemplatesIdentifiers.push(template.get(CMDBuild.core.constants.Proxy.KEY));
					}
				}, this);

			// Load grid's draft templates names to local array
			if (!Ext.isEmpty(gridsDraftEmail) && Ext.isArray(gridsDraftEmail))
				Ext.Array.each(gridsDraftEmail, function (record, i, allItems) {
					var templateIdentifier = null;
					var template = record.get(CMDBuild.core.constants.Proxy.TEMPLATE);

					if (Ext.isObject(template)) {
						templateIdentifier = template.get(CMDBuild.core.constants.Proxy.KEY) || template.get(CMDBuild.core.constants.Proxy.NAME);
					} else if (!Ext.isEmpty(template)) {
						templateIdentifier = template;
					}

					if (!Ext.isEmpty(templateIdentifier) && !Ext.Array.contains(this.emailTemplatesIdentifiers, templateIdentifier))
						this.emailTemplatesIdentifiers.push(templateIdentifier);
				}, this);

			var params = {};
			params[CMDBuild.core.constants.Proxy.TEMPLATES] = Ext.encode(this.emailTemplatesIdentifiers);

			CMDBuild.proxy.email.Template.readAll({
				params: params,
				loadMask: this.cmfg('tabEmailGlobalLoadMaskGet'),
				scope: this,
				failure: function (response, options, decodedResponse) {
					CMDBuild.core.Message.error(
						CMDBuild.Translation.common.failure,
						Ext.String.format(CMDBuild.Translation.errors.getTemplateWithNameFailure),
						false
					);
				},
				success: function (response, options, decodedResponse) {
					var templates = decodedResponse.response.elements;

					// Load grid's templates to local array
					Ext.Array.forEach(templates, function (template, i, allTemplates) {
						this.emailTemplatesObjects.push(Ext.create('CMDBuild.model.common.tabs.email.Template', template));
					}, this);
				},
				callback: function (options, success, response) {
					this.regenerateAllEmails();
				}
			});
		},

		// GlobalLoadMask property functions
			/**
			 * @returns {Boolean}
			 */
			tabEmailGlobalLoadMaskGet: function () {
				return this.globalLoadMask;
			},

			/**
			 * @param {Boolean} state
			 */
			tabEmailGlobalLoadMaskSet: function (state) {
				state = Ext.isBoolean(state) ? state : true;

				this.globalLoadMask = state;
			},

		// RegenerateAllEmails property functions
			/**
			 * Launch regeneration of all grid records if needed.
			 *
			 * {regenerationTrafficLightArray} Implements a trafficLight functionality to manage multiple asynchronous calls and have a global callback
			 * to reload grid only at real end of calls and avoid to have multiple and useless store load calls.
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			regenerateAllEmails: function () {
				var gridsDraftEmail = this.controllerGrid.cmfg('tabEmailGridDraftEmailsGet');
				var isRegenerationStarted = false; // Marks that regeneration process is started
				var templatesFromConfiguration = this.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.TEMPLATES);

				if (this.tabEmailRegenerateAllEmailsGet()) {
					var regenerationTrafficLightArray = [];

					this.controllerConfirmRegenerationWindow.reset();

					if (this.forceRegenerationGet() || this.relatedAttributeChanged) {
						var templatesCheckedForRegenerationIdentifiers = [];
						var emailTemplatesToRegenerate = this.checkTemplatesToRegenerate();

						// Build records to regenerate array
						if (!Ext.isEmpty(gridsDraftEmail) && Ext.isArray(gridsDraftEmail))
							Ext.Array.each(gridsDraftEmail, function (item, i, allItems) {
								var recordTemplate = item.get(CMDBuild.core.constants.Proxy.TEMPLATE);

								if (
									this.controllerGrid.cmfg('tabEmailGridRecordIsRegenerable', item)
									&& (
										Ext.Array.contains(emailTemplatesToRegenerate, recordTemplate)
										|| this.forceRegenerationGet()
									)
									&& item.get(CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION)
								) {
									if (item.get(CMDBuild.core.constants.Proxy.PROMPT_SYNCHRONIZATION) && !this.forceRegenerationGet()) { // PromptSynch implementation
										this.controllerConfirmRegenerationWindow.addRecordToArray(item);
									} else {
										isRegenerationStarted = true;

										this.regenerateEmail(item, regenerationTrafficLightArray);
									}
								}

								templatesCheckedForRegenerationIdentifiers.push(recordTemplate);
							}, this);

						// Build template to regenerate array
						if (!Ext.isEmpty(templatesFromConfiguration) && Ext.isArray(templatesFromConfiguration))
							Ext.Array.each(templatesFromConfiguration, function (item, i, allItems) {
								var templateIdentifier = item.get(CMDBuild.core.constants.Proxy.KEY);

								if (
									!Ext.isEmpty(templateIdentifier)
									&& (
										Ext.Array.contains(emailTemplatesToRegenerate, templateIdentifier)
										|| this.forceRegenerationGet()
									)
									&& !Ext.Array.contains(templatesCheckedForRegenerationIdentifiers, templateIdentifier) // Avoid to generate already regenerated templates
								) {
									if (item.get(CMDBuild.core.constants.Proxy.PROMPT_SYNCHRONIZATION) && !this.forceRegenerationGet()) { // PromptSynch implementation
										this.controllerConfirmRegenerationWindow.addTemplateToArray(item);
									} else {
										isRegenerationStarted = true;

										this.regenerateTemplate(item, regenerationTrafficLightArray);
									}
								}

								templatesCheckedForRegenerationIdentifiers.push(templateIdentifier);
							}, this);

						this.controllerConfirmRegenerationWindow.beforeShow();

						this.relatedAttributeChanged = false; // Reset attribute changed flag
						this.forceRegenerationSet(); // Reset force regeneration flag
					}

					this.cmfg('tabEmailRegenerateAllEmailsSet', false); // Reset regenerate all emails flag
				}

				// Set all email as outgoing on save card
				if (this.tabEmailSendAllOnSaveGet()) {
					this.cmfg('tabEmailSendAllOnSaveSet');

					this.controllerGrid.cmfg('tabEmailGridSendAll');
				} else if (
					!isRegenerationStarted
					&& Ext.isFunction(this.tabEmailRegenerationEndPointCallbackGet(CMDBuild.core.constants.Proxy.FUNCTION))
				) { // Executed if no regeneration was performed
					Ext.callback(
						this.tabEmailRegenerationEndPointCallbackGet(CMDBuild.core.constants.Proxy.FUNCTION),
						this.tabEmailRegenerationEndPointCallbackGet(CMDBuild.core.constants.Proxy.SCOPE)
					);
				}

				this.cmfg('tabEmailBusyStateSet', false); // Reset widget busy state to false
			},

			/**
			 * @returns {Boolean}
			 */
			tabEmailRegenerateAllEmailsGet: function () {
				return this.flagRegenerateAllEmails;
			},

			/**
			 * @param {Boolean} state
			 */
			tabEmailRegenerateAllEmailsSet: function (state) {
				state = Ext.isBoolean(state) ? state : false;

				this.flagRegenerateAllEmails = state;
			},

		/**
		 * Launch regeneration only of selected grid records
		 *
		 * {regenerationTrafficLightArray} Implements a trafficLight functionality to manage multiple asynchronous calls and have a global callback
		 * to reload grid only at real end of calls and avoid to have multiple and useless store load calls.
		 *
		 * @param {Array} records
		 */
		tabEmailRegenerateSelectedEmails: function (records) {
			if (!Ext.isEmpty(records) && Ext.isArray(records)) {
				var regenerationTrafficLightArray = [];

				Ext.Array.each(records, function (item, i, allItems) {
					var recordTemplate = item.get(CMDBuild.core.constants.Proxy.TEMPLATE);

					if (Ext.isEmpty(recordTemplate)) {
						this.regenerateEmail(item, regenerationTrafficLightArray);
					} else {
						this.regenerateTemplate(item, regenerationTrafficLightArray);
					}
				}, this);

				this.relatedAttributeChanged = false; // Reset attribute changed flag
			}
		},

		// RegenerationEndPointCallback property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			tabEmailRegenerationEndPointCallbackGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'regenerationEndPointCallback';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			tabEmailRegenerationEndPointCallbackReset: function () {
				this.propertyManageReset('regenerationEndPointCallback');
			},

			/**
			 * @param {Object} parameters
			 */
			tabEmailRegenerationEndPointCallbackSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.tabs.email.RegenerationEndPointCallback';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'regenerationEndPointCallback';

					this.propertyManageSet(parameters);
				}
			},

		// SelectedEntity property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			tabEmailSelectedEntityGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedEntity';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * Initialize selectedEntity object
			 *
			 * @param {Object} parameters
			 * @param {Object} parameters.scope
			 * @param {Function} parameters.callbackFunction
			 */
			tabEmailSelectedEntityInit: function (parameters) {
				parameters = Ext.Object.isEmpty(parameters) ? {} : parameters;
				parameters.callbackFunction = Ext.isFunction(parameters.callbackFunction) ? parameters.callbackFunction : Ext.emptyFn;
				parameters.scope = Ext.isEmpty(parameters.scope) ? parameters.scope : this;

				var params = {};
				params[CMDBuild.core.constants.Proxy.NOT_POSITIVES] = true;

				CMDBuild.proxy.Utils.generateId({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.tabs.email.SelectedEntity';
						parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedEntity';
						parameters[CMDBuild.core.constants.Proxy.VALUE] = { id: decodedResponse.response };

						this.propertyManageSet(parameters);
					},
					callback: parameters.callbackFunction
				});
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			tabEmailSelectedEntityIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedEntity';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * Creates SelectedEntity object and bind relative original object
			 *
			 * @param {Object} parameters
			 * @param {Mixed} parameters.selectedEntity
			 * @param {Object} parameters.scope
			 * @param {Function} parameters.callbackFunction
			 */
			tabEmailSelectedEntitySet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters.callbackFunction = Ext.isFunction(parameters.callbackFunction) ? parameters.callbackFunction : Ext.emptyFn;
					parameters.scope = Ext.isEmpty(parameters.scope) ? parameters.scope : this;

					if (Ext.isEmpty(parameters.selectedEntity)) {
						var params = {};
						params[CMDBuild.core.constants.Proxy.NOT_POSITIVES] = true;

						CMDBuild.proxy.Utils.generateId({
							params: params,
							scope: this,
							success: function (response, options, decodedResponse) {
								var serviceParams = {};
								serviceParams[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.tabs.email.SelectedEntity';
								serviceParams[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedEntity';
								serviceParams[CMDBuild.core.constants.Proxy.VALUE] = { id: decodedResponse.response };

								this.propertyManageSet(serviceParams);
							},
							callback: parameters.callbackFunction
						});
					} else if (Ext.isEmpty(parameters.selectedEntity.get(CMDBuild.core.constants.Proxy.ID))) {
						var params = {};
						params[CMDBuild.core.constants.Proxy.NOT_POSITIVES] = true;

						CMDBuild.proxy.Utils.generateId({
							params: params,
							scope: this,
							success: function (response, options, decodedResponse) {
								var serviceParams = {};
								serviceParams[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.tabs.email.SelectedEntity';
								serviceParams[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedEntity';
								serviceParams[CMDBuild.core.constants.Proxy.VALUE] = {
									id: decodedResponse.response,
									entity: parameters.selectedEntity
								};

								this.propertyManageSet(serviceParams);
							},
							callback: parameters.callbackFunction
						});
					} else {
						var serviceParams = {};
						serviceParams[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.tabs.email.SelectedEntity';
						serviceParams[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedEntity';
						serviceParams[CMDBuild.core.constants.Proxy.VALUE] = {
							id: parameters.selectedEntity.get(CMDBuild.core.constants.Proxy.ID),
							entity: parameters.selectedEntity
						};

						this.propertyManageSet(serviceParams);

						if (Ext.isFunction(parameters.callbackFunction))
							Ext.callback(parameters.callbackFunction, parameters.scope);
					}
				}
			},

		// SendAllOnSave property functions
			/**
			 * @returns {Boolean}
			 */
			tabEmailSendAllOnSaveGet: function () {
				return this.sendAllOnSave;
			},

			/**
			 * @param {Boolean} state
			 */
			tabEmailSendAllOnSaveSet: function (state) {
				state = Ext.isBoolean(state) ? state : false;

				this.sendAllOnSave = state;
			}
	});

})();
