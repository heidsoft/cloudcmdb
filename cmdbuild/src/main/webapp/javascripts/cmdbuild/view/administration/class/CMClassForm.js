(function() {
	var tr = CMDBuild.Translation.administration.modClass.classProperties;

	Ext.define("CMDBuild.view.administration.classes.CMClassForm", {
		extend: "Ext.panel.Panel",

		alias: "classform",

		requires: ['CMDBuild.core.constants.Proxy'],

		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		defaultParent: "Class",

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		title: tr.title_add,

		initComponent : function() {
			this.plugins = [new CMDBuild.FormPlugin()];
			this.border = false;
			this.frame = false;
			this.cls = "x-panel-body-default-framed";
			this.bodyCls = 'cmdb-gray-panel';

			this.cmButtons = [
				this.saveButton = Ext.create('CMDBuild.core.buttons.text.Save', { margin: '5' }),
				this.abortButton = Ext.create('CMDBuild.core.buttons.text.Abort')
			];

			this.buildFormFields();

			Ext.apply(this, {
				items: this.getFormItems()
			});

			this.tbar = this.cmTBar = [
				this.modifyButton = new Ext.button.Button({
					iconCls: 'modify',
					text: tr.modify_class,
					handler: function() {
						this.enableModify();
						this.iconForm.setDisabledModify(false);
					},
					scope: this
				}),
				this.deleteButton = new Ext.button.Button({
					iconCls: 'delete',
					text: tr.remove_class
				}),
				this.printClassButton = Ext.create('CMDBuild.core.buttons.iconized.split.Print', {
					formatList: [
							CMDBuild.core.constants.Proxy.PDF, CMDBuild.core.constants.Proxy.ODT
					],
					mode: 'legacy',
					text: tr.print_class
				})
			];

			this.callParent(arguments);

			this.typeCombo.on("select", onSelectType, this);
			this.className.on("change", function(fieldname, newValue, oldValue) {
				this.autoComplete(this.classDescription, newValue, oldValue);
			}, this);

			this.disableModify();
		},

		getForm : function() {
			return this.form.getForm();
		},

		/**
		 * @property {CMDBuild.cache.CMEntryTypeModel} selection
		 */
		onClassSelected: function(selection) {
			this.getForm().loadRecord(selection);

			this.disableModify(true);
		},

		onAddClassButtonClick: function() {
			this.reset();
			this.inheriteCombo.store.cmFill();
			this.enableModify(all=true);
			this.iconForm.imageIconDisplayField.setSrc('');
			this.iconForm.setDisabledModify(true, true);
			this.setDefaults();
		},

		setDefaults: function() {
			this.isActive.setValue(true);
			this.typeCombo.setValue("standard");
			this.inheriteCombo.setValue(_CMCache.getClassRootId());
		},

		// protected
		buildFormFields: function() {
			this.inheriteCombo = new Ext.form.ComboBox( {
				fieldLabel : tr.inherits,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name : 'parent',
				valueField : 'id',
				displayField : 'description',
				editable : false,
				cmImmutable : true,
				defaultParent : this.defaultParent,
				queryMode : "local",
				store : this.buildInheriteComboStore()
			});

			this.className = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.name,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name: CMDBuild.core.constants.Proxy.NAME,
				allowBlank: false,
				vtype: 'alphanum',
				cmImmutable: true
			});

			this.classDescription = Ext.create('CMDBuild.view.common.field.translatable.Text', {
				name: CMDBuild.core.constants.Proxy.TEXT,
				fieldLabel: CMDBuild.Translation.descriptionLabel,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				allowBlank: false,
				vtype: 'commentextended',

				translationFieldConfig: {
					type: CMDBuild.core.constants.Proxy.CLASS,
					identifier: { sourceType: 'form', key: CMDBuild.core.constants.Proxy.NAME, source: this },
					field: CMDBuild.core.constants.Proxy.DESCRIPTION
				}
			});

			this.isSuperClass = new Ext.ux.form.XCheckbox( {
				fieldLabel : tr.superclass,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name : 'superclass',
				cmImmutable : true
			});

			this.isActive = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.active,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name : 'active'
			});

			var types = Ext.create('Ext.data.Store', {
				fields: ['value', 'name'],
				data : [
					{"value":"standard", "name":tr.standard},
					{"value":'simpletable', "name":tr.simple}
				]
			});

			this.typeCombo = new Ext.form.field.ComboBox({
				fieldLabel : tr.type,
				labelWidth : CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_MEDIUM,
				name : 'tableType',
				hiddenName : 'tableType',
				valueField : 'value',
				displayField : 'name',
				editable : false,
				queryMode : "local",
				cmImmutable : true,
				store: types
			});

			this.typeCombo.setValue = Ext.Function.createInterceptor(this.typeCombo.setValue,
			onTypeComboSetValue, this);
		},

		// protected
		getFormItems: function() {
			return [
				Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.baseProperties,

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [
						this.form = Ext.create('Ext.form.Panel', {
							frame: false,
							border: false,
							bodyCls: 'cmdb-gray-panel-no-padding',
							defaultType: 'textfield',
							autoScroll: true,

							layout: {
								type: 'vbox',
								align: 'stretch'
							},

							items: [
								this.className,
								this.classDescription,
								this.typeCombo,
								this.inheriteCombo,
								this.isSuperClass,
								this.isActive
							]
						}),
						Ext.create('Ext.container.Container', {
							style: {
								borderTop: '1px solid #d0d0d0'
							},

							layout: {
								type: 'hbox',
								align: 'middle',
								pack: 'center'
							},

							items: this.cmButtons
						})
					]
				}),
				Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.icon,
					layout: 'fit',

					items: [
						this.iconForm = Ext.create('CMDBuild.view.administration.class.IconForm', { parentForm: this })
					]
				})
			];
		},

		buildInheriteComboStore: function() {
			return _CMCache.getSuperclassesAsStore();
		}
	});

	function onSelectType(field, selections) {
		var s = selections[0];
		if (s) {
			onTypeComboSetValue.call(this, s.get("value"));
		}
	}

	function onTypeComboSetValue(value) {
		if (value == "simpletable") {
			this.isSuperClass.hide();
			this.inheriteCombo.hide();
		} else {
			this.isSuperClass.show();
			this.inheriteCombo.show();
		}
	}

})();