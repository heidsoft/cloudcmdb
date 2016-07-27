(function($) {
	var ENTER = 13;

	var grid = function() {
		this.currentPage = 0;
		this.nRows = 10;
		this.paginationSettings = {};
		this.checked = {};
		this.bkChecked = {};
		this.config = {};
		this.id = null;
		this.init = function(param) {
			this.config = param;
			
			// backend load positions
			if (param.selectedItemId) {
				var positionOf = $.Cmdbuild.dataModel.resolveVariable(this.config.selectedItemId);
				param.positionOf = positionOf;
			}

			var backendFn = $.Cmdbuild.utilities.getBackend(param.backend);
			this.backend = new backendFn(this.config, this.onBackendLoaded, this);
		};
		this.onBackendLoaded = function() {
			var param = this.config;
			this.bkChecked = $.Cmdbuild.utilities.clone(this.checked);
			this.id = param.form;
			var xmlForm = $.Cmdbuild.elementsManager.getElement(this.id);
			$.Cmdbuild.elementsManager.getRowButtons(xmlForm, param);
			$.Cmdbuild.elementsManager.getColumnsCommands(xmlForm, param);

			// fields to show
			var fields = $.Cmdbuild.utilities.getFields(xmlForm);
			if (fields && fields.fields) {
				fields.form = this.id;
				$.Cmdbuild.dataModel.putFormFields(fields);
			}
			this.onDblClick = $.Cmdbuild.elementsManager.getEvent("onDblClick", xmlForm);
			this.paginationSettings = {
					nRows:  param.nRows || nRows,
					nTotalRows: 0,
					firstRow: 0,
					condition: param.condition,
					hookParent: (param.hookParent === "true"),
					parent: param.parent,
					singleSelect: (param.singleSelect == 1) ? true : false,
					onClick: $.Cmdbuild.elementsManager.getEvent("onClick", xmlForm),
					onChange: $.Cmdbuild.elementsManager.getEvent("onChange", xmlForm),
					className: param.className,
					cardId: param.cardId,
					allowSorting : (param.allowSorting && param.allowSorting == "false") ? false : true,
					allowScrollX : (param.allowScrollX && param.allowScrollX == "false") ? false : true,
					fixedLeftColumns: param.fixedLeftColumns ? param.fixedLeftColumns : 0,
					fixedRightColumns: param.fixedRightColumns ? param.fixedRightColumns : 0,
					onInitComplete : param.onInitComplete ? param.onInitComplete : null,
					emptyGridMessage : extractEmptyGridMessage(param),
					rawParam: param
			};
			var htmlStr = (param.withComboProcesses == "true") ? this.gridHeader(this.id) : "";
			if (param.lookup == "true") {
				htmlStr += this.gridButtons(this.id);
			}
			htmlStr += "<table id='" + this.id + "' class='display' width='100%'></table>";
			$.Cmdbuild.eventsManager.deferEvents();
			var htmlContainer = $("#" + param.container)[0];
			htmlStr += $.Cmdbuild.elementsManager.insertChildren(xmlForm);
			if (param.navigation != "false") {
				htmlStr += this.gridButtons(this.id);
			}
			htmlContainer.innerHTML = htmlStr;
			$.Cmdbuild.elementsManager.initialize();
			$.Cmdbuild.eventsManager.unDeferEvents();

			var theId = this.id;
			$("#" + this.id + "_filtertext").keypress(function(event) {
				if (event.which == ENTER) {
					$.Cmdbuild.standard.grid.onFilter(theId);
					return false;
				}
			});

			this.chargeFilter();
		};
		this.reset = function(param) {
			alert("grid reset " + param.type + " " + param.form);
		};
		this.change = function(param) {
			alert("grid change " + param.type + " " + param.form);
		};
		this.chargeFilter = function() {
			this.paginationSettings.backend = this.backend;
			var param = this.paginationSettings.rawParam;
			param.grid = this;
			if (param.fieldName && param.formName) {
				$.Cmdbuild.CqlManager.resolve(param.formName, param.fieldName, this.show, this);
			}
			else {
				this.show("");
			}
		};
		this.show = function(filterCql) {
			var backend = this.paginationSettings.backend;
			backend.noValidFilter = (filterCql === undefined);
			this.paginationSettings.backend = this.backend;
			
			try {
				// update first row from value of selected item
				this.updateFirstRowFromSelectedItem();

				var param = this.paginationSettings.rawParam;
				param.grid = this;
				if (param.fieldName && param.formName) {
					if (filterCql) {
						this.paginationSettings.backend.filter["CQL"] = filterCql;
					}
				}
				$.Cmdbuild.dataModel.evaluateXmlAttributes(this.id, this.paginationSettings.backend.getAttributes());
				this.showAttributes(param);
				var me = this;
				var orderColumn = (param.sort) ? this.getIndexColumn(param.sort) : this.getFirstValidColumn();
				var sortDirection = param.direction ? param.direction.toLowerCase() : "asc";
				var scrollY = undefined;
				var scrollCollapse = undefined;
				var paging = undefined;
				if (param.scroll) {
					scrollY =  param.scrollHeight + "px";
					scrollCollapse = true;
					paging = false;
				}
				var table= $('#' + this.id).dataTable({
					"scrollY":        scrollY,
			        "scrollCollapse": scrollCollapse,
			        "paging":         paging,
					"pageLength": me.nRows,
					"lengthChange": false,
					"bProcessing": true,
					"bServerSide": true,
					"searching": false,
					"paging": false,
					"scrollX": me.paginationSettings.allowScrollX,
					"info": false,
					"ordering": me.paginationSettings.allowSorting,
					"aaSorting": [[orderColumn, sortDirection]],
					"ajax": function (data, callback, settings) {
						var config = me.getParamFormLoadData(settings);
						me.paginationSettings.backend.loadData(config, function() {
							var data = {"data" : me.getData()};
							callback(data);
							me.convertReferencedValues();

							if (data.data && data.data.length) {
								// select the first row
								var selectedRow = 0;
								// if is set selected item, get index its index
								if (me.paginationSettings.rawParam.selectedItemId) {
									var positions = this.paginationSettings.backend.positions;
									var selectedItem = this.paginationSettings.rawParam.selectedItemId;
									if (positions && selectedItem) {
										var selectedItemPosition = positions[selectedItem];
										if (selectedItemPosition) {
											var rowsForPage = this.paginationSettings.nRows;
											selectedRow = (selectedItemPosition) % rowsForPage;
											this.paginationSettings.rawParam.selectedItemId = undefined;
										}
									}
									
								}
								var row = $('#' + me.id + ' tbody tr:not(".cmdbuildGroupByHeader"):eq(' + selectedRow + ')');
								var table = $('#' + me.id).DataTable();
								if (! me.paginationSettings.hookParent || ! me.paginationSettings.parent.getSelection) {
									me.selectRow(me.id, table, row, selectedRow, false);
								}
								else {
									me.selectRows(me.paginationSettings.parent.getSelection());
								}
								me.onLoad();
								me.disableRowButtons();
							} else {
								$.Cmdbuild.dataModel.setCurrentIndex(me.id, -1);
								me.onLoad();
							}
						}, me);
					},
					"columns": me.paginationSettings.attributesInGrid,
					"columnDefs": me.getNoAttributeColumns(),
					"fnInitComplete": me.paginationSettings.onInitComplete ? $.Cmdbuild.dataModel.resolveVariable(me.paginationSettings.onInitComplete) : null,
					"drawCallback": function ( settings ) {
						me.drawCallback(this, me, param, settings);
					},
					"language" : {
						"zeroRecords" : me.paginationSettings.emptyGridMessage
					}
				});
			
				if (param.groupBy) {
					var dt = $('#' + this.id).DataTable();
					dt.columns([0]).visible( false, false );
					dt.order([[ 0, 'asc' ]]);
				}
				// set fixed columns
				new $.fn.dataTable.FixedColumns(table, {
					iLeftColumns : me.paginationSettings.fixedLeftColumns,
					iRightColumns : me.paginationSettings.fixedRightColumns
				});
				// listeners
				table.on('order.dt', function() {
					// adjust columns width
					$($.fn.dataTable.tables(true)).DataTable().columns.adjust();
				});

				$('#' + this.id + ' tbody').on('click', 'tr', function (event) {
					var htmlTable =  $(this).parents('table');
					me.id = htmlTable.attr("id");
					var table = $('#' + me.id).DataTable();
					if (! $(this).hasClass('selected') || me.paginationSettings.hookParent) {
						me.selectRow(param.form, table, $(this), table.row(this).index(), event.ctrlKey);
					}
					$.Cmdbuild.eventsManager.onEvent(me.paginationSettings.onClick);
				});
				if (this.onDblClick) {
					$('#' + me.id + ' tbody').on( 'dblclick', 'tr', function () {
						$.Cmdbuild.eventsManager.executeEvent(me.onDblClick);
					});
				}
				$('#' + this.id + ' tbody').on('click', 'span', function (event) {
					var htmlTable =  $(this).parents('table');
					me.id = htmlTable.attr("id");
					var table = $('#' + me.id).DataTable();
					var row =  $(this).parents('tr');
//					var data = table.row(row).data();
					me.selectRow(param.form, table, row, table.row(row).index(), event.ctrlKey);
					var p = jQuery.parseJSON($(this).attr("method"));
					$.Cmdbuild.eventsManager.onEvent(p);
				});
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log(e);
				throw e;
			}
		};
		this.disableRowButtons = function() {
			var idGrid = this.id;
			var rowIndex = 0;
			var backend = this.paginationSettings.backend;
			$('#' + idGrid + ' tbody tr').each(function(index) {
				if (! $(this).hasClass("cmdbuildGroupByHeader")) {
					if (backend.disableRowButtons) {						
						var classesToDisable = backend.disableRowButtons(rowIndex);
						for (var i = 0; i < classesToDisable.length; i++) {
							var el = $(this).find("." + classesToDisable[i]);
							$(el).removeClass(classesToDisable[i]);
						}
						rowIndex++;
					}
				}
			});
		};
		this.onLoad = function() {
			if (this.paginationSettings.hookParent && this.paginationSettings.parent.onLoad) {
				this.paginationSettings.parent.onLoad();				
			}
		};
		this.convertReferencedValues = function() {
			var table = $('#' + this.id).DataTable();
			var backend = this.paginationSettings.backend;
			var data = backend.getData();
			for (var i = 0; i < data.length; i++) {
				for (var j = 0; j < this.paginationSettings.attributesInGrid.length; j++) {
					this.convertReferencedValue(data, table, i, j);
				}
			}
		};
		this.convertReferencedValue = function(data, table, row, column) {
			var card = data[row];
			var attribute = this.paginationSettings.attributesInGrid[column];
			var value = card[attribute.name];
			if (attribute && value) {
				$.Cmdbuild.utilities.getFieldDescription(attribute, value, function(response) {
					table.cell(row, column).data(response);
					table.columns.adjust();
				});
			} else {
				table.cell(row, column).data(value || "");
				table.columns.adjust();
			}
		};
		this.addColumnsCommands = function() {
			var commands = this.paginationSettings.rawParam.columnsCommands;
			var me = this;
			var defs = [];
			$.each(commands, function(index, command) {
				var column_index = me.getIndexColumn(command.attribute);
				defs.push({
					targets: column_index,
					mRender: function (data, type, row) {
						var html = "";
						if (data) {
							var strTooltip = (command.tooltip) ? " title = '" + command.tooltip + "' " : "";
							html = "<span "+ 
									strTooltip + 
									" class='commandcell " + 
									command.icon + 
									"' method='" + 
									JSON.stringify(command.onClick) + "'>" + 
									data + 
									"</span>";
						}
						return html;
					}
				});
			});
			return defs;
		};
		this.drawCallback = function(table, grid, param, settings) {
			if (! param.groupBy) {
				return;
			}
	        var api = table.api();
	        var rows = api.rows( {page:'current'} ).nodes();
	        var last=null;
	
	        api.column(0, {page:'current'} ).data().each( function ( group, i ) {
	            if ( last !== group ) {
	                $(rows).eq( i ).before(
	                    '<tr class="cmdbuildGroupByHeader"><td colspan="5">'+group+'</td></tr>'
	                );
	
	                last = group;
	            }
	        });
		};
		
		this.selectRows = function(selected) {
			var table = $('#' + this.id).DataTable();
			var data = this.paginationSettings.backend.getData();
			for (var i = 0; i < data.length; i++) {
				var row = data[i];
				var tRow = table.row(i).node();
				if (selected[row.id]) {
					$(tRow).addClass('selected');
				}
				else {
					$(tRow).removeClass('selected');
				}
			}			
		};
		this.selectRow = function(name, table, row, index, ctrlKey) {
			$.Cmdbuild.dataModel.setCurrentIndex(name, index);
			
				table.$('tr.selected').removeClass('selected');
				row.addClass('selected');
			if (this.paginationSettings.onChange) {
				this.paginationSettings.onChange.addSelection = ctrlKey;
				$.Cmdbuild.eventsManager.onEvent(this.paginationSettings.onChange);
			}
		};
		this.getNoAttributeColumns = function() {
			var columnDefs = [];
			var attributes = this.paginationSettings.backend.getAttributes();
			var index = 0;
			for (var i = 0; attributes && i < attributes.length; i++) {
				var attribute = attributes[i];
				if (attribute.displayableInList === true || attribute.displayableInList === "true") {
					if (attribute.type == "IMAGEBUTTON") {
						var strTooltip = (attribute.tooltip) ? " title = '" + attribute.tooltip + "' " : "";
						columnDefs.push({
							"width": 20,
							"targets": index,
							"data": null,
							"title" : "&nbsp;",
							"orderable": false,
							"defaultContent": "<span "+ strTooltip + " class='" + attribute.className + "' method='" + attribute.onClick + "'></span>"
						});
					}
					else if (attribute.type == "ROWSELECTIONCHECK") {
						var me = this;
						var strTooltip = (attribute.tooltip) ? " title = '" + attribute.tooltip + "' " : "";
						columnDefs.push({
							"width": 20,
							"targets": index,
							"data": null,
							"title" : "&nbsp;",
							"orderable": false,
							"render": function ( data, type, full, meta ) {
									var checked = (data[0]) ? " checked " : "";//TODO column 0 is WRONG!!!!! 
									//(is because check is on first but is a bit an assumption)
									return  "<input name='rowSelectionCheck' "+ strTooltip + " type='checkbox' " + checked + " onclick='" +
										"$.Cmdbuild.standard.grid.clickOnCheck(this, \"" + me.id + "\")'>";
							 }
						});
						
					}
					index++;
				}
			}
			var commands = this.addColumnsCommands();
			return $.merge(columnDefs, commands);
		};
		this.getFirstValidColumn = function() {
			for (var i = 0; i < this.paginationSettings.attributesInGrid.length; i++) {
				var type = this.paginationSettings.attributesInGrid[i].type;
				if (type != "IMAGEBUTTON" && type != "ROWSELECTIONCHECK") {
					return i;
				}
			}
		};
		this.showAttributes = function(param) {
			try {
				if (param.rowButtons) {
					$.Cmdbuild.elementsManager.loadIconButtons(this.paginationSettings.backend.getAttributes(), param.rowButtons, param.positionButtons);
				}
				if (param.selection === "true") {
					$.Cmdbuild.elementsManager.pushSelectionCheck(this.paginationSettings.backend.getAttributes(), "left");
				}
				this.paginationSettings.attributesInGrid = [];
				for (var i = 0; i < this.paginationSettings.backend.getAttributes().length; i++) {
					var attribute = this.paginationSettings.backend.getAttributes()[i];
					if (attribute.displayableInList === true || attribute.displayableInList === "true") {
						this.paginationSettings.attributesInGrid.push({
							"name": attribute.name,
							"title": attribute.description,
							"tooltip": attribute.tooltip,
							"type": attribute.type,
							"length": attribute.length,
							"targetClass": attribute.targetClass,
							"lookupType": attribute.lookupType,
							"scale": attribute.scale
						});
					}
				}
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log(e);
			}
		};
		this.getIndexColumn = function(name) {
			for (var i = 0; i < this.paginationSettings.attributesInGrid.length; i++) {
				if (this.paginationSettings.attributesInGrid[i].name == name) {
					return i;
				}
			}
			return 0;
		};
		this.getParamFormLoadData = function(settings) {
			param = this.paginationSettings;
			param.sort = this.paginationSettings.attributesInGrid[settings.aaSorting[0][0]].name;
			param.direction = (settings.aaSorting[0][1] == "asc") ? "ASC" : "DESC";
			if (!param.form) {
				param.form = this.id;
			}
			return param;
		};
		this.getData = function() {
			try {
				if (this.paginationSettings.rawParam.selectionDefault === "true") {
					this.selectAll();
				}
				var backend = this.paginationSettings.backend;
				var data = backend.getData();
				$.Cmdbuild.dataModel.push({
					form: this.id,
					type: "grid",
					currentIndex: (backend.getTotalRows() > 0) ? 0 : -1,
					data: data
				});
				var rowsInGrid = [];
				for (var i = 0; i < data.length; i++) {
					var card = data[i];
					var row = [];
					for (var j = 0; j < this.paginationSettings.attributesInGrid.length; j++) {
						var value = undefined;
						if (this.paginationSettings.attributesInGrid[j].name == "selectionCheck") {
							value = this.checked[card._id];
						}
						else {
							value = this.getFieldValue(card, this.paginationSettings.attributesInGrid[j]);
						}
						row.push(value);
					}
					rowsInGrid.push(row);
				}
				this.paginationSettings.nTotalRows = backend.getTotalRows();
				this.enableGridButtons(this.id);
				this.setPageCount(this.id);
				return rowsInGrid;
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.grid.getData " + e.message);
				throw e;
			}
		};
		this.getFieldValue = function(card, attribute) {
			if (! card[attribute.name]) {
				return "";
			}
			else if (attribute.type == "lookup") {
				return "";
			}
			else if (attribute.type == "reference") {
				return "";
			}
			else if (["date", "time", "timestamp","dateTime"].indexOf(attribute.type) != -1) {
				return $.Cmdbuild.utilities.convertDateDB2GUI(card[attribute.name], attribute.type);
			}
			else {
				return card[attribute.name] || "";
			}
		};
		this.gridHeader = function(id) {
			var htmlStr = "<div class='cmdbuildGridHeaderWrapper'>";
			var lookupId = id + "_processStatuses";
			var ca = " id='" + lookupId + "' class='cmdbuildGridProcessStatuses' ";
			var change = " onChange='$.Cmdbuild.standard.grid.onSelectStatus(\"" + lookupId + "\", \"" + this.id + "\")'";
			htmlStr += "<select " + ca + change + ">";
			htmlStr += "</select>";
			var param = {
					script : "select",
					id : lookupId,
					backend: "Select",
					processName: this.paginationSettings.className,
					value: "$fromBackend"
				};
			$.Cmdbuild.scriptsManager.push(param);
			htmlStr += "</div>";
			return htmlStr;
		};
		this.newgridButtons = function(id) {
                        var htmlStr = "<div class='cmdbuildGridNavigationWrapper'>";
                        htmlStr += "<div class='cmdbuildGridFilterContainer'>";
                        //add for IE
                        htmlStr += "<!--[if IE]><br/><input type='text' ";//style='display:none;'";
                        htmlStr += /*" disabled='disabled'*/" size='20' value='Ignore field IE bug fix'";
			htmlStr += "/><br/><![endif]-->";
                        //add for IE
                        htmlStr += "<input type='text' id='" + id + "_filtertext' />";
                        htmlStr += this.gridButton(id + "_filter", "ui-icon-search",
                                        "$.Cmdbuild.standard.gridMenu.onFilter(\"" + id + "\")");
                        htmlStr += this.gridButton(id + "_clearFilter", "ui-icon-closethick",
                                        "$.Cmdbuild.standard.gridMenu.onClearFilter(\"" + id + "\")");
                        htmlStr += "</div>";
                        htmlStr += "<div class='cmdbuildGridNavigationContainer'>";
                        htmlStr += this.gridButton(id + "_init", "ui-icon-arrowthickstop-1-w",
                                        "$.Cmdbuild.standard.gridMenu.onNavigate(\"begin\", \"" + id + "\")");
                        htmlStr += this.gridButton(id + "_previous", "ui-icon-arrowthick-1-w",
                                        "$.Cmdbuild.standard.gridMenu.onNavigate(\"previous\", \"" + id + "\")");
                        htmlStr += this.gridButton(id + "_next", "ui-icon-arrowthick-1-e",
                                        "$.Cmdbuild.standard.gridMenu.onNavigate(\"next\", \"" + id + "\")");
                        htmlStr += this.gridButton(id + "_end", "ui-icon-arrowthickstop-1-e",
                                        "$.Cmdbuild.standard.gridMenu.onNavigate(\"end\", \"" + id + "\")");
                        htmlStr += "<p id='" + id + "_pageCount' class='cmdbuildGridNavigationCount'>Page 1 of 123</p>";
                        htmlStr += "</div>";
                        htmlStr += '<div class="cmdbuildClear"></div>';
                        htmlStr += "</div>";
                        return htmlStr;
                };
		this.gridButtons = function(id) {
			var htmlStr = "<div class='cmdbuildGridNavigationWrapper'>";
			htmlStr += "<div class='cmdbuildGridFilterContainer'>";
			
			htmlStr += "<input type='text' id='" + id + "_filtertext' />";
			htmlStr += this.gridButton(id + "_filter", "ui-icon-search",
					"$.Cmdbuild.standard.grid.onFilter(\"" + id + "\")");
			htmlStr += this.gridButton(id + "_clearFilter", "ui-icon-closethick",
					"$.Cmdbuild.standard.grid.onClearFilter(\"" + id + "\")");
			htmlStr += "</div>";
			htmlStr += "<div class='cmdbuildGridNavigationContainer'>";
			htmlStr += this.gridButton(id + "_init", "ui-icon-arrowthickstop-1-w",
					"$.Cmdbuild.standard.grid.onNavigate(\"begin\", \"" + id + "\")");
			htmlStr += this.gridButton(id + "_previous", "ui-icon-arrowthick-1-w",
					"$.Cmdbuild.standard.grid.onNavigate(\"previous\", \"" + id + "\")");
			htmlStr += this.gridButton(id + "_next", "ui-icon-arrowthick-1-e",
					"$.Cmdbuild.standard.grid.onNavigate(\"next\", \"" + id + "\")");
			htmlStr += this.gridButton(id + "_end", "ui-icon-arrowthickstop-1-e",
					"$.Cmdbuild.standard.grid.onNavigate(\"end\", \"" + id + "\")");
			htmlStr += "<p id='" + id + "_pageCount' class='cmdbuildGridNavigationCount'>Page 1 of 123</p>";
			htmlStr += "</div>";
			htmlStr += '<div class="cmdbuildClear"></div>';
			htmlStr += "</div>";
			return htmlStr;
		};
		this.enableGridButtons = function(id) {
			var firstRow = parseInt(this.paginationSettings.firstRow);
			var nRows = parseInt(this.paginationSettings.nRows);
			var nTotalRows = parseInt(this.paginationSettings.nTotalRows);
			$("#" + id + "_init").button( "option", "disabled", firstRow == 0);
			$("#" + id + "_previous").button( "option", "disabled", firstRow == 0);
			$("#" + id + "_next").button( "option", "disabled", firstRow + nRows >= nTotalRows );
			$("#" + id + "_end").button( "option", "disabled", firstRow + nRows >= nTotalRows);
		};
		this.setPageCount = function(id) {
			var firstRow = parseInt(this.paginationSettings.firstRow);
			var nRows = parseInt(this.paginationSettings.nRows);
			var nTotalRows = parseInt(this.paginationSettings.nTotalRows);
			var strRowsCount = $.Cmdbuild.utilities.formatVarString($.Cmdbuild.global.GRIDROWSCOUNT, 
					(parseInt(firstRow / nRows) + 1),
					parseInt((nTotalRows + nRows - 1) / nRows),
					nTotalRows
			);
			$("#" + id + "_pageCount").html(strRowsCount);
		};
		this.gridButton = function(id, icon, onclick) {
			return $.Cmdbuild.elementsManager.makeServiceButton("cmdbuildButton", id, icon, onclick);
		};
		this.getBackend = function() {
			return this.paginationSettings.backend;
		};
		this.selectAll = function() {
			var backend = this.paginationSettings.backend;
			this.checked = {};
			for (var i = 0; i < backend.getTotalRows(); i++) {
				this.checked[backend.data[i]._id] = "true";
			};
		};
		this.updateFirstRowFromSelectedItem = function() {
			var positions = this.paginationSettings.backend.positions;
			var selectedItem = this.paginationSettings.rawParam.selectedItemId;
			if (positions && selectedItem) {
				var selectedItemPosition = positions[selectedItem];
				if (selectedItemPosition) {
					var rowsForPage = this.paginationSettings.nRows;
					var firstRow = (Math.floor(selectedItemPosition / rowsForPage) * rowsForPage);
					this.paginationSettings.firstRow = firstRow;
				}
			}
		};
		
	};
	$.Cmdbuild.standard.grid = grid;
	// Statics
	$.Cmdbuild.standard.grid.onSelectStatus = function(name, form) {
		var status = $.Cmdbuild.utilities.getHtmlFieldValue("#" + name);
		var formObject = $.Cmdbuild.dataModel.forms[form];
		var backend = formObject.getBackend();
		if (backend.setFilter) {
			backend.setFilter({
				flowStatus: status
			});
			var table = $('#' + form).DataTable();
			table.ajax.reload( function ( json ) {
			} );
		}
		else {
			$.Cmdbuild.errorsManager.log("The method setFilter is not defined on the grid backend");
		}
	};
	$.Cmdbuild.standard.grid.onFilter = function(id) {
		var grid = $.Cmdbuild.dataModel.forms[id];
		grid.paginationSettings.firstRow = 0;
		var strFilter = $("#" + id + "_filtertext").val();
		grid.paginationSettings.backend.filter["query"] = $.trim(strFilter);
		var table = $('#' + id).DataTable();
		table.ajax.reload( function ( json ) {
		} );
	};
	$.Cmdbuild.standard.grid.onClearFilter = function(id) {
		var grid = $.Cmdbuild.dataModel.forms[id];
		grid.paginationSettings.firstRow = 0;
		grid.paginationSettings.backend.filter["query"] = "";
		$("#" + id + "_filtertext").val("");
		var table = $('#' + id).DataTable();
		table.ajax.reload( function ( json ) {
		} );
	};
	$.Cmdbuild.standard.grid.cancelSelection = function(form) {
		var formObject = $.Cmdbuild.dataModel.forms[form];
		if (formObject) {
			formObject.checked = $.Cmdbuild.utilities.clone(formObject.bkChecked);
		}
	};
	$.Cmdbuild.standard.grid.clearSelection = function(form) {
		var formObject = $.Cmdbuild.dataModel.forms[form];
		if (formObject) {
			formObject.checked = {};
		}
	};
	$.Cmdbuild.standard.grid.getChecked = function(form) {
		var formObject = $.Cmdbuild.dataModel.forms[form];
		if (formObject) {
			return formObject.checked;
		}
		else {
			return {};
		}
	};
	$.Cmdbuild.standard.grid.clickOnCheck = function(element, form) {
		var checked = element.checked;
		var htmlTable =  $(element).parents('table');
		var id = htmlTable.attr("id");
		var table = $('#' + id).DataTable();
		var row =  $(element).parents('tr');
		var formObject = $.Cmdbuild.dataModel.forms[form];
		formObject.selectRow(form, table, row, table.row(row).index());
		var data = $.Cmdbuild.dataModel.getValues(form);
		var ar = $(htmlTable).find("input[name='rowSelectionCheck']");
		if (formObject.paginationSettings.singleSelect) {
			ar.each(function() {
				if (this != element) {
					$(this).attr('checked', false);
				}
			});	
			formObject.checked = [];
		}
		formObject.checked[data._id] = checked;
	};
	$.Cmdbuild.standard.grid.onNavigate = function(where, id) {
		var grid = $.Cmdbuild.dataModel.forms[id];
		var firstRow = parseInt(grid.paginationSettings.firstRow);
		var nRows = parseInt(grid.paginationSettings.nRows);
		var nTotalRows = parseInt(grid.paginationSettings.nTotalRows);
		switch (where) {
			case "begin":
				firstRow = 0;
				break;
			case "end":
				if (nTotalRows > 0 && nRows > 0) {
					firstRow = Math.floor(nTotalRows/nRows) * nRows;
				} else {
					firstRow = 0;
				}
				break;
			case "previous":
				var n = firstRow - nRows;
				firstRow = (n > 0) ? n : 0;
				break;
			case "next":
				firstRow = firstRow + nRows;
				break;
			default:
				firstRow = 0;
				break;
		}
		grid.paginationSettings.firstRow = firstRow;
		var table = $('#' + id).DataTable();
		table.ajax.reload( function ( json ) {
		} );
	};

	/**
	 * Get the string for empty table.
	 * @param {Object} config Grid configuration
	 * @return {String}
	 */
	function extractEmptyGridMessage(config) {
		if (config.emptyGridI18nMessage) {
			return $.Cmdbuild.translations.getTranslation(
					config.emptyGridI18nMessage, config.emptyGridMessage
							? config.emptyGridMessage
							: config.emptyGridI18nMessage);
		} else if (config.emptyGridMessage) {
			return config.emptyGridMessage;
		}
		return "";
	}
}) (jQuery);