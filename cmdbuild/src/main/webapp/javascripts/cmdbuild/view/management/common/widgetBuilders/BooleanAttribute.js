/**
 * @class CMDBuild.WidgetBuilders.BooleanAttribute
 * @extends CMDBuild.WidgetBuilders.SimpleQueryAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.BooleanAttribute = function(){};
CMDBuild.extend(CMDBuild.WidgetBuilders.BooleanAttribute, CMDBuild.WidgetBuilders.SimpleQueryAttribute);
/**
 * @override
 * @return Ext.grid.CheckColumn
 */
CMDBuild.WidgetBuilders.BooleanAttribute.prototype.buildGridHeader = function(attribute) {
	var headerWidth =  attribute.name.length * 9;

	var h = new Ext.ux.CheckColumn({
		header : attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden : !attribute.isbasedsp,
		width : headerWidth,
		cmReadOnly: true
	});

	if (
		!Ext.isEmpty(attribute)
		&& !Ext.isEmpty(attribute.fieldmode)
		&& attribute.fieldmode == "read"
	) { // ReadOnly mode for us CheckColumn with processEvent parameter override
		h = new Ext.ux.CheckColumn({
			header : attribute.description,
			sortable : true,
			dataIndex : attribute.name,
			hidden : !attribute.isbasedsp,
			width : headerWidth,
			cmReadOnly: true,

			processEvent: Ext.emptyFn
		});
	}

	return h;
};
/**
 * @override
 * @return Ext.form.BooleanDisplayField
 */
CMDBuild.WidgetBuilders.BooleanAttribute.prototype.buildReadOnlyField = function(attribute) {
	var field = new Ext.form.BooleanDisplayField ({
		labelAlign: "right",
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		fieldLabel: attribute.description,
		name: attribute.name,
		disabled: false,

		/**
		 * Validate also display field
		 *
		 * @override
		 */
		isValid: function() {
			if (this.allowBlank)
				return true;

			return !Ext.isEmpty(this.getValue());
		}
	});

	return this.markAsRequired(field, attribute);
};
/**
 * @override
 * @return Ext.ux.form.XCheckbox
 */
CMDBuild.WidgetBuilders.BooleanAttribute.prototype.buildAttributeField = function(attribute) {
	return new Ext.ux.form.XCheckbox({
		labelAlign: "right",
		fieldLabel: attribute.description || attribute.name,
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		name: attribute.name,
		CMAttribute: attribute
	});
};