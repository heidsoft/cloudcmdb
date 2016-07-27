(function () {

	/**
	 * To fix an error (Uncaught TypeError: Cannot read property 'setSize' of undefined) occured when editing a workflow grid u change task clicking on grid
	 */
	Ext.define('CMDBuild.override.layout.container.Editor', {
		override: 'Ext.layout.container.Editor',

		/**
		 * @param {Object} ownerContext
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		calculate: function (ownerContext) {
			var me = this;
			var owner = me.owner;
			var autoSize = owner.autoSize;
			var fieldWidth;
			var fieldHeight;

			if (autoSize === true)
				autoSize = me.autoSizeDefault;

			// Calculate size of both Editor, and its owned Field
			if (autoSize) {
				fieldWidth  = me.getDimension(owner, autoSize.width,  'getWidth',  owner.width);
				fieldHeight = me.getDimension(owner, autoSize.height, 'getHeight', owner.height);
			}

			// Set Field size
			if (!Ext.isEmpty(ownerContext.childItems[0])) // FIX
				ownerContext.childItems[0].setSize(fieldWidth, fieldHeight);

			// Bypass validity checking. Container layouts should not usually set their owner's size.
			ownerContext.setWidth(fieldWidth);
			ownerContext.setHeight(fieldHeight);

			// This is a Container layout, so publish content size
			ownerContext.setContentSize(fieldWidth || owner.field.getWidth(), fieldHeight || owner.field.getHeight());
		}
	});

})();
