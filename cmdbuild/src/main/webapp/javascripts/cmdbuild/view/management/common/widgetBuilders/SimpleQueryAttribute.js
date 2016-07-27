/**
 * @class CMDBuild.WidgetBuilders.SimpleQueryAttribute
 * @extends CMDBuild.WidgetBuilders.BaseAttribute
 **/
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.SimpleQueryAttribute = function(){};
CMDBuild.extend(CMDBuild.WidgetBuilders.SimpleQueryAttribute, CMDBuild.WidgetBuilders.BaseAttribute);
/**
 * @override
 */
CMDBuild.WidgetBuilders.SimpleQueryAttribute.prototype.getQueryOptions = function() {
	var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
	return [
		[operator.EQUAL, CMDBuild.Translation.equals],
		[operator.NULL, CMDBuild.Translation.isNull],
		[operator.NOT_NULL, CMDBuild.Translation.isNotNull]
	];
};
/**
 * @override
 */
CMDBuild.WidgetBuilders.SimpleQueryAttribute.prototype.needsDoubleFielForQuery = function() {
	return false;
};