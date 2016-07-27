/**
 * @deprecated (use new field manager)
 */
CMDBuild.Management.ForeignKeyField = (function() {
	return {
		build: function(attribute) {
			var store = _CMCache.getForeignKeyStore(attribute);

			var field = new CMDBuild.view.common.field.SearchableCombo({
				plugins: new CMDBuild.SetValueOnLoadPlugin(),
				fieldLabel: attribute.description,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name: attribute.name,
				store: store,
				queryMode: 'local',
				valueField: 'Id',
				displayField: 'Description',
				triggerAction: 'all',
				allowBlank: !attribute.isnotnull,
				grow: true, // XComboBox autogrow
				minChars: 1,
				filtered: false,
				CMAttribute: attribute
			});

			return field;
		}
	};
})();