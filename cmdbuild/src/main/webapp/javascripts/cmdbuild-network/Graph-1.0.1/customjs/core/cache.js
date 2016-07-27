(function($) {
	var FILESTORE_IMAGES = "images";
	var TYPE_CLASS = "class";
	var TYPE_PROCESS = "process";

	var cache = function(callback, callbackScope) {
		$.Cmdbuild.customvariables.cacheProcess = new cacheProcesses();
		$.Cmdbuild.customvariables.cacheDomains = new cacheDomains();
		$.Cmdbuild.customvariables.cacheClasses = new cacheClasses();
		$.Cmdbuild.customvariables.cacheImages = new cacheImages();
		$.Cmdbuild.customvariables.cacheTrees = new cacheTrees();

		// load icons data
		$.Cmdbuild.customvariables.cacheImages
				.loadData(callback, callbackScope);
	};
	var cacheTrees = function() {
		this.data = {};
		this.navigationManager = new $.Cmdbuild.g3d.navigationManager();
		this.getFilterEqual = function(classId) {
			var filter = {
				"attribute" : {
					"simple" : {
						"attribute" : "targetClass",
						"operator" : "equal",
						"value" : [ classId ]
					}
				}
			};
			return filter;
		};
		this.setTreeOnNavigationManager = function(node, callback,
				callbackScope) {
			this.navigationManager.setCurrentTree(node, function(value) {
				callback.apply(callbackScope, [ value ]);
			}, this);
		};
		this.getClassPathInTree = function(node) {
			return this.navigationManager.getClassPathInTree(node);
		};
		this.getRootNavigationTree = function(node) {
			return this.navigationManager.getRoot();
		};
		this.getFilterContain = function(classId) {
			var filter = {
				"attribute" : {
					"simple" : {
						"attribute" : "targetClass",
						"operator" : "contain",
						"value" : [ classId ]
					}
				}
			};
			return filter;
		};
		this.pushTreeForClass = function(classId, callback, callbackScope) {
			if (this.data[classId]) {
				callback.apply(callbackScope, []);
				return;
			}
			var filterEqual = this.getFilterEqual(classId);
			var filterContain = this.getFilterContain(classId);
			$.Cmdbuild.g3d.proxy.getDomainTrees({
				filter : filterEqual
			}, function(treesWithClassLikeRoot) {
				$.Cmdbuild.g3d.proxy.getDomainTrees({
					filter : filterContain
				}, function(treesAboutClass) {
					this.data[classId] = this.merge(treesWithClassLikeRoot,
							treesAboutClass);
					callback.apply(callbackScope, []);
				}, this);
			}, this);
		};
		this.merge = function(ar1, ar2) {
			function isJustHere(ar, value) {
				for (var i = 0; i < ar.length; i++) {
					if (value === ar[i]._id) {
						return true;
					}
				}
				return false;
			}
			for (var i = 0; i < ar2.length; i++) {
				if (!isJustHere(ar1, ar2[i]._id)) {
					ar1.push(ar2[i]);
				}
			}
			return ar1;
		};
		this.getTreesFromClass = function(classId) {
			if (!classId) {
				return [];
			}
			if (this.data[classId] && this.data[classId].length > 0) {
				return this.data[classId];
			}
			var classType = $.Cmdbuild.customvariables.cacheClasses
					.getClass(classId);
			if (classType && classType.parent) {
				return this.getTreesFromClass(classType.parent);
			}
			return [];
		};
		this.setCurrentNavigationTree = function(navigationTree, callback,
				callbackScope) {
			if (navigationTree) {
				$.Cmdbuild.g3d.proxy.getDomainTree(navigationTree, function(
						tree) {
					this.navigationManager.setServerTree(tree);
					callback.apply(callbackScope, [ tree ]);
				}, this);
			}
		};
		this.cleanCurrentNavigationTree = function() {
			this.navigationManager.cleanCurrentTree()
		};
		this.getCurrentNavigationTree = function() {
			return this.navigationManager.getCurrentTree();
		};
	};
	var cacheProcesses = function() {
		this.data = {};
		$.Cmdbuild.utilities.proxy.getProcesses(function(processes) {
			for (var i = 0; i < processes.length; i++) {
				this.data[processes[i]._id] = true;
			}
		}, this);
		this.isProcess = function(processId) {
			return (this.data[processId]) ? true : false;
		};
	};
	var cacheImages = function() {
		this.data = [];
		this.loadData = function(callback, callbackScope) {
			var me = this;
			try {
				$.Cmdbuild.g3d.proxy.getIcons({}, function(data, metadata) {
					me.data = data;
					callback.apply(callbackScope);
				});
			} catch (e) {
				console.log("Error on images", $.Cmdbuild.g3d.proxy.getIcons);
				me.data = [];
				callback.apply(callbackScope);
			}
		};
		this.getBaseImages = function(type) {
			var base_url = $.Cmdbuild.global.getAppConfigUrl()
					+ $.Cmdbuild.g3d.constants.SPRITES_PATH;
			switch (type) {
			case "default":
				return base_url + "default.png";
			case "selected":
				return base_url + "selected.png";
			case "current":
				return base_url + "current.png";
			case "compound":
				return base_url + "compound.png";
			case "process":
				return base_url + "process.png";
			default:
				return "";
			}
		};
		this.getImage = function(classId) {
			var type;
			if ($.Cmdbuild.dataModel.isAClass(classId)) {
				type = TYPE_CLASS;
			} else if ($.Cmdbuild.dataModel.isAProcess(classId)) {
				type = TYPE_PROCESS;
				return $.Cmdbuild.customvariables.cacheImages.getBaseImages("process");
			}
			var icons = $.grep(this.data, function(item) {
				return item.type === type && item.details.id === classId;
			});
			var url;
			if (icons && icons.length) {
				var icon = icons[0];
				try {
					url = $.Cmdbuild.utilities.proxy
							.getURIForFileStoreItemDownload(FILESTORE_IMAGES,
									icon.image.details.folder,
									icon.image.details.file);

				} catch (e) {
					console.log("Error on file : ", icon.image.details.file);
					url = $.Cmdbuild.customvariables.cacheImages
							.getBaseImages("default");
				}
			} else {
				url = $.Cmdbuild.customvariables.cacheImages
						.getBaseImages("default");
			}
			return url;
		};
	};
	var cacheClasses = function() {
		this.data = {};
		this.classInFilter = function(classId) {
			var filterByAttributes = $.Cmdbuild.custom.configuration.filterByAttributes;
			for (var key in filterByAttributes) {
				if (this.sameClass(classId, key)) {
					return filterByAttributes[key];	
				}
			}
			return null;
		};
		this.getAllParents = function(currentClass) {
			var classes = [];
			do {
				var classAttributes = this.getClass(currentClass);
				if (! classAttributes) {
					// GUICOMPOUNDNODEconsole.log("Error!", currentClass, classes);
					break;
				}
				currentClass = classAttributes.parent;
				if (currentClass) {
					classes.push(currentClass);
				}
			} while (currentClass);
			return classes;
		}
		this.sameClass = function(superClass, currentClass) {
			if (superClass === currentClass) {
				return true;
			}
			var classAttributes = this.getClass(currentClass);
			if (!(classAttributes && classAttributes.parent)) {
				return false;
			}
			return this.sameClass(superClass, classAttributes.parent);
		};
		this.getLoadingClass = function(classId, callback, callbackScope) {
			if (this.data[classId]) {
				callback.apply(callbackScope, [ this.data[classId] ]);
			} else if (classId === $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				callback.apply(callbackScope,
						[ [ $.Cmdbuild.g3d.constants.COMPOUND_ATTRIBUTES ] ]);
			} else {
				$.Cmdbuild.g3d.proxy.getClass(classId,
						function(classAttributes) {
							this.data[classId] = classAttributes;
							$.Cmdbuild.customvariables.cacheTrees
									.pushTreeForClass(classId, function() {
										if (!classAttributes.parent) {
											callback.apply(callbackScope,
													[ classAttributes ]);
										} else {
											this.getLoadingClass(
													classAttributes.parent,
													callback, callbackScope);
										}
									}, this);
						}, this);
			}
		};
		this.getClass = function(classId) {
			return this.data[classId];
		};
		this.getDescription = function(classId) {
			if (classId === $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				return $.Cmdbuild.g3d.constants.GUICOMPOUNDNODEDESCRIPTION;
			}
			return (this.data[classId] && this.data[classId].description) ? this.data[classId].description
					: "";
		};
		this.getClasses = function() {
			var classes = [];
			for ( var key in this.data) {
				classes.push({
					_id : key,
					description : this.data[key].description
				});
			}
			return classes;
		};
		this.pushClassesRecursive = function(nodes, index, callback,
				callbackScope) {
			if (index >= nodes.length) {
				callback.apply(callbackScope, []);
				return;
			}
			var node = nodes[index];
			this.getLoadingClass(node.data.classId, function() {
				this.pushClassesRecursive(nodes, index + 1, callback,
						callbackScope);
			}, this);
		};
		this.pushClasses = function(elements, callback, callbackScope) {
			this.pushClassesRecursive(elements.nodes, 0, function() {
				callback.apply(callbackScope, []);
			}, this);
		};
	};
	var cacheDomains = function() {
		this.data = [];
		this.pushClass = function(classId, callback, callbackScope) {
			$.Cmdbuild.customvariables.cacheClasses.getLoadingClass(classId,
					function() {
						this.getAllDomains(classId, function() {
							callback.apply(callbackScope, []);
						}, this);
					}, this);
		};
		this.getAllDomains = function(classId, callback, callbackScope) {
			var filter = this.getFilterForDomain(classId);
			var param = {
				filter : filter
			};
			if (! classId) {
				callback.apply(callbackScope, []);
			} else {
				$.Cmdbuild.utilities.proxy.getDomains(param,
						function(response) {
							this.getAllDomainsRecursive(response, callback,
									callbackScope);
						}, this);
			}
		};

		this.getAllDomainsRecursive = function(domains, callback, callbackScope) {
			if (domains.length === 0) {
				callback.apply(callbackScope, []);
				return;
			}
			var domain = domains[0];
			domains.splice(0, 1);
			if (this.getDomainIndex(domain._id) > -1) {
				this.getAllDomainsRecursive(domains, callback, callbackScope);
			} else {
				this.loadSingleDomain(domain._id, function() {
					this.getAllDomainsRecursive(domains, callback,
							callbackScope);
				}, this);
			}
		};
		this.loadSingleDomain = function(domainId, callback, callbackScope) {
			$.Cmdbuild.utilities.proxy.getDomain(domainId, function(
					domainAttributes) {
				$.Cmdbuild.utilities.proxy.getDomainAttributes(domainId,
						function(domainCustomAttributes) {
							this.loadExtremeClasses(domainAttributes.source,
									domainAttributes.destination, function() {
										var retDomain = this.pushDomain(
												domainAttributes,
												domainCustomAttributes);
										callback.apply(callbackScope,
												[ retDomain ]);
									}, this);
						}, this);
			}, this);
		};
		this.loadExtremeClasses = function(sourceId, destinationId, callback,
				callbackScope) {
			$.Cmdbuild.customvariables.cacheClasses.getLoadingClass(sourceId,
					function() {
						$.Cmdbuild.customvariables.cacheClasses
								.getLoadingClass(destinationId, function() {
									callback.apply(callbackScope, []);
								}, this);
					}, this);
		};
		this.pushDomain = function(domainAttributes, domainCustomAttributes) {
			var destinationDescription = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(domainAttributes.destination);
			var sourceDescription = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(domainAttributes.source);
			this.data.push({
				_id : domainAttributes._id,
				active : true,
				domainDescription : domainAttributes.description,
				destinationId : domainAttributes.destination,
				sourceId : domainAttributes.source,
				destinationDescription : destinationDescription,
				sourceDescription : sourceDescription,
				domainCustomAttributes : domainCustomAttributes,
				descriptionDirect : domainAttributes.descriptionDirect,
				descriptionInverse : domainAttributes.descriptionInverse
			});
			return this.data[this.data.length - 1];
		};
		this.getDomainIndex = function(domainId) {
			for (var i = 0; i < this.data.length; i++) {
				if (this.data[i]._id === domainId) {
					return i;
				}
			}
			return -1;
		};
		this.getDomain = function(domainId) {
			var index = this.getDomainIndex(domainId);
			return (index === -1) ? null : this.data[index];
		};
		this.getDescription = function(domainId) {
			var index = this.getDomainIndex(domainId);
			return (index === -1) ? "" : this.data[index].domainDescription;
		};
		this.setActive = function(domainId, active) {
			var domainIndex = this.getDomainIndex(domainId);
			if (domainIndex === -1) {
				console.log("ERROR ! cacheDomains setActive " + domainId);
			} else {
				this.data[domainIndex].active = active;
			}
		};
		this.getDomains4Class = function(classId) {
			var allDomains = [];
			do {
				var domains = this._getDomains4Class(classId);
				if (domains.length > 0) {
					allDomains = allDomains.concat(domains);
				}
				var classAttributes = $.Cmdbuild.customvariables.cacheClasses
						.getClass(classId);
				if (!(classAttributes && classAttributes.parent)) {
					return allDomains;
				}
				classId = classAttributes.parent;
			} while (true);
		};
		this._getDomains4Class = function(classId) {
			var domains = [];
			for (var i = 0; i < this.data.length; i++) {
				if (this.data[i].sourceId === classId
						|| this.data[i].destinationId === classId) {
					domains.push(this.data[i]);
				}
			}
			return domains;
		};
		this.getData = function() {
			return this.data;
		};
		this.getLoadingDomains4Class = function(classId, callback,
				callbackScope) {
			this.pushClass(classId, function() {
				callback.apply(callbackScope,
						[ this.getDomains4Class(classId) ]);
			}, this);
		};
		this.getFilterForDomain = function(classId) {
			var filter = {
				attribute : {
					or : [ {
						simple : {
							attribute : "source",
							operator : "contain",
							value : [ classId ]
						}
					}, {
						simple : {
							attribute : "destination",
							operator : "contain",
							value : [ classId ]
						}
					} ]
				}
			};
			return filter;
		};
	};
	$.Cmdbuild.g3d.cache = cache;
})(jQuery);