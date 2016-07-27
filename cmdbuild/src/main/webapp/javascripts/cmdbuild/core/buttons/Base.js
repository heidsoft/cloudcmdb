(function() {

	Ext.define('CMDBuild.core.buttons.Base', {
		extend: 'Ext.button.Button',

		/**
		 * Default text value if not specified
		 *
		 * @cfg {String}
		 */
		textDefault: undefined,

		/**
		 * @cfg {Boolean}
		 */
		withSpacer: false,

		initComponent: function() {
			// Button minWidth setup
			if (Ext.isEmpty(this.iconCls))
				Ext.apply(this, {
					minWidth: 75
				});

			// Apply defaultText property if no defined text and tooltip properties
			if (Ext.isEmpty(this.text, true) && Ext.isEmpty(this.tooltip))
				Ext.apply(this, {
					text: this.textDefault
				});

			// Add spacer class when button is used as grid icon
			if (!Ext.isEmpty(this.tooltip) && this.withSpacer)
				Ext.apply(this, {
					iconCls: this.iconCls + ' cm-action-col-icon-spacer'
				});

			// IsDisabled method instantiantion
			if (!Ext.isEmpty(this.isDisabled) && Ext.isFunction(this.isDisabled))
				Ext.apply(this, {
					isDisabled: Ext.bind(this.isDisabled, this.scope)
				});

			this.on('show', function(button, eOpts) {
				// IsDisabled method execute
				if (Ext.isFunction(this.isDisabled))
					this.setDisabled(this.isDisabled());
			}, this);

			this.callParent(arguments);
		}
	});

})();