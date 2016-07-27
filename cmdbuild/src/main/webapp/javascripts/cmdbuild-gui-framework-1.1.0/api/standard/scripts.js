(function($) {
	var scripts = {
		init: function(param) {
			$.Cmdbuild.eventsManager.onEvent(param);
		},
		htmlText: function(param) {
			$('#' + param.id).cleditor();	
		},
		spinner: function(param) {
			var maxValue = param.max;
			var minValue = param.min;
			$('#' + param.id).spinner({
				  spin: function( event, ui ) {
				        if ( ui.value > maxValue ) {
				            $( this ).spinner( "value", maxValue );
				            return false;
				          } else if ( ui.value < minValue ) {
				            $( this ).spinner( "value", minValue );
				            return false;
				          }
					  $.Cmdbuild.eventsManager.onEvent(param.spin);
				  },
				  change: function( event, ui ) {
				        if ( ui.value > maxValue ) {
				            $( this ).spinner( "value", maxValue );
				            return false;
				          } else if ( ui.value < minValue ) {
				            $( this ).spinner( "value", minValue );
				            return false;
				          }
					  $.Cmdbuild.eventsManager.onEvent(param.spin);
				  }
			});	
		},
		lookup: function(param) {
			try {
				if(!param.readOnly) {
					$('#' + param.id).selectmenu();
					$('#' + param.id).selectmenu({ 
						select: function (event, ui) { 
							var val = $.Cmdbuild.utilities.getHtmlFieldValue("#" + param.id);
							param.value = val;
							if ($.Cmdbuild.custom.commands && $.Cmdbuild.custom.commands.fieldChanged) {
								$.Cmdbuild.custom.commands.fieldChanged(param);
							}
							else {
								$.Cmdbuild.standard.commands.fieldChanged(param);
							}
							var onchange = $('#' + param.id).attr("onChange");
							eval(onchange);
						}
					});
				};
				var objectField = new $.Cmdbuild.standard.lookupField(param);
				$('#' + param.id)[0].objectField = objectField;
			} catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.scripts.lookup " + e.message);
				throw e;
			}
		},
		select: function(param) {
			try {
				$('#' + param.id).selectmenu();
				$('#' + param.id).selectmenu({ 
					select: function (event, ui) { 
						var val = $.Cmdbuild.utilities.getHtmlFieldValue("#" + param.id);
						param.value = val;
						if ($.Cmdbuild.custom.commands && $.Cmdbuild.custom.commands.fieldChanged) {
							$.Cmdbuild.custom.commands.fieldChanged(param);
						}
						else {
							$.Cmdbuild.standard.commands.fieldChanged(param);
						}
						var onchange = $('#' + param.id).attr("onChange");
						eval(onchange);
					}
				});
				var objectField = new $.Cmdbuild.standard.selectField(param);
				$('#' + param.id)[0].objectField = objectField;
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log(e);
			}
		},
		reference: function(param) {
			try {
				if(!param.readOnly) {
					$('#' + param.id).selectmenu();
					$('#' + param.id).selectmenu({ 
						select: function (event, ui) { 
							var val = $.Cmdbuild.utilities.getHtmlFieldValue("#" + param.id);
							param.value = val;
							if ($.Cmdbuild.custom.commands && $.Cmdbuild.custom.commands.fieldChanged) {
								$.Cmdbuild.custom.commands.fieldChanged(param);
							}
							else {
								$.Cmdbuild.standard.commands.fieldChanged(param);
							}
							var onchange = $('#' + param.id).attr("onChange");
							eval(onchange);
						} 
					});
				}
				var objectField = new $.Cmdbuild.standard.referenceField(param);
				$('#' + param.id)[0].objectField = objectField;
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.scripts.lookup " + e.message);
				throw e;
			}
		},
		checkbox: function(param) {
			$('input[type="checkbox"][id="' + param.id + '"]').change(function() {
				if(this.checked) {
					$('label[for="' + param.id + '"]').addClass("checked");
				} else {
					$('label[for="' + param.id + '"]').removeClass("checked");
				}
			});
		},
		integer: function(param) {
			try {
				//$('#' + param.id).mask("##########");
			}
			catch (e) {
				if (e.message != $.Cmdbuild.errorsManager.CMERROR) {
					e.message += "\n" + "$.Cmdbuild.standard.scripts.integer";
				}
				$.Cmdbuild.errorsManager.log(e);
				throw e;
			}
		},
		openMenu: function(param) {
			try {
				$('#' + param.id).tooltip({ content: $('#' + param.id).attr("title")});
			}
			catch (e) {
				if (e.message != $.Cmdbuild.errorsManager.CMERROR) {
					e.message += "\n" + "$.Cmdbuild.standard.scripts.openMenu";
				}
				$.Cmdbuild.errorsManager.log(e);
				throw e;
			}
		},
		date: function(param) {
			try {
				var $input = $('#' + param.id);
				switch (param.type) {
				case "timestamp":
					$input.datetimepicker({dateFormat: 'dd/mm/yy', timeFormat: "HH:mm:ss"});
					break;
				case "time":
					$input.timepicker({timeFormat: "HH:mm:ss"});
					break;
				case "dateTime":
					$input.datetimepicker({dateFormat: 'dd/mm/yy', timeFormat: "HH:mm:ss"});
					break;
				case "date":
					$input.datepicker({dateFormat: 'dd/mm/yy'});
					break;
				}
			}
			catch (e) {
				if (e.message != $.Cmdbuild.errorsManager.CMERROR) {
					e.message += "\n" + "$.Cmdbuild.standard.scripts.date";
				}
				$.Cmdbuild.errorsManager.log(e);
				throw e;
			}
		},
		dialog: function(param) {
			$("#" + param.id).dialog({
				autoOpen: false,
				modal: true,
				beforeClose: (param.callback) ? param.callback : function() {},
				show: {
					effect: "fade",
					duration: 250
				},
				hide: {
					effect: "explode",
					duration: 500
				}
			});
		},
		menu: function(param) {
			$('#' + param.id).menu();
		},
		grid: function(param) {
			try {
				param.backend = $.Cmdbuild.utilities.getBackend(param.backend);
			}
			catch (e) {
				if (e.message != $.Cmdbuild.errorsManager.CMERROR) {
					e.message += "\n" + "$.Cmdbuild.standard.scripts.grid";
				}
				$.Cmdbuild.errorsManager.log(e);
				throw e;
			}
		},
		button: function(param) {
			$('#' + param.id)
				.button({
					icons: {
						primary: param.icon
					},
					text: false
				})
				.click(function( event ) {
					event.preventDefault();
				}
			);
		},
		graph: function(param) {
		    var settings = {
		            defaultNodeColor: '#ec5148',
		            defaulLabelColor: '#99f',
		            defaultEdgeColor: '#aaa',
		            edgeColor: "default", 
		            labelSizeRatio: ".5",
		            labelThreshold: .1,
		        };
			var s = new sigma({
				  container: param.id,
				  settings: settings
				});
			visit(param.className, param.cardId, param.description, {}, {}, {}, s);
//			s.refresh();
			s.startForceAtlas2();
//			this.TM = setTimeout(function() { s.stopForceAtlas2(); }, 5000);
		}
	};
	$.Cmdbuild.standard.scripts = scripts;
}) (jQuery);

