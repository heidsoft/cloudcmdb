Ext.define("CMDBuild.delegate.administration.common.basepanel.CMBaseGridConfigurator", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMGridConfigurator",

	/**
	 * @return a Ext.data.Store to use for the grid
	 */
	getStore: function() {

	},

	/**
	 * @return an array of Ext.grid.column.Column to use for the grid
	 */
	getColumns: function() {
		return [{
			header: CMDBuild.Translation.administration.modClass.attributeProperties.name,
			dataIndex: CMDBuild.core.constants.Proxy.NAME,
			flex: 1
		}, {
			header: CMDBuild.Translation.administration.modClass.attributeProperties.description,
			dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
			flex: 1
		}];
	}
});
