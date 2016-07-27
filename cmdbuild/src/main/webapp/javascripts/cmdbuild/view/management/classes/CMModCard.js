(function() {

	var TITLE_PREFIX = CMDBuild.Translation.management.modcard.title;

	Ext.define("CMDBuild.view.management.classes.CMModCard", {
		extend: "Ext.panel.Panel",

		mixins: {
			uistatedelegate: "CMDBuild.state.UIStateDelegate"
		},

		whitMap: true,
		layout: "border",
		border: true,

		constructor: function() {
			this.CMEVENTS = {
				addButtonClick: "cm-addcard-click"
			};

			this.buildComponents();
			this.callParent(arguments);

			if (typeof _CMUIState != "undefined") {
				_CMUIState.addDelegate(this);
			}
		},

		initComponent: function() {
			this.centralPanelItems = [this.cardGrid];

			buildMapPanel.call(this);

			this.centralPanel = new Ext.panel.Panel({
				region: "center",
				layout: "card",
				activeItem: 0,
				hideMode: "offsets",
				cls: "cmdb-border-bottom",
				border: false,
				frame: false,
				cardGrid: this.cardGrid,
				theMap: this.theMap,
				items: this.centralPanelItems,
				animCollapse: false,
				showGrid: function() {
					this.getLayout().setActiveItem(this.cardGrid.id);
					this.cardGrid.setCmVisible(true);
					this.theMap.setCmVisible(false);
				},
				showMap: function() {
					this.getLayout().setActiveItem(this.theMap.id);
					// update the size of the map because
					// it is not able to detect the change of its
					// container div.
					this.theMap.updateSize();

					this.theMap.setCmVisible(true);
					this.cardGrid.setCmVisible(false);
				}
			});

			Ext.apply(this, {
				items: [this.centralPanel, this.cardTabPanel],
				tools: [
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Properties'),
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Minimize'),
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Maximize'),
					Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.Restore')
				]
			});

			this.callParent(arguments);

			_CMUtils.forwardMethods(this, this.cardTabPanel, [
				"activateFirstTab",
				"setActivateTab",
				"getCardPanel",
				"getNotePanel",
				"getMDPanel",
				"getAttachmentsPanel",
				"getHistoryPanel",
				"getRelationsPanel",
				"getEmailPanel"
			]);

			// Defer map controller build after view render
			this.on('afterrender', function (panel, eOpts) {
				if (Ext.isFunction(this.delegate.buildMapController))
					this.delegate.buildMapController();
			}, this);
		},

		minimize: function() {
			Ext.suspendLayouts();

			this.centralPanel.hide();
			this.centralPanel.region = "";

			this.cardTabPanel.show();
			this.cardTabPanel.region = "center";

			Ext.resumeLayouts(true);
		},

		maximize: function() {

			Ext.suspendLayouts();

			this.cardTabPanel.hide();
			this.cardTabPanel.region = "";

			this.centralPanel.show();
			this.centralPanel.region = "center";

			Ext.resumeLayouts(true);
		},

		restore: function() {
			Ext.suspendLayouts();
			this.cardTabPanel.show();
			this.cardTabPanel.region = "south";

			this.centralPanel.show();
			this.centralPanel.region = "center";

			Ext.resumeLayouts(true);
		},

		updateTitleForEntry: function(entry) {
			var description = "";
			if (entry) {
				description = entry.get("text") || entry.get("name");
			}

			this.setTitle(TITLE_PREFIX+description);
		},

		// protected
		buildComponents: function() {
			var gridratio = CMDBuild.configuration.instance.get('cardFormRatio') || 50; // TODO: use proxy constants
			var tbar = [
				this.addCardButton = new CMDBuild.AddCardMenuButton({
					classId: undefined,
					disabled: true
				})
			];

			this.mon(this.addCardButton, "cmClick", function(p) {
				this.fireEvent(this.CMEVENTS.addButtonClick, p);
			}, this);

			buildMapButton.call(this, tbar);

			this.cardGrid = new CMDBuild.view.management.classes.CMModCard.Grid({
				hideMode: "offsets",
				filterCategory: this.cmName,
				border: false,
				tbar: tbar,
				columns: [],
				forceSelectionOfFirst: true
			});

			this.cardTabPanel = new CMDBuild.view.management.classes.CMCardTabPanel({
				cls: "cmdb-border-top",
				region: "south",
				hideMode: "offsets",
				border: false,
				split: true,
				height: gridratio + "%"
			});

			var widgetManager = new CMDBuild.view.management.common.widgets.CMWidgetManager(this.cardTabPanel.getCardPanel(), this.cardTabPanel);
			this.getWidgetManager = function() {
				return widgetManager;
			};
		},

		getGrid: function() {
			return this.cardGrid;
		},

		/**
		 * @deprecated
		 */
		reset: function(id) {
			_deprecated('reset', this);

			this.cardTabPanel.reset(id);
		},

		/**
		 * @deprecated
		 */
		onEntrySelected: function(entry) {
			_deprecated('onEntrySelected', this);

			var id = entry.get("id");

			this.cardGrid.updateStoreForClassId(id, {
				cb: function cbUpdateStoreForClassId() {
					this.loadPage(1, {
						cb: function cbLoadPage() {
							try {
								this.getSelectionModel().select(0);
							} catch (e) {
								_debug(e);
							}
						}
					});
				}
			});

			this.cardTabPanel.onClassSelected(id, activateFirst = true);

			this.addCardButton.updateForEntry(entry);
			this.mapAddCardButton.updateForEntry(entry);

			this.updateTitleForEntry(entry);

			this.cardGrid.openFilterButton.enable();
			this.cardGrid.gridSearchField.reset();
		},

		// as UIStateDelegate
		onFullScreenChangeToGridOnly: function() {
			this.maximize();
		},

		onFullScreenChangeToFormOnly: function() {
			this.minimize();
		},

		onFullScreenChangeToOff: function() {
			this.restore();
		}
	});

	Ext.define("CMDBuild.view.management.classes.CMModCard.Grid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		cmVisible: true,

		setCmVisible: function(visible) {
			this.cmVisible = visible;

			if (this.paramsToLoadWhenVisible) {
				this.updateStoreForClassId(this.paramsToLoadWhenVisible.classId, this.paramsToLoadWhenVisible.o);
				this.paramsToLoadWhenVisible = null;
			}

			this.fireEvent("cmVisible", visible);
		},

		updateStoreForClassId: function(classId, o) {
			if (this.cmVisible) {
				this.callParent(arguments);
				this.paramsToLoadWhenVisible = null;
			} else {
				this.paramsToLoadWhenVisible = {
					classId:classId,
					o: o
				};
			}
		}

	});

	function buildMapButton(tbar) {
		if (CMDBuild.configuration.gis.get('enabled')) { // TODO: use proxy constants

			this.showMapButton = new Ext.button.Button({
				text: CMDBuild.Translation.management.modcard.tabs.map,
				iconCls: 'map',
				scope: this,
				handler: function() {
					this.centralPanel.showMap();
				}
			});

			tbar.push('->', this.showMapButton);
		}
	}

	function buildMapPanel() {
		if (CMDBuild.configuration.gis.get('enabled') && this.whitMap) { // TODO: use proxy constants
			this.showGridButton = new Ext.button.Button({
				text: CMDBuild.Translation.management.modcard.add_relations_window.list_tab,
				iconCls: 'table',
				scope: this,
				handler: function() {
					this.centralPanel.showGrid();
				}
			});

			this.mapAddCardButton = new CMDBuild.AddCardMenuButton({
				classId: undefined,
				disabled: true
			});

			this.mapAddCardButton.on("cmClick", function(p) {
				this.fireEvent(this.CMEVENTS.addButtonClick, p);
			}, this);

			this.theMap = Ext.create('CMDBuild.view.management.classes.map.CMMapPanel', {
				tbar: [this.mapAddCardButton, '->', this.showGridButton],
				frame: false,
				border: false
			});

			this.centralPanelItems.push(this.theMap);

			this.getMapPanel = function() {
				return this.theMap;
			};
		} else {
			this.mapAddCardButton = {
				updateForEntry: Ext.emptyFn
			};
		}
	}

})();