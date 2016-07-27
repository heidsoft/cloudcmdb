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
				$.Cmdbuild.g3d.proxy.getCardData(params.classId, params.cardId,
						{}, function(card) {
							this.singleCard(card, params, nodeOnNavigationTree,
									callback, callbackScope);
						}, this);
			} else {
				callback.apply(callbackScope, []);
			}
		};
		this.singleCard = function(card, params, nodeOnNavigationTree,
				callback, callbackScope) {
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
		this.getANodesBunch = function(id, callback, callbackScope) {
			var node = this.model.getNode(id);
			var classId = $.Cmdbuild.g3d.Model.getGraphData(node, "classId");
			this.getAllDomains(node, classId, id, callback, callbackScope);
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
		this.getAllDomains = function(node, classId, cardId, callback,
				callbackScope) {
			var elements = {
				nodes : [],
				edges : []
			};
			var configuration = $.Cmdbuild.custom.configuration;
			this.filteredDomains(node, configuration.filterClassesDomains,
					classId, function(filteredDomains) {
						this.getRelations(node, classId, cardId,
								filteredDomains, elements, callback,
								callbackScope);
					}, this);
		};
		this.getRelations = function(node, classId, cardId, filteredDomains,
				elements, callback, callbackScope) {
			if (filteredDomains) {
				// /------------------------------------------------
				this.getAllRelations(node, 0, filteredDomains, classId,
						parseInt(cardId), elements, callback, callbackScope);
			} else {
				$.Cmdbuild.customvariables.cacheDomains
						.getLoadingDomains4Class(classId, function(response) {
							this.getAllRelations(node, 0, response, classId,
									parseInt(cardId), elements, callback,
									callbackScope);
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
		this.getAllRelations = function(node, index, domains, classId, cardId,
				elements, callback, callbackScope) {
			if (!domains || index >= domains.length) {
				callback.apply(callbackScope, [ elements ]);
				return;
			}
			var domain = domains[index];
			var domainId = domain._id;
			var filter = this.getFilterForRelation(classId, cardId, domain);
			var param = {
				filter : filter,
				start : 0,
				limit : $.Cmdbuild.customvariables.options.clusteringThreshold
			};
			$.Cmdbuild.utilities.proxy.getRelations(domainId, param, function(
					relations, metadata) {
				if (relations.length <= 0) {
					this.getAllRelations(node, index + 1, domains, classId,
							cardId, elements, callback, callbackScope);
				} else {
					this.getCopiousRelations(node, index, domains, classId,
							cardId, elements, relations, metadata.total,
							filter, function() {
								this.getAllRelations(node, index + 1, domains,
										classId, cardId, elements, callback,
										callbackScope);
							}, this);
				}
			}, this);
		};
		this.getNoCopiousRelations = function(node, index, domains, classId,
				cardId, elements, relations, total, filter, callback,
				callbackScope) {
			var children = [];
			var domain = domains[index];
			var domainId = domain._id;
			var relationedClassFilterObj = this
					.getRelationedClassFilterOnDomain(classId, cardId, domain);
			if (relationedClassFilterObj && $.Cmdbuild.customvariables.options.filterEnabled === true) {
				this.excludeByClassFiltering(relations, elements,
						relationedClassFilterObj, function() {
							this.explodeChildren(elements, domainId,
									domain.nodeOnNavigationTree, node, classId,
									cardId, children, relations);
							node.data.children = children;
							callback.apply(callbackScope, [ elements ]);
						}, this);
				return;
			} else {
				this.explodeChildren(elements, domainId,
						domain.nodeOnNavigationTree, node, classId, cardId,
						children, relations);
			}
			node.data.children = children;
			callback.apply(callbackScope, [ elements ]);
		};
		this.compressInCompound = function(node, index, domains, classId,
				cardId, elements, relations, total, filter, callback,
				callbackScope) {
			var clusteringThreshold = $.Cmdbuild.customvariables.options.clusteringThreshold;
			if (total <= clusteringThreshold) {
				callback.apply(callbackScope, [ false ]);
				return;
			}
			var domain = domains[index];
			var domainId = domain._id;
			var relationedClassFilterObj = this
					.getRelationedClassFilterOnDomain(classId, cardId, domain);
			this.isCompoundAlsoFiltered(relations, relationedClassFilterObj,
					total, function(bToPush) {
						if (!bToPush) {
							callback.apply(callbackScope, [ false ]);
						} else {
							var compoundData = {
								domainId : domainId,
								filter : filter
							};
							this.pushCompound(relations[0], compoundData,
									domain.nodeOnNavigationTree, total,
									classId, elements, domainId, node, cardId,
									[]);
							callback.apply(callbackScope, [ true ]);
						}
					}, this);
		};
		this.getCopiousRelations = function(node, index, domains, classId,
				cardId, elements, relations, total, filter, callback,
				callbackScope) {
			var children = [];
			var domain = domains[index];
			var domainId = domain._id;
			this.compressInCompound(node, index, domains, classId, cardId,
					elements, relations, total, filter, function(bCompressed) {
						if (bCompressed) {
							callback.apply(callbackScope, [ elements ]);
						} else {
							this.getNoCopiousRelations(node, index, domains,
									classId, cardId, elements, relations,
									total, filter, function() {
										callback.apply(callbackScope,
												[ elements ]);

									}, this);
						}
					}, this);
		};
		this.isCompoundAlsoFiltered = function(relations, advancedFilterObj,
				total, callback, callbackScope) {
			if (!advancedFilterObj) {
				callback.apply(callbackScope, [ true ]);
				return;
			}
//			console.log(total + " isCompound "
//					+ advancedFilterObj.destinationId,
//					$.Cmdbuild.customvariables.cacheClasses
//							.getSubClasses(advancedFilterObj.destinationId));
			callback.apply(callbackScope, [ true ]);
		};
		this.deleteFilteredRelations = function(cardsInFilter, filters,
				relations) {
			var cardsToDelete = [];
			for (var i = 0; i < filters.data.length; i++) {
				var found = false;
				for (var j = 0; j < cardsInFilter.length; j++) {
					if (cardsInFilter[j]._id === filters.data[i].relation._sourceId
							|| cardsInFilter[j]._id === filters.data[i].relation._destinationId) {
						found = true;
						break;
					}
				}
				if (!found) {
					cardsToDelete.push(filters.data[i].relation._sourceId);
				}
			}
			for (var i = relations.length - 1; i >= 0; i--) {
				for (var j = 0; j < cardsToDelete.length; j++) {
					if (cardsToDelete[j] === relations[i]._sourceId) {
						relations.splice(i, 1);
						break;
					}
				}
			}
		};
		this.filterCardsClassByClass = function(index, filters, relations,
				advancedFilterObj, callback, callbackScope) {
			if (index >= filters.length) {
				callback.apply(callbackScope, []);
				return;
			}
			var filter = filters[index];

			var advancedFilter = this.getCardsFilterOnDomain(
					advancedFilterObj.direction, advancedFilterObj.domainId,
					advancedFilterObj.destinationId,
					advancedFilterObj.sourceId, advancedFilterObj.cardId,
					advancedFilterObj.classId);
			var filterOnClass = $.Cmdbuild.custom.configuration.filterByAttributes[filter.type];// advancedFilter.relation[0].source];//
			var jsonFilter = $.Cmdbuild.g3d.backend.CmdbuildModel
					.getJsonFilter(advancedFilter, filterOnClass);
			var param = {
				filter : jsonFilter
			};
			var ids = this.fromNodes2Id(relations, filter.type);
			$.Cmdbuild.utilities.proxy.getCardList(filter.type, param,
					function(response) {
						var filtersOnClass = this.getFiltersOnClass(
								filter.type, filters);
						this.deleteFilteredRelations(response, filtersOnClass,
								relations);
						this.filterCardsClassByClass(index + 1, filters,
								relations, advancedFilterObj, callback,
								callbackScope);
					}, this);
		};
		this.getFiltersOnClass = function(classId, filters) {
			for (var i = 0; i < filters.length; i++) {
				if (filters[i].type === classId) {
					return filters[i];
				}
			}
			return null;
		};
		this.excludeByClassFiltering = function(relations, elements,
				advancedFilterObj, callback, callbackScope) {

			var classes = {};
			for (var i = relations.length - 1; i >= 0; i--) {
				if (!classes[relations[i]._sourceType]) {
					classes[relations[i]._sourceType] = [];
				}
				classes[relations[i]._sourceType].push({
					index : i,
					relation : relations[i]
				});
				if (!classes[relations[i]._destinationType]) {
					classes[relations[i]._destinationType] = [];
				}
				classes[relations[i]._destinationType].push({
					index : i,
					relation : relations[i]
				});
			}
			// returns only classes for which is defined a filter
			var filters = this.getFilters4Classes(classes);
			this.filterCardsClassByClass(0, filters, relations,
					advancedFilterObj, function() {
						callback.apply(callbackScope, []);
					}, this);
		};
		this.getFilters4Classes = function(classes) {
			var filterClasses = $.Cmdbuild.custom.configuration.filterClasses;
			var filters = [];
			for ( var key in classes) {
				if ($.Cmdbuild.custom.configuration.filterByAttributes[key]) {
					filters.push({
						type : key,
						data : classes[key]
					});
				}
			}
			return filters;
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
			var sDescription = !direction ? destinationDescription
					: sourceDescription;
			var dDescription = direction ? destinationDescription
					: sourceDescription;
			var description = sDescription + " " + rDescription + " " + total
					+ " " + dDescription;
			var id = "CN" + relationSample._type + relationSample._sourceId
					+ relationSample._destinationId;
			compoundData._destinationType = (direction) ? relationSample._destinationType
					: relationSample._sourceType;
			this.pushAnOpeningChild(elements, domainId, nodeOnNavigationTree,
					relationSample, id, description,
					$.Cmdbuild.g3d.constants.GUICOMPOUNDNODE, compoundData,
					node, cardId, children);
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
			var compoundData = $.Cmdbuild.g3d.Model.getGraphData(node,
					"compoundData");
			var destinationType = compoundData._destinationType;
			var filterOnClass = ($.Cmdbuild.custom.configuration.filterByAttributes) ? $.Cmdbuild.custom.configuration.filterByAttributes[destinationType]
					: undefined;
			var attribute = $.Cmdbuild.g3d.backend.CmdbuildModel
					.getJsonFilterAttributes(filterOnClass);
			if (filterOnClass) {
				var jsonValues = this.fromNodes2Id(data, destinationType);
				$.Cmdbuild.g3d.backend.CmdbuildModel.getFilteredCardList(
						destinationType, jsonValues, attribute, function(
								response) {
							data = this.filterIds2Explode(response, data,
									destinationType);
							this.explodeChildren(elements, domainId, null,
									parentNode, classId, parentId, children,
									data);
							callback.apply(callbackScope, [ elements ]);
						}, this);
			} else {
				this.explodeChildren(elements, domainId, null, parentNode,
						classId, parentId, children, data);
				callback.apply(callbackScope, [ elements ]);
			}
		};
		this.filterIds2Explode = function(toExplode, ids) {
			var returnIds = [];
			var table = {};
			for (var i = 0; i < toExplode.length; i++) {
				table[toExplode[i]._id] = true;
			}
			for (var i = 0; i < ids.length; i++) {
				if (table[ids[i]._sourceId] || table[ids[i]._destinationId]) {
					returnIds.push(ids[i]);
				}
			}
			return returnIds;
		};
		this.fromNodes2Id = function(nodes, classId) {
			var ids = [];
			for (var i = 0; i < nodes.length; i++) {
				var node = nodes[i];
				ids
						.push((classId === node._destinationType) ? node._destinationId
								: node._sourceId);
			}
			return ids;
		};
		this.explodeChildren = function(elements, domainId,
				nodeOnNavigationTree, node, classId, cardId, children,
				relations) {
			var destinationId;
			var destinationDescription;
			var destinationType;
			var filterClasses = $.Cmdbuild.custom.configuration.filterClasses;
			var pushableElements = [];
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
				pushableElements.push({
					relation : relation,
					destinationId : destinationId,
					destinationDescription : destinationDescription,
					destinationType : destinationType
				});
			}
			for (var i = 0; i < pushableElements.length; i++) {
				var pe = pushableElements[i];
				this.pushAnOpeningChild(elements, domainId,
						nodeOnNavigationTree, pe.relation, pe.destinationId,
						pe.destinationDescription, pe.destinationType, {},
						node, cardId, children);
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
		this.getRelationedClassFilterOnDomain = function(classId, cardId,
				domain) {
			var bFirstClass = $.Cmdbuild.customvariables.cacheClasses
					.sameClass(domain.sourceId, classId);
			var bSecondClass = $.Cmdbuild.customvariables.cacheClasses
					.sameClass(domain.destinationId, classId);
			if (bFirstClass) {
				if ($.Cmdbuild.customvariables.cacheClasses
						.classInFilter(domain.destinationId)) {
					return {
						domainId : domain._id,
						classId : classId,
						direction : "_2",
						sourceId : domain.sourceId,
						cardId : cardId,
						destinationId : domain.destinationId
					}

				}
			} else if (bSecondClass) {
				if ($.Cmdbuild.customvariables.cacheClasses
						.classInFilter(domain.sourceId)) {
					return {
						domainId : domain._id,
						classId : classId,
						direction : "_1",
						sourceId : domain.destinationId,
						cardId : cardId,
						destinationId : domain.sourceId
					}

				}

			} else {
				console.log("Error. Class " + classId + " not found in domain "
						+ domain._id);
			}
			return null;
		};
		this.getCardsFilterOnDomain = function(direction, domainId, sourceId,
				destinationId, cardId, classId) {
			var filter = {
				"relation" : [ {
					"domain" : domainId,
					"type" : "oneof",
					"destination" : destinationId,
					"source" : sourceId,
					"direction" : direction,
					"cards" : [ {
						"className" : classId,
						"id" : cardId
					} ]
				} ]
			};
			return filter;
		};
		this.getFilterForRelation = function(classId, cardId, domain) {
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

	$.Cmdbuild.g3d.backend.CmdbuildModel.getOrs = function(filterOnAttribute) {
		var ors = [];
		var attributeId = filterOnAttribute.attribute._id;
		for (var i = 0; i < filterOnAttribute.data.length; i++) {
			var or = filterOnAttribute.data[i];
			ors.push({
				simple : {
					attribute : attributeId,
					operator : or.operator[0],
					value : [ or.data.firstParameter ],
					parameterType : "fixed"
				}

			});
		}
		var attribute = {};
		if (ors.length > 1) {
			attribute.or = ors;
		} else {
			attribute = ors[0];
		}
		return attribute;
	};
	$.Cmdbuild.g3d.backend.CmdbuildModel.getJsonFilterAttributes = function(
			filterAttributes) {
		var attribute = null;
		var ands = [];
		for ( var key in filterAttributes) {
			var ors = $.Cmdbuild.g3d.backend.CmdbuildModel
					.getOrs(filterAttributes[key]);
			ands.push(ors);
		}
		if (ands.length === 1) {
			attribute = ands[0];
		} else if (ands.length > 1) {
			attribute = {
				and : []
			};
			for (var i = 0; i < ands.length; i++) {
				attribute.and.push(ands[i]);
			}
		}
		return attribute;
	};
	$.Cmdbuild.g3d.backend.CmdbuildModel.getJsonFilter = function(
			filterRelation, filterAttributes) {
		var filter = {
			relation : filterRelation.relation
		};
		var attribute = $.Cmdbuild.g3d.backend.CmdbuildModel
				.getJsonFilterAttributes(filterAttributes);
		if (attribute) {
			filter.attribute = attribute;
		}
		return filter;
	};
	$.Cmdbuild.g3d.backend.CmdbuildModel.getFilteredCardList = function(
			classId, jsonValues, attribute, callback, callbackScope) {
		// create the and for insert the filter on ids values
		var param = {};
		if (attribute.simple) {
			attribute.and = [];
			attribute.and.push({
				simple : attribute.simple
			});
			delete attribute.simple;
		} else if (attribute.or) {
			attribute.and = [];
			attribute.and.push({
				or : attribute.or
			});
			delete attribute.or;
		}
		var filterOnIds = {
			// attribute : {
			simple : {
				attribute : "Id",
				operator : "in",
				value : jsonValues
			}
		// }
		};
		attribute.and.push(filterOnIds);
		if (attribute) {
			var filterOnClass = {
				attribute : attribute
			};
			param.filter = filterOnClass;
		}
		;
		$.Cmdbuild.utilities.proxy.getCardList(classId, param, callback,
				callbackScope);
	};
})(jQuery);
