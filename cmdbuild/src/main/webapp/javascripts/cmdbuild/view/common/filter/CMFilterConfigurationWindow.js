/**
 * @deprecated new class (CMDBuild.view.common.field.filter.advanced.Advanced)
 */

Ext.define("CMDBuild.view.common.filter.CMFilterConfigurationWindow", {
	extend: "CMDBuild.view.management.common.filter.CMFilterWindow",

	// configuration
	/**
	 * set it as true to have only the
	 * close button
	 */
	readOnly: false,
	// configuration

	// override
	buildButtons: function() {
		var me = this;

		var buttons = [{
			text: CMDBuild.Translation.cancel,
			handler: function() {
				me.callDelegates("onCMFilterWindowAbortButtonClick", [me]);
			}
		}];

		if (this.readOnly) {
			this.buttons = buttons;
		} else {
			this.buttons = [{
				text: CMDBuild.Translation.save,
				handler: function() {
					me.callDelegates("onCMFilterWindowSaveButtonClick", [me, me.getFilter()]);
				}
			}].concat(buttons);
		}

	},

	// override
	buildFilterAttributePanel: function() {
		return new CMDBuild.view.management.common.filter.CMFilterAttributes({
			attributes: this.attributes,
			className: this.className,
			readOnly: this.readOnly
		});
	}
});