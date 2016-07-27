(function() {

	Ext.ns('CMDBuild.cache');
	Ext.define("CMDBuild.cache.CMCache", {
		extend: "Ext.util.Observable",

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.proxy.Cache',
			'CMDBuild.proxy.common.tabs.attribute.Attribute',
			'CMDBuild.proxy.gis.Layer',
			'CMDBuild.proxy.index.Json'
		],

		mixins: {
			lookup: "CMDBUild.cache.CMCacheLookupFunctions",
			entryType: "CMDBUild.cache.CMCacheClassFunctions",
			domains: "CMDBUild.cache.CMCacheDomainFunctions",
			dashboards: "CMDBuild.cache.CMCacheDashboardFunctions",
			attachmentCategories: "CMDBUild.cache.CMCacheAttachmentCategoryFunctions",
			gis: "CMDBUild.cache.CMCacheGisFunctions",
			filters: "CMDBuild.cache.CMCacheFilterFunctions"
		},

		constructor: function() {
			this._lookupTypes={};

			this.toString = function() {
				return "CMCache";
			};

			this.callParent(arguments);
			this.mapOfAttributes = {};
			this.mapOfReferenceStore = {};
		},

		/**
		 * Loads all classes attributes
		 *
		 * @return (Array) mapOfAttributes
		 */
		getAllAttributesList: function() {
			for (key in _CMCache.getClasses()) {
				_CMCache.getAttributeList(key, function(attributes) {
					return;
				});
			}

			return this.mapOfAttributes;
		},

		getAttributeList: function(idClass, callback) {
			if (this.mapOfAttributes[idClass]) {
				var attributes = this.mapOfAttributes[idClass];
				callback(attributes);
			} else {
				this.loadAttributes(idClass, callback);
			}
		},

		loadAttributes: function(classId, callback) {
			var me = this;
			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(classId);

			function success(response, options, result) {
				var attributes = result.attributes;
				var visibleAttributes = [];
				for (var i=0, l=attributes.length; i<l; ++i) {
					var attribute = attributes[i];
					if (attribute.fieldmode != "hidden") {
						visibleAttributes.push(attribute);
					}
				}

				visibleAttributes.sort(function(a,b){return a.index - b.index;});

				me.mapOfAttributes[classId] = visibleAttributes;
				if (callback) {
					callback(visibleAttributes);
				}
			}

			CMDBuild.proxy.common.tabs.attribute.Attribute.read({
				params: params,
				loadMask: false,
				success: success
			});
		},

		getReferenceStore: function(reference) {
			var key = reference.referencedClassName || reference.referencedIdClass;
			var fieldFilter = false;
			var oneTimeStore = null;

			if (reference.filter || reference.oneTime) {
				//build a non cached store with the filter active
				oneTimeStore = this.buildReferenceStore(reference);
				//set the fieldFilter to false and save the current value
				//of the fieldFilter to allow the building of a full store
				fieldFilter = reference.filter;
				reference.filter = false;
			}

			//build a not filtered store and cache it
			if (!this.mapOfReferenceStore[key]) {
				this.mapOfReferenceStore[key] = this.buildReferenceStore(reference);
			}

			//restore the fieldFilter
			if (fieldFilter) {
				reference.filter = fieldFilter;
			}

			return oneTimeStore || this.mapOfReferenceStore[key];
		},

		/**
		 * @param {Object} reference
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 *
		 * @private
		 */
		buildReferenceStore: function(reference) {
			var baseParams = this.buildParamsForReferenceRequest(reference);
			var isOneTime = baseParams.CQL ? true : false;

			// Filters wrongly requested reference stores
			if (!Ext.isEmpty(baseParams['className']) || !Ext.isEmpty(baseParams['filter']))
				return CMDBuild.proxy.Cache.getStoreReference(isOneTime, baseParams);

			_warning('Invalid reference property object', this, reference);

			return Ext.create('Ext.data.Store', { // Fake empty store on invalid reference property
				fields: [],
				data: [],
				baseParams: {
					IdClass: null
				}
			});
		},

		//private
		buildParamsForReferenceRequest: function(reference) {
			var idClass = reference.idClass || reference.referencedIdClass;
			var className = reference.referencedClassName || _CMCache.getEntryTypeNameById(idClass);

			var baseParams = {
				className: className
			};

			if (reference.filter) {
				baseParams.filter = Ext.encode({
					CQL: reference.filter
				});
			} else {
				baseParams.NoFilter = true;
			}

			return baseParams;
		},

		/**
		 * @param {Object} foreignKey
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getForeignKeyStore: function(foreignKey) {
			var baseParams = { className: foreignKey.fkDestination };

			if (!Ext.isEmpty(foreignKey.filter)) {
				baseParams.filter = Ext.encode({ CQL: foreignKey.filter });
			} else {
				baseParams.NoFilter = true;
			}

			// Filters wrongly requested reference stores
			if (!Ext.isEmpty(baseParams['className']) || !Ext.isEmpty(baseParams['filter']))
				return CMDBuild.proxy.Cache.getStoreForeignKey(baseParams);

			_warning('Invalid ForeignKey object', this, reference);

			return Ext.create('Ext.data.Store', { // Fake empty store on invalid ForeignKey property
				fields: [],
				data: [],
				baseParams: {
					IdClass: null
				}
			});
		},

		isDescendant: function(subclassId, superclassId) {
			function isDescendant(tree, superclassId, subclassId) {
				if (!tree) { // don't know if this is needed
					return false;
				}

				var ids = {};
				tree.cascade(function() { ids[this.id]=this; });
				var subClass = ids[subclassId];
				var superClass = ids[superclassId];
				return superClass && subClass && subClass.isAncestor(superClass);
			};
			return superclassId == subclassId
				|| isDescendant(this.getTree('class_tree'), superclassId, subclassId)
				|| isDescendant(this.getTree('process_tree'), superclassId, subclassId);
		},

		onClassContentChanged: function(idClass) {
			reloadRelferenceStore(this.mapOfReferenceStore, idClass);
		},

		getTableGroup: getTableGroup
	});

	function readDashboardsForComboStore(me) {
		var dashboardsRaw = me.getDashboards();
		var dashboards = [];
		for (var d in dashboardsRaw) {
			d = dashboardsRaw[d];
			if (d) {
				dashboards.push({
					id: d.getId(),
					name: d.getName(),
					description: d.getDescription()
				});
			}
		}

		return dashboards;
	}

	function getTableGroup (table) {
		//the simple table are discriminate by the tableType
		var type;
		var cachedTableType = {
			"class": "class",
			processclass: "processclass",
			simpletable: "simpletable",
			report: "report",
			lookuptype: "lookuptype",
			group: "group"
		};

		if (table.tableType && table.tableType != "standard") {
			type = table.tableType;
		} else {
			type = table.type;
		}

		if (cachedTableType[type]) {
			return type;
		} else {
			throw new Error("Unsupported node type: "+type);
		}
	};

	function addAttributesToDomain(rawDomain, domain) {
		var rawAttributes = rawDomain.attributes;
		var attributeLibrary = domain.getAttributeLibrary();
		for (var i=0, l=rawAttributes.length; i<l; ++i) {

			var rawAttribute = rawAttributes[i];
			try {
				var attr = CMDBuild.core.model.CMAttributeModel.buildFromJson(rawAttribute);
				attributeLibrary.add(attr);
			} catch (e) {
				_debug(e);
			}
		}
	}

	function getFakeStore() {
		return {
			cmFill: function() {},
			cmFake: true
		};
	}

	function reloadRelferenceStore(stores, idClass) {
		var anchestors = _CMUtils.getAncestorsId(idClass);
		Ext.Array.each(anchestors, function(id) {
			var store = stores[id];
			if (store) {
				store.load();
			}
		});
	}

	_CMCache = Ext.create('CMDBuild.cache.CMCache');

})();