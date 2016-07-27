(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.grid.RowEdit', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.Grid}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onRowEditWindowAbortButtonClick',
			'onRowEditWindowSaveButtonClick'
		],

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @cfg {Ext.data.Model}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.RowEditWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.widgets.grid.Grid} configurationObject.parentDelegate
		 * @param {Object} configurationObject.record
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.grid.RowEditWindow', {
				delegate: this
			});

			// Shorthand
			this.form = this.view.form;

			this.form.add(this.buildFormFields());
			this.form.loadRecord(this.record);

			this.fieldsInitialization();

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * @return {Array} itemsArray
		 */
		buildFormFields: function() {
			var itemsArray = [];

			Ext.Array.forEach(this.cmfg('getCardAttributes'), function(attribute, i, allAttributes) {
				var item = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);

				if (attribute[CMDBuild.core.constants.Proxy.FIELD_MODE] == 'read')
					item.setDisabled(true);

				itemsArray.push(item);
			}, this);

			return itemsArray;
		},

		/**
		 * Calls field template resolver and store load
		 */
		fieldsInitialization: function() {
			Ext.Array.forEach(this.form.getForm().getFields().getRange(), function(field, i, allFields) {
				if (!Ext.Object.isEmpty(field) && !Ext.isEmpty(field.resolveTemplate))
					field.resolveTemplate();

				// Force editor fields store load (must be done because FieldManager field don't works properly)
				if (!Ext.Object.isEmpty(field) && !Ext.Object.isEmpty(field.store) && field.store.count() == 0)
					field.store.load();
			}, this);
		},

		/**
		 * Accept in input only dates with format "dd/mm/yy" and switch dd and mm to fix a bug that grid columns takes default format and not configured one
		 * TODO: should be fixed applying this function only to date fields (testing attributes object)
		 *
		 * @param {Object} value
		 *
		 * @return {String or Object}
		 */
		formatDate: function(value) {
			if (
				!Ext.isEmpty(value)
				&& Ext.isString(value)
				&& !/<[a-z][\s\S]*>/i.test(value) // Avoids to format HTML strings
			) {
				var splittedDate = value.split('/');

				if (splittedDate.length == 3)
					return new Date(splittedDate[1] + '/' + splittedDate[0] + '/' + splittedDate[2]);
			}

			return value;
		},

		onRowEditWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Saves data to widget's grid
		 */
		onRowEditWindowSaveButtonClick: function() {
			Ext.Object.each(this.form.getValues(), function(key, value, myself) {
				this.record.set(key, this.formatDate(value));
			}, this);

			this.onRowEditWindowAbortButtonClick();
		}
	});

})();