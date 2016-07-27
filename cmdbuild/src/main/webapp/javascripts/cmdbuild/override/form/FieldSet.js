(function () {

	Ext.define('CMDBuild.override.form.FieldSet', {
		override: 'Ext.form.FieldSet',

		/**
		 * @cfg {Mixed}
		 */
		checkboxValue: undefined,

		/**
		 * An ExtJs fix for CellEditing plugin within FieldSet 21/03/2014
		 *
		 * @returns {Ext.form.FieldSet}
		 */
		setExpanded: function (expanded) {
			var me = this;
			var checkboxCmp = me.checkboxCmp;
			var operation = expanded ? 'expand' : 'collapse';

			if (!me.rendered || me.fireEvent('before' + operation, me) !== false) {
				expanded = !!expanded;

				if (checkboxCmp)
					checkboxCmp.setValue(expanded);

				if (expanded) {
					me.removeCls(me.baseCls + '-collapsed');
				} else {
					me.addCls(me.baseCls + '-collapsed');
				}

				me.collapsed = !expanded;

				if (expanded) {
					delete me.getHierarchyState().collapsed;
				} else {
					me.getHierarchyState().collapsed = true;
				}

				if (me.rendered) {
					// say explicitly we are not root because when we have a fixed/configured height
					// our ownerLayout would say we are root and so would not have it's height
					// updated since it's not included in the layout cycle
					me.updateLayout({ isRoot: false });
					me.fireEvent(operation, me);
				}
			}

			return me;
		},

		/**
		 * An ExtJs fix to have a correct fields label and field width in FieldSet - 08/04/2014
		 *
		 * @returns {Void}
		 */
		fieldWidthsFix: function () {
			this.cascade(function (item) {
				if (Ext.isEmpty(item.checkboxToggle) && item instanceof Ext.form.Field) {
					item.labelWidth = item.labelWidth - 10;
					item.width = item.width - 10;
				}
			});
		},

		/**
		 * An ExtJs feature implementation to reset function for FieldSet - 08/04/2014
		 *
		 * @returns {Void}
		 */
		reset: function () { // Resets all items except fieldset toglecheckbox
			this.cascade(function (item) {
				if (Ext.isEmpty(item.checkboxToggle))
					item.reset();
			});
		},


		//

		/**
		 * Implementation of "checkboxValue" property that allow to return different values relatively to each fieldset
		 *
		 * @returns {Ext.Component}
		 *
		 * @override
		 */
		createCheckboxCmp: function () {
			var me = this;
			var suffix = '-checkbox';

			this.checkboxCmp = Ext.widget({
				xtype: 'checkbox',
				hideEmptyLabel: true,
				name: me.checkboxName || me.id + suffix,
				cls: me.baseCls + '-header' + suffix,
				id: me.id + '-legendChk',
				checked: !me.collapsed,
				inputValue: me.checkboxValue || 'on',

				listeners: {
					change: me.onCheckChange,
					scope: me
				}
			});

			return this.checkboxCmp;
		}
	});

})();
