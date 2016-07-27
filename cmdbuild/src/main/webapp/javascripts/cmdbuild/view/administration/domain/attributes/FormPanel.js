(function() {

	var tr =  CMDBuild.Translation.administration.modClass.attributeProperties;

	Ext.define("CMDBuild.view.administration.domain.attributes.FormPanel",{
		extend: "CMDBuild.view.administration.classes.CMAttributeForm",

		domainName: undefined,

		/**
		 * @property {Object}
		 */
		selectedAttribute: undefined,

		initComponent: function() {
			this.callParent(arguments);
			this.comboType.getStore().load({
				params: {
					tableType: "DOMAIN"
				}
			});
		},

		onClassSelected: Ext.emptyFn,

		onDomainSelected: function(cmDomain) { // Probably not used
			this.domainName = cmDomain.getName();
			this.hideContextualFields();
		},

		onAttributeSelected : function(attribute) {
			this.reset();

			if (attribute) {
				var attributeData = attribute.raw || attribute.data;

				this.selectedAttribute = attributeData;

				this.getForm().setValues(attributeData);
				this.disableModify(enableCMTbar = true);
				this.deleteButton.setDisabled(attribute.get("inherited"));
				this.hideContextualFields();
				this.showContextualFieldsByType(attribute.get("type"));
			}
		},

		buildBasePropertiesPanel: function() {
			this.baseProperties = new Ext.form.FieldSet({
				title: tr.baseProperties,
				margin: '0 3 0 0',
				autoScroll: true,
				defaultType: "textfield",
				flex: 1,
				items: [
					this.attributeName,
					this.attributeDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
						allowBlank: false,
						vtype: 'commentextended',

						listeners: {
							scope: this,
							enable: function(field, eOpts) { // TODO: on creation, domainName should be already known (refactor)
								field.translationFieldConfig = {
									type: CMDBuild.core.constants.Proxy.ATTRIBUTE_DOMAIN,
									owner: this.domainName,
									identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
									field: CMDBuild.core.constants.Proxy.DESCRIPTION
								};

								field.translationsRead();
							}
						}
					}),
					this.isBasedsp,
					this.attributeUnique,
					this.attributeNotNull,
					this.isActive,
					{
						xtype: "hidden",
						name: "meta"
					},
					this.fieldMode
				]
			});
		}

	});
})();