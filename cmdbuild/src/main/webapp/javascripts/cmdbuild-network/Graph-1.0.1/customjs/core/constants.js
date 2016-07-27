(function($) {
	$.Cmdbuild.g3d.constants = {
		GUICOMPOUNDNODE : "GUICOMPOUNDNODE",
		GUICOMPOUNDNODEDESCRIPTION : "Compound node",
		ICON_DELETE : "cross.png",
		DEFAULT_SHAPE : "cmdbuildDefaultShape",
		SELECTION_SHAPE : "cmdbuildSelectionShape",
		TEMPLATES_PATH : "theme/templates/",
		SPRITES_PATH : "theme/images/sprites/",
		ICONS_PATH : "theme/images/icons/",
		LABELS_ON_SELECTED : "selected",
		NO_LABELS : "none",
		RANGE_VIEWPOINTDISTANCE : 100,
		EXPANDING_THRESHOLD : 30,
		MIN_MOVEMENT : 5,
		TOOLTIP_WINDOW : "viewerInformation",
		REMOVE_NAVIGATON_TREE : "removeNavigatonTree",
		REMOVE_NAVIGATON_TREE_STRING : "Disable navigation tree",
		PRINT_TEMPLATE : "print.html",
		CMDBUILD_NETWORK_IMAGE : "CMDBUILD_NETWORK_IMAGE",
		PRINT_NETWORK_IMAGE : "PRINT_NETWORK_IMAGE",
		COMPOUND_ATTRIBUTES : [ {
			type : "string",
			_id : "type",
			description : "Type",
			displayableInList : true
		}, {
			type : "string",
			_id : "description",
			description : "Description",
			displayableInList : true
		} ],
		FILTER_OPERATORS : {
			EQUAL : "equal",
			NOT_EQUAL : "notequal",
			NULL : "isnull",
			NOT_NULL : "isnotnull",
			GREATER_THAN : "greater",
			LESS_THAN : "less",
			BETWEEN : "between",
			LIKE : "like",
			CONTAIN : "contain",
			NOT_CONTAIN : "notcontain",
			BEGIN : "begin",
			NOT_BEGIN : "notbegin",
			END : "end",
			NOT_END : "notend",

			NET_CONTAINS : "net_contains",
			NET_CONTAINED : "net_contained",
			NET_CONTAINSOREQUAL : "net_containsorequal",
			NET_CONTAINEDOREQUAL : "net_containedorequal",
			NET_RELATION : "net_relation"
		},
		OBJECT_STATUS_MOVED : "moved",
		OBJECT_STATUS_NEW : "new",

	};
})(jQuery);