(function () {

	Ext.define('CMDBuild.view.management.classes.tabs.history.RowExpander', {
		extend: 'Ext.grid.plugin.RowExpander',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		expandOnEnter: false,
		selectRowOnExpand: false,

		// XTemplate formats all values to an array of key-value objects before display
		rowBodyTpl: new Ext.XTemplate(
			'<tpl exec="this.formatter(' + CMDBuild.core.constants.Proxy.VALUES + ')"></tpl>',
			'<tpl for="this.formattedArray">',
				'<tpl if="' + CMDBuild.core.constants.Proxy.CHANGED + '">',
					'<p class="changedRow">',
				'<tpl else>',
					'<p>',
				'</tpl>',
				'<b>{attribute}:</b> {value}</p>',
			'</tpl>',
			'<tpl if="this.formattedArray.length == 0">',
				'<p>' + CMDBuild.Translation.noAvailableData + '<p>',
			'</tpl>',
			{
				/**
				 * @param {Object} values
				 */
				formatter: function (values) {
					if (!Ext.isEmpty(values)) {
						this.formattedArray = [];

						Ext.Object.each(values, function (key, value, myself) {
							this.formattedArray.push({
								attribute: value.get(CMDBuild.core.constants.Proxy.ATTRIBUTE_DESCRIPTION) || key,
								changed: value.get(CMDBuild.core.constants.Proxy.CHANGED),
								index: value.get(CMDBuild.core.constants.Proxy.INDEX),
								value: value.get(CMDBuild.core.constants.Proxy.DESCRIPTION)
							});
						}, this);

						// Sort by index value (CMDBuild attribute sort order)
						CMDBuild.core.Utils.objectArraySort(this.formattedArray, CMDBuild.core.constants.Proxy.INDEX);
					}
				}
			}
		)
	});

})();
