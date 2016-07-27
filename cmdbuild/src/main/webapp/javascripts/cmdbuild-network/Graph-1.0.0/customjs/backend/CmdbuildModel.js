(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	if (!$.Cmdbuild.g3d.backend) {
		$.Cmdbuild.g3d.backend = {};
	}
	var CmdbuildModel = function() {
		this.navigationTree = undefined;
		this.setModel = function(model) {
			this.model = model;
		};
		this.getInitModel = function(params, callback, callbackScope) {
			var navigationTree = $.Cmdbuild.customvariables.cacheTrees
					.getRootNavigationTree();
			var nodeOnNavigationTree = (navigationTree) ? navigationTree._id
					: null;
			if (params) {
				$.Cmdbuild.g3d.proxy
						.getCardData(
								params.classId,
								params.cardId,
								{},
								function(card) {
									var elements = {
										nodes : [ {
											data : {
												label : card.Description,
												classId : params.classId,
												id : params.cardId,
												position : {
													x : 0,
													y : 0,
													z : 0
												},
												nodeOnNavigationTree : nodeOnNavigationTree
											}
										} ],
										edges : []
									};
									callback.apply(callbackScope, [ elements ]);
								}, this);
			} else {
				callback.apply(callbackScope, []);
			}
		};
		this.chargeModel = function(elements, domainId, nodeOnNavigationTree,
				relation, sourceId, targetId, targetDescription,
				targetClassName, compoundData, parentNode, isNew) {
			sourceId = "" + sourceId;
			targetId = "" + targetId;
			var domain = $.Cmdbuild.customvariables.cacheDomains
					.getDomain(domainId);
			if (!domain) {
				console.log("Error :", Error().stack);
			}
			if (isNew) {
				var data = {
					classId : targetClassName,
					id : targetId,
					label : targetDescription,
					color : "#ff0000",
					faveShape : 'triangle',
					domainId : domain._id,
					position : {
						x : Math.random() * 1000 - 500,
						y : Math.random() * 600 - 300,
						z : 200
					},
					nodeOnNavigationTree : nodeOnNavigationTree,
					compoundData : compoundData,
					previousPathNode : sourceId,
					fromDomain : domainId
				};
				var node = {
					data : data
				};

				elements.nodes.push(node);
			}
			var edgeId = sourceId + domain._id + targetId;
			var edge = {
				id : edgeId,
				source : sourceId,
				target : targetId,
				relationId : relation._id,
				domainId : domain._id,
				label : domain.domainDescription,
				color : $.Cmdbuild.custom.configuration.edgeColor,
				strength : 90
			};
			elements.edges.push({
				data : edge
			});
			var newNode = this.model.getNode(targetId);
			return newNode;
		};
		this.getANodesBunch = function(id, domainList, callback, callbackScope) {
			var node = this.model.getNode(id);
			var classId = $.Cmdbuild.g3d.Model.getGraphData(node, "classId");
			this.getAllDomains(node, classId, id, domainList, callback,
					callbackScope);
		};
		this.loadFilteredDomains = function(index, domains, filteredDomains,
				callback, callbackScope) {
			if (!domains || index >= domains.length) {
				callback.apply(callbackScope, [ filteredDomains ]);
				return;
			}
			$.Cmdbuild.customvariables.cacheDomains.loadSingleDomain(
					domains[index].domainId, function(domain) {
						domain.nodeOnNavigationTree = domains[index]._id;
						filteredDomains.push(domain);
						this.loadFilteredDomains(++index, domains,
								filteredDomains, callback, callbackScope);
					}, this);
		};
		this.filteredDomains = function(node, domainList, classId, callback,
				callbackScope) {
			var domains = [];
			var navigationTree = $.Cmdbuild.customvariables.cacheTrees
					.getCurrentNavigationTree();
			if (navigationTree) {
				// this overrides other filters on domains
				domains = $.Cmdbuild.customvariables.cacheTrees
						.getClassPathInTree(node);
				var filteredDomains = [];
				this.loadFilteredDomains(0, domains, filteredDomains,
						function() {
							callback.apply(callbackScope, [ filteredDomains ]);
						}, this);
				return;
			}
			// -------------------------------------

			if (!domainList) {
				callback.apply(callbackScope, [ null ]);
				return;
			}
			domains = $.Cmdbuild.customvariables.cacheDomains
					.getDomains4Class(classId);
			var ret = [];
			$.Cmdbuild.customvariables.cacheDomains.getLoadingDomains4Class(
					classId, function(response) {
						for (var j = 0; j < response.length; j++) {
							var insert = true;
							for (var i = 0; i < domains.length; i++) {
								if (response[j]._id === domains[i]._id
										&& domains[i].active === false) {
									insert = false;
									break;
								}
							}
							if (insert) {
								ret.push(response[j]);
							}
						}
						callback.apply(callbackScope, [ ret ]); // this
						// overrides
						// other
						// filters on domains
					}, this);
		};
		this.getAllDomains = function(node, classId, cardId, domainList,
				callback, callbackScope) {
			var elements = {
				nodes : [],
				edges : []
			};
			var configuration = $.Cmdbuild.custom.configuration;
			this.filteredDomains(node, configuration.filterClassesDomains,
					classId, function(filteredDomains) {
						this.getRelations(node, classId, cardId, domainList,
								filteredDomains, elements, callback,
								callbackScope);
					}, this);
		};
		this.getRelations = function(node, classId, cardId, domainList,
				filteredDomains, elements, callback, callbackScope) {
			if (filteredDomains) {
				// /------------------------------------------------
				this.getAllRelations(node, filteredDomains, domainList,
						classId, parseInt(cardId), elements, callback,
						callbackScope);
			} else {
				$.Cmdbuild.customvariables.cacheDomains
						.getLoadingDomains4Class(classId, function(response) {
							this.getAllRelations(node, response, domainList,
									classId, parseInt(cardId), elements,
									callback, callbackScope);
						}, this);
			}
		};
		this.pushAnOpeningChild = function(elements, domainId,
				nodeOnNavigationTree, relation, id, description, classId, data,
				node, parentId, children) {
			var cyNode = this.model.getNode(id);
			if (cyNode.length === 0) {
				children.push(id);
			}
			this.chargeModel(elements, domainId, nodeOnNavigationTree,
					relation, parentId, id, description, classId, data, node,
					cyNode.length === 0);
		};
		this.getAllRelations = function(node, domains, domainList, classId,
				cardId, elements, callback, callbackScope) {
			if (!domains || domains.length === 0) {
				callback.apply(callbackScope, [ elements ]);
				return;
			}
			var domain = domains[0];
			domains.splice(0, 1);
			if (domainList) {
				var arDomains = domainList.filter(function(value) {
					return (value.domainId == domain._id);
				});
				if (arDomains.length <= 0) {
					this.getAllRelations(node, domains, domainList, classId,
							cardId, elements, callback, callbackScope);
					return;
				}
			}
			var domainId = domain._id;
			var children = [];
			var filter = this.getFilterForRelation(cardId);
			var param = {
				filter : filter,
				start : 0,
				limit : $.Cmdbuild.customvariables.options.clusteringThreshold
			};
			$.Cmdbuild.utilities.proxy.getRelations(domainId, param, function(
					relations, metadata) {
				if (relations.length <= 0) {
					this.getAllRelations(node, domains, domainList, classId,
							cardId, elements, callback, callbackScope);
					return;
				}
				if (this.isCompound(relations)) {
					var compoundData = {
						domainId : domainId,
						filter : filter
					};
					this
							.pushCompound(relations[0], compoundData,
									domain.nodeOnNavigationTree,
									metadata.total, classId, elements,
									domainId, node, cardId, children);
				} else {
					this.explodeChildren(elements, domainId,
							domain.nodeOnNavigationTree, node, classId, cardId,
							children, relations);
				}
				node.data.children = children;
				this.getAllRelations(node, domains, domainList, classId,
						cardId, elements, callback, callbackScope);
			}, this);
		};
		this.pushCompound = function(relationSample, compoundData,
				nodeOnNavigationTree, total, classId, elements, domainId, node,
				cardId, children) {
			var destinationDescription = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(relationSample._destinationType);
			destinationDescription = (destinationDescription) ? destinationDescription
					: relationSample._destinationType;
			var sourceDescription = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(relationSample._sourceType);
			sourceDescription = (sourceDescription) ? sourceDescription
					: relationSample._sourceType;
			var domain = $.Cmdbuild.customvariables.cacheDomains
					.getDomain(domainId);
			var direction = (relationSample._sourceId == cardId && relationSample._sourceType == classId);
			var rDescription = direction ? domain.descriptionDirect
					: domain.descriptionInverse;
			var sDescription = ! direction ? destinationDescription
					: sourceDescription;
			var dDescription = direction ? destinationDescription
					: sourceDescription;
			var description = sDescription + " " + rDescription + " " + total + " "
					+ dDescription;
			var id = "CN" + relationSample._type + relationSample._sourceId
					+ relationSample._destinationId;
			this.pushAnOpeningChild(elements, domainId, nodeOnNavigationTree,
					relationSample, id, description,
					$.Cmdbuild.g3d.constants.GUICOMPOUNDNODE, compoundData,
					node, cardId, children);
		};
		this.isCompound = function(relations) {
			var clusteringThreshold = $.Cmdbuild.customvariables.options.clusteringThreshold;
			return (relations.length >= clusteringThreshold);
		};
		this.openCompoundNode = function(id, data, domainId, callback,
				callbackScope) {
			var node = this.model.getNode(id);
			var parentId = $.Cmdbuild.g3d.Model.getGraphData(node,
					"previousPathNode");
			var parentNode = this.model.getNode(parentId);
			var children = [];
			var elements = {
				nodes : [],
				edges : []
			};
			var classId = $.Cmdbuild.g3d.Model.getGraphData(parentNode,
					"classId");
			this.explodeChildren(elements, domainId, null,// nodeOnNavigationTree,
			parentNode, classId, parentId, children, data);
			callback.apply(callbackScope, [ elements ]);
		};
		this.explodeChildren = function(elements, domainId,
				nodeOnNavigationTree, node, classId, cardId, children,
				relations) {
			var destinationId;
			var destinationDescription;
			var destinationType;
			var filterClasses = $.Cmdbuild.custom.configuration.filterClasses;
			for (var i = 0; i < relations.length; i++) {
				var relation = relations[i];
				if (filterClasses
						&& filterClasses.length > 0
						&& (filterClasses.indexOf(relation._destinationType) != -1 || filterClasses
								.indexOf(relation._sourceType) != -1)) {
					continue;

				}
				if (relation._sourceId == cardId
						&& relation._sourceType == classId) {
					destinationId = relation._destinationId;
					destinationDescription = relation._destinationDescription;
					destinationType = relation._destinationType;
				} else {
					destinationId = relation._sourceId;
					destinationDescription = relation._sourceDescription;
					destinationType = relation._sourceType;
				}
				this.pushAnOpeningChild(elements, domainId,
						nodeOnNavigationTree, relation, destinationId,
						destinationDescription, destinationType, {}, node,
						cardId, children);
			}
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
		this.getFilterForRelation = function(cardId) {
			var filter = {
				attribute : {
					or : [ {
						simple : {
							attribute : "_sourceId",
							operator : "in",
							value : [ cardId ]
						}
					}, {
						simple : {
							attribute : "_destinationId",
							operator : "in",
							value : [ cardId ]
						}
					} ]
				}
			};
			return filter;
		};
	};
	$.Cmdbuild.g3d.backend.CmdbuildModel = CmdbuildModel;

})(jQuery);