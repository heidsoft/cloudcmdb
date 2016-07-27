(function() {

	Ext.require([ // Legacy
		'CMDBuild.controller.management.widget.manageRelation.CMManageRelationController',
		'CMDBuild.core.configurations.Timeout',
		'CMDBuild.core.Message'
	]);

	Ext.define("CMDBuild.controller.management.common.CMWidgetManagerController", {

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
			if (!Ext.isEmpty(controller.widgetConfiguration) && controller.widgetConfiguration['type'] == '.OpenReport') // FIXME: hack to hide errors on OpenReport widget button click because of row permissions
				CMDBuild.global.interfaces.Configurations.set('disableAllMessages', false);

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
				var controllers = {};
				for (var i=0, l=definitions.length, w=null, ui=null; i<l; ++i) {
					w = definitions[i];
					ui = me.view.buildWidget(w, card);

					if (ui) {
						var wc = me.buildWidgetController(ui, w, card);
						if (wc) {
							controllers[me.getWidgetId(w)] = wc;
						}
					}
				}

				this.controllers = Ext.clone(controllers); // Fixes problem of multiple instantiation of this class and losing controller pointers
			}
		},

		/**
		 * @param {Object} widgetConfigurationObject
		 *
		 * @returns {Void}
		 *
		 * @public
		 */
		onWidgetButtonClick: function (widgetConfigurationObject) {
			var controller = this.controllers[this.getWidgetId(widgetConfigurationObject)];

			if (!Ext.Object.isEmpty(widgetConfigurationObject) && widgetConfigurationObject['type'] == '.OpenReport') // FIXME: hack to hide errors on OpenReport widget button click because of row permissions
				CMDBuild.global.interfaces.Configurations.set('disableAllMessages', true);

			this.delegate.ensureEditPanel(); // Creates editPanel with relative form fields

			if (!Ext.isEmpty(controller)) {
				this.view.showWidget(controller.view, this.getWidgetLable(widgetConfigurationObject));

				// cmfg() implementation adapter
				if (!Ext.isEmpty(controller.cmfg) && Ext.isFunction(controller.cmfg)) {
					controller.cmfg('beforeActiveView');
				} else if (Ext.isFunction(controller.beforeActiveView)) {
					controller.beforeActiveView();
				}
			}
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Void}
		 *
		 * @public
		 */
		onCardGoesInEdit: function() {
			Ext.Object.each(this.controllers, function(id, controller, myself) {
				// FIXME: widget instance data storage should be implemented inside this class
				if (!Ext.isEmpty(controller.instancesDataStorageReset) && Ext.isFunction(controller.instancesDataStorageReset))
					controller.instancesDataStorageReset();

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

			return widgetsAreValid ? null : '<ul style="text-align: left;">' + out + '</ul>';
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
				id: 'widgetManagerBeforeSaveBarrier',
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

	Ext.define("CMDBuild.controller.management.common.CMWidgetManagerControllerPopup", {
		extend: "CMDBuild.controller.management.common.CMWidgetManagerController",
		buildControllers: function(widgets, card) {
			var me = this;
			me.removeAll();

			for (var w in widgets) {
				ui = me.view.buildWidget(widgets[w], card);

				if (ui) {
					var wc = me.buildWidgetController(ui, widgets[w], card);
					if (wc) {
						me.controllers[me.getWidgetId(widgets[w])] = wc;
					}
				}
			}
		}
	});

})();
