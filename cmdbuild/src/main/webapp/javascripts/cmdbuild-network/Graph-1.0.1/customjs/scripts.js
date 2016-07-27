(function($) {
	var scripts = {
		canvas3d : function(param) {
			new $.Cmdbuild.g3d.Viewer(param.id);
		},
		counter : function(param) {
			new counter(param);
		},
		selectFilter : function(param) {
			try {
				$('#' + param.id).selectmenu();
				$('#' + param.id).selectmenu({
					select : function(event, ui) {
						var value = ui.item.value;
						var count = 0;
						for (var i = 0; i < param.operatorsList.length; i++) {
							if (value === param.operatorsList[i][0]) {
								count = param.operatorsList[i][2]
								break;
							}
						}
						param.indexData.operator = [ value, "", count ];
						$.Cmdbuild.standard.commands.navigate({
							form : param.navigationForm,
							container : param.navigationContainer
						});
					}
				});
			} catch (e) {
				$.Cmdbuild.errorsManager.log(e);
			}
		},
		navigationTreesBtnMenu : function(param) {
			var $ul = $("<ul></ul>").menu();
			var $div = $("<div></div>").addClass("menu-icon-separator");
			$("#" + param.id).click(function() {
				var menu = $(this).next().show().position({
					my : "right+3 top+3",
					at : "right bottom",
					of : this
				});
				menu.parent().addClass("btn-active");
				$(document).one("click", function() {
					menu.hide().parent().removeClass("btn-active");
				});
				return false;
			}).next().hide().append($div).append($ul).parent().addClass(
					"btn-disabled");
		},
		buttonset : function(param) {
			$("#" + param.id).buttonset();
		},
		slider : function(param) {
			$("#" + param.id).slider(
					{
						slide : function(event, ui) {
							$("#" + param.id + " input").val(ui.value).trigger(
									"change");
						},
						max : 10,
						min : 1,
						value : 1

					});
		},
		toggler : function(param) {
			var isEnabled = true;
			$("#" + param.id)
					.ready(
							function() {
								var val = $.Cmdbuild.custom.commands.variables.BUTTONACTIVECLASS
								if (param.initValue === "active") {
									$("#" + param.id).parent().addClass(val);
								}
							});
			$("#" + param.id)
					.click(
							function() {
								if ($(this)
										.parent()
										.hasClass(
												$.Cmdbuild.custom.commands.variables.BUTTONACTIVECLASS)) {
									// TODO: disable tooltips
									$.Cmdbuild.custom.commands[param.command]({
										id : param.id,
										active : false
									});
								} else {
									// TODO: active tooltips
									$.Cmdbuild.custom.commands[param.command]({
										id : param.id,
										active : true
									});
								}
							});
		}
	};
	$.Cmdbuild.custom.scripts = scripts;
})(jQuery);

function counter(param) {
	var myVar = setInterval(function() {
		setText(param);
	}, 1000);

	function setText(param) {
		if ($("#" + param.id).length > 0) {
			var text = "";
			switch (param.type) {
			case "nodes":
				text = $.Cmdbuild.customvariables.model.nodesLength();
				break;
			case "edges":
				text = $.Cmdbuild.customvariables.model.edgesLength();
				break;
			case "selected":
				text = $.Cmdbuild.customvariables.selected.length();
				break;
			default:
				alert("Unknown counter");
			}
			$("#" + param.id).text(text);
		} else {
			clearInterval(myVar);
		}
	}
}

