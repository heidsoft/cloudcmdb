(function() {

	var tr_attribute = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr_geoserver = CMDBuild.Translation.administration.modcartography.geoserver;
	var tr = CMDBuild.Translation.administration.modClass.geo_attributes;

	Ext.define('CMDBuild.view.administration.gis.GisLayersForm', {
		extend: 'Ext.form.Panel',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		autoScroll: false,
		bodyCls:'cmdb-gray-panel',
		border: false,
		buttonAlign: 'center',
		cls: 'x-panel-body-default-framed cmdb-border-top',
		fileUpload: true,
		frame: false,
		hideMode: 'offsets',
		plugins: [new CMDBuild.FormPlugin(), new CMDBuild.CallbackPlugin()],

		layout: {
			type: 'border'
		},

		initComponent: function() {
			var me = this;

			// Buttons configuration
				this.cmTBar = [
					this.modifyButton = Ext.create('Ext.button.Button', {
						text: tr_geoserver.modify_layer,
						iconCls: 'modify',
						scope: this,

						handler: function() {
							this.enableModify();
						}
					}),
					this.deleteButton = Ext.create('Ext.button.Button', {
						text: tr_geoserver.delete_layer,
						iconCls: 'delete'
					})
				];

				this.cmButtons = [
					this.saveButton = Ext.create('CMDBuild.core.buttons.text.Save'),
					this.abortButton = Ext.create('CMDBuild.core.buttons.text.Abort')
				];
			// END: Buttons configuration

			this.nameField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr_attribute.name,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name: CMDBuild.core.constants.Proxy.NAME,
				allowBlank: false,
				cmImmutable: true,
				vtype: 'alphanum',
				disabled: true,

				listeners: {
					change: function(field, newValue, oldValue, eOpts) {
						me.autoComplete(me.descriptionField, newValue, oldValue);
					}
				}
			});

			this.typesCombo = Ext.create('Ext.form.field.ComboBox', {
				allowBlank: false,
				autoScroll: true,
				name: CMDBuild.core.constants.Proxy.TYPE,
				fieldLabel: tr_attribute.type,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				valueField: CMDBuild.core.constants.Proxy.VALUE,
				displayField: CMDBuild.core.constants.Proxy.NAME,
				hiddenName: CMDBuild.core.constants.Proxy.TYPE,
				triggerAction: 'all',
				cmImmutable: true,
				disabled: true,

				store: Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.NAME],
					data: [
						['GEOTIFF', 'GeoTiff'],
						['WORLDIMAGE', 'WorldImage'],
						['SHAPE', 'Shape']
					]
				}),
				queryMode: 'local'
			});

			this.rangeFields = Ext.create('CMDBuild.form.RangeSliders', {
				maxSliderField: Ext.create('Ext.slider.Single', {
					fieldLabel: tr.max_zoom,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					minValue: 0,
					maxValue: 25,
					value: 25,
					name: 'maxZoom',
					disabled: true
				}),
				minSliderField: Ext.create('Ext.slider.Single', {
					fieldLabel: tr.min_zoom,
					labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
					width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
					minValue: 0,
					maxValue: 25,
					name: 'minZoom',
					disabled: true
				}),
				disabled: true
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr_attribute.description,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name: CMDBuild.core.constants.Proxy.DESCRIPTION,
				allowBlank: false,
				disabled: true
			});

			this.fileField = Ext.create('Ext.form.field.File', {
				fieldLabel: tr_geoserver.file,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				name: 'file',
				form: this,
				disabled: true
			});

			this.bindToCardFieldset = Ext.create('CMDBuild.view.administration.gis.BindCardFieldset', {
				padding: '5 0 0 0'
			});

			this.wrapper = Ext.create('Ext.panel.Panel', {
				frame: true,
				border: false,
				region: 'center',

				items: [
					this.nameField,
					this.descriptionField,
					this.fileField,
					this.typesCombo,
					this.rangeFields,
					this.bindToCardFieldset
				]
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
						items: this.cmTBar
					}
				],
				items: [this.wrapper],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
			this.setFieldsDisabled();

			this.disableModify();
		},

		getName: function() {
			return this.nameField.getValue();
		},

		getCardsBinding: function() {
			return this.bindToCardFieldset.getValue();
		},

		onAddLayer: function() {
			this.lastSelection = undefined;
			this.setCardBinding([]);
			this.getForm().reset();
			this.enableModify(true);
		},

		onLayerSelect: function(layerModel) {
			this.lastSelection = layerModel;
			this.getForm().loadRecord(layerModel);
			this.setCardBinding(layerModel.getCardBinding());
			this.disableModify(true);
		},

		setCardBinding: function(cardBinding) {
			this.bindToCardFieldset.setValue(cardBinding);
		}
	});

})();