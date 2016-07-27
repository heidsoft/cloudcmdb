(function() {
	var constants = CMDBuild.Constants;

	var lookupTypeStore = getFakeStore();
	var lookupTypeStoreOnlyLeves = getFakeStore();


	var lookupTypes = {};

	var lookupAttributeStoreMap = {};

	Ext.define('CMDBuild.model.cache.LookupFieldStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'Description', type: 'string' },
			{ name: 'Id', type: 'int', defaultValue: '' },
			{ name: 'Number', type: 'int' },
			{ name: 'ParentId', type: 'int' },
			{ name: 'TranslationUuid', type: 'string' }
		]
	});

	Ext.define("CMDBUild.cache.CMCacheLookupFunctions", {

		getLookupTypes: function() {
			return lookupTypes;
		},

		addLookupTypes: function(ltypes) {
			for (var i=0, len=ltypes.length; i<len; ++i) {
				this.addLookupType(ltypes[i]);
			}
		},

		addLookupType: function(lt) {
			lookupTypes[lt.id] = Ext.create("CMDBuild.cache.CMLookupTypeModel", lt);
            lookupTypeStore.cmFill()
			lookupTypeStoreOnlyLeves.cmFill();
		},

		getLookupTypeAsStore: function() {
			if (lookupTypeStore.cmFake) {
				lookupTypeStore = buildLookupTypeStore(onlyLeaves = false);
				lookupTypeStore.cmFill();
			}

			return lookupTypeStore;
		},

		getLookupTypeLeavesAsStore: function() {
			if (lookupTypeStoreOnlyLeves.cmFake) {
				lookupTypeStoreOnlyLeves = buildLookupTypeStore(onlyLeaves = true);
				lookupTypeStoreOnlyLeves.cmFill();
			}

			return lookupTypeStoreOnlyLeves;
		},

		onNewLookupType: function(lType) {
			this.addLookupType(lType);
			lookupTypeStore.cmFill()
			lookupTypeStoreOnlyLeves.cmFill();
			this.fireEvent("cm_new_lookuptype", lType);
		},

		onModifyLookupType: function(lType) {
			if (lType.oldId) {
				lookupTypes[lType.oldId] = undefined;
				delete lookupTypes[lType.oldId];
				this.addLookupType(lType)
			} else {
				var lt = lookupTypes(lType.id);
				lt.set(lType);
			}

			this.syncLookupTypesParent(lType);
			lookupTypeStore.cmFill()
			lookupTypeStoreOnlyLeves.cmFill();
			this.fireEvent("cm_modified_lookuptype", lType);
		},

		syncLookupTypesParent: function(lType) {
			for (var i in lookupTypes) {
				var lt = lookupTypes[i];

				if (lt.get("parent") == lType.oldId) {
					lt.set("parent", lType.id);
				}
			}
		},

		/**
		 * @param {String} type
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getLookupStore: function(type) {
			if (!lookupAttributeStoreMap[type])
				lookupAttributeStoreMap[type] = CMDBuild.proxy.Cache.getStoreLookup(type);

			return lookupAttributeStoreMap[type];
		},

		getLookupchainForType: function(type) {
			var chain = [];
			type = type || "";

			while (type) {
				var findParent = false;
				for (var i in lookupTypes) {
					var lt = lookupTypes[i];
					if (lt.get("id") == type) {
						chain.push(type);
						findParent = true;
						type = lt.get("parent");
						break;
					}
				}
				if (! findParent) {
					break;
				}
			}

			return chain;
		}
	});

	function buildLookupTypeStore(onlyLeaves) {
		var store = new Ext.data.Store({
			model: "CMDBuild.cache.Lookup.typeComboStore",
			cmOnlyLeaves: onlyLeaves,
			cmFill: function() {
				this.removeAll();
				var data = [];
				for (var i in lookupTypes) {
					var lt = lookupTypes[i];
					if (!this.cmOnlyLeaves || (this.cmOnlyLeaves && !lt.children)) {
						data.push([lt.get("id")]);
					}
				}
				this.loadData(data);
				this.sort("type", "ASC");
			},
			sorters: [{
				property : 'type',
				direction : 'ASC'
			}]
		});

		return store;
	}

	function getFakeStore() {
		return {
			cmFill: function() {},
			cmFake: true
		};
	}

})();