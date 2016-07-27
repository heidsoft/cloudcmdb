package org.cmdbuild.service.rest.v1.constants;

public class Serialization {

	public static final String NAMESPACE = "http://cmdbuild.org/services/rest/v1/";

	public static final String //
			ID = "id", //
			ID_CAPITALIZED = "Id", //
			PROCESS_CAPITAL = "Process";

	public static final String //
			ACTIVE = "active", //
			ADVANCE = "advance", //
			ATTRIBUTE = "attribute", //
			ATTRIBUTES = "attributes", //
			ATTACHMENT = "attachment", //
			ATTACHMENT_ID = ATTACHMENT + ID_CAPITALIZED, //
			ATTACHMENT_METADATA = ATTACHMENT + "Metadata", //
			AUTHOR = "author", //
			AVAILABLE_ROLES = "availableRoles", //
			CARD = "card", //
			CARD_ID = CARD + ID_CAPITALIZED, //
			CARDINALITY = "cardinality", //
			CATEGORY = "category", //
			CATEGORY_ID = CATEGORY + ID_CAPITALIZED, //
			CHILDREN = "children", //
			CLASS = "class", //
			CLASS_ID = CLASS + ID_CAPITALIZED, //
			CODE = "code", //
			CODE_CAPITALIZED = "Code", //
			CREATED = "created", //
			DATA = "data", //
			DEFAULT = "default", //
			DEFAULT_STATUS = DEFAULT + "Status", //
			DEFAULT_VALUE = DEFAULT + "Value", //
			DESCRIPTION = "description", //
			DESCRIPTION_CAPITALIZED = "Description", //
			DESCRIPTION_ATTRIBUTE_NAME = DESCRIPTION + "_attribute_name", //
			DESCRIPTION_DIRECT = DESCRIPTION + "Direct", //
			DESCRIPTION_INVERSE = DESCRIPTION + "Inverse", //
			DESCRIPTION_MASTER_DETAIL = DESCRIPTION + "MasterDetail", //
			DESTINATION = "destination", //
			DESTINATION_PROCESS = DESTINATION + PROCESS_CAPITAL, //
			DETAILED = "detailed", //
			DISPLAYABLE_IN_LIST = "displayableInList", //
			DOMAIN = "domain", //
			DOMAIN_ID = DOMAIN + ID_CAPITALIZED, //
			DOMAIN_NAME = "domainName", //
			DOMAIN_SOURCE = DOMAIN + "Source", //
			EDITOR_TYPE = "editorType", //
			EXTRA = "extra", //
			FILE = "file", //
			FILTER = "filter", //
			GROUP = "group", //
			HIDDEN = "hidden", //
			INDEX = "index", //
			INHERITED = "inherited", //
			INSTRUCTIONS = "instructions", //
			LENGTH = "length", //
			LIMIT = "limit", //
			MANDATORY = "mandatory", //
			MENU = "menu", //
			MENU_TYPE = MENU + "Type", //
			METADATA = "metadata", //
			MODE = "mode", //
			MODIFIED = "modified", //
			NAME = "name", //
			NUMBER = "number", //
			PARAMS = "params", //
			PROTOTYPE = "prototype", //
			OBJECT_DESCRIPTION = "objectDescription", //
			OBJECT_ID = "objectId", //
			OBJECT_TYPE = "objectType", //
			PARENT_ID = "parent_id", //
			PARENT = "parent", //
			PARENT_TYPE = "parent_type", //
			PASSWORD = "password", //
			POSITION_OF = "positionOf", //
			POSITIONS = "positions", //
			PRECISION = "precision", //
			RELATION = "relation", //
			RELATION_ID = RELATION + ID_CAPITALIZED, //
			RESPONSE_METADATA = "meta", //
			ROLE = "role", //
			ROLE_ID = "role" + ID_CAPITALIZED, //
			SCALE = "scale", //
			SESSION = "session", //
			SORT = "sort", //
			SOURCE = "source", //
			SOURCE_PROCESS = SOURCE + PROCESS_CAPITAL, //
			START = "start", //
			STATUS = "status", //
			STATUSES = "statuses", //
			TARGET_CLASS = "targetClass", //
			TEXT = "text", //
			TOTAL = "total", //
			TYPE = "type", //
			TYPE_CAPITALIZED = "Type", //
			UNIQUE = "unique", //
			USERNAME = "username", //
			VALUE = "value", //
			VALUES = "values", //
			VERSION = "version", //
			WIDGETS = "widgets", //
			WRITABLE = "writable";

	public static final String //
			LOOKUP = "lookup", //
			LOOKUP_TYPE = LOOKUP + "Type", //
			LOOKUP_TYPE_ID = LOOKUP_TYPE + ID_CAPITALIZED, //
			LOOKUP_VALUE = LOOKUP + "Value", //
			LOOKUP_VALUE_ID = LOOKUP_VALUE + ID_CAPITALIZED;

	public static final String //
			PROCESS = "process", //
			PROCESS_ACTIVITY = PROCESS + "Activity", //
			PROCESS_ACTIVITY_ID = PROCESS_ACTIVITY + ID_CAPITALIZED, //
			PROCESS_ID = PROCESS + ID_CAPITALIZED, //
			PROCESS_INSTANCE = PROCESS + "Instance", //
			PROCESS_INSTANCE_ID = PROCESS_INSTANCE + ID_CAPITALIZED;

	public static final String //
			TYPE_BOOLEAN = "boolean", //
			TYPE_CHAR = "char", //
			TYPE_DATE = "date", //
			TYPE_DATE_TIME = "dateTime", //
			TYPE_DECIMAL = "decimal", //
			TYPE_DOUBLE = "double", //
			TYPE_ENTRY_TYPE = "entryType", //
			TYPE_FOREIGN_KEY = "foreignKey", //
			TYPE_INTEGER = "integer", //
			TYPE_IP_ADDRESS = "ipAddress", //
			TYPE_LIST = "list", //
			TYPE_LOOKUP = "lookup", //
			TYPE_REFERENCE = "reference", //
			TYPE_STRING_ARRAY = "stringArray", //
			TYPE_STRING = "string", //
			TYPE_TEXT = "text", //
			TYPE_TIME = "time";

	public static final String //
			UNDERSCORED_ACTIVITY = "_activity", //
			UNDERSCORED_ADVANCE = "_" + ADVANCE, //
			UNDERSCORED_ATTACHMENT = "_" + ATTACHMENT, //
			UNDERSCORED_AUTHOR = "_" + AUTHOR, //
			UNDERSCORED_CATEGORY = "_" + CATEGORY, //
			UNDERSCORED_CREATED = "_" + CREATED, //
			UNDERSCORED_DESCRIPTION = "_" + DESCRIPTION, //
			UNDERSCORED_DESTINATION = "_" + DESTINATION, //
			UNDERSCORED_DESTINATION_DESCRIPTION = UNDERSCORED_DESTINATION + DESCRIPTION_CAPITALIZED, //
			UNDERSCORED_DESTINATION_ID = UNDERSCORED_DESTINATION + ID_CAPITALIZED, //
			UNDERSCORED_DESTINATION_TYPE = UNDERSCORED_DESTINATION + TYPE_CAPITALIZED, //
			UNDERSCORED_ID = "_" + ID, //
			UNDERSCORED_MODIFIED = "_" + MODIFIED, //
			UNDERSCORED_NAME = "_" + NAME, //
			UNDERSCORED_SOURCE = "_" + SOURCE, //
			UNDERSCORED_SOURCE_DESCRIPTION = UNDERSCORED_SOURCE + DESCRIPTION_CAPITALIZED, //
			UNDERSCORED_SOURCE_ID = UNDERSCORED_SOURCE + ID_CAPITALIZED, //
			UNDERSCORED_SOURCE_TYPE = UNDERSCORED_SOURCE + TYPE_CAPITALIZED, //
			UNDERSCORED_STATUS = "_" + STATUS, //
			UNDERSCORED_TYPE = "_" + TYPE, //
			UNDERSCORED_VERSION = "_" + VERSION;

	private Serialization() {
		// prevents instantiation
	}

}
