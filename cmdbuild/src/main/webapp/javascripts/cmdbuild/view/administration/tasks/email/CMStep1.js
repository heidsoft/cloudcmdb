(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormEmailController}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		taskType: 'email',

		/**
		 * @property {CMDBuild.view.administration.tasks.email.CMStep1}
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
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onFilterTypeComboChange':
					return this.onFilterTypeComboChange();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			/**
			 * @return {CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormController} delegate
			 */
			getFromAddressFilterDelegate: function() {
				return this.view.fromAddresFilter.delegate;
			},

			/**
			 * @return {CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormController} delegate
			 */
			getSubjectFilterDelegate: function() {
				return this.view.subjectFilter.delegate;
			},

			/**
			 * @return {Boolean}
			 */
			getValueRejectedFieldsetCheckbox: function() {
				return this.view.rejectedFieldset.checkboxCmp.getValue();
			},

			/**
			 * @return {String}
			 */
			getValueId: function() {
				return this.view.idField.getValue();
			},

		onFilterTypeComboChange: function () {
			this.view.filterDefinitionContainer.removeAll(false);

			switch (this.view.filterTypeCombobox.getValue()) {
				case 'regex':
					return this.view.filterDefinitionContainer.add([this.view.fromAddresFilter, this.view.subjectFilter]);

				case 'function':
					return this.view.filterDefinitionContainer.add(this.view.filterFunctionCombobox);

				case 'none':
				default:
					return;
			}
		},

		// SETters functions
			/**
			 * @param {Boolean} state
			 */
			setAllowBlankIncomingFolder: function(state) {
				this.view.incomingFolder.allowBlank = state;
			},

			/**
			 * @param {Boolean} state
			 */
			setAllowBlankEmailAccountCombo: function(state) {
				this.view.emailAccountCombo.allowBlank = state;
			},

			/**
			 * @param {Boolean} state
			 */
			setAllowBlankProcessedFolder: function(state) {
				this.view.processedFolder.allowBlank = state;
			},

			/**
			 * @param {Boolean} state
			 */
			setAllowBlankRejectedFolder: function(state) {
				this.view.rejectedFolder.allowBlank = state;
			},

			/**
			 * @param {Boolean} state
			 */
			setDisabledTypeField: function(state) {
				this.view.typeField.setDisabled(state);
			},

			/**
			 * @param {String} value
			 */
			setValueActive: function(value) {
				this.view.activeField.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueDescription: function(value) {
				this.view.descriptionField.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueEmailAccount: function(value) {
				this.view.emailAccountCombo.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueFilterFromAddress: function(value) {
				this.getFromAddressFilterDelegate().setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueFilterFunction: function(value) {
				this.view.filterFunctionCombobox.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueFilterSubject: function(value) {
				this.getSubjectFilterDelegate().setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueFilterType: function(value) {
				this.view.filterTypeCombobox.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueIncomingFolder: function(value) {
				this.view.incomingFolder.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueProcessedFolder: function(value) {
				this.view.processedFolder.setValue(value);
			},

			/**
			 * @param {Boolean} state
			 */
			setValueRejectedFieldsetCheckbox: function(state) {
				if (state) {
					this.view.rejectedFieldset.expand();
				} else {
					this.view.rejectedFieldset.collapse();
				}
			},

			/**
			 * @param {String} value
			 */
			setValueRejectedFolder: function(value) {
				this.view.rejectedFolder.setValue(value);
			},

			/**
			 * @param {Int} value
			 */
			setValueId: function(value) {
				this.view.idField.setValue(value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep1', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.email.Account'
		],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.email.CMStep1Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeField: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		descriptionField: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		emailAccountCombo: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterForm}
		 */
		fromAddresFilter: undefined,

		/**
		 * @property {Ext.form.field.Hidden}
		 */
		idField: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterForm}
		 */
		subjectFilter: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		typeField: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		defaults: {
			maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
			anchor: '100%'
		},

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep1Delegate', this);

			// Filter configuration
				this.fromAddresFilter = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterForm', {
					maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG - 5,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 5,

					fieldContainer: {
						fieldLabel: CMDBuild.Translation.sender
					},
					textarea: {
						name: CMDBuild.core.constants.Proxy.FILTER_FROM_ADDRESS,
						id: 'FromAddresFilterField'
					},
					button: {
						titleWindow: CMDBuild.Translation.sender
					}
				});
				this.subjectFilter = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterForm', {
					maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG - 5,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 5,

					fieldContainer: {
						fieldLabel: CMDBuild.Translation.subject
					},
					textarea: {
						name: CMDBuild.core.constants.Proxy.FILTER_SUBJECT,
						id: 'SubjectFilterField'
					},
					button: {
						titleWindow: CMDBuild.Translation.subject
					}
				});
				this.filterFunctionCombobox = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.core.constants.Proxy.FILTER_FUNCTION,
					fieldLabel: CMDBuild.Translation.functionLabel,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 5,
					maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG - 5,
					valueField: 'name',
					displayField: 'name',
					editable: false,
					allowBlank: false,

					store: _CMCache.getAvailableDataSourcesStore(),
					queryMode: 'local'
				});
			// END: Filter configuration

			// Rejected configuration
				this.rejectedFolder = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.core.constants.Proxy.REJECTED_FOLDER,
					fieldLabel: CMDBuild.Translation.rejectedFolder,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG - 10, // FIX: field with inside FieldSet is narrow
					anchor: '100%'
				});

				this.rejectedFieldset = Ext.create('Ext.form.FieldSet', {
					checkboxName: CMDBuild.core.constants.Proxy.REJECT_NOT_MATCHING,
					title: CMDBuild.Translation.enableMoveRejectedNotMatching,
					checkboxToggle: true,
					collapsed: true,
					collapsible: true,
					toggleOnTitleClick: true,
					overflowY: 'auto',
					maxWidth: 'auto',

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [this.rejectedFolder]
				});

				this.rejectedFieldset.fieldWidthsFix();
			// END: Rejected configuration

			Ext.apply(this, {
				items: [
					this.typeField = Ext.create('Ext.form.field.Text', {
						fieldLabel: CMDBuild.Translation.administration.tasks.type,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						name: CMDBuild.core.constants.Proxy.TYPE,
						value: tr.tasksTypes.email,
						disabled: true,
						cmImmutable: true,
						readOnly: true,
						submitValue: false
					}),
					this.descriptionField = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.description_,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						allowBlank: false
					}),
					this.activeField = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.ACTIVE,
						fieldLabel: CMDBuild.Translation.administration.tasks.startOnSave,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
					}),
					this.emailAccountCombo = Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT,
						fieldLabel: tr.taskEmail.emailAccount,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						displayField: CMDBuild.core.constants.Proxy.NAME,
						valueField: CMDBuild.core.constants.Proxy.NAME,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						forceSelection: true,
						editable: false,

						store: CMDBuild.proxy.email.Account.getStore()
					}),
					this.incomingFolder = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.INCOMING_FOLDER,
						fieldLabel: CMDBuild.Translation.incomingFolder,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
					}),
					this.filterDefinitionFieldset = Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.filter,
						maxWidth: 'auto',
						overflowY: 'auto',
						padding: '0 5 5 5',

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							this.filterTypeCombobox = Ext.create('Ext.form.field.ComboBox', {
								name: CMDBuild.core.constants.Proxy.FILTER_TYPE,
								fieldLabel: CMDBuild.Translation.type,
								labelWidth: CMDBuild.core.constants.FieldWidths.LABEL - 5,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								valueField: CMDBuild.core.constants.Proxy.VALUE,
								maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG - 5,
								value: CMDBuild.core.constants.Proxy.NONE, // Default value
								forceSelection: true,
								editable: false,

								store: Ext.create('Ext.data.ArrayStore', { // TODO: move to proxy
									fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.VALUE],
									data: [
										[CMDBuild.Translation.none, CMDBuild.core.constants.Proxy.NONE],
										[CMDBuild.Translation.regex, CMDBuild.core.constants.Proxy.REGEX],
										[CMDBuild.Translation.functionLabel, CMDBuild.core.constants.Proxy.FUNCTION]
									]
								}),
								queryMode: 'local',

								listeners: {
									scope: this,
									change: function (field, newValue, oldValue, eOpts) {
										this.delegate.cmOn('onFilterTypeComboChange');
									}
								}
							}),
							this.filterDefinitionContainer = Ext.create('Ext.container.Container', {
								layout: {
									type: 'vbox',
									align: 'stretch'
								},

								items: []
							})
						]
					}),
					this.processedFolder = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.PROCESSED_FOLDER,
						fieldLabel: CMDBuild.Translation.processedFolder,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
					}),
					this.rejectedFieldset,
					this.idField = Ext.create('Ext.form.field.Hidden', {
						name: CMDBuild.core.constants.Proxy.ID
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();