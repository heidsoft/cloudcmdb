(function($) {
	
	var SELECTED_CLASS = "selectedNavItem";
	var BASE_UL_CLASS = "cmdbuild_navigation";

	var navigation = function() {

		// configuration
		this.id = null;
		this.config = {};
		this.backend = undefined;
		this.tree = {};

		/*
		 * @param {Object} config - command configuration 
		 * @param {string} config.container - container id 
		 * @param {string} config.form - form id
		 * @param {string} config.backend - backend to use to get data
		 * @param {string} config.className - type for backend
		 * @param {string} config.labelField - field used as label; default _description
		 * @param {string} config.idField - field used as id; default _id
		 * @param {string} config.parentField - field used as parent
		 * @param {string} config.collapsible - show only first level, close others
		 */
		this.init = function(config) {
			this.config = config;
			this.id = config.form;

			// set defaults
			if (!config.labelField) {
				config.labelField = "_description";
			}
			if (!config.idField) {
				config.idField = "_id";
			}

			// get backend
			var backendFn = $.Cmdbuild.utilities.getBackend(config.backend);
			this.backend = new backendFn(config, this.onInitComplete, this);
		};
		// init callback
		this.onInitComplete = function() {
			var config = {};
			var me = this;
			this.backend.loadData(config, function() {
				me.show();
			}, this);
		};

		// show navigation
		this.show = function() {
			// push form into data model
			var data = this.backend.getData();
			$.Cmdbuild.dataModel.push({
				form : this.id,
				type : "navigation",
				currentIndex : -1,
				data : data
			});

			// get navigation tree
			try {
				this.organizeTree();
			} catch (e) {
				$.Cmdbuild.errorsManager.popup(e);
			}

			var $container = $("#" + this.config.container);
			var nav = this.generateUL("root", this.config.form, BASE_UL_CLASS);
			$container.append(nav);

			if (this.config.collapsible) {
				MenuTree.walk(this.config.form);
				$container.addClass("collapsibleNavigation");
			}

			// select first element
			selectNavElement(this.config.form, 0);
			$container.children("ul").children("li:first-child").children("a:first-child").addClass(SELECTED_CLASS);
		};

		// create navigation levels
		this.organizeTree = function() {
			this.tree = {};
			var me = this;
			var parentField = this.config.parentField;
			if (!parentField) {
				var error = new Error("Missing parentField parameter.");
				error.name = "ConfigurationError";
				throw error;
			}
			$.each(this.backend.getData(), function(index, item) {
				// get parent; if undefined set root as parent
				var parent = item[parentField];
				if (!parent && parent === null) {
					parent = "root";
				}

				// if tree has not parent key, creates it
				if (Object.keys(me.tree).indexOf(parent.toString()) === -1) {
					me.tree[parent] = [];
				}
				me.tree[parent].push(item);
			});
		};

		// create ul tag
		this.generateUL = function(root, cssid, cssclass) {
			var me = this;
			
			var htmlAttributeId = "rel-itemid";
			var labelFiled = this.config.labelField;
			var idField = this.config.idField;

			var $ul = $("<ul></ul>");
			if (cssid) {
				$ul.attr("id", cssid);
			}
			if (cssclass) {
				$ul.addClass(cssclass);
			}

			// children
			var children = this.tree[root];
			if (children) {
				$.each(children,
					function(index, child) {
						var childId = child[idField];
						// generate li tag
						var $li = $("<li></li>");
						// generate a tag
						var $a = $("<a></a>").attr("href", "#").attr(htmlAttributeId, childId)
							.text(child[labelFiled]).click(
								function() {
									var $this = $(this);
									// modify css class
									$("#" + me.config.container
											+ " ." + SELECTED_CLASS)
											.removeClass(SELECTED_CLASS);
									$this.addClass(SELECTED_CLASS);

									// save in modelData selected item
									var index = findWithAttr(me.backend.getData(), idField, $this.attr(htmlAttributeId));
									selectNavElement(me.config.form, index);
									return false;
								});

						// append elements to li
						$li.append($a);
						if (Object.keys(me.tree)
								.indexOf(childId.toString()) !== -1) {
							$li.append(me.generateUL(childId.toString()));
						}

						// append elements to ul
						$ul.append($li);
					});
			}
			return $ul;
		};
	};
	

	var findWithAttr = function(array, attr, value) {
		for (var i = 0; i < array.length; i += 1) {
			if (array[i][attr] == value) {
				return i;
			}
		}
	};

	var selectNavElement = function(form, index) {
		$.Cmdbuild.dataModel.setCurrentIndex(form, index);
	};

	var MenuTree = {
		collapse : function(element) {
			element.slideToggle(500);
		},
		walk : function(menu) {
			var me = this;
			$('a', '#' + menu).each(function() {
				var $a = $(this);
				var $li = $a.parent();
				var $span;
				if ($a.next().is('ul')) {
					var $ul = $a.next();
					$span = $("<span></span>").addClass("navCommandIcon");
					$span.click(function(e) {
						e.preventDefault();
						me.collapse($ul);
						$span.toggleClass('active');
					});
				} else {
					$span = $("<span></span>").addClass("navBullet").html("&bull;");
				}
				$a.before($span);
			});
		}
	};

	$.Cmdbuild.standard.navigation = navigation;
})(jQuery);