(function() {

	TEXT_EDITOR_TYPE = {
		plain: "PLAIN",
		html: "HTML"
	};

	/**
	 * @class CMDBuild.WidgetBuilders.TextAttribute
	 * @extends CMDBuild.WidgetBuilders.StringAttribute
	 */
	Ext.ns("CMDBuild.WidgetBuilders");
	CMDBuild.WidgetBuilders.TextAttribute = function() {};
	CMDBuild.extend(CMDBuild.WidgetBuilders.TextAttribute, CMDBuild.WidgetBuilders.StringAttribute);

	/**
	 * @override
	 * @param attribute
	 * @return Ext.form.TextArea
	 */
	CMDBuild.WidgetBuilders.TextAttribute.prototype.buildAttributeField = function(attribute) {
		if (attribute.editorType != TEXT_EDITOR_TYPE.html) {
			var attr = Ext.apply({},attribute);
			attr.len = this.MAXWIDTH + 1; // MAXWIDTH is the length for switching to a textarea
			return CMDBuild.WidgetBuilders.TextAttribute.superclass.buildAttributeField(attr);
		} else {
			var editor = Ext.create('CMDBuild.view.common.field.HtmlEditor', {
				labelAlign: 'right',
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.EDITOR_HTML,
	 			fieldLabel: attribute.description || attribute.name,
	 			name: attribute.name,
	 			disabled: false,
				CMAttribute: attribute
			});

			return editor;
		}
	};

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
			renderer: 'stripTags'
		};
	};

})();