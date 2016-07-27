(function () {

	Ext.require('CMDBuild.proxy.index.Json');

	Ext.define("CMDBuild.view.management.utility.importCsv.CMCardGridDelegate", {
		/**
		 *
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 */
		onCMCardGridSelect: function(grid, record) {},

		/**
		 *
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 */
		onCMCardGridDeselect: function(grid, record) {},

		/**
		 *
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridBeforeLoad: function(grid) {},

		/**
		 *
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridLoad: function(grid) {},

		/**
		 *
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridColumnsReconfigured: function(grid) {},

		/**
		 *
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridIconRowClick: function(grid, action, model) {}

	});

	Ext.define("CMDBuild.view.management.utility.importCsv.CMCardGridPagingBar", {
		extend: "Ext.toolbar.Paging",

		// configuration
		grid: undefined,
		// configuration

		// override
		doRefresh: function(value) {
			if (this.grid) {
				var sm = this.grid.getSelectionModel();
				if (sm) {
					sm.deselectAll();
				}
			}
			return this.callOverridden(arguments);
		}
	});

	Ext.define('CMDBuild.view.management.utility.importCsv.GridPanel', {
		extend: "Ext.grid.Panel",

		requires: ['CMDBuild.core.constants.Global'],

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		/**
		 * @cfg {CMDBuild.controller.management.utility.importCsv.ImportCsv}
		 */
		delegate: undefined,

		// configuration
		columns: [],
		extraParams: {}, // extra params for the store
		// configuration

		border: false,
		cls: 'cmdb-border-top cmdb-border-bottom',

		constructor: function(c) {
			this.mixins.delegable.constructor.call(this, "CMDBuild.view.management.utility.importCsv.CMCardGridDelegate");

			this.cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit : 1,
				listeners: {
					/*
					 * eventObj.record = the Ext.model.Model for the row
					 * eventObj.field = the name of the column
					 * eventObj.value = the value to pass to the editor
					 */
					beforeedit: function(editor, eventObj) {
						var storedValue = eventObj.record.get(eventObj.field);
						if (!storedValue) {
							eventObj.value = null;
						}
					},
					edit: function(editor, eventObj) {
						var value = eventObj.value;
						var oldValue = eventObj.originalValue;

						// To deny to set a string as value
						// if enter in editing for a ComboBox and
						// then leave the field without select an item
						if (typeof oldValue == "object"
							&& typeof value != "object"
							&& oldValue.id == value) {

								eventObj.record.set(eventObj.field, oldValue);
								return;
							}

						// prevent the red triangle if enter in
						// editing for a date and leave the field
						// without change something
						if (isADate(value)
							&& typeof oldValue == "string"
							&& formatDate(value) == oldValue) {

								eventObj.record.set(eventObj.field, oldValue);
								return;
							}
					}
				}
			});

			this.validFlag = new Ext.form.Checkbox({
				hideLabel: true,
				boxLabel: CMDBuild.Translation.showInvalidRecordsOnly,
				checked: false,
				scope: this,
				handler: function(obj, checked) {
					this.filterStore();
				}
			});

			var me = this;
			this.searchField = new CMDBuild.field.LocaleSearchField({
				grid: me,
				onTrigger1Click: function() {
					me.filterStore();
				}
			});

			this.store = new Ext.data.Store({
				fields:[],
				data: []
			});

			this.columns = [];
			this.bbar = [this.searchField, "-", this.validFlag];
			this.plugins = [this.cellEditing];
			this.callParent(arguments);
		},

		initComponent: function() {
			this.loadMask = false;
			this.store = this.getStoreForFields([]);

			this.viewConfig = {
				stripeRows: true,
				// Business rule: voluntarily hide the horizontal scroll-bar
				// because probably no one want it
				autoScroll: false,
				overflowX: "hidden",
				overflowY: "auto"
			};

			this.layout = {
				type: "fit",
				reserveScrollbar: true
			};

			this.callParent(arguments);
			this.mon(this, 'beforeitemclick', cellclickHandler, this);

			// register to events for delegates
			this.mon(this, 'select', function(grid, record) {
				this.callDelegates("onCMCardGridSelect", [grid, record]);
			}, this);

			this.mon(this, 'deselect', function(grid, record) {
				this.callDelegates("onCMCardGridDeselect", [grid, record]);
			}, this);
		},

		/*
		 * rawData is an array of object
		 * {
		 * 	card: {...}
		 * 	not_valid_fields: {...}
		 * }
		 */
		loadData: function(rawData) {
			var records = [];
			for (var	i=0,
						l=rawData.length,
						r=null,
						card=null; i<l; ++i) {

				r = rawData[i];
				card = r[CMDBuild.core.constants.Proxy.CARD];
				card['not_valid_values'] = r['not_valid_values'];

				records.push(new CMDBuild.DummyModel(card));
			}

			Ext.suspendLayouts();
			this.store.loadRecords(records);
			Ext.resumeLayouts(true);
		},

		filterStore: function() {
			var me = this;

			this.store.clearFilter(false);
			var nonValid = this.validFlag.getValue();
			var query = this.searchField.getRawValue().toUpperCase();

			if (query == "") {
				if (nonValid) {
					this.store.filterBy(isInvalid, me);
				}
			} else {
				if (nonValid) {
					this.store.filterBy(isInvalidAndFilterQuery, me);
				} else {
					this.store.filterBy(filterQuery, me);
				}
			}
		},

		updateStoreForClassId: function(classId, o) {

			var me = this;

			function callCbOrLoadFirstPage(me) {
				if (o && o.cb) {
					o.cb.call(o.scope || me);
				} else {
					me.store.loadPage(1);
				}
			}

			if (me.currentClassId == classId) {
				callCbOrLoadFirstPage(me);
			} else {
				me.currentClassId = classId;

				if (this.gridSearchField) {
					this.gridSearchField.setValue(""); // clear only the field without reload the grid
				}

				if (me.printGridMenu) {
					me.printGridMenu.setDisabled(!classId);
				}

				me.loadAttributes( //
					classId, //
					function(attributes) { //
						me.setColumnsForClass(attributes);
						me.setGridSorting(attributes);
						callCbOrLoadFirstPage(me);
					} //
				);

			}
		},

		// protected
		loadAttributes: function(classId, cb) {
			var me = this;
			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(classId);

			CMDBuild.proxy.common.tabs.attribute.Attribute.read({
				params: params,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					var attributes = decodedResponse.attributes;
					attributes.sort( //
						function(a,b) { //
							return a.index - b.index;
						} //
					);

					if (cb) {
						cb(attributes);
					}

				}
			});
		},

		/**
		 * @param {Number} pageNumber
		 * @param {Object} options
		 */
		loadPage: function(pageNumber, options) {
			options = options || {};
			scope = options.scope || this;
			cb = options.cb || function(args) { // Not a good implementation but there isn't another way
				if (!args[2]) {
					CMDBuild.core.Message.error(null, {
						text: CMDBuild.Translation.errors.anErrorHasOccurred
					});
				}
			};

			this.mon(this, 'load', cb, scope, { single: true }); // LoadPage does not allow the definition of a callBack

			this.getStore().loadPage(Math.floor(pageNumber));
		},

		/**
		 * @param {Boolean} reselect
		 */
		reload: function(reselect) {
			reselect = Ext.isBoolean(reselect) && reselect;

			this.getStore().load({
				scope: this,
				callback: function(records, operation, success) {
					if (success) {
						// If we have a start parameter greater than zero and no loaded records load first page to avoid to stick in empty page also if we have records
						if (operation.start > 0 && Ext.isEmpty(records))
							this.loadPage(1);

						if (reselect) {
							if (this.getSelectionModel().hasSelection()) {
								var record = this.getStore().findRecord('Id', this.getSelectionModel().getSelection()[0].get('Id'));

								if (!Ext.isEmpty(record))
									this.getSelectionModel().select(record);
							} else {
								this.getSelectionModel().select(0);
							}
						}
					}
				}
			});
		},

		getVisibleColumns: function() {
			var columns = this.columns;
			var visibleColumns = [];

			for (var i = 0, len = columns.length ; i<len ; i++) {
				var col = columns[i];
				if (!col.hidden
						&& col.dataIndex // the expander column has no dataIndex
						&& col.dataIndex != "Id") { // The graph column has dataIndex Id

					var columnName = col.dataIndex;
					if (columnName) {
						var index = columnName.lastIndexOf("_value");
						if (index >= 0) {
							columnName = columnName.slice(0,index);
						}
						visibleColumns.push(columnName);
					}
				}
			};

			return visibleColumns;
		},

		// protected
		setColumnsForClass: function(classAttributes) {
			this.classAttributes = classAttributes;
		},

		configureHeadersAndStore: function(headersToShow) {
			var grid = this;
			var headers = [];
			var fields = [];

			for (var i=0, l=headersToShow.length; i<l; i++) {

				var a = getClassAttributeByName(this, headersToShow[i]);

				if (a != null) {
					var _attribute = Ext.apply({}, a);
					_attribute.fieldmode = "write";
					var header = CMDBuild.Management.FieldManager.getHeaderForAttr(_attribute);
					var editor = CMDBuild.Management.FieldManager.getCellEditorForAttribute(_attribute);
					editor.hideLabel = true;

					if (a.type == "REFERENCE" || a.type == "LOOKUP") {
						editor.on("select", updateStoreRecord, this);
						editor.on("cmdbuild-reference-selected", function(record, field) {
							updateStoreRecord.call(this, field, record);
						}, this);
					}

					if (header) {
						header.field = editor;
						header.hidden = false;
						header.renderer = Ext.Function.bind(renderer, a,[header.dataIndex, grid],true);
						headers.push(header);
						fields.push(header.dataIndex);
					}
				}
			}

			// Add a field to use to read the real value set by
			// the editors.
			fields.push('__objectValues__');

			this.reconfigure(this.getStoreForFields(fields), headers);
		},

		getRecordToUpload: function() {
			var data = [];
			var records = this.store.data.items || [];

			for (var i=0, l=records.length, r=null; i<l; ++i) {
				r = records[i];

				var currentData = {};
				var objectValues = r.data['__objectValues__'] || {};
				var wrongFields = r.get('not_valid_values');

				for (var j=0; j<this.classAttributes.length; j++) {
					var name = this.classAttributes[j].name;
					var value = objectValues[name] || r.data[name] || wrongFields[name];

					if (value) {
						currentData[name] = value;
					}
				}

				currentData['Id'] = r.get('Id');
				currentData['IdClass'] = r.get('IdClass');
				currentData['IdClass_value'] = r.get('IdClass_value');

				data.push(currentData);
			}

			return data;
		},

		removeAll: function() {
			this.store.removeAll();
		},

		// protected
		buildColumnsForAttributes: function(classAttributes) {
			this.classAttributes = classAttributes;
			var headers = [];
			var fields = [];

			if (_CMUtils.isSuperclass(this.currentClassId)) {
				headers.push(this.buildClassColumn());
			}

			for (var i=0; i<classAttributes.length; i++) {
				var attribute = classAttributes[i];
				var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);

				if (header &&
						header.dataIndex != 'IdClass_value') {

					this.addRendererToHeader(header);
					// There was a day in which I receved the order to skip the Notes attribute.
					// Today, the boss told  me to enable the notes. So, I leave the condition
					// commented to document the that a day the notes were hidden.

					// if (attribute.name != "Notes") {
						headers.push(header);
					// }

					fields.push(header.dataIndex);
				} else if (attribute.name == "Description") {
					// FIXME Always add Description, even if hidden, for the reference popup
					fields.push("Description");
				}
			}

			return {
				headers: headers,
				fields: fields
			};
		},

		// protected
		setGridSorting: function(attributes) {
			if (!this.store.sorters) {
				return;
			}

			this.store.sorters.clear();

			var sorters = [];
			for (var i=0, l=attributes.length; i<l; ++i) {
				var attribute = attributes[i];
				var sorter = {};
				/*
				 *
				 * After some trouble I understood that
				 * classOrderSign is:
				 * 1 if the direction is ASC
				 * 0 if the attribute is not used for the sorting
				 * -1 if the direction is DESC
				 *
				 * the absoluteClassOrder is the
				 * index of the sorting criteria
				 */
				var index = attribute.classOrderSign * attribute.absoluteClassOrder;
				if (index != 0) {
					sorter.property = attribute.name;
					if (index > 0) {
						sorter.direction = "ASC";
					} else {
						sorter.direction = "DESC";
						index = -index;
					}

					sorters[index] = sorter;
				}
			}

			for (var i = 0, l = sorters.length; i<l; ++i) {
				var sorter = sorters[i];
				if (sorter) {
					this.store.sorters.add(sorter);
				}
			}

		},

		// protected
		addRendererToHeader: function(h) {
			h.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
				value = value || record.get(h.dataIndex);

				if (typeof value == 'undefined' || value == null) {
					return '';
				} else if (typeof value == 'object') {
					/**
					 * Some values (like reference or lookup) are serialized as object {id: "", description:""}.
					 * Here we display the description
					 */
					value = value.description;
				} else if (typeof value == 'boolean') { // Localize the boolean values
					value = value ? Ext.MessageBox.buttonText.yes : Ext.MessageBox.buttonText.no;
				} else if (typeof value == 'string') { // Strip HTML tags from strings in grid
					value = Ext.util.Format.stripTags(value);
				}

				return value;
			};
		},

		// protected
		getStoreForFields: function(fields) {
			fields.push('Id');
			fields.push('IdClass');
			fields.push('IdClass_value');

			return new Ext.data.Store({
				fields: fields,
				data: [],
				autoLoad: false
			});
		},

		// protected
		buildClassColumn: function() {
			return {
				header: CMDBuild.Translation.subClass,
				width: 100,
				sortable: false,
				dataIndex: 'IdClass_value'
			};
		},

		applyFilterToStore: function(filter) {
			try {
				var encoded = filter;
				if (typeof encoded != "string") {
					encoded = Ext.encode(filter);
				}

				this.getStore().proxy.extraParams.filter = encoded;
			} catch (e) {
				_debug("I'm not able to set the filter to the store", this, filter);
			}
		}
	});

	/**
	 * @param {Array} headers
	 *
	 * @private
	 */
	function buildGraphIconColumn(headers) {
		var classModel = _CMCache.getClassById(this.currentClassId);

		if (
			!Ext.isEmpty(classModel) && classModel.get('tableType') != CMDBuild.core.constants.Global.getTableTypeSimpleTable()
			&& Ext.isArray(headers)
		) {
			headers.push(
				Ext.create('Ext.grid.column.Action', {
					align: 'center',
					width: 30,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,

					items: [
						Ext.create('CMDBuild.core.buttons.iconized.Graph', {
							withSpacer: true,
							tooltip: CMDBuild.Translation.openRelationGraph,
							scope: this,

							// TODO: cmfg() controller call implementation  on controller refactor
							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								Ext.create('CMDBuild.controller.management.common.graph.Graph', {
									parentDelegate: this,
									classId: record.get('IdClass'),
									cardId: record.get('id')
								});
							}
						})
					]
				})
			);
		}
	};

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		this.callDelegates("onCMCardGridIconRowClick", [grid, event.target.className, model]);
	}

	function getClassAttributeByName(me, name) {
		for (var i=0, l=me.classAttributes.length; i<l; i++) {
			var classAttr = me.classAttributes[i];
			if (classAttr.name == name) {
				return classAttr;
			}
		}

		return null;
	}

	function isInvalid(record, id) {
		var invalidFields = record.get('not_valid_values');
		// return true if there are some invalid fields
		for (var i in invalidFields) {
			return true;
		}
		return false;
	}

	function filterQuery(record, id) {
		var query = this.searchField.getRawValue().toUpperCase();
		var data = Ext.apply({}, record.get('not_valid_values'), record.data);
		var objectValues = record.data['__objectValues__'] || {};

		for (var attributeName in data) {
			var value = objectValues[attributeName] || data[attributeName];
			var attributeAsString = "";
			var searchIndex = -1;

			if (typeof value == "object") {
				value = value.description;
			}
			attributeAsString = (value+"").toUpperCase();
			searchIndex = attributeAsString.search(query);
			if (searchIndex != -1) {
				return true;
			}
		}

		return false;
	}

	function isInvalidAndFilterQuery(record, id) {
		if (isInvalid(record, id)) {
			return filterQuery.call(this, record, id);
		} else {
			return false;
		}
	}

	function renderer(value, metadata, record, rowindex, collindex, store, grid, colName) {
		// look before if there is a object value, if not search it as simple value;
		var v = null;
		if (typeof value == "object") {
			v = value;
		} else {
			var objectValues = record.get('__objectValues__') || {};
			v = objectValues[colName]|| record.get(colName);
		}

		if (v && typeof v == "object") {
			if (isADate(v)) {
				v = formatDate(v);
			} else {
				v = v.description;
			}
		}

		if (v) {
			return v;
		} else {
			var wrongs = record.get('not_valid_values');
			if (wrongs[colName]) {
				return	'<span class="importcsv-invalid-cell">' + wrongs[colName] + '</span>';
			} else {
				return	'<span class="importcsv-empty-cell"></span>';
			}
		}
	}

	function updateStoreRecord(field, selectedValue) {
		if (Ext.isArray(selectedValue)) {
			selectedValue = selectedValue[0];
		}

		var record = this.getSelectionModel().getSelection()[0];
		var objectValues = record.get('__objectValues__') || {};

		objectValues[field.name] = {
			description: selectedValue.get("Description"),
			id: selectedValue.get("Id")
		};

		record.set('__objectValues__', objectValues);

		return false; // to block the set value of the editor;
	}

	function formatDate(date) {
		var toString = "";

		var day = date.getDate();
		if (day < 10) {
			day = "0" + day;
		}
		toString += day + "/";

		var month = date.getMonth() + 1; // getMonth return 0-11
		if (month < 10) {
			month = "0"+month;
		}
		toString += month + "/" + date.getFullYear();

		return toString;
	}

	function isADate(v) {
		return (v && v.constructor && v.constructor.name == "Date");
	}

})();
