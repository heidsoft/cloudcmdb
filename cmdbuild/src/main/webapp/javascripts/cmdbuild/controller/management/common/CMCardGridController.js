(function() {

	Ext.require([
		'CMDBuild.core.Message',
		'CMDBuild.proxy.Card',
		'CMDBuild.core.Utils'
	]);

	Ext.define("CMDBuild.controller.management.common.CMCardGridController", {

		mixins: {
			observable: "Ext.util.Observable",
			filterWindow: "CMDBuild.view.management.common.filter.CMFilterWindowDelegate",
			saveFilterWindow: "CMDBuild.view.management.common.filter.CMSaveFilterWindowDelegate",
			runtimeFilterParamsWindow: "CMDBuild.delegate.common.filter.CMRuntimeParameterWindowDelegate"
		},

		constructor: function(view, supercontroller) {

			this.mixins.observable.constructor.call(this, arguments);

			if (typeof view == "undefined") {
				throw ("OOO snap, you have not passed a view to me");
			} else {
				this.view = view;
			}

			this.view.delegate = this;
			this.supercontroller = supercontroller;
			this.gridSM = this.view.getSelectionModel();

			this.CMEVENTS = {
				cardSelected: "cm-card-selected",
				wrongSelection: "cm-wrong-selection",
				gridVisible: "cm-visible-grid",
				itemdblclick: "itemdblclick",
				load: "load"
			};

			this.addEvents(this.CMEVENTS.cardSelected);
			this.addEvents(this.CMEVENTS.wrongSelection);
			this.addEvents(this.CMEVENTS.gridVisible);
			this.relayEvents(this.view, ["itemdblclick", "load"]);

			this.mon(this.gridSM, "selectionchange", this.onCardSelected, this);
			this.mon(this.view, "cmWrongSelection", this.onWrongSelection, this);
			this.mon(this.view, "cmVisible", this.onGridIsVisible, this);
			this.mon(this.view.printGridMenu, "click", this.onPrintGridMenuClick, this);

			this.stateDelegate = this.buildStateDelegate();
		},

		buildStateDelegate: function() {
			var sd = new CMDBuild.state.CMCardModuleStateDelegate();
			var me = this;

			sd.onEntryTypeDidChange = function(state, entryType, danglingCard, viewFilter) {
				me.onEntryTypeSelected(entryType, danglingCard, viewFilter);
			};

			sd.onCardDidChange = function(state, card) {
				if (!card) {
					return;
				}

				var currentSelection = me.gridSM.getSelection();
				if (Ext.isArray(currentSelection) && currentSelection.length>0) {
					currentSelection = currentSelection[0];
				}

				if (
					!Ext.isEmpty(currentSelection)
					&& !Ext.isEmpty(currentSelection.get) && Ext.isFunction(currentSelection.get)
				) {
					var id = currentSelection.get("Id");

					if (id && id == card.get("Id")) {
						return;
					} else {
						me.openCard({
							Id: card.get("Id"),
							IdClass: card.get("IdClass")
						});
					}
				}
			};

			_CMCardModuleState.addDelegate(sd);
		},

		getEntryType: function() {
			return _CMCardModuleState.entryType;
		},

		onEntryTypeSelected : function(entryType, danglingCard, viewFilter) {
			if (!entryType) {
				return;
			}

			unApplyFilter(this);

			var me = this,
				afterStoreUpdated;

			if (danglingCard) {
				afterStoreUpdated = function() {
					me.openCard(danglingCard, retryWithoutFilter = true);
				};
			} else {
				afterStoreUpdated = function() {
					if (viewFilter) {
						var filter = {};

						// If viewFilter is a CMFilterModel instance we have a default filter so force button enable
						if (Ext.getClassName(viewFilter) == 'CMDBuild.model.CMFilterModel') {
							filter = viewFilter;

							me.view.enableFilterMenuButton();
						} else {
							filter = Ext.create('CMDBuild.model.CMFilterModel', {
								configuration: Ext.decode(viewFilter),
								entryType: entryType.get('name'),
								name: CMDBuild.Translation.parameters
							});
						}

						applyFilter(me, filter);
					} else {
						me.view.loadPage(1, {
							cb: function(args) {
								var records = args[1];

								if (records && records.length > 0) {
									try {
										me.gridSM.select(0);
									} catch (e) {
										_debug(e);
									}
								}

								// Not a good implementation but don't exists another way
								if (!args[2]) {
									CMDBuild.core.Message.error(null, {
										text: CMDBuild.Translation.errors.anErrorHasOccurred
									});
								}
							}
						});
					}
				};
			}

			me.view.updateStoreForClassId(me.getEntryType().get("id"), {
				cb: afterStoreUpdated
			});

		},

		onAddCardButtonClick: function() {
			this.gridSM.deselectAll();
		},

		/**
		 * @params {String} format
		 */
		onPrintGridMenuClick: function(format) {
			if (!Ext.isEmpty(format)) {
				var params = Ext.apply({}, this.view.getStore().proxy.extraParams);
				params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(this.view.getVisibleColumns());
				params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(this.view.getStore().getSorters());
				params[CMDBuild.core.constants.Proxy.TYPE] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					parentDelegate: this,
					format: format,
					mode: 'view',
					parameters: params
				});
			}
		},

		onCardSelected: function(sm, selection) {
			if (Ext.isArray(selection)) {
				if (selection.length > 0) {
					_CMCardModuleState.setCard(selection[0]);
				}
			}
		},

		onWrongSelection: function() {
			this.fireEvent(this.CMEVENTS.wrongSelection);
		},

		onGridIsVisible: function(visible) {
			if (visible) {
				if (_CMCardModuleState.card) {
					this.openCard({
						Id: _CMCardModuleState.card.get("Id"),
						IdClass: _CMCardModuleState.card.get("IdClass")
					});
				}
			}

			var selection = this.gridSM.getSelection();
			this.fireEvent(this.CMEVENTS.gridVisible, visible, selection);
		},

		onCardSaved: function(c) {
			var retryIfTheCardIsNotInFilter = true;
			this.openCard(c, retryIfTheCardIsNotInFilter);
		},

		onCardDeleted: function() {
			this.view.reload();
		},

		onCloneCard: function() {
			this.gridSM.deselectAll();
		},

		/**
		 *
		 * @param {object} p
		 * @param {int} p.IdClass the id of the class
		 * @param {int} p.Id the id of the card to open
		 * @param {boolean} retryWithoutFilter
		 */
		openCard: function(p, retryWithoutFilter) {
			var me = this;
			var store = this.view.getStore();

			if (!store ||
					!store.proxy ||
						!store.proxy.extraParams) {
				return;
			}

			// Take the current store configuration
			// to have the sort and filter
			var params = Ext.apply({}, store.proxy.extraParams);
			params[CMDBuild.core.constants.Proxy.CARD_ID] = p.Id;
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(p.IdClass);
			params[CMDBuild.core.constants.Proxy.RETRY_WITHOUT_FILTER] = retryWithoutFilter;
			params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(getSorting(store));

			CMDBuild.proxy.Card.readPosition({
				params: params,
				loadMask: false,
				failure: function onGetPositionFailure(response, options, decoded) {
					// reconfigure the store and blah blah blah
				},
				success: function onGetPositionSuccess(response, options, resText) {
					var position = resText.position,
						found = position >= 0,
						foundButNotInFilter = resText.outOfFilter;

					if (found) {
						if (foundButNotInFilter) {
							me._onGetPositionSuccessForcingTheFilter(p, position, resText);
							me.view.gridSearchField.onUnapplyFilter();
						} else {
							updateStoreAndSelectGivenPosition(me, p.IdClass, position);
						}
					} else {
						if (retryWithoutFilter) {
							CMDBuild.core.Message.error(CMDBuild.Translation.common.failure,
									Ext.String.format(CMDBuild.Translation.errors.reasons.CARD_NOTFOUND, p.IdClass));
						} else {
							me._onGetPositionFailureWithoutForcingTheFilter(resText);
						}

						me.view.store.loadPage(1);
					}
				}
			});
		},

		reload: function(reselect) {
			this.view.reload(reselect);
		},

		_onGetPositionSuccessForcingTheFilter: function(p, position, resText) {
			var me = this;
			var view = me.view;
			unApplyFilter(me);
			updateStoreAndSelectGivenPosition(me, p.IdClass, position);
		},

		_onGetPositionFailureWithoutForcingTheFilter: function() {
			CMDBuild.core.Message.info(undefined, CMDBuild.Translation.cardNotMatchFilter);
		},

		// protected
		unApplyFilter: unApplyFilter,

		// As CMDBuild.controller.common.entryTypeGrid.filter.advanced.Advanced delegate
		/**
		 * Called by the CMDBuild.controller.common.entryTypeGrid.filter.advanced.Advanced when click to on the apply icon or on a row of the picker
		 *
		 * @param {object} filter
		 *
		 * @returns {Void}
		 */
		onFilterMenuButtonApplyActionClick: function (filter) {
			if (filter.getRuntimeParameters().length > 0) {
				showRuntimeParameterWindow(this, filter);
			} else {
				this.appliedFilter = filter;

				if (filter.dirty)
					addFilterToStore(this, filter, true);

				this.view.setFilterButtonLabel(Ext.String.trim(filter.getDescription()) == '' ? filter.getName() : filter.getDescription());
				this.view.applyFilterToStore(filter.getConfigurationMergedWithRuntimeAttributes());
				this.view.enableClearFilterButton();
				this.view.loadPage(1);
			}
		},

		// as runtimeFilterParamsWindow
		onRuntimeParameterWindowSaveButtonClick: function(runtimeParameterWindow, filter) {
			applyFilter(this, filter, runtimeParameterWindow.runtimeAttributes);
			runtimeParameterWindow.destroy();
		}
	});

	function getFilterStore(me) {
		return me.view.controllerAdvancedFilterButtons.getView().getFilterStore();
	}

	function addFilterToStore(me, filter, atFirst) {
		_CMCache.addFilter(getFilterStore(me), filter, atFirst);
	}

	function setStoredFilterApplied(me, filter) {
		var applied = true;
		_CMCache.setFilterApplied(getFilterStore(me), filter, applied);
	}

	function setStoreFilterUnapplied(me, filter) {
		var applied = false;
		_CMCache.setFilterApplied(getFilterStore(me), filter, applied);
	}

	function applyFilter(me, filter, runtimeAttributeFields) {
		if (filter.getRuntimeParameters().length > 0 && !runtimeAttributeFields) {
			showRuntimeParameterWindow(me, filter);
		} else {
			unApplyFilter(me);

			me.appliedFilter = filter;

			if (filter.dirty) {
				var atFirst = true;
				addFilterToStore(me, filter, atFirst);
			}

			var s = (Ext.String.trim(filter.getDescription()) == "") ? filter.getName() : filter.getDescription();
			me.view.setFilterButtonLabel(s);
			me.view.applyFilterToStore( //
					filter.getConfigurationMergedWithRuntimeAttributes(runtimeAttributeFields) //
				);

			me.view.enableClearFilterButton();
			me.view.loadPage(1);

			setStoredFilterApplied(me, filter);
		}
	}

	function showRuntimeParameterWindow(me, filter) {
		var runtimeAttributeConfigurations = filter.getRuntimeParameters();
		var runtimeAttributes = [];
		var showWindowToFillRuntimeParameters = runtimeAttributeConfigurations.length > 0;

		if (showWindowToFillRuntimeParameters) {
			var referredEntryTypeName = filter.getEntryType();
			var referredEntryType = _CMCache.getEntryTypeByName(referredEntryTypeName);

			if (referredEntryType) {
				_CMCache.getAttributeList(referredEntryType.getId(), //
					function(attributes) { //
						for (var i=0; i<runtimeAttributeConfigurations.length; ++i) {
							var runtimeAttributeToSearch = runtimeAttributeConfigurations[i];

							for (var j=0; j<attributes.length; ++j) {
								/*
								 * Force the attribute to be writable
								 * to allow the user to edit it
								 * in the RealTimeParameterWindow
								 */
								var attribute = Ext.apply({}, attributes[j]);
								attribute.fieldmode = "write";

								if (attribute.name == runtimeAttributeToSearch.attribute) {
									var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute);
									field._cmOperator = runtimeAttributeToSearch.operator;
									runtimeAttributes.push(field);

									break;
								}
							}
						}
					}
				);
			}

			if (!Ext.isEmpty(runtimeAttributes) && Ext.isArray(runtimeAttributes)) {
				var runtimeParametersWindow = new CMDBuild.view.management.common.filter.CMRuntimeParameterWindow({
					runtimeAttributes: runtimeAttributes,
					filter: filter,
					title: filter.getName()
				});

				runtimeParametersWindow.addDelegate(me);
				runtimeParametersWindow.show();
			}
		}

		return showWindowToFillRuntimeParameters;
	}

	function unApplyFilter(me) {
		if (me.gridSM.hasSelection())
			me.gridSM.deselectAll();

		if (me.appliedFilter) {
			setStoreFilterUnapplied(me, me.appliedFilter);
			me.appliedFilter = undefined;
		}

		me.view.setFilterButtonLabel();
		me.view.applyFilterToStore({});
		me.view.disableClearFilterButton();
	}

	function showSaveFilterDialog(me, filter, referredFilterWindow) {
		var saveFilterWindow = new CMDBuild.view.management.common.filter.CMSaveFilterWindow({
			filter: filter,
			referredFilterWindow: referredFilterWindow
		});

		saveFilterWindow.addDelegate(me);
		saveFilterWindow.show();
	}

	function updateStoreAndSelectGivenPosition(me, idClass, position) {
		var view = me.view;
		view.updateStoreForClassId(idClass, {
			cb: function cbOfUpdateStoreForClassId() {
				var	pageNumber = CMDBuild.core.Utils.getPageNumber(position),
					pageSize = CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
					relativeIndex = position % pageSize;

				view.loadPage(pageNumber, {
					cb: function() {
						var parameters = arguments[0];

						try {
							me.gridSM.deselectAll();
							me.gridSM.select(relativeIndex);

							// Force row expanding on select
							if (
								!Ext.isEmpty(me.view)
								&& !Ext.isEmpty(me.view.plugins)
								&& !Ext.isEmpty(me.view.plugins[0])
								&& me.view.plugins[0].ptype == 'activityrowexpander'
							) {
								me.view.plugins[0].toggleRow(relativeIndex, me.view.getStore().getAt(relativeIndex));
							}
						} catch (e) {
							view.fireEvent("cmWrongSelection");
							_warning("I was not able to select the record at " + relativeIndex, this);
						}

						if (!parameters[2]) {
							CMDBuild.core.Message.error(null, {
								text: CMDBuild.Translation.errors.anErrorHasOccurred
							});
						}
					}
				});
			}
		});
	}

	function getSorting(store) {
		var sorters = store.getSorters();
		var out = [];
		for (var i=0, l=sorters.length; i<l; ++i) {
			var s = sorters[i];
			out.push({
				property: s.property,
				direction: s.direction
			});
		}

		return out;
	}
})();