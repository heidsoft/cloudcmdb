(function($) {
	var sliderValue = 1;
	function reopenWithLevels(controlId, value) {
		setTimeout(function() {
			if (value === sliderValue) {
				$("#" + controlId).slider('disable');
				$.Cmdbuild.custom.commands.navigateOnNode({}, function() {
					$("#" + controlId).slider('enable');
				}, this);
			}
		}, 1000);
	}
	var commands = {
		variables : {
			BUTTONACTIVECLASS : "btn-active"
		},
		test : function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			console.log("Test", param, paramActualized);
		},
		print : function(param) {
			var file = $.Cmdbuild.global.getAppConfigUrl()
					+ $.Cmdbuild.g3d.constants.TEMPLATES_PATH
					+ $.Cmdbuild.g3d.constants.PRINT_TEMPLATE;

			var mywindow = window.open('', 'my div', 'height=600,width=900');
			$.Cmdbuild.g3d.Options.getFileFromServer(file, function(template) {
				var renderer = $.Cmdbuild.customvariables.viewer.getRenderer();
				var strImg = "<img src='"
						+ renderer.domElement.toDataURL("image/png") + "'";
				var res = template.replace("%PRINT_NETWORK_IMAGE", strImg);
				Popup(res);

				function Popup(data) {
					mywindow.document.write(data);

					mywindow.document.close(); // necessary for IE >= 10
					mywindow.focus(); // necessary for IE >= 10

					mywindow.print();
					mywindow.close();

					return true;
				}

			});

		},
		showSearchByAttributes : function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			var description = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(paramActualized.classId);
			$("#cmdbuildSearchClassDescription").text(description);
			$.Cmdbuild.standard.commands.navigate({
				form : param.navigationForm,
				container : param.navigationContainer
			});
		},
		showFilterByAttributes : function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			var description = $.Cmdbuild.customvariables.cacheClasses
					.getDescription(paramActualized.classId);
			$("#cmdbuildClassDescription").text(description);
			$.Cmdbuild.standard.commands.navigate({
				form : param.navigationForm,
				container : param.navigationContainer
			});
		},
		slidingLevels : function(param) {
			var value = $("#" + param.id + " input").val();
			if (value !== sliderValue
					&& !$.Cmdbuild.customvariables.selected.isEmpty()) {
				sliderValue = value;
				reopenWithLevels(param.id, value);
				$.Cmdbuild.customvariables.options.baseLevel = value;
				$("#baseLevel").spinner("value",
						$.Cmdbuild.customvariables.options.baseLevel);
			}
		},
		navigateOnAttributes : function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			var classes = $.Cmdbuild.custom.configuration.filterByAttributes;
			$.Cmdbuild.custom.configuration.temporaryFilterByAttributes = (classes) ? $.Cmdbuild.utilities
					.clone(classes)
					: {};
			$.Cmdbuild.dataModel.prepareCallerParameters(param.caller, {
				classId : param.classId
			});
			$.Cmdbuild.standard.commands.navigate({
				form : paramActualized.form,
				dialog : paramActualized.dialog,
				classId : paramActualized.classId
			});
		},
		closeSearchAttributesDialog : function(param) {
			$.Cmdbuild.standard.commands.dialogClose(param);
			$.Cmdbuild.customvariables.selected.erase();

			var temporaryClasses = $.Cmdbuild.custom.configuration.temporaryFilterByAttributes;
			$.Cmdbuild.custom.commands.selectByFilter({
				classId : param.classId,
				filterByAttributes : $.Cmdbuild.utilities
						.clone(temporaryClasses)
			});
		},
		closeFilterAttributesDialog : function(param) {
			var temporaryClasses = $.Cmdbuild.custom.configuration.temporaryFilterByAttributes;
			if (!$.Cmdbuild.custom.configuration.filterByAttributes) {
				$.Cmdbuild.custom.configuration.filterByAttributes = {};
			}
			$.Cmdbuild.custom.configuration.filterByAttributes = $.Cmdbuild.utilities
					.clone(temporaryClasses);
			$.Cmdbuild.standard.commands.dialogClose(param);
			$.Cmdbuild.standard.commands.navigate({
				form : param.navigationForm,
				container : param.navigationContainer,
				stayOnRow : param.stayOnRow
			});
		},
		addFilterAttribute : function(param) {
			var formObject = $.Cmdbuild.dataModel.forms[param.navigationForm];
			param = $.Cmdbuild.dataModel.resolveVariables(param);
			var attributeId = $.Cmdbuild.utilities.getHtmlFieldValue("#"
					+ param.id);
			if (!attributeId) {
				$.Cmdbuild.standard.commands.navigate({
					form : param.navigationForm,
					container : param.navigationContainer
				});
				return;
			}
			var attributeDescription = $(
					"#" + param.id + " option[value='" + attributeId + "']")
					.text();
			if (!$.Cmdbuild.custom.configuration.temporaryFilterByAttributes) {
				$.Cmdbuild.custom.configuration.temporaryFilterByAttributes = {};
			}
			var fAttributes = $.Cmdbuild.custom.configuration.temporaryFilterByAttributes;
			if (!fAttributes[param.classId]) {
				fAttributes[param.classId] = {};
			}
			$.Cmdbuild.g3d.proxy.getClassAttributes(param.classId, function(
					response) {
				var attribute = this.getAttribute(response, attributeId);
				if (!attribute) {
					console.log("Error on attribute", param.classId,
							attributeId);
				}
				if (attribute.type === "hidden") {
					attribute.type = "string";
				}
				attribute.hidden = false;
				attribute.writable = true;
				if (!fAttributes[param.classId][attribute._id]) {
					fAttributes[param.classId][attribute._id] = {
						attribute : attribute,
						data : []
					};
				}
				fAttributes[param.classId][attribute._id].data.push({
					operator : this.firstOperatorOnType(attribute),
					data : {
						firstParameter : "",
						secondParameter : ""
					}
				});

				$.Cmdbuild.standard.commands.navigate({
					form : param.navigationForm,
					container : param.navigationContainer
				});
			}, this);

		},
		firstOperatorOnType : function(attribute) {
			var options = $.Cmdbuild.custom.formAttributesFilter
					.getSpecificOperators(attribute);
			return options[0];
		},
		fieldChanged : function(configuration) {
			$.Cmdbuild.standard.commands.fieldChanged(configuration);
			if ($.Cmdbuild.customvariables.selectObservers) {
				var observers = $.Cmdbuild.customvariables.selectObservers;
				for (var i = 0; i < observers.length; i++) {
					observers[i].changedSelect(configuration);
				}

			}
		},
		getAttribute : function(attributes, id) {
			for (var i = 0; i < attributes.length; i++) {
				if (id === attributes[i]._id) {
					return attributes[i];
				}
			}
			return null;
		},
		filterDomains : function(param) {
			var classDescription = $.Cmdbuild.utilities.getHtmlFieldValue("#"
					+ param.id);
			$.Cmdbuild.standard.commands.navigate({
				form : param.navigationForm,
				container : param.navigationContainer,
				classDescription : (classDescription) ? classDescription : "-1"
			});
		},
		removeNavigationTree : function(param) {
			$.Cmdbuild.customvariables.cacheTrees.cleanCurrentNavigationTree();
			$('#treeDisplay').text("");
			$('#treeDisplayLabel').hide();
			$('#treeDisplay').hide();
		},
		applyNavigationTree : function(param) {
			var treeValue = param.treeValue;
			$.Cmdbuild.customvariables.cacheTrees
					.setCurrentNavigationTree(
							treeValue,
							function(tree) {
								var selected = $.Cmdbuild.customvariables.selected
										.getCards(0, 1);
								if (selected.total <= 0) {
									return;
								}
								var classId = selected.rows[0].classId;
								var cardId = selected.rows[0].id;
								var navigationTree = $.Cmdbuild.customvariables.cacheTrees
										.getCurrentNavigationTree();
								if (navigationTree) {
									var node = $.Cmdbuild.customvariables.model
											.getNode(cardId);
									$.Cmdbuild.customvariables.cacheTrees
											.setTreeOnNavigationManager(
													node,
													function(value) {
														if (!value) {
															return;
														}
														$('#treeDisplay')
																.text(
																		tree.description);
														$('#treeDisplayLabel')
																.show();
														$('#treeDisplay')
																.show();
														$.Cmdbuild.custom.commands
																._navigateOnNode(
																		classId,
																		cardId);
													}, this);
								}
							}, this);
		},
		navigateOnNode : function(param, callback, callbackScope) {
			var selected = $.Cmdbuild.customvariables.selected.getCards(0, 1);
			if (selected.total <= 0) {
				callback.apply(callbackScope, []);
				return;
			}
			var classId = selected.rows[0].classId;
			if (classId === $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				callback.apply(callbackScope, []);
				return;
			}
			var cardId = selected.rows[0].id;
			var navigationTree = $.Cmdbuild.customvariables.cacheTrees
					.getCurrentNavigationTree();
			if (navigationTree) {
				var node = $.Cmdbuild.customvariables.model.getNode(cardId);
				$.Cmdbuild.customvariables.cacheTrees
						.setTreeOnNavigationManager(node, function(value) {
							if (!value) {
								if (callback) {
									callback.apply(callbackScope, []);
								}
								return;
							}
							this._navigateOnNode(classId, cardId, callback,
									callbackScope);
						}, this);
			} else {
				this._navigateOnNode(classId, cardId, callback, callbackScope);

			}
		},
		applyFiltersByClass : function(param) {
			var formObject = $.Cmdbuild.dataModel.forms[param.filterByClass];
			var configuration = $.Cmdbuild.custom.configuration;
			configuration.filterClasses = [];
			if (formObject) {
				$.Cmdbuild.customvariables.selected.erase();
				for ( var key in formObject.checked) {
					if (formObject.checked[key] === false) {
						$.Cmdbuild.customvariables.selected.selectByClassName(
								key, true);
						configuration.filterClasses.push(key);
					}
				}
			}
			$.Cmdbuild.custom.commands.deleteSelection({
				selected : "true"
			});
		},
		applyFiltersByDomain : function(param) {
			var configuration = $.Cmdbuild.custom.configuration;
			var formObject = $.Cmdbuild.dataModel.forms[param.filterByDomain];
			if (formObject) {
				configuration.filterClassesDomains = [];
				for ( var key in formObject.checked) {
					$.Cmdbuild.customvariables.cacheDomains.setActive(key,
							formObject.checked[key]);
					if (formObject.checked[key] === false) {
						var domain = $.Cmdbuild.customvariables.cacheDomains
								.getDomain(key);
						if (!configuration.filterClassesDomains[domain.sourceId]) {
							configuration.filterClassesDomains[domain.sourceId] = [];
						}
						if (!configuration.filterClassesDomains[domain.destinationId]) {
							configuration.filterClassesDomains[domain.destinationId] = [];
						}
						configuration.filterClassesDomains[domain.sourceId]
								.push({
									_id : key,
									description : key
								});
						configuration.filterClassesDomains[domain.destinationId]
								.push({
									_id : key,
									description : key
								});
						$.Cmdbuild.customvariables.model.removeEdge({
							domainId : key
						});
					}
				}
				$.Cmdbuild.customvariables.model.changed(true);
			}
		},
		attributeCascade : function(classId) {
			var fAttributes = $.Cmdbuild.custom.configuration.temporaryFilterByAttributes;
			var parents = $.Cmdbuild.customvariables.cacheClasses
					.getAllParents(classId);
			// /// EMPTY
		},
		applyFiltersByAttributeRecursive : function(index, classes, toDelete,
				callback, callbackScope) {
			if (index >= classes.length) {
				callback.apply(callbackScope, []);
				return;
			}
			var key = classes[index].key;
			var nodesForClass = $.Cmdbuild.customvariables.model
					.getNodesByClassName(key);
			var attribute = $.Cmdbuild.g3d.backend.CmdbuildModel
					.getJsonFilterAttributes(classes[index].data);
			this.attributeCascade(key);
			if (attribute && nodesForClass.length > 0) {
				var jsonValues = this.getIdsArray(nodesForClass);
				$.Cmdbuild.g3d.backend.CmdbuildModel
						.getFilteredCardList(key, jsonValues, attribute,
								function(response) {
									this.deleteIfNotInFilter(nodesForClass,
											this.toTable(response), toDelete);
									this.applyFiltersByAttributeRecursive(
											index + 1, classes, toDelete,
											callback, callbackScope);
								}, this);
			} else {

				this.applyFiltersByAttributeRecursive(index + 1, classes,
						toDelete, callback, callbackScope);
			}
		},
		getIdsArray : function(nodesForClass) {
			var ids = [];
			for (var i = 0; i < nodesForClass.length; i++) {
				ids.push(parseInt(nodesForClass[i].id()));
			}
			return ids;
		},
		selectByFilter : function(param) {
			param = $.Cmdbuild.dataModel.resolveVariables(param);
			var nodesForClass = $.Cmdbuild.customvariables.model
					.getNodesByClassName(param.classId, true);
			var classes = param.filterByAttributes;
			var attribute = $.Cmdbuild.g3d.backend.CmdbuildModel
					.getJsonFilterAttributes(classes[param.classId]);
			if (attribute && nodesForClass.length > 0) {
				var jsonValues = this.getIdsArray(nodesForClass);
				$.Cmdbuild.g3d.backend.CmdbuildModel.getFilteredCardList(
						param.classId, jsonValues, attribute,
						function(response) {
							for (var i = 0; i < response.length; i++) {
								$.Cmdbuild.customvariables.selected.select(
										response[i]._id, true);
							}
							$.Cmdbuild.customvariables.selected.changed();
						}, this);
			}
		},
		applyFiltersByAttribute : function(param) {
			var configuration = $.Cmdbuild.custom.configuration;
			var toDelete = [];
			var classes = $.Cmdbuild.custom.configuration.filterByAttributes;
			this.applyFiltersByAttributeRecursive(0, this.toArray(classes),
					toDelete, function() {
						this.deleteBunch(toDelete);
					}, this);
		},
		deleteBunch : function(ids) {
			$.Cmdbuild.customvariables.selected.erase();
			for (var i = 0; i < ids.length; i++) {
				$.Cmdbuild.customvariables.selected.select(ids[i], true);
			}
			var deleteCards = new $.Cmdbuild.g3d.commands.deleteCards(
					$.Cmdbuild.customvariables.model,
					$.Cmdbuild.customvariables.selected, "true");
			$.Cmdbuild.customvariables.commandsManager.execute(deleteCards, {});
		},
		toArray : function(classes) {
			var ar = [];
			for ( var key in classes) {
				ar.push({
					key : key,
					data : classes[key]
				})
			}
			return ar;
		},
		toTable : function(cards) {
			var obj = {};
			for (var i = 0; i < cards.length; i++) {
				obj[cards[i]._id] = true;
			}
			return obj;
		},
		deleteIfNotInFilter : function(nodesForClass, tableCards, toDelete) {
			for (var i = 0; i < nodesForClass.length; i++) {
				var id = nodesForClass[i].id();
				if (!tableCards[id]) {
					toDelete.push(id);
				}
			}
		},
		applyFilters : function(param) {
			this.applyFiltersByClass(param);
			this.applyFiltersByDomain(param);
			this.applyFiltersByAttribute(param);
			$.Cmdbuild.standard.commands.dialogClose(param);
		},
		switchOnSelected : function(param) {
			var check = $.Cmdbuild.utilities.getHtmlFieldValue("#"
					+ param.check);
			$.Cmdbuild.standard.commands.tab({
				form : param.form,
				activeTab : (check) ? 1 : 0
			});
		},
		initOptions : function(param) {
			$("#nodeTooltipEnabled").prop("checked",
					$.Cmdbuild.customvariables.options.nodeTooltipEnabled);
			$("#edgeTooltipEnabled").prop("checked",
					$.Cmdbuild.customvariables.options.edgeTooltipEnabled);
			setTimeout(
					function() {
						$("#clusteringThreshold")
								.spinner(
										"value",
										$.Cmdbuild.customvariables.options.clusteringThreshold);
						$("#spriteDimension")
								.spinner(
										"value",
										$.Cmdbuild.customvariables.options.spriteDimension);
						$("#stepRadius").spinner("value",
								$.Cmdbuild.customvariables.options.stepRadius);
					}, 100);
		},
		_navigateOnNode : function(classId, cardId, callback, callbackScope) {
			$.Cmdbuild.customvariables.viewer.clearSelection();
			$.Cmdbuild.customvariables.model.erase();
			$.Cmdbuild.customvariables.viewer.refresh(true);
			var init = new $.Cmdbuild.g3d.commands.init_explode(
					$.Cmdbuild.customvariables.model, {
						classId : classId,
						cardId : cardId
					});
			$.Cmdbuild.customvariables.commandsManager.execute(init, {},
					function(response) {
						$.Cmdbuild.customvariables.selected.erase();
						$.Cmdbuild.customvariables.selected.select(cardId);
						var me = this;
						setTimeout(function() {
							me.centerOnViewer();
							if (callback) {
								callback.apply(callbackScope, []);
							}
						}, 500);
					}, this);
		},
		selectAll : function() {
			$.Cmdbuild.customvariables.selected.erase();
			var nodes = $.Cmdbuild.customvariables.model.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				$.Cmdbuild.customvariables.selected.select(nodes[i].id(), true);
			}
			$.Cmdbuild.customvariables.selected.changed();
		},
		centerOnViewer : function() {
			var box = $.Cmdbuild.customvariables.viewer.boundingBox();
			$.Cmdbuild.customvariables.viewer.zoomAll(box);

		},
		openSelection : function(param) {
			var selected = $.Cmdbuild.customvariables.selected.getData();
			var levels = $.Cmdbuild.customvariables.options.baseLevel;
			var arCommands = getExplodeCommands(selected, levels);
			var macroCommand = new $.Cmdbuild.g3d.commands.macroCommand(
					$.Cmdbuild.customvariables.model, arCommands);
			$.Cmdbuild.customvariables.commandsManager
					.execute(macroCommand, {},
							function() {
								var nodes = $.Cmdbuild.customvariables.model
										.getNodes();
								$.Cmdbuild.g3d.Model.removeGraphData(nodes,
										"exploded_children");
								$.Cmdbuild.custom.commands.centerOnViewer();
							}, $.Cmdbuild.customvariables.viewer);

		},
		optionsOk : function(param) {
			$.Cmdbuild.customvariables.viewer.refresh(true);
			$.Cmdbuild.standard.commands.dialogClose(param);
		},
		optionsPreview : function(param) {
			$.Cmdbuild.customvariables.viewer.refresh(true);
		},
		optionsCancel : function(param) {
			$.Cmdbuild.customvariables.viewer.refresh(true);
			$.Cmdbuild.standard.commands.dialogClose(param);
		},
		optionsReset : function(param) {
			$.Cmdbuild.g3d.Options.initVariables();
			$.Cmdbuild.customvariables.viewer.refresh(true);
			$.Cmdbuild.standard.commands.dialogClose(param);
		},
		boolean : function(param) {
			var value = (param.type === "displayLabel") ? param.value
					: $.Cmdbuild.utilities.getHtmlFieldValue("#" + param.type);
			if (param.type === "baseLevel") {
				$("#openLevelsSlider input").val(value);
				$("#openLevelsSlider").slider("value", value);

			}
			$.Cmdbuild.customvariables.options[param.type] = value;
			$.Cmdbuild.customvariables.options.changed();
			if ($.Cmdbuild.customvariables.viewer) {
				$.Cmdbuild.customvariables.viewer.refresh();

			}
		},
		selectClass : function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			$.Cmdbuild.customvariables.selected.selectByClassName(
					paramActualized.node, param.addSelection,
					param.superClasses === "true");
			var form2Hook = $.Cmdbuild.dataModel.forms[paramActualized.id];
			form2Hook.selectRows($.Cmdbuild.custom.classesGrid
					.getAllSelected(paramActualized.node));
		},
		selectNode : function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			if (param.addSelection === false) {
				$.Cmdbuild.customvariables.selected.erase();
				$.Cmdbuild.customvariables.selected
						.select(paramActualized.node);
			} else {
				if ($.Cmdbuild.customvariables.selected
						.isSelect(paramActualized.node)) {
					$.Cmdbuild.customvariables.selected
							.unSelect(paramActualized.node);
				} else {
					$.Cmdbuild.customvariables.selected
							.select(paramActualized.node);
				}
			}
			var form2Hook = $.Cmdbuild.dataModel.forms[paramActualized.id];
			form2Hook.selectRows($.Cmdbuild.customvariables.selected.getData());
		},
		showOnlyClasses : function(param) {
			$.Cmdbuild.customvariables.filterClassSuperclasses = false;
			$.Cmdbuild.customvariables.filterClassOnlyClasses = true;
			$.Cmdbuild.standard.commands.navigate({
				form : param.formButtons,
				container : param.containerButtons
			});
			$.Cmdbuild.standard.commands.navigate({
				form : param.form,
				container : param.container
			});

		},
		showSuperClasses : function(param) {
			$.Cmdbuild.customvariables.filterClassSuperclasses = true;
			$.Cmdbuild.customvariables.filterClassOnlyClasses = false;
			$.Cmdbuild.standard.commands.navigate({
				form : param.formButtons,
				container : param.containerButtons
			});
			$.Cmdbuild.standard.commands.navigate({
				form : param.form,
				container : param.container
			});

		},
		initialize : function(callback) {
			$.Cmdbuild.customvariables.model = new $.Cmdbuild.g3d.Model();
			$.Cmdbuild.customvariables.selected = new $.Cmdbuild.g3d.Selected(
					$.Cmdbuild.customvariables.model);

			new $.Cmdbuild.g3d.cache(callback, this);
			$.Cmdbuild.customvariables.filterClassSuperclasses = false;
			$.Cmdbuild.customvariables.filterClassOnlyClasses = true;
		},
		doLayout : function(param) {
			this.blockLayout({
				active : false,
				id : param.blockButton
			});
			$.Cmdbuild.customvariables.model.doLayout();
		},
		undo : function(param) {
			$.Cmdbuild.customvariables.commandsManager.undo();
		},
		stopCommands : function(param) {
			$.Cmdbuild.customvariables.commandsManager.stopped = true;
		},
		deleteSelection : function(param) {
			if (!$.Cmdbuild.customvariables.selected.isEmpty()) {
				var deleteCards = new $.Cmdbuild.g3d.commands.deleteCards(
						$.Cmdbuild.customvariables.model,
						$.Cmdbuild.customvariables.selected, param.selected);
				$.Cmdbuild.customvariables.commandsManager.execute(deleteCards,
						{});
			}
			$.Cmdbuild.customvariables.selected.erase();
			$.Cmdbuild.customvariables.selected.changed({});
		},
		dijkstra : function(param) {
			new $.Cmdbuild.g3d.algorithms.dijkstra(
					$.Cmdbuild.customvariables.model,
					$.Cmdbuild.customvariables.selected);
		},
		connect : function(param) {
			if ($.Cmdbuild.customvariables.selected.getCards(0, 100).total > 1) {
				new $.Cmdbuild.g3d.algorithms.connect(
						$.Cmdbuild.customvariables.model,
						$.Cmdbuild.customvariables.selected);
			}
		},
		zoomOn : function(param) {
			var paramActualized = $.Cmdbuild.dataModel.resolveVariables(param);
			$.Cmdbuild.customvariables.camera.zoomOn(paramActualized.node);
		},

		/**
		 * Execute this script when home page is ready
		 */
		homepageready : function(param) {
			var $container = $(".mainContainer");
			var $header = $(".mainContainerHeader");
			var $body = $(".mainContainerBody");
			var $footer = $(".mainContainerFooter");
			$body.css("margin-top", ($header.outerHeight() - 1) + "px").css(
					"height",
					($container.height() - $header.outerHeight()
							- $footer.outerHeight() + 2)
							+ "px");
		},

		/**
		 * Show relation attributes
		 * 
		 * @param {Object}
		 *            params
		 * @param {String}
		 *            params.form DataTable id
		 */
		showRelatioAttributes : function(params) {
			var table = $("#" + params.form).DataTable();
			var tr = $("#" + params.form).find('tr.selected');
			var row = table.row(tr);

			if (row.child.isShown()) {
				// This row is already open - close it
				row.child.hide();
				tr.removeClass('shown');
			} else {
				// Open this row
				row.child(format(params.form, {
					name : "Name",
					extn : 351351
				})).show();
				tr.addClass('shown');
			}
			table.columns.adjust();
		},

		/**
		 * Update UI of toggleTooltips element
		 * 
		 * @param {Object}
		 *            params
		 * @param {String}
		 *            params.id
		 * @param {Boolean}
		 *            params.active
		 */
		updateToggleTooltips : function(params) {
			if (params.active) {
				$("#" + params.id).parent().addClass(
						$.Cmdbuild.custom.commands.variables.BUTTONACTIVECLASS);
				$.Cmdbuild.customvariables.options.nodeTooltipEnabled = true;
				$.Cmdbuild.customvariables.options.edgeTooltipEnabled = true;
			} else {
				$("#" + params.id).parent().removeClass(
						$.Cmdbuild.custom.commands.variables.BUTTONACTIVECLASS);
				$.Cmdbuild.customvariables.options.nodeTooltipEnabled = false;
				$.Cmdbuild.customvariables.options.edgeTooltipEnabled = false;
			}
		},
		/**
		 * Update UI of toggleTooltips element
		 * 
		 * @param {Object}
		 *            params
		 * @param {String}
		 *            params.id
		 * @param {Boolean}
		 *            params.active
		 */
		updateToggleFilter : function(params) {
			if (params.active) {
				$("#" + params.id).parent().addClass(
						$.Cmdbuild.custom.commands.variables.BUTTONACTIVECLASS);
				$.Cmdbuild.customvariables.options.filterEnabled = true;
			} else {
				$("#" + params.id).parent().removeClass(
						$.Cmdbuild.custom.commands.variables.BUTTONACTIVECLASS);
				$.Cmdbuild.customvariables.options.filterEnabled = false;
			}
		},
		/**
		 * Block the layout
		 * 
		 * @param {Object}
		 *            params
		 * @param {String}
		 *            params.id
		 * @param {Boolean}
		 *            params.active
		 */
		blockLayout : function(params) {
			if (params.active) {
				$("#" + params.id).parent().addClass(
						$.Cmdbuild.custom.commands.variables.BUTTONACTIVECLASS);
				$.Cmdbuild.customvariables.options.blockedLayout = true;
			} else {
				$("#" + params.id).parent().removeClass(
						$.Cmdbuild.custom.commands.variables.BUTTONACTIVECLASS);
				$.Cmdbuild.customvariables.options.blockedLayout = false;
			}
		}

	};
	$.Cmdbuild.custom.commands = commands;

	function getExplodeCommands(selected, levels) {
		var arCommands = [];
		for ( var key in selected) {
			arCommands.push({
				command : "explode_levels",
				id : key,
				levels : levels
			});
		}
		return arCommands;
	}

	/* Formatting function for row details - modify as you need */
	function format(formid, d) {
		var data = $.Cmdbuild.dataModel.getValues(formid);
		var ddefinition = $.Cmdbuild.customvariables.cacheDomains
				.getDomain(data.domainId);
		var cAttributes = data.attributes;
		var result = '<table cellpadding="5" cellspacing="0" border="0" class="gridAttributesTable">';
		if (ddefinition.domainCustomAttributes.length) {
			result += '<tr><th colspan="2">'
					+ $.Cmdbuild.translations.getTranslation(
							"label_attributes", 'Attributes') + '</th></tr>';
			$.each(ddefinition.domainCustomAttributes, function(index,
					attribute) {
				if (cAttributes[attribute._id]) {
					result += '<tr><td>' + attribute.description + '</td><td>'
							+ cAttributes[attribute._id] + '</td></tr>';
				}
			});
		}
		result += '</table>';
		return result;
	}

	/**
	 * Add Array.filter for browsers which doesn't support it
	 */
	if (!Array.prototype.filter) {
		Array.prototype.filter = function(fun /* , thisp */) {
			var len = this.length >>> 0;
			if (typeof fun != "function")
				throw new TypeError();

			var res = [];
			var thisp = arguments[1];
			for (var i = 0; i < len; i++) {
				if (i in this) {
					var val = this[i]; // in case fun mutates this
					if (fun.call(thisp, val, i, this))
						res.push(val);
				}
			}
			return res;
		};
	}

	/**
	 * Get current item class description from cache @
	 */
	var firstTimeIsGone = false;
	function getCurrentClassDescription() {
		var classId = $.Cmdbuild.dataModel.getValue("selectedForm", "classId");
		if (!classId && firstTimeIsGone) {
			return $.Cmdbuild.translations.getTranslation("TITLE_NOSELECTION",
					"No selection")
		} else if (classId) {
			firstTimeIsGone = true;
			return $.Cmdbuild.customvariables.cacheClasses
					.getDescription(classId);
		} else {
			return "";
		}
	}

	/**
	 * update title for card and card overview pages
	 */
	window.cmdbUpdateCardOverviewTitle = function() {
		$("#cmdbuildCardOverviewTitle").text(getCurrentClassDescription());
	};
	window.cmdbUpdateCardTitle = function() {
		$("#cmdbuildCardTitle").text(getCurrentClassDescription());
	};
})(jQuery);
