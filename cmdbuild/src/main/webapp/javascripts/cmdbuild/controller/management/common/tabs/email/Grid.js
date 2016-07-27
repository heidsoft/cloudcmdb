(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.Grid', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.controller.management.common.tabs.email.Email',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.common.tabs.email.Email'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTabEmailGridAddEmailButtonClick',
			'onTabEmailGridDeleteEmailButtonClick',
			'onTabEmailGridEditEmailButtonClick',
			'onTabEmailGridItemDoubleClick',
			'onTabEmailGridRegenerationEmailButtonClick',
			'onTabEmailGridReplyEmailButtonClick',
			'onTabEmailGridSendEmailButtonClick',
			'onTabEmailGridViewEmailButtonClick',
			'tabEmailGridDraftEmailsGet',
			'tabEmailGridDraftEmailsIsEmpty',
			'tabEmailGridRecordAdd',
			'tabEmailGridRecordEdit',
			'tabEmailGridRecordIsEditable',
			'tabEmailGridRecordIsRegenerable',
			'tabEmailGridRecordIsSendable',
			'tabEmailGridRecordRemove',
			'tabEmailGridSendAll',
			'tabEmailGridStoreLoad',
			'tabEmailGridUiStateSet'
		],

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.GridPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.tabs.email.Email} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.tabs.email.GridPanel', { delegate: this });
		},

		/**
		 * @param {String} group
		 *
		 * @returns {Array}
		 *
		 * @private
		 */
		getEmailsByGroup: function (group) {
			var out = this.view.getStore().getGroups(group);

			if (out)
				out = out.children; // ExtJS mystic output { name: group, children: [...] }

			return out || [];
		},

		/**
		 * Creates a new email to generate an emailId
		 */
		onTabEmailGridAddEmailButtonClick: function () {
			var record = this.recordCreate();

			this.cmfg('tabEmailGridRecordAdd', {
				record: record,
				scope: this,
				success: function (response, options, decodedResponse) { // Success function override
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					record.set(CMDBuild.core.constants.Proxy.ID, decodedResponse);

					Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
						parentDelegate: this,
						record: record
					});

					this.cmfg('tabEmailGridStoreLoad');
				}
			});
		},

		/**
		 * @param {Mixed} record
		 */
		onTabEmailGridDeleteEmailButtonClick: function (record) {
			Ext.Msg.confirm(
				CMDBuild.Translation.common.confirmpopup.title,
				CMDBuild.Translation.common.confirmpopup.areyousure,
				function (btn) {
					if (btn == 'yes')
						this.cmfg('tabEmailGridRecordRemove', record);
				},
				this
			);
		},

		/**
		 * @param {Mixed} record
		 */
		onTabEmailGridEditEmailButtonClick: function (record) {
			Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
				parentDelegate: this,
				record: record,
				windowMode: 'edit'
			});
		},

		/**
		 * @param {Mixed} record
		 */
		onTabEmailGridItemDoubleClick: function (record) {
			if (
				!this.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)
				&& this.cmfg('tabEmailEditModeGet')
				&& this.cmfg('tabEmailGridRecordIsEditable', record)
			) {
				this.onTabEmailGridEditEmailButtonClick(record);
			} else {
				this.onTabEmailGridViewEmailButtonClick(record);
			}
		},

		/**
		 * @param {Mixed} record
		 */
		onTabEmailGridRegenerationEmailButtonClick: function (record) {
			if (!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.TEMPLATE)))
				this.cmfg('tabEmailRegenerateSelectedEmails', [record]);
		},

		/**
		 * Creates reply email object
		 *
		 * @param {Mixed} record
		 */
		onTabEmailGridReplyEmailButtonClick: function (record) {
			if (!Ext.Object.isEmpty(record)) {
				var content = '<p>'
					+ CMDBuild.Translation.onDay + ' ' + record.get(CMDBuild.core.constants.Proxy.DATE)
					+ ', <' + record.get(CMDBuild.core.constants.Proxy.FROM) + '> ' + CMDBuild.Translation.hasWrote
				+ ':</p>'
				+ '<blockquote>' + record.get(CMDBuild.core.constants.Proxy.BODY) + '</blockquote>';

				var replyRecordData = {};
				replyRecordData[CMDBuild.core.constants.Proxy.ACCOUNT] = record.get(CMDBuild.core.constants.Proxy.ACCOUNT);
				replyRecordData[CMDBuild.core.constants.Proxy.BCC] = record.get(CMDBuild.core.constants.Proxy.BCC);
				replyRecordData[CMDBuild.core.constants.Proxy.BODY] = content;
				replyRecordData[CMDBuild.core.constants.Proxy.CC] = record.get(CMDBuild.core.constants.Proxy.CC);
				replyRecordData[CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION] = false;
				replyRecordData[CMDBuild.core.constants.Proxy.NOTIFY_WITH] = record.get(CMDBuild.core.constants.Proxy.NOTIFY_WITH);
				replyRecordData[CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX] = record.get(CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX);
				replyRecordData[CMDBuild.core.constants.Proxy.REFERENCE] = this.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ID);
				replyRecordData[CMDBuild.core.constants.Proxy.SUBJECT] = 'RE: ' + record.get(CMDBuild.core.constants.Proxy.SUBJECT);
				replyRecordData[CMDBuild.core.constants.Proxy.TO] = record.get(CMDBuild.core.constants.Proxy.FROM) || record.get(CMDBuild.core.constants.Proxy.TO);

				var record = this.recordCreate(replyRecordData);

				this.cmfg('tabEmailGridRecordAdd', {
					record: record,
					scope: this,
					success: function (response, options, decodedResponse) { // Success function override
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						record.set(CMDBuild.core.constants.Proxy.ID, decodedResponse);

						Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
							parentDelegate: this,
							record: record,
							windowMode: 'reply'
						});

						this.cmfg('tabEmailGridStoreLoad');
					}
				});
			} else {
				_error('empty record parameter on reply button click', this);
			}
		},

		/**
		 * @param {Mixed} record
		 */
		onTabEmailGridSendEmailButtonClick: function (record) {
			this.recordSend(record);
		},

		/**
		 * @param {Mixed} record
		 */
		onTabEmailGridViewEmailButtonClick: function (record) {
			Ext.create('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
				parentDelegate: this,
				record: record,
				windowMode: 'view'
			});
		},

		/**
		 * Creates email model with default attributes setup
		 *
		 * @param {Object} recordValues
		 *
		 * @returns {Mixed}
		 *
		 * @private
		 */
		recordCreate: function (recordValues) {
			recordValues = Ext.Object.isEmpty(recordValues) ? {} : recordValues;
			recordValues[CMDBuild.core.constants.Proxy.KEEP_SYNCHRONIZATION] = false;
			recordValues[CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX] = recordValues.hasOwnProperty(CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX) ? recordValues[CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX] : this.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.NO_SUBJECT_PREFIX);
			recordValues[CMDBuild.core.constants.Proxy.REFERENCE] = this.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ID);
			recordValues[CMDBuild.core.constants.Proxy.TEMPORARY] = this.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ID) < 0; // Setup temporary parameter

			return Ext.create('CMDBuild.model.common.tabs.email.Email', recordValues);
		},

		/**
		 * Updates selected record with Outgoing status
		 *
		 * @param {Mixed} record
		 * @param {Array} trafficLightArray
		 *
		 * @private
		 */
		recordSend: function (record, trafficLightArray) {
			trafficLightArray = trafficLightArray || [];

			if (!Ext.isEmpty(record)) {
				record.set(CMDBuild.core.constants.Proxy.STATUS, CMDBuild.core.constants.Proxy.OUTGOING);

				this.cmfg('tabEmailGridRecordEdit', {
					record: record,
					regenerationTrafficLightArray: trafficLightArray
				});
			}
		},

		/**
		 * @returns {Array}
		 */
		tabEmailGridDraftEmailsGet: function () {
			return this.getEmailsByGroup(CMDBuild.core.constants.Proxy.DRAFT);
		},

		/**
		 * @returns {Boolean}
		 */
		tabEmailGridDraftEmailsIsEmpty: function () {
			return !Ext.isEmpty(this.cmfg('tabEmailGridDraftEmailsGet'));
		},

		/**
		 * @param {Object} parameters
		 * @param {Mixed} parameters.record
		 * @param {Array} parameters.regenerationTrafficLightArray
		 * @param {Object} parameters.scope
		 * @param {Function} parameters.success
		 */
		tabEmailGridRecordAdd: function (parameters) {
			if (!Ext.Object.isEmpty(parameters)) {
				parameters.regenerationTrafficLightArray = Ext.isArray(parameters.regenerationTrafficLightArray) ? parameters.regenerationTrafficLightArray : [];
				parameters.scope = Ext.isEmpty(parameters.scope) ? this : parameters.scope;

				if (!Ext.Object.isEmpty(parameters.record)) {
					CMDBuild.proxy.common.tabs.email.Email.create({
						params: parameters.record.getAsParams(),
						scope: parameters.scope,
						loadMask: this.cmfg('tabEmailGlobalLoadMaskGet'),
						failure: function (response, options, decodedResponse) {
							CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailCreate, false);
						},
						success: parameters.success || function (response, options, decodedResponse) {
							if (CMDBuild.controller.management.common.tabs.email.Email.trafficLightArrayCheck(parameters.record, parameters.regenerationTrafficLightArray))
								this.cmfg('tabEmailGridStoreLoad');
						}
					});
				} else {
					_warning('tried to add empty record', this);

					this.cmfg('tabEmailGridStoreLoad');
				}
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {Mixed} parameters.record
		 * @param {Array} parameters.regenerationTrafficLightArray
		 */
		tabEmailGridRecordEdit: function (parameters) {
			if (!Ext.Object.isEmpty(parameters)) {
				parameters.regenerationTrafficLightArray = Ext.isArray(parameters.regenerationTrafficLightArray) ? parameters.regenerationTrafficLightArray : [];

				if (!Ext.Object.isEmpty(parameters.record)) {
					CMDBuild.proxy.common.tabs.email.Email.update({
						params: parameters.record.getAsParams(),
						scope: this,
						loadMask: this.cmfg('tabEmailGlobalLoadMaskGet'),
						failure: function (response, options, decodedResponse) {
							CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailUpdate, false);
						},
						success: function (response, options, decodedResponse) {
							if (CMDBuild.controller.management.common.tabs.email.Email.trafficLightArrayCheck(parameters.record, parameters.regenerationTrafficLightArray))
								this.cmfg('tabEmailGridStoreLoad');
						}
					});
				} else {
					_warning('tried to edit empty record', this);

					this.cmfg('tabEmailGridStoreLoad');
				}
			}
		},

		/**
		 * @param {Mixed} record
		 *
		 * @returns {Boolean}
		 */
		tabEmailGridRecordIsEditable: function (record) {
			return record.get(CMDBuild.core.constants.Proxy.STATUS) == CMDBuild.core.constants.Proxy.DRAFT;
		},

		/**
		 * @param {Mixed} record
		 *
		 * @returns {Boolean}
		 */
		tabEmailGridRecordIsRegenerable: function (record) {
			return !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.TEMPLATE));
		},

		/**
		 * @param {Mixed} record
		 *
		 * @returns {Boolean}
		 */
		tabEmailGridRecordIsSendable: function (record) {
			return (
				!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.TO))
				&& !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.SUBJECT))
				&& record.get(CMDBuild.core.constants.Proxy.STATUS) != CMDBuild.core.constants.Proxy.OUTGOING
				&& record.get(CMDBuild.core.constants.Proxy.STATUS) != CMDBuild.core.constants.Proxy.RECEIVED
				&& record.get(CMDBuild.core.constants.Proxy.STATUS) != CMDBuild.core.constants.Proxy.SENT
			);
		},

		/**
		 * @param {Mixed} record
		 * @param {Array} regenerationTrafficLightArray
		 */
		tabEmailGridRecordRemove: function (record, regenerationTrafficLightArray) {
			if (!Ext.Object.isEmpty(record)) {
				CMDBuild.proxy.common.tabs.email.Email.remove({
					params: record.getAsParams([CMDBuild.core.constants.Proxy.ID, CMDBuild.core.constants.Proxy.TEMPORARY]),
					scope: this,
					loadMask: this.cmfg('tabEmailGlobalLoadMaskGet'),
					failure: function (response, options, decodedResponse) {
						CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.emailRemove, false);
					},
					success: function (response, options, decodedResponse) {
						if (CMDBuild.controller.management.common.tabs.email.Email.trafficLightArrayCheck(record, regenerationTrafficLightArray))
							this.cmfg('tabEmailGridStoreLoad');
					}
				});
			} else {
				_warning('tried to remove empty record', this);

				this.cmfg('tabEmailGridStoreLoad');
			}
		},

		/**
		 * Send all draft email records
		 */
		tabEmailGridSendAll: function () {
			if (!this.cmfg('tabEmailGridDraftEmailsIsEmpty')) {
				var updateTrafficLightArray = [];

				Ext.Array.forEach(this.cmfg('tabEmailGridDraftEmailsGet'), function (email, i, allEmails) {
					this.recordSend(email, updateTrafficLightArray);
				}, this);
			}
		},

		/**
		 * Loads grid store with activityId parameter
		 */
		tabEmailGridStoreLoad: function () {
			this.cmfg('tabEmailBusyStateSet', true); // Setup widget busy state and the begin of store load

			this.view.getStore().removeAll(); // Clear store before load new one

			var params = {};
			params[CMDBuild.core.constants.Proxy.REFERENCE] = this.cmfg('tabEmailSelectedEntityGet', CMDBuild.core.constants.Proxy.ID);

			this.view.getStore().load({
				params: params,
				scope: this,
				callback: function (records, operation, success) {
					if (success)
						this.cmfg('tabEmailGetAllTemplatesData');
				}
			});
		},

		/**
		 * Disable topToolbar evaluating readOnly and edit mode (disable only when readOnly = false and editMode = true)
		 */
		tabEmailGridUiStateSet: function () {
			this.view.setDisabledTopBar(
				!(
					!this.cmfg('tabEmailConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY)
					&& this.cmfg('tabEmailEditModeGet')
				)
			);
		}
	});

})();
