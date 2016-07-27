/*
 * COMPILER
 */
// private 
// static
function cqlEvaluate(obj, callback, callbackScope) {
	// obj have the follow structure:
	// filter: cql to evaluate,
	// meta: meta,
	// form: form

	var output = function(msg) {
		console.log("cql.manager: " + msg.msg);
	};

	// load CQL manager
	var configurator = new Configurator(output);
	var cqlManager = new CqlManager(configurator);
	var meta = {};
	for ( var key in obj.meta) {
		cqlManager.lexer.analize(obj.meta[key]);
		meta[key] = new treeObject(configurator, cqlManager.lexer.tree, null,
				obj.form);
	}
	cqlManager.lexer.analize(obj.filter);
	var treeObj = new treeObject(configurator, cqlManager.lexer.tree, meta,
			obj.form);
	treeObj.resolve(function(response) {
		callback.apply(callbackScope, [response]);
	}, this);
}
// end static
function CqlManager(configurator) {
	// called by form interested in cql
	this.configurator = configurator;
	this.lexer = new lexer(this.configurator);
	this.commandsTable = new commandsTable(this.configurator);
	this.metaTable = new commandsTable(this.configurator);
	this.variablesTable = new variablesTable(this.configurator);
	this.compile = function(form, attributes) {
		for (var i = 0; i < attributes.length; i++) {
			var attribute = attributes[i];
			if ((attribute.filter && attribute.filter.text)) {
				this.compileAttribute(form, attribute);
			}
		}
		this.variablesTable.generate(form, this.commandsTable);
	};
	this.compileAttribute = function(form, attribute) {
		if (!(attribute.filter && attribute.filter.text)) {
			return;
		}
		this.lexer.analize(attribute.filter.text);
		this.commandsTable.prepareEntry(form, attribute.name);
		var attributeTree = this.lexer.tree.slice();
		for ( var key in attribute.filter.params) {
			this.lexer.analize(attribute.filter.params[key]);
			var nameSpaces = key.split(".");
			var nameMeta = nameSpaces[nameSpaces.length - 1];
			this.commandsTable.pushMeta(form, attribute.name, nameMeta,
					this.lexer.tree);
		}
		this.commandsTable.push(form, attribute.name, attributeTree);
	};
	this.onFilterChanged = function(e) {
		alert(e.form + " " + e.field);
	};
	this.fieldChanged = function(form, field) {
		// if(this.cqlEnabled) {
		this.variablesTable.fieldChanged(form, field);
		// }
	};
	this.resolve = function(form, field, callback, callbackScope) {
		// have to return 2 different values in the case a filter is empty and
		// in the case a filter is not defined (a variable is empty for example)
		this.commandsTable.resolve(form, field, function(response) {
			callback.apply(callbackScope, [response]);
		}, this);
	};
	this.isUndefined = function(filter) {
		return (filter.indexOf($.Cmdbuild.global.CQLUNDEFINEDVALUE) != -1);
	};
};
/*
 * LEXER
 */
function lexer(configurator) {
	/*
	 * the lexer subdivide the command in tokens taking in consideration only
	 * the indentation of brackets. it does not make any assumption on the cql's
	 * sintax. With the entry:
	 * uno{mg:due{mg:tre}}quattro{mg:cinque{mg:sei{mg:sette}{mg:otto}}}nove it
	 * gives the tree: uno ---- mg:due -------- mg:tre quattro ---- mg:cinque
	 * -------- mg:sei ------------ mg:sette ------------ mg:otto nove
	 */
	this.configurator = configurator;
	this.CQLSINTAXERRORBADFORMATTEDCOMMAND = "Cql sintax error bad formatted command";
	this.tree = [];
	this.stack = [];
	this.stackPointer = 0;
	this.originalCommand = "";
	this.analize = function(str) {
		this.stackPointer = 0;
		this.tree = [];
		this.stack = [this.tree];
		this.originalCommand = str;
		this.stackStrings = [];
		this._analize(" " + str, false);// the space exclude a begin with a {
		// at the end of recursion I can evaluate if the string is well formed
		if (this.stackPointer != 0) {
			console.log(this.CQLSINTAXERRORBADFORMATTEDCOMMAND + " "
					+ this.originalCommand);
			var error = new Error({
				message: this.CQLSINTAXERRORBADFORMATTEDCOMMAND + " "
						+ this.originalCommand
			});
			throw error;
		}
	};
	this._analize = function(str, canBeManager) {
		if (str == "") {
			return;
		}
		this.stackStrings.push(str);
		var indexOpen = str.indexOf("{");
		var indexClose = str.indexOf("}");
		if (this.stackPointer < 0) {
			console.log(this.CQLSINTAXERRORBADFORMATTEDCOMMAND + " " + str
					+ " " + this.originalCommand, this.stackStrings);
			var error = new Error({
				message: this.CQLSINTAXERRORBADFORMATTEDCOMMAND + " "
						+ this.originalCommand
			});
			throw error;
		}
		if (indexClose == -1 && indexOpen == -1) {// there are no brackets
			var token = str;//.trim();
			this.pushToken(token, canBeManager);
		} else if (indexClose < indexOpen || indexOpen == -1) {// token
			var token = (str.substr(0, indexClose));//.trim();
			this.pushToken(token, canBeManager);
			// this because the sequence }{ goes directly in the case indexOpen
			// == 0
			this.stackPointer += (str.substr(indexClose + 1, 1) === "{")
					? 0
					: -1;
			this._analize(str.substr(indexClose + 1), false);
		} else if (indexOpen == 0) {
			this._analize(str.substr(indexOpen + 1), true);// can be a manager
		} else {
			var token = (str.substr(0, indexOpen));//.trim();
			var node = this.stack[this.stackPointer];
			this.pushToken(token, canBeManager);
			node.push([]);
			this.stackPointer += 1;
			this.stack[this.stackPointer] = node[node.length - 1];
			this._analize(str.substr(indexOpen), false);
		}

	};
	this.pushToken = function(token, canBeManager) {
		var node = this.stack[this.stackPointer];
		if (token != "") {
			var tokenParts = token.split(":");
			if (tokenParts.length == 2 && canBeManager) {
				node.push({
					type: "object",
					manager: tokenParts[0],
					value: tokenParts[1]
				});
			} else {
				node.push({
					type: "text",
					value: token
				});
			}
		}
	};
};
/*
 * TABLES
 */
function variablesTable(configurator) {
	// this table generate the events for observing values
	this.configurator = configurator;
	this.table = {};
	this.generate = function(form, commandsTable) {
		this.table[form] = [];
		var commands = commandsTable.getForm(form);
		if (!commands) {
			return;
		}
		for ( var field in commands) {
			var fieldTree = commandsTable.getTree(form, field);
			var fieldMeta = commandsTable.getMeta(form, field);
			if (!fieldTree) {
				return;
			}
			this.generateVariables(form, field, fieldTree.getTree(), fieldMeta);
		}
	};
	this.generateVariables = function(form, field, tree, meta) {
		for (var i = 0; i < tree.length; i++) {
			if (tree[i].type == undefined) { // is an array
				this.generateVariables(form, field, tree[i], meta);
			} else if (tree[i].type == "object") {
				// can be a lookup the the variable is only the first part
				// (ex: nameLookup.id)
				var variables = tree[i].value.split(".");
				this.table[form].push({
					form: form,
					field: field,
					manager: tree[i].manager,
					variable: variables[0]
				});
			} else {
				;// nop (is a constant)
			}
		}
		if (meta) {
			for ( var key in meta) {
				this.generateVariables(form, field, meta[key].getTree(), null);
			}
		}
	};
	this.fieldChanged = function(form, field) {
		var changed = {};
		if (!this.table[form]) {
			console.log("No dependancies for " + form + "." + field);
			return;
		}
		for (var i = 0; i < this.table[form].length; i++) {
			var variable = this.table[form][i];
			if (variable.variable == field && !changed[variable.field]) {
				changed[variable.field] = true;
				if (!this.isVariableManager(variable.manager)) {
					this.fieldChanged(form, variable.field);
				}
			}
		}
		for ( var key in changed) {
			this.configurator.changed(form, key);
		}
	};
	this.debug = function() {
		for ( var key in this.table) {
			for (var i = 0; i < this.table[key].length; i++) {
				var variable = this.table[key][i];
				this.configurator.output({
					type: "debug",
					msg: variable.form + "|" + variable.field + "|"
							+ variable.variable + "|" + variable.manager
				});
			}
		}
	};
	this.isVariableManager = function(manager) {
		return this.configurator.manager[manager].isVariableManager;
	};
};
function commandsTable(configurator) {
	this.configurator = configurator;
	this.table = {};
	this.prepareEntry = function(form, field) {
		if (!this.table[form]) {
			this.table[form] = {};
		}
		this.table[form][field] = {};
	}, this.push = function(form, field, tree) {
		this.table[form][field]["tree"] = new treeObject(configurator, tree,
				this.getMeta(form, field), form);
	};
	this.pushMeta = function(form, field, nameMeta, tree) {
		if (!this.table[form][field]["meta"]) {
			this.table[form][field]["meta"] = {};
		}
		this.table[form][field]["meta"][nameMeta] = new treeObject(
				configurator, tree, this.getMeta(form, field), form);
	};
	this.getForm = function(form) {
		if (!this.table[form]) {
			return undefined;
		}
		return this.table[form];
	};
	this.getTree = function(form, field) {
		if (this.table[form] && this.table[form][field]) {
			return this.table[form][field]["tree"];
		} else {
			return null;
		}
	};
	this.getMeta = function(form, field) {
		if (this.table[form] && this.table[form][field]) {
			return this.table[form][field]["meta"];
		} else {
			return null;
		}
	};
	this.resolve = function(form, field, callback, callbackScope) {
		if (this.getTree(form, field)) {
			this.getTree(form, field).resolve(function(response) {
				callback.apply(callbackScope, [response]);
			}, this);
		} else {
			callback.apply(callbackScope, [""]);
		}
	};
	this.debug = function() {
		for ( var form in this.table) {
			for ( var field in this.table[form]) {
				var str = form + "|" + field;
				str += "|" + this.getTree(form, field).write();
				this.configurator.output({
					type: "debug",
					msg: str
				});
				for ( var meta in this.getMeta(form, field)) {
					var str = form + " " + field + "-->meta-->" + meta;
					str += "|" + this.getMeta(form, field)[meta].write();
					this.configurator.output({
						type: "debug",
						msg: str
					});
				}
			}
		}
	};
	this.isVariableManager = function(manager) {
		return this.configurator.manager[manager].isVariableManager;
	};
};
function treeObject(configurator, tree, meta, form) {
	this.configurator = configurator;
	this.tree = tree;
	this.meta = meta;// for meta
	this.form = form;
	this.getTree = function() {
		return this.tree;
	};
	this.write = function() {
		return this.writeTree(this.tree);
	};
	this.writeTree = function(tree) {
		var str = "";
		for (var i = 0; i < tree.length; i++) {
			if (tree[i].type == undefined) { // is an array
				str += "+" + this.writeTree(tree[i]);
			} else if (tree[i].type == "object") {
				str += "+" + "[" + tree[i].manager + "]" + tree[i].value;
			} else {
				str += "+" + tree[i].value;
			}
		}
		return str;
	};
	this.resolve = function(callback, callbackScope) {
		if (this.tree == null || this.tree.length <= 0) {
			callback.apply(callbackScope, [""]);
			return;
		}
		try {
			var temporaryTree = this.tree.slice();
			this.resolveTree(temporaryTree, "", function(response) {
				callback.apply(callbackScope, [response]);
			}, this);
		} catch (e) {
			callback.apply(callbackScope, [undefined]);
		}
	};
	this.resolveTree = function(tree, returnValue, callback, callbackScope) {
		if (tree.length <= 0) {
			callback.apply(callbackScope, [returnValue]);
			return;
		}
		var node = tree[0];
		tree.splice(0, 1);
		if (node.type == undefined) { // is an array
			var temporaryTree = node.slice();
			this.resolveTree(temporaryTree, "", function(response) {
				this.resolveTree(tree, returnValue + response, callback,
						callbackScope);
			}, this);
		} else if (node.type == "object") {
			var strValue = "";
			var arValues = node.value.split(".");
			if (this.isInMyMeta(arValues[0])) {// tree[i].value)) {
				this.resolveMeta(arValues[0], function(response) {
					if (response == undefined) {
						response = $.Cmdbuild.global.CQLUNDEFINEDVALUE;
						this.resolveTree(tree, returnValue + response,
								callback, callbackScope);
					}
					this.evaluate(node.manager, this.form, response, function(
							evaluated) {
						var keys = node.value.split(".");
						if (evaluated === undefined) {
							evaluated = $.Cmdbuild.global.CQLUNDEFINEDVALUE;
						}
						if (keys.length > 1 && evaluated.length > 0) {
							evaluated = evaluated[0][keys[1]];
						}
						this.resolveTree(tree, returnValue + evaluated,
								callback, callbackScope);
					}, this);
				}, this);
			} else {
				strValue = node.value;
				this.evaluate(node.manager, this.form, strValue, function(
						evaluated) {
					if (evaluated === undefined) {
						evaluated = $.Cmdbuild.global.CQLUNDEFINEDVALUE;
					}
					this.resolveTree(tree, returnValue + evaluated, callback,
							callbackScope);
				}, this);
			}
		} else {
			this.resolveTree(tree, returnValue + node.value, callback,
					callbackScope);
		}
	};
	this.isInMyMeta = function(key) {
		if (!this.meta) {
			return false;
		}
		return (this.meta && this.meta[key]);
	};
	this.resolveMeta = function(key, callback, callbackScope) {
		// var meta = this.commandTable.getMeta(this.form, this.field);
		this.meta[key].resolve(function(response) {
			callback.apply(callbackScope, [response]);
		}, this);
	};
	this.evaluate = function(manager, form, value, callback, callbackScope) {
		if (this.configurator.manager[manager]) {
			this.configurator.manager[manager].getValue(form, value, function(
					response) {
				callback.apply(callbackScope, [response]);
			}, this);
		} else {
			output(HOOKMANAGERNOTDEFINED + ": " + manager);
		}
	};
}
/*
 * CONFIGURATOR
 */
function configurator(output) {
	var HOOKMANAGERNOTDEFINED = "Non e' stato definito un manager Cql";
	this.debug = false;
	this.manager = {
		xa: {
			getValue: function(form, name) {
				output(HOOKMANAGERNOTDEFINED + ": " + "xa");
			}
		},
		client: {
			getValue: function(form, name) {
				output(HOOKMANAGERNOTDEFINED + ": " + "client");
			}
		},
		server: {
			getValue: function(form, name) {
				output(HOOKMANAGERNOTDEFINED + ": " + "server");
			}
		}
	};
	this.changed = function(fields) {

	};
	this.output = function(msg) {
		output(msg);
	};
};

