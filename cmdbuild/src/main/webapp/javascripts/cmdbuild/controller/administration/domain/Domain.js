(function() {

	Ext.define('CMDBuild.controller.administration.domain.Domain', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.domain.Domain',
			'CMDBuild.model.domain.Domain',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'domainSelectedDomainGet',
			'domainSelectedDomainIsEmpty',
			'domainSelectedDomainReset',
			'onDomainAbortButtonClick',
			'onDomainAddButtonClick',
			'onDomainModifyButtonClick',
			'onDomainModuleInit = onModuleInit',
			'onDomainRemoveButtonClick',
			'onDomainSaveButtonClick',
			'onDomainSelected -> controllerProperties, controllerEnabledClasses, controllerAttributes'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.controller.administration.domain.Attributes}
		 */
		controllerAttributes: undefined,

		/**
		 * @property {CMDBuild.controller.administration.domain.EnabledClasses}
		 */
		controllerEnabledClasses: undefined,

		/**
		 * @property {CMDBuild.controller.administration.domain.Properties}
		 */
		controllerProperties: undefined,

		/**
		 * @property {CMDBuild.model.domain.Domain}
		 */
		selectedDomain: null,

		/**
		 * @cfg {CMDBuild.view.administration.domain.DomainView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.domain.DomainView', { delegate: this });

			// Controller build
			this.controllerAttributes = Ext.create('CMDBuild.controller.administration.domain.Attributes', { parentDelegate: this });
			this.controllerEnabledClasses = Ext.create('CMDBuild.controller.administration.domain.EnabledClasses', { parentDelegate: this });
			this.controllerProperties = Ext.create('CMDBuild.controller.administration.domain.Properties', { parentDelegate: this });

			// Inject tabs
			this.view.tabPanel.add(this.controllerProperties.getView());
			this.view.tabPanel.add(this.controllerEnabledClasses.getView());
			this.view.tabPanel.add(this.controllerAttributes.getView());
		},

		/**
		 * Method forwarder
		 */
		onDomainAbortButtonClick: function() {
			this.controllerEnabledClasses.cmfg('onDomainEnabledClassesAbortButtonClick');
			this.controllerProperties.cmfg('onDomainPropertiesAbortButtonClick');
		},

		onDomainAddButtonClick: function() {
			this.cmfg('mainViewportAccordionDeselect', this.cmfg('identifierGet'));

			this.setViewTitle();

			this.controllerAttributes.cmfg('onDomainAddButtonClick');
			this.controllerEnabledClasses.cmfg('onDomainEnabledClassesAddButtonClick');
			this.controllerProperties.cmfg('onDomainPropertiesAddButtonClick');

			this.view.tabPanel.setActiveTab(0);
		},

		/**
		 * Method forwarder
		 */
		onDomainModifyButtonClick: function() {
			this.controllerEnabledClasses.cmfg('onDomainEnabledClassesModifyButtonClick');
			this.controllerProperties.cmfg('onDomainPropertiesModifyButtonClick');
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @override
		 */
		onDomainModuleInit: function(node) {
			if (!Ext.isEmpty(node)) {
				var params = {};

				CMDBuild.proxy.domain.Domain.read({ // TODO: waiting for refactor (crud)
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
							this.domainSelectedDomainSet({
								value: CMDBuild.model.domain.Domain.convertFromLegacy( // TODO: waiting for refactor (rename)
									Ext.Array.findBy(decodedResponse, function(item, i) {
										return node.get(CMDBuild.core.constants.Proxy.ENTITY_ID) == item[CMDBuild.core.constants.Proxy.ID_DOMAIN];
									}, this)
								)
							});

							this.cmfg('onDomainSelected');

							this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.TEXT));

							if (Ext.isEmpty(this.view.tabPanel.getActiveTab()))
								this.view.tabPanel.setActiveTab(0);
						}

						this.onModuleInit(node); // Custom callParent() implementation
					}
				});
			}
		},

		onDomainRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.removeDomain,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function(buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		onDomainSaveButtonClick: function() {
			if (this.validate(this.controllerProperties.getView().form)) {
				var params = Ext.create('CMDBuild.model.domain.Domain',
					Ext.Object.merge(
						this.controllerProperties.cmfg('domainPropertiesDataGet'),
						this.controllerEnabledClasses.cmfg('domainEnabledClassesDataGet')
					)
				).getDataForSubmit();

				if (Ext.isEmpty(params[CMDBuild.core.constants.Proxy.ID])) {
					CMDBuild.proxy.domain.Domain.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.domain.Domain.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		/**
		 * @private
		 */
		removeItem: function() {
			if (!this.cmfg('domainSelectedDomainIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.DOMAIN_NAME] = this.cmfg('domainSelectedDomainGet', CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.domain.Domain.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.controllerProperties.getView().form.reset();
						this.controllerProperties.getView().form.setDisabledModify(true);

						_CMCache.onDomainDeleted(this.cmfg('domainSelectedDomainGet', CMDBuild.core.constants.Proxy.ID));

						this.cmfg('mainViewportAccordionControllerUpdateStore', { identifier: this.cmfg('identifierGet') });

						CMDBuild.core.Message.success();
					}
				});
			}
		},

		/**
		 * @private
		 */
		success: function(response, options, decodedResponse) {
			this.view.tabPanel.setActiveTab(0);
			this.view.tabPanel.getActiveTab().form.setDisabledModify(true);

			_CMCache.onDomainSaved(decodedResponse.domain);

			this.cmfg('mainViewportAccordionControllerUpdateStore', {
				identifier: this.cmfg('identifierGet'),
				nodeIdToSelect: decodedResponse[CMDBuild.core.constants.Proxy.DOMAIN][CMDBuild.core.constants.Proxy.ID_DOMAIN]
			});

			CMDBuild.view.common.field.translatable.Utils.commit(this.controllerProperties.getView().form);

			CMDBuild.core.Message.success();
		},

		// SelectedDomain property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			domainSelectedDomainGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			domainSelectedDomainIsEmpty: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			domainSelectedDomainReset: function() {
				this.propertyManageReset('selectedDomain');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			domainSelectedDomainSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.domain.Domain';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';

					this.propertyManageSet(parameters);
				}
			}
	});

})();