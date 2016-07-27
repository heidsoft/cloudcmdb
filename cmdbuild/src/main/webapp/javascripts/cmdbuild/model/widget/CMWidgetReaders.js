Ext.define("CMDBuild.controller.management.common.widgets.CMCalendarControllerWidgetReader", {
	getStartDate : function(w) {
		return w.startDate;
	},
	getEndDate : function(w) {
		return w.endDate;
	},
	getTitle : function(w) {
		return w.eventTitle;
	},
	getEventClass : function(w) {
		return w.eventClass;
	},
	getFilterVarName : function(w) {
		return "filter";
	},
	getDefaultDate : function(w) {
		return w.defaultDate;
	}
});