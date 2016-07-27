/**
 * @class CMDBuild.WidgetBuilders.DecimalAttribute
 * @extends CMDBuild.WidgetBuilders.RangeQueryAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.DecimalAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.DecimalAttribute, CMDBuild.WidgetBuilders.RangeQueryAttribute);
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.MAXWIDTH = 100;
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.customVType = "numeric";
/**
 * @override
 * @param attribute
 * @return object
 */
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.buildGridHeader = function(attribute) {
	return {
		header: attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden: !attribute.isbasedsp,
		flex: this.MAXWIDTH
	};
};
/**
 * @override
 * @param attribute
 * @return Ext.form.TextField
 */
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.buildAttributeField = function(attribute) {
	return new Ext.form.TextField({
		labelAlign: "right",
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		fieldLabel: attribute.description || attribute.name,
		name: attribute.name,
		allowBlank: !attribute.isnotnull,
		width: CMDBuild.core.constants.FieldWidths.LABEL + this.MAXWIDTH,
		scale: attribute.scale,
		precision: attribute.precision,
		vtype: this.customVType,
		CMAttribute: attribute
	});
};