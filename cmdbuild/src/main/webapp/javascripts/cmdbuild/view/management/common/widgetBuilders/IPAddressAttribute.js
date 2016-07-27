/**
 * @class CMDBuild.WidgetBuilders.IPAddressAttribute
 * @extends CMDBuild.WidgetBuilders.DecimalAttribute
 */
	Ext.ns("CMDBuild.WidgetBuilders");
	CMDBuild.WidgetBuilders.IPAddressAttribute = function() {};
	CMDBuild.extend(CMDBuild.WidgetBuilders.IPAddressAttribute, CMDBuild.WidgetBuilders.DecimalAttribute);
	CMDBuild.WidgetBuilders.IPAddressAttribute.prototype.MAXWIDTH = 150;
	CMDBuild.WidgetBuilders.IPAddressAttribute.prototype.customVType = "ipv4";
	CMDBuild.WidgetBuilders.IPAddressAttribute.prototype.getQueryOptions = function() {
		var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
		return [
			[operator.EQUAL, CMDBuild.Translation.equals],
			[operator.NOT_NULL, CMDBuild.Translation.isNotNull],
			[operator.NET_CONTAINS, CMDBuild.Translation.contains],
			[operator.NET_CONTAINED, CMDBuild.Translation.contained],
			[operator.NET_CONTAINSOREQUAL, CMDBuild.Translation.containsorequal],
			[operator.NET_CONTAINEDOREQUAL, CMDBuild.Translation.containedorequal],
			[operator.NET_RELATION, CMDBuild.Translation.relation]
		];
	};
	CMDBuild.WidgetBuilders.IPAddressAttributeV6 = function() {};
	CMDBuild.extend(CMDBuild.WidgetBuilders.IPAddressAttributeV6, CMDBuild.WidgetBuilders.IPAddressAttribute);
	CMDBuild.WidgetBuilders.IPAddressAttributeV6.prototype.customVType = "ipv6";
	CMDBuild.WidgetBuilders.IPAddressAttributeV6.prototype.MAXWIDTH = 300;
