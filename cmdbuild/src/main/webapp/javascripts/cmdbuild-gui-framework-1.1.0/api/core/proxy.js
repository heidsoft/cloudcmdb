(function($) {
	var methods = {
		GET : "GET",
		POST : "POST",
		PUT : "PUT",
		DELETE : "DELETE"
	};
	
	var proxy = {

		/**
		 * Utilities
		 */
		prepareParamsForList : function(configData) {
			// pagination
			var params = {
				start : configData.start ? configData.start : 0,
			};
			if (configData.limit) {
				params.limit = configData.limit;
			}
			// sorting
			var sort = [];
			if (configData.sort) {
				sort.push({
						property : configData.sort,
						direction : configData.direction
					});
				params.sort = JSON.stringify(sort);
			}
			// filter
			if (configData.filter) {
				params.filter = JSON.stringify(configData.filter);
			}
			else {
				params.filter = "{query : ''}";
			}
			// active 
			if (configData.active) {
				params.active = configData.active;
			}
			//positionOf
			if (configData.positionOf) {
				params.positionOf = configData.positionOf;
			}

			return params;
		},

		/**
		 * Get URIs
		 */
		getURIForFileStoreItemDownload : function(filestore, folder, file) {
			return $.Cmdbuild.global.getApiUrl() + 'filestores/' + filestore
					+ '/folders/' + folder + '/files/' + file
					+ "/download?CMDBuild-Authorization="
					+ $.Cmdbuild.authentication.getAuthenticationToken();
		},

		/**
		 * Getters for resources
		 */
		getAttachmentsCategories: function(callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'configuration/attachments/categories';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},
		getClassPrivileges: function(roleId, type, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'roles/' + roleId + '/classes_privileges/' + type;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},
		getClassAttributes: function(type, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'classes/' + type + '/attributes';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				var attributes = [];
				$.each(response, function(index, attribute) {
					if (attribute.active) {
						attributes.push(attribute);
					}
				});
				callback.apply(callbackScope, [attributes]);
			});
		},
		getFilteredCards: function(config, callback, callbackScope) {
			// params
			var params = this.prepareParamsForList(config);
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'classes';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		getCardList: function(type, config, callback, callbackScope) {
			// params
			var params = this.prepareParamsForList(config);
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'classes/' + type + '/cards';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		getRelations: function(domainId, config, callback, callbackScope) {
			// params
			var params = this.prepareParamsForList(config);

			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'domains/' + domainId + "/relations/";
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		getRelation: function(domainId, relationId, config, callback, callbackScope) {
			// params
			var params = this.prepareParamsForList(config);

			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'domains/' + domainId + "/relations/" + relationId;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		getDomains: function(config, callback, callbackScope) {
			// params
			var params = this.prepareParamsForList(config);

			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'domains/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		getDomain: function(id, callback, callbackScope, params) {
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'domains/' + id;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		getDomainAttributes: function(id, callback, callbackScope, params) {
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'domains/' + id + "/attributes";
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		getClass: function(type, callback, callbackScope) {
			// params
			var callbackObj = {
				success: function(data, metadata){
					callback.apply(callbackScope, [data, metadata]);
				},
				fail: function(response){
					callback.apply(callbackScope, [[], []]);
				}
			};
			var url = $.Cmdbuild.global.getApiUrl() + 'classes/' + type;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, callbackObj, {});
		},
		getClasses: function(callback, callbackScope) {
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'classes/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, {});
		},
		deleteCard: function(type, cardId, callback, callbackScope) {
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'classes/' + type + "/cards/" + cardId;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.DELETE, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, {});
		},
		getCardData: function(type, cardId, config, callback, callbackScope) {
			// params
			var params = this.prepareParamsForList(config);
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'classes/' + type + "/cards/" + cardId;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		getCardAttachments: function(type, cardId, config, callback, callbackScope) {
			// params
			var params = this.prepareParamsForList(config);

			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'classes/' + type + "/cards/" + cardId + "/attachments/";
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		getLookupValue: function(lookupTypeId, lookupValueId, config, callback, callbackScope) {
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'lookup_types/' + lookupTypeId + '/values/' + lookupValueId;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, {});
		},
		getLookupType: function(lookupTypeId, config, callback, callbackScope) {
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'lookup_types/' + lookupTypeId;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, {});
		},
		getLookupData: function(lookupTypeId, config, callback, callbackScope) {
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'lookup_types/' + lookupTypeId + '/values/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, config);
		},
		getStepProcessAttributes: function(type, processInstanceId, processActivityId, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/instances/' + processInstanceId + '/activities/' + processActivityId + '/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},
		putProcessMail: function(type, processInstanceId, mailId, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
			'/instances/' + processInstanceId + '/emails/' + mailId;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.PUT, function(response){
				callback.apply(callbackScope, [response]);
			}, JSON.stringify(data));
		},
		deleteProcessMail: function(type, processInstanceId, mailId, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
			'/instances/' + processInstanceId + '/emails/' + mailId;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.DELETE, function(response){
				callback.apply(callbackScope, [response]);
			}, {});
		},
		postProcessMail: function(type, processInstanceId, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
			'/instances/' + processInstanceId + '/emails/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.POST, function(response){
				callback.apply(callbackScope, [response]);
			}, JSON.stringify(data));
		},
		getProcessMails: function(type, processInstanceId, callback, callbackScope, params) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/instances/' + processInstanceId + '/emails/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		getProcessSingleMail: function(type, processInstanceId, mailId, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/instances/' + processInstanceId + '/emails/' + mailId + '/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},
		getProcess: function(type, callback, callbackScope) {
			var callbackObj = {
				success: function(response){
					callback.apply(callbackScope, [response]);
				},
				fail: function(response){
					callback.apply(callbackScope, [[]]);
				}
			};
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, callbackObj);
		},
		getProcessAttributes: function(type, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + '/attributes';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				var attributes = [];
				$.each(response, function(index, attribute) {
					if (attribute.active) {
						attributes.push(attribute);
					}
				});
				callback.apply(callbackScope, [attributes]);
			});
		},
//		getProcess: function(type, callback, callbackScope) {
//			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + '/';
//			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
//				callback.apply(callbackScope, [response]);
//			});
//		},
		getProcesses: function(callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},
		getProcessStatuses: function(callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'configuration/processes/statuses/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},
		getStartProcessAttributes: function(type, processActivityId, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
			'/start_activities/' + processActivityId + "/";
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},
		getStartProcessActivities: function(type, processInstanceId, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/start_activities/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},
		getCardProcess: function(type, processInstanceId, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/instances/' + processInstanceId;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			});
		},
		getActivity: function(type, processInstanceId, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/instances/' + processInstanceId + '/activities/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},
		getActivityList: function(type, config, callback, callbackScope) {
			// params
			var params = this.prepareParamsForList(config);
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + '/instances';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		},
		putSession: function(token, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'sessions/' + token;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.PUT, function(){
				
			}, JSON.stringify(data));
		},
		getSession: function(token, callback, callbackScope, paramsList) {
			// get url and make request
			if (!paramsList) {
				paramsList = [];
			}
			var url = $.Cmdbuild.global.getApiUrl() + 'sessions/' + token;
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				paramsList.push([data, metadata]);
				callback.apply(callbackScope, paramsList);
			}, {});
		},

		postNewCard: function(type, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'classes/' + type + 
				'/cards/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.POST, function(response){
				callback.apply(callbackScope, [response]);
			}, JSON.stringify(data));
		},
		getSingleProcessInstance: function(type, processInstanceId, config, callbackData, callback, callbackScope) {
			// params
			var params = this.prepareParamsForList(config);
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/instances/' + processInstanceId + '/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, callBackData, [response]);
			}, params);
		},
		abortProcess: function(type, processInstanceId, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/instances/' + processInstanceId + '/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.DELETE, function(response){
				callback.apply(callbackScope, [response]);
			}, JSON.stringify(data));
		},
		putStepProcess: function(type, processInstanceId, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/instances/' + processInstanceId + '/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.PUT, function(response){
				callback.apply(callbackScope, [response]);
			}, JSON.stringify(data));
		},
		postNewProcess: function(type, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/instances/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.POST, function(response) {
				callback.apply(callbackScope, [response]);
			}, JSON.stringify(data));
		},
		postDomain: function(type, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'domains/' + type + 
				'/relations/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.POST, function(response){
				callback.apply(callbackScope, [response]);
			}, JSON.stringify(data));
		},
		putCard : function(type, objId, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'classes/' + type
					+ '/cards/' + objId + '/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.PUT, function(response) {
				callback.apply(callbackScope, [response]);
			}, JSON.stringify(data));
		},
		putImpersonate: function(sessionId, username, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'sessions/'+ sessionId + 
			'/impersonate/' + username + '/';
			var callbackObj = {
					success: function(response){
						callback.apply(callbackScope, [true]);
					},
					fail: function(response){
						callback.apply(callbackScope, [false]);
					}
				};
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.PUT, callbackObj);
		},

		// Reports
		getReports : function(data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'reports/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},
		getReportAttributes: function(type, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'reports/' + type + '/attributes';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				var attributes = [];
				$.each(response, function(index, attribute) {
					if (attribute.active) {
						attributes.push(attribute);
					}
				});
				callback.apply(callbackScope, [attributes]);
			});
		},
		// get process attachment
		getInstanceAttachment : function(type, objId, data, callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'processes/'+ type + 
				'/instances/' + objId + '/attachments/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(response){
				callback.apply(callbackScope, [response]);
			});
		},

		// post process attachment
		postInstanceAttachment: function(type, objId, data, callback, callbackScope) {
			var serverUrl = $.Cmdbuild.global.getApiUrl() + 'processes/' + type + 
				'/instances/' + objId + '/attachments/';
			var callbackObj = {
					success: function(data, metadata){
						callback.apply(callbackScope, [data, metadata]);
					},
					fail: function(response){
						$.Cmdbuild.errorsManager.popup(new Error("The document is just saved"));
						callback.apply(callbackScope, [response]);
					}
				};
			$.Cmdbuild.authProxy.makeAjaxRequest(serverUrl, "POST", callbackObj, data, true);
		},

		// get cql results
		getCqlResults : function(config, callback, callbackScope) {
			var params = this.prepareParamsForList(config);
			var url = $.Cmdbuild.global.getApiUrl() + 'cql/';
			var callbackObj = undefined;
			if (typeof(callback) === "function") {
				callbackObj = {
						success: function(data, metadata){
							callback.apply(callbackScope, [data, metadata]);
						},
						fail: function(response){
							callback.apply(callbackScope, [[], []]);
						}
					};
			} else  {
				callbackObj = callback;					
			}
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, callbackObj, params);
		},

		// get icons
		getIcons : function(config, callback, callbackScope) {
			// params
			var params = this.prepareParamsForList(config);
			// get url and make request
			var url = $.Cmdbuild.global.getApiUrl() + 'icons/';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(data, metadata){
				callback.apply(callbackScope, [data, metadata]);
			}, params);
		}
	};
	$.Cmdbuild.utilities.proxy = proxy;
}) (jQuery);
