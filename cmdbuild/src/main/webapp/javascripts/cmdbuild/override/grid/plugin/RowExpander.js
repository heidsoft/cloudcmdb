(function() {

	Ext.define('CMDBuild.override.grid.plugin.RowExpander', {
		override: 'Ext.grid.plugin.RowExpander',

		collapseAll: function() {
			Ext.Array.each(this.grid.getStore().getRange(), function(record, i, allRecords) {
				if(this.recordsExpanded[record.internalId])
					this.toggleRow(record.index, record);
			}, this);
		}
	});

})();
