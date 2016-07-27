(function() {

	var ARROW_ELEMENT_SELECTOR = '.x-btn-split';
	var ARROW_CLASS = 'x-btn-split-right';

	Ext.define('CMDBuild.core.buttons.AddCardMenuButton', {
		alternateClassName: 'CMDBuild.AddCardMenuButton', // Legacy class name
		extend: 'Ext.button.Split',

		translation: CMDBuild.Translation.management.moddetail,
		iconCls: 'add',

		//custom fields
		cmName: undefined,
		baseText: CMDBuild.Translation.management.modcard.add_card,
		textPrefix: CMDBuild.Translation.management.modcard.add_card,

		//private
		initComponent: function() {
			this.subClasses = {};
			Ext.apply(this, {
				text: this.baseText,
				menu : {items :[]},
				handler: onClick,
				scope: this
			});

			this.callParent(arguments);
		},

		updateForEntry: function(entry) {
			if (!entry) {
				return;
			}

			this.classId = entry.get('id');
			fillMenu.call(this, entry);

			var privileges = _CMUtils.getClassPrivileges(this.classId);
			if (_CMUtils.isSuperclass(this.classId)) {
				this.setDisabled(this.isEmpty() || privileges.crudDisabled.create);
				this.showDropDownArrow();
			} else {
				this.setDisabled(!privileges.create || privileges.crudDisabled.create);
				this.hideDropDownArrow();
			}
		},

		/**
		 * Enable only if is not superclass or if is superclass not empty
		 *
		 * @override
		 */
		enable: function () {
			if (
				!Ext.isEmpty(this.classId)
				&& (
					!_CMUtils.isSuperclass(this.classId)
					|| (_CMUtils.isSuperclass(this.classId) && !this.isEmpty())
				)
			) {
				this.callParent(arguments);
			}
		},

		showDropDownArrow: function() {
			var arrowEl = this.getArrowEl();
			if (arrowEl) {
				arrowEl.addCls(ARROW_CLASS);
			}
		},

		hideDropDownArrow: function() {
			var arrowEl = this.getArrowEl();
			if (arrowEl) {
				arrowEl.removeCls(ARROW_CLASS);
			}
		},

		disableIfEmpty: function() {
			if (this.isEmpty()) {
				this.disable();
			} else {
				this.enable();
			}
		},

		setTextSuffix: function(suffix) {
			this.setText(this.textPrefix +' '+suffix);
		},

		getArrowEl: function() {
			try {
				var arrowEl = Ext.DomQuery.selectNode(ARROW_ELEMENT_SELECTOR, this.el.dom);
				return Ext.get(arrowEl);
			} catch (e) {
				return null;
			}
		},

		//private
		isEmpty: function() {
			return (this.menu && this.menu.items.length == 0);
		},

		//private
		resetText: function() {
			this.setText(this.baseText);
		}
	});

	var WANNABE_DESCRIPTION = 'text';

	function fillMenu(entry) {
		this.menu.removeAll();

		if (entry) {
			var entryId = entry.get('id'),
				isSuperclass = _CMUtils.isSuperclass(entryId);

			this.setTextSuffix(entry.data.text);

			if (isSuperclass) {
				var descendants = _CMUtils.getDescendantsById(entryId);

				Ext.Array.sort(descendants, function(et1, et2) {
						return et1.get(WANNABE_DESCRIPTION) >= et2.get(WANNABE_DESCRIPTION);
					});

				for (var i=0; i<descendants.length; ++i) {
					var d = descendants[i];
					addSubclass.call(this, d);
				}
			}
		}
	}

	function addSubclass(entry) {
		var privileges = _CMUtils.getClassPrivileges(entry.get('id'));

		if (privileges.create && ! privileges.crudDisabled.create) {
			this.menu.add({
				text: entry.get('text'),
				subclassId: entry.get('id'),
				subclassName: entry.get('text'),
				scope: this,
				handler: function(item, e){
					this.fireEvent('cmClick', {
						classId: item.subclassId,
						className: item.subclassName
					});
				}
			});
		};
	}

	function onClick() {
		//Extjs calls the handler even when disabled
		if (!this.disabled) {
			if (this.isEmpty()) {
				this.fireEvent('cmClick', {
					classId: this.classId,
					className: this.text
				});
			} else {
				this.showMenu();
			}
		}
	}

})();