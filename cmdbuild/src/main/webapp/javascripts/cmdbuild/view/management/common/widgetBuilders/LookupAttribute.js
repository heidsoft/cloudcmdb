(function() {

	Ext.require(['CMDBuild.core.Utils']);

	/**
	 * @class CMDBuild.WidgetBuilders.LookupAttribute
	 * @extends CMDBuild.WidgetBuilders.ComboAttribute
	 */
	Ext.ns("CMDBuild.WidgetBuilders");
	CMDBuild.WidgetBuilders.LookupAttribute = function() {};
	CMDBuild.extend(CMDBuild.WidgetBuilders.LookupAttribute, CMDBuild.WidgetBuilders.ComboAttribute);

	/**
	 * @override
	 * @param attribute
	 * @return CMDBuild.Management.LookupCombo
	 */
	CMDBuild.WidgetBuilders.LookupAttribute.prototype.buildAttributeField = function(attribute) {
		return CMDBuild.Management.LookupCombo.build(attribute);
	};

	/**
	 * @override
	 */
	CMDBuild.WidgetBuilders.LookupAttribute.prototype.markAsRequired = function(field, attribute) {
		//do nothing because the LookupField class manage this function
		return field;
	};

	/**
	 * @override
	 * @return Ext.form.DisplayField
	 */
	CMDBuild.WidgetBuilders.LookupAttribute.prototype.buildReadOnlyField = function(attribute) {
		var field = new Ext.form.DisplayField({
			allowBlank: true,
			labelAlign: "right",
			labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
			fieldLabel: attribute.description || attribute.name,
			width: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
			submitValue: false,
			name: attribute.name,
			disabled: false,

			/**
			 * Validate also display field
			 *
			 * @override
			 */
			isValid: function() {
				if (this.allowBlank)
					return true;

				return !Ext.isEmpty(this.getValue());
			}
		});

		// Overrides setValue function to translate attribute value to description
		var originalSetValue = field.setValue;
		field.setValue = function(value) {
			if (!Ext.isEmpty(value)) {
				var store = _CMCache.getLookupStore(attribute.lookup);

				if (value.hasOwnProperty(CMDBuild.core.constants.Proxy.DESCRIPTION)) {
					value = value[CMDBuild.core.constants.Proxy.DESCRIPTION];
				} else if (value.hasOwnProperty('Description')) {
					value = value['Description'];
				} else if (value.hasOwnProperty(CMDBuild.core.constants.Proxy.ID)) {
					value = value[CMDBuild.core.constants.Proxy.ID];
				} else if (value.hasOwnProperty('Id')) {
					value = value['Id'];
				} else if (typeof value == 'string' && !isNaN(parseInt(value))) {
					value = parseInt(value);
				}

				var foundRecord = store.findRecord('Id', value);

				if (!Ext.isEmpty(foundRecord)) {
					originalSetValue.call(field, foundRecord.get('Description'));
				} else {
					originalSetValue.call(field, value);
				}
			}
		};

		// markAsRequired method
		if (attribute.isnotnull || attribute.fieldmode == "required") {
			field.allowBlank = false;

			if (field.fieldLabel)
				field.fieldLabel = CMDBuild.core.Utils.prependMandatoryLabel(field.fieldLabel);
		}

		return field;
	};

	/**
	 * @override
	 */
	CMDBuild.WidgetBuilders.LookupAttribute.prototype.buildCellEditor = function(attribute) {
		var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, readOnly = false);

		if (field.isMultiLevel) {
			var fake_field = buildFakeField(attribute);
			fake_field.on("focus", function() {
				onFakeFieldFocus.call(this, attribute);
			}, fake_field);

			return fake_field;
		} else {
			return field;
		}
	};

	/**
	 * @override
	 */

	CMDBuild.WidgetBuilders.LookupAttribute.prototype.genericBuildFieldsetForFilter = function(fieldId, fields, query, originalFieldName) {

		var field = fields[0];

		if (field instanceof CMDBuild.field.MultiLevelLookupPanel) {
			var removeFieldButton = new Ext.button.Button({
				iconCls : 'delete',
				border : false,
				padding : "3 0 0 3"
			});

			var orPanel = new Ext.Panel({
				columnWidth: 0.2,
				html : 'or',
				border : false,
				bodyCls : "x-panel-body-default-framed"
			});

			field.columnWidth = .5;
			field.items.each(function(f) {
				f.padding = 0;
			});

			var fieldset = new Ext.panel.Panel({
				frame : false,
				border : false,
				bodyCls : "x-panel-body-default-framed",
				removeButton : removeFieldButton,
				fieldsetCategory : originalFieldName,
				queryCombo : query,
				hideMode : 'offsets',

				layout : {
					type : 'hbox',
					pack : 'start',
					align : 'top'
				},

				defaults: {
					margins:'0 5 0 0'
				},

				items : [removeFieldButton, query, field, orPanel],

				getAttributeField : function() {
					return fields[0];
				},

				getQueryCombo : function() {
					return query;
				},

				getOrPanel : function() {
					return orPanel;
				}
			});

			return fieldset;

		} else {
			return CMDBuild.WidgetBuilders.BaseAttribute.prototype.genericBuildFieldsetForFilter.apply(this, arguments);
		}
	}


	function buildFakeField(attribute) {
		return new CMDBuild.view.common.field.CMErasableCombo({
			labelAlign: "right",
			labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
			fieldLabel: attribute.fieldLabel || attribute.name,
			labelSeparator: ":",
			name: attribute.name,
			hiddenName: attribute.name,
			store: new Ext.data.Store({
				fields: ["Id", "Description"],
				data: []
			}),
			queryMode: 'local',
			triggerAction: "all",
			valueField: 'Id',
			displayField: 'Description',
			allowBlank: true,
			CMAttribute: attribute
		});
	}

	function onFakeFieldFocus(attribute) {
		var me = this,
			pos = this.getPosition();

		if (this.editingWindow) {
			if (this.editingWindow.pos[0] == pos[0] && this.editingWindow.pos[1] == pos[1]) {
				return;
			} else {
				this.editingWindow.destroy();
				delete this.editingWindow;
			}
		}

		var fieldForTheWindow = buildFieldForTheWindow(attribute);

		this.editingWindow = new Ext.window.Window({
			pos: pos,
			x: pos[0],
			y: pos[1],
			width: me.getWidth(),
			draggable: false,
			closable: false,
			items: [fieldForTheWindow],
			buttonAlign: "center",
			buttons: [{
				xtype: "button",
				text: CMDBuild.Translation.ok,
				handler: function() {
					var value = fieldForTheWindow.getValue(),
						rawValue = fieldForTheWindow.getRawValue(),
						recordIndex = me.store.find(me.valueField, value),
						notInStore = recordIndex == -1,
						record;

					if (notInStore) {
						var data = {};
						data[me.displayField] = rawValue;
						data[me.valueField] = value;
						record = me.store.add(data)[0];
					} else {
						record = me.store.getAt(recordIndex);
					}

					me.setValue(value);
					me.fireEvent("select", me, record);

					me.editingWindow.destroy();
					delete me.editingWindow;
				}
			},{
				xtype: "button",
				text: CMDBuild.Translation.cancel,
				handler: function() {
					me.editingWindow.destroy();
					delete me.editingWindow;
				}
			}]
		}).show();
	}

	function buildFieldForTheWindow(attribute) {
		var fieldForTheWindow = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, readOnly = false),
			fields = fieldForTheWindow.items.items;

		for (var i=0, l=fields.length; i<l; ++i) {
			var item = fields[i];
			item.hideLabel = true;
			item.padding = "0 0 0 0";
		}

		return fieldForTheWindow;
	}
})();
