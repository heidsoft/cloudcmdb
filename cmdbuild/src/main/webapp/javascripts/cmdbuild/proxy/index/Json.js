(function () {

	Ext.define('CMDBuild.proxy.index.Json', {

		singleton: true,

		attachment: {
			create: 'services/json/attachments/uploadattachment',
			read: '',
			update: 'services/json/attachments/modifyattachment',
			remove: 'services/json/attachments/deleteattachment',

			readAll: 'services/json/attachments/getattachmentlist',

			download: 'services/json/attachments/downloadattachment',
			getContext: 'services/json/attachments/getattachmentscontext'
		},

		attribute: {
			create: '', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modclass/getattributelist', // TODO: waiting for refactor (crud + rename)
			update: 'services/json/schema/modclass/saveattribute', // TODO: waiting for refactor (crud + rename)
			remove: 'services/json/schema/modclass/deleteattribute',

			readAll: 'services/json/schema/modclass/getattributelist', // TODO: waiting for refactor (crud + rename)
			readTypes: 'services/json/schema/modclass/getattributetypes', // TODO: waiting for refactor (rename name, description)
			readRenceableDomains: 'services/json/schema/modclass/getreferenceabledomainlist',

			sorting: {
				reorder: 'services/json/schema/modclass/reorderattribute',
				update: 'services/json/schema/modclass/saveordercriteria'
			}
		},

		bim: {
			create: 'services/json/bim/create',
			read: 'services/json/bim/read',
			update: 'services/json/bim/update',
			remove: '',

			activeForClassName: 'services/json/bim/getactiveforclassname',
			disable: 'services/json/bim/disableproject',
			enable: 'services/json/bim/enableproject',
			fetchCardFromViewewId: 'services/json/bim/fetchcardfromviewewid',
			fetchJsonForBimViewer: 'services/json/bim/fetchjsonforbimviewer',
			roidForCardId: 'services/json/bim/getroidforcardid',

			ifc: {
				download: 'services/json/bim/download',
				imports: 'services/json/bim/importifc'
			},

			layer: {
				create: '', // TODO: waiting for refactor (crud)
				read: '',
				update: 'services/json/bim/savebimlayer', // TODO: waiting for refactor (crud)
				remove: '',

				readAll: 'services/json/bim/readbimlayer',

				rootName: 'services/json/bim/rootclassname'
			}
		},

		card: {
			create: '', // TODO: waiting for refactor (crud)
			read: 'services/json/management/modcard/getcard',
			update: 'services/json/management/modcard/updatecard', // TODO: waiting for refactor (crud)
			remove: 'services/json/management/modcard/deletecard',

			readAll: 'services/json/management/modcard/getcardlist',
			readAllDetails: 'services/json/management/modcard/getdetaillist',
			readAllShort: 'services/json/management/modcard/getcardlistshort',

			bulkUpdate: 'services/json/management/modcard/bulkupdate',
			bulkUpdateFromFilter: 'services/json/management/modcard/bulkupdatefromfilter',
			getPosition: 'services/json/management/modcard/getcardposition',
			getSqlCardList: 'services/json/management/modcard/getsqlcardlist',
			lock: 'services/json/lock/lockcard',
			unlock: 'services/json/lock/unlockcard',
			unlockAll: 'services/json/lock/unlockall'
		},

		classes: {
			create: 'services/json/schema/modclass/savetable', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modclass/getallclasses', // TODO: waiting for refactor (crud)
			update: 'services/json/schema/modclass/savetable', // TODO: waiting for refactor (crud)
			remove: 'services/json/schema/modclass/deletetable',

			readAll: 'services/json/schema/modclass/getallclasses',

			foreignKeyTargetClass: 'services/json/schema/modclass/getfktargetingclass'
		},

		configuration: {
			create: '',
			read: 'services/json/schema/setup/getconfiguration',
			update: 'services/json/schema/setup/saveconfiguration', // TODO: waiting for refactor (rename)
			remove: '',

			readAll: 'services/json/schema/setup/getconfigurations',

			apply: 'services/json/configure/apply',
			connectionTest: 'services/json/configure/testconnection',

			dms: {
				readAllPreset: 'services/json/attachments/getpresets'
			},

			ui: {
				read: 'services/json/schema/modsecurity/getuiconfiguration'
			}
		},

		csv: {
			readAll: 'services/json/management/importcsv/getcsvrecords',

			exports: {
				create: 'services/json/management/exportcsv/writecsv',
				read: '',
				update: '',
				remove: '',

				download: 'services/json/management/exportcsv/export'
			},

			imports: {
				create: 'services/json/management/importcsv/uploadcsv',
				read: 'services/json/management/importcsv/readcsv',
				update: 'services/json/management/importcsv/storecsvrecords', // TODO: waiting for refactor (rename)
				remove: '',

				clearSession: 'services/json/management/importcsv/clearsession',
				updateRecords: 'services/json/management/importcsv/updatecsvrecords'
			}
		},

		customPage: {
			readForCurrentUser: 'services/json/custompages/readforcurrentuser'
		},

		dashboard: {
			create: 'services/json/dashboard/add',
			read: '',
			update: 'services/json/dashboard/modifybaseproperties',
			remove: 'services/json/dashboard/remove',

			readAll: 'services/json/dashboard/fulllist', // @administration
			readAllVisible: 'services/json/dashboard/list', // @management - manage permissions

			chart: {
				create: 'services/json/dashboard/addchart',
				read: 'services/json/dashboard/getchartdata',
				update: 'services/json/dashboard/modifychart',
				remove: 'services/json/dashboard/removechart',

				readForPreview: 'services/json/dashboard/getchartdataforpreview'
			},

			columns: {
				create: '',
				read: '',
				update: 'services/json/dashboard/modifycolumns',
				remove: ''
			}
		},

		dataView: {
			readAll: 'services/json/viewmanagement/read', // TODO: waiting for refactor (rename on server)

			filter: {
				create: 'services/json/viewmanagement/createfilterview',
				read: 'services/json/viewmanagement/readfilterview', // TODO: waiting for refactor (crud)
				update: 'services/json/viewmanagement/updatefilterview',
				remove: 'services/json/viewmanagement/deletefilterview',

				readAll: 'services/json/viewmanagement/readfilterview' // TODO: waiting for refactor (crud)
			},

			sql: {
				create: 'services/json/viewmanagement/createsqlview',
				read: 'services/json/viewmanagement/readsqlview', // TODO: waiting for refactor (crud)
				update: 'services/json/viewmanagement/updatesqlview',
				remove: 'services/json/viewmanagement/deletesqlview',

				readAll: 'services/json/viewmanagement/readsqlview' // TODO: waiting for refactor (crud)
			}
		},

		domain: {
			create: 'services/json/schema/modclass/savedomain', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modclass/getalldomains', // TODO: waiting for refactor (crud)
			update: 'services/json/schema/modclass/savedomain', // TODO: waiting for refactor (crud)
			remove: 'services/json/schema/modclass/deletedomain',

			readAll: 'services/json/schema/modclass/getalldomains',
			readAllByClass: 'services/json/schema/modclass/getdomainlist'
		},

		email: {
			create: 'services/json/email/email/create',
			read: 'services/json/email/email/read',
			update: 'services/json/email/email/update',
			remove: 'services/json/email/email/delete',

			readAll: 'services/json/email/email/readall',

			enabled: 'services/json/email/email/enabled',

			account:{
				create: 'services/json/schema/emailaccount/post',
				read: 'services/json/schema/emailaccount/get',
				update: 'services/json/schema/emailaccount/put',
				remove: 'services/json/schema/emailaccount/delete',

				readAll: 'services/json/schema/emailaccount/getall',

				setDefault: 'services/json/schema/emailaccount/setdefault'
			},

			attachment: {
				create: 'services/json/email/attachment/upload',
				read: '',
				update: '',
				remove: 'services/json/email/attachment/delete',

				readAll: 'services/json/email/attachment/readall',

				copy: 'services/json/email/attachment/copy',
				download: 'services/json/email/attachment/download'
			},

			queue: {
				read: 'services/json/email/queue/configuration',
				save: 'services/json/email/queue/configure',

				isRunning: 'services/json/email/queue/running',
				start: 'services/json/email/queue/start',
				stop: 'services/json/email/queue/stop'
			},

			template:{
				create: 'services/json/email/template/create',
				read: 'services/json/email/template/read',
				update: 'services/json/email/template/update',
				remove: 'services/json/email/template/delete',

				readAll: 'services/json/email/template/readall'
			}
		},

		filter: {
			defaults: {
				create: '',
				read: 'services/json/filter/getdefault',
				update: 'services/json/filter/setdefault',
				remove: ''
			},

			group: {
				create: 'services/json/filter/create',
				read: 'services/json/filter/readallgroupfilters', // TODO: waiting for refactor (CRUD)
				update: 'services/json/filter/update',
				remove: 'services/json/filter/delete',

				readAll: 'services/json/filter/readallgroupfilters', // TODO: waiting for refactor (CRUD)

				defaults: {
					create: '',
					read: 'services/json/filter/getgroups',
					update: 'services/json/filter/setdefault',
					remove: ''
				}
			},

			user: {
				create: '',
				read: 'services/json/filter/read',
				update: '',
				remove: '',

				readAll: 'services/json/filter/readforuser'
			}
		},

		functions: {
			readAll: 'services/json/schema/modclass/getfunctions',

			readCards: 'services/json/management/modcard/getsqlcardlist'
		},

		gis: {
			expandDomainTree: 'services/json/gis/expanddomaintree',
			getFeatures: 'services/json/gis/getfeature',
			getGeoCardList: 'services/json/gis/getgeocardlist',

			geoAttribute: {
				create: 'services/json/gis/addgeoattribute',
				read: '',
				update: 'services/json/gis/modifygeoattribute',
				remove: 'services/json/gis/deletegeoattribute'
			},

			geoServer: {
				layer: {
					create: 'services/json/gis/addgeoserverlayer',
					read: '',
					update: 'services/json/gis/modifygeoserverlayer',
					remove: 'services/json/gis/deletegeoserverlayer',

					readAll: 'services/json/gis/getgeoserverlayers'
				}
			},

			icons: {
				create: 'services/json/icon/upload',
				read: '',
				update: 'services/json/icon/update',
				remove: 'services/json/icon/remove',

				readAll: 'services/json/icon/list'
			},

			layer: {
				create: '',
				read: '',
				update: '',
				remove: '',

				readAll: 'services/json/gis/getalllayers',

				setOrder: 'services/json/gis/setlayersorder',
				setVisibility: 'services/json/gis/setlayervisibility'
			},

			treeNavigation: {
				create: '', // TODO: waiting for refactor (CRUD)
				read: 'services/json/gis/getgistreenavigation',
				update: 'services/json/gis/savegistreenavigation', // TODO: waiting for refactor (CRUD)
				remove: 'services/json/gis/removegistreenavigation'
			}
		},

		group: {
			create: 'services/json/schema/modsecurity/savegroup', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modsecurity/getgrouplist', // TODO: waiting for refactor (crud)
			update: 'services/json/schema/modsecurity/savegroup', // TODO: waiting for refactor (crud)
			remove: '',

			readAll: 'services/json/schema/modsecurity/getgrouplist',

			enableDisableGroup: 'services/json/schema/modsecurity/enabledisablegroup',

			user: {
				readAll: 'services/json/schema/modsecurity/getgroupuserlist',
				save: 'services/json/schema/modsecurity/savegroupuserlist'
			},

			userInterface: {
				read: 'services/json/schema/modsecurity/getgroupuiconfiguration',
				save: 'services/json/schema/modsecurity/savegroupuiconfiguration'
			}
		},

		history: {
			classes: {
				card: {
					create: '',
					read: 'services/json/management/modcard/getcardhistory',
					update: '',
					remove: '',

					readRelations: 'services/json/management/modcard/getrelationshistory',

					readHistoric: 'services/json/management/modcard/gethistoriccard',
					readHistoricRelation: 'services/json/management/modcard/gethistoricrelation'
				}
			},

			workflow: { // TODO: waiting for refactor (different endpoints)
				activity: {
					create: '',
					read: 'services/json/management/modcard/getprocesshistory',
					update: '',
					remove: '',

					readRelations: 'services/json/management/modcard/getrelationshistory',

					readHistoric: 'services/json/management/modcard/gethistoriccard',
					readHistoricRelation: 'services/json/management/modcard/gethistoricrelation'
				}
			}
		},

		localization: {
			translation: {
				create: '',
				read: 'services/json/schema/translation/read',
				update: 'services/json/schema/translation/update',
				remove: '',

				readAll: 'services/json/schema/translation/readall'
			},

			utility: {
				csv: {
					exports: 'services/json/schema/translation/exportcsv',
					imports: 'services/json/schema/translation/importcsv'
				}
			}
		},

		lookup: {
			create: 'services/json/schema/modlookup/savelookup', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modlookup/getlookuplist', // TODO: waiting for refactor (crud)
			update: 'services/json/schema/modlookup/savelookup', // TODO: waiting for refactor (crud)
			remove: '',

			readAll: 'services/json/schema/modlookup/getlookuplist',
			readAllParents: 'services/json/schema/modlookup/getparentlist',

			disable: 'services/json/schema/modlookup/disablelookup',
			enable: 'services/json/schema/modlookup/enablelookup',
			setOrder: 'services/json/schema/modlookup/reorderlookup',

			type: {
				create: 'services/json/schema/modlookup/savelookuptype', // TODO: waiting for refactor (crud)
				read: 'services/json/schema/modlookup/tree', // TODO: waiting for refactor (crud)
				update: 'services/json/schema/modlookup/savelookuptype', // TODO: waiting for refactor (crud)
				remove: '',

				readAll: 'services/json/schema/modlookup/tree' // TODO: waiting for refactor (crud)
			}
		},

		menu: {
			create: '', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modmenu/getassignedmenu',
			update: 'services/json/schema/modmenu/savemenu', // TODO: waiting for refactor (crud)
			remove: 'services/json/schema/modmenu/deletemenu',

			readAvailableItems: 'services/json/schema/modmenu/getavailablemenuitems',
			readConfiguration: 'services/json/schema/modmenu/getmenuconfiguration'
		},

		navigationTree: {
			create: 'services/json/navigationtree/create',
			read: 'services/json/navigationtree/read',
			update: 'services/json/navigationtree/save', // TODO: waiting for refactor (rename update)
			remove: 'services/json/navigationtree/remove',

			readAll: 'services/json/navigationtree/get' // TODO: waiting for refactor (rename readAll)
		},

		patchManager: {
			update: 'services/json/configure/applypatches',

			readAll: 'services/json/configure/getpatches'
		},

		privilege: {
			classes: {
				update: 'services/json/schema/modsecurity/saveclassprivilege',

				readAll: 'services/json/schema/modsecurity/getclassprivilegelist',

				setRowAndColumnPrivileges: 'services/json/schema/modsecurity/setrowandcolumnprivileges',

				uiConfiguration: {
					read: 'services/json/schema/modsecurity/loadclassuiconfiguration',
					update: 'services/json/schema/modsecurity/saveclassuiconfiguration'
				}
			},

			customPage: {
				update: 'services/json/schema/modsecurity/savecustompageprivilege',

				readAll: 'services/json/schema/modsecurity/getcustompageprivilegelist'
			},

			dataView: {
				update: 'services/json/schema/modsecurity/saveviewprivilege',

				readAll: 'services/json/schema/modsecurity/getviewprivilegelist'
			},

			filter: {
				update: 'services/json/schema/modsecurity/savefilterprivilege',

				readAll: 'services/json/schema/modsecurity/getfilterprivilegelist'
			},

			workflow: {
				update: 'services/json/schema/modsecurity/saveprocessprivilege',

				readAll: 'services/json/schema/modsecurity/getprocessprivilegelist',

				setRowAndColumnPrivileges: 'services/json/schema/modsecurity/setrowandcolumnprivileges'
			}
		},

		relation: {
			create: 'services/json/management/modcard/createrelations',
			read: '',
			update: 'services/json/management/modcard/modifyrelation',
			remove: 'services/json/management/modcard/deleterelation',

			readAll: 'services/json/management/modcard/getrelationlist',

			readAlreadyRelatedCards: 'services/json/management/modcard/getalreadyrelatedcards',
			removeDetail: 'services/json/management/modcard/deletedetail'
		},

		report: {
			readByType: 'services/json/management/modreport/getreportsbytype',
			readTypesTree: 'services/json/management/modreport/getreporttypestree',
			menuTree: 'services/json/schema/modreport/menutree',

			factory: {
				create: 'services/json/management/modreport/createreportfactory',
				createByTypeCode: 'services/json/management/modreport/createreportfactorybytypecode',
				print: 'services/json/management/modreport/printreportfactory',
				updateParams: 'services/json/management/modreport/updatereportfactoryparams'
			},

			jasper: {
				create: 'services/json/management/modreport/createreportfactory',
				read: '',
				update: '',
				remove: 'services/json/schema/modreport/deletereport',

				save: 'services/json/schema/modreport/savejasperreport',

				analyze: 'services/json/schema/modreport/analyzejasperreport',
				imports: 'services/json/schema/modreport/importjasperreport',
				resetSession: 'services/json/schema/modreport/resetsession'
			},

			print: {
				cardDetails: 'services/json/management/modreport/printcarddetails',
				classSchema: 'services/json/schema/modreport/printclassschema',
				currentView: 'services/json/management/modreport/printcurrentview',
				schema: 'services/json/schema/modreport/printschema',
				sqlView: 'services/json/management/modreport/printsqlview'
			}
		},

		session: {
			create: 'services/json/session/create', // @unauthorized
			update: 'services/json/session/update', // @unauthorized
			remove: 'services/json/session/delete'
		},

		taskManager: {
			readAll: 'services/json/schema/taskmanager/readall',

			cyclicExecution: 'services/json/schema/taskmanager/start',
			singleExecution: 'services/json/schema/taskmanager/execute',
			stop: 'services/json/schema/taskmanager/stop',

			connector: {
				create: 'services/json/schema/taskmanager/connector/create',
				read: 'services/json/schema/taskmanager/connector/read',
				update: 'services/json/schema/taskmanager/connector/update',
				remove: 'services/json/schema/taskmanager/connector/delete',

				readAll: 'services/json/schema/taskmanager/connector/readall',

				readSqlSources: 'services/json/schema/taskmanager/connector/availablesqlsources'
			},

			email: {
				create: 'services/json/schema/taskmanager/reademail/create',
				read: 'services/json/schema/taskmanager/reademail/read',
				update: 'services/json/schema/taskmanager/reademail/update',
				remove: 'services/json/schema/taskmanager/reademail/delete',

				readAll: 'services/json/schema/taskmanager/reademail/readall'
			},

			event: {
				readAll: 'services/json/schema/taskmanager/event/readall',

				asynchronous: {
					create: 'services/json/schema/taskmanager/event/asynchronous/create',
					read: 'services/json/schema/taskmanager/event/asynchronous/read',
					update: 'services/json/schema/taskmanager/event/asynchronous/update',
					remove: 'services/json/schema/taskmanager/event/asynchronous/delete',

					readAll: 'services/json/schema/taskmanager/event/asynchronous/readall'
				},

				synchronous: {
					create: 'services/json/schema/taskmanager/event/synchronous/create',
					read: 'services/json/schema/taskmanager/event/synchronous/read',
					update: 'services/json/schema/taskmanager/event/synchronous/update',
					remove: 'services/json/schema/taskmanager/event/synchronous/delete',

					readAll: 'services/json/schema/taskmanager/event/synchronous/readall'
				}
			},

			generic: {
				create: 'services/json/schema/taskmanager/generic/create',
				read: 'services/json/schema/taskmanager/generic/read',
				update: 'services/json/schema/taskmanager/generic/update',
				remove: 'services/json/schema/taskmanager/generic/delete',

				readAll: 'services/json/schema/taskmanager/generic/readall'
			},

			workflow: {
				create: 'services/json/schema/taskmanager/startworkflow/create',
				read: 'services/json/schema/taskmanager/startworkflow/read',
				update: 'services/json/schema/taskmanager/startworkflow/update',
				remove: 'services/json/schema/taskmanager/startworkflow/delete',

				readAll: 'services/json/schema/taskmanager/startworkflow/readall',
				readAllByWorkflow: 'services/json/schema/taskmanager/startworkflow/readallbyworkflow'
			}
		},

		user: {
			create: 'services/json/schema/modsecurity/saveuser', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modsecurity/getgrouplist', // TODO: waiting for refactor (crud)
			update: 'services/json/schema/modsecurity/saveuser', // TODO: waiting for refactor (crud)
			remove: '',

			readAll: 'services/json/schema/modsecurity/getuserlist',
			readAllGroups: 'services/json/schema/modsecurity/getusergrouplist',

			disable: 'services/json/schema/modsecurity/disableuser'
		},

		utility: {
			changePassword: 'services/json/schema/modsecurity/changepassword'
		},

		utils: {
			clearCache: 'services/json/utils/clearcache',
			generateId: 'services/json/utils/generateid',
			readAllAvailableTranslations: 'services/json/utils/listavailabletranslations', // @unauthorized
			readDefaultLanguage: 'services/json/utils/getdefaultlanguage' // @unauthorized
		},

		widget: {
			create: 'services/json/widget/create',
			read: 'services/json/widget/read',
			update: 'services/json/widget/update',
			remove: 'services/json/widget/delete',

			readAll: 'services/json/widget/readall',
			readAllForClass: 'services/json/widget/readallforclass',

			// Widgets end-points
			ping: 'services/json/widget/callwidget',
			webService: 'services/json/widget/callwidget'
		},

		workflow: {
			create: 'services/json/schema/modclass/savetable', // TODO: waiting for refactor (crud)
			read: 'services/json/schema/modclass/getallclasses', // TODO: waiting for refactor (crud)
			update: 'services/json/schema/modclass/savetable', // TODO: waiting for refactor (crud)
			remove: 'services/json/schema/modclass/deletetable', // TODO: waiting for refactor (crud)

			readAll: 'services/json/schema/modclass/getallclasses', // TODO: waiting for refactor (crud)

			isProcessUpdated: 'services/json/workflow/isprocessupdated',
			synchronize: 'services/json/workflow/sync',

			activity: {
				create: '', // TODO: waiting for refactor (crud)
				read: 'services/json/workflow/getactivityinstance', // TODO: waiting for refactor (crud)
				update: 'services/json/workflow/saveactivity', // TODO: waiting for refactor (crud)
				remove: '',

				readAll: 'services/json/workflow/getprocessinstancelist',
				readStart: 'services/json/workflow/getstartactivity',

				abort: 'services/json/workflow/abortprocess',
				lock: 'services/json/lock/lockactivity',
				unlock: 'services/json/lock/unlockactivity'
			},

			xpdl: {
				download: 'services/json/workflow/downloadxpdl',
				downloadTemplate: 'services/json/workflow/downloadxpdltemplate',
				upload: 'services/json/workflow/uploadxpdl',
				versions: 'services/json/workflow/xpdlversions'
			}
		}
	});

})();
