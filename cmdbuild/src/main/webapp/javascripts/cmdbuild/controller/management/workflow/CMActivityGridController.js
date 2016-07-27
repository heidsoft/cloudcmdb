(function() {

	Ext.define("CMDBuild.controller.management.workflow.CMActivityGridController", {
		extend: "CMDBuild.controller.management.common.CMCardGridController",

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.Card',
			'CMDBuild.proxy.workflow.Activity',
			'CMDBuild.core.Utils'
		],

		mixins: {
			activityPanelControllerDelegate: "CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate"
		},

		constructor: function(view, supercontroller) {
			this.callParent(arguments);

			this.CMEVENTS.processClosed = "processTerminated";

			this.addEvents(this.CMEVENTS.processClosed);

			// from cmmodworkflow
			this.mon(this.view.statusCombo, "select", onStatusComboSelect, this);
			this.mon(this.view.addCardButton, "cmClick", this.onAddCardButtonClick, this);
			this.mon(this.view, "activityInstaceSelect", this.onActivityInfoSelect, this);


		},

		// override
		buildStateDelegate: function() {
			var sd = new CMDBuild.state.CMWorkflowStateDelegate();
			var me = this;

			sd.onProcessClassRefChange = function(entryType, danglingCard, filter) {
				me.onEntryTypeSelected(entryType, danglingCard, filter);
			};

			_CMWFState.addDelegate(sd);
		},

		// override
		getEntryType: function() {
			return _CMWFState.getProcessClassRef();
		},

		onAddCardButtonClick: function(p) {
			this.gridSM.deselectAll();

			_CMWFState.setProcessInstance(new CMDBuild.model.CMProcessInstance({
				classId: p.classId
			}));

			CMDBuild.proxy.workflow.Activity.readStart({
				params: {
					classId: p.classId
				},
				important: true,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					var activity = new CMDBuild.model.CMActivityInstance(decodedResponse.response || {});

					_CMWFState.setActivityInstance(activity);
				}
			});
		},

		/**
		 * The activityInfo has only the base info about the activity.
		 * Do a request to have the activity data and set it in the _CMWFState
		 *
		 * @param {Int} activityInfoId
		 */
		onActivityInfoSelect: function(activityInfoId) {
			var me = this;

			// Prevent the selection of the same activity
			if (!activityInfoId || (me.lastActivityInfoId && me.lastActivityInfoId == activityInfoId)) {
				return;
			} else {
				me.lastActivityInfoId = null;
			}

			updateViewSelection(activityInfoId, me);

			CMDBuild.proxy.workflow.Activity.read({
				params:{
					classId: _CMWFState.getProcessInstance().getClassId(),
					cardId: _CMWFState.getProcessInstance().getId(),
					activityInstanceId: activityInfoId
				},
				important: true,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					var activity = new CMDBuild.model.CMActivityInstance(decodedResponse.response || {});

					me.lastActivityInfoId = activityInfoId;
					_CMWFState.setActivityInstance(activity);
				}
			});
		},

		/**
		 * @param {Ext.selection.RowModel} sm
		 * @param {CMDBuild.model.CMProcessInstance} selection
		 *
		 * @override
		 */
		onCardSelected: function(sm, selection) {
			if (Ext.isArray(selection)) {
				if (selection.length > 0) {
					var me = this;
					var pi = selection[0];
					var activities = pi.getActivityInfoList();

					this.lastActivityInfoId = null;

					CMDBuild.core.LoadMask.show();

					_CMWFState.setProcessInstance(pi, function() {
						if (activities.length > 0) {
							toggleRow(pi, me);
							if (activities.length == 1) {
								var ai = activities[0];

								if (ai && ai.id)
									me.onActivityInfoSelect(ai.id);
							}
						} else {
							_debug('A proces without activities', pi);
						}

						CMDBuild.core.LoadMask.hide();
					});
				}
			}
		},

		/**
		 *
		 * @param {Object} parameters
		 * @param {Number} parameters.IdClass
		 * @param {Number} parameters.Id
		 * @param {String} parameters.flowStatus
		 */
		openCard: function(parameters) {
			var store = this.view.getStore();

			if (!Ext.isEmpty(store) && !Ext.isEmpty(store.proxy) && !Ext.isEmpty(store.proxy.extraParams)) {
				// Take the current store configuration
				// to have the sort and filter
				var params = Ext.apply({}, store.proxy.extraParams);
				params[CMDBuild.core.constants.Proxy.CARD_ID] = parameters['Id'];
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(parameters['IdClass']);
				params[CMDBuild.core.constants.Proxy.RETRY_WITHOUT_FILTER] = false;
				params[CMDBuild.core.constants.Proxy.SORT] = Ext.encode(getSorting(store));

				CMDBuild.proxy.Card.readPosition({
					params: params,
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						var position = decodedResponse.position;

						if (position >= 0) {
							if (decodedResponse.outOfFilter) {
								this._onGetPositionSuccessForcingTheFilter(parameters, position, decodedResponse);
								this.view.gridSearchField.onUnapplyFilter();
							} else {
								updateStoreAndSelectGivenPosition(this, parameters.IdClass, position);
							}
						} else {
							this._onGetPositionFailureWithoutForcingTheFilter(parameters.flowStatus);

							this.view.store.loadPage(1);
						}
					}
				});
			}
		},

		// override
		_onGetPositionSuccessForcingTheFilter: function(p, position, resText) {
			this.view.setStatus(resText.FlowStatus);
			this.callParent(arguments);
		},

		/**
		 * @param {String} flowStatus
		 */
		_onGetPositionFailureWithoutForcingTheFilter: function(flowStatus) {
			if (flowStatus == 'COMPLETED') {
				this.view.skipNextSelectFirst();

				_CMWFState.setProcessInstance(new CMDBuild.model.CMProcessInstance());
				_CMUIState.onlyGridIfFullScreen();
			} else {
				CMDBuild.core.Message.info(undefined, CMDBuild.Translation.cardNotMatchFilter);
			}
		},

		// override
		onEntryTypeSelected: function(entryType, danglingCard) {
			this.callParent(arguments);
			this.view.addCardButton.updateForEntry(entryType);
		},

		/**
		 * activityPanelControllerDelegate
		 *
		 * @param {Number} cardId
		 * @param {String} cardFlowStatus
		 */
		onCardSaved: function(cardId, cardFlowStatus) {
			this.openCard({
				Id: cardId,
				IdClass: this.getEntryType().get("id"), // use the id class of the grid to use the right filter when look for the position
				flowStatus: cardFlowStatus
			});
		}
	});

	function onStatusComboSelect() {
		this.view.updateStatusParamInStoreProxyConfiguration();
		this.view.loadPage(1);
	}

	function toggleRow(pi, me) {
		var p = me.view.plugins[0];
		if (p) {
			p.toggleRow(pi.index, forceExpand = true);
		}
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

	function updateViewSelection(activityInfoId, me) {
		try {
			var activityRowEl = Ext.query('p[id='+activityInfoId+']', me.view.getEl().dom)[0];
			activityRowEl = new Ext.Element(activityRowEl);
			var p = me.view.plugins[0];

			p.selectSubRow(me.view, activityRowEl);
		} catch (e) {
			_debug("Can't select the activity " + activityInfoId);
		}
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