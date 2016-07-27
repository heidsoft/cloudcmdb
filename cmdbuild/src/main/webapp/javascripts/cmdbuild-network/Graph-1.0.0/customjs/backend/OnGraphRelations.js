(function($) {
	var OnGraphRelations = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.model = $.Cmdbuild.customvariables.model;
		this.config = param;
		this.filter = {};
		this.attributes = [];
		this.alldata = [];
		this.data = [];
		this.metadata = {};
		this.init = function() {
			this.loadAttributes();
		};
		this.loadAttributes = function() {
			this.attributes = [
					{
						type : "string",
						name : "domainDescription",
						description : $.Cmdbuild.translations.getTranslation(
								"COLUMNHEADER_RELATION", "Relation"),
						displayableInList : true
					},
					{
						type : "string",
						name : "classId",
						description : $.Cmdbuild.translations.getTranslation(
								"attr_typeId", 'Class Id'),
						displayableInList : false
					},
					{
						type : "string",
						name : "cardDescription",
						description : $.Cmdbuild.translations.getTranslation(
								"COLUMNHEADER_CARD", "Card"),
						displayableInList : true
					},
					{
						type : "string",
						name : "classDescription",
						description : $.Cmdbuild.translations.getTranslation(
								"COLUMNHEADER_CLASS", "Class"),
						displayableInList : true
					} ];

			var me = this;
			setTimeout(function() {
				me.preLoadData();
			}, 100);
		};
		this.cyCollection2Array = function(collection) {
			var array = [];
			for (var i = 0; i < collection.length; i++) {
				var element = collection[i];
				var domainId = $.Cmdbuild.g3d.Model.getGraphData(element,
						"domainId");
				var domainDescription = $.Cmdbuild.g3d.Model.getGraphData(
						element, "label");
				var relationId = $.Cmdbuild.g3d.Model.getGraphData(element,
						"relationId");
				array.push({
					domainId : domainId,
					domainDescription : domainDescription,
					relationId : relationId
				});
			}
			return array;
		};
		this.preLoadData = function() {
			this.alldata = [];
			var me = this;
			var edgesCollection = $.Cmdbuild.customvariables.model
					.connectedEdges(param.cardId);
			var edges = me.cyCollection2Array(edgesCollection);
			me.getEdges(param.cardId, edges, function(response) {

			}, me);
		};
		this.getEdges = function(cardId, edges) {
			if (edges.length == 0) {
				onObjectReady();
				return;
			}
			var edge = edges[0];
			edges.splice(0, 1);
			var domainId = edge.domainId;
			var domainDescription = edge.domainDescription;
			var relationId = edge.relationId;

			// define callbacks
			var relationCB = function(relations, r_metadata) {
				this.getRelationCB(cardId, relations, function() {
					this.getEdges(cardId, edges);
				}, this);
			};
			$.Cmdbuild.utilities.proxy.getRelation(domainId, relationId, {},
					relationCB, this);
		};
		this.getDirectRelation = function(cardId, relation, classSource, domain_info) {
			var directRelation = "";
			if (domain_info.sourceId === domain_info.destinationId) { //recursion
				directRelation = (parseInt(relation._sourceId) === parseInt(cardId)); 
			}
			else {
				directRelation = $.Cmdbuild.customvariables.cacheClasses.sameClass(
						domain_info.sourceId, classSource);				
			}
			return directRelation;
		};
		this.getRelationCB = function(cardId, relation, callback, callbackScope) {
			var me = this;
			var source = this.model.getNode(cardId);
			var classSource = $.Cmdbuild.g3d.Model.getGraphData(source,
					"classId");
			var domain_info = $.Cmdbuild.customvariables.cacheDomains
					.getDomain(relation._type);
			var directRelation = this.getDirectRelation(cardId, relation, classSource,
					domain_info);
			var domainDescription = directRelation ? domain_info.descriptionDirect
					: domain_info.descriptionInverse;
			var classId = directRelation ? domain_info.destinationId
					: domain_info.sourceId;
			var classDescription = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(classId);
			// var cardDescription = (classSource === relation._sourceType) ?
			// relation._destinationDescription
			// : relation._sourceDescription;
			var targetId = directRelation ? relation._destinationId
					: relation._sourceId;
			var target = this.model.getNode(targetId);
			var cardDescription = $.Cmdbuild.g3d.Model.getGraphData(target,
					"label");
			if (!cardDescription) {
				cardDescription = $.Cmdbuild.g3d.constants.GUICOMPOUNDNODEDESCRIPTION;
			}
			var item = {
				domainId : relation._type,
				domainDescription : domainDescription,
				relationId : relation._id,
				classId : classId,
				classDescription : classDescription,
				cardDescription : cardDescription,
				attributes : {}
			};

			resolveVariables = function(attributes) {
				if (!attributes.length) {
					me.alldata.push(item);
					callback.apply(callbackScope);
				} else {
					var attribute = attributes.splice(0, 1)[0];
					var value = relation[attribute._id];
					if (value && attribute.type === "lookup") {
						$.Cmdbuild.utilities.proxy
								.getLookupValue(
										attribute.lookupType,
										value,
										{},
										function(lookupvalue) {
											item.attributes[attribute._id] = lookupvalue.description;
										}, me);
					} else if (value) {
						item.attributes[attribute._id] = value;
					}
					resolveVariables(attributes);
				}
			};

			var custom_attributes = domain_info.domainCustomAttributes.slice();
			resolveVariables(custom_attributes);

		};
		this.loadData = function(param, callback, callbackScope) {
			// filter data
			var all_data;
			if (this.filter && this.filter.query) {
				var query = this.filter.query.trim().toLowerCase();
				all_data = this.alldata
						.filter(function(el) {
							return el.domainDescription.toLowerCase().search(
									query) !== -1
									|| el.classDescription.toLowerCase()
											.search(query) !== -1
									|| el.cardDescription.toLowerCase().search(
											query) !== -1;
						});
			} else {
				all_data = this.alldata;
			}
			this.metadata.total = all_data.length;
			// sort data
			if (param.sort) {
				var sortingColumn = param.sort;
				this.alldata.sort(function(a, b) {
					var val_a = a[sortingColumn];
					var val_b = b[sortingColumn];
					if (typeof val_a === "string") {
						val_a = val_a.toUpperCase();
					}
					if (typeof val_b === "string") {
						val_b = val_b.toUpperCase();
					}
					if (param.direction === "ASC") {
						return (val_a > val_b) ? 1 : -1;
					} else {
						return (val_a < val_b) ? 1 : -1;
					}
				});
			}
			// apply pagination
			var limit = param.firstRow + parseInt(param.nRows);
			this.data = all_data.slice(param.firstRow, limit);
			callback.apply(callbackScope, this.data);
		};
		this.getAttributes = function() {
			return this.attributes;
		};
		this.getData = function() {
			return this.data;
		};
		this.getMetadata = function() {
			return this.metadata;
		};

		/**
		 * Private functions
		 */
		var onObjectReady = function() {
			onReadyFunction.apply(onReadyScope);
		};

		/**
		 * Custom functions
		 */
		this.getTotalRows = function() {
			var metadata = this.getMetadata();
			return metadata && metadata.total ? metadata.total
					: this.alldata.length;
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.custom.backend.OnGraphRelations = OnGraphRelations;
})(jQuery);
