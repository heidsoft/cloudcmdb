(function($) {
	var tabbed = function() {
		this.param = undefined;
		this.forms = [];
		this.init = function(param) {
			try {
				this.param = param;
				this.show();
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.tabbed.init");
				throw e;
			}
		};
		this.show = function() {
			try {
				var xmlForm = $.Cmdbuild.elementsManager.getElement(this.param.form);
				this.forms = this.getForms(xmlForm);
				this.prepareParams(this.forms);
				this.includeForms();
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.tabbed.show");
				throw e;
			}
		};
		this.showCB = function() {
			try {
				var xmlForm = $.Cmdbuild.elementsManager.getElement(this.param.form);
				$.Cmdbuild.eventsManager.deferEvents();
				var id = $.Cmdbuild.elementsManager.getXmlElementId(xmlForm);
				this.forms = this.getForms(xmlForm); // includeForms consumes the this.forms array
				var htmlStr = "<div id='" + id + "'>";
				htmlStr += this.insertTitles(this.forms);
				htmlStr += this.insertForms(this.forms);
				htmlStr += "</div>";
				var htmlContainer = $("#" + this.param.container)[0];
				htmlContainer.innerHTML = htmlStr;
				$.Cmdbuild.elementsManager.initialize();
				$.Cmdbuild.eventsManager.unDeferEvents();
				$("#"+ id).tabs({
					"activate": function(event, ui) {
						$($.fn.dataTable.tables(true)).DataTable().columns.adjust();
						$.Cmdbuild.standard.tabbed.fireChangeTab(ui.newPanel[0].id);
					}
				});
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.tabbed.show");
				throw e;
			}
		};
		this.getForms = function(xmlForm) {
			var forms = [];
			var $xmlForm = $(xmlForm);
			$xmlForm.children("form").each(function(index) {
				var $this = $(this);
				var param = $.Cmdbuild.elementsManager.getParams($this);
				var fields = $.Cmdbuild.utilities.getFields($this);
				if (fields) {
					$.Cmdbuild.dataModel.putFormFields(fields);
				}
				var title = $this.attr("title");
				var i18nTitle = $this.attr("i18nTitle");
				if (i18nTitle) {
					title = $.Cmdbuild.translations.getTranslation(i18nTitle, title);
				}
				forms.push({
					form: this,
					title: title,
					id: $this.attr("id"),
					include: $this.attr("include"),
					classes: $this.attr("class"),
					invisible: $this.attr("invisible"),
					params: param
				});
			});
			return forms;
		};
		this.prepareParams = function(forms) {
			try {
				for (var i = 0; i < forms.length; i++) {
					if (forms[i].params) {
						$.Cmdbuild.dataModel.prepareCallerParameters(forms[i].id, forms[i].params);
					}
				}
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.tabbed.prepareParams");
				throw e;
			}
		};		
		this.insertTitles = function(forms) {
			try {
				var htmlStr = "";
				htmlStr += "<ul>";
				for (var i = 0; i < forms.length; i++) {
					var myClass = "";
					if (forms[i].invisible === "true") {
						myClass = " class='invisible' ";
					}
					htmlStr += "<li " + myClass + "><a href='#" + forms[i].id + "'>" + forms[i].title + "</a></li>";
				}
				htmlStr += "</ul>";
				return htmlStr;
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.tabbed.insertTitles");
				throw e;
			}
		};		
		this.includeForms = function() {
			if (this.forms.length <= 0) {
				this.showCB();
				return;
			}
			var form = this.forms[0];
			this.forms.splice(0, 1);
			$.Cmdbuild.utilities.include(form.form, this.includeForms, {}, this);// include if is definite an attribute include
		};		
		this.insertForms = function(forms) {
			try {
				var htmlStr = "";
				for (var i = 0; i < forms.length; i++) {
					htmlStr += "<div id='" + forms[i].id + "' class='" + forms[i].classes +"'>";
					htmlStr += $.Cmdbuild.elementsManager.insertChildren(forms[i].form);
					htmlStr += "</div>";
				}
				return htmlStr;
			}
			catch (e) {
				$.Cmdbuild.errorsManager.log("$.Cmdbuild.standard.tabbed.insertForms");
				throw e;
			}
		};	
	};
	$.Cmdbuild.standard.tabbed = tabbed;
	// Statics
	$.Cmdbuild.standard.tabbed.fireChangeTab = function(id) {
		var xmlElement = $.Cmdbuild.elementsManager.getElement(id);
		$.Cmdbuild.dataModel.pushParametersOnStack(id);
		var param = $.Cmdbuild.elementsManager.getEvent("onClick", xmlElement);
		$.Cmdbuild.eventsManager.onEvent(param);
	};
}) (jQuery);
