(function() {

	/**
	 * Class with adapted functions to be compatible with old FieldManager
	 */
	Ext.define('CMDBuild.view.common.field.filter.advanced.window.panels.attributes.FieldSet', {
		extend: 'Ext.form.FieldSet',

		mixins: {
			delegable: 'CMDBuild.core.CMDelegable',
			conditionDelegate: 'CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanelDelegate'
		},

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.panels.Attributes}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		attributeName: undefined,

		defaults: {
			padding: '0 0 5 0'
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this, 'CMDBuild.view.management.common.filter.CMFilterAttributes.AttributeFieldsetDelegate');

			this.callParent(arguments);
		},

		addCondition: function(condition) {
			condition.addDelegate(this);

			if (this.items.length >= 1)
				this.items.last().showOr();

			Ext.suspendLayouts();
			this.add(condition);
			Ext.resumeLayouts();
		},

		getData: function() {
			var data = [];
			var out = {};

			this.items.each(function(i) {
				if (typeof i.getData == 'function')
					data.push(i.getData());
			});

			if (data.length == 1) {
				out = data[0];
			} else if (data.length > 1) {
				out = {
					or: data
				};
			}

			return out;
		},

		// as conditionDelegate
		onFilterAttributeConditionPanelRemoveButtonClick: function(condition) {
			Ext.suspendLayouts();
			this.remove(condition);
			Ext.resumeLayouts();

			var count = this.items.length;

			if (count > 0) {
				this.items.last().hideOr();
			} else {
				this.delegate.cmfg('onFieldFilterAdvancedWindowAttributesFieldSetEmptied', this.attributeName);
			}
		}
	});

})();