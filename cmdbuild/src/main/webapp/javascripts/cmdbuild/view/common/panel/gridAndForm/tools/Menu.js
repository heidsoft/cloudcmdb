(function () {

	Ext.define('CMDBuild.view.common.panel.gridAndForm.tools.Menu', {
		extend: 'Ext.panel.Tool',

		/**
		 * @param {Ext.EventObject} e
		 * @param {HTMLElement} target
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 * @override
		 */
		onClick: function (e, target) {
			if (this.fireEvent('beforeshow')) {
				if (!Ext.isEmpty(this.menu) && Ext.isArray(this.menu))
					Ext.apply(this, {
						menu: Ext.create('Ext.menu.Menu', {
							items: this.menu
						})
					});

				if (!Ext.isEmpty(this.menu) && Ext.getClassName(this.menu) == 'Ext.menu.Menu') {
					this.menu.showAt(0, 0);
					this.menu.showAt(
							this.getX() + this.getWidth() - this.menu.getWidth(),
							this.getY() + this.getHeight()
					);
				}
			}

			return this.callParent(arguments);
		},

		/**
		 * @override
		 */
		onDestroy: function () {
			Ext.destroyMembers(this, 'menu');

			this.callParent(arguments);
		},

		listeners: {
			beforeshow: function (tool, eOpts) {
				// BeforeShow event forwarding
				if (
					!Ext.isEmpty(this.menu)
					&& !Ext.isEmpty(this.menu.items) && Ext.isFunction(this.menu.items.getRange)
				) {
					Ext.Array.forEach(this.menu.items.getRange(), function (item, i, allItems) {
						item.fireEvent('beforeshow');
					}, this);
				}
			}
		}
	});

})();
