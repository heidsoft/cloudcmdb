(function() {
	var tableTypeMap = {
		simpletable: "SIMPLECLASS",
		standard: "CLASS"
	},

	TEXT_EDITOR_TYPE = {
		plain: "PLAIN",
		html: "HTML"
	};
	IP_TYPE = {
		ipv4: "ipv4",
		ipv6: "ipv6"
	};

	function getTableType(classObj) {
		return tableTypeMap[classObj.get("tableType")];
	}

	function cannotHaveUniqueAttributes(classObj) {
		return isSuperClass(classObj) || isSimpleClass(classObj);
	}

	function cannotHaveNotNullAttributes(classObj) {
		return isSuperClass(classObj);
	}

	function isSuperClass(classObj) {
		return classObj && classObj.get("superclass");
	}

	function isSimpleClass(classObj) {
		return getTableType(classObj) == tableTypeMap.simpletable;
	}

	var tr =  CMDBuild.Translation.administration.modClass.attributeProperties;

	// FIXME this take a store given from the cache. The model set the valueField as a
	// string and it works for the other combo. For this the data have the valueField as
	// integer, so the set value does not work (the values are taken from the attributes grid)
	Ext.define("CMDBuild.FkCombo", {
		extend: "Ext.form.ComboBox",
		setValue: function(v) {
			if (typeof v == "number") {
				v = ""+v;
			}

			this.callParent([v]);
		}
	});

	Ext.define("CMDBuild.view.administration.classes.CMAttributeForm", {
		extend: "Ext.form.Panel",

		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel}
		 */
		classObj: undefined,

		/**
		 * @property {Ext.data.Model}
		 */
		selectedAttribute: undefined,

		constructor:function() {

			this.modifyButton = new Ext.button.Button({
				iconCls : "modify",
				text : tr.modify_attribute,
				scope : this,
				handler: function() {
					this.enableModify();
				}
			});

			this.deleteButton = new Ext.button.Button({
				iconCls : "delete",
				text : tr.delete_attribute
			});

			this.saveButton = Ext.create('CMDBuild.core.buttons.text.Save');
			this.abortButton = Ext.create('CMDBuild.core.buttons.text.Abort');

			this.cmTBar = [this.modifyButton, this.deleteButton];
			this.cmButtons = [this.saveButton, this.abortButton];

			this.fieldMode = new Ext.form.ComboBox({
				name: CMDBuild.core.constants.Proxy.FIELD_MODE,
				fieldLabel: tr.field_visibility,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.MIDDLE_FIELD_WIDTH,
				valueField: "value",
				displayField: "name",
				hiddenName: "fieldmode",
				queryMode: "local",
				editable: false,
				allowBlank: false,
				store: new Ext.data.SimpleStore({
					fields: ["value","name"],
					data : [
						["write",tr.field_write],
						["read",tr.field_read],
						["hidden",tr.field_hidden]
					]
				})
			});

			this.attributeGroup = new Ext.form.ComboBox({
				name: CMDBuild.core.constants.Proxy.GROUP,
				fieldLabel: tr.group,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.MIDDLE_FIELD_WIDTH,
				valueField: "value",
				displayField: "value",
				hiddenName: "group",
				queryMode: "local",
				editable: true,
				allowBlank: true,
				store: new Ext.data.SimpleStore({
					fields: ["value"],
					data : []
				})
			});

			this.attributeName = new Ext.form.TextField( {
				fieldLabel : tr.name,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name : CMDBuild.core.constants.Proxy.NAME,
				allowBlank : false,
				vtype : "alphanum",
				cmImmutable : true
			});

			this.attributeDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
				name: CMDBuild.core.constants.Proxy.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.descriptionLabel,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				allowBlank: false,
				vtype: 'commentextended',

				listeners: {
					scope: this,
					enable: function(field, eOpts) { // TODO: on creation, classObj should be already known (refactor)
						field.translationFieldConfig = {
							type: CMDBuild.core.constants.Proxy.ATTRIBUTE_CLASS,
							owner: { sourceType: 'model', key: CMDBuild.core.constants.Proxy.NAME, source: this.classObj },
							identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
							field: CMDBuild.core.constants.Proxy.DESCRIPTION
						};

						field.translationsRead();
					}
				}
			});

			this.attributeNotNull = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isnotnull,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name : 'isnotnull'
			});

			this.attributeUnique = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isunique,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name : 'isunique'
			});

			this.isBasedsp = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isbasedsp,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name : 'isbasedsp'
			});

			this.isActive = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.isactive,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name : CMDBuild.core.constants.Proxy.ACTIVE
			});

			this.comboType = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.constants.Proxy.TYPE,
				fieldLabel: tr.type,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				displayField: CMDBuild.core.constants.Proxy.NAME,
				valueField: CMDBuild.core.constants.Proxy.VALUE,
				plugins: [ new CMDBuild.SetValueOnLoadPlugin() ],
				triggerAction: 'all',
				editable: false,
				cmImmutable: true,
				allowBlank: false,
				listConfig: {
					loadMask: false
				},

				store: CMDBuild.proxy.common.tabs.attribute.Attribute.getStoreTypes(),
				queryMode: 'local'
			});

			this.stringLength = new Ext.form.NumberField({
				fieldLabel : tr.length,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
				minValue : 1,
				maxValue : Math.pow(2, 31) - 1,
				name : 'len',
				allowBlank : false
			});

			this.decimalPrecision = new Ext.form.NumberField({
				fieldLabel : tr.precision,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
				minValue : 1,
				maxValue : 20,
				name : CMDBuild.core.constants.Proxy.PRECISION,
				allowBlank : false
			});

			this.fieldFilter = new Ext.form.TextArea( {
				fieldLabel : tr.referencequery,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name : CMDBuild.core.constants.Proxy.FILTER,
				allowBlank : true,
				vtype : "comment",
				invalidText : tr.pipeNotAllowed,
				editableOnInherited : true
			});

			this.referenceFilterMetadata = {};

			this.addMetadataBtn = Ext.create('CMDBuild.core.buttons.iconized.Modify', {
				text: CMDBuild.Translation.editMetadata,
				margin: '0 0 0 ' + (CMDBuild.core.constants.FieldWidths.LABEL + 5),
				scope: this,

				handler: function(button, e) { // TODO: would be better to use controller call (cmfg)
					Ext.create('CMDBuild.controller.administration.common.attributes.Metadata', {
						parentDelegate: this,
						data: this.referenceFilterMetadata,
						nameSpace: 'system.template.'
					});
				}
			});

			this.preselectIfUniqueCheckbox = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.core.constants.Proxy.PRESELECT_IF_UNIQUE,
				fieldLabel: CMDBuild.Translation.preselectIfUnique,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
			});

			this.decimalScale = new Ext.form.NumberField( {
				fieldLabel : tr.scale,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_SMALL,
				minValue : 1,
				maxValue : 20,
				name : CMDBuild.core.constants.Proxy.SCALE,
				allowBlank : false
			});

			this.lookupTypes = new Ext.form.ComboBox({
				plugins: [new CMDBuild.SetValueOnLoadPlugin()],
				fieldLabel : tr.lookup,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name : CMDBuild.core.constants.Proxy.LOOKUP,
				valueField : "type",
				displayField : "type",
				allowBlank : false,
				cmImmutable : true,
				store : _CMCache.getLookupTypeLeavesAsStore(),
				queryMode : "local"
			});

			this.referenceDomains = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.constants.Proxy.DOMAIN_NAME,
				fieldLabel: tr.domain,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
				valueField: CMDBuild.core.constants.Proxy.NAME,
				plugins: [ new CMDBuild.SetValueOnLoadPlugin() ],
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				allowBlank: false,
				cmImmutable: true,
				listConfig: {
					loadMask: false
				},

				store: CMDBuild.proxy.common.tabs.attribute.Attribute.getStoreRenceableDomains(),
				queryMode: 'local'
			});

			this.foreignKeyDest = new CMDBuild.FkCombo( {
				plugins: [new CMDBuild.SetValueOnLoadPlugin()],
				fieldLabel : tr.destination,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name : CMDBuild.core.constants.Proxy.FK_DESTINATION,
				hiddenName : CMDBuild.core.constants.Proxy.FK_DESTINATION,
				valueField : "name",
				displayField : "description",
				editable : false,
				allowBlank : false,
				cmImmutable : true,
				queryMode : "local",
				store : _CMCache.getClassesAndProcessesStore()
			});

			this.textAttributeWidget = new Ext.form.ComboBox({
				name: CMDBuild.core.constants.Proxy.EDITOR_TYPE,
				fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.editorType.label,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.MIDDLE_FIELD_WIDTH,
				valueField: "value",
				displayField: "name",
				queryMode: "local",
				editable: false,
				allowBlank: false,
				store: new Ext.data.SimpleStore({
					fields: ["value","name"],
					data : [
						[TEXT_EDITOR_TYPE.plain, CMDBuild.Translation.administration.modClass.attributeProperties.editorType.plain],
						[TEXT_EDITOR_TYPE.html, CMDBuild.Translation.administration.modClass.attributeProperties.editorType.html]
					]
				})
			});
			this.ipAttributeWidget = new Ext.form.ComboBox({
				name: CMDBuild.core.constants.Proxy.IP_TYPE,
				fieldLabel: CMDBuild.Translation.ipType,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.MIDDLE_FIELD_WIDTH,
				valueField: "value",
				displayField: "name",
				queryMode: "local",
				editable: false,
				allowBlank: false,
				store: new Ext.data.SimpleStore({
					fields: ["value","name"],
					data : [
						[IP_TYPE.ipv4, CMDBuild.Translation.ipv4],
						[IP_TYPE.ipv6, CMDBuild.Translation.ipv6]
					]
				})
			});

			this.contextualFields = {
				STRING : [ this.stringLength ],
				DECIMAL : [ this.decimalPrecision,this.decimalScale ],
				LOOKUP : [ this.lookupTypes ],
				FOREIGNKEY : [ this.foreignKeyDest ],
				REFERENCE : [ this.referenceDomains, this.fieldFilter, this.addMetadataBtn, this.preselectIfUniqueCheckbox ],
				TEXT: [this.textAttributeWidget],
				INET: [this.ipAttributeWidget]
			};

			this.buildBasePropertiesPanel();

			this.specificProperties = new Ext.form.FieldSet({
				margin: '0 0 0 3',
				title : tr.typeProperties,
				overflowY: 'auto',
				defaultType : "textfield",
				flex: 1,
				items: [
					this.comboType,
					this.stringLength,
					this.decimalPrecision,
					this.decimalScale,
					this.referenceDomains,
					this.foreignKeyDest,
					this.lookupTypes,
					this.fieldFilter,
					this.addMetadataBtn,
					this.preselectIfUniqueCheckbox,
					this.textAttributeWidget,
					this.ipAttributeWidget
				]
			});

			this.plugins = [new CMDBuild.FormPlugin()];
			this.callParent(arguments);
		},

		initComponent: function() {
			this.frame = false;
			this.border = false;
			this.cls = "x-panel-body-default-framed cmdb-border-top";
			this.bodyCls = 'cmdb-gray-panel';
			this.buttonAlign = "center";
			this.buttons = this.cmButtons;
			this.tbar = this.cmTBar;
			this.layout = {
				type: 'hbox',
				align: 'stretch'
			};
			this.defaults = {
				flex: 1,
				layout: {
					type: 'vbox',
					align: 'stretch'
				}
			};
			this.items = [this.baseProperties, this.specificProperties];
			this.callParent(arguments);
			this.comboType.on("select", onSelectComboType, this);
			this.attributeName.on("change", function(fieldname, newValue, oldValue) {
				this.autoComplete(this.attributeDescription, newValue, oldValue);
			}, this);
		},

		onClassSelected: function(idClass) {
			this.idClass = idClass;
			this.classObj = this.takeDataFromCache(idClass);

			if (this.classObj) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(idClass);

				this.referenceDomains.getStore().load({ params: params });

				params = {};
				params[CMDBuild.core.constants.Proxy.TABLE_TYPE] = getTableType(this.classObj);

				this.comboType.getStore().load({ params: params });

				this.hideContextualFields();
				this.attributeUnique.cmImmutable = cannotHaveUniqueAttributes(this.classObj);
				this.attributeNotNull.cmImmutable = cannotHaveNotNullAttributes(this.classObj);
			}
		},

		// private and overridden in subclasses
		takeDataFromCache: function(idClass) {
			return _CMCache.getClassById(idClass);
		},

		/**
		 * @param {Ext.data.Model} attribute
		 */
		onAttributeSelected: function(attribute) {
			this.reset();

			if (attribute) {
				this.selectedAttribute = attribute;

				this.getForm().setValues(attribute.raw);
				this.disableModify(enableCMTbar = true);
				this.deleteButton.setDisabled(attribute.get("inherited"));
				this.hideContextualFields();
				this.showContextualFieldsByType(attribute.get("type"));

				this.referenceFilterMetadata = attribute.raw.meta || {};
				this.preselectIfUniqueCheckbox.setValue(attribute.raw.meta['system.type.reference.' + CMDBuild.core.constants.Proxy.PRESELECT_IF_UNIQUE]);
			}
		},

		// override
		reset: function() {
			this.mixins.cmFormFunctions.reset.call(this);
			this.referenceFilterMetadata = {};
		},

		iterateOverContextualFields: function(type, fn) {
			var typeFields = this.contextualFields[type];
			if (typeFields) {
				for (var i=0, len=typeFields.length; i<len; i++) {
					fn(typeFields[i]);
				}
			}
		},

		showContextualFieldsByType: function(type) {
			this.iterateOverContextualFields(type, function(field) {
				field.show();
			});
		},

		showAndEnableContextualFieldsByType: function(type) {
			this.iterateOverContextualFields(type, function(field) {
				field.show();
				field.enable();
			});
		},

		hideContextualFields: function() {
			for (var type in this.contextualFields) {
				this.iterateOverContextualFields(type, function(field) {
					field.hide();
					field.disable();
				});
			}
		},

		onAddAttributeClick : function(params, enableAll) {
			this.reset();
			this.setDefaultValues();
			this.hideContextualFields();
			this.enableModify(all = true);
			if (!enableAll) {
				this.attributeUnique.setDisabled(cannotHaveUniqueAttributes(this.classObj));
				this.attributeNotNull.setDisabled(cannotHaveNotNullAttributes(this.classObj));
			}
		},

		setDefaultValues: function() {
			this.isActive.setValue(true);
			this.isBasedsp.setValue(false);
			this.fieldMode.setValue("write");
		},

		buildBasePropertiesPanel: function() {
			this.baseProperties = new Ext.form.FieldSet({
				title: tr.baseProperties,
				margin: '0 3 0 0',
				overflowY: 'auto',
				defaultType: "textfield",
				flex: 1,
				items: [
					this.attributeName,
					this.attributeDescription,
					this.attributeGroup,
					this.isBasedsp,
					this.attributeUnique,
					this.attributeNotNull,
					this.isActive,
					this.fieldMode
				]
			});
		},

		fillAttributeGroupsStore: function(attributes) {
			var store = this.attributeGroup.store,
				addtributesGroup = {},
				groups = [],
				attribute;

			store.removeAll();

			// build a map before to deny duplications
			for (var i=0, len=attributes.length; i<len; ++i) {
				attribute = attributes[i];
				if (attribute.data.group) {
					addtributesGroup[attribute.data.group] = true;
				};
			}

			for (var g in addtributesGroup) {
				groups.push([g]);
			}

			store.loadData(groups);
		},

		// override
		enableModify: function(all) {
			this.mixins.cmFormFunctions.enableModify.call(this, all);
			this.addMetadataBtn.enable();

			/*
			 * Business rule 11/01/2013
			 * Someone has verified that disable the description
			 * attribute could be a problem. This is true if
			 * the class is used to fill a reference.
			 *
			 * So, deny to the user to turn it off
			 */
			if (this.attributeName.getValue() == "Description") {
				this.isActive.disable();
			}
		},

		// override
		disableModify: function(enableCMTBar) {
			this.mixins.cmFormFunctions.disableModify.call(this, enableCMTBar);
			this.addMetadataBtn.disable();
		}
	});

	function onSelectComboType (combo, record, index) {
		var type = record[0].data.value;
		this.hideContextualFields();
		this.showAndEnableContextualFieldsByType(type);
	}

})();