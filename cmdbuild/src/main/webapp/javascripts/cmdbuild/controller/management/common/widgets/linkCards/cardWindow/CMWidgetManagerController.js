(function() {

	Ext.require([ // Legacy
		'CMDBuild.controller.management.widget.manageRelation.CMManageRelationController',
		'CMDBuild.core.configurations.Timeout',
		'CMDBuild.core.Message'
	]);

	Ext.define("CMDBuild.controller.management.common.widgets.linkCards.cardWindow.CMWidgetManagerController", {

		/**
		 * @property {Object}
		 */
		controllerClasses: {},

		/**
		 * @property {Object}
		 */
		controllers: {},

		constructor: function(view) {
			Ext.apply(this, {
				controllerClasses: {
					'.Calendar': CMDBuild.controller.management.common.widgets.CMCalendarController,
					'.CreateModifyCard': CMDBuild.controller.management.common.widgets.CMCreateModifyCardController,
					'.CustomForm': 'CMDBuild.controller.management.widget.customForm.CustomForm',
					'.Grid': 'CMDBuild.controller.management.common.widgets.grid.Grid',
					'.LinkCards': CMDBuild.controller.management.common.widgets.linkCards.LinkCardsController,
					'.ManageEmail': 'CMDBuild.controller.management.widget.ManageEmail',
					'.ManageRelation': CMDBuild.controller.management.widget.manageRelation.CMManageRelationController,
					'.NavigationTree': CMDBuild.controller.management.common.widgets.CMNavigationTreeController,
					'.OpenAttachment': CMDBuild.controller.management.common.widgets.CMOpenAttachmentController,
					'.OpenNote': CMDBuild.controller.management.common.widgets.CMOpenNoteController,
					'.OpenReport': 'CMDBuild.controller.management.widget.openReport.OpenReport',
					'.Ping': 'CMDBuild.controller.management.widget.Ping',
					'.PresetFromCard': CMDBuild.controller.management.common.widgets.CMPresetFromCardController,
					'.WebService': CMDBuild.controller.management.common.widgets.CMWebServiceController,
					'.Workflow': CMDBuild.controller.management.common.widgets.CMWorkflowController
				},
				view: view
			});

			this.view.delegate = this;
		},

		setDelegate: function(delegate) {
			this.delegate = delegate;
		},

		/**
		 * Forwarder method
		 *
		 * @param {Object} controller
		 */
		beforeHideView: function(controller) {
			if (!Ext.isEmpty(controller)) {
				// cmfg() implementation adapter
				if (!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)) {
					controller.cmfg('beforeHideView');
				} else if (Ext.isFunction(controller.beforeHideView)) {
					controller.beforeHideView();
				}
			}
		},

		buildControllers: function(card) {
			var me = this;
			me.removeAll();

			if (card) {
				var definitions = me.takeWidgetFromCard(card);
				for (var i=0, l=definitions.length, w=null, ui=null; i<l; ++i) {
					w = definitions[i];
					ui = me.view.buildWidget(w, card);

					if (ui) {
						var wc = me.buildWidgetController(ui, w, card);
						if (wc) {
							me.controllers[me.getWidgetId(w)] = wc;
						}
					}
				}
			}
		},

		onWidgetButtonClick: function(w) {
			this.delegate.ensureEditPanel();
			var me = this;
			Ext.defer(function() {
				var wc = me.controllers[me.getWidgetId(w)];
				if (wc) {
					me.view.showWidget(wc.view, me.getWidgetLable(w));

					// cmfg() implementation adapter
					if (!Ext.isEmpty(wc.cmfg) && Ext.isFunction(wc.cmfg)) {
						wc.cmfg('beforeActiveView');
					} else if (Ext.isFunction(wc.beforeActiveView)) {
						wc.beforeActiveView();
					}
				}
			}, 1);
		},

		/**
		 * Forwarder method
		 *
		 * @public
		 */
		onCardGoesInEdit: function() {
			Ext.Object.each(this.controllers, function(id, controller, myself) {
				// cmfg() implementation adapter
				if (!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)) {
					controller.cmfg('onEditMode');
				} else if (!Ext.isEmpty(controller.onEditMode) && Ext.isFunction(controller.onEditMode)) {
					controller.onEditMode();
				}
			}, this);
		},

		/**
		 * @returns {String or null}
		 *
		 * @public
		 */
		getWrongWFAsHTML: function () {
			var out = '';
			var widgetsAreValid = true;

			Ext.Object.each(this.controllers, function (id, controller, myself) {
				// cmfg() implementation adapter
				if (
					!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)
					&& !controller.cmfg('isValid')
				) {
					widgetsAreValid = false;
					out += '<li>' + controller.cmfg('getLabel') + '</li>';
				} else if (
					!Ext.isEmpty(controller.isValid) && Ext.isFunction(controller.isValid)
					&& !Ext.isEmpty(controller.getLabel) && Ext.isFunction(controller.getLabel)
					&& !controller.isValid()
				) {
					widgetsAreValid = false;
					out += '<li>' + controller.getLabel() + '</li>';
				}
			}, this);

			return widgetsAreValid ? null : '<ul>' + out + '</ul>';
		},

		removeAll: function clearWidgetControllers() {
			this.view.reset();
			for (var wcId in this.controllers) {
				var wc = this.controllers[wcId];
				wc.destroy();
				delete this.controllers[wcId];
				delete wc;
			}
		},

		/**
		 * @param {Function} callback
		 * @param {Object} callback
		 *
		 * @returns {Void}
		 */
		waitForBusyWidgets: function (callback, scope) {
			var requestBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
				id: 'widgetLinkCardBeforeSaveBarrier',
				executionTimeout: CMDBuild.core.configurations.Timeout.getWorkflowWidgetsExecutionTimeout(),
				scope: scope,
				callback: callback,
				failure: function () {
					CMDBuild.core.Message.error(null, CMDBuild.Translation.errors.busyVisualControls, false);
				}
			});

			Ext.Object.each(this.controllers, function (id, controller, myself) {
				// cmfg() implementation adapter
				if (!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)) {
					controller.cmfg('onBeforeSave', {
						scope: this,
						callback: requestBarrier.getCallback('widgetManagerBeforeSaveBarrier')
					});
				} else if (Ext.isFunction(controller.onBeforeSave)) {
					controller.onBeforeSave({
						scope: this,
						callback: requestBarrier.getCallback('widgetManagerBeforeSaveBarrier')
					});
				}
			}, this);

			requestBarrier.finalize('widgetManagerBeforeSaveBarrier', true);
		},

		/**
		 * Forwarder method
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Object} widgetsData
		 *
		 * @public
		 */
		getData: function(parameters) {
			var widgetsData = {};

			Ext.Object.each(this.controllers, function(id, controller, myself) {
				// cmfg() implementation adapter
				if (!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)) {
					var widgetData = controller.cmfg('getData', parameters);

					if (!Ext.isEmpty(widgetData))
						widgetsData[id] = widgetData;
				} else if (Ext.isFunction(controller.getData)) {
					var widgetData = controller.getData(parameters);

					if (!Ext.isEmpty(widgetData))
						widgetsData[id] = widgetData;
				}
			}, this);

			return widgetsData;
		},

		hideWidgetsContainer: function() {
			this.view.widgetsContainer.hide();
		},

		/**
		 * @param {Object} view
		 * @param {Object} widgetConfiguration
		 * @param {Ext.data.Model or CMDBuild.model.CMActivityInstance} card
		 *
		 * @returns {Object or null} controller
		 */
		buildWidgetController: function(view, widgetConfiguration, card) {
			var controller = null;
			var controllerClass = this.controllerClasses[widgetConfiguration.type];

			if (!Ext.isEmpty(controllerClass)) {
				if (Ext.isFunction(controllerClass)) { // @deprecated
					controller = new controllerClass(
						view,
						superController = this,
						widgetConfiguration,
						clientForm = this.view.getFormForTemplateResolver(),
						card
					);
				} else if (Ext.isString(controllerClass)) { // New widget controller declaration mode
					controller = Ext.create(controllerClass, {
						view: view,
						parentDelegate: this,
						widgetConfiguration: widgetConfiguration,
						clientForm: this.view.getFormForTemplateResolver(),
						card: card
					});
				}
			}

			return controller;
		},

		takeWidgetFromCard: function(card) {
			var widgets = [];
			if (Ext.getClassName(card) == "CMDBuild.model.CMActivityInstance") {
				widgets = card.getWidgets();
			} else {
				var et = _CMCache.getEntryTypeById(card.get("IdClass"));
				if (et) {
					widgets = et.getWidgets();
				}
			}

			return widgets;
		},

		getWidgetId: function(widget) {
			return widget.id;
		},

		getWidgetLable: function(widget) {
			return widget.label;
		},

		activateFirstTab: function() {
			this.view.activateFirstTab();
		}
	});

})();