(function($) {
	var Mail = function(param, onReadyFunction, onReadyScope) {
		/**
		 * Attributes
		 */
		this.param = param;
		this.type = param.className;
		this.searchCard = function(cardId) {
			var mail = this.innerSearchCard(cardId);
			if (this.param.mailStatus == "reply") {
				return this.composeReplyMail(mail);
			}
			else if (this.param.mailStatus == "new") {
				return this.composeNewMail(mail);;
			}
			else {
				return mail;
			}
		};
		this.composeReplyMail = function(mail) {
			return {
				_id: "newmail_" + $.Cmdbuild.utilities.uniqueId(),
				account: "default",
				to: mail.from,
				from: mail.to,
				body: mail.from + "wrote: </br>" + mail.body,
				subject: "R: " + mail.subject
			};
		};
		this.composeNewMail = function(mail) {
			return {
				_id: "newmail_" + $.Cmdbuild.utilities.uniqueId(),
				account: "default",
				to: "",
				from: "default",
				body: "-----------",
				subject: "........."
			};
		};
		this.innerSearchCard = function(cardId) {
			var obj = $.Cmdbuild.dataModel.forms[this.param.formData];
			var mails = obj.getBackendData().mails;
			for (var i = 0; i < mails.length; i++) {
				var mail = mails[i];
				if (mail._id === cardId) {
					var attributes = [];
					switch (mail.status) {
						case $.Cmdbuild.global.MAILSTATUSDRAFT:
							if (mail.fromTemplate) {
								attributes = ["keepSynchronization", "delay", "account", "from", "to", "bcc", "subject", "body"];
							}
							else {
								attributes = ["delay", "account", "from", "to", "bcc", "subject", "body"];
							}
							
							break;
						default :
							attributes = ["account", "from", "to", "bcc", "subject", "body"];
							break;
					}
					this.loadAttributes(attributes, this.possibleAttributes);
					return mail;
				}
			}
			return undefined;
		};
		this.updateData = function(param, callback, callbackScope) {
			var data = {};
			data["_id"] = param.data._id;
			for (var i = 0; i < this.attributes.length; i++) {
				data[this.attributes[i].name] = param.data[this.attributes[i].name];
			}
			if (this.param.mailStatus == "reply" || this.param.mailStatus == "new") {
				var obj = $.Cmdbuild.dataModel.forms[this.param.formData];
				data.status = $.Cmdbuild.global.MAILSTATUSDRAFT;

				obj.getBackendData().mails.push(data);
			}
			if (callback) {
				callback.apply(callbackScope, []);
			}
		};
		this.possibleAttributes = [
			{
				type: "boolean",
				name: "keepSynchronization",
   				description: "keepSynchronization"
			},
			{
				type: "select",
				name: "delay",
   				description: "Delay",
   				backend: "DelayTable"
			},
			{
				type: "string",
				name: "account",
   				description: "Account",
			},
			{
				type: "string",
				name: "from",
   				description: "From"
			},
			{
				type: "string",
				name: "to",
   				description: "To"
			},
			{
				type: "string",
				name: "bcc",
   				description: "Bcc"
			},
			{
				type: "string",
				name: "subject",
   				description: "Subject"
			},
			{
				type: "text",
				name: "body",
   				description: "Body"
			}
		];
		this.replyTable = {
			account: "to",
			body: "body",
			subject: "subject"
		}

		/**
		 * Private attributes
		 */
		var onReadyFunction = onReadyFunction;
		var onReadyScope = onReadyScope;

		/**
		 * Base functions
		 */
		this.init = function() {
			this.loadData();
		};
		this.loadData = function() {
			this.data = this.searchCard(this.param.cardId);
			onObjectReady();
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
		this.loadAttributes = function(attributes, possibleAttributes) {
			this.attributes = [];
			for (var i = 0; i < attributes.length; i++) {
				for (var j = 0; j < possibleAttributes.length; j++) {
					if (attributes[i] == possibleAttributes[j].name) {
						this.attributes.push(possibleAttributes[j]);
						break;
					}
				}
			}
		};
		var onObjectReady = function() {
			onReadyFunction.apply(onReadyScope);
		};

		/**
		 * Call init function and return object
		 */
		this.init();
	};
	$.Cmdbuild.standard.backend.Mail = Mail;
}) (jQuery);
