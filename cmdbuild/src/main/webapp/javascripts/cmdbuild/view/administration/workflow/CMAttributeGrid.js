(function() {

	var ATTR = {
		INDEX: CMDBuild.core.constants.Proxy.INDEX,
		NAME: CMDBuild.core.constants.Proxy.NAME,
		DESCRIPTION: CMDBuild.core.constants.Proxy.DESCRIPTION,
		TYPE: CMDBuild.core.constants.Proxy.TYPE,
		IS_BASEDSP: 'isbasedsp',
		IS_UNIQUE: 'isunique',
		IS_NOT_NULL: 'isnotnull',
		IS_INHERITED: 'inherited',
		IS_ACTIVE: CMDBuild.core.constants.Proxy.ACTIVE,
		FIELD_MODE: CMDBuild.core.constants.Proxy.FIELD_MODE,
		GROUP: CMDBuild.core.constants.Proxy.GROUP,
		ABSOLUTE_CLASS_ORDER: 'absoluteClassOrder',
		CLASS_ORDER_SIGN: 'classOrderSign',
		EDITOR_TYPE: CMDBuild.core.constants.Proxy.EDITOR_TYPE
	};
	var REQUEST = {
		ROOT: 'attributes'
	};
	var ATTR_TO_SKIP = 'Notes';
	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;

	Ext.define('CMDBuild.view.administration.workflow.CMAttributeGrid', {
		extend: 'Ext.grid.Panel',

		statics: {
			ATTRIBUTES: ATTR
		},

		cls: 'cmdb-border-bottom',
		includeInherited: true,
		eventtype: 'class',

		hideNotNull: false, // for processes

		hideMode: 'offsets',

		constructor: function() {
			this.addAttributeButton = new Ext.button.Button({
				iconCls: 'add',
				text: translation.add_attribute
			});

			this.orderButton = new Ext.button.Button({
				iconCls: 'order',
				text: translation.set_sorting_criteria
			});

			this.inheriteFlag = new Ext.form.Checkbox({
				boxLabel: CMDBuild.Translation.administration.modClass.include_inherited,
				boxLabelCls: 'cmdb-toolbar-item',
				checked: true,
				scope: this,
				handler: function(obj, checked) {
					this.setIncludeInheritedAndFilter(includeInherited = checked);
				}
			});

			Ext.apply(this, {
				store: CMDBuild.proxy.common.tabs.attribute.Attribute.getStore(),
				queryMode: 'local'
			});

			this.buildColumnConf();
			this.buildTBar();

			this.callParent(arguments);
		},

		initComponent: function() {
			Ext.apply(this, {
				viewConfig: {
					loadMask: false,
					plugins: {
						ptype: 'gridviewdragdrop',
						dragGroup: 'dd',
						dropGroup: 'dd'
					},
					listeners: {
						scope: this,
						beforedrop: function() {
							// it is not allowed to reorder the attribute if there are also the inherited attrs
							return this.inheriteFlag.checked;
						},
						drop: function(node, data, dropRec, dropPosition) {
							this.fireEvent('cm_attribute_moved', arguments);
						}
					}
				}
			});

			this.callParent(arguments);

			this.getStore().on('load', function(store, records, opt) {
				this.filterInheritedAndNotes();
			}, this);
		},

		// private
		buildColumnConf: function() {
			this.columns = [
				{
					hideable: false,
					hidden: true,
					dataIndex: ATTR.INDEX,
					flex: 1
				},
				{
					header: translation.name,
					dataIndex: ATTR.NAME,
					flex: 1
				},
				{
					header: translation.description,
					dataIndex: ATTR.DESCRIPTION,
					flex: 1
				},
				{
					header: translation.type,
					dataIndex: ATTR.TYPE,
					flex: 1
				},
				{
					header: translation.editorType.label,
					dataIndex: ATTR.EDITOR_TYPE,
					flex: 1,
					hidden: true
				},
				new Ext.ux.CheckColumn({
					header: translation.isbasedsp,
					dataIndex: ATTR.IS_BASEDSP,
					cmReadOnly: true
				}),
				new Ext.ux.CheckColumn({
					header: translation.isunique,
					dataIndex: ATTR.IS_UNIQUE,
					cmReadOnly: true
				}),
				new Ext.ux.CheckColumn({
					header: translation.isnotnull,
					dataIndex: ATTR.IS_NOT_NULL,
					cmReadOnly: true
				}),
				new Ext.ux.CheckColumn({
					header: translation.inherited,
					hidden: true,
					dataIndex: ATTR.IS_INHERITED,
					cmReadOnly: true
				}),
				new Ext.ux.CheckColumn({
					header: translation.isactive,
					dataIndex: ATTR.IS_ACTIVE,
					cmReadOnly: true
				}),
				{
					header: translation.field_visibility,
					dataIndex: ATTR.FIELD_MODE,
					renderer: renderEditingMode,
					flex: 1
				},
				{
					header: translation.group,
					dataIndex: ATTR.GROUP,
					hidden: true,
					flex: 1
				}
			];
		},

		buildTBar: function() {
			this.tbar = [this.addAttributeButton, this.orderButton, '->', this.inheriteFlag];
		},

		onClassSelected: function(idClass, className) {
			this.refreshStore(idClass, null, className);
		},

		refreshStore: function(idClass, indexAttributeToSelectAfter, className) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = className;

			this.store.load({
				params: params,
				scope: this,
				callback: function(records, opt, success) {
					this.filterInheritedAndNotes();

					if (this.rendered)
						this.selectRecordAtIndexOrTheFirst(indexAttributeToSelectAfter);
				}
			});
		},

		setIncludeInheritedAndFilter: function(includeInherited) {
			this.includeInherited = includeInherited;
			this.filterInheritedAndNotes();
		},

		filterInheritedAndNotes: function() {
			var inh = this.includeInherited;

			this.getStore().filterBy(function(record) {
				return (record.get(ATTR.NAME) != ATTR_TO_SKIP) && (inh || !record.get(ATTR.IS_INHERITED));
			});
		},

		selectFirstRow: function() {
			var _this = this;

			Ext.Function.defer(function() {
				if (_this.store.getCount() > 0 && _this.isVisible()) {
					var sm = _this.getSelectionModel();

					if (!sm.hasSelection())
						sm.select(0);
				}
			}, 200);
		},

		selectRecordAtIndexOrTheFirst: function(indexAttributeToSelectAfter) {
			if (indexAttributeToSelectAfter) {
				var recordIndex = this.store.findRecord(ATTR.INDEX, indexAttributeToSelectAfter);

				if (recordIndex)
					this.getSelectionModel().select(recordIndex);
			} else {
				try {
					if (this.store.count() != 0)
						this.getSelectionModel().select(0);
				} catch (e) {
					// fail if the grid is not rendered
				}
			}
		},

		onAddAttributeClick: function() {
			this.getSelectionModel().deselectAll();
		}
	});

	function renderEditingMode(val) {
		return translation['field_' + val];
	}

})();