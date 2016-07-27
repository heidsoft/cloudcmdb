(function () {

	/*
	 * The grid must be reload when is shown, so resolve the template and load it.
	 * If there is a defaultSelection, when the activity form goes in edit mode resolve the template to calculate the selection and if needed add dependencies to the fields.
	 */
	Ext.define('CMDBuild.controller.management.common.widgets.linkCards.LinkCardsController', {
		extend: 'CMDBuild.controller.management.common.widgets.CMWidgetController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.Utils',
			'CMDBuild.proxy.widget.linkCards.LinkCards'
		],

		mixins: {
			observable: 'Ext.util.Observable'
		},

		/**
		 * @cfg {Boolean}
		 */
		alertIfChangeDefaultSelection: false,

		/**
		 * @property {CMDBuild.model.CMActivityInstance}
		 */
		card: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Boolean}
		 *
		 * @private
		 */
		firstWidgetVisualization: true,

		/**
		 * @property {Boolean}
		 *
		 * @private
		 */
		gisMapEnabled: false,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.LinkCardsGrid}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.widgets.linkCards.LinkCardsMapController}
		 */
		mapController: undefined,

		/**
		 * @property {CMDBuild.model.widget.linkCards.Selection}
		 */
		model: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: undefined,

		/**
		 * @cfg {Boolean}
		 */
		singleSelect: undefined,

		/**
		 * @cfg {String}
		 */
		outputName: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		ownerController: undefined,

		/**
		 * @property {Object}
		 */
		targetClass: undefined,

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * Is busy when load the default selection
		 *
		 * @property {Boolean}
		 */
		templateResolverIsBusy: false,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.LinkCards}
		 */
		view: undefined,

		/**
		 * @property {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.linkCards.LinkCards} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetConf
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 *
		 * @override
		 */
		constructor: function (view, ownerController, widgetConf, clientForm, card) {
			var me = this;
			var targetClassName = null;

			this.mixins.observable.constructor.call(this);

			this.callParent(arguments);

			// Try to get targetClassName from a source (widgetConf.className or widgetConf.filter)
			if (
				!Ext.isEmpty(this.widgetConf[CMDBuild.core.constants.Proxy.CLASS_NAME])
				&& _CMCache.isEntryTypeByName(this.widgetConf[CMDBuild.core.constants.Proxy.CLASS_NAME])
			) {
				targetClassName = this.widgetConf[CMDBuild.core.constants.Proxy.CLASS_NAME];
			} else if (
				!Ext.isEmpty(this.widgetConf[CMDBuild.core.constants.Proxy.FILTER])
				&& _CMCache.isEntryTypeByName(
					this.getClassNameFromFilterString(this.widgetConf[CMDBuild.core.constants.Proxy.FILTER])
				)
			) {
				targetClassName = this.getClassNameFromFilterString(this.widgetConf[CMDBuild.core.constants.Proxy.FILTER]);
			} else {
				return CMDBuild.core.Message.error(
					CMDBuild.Translation.error,
					CMDBuild.Translation.errors.widgetLinkCardsNoClassNameError,
					false
				);
			}

			// Set local tergetClass and set it also in CMCardModuleState avoiding delegates call
			this.targetClass = _CMCache.getEntryTypeByName(targetClassName);
			_CMCardModuleState.setEntryType(this.targetClass, null, null, false);

			this.singleSelect = this.widgetConf[CMDBuild.core.constants.Proxy.SINGLE_SELECT];
			this.readOnly = this.widgetConf[CMDBuild.core.constants.Proxy.READ_ONLY];

			this.view.delegate = this;
			this.grid = this.view.grid;
			this.grid.delegate = this;
			this.view.widgetConf = this.widgetConf;

			this.model = Ext.create('CMDBuild.model.widget.linkCards.Selection', { singleSelect: this.singleSelect });

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.clientForm,
				xaVars: me._extractVariablesForTemplateResolver(),
				serverVars: this.getTemplateResolverServerVars()
			});

			if (this.gisMapEnabled && !Ext.isEmpty(this.view.getMapPanel()))
				this.mapController = Ext.create('CMDBuild.controller.management.common.widgets.linkCards.LinkCardsMapController', {
					view: this.view.getMapPanel(),
					model: this.model,
					parentDelegate: this,
					widgetConf: this.widgetConf
				});
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function (name, param, callBack) {
			switch (name) {
				case 'onDeselect':
					return this.onDeselect(param);

				case 'onGridPageChange':
					return this.onGridPageChange();

				case 'onGridShow':
					return this.onGridShow();

				case 'onToggleGridFilterButtonClick':
					return this.onToggleGridFilterButtonClick(param);

				case 'onToggleMapButtonClick' :
					return this.onToggleMapButtonClick();

				case 'onRowEditButtonClick':
					return this.onRowEditButtonClick(param);

				case 'onRowViewButtonClick':
					return this.onRowViewButtonClick(param);

				case 'onSelect':
					return this.onSelect(param.record);

				case 'onLinkCardApplyDefaultSelectionButtonClick':
					return this.onLinkCardApplyDefaultSelectionButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @return {Object} variables
		 */
		_extractVariablesForTemplateResolver: function () {
			var variables = {};
			variables[CMDBuild.core.constants.Proxy.DEFAULT_SELECTION] = this.widgetConf[CMDBuild.core.constants.Proxy.DEFAULT_SELECTION];
			variables[CMDBuild.core.constants.Proxy.FILTER] = this.widgetConf[CMDBuild.core.constants.Proxy.FILTER];

			Ext.apply(variables, this.widgetConf[CMDBuild.core.constants.Proxy.TEMPLATES] || {});

			return variables;
		},

		alertIfNeeded: function () {
			if (this.alertIfChangeDefaultSelection) {
				CMDBuild.core.Message.warning(
					null,
					Ext.String.format(
						CMDBuild.Translation.warnings.link_cards_changed_values,
						this.widgetConf[CMDBuild.core.constants.Proxy.LABEL] || this.view.id
					),
					false
				);

				this.alertIfChangeDefaultSelection = false;
			}
		},

		/**
		 * When the linkCard is not busy load the grid
		 *
		 * @override
		 */
		beforeActiveView: function () {
			if (!Ext.isEmpty(this.targetClass)) {
				this.gisMapEnabled = this.widgetConf[CMDBuild.core.constants.Proxy.ENABLE_MAP] && CMDBuild.configuration.gis.get(CMDBuild.core.constants.Proxy.ENABLED);

				// Manage UI items display state
				this.toggleGridFilterButtonManageState();// Disable toggle grid filter button
				this.grid.getSelectionModel().setLocked(this.widgetConf[CMDBuild.core.constants.Proxy.READ_ONLY]); // Hide checkcolumn if readonly mode

				new _CMUtils.PollingFunction({
					success: function () {
						// CQL filter and regular filter cannot be merged now.
						// The filter button should be enabled only if no other filter is present.
						if (
							!Ext.isEmpty(this.widgetConf[CMDBuild.core.constants.Proxy.FILTER])
							&& this.view.toggleGridFilterButton.getActiveState() == CMDBuild.core.constants.Proxy.ENABLE
						) {
							this.resolveFilterTemplate(this.widgetConf[CMDBuild.core.constants.Proxy.FILTER], this.targetClass.getId());
						} else {
							this.updateViewGrid(this.targetClass.getId());
						}

						if (!this.model.hasSelection())
							this.resolveDefaultSelectionTemplate();

						this.onGridShow();
					},
					failure: function () {
						CMDBuild.core.Message.error(null, CMDBuild.Translation.errors.busyVisualControls, false);
					},
					checkFn: function () {
						// I want exit if I'm not busy
						return !this.isBusy();
					},
					cbScope: this,
					checkFnScope: this
				}).run();
			}
		},

		/**
		 * @param {Boolean} forceState
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		toggleGridFilterButtonManageState: function (forceState) {
			forceState = Ext.isBoolean(forceState) ? forceState : null;

			if (Ext.isEmpty(forceState)) { // Manage state evaluating widget configuration
				this.view.toggleGridFilterButton.setDisabled(
					(
						this.widgetConf.hasOwnProperty(CMDBuild.core.constants.Proxy.DISABLE_GRID_FILTER_TOGGLER)
						&& this.widgetConf[CMDBuild.core.constants.Proxy.DISABLE_GRID_FILTER_TOGGLER]
					)
					|| Ext.isEmpty(this.widgetConf[CMDBuild.core.constants.Proxy.FILTER])
				);
			} else { // Set forced state
				this.view.toggleGridFilterButton.setDisabled(forceState);
			}
		},

		/**
		 * @param {Ext.data.Model} model
		 * @param {Boolean] editable
		 */
		getCardWindow: function (model, editable) {
			var cardWindow = Ext.create('CMDBuild.view.management.common.widgets.linkCards.cardWindow.CMCardWindow', {
				cmEditMode: editable,
				withButtons: editable,
				title: model.get('IdClass_value')
			});

			Ext.create('CMDBuild.controller.management.common.widgets.linkCards.cardWindow.CMCardWindowController', {
				view: cardWindow,
				configuration: {
					entryType: model.get('IdClass'),
					card: model.get('Id'),
					cmEditMode: editable
				}
			});

			return cardWindow;
		},

		/**
		 * Takes in input a linkCards cql filter string (so is assumed that the filter is like "from className ..."), splits for "from" and the for " " so we get className
		 *
		 * @param {String} cqlFilter
		 *
		 * @return {String}
		 */
		getClassNameFromFilterString: function (cqlFilter) {
			var splitFilter = cqlFilter.trim().split('from');
			splitFilter = splitFilter[1].trim();

			return splitFilter.split(' ')[0];
		},

		/**
		 * Return selection data. Converts lat, lon metadata only if widget is in singleSelect mode, otherwise metadata will be passed to server without being edited.
		 *
		 * @return {Object} out
		 * 	{
		 * 		output: {
		 * 			cardId: {
		 * 				... metadata ...
		 * 			},
		 * 			cardId2: {},
		 * 			...
		 * 		},
		 * 		metadataOutput: configuration value
		 * 	}
		 *
		 * @override
		 */
		getData: function () {
			var out = null;

			if (!this.readOnly) {
				var modelSelections = this.model.getSelections();
				var widgetConfMetadata = this.widgetConf[CMDBuild.core.constants.Proxy.METADATA];

				// Output metadata codification only for single select mode
				if (this.singleSelect && !Ext.Object.isEmpty(widgetConfMetadata)) {
					for (var confIndex in widgetConfMetadata) {
						switch (widgetConfMetadata[confIndex]) {
							case 'POINT': {
								var selectionKey = Ext.Object.getKeys(modelSelections)[0];

								if (!Ext.Object.isEmpty(modelSelections[selectionKey])) {
									var lat = modelSelections[selectionKey]['lat'];
									var lon = modelSelections[selectionKey]['lon'];

									modelSelections[selectionKey] = {};
									modelSelections[selectionKey][confIndex] = new OpenLayers.Geometry.Point(lon, lat).toString();
								}
							} break;

							default:
								throw 'ERROR: LinkCardsController getData wrong widget metadata configuration (' + widgetConfMetadata[confIndex] + ')';
						}
					}
				}

				out = {};
				out[CMDBuild.core.constants.Proxy.OUTPUT] = modelSelections;
				out[CMDBuild.core.constants.Proxy.METADATA_OUTPUT] = this.widgetConf[CMDBuild.core.constants.Proxy.METADATA_OUTPUT]; // Simple property echo
			}

			return out;
		},

		/**
		 * @return {Object} targetClass
		 */
		getTargetClass: function () {
			return this.targetClass;
		},

		/**
		 * @return {Boolean} templateResolverIsBusy
		 *
		 * @override
		 */
		isBusy: function () {
			return this.templateResolverIsBusy;
		},

		/**
		 * @return {Boolean}
		 *
		 * @override
		 */
		isValid: function () {
			if (!this.readOnly && this.widgetConf[CMDBuild.core.constants.Proxy.REQUIRED]) {
				return this.model.hasSelection();
			} else {
				return true;
			}
		},

		/**
		 * @param {Object} params
		 * 	{
		 * 		{Ext.data.Model} record
		 * 	}
		 */
		onDeselect: function (params) {
			this.model.deselect(params.record.get('Id'));
		},

		/**
		 * Event to select right cards on grid page change
		 */
		onGridPageChange: function () {
			var modelSelections = this.model.getSelections();

			this.grid.getSelectionModel().deselectAll();

			for (var index in modelSelections)
				this.grid.getSelectionModel().select(
					this.grid.getStore().find(CMDBuild.core.constants.Proxy.ID, index)
				);
		},

		/**
		 * Loads grid's page for last selection and select
		 *
		 * @private
		 */
		onGridShow: function () {
			var lastSelectionId = this.model.getLastSelection();

			if (!Ext.isEmpty(lastSelectionId)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CARD_ID] = lastSelectionId;
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.widgetConf[CMDBuild.core.constants.Proxy.CLASS_NAME];
				params[CMDBuild.core.constants.Proxy.RETRY_WITHOUT_FILTER] = false;
				params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(this.grid.getStore().sorters.getRange());

				if (this.view.toggleGridFilterButton.getActiveState() == CMDBuild.core.constants.Proxy.ENABLE)
					params[CMDBuild.core.constants.Proxy.FILTER] = this.grid.getStore().getProxy().extraParams[CMDBuild.core.constants.Proxy.FILTER];

				this.model._silent = true;

				CMDBuild.proxy.widget.linkCards.LinkCards.readCardPosition({
					params: params,
					loadMask: false,
					scope: this,
					success: function (result, options, decodedResult) {
						var position = decodedResult.position;

						if (position >= 0) {
							this.grid.loadPage(
								CMDBuild.core.Utils.getPageNumber(position),
								{
									scope: this,
									cb: function () {
										this.grid.getSelectionModel().select(
											this.grid.getStore().find(CMDBuild.core.constants.Proxy.ID, lastSelectionId)
										);

										// Retry without grid store filter or server answer out of filter
										if (!this.grid.getSelectionModel().hasSelection() && this.view.toggleGridFilterButton.getActiveState()) {
											this.onToggleGridFilterButtonClick(false);
										}

										this.model._silent = false;
									}
								}
							);
						} else if (this.view.toggleGridFilterButton.getActiveState()) {
							this.onToggleGridFilterButtonClick(false);
						}

						this.model._silent = false;
					}
				});
			}
		},

		/**
		 * @param {Ext.data.Model} model
		 */
		onRowEditButtonClick: function (model) {
			var cardWindow = this.getCardWindow(model, true);

			cardWindow.on('destroy', function () {
				this.grid.reload();
			}, this, { single: true });

			cardWindow.show();
		},

		/**
		 * @param {Ext.data.Model} model
		 */
		onRowViewButtonClick: function (model) {
			this.getCardWindow(model, false).show();
		},

		/**
		 * @param {Object} record
		 */
		onSelect: function (record) {
			if (!Ext.isEmpty(record) && !Ext.isEmpty(record.get('Id'))) {
				_CMCardModuleState.setCard(record, null, false);

				this.model.select(record.get('Id'));
			} else {
				this.grid.getSelectionModel().deselectAll();
				this.model.reset();
			}
		},

		/**
		 * Disable grid store filter
		 *
		 * @param {Boolean} forceState
		 */
		onToggleGridFilterButtonClick: function (forceState) {
			var classId = this.targetClass.getId();
			var cqlQuery = this.widgetConf[CMDBuild.core.constants.Proxy.FILTER];

			if (Ext.isBoolean(forceState))
				this.view.toggleGridFilterButton.setActiveState(forceState ? CMDBuild.core.constants.Proxy.ENABLE : CMDBuild.core.constants.Proxy.DISABLE);

			if (forceState == CMDBuild.core.constants.Proxy.ENABLE) {
				this.resolveFilterTemplate(cqlQuery, classId);
			} else {
				this.resolveFilterTemplate(null, classId);
			}

			this.onGridShow();
		},

		/**
		 * @returns {Void}
		 */
		onToggleMapButtonClick: function () {
			if (this.gisMapEnabled && !Ext.isEmpty(this.view.getMapPanel())) {
				if (this.grid.isVisible()) {
					this.view.showMap();

					this.toggleGridFilterButtonManageState(true);
				} else {
					this.view.showGrid();

					this.toggleGridFilterButtonManageState();
				}
			}
		},

		/**
		 * @returns {Void}
		 */
		onLinkCardApplyDefaultSelectionButtonClick: function () {
			this.resolveDefaultSelectionTemplate();
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		resolveDefaultSelectionTemplate: function () {
			this.templateResolverIsBusy = true;
			this.viewReset();
			this.alertIfNeeded();

			this.templateResolver.resolveTemplates({
				attributes: [CMDBuild.core.constants.Proxy.DEFAULT_SELECTION],
				scope: this,
				callback: function (out, ctx) {
					var defaultSelection = this.templateResolver.buildCQLQueryParameters(out[CMDBuild.core.constants.Proxy.DEFAULT_SELECTION], ctx);

					// Do the request only if there are a default selection
					if (!Ext.isEmpty(defaultSelection)) {
						CMDBuild.proxy.widget.linkCards.LinkCards.readAllCards({
							params: defaultSelection,
							loadMask: false,
							scope: this,
							success: function (response, options, decodedResponse) {
								var decodedResponse = decodedResponse.rows;
								var lastSelection = undefined;

								if (!Ext.isEmpty(decodedResponse)) {
									Ext.Array.forEach(decodedResponse, function (row, i, allRows) {
										this.model.select(row['Id']);

										lastSelection = row;
									}, this);

									_CMCardModuleState.setCard(
										{
											Id: lastSelection['Id'],
											IdClass: lastSelection['IdClass']
										},
										null,
										false
									);
								}

								this.templateResolverIsBusy = false;

								this.onGridShow();
							}
						});

						this.templateResolver.bindLocalDepsChange(function () {
							this.resolveDefaultSelectionTemplate();
						});
					} else {
						this.templateResolverIsBusy = false;
					}
				}
			});
		},

		/**
		 * @param {String} cqlQuery
		 * @param {Int} classId
		 */
		resolveFilterTemplate: function (cqlQuery, classId) {
			var me = this;

			this.templateResolver.resolveTemplates({
				attributes: [CMDBuild.core.constants.Proxy.FILTER],
				callback: function (out, ctx) {
					var cardReqParams = me.templateResolver.buildCQLQueryParameters(cqlQuery, ctx);
					me.updateViewGrid(classId, cardReqParams);

					me.templateResolver.bindLocalDepsChange(function () {
						me.viewReset();
					});
				}
			});
		},

		/**
		 * @param {Int} classId
		 * @param {Object} cqlParams
		 */
		updateViewGrid: function (classId, cqlParams) {
			this.grid.CQL = cqlParams;
			this.grid.store.proxy.extraParams = this.grid.getStoreExtraParams();
			this.grid.updateStoreForClassId(classId);
		},

		viewReset: function () {
			this.grid.getSelectionModel().deselectAll();
			this.model.reset();
		}
	});

})();
