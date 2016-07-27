(function () {

	/**
	 * Classes specific email tab controller
	 */
	Ext.define('CMDBuild.controller.management.classes.tabs.Email', {
		extend: 'CMDBuild.controller.management.common.tabs.email.Email',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.tabs.email.Email'
		],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		/**
		 * @property {Ext.data.Model}
		 */
		card: undefined,

		/**
		 * @property {CMDBuild.state.CMCardModuleStateDelegate}
		 */
		cardStateDelegate: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.classes.CMModCardController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		entryType: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.EmailView}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {Mixed} configObject.parentDelegate - CMModCardController
		 */
		constructor: function(configObject) {
			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.EmailView', { delegate: this });
			this.view.add(this.grid);

			this.buildCardModuleStateDelegate();
		},

		/**
		 * @private
		 */
		buildCardModuleStateDelegate: function() {
			var me = this;

			this.cardStateDelegate = new CMDBuild.state.CMCardModuleStateDelegate();

			this.cardStateDelegate.onEntryTypeDidChange = function(state, entryType) {
				me.onEntryTypeSelected(entryType);
			};

			this.cardStateDelegate.onCardDidChange = function(state, card) {
				Ext.suspendLayouts();
				me.onCardSelected(card);
				Ext.resumeLayouts();
			};

			_CMCardModuleState.addDelegate(this.cardStateDelegate);

			if (!Ext.isEmpty(this.view))
				this.mon(this.view, 'destroy', function(view) {
					_CMCardModuleState.removeDelegate(me.cardStateDelegate);

					delete this.cardStateDelegate;
				}, this);
		},

		onAbortCardClick: function() {
			this.cmfg('tabEmailEditModeSet', false);
			this.cmfg('tabEmailConfigurationReset');
		},

		/**
		 * @param {Ext.data.Model} card
		 */
		onCardSelected: function(card) {
			if (!Ext.isEmpty(card)) {
				this.card = card;

				this.cmfg('tabEmailConfigurationReset');
				this.cmfg('tabEmailConfigurationSet', {
					propertyName: CMDBuild.core.constants.Proxy.READ_ONLY,
					value: false
				});
				this.cmfg('tabEmailEditModeSet', false);
				this.cmfg('tabEmailSelectedEntitySet', {
					selectedEntity: this.card,
					scope: this,
					callbackFunction: function(options, success, response) {
						this.cmfg('tabEmailRegenerateAllEmailsSet', Ext.isEmpty(this.card));
						this.forceRegenerationSet(Ext.isEmpty(this.card));
						this.cmfg('onTabEmailPanelShow');
					}
				});
			}
		},

		onCloneCard: function() {
			this.card = null;

			this.cmfg('tabEmailConfigurationSet', {
				propertyName: CMDBuild.core.constants.Proxy.READ_ONLY,
				value: false
			});

			this.cmfg('tabEmailEditModeSet', true);

			this.cmfg('tabEmailSelectedEntitySet', {
				selectedEntity: this.card,
				scope: this,
				callbackFunction: function(options, success, response) {
					this.cmfg('tabEmailRegenerateAllEmailsSet', Ext.isEmpty(this.card));
					this.forceRegenerationSet(Ext.isEmpty(this.card));
					this.cmfg('onTabEmailPanelShow');
				}
			});
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 * @param {Object} dc
		 * @param {Object} filter
		 */
		onEntryTypeSelected: function(entryType, dc, filter) {
			this.entryType = entryType;

			this.cmfg('tabEmailConfigurationSet', {
				propertyName: CMDBuild.core.constants.Proxy.READ_ONLY,
				value: false
			});

			this.cmfg('tabEmailEditModeSet', false);
		},

		/**
		 * Works in place of ManageEmail widget for Workflows
		 *
		 * @override
		 */
		onModifyCardClick: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.card.get('IdClass'));
			params[CMDBuild.core.constants.Proxy.CARD_ID] = this.card.get(CMDBuild.core.constants.Proxy.ID);

			CMDBuild.proxy.common.tabs.email.Email.isEmailEnabledForCard({
				params: params,
				scope: this,
				loadMask: true,
				success: function(response, options, decodedResponse) {
					this.cmfg('tabEmailConfigurationSet', {
						propertyName: CMDBuild.core.constants.Proxy.READ_ONLY,
						value: !decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE]
					});

					this.cmfg('tabEmailEditModeSet', decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE]);

					if (!this.grid.getStore().isLoading())
						this.cmfg('onTabEmailGlobalRegenerationButtonClick');
				}
			});
		},

		/**
		 * Launch regeneration on save button click and send all draft emails
		 */
		onSaveCardClick: function() {
			this.cmfg('tabEmailSendAllOnSaveSet', true);

			if (!this.grid.getStore().isLoading()) {
				this.cmfg('tabEmailRegenerateAllEmailsSet', true);
				this.cmfg('onTabEmailPanelShow');
			}
		},

		/**
		 * @override
		 */
		onTabEmailPanelShow: function() {
			if (this.view.isVisible()) {
				// History record save
				if (!Ext.isEmpty(_CMCardModuleState.entryType) && !Ext.isEmpty(_CMCardModuleState.card))
					CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
						moduleId: 'class',
						entryType: {
							description: _CMCardModuleState.entryType.get(CMDBuild.core.constants.Proxy.TEXT),
							id: _CMCardModuleState.entryType.get(CMDBuild.core.constants.Proxy.ID),
							object: _CMCardModuleState.entryType
						},
						item: {
							description: _CMCardModuleState.card.get('Description') || _CMCardModuleState.card.get('Code'),
							id: _CMCardModuleState.card.get(CMDBuild.core.constants.Proxy.ID),
							object: _CMCardModuleState.card
						},
						section: {
							description: this.view.title,
							object: this.view
						}
					});
			}

			this.callParent(arguments);
		}
	});

})();