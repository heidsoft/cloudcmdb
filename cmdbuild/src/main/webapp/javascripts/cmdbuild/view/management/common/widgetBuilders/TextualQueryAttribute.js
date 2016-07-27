/**
 * @class CMDBuild.WidgetBuilders.TextualQueryAttribute
 * @extends CMDBuild.WidgetBuilders.BaseAttribute
 *
 */
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.TextualQueryAttribute = function(){};
CMDBuild.extend(CMDBuild.WidgetBuilders.TextualQueryAttribute, CMDBuild.WidgetBuilders.BaseAttribute);
/**
 * @override
 */
CMDBuild.WidgetBuilders.TextualQueryAttribute.prototype.getQueryOptions = function() {
	var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
	return [
		[operator.BEGIN, CMDBuild.Translation.beginsWith],
		[operator.CONTAIN, CMDBuild.Translation.contains],
		[operator.END, CMDBuild.Translation.endsWith],
		[operator.EQUAL, CMDBuild.Translation.equals],
		[operator.NOT_BEGIN, CMDBuild.Translation.doesNotBeginWith],
		[operator.NOT_CONTAIN, CMDBuild.Translation.doesNotContain],
		[operator.NOT_END, CMDBuild.Translation.doesNotEndWith],
		[operator.NOT_EQUAL, CMDBuild.Translation.different],
		[operator.NOT_NULL, CMDBuild.Translation.isNotNull],
		[operator.NULL, CMDBuild.Translation.isNull]
	];
};

CMDBuild.WidgetBuilders.TextualQueryAttribute.prototype.getDefaultValueForQueryCombo = function() {
	return CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator.CONTAIN;
};

/**
 * @override
 */
CMDBuild.WidgetBuilders.TextualQueryAttribute.prototype.needsDoubleFielForQuery = function() {
	return false;
};