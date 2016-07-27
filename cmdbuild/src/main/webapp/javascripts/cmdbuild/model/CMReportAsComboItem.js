(function() {
	//REPORT

	var _REPORT_FIELDS = {
		value: "title",
		description: "description",
		id: "id"
	};

	Ext.define('CMDBuild.model.CMReportAsComboItem', {
		statics: {
			_FIELDS: _REPORT_FIELDS
		},
		extend: 'Ext.data.Model',
		fields: [
			{name: _REPORT_FIELDS.value, type: "string"},
			{name: _REPORT_FIELDS.description, type: "string"},
			{name: _REPORT_FIELDS.id, type: "string"}
		]
	});

})();