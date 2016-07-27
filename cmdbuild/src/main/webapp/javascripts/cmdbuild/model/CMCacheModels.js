(function() {

	/**
	 * @deprecated (CMDBuild.model.common.Generic)
	 */
	Ext.define("CMDBuild.DummyModel", {
		extend: "Ext.data.Model",
		fields:[],

		// in recent past, ExtJs add any data passed to the
		// constructor to the new model. In ExtJs 4.1 this is
		// not true. So use the setFields at the costructor to
		// set the fields of the DummyModel every time that it
		// is created
		constructor: function(data) {
			data = data || {};
			CMDBuild.DummyModel.setFields(Ext.Object.getKeys(data));
			this.callParent(arguments);
		}
	});

	// TODO: should be fixed with id as int but if try to do it all comboboxes will display id in place of description because '123' != 123
	Ext.define("CMTableForComboModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "name", type: 'string'},
			{name: "id",  type: 'string'},
			{name: "description",  type: 'string'}
		]
	});

	Ext.define('CMDBuild.cache.Lookup.typeComboStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' }
		]
	});

	Ext.define("CMDBuild.cache.CMLookupTypeModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "text",type: 'string'},
			{name: "parent",type: 'string'},
			{name: "type",type: 'string'}
		]
	});

	Ext.define("CMDBuild.cache.CMEntryTypeModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "text",type: 'string'},
			{name: "superclass",type: 'boolean'},
			{name: "active",type: 'boolean'},
			{name: "parent",type: 'string'},
			{name: "tableType",type: 'string'},
			{name: "type",type: 'string'},
			{name: "name",type: 'string'},
			{name: "priv_create",type: 'boolean'},
			{name: "priv_write",type: 'boolean'},
			{name: "ui_card_edit_mode",type: 'object'},
			{name: "meta", type:"auto"},
			// Process only
			{name: "userstoppable",type: 'boolean'},
			{name: "startable", type: "boolean"}
		],

		constructor: function() {
			this.callParent(arguments);
			this._widgets = [];
		},

		isSuperClass: function() {
			return this.get("superclass");
		},

		getTableType: function() {
			return this.get("tableType");
		},

		isProcess: function() {
			return this.get("type") == "processclass";
		},

		isUserStoppable: function() {
			return this.get("userstoppable");
		},

		isStartable: function() {
			return this.get("startable");
		},

		setWidgets: function(widgets) {
			this._widgets = widgets || [];
		},

		getWidgets: function() {
			return this._widgets;
		},

		addWidget: function(w) {
			this._widgets.push(w);
		},

		removeWidgetById: function(id) {
			var ww = this._widgets;
			for (var i=0, l=ww.length; i<l; ++i) {
				var widget = ww[i];
				if (widget.id == id) {
					delete ww[i];
					ww.splice(i, 1);
					return;
				}
			}
		},

		getName: function() {
			return this.get("name");
		},

		getDescription: function() {
			return this.get("text");
		},

		// Attachment metadata management

		/*
		 * In the meta could be a map called attachments.
		 * Here are stored the rules to autocomplete the
		 * attachments metadata. The aspected structure is:
		 * ...
		 * meta: {
		 * 		...
		 * 		attachments: {
		 * 			...
		 * 			autocompletion: {
		 * 				groupName: {
		 * 					metadataName: rule,
		 * 					metadataName: rule,
		 * 					....
		 * 				},
		 * 				groupName: {
		 * 					...
		 *				}
		 * 			}
		 * 		}
		 * }
		 */
		getAttachmentAutocompletion: function() {
			var meta = this.get("meta");
			var out = {};
			if (meta
					&& meta.attachments) {

				out = meta.attachments.autocompletion || {};
			}

			return out;
		},

		getAttachmentCopletionRuleByGropAndMetadataName: function(groupName, metaDataName) {
			var rulesByGroup = this.getAttachmentAutocompletion();
			var groupRules = rulesByGroup[groupName];
			var rule = null;

			if (groupRules) {
				rule = groupRules[metaDataName] || null;
			}

			return rule;
		},

		toString: function() {
			return this.get("name");
		}
	});

	/**
	 * Use "CMDBuild.model.domain.Domain" and complete functionalities
	 *
	 * @deprecated
	 */
	Ext.define("CMDBuild.cache.CMDomainModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "active", type: "boolean"},
			{name: "attributes", type: "auto"},
			{name: "cardinality", type: "string"},
			{name: "nameClass1", type: "string"},
			{name: "nameClass2", type: "string"},
			{name: "idClass1", type: "string"},
			{name: "idClass2", type: "string"},
			{name: "classType", type: "string"},
			{name: "name", type: "string"},
			{name: "createPrivileges", type: "boolean"},
			{name: "writePrivileges", type: "boolean"},
			{name: "isMasterDetail", type: "boolean"},
			{name: "description", type: "string"},
			{name: "descr_1", type: "string"},
			{name: "descr_2", type: "string"},
			{name: "md_label", type: "string"},
			{name: "disabled1", type: "auto"},
			{name: "disabled2", type: "auto"}
		],

		getAttributes: function() {
			var a = null;
			if (this.raw) {
				a = this.raw.attributes;
			}

			return a || this.data.attributes || [];
		},

		hasCreatePrivileges: function() {
			if (this.raw) {
				return this.raw.createPrivileges;
			} else {
				return this.data.createPrivileges;
			}
		},

		getSourceClassId: function() {
			return this.get("idClass1");
		},

		getDestinationClassId: function() {
			return this.get("idClass2");
		},

		getNSideIdInManyRelation: function() {
			var cardinality = this.get("cardinality");
			if (cardinality == "1:N") {
				return this.getDestinationClassId();
			}

			if (cardinality == "N:1") {
				return this.getSourceClassId();
			}

			return null;
		},

		getName: function() {
			return this.get("name");
		},

		getDescription: function() {
			return this.get("description");
		},

		// As master detail domain

		getDetailClassId: function() {
			var cardinality = this.get("cardinality");
			var classId = "";
			if (cardinality == "1:N") {
				classId = this.get("idClass2");
			} else if (cardinality == "N:1") {
				classId = this.get("idClass1");
			}

			return classId;
		},

		getDetailClassName: function() {
			var cardinality = this.get("cardinality");
			var className = "";
			if (cardinality == "1:N") {
				className = this.get("nameClass2");
			} else if (cardinality == "N:1") {
				className = this.get("nameClass1");
			}

			return className;
		},

		getMasterClassName: function() {
			var cardinality = this.get("cardinality");
			var className = "";
			if (cardinality == "1:N") {
				className = this.get("nameClass1");
			} else if (cardinality == "N:1") {
				className = this.get("nameClass2");
			}

			return className;
		},

		getDetailSide: function() {
			var c = this.get("cardinality");
			if (c == "1:N") {
				return "_2";
			} else if (c == "N:1") {
				return "_1";
			} else {
				return undefined;
			}
		}
	});

	Ext.define("CMDBuild.cache.CMReferenceStoreModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "Id", type: 'int'},
			{name: "Description",type: 'string'}
		]
	});

})();