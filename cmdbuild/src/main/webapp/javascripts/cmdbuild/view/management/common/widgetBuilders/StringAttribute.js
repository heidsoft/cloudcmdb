/**
 * @class CMDBuild.WidgetBuilders.StringAttribute
 * @extends CMDBuild.WidgetBuilders.TextualQueryAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.StringAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.StringAttribute, CMDBuild.WidgetBuilders.TextualQueryAttribute);

CMDBuild.WidgetBuilders.StringAttribute.prototype.MAXWIDTH = 100;

/**
 * @override
 * @param attribute
 * @return object
 */
CMDBuild.WidgetBuilders.StringAttribute.prototype.buildGridHeader = function(attribute) {
	var innerTextWidth = attribute.len * 10;
	if (innerTextWidth > this.MAXWIDTH) {
		innerTextWidth = this.MAXWIDTH;
	}

	return {
		header : attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden: !attribute.isbasedsp,
		flex: innerTextWidth,
		renderer: CMDBuild.Utils.Format.htmlEntityEncode
	};
};

/**
 * @override
 * @param attribute
 * @return Ext.form.DisplayField
 */
CMDBuild.WidgetBuilders.StringAttribute.prototype.buildReadOnlyField = function(attribute) {
	var field = new CMDBuild.view.common.field.CMDisplayField({
		labelAlign: "right",
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		fieldLabel: attribute.description || attribute.name,
		width: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
		submitValue: false,
		name: attribute.name,
		disabled: false,
		style: {
			overflow: "hidden"
		},

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
 * @param attribute
 * @return Ext.form.TextField or Ext.form.TextField
 */
CMDBuild.WidgetBuilders.StringAttribute.prototype.buildAttributeField = function(attribute) {
	var field;
	if (attribute.len > this.MAXWIDTH) {
		field = new Ext.form.TextArea({
			labelAlign: "right",
			labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
 			fieldLabel: attribute.description || attribute.name,
    		name: attribute.name,
    		allowBlank: !attribute.isnotnull,
    		width: CMDBuild.core.constants.FieldWidths.STANDARD_BIG
		});
	} else {
		field = new Ext.form.TextField({
			labelAlign: "right",
			labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
 			fieldLabel: attribute.description || attribute.name,
 			name: attribute.name,
    		maxLength: attribute.len,
    		allowBlank: !attribute.isnotnull,
			width: CMDBuild.core.constants.FieldWidths.LABEL + (function(length) {
				length = (length * 13) + 12; // arbitrary choice
				if (length < CMDBuild.core.constants.FieldWidths.STANDARD_BIG_FIELD_ONLY) {
    				return length;
    			} else {
    				return CMDBuild.core.constants.FieldWidths.STANDARD_BIG_FIELD_ONLY;
    			}
    		})(attribute.len)
		});
	};
	if (this.customVType) {
		field.vtype = this.customVType;
	};
	field.CMAttribute = attribute;
	return field;
};

CMDBuild.WidgetBuilders.StringAttribute.prototype.buildCellEditor = function(attribute) {
	return new Ext.form.TextField({
		labelAlign: "right",
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		fieldLabel: attribute.description || attribute.name,
		name: attribute.name,
		maxLength: attribute.len,
		allowBlank: !attribute.isnotnull
	});
}