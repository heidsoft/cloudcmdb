(function() {
	Ext.define("CMDBuild.view.management.common.widgets.linkCards.cardWindow.CMWidgetButton", {
		extend: "Ext.button.Button",

		widgetDefinition: undefined, // pass on instantiation

		constructor: function() {
			this.callParent(arguments);
			this.margins ='0 0 5 0';
			this.text = this.widgetDefinition.label
					|| CMDBuild.Translation.management.modworkflow[this.widgetDefinition.labelId];
			this.disabled = !this.widgetDefinition.alwaysenabled;
		},

		disable: function() {
			if (
				( this.widgetDefinition && this.widgetDefinition.alwaysenabled)
				|| CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.PROCESS_WIDGET_ALWAYS_ENABLED)
			) {
				return this.enable();
			} else {
				return this.callParent(arguments);
			}
		}
	});
})();

Ext.define("CMDBuild.view.management.common.widgets.linkCards.cardWindow.CMWidgetButtonsPanel", {
	extend: "Ext.panel.Panel",
	statics: {
		CMEVENTS: {
			widgetButtonClick: "widget-click"
		}
	},

	initComponent: function() {
		this.frame = false;
		this.border = false;
		this.layout = {
			type : 'vbox',
			align : 'stretch'
		};
		this.bodyCls = "x-panel-body-default-framed";
		this.bodyStyle = {
			padding: "30px 5px 0 5px"
		};

		this.callParent(arguments);
		this.CMEVENTS = this.self.CMEVENTS;
	},

	addWidget: function addWidget(widget) {
		var me = this;
		if (me._hidden) {
			me.show();
			me._hidden = false;
		}

		me.add(Ext.create('CMDBuild.view.management.common.widgets.linkCards.cardWindow.CMWidgetButton', {
			widgetDefinition: widget,
			handler: function() {
				me.fireEvent(me.CMEVENTS.widgetButtonClick, widget);
			}
		}));
	},

	displayMode: function displayMode() {
		this.items.each(function(i) {
			i.disable();
		});
	},

	editMode: function editMode() {
		this.items.each(function(i) {
			i.enable();
		});
	},

	removeAllButtons: function removeAllButtons() {
		this.removeAll();
		this.hide();
		this._hidden = true;
	}
});
Ext.define("CMDBuild.view.management.common.widgets.linkCards.cardWindow.CMWidgetButtonsPanelPopup", {
	extend: "CMDBuild.view.management.common.widgets.linkCards.cardWindow.CMWidgetButtonsPanel",
	addWidget: function addWidget(widget) {
		var me = this;
		if (me._hidden) {
			me.show();
			me._hidden = false;
		}

		me.add(Ext.create('CMDBuild.view.management.common.widgets.linkCards.cardWindow.CMWidgetButton', {
			widgetDefinition: widget,
			handler: function() {
				me.delegate.onWidgetButtonClick(widget);
			}
		}));
	}
});
