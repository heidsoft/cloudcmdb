(function () {

	Ext.define('CMDBuild.controller.management.common.CMModClassAndWFCommons', {
		/**
		 * Retrieve the form to use as target for the templateResolver asking it to its view
		 *
		 * @return {Object} or null if something is not right
		 */
		getFormForTemplateResolver: function() {
			var form = null;

			if (this.view) {
				var wm = this.view.getWidgetManager();

				if (wm && typeof wm.getFormForTemplateResolver == 'function')
					form = wm.getFormForTemplateResolver() || null;
			}

			return form;
		}
	});

})();