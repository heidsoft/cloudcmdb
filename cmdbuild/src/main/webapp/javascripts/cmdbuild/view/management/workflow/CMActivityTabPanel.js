(function() {

	Ext.define("CMDBuild.view.management.workflow.CMActivityTabPanel", {
		extend: "Ext.panel.Panel",

		frame: false,
		layout: 'border',

		constructor: function(config) {
			this.activityTab = new CMDBuild.view.management.workflow.CMActivityPanel({
				title: CMDBuild.Translation.management.modworkflow.tabs.card,
				border: false,
				withToolBar: true,
				withButtons: true,

				listeners: {
					show: function(panel, eOpts) {
						// History record save
						if (!Ext.isEmpty(_CMWFState.getProcessClassRef()) && !Ext.isEmpty( _CMWFState.getProcessInstance()))
							CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
								moduleId: 'workflow',
								entryType: {
									description: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.TEXT),
									id: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.ID),
									object: _CMWFState.getProcessClassRef()
								},
								item: {
									description: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.TEXT),
									id: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.ID),
									object: _CMWFState.getProcessInstance()
								},
								section: {
									description: this.title,
									object: this
								}
							});
					}
				}
			});

			this.openNotePanel = CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_NOTE_TAB) ? null
				: new CMDBuild.view.management.common.widgets.CMOpenNotes({
					title: CMDBuild.Translation.management.modworkflow.tabs.notes,
					border: false,

					listeners: {
						show: function(panel, eOpts) {
							// History record save
							if (!Ext.isEmpty(_CMWFState.getProcessClassRef()) && !Ext.isEmpty( _CMWFState.getProcessInstance()))
								CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
									moduleId: 'workflow',
									entryType: {
										description: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.TEXT),
										id: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.ID),
										object: _CMWFState.getProcessClassRef()
									},
									item: {
										description: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.TEXT),
										id: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.ID),
										object: _CMWFState.getProcessInstance()
									},
									section: {
										description: this.title,
										object: this
									}
								});
						}
					}
				})
			;

			this.relationsPanel = CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_RELATION_TAB) ? null
				: new CMDBuild.view.management.classes.CMCardRelationsPanel({
					title: CMDBuild.Translation.management.modworkflow.tabs.relations,
					border: false,
					cmWithAddButton: false,
					cmWithEditRelationIcons: false,

					listeners: {
						show: function(panel, eOpts) {
							// History record save
							if (!Ext.isEmpty(_CMWFState.getProcessClassRef()) && !Ext.isEmpty( _CMWFState.getProcessInstance()))
								CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
									moduleId: 'workflow',
									entryType: {
										description: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.TEXT),
										id: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.ID),
										object: _CMWFState.getProcessClassRef()
									},
									item: {
										description: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.TEXT),
										id: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.ID),
										object: _CMWFState.getProcessInstance()
									},
									section: {
										description: this.title,
										object: this
									}
								});
						}
					}
				})
			;

			this.openAttachmentPanel = CMDBuild.configuration.userInterface.isDisabledProcessTab(CMDBuild.core.constants.Proxy.PROCESS_ATTACHMENT_TAB) ? null
				: new CMDBuild.view.management.common.widgets.CMOpenAttachment({
					title: CMDBuild.Translation.management.modworkflow.tabs.attachments,
					border: false,

					listeners: {
						show: function(panel, eOpts) {
							// History record save
							if (!Ext.isEmpty(_CMWFState.getProcessClassRef()) && !Ext.isEmpty( _CMWFState.getProcessInstance()))
								CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
									moduleId: 'workflow',
									entryType: {
										description: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.TEXT),
										id: _CMWFState.getProcessClassRef().get(CMDBuild.core.constants.Proxy.ID),
										object: _CMWFState.getProcessClassRef()
									},
									item: {
										description: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.TEXT),
										id: _CMWFState.getProcessInstance().get(CMDBuild.core.constants.Proxy.ID),
										object: _CMWFState.getProcessInstance()
									},
									section: {
										description: this.title,
										object: this
									}
								});
						}
					}
				})
			;

			this.acutalPanel = new Ext.tab.Panel({
				region: "center",
				cls: "cmdb-border-right",
				activeTab: 0,
				frame: false,
				border: false,
				split: true
			});

			this.docPanel = new CMDBuild.view.management.workflow.CMActivityTabPanel.DocPanel();

			this.callParent(arguments);

			this.disableTabs();
		},

		initComponent : function() {
			Ext.apply(this,{
				items: [this.acutalPanel, this.docPanel]
			});

			this.callParent(arguments);
		},

		reset: function(idClass) {
			this.showActivityPanel();
			this.acutalPanel.items.each(function(item) {
				if (item.reset) {
					item.reset();
				}
				if (item.onClassSelected) {
					item.onClassSelected(idClass);
				}
			});
		},

		updateDocPanel: function(activity) {
			this.docPanel.updateBody(activity);
		},

		showActivityPanel: function() {
			this.acutalPanel.setActiveTab(this.activityTab);
		},

		disableTabs: function() {
			if (this.openNotePanel != null) {
				this.openNotePanel.disable();
			}

			if (this.relationsPanel != null) {
				this.relationsPanel.disable();
			}

			if (this.openAttachmentPanel != null) {
				this.openAttachmentPanel.disable();
			}
		},

		showActivityPanelIfNeeded: function() {
			if (this.ignoreTabActivationManagement) {
				this.ignoreTabActivationManagement = false;
				return;
			}
		},

		getWidgetButtonsPanel: function() {
			return this.activityTab;
		},

		getActivityPanel: function() {
			return this.activityTab;
		},

		getRelationsPanel: function() {
			return this.relationsPanel;
		},

		getHistoryPanel: function() {
			return this.cardHistoryPanel;
		},

		// CMTabbedWidgetDelegate

		getAttachmentsPanel: function() {
			return this.openAttachmentPanel;
		},

		getNotesPanel: function() {
			return this.openNotePanel;
		},

		getEmailPanel: function() {
			return this.emailPanel;
		},

		// return false if is not able to manage the widget
		showWidget: function (w) {
			var managedClasses = {
				"CMDBuild.view.management.common.widgets.CMOpenAttachment": function(me) {
					if (me.openAttachmentPanel != null) {
						me.openAttachmentPanel.cmActivate();
					}
				},
				"CMDBuild.view.management.common.widgets.CMOpenNotes": function(me) {
					if (me.openNotePanel != null) {
						me.openNotePanel.cmActivate();
					}
				},
				'CMDBuild.view.management.workflow.tabs.Email': function(me) {
					if (!Ext.isEmpty(me.emailPanel))
						me.emailPanel.cmActivate();
				}
			};

			var fn = managedClasses[Ext.getClassName(w)];

			if (typeof fn == "function") {
				fn(this);
				return true;
			} else {
				return false;
			}
		},

		activateFirstTab: function() {
			this.acutalPanel.setActiveTab(this.activityTab);
		},

		/**
		 * @param {Object} tab
		 */
		activeTabSet: function(tab) {
			if (!Ext.Object.isEmpty(tab) && Ext.isObject(tab))
				return this.acutalPanel.setActiveTab(tab);

			return this.acutalPanel.setActiveTab(this.activityTab);
		}
	});

	Ext.define("CMDBuild.view.management.workflow.CMActivityTabPanel.DocPanel", {
		extend: "Ext.panel.Panel",
		initComponent: function() {
			Ext.apply(this, {
				autoScroll: true,
				width: "30%",
				hideMode: "offsets",
				region: "east",
				frame: true,
				border: true,
				collapsible: true,
				collapsed: true,
				animCollapse: false,
				split: true,
				margin: "0 5 5 0",
				title: CMDBuild.Translation.management.modworkflow.activitydocumentation,
				html: ""
			});

			this.callParent(arguments);
		},

		updateBody: function(instructions) {
			if (this.body) {
				this.body.update(instructions || "");
			}
		}
	});

})();