(function() {

	Ext.define('CMDBuild.view.common.field.GridLocalSearch', {
		extend: 'CMDBuild.field.GridSearchField',

		/**
		 * @cfg {Ext.grid.Panel}
		 */
		grid: undefined,

		/**
		 * Filter the loaded record comparing every data element with the content of the field (case insensitive)
		 */
		onTrigger1Click: function() {
			var query = this.getValue() || '';

			this.grid.getStore().clearFilter();
			this.grid.getStore().filterBy(function(record, id) {
				var returnValue = false;

				Ext.Object.each(record.getData(), function(key, value, myself) {
					if (Ext.util.Format.lowercase(value).indexOf(Ext.util.Format.lowercase(query)) >= 0)
						returnValue = true;
				}, this);

				return returnValue;
			}, this);
		},

		onTrigger2Click: function() {
			this.grid.getStore().clearFilter();

			this.setValue();
		},

		reset: function() {
			this.onTrigger2Click();
		}
	});

})();