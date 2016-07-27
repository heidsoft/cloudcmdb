/**
 * @class CMDBuild.WidgetBuilders.ComboAttribute
 * @extends CMDBuild.WidgetBuilders.SimpleQueryAttribute
 */
CMDBuild.WidgetBuilders.ComboAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.ComboAttribute, CMDBuild.WidgetBuilders.SimpleQueryAttribute);

/**
 * @override
 * @return object
 */
CMDBuild.WidgetBuilders.ComboAttribute.prototype.buildGridHeader = function(attribute) {
	return {
		header : attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden: !attribute.isbasedsp,
		flex: 60
	};
};
/**
 * @override
 * @return Ext.form.DisplayField
 */
CMDBuild.WidgetBuilders.ComboAttribute.prototype.buildReadOnlyField = function(attribute) {
	var attr = Ext.apply({}, attribute);
	attr.name = attribute.name;

	return CMDBuild.WidgetBuilders.ComboAttribute.superclass.buildReadOnlyField(attr);
};

/**
 * @override
 */
CMDBuild.WidgetBuilders.ComboAttribute.prototype.getQueryOptions = function() {
	var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
	return [
		[operator.EQUAL, CMDBuild.Translation.equals],
		[operator.NULL, CMDBuild.Translation.isNull],
		[operator.NOT_NULL, CMDBuild.Translation.isNotNull],
		[operator.NOT_EQUAL, CMDBuild.Translation.different]
	];
};