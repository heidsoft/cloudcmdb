(function() {

	Ext.define("CMDBuild.view.management.common.widgets.CMWidgetManagerDelegate", {
		getFormForTemplateResolver: Ext.emptyFn,
		getWidgetButtonsPanel: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMTabbedWidgetDelegate", {
		getNotesPanel: Ext.emptyFn,
		getAttachmentsPanel: Ext.emptyFn,
		showWidget: Ext.emptyFn,
		activateFirstTab: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMWidgetManager", {

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		delegate: undefined,

		/**
		 * @property {Object}
		 */
		builders: {},

		/**
		 * @param {CMDBuild.view.management.classes.CMCardPanel or CMDBuild.view.management.workflow.CMActivityPanel} mainView
		 * @param {Mixed} tabbedWidgetDelegate
		 */
		constructor: function(mainView, tabbedWidgetDelegate) {
			var me = this;

			this.mainView = mainView;

			this.tabbedWidgetDelegate = tabbedWidgetDelegate || null;

			Ext.apply(this, {
				builders: {
				// OpenTabs widgets: they have to open a tab in the activityTabPanel, and not a separate window
					'.OpenNote': function(widget, card) {
						var widgetUI = null;
						if (me.tabbedWidgetDelegate) {
							widgetUI = me.tabbedWidgetDelegate.getNotesPanel() || null;

							if (widgetUI != null) {
								widgetUI.configure({
									widget: widget,
									activityInstance: card
								});
							}
						}

						return widgetUI;
					},

					'.OpenAttachment': function(widget, card) {
						var widgetUI = null;
						if (me.tabbedWidgetDelegate) {
							widgetUI = me.tabbedWidgetDelegate.getAttachmentsPanel() || null;

							if (widgetUI != null) {
								widgetUI.configure({
									widget: widget,
									activityInstance: card
								});
							}
						}

						return widgetUI;
					},

					/**
					 * @param {Object} widget
					 * @param {Ext.data.Model or CMDBuild.model.CMActivityInstance} card or activity
					 *
					 * @returns {Null}
					 */
					'.ManageEmail': function(widget, card) {
						if (!Ext.isEmpty(me.tabbedWidgetDelegate) && !Ext.isEmpty(me.tabbedWidgetDelegate.getEmailPanel()))
							return me.tabbedWidgetDelegate.getEmailPanel();

						return null;
					},
				// End OpenTabs widgets

					/**
					 * @param {Object} widget
					 * @param {Ext.data.Model or CMDBuild.model.CMActivityInstance} card or activity
					 */
					'.Calendar': function(widget, card) {
						var w = new CMDBuild.view.management.common.widgets.CMCalendar();

						me.widgetsContainer.addWidgt(w);

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {Ext.data.Model or CMDBuild.model.CMActivityInstance} card or activity
					 */
					'.CreateModifyCard': function(widget, card) {
						var w = new CMDBuild.view.management.common.widgets.CMCreateModifyCard(widget);

						me.widgetsContainer.addWidgt(w);

						var widgetManager = new CMDBuild.view.management.common.widgets.CMWidgetManager(w);

						w.getWidgetManager = function() {
							return widgetManager;
						};

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {CMDBuild.model.CMActivityInstance} activity
					 *
					 * @returns {CMDBuild.view.management.widget.customForm.CustomFormView}
					 */
					'.CustomForm': function(widget, card) {
						var w = Ext.create('CMDBuild.view.management.widget.customForm.CustomFormView');

						me.widgetsContainer.addWidgt(w);

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {CMDBuild.model.CMActivityInstance} activity
					 *
					 * @returns {CMDBuild.view.management.common.widgets.grid.GridView}
					 */
					'.Grid': function(widget, card) {
						var w = Ext.create('CMDBuild.view.management.common.widgets.grid.GridView');

						me.widgetsContainer.addWidgt(w);

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {CMDBuild.model.CMActivityInstance} card or activity
					 */
					'.LinkCards': function(widget, card) {
						var w = Ext.create('CMDBuild.view.management.common.widgets.linkCards.LinkCards', {
							widgetConf: widget // TODO: this dependency should be deleted
						});

						me.widgetsContainer.addWidgt(w);

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {CMDBuild.model.CMActivityInstance} card or activity
					 */
					'.ManageRelation': function(widget, card) {
						var w = Ext.create('CMDBuild.view.management.widget.manageRelation.CMManageRelation', {
							widget: widget // TODO: this dependency should be deleted
						});

						me.widgetsContainer.addWidgt(w);

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {CMDBuild.model.CMActivityInstance} card or activity
					 */
					'.NavigationTree': function(widget, card) {
						var w = new CMDBuild.view.management.common.widgets.CMNavigationTree();

						me.widgetsContainer.addWidgt(w);

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {Ext.data.Model or CMDBuild.model.CMActivityInstance} card or activity
					 */
					'.OpenReport': function(widget, card) {
						var w = Ext.create('CMDBuild.view.management.widget.openReport.OpenReportView');

						me.widgetsContainer.addWidgt(w);

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {Ext.data.Model or CMDBuild.model.CMActivityInstance} card or activity
					 */
					'.Ping': function(widget, card) {
						var w = Ext.create('CMDBuild.view.management.widget.PingView');

						me.widgetsContainer.addWidgt(w);

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {CMDBuild.model.CMActivityInstance} card or activity
					 */
					'.PresetFromCard': function(widget, card) {
						var w = new CMDBuild.view.management.common.widgets.CMPresetFromCard();

						me.widgetsContainer.addWidgt(w);

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {CMDBuild.model.CMActivityInstance} card or activity
					 */
					'.WebService': function(widget, card) {
						var w = new CMDBuild.view.management.common.widgets.CMWebService();

						me.widgetsContainer.addWidgt(w);

						return w;
					},

					/**
					 * @param {Object} widget
					 * @param {Ext.data.Model or CMDBuild.model.CMActivityInstance} card or activity
					 */
					'.Workflow': function(widget, card) {
						var w = new CMDBuild.view.management.common.widgets.CMWorkflow();

						me.widgetsContainer.addWidgt(w);

						return w;
					}
				}
			});
		},

		buildWidget: function(widget, card) {
			this.mainView.getWidgetButtonsPanel().addWidget(widget);
			return this._buildWidget(widget, card);
		},

		showWidget: function(w, title) {
			if (this.tabbedWidgetDelegate == null
				|| !this.tabbedWidgetDelegate.showWidget(w, title)) {

				this.widgetsContainer.showWidget(w, title);
			}
		},

		hideWidgetsContainer: function() {
			if (this.widgetsContainer) {
				this.widgetsContainer.hide();
			}
		},

		buildWidgetsContainer: function() {
			return Ext.create('CMDBuild.view.management.common.widgets.CMWidgetsWindow', {
				delegate: this.delegate
			});
		},

		reset: function() {
			if (!Ext.isEmpty(this.widgetsContainer))
				this.widgetsContainer.destroy();

			// Email tab configuration reset
			// TODO: find a better implementation
			if (
				!Ext.isEmpty(this.mainView.delegate)
				&& !Ext.isEmpty(this.mainView.delegate.superController)
				&& !Ext.isEmpty(this.mainView.delegate.superController.controllerTabEmail)
				&& Ext.isFunction(this.mainView.delegate.superController.controllerTabEmail.reset)
			) {
				this.mainView.delegate.superController.controllerTabEmail.reset();
			}

			this.widgetsContainer = this.buildWidgetsContainer();

			this.mainView.getWidgetButtonsPanel().removeAllButtons();

			this.widgetsMap = {};
		},

		getFormForTemplateResolver: function getFormForTemplateResolver() {
			return this.mainView.getFormForTemplateResolver();
		},

		activateFirstTab: function() {
			if (this.tabbedWidgetDelegate != null) {
				this.tabbedWidgetDelegate.activateFirstTab();
			}
		},

		_buildWidget: function(widget, card) {
			if (this.builders[widget.type]) {
				return this.builders[widget.type](widget, card);
			} else {
				return null;
			}
		}
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMWidgetManagerPopup", {
		extend: "CMDBuild.view.management.common.widgets.CMWidgetManager",

		buildWidgetsContainer: function() {
			return new CMDBuild.view.management.common.widgets.CMWidgetsWindowPopup();
		}
	});

})();