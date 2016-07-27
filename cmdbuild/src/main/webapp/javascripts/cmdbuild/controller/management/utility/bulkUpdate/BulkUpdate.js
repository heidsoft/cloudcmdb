(function () {

	Ext.define('CMDBuild.controller.management.utility.bulkUpdate.BulkUpdate', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.utility.BulkUpdate'
		],

		/**
		 * @cfg {CMDBuild.controller.management.utility.Utility}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUtilityBulkUpdateAbortButtonClick',
			'onUtilityBulkUpdateClassSelected',
			'onUtilityBulkUpdatePanelShow',
			'onUtilityBulkUpdateSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.utility.bulkUpdate.ClassesTree}
		 */
		classesTree: undefined,

		/**
		 * @property {CMDBuild.view.management.utility.bulkUpdate.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.utility.bulkUpdate.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 *
		 * @private
		 */
		selectedClass: undefined,

		/**
		 * @cfg {CMDBuild.view.management.utility.bulkUpdate.BulkUpdateView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.utility.Utility} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.utility.bulkUpdate.BulkUpdateView', { delegate: this });

			// Shorthands
			this.classesTree = this.view.classesTree;
			this.form = this.view.form;
			this.grid = this.view.grid;

			/**
			 * Sub-controllers (Legacy)
			 *
			 * @deprecated
			 */
			this.cardGridController = Ext.create('CMDBuild.controller.management.utility.bulkUpdate.CardGrid', {
				parentDelegate: this,
				view: this.grid
			});
		},

		/**
		 * @returns {Object}
		 *
		 * @private
		 */
		getFormCheckedValues: function () {
			var out = {};

			this.form.items.each(function (item) {
				var checkbox = item.checkbox;
				var field = item.field;

				if (checkbox.getValue())
					out[field.name] = field.getValue();
			});

			return out;
		},

		/**
		 * @returns {String}
		 *
		 * @private
		 */
		gridFilterGet: function() {
			var filter = {};
			var store = this.grid.getStore();

			if (!Ext.isEmpty(store.proxy) && !Ext.isEmpty(store.proxy.extraParams))
				filter = store.proxy.extraParams.filter;

			return filter;
		},

		/**
		 * @returns {Void}
		 */
		onUtilityBulkUpdateAbortButtonClick: function () {
			this.grid.getSelectionModel().reset();
			this.grid.reload(false);

			this.form.getForm().reset();
		},

		/**
		 * @param {CMDBuild.model.utility.bulkUpdate.ClassesTree} selectedNode
		 *
		 * @returns {Void}
		 */
		onUtilityBulkUpdateClassSelected: function (selectedNode) {
			if (Ext.isObject(selectedNode) && !Ext.Object.isEmpty(selectedNode)) {
				this.grid.getStore().removeAll();
				this.form.removeAll();

				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.utility.BulkUpdate.readClass({ // TODO: waiting for refactor (CRUD)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						var selectedNodeId = selectedNode.get(CMDBuild.core.constants.Proxy.ID);

						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
							var selectedClass = Ext.Array.findBy(decodedResponse, function (item, index) {
								return item[CMDBuild.core.constants.Proxy.ID] == selectedNodeId;
							}, this);

							if (Ext.isObject(selectedClass) && !Ext.Object.isEmpty(selectedClass)) {
								this.selectedClassSet({ value: selectedClass });

								// Grid update
								if (!this.selectedClassIsEmpty())
									this.cardGridController.onEntryTypeSelected(this.selectedClassGet(), this); // @legacy

								// Form update
								var params = {};
								params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
								params[CMDBuild.core.constants.Proxy.CLASS_NAME] = selectedNode.get(CMDBuild.core.constants.Proxy.NAME);

								CMDBuild.proxy.utility.BulkUpdate.readAttributes({
									params: params,
									scope: this,
									success: function (response, options, decodedResponse) {
										decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTES];

										if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse))
											Ext.Array.each(decodedResponse, function (attributeObject, i, allAttributesObjects) {
												if (
													attributeObject[CMDBuild.core.constants.Proxy.NAME] != 'Notes'
													|| ( // FIXME: HTML fields breaks the UI
														!Ext.isEmpty(attributeObject[CMDBuild.core.constants.Proxy.EDITOR_TYPE])
														&& attributeObject[CMDBuild.core.constants.Proxy.EDITOR_TYPE] == 'HTML'
													)
												) {
													var field = CMDBuild.Management.FieldManager.getFieldForAttr(attributeObject);

													if (field) {
														field.disable();
														field.margin = '0 0 0 5';

														this.form.add(Ext.create('CMDBuild.view.management.utility.bulkUpdate.FieldPanel', { field: field }));
													}
												}
											}, this);
									}
								});
							} else {
								_error('selected class not found', this);
							}
						}
					}
				});
			}
		},

		/**
		 * @returns {Void}
		 */
		onUtilityBulkUpdatePanelShow: function () {
			if (!this.classesTree.getSelectionModel().hasSelection())
				this.classesTree.getSelectionModel().select(0);
		},

		/**
		 * @param {Boolean} confirmed
		 *
		 * @returns {Void}
		 */
		onUtilityBulkUpdateSaveButtonClick: function (confirmed) {
			confirmed = Ext.isBoolean(confirmed) ? confirmed : false;

			if (!this.grid.getSelectionModel().cmReverse && !this.grid.getSelectionModel().hasSelection()) {
				return CMDBuild.core.Message.error(
						CMDBuild.Translation.common.failure,
						'<p class="' + CMDBuild.core.constants.Global.getErrorMsgCss() + '">' + CMDBuild.Translation.errors.noSelectedCardToUpdate + '</p>',
						false
					);
			} else {
				var selectedCardsArray = [];
				Ext.Array.each(this.grid.getSelectionModel().getSelection(), function (selectedCard, i, allSelectedCards) {
					var cardObject = {};
					cardObject[CMDBuild.core.constants.Proxy.CARD_ID] = selectedCard.get('Id');
					cardObject[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.selectedClassGet(CMDBuild.core.constants.Proxy.NAME);

					selectedCardsArray.push(cardObject);
				}, this);

				var params = this.getFormCheckedValues();
				params[CMDBuild.core.constants.Proxy.CARDS] = Ext.encode(selectedCardsArray);
				params[CMDBuild.core.constants.Proxy.CONFIRMED] = confirmed;

				if (!Ext.isEmpty(this.grid.getSelectionModel().cmReverse) && this.grid.getSelectionModel().cmReverse) {
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.selectedClassGet(CMDBuild.core.constants.Proxy.NAME);
					params[CMDBuild.core.constants.Proxy.FILTER] = this.gridFilterGet();

					CMDBuild.proxy.utility.BulkUpdate.bulkUpdateFromFilter({
						params: params,
						scope: this,
						success: function (response, options, decordedResponse) {
							if (confirmed) {
								this.grid.reload(false);

								this.cmfg('onUtilityBulkUpdateAbortButtonClick')
							} else {
								this.utilityBulkUpdateConfirmationModalShow(
									'<p>' + CMDBuild.Translation.warnings.changeAppliedOnlyToFilteredCards + '.</p>'
									+ Ext.String.format(
										'<p>' + CMDBuild.Translation.cardsWillBeModified + '.</p>',
										decordedResponse[CMDBuild.core.constants.Proxy.COUNT]
									)
								);
							}
						}
					});
				} else {
					CMDBuild.proxy.utility.BulkUpdate.bulkUpdate({
						params: params,
						scope: this,
						success: function (response, options, decordedResponse) {
							if (confirmed) {
								this.grid.reload(false);

								this.cmfg('onUtilityBulkUpdateAbortButtonClick')
							} else {
								this.utilityBulkUpdateConfirmationModalShow(
									Ext.String.format(
										'<p>' + CMDBuild.Translation.cardsWillBeModified + '.</p>',
										decordedResponse[CMDBuild.core.constants.Proxy.COUNT]
									)
								);
							}
						}
					});
				}
			}
		},

		// SelectedClass property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			selectedClassGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedClass';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 *
			 * @private
			 */
			selectedClassIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedClass';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			selectedClassSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.cache.CMEntryTypeModel'; // Because of old grid implementation
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedClass';

					this.propertyManageSet(parameters);
				}
			},

		/**
		 * @param {String} message
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		utilityBulkUpdateConfirmationModalShow: function (message) {
			Ext.Msg.show({
				title: CMDBuild.Translation.warning,
				msg: message,
				buttons: Ext.Msg.OKCANCEL,
				icon: Ext.MessageBox.WARNING,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'ok')
						this.cmfg('onUtilityBulkUpdateSaveButtonClick', true);
				}
			});
		}
	});

})();
