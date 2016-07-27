
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;


CREATE FUNCTION _bim_carddata_from_globalid(globalid character varying, OUT "Id" integer, OUT "IdClass" integer, OUT "Description" character varying, OUT "ClassName" character varying) RETURNS record
    LANGUAGE plpgsql
    AS $$
DECLARE
	query varchar;
	table_name varchar;
	tables CURSOR FOR SELECT tablename FROM pg_tables WHERE schemaname = 'bim' ORDER BY tablename;
	
BEGIN
	query='';
	FOR table_record IN tables LOOP
		query= query || '
		SELECT	b."Master" as "Id" , 
			p."Description" AS "Description", 
			p."IdClass"::integer as "IdClass" ,
			p."IdClass" as "ClassName"
		FROM bim."' || table_record.tablename || '" AS b 
			JOIN public."' ||  table_record.tablename || '" AS p 
			ON b."Master"=p."Id" 
		WHERE p."Status"=''A'' AND b."GlobalId" = ''' || globalid || ''' UNION ALL';
	END LOOP;

	SELECT substring(query from 0 for LENGTH(query)-9) INTO query;
	RAISE NOTICE 'execute query : %', query;
	EXECUTE(query) INTO "Id","Description","IdClass","ClassName";
END;
$$;



COMMENT ON FUNCTION _bim_carddata_from_globalid(globalid character varying, OUT "Id" integer, OUT "IdClass" integer, OUT "Description" character varying, OUT "ClassName" character varying) IS 'TYPE: function';



CREATE FUNCTION _bim_create_function_for_export(OUT success boolean) RETURNS boolean
    LANGUAGE plpgsql
    AS $_$
DECLARE
	query text;
BEGIN
	query = 'CREATE OR REPLACE FUNCTION _bim_data_for_export(IN id integer, IN "className" character varying, IN "containerAttributeName" character varying, 
		IN "containerClassName" character varying, OUT "Code" character varying, OUT "Description" character varying, OUT "GlobalId" character varying, 
		OUT container_id integer, OUT container_globalid character varying, OUT x character varying, OUT y character varying, OUT z character varying)
		RETURNS record AS
		\$BODY\$
		DECLARE
			query varchar;
			myrecord record;
			objectposition geometry;
			roomperimeter geometry;
			isinside boolean;
		BEGIN	
			query = 
				''SELECT bimclass."Position" '' || -- 
				''FROM bim."'' || "className" || ''" AS bimclass '' || --
				''WHERE "Master"= '' || id || '';'' ;
			
		RAISE NOTICE ''%'',query;
		EXECUTE(query) INTO objectposition;


		query = 
			''SELECT "'' || "containerAttributeName" || ''" '' || --
			''FROM "'' || "className" || ''" ''--
			''WHERE "Id"='' || id || '';'';

		RAISE NOTICE ''%'',query;
		EXECUTE(query) INTO container_id;

		query = 
			''SELECT "GlobalId"'' || '' '' || --
			''FROM bim."'' || "containerClassName" || ''" ''--
			''WHERE "Master"='' || coalesce(container_id,-1) || '';'';

		RAISE NOTICE ''%'',query;
		EXECUTE(query) INTO container_globalid;
		
		
		query = ''SELECT bimclass."Perimeter" '' || -- 
			''FROM bim."'' || "containerClassName" || ''" AS bimclass '' || --
			''WHERE "Master"= '' || coalesce(container_id,-1) || '';'' ;

		RAISE NOTICE ''%'',query;
		EXECUTE(query) INTO roomperimeter;

		isinside = ST_Within(objectposition,roomperimeter);
		RAISE NOTICE ''ok? %'',isinside;
		IF(NOT isinside) THEN
			query = 
				''UPDATE bim."'' || "className" || ''" ''--
				''SET "Position" = null '' || --
				''WHERE "Master"= '' || id || '';'' ;

			RAISE NOTICE ''%'',query;

			EXECUTE(query);
		END IF;

		query = 
			''SELECT master."Code", master."Description", bimclass."GlobalId", st_x(bimclass."Position"),st_y(bimclass."Position"),st_z(bimclass."Position") '' || --
			''FROM "'' || "className" || ''" AS master LEFT JOIN bim."'' || "className" || ''" AS bimclass ON '' || '' bimclass."Master"=master."Id" '' || --
			''WHERE master."Id" = '' || id || '' AND master."Status"=''''A'''''';

		RAISE NOTICE ''%'',query;

		EXECUTE(query) INTO "Code", "Description", "GlobalId", x, y, z;
		END;
		\$BODY\$
		  LANGUAGE plpgsql VOLATILE
	  	COST 100;

	  	COMMENT ON FUNCTION _bim_data_for_export(integer, character varying, character varying, character varying) IS ''TYPE: function|CATEGORIES: system'';
		';

	EXECUTE query;
	success = true;

	END;
$_$;



COMMENT ON FUNCTION _bim_create_function_for_export(OUT success boolean) IS 'TYPE: function|CATEGORIES: system';



CREATE FUNCTION _bim_set_coordinates(globalid character varying, classname character varying, coords character varying, OUT success boolean) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	query varchar;
BEGIN	

	query = 
	' UPDATE bim."' || classname || '"' ||--
	' SET "Position"= ST_GeomFromText(''' || coords || ''')' || --
	' WHERE "GlobalId"= ''' || globalid || '''';
			
	RAISE NOTICE '%',query;

	EXECUTE(query);

	success = true;
END;
$$;



COMMENT ON FUNCTION _bim_set_coordinates(globalid character varying, classname character varying, coords character varying, OUT success boolean) IS 'TYPE: function|CATEGORIES: system';



CREATE FUNCTION _bim_set_room_geometry(globalid character varying, classname character varying, perimeter character varying, height character varying, OUT success boolean) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	query varchar;
BEGIN	

	query = 
	' UPDATE bim."' || classname || '"' ||--
	' SET "Perimeter"= ST_GeomFromText(''' || perimeter || '''), "Height"=' || height ||--
	' WHERE "GlobalId"= ''' || globalid || '''';
			
	RAISE NOTICE '%',query;

	EXECUTE(query);

	success = true;
END;
$$;



COMMENT ON FUNCTION _bim_set_room_geometry(globalid character varying, classname character varying, perimeter character varying, height character varying, OUT success boolean) IS 'TYPE: function|CATEGORIES: system';



CREATE FUNCTION _bim_store_data(cardid integer, classname character varying, globalid character varying, x character varying, y character varying, z character varying, OUT success boolean) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	query varchar;
	query1 varchar;
	myrecord record;
BEGIN	
	query1 = 'DELETE FROM bim."' || classname || '" where "GlobalId"=''' || globalid || ''';';
	RAISE NOTICE '%',query1;
	EXECUTE(query1);
	
	query = '
		INSERT INTO bim."' || classname || '" ("GlobalId", "Position", "Master")
		VALUES (''' || globalid || ''',' || 'ST_GeomFromText(''POINT(' || x || ' ' || y || ' ' || z || ')''),' || cardid || ');';	
	RAISE NOTICE '%',query;
	EXECUTE(query);
	
	success = true;
END;
$$;



COMMENT ON FUNCTION _bim_store_data(cardid integer, classname character varying, globalid character varying, x character varying, y character varying, z character varying, OUT success boolean) IS 'TYPE: function|CATEGORIES: system';



CREATE FUNCTION _bim_update_coordinates(classname character varying, globalid character varying, x character varying, y character varying, z character varying, OUT success boolean) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	query varchar;
	query1 varchar;
	myrecord record;
BEGIN	
	query = 
		'UPDATE bim."' || classname || '" ' || --
		'SET "Position" = ST_GeomFromText(''POINT(' || x || ' ' || y || ' ' || z || ')'') ' || --
		'WHERE "GlobalId"=''' || globalid || ''';';	
	RAISE NOTICE '%',query;
	EXECUTE(query);
	
	success = true;
END;
$$;



COMMENT ON FUNCTION _bim_update_coordinates(classname character varying, globalid character varying, x character varying, y character varying, z character varying, OUT success boolean) IS 'TYPE: function|CATEGORIES: system';



CREATE FUNCTION _cm_add_class_cascade_delete_on_relations_trigger(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_CascadeDeleteOnRelations"
			AFTER UPDATE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();
	';
END;
$$;



CREATE FUNCTION _cm_add_class_history_trigger(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_CreateHistoryRow"
			AFTER DELETE OR UPDATE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_create_card_history_row()
	';
END;
$$;



CREATE FUNCTION _cm_add_class_sanity_check_trigger(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_SanityCheck"
			BEFORE INSERT OR UPDATE OR DELETE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_sanity_check();
	';
END;
$$;



CREATE FUNCTION _cm_add_domain_history_trigger(domainid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_CreateHistoryRow"
			AFTER DELETE OR UPDATE
			ON '|| DomainId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_create_relation_history_row()
	';
END;
$$;



CREATE FUNCTION _cm_add_domain_sanity_check_trigger(domainid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_SanityCheck"
			BEFORE INSERT OR UPDATE OR DELETE
			ON '|| DomainId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_sanity_check();
	';
END
$$;



CREATE FUNCTION _cm_add_fk_constraints(fksourceid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	FKTargetId oid := _cm_get_fk_target_table_id(FKSourceId, AttributeName);
	SubTableId oid;
BEGIN
	IF FKTargetId IS NULL THEN
		RETURN;
	END IF;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(FKSourceId) LOOP
		PERFORM _cm_add_fk_trigger(SubTableId, FKSourceId, AttributeName, FKTargetId);
	END LOOP;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(FKTargetId) LOOP
		PERFORM _cm_add_restrict_trigger(SubTableId, FKSourceId, AttributeName);
	END LOOP;
END;
$$;



CREATE FUNCTION _cm_add_fk_trigger(tableid oid, fksourceid oid, fkattribute text, fktargetid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	TriggerVariant text;
BEGIN
	IF _cm_is_simpleclass(FKSourceId) THEN
		TriggerVariant := 'simple';
	ELSE
		TriggerVariant := '';
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident(_cm_classfk_name(FKSourceId, FKAttribute)) || '
			BEFORE INSERT OR UPDATE
			ON ' || TableId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_fk('||
				quote_literal(FKAttribute) || ',' ||
				quote_literal(FKTargetId::regclass) || ',' ||
				quote_literal(TriggerVariant) ||
			');
	';
END;
$$;



CREATE FUNCTION _cm_add_reference_handling(tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	objid integer;
	referencedid integer;
	ctrlint integer;

	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
	ReferenceTargetId oid := _cm_read_reference_target_id_comment(AttributeComment);
	AttributeReferenceType text := _cm_read_reference_type_comment(AttributeComment);
	ReferenceDomainId oid := _cm_read_reference_domain_id_comment(AttributeComment);

	RefSourceIdAttribute text := _cm_get_ref_source_id_domain_attribute(TableId, AttributeName);
	RefSourceClassIdAttribute text := _cm_get_ref_source_class_domain_attribute(TableId, AttributeName);
	RefTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(TableId, AttributeName);

	ChildId oid;
BEGIN
	IF ReferenceTargetId IS NULL OR AttributeReferenceType IS NULL OR ReferenceDomainId IS NULL THEN
		RETURN;
	END IF;

	-- Updates the reference for every relation
	-- TODO: UNDERSTAND WHAT IT DOES AND MAKE IT READABLE!
	FOR objid IN EXECUTE 'SELECT "Id" from '||TableId::regclass||' WHERE "Status"=''A'''
	LOOP
		FOR referencedid IN EXECUTE '
			SELECT '|| quote_ident(RefTargetIdAttribute) ||
			' FROM '|| ReferenceDomainId::regclass ||
			' WHERE '|| quote_ident(RefSourceClassIdAttribute) ||'='|| TableId ||
				' AND '|| quote_ident(RefSourceIdAttribute) ||'='|| objid ||
				' AND "Status"=''A'''
		LOOP
			EXECUTE 'SELECT count(*) FROM '||ReferenceTargetId::regclass||' where "Id"='||referencedid INTO ctrlint;
			IF(ctrlint<>0) THEN
				EXECUTE 'UPDATE '|| TableId::regclass ||
					' SET '|| quote_ident(AttributeName) ||'='|| referencedid ||
					' WHERE "Id"='|| objid;
			END IF;
		END LOOP;
	END LOOP;

	-- Trigger on reference class (reference -> relation)
	FOR ChildId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		PERFORM _cm_add_update_relation_trigger(ChildId, TableId, AttributeName);
	END LOOP;

	-- Trigger on domain (relation -> reference)
	PERFORM _cm_add_update_reference_trigger(TableId, AttributeName);
END;
$$;



CREATE FUNCTION _cm_add_restrict_trigger(fktargetclassid oid, fkclassid oid, fkattribute text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF FKClassId IS NULL THEN
		RETURN;
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident('_Constr_'||_cm_cmtable(FKClassId)||'_'||FKAttribute) || '
			BEFORE UPDATE OR DELETE
			ON ' || FKTargetClassId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_restrict(' ||
					quote_literal(FKClassId::regclass) || ',' ||
					quote_literal(FKAttribute) ||
				');
	';
END;
$$;



CREATE FUNCTION _cm_add_simpleclass_sanity_check_trigger(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_SanityCheck"
			BEFORE INSERT OR UPDATE OR DELETE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();
	';
END;
$$;



CREATE FUNCTION _cm_add_spherical_mercator() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	FoundSrid integer;
BEGIN
	SELECT "srid" INTO FoundSrid FROM "spatial_ref_sys" WHERE "srid" = 900913 LIMIT 1;
	IF NOT FOUND THEN
		INSERT INTO "spatial_ref_sys" ("srid","auth_name","auth_srid","srtext","proj4text") VALUES (900913,'spatialreferencing.org',900913,'','+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +units=m +k=1.0 +nadgrids=@null +no_defs');
	END IF;
END;
$$;



CREATE FUNCTION _cm_add_update_reference_trigger(tableid oid, refattribute text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	DomainId oid := _cm_get_reference_domain_id(TableId, RefAttribute);
	DomainSourceIdAttribute text := _cm_get_ref_source_id_domain_attribute(TableId, RefAttribute);
	DomainTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(TableId, RefAttribute);
BEGIN
	IF DomainId IS NULL OR DomainSourceIdAttribute IS NULL OR DomainTargetIdAttribute IS NULL THEN
		RETURN;
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident(_cm_update_reference_trigger_name(TableId, RefAttribute)) || '
			AFTER INSERT OR UPDATE
			ON ' || DomainId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_update_reference(' ||
					quote_literal(RefAttribute) || ',' ||
					quote_literal(TableId::regclass) || ',' ||
					quote_literal(DomainSourceIdAttribute) || ',' ||
					quote_literal(DomainTargetIdAttribute) ||
				');
	';
END;
$$;



CREATE FUNCTION _cm_add_update_relation_trigger(tableid oid, reftableid oid, refattribute text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	DomainId oid := _cm_get_reference_domain_id(RefTableId, RefAttribute);
	DomainSourceIdAttribute text := _cm_get_ref_source_id_domain_attribute(RefTableId, RefAttribute);
	DomainTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(RefTableId, RefAttribute);
BEGIN
	IF DomainId IS NULL OR DomainSourceIdAttribute IS NULL OR DomainTargetIdAttribute IS NULL THEN
		RETURN;
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident(_cm_update_relation_trigger_name(RefTableId, RefAttribute)) || '
			AFTER INSERT OR UPDATE
			ON ' || TableId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_update_relation(' ||
				quote_literal(RefAttribute) || ',' ||
				quote_literal(DomainId::regclass) || ',' ||
				quote_literal(DomainSourceIdAttribute) || ',' ||
				quote_literal(DomainTargetIdAttribute) ||
			');
	';
END;
$$;



CREATE FUNCTION _cm_attribute_default_to_src(tableid oid, attributename text, newdefault text) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
	SQLType text := _cm_get_attribute_sqltype(TableId, AttributeName);
BEGIN
	IF (NewDefault IS NULL OR TRIM(NewDefault) = '') THEN
		RETURN NULL;
	END IF;

    IF SQLType ILIKE 'varchar%' OR SQLType = 'text' OR
    	((SQLType = 'date' OR SQLType = 'timestamp') AND TRIM(NewDefault) <> 'now()')
    THEN
		RETURN quote_literal(NewDefault);
	ELSE
		RETURN NewDefault;
	END IF;
END;
$$;



CREATE FUNCTION _cm_attribute_is_empty(tableid oid, attributename text) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	Out boolean;
BEGIN
	EXECUTE 'SELECT (COUNT(*) = 0) FROM '|| TableId::regclass ||
		' WHERE '|| quote_ident(AttributeName) ||' IS NOT NULL' || 
	    ' AND '|| quote_ident(AttributeName) ||'::text <> '''' LIMIT 1' INTO Out;
	RETURN Out;
END;
$$;



CREATE FUNCTION _cm_attribute_is_inherited(tableid oid, attributename text) RETURNS boolean
    LANGUAGE sql
    AS $_$
	SELECT pg_attribute.attinhcount <> 0
	FROM pg_attribute
	WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$;



CREATE FUNCTION _cm_attribute_is_local(tableid oid, attributename text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT (attinhcount = 0) FROM pg_attribute WHERE attrelid = $1 AND attname = $2 LIMIT 1;
$_$;



CREATE FUNCTION _cm_attribute_is_notnull(tableid oid, attributename text) RETURNS boolean
    LANGUAGE sql
    AS $_$
SELECT pg_attribute.attnotnull OR c.oid IS NOT NULL
FROM pg_attribute
LEFT JOIN pg_constraint AS c
	ON c.conrelid = pg_attribute.attrelid
	AND c.conname::text = _cm_notnull_constraint_name(pg_attribute.attname::text)
WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$;



CREATE FUNCTION _cm_attribute_is_unique(tableid oid, attributename text) RETURNS boolean
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE
	IsUnique boolean;
BEGIN
	SELECT INTO IsUnique (count(*) > 0) FROM pg_class
		JOIN pg_index ON pg_class.oid = pg_index.indexrelid
		WHERE pg_index.indrelid = TableId AND relname = _cm_unique_index_name(TableId, AttributeName);
	RETURN IsUnique;
END;
$$;



CREATE FUNCTION _cm_attribute_list(tableid oid) RETURNS SETOF text
    LANGUAGE sql STABLE
    AS $_$
	SELECT attname::text FROM pg_attribute WHERE attrelid = $1 AND attnum > 0 AND atttypid > 0 ORDER BY attnum;
$_$;



CREATE FUNCTION _cm_attribute_list_cs(classid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT array_to_string(array(
		SELECT quote_ident(name) FROM _cm_attribute_list($1) AS name
	),',');
$_$;



CREATE FUNCTION _cm_attribute_notnull_is_check(tableid oid, attributename text) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	RETURN NOT (
		_cm_is_simpleclass(TableId)
		OR _cm_is_system(TableId)
		OR _cm_check_comment(_cm_comment_for_attribute(TableId, AttributeName), 'MODE', 'reserved')
	);
END
$$;



CREATE FUNCTION _cm_attribute_root_table_id(tableid oid, attributename text) RETURNS oid
    LANGUAGE plpgsql
    AS $$
DECLARE
	CurrentTableId oid := TableId;
BEGIN
	LOOP
	    EXIT WHEN CurrentTableId IS NULL OR _cm_attribute_is_local(CurrentTableId, AttributeName);
		CurrentTableId := _cm_parent_id(CurrentTableId);
	END LOOP;
	RETURN CurrentTableId;
END
$$;



CREATE FUNCTION _cm_attribute_set_notnull(tableid oid, attributename text, willbenotnull boolean) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	IF WillBeNotNull = _cm_attribute_is_notnull(TableId, AttributeName) THEN
		RETURN;
	END IF;

    IF WillBeNotNull AND _cm_is_superclass(TableId) AND _cm_check_comment(AttributeComment, 'MODE', 'write')
    THEN
    	RAISE NOTICE 'Non-system superclass attributes cannot be not null';
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
    END IF;

	PERFORM _cm_attribute_set_notnull_unsafe(TableId, AttributeName, WillBeNotNull);
END;
$$;



CREATE FUNCTION _cm_attribute_set_notnull_unsafe(tableid oid, attributename text, willbenotnull boolean) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    IsCheck boolean := _cm_attribute_notnull_is_check(TableId, AttributeName);
BEGIN
	IF (WillBeNotNull) THEN
		IF (IsCheck) THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||
				' ADD CONSTRAINT ' || quote_ident(_cm_notnull_constraint_name(AttributeName)) ||
				' CHECK ("Status"<>''A'' OR ' || quote_ident(AttributeName) || ' IS NOT NULL)';
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' SET NOT NULL';
		END IF;
	ELSE
		IF (IsCheck) THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP CONSTRAINT '||
				quote_ident(_cm_notnull_constraint_name(AttributeName));
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' DROP NOT NULL';
		END IF;
	END IF;
END;
$$;



CREATE FUNCTION _cm_attribute_set_uniqueness(tableid oid, attributename text, attributeunique boolean) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF _cm_attribute_is_unique(TableId, AttributeName) <> AttributeUnique THEN
		IF AttributeUnique AND (_cm_is_simpleclass(TableId) OR _cm_is_superclass(TableId)) AND NOT _cm_is_system(TableId) THEN
			RAISE NOTICE 'User defined superclass or simple class attributes cannot be unique';
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;

		PERFORM _cm_attribute_set_uniqueness_unsafe(TableId, AttributeName, AttributeUnique);
	END IF;
END;
$$;



CREATE FUNCTION _cm_attribute_set_uniqueness_unsafe(tableid oid, attributename text, attributeunique boolean) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF _cm_is_simpleclass(TableId) THEN
		IF AttributeUnique THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ADD UNIQUE ('|| quote_ident(AttributeName) || ')';
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP UNIQUE ('|| quote_ident(AttributeName) || ')';
		END IF;
	ELSE
		IF AttributeUnique THEN
			EXECUTE 'CREATE UNIQUE INDEX '||
				quote_ident(_cm_unique_index_name(TableId, AttributeName)) ||
				' ON '|| TableId::regclass ||' USING btree (('||
				' CASE WHEN "Status"::text = ''N''::text THEN NULL'||
				' ELSE '|| quote_ident(AttributeName) || ' END))';
		ELSE
			EXECUTE 'DROP INDEX '|| _cm_unique_index_id(TableId, AttributeName)::regclass;
		END IF;
	END IF;
END
$$;



CREATE FUNCTION _cm_cascade(id integer, tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE 'DELETE FROM '|| TableId::regclass ||
		' WHERE '||quote_ident(AttributeName)||' = '||Id::text;
END;
$$;



CREATE FUNCTION _cm_check_attribute_comment_and_type(attributecomment text, sqltype text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	SpecialTypeCount integer := 0; 
BEGIN
	IF _cm_read_reference_domain_comment(AttributeComment) IS NOT NULL THEN
		SpecialTypeCount := SpecialTypeCount +1;
	END IF;

	IF _cm_get_fk_target_comment(AttributeComment) IS NOT NULL THEN
		SpecialTypeCount := SpecialTypeCount +1;
	END IF;

	IF _cm_get_lookup_type_comment(AttributeComment) IS NOT NULL THEN
		SpecialTypeCount := SpecialTypeCount +1;
	END IF;

	IF (SpecialTypeCount > 1) THEN
		RAISE NOTICE 'Too many CMDBuild types specified';
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	IF SpecialTypeCount = 1 AND SQLType NOT IN ('int4','integer') THEN
		RAISE NOTICE 'The SQL type does not match the CMDBuild type';
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
END;
$$;



CREATE FUNCTION _cm_check_comment(classcomment text, key text, value text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT (_cm_read_comment($1, $2) ILIKE $3);
$_$;



CREATE FUNCTION _cm_check_id_exists(id integer, tableid oid, deletedalso boolean) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_value_exists($1, $2, 'Id', $3);
$_$;



CREATE FUNCTION _cm_check_value_exists(id integer, tableid oid, attributename text, deletedalso boolean) RETURNS boolean
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE
	Out BOOLEAN := TRUE;
	StatusPart TEXT;
BEGIN
	IF _cm_is_simpleclass(TableId) OR DeletedAlso THEN
		StatusPart := '';
	ELSE
		StatusPart := ' AND "Status"=''A''';
	END IF;
	IF Id IS NOT NULL THEN
		EXECUTE 'SELECT (COUNT(*) > 0) FROM '|| TableId::regclass ||' WHERE '||
		quote_ident(AttributeName)||'='||Id||StatusPart||' LIMIT 1' INTO Out;
	END IF;
	RETURN Out;
END
$$;



CREATE FUNCTION _cm_class_has_children(tableid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT (COUNT(*) > 0) FROM pg_inherits WHERE inhparent = $1 AND _cm_is_cmobject(inhrelid) LIMIT 1;
$_$;



CREATE FUNCTION _cm_class_has_domains(tableid oid) RETURNS boolean
    LANGUAGE sql
    AS $_$
	SELECT (COUNT(*) > 0) FROM _cm_domain_list() AS d
	WHERE _cm_table_id(_cm_read_comment(_cm_comment_for_cmobject(d), 'CLASS1')) = $1 OR
		_cm_table_id(_cm_read_comment(_cm_comment_for_cmobject(d), 'CLASS2')) = $1;
$_$;



CREATE FUNCTION _cm_class_list() RETURNS SETOF oid
    LANGUAGE sql STABLE
    AS $$
	SELECT oid FROM pg_class WHERE _cm_is_any_class_comment(_cm_comment_for_cmobject(oid));
$$;



CREATE FUNCTION _cm_classfk_name(tableid oid, attributename text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_cmtable($1) || '_' || $2 || '_fkey';
$_$;



CREATE FUNCTION _cm_classfk_name(cmclassname text, attributename text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_cmtable($1) || '_' || $2 || '_fkey';
$_$;



CREATE FUNCTION _cm_classidx_name(tableid oid, attributename text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT 'idx_' || REPLACE(_cm_cmtable_lc($1), '_', '') || '_' || lower($2);
$_$;



CREATE FUNCTION _cm_classpk_name(cmclassname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_cmtable($1) || '_pkey';
$_$;



CREATE FUNCTION _cm_cmschema(tableid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT pg_namespace.nspname::text FROM pg_class
	JOIN pg_namespace ON pg_class.relnamespace = pg_namespace.oid
	WHERE pg_class.oid=$1
$_$;



CREATE FUNCTION _cm_cmschema(cmname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT (_cm_split_cmname($1))[1];
$_$;



CREATE FUNCTION _cm_cmtable(tableid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT pg_class.relname::text FROM pg_class	WHERE pg_class.oid=$1
$_$;



CREATE FUNCTION _cm_cmtable(cmname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT (_cm_split_cmname($1))[2];
$_$;



CREATE FUNCTION _cm_cmtable_lc(tableid oid) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT lower(_cm_cmtable($1));
$_$;



CREATE FUNCTION _cm_cmtable_lc(cmname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT lower(_cm_cmtable($1));
$_$;



CREATE FUNCTION _cm_comment_add_parts(comment text, parts text[], missingonly boolean) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
	part text;
	key text;
	value text;
BEGIN
	RAISE INFO 'adding comment parts % inside "%" (missing only is %)', parts, comment, missingOnly;
	RAISE DEBUG 'comment (before): "%"', comment;
	-- we MUST use coalesce since "array_" functions handle empty array in some funny way
	FOR i IN COALESCE(array_lower(parts, 1),0) .. COALESCE(array_upper(parts, 1),-1) LOOP
		part = parts[i];
		RAISE DEBUG 'comment part: "%"', part;		
		key = split_part(part, ': ', 1);
		value = split_part(part, ': ', 2);
		IF NOT (missingOnly AND substring(comment, key || ': [^|]+') IS NOT NULL) THEN
			comment = _cm_concat('|', ARRAY[comment, key || ': ' || value]);
		ELSE
		END IF;
	END LOOP;
	RAISE DEBUG 'comment (after): "%"', comment;
	RETURN comment;
END
$$;



CREATE FUNCTION _cm_comment_for_attribute(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
SELECT description
FROM pg_description
JOIN pg_attribute ON pg_description.objoid = pg_attribute.attrelid AND pg_description.objsubid = pg_attribute.attnum
WHERE attrelid = $1 and attname = $2 LIMIT 1;
$_$;



CREATE FUNCTION _cm_comment_for_class(cmclass text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_comment_for_table_id(_cm_table_id($1));
$_$;



CREATE FUNCTION _cm_comment_for_cmobject(tableid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT description FROM pg_description
	WHERE objoid = $1 AND objsubid = 0 AND _cm_read_comment(description, 'TYPE') IS NOT NULL LIMIT 1;
$_$;



CREATE FUNCTION _cm_comment_for_domain(cmdomain text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_comment_for_table_id(_cm_domain_id($1));
$_$;



CREATE FUNCTION _cm_comment_for_table_id(tableid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT description FROM pg_description WHERE objoid = $1;
$_$;



CREATE FUNCTION _cm_comment_set_parts(comment text, parts text[]) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
	part text;
	key text;
	value text;
BEGIN
	RAISE INFO 'setting comment parts % inside "%"', parts, comment;
	RAISE DEBUG 'comment (before): "%"', comment;
	-- we MUST use coalesce since "array_" functions handle empty array in some funny way
	FOR i IN COALESCE(array_lower(parts, 1),0) .. COALESCE(array_upper(parts, 1),-1) LOOP
		part = parts[i];
		RAISE DEBUG 'comment part: "%"', part;		
		key = split_part(part, ': ', 1);
		value = split_part(part, ': ', 2);
		comment = regexp_replace(comment, key || ': [^|]+', key || ': ' || value);
	END LOOP;
	RAISE DEBUG 'comment (after): "%"', comment;
	RETURN comment;
END
$$;



CREATE FUNCTION _cm_concat(separator text, elements text[]) RETURNS text
    LANGUAGE sql
    AS $_$
	SELECT case
		WHEN $1 IS NULL OR trim(array_to_string($2, coalesce($1, ''))) = ''
			THEN null
			ELSE array_to_string($2, coalesce($1, ''))
		END
$_$;



CREATE FUNCTION _cm_copy_fk_trigger(fromid oid, toid oid) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT _cm_copy_trigger($1, $2, '%_fkey');
$_$;



CREATE FUNCTION _cm_copy_restrict_trigger(fromid oid, toid oid) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT _cm_copy_trigger($1, $2, '_Constr_%');
$_$;



CREATE FUNCTION _cm_copy_superclass_attribute_comments(tableid oid, parenttableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	AttributeName text;
BEGIN
	FOR AttributeName IN SELECT * FROM _cm_attribute_list(ParentTableId)
	LOOP
		EXECUTE 'COMMENT ON COLUMN '|| TableId::regclass || '.' || quote_ident(AttributeName) ||
			' IS '|| quote_literal(_cm_comment_for_attribute(ParentTableId, AttributeName));
	END LOOP;
END
$$;



CREATE FUNCTION _cm_copy_trigger(fromid oid, toid oid, triggernamematcher text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	TriggerData record;
BEGIN
	FOR TriggerData IN
		SELECT
			t.tgname AS TriggerName,
			t.tgtype AS TriggerType,
			p.proname AS TriggerFunction,
			array_to_string(array(
				SELECT quote_literal(q.param)
					FROM (SELECT regexp_split_to_table(encode(tgargs, 'escape'), E'\\\\000') AS param) AS q
					WHERE q.param <> ''
			),',') AS TriggerParams
		FROM pg_trigger t, pg_proc p
		WHERE tgrelid = FromId AND tgname LIKE TriggerNameMatcher AND t.tgfoid = p.oid
	LOOP
		EXECUTE '
			CREATE TRIGGER '|| quote_ident(TriggerData.TriggerName) ||'
				'|| _cm_trigger_when(TriggerData.TriggerType) ||'
				ON '|| ToId::regclass ||'
				FOR EACH '|| _cm_trigger_row_or_statement(TriggerData.TriggerType) ||'
				EXECUTE PROCEDURE '|| quote_ident(TriggerData.TriggerFunction) ||'('|| TriggerData.TriggerParams ||')
		';
	END LOOP;
END;
$$;



CREATE FUNCTION _cm_copy_update_relation_trigger(fromid oid, toid oid) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT _cm_copy_trigger($1, $2, '_UpdRel_%');
$_$;



CREATE FUNCTION _cm_create_class_default_order_indexes(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	classindex text;
	sqlcommand text;
BEGIN
	SELECT INTO classindex coalesce(_cm_string_agg(attname || ' ' || ordermode), '"Description" asc')
	FROM (
		SELECT quote_ident(attname) AS attname, abs(_cm_read_comment(_cm_comment_for_attribute(tableid, attname), 'CLASSORDER')::integer), CASE WHEN (_cm_get_safe_classorder(tableid, attname) > 0) THEN 'asc' ELSE 'desc' END AS ordermode
		FROM (
			SELECT _cm_attribute_list(tableid) AS attname) AS a
				WHERE _cm_get_safe_classorder(tableid, attname) <> 0
				ORDER by 2
	) AS b;
	RAISE NOTICE '% %', tableid::regclass, classindex;

	sqlcommand = 'DROP INDEX IF EXISTS idx_' || REPLACE(_cm_cmtable_lc(tableid), '_', '') || '_defaultorder;';
	RAISE NOTICE '... %', sqlcommand;
	EXECUTE sqlcommand;

	sqlcommand = 'CREATE INDEX idx_' || REPLACE(_cm_cmtable_lc(tableid), '_', '') || '_defaultorder' || ' ON ' || tableid::regclass || ' USING btree (' || classindex || ', "Id" asc);';
	RAISE NOTICE '... %', sqlcommand;
	EXECUTE sqlcommand;
END;
$$;



CREATE FUNCTION _cm_create_class_default_order_indexes(cmclass character varying, OUT always_true boolean) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
BEGIN
	PERFORM _cm_create_class_default_order_indexes(_cm_table_id(cmclass));
	always_true = TRUE;
END;
$$;



COMMENT ON FUNCTION _cm_create_class_default_order_indexes(cmclass character varying, OUT always_true boolean) IS 'TYPE: function|CATEGORIES: system';



CREATE FUNCTION _cm_create_class_history(cmclassname text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE '
		CREATE TABLE '|| _cm_history_dbname_unsafe(CMClassName) ||'
		(
			"CurrentId" int4 NOT NULL,
			"EndDate" timestamp NOT NULL DEFAULT now(),
			CONSTRAINT ' || quote_ident(_cm_historypk_name(CMClassName)) ||' PRIMARY KEY ("Id"),
			CONSTRAINT '|| quote_ident(_cm_historyfk_name(CMClassName, 'CurrentId')) ||' FOREIGN KEY ("CurrentId")
				REFERENCES '||_cm_table_dbname(CMClassName)||' ("Id") ON UPDATE RESTRICT ON DELETE SET NULL
		) INHERITS ('||_cm_table_dbname(CMClassName)||');
	';
	PERFORM _cm_create_index(_cm_history_id(CMClassName), 'CurrentId');
END;
$$;



CREATE FUNCTION _cm_create_class_indexes(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	PERFORM _cm_create_index(TableId, 'Code');
	PERFORM _cm_create_index(TableId, 'Description');
	PERFORM _cm_create_index(TableId, 'IdClass');
END;
$$;



CREATE FUNCTION _cm_create_class_triggers(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF _cm_is_superclass(TableId) THEN
		RAISE DEBUG 'Not creating triggers for class %', TableId::regclass;
	ELSIF _cm_is_simpleclass(TableId) THEN
		PERFORM _cm_add_simpleclass_sanity_check_trigger(TableId);
	ELSE
		PERFORM _cm_add_class_sanity_check_trigger(TableId);
		PERFORM _cm_add_class_history_trigger(TableId);
		PERFORM _cm_add_class_cascade_delete_on_relations_trigger(TableId);
	END IF;
END;
$$;



CREATE FUNCTION _cm_create_domain_indexes(domainid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    Cardinality text := _cm_domain_cardinality(DomainId);
BEGIN
	PERFORM _cm_create_index(DomainId, 'IdDomain');
	PERFORM _cm_create_index(DomainId, 'IdObj1');
	PERFORM _cm_create_index(DomainId, 'IdObj2');

	EXECUTE 'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId, 'ActiveRows')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ('||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdDomain" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdClass1" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdObj1" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdClass2" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdObj2" END)'||
		')';

	IF substring(Cardinality, 3, 1) = '1' THEN
		EXECUTE
		'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId,'UniqueLeft')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ( '||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdClass1" ELSE NULL END),'||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdObj1" ELSE NULL END)'||
		' )';
	END IF;

	IF substring(Cardinality, 1, 1) = '1' THEN
		EXECUTE
		'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId,'UniqueRight')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ( '||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdClass2" ELSE NULL END),'||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdObj2" ELSE NULL END)'||
		' )';
	END IF;
END
$$;



CREATE FUNCTION _cm_create_domain_triggers(domainid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	PERFORM _cm_add_domain_sanity_check_trigger(DomainId);
	PERFORM _cm_add_domain_history_trigger(DomainId);
END;
$$;



CREATE FUNCTION _cm_create_index(tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE 'CREATE INDEX ' || quote_ident(_cm_classidx_name(TableId, AttributeName)) ||
		' ON ' || TableId::regclass ||
		' USING btree (' || quote_ident(AttributeName) || ')';
EXCEPTION
	WHEN undefined_column THEN
		RAISE LOG 'Index for attribute %.% not created because the attribute does not exist',
			TableId::regclass, quote_ident(AttributeName);
END
$$;



CREATE FUNCTION _cm_create_schema_if_needed(cmname text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF _cm_cmschema(CMName) IS NOT NULL THEN
		EXECUTE 'CREATE SCHEMA '||quote_ident(_cm_cmschema(CMName));
	END IF;
EXCEPTION
	WHEN duplicate_schema THEN
		RETURN;
END;
$$;



CREATE FUNCTION _cm_delete_local_attributes(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	AttributeName text;
BEGIN
	FOR AttributeName IN SELECT _cm_attribute_list(TableId) LOOP
		IF NOT _cm_attribute_is_inherited(TableId, AttributeName) THEN
			PERFORM cm_delete_attribute(TableId, AttributeName);
		END IF;
	END LOOP;
END
$$;



CREATE FUNCTION _cm_delete_relation(username text, domainid oid, cardidcolumn text, cardid integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
	EXECUTE 'UPDATE ' || DomainId::regclass ||
		' SET "Status" = ''N'', "User" = ' || coalesce(quote_literal(UserName),'NULL') ||
		' WHERE "Status" = ''A'' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId;
END;
$$;



CREATE FUNCTION _cm_dest_classid_for_domain_attribute(domainid oid, attributename text) RETURNS oid
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_table_id(
		_cm_read_comment(
			_cm_comment_for_table_id($1),
			CASE $2
			WHEN 'IdObj1' THEN
				'CLASS1'
			WHEN 'IdObj2' THEN
				'CLASS2'
			ELSE
				NULL
			END
		)
	);
$_$;



CREATE FUNCTION _cm_dest_reference_classid(domainid oid, refidcolumn text, refid integer) RETURNS oid
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_subclassid(_cm_dest_classid_for_domain_attribute($1, $2), $3)
$_$;



CREATE FUNCTION _cm_disable_triggers_recursively(superclass regclass) RETURNS void
    LANGUAGE plpgsql
    AS $_$
DECLARE
	CurrentClass regclass := $1;
BEGIN
	FOR CurrentClass IN SELECT _cm_subtables_and_itself(SuperClass) LOOP
		EXECUTE 'ALTER TABLE '|| CurrentClass::regclass ||' DISABLE TRIGGER USER';
	END LOOP;
END;
$_$;



CREATE FUNCTION _cm_domain_cardinality(domainid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_read_domain_cardinality(_cm_comment_for_table_id($1));
$_$;



CREATE FUNCTION _cm_domain_cmname(cmdomain text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT coalesce(_cm_cmschema($1)||'.','')||coalesce('Map_'||_cm_cmtable($1),'Map');
$_$;



CREATE FUNCTION _cm_domain_cmname_lc(cmdomainname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT lower(_cm_domain_cmname($1));
$_$;



CREATE FUNCTION _cm_domain_dbname(cmdomain text) RETURNS regclass
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_table_dbname(_cm_domain_cmname($1));
$_$;



CREATE FUNCTION _cm_domain_dbname_unsafe(cmdomain text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_table_dbname_unsafe(_cm_domain_cmname($1));
$_$;



CREATE FUNCTION _cm_domain_direction(domainid oid) RETURNS boolean
    LANGUAGE plpgsql STABLE STRICT
    AS $$
DECLARE
	Cardinality text := _cm_domain_cardinality(DomainId);
BEGIN
	IF Cardinality = 'N:1' THEN
		RETURN TRUE;
	ELSIF Cardinality = '1:N' THEN
		RETURN FALSE;
	ELSE
		RETURN NULL;
	END IF;
END
$$;



CREATE FUNCTION _cm_domain_id(cmdomain text) RETURNS oid
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_table_id(_cm_domain_cmname($1));
$_$;



CREATE FUNCTION _cm_domain_list() RETURNS SETOF oid
    LANGUAGE sql STABLE
    AS $$
	SELECT oid FROM pg_class WHERE _cm_is_domain_comment(_cm_comment_for_cmobject(oid));
$$;



CREATE FUNCTION _cm_domainidx_name(domainid oid, type text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT 'idx_' || _cm_cmtable_lc($1) || '_' || lower($2);
$_$;



CREATE FUNCTION _cm_domainpk_name(cmdomainname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_classpk_name(_cm_domain_cmname($1));
$_$;



CREATE FUNCTION _cm_drop_triggers_recursively(tableid oid, triggername text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	SubClassId oid;
BEGIN
	FOR SubClassId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		EXECUTE 'DROP TRIGGER IF EXISTS '|| quote_ident(TriggerName) ||' ON '|| SubClassId::regclass;
	END LOOP;
END;
$$;



CREATE FUNCTION _cm_enable_triggers_recursively(superclass regclass) RETURNS void
    LANGUAGE plpgsql
    AS $_$
DECLARE
	CurrentClass regclass := $1;
BEGIN
	FOR CurrentClass IN SELECT _cm_subtables_and_itself(SuperClass) LOOP
		EXECUTE 'ALTER TABLE '|| CurrentClass::text ||' ENABLE TRIGGER USER';
	END LOOP;
END;
$_$;



CREATE FUNCTION _cm_function_list(OUT function_name text, OUT function_id oid, OUT arg_io character[], OUT arg_names text[], OUT arg_types text[], OUT returns_set boolean, OUT comment text) RETURNS SETOF record
    LANGUAGE plpgsql STABLE
    AS $_$
DECLARE
	R record;
	i integer;
BEGIN
	FOR R IN
		SELECT *
		FROM pg_proc
		WHERE _cm_comment_for_cmobject(oid) IS NOT NULL
	LOOP
		function_name := R.proname::text;
		function_id := R.oid;
		returns_set := R.proretset;
		comment := _cm_comment_for_cmobject(R.oid);
		IF R.proargmodes IS NULL
		THEN
			arg_io := '{}'::char[];
			arg_types := '{}'::text[];
			arg_names := '{}'::text[];
			-- add input columns
			FOR i IN SELECT generate_series(1, array_upper(R.proargtypes,1)) LOOP
				arg_io := arg_io || 'i'::char;
				arg_types := arg_types || _cm_get_sqltype_string(R.proargtypes[i], NULL);
				arg_names := arg_names || COALESCE(R.proargnames[i], '$'||i);
			END LOOP;
			-- add single output column
			arg_io := arg_io || 'o'::char;
			arg_types := arg_types || _cm_get_sqltype_string(R.prorettype, NULL);
			arg_names := arg_names || function_name;
		ELSE
			-- just normalize existing columns
			arg_io := R.proargmodes;
			arg_types := '{}'::text[];
			arg_names := R.proargnames;
			FOR i IN SELECT generate_series(1, array_upper(arg_io,1)) LOOP
				-- normalize table output
				IF arg_io[i] = 't' THEN
					arg_io[i] := 'o';
				ELSIF arg_io[i] = 'b' THEN
					arg_io[i] := 'io';
				END IF;
				arg_types := arg_types || _cm_get_sqltype_string(R.proallargtypes[i], NULL);
				IF arg_names[i] = '' THEN
					IF arg_io[i] = 'i' THEN
						arg_names[i] = '$'||i;
					ELSE
						arg_names[i] = 'column'||i;
					END IF;
				END IF;
			END LOOP;
		END IF;
		RETURN NEXT;
	END LOOP;

	RETURN;
END
$_$;



CREATE FUNCTION _cm_get_attribute_default(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT pg_attrdef.adsrc
		FROM pg_attribute JOIN pg_attrdef ON pg_attrdef.adrelid = pg_attribute.attrelid AND pg_attrdef.adnum = pg_attribute.attnum
		WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$;



CREATE FUNCTION _cm_get_attribute_sqltype(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_get_sqltype_string(pg_attribute.atttypid, pg_attribute.atttypmod)
		FROM pg_attribute
		WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$;



CREATE FUNCTION _cm_get_domain_reference_target_comment(domaincomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT CASE _cm_read_domain_cardinality($1)
		WHEN '1:N' THEN _cm_read_comment($1, 'CLASS1')
		WHEN 'N:1' THEN _cm_read_comment($1, 'CLASS2')
		ELSE NULL
	END
$_$;



CREATE FUNCTION _cm_get_fk_target(tableid oid, attributename text) RETURNS text
    LANGUAGE plpgsql STABLE STRICT
    AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	RETURN COALESCE(
		_cm_get_fk_target_comment(AttributeComment),
		_cm_read_reference_target_comment(AttributeComment)
	);
END
$$;



CREATE FUNCTION _cm_get_fk_target_comment(attributecomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_read_comment($1, 'FKTARGETCLASS');
$_$;



CREATE FUNCTION _cm_get_fk_target_table_id(tableid oid, attributename text) RETURNS oid
    LANGUAGE plpgsql STABLE STRICT
    AS $_$ BEGIN
	RETURN _cm_table_id(_cm_get_fk_target($1, $2));
END $_$;



CREATE FUNCTION _cm_get_geometry_type(tableid oid, attribute text) RETURNS text
    LANGUAGE plpgsql STABLE
    AS $_$
DECLARE
	GeoType text;
BEGIN
	SELECT geometry_columns.type INTO GeoType
	FROM pg_attribute
	LEFT JOIN geometry_columns
		ON f_table_schema = _cm_cmschema($1)
		AND f_table_name = _cm_cmtable($1)
		AND f_geometry_column = $2
	WHERE attrelid = $1 AND attname = $2 AND attnum > 0 AND atttypid > 0;
	RETURN GeoType;
EXCEPTION WHEN undefined_table THEN
	RETURN NULL;
END
$_$;



CREATE FUNCTION _cm_get_lookup_type_comment(attributecomment text) RETURNS text
    LANGUAGE sql
    AS $_$
	SELECT _cm_read_comment($1, 'LOOKUP');
$_$;



CREATE FUNCTION _cm_get_ref_source_class_domain_attribute(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdClass1'
		WHEN FALSE THEN 'IdClass2'
		ELSE NULL
	END;
$_$;



CREATE FUNCTION _cm_get_ref_source_id_domain_attribute(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdObj1'
		WHEN FALSE THEN 'IdObj2'
		ELSE NULL
	END;
$_$;



CREATE FUNCTION _cm_get_ref_target_id_domain_attribute(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdObj2'
		WHEN FALSE THEN 'IdObj1'
		ELSE NULL
	END;
$_$;



CREATE FUNCTION _cm_get_reference_domain_id(tableid oid, attributename text) RETURNS oid
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_read_reference_domain_id_comment(_cm_comment_for_attribute($1, $2));
$_$;



CREATE FUNCTION _cm_get_safe_classorder(tableid regclass, attname character varying, OUT classorder integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
	SELECT 
		INTO classorder 
		CASE WHEN (coalesce(_cm_read_comment(_cm_comment_for_attribute(tableid, attname), 'CLASSORDER'), '')<>'') THEN _cm_read_comment(_cm_comment_for_attribute(tableid, attname), 'CLASSORDER')::integer
		ELSE 0 END;
END;
$$;



CREATE FUNCTION _cm_get_sqltype_string(sqltypeid oid, typemod integer) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT pg_type.typname::text || COALESCE(
			CASE
				WHEN pg_type.typname IN ('varchar','bpchar') THEN '(' || $2 - 4 || ')'
				WHEN pg_type.typname = 'numeric' THEN '(' ||
					$2 / 65536 || ',' ||
					$2 - $2 / 65536 * 65536 - 4|| ')'
			END, '')
		FROM pg_type WHERE pg_type.oid = $1;
$_$;



CREATE FUNCTION _cm_get_type_comment(classcomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_read_comment($1, 'TYPE');
$_$;



CREATE FUNCTION _cm_history_cmname(cmclass text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT $1 || '_history';
$_$;



CREATE FUNCTION _cm_history_dbname(cmtable text) RETURNS regclass
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_table_dbname(_cm_history_cmname($1));
$_$;



CREATE FUNCTION _cm_history_dbname_unsafe(cmtable text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_table_dbname_unsafe(_cm_history_cmname($1));
$_$;



CREATE FUNCTION _cm_history_id(cmtable text) RETURNS oid
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_table_id(_cm_history_cmname($1));
$_$;



CREATE FUNCTION _cm_historyfk_name(cmclassname text, attributename text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_classfk_name(_cm_history_cmname($1), $2);
$_$;



CREATE FUNCTION _cm_historypk_name(cmclassname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_classpk_name(_cm_history_cmname($1));
$_$;



CREATE FUNCTION _cm_insert_relation(username text, domainid oid, cardidcolumn text, cardid integer, refidcolumn text, refid integer, cardclassid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	CardClassIdColumnPart text;
	RefClassIdColumnPart text;
	CardClassIdValuePart text;
	RefClassIdValuePart text;
	StopRecursion boolean;
BEGIN
	IF (CardId IS NULL OR RefId IS NULL) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- Needed for backward compatibility
	CardClassIdColumnPart := coalesce(quote_ident('IdClass'||substring(CardIdColumn from '^IdObj(.)+')) || ', ', '');
	RefClassIdColumnPart := coalesce(quote_ident('IdClass'||substring(RefIdColumn from '^IdObj(.)+')) || ', ', '');
	CardClassIdValuePart := CASE WHEN CardClassIdColumnPart IS NOT NULL THEN (coalesce(CardClassId::text, 'NULL') || ', ') ELSE '' END;
	RefClassIdValuePart := coalesce(_cm_dest_reference_classid(DomainId, RefIdColumn, RefId)::text, 'NULL') || ', ';

	-- Stop trigger recursion
	EXECUTE 'SELECT (COUNT(*) > 0) FROM ' || DomainId::regclass ||
		' WHERE' ||
			' "IdDomain" = ' || DomainId::text || -- NOTE: why is this check done?
			' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId::text ||
			' AND ' || quote_ident(RefIdColumn) || ' = ' || RefId::text ||
			' AND "Status" = ''A''' INTO StopRecursion;
	IF NOT StopRecursion THEN
		EXECUTE 'INSERT INTO ' || DomainId::regclass ||
			' (' ||
				'"IdDomain", ' ||
				quote_ident(CardIdColumn) || ', ' ||
				quote_ident(RefIdColumn) || ', ' ||
				CardClassIdColumnPart ||
				RefClassIdColumnPart ||
				'"Status", ' ||
				'"User"' ||
			') VALUES (' ||
				DomainId::text || ', ' ||
				CardId::text || ', ' ||
				RefId::text || ', ' ||
				CardClassIdValuePart ||
				RefClassIdValuePart ||
				'''A'', ' ||
				coalesce(quote_literal(UserName), 'NULL') ||
			')';
	END IF;
END;
$$;



CREATE FUNCTION _cm_is_active_comment(classcomment text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment($1, 'STATUS', 'active');
$_$;



CREATE FUNCTION _cm_is_any_class(classid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_any_class_comment(_cm_comment_for_table_id($1))
$_$;



CREATE FUNCTION _cm_is_any_class_comment(classcomment text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment($1, 'TYPE', '%class');
$_$;



CREATE FUNCTION _cm_is_cmobject(tableid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_comment_for_cmobject($1) IS NOT NULL;
$_$;



CREATE FUNCTION _cm_is_domain_comment(classcomment text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment($1, 'TYPE', 'domain');
$_$;



CREATE FUNCTION _cm_is_geometry_type(cmattributetype text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT $1 IN ('POINT','LINESTRING','POLYGON');
$_$;



CREATE FUNCTION _cm_is_process(classid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT $1 IN (SELECT _cm_subtables_and_itself(_cm_table_id('Activity')));
$_$;



CREATE FUNCTION _cm_is_process(cmclass text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_process(_cm_table_id($1));
$_$;



CREATE FUNCTION _cm_is_reference_comment(attributecomment text) RETURNS boolean
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT COALESCE(_cm_read_reference_domain_comment($1),'') != '';
$_$;



CREATE FUNCTION _cm_is_simpleclass(classid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_simpleclass_comment(_cm_comment_for_table_id($1))
$_$;



CREATE FUNCTION _cm_is_simpleclass(cmclass text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_simpleclass_comment(_cm_comment_for_class($1));
$_$;



CREATE FUNCTION _cm_is_simpleclass_comment(classcomment text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment($1, 'TYPE', 'simpleclass');
$_$;



CREATE FUNCTION _cm_is_superclass(classid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_superclass_comment(_cm_comment_for_table_id($1));
$_$;



CREATE FUNCTION _cm_is_superclass(cmclass text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_superclass_comment(_cm_comment_for_class($1));
$_$;



CREATE FUNCTION _cm_is_superclass_comment(classcomment text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment($1, 'SUPERCLASS', 'true');
$_$;



CREATE FUNCTION _cm_is_system(tableid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment(_cm_comment_for_table_id($1), 'MODE', 'reserved')
$_$;



CREATE FUNCTION _cm_join_cmname(cmschema name, cmtable name) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT $1 || '.' || $2;
$_$;



CREATE FUNCTION _cm_legacy_get_menu_code(boolean, boolean, boolean, boolean) RETURNS character varying
    LANGUAGE plpgsql
    AS $_$
    DECLARE 
        issuperclass ALIAS FOR $1;
        isprocess ALIAS FOR $2;
        isreport ALIAS FOR $3;
        isview ALIAS FOR $4;
	menucode varchar;
    BEGIN
	IF (issuperclass) THEN IF (isprocess) THEN menucode='superclassprocess'; ELSE menucode='superclass'; END IF;
	ELSIF(isview) THEN menucode='view';
	ELSIF(isreport) THEN menucode='report';
	ELSIF (isprocess) THEN menucode='processclass'; ELSE menucode='class';
	END IF;

	RETURN menucode;
    END;
$_$;



CREATE FUNCTION _cm_legacy_get_menu_type(boolean, boolean, boolean, boolean) RETURNS character varying
    LANGUAGE plpgsql
    AS $_$
    DECLARE 
        issuperclass ALIAS FOR $1;
        isprocess ALIAS FOR $2;
        isreport ALIAS FOR $3;
        isview ALIAS FOR $4;
	menutype varchar;
    BEGIN
	IF (isprocess) THEN menutype='processclass';
	ELSIF(isview) THEN menutype='view';
	ELSIF(isreport) THEN menutype='report';
	ELSE menutype='class';
	END IF;

	RETURN menutype;
    END;
$_$;



CREATE FUNCTION _cm_legacy_read_comment(text, text) RETURNS character varying
    LANGUAGE sql STABLE
    AS $_$
	SELECT COALESCE(_cm_read_comment($1, $2), '');
$_$;



CREATE FUNCTION _cm_new_card_id() RETURNS integer
    LANGUAGE sql
    AS $$
	SELECT nextval(('class_seq'::text)::regclass)::integer;
$$;



CREATE FUNCTION _cm_notnull_constraint_name(attributename text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT '_NotNull_'||$1;
$_$;



CREATE FUNCTION _cm_parent_id(tableid oid) RETURNS SETOF oid
    LANGUAGE sql
    AS $_$
	SELECT COALESCE((SELECT inhparent FROM pg_inherits WHERE inhrelid = $1 AND _cm_is_cmobject(inhparent) LIMIT 1), NULL);
$_$;



CREATE FUNCTION _cm_propagate_superclass_triggers(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	ParentId oid := _cm_parent_id(TableId);
BEGIN
	PERFORM _cm_copy_restrict_trigger(ParentId, TableId);
	PERFORM _cm_copy_update_relation_trigger(ParentId, TableId);
	PERFORM _cm_copy_fk_trigger(ParentId, TableId);
END
$$;



CREATE FUNCTION _cm_read_comment(comment text, key text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT TRIM(SUBSTRING($1 FROM E'(?:^|\\|)'||$2||E':[ ]*([^\\|]+)'));
$_$;



CREATE FUNCTION _cm_read_domain_cardinality(attributecomment text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_read_comment($1, 'CARDIN');
$_$;



CREATE FUNCTION _cm_read_reference_domain_comment(attributecomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_read_comment($1, 'REFERENCEDOM');
$_$;



CREATE FUNCTION _cm_read_reference_domain_id_comment(attributecomment text) RETURNS oid
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_domain_id(_cm_read_reference_domain_comment($1));
$_$;



CREATE FUNCTION _cm_read_reference_target_comment(attributecomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_get_domain_reference_target_comment(_cm_comment_for_domain(_cm_read_reference_domain_comment($1)));
$_$;



CREATE FUNCTION _cm_read_reference_target_id_comment(attributecomment text) RETURNS oid
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_table_id(_cm_read_reference_target_comment($1));
$_$;



CREATE FUNCTION _cm_read_reference_type_comment(attributecomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT COALESCE(NULLIF(_cm_read_comment($1, 'REFERENCETYPE'), ''), 'restrict');
$_$;



CREATE FUNCTION _cm_remove_attribute_triggers(tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	PERFORM _cm_remove_fk_constraints(TableId, AttributeName);
	PERFORM _cm_remove_reference_handling(TableId, AttributeName);
END;
$$;



CREATE FUNCTION _cm_remove_constraint_trigger(fktargetclassid oid, fkclassid oid, fkattribute text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE '
		DROP TRIGGER ' || quote_ident('_Constr_'||_cm_cmtable(FKClassId)||'_'||FKAttribute) ||
			' ON ' || FKTargetClassId::regclass || ';
	';
END;
$$;



CREATE FUNCTION _cm_remove_fk_constraints(fksourceid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	TargetId oid := _cm_get_fk_target_table_id(FKSourceId, AttributeName);
	SubTableId oid;
BEGIN
	IF TargetId IS NULL THEN
		RETURN;
	END IF;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(FKSourceId) LOOP
		EXECUTE 'DROP TRIGGER '|| quote_ident(_cm_classfk_name(FKSourceId, AttributeName)) ||
			' ON '|| SubTableId::regclass;
	END LOOP;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(TargetId) LOOP
		PERFORM _cm_remove_constraint_trigger(SubTableId, FKSourceId, AttributeName);
	END LOOP;
END;
$$;



CREATE FUNCTION _cm_remove_reference_handling(tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	-- remove UpdRel and UpdRef triggers
	PERFORM _cm_drop_triggers_recursively(
		TableId,
		_cm_update_relation_trigger_name(TableId, AttributeName)
	);
	PERFORM _cm_drop_triggers_recursively(
		_cm_get_reference_domain_id(TableId, AttributeName),
		_cm_update_reference_trigger_name(TableId, AttributeName)
	);
END
$$;



CREATE FUNCTION _cm_restrict(id integer, tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $_$
BEGIN
	IF _cm_check_value_exists($1, $2, $3, FALSE) THEN
		RAISE EXCEPTION 'CM_RESTRICT_VIOLATION';
	END IF;
END;
$_$;



CREATE FUNCTION _cm_set_attribute_comment(tableid oid, attributename text, comment text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	SubClassId oid;
BEGIN
	FOR SubClassId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		EXECUTE 'COMMENT ON COLUMN '|| SubClassId::regclass ||'.'|| quote_ident(AttributeName) ||' IS '|| quote_literal(Comment);
	END LOOP;
END;
$$;



CREATE FUNCTION _cm_set_attribute_default(tableid oid, attributename text, newdefault text, updateexisting boolean) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	CurrentDefaultSrc text := _cm_get_attribute_default(TableId, AttributeName);
	NewDefaultSrc text := _cm_attribute_default_to_src(TableId, AttributeName, NewDefault);
BEGIN
    IF (NewDefaultSrc IS DISTINCT FROM CurrentDefaultSrc) THEN
    	IF (CurrentDefaultSrc IS NULL) THEN
	        EXECUTE 'ALTER TABLE ' || TableId::regclass ||
					' ALTER COLUMN ' || quote_ident(AttributeName) ||
					' SET DEFAULT ' || NewDefaultSrc;
			IF UpdateExisting THEN
	        	EXECUTE 'UPDATE '|| TableId::regclass ||' SET '|| quote_ident(AttributeName) ||' = '|| NewDefaultSrc;
	        END IF;
	    ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN	'|| quote_ident(AttributeName) ||' DROP DEFAULT';
		END IF;
    END IF;
END;
$$;



CREATE FUNCTION _cm_setnull(id integer, tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE 'UPDATE '|| TableId::regclass ||
		' SET '||quote_ident(AttributeName)||' = NULL'||
		' WHERE '||quote_ident(AttributeName)||' = '||Id::text;
END;
$$;



CREATE FUNCTION _cm_split_cmname(cmname text) RETURNS text[]
    LANGUAGE sql IMMUTABLE
    AS $_$
    SELECT regexp_matches($1,E'(?:([^\\.]+)\\.)?([^\\.]+)?');
$_$;



CREATE FUNCTION _cm_string_agg(anyarray) RETURNS text
    LANGUAGE sql
    AS $_$
	SELECT case when trim(array_to_string($1, ', ')) = '' THEN null else array_to_string($1, ', ') END
$_$;



CREATE FUNCTION _cm_subclassid(superclassid oid, cardid integer) RETURNS oid
    LANGUAGE plpgsql STABLE STRICT
    AS $$
DECLARE
	Out integer;
BEGIN
	EXECUTE 'SELECT tableoid FROM '||SuperClassId::regclass||' WHERE "Id"='||CardId||' LIMIT 1' INTO Out;
	RETURN Out;
END;
$$;



CREATE FUNCTION _cm_subtables_and_itself(tableid oid) RETURNS SETOF oid
    LANGUAGE sql
    AS $_$
	SELECT $1 WHERE _cm_is_cmobject($1)
	UNION
	SELECT _cm_subtables_and_itself(inhrelid) FROM pg_inherits WHERE inhparent = $1
$_$;



CREATE FUNCTION _cm_table_dbname(cmname text) RETURNS regclass
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_table_dbname_unsafe($1)::regclass;
$_$;



CREATE FUNCTION _cm_table_dbname_unsafe(cmname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT coalesce(quote_ident(_cm_cmschema($1))||'.','')||quote_ident(_cm_cmtable($1));
$_$;



CREATE FUNCTION _cm_table_id(cmname text) RETURNS oid
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_table_dbname_unsafe($1)::regclass::oid;
$_$;



CREATE FUNCTION _cm_table_is_empty(tableid oid) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	NotFound boolean;
BEGIN
	-- Note: FOUND variable is not set on EXECUTE, so we can't use it!
	EXECUTE 'SELECT (COUNT(*) = 0) FROM '|| TableId::regclass ||' LIMIT 1' INTO NotFound;
	RETURN NotFound;
END;
$$;



CREATE FUNCTION _cm_trigger_cascade_delete_on_relations() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (NEW."Status"='N') THEN
		UPDATE "Map" SET "Status"='N'
			WHERE "Status"='A' AND (
				("IdObj1" = OLD."Id" AND "IdClass1" = TG_RELID)
				OR ("IdObj2" = OLD."Id" AND "IdClass2" = TG_RELID)
			);
	END IF;
	RETURN NEW;
END;
$$;



CREATE FUNCTION _cm_trigger_create_card_history_row() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	-- Does not create the row on logic deletion
	IF (TG_OP='UPDATE') THEN
		OLD."Id" = _cm_new_card_id();
		OLD."Status" = 'U';
		EXECUTE 'INSERT INTO '||_cm_history_dbname(_cm_join_cmname(TG_TABLE_SCHEMA, TG_TABLE_NAME)) ||
			' ('||_cm_attribute_list_cs(TG_RELID)||',"CurrentId","EndDate")' ||
			' VALUES (' ||
			' (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').*, ' ||
			' (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ')."Id", now())';
	ELSIF (TG_OP='DELETE') THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	RETURN NEW;
END;
$$;



CREATE FUNCTION _cm_trigger_create_relation_history_row() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	-- Does not create the row on logic deletion
	IF (TG_OP='UPDATE') THEN
		OLD."Status" = 'U';
		OLD."EndDate" = now();
		EXECUTE 'INSERT INTO '||_cm_history_dbname(_cm_join_cmname(TG_TABLE_SCHEMA, TG_TABLE_NAME)) ||
			' ('||_cm_attribute_list_cs(TG_RELID)||')' ||
			' VALUES (' ||
			' (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').*)';
	ELSIF (TG_OP='DELETE') THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	RETURN NEW;
END;
$$;



CREATE FUNCTION _cm_trigger_fk() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	SourceAttribute text := TG_ARGV[0];
	TargetClassId oid := TG_ARGV[1]::regclass::oid;
	TriggerVariant text := TG_ARGV[2];
	RefValue integer;
	ActiveCardsOnly boolean;
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(SourceAttribute) INTO RefValue;

	IF (TriggerVariant = 'simple') THEN
		ActiveCardsOnly := FALSE;
	ELSE
		ActiveCardsOnly := NEW."Status" <> 'A';
	END IF;

	IF NOT _cm_check_id_exists(RefValue, TargetClassId, ActiveCardsOnly) THEN
		RETURN NULL;
	END IF;

	RETURN NEW;
END;
$$;



CREATE FUNCTION _cm_trigger_restrict() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	TableId oid := TG_ARGV[0]::regclass::oid;
	AttributeName text := TG_ARGV[1];
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (TG_OP='UPDATE' AND NEW."Status"='N') THEN
		PERFORM _cm_restrict(OLD."Id", TableId, AttributeName);
	END IF;
	RETURN NEW;
END;
$$;



CREATE FUNCTION _cm_trigger_row_or_statement(tgtype smallint) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT CASE $1 & cast(1 as int2)
         WHEN 0 THEN 'STATEMENT'
         ELSE 'ROW'
       END;
$_$;



CREATE FUNCTION _cm_trigger_sanity_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF (TG_OP='UPDATE') THEN
		IF (NEW."Id" <> OLD."Id") THEN -- Id change
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
		IF (NEW."Status"='N' AND OLD."Status"='N') THEN -- Deletion of a deleted card
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
	ELSIF (TG_OP='INSERT') THEN
		IF (NEW."Status" IS NULL) THEN
			NEW."Status"='A';
		ELSIF (NEW."Status"='N') THEN -- Creation of a deleted card
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
		NEW."Id" = _cm_new_card_id();
		-- Class ID is needed because of the history tables
		BEGIN
			NEW."IdClass" = TG_RELID;
		EXCEPTION WHEN undefined_column THEN
			NEW."IdDomain" = TG_RELID;
		END;
	ELSE -- TG_OP='DELETE'
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- 'U' is reserved for history tables only
	IF (position(NEW."Status" IN 'AND') = 0) THEN -- Invalid status
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	NEW."BeginDate" = now();
	RETURN NEW;
END;
$$;



CREATE FUNCTION _cm_trigger_sanity_check_simple() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF (TG_OP='UPDATE') THEN
		IF (NEW."Id" <> OLD."Id") THEN -- Id change
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
	ELSIF (TG_OP='DELETE') THEN
		-- RETURN NEW would return NULL forbidding the operation
		RETURN OLD;
	ELSE
		NEW."BeginDate" = now();
		NEW."IdClass" = TG_RELID;
	END IF;
	RETURN NEW;
END;
$$;



CREATE FUNCTION _cm_trigger_update_reference() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	AttributeName text := TG_ARGV[0];
	TableId oid := TG_ARGV[1]::regclass::oid;
	CardColumn text := TG_ARGV[2]; -- Domain column name for the card id
	RefColumn text := TG_ARGV[3];  -- Domain column name for the reference id

	OldCardId integer;
	NewCardId integer;
	OldRefValue integer;
	NewRefValue integer;
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (NEW."Status"='A') THEN
		EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(RefColumn) INTO NewRefValue;
	ELSIF (NEW."Status"<>'N') THEN
		-- Ignore history rows
		RETURN NEW;
	END IF;

	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(CardColumn) INTO NewCardId;

	IF (TG_OP='UPDATE') THEN
		EXECUTE 'SELECT (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').' || quote_ident(CardColumn) INTO OldCardId;
		IF (OldCardId <> NewCardId) THEN -- If the non-reference side changes...
			PERFORM _cm_update_reference(NEW."User", TableId, AttributeName, OldCardId, NULL);
			-- OldRefValue is kept null because it is like a new relation
		ELSE
			EXECUTE 'SELECT (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').' || quote_ident(RefColumn) INTO OldRefValue;
		END IF;
	END IF;

	IF ((NewRefValue IS NULL) OR (OldRefValue IS NULL) OR (OldRefValue <> NewRefValue)) THEN
		PERFORM _cm_update_reference(NEW."User", TableId, AttributeName, NewCardId, NewRefValue);
	END IF;

	RETURN NEW;
END;
$$;



CREATE FUNCTION _cm_trigger_update_relation() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	AttributeName text := TG_ARGV[0];
	DomainId oid := TG_ARGV[1]::regclass::oid;
	CardColumn text := TG_ARGV[2]; -- Domain column name for the card id
	RefColumn text := TG_ARGV[3];  -- Domain column name for the reference id

	OldRefValue integer;
	NewRefValue integer;
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (TG_OP = 'UPDATE') THEN
		EXECUTE 'SELECT (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').' || quote_ident(AttributeName) INTO OldRefValue;
	END IF;
	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(AttributeName) INTO NewRefValue;

	IF (NewRefValue IS NOT NULL) THEN
		IF (OldRefValue IS NOT NULL) THEN
			IF (OldRefValue <> NewRefValue) THEN
				PERFORM _cm_update_relation(NEW."User", DomainId, CardColumn, NEW."Id", RefColumn, NewRefValue);
			END IF;
		ELSE
			PERFORM _cm_insert_relation(NEW."User", DomainId, CardColumn, NEW."Id", RefColumn, NewRefValue, TG_RELID);
		END IF;
	ELSE
		IF (OldRefValue IS NOT NULL) THEN
			PERFORM _cm_delete_relation(NEW."User", DomainId, CardColumn, NEW."Id");
		END IF;
	END IF;
	RETURN NEW;
END;
$$;



CREATE FUNCTION _cm_trigger_when(tgtype smallint) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT CASE $1 & cast(2 as int2)
         WHEN 0 THEN 'AFTER'
         ELSE 'BEFORE'
       END || ' ' ||
       CASE $1 & cast(28 as int2)
         WHEN 16 THEN 'UPDATE'
         WHEN  8 THEN 'DELETE'
         WHEN  4 THEN 'INSERT'
         WHEN 20 THEN 'INSERT OR UPDATE'
         WHEN 28 THEN 'INSERT OR UPDATE OR DELETE'
         WHEN 24 THEN 'UPDATE OR DELETE'
         WHEN 12 THEN 'INSERT OR DELETE'
       END;
$_$;



CREATE FUNCTION _cm_unique_index_id(tableid oid, attributename text) RETURNS oid
    LANGUAGE sql STABLE
    AS $_$
	SELECT (
		quote_ident(_cm_cmschema($1))
		||'.'||
		quote_ident(_cm_unique_index_name($1, $2))
	)::regclass::oid;
$_$;



CREATE FUNCTION _cm_unique_index_name(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT '_Unique_'|| _cm_cmtable($1) ||'_'|| $2;
$_$;



CREATE FUNCTION _cm_update_reference(username text, tableid oid, attributename text, cardid integer, referenceid integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE 'UPDATE ' || TableId::regclass ||
		' SET ' || quote_ident(AttributeName) || ' = ' || coalesce(ReferenceId::text, 'NULL') ||
		', "User" = ' || coalesce(quote_literal(UserName),'NULL') ||
		' WHERE "Status"=''A'' AND "Id" = ' || CardId::text ||
		' AND coalesce(' || quote_ident(AttributeName) || ', 0) <> ' || coalesce(ReferenceId, 0)::text;
END;
$$;



CREATE FUNCTION _cm_update_reference_trigger_name(reftableid oid, refattribute text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT '_UpdRef_'|| _cm_cmtable($1) ||'_'|| $2;
$_$;



CREATE FUNCTION _cm_update_relation(username text, domainid oid, cardidcolumn text, cardid integer, refidcolumn text, refid integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	RefClassUpdatePart text;
BEGIN
	-- Needed to update IdClassX (if the domain attributres are IdClass1/2)
	RefClassUpdatePart := coalesce(
		', ' || quote_ident('IdClass'||substring(RefIdColumn from E'^IdObj(\\d)+')) || 
			'=' || _cm_dest_reference_classid(DomainId, RefIdColumn, RefId),
		''
	);

	EXECUTE 'UPDATE ' || DomainId::regclass ||
		' SET ' || quote_ident(RefIdColumn) || ' = ' || RefId ||
			', "User" = ' || coalesce(quote_literal(UserName),'NULL') ||
			RefClassUpdatePart ||
		' WHERE "Status"=''A'' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId ||
			' AND ' || quote_ident(RefIdColumn) || ' <> ' || RefId;
END;
$$;



CREATE FUNCTION _cm_update_relation_trigger_name(reftableid oid, refattribute text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT '_UpdRel_'|| _cm_cmtable($1) ||'_'|| $2;
$_$;



CREATE FUNCTION _cm_zero_rownum_sequence() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	temp BIGINT;
BEGIN
	SELECT INTO temp setval('rownum', 0, true);
EXCEPTION WHEN undefined_table THEN
	CREATE TEMPORARY SEQUENCE rownum MINVALUE 0 START 1;
END
$$;



CREATE FUNCTION _cmf_class_description(cid oid) RETURNS character varying
    LANGUAGE sql STABLE
    AS $_$
    SELECT _cm_read_comment(_cm_comment_for_table_id($1), 'DESCR');
$_$;



CREATE FUNCTION _cmf_is_displayable(cid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
    SELECT _cm_read_comment(_cm_comment_for_table_id($1), 'MODE') IN
('write','read','baseclass');
$_$;



CREATE FUNCTION cm_attribute_exists(schemaname text, tablename text, attributename text, OUT attribute_exists boolean) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	attribute_name varchar;
BEGIN
	SELECT attname into attribute_name
	FROM pg_attribute 
	WHERE 	attrelid = (SELECT oid FROM pg_class WHERE relname = tablename AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname=schemaname)) AND
		attname = attributename;

	IF(attribute_name is not null) THEN
		attribute_exists = true;
	ELSE
		attribute_exists = false;
	END IF;
END;
$$;



COMMENT ON FUNCTION cm_attribute_exists(schemaname text, tablename text, attributename text, OUT attribute_exists boolean) IS 'TYPE: function';



CREATE FUNCTION cm_create_attribute(tableid oid, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	PERFORM _cm_check_attribute_comment_and_type(AttributeComment, SQLType);

	IF _cm_is_geometry_type(SQLType) THEN
		PERFORM _cm_add_spherical_mercator();
		PERFORM AddGeometryColumn(_cm_cmschema(TableId), _cm_cmtable(TableId), AttributeName, 900913, SQLType, 2);
	ELSE
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ADD COLUMN '|| quote_ident(AttributeName) ||' '|| SQLType;
	END IF;

    PERFORM _cm_set_attribute_default(TableId, AttributeName, AttributeDefault, TRUE);

	-- set the comment recursively (needs to be performed before unique and notnull, because they depend on the comment)
    PERFORM _cm_set_attribute_comment(TableId, AttributeName, AttributeComment);

	PERFORM _cm_attribute_set_notnull(TableId, AttributeName, AttributeNotNull);
	PERFORM _cm_attribute_set_uniqueness(TableId, AttributeName, AttributeUnique);

    PERFORM _cm_add_fk_constraints(TableId, AttributeName);
	PERFORM _cm_add_reference_handling(TableId, AttributeName);
END;
$$;



CREATE FUNCTION cm_create_class(cmclass text, parentid oid, classcomment text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	IsSimpleClass boolean := _cm_is_simpleclass_comment(ClassComment);
	TableId oid;
BEGIN
	IF (IsSimpleClass AND ParentId IS NOT NULL) OR (NOT _cm_is_any_class_comment(ClassComment))
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	-- TODO: Check if the superclass is a superclass

	PERFORM _cm_create_schema_if_needed(CMClass);

	DECLARE
		DBClassName text := _cm_table_dbname_unsafe(CMClass);
		InheritancePart text;
		AttributesPart text;
	BEGIN
		IF ParentId IS NULL THEN
			AttributesPart := '
				"Id" integer NOT NULL DEFAULT _cm_new_card_id(),
			';
			InheritancePart := '';
		ELSE
			AttributesPart := '';
			InheritancePart := ' INHERITS ('|| ParentId::regclass ||')';
		END IF;
		EXECUTE 'CREATE TABLE '|| DBClassName ||
			'('|| AttributesPart ||
				' CONSTRAINT '|| quote_ident(_cm_classpk_name(CMClass)) ||' PRIMARY KEY ("Id")'||
			')' || InheritancePart;
		EXECUTE 'COMMENT ON TABLE '|| DBClassName ||' IS '|| quote_literal(ClassComment);
		EXECUTE 'COMMENT ON COLUMN '|| DBClassName ||'."Id" IS '|| quote_literal('MODE: reserved');
		TableId := _cm_table_id(CMClass);
	END;

	PERFORM _cm_copy_superclass_attribute_comments(TableId, ParentId);

	PERFORM _cm_create_class_triggers(TableId);

	IF ParentId IS NULL THEN
		PERFORM cm_create_attribute(TableId, 'IdClass', 'regclass', NULL, TRUE, FALSE, 'MODE: reserved');
		IF NOT IsSimpleClass THEN
			PERFORM cm_create_attribute(TableId, 'Code', 'varchar(100)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true');
			PERFORM cm_create_attribute(TableId, 'Description', 'varchar(250)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true');
			-- Status is the only attribute needed
			PERFORM cm_create_attribute(TableId, 'Status', 'character(1)', NULL, FALSE, FALSE, 'MODE: reserved');
		END IF;
		PERFORM cm_create_attribute(TableId, 'User', 'varchar(40)', NULL, FALSE, FALSE, 'MODE: reserved');
		IF IsSimpleClass THEN
			PERFORM cm_create_attribute(TableId, 'BeginDate', 'timestamp', 'now()', TRUE, FALSE, 'MODE: write|FIELDMODE: read|BASEDSP: true');
		ELSE
			PERFORM cm_create_attribute(TableId, 'BeginDate', 'timestamp', 'now()', TRUE, FALSE, 'MODE: reserved');
			PERFORM cm_create_attribute(TableId, 'Notes', 'text', NULL, FALSE, FALSE, 'MODE: read|DESCR: Notes|INDEX: 3');
		END IF;
	ELSE
	    PERFORM _cm_propagate_superclass_triggers(TableId);
	END IF;

	IF IsSimpleClass THEN
		PERFORM _cm_create_index(TableId, 'BeginDate');
	ELSE
		PERFORM _cm_create_class_indexes(TableId);
		IF NOT _cm_is_superclass_comment(ClassComment) THEN
			PERFORM _cm_create_class_history(CMClass);
		END IF;
	END IF;

	RETURN TableId::integer;
END;
$$;



CREATE FUNCTION cm_create_class(cmclass text, cmparentclass text, classcomment text) RETURNS integer
    LANGUAGE sql
    AS $_$
	SELECT cm_create_class($1, _cm_table_id($2), $3);
$_$;



CREATE FUNCTION cm_create_class_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_create_attribute(_cm_table_id($1), $2, $3, $4, $5, $6, $7);
$_$;



CREATE FUNCTION cm_create_domain(cmdomain text, domaincomment text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	DomainId oid;
	HistoryDBName text := _cm_history_dbname_unsafe(_cm_domain_cmname(CMDomain));
BEGIN
	-- TODO: Add Creation of Map (from its name)
	EXECUTE 'CREATE TABLE '|| _cm_domain_dbname_unsafe(CMDomain) ||
		' (CONSTRAINT '|| quote_ident(_cm_domainpk_name(CMDomain)) ||
		' PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate"))'||
		' INHERITS ("Map")';

	DomainId := _cm_domain_id(CMDomain);

	EXECUTE 'COMMENT ON TABLE '|| DomainId::regclass ||' IS '|| quote_literal(DomainComment);
	PERFORM _cm_copy_superclass_attribute_comments(DomainId, '"Map"'::regclass);

	EXECUTE 'CREATE TABLE '|| HistoryDBName ||
		' ( CONSTRAINT '|| quote_ident(_cm_historypk_name(_cm_domain_cmname(CMDomain))) ||
		' PRIMARY KEY ("IdDomain","IdClass1", "IdObj1", "IdClass2", "IdObj2","EndDate"))'||
		' INHERITS ('|| DomainId::regclass ||')';
	EXECUTE 'ALTER TABLE '|| HistoryDBName ||' ALTER COLUMN "EndDate" SET DEFAULT now()';

	PERFORM _cm_create_domain_indexes(DomainId);

	PERFORM _cm_create_domain_triggers(DomainId);

	RETURN DomainId;
END
$$;



CREATE FUNCTION cm_create_domain_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_create_attribute(_cm_domain_id($1), $2, $3, $4, $5, $6, $7);
$_$;



CREATE FUNCTION cm_delete_attribute(tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	GeoType text := _cm_get_geometry_type(TableId, AttributeName);
BEGIN
	IF NOT _cm_attribute_is_local(TableId, AttributeName) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

    IF NOT _cm_attribute_is_empty(TableId, AttributeName) THEN
		RAISE EXCEPTION 'CM_CONTAINS_DATA';
	END IF;

	PERFORM _cm_remove_attribute_triggers(TableId, AttributeName);

	IF GeoType IS NOT NULL THEN
		PERFORM DropGeometryColumn(_cm_cmschema(TableId), _cm_cmtable(TableId), AttributeName);
	ELSE
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP COLUMN '|| quote_ident(AttributeName) ||' CASCADE';
	END IF;
END;
$$;



CREATE FUNCTION cm_delete_card(cardid integer, tableid oid, username text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	ClassComment text := _cm_comment_for_table_id(TableId);
	IsSimpleClass boolean := _cm_is_simpleclass_comment(ClassComment);
BEGIN
	IF IsSimpleClass THEN
		RAISE DEBUG 'deleting a card from a simple class';
		EXECUTE 'DELETE FROM ' || TableId::regclass || ' WHERE "Id" = ' || CardId;
	ELSE
		RAISE DEBUG 'deleting a card from a standard class';
		EXECUTE 'UPDATE ' || TableId::regclass || ' SET "User" = ''' || coalesce(UserName,'') || ''', "Status" = ''N'' WHERE "Id" = ' || CardId;
	END IF;
END;
$$;



CREATE FUNCTION cm_delete_class(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF _cm_class_has_domains(TableId) THEN
		RAISE EXCEPTION 'CM_HAS_DOMAINS';
	ELSEIF _cm_class_has_children(TableId) THEN
		RAISE EXCEPTION 'CM_HAS_CHILDREN';
	ELSEIF NOT _cm_table_is_empty(TableId) THEN
		RAISE EXCEPTION 'CM_CONTAINS_DATA';
	END IF;

	PERFORM _cm_delete_local_attributes(TableId);

	-- Cascade for the history table
	EXECUTE 'DROP TABLE '|| TableId::regclass ||' CASCADE';
END;
$$;



CREATE FUNCTION cm_delete_class(cmclass text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_class(_cm_table_id($1));
$_$;



CREATE FUNCTION cm_delete_class_attribute(cmclass text, attributename text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_attribute(_cm_table_id($1), $2);
$_$;



CREATE FUNCTION cm_delete_domain(domainid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF NOT _cm_table_is_empty(DomainId) THEN
		RAISE EXCEPTION 'CM_CONTAINS_DATA';
	END IF;

	PERFORM _cm_delete_local_attributes(DomainId);

	EXECUTE 'DROP TABLE '|| DomainId::regclass ||' CASCADE';
END
$$;



CREATE FUNCTION cm_delete_domain(cmdomain text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_domain(_cm_domain_id($1));
$_$;



CREATE FUNCTION cm_delete_domain_attribute(cmclass text, attributename text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_attribute(_cm_domain_id($1), $2);
$_$;



CREATE FUNCTION cm_modify_attribute(tableid oid, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, newcomment text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	OldComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	IF COALESCE(_cm_read_reference_domain_comment(OldComment), '') IS DISTINCT FROM COALESCE(_cm_read_reference_domain_comment(NewComment), '')
		OR  _cm_read_reference_type_comment(OldComment) IS DISTINCT FROM _cm_read_reference_type_comment(NewComment)
		OR  COALESCE(_cm_get_fk_target_comment(OldComment), '') IS DISTINCT FROM COALESCE(_cm_get_fk_target_comment(NewComment), '')
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM _cm_check_attribute_comment_and_type(NewComment, SQLType);

	IF _cm_get_attribute_sqltype(TableId, AttributeName) <> trim(SQLType) THEN
		IF _cm_attribute_is_inherited(TableId, AttributeName) THEN
			RAISE NOTICE 'Not altering column type'; -- Fail silently
			--RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' TYPE '|| SQLType;
		END IF;
	END IF;

	PERFORM _cm_attribute_set_uniqueness(TableId, AttributeName, AttributeUnique);
	PERFORM _cm_attribute_set_notnull(TableId, AttributeName, AttributeNotNull);
	PERFORM _cm_set_attribute_default(TableId, AttributeName, AttributeDefault, FALSE);
	PERFORM _cm_set_attribute_comment(TableId, AttributeName, NewComment);
END;
$$;



CREATE FUNCTION cm_modify_attribute(tableid oid, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, commentparts text[], classes text[]) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	OldComment text := _cm_comment_for_attribute(TableId, AttributeName);
	-- creates full new comment string for consistency checks
	NewComment text := _cm_comment_add_parts(_cm_comment_set_parts(OldComment, commentparts), commentparts, true);
	subClassId oid;
	_classes text[] := COALESCE(classes, ARRAY[]::text[]);
BEGIN
	IF COALESCE(_cm_read_reference_domain_comment(OldComment), '') IS DISTINCT FROM COALESCE(_cm_read_reference_domain_comment(NewComment), '')
		OR  _cm_read_reference_type_comment(OldComment) IS DISTINCT FROM _cm_read_reference_type_comment(NewComment)
		OR  COALESCE(_cm_get_fk_target_comment(OldComment), '') IS DISTINCT FROM COALESCE(_cm_get_fk_target_comment(NewComment), '')
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM _cm_check_attribute_comment_and_type(NewComment, SQLType);

	IF _cm_get_attribute_sqltype(TableId, AttributeName) <> trim(SQLType) THEN
		IF _cm_attribute_is_inherited(TableId, AttributeName) THEN
			RAISE NOTICE 'Not altering column type'; -- Fail silently
			--RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' TYPE '|| SQLType;
		END IF;
	END IF;

	PERFORM _cm_attribute_set_uniqueness(TableId, AttributeName, AttributeUnique);
	PERFORM _cm_attribute_set_notnull(TableId, AttributeName, AttributeNotNull);
	PERFORM _cm_set_attribute_default(TableId, AttributeName, AttributeDefault, FALSE);
	-- updates comment according to specified tables (empty means all)
	FOR subClassId IN SELECT _cm_subtables_and_itself(tableid) LOOP
		IF (COALESCE(array_length(_classes, 1),0) = 0) OR (_classes @> ARRAY[replace(subClassId::regclass::text, '"', '')]) THEN
			OldComment = _cm_comment_for_attribute(subClassId, AttributeName);
			NewComment = _cm_comment_add_parts(_cm_comment_set_parts(OldComment, commentparts), commentparts, true);
			EXECUTE 'COMMENT ON COLUMN '|| subClassId::regclass ||'.'|| quote_ident(AttributeName) ||' IS '|| quote_literal(NewComment);
		END IF;		
	END LOOP;
END;
$$;



CREATE FUNCTION cm_modify_class(tableid oid, newcomment text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(TableId);
BEGIN
	IF _cm_is_superclass_comment(OldComment) <> _cm_is_superclass_comment(NewComment)
		OR _cm_get_type_comment(OldComment) <> _cm_get_type_comment(NewComment)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	EXECUTE 'COMMENT ON TABLE ' || TableId::regclass || ' IS ' || quote_literal(NewComment);
END;
$$;



CREATE FUNCTION cm_modify_class(cmclass text, newcomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_modify_class(_cm_table_id($1), $2);
$_$;



CREATE FUNCTION cm_modify_class_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_modify_attribute(_cm_table_id($1), $2, $3, $4, $5, $6, $7);
$_$;



CREATE FUNCTION cm_modify_domain(domainid oid, newcomment text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(DomainId);
BEGIN
	IF _cm_read_domain_cardinality(OldComment) <> _cm_read_domain_cardinality(NewComment)
		OR _cm_read_comment(OldComment, 'CLASS1') <> _cm_read_comment(NewComment, 'CLASS1')
		OR _cm_read_comment(OldComment, 'CLASS2') <> _cm_read_comment(NewComment, 'CLASS2')
		OR _cm_get_type_comment(OldComment) <> _cm_get_type_comment(NewComment)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- Check that the cardinality does not change
	EXECUTE 'COMMENT ON TABLE '|| DomainId::regclass || ' IS '|| quote_literal(NewComment);
END
$$;



CREATE FUNCTION cm_modify_domain(cmdomain text, domaincomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_modify_domain(_cm_domain_id($1), $2);
$_$;



CREATE FUNCTION cm_modify_domain_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_modify_attribute(_cm_domain_id($1), $2, $3, $4, $5, $6, $7);
$_$;



CREATE FUNCTION cmf_active_asset_for_brand(OUT "Brand" character varying, OUT "Number" integer) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY EXECUTE
        'SELECT coalesce("LookUp"."Description", ''N.D.'')::character varying AS "Brand", COUNT(*)::integer AS "CardCount"' ||
        '    FROM "Asset" ' ||
				'    LEFT OUTER JOIN "LookUp" on "LookUp"."Id" = "Asset"."Brand" and "LookUp"."Status" = ''A'' ' ||
        '    WHERE "Asset"."Status" = ''A'' ' ||
        '    GROUP BY "LookUp"."Description"' ||
        '    ORDER BY case when coalesce("LookUp"."Description", ''N.D.'') = ''N.D.'' then ''zz'' else "LookUp"."Description" end';
END
$$;



COMMENT ON FUNCTION cmf_active_asset_for_brand(OUT "Brand" character varying, OUT "Number" integer) IS 'TYPE: function';



CREATE FUNCTION cmf_active_cards_for_class("ClassName" character varying, OUT "Class" character varying, OUT "Number" integer) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $_$
BEGIN
RETURN QUERY EXECUTE
'SELECT _cmf_class_description("IdClass") AS "ClassDescription", COUNT(*)::integer
AS "CardCount"' ||
' FROM ' || quote_ident($1) ||
' WHERE "Status" = ' || quote_literal('A') ||
' AND _cmf_is_displayable("IdClass")' ||
' AND "IdClass" not IN (SELECT _cm_subtables_and_itself(_cm_table_id(' ||
quote_literal('Activity') || ')))'
' GROUP BY "IdClass"' ||
' ORDER BY "ClassDescription"';
END
$_$;



COMMENT ON FUNCTION cmf_active_cards_for_class("ClassName" character varying, OUT "Class" character varying, OUT "Number" integer) IS 'TYPE: function';



CREATE FUNCTION cmf_count_active_cards("ClassName" character varying, OUT "Count" integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
EXECUTE 'SELECT count(*) FROM ' || quote_ident("ClassName") || ' WHERE "Status" = ' ||
quote_literal('A') INTO "Count";
END
$$;



COMMENT ON FUNCTION cmf_count_active_cards("ClassName" character varying, OUT "Count" integer) IS 'TYPE: function';



CREATE FUNCTION cmf_open_rfc_for_status(OUT "Status" character varying, OUT "Number" integer) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY EXECUTE
        'SELECT coalesce("LookUp"."Description", ''N.D.'')::character varying AS "Status", COUNT(*)::integer AS "CardCount"' ||
        '    FROM "RequestForChange" ' ||
				'    LEFT OUTER JOIN "LookUp" on "LookUp"."Id" = "RequestForChange"."RFCStatus" and "LookUp"."Status" = ''A'' ' ||
        '    WHERE "RequestForChange"."Status" = ''A'' ' ||
        '    GROUP BY "LookUp"."Description"' ||
        '    ORDER BY case when coalesce("LookUp"."Description", ''N.D.'') = ''N.D.'' then ''zz'' else "LookUp"."Description" end';
END
$$;



COMMENT ON FUNCTION cmf_open_rfc_for_status(OUT "Status" character varying, OUT "Number" integer) IS 'TYPE: function';



CREATE FUNCTION "cmwf_getRFCNumber"(OUT "RFCNumber" integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
	select into "RFCNumber" coalesce(max("RequestNumber")+1,0) from "RequestForChange" where "Status"='A';
END
$$;



COMMENT ON FUNCTION "cmwf_getRFCNumber"(OUT "RFCNumber" integer) IS 'TYPE: function';



CREATE FUNCTION set_data_employee() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
  BEGIN
    NEW."Description" = coalesce(NEW."Surname", '') || ' ' || coalesce(NEW."Name", '');
    RETURN NEW;
  END;
$$;



CREATE FUNCTION set_data_suppliercontact() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
  BEGIN
    NEW."Description" = coalesce(NEW."Surname", '') || ' ' || coalesce(NEW."Name", '');
    RETURN NEW;
  END;
$$;



CREATE FUNCTION system_attribute_create(cmclass character varying, attributename character varying, denormalizedsqltype character varying, attributedefault character varying, attributenotnull boolean, attributeunique boolean, attributecomment character varying, attributereference character varying, attributereferencedomain character varying, attributereferencetype character varying, attributereferenceisdirect boolean) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    AttributeIndex integer;
    SQLType varchar;
BEGIN
	-- redundant parameters sanity check
	IF COALESCE(AttributeReferenceDomain,'') <> COALESCE(_cm_read_reference_domain_comment(AttributeComment),'')
		OR (COALESCE(_cm_read_reference_domain_comment(AttributeComment),'') <> '' AND
			(
			COALESCE(AttributeReferenceIsDirect,FALSE) <> COALESCE(_cm_read_comment(AttributeComment, 'REFERENCEDIRECT')::boolean,FALSE)
			OR COALESCE(AttributeReference,'') <> COALESCE(_cm_read_reference_target_comment(AttributeComment),'')
			OR COALESCE(AttributeReferenceType,'') <> COALESCE(_cm_read_comment(AttributeComment, 'REFERENCETYPE'),'')
			)
		)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	IF DenormalizedSQLType ILIKE 'bpchar%' THEN
		SQLType := 'bpchar(1)';
	ELSE
		SQLType := DenormalizedSQLType;
	END IF;

	PERFORM cm_create_class_attribute(CMClass, AttributeName, SQLType, AttributeDefault, AttributeNotNull, AttributeUnique, AttributeComment);

    SELECT CASE
	    	WHEN _cm_check_comment(AttributeComment,'MODE','reserved') THEN -1
			ELSE COALESCE(_cm_read_comment(AttributeComment, 'INDEX'),'0')::integer
		END INTO AttributeIndex;
    RETURN AttributeIndex;
END;
$$;



CREATE FUNCTION system_attribute_delete(cmclass character varying, attributename character varying) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
BEGIN
	PERFORM cm_delete_class_attribute(CMClass, AttributeName);
	RETURN TRUE;
END;
$$;



CREATE FUNCTION system_attribute_modify(cmclass text, attributename text, attributenewname text, denormalizedsqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    SQLType varchar;
BEGIN
	IF (AttributeName <> AttributeNewName) THEN 
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
    END IF;

   	IF DenormalizedSQLType ILIKE 'bpchar%' THEN
		SQLType := 'bpchar(1)';
	ELSE
		SQLType := DenormalizedSQLType;
	END IF;

	PERFORM cm_modify_class_attribute(CMClass, AttributeName, SQLType,
		AttributeDefault, AttributeNotNull, AttributeUnique, AttributeComment);
	RETURN TRUE;
END;
$$;



CREATE FUNCTION system_class_create(classname character varying, parentclass character varying, issuperclass boolean, classcomment character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
	-- consistency checks for wrong signatures
	IF IsSuperClass <> _cm_is_superclass_comment(ClassComment) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	RETURN cm_create_class(ClassName, ParentClass, ClassComment);
END;
$$;



CREATE FUNCTION system_class_delete(cmclass character varying) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_class($1);
$_$;



CREATE FUNCTION system_class_modify(classid integer, newclassname character varying, newissuperclass boolean, newclasscomment character varying) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF _cm_cmtable(ClassId) <> NewClassName
		OR _cm_is_superclass_comment(NewClassComment) <> NewIsSuperClass
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM cm_modify_class(ClassId::oid, NewClassComment);
	RETURN TRUE;
END;
$$;



CREATE FUNCTION system_domain_create(cmdomain text, domainclass1 text, domainclass2 text, domaincomment text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	TableName text := _cm_domain_cmname(CMDomain);
	HistoryTableName text := _cm_history_cmname(TableName);
    DomainId oid;
BEGIN
	-- TODO: Check DomainClass1 and DomainClass2

	RETURN cm_create_domain(CMDomain, DomainComment);
END
$$;



CREATE FUNCTION system_domain_delete(cmdomain text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_domain($1);
$_$;



CREATE FUNCTION system_domain_modify(domainid oid, domainname text, domainclass1 text, domainclass2 text, newcomment text) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(DomainId);
BEGIN
	-- TODO: Check DomainName, DomainClass1 and DomainClass2
	IF _cm_domain_id(DomainName) <> DomainId
		OR _cm_read_comment(NewComment, 'CLASS1') <> DomainClass1
		OR _cm_read_comment(NewComment, 'CLASS2') <> DomainClass2
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM cm_modify_domain(DomainId, NewComment);

	RETURN TRUE;
END;
$$;



CREATE AGGREGATE _cm_string_agg(anyelement) (
    SFUNC = array_append,
    STYPE = anyarray,
    INITCOND = '{}',
    FINALFUNC = public._cm_string_agg
);


SET default_tablespace = '';

SET default_with_oids = false;


CREATE TABLE "Class" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "Code" character varying(100),
    "Description" character varying(250),
    "Status" character(1),
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Notes" text
);



COMMENT ON TABLE "Class" IS 'MODE: baseclass|TYPE: class|DESCR: Class|SUPERCLASS: true|STATUS: active';



COMMENT ON COLUMN "Class"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Class"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Class"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



CREATE TABLE "Activity" (
    "FlowStatus" integer,
    "ActivityDefinitionId" character varying[],
    "ProcessCode" text,
    "NextExecutor" character varying[],
    "ActivityInstanceId" character varying[],
    "PrevExecutors" character varying[],
    "UniqueProcessDefinition" text
)
INHERITS ("Class");



COMMENT ON TABLE "Activity" IS 'MODE: baseclass|TYPE: class|DESCR: Attività|SUPERCLASS: true|MANAGER: activity|STATUS: active';



COMMENT ON COLUMN "Activity"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Activity"."IdClass" IS 'MODE: reserved|DESCR: Classe';



COMMENT ON COLUMN "Activity"."Code" IS 'MODE: read|DESCR: Activity Name|INDEX: 0|DATEEXPIRE: false|BASEDSP: true|STATUS: active';



COMMENT ON COLUMN "Activity"."Description" IS 'MODE: read|DESCR: Description|INDEX: 1|DATEEXPIRE: false|BASEDSP: true|STATUS: active';



COMMENT ON COLUMN "Activity"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Activity"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Activity"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Activity"."Notes" IS 'MODE: read|DESCR: Annotazioni';



COMMENT ON COLUMN "Activity"."FlowStatus" IS 'MODE: system|DESCR: Process Status|INDEX: 2|LOOKUP: FlowStatus';



COMMENT ON COLUMN "Activity"."ActivityDefinitionId" IS 'MODE: system|DESCR: Activity Definition Ids (for speed)';



COMMENT ON COLUMN "Activity"."ProcessCode" IS 'MODE: system|DESCR: Process Instance Id';



COMMENT ON COLUMN "Activity"."NextExecutor" IS 'MODE: system|DESCR: Activity Instance performers';



COMMENT ON COLUMN "Activity"."ActivityInstanceId" IS 'MODE: system|DESCR: Activity Instance Ids';



COMMENT ON COLUMN "Activity"."PrevExecutors" IS 'MODE: system|DESCR: Process Instance performers up to now';



COMMENT ON COLUMN "Activity"."UniqueProcessDefinition" IS 'MODE: system|DESCR: Unique Process Definition (for speed)';



CREATE TABLE "Asset" (
    "SerialNumber" character varying(40),
    "Supplier" integer,
    "PurchaseDate" date,
    "AcceptanceDate" date,
    "FinalCost" numeric(6,2),
    "Brand" integer,
    "Model" character varying(100),
    "Room" integer,
    "Assignee" integer,
    "TechnicalReference" integer,
    "Workplace" integer,
    "AcceptanceNotes" text
)
INHERITS ("Class");



COMMENT ON TABLE "Asset" IS 'DESCR: Asset|MODE: read|STATUS: active|SUPERCLASS: true|TYPE: class';



COMMENT ON COLUMN "Asset"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Asset"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Asset"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Asset"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Asset"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Asset"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Asset"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Asset"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Asset"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Asset"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Asset"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Asset"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Asset"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Asset"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



CREATE TABLE "Building" (
    "Address" character varying(100),
    "ZIP" character varying(5),
    "City" character varying(50),
    "Country" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Building" IS 'DESCR: Building|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Building"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Building"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Building"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Name|FIELDMODE: write|GROUP: |INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Building"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Building"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Building"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Building"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Building"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Building"."Address" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Address|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Building"."ZIP" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: ZIP|FIELDMODE: write|GROUP: |INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "Building"."City" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: City|FIELDMODE: write|GROUP: |INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Building"."Country" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Country|FIELDMODE: write|GROUP: |INDEX: 7|LOOKUP: Country|MODE: write|STATUS: active';



CREATE TABLE "Building_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Building");



CREATE TABLE "Computer" (
    "RAM" integer,
    "CPUNumber" integer,
    "CPUSpeed" numeric(5,3),
    "HDSize" integer,
    "IPAddress" inet
)
INHERITS ("Asset");



COMMENT ON TABLE "Computer" IS 'DESCR: Computer|MODE: read|STATUS: active|SUPERCLASS: true|TYPE: class';



COMMENT ON COLUMN "Computer"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Computer"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Computer"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Computer"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Computer"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Computer"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Computer"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Computer"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Computer"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."RAM" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: RAM|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."CPUNumber" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Number of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."CPUSpeed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Speed of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."HDSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Harddisk total size (GB)|FIELDMODE: write|GROUP: Technical data|INDEX: 18|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."IPAddress" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: IPAddress|FIELDMODE: write|GROUP: Technical data|INDEX: 19|MODE: write|STATUS: active';



CREATE TABLE "Email" (
    "EmailStatus" integer NOT NULL,
    "FromAddress" text,
    "ToAddresses" text,
    "CcAddresses" text,
    "Subject" text,
    "Content" text,
    "NotifyWith" text,
    "NoSubjectPrefix" boolean DEFAULT false,
    "Account" text,
    "BccAddresses" text,
    "Template" text,
    "KeepSynchronization" boolean,
    "PromptSynchronization" boolean,
    "Card" integer,
    "Delay" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Email" IS 'MODE: reserved|TYPE: class|DESCR: Email|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Email"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Email"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Email"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Email"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Email"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Email"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Email"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Email"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Email"."EmailStatus" IS 'MODE: read|FIELDMODE: write|DESCR: EmailStatus|INDEX: 5|BASEDSP: true|LOOKUP: EmailStatus|STATUS: active';



COMMENT ON COLUMN "Email"."FromAddress" IS 'MODE: read|FIELDMODE: write|DESCR: From|INDEX: 6|BASEDSP: true|STATUS: active';



COMMENT ON COLUMN "Email"."ToAddresses" IS 'MODE: read|FIELDMODE: write|DESCR: TO|INDEX: 7|BASEDSP: true|STATUS: active';



COMMENT ON COLUMN "Email"."CcAddresses" IS 'MODE: read|FIELDMODE: write|DESCR: CC|INDEX: 8|CLASSORDER: 0|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "Email"."Subject" IS 'MODE: read|FIELDMODE: write|DESCR: Subject|INDEX: 9|BASEDSP: true|STATUS: active';



COMMENT ON COLUMN "Email"."Content" IS 'MODE: read|FIELDMODE: write|DESCR: Body|INDEX: 10|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "Email"."NotifyWith" IS 'MODE: write|DESCR: NotifyWith|INDEX: 10|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "Email"."NoSubjectPrefix" IS 'MODE: write|DESCR: No subject prefix|INDEX: 12|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "Email"."Account" IS 'MODE: user|DESCR: Account|INDEX: 11|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "Email"."BccAddresses" IS 'MODE: user|FIELDMODE: write|DESCR: BCC|INDEX: 14|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "Email"."Template" IS 'MODE: user|FIELDMODE: write|DESCR: Template|INDEX: 15|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "Email"."KeepSynchronization" IS 'MODE: write|FIELDMODE: write|DESCR: Keep synchronization|INDEX: 16|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "Email"."PromptSynchronization" IS 'MODE: write|FIELDMODE: write|DESCR: Prompt synchronization|INDEX: 17|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "Email"."Card" IS 'MODE: read|FIELDMODE: write|DESCR: Card|INDEX: 4|REFERENCEDOM: ClassEmail|REFERENCEDIRECT: false|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Email"."Delay" IS 'MODE: user|FIELDMODE: write|DESCR: Delay|INDEX: 18|STATUS: active';



CREATE TABLE "Email_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Email");



CREATE TABLE "Employee" (
    "Surname" character varying(50),
    "Name" character varying(50),
    "Type" integer,
    "Qualification" integer,
    "Level" integer,
    "Email" character varying(50),
    "Office" integer,
    "Phone" character varying(20),
    "Mobile" character varying(20),
    "Fax" character varying(20),
    "State" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Employee" IS 'DESCR: Employee|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Employee"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Employee"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Employee"."Code" IS 'BASEDSP: true|CLASSORDER: 1|DESCR: Number|FIELDMODE: write|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Employee"."Description" IS 'BASEDSP: true|CLASSORDER: -2|DESCR: Nominative|FIELDMODE: hidden|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Employee"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Employee"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Employee"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Employee"."Notes" IS 'BASEDSP: false|CLASSORDER: -3|DESCR: Notes|FIELDMODE: write|INDEX: 3|MODE: read|STATUS: active';



COMMENT ON COLUMN "Employee"."Surname" IS 'BASEDSP: true|CLASSORDER: -4|DESCR: Surname|FIELDMODE: write|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Name" IS 'BASEDSP: true|CLASSORDER: -5|DESCR: Name|FIELDMODE: write|INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Type" IS 'BASEDSP: true|CLASSORDER: -6|DESCR: Type|FIELDMODE: write|INDEX: 6|LOOKUP: Employee type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Qualification" IS 'BASEDSP: true|CLASSORDER: -7|DESCR: Qualification|FIELDMODE: write|INDEX: 7|LOOKUP: Employee qualification|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Level" IS 'BASEDSP: false|CLASSORDER: -8|DESCR: Level|FIELDMODE: write|INDEX: 8|LOOKUP: Employee level|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Email" IS 'BASEDSP: true|CLASSORDER: -9|DESCR: Email|FIELDMODE: write|INDEX: 9|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Office" IS 'BASEDSP: true|CLASSORDER: -10|DESCR: Office|FIELDMODE: write|INDEX: 10|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: Members|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Employee"."Phone" IS 'BASEDSP: true|CLASSORDER: -11|DESCR: Phone|FIELDMODE: write|INDEX: 11|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Mobile" IS 'BASEDSP: false|CLASSORDER: -12|DESCR: Mobile|FIELDMODE: write|INDEX: 12|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Fax" IS 'BASEDSP: false|CLASSORDER: -13|DESCR: Fax|FIELDMODE: write|INDEX: 13|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."State" IS 'BASEDSP: true|CLASSORDER: -14|DESCR: State|FIELDMODE: write|INDEX: 14|LOOKUP: Employee state|MODE: write|STATUS: active';



CREATE TABLE "Employee_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Employee");



CREATE TABLE "Floor" (
    "Building" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Floor" IS 'DESCR: Floor|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Floor"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Floor"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Floor"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Floor"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Floor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Floor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Floor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Floor"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Floor"."Building" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Building|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: BuildingFloor|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "Floor_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Floor");



CREATE TABLE "Grant" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Code" character varying(100),
    "Description" character varying(250),
    "Status" character(1),
    "Notes" text,
    "IdRole" integer NOT NULL,
    "IdGrantedClass" regclass,
    "Mode" character varying(1) NOT NULL,
    "Type" character varying(70) NOT NULL,
    "IdPrivilegedObject" integer,
    "PrivilegeFilter" text,
    "AttributesPrivileges" character varying[],
    "UI_EnabledCardEditMode" text
);



COMMENT ON TABLE "Grant" IS 'MODE: reserved|TYPE: simpleclass|DESCR: Privileges |SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "Grant"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Grant"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Grant"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Grant"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "Grant"."Code" IS 'MODE: read|DESCR: Code|BASEDSP: true|INDEX: 1';



COMMENT ON COLUMN "Grant"."Description" IS 'MODE: read|DESCR: Description|BASEDSP: true|INDEX: 2';



COMMENT ON COLUMN "Grant"."Status" IS 'MODE: read|INDEX: 3';



COMMENT ON COLUMN "Grant"."Notes" IS 'MODE: read|DESCR: Annotazioni|INDEX: 4';



COMMENT ON COLUMN "Grant"."IdRole" IS 'MODE: read|DESCR: RoleId|INDEX: 5|STATUS: active';



COMMENT ON COLUMN "Grant"."IdGrantedClass" IS 'MODE: read|DESCR: granted class|INDEX: 6|STATUS: active';



COMMENT ON COLUMN "Grant"."Mode" IS 'MODE: read|DESCR: mode|INDEX: 7|STATUS: active';



COMMENT ON COLUMN "Grant"."Type" IS 'MODE: read|DESCR: type of grant|INDEX: 8|STATUS: active';



COMMENT ON COLUMN "Grant"."IdPrivilegedObject" IS 'MODE: read|DESCR: id of privileged object|INDEX: 9|STATUS: active';



COMMENT ON COLUMN "Grant"."PrivilegeFilter" IS 'MODE: read|DESCR: filter for row privileges|INDEX: 10|STATUS: active';



COMMENT ON COLUMN "Grant"."AttributesPrivileges" IS 'MODE: read|DESCR: disabled attributes for column privileges|INDEX: 11|STATUS: active';



COMMENT ON COLUMN "Grant"."UI_EnabledCardEditMode" IS 'MODE: write|DESCR: UI_EnabledCardEditMode|INDEX: 12|BASEDSP: false|STATUS: active';



CREATE TABLE "Invoice" (
    "TotalAmount" numeric(6,2),
    "Type" integer,
    "Supplier" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Invoice" IS 'DESCR: Invoice|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Invoice"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Invoice"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Invoice"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Number|FIELDMODE: write|GROUP: |INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Invoice"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Invoice"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Invoice"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Invoice"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Invoice"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Invoice"."TotalAmount" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Total amount|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Invoice"."Type" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Type|FIELDMODE: write|GROUP: |INDEX: 5|LOOKUP: Invoice type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Invoice"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: |INDEX: 6|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierInvoice|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "Invoice_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Invoice");



CREATE TABLE "License" (
    "Category" integer,
    "Version" character varying(20)
)
INHERITS ("Asset");



COMMENT ON TABLE "License" IS 'DESCR: License|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "License"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "License"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "License"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "License"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "License"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "License"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "License"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "License"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "License"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "License"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "License"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "License"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "License"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "License"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Category" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Category|FIELDMODE: write|GROUP: Technical data|INDEX: 15|LOOKUP: License category|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Version" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Version|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



CREATE TABLE "License_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("License");



CREATE TABLE "LookUp" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Code" character varying(100),
    "Description" character varying(250),
    "Status" character(1),
    "Notes" text,
    "Type" character varying(64),
    "ParentType" character varying(64),
    "ParentId" integer,
    "Number" integer NOT NULL,
    "IsDefault" boolean,
    "TranslationUuid" text
);



COMMENT ON TABLE "LookUp" IS 'MODE: reserved|TYPE: simpleclass|DESCR: Lookup list|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "LookUp"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "LookUp"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "LookUp"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "LookUp"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "LookUp"."Code" IS 'MODE: read|DESCR: Code|BASEDSP: true';



COMMENT ON COLUMN "LookUp"."Description" IS 'MODE: read|DESCR: Description|BASEDSP: true';



COMMENT ON COLUMN "LookUp"."Status" IS 'MODE: read';



COMMENT ON COLUMN "LookUp"."Notes" IS 'MODE: read|DESCR: Annotazioni';



COMMENT ON COLUMN "LookUp"."Type" IS 'MODE: read';



COMMENT ON COLUMN "LookUp"."ParentType" IS 'MODE: read';



COMMENT ON COLUMN "LookUp"."ParentId" IS 'MODE: read';



COMMENT ON COLUMN "LookUp"."Number" IS 'MODE: read';



COMMENT ON COLUMN "LookUp"."IsDefault" IS 'MODE: read';



COMMENT ON COLUMN "LookUp"."TranslationUuid" IS 'MODE: write|DESCR: Translations Uuid|STATUS: active';



CREATE TABLE "Map" (
    "IdDomain" regclass NOT NULL,
    "IdClass1" regclass NOT NULL,
    "IdObj1" integer NOT NULL,
    "IdClass2" regclass NOT NULL,
    "IdObj2" integer NOT NULL,
    "Status" character(1),
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "EndDate" timestamp without time zone,
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL
);



COMMENT ON TABLE "Map" IS 'MODE: baseclass|TYPE: domain|DESCRDIR: è in relazione con|DESCRINV: è in relazione con|STATUS: active';



COMMENT ON COLUMN "Map"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_AccountTemplate" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_AccountTemplate" IS 'MODE: reserved|TYPE: domain|CLASS1: _EmailAccount|CLASS2: _EmailTemplate|DESCRDIR: is default|DESCRINV: has default|CARDIN: 1:N|STATUS: active';



COMMENT ON COLUMN "Map_AccountTemplate"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AccountTemplate"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AccountTemplate"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AccountTemplate"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AccountTemplate"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AccountTemplate"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AccountTemplate"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AccountTemplate"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AccountTemplate"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AccountTemplate"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_AccountTemplate_history" (
)
INHERITS ("Map_AccountTemplate");
ALTER TABLE ONLY "Map_AccountTemplate_history" ALTER COLUMN "EndDate" SET NOT NULL;



CREATE TABLE "Map_AssetAssignee" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_AssetAssignee" IS 'CARDIN: 1:N|CLASS1: Employee|CLASS2: Asset|DESCRDIR: has in assignment|DESCRINV: assigned to|LABEL: Asset assignee|MASTERDETAIL: true|MDLABEL: Asset|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_AssetAssignee"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_AssetAssignee_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_AssetAssignee");



CREATE TABLE "Map_AssetReference" (
    "Role" integer
)
INHERITS ("Map");



COMMENT ON TABLE "Map_AssetReference" IS 'CARDIN: 1:N|CLASS1: Employee|CLASS2: Asset|DESCRDIR: technical reference for assets|DESCRINV: has technical reference|LABEL: Asset reference|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_AssetReference"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."Role" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Role|FIELDMODE: write|INDEX: 1|LOOKUP: Technical reference role|MODE: write|STATUS: active';



CREATE TABLE "Map_AssetReference_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_AssetReference");



CREATE TABLE "Map_BuildingFloor" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_BuildingFloor" IS 'CARDIN: 1:N|CLASS1: Building|CLASS2: Floor|DESCRDIR: includes floors|DESCRINV: belongs to building|LABEL: Building floor|MASTERDETAIL: true|MDLABEL: Floor|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_BuildingFloor"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_BuildingFloor_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_BuildingFloor");



CREATE TABLE "Map_ClassEmail" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_ClassEmail" IS 'MODE: reserved|TYPE: domain|CLASS1: Class|CLASS2: Email|DESCRDIR: |DESCRINV: |CARDIN: 1:N|STATUS: active';



COMMENT ON COLUMN "Map_ClassEmail"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassEmail"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassEmail"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassEmail"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassEmail"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassEmail"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassEmail"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassEmail"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassEmail"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassEmail"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_ClassEmail_history" (
)
INHERITS ("Map_ClassEmail");
ALTER TABLE ONLY "Map_ClassEmail_history" ALTER COLUMN "EndDate" SET NOT NULL;



CREATE TABLE "Map_ClassMetadata" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_ClassMetadata" IS 'MODE: reserved|TYPE: domain|CLASS1: Class|CLASS2: Metadata|DESCRDIR: |DESCRINV: |CARDIN: 1:N|STATUS: active';



COMMENT ON COLUMN "Map_ClassMetadata"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassMetadata"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassMetadata"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassMetadata"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassMetadata"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassMetadata"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassMetadata"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassMetadata"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassMetadata"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ClassMetadata"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_ClassMetadata_history" (
)
INHERITS ("Map_ClassMetadata");
ALTER TABLE ONLY "Map_ClassMetadata_history" ALTER COLUMN "EndDate" SET NOT NULL;



CREATE TABLE "Map_FilterRole" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_FilterRole" IS 'MODE: reserved|TYPE: domain|CLASS1: _Filter|CLASS2: Role|DESCRDIR: |DESCRINV: |CARDIN: N:N|STATUS: active';



COMMENT ON COLUMN "Map_FilterRole"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FilterRole"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FilterRole"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FilterRole"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FilterRole"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FilterRole"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FilterRole"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FilterRole"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FilterRole"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FilterRole"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_FilterRole_history" (
)
INHERITS ("Map_FilterRole");
ALTER TABLE ONLY "Map_FilterRole_history" ALTER COLUMN "EndDate" SET NOT NULL;



CREATE TABLE "Map_FloorRoom" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_FloorRoom" IS 'CARDIN: 1:N|CLASS1: Floor|CLASS2: Room|DESCRDIR: includes rooms|DESCRINV: belongs to floor|LABEL: Floor room|MASTERDETAIL: true|MDLABEL: Room|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_FloorRoom"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_FloorRoom_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_FloorRoom");



CREATE TABLE "Map_Members" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_Members" IS 'CARDIN: 1:N|CLASS1: Office|CLASS2: Employee|DESCRDIR: includes|DESCRINV: is member of|LABEL: Members|MASTERDETAIL: true|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_Members"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_Members_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_Members");



CREATE TABLE "Map_NetworkDeviceConnection" (
    "PortNumber" integer,
    "CableColor" integer
)
INHERITS ("Map");



COMMENT ON TABLE "Map_NetworkDeviceConnection" IS 'CARDIN: N:N|CLASS1: NetworkDevice|CLASS2: NetworkDevice|DESCRDIR: connected to|DESCRINV: connected to|LABEL: Network device connection|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."PortNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Port number|FIELDMODE: write|INDEX: 1|MODE: write|STATUS: active';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."CableColor" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Cable color|FIELDMODE: write|INDEX: 2|LOOKUP: Cable color|MODE: write|STATUS: active';



CREATE TABLE "Map_NetworkDeviceConnection_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_NetworkDeviceConnection");



CREATE TABLE "Map_OfficeRoom" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_OfficeRoom" IS 'CARDIN: 1:N|CLASS1: Office|CLASS2: Room|DESCRDIR: uses rooms|DESCRINV: used by office|LABEL: Office room|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_OfficeRoom"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_OfficeRoom_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_OfficeRoom");



CREATE TABLE "Map_RFCChangeManager" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RFCChangeManager" IS 'CARDIN: N:1|CLASS1: RequestForChange|CLASS2: Employee|DESCRDIR: has change manager|DESCRINV: change manager for|LABEL: RFCChangeManager|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RFCChangeManager"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RFCChangeManager_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RFCChangeManager");



CREATE TABLE "Map_RFCExecutor" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RFCExecutor" IS 'CARDIN: N:1|CLASS1: RequestForChange|CLASS2: Employee|DESCRDIR: Executed by|DESCRINV: Perform|LABEL: RFC Executor|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RFCExecutor"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RFCExecutor_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RFCExecutor");



CREATE TABLE "Map_RFCRequester" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RFCRequester" IS 'CARDIN: N:1|CLASS1: RequestForChange|CLASS2: Employee|DESCRDIR: Requested by|DESCRINV: Requests|LABEL: RFC Requester|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RFCRequester"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RFCRequester_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RFCRequester");



CREATE TABLE "Map_RoomAsset" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RoomAsset" IS 'CARDIN: 1:N|CLASS1: Room|CLASS2: Asset|DESCRDIR: contains assets|DESCRINV: located in room|LABEL: Room asset|MASTERDETAIL: true|MDLABEL: Asset|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RoomAsset"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RoomAsset_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RoomAsset");



CREATE TABLE "Map_RoomNetworkPoint" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RoomNetworkPoint" IS 'CARDIN: 1:N|CLASS1: Room|CLASS2: NetworkPoint|DESCRDIR: contains network points|DESCRINV: located in room|LABEL: Room network point|MASTERDETAIL: true|MDLABEL: Network Points|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RoomNetworkPoint_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RoomNetworkPoint");



CREATE TABLE "Map_RoomWorkplace" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RoomWorkplace" IS 'CARDIN: 1:N|CLASS1: Room|CLASS2: Workplace|DESCRDIR: contains workplaces|DESCRINV: located in room|LABEL: Room workplace|MASTERDETAIL: true|MDLABEL: Workplace|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RoomWorkplace"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RoomWorkplace_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RoomWorkplace");



CREATE TABLE "Map_Supervisor" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_Supervisor" IS 'CARDIN: 1:N|CLASS1: Employee|CLASS2: Office|DESCRDIR: supervisor of|DESCRINV: has supervisor|LABEL: Supervisor|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_Supervisor"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_Supervisor_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_Supervisor");



CREATE TABLE "Map_SupplierAsset" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_SupplierAsset" IS 'CARDIN: 1:N|CLASS1: Supplier|CLASS2: Asset|DESCRDIR: provided assets|DESCRINV: provided by supplier|LABEL: Supplier asset|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_SupplierAsset"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_SupplierAsset_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_SupplierAsset");



CREATE TABLE "Map_SupplierContact" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_SupplierContact" IS 'CARDIN: 1:N|CLASS1: Supplier|CLASS2: SupplierContact|DESCRDIR: has contacts|DESCRINV: belongs to supplier|LABEL: Supplier contact|MASTERDETAIL: true|MDLABEL: SupplierContact|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_SupplierContact"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_SupplierContact_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_SupplierContact");



CREATE TABLE "Map_SupplierInvoice" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_SupplierInvoice" IS 'CARDIN: 1:N|CLASS1: Supplier|CLASS2: Invoice|DESCRDIR: invoices delivered|DESCRINV: delivered by supplier|LABEL: Supplier invoice|MASTERDETAIL: true|MDLABEL: Invoice|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_SupplierInvoice"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_SupplierInvoice_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_SupplierInvoice");



CREATE TABLE "Map_UserRole" (
    "DefaultGroup" boolean
)
INHERITS ("Map");



COMMENT ON TABLE "Map_UserRole" IS 'MODE: system|TYPE: domain|CLASS1: User|CLASS2: Role|DESCRDIR: has role|DESCRINV: contains|CARDIN: N:N|STATUS: active';



COMMENT ON COLUMN "Map_UserRole"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."DefaultGroup" IS 'MODE: read|FIELDMODE: write|DESCR: Default Group|INDEX: 1|BASEDSP: true|STATUS: active';



CREATE TABLE "Map_UserRole_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_UserRole");



CREATE TABLE "Map_WorkplaceComposition" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_WorkplaceComposition" IS 'CARDIN: 1:N|CLASS1: Workplace|CLASS2: Asset|DESCRDIR: includes assets|DESCRINV: belongs to workplace|LABEL: Workplace composition|MASTERDETAIL: true|MDLABEL: Asset|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_WorkplaceComposition"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_WorkplaceComposition_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_WorkplaceComposition");



CREATE TABLE "Menu" (
    "IdParent" integer DEFAULT 0,
    "IdElementClass" regclass,
    "IdElementObj" integer DEFAULT 0 NOT NULL,
    "Number" integer DEFAULT 0 NOT NULL,
    "Type" character varying(70) NOT NULL,
    "GroupName" text NOT NULL
)
INHERITS ("Class");



COMMENT ON TABLE "Menu" IS 'MODE: reserved|TYPE: class|DESCR: Menu|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "Menu"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Menu"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Menu"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Menu"."IdParent" IS 'MODE: read|DESCR: Parent Item, 0 means no parent';



COMMENT ON COLUMN "Menu"."IdElementClass" IS 'MODE: read|DESCR: Class connect to this item';



COMMENT ON COLUMN "Menu"."IdElementObj" IS 'MODE: read|DESCR: Object connected to this item, 0 means no object';



COMMENT ON COLUMN "Menu"."Number" IS 'MODE: read|DESCR: Ordering';



COMMENT ON COLUMN "Menu"."Type" IS 'MODE: read';



COMMENT ON COLUMN "Menu"."GroupName" IS 'MODE: read';



CREATE TABLE "Menu_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Menu");



CREATE TABLE "Metadata" (
)
INHERITS ("Class");



COMMENT ON TABLE "Metadata" IS 'MODE: reserved|TYPE: class|DESCR: Metadata|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "Metadata"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Metadata"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Metadata"."Code" IS 'MODE: read|DESCR: Schema|INDEX: 1';



COMMENT ON COLUMN "Metadata"."Description" IS 'MODE: read|DESCR: Key|INDEX: 2';



COMMENT ON COLUMN "Metadata"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Metadata"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Metadata"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Metadata"."Notes" IS 'MODE: read|DESCR: Value|INDEX: 3';



CREATE TABLE "Metadata_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Metadata");



CREATE TABLE "Monitor" (
    "Type" integer,
    "ScreenSize" integer
)
INHERITS ("Asset");



COMMENT ON TABLE "Monitor" IS 'DESCR: Monitor|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Monitor"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Monitor"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Monitor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Monitor"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Monitor"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Monitor"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Monitor"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Monitor"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Monitor"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."Type" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Type|FIELDMODE: write|GROUP: Technical data|INDEX: 15|LOOKUP: Monitor type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."ScreenSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: ScreenSize|FIELDMODE: write|GROUP: Technical data|INDEX: 16|LOOKUP: Screen size|MODE: write|STATUS: active';



CREATE TABLE "Monitor_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Monitor");



CREATE TABLE "NetworkDevice" (
    "Type" integer,
    "PortNumber" integer,
    "PortSpeed" integer
)
INHERITS ("Asset");



COMMENT ON TABLE "NetworkDevice" IS 'DESCR: Network device|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "NetworkDevice"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkDevice"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkDevice"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkDevice"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkDevice"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkDevice"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "NetworkDevice"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Type" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Type|FIELDMODE: write|GROUP: Technical data|INDEX: 15|LOOKUP: Network device type|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."PortNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Port number|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."PortSpeed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Port speed (Mb)|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



CREATE TABLE "NetworkDevice_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("NetworkDevice");



CREATE TABLE "NetworkPoint" (
    "Room" integer
)
INHERITS ("Class");



COMMENT ON TABLE "NetworkPoint" IS 'DESCR: Network point|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "NetworkPoint"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkPoint"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkPoint"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "NetworkPoint"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "NetworkPoint"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkPoint"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkPoint"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkPoint"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "NetworkPoint"."Room" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomNetworkPoint|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "NetworkPoint_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("NetworkPoint");



CREATE TABLE "Notebook" (
    "ScreenSize" integer
)
INHERITS ("Computer");



COMMENT ON TABLE "Notebook" IS 'DESCR: Notebook|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Notebook"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Notebook"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Notebook"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Notebook"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Notebook"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Notebook"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Notebook"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Notebook"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Notebook"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Notebook"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Notebook"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Notebook"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Notebook"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Notebook"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."RAM" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: RAM|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."CPUNumber" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Number of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."CPUSpeed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Speed of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."HDSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Harddisk total size (GB)|FIELDMODE: write|GROUP: Technical data|INDEX: 18|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."IPAddress" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: IPAddress|FIELDMODE: write|GROUP: Technical data|INDEX: 19|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."ScreenSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Screen size|FIELDMODE: write|GROUP: Technical data|INDEX: 19|LOOKUP: Screen size|MODE: write|STATUS: active';



CREATE TABLE "Notebook_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Notebook");



CREATE TABLE "Office" (
    "ShortDescription" character varying(100),
    "Supervisor" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Office" IS 'DESCR: Office|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Office"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Office"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Office"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Office"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Office"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Office"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Office"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Office"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Office"."ShortDescription" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Short description|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Office"."Supervisor" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supervisor|FIELDMODE: write|GROUP: |INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: Supervisor|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "Office_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Office");



CREATE TABLE "PC" (
    "SoundCard" character varying(50),
    "VideoCard" character varying(50)
)
INHERITS ("Computer");



COMMENT ON TABLE "PC" IS 'DESCR: PC|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "PC"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "PC"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "PC"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "PC"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "PC"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "PC"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "PC"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "PC"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "PC"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 3|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 4|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "PC"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 8|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 9|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 10|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "PC"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "PC"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "PC"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "PC"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 14|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."RAM" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: RAM|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."CPUNumber" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Number of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."CPUSpeed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Speed of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."HDSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Harddisk total size (GB)|FIELDMODE: write|GROUP: Technical data|INDEX: 18|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."IPAddress" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: IPAddress|FIELDMODE: write|GROUP: Technical data|INDEX: 19|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."SoundCard" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Sound card|FIELDMODE: write|GROUP: Technical data|INDEX: 20|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."VideoCard" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Video card|FIELDMODE: write|GROUP: Technical data|INDEX: 21|MODE: write|STATUS: active';



CREATE TABLE "PC_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("PC");



CREATE TABLE "Patch" (
    "Category" text
)
INHERITS ("Class");



COMMENT ON TABLE "Patch" IS 'DESCR: |MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Patch"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Patch"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Patch"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Patch"."Category" IS 'STATUS: active|BASEDSP: false|CLASSORDER: 0|DESCR: Category|GROUP: |INDEX: 4|MODE: write|FIELDMODE: write';



CREATE TABLE "Patch_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Patch");



CREATE TABLE "Printer" (
    "Type" integer,
    "PaperSize" integer,
    "Color" boolean,
    "Usage" integer
)
INHERITS ("Asset");



COMMENT ON TABLE "Printer" IS 'DESCR: Printer|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Printer"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Printer"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Printer"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Printer"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Printer"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Printer"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Printer"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Printer"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Printer"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Printer"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Printer"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Printer"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Printer"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Printer"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Type" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Type|FIELDMODE: write|GROUP: Technical data|INDEX: 15|LOOKUP: Printer type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."PaperSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Paper size|FIELDMODE: write|GROUP: Technical data|INDEX: 16|LOOKUP: Paper size|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Color" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Color|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Usage" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Usage|FIELDMODE: write|GROUP: Technical data|INDEX: 18|LOOKUP: Printer usage|MODE: write|STATUS: active';



CREATE TABLE "Printer_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Printer");



CREATE TABLE "Rack" (
    "UnitNumber" integer,
    "Depth" integer
)
INHERITS ("Asset");



COMMENT ON TABLE "Rack" IS 'DESCR: Rack|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Rack"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Rack"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Rack"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Rack"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Rack"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Rack"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Rack"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Rack"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Rack"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Rack"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Rack"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Rack"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Rack"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Rack"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."UnitNumber" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Unit number|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."Depth" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Depth (cm)|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



CREATE TABLE "Rack_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Rack");



CREATE TABLE "Report" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "Code" character varying(40),
    "Description" character varying(100),
    "Status" character varying(1),
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Type" character varying(20),
    "Query" text,
    "SimpleReport" bytea,
    "RichReport" bytea,
    "Wizard" bytea,
    "Images" bytea,
    "ImagesLength" integer[],
    "ReportLength" integer[],
    "IdClass" regclass,
    "ImagesName" character varying[],
    "Groups" character varying[]
);



COMMENT ON TABLE "Report" IS 'MODE: reserved|TYPE: class|DESCR: Report|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "Report"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."Code" IS 'MODE: read|DESCR: Codice';



COMMENT ON COLUMN "Report"."Description" IS 'MODE: read|DESCR: Descrizione';



COMMENT ON COLUMN "Report"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."Type" IS 'MODE: read|DESCR: Tipo';



COMMENT ON COLUMN "Report"."Query" IS 'MODE: read|DESCR: Query';



COMMENT ON COLUMN "Report"."SimpleReport" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."RichReport" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."Wizard" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."Images" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."ImagesLength" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."ReportLength" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."ImagesName" IS 'MODE: read';



COMMENT ON COLUMN "Report"."Groups" IS 'MODE: read';



CREATE TABLE "RequestForChange" (
    "Requester" integer,
    "RFCStartDate" timestamp without time zone,
    "RequestNumber" integer,
    "RFCStatus" integer,
    "RFCDescription" text,
    "Category" integer,
    "FormalEvaluation" integer,
    "RFCPriority" integer,
    "ImpactAnalysisRequest" boolean,
    "CostAnalysisRequest" boolean,
    "RiskAnalysisRequest" boolean,
    "ImpactAnalysisResult" text,
    "CostAnalysisResult" text,
    "RiskAnalysisResult" text,
    "Decision" integer,
    "PlannedActions" text,
    "ExecutionStartDate" timestamp without time zone,
    "ActionsPerformed" text,
    "ExecutionEndDate" timestamp without time zone,
    "FinalResult" integer,
    "RFCEndDate" timestamp without time zone
)
INHERITS ("Activity");



COMMENT ON TABLE "RequestForChange" IS 'DESCR: Request for change|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class|USERSTOPPABLE: false';



COMMENT ON COLUMN "RequestForChange"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "RequestForChange"."IdClass" IS 'MODE: reserved|DESCR: Classe';



COMMENT ON COLUMN "RequestForChange"."Code" IS 'MODE: read|DESCR: Nome Attività|INDEX: 0|DATEEXPIRE: false|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."Description" IS 'MODE: read|DESCR: Description|INDEX: 1|DATEEXPIRE: false|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "RequestForChange"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "RequestForChange"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "RequestForChange"."Notes" IS 'MODE: read|DESCR: Annotazioni';



COMMENT ON COLUMN "RequestForChange"."FlowStatus" IS 'MODE: system|DESCR: Process Status|INDEX: 2|LOOKUP: FlowStatus';



COMMENT ON COLUMN "RequestForChange"."ActivityDefinitionId" IS 'MODE: system|DESCR: Activity Definition Ids (for speed)';



COMMENT ON COLUMN "RequestForChange"."ProcessCode" IS 'MODE: system|DESCR: Process Instance Id';



COMMENT ON COLUMN "RequestForChange"."NextExecutor" IS 'MODE: system|DESCR: Activity Instance performers';



COMMENT ON COLUMN "RequestForChange"."ActivityInstanceId" IS 'MODE: system|DESCR: Activity Instance Ids';



COMMENT ON COLUMN "RequestForChange"."PrevExecutors" IS 'MODE: system|DESCR: Process Instance performers up to now';



COMMENT ON COLUMN "RequestForChange"."UniqueProcessDefinition" IS 'MODE: system|DESCR: Unique Process Definition (for speed)';



COMMENT ON COLUMN "RequestForChange"."Requester" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Requester|FIELDMODE: write|INDEX: 24|MODE: write|REFERENCEDIRECT: true|REFERENCEDOM: RFCRequester|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RFCStartDate" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Start date|FIELDMODE: write|INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RequestNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Request number|FIELDMODE: write|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RFCStatus" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Status|FIELDMODE: write|INDEX: 6|LOOKUP: RFC status|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RFCDescription" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Description|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."Category" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Category|FIELDMODE: write|INDEX: 8|LOOKUP: RFC Category|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."FormalEvaluation" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Formal evaluation|FIELDMODE: write|INDEX: 9|LOOKUP: RFC formal evaluation|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RFCPriority" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Priority|FIELDMODE: write|INDEX: 25|LOOKUP: RFC priority|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."ImpactAnalysisRequest" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Impact analysis request|FIELDMODE: write|INDEX: 11|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."CostAnalysisRequest" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Cost analysis request|FIELDMODE: write|INDEX: 12|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RiskAnalysisRequest" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Risk analysis request|FIELDMODE: write|INDEX: 13|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."ImpactAnalysisResult" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Impact analysis result|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 14|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."CostAnalysisResult" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Cost analysis result|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RiskAnalysisResult" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Risk analysis result|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."Decision" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Decision|FIELDMODE: write|INDEX: 17|LOOKUP: RFC decision|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."PlannedActions" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Planned actions|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 18|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."ExecutionStartDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Execution start date|FIELDMODE: write|INDEX: 19|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."ActionsPerformed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Actions performed|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 20|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."ExecutionEndDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Execution end date|FIELDMODE: write|INDEX: 21|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."FinalResult" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Final result|FIELDMODE: write|INDEX: 22|LOOKUP: RFC final result|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RFCEndDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: End date|FIELDMODE: write|INDEX: 23|MODE: write|STATUS: active';



CREATE TABLE "RequestForChange_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("RequestForChange");



CREATE TABLE "Role" (
    "Administrator" boolean,
    "startingClass" regclass,
    "Email" character varying(320),
    "DisabledModules" character varying[],
    "DisabledCardTabs" character varying[],
    "DisabledProcessTabs" character varying[],
    "HideSidePanel" boolean DEFAULT false NOT NULL,
    "FullScreenMode" boolean DEFAULT false NOT NULL,
    "SimpleHistoryModeForCard" boolean DEFAULT false NOT NULL,
    "SimpleHistoryModeForProcess" boolean DEFAULT false NOT NULL,
    "ProcessWidgetAlwaysEnabled" boolean DEFAULT false NOT NULL,
    "CloudAdmin" boolean DEFAULT false NOT NULL,
    "Active" boolean DEFAULT true NOT NULL
)
INHERITS ("Class");



COMMENT ON TABLE "Role" IS 'MODE: sysread|TYPE: class|DESCR: Groups|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "Role"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Role"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Role"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Role"."Administrator" IS 'MODE: read|DESCR: Administrator|INDEX: 5|STATUS: active';



COMMENT ON COLUMN "Role"."startingClass" IS 'MODE: read|DESCR: Starting Class|INDEX: 6|STATUS: active';



COMMENT ON COLUMN "Role"."Email" IS 'MODE: read|DESCR: Email|INDEX: 7';



COMMENT ON COLUMN "Role"."DisabledModules" IS 'MODE: read';



COMMENT ON COLUMN "Role"."DisabledCardTabs" IS 'MODE: read';



COMMENT ON COLUMN "Role"."DisabledProcessTabs" IS 'MODE: read';



COMMENT ON COLUMN "Role"."HideSidePanel" IS 'MODE: read';



COMMENT ON COLUMN "Role"."FullScreenMode" IS 'MODE: read';



COMMENT ON COLUMN "Role"."SimpleHistoryModeForCard" IS 'MODE: read';



COMMENT ON COLUMN "Role"."SimpleHistoryModeForProcess" IS 'MODE: read';



COMMENT ON COLUMN "Role"."ProcessWidgetAlwaysEnabled" IS 'MODE: read';



COMMENT ON COLUMN "Role"."CloudAdmin" IS 'MODE: read';



COMMENT ON COLUMN "Role"."Active" IS 'MODE: read';



CREATE TABLE "Role_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Role");



CREATE TABLE "Room" (
    "Floor" integer,
    "UsageType" integer,
    "Surface" numeric(6,2),
    "Office" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Room" IS 'DESCR: Room|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Room"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Room"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Room"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Room"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Room"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Room"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Room"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Room"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Room"."Floor" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Floor|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: FloorRoom|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Room"."UsageType" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Usage type|FIELDMODE: write|GROUP: |INDEX: 5|LOOKUP: Room usage type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Room"."Surface" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Surface|FIELDMODE: write|GROUP: |INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Room"."Office" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Office|FIELDMODE: write|GROUP: |INDEX: 7|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: OfficeRoom|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "Room_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Room");



CREATE TABLE "Server" (
    "RAID" integer,
    "RedundantPowerSupply" boolean
)
INHERITS ("Computer");



COMMENT ON TABLE "Server" IS 'DESCR: Server|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Server"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Server"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Server"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Server"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Server"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Server"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Server"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Server"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Server"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Server"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Server"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Server"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Server"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Server"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."RAM" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: RAM|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."CPUNumber" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Number of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."CPUSpeed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Speed of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."HDSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Harddisk total size (GB)|FIELDMODE: write|GROUP: Technical data|INDEX: 18|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."IPAddress" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: IPAddress|FIELDMODE: write|GROUP: Technical data|INDEX: 19|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."RAID" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: RAID|FIELDMODE: write|GROUP: Technical data|INDEX: 19|LOOKUP: RAID|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."RedundantPowerSupply" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Redundant power supply|FIELDMODE: write|GROUP: Technical data|INDEX: 20|MODE: write|STATUS: active';



CREATE TABLE "Server_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Server");



CREATE TABLE "Supplier" (
    "Type" integer,
    "Address" character varying(50),
    "ZIP" character varying(5),
    "City" character varying(50),
    "Phone" character varying(20),
    "Email" character varying(50),
    "WebSite" character varying(50),
    "Country" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Supplier" IS 'DESCR: Supplier|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Supplier"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Supplier"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Supplier"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Supplier"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Supplier"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Supplier"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Supplier"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Supplier"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Supplier"."Type" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Type|FIELDMODE: write|INDEX: 3|LOOKUP: Supplier type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."Address" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Address|FIELDMODE: write|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."ZIP" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: ZIP|FIELDMODE: write|INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."City" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: City|FIELDMODE: write|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."Phone" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Phone|FIELDMODE: write|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."Email" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Email|FIELDMODE: write|INDEX: 9|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."WebSite" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: WebSite|FIELDMODE: write|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."Country" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Country|FIELDMODE: write|GROUP: |INDEX: 7|LOOKUP: Country|MODE: write|STATUS: active';



CREATE TABLE "SupplierContact" (
    "Surname" character varying(50),
    "Name" character varying(50),
    "Supplier" integer,
    "Phone" character varying(20),
    "Mobile" character varying(20),
    "Email" character varying(50)
)
INHERITS ("Class");



COMMENT ON TABLE "SupplierContact" IS 'DESCR: SupplierContact|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "SupplierContact"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "SupplierContact"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "SupplierContact"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "SupplierContact"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: hidden|GROUP: |INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "SupplierContact"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "SupplierContact"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "SupplierContact"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "SupplierContact"."Surname" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Surname|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Name" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Name|FIELDMODE: write|GROUP: |INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: |INDEX: 6|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierContact|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Phone" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Phone|FIELDMODE: write|GROUP: |INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Mobile" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Mobile|FIELDMODE: write|GROUP: |INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Email" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Email|FIELDMODE: write|GROUP: |INDEX: 9|MODE: write|STATUS: active';



CREATE TABLE "SupplierContact_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("SupplierContact");



CREATE TABLE "Supplier_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Supplier");



CREATE TABLE "UPS" (
    "Power" integer
)
INHERITS ("Asset");



COMMENT ON TABLE "UPS" IS 'DESCR: UPS|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "UPS"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "UPS"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "UPS"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "UPS"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "UPS"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "UPS"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "UPS"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "UPS"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "UPS"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "UPS"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "UPS"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "UPS"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "UPS"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "UPS"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."Power" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Power (W)|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



CREATE TABLE "UPS_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("UPS");



CREATE TABLE "User" (
    "Username" character varying(40) NOT NULL,
    "Password" character varying(40),
    "Email" character varying(320),
    "Active" boolean DEFAULT true NOT NULL,
    "Service" boolean,
    "Privileged" boolean
)
INHERITS ("Class");



COMMENT ON TABLE "User" IS 'MODE: sysread|TYPE: class|DESCR: Users|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "User"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "User"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "User"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "User"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "User"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "User"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "User"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "User"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "User"."Username" IS 'MODE: read|DESCR: Username|INDEX: 5|BASEDSP: true|STATUS: active';



COMMENT ON COLUMN "User"."Password" IS 'MODE: read|DESCR: Password|INDEX: 6|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "User"."Email" IS 'MODE: read|DESCR: Email|INDEX: 7';



COMMENT ON COLUMN "User"."Active" IS 'MODE: read';



COMMENT ON COLUMN "User"."Service" IS 'MODE: user|DESCR: Service|INDEX: 9';



COMMENT ON COLUMN "User"."Privileged" IS 'MODE: user|DESCR: Privileged|INDEX: 10';



CREATE TABLE "User_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("User");



CREATE TABLE "Workplace" (
    "Room" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Workplace" IS 'DESCR: Workplace|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Workplace"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Workplace"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Workplace"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Workplace"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Workplace"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Workplace"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Workplace"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Workplace"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Workplace"."Room" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomWorkplace|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "Workplace_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Workplace");



CREATE TABLE "_BimLayer" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "User" character varying(40),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "ClassName" character varying NOT NULL,
    "Root" boolean DEFAULT false NOT NULL,
    "Active" boolean DEFAULT false NOT NULL,
    "Export" boolean DEFAULT false NOT NULL,
    "Container" boolean DEFAULT false NOT NULL,
    "RootReference" character varying
);



COMMENT ON TABLE "_BimLayer" IS 'MODE: reserved|TYPE: simpleclass|DESCR: BIM Project|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_BimLayer"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_BimLayer"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_BimLayer"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_BimLayer"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_BimLayer"."ClassName" IS 'MODE: write|DESCR: ClassName|INDEX: 1|STATUS: active';



COMMENT ON COLUMN "_BimLayer"."Root" IS 'MODE: write|DESCR: Root|INDEX: 2|STATUS: active';



COMMENT ON COLUMN "_BimLayer"."Active" IS 'MODE: write|DESCR: Active|INDEX: 3|STATUS: active';



COMMENT ON COLUMN "_BimLayer"."Export" IS 'MODE: write|DESCR: Export|INDEX: 4|STATUS: active';



COMMENT ON COLUMN "_BimLayer"."Container" IS 'MODE: write|DESCR: Container|INDEX: 5|STATUS: active';



COMMENT ON COLUMN "_BimLayer"."RootReference" IS 'MODE: write|DESCR: RootReference|INDEX: 6|STATUS: active';



CREATE TABLE "_BimProject" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "User" character varying(40),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Code" character varying NOT NULL,
    "Description" character varying,
    "ProjectId" character varying NOT NULL,
    "Active" boolean DEFAULT true NOT NULL,
    "LastCheckin" timestamp without time zone,
    "Synchronized" boolean DEFAULT false NOT NULL,
    "ImportMapping" text,
    "ExportMapping" text,
    "ExportProjectId" character varying,
    "ShapesProjectId" character varying
);



COMMENT ON TABLE "_BimProject" IS 'MODE: reserved|TYPE: simpleclass|DESCR: BIM Project|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_BimProject"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_BimProject"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_BimProject"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_BimProject"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_BimProject"."Code" IS 'MODE: write|DESCR: Name|INDEX: 1|STATUS: active';



COMMENT ON COLUMN "_BimProject"."Description" IS 'MODE: write|DESCR: Description|INDEX: 2|STATUS: active';



COMMENT ON COLUMN "_BimProject"."ProjectId" IS 'MODE: write|DESCR: Project ID|INDEX: 3|STATUS: active';



COMMENT ON COLUMN "_BimProject"."Active" IS 'MODE: write|DESCR: Active|INDEX: 4|STATUS: active';



COMMENT ON COLUMN "_BimProject"."LastCheckin" IS 'MODE: write|DESCR: Last Checkin|INDEX: 5|STATUS: active';



COMMENT ON COLUMN "_BimProject"."Synchronized" IS 'MODE: write|DESCR: Synchronized|INDEX: 6|STATUS: active';



COMMENT ON COLUMN "_BimProject"."ImportMapping" IS 'MODE: write|DESCR: ImportMapping|INDEX: 7|STATUS: active';



COMMENT ON COLUMN "_BimProject"."ExportMapping" IS 'MODE: write|DESCR: ImportMapping|INDEX: 8|STATUS: active';



COMMENT ON COLUMN "_BimProject"."ExportProjectId" IS 'MODE: write|DESCR: ExportProjectId|INDEX: 9|STATUS: active';



COMMENT ON COLUMN "_BimProject"."ShapesProjectId" IS 'MODE: write|DESCR: ShapesProjectId|INDEX: 10|STATUS: active';



CREATE TABLE "_CustomPage" (
)
INHERITS ("Class");



COMMENT ON TABLE "_CustomPage" IS 'MODE: reserved|TYPE: class|DESCR: CustomPage|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_CustomPage"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_CustomPage"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_CustomPage"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "_CustomPage"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "_CustomPage"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "_CustomPage"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_CustomPage"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "_CustomPage"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



CREATE TABLE "_CustomPage_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("_CustomPage");



CREATE TABLE "_Dashboards" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Definition" text NOT NULL,
    "IdClass" regclass NOT NULL
);



COMMENT ON TABLE "_Dashboards" IS 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass';



COMMENT ON COLUMN "_Dashboards"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_Dashboards"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_Dashboards"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_Dashboards"."Definition" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Dashboards"."IdClass" IS 'MODE: reserved';



CREATE TABLE "_DomainTreeNavigation" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "IdParent" integer,
    "IdGroup" integer,
    "Type" character varying,
    "DomainName" character varying,
    "Direct" boolean,
    "BaseNode" boolean,
    "TargetClassName" character varying,
    "TargetClassDescription" character varying,
    "IdClass" regclass NOT NULL,
    "Description" character varying,
    "TargetFilter" character varying,
    "EnableRecursion" boolean
);



COMMENT ON TABLE "_DomainTreeNavigation" IS 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass';



COMMENT ON COLUMN "_DomainTreeNavigation"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_DomainTreeNavigation"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_DomainTreeNavigation"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_DomainTreeNavigation"."IdParent" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_DomainTreeNavigation"."IdGroup" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_DomainTreeNavigation"."Type" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_DomainTreeNavigation"."DomainName" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_DomainTreeNavigation"."Direct" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_DomainTreeNavigation"."BaseNode" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_DomainTreeNavigation"."TargetClassName" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_DomainTreeNavigation"."TargetClassDescription" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_DomainTreeNavigation"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_DomainTreeNavigation"."Description" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_DomainTreeNavigation"."TargetFilter" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_DomainTreeNavigation"."EnableRecursion" IS 'MODE: write|STATUS: active';



CREATE TABLE "_EmailAccount" (
    "IsDefault" boolean,
    "Address" character varying(100),
    "Username" character varying(100),
    "Password" character varying(100),
    "SmtpServer" character varying(100),
    "SmtpPort" integer,
    "SmtpSsl" boolean,
    "ImapServer" character varying(100),
    "ImapPort" integer,
    "ImapSsl" boolean,
    "OutputFolder" character varying(100),
    "SmtpStartTls" boolean,
    "ImapStartTls" boolean
)
INHERITS ("Class");



COMMENT ON TABLE "_EmailAccount" IS 'MODE: reserved|TYPE: class|DESCR: Email Accounts|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_EmailAccount"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_EmailAccount"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "_EmailAccount"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "_EmailAccount"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "_EmailAccount"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_EmailAccount"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "_EmailAccount"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "_EmailAccount"."IsDefault" IS 'MODE: write|DESCR: Is default|INDEX: 1|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."Address" IS 'MODE: write|DESCR: Address|INDEX: 2|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."Username" IS 'MODE: write|DESCR: Username|INDEX: 3|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."Password" IS 'MODE: write|DESCR: Password|INDEX: 4|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."SmtpServer" IS 'MODE: write|DESCR: SMTP server|INDEX: 5|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."SmtpPort" IS 'MODE: write|DESCR: SMTP port|INDEX: 6|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."SmtpSsl" IS 'MODE: write|DESCR: SMTP SSL|INDEX: 7|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."ImapServer" IS 'MODE: write|DESCR: IMAP server|INDEX: 8|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."ImapPort" IS 'MODE: write|DESCR: IMAP port|INDEX: 9|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."ImapSsl" IS 'MODE: write|DESCR: IMAP SSL|INDEX: 10|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."OutputFolder" IS 'MODE: write|DESCR: Output Folder|INDEX: 15|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."SmtpStartTls" IS 'MODE: write|DESCR: SMTP STARTTLS|STATUS: active';



COMMENT ON COLUMN "_EmailAccount"."ImapStartTls" IS 'MODE: write|DESCR: IMAP STARTTLS|STATUS: active';



CREATE TABLE "_EmailAccount_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("_EmailAccount");



CREATE TABLE "_EmailTemplate" (
    "From" text,
    "To" text,
    "CC" text,
    "BCC" text,
    "Subject" text,
    "Body" text,
    "Account" integer,
    "KeepSynchronization" boolean,
    "PromptSynchronization" boolean,
    "Delay" integer
)
INHERITS ("Class");



COMMENT ON TABLE "_EmailTemplate" IS 'MODE: reserved|TYPE: class|DESCR: Email Templates|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_EmailTemplate"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_EmailTemplate"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_EmailTemplate"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "_EmailTemplate"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "_EmailTemplate"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "_EmailTemplate"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_EmailTemplate"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "_EmailTemplate"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "_EmailTemplate"."From" IS 'MODE: write|DESCR: From|INDEX: 2|STATUS: active';



COMMENT ON COLUMN "_EmailTemplate"."To" IS 'MODE: write|DESCR: To|INDEX: 3|STATUS: active';



COMMENT ON COLUMN "_EmailTemplate"."CC" IS 'MODE: write|DESCR: CC|INDEX: 4|STATUS: active';



COMMENT ON COLUMN "_EmailTemplate"."BCC" IS 'MODE: write|DESCR: BCC|INDEX: 5|STATUS: active';



COMMENT ON COLUMN "_EmailTemplate"."Subject" IS 'MODE: write|DESCR: Subject|INDEX: 6|STATUS: active';



COMMENT ON COLUMN "_EmailTemplate"."Body" IS 'MODE: write|DESCR: Body|INDEX: 7|STATUS: active';



COMMENT ON COLUMN "_EmailTemplate"."Account" IS 'MODE: user|FIELDMODE: write|DESCR: Account|INDEX: 8|REFERENCEDOM: AccountTemplate|REFERENCEDIRECT: false|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "_EmailTemplate"."KeepSynchronization" IS 'MODE: write|FIELDMODE: write|DESCR: Keep synchronization|INDEX: 9|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "_EmailTemplate"."PromptSynchronization" IS 'MODE: write|FIELDMODE: write|DESCR: Prompt synchronization|INDEX: 10|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "_EmailTemplate"."Delay" IS 'MODE: user|FIELDMODE: write|DESCR: Delay|INDEX: 11|STATUS: active';



CREATE TABLE "_EmailTemplate_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("_EmailTemplate");



CREATE TABLE "_Filter" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Code" character varying NOT NULL,
    "Description" character varying,
    "UserId" integer,
    "Filter" text,
    "ClassId" regclass NOT NULL,
    "Shared" boolean DEFAULT false NOT NULL
);



COMMENT ON TABLE "_Filter" IS 'MODE: reserved|TYPE: simpleclass|DESCR: Filter|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_Filter"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_Filter"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_Filter"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_Filter"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_Filter"."Code" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Filter"."Description" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Filter"."UserId" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Filter"."Filter" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Filter"."ClassId" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Filter"."Shared" IS 'MODE: write|STATUS: active';



CREATE TABLE "_Icon" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "User" character varying(40),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Element" text NOT NULL,
    "Path" text NOT NULL
);



COMMENT ON TABLE "_Icon" IS 'MODE: reserved|TYPE: simpleclass|DESCR: _Icon|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_Icon"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_Icon"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_Icon"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_Icon"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_Icon"."Element" IS 'MODE: write|DESCR: Element|STATUS: active';



COMMENT ON COLUMN "_Icon"."Path" IS 'MODE: write|DESCR: Path|STATUS: active';



CREATE TABLE "_Layer" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Description" character varying,
    "FullName" character varying,
    "Index" integer,
    "MinimumZoom" integer,
    "MaximumZoom" integer,
    "MapStyle" text,
    "Name" character varying,
    "GeoServerName" character varying,
    "Type" character varying,
    "Visibility" text,
    "CardsBinding" text,
    "IdClass" regclass NOT NULL
);



COMMENT ON TABLE "_Layer" IS 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass';



COMMENT ON COLUMN "_Layer"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_Layer"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_Layer"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_Layer"."Description" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."FullName" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."Index" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."MinimumZoom" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."MaximumZoom" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."MapStyle" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."Name" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."GeoServerName" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."Type" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."Visibility" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."CardsBinding" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Layer"."IdClass" IS 'MODE: reserved';



CREATE TABLE "_Task" (
    "CronExpression" text,
    "Type" text,
    "Running" boolean
)
INHERITS ("Class");



COMMENT ON TABLE "_Task" IS 'MODE: reserved|TYPE: class|DESCR: Scheduler|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_Task"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_Task"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_Task"."Code" IS 'MODE: read|DESCR: Job Type|INDEX: 1';



COMMENT ON COLUMN "_Task"."Description" IS 'MODE: read|DESCR: Job Description|INDEX: 2';



COMMENT ON COLUMN "_Task"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "_Task"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_Task"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "_Task"."Notes" IS 'MODE: read|DESCR: Job Parameters|INDEX: 3';



COMMENT ON COLUMN "_Task"."CronExpression" IS 'MODE: write|DESCR: Cron Expression|STATUS: active';



COMMENT ON COLUMN "_Task"."Type" IS 'MODE: write|DESCR: JobType|STATUS: active';



COMMENT ON COLUMN "_Task"."Running" IS 'MODE: write|DESCR: Running|STATUS: active';



CREATE TABLE "_TaskParameter" (
    "Owner" integer,
    "Key" text NOT NULL,
    "Value" text
)
INHERITS ("Class");



COMMENT ON TABLE "_TaskParameter" IS 'MODE: reserved|TYPE: class|DESCR: Email Accounts|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_TaskParameter"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_TaskParameter"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_TaskParameter"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "_TaskParameter"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "_TaskParameter"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "_TaskParameter"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_TaskParameter"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "_TaskParameter"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "_TaskParameter"."Owner" IS 'MODE: write|DESCR: Scheduler Id|INDEX: 1|STATUS: active';



COMMENT ON COLUMN "_TaskParameter"."Key" IS 'MODE: write|DESCR: Key|INDEX: 2|STATUS: active';



COMMENT ON COLUMN "_TaskParameter"."Value" IS 'MODE: write|DESCR: Value|INDEX: 3|STATUS: active';



CREATE TABLE "_TaskParameter_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("_TaskParameter");



CREATE TABLE "_TaskRuntime" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "User" character varying(40),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Owner" integer,
    "LastExecution" timestamp without time zone
);



COMMENT ON TABLE "_TaskRuntime" IS 'MODE: reserved|TYPE: simpleclass|DESCR: _TaskRuntime|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_TaskRuntime"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_TaskRuntime"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_TaskRuntime"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_TaskRuntime"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_TaskRuntime"."Owner" IS 'MODE: write|DESCR: Owner|STATUS: active|FKTARGETCLASS: _Task';



COMMENT ON COLUMN "_TaskRuntime"."LastExecution" IS 'MODE: write|DESCR: Last Execution|STATUS: active';



CREATE TABLE "_Task_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("_Task");



CREATE TABLE "_Templates" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Name" text NOT NULL,
    "Template" text NOT NULL,
    "IdClass" regclass NOT NULL
);



COMMENT ON TABLE "_Templates" IS 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass';



COMMENT ON COLUMN "_Templates"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_Templates"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_Templates"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_Templates"."Name" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Templates"."Template" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Templates"."IdClass" IS 'MODE: reserved';



CREATE TABLE "_Translation" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "User" character varying(40),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Element" text NOT NULL,
    "Lang" text NOT NULL,
    "Value" text NOT NULL
);



COMMENT ON TABLE "_Translation" IS 'MODE: reserved|TYPE: simpleclass|DESCR: Translations|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_Translation"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_Translation"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_Translation"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_Translation"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_Translation"."Element" IS 'MODE: write|DESCR: Element|INDEX: 1|STATUS: active';



COMMENT ON COLUMN "_Translation"."Lang" IS 'MODE: write|DESCR: Lang|INDEX: 2|STATUS: active';



COMMENT ON COLUMN "_Translation"."Value" IS 'MODE: write|DESCR: Value|INDEX: 3|STATUS: active';



CREATE TABLE "_View" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "User" character varying(100),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Name" character varying NOT NULL,
    "Description" character varying,
    "Filter" text,
    "IdSourceClass" regclass,
    "SourceFunction" text,
    "Type" character varying NOT NULL
);



COMMENT ON TABLE "_View" IS 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass';



COMMENT ON COLUMN "_View"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_View"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_View"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_View"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_View"."Name" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_View"."Description" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_View"."Filter" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_View"."IdSourceClass" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_View"."SourceFunction" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_View"."Type" IS 'MODE: write|STATUS: active';



CREATE TABLE "_Widget" (
    "Definition" text
)
INHERITS ("Class");



COMMENT ON TABLE "_Widget" IS 'MODE: reserved|TYPE: class|DESCR: Widget|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "_Widget"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_Widget"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "_Widget"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "_Widget"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "_Widget"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "_Widget"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_Widget"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "_Widget"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "_Widget"."Definition" IS 'MODE: write|STATUS: active';



CREATE TABLE "_Widget_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("_Widget");



CREATE SEQUENCE class_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



COMMENT ON SEQUENCE class_seq IS 'Sequence for autoincrement class';



CREATE VIEW system_classcatalog AS
SELECT pg_class.oid AS classid, (CASE WHEN (pg_namespace.nspname = 'public'::name) THEN ''::text ELSE ((pg_namespace.nspname)::text || '.'::text) END || (pg_class.relname)::text) AS classname, pg_description.description AS classcomment, (pg_class.relkind = 'v'::"char") AS isview FROM ((pg_class JOIN pg_description ON ((((pg_description.objoid = pg_class.oid) AND (pg_description.objsubid = 0)) AND _cm_is_any_class_comment(pg_description.description)))) JOIN pg_namespace ON ((pg_namespace.oid = pg_class.relnamespace))) WHERE (pg_class.reltype > (0)::oid);



CREATE VIEW system_domaincatalog AS
SELECT pg_class.oid AS domainid, "substring"((pg_class.relname)::text, 5) AS domainname, "substring"(pg_description.description, 'CLASS1: ([^|]*)'::text) AS domainclass1, "substring"(pg_description.description, 'CLASS2: ([^|]*)'::text) AS domainclass2, "substring"(pg_description.description, 'CARDIN: ([^|]*)'::text) AS domaincardinality, pg_description.description AS domaincomment, (pg_class.relkind = 'v'::"char") AS isview FROM (pg_class LEFT JOIN pg_description pg_description ON (((pg_description.objoid = pg_class.oid) AND (pg_description.objsubid = 0)))) WHERE (strpos(pg_description.description, 'TYPE: domain'::text) > 0);



CREATE VIEW system_attributecatalog AS
SELECT cmtable.classid, cmtable.classname, pg_attribute.attname AS attributename, pg_attribute.attnum AS dbindex, CASE WHEN (strpos(attribute_description.description, 'MODE: reserved'::text) > 0) THEN (-1) WHEN (strpos(attribute_description.description, 'INDEX: '::text) > 0) THEN ("substring"(attribute_description.description, 'INDEX: ([^|]*)'::text))::integer ELSE 0 END AS attributeindex, (pg_attribute.attinhcount = 0) AS attributeislocal, CASE pg_type.typname WHEN 'geometry'::name THEN (_cm_get_geometry_type(cmtable.classid, (pg_attribute.attname)::text))::name ELSE pg_type.typname END AS attributetype, CASE WHEN (pg_type.typname = 'varchar'::name) THEN (pg_attribute.atttypmod - 4) ELSE NULL::integer END AS attributelength, CASE WHEN (pg_type.typname = 'numeric'::name) THEN (pg_attribute.atttypmod / 65536) ELSE NULL::integer END AS attributeprecision, CASE WHEN (pg_type.typname = 'numeric'::name) THEN ((pg_attribute.atttypmod - ((pg_attribute.atttypmod / 65536) * 65536)) - 4) ELSE NULL::integer END AS attributescale, ((notnulljoin.oid IS NOT NULL) OR pg_attribute.attnotnull) AS attributenotnull, pg_attrdef.adsrc AS attributedefault, attribute_description.description AS attributecomment, _cm_attribute_is_unique(cmtable.classid, (pg_attribute.attname)::text) AS isunique, _cm_legacy_read_comment(((attribute_description.description)::character varying)::text, ('LOOKUP'::character varying)::text) AS attributelookup, _cm_legacy_read_comment(((attribute_description.description)::character varying)::text, ('REFERENCEDOM'::character varying)::text) AS attributereferencedomain, _cm_legacy_read_comment(((attribute_description.description)::character varying)::text, ('REFERENCETYPE'::character varying)::text) AS attributereferencetype, _cm_legacy_read_comment(((attribute_description.description)::character varying)::text, ('REFERENCEDIRECT'::character varying)::text) AS attributereferencedirect, CASE WHEN (system_domaincatalog.domaincardinality = '1:N'::text) THEN system_domaincatalog.domainclass1 ELSE system_domaincatalog.domainclass2 END AS attributereference FROM ((((((pg_attribute JOIN (SELECT system_classcatalog.classid, system_classcatalog.classname FROM system_classcatalog UNION SELECT system_domaincatalog.domainid AS classid, system_domaincatalog.domainname AS classname FROM system_domaincatalog) cmtable ON ((pg_attribute.attrelid = cmtable.classid))) LEFT JOIN pg_type ON ((pg_type.oid = pg_attribute.atttypid))) LEFT JOIN pg_description attribute_description ON (((attribute_description.objoid = cmtable.classid) AND (attribute_description.objsubid = pg_attribute.attnum)))) LEFT JOIN pg_attrdef pg_attrdef ON (((pg_attrdef.adrelid = pg_attribute.attrelid) AND (pg_attrdef.adnum = pg_attribute.attnum)))) LEFT JOIN system_domaincatalog ON (((_cm_legacy_read_comment(((attribute_description.description)::character varying)::text, ('REFERENCEDOM'::character varying)::text))::text = system_domaincatalog.domainname))) LEFT JOIN pg_constraint notnulljoin ON (((notnulljoin.conrelid = pg_attribute.attrelid) AND ((notnulljoin.conname)::text = _cm_notnull_constraint_name((pg_attribute.attname)::text))))) WHERE (((pg_attribute.atttypid > (0)::oid) AND (pg_attribute.attnum > 0)) AND (attribute_description.description IS NOT NULL));



CREATE VIEW system_inheritcatalog AS
SELECT pg_inherits.inhparent AS parentid, pg_inherits.inhrelid AS childid FROM pg_inherits UNION SELECT ('"Class"'::regclass)::oid AS parentid, pg_class.oid AS childid FROM ((pg_class JOIN pg_description ON (((pg_description.objoid = pg_class.oid) AND (pg_description.objsubid = 0)))) LEFT JOIN pg_inherits ON ((pg_inherits.inhrelid = pg_class.oid))) WHERE ((pg_class.relkind = 'v'::"char") AND (strpos(pg_description.description, 'TYPE: class'::text) > 0));



CREATE VIEW system_privilegescatalog AS
SELECT DISTINCT ON (permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass") permission."Id", permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."BeginDate", permission."Notes", permission."IdRole", permission."IdGrantedClass", permission."Mode" FROM ((SELECT "Grant"."Id", "Grant"."IdClass", "Grant"."Code", "Grant"."Description", "Grant"."Status", "Grant"."User", "Grant"."BeginDate", "Grant"."Notes", "Grant"."IdRole", "Grant"."IdGrantedClass", "Grant"."Mode" FROM "Grant" UNION SELECT (-1), '"Grant"'::regclass AS regclass, ''::character varying AS "varchar", ''::character varying AS "varchar", 'A'::bpchar AS bpchar, 'admin'::character varying AS "varchar", now() AS now, NULL::text AS unknown, "Role"."Id", (system_classcatalog.classid)::regclass AS classid, '-'::character varying AS "varchar" FROM system_classcatalog, "Role" WHERE ((((system_classcatalog.classid)::regclass)::oid <> ('"Class"'::regclass)::oid) AND (NOT ((("Role"."Id")::text || ((system_classcatalog.classid)::integer)::text) IN (SELECT (("Grant"."IdRole")::text || ((("Grant"."IdGrantedClass")::oid)::integer)::text) FROM "Grant"))))) permission JOIN system_classcatalog ON ((((permission."IdGrantedClass")::oid = system_classcatalog.classid) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('MODE'::character varying)::text))::text = ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text]))))) ORDER BY permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass";



CREATE VIEW system_relationlist AS
SELECT "Map"."Id" AS id, pg_class1.relname AS class1, pg_class2.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass1")::integer AS idclass1, "Map"."IdObj1" AS idobj1, ("Map"."IdClass2")::integer AS idclass2, "Map"."IdObj2" AS idobj2, "Map"."BeginDate" AS begindate, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRDIR'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description2.description, 'DESCR'::text))::text AS classdescription, true AS direct, NULL::text AS version FROM ((((((("Map" JOIN "Class" ON ((((("Class"."IdClass")::oid = ("Map"."IdClass2")::oid) AND ("Class"."Id" = "Map"."IdObj2")) AND ("Class"."Status" = 'A'::bpchar)))) LEFT JOIN pg_class pg_class0 ON ((pg_class0.oid = ("Map"."IdDomain")::oid))) LEFT JOIN pg_description pg_description0 ON (((((pg_description0.objoid = pg_class0.oid) AND (pg_description0.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description)))) LEFT JOIN pg_class pg_class1 ON ((pg_class1.oid = ("Map"."IdClass1")::oid))) LEFT JOIN pg_description pg_description1 ON (((pg_description1.objoid = pg_class1.oid) AND (pg_description1.objsubid = 0)))) LEFT JOIN pg_class pg_class2 ON ((pg_class2.oid = ("Map"."IdClass2")::oid))) LEFT JOIN pg_description pg_description2 ON (((pg_description2.objoid = pg_class2.oid) AND (pg_description2.objsubid = 0)))) UNION SELECT "Map"."Id" AS id, pg_class2.relname AS class1, pg_class1.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass2")::integer AS idclass1, "Map"."IdObj2" AS idobj1, ("Map"."IdClass1")::integer AS idclass2, "Map"."IdObj1" AS idobj2, "Map"."BeginDate" AS begindate, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRINV'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description1.description, 'DESCR'::text))::text AS classdescription, false AS direct, NULL::text AS version FROM ((((((("Map" JOIN "Class" ON ((((("Class"."IdClass")::oid = ("Map"."IdClass1")::oid) AND ("Class"."Id" = "Map"."IdObj1")) AND ("Class"."Status" = 'A'::bpchar)))) LEFT JOIN pg_class pg_class0 ON ((pg_class0.oid = ("Map"."IdDomain")::oid))) LEFT JOIN pg_description pg_description0 ON (((((pg_description0.objoid = pg_class0.oid) AND (pg_description0.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description)))) LEFT JOIN pg_class pg_class1 ON ((pg_class1.oid = ("Map"."IdClass1")::oid))) LEFT JOIN pg_description pg_description1 ON (((pg_description1.objoid = pg_class1.oid) AND (pg_description1.objsubid = 0)))) LEFT JOIN pg_class pg_class2 ON ((pg_class2.oid = ("Map"."IdClass2")::oid))) LEFT JOIN pg_description pg_description2 ON (((pg_description2.objoid = pg_class2.oid) AND (pg_description2.objsubid = 0))));



CREATE VIEW system_relationlist_history AS
SELECT "Map"."Id" AS id, pg_class1.relname AS class1, pg_class2.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass1")::integer AS idclass1, "Map"."IdObj1" AS idobj1, ("Map"."IdClass2")::integer AS idclass2, "Map"."IdObj2" AS idobj2, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRDIR'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description2.description, 'DESCR'::text))::text AS classdescription, true AS direct, "Map"."User" AS username, "Map"."BeginDate" AS begindate, "Map"."EndDate" AS enddate, NULL::text AS version FROM ("Map" LEFT JOIN "Class" ON (((("Class"."IdClass")::oid = ("Map"."IdClass2")::oid) AND ("Class"."Id" = "Map"."IdObj2")))), pg_class pg_class0, pg_description pg_description0, pg_class pg_class1, pg_description pg_description1, pg_class pg_class2, pg_description pg_description2 WHERE (((((((((((("Map"."Status" = 'U'::bpchar) AND (pg_class1.oid = ("Map"."IdClass1")::oid)) AND (pg_class2.oid = ("Map"."IdClass2")::oid)) AND (pg_class0.oid = ("Map"."IdDomain")::oid)) AND (pg_description0.objoid = pg_class0.oid)) AND (pg_description0.objsubid = 0)) AND (pg_description1.objoid = pg_class1.oid)) AND (pg_description1.objsubid = 0)) AND (pg_description2.objoid = pg_class2.oid)) AND (pg_description2.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description)) UNION SELECT "Map"."Id" AS id, pg_class2.relname AS class1, pg_class1.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass2")::integer AS idclass1, "Map"."IdObj2" AS idobj1, ("Map"."IdClass1")::integer AS idclass2, "Map"."IdObj1" AS idobj2, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRINV'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description2.description, 'DESCR'::text))::text AS classdescription, false AS direct, "Map"."User" AS username, "Map"."BeginDate" AS begindate, "Map"."EndDate" AS enddate, NULL::text AS version FROM ("Map" LEFT JOIN "Class" ON (((("Class"."IdClass")::oid = ("Map"."IdClass1")::oid) AND ("Class"."Id" = "Map"."IdObj1")))), pg_class pg_class0, pg_description pg_description0, pg_class pg_class1, pg_description pg_description1, pg_class pg_class2, pg_description pg_description2 WHERE (((((((((((("Map"."Status" = 'U'::bpchar) AND (pg_class1.oid = ("Map"."IdClass1")::oid)) AND (pg_class2.oid = ("Map"."IdClass2")::oid)) AND (pg_class0.oid = ("Map"."IdDomain")::oid)) AND (pg_description0.objoid = pg_class0.oid)) AND (pg_description0.objsubid = 0)) AND (pg_description1.objoid = pg_class1.oid)) AND (pg_description1.objsubid = 0)) AND (pg_description2.objoid = pg_class2.oid)) AND (pg_description2.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description));



CREATE VIEW system_treecatalog AS
SELECT parent_class.classid AS parentid, parent_class.classname AS parent, parent_class.classcomment AS parentcomment, child_class.classid AS childid, child_class.classname AS child, child_class.classcomment AS childcomment FROM ((system_inheritcatalog JOIN system_classcatalog parent_class ON ((system_inheritcatalog.parentid = parent_class.classid))) JOIN system_classcatalog child_class ON ((system_inheritcatalog.childid = child_class.classid)));



ALTER TABLE ONLY "Activity" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Activity" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Asset" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Asset" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Building" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Building" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Building_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Building_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Computer" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Computer" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Email" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Email" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Email_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Email_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Email_history" ALTER COLUMN "NoSubjectPrefix" SET DEFAULT false;



ALTER TABLE ONLY "Employee" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Employee" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Employee_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Employee_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Floor" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Floor" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Floor_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Floor_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Invoice" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Invoice" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Invoice_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Invoice_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "License" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "License" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "License_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "License_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_AccountTemplate" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_AccountTemplate" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_AccountTemplate_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_AccountTemplate_history" ALTER COLUMN "EndDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_AccountTemplate_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_AssetAssignee" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_AssetAssignee" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_AssetAssignee_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_AssetAssignee_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_AssetReference" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_AssetReference" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_AssetReference_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_AssetReference_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_BuildingFloor" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_BuildingFloor" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_BuildingFloor_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_BuildingFloor_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_ClassEmail" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_ClassEmail" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_ClassEmail_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_ClassEmail_history" ALTER COLUMN "EndDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_ClassEmail_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_ClassMetadata" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_ClassMetadata" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_ClassMetadata_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_ClassMetadata_history" ALTER COLUMN "EndDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_ClassMetadata_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_FilterRole" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_FilterRole" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_FilterRole_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_FilterRole_history" ALTER COLUMN "EndDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_FilterRole_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_FloorRoom" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_FloorRoom" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_FloorRoom_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_FloorRoom_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_Members" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_Members" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_Members_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_Members_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_NetworkDeviceConnection" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_NetworkDeviceConnection" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_NetworkDeviceConnection_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_NetworkDeviceConnection_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_OfficeRoom" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_OfficeRoom" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_OfficeRoom_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_OfficeRoom_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RFCChangeManager" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RFCChangeManager" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RFCChangeManager_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RFCChangeManager_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RFCExecutor" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RFCExecutor" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RFCExecutor_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RFCExecutor_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RFCRequester" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RFCRequester" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RFCRequester_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RFCRequester_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RoomAsset" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RoomAsset" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RoomAsset_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RoomAsset_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RoomNetworkPoint" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RoomNetworkPoint" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RoomNetworkPoint_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RoomNetworkPoint_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RoomWorkplace" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RoomWorkplace" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_RoomWorkplace_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_RoomWorkplace_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_Supervisor" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_Supervisor" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_Supervisor_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_Supervisor_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_SupplierAsset" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_SupplierAsset" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_SupplierAsset_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_SupplierAsset_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_SupplierContact" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_SupplierContact" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_SupplierContact_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_SupplierContact_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_SupplierInvoice" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_SupplierInvoice" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_SupplierInvoice_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_SupplierInvoice_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_UserRole" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_UserRole" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_UserRole_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_UserRole_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_WorkplaceComposition" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_WorkplaceComposition" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Map_WorkplaceComposition_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Map_WorkplaceComposition_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Menu" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Menu" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Menu_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Menu_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Menu_history" ALTER COLUMN "IdParent" SET DEFAULT 0;



ALTER TABLE ONLY "Menu_history" ALTER COLUMN "IdElementObj" SET DEFAULT 0;



ALTER TABLE ONLY "Menu_history" ALTER COLUMN "Number" SET DEFAULT 0;



ALTER TABLE ONLY "Metadata" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Metadata" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Metadata_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Metadata_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Monitor" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Monitor" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Monitor_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Monitor_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "NetworkDevice" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "NetworkDevice" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "NetworkDevice_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "NetworkDevice_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "NetworkPoint" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "NetworkPoint" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "NetworkPoint_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "NetworkPoint_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Notebook" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Notebook" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Notebook_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Notebook_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Office" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Office" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Office_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Office_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "PC" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "PC" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "PC_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "PC_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Patch" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Patch" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Patch_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Patch_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Printer" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Printer" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Printer_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Printer_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Rack" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Rack" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Rack_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Rack_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "RequestForChange" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "RequestForChange" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "RequestForChange_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "RequestForChange_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Role" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Role" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Role_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Role_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Role_history" ALTER COLUMN "HideSidePanel" SET DEFAULT false;



ALTER TABLE ONLY "Role_history" ALTER COLUMN "FullScreenMode" SET DEFAULT false;



ALTER TABLE ONLY "Role_history" ALTER COLUMN "SimpleHistoryModeForCard" SET DEFAULT false;



ALTER TABLE ONLY "Role_history" ALTER COLUMN "SimpleHistoryModeForProcess" SET DEFAULT false;



ALTER TABLE ONLY "Role_history" ALTER COLUMN "ProcessWidgetAlwaysEnabled" SET DEFAULT false;



ALTER TABLE ONLY "Role_history" ALTER COLUMN "CloudAdmin" SET DEFAULT false;



ALTER TABLE ONLY "Role_history" ALTER COLUMN "Active" SET DEFAULT true;



ALTER TABLE ONLY "Room" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Room" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Room_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Room_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Server" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Server" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Server_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Server_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Supplier" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Supplier" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "SupplierContact" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "SupplierContact" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "SupplierContact_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "SupplierContact_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Supplier_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Supplier_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "UPS" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "UPS" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "UPS_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "UPS_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "User" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "User" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "User_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "User_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "User_history" ALTER COLUMN "Active" SET DEFAULT true;



ALTER TABLE ONLY "Workplace" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Workplace" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "Workplace_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "Workplace_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_CustomPage" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_CustomPage" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_CustomPage_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_CustomPage_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_EmailAccount" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_EmailAccount" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_EmailAccount_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_EmailAccount_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_EmailTemplate" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_EmailTemplate" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_EmailTemplate_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_EmailTemplate_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_Task" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_Task" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_TaskParameter" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_TaskParameter" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_TaskParameter_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_TaskParameter_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_Task_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_Task_history" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_Widget" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_Widget" ALTER COLUMN "BeginDate" SET DEFAULT now();



ALTER TABLE ONLY "_Widget_history" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();



ALTER TABLE ONLY "_Widget_history" ALTER COLUMN "BeginDate" SET DEFAULT now();









INSERT INTO "Building" VALUES (64, '"Building"', 'DC', 'Data Center', 'A', 'admin', '2011-07-24 18:40:14.637', NULL, 'Main street 16', '58213', 'London', 25);
INSERT INTO "Building" VALUES (76, '"Building"', 'B2', 'Office Building B', 'A', 'admin', '2011-07-24 18:41:06.636', NULL, 'Liverpool Street 22', '12100', 'London', 25);
INSERT INTO "Building" VALUES (73, '"Building"', 'B1', 'Office Building A', 'A', 'admin', '2011-07-24 18:41:12.996', NULL, 'Liverpool Street 18', '12100', 'London', 25);



INSERT INTO "Building_history" VALUES (71, '"Building"', 'Data Center', 'Data Center', 'U', 'admin', '2011-07-24 18:28:28.63', NULL, 'Main street 16', '58213', 'London', NULL, 64, '2011-07-24 18:35:47.898');
INSERT INTO "Building_history" VALUES (74, '"Building"', 'Data Center', 'Data Center', 'U', 'admin', '2011-07-24 18:35:47.898', NULL, 'Main street 16', '58213', 'London', 25, 64, '2011-07-24 18:40:14.637');
INSERT INTO "Building_history" VALUES (77, '"Building"', 'B1', 'Office Building A', 'U', 'admin', '2011-07-24 18:40:06.618', NULL, 'Liverpool street 18', '12100', 'London', 25, 73, '2011-07-24 18:41:12.996');















INSERT INTO "Employee" VALUES (134, '"Employee"', '10', 'Taylor William', 'A', 'admin', '2011-07-24 23:35:18.412', NULL, 'Taylor', 'William', 21, 22, 146, 'william.taylor@example.com', 108, '23456', '763477', '', 24);
INSERT INTO "Employee" VALUES (118, '"Employee"', '02', 'Johnson Mary', 'A', 'admin', '2011-07-24 23:36:23.281', NULL, 'Johnson', 'Mary', 21, 147, 23, 'mary.johnson@example.com', 108, '76543', '9876554', '', 24);
INSERT INTO "Employee" VALUES (124, '"Employee"', '05', 'Brown Robert', 'A', 'admin', '2011-07-24 23:43:44.824', NULL, 'Brown', 'Robert', 149, 22, 146, 'robert.brown@example.com', 110, '65432', '24555556', '', 152);
INSERT INTO "Employee" VALUES (122, '"Employee"', '04', 'Jones Patricia', 'A', 'admin', '2011-07-24 23:45:11.466', NULL, 'Jones', 'Patricia', 21, 148, 145, 'patricia.jones@example.com', 112, '76543', '45678889', '', 24);
INSERT INTO "Employee" VALUES (132, '"Employee"', '09', 'Moore Elizabeth', 'A', 'admin', '2011-07-24 23:45:30.27', NULL, 'Moore', 'Elizabeth', 149, 22, 146, 'elizabeth.moore@example.com', 110, '76545', '2345666', '', 151);
INSERT INTO "Employee" VALUES (126, '"Employee"', '06', 'Davis Michael', 'A', 'admin', '2011-07-24 23:46:29.744', NULL, 'Davis', 'Michael', 21, 147, 23, 'michael.davis@example.com', 110, '45556', '3567789', '', 24);
INSERT INTO "Employee" VALUES (130, '"Employee"', '08', 'Wilson Barbara', 'A', 'admin', '2011-07-24 23:47:15.594', NULL, 'Wilson', 'Barbara', 21, 147, 146, 'barbara.wilson@example.com', 112, '644353', '7789999', '', 151);
INSERT INTO "Employee" VALUES (128, '"Employee"', '07', 'Miller Linda', 'A', 'admin', '2011-07-24 23:48:03.801', NULL, 'Miller', 'Linda', 21, 147, 23, 'linda.miller@example.com', 108, '5757578', '686868686', '', 24);
INSERT INTO "Employee" VALUES (120, '"Employee"', '03', 'Williams John', 'A', 'admin', '2011-07-24 23:48:45.557', NULL, 'Williams', 'John', 150, 22, 146, 'john.williams@example.com', 108, '64646', '56868768', '', 24);
INSERT INTO "Employee" VALUES (116, '"Employee"', '01', 'Smith James', 'A', 'admin', '2011-07-24 23:49:33.373', NULL, 'Smith', 'James', 149, 22, 146, 'james.smith@example.com', 112, '565675', '27575678', '', 24);



INSERT INTO "Employee_history" VALUES (164, '"Employee"', '10', 'Taylor William', 'U', 'admin', '2011-07-24 19:04:25.125', NULL, 'Taylor', 'William', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 134, '2011-07-24 23:35:18.412');
INSERT INTO "Employee_history" VALUES (167, '"Employee"', '02', 'Johnson Mary', 'U', 'admin', '2011-07-24 18:55:41.127', NULL, 'Johnson', 'Mary', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 118, '2011-07-24 23:36:23.281');
INSERT INTO "Employee_history" VALUES (171, '"Employee"', '09', 'Moore Elizabeth', 'U', 'admin', '2011-07-24 19:03:30.275', NULL, 'Moore', 'Elizabeth', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 132, '2011-07-24 23:40:39.563');
INSERT INTO "Employee_history" VALUES (174, '"Employee"', '05', 'Brown Robert', 'U', 'admin', '2011-07-24 18:56:57.522', NULL, 'Brown', 'Robert', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 124, '2011-07-24 23:43:44.824');
INSERT INTO "Employee_history" VALUES (177, '"Employee"', '04', 'Jones Patricia', 'U', 'admin', '2011-07-24 18:56:41.314', NULL, 'Jones', 'Patricia', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 122, '2011-07-24 23:45:11.466');
INSERT INTO "Employee_history" VALUES (180, '"Employee"', '09', 'Moore Elizabeth', 'U', 'admin', '2011-07-24 23:40:39.563', NULL, 'Moore', 'Elizabeth', 149, 22, 146, 'elizabeth.moore@example.com', 110, '76545', '2345666', '', NULL, 132, '2011-07-24 23:45:30.27');
INSERT INTO "Employee_history" VALUES (181, '"Employee"', '06', 'Davis Michael', 'U', 'admin', '2011-07-24 19:01:57.725', NULL, 'Davis', 'Michael', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 126, '2011-07-24 23:46:29.744');
INSERT INTO "Employee_history" VALUES (184, '"Employee"', '08', 'Wilson Barbara', 'U', 'admin', '2011-07-24 19:03:05.826', NULL, 'Wilson', 'Barbara', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 130, '2011-07-24 23:47:15.594');
INSERT INTO "Employee_history" VALUES (187, '"Employee"', '07', 'Miller Linda', 'U', 'admin', '2011-07-24 19:02:43.379', NULL, 'Miller', 'Linda', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 128, '2011-07-24 23:48:03.801');
INSERT INTO "Employee_history" VALUES (190, '"Employee"', '03', 'Williams John', 'U', 'admin', '2011-07-24 18:56:16.778', NULL, 'Williams', 'John', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 120, '2011-07-24 23:48:45.557');
INSERT INTO "Employee_history" VALUES (193, '"Employee"', '01', 'Smith James', 'U', 'admin', '2011-07-24 18:54:06.251', NULL, 'Smith', 'James', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 116, '2011-07-24 23:49:33.373');



INSERT INTO "Floor" VALUES (79, '"Floor"', 'DC01', 'Data Center - Floor 1', 'A', 'admin', '2011-07-24 18:42:21.976', NULL, 64);
INSERT INTO "Floor" VALUES (87, '"Floor"', 'B102', 'Office Building A - Floor 2', 'A', 'admin', '2011-07-24 18:43:43.349', NULL, 73);
INSERT INTO "Floor" VALUES (83, '"Floor"', 'B101', 'Office Building A - Floor 1', 'A', 'admin', '2011-07-24 18:43:49.308', NULL, 73);
INSERT INTO "Floor" VALUES (92, '"Floor"', 'B103', 'Office Building A - Floor 3', 'A', 'admin', '2011-07-24 18:44:07.204', NULL, 73);
INSERT INTO "Floor" VALUES (96, '"Floor"', 'B201', 'Office Building B - Floor 1', 'A', 'admin', '2011-07-24 18:44:21.333', NULL, 76);
INSERT INTO "Floor" VALUES (100, '"Floor"', 'B202', 'Office Building B - Floor 2', 'A', 'admin', '2011-07-24 18:44:39.015', NULL, 76);



INSERT INTO "Floor_history" VALUES (90, '"Floor"', 'B101', 'Office Building - Floor 1', 'U', 'admin', '2011-07-24 18:43:05.005', NULL, 73, 83, '2011-07-24 18:43:49.308');



INSERT INTO "Grant" VALUES (684, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Asset"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (685, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Building"', 'r', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (686, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Computer"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (687, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Employee"', 'r', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (688, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Floor"', 'r', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (690, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"License"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (691, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Monitor"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (692, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"NetworkDevice"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (693, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"NetworkPoint"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (694, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Notebook"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (695, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Office"', 'r', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (689, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Invoice"', '-', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (696, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"PC"', 'r', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (697, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Printer"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (698, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Rack"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (699, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Room"', 'r', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (700, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Server"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (701, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"UPS"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (702, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 677, '"Workplace"', 'r', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1136, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Asset"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1137, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Building"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1138, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Computer"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1139, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Employee"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1140, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Floor"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1141, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Invoice"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1142, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"License"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1143, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Monitor"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1144, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"NetworkDevice"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1145, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"NetworkPoint"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1146, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Notebook"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1147, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Office"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1148, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"PC"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1149, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Printer"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1150, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Rack"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1152, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Room"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1153, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Server"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1154, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Supplier"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1155, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"SupplierContact"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1156, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"UPS"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1157, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"User"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1158, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"Workplace"', 'w', 'Class', NULL, NULL, NULL, NULL);
INSERT INTO "Grant" VALUES (1151, '"Grant"', 'system', '2013-05-09 12:57:49.186365', NULL, NULL, 'A', NULL, 942, '"RequestForChange"', 'r', 'Class', NULL, NULL, NULL, NULL);















INSERT INTO "LookUp" VALUES (6, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'open.running', 'Avviato', 'A', NULL, 'FlowStatus', NULL, NULL, 1, true, '1459e55b-10f1-3d5f-8920-6c3b992bb693');
INSERT INTO "LookUp" VALUES (7, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'open.not_running.suspended', 'Sospeso', 'A', NULL, 'FlowStatus', NULL, NULL, 2, false, '972b3242-5316-3bcb-9d9a-f419b9b03be1');
INSERT INTO "LookUp" VALUES (65, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'IT', 'Italy', 'A', '', 'Country', NULL, NULL, 2, false, '0dbbb901-cf14-c5ec-e506-62c7fe65af79');
INSERT INTO "LookUp" VALUES (8, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'closed.completed', 'Completato', 'A', NULL, 'FlowStatus', NULL, NULL, 3, false, 'bc91b479-a6dc-d364-aa61-666e9040d51a');
INSERT INTO "LookUp" VALUES (9, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'closed.terminated', 'Terminato', 'A', NULL, 'FlowStatus', NULL, NULL, 4, false, 'd483f206-647e-5c1b-18b9-996b1c99ddaf');
INSERT INTO "LookUp" VALUES (10, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'closed.aborted', 'Interrotto', 'A', NULL, 'FlowStatus', NULL, NULL, 5, false, '34b19dd6-7b4e-626c-3935-838cbd634619');
INSERT INTO "LookUp" VALUES (330, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, '15 inches', 'A', NULL, 'Screen size', NULL, NULL, 1, false, '139c126d-0a1a-d013-473e-a85f4c06d3a9');
INSERT INTO "LookUp" VALUES (25, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'UK', 'United Kingdom', 'A', '', 'Country', NULL, NULL, 1, false, '052d2615-718d-58cd-0f18-5f7afbdc984f');
INSERT INTO "LookUp" VALUES (66, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'DE', 'Germany', 'A', '', 'Country', NULL, NULL, 3, false, 'e3ba430c-d1c9-a0af-6300-521da662f8a9');
INSERT INTO "LookUp" VALUES (67, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'FR', 'France', 'A', '', 'Country', NULL, NULL, 4, false, 'c97ce058-be65-efb7-b155-18e6aeca72a9');
INSERT INTO "LookUp" VALUES (68, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'ES', 'Spain', 'A', '', 'Country', NULL, NULL, 5, false, '5dce6b38-e498-cee1-b35a-33a1a57fa48c');
INSERT INTO "LookUp" VALUES (69, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'US', 'United States', 'A', '', 'Country', NULL, NULL, 6, false, 'cc1a8cc6-da14-0b9f-c6fe-9a7aa67730b2');
INSERT INTO "LookUp" VALUES (70, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'AT', 'Austria', 'A', '', 'Country', NULL, NULL, 7, false, 'b76fad2e-21de-3567-4e98-416fdc5f807c');
INSERT INTO "LookUp" VALUES (31, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'IBM', 'A', '', 'Brand', NULL, NULL, 1, false, '629b3b01-7c98-2f3e-622d-cca557a4479f');
INSERT INTO "LookUp" VALUES (135, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'HP', 'A', '', 'Brand', NULL, NULL, 2, false, 'd3a264d0-d27d-42d0-b528-09be8dfac8e6');
INSERT INTO "LookUp" VALUES (136, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Sony', 'A', '', 'Brand', NULL, NULL, 3, false, 'ad79e19b-cee3-0d71-3b36-0f02869a9af6');
INSERT INTO "LookUp" VALUES (137, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Cisco', 'A', '', 'Brand', NULL, NULL, 4, false, 'b926e68c-5242-22fc-5db5-02c1ae89031b');
INSERT INTO "LookUp" VALUES (138, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Acer', 'A', '', 'Brand', NULL, NULL, 5, false, 'b0947bf8-3813-f6ac-a778-7de91008f535');
INSERT INTO "LookUp" VALUES (139, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Canon', 'A', '', 'Brand', NULL, NULL, 6, false, 'c669b687-4f93-8b1e-a2e6-ddf50da417a9');
INSERT INTO "LookUp" VALUES (140, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Epson', 'A', '', 'Brand', NULL, NULL, 7, false, '9afb6d99-3b20-ade3-5017-b99223a4e065');
INSERT INTO "LookUp" VALUES (141, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Microsoft', 'A', '', 'Brand', NULL, NULL, 8, false, '7e1efc5c-4a37-413b-3648-495b47c8f804');
INSERT INTO "LookUp" VALUES (30, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'In use', 'A', '', 'Asset state', NULL, NULL, 1, false, '0d7ff6d6-ffb8-fbb1-2bd5-84473099bc92');
INSERT INTO "LookUp" VALUES (142, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'To repair', 'A', '', 'Asset state', NULL, NULL, 2, false, '8f8e88ad-7b4e-0ad7-f1ae-4b39d46f16d1');
INSERT INTO "LookUp" VALUES (143, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Scrapped', 'A', '', 'Asset state', NULL, NULL, 3, false, '5e83d019-9644-3157-8d3f-5e572ee45f83');
INSERT INTO "LookUp" VALUES (144, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Available', 'A', '', 'Asset state', NULL, NULL, 4, false, 'bf91cc9d-542b-8971-11f1-397941b68c79');
INSERT INTO "LookUp" VALUES (23, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Gold', 'A', '', 'Employee level', NULL, NULL, 1, false, 'b39a946a-007e-b325-ffb4-5d0f52aacef7');
INSERT INTO "LookUp" VALUES (145, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Platinum', 'A', '', 'Employee level', NULL, NULL, 2, false, '640d557f-eb24-fbf1-e6e9-17fb60e9d1b4');
INSERT INTO "LookUp" VALUES (146, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Silver', 'A', '', 'Employee level', NULL, NULL, 3, false, 'e07246c3-41a5-2bfa-1cdd-1353e577c7d0');
INSERT INTO "LookUp" VALUES (22, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Clerk', 'A', '', 'Employee qualification', NULL, NULL, 1, false, '7929df84-c3b4-fbab-f94b-760f1b36ed0f');
INSERT INTO "LookUp" VALUES (147, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Head office', 'A', '', 'Employee qualification', NULL, NULL, 2, false, 'b061db92-b25a-ad8e-aecd-4ac9028a8ceb');
INSERT INTO "LookUp" VALUES (148, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Manager', 'A', '', 'Employee qualification', NULL, NULL, 3, false, '51a26f29-ff12-3d9a-9e44-55ea6525071c');
INSERT INTO "LookUp" VALUES (21, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Employee', 'A', '', 'Employee type', NULL, NULL, 1, false, 'c2d423ee-db39-5eac-83e3-476955b700fa');
INSERT INTO "LookUp" VALUES (149, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'External consultant', 'A', '', 'Employee type', NULL, NULL, 2, false, '959dc753-587d-50cc-5814-a5d8aff27380');
INSERT INTO "LookUp" VALUES (150, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Stage', 'A', '', 'Employee type', NULL, NULL, 3, false, '714a717b-cc27-f796-47ca-63b8cd2292f6');
INSERT INTO "LookUp" VALUES (24, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Active', 'A', '', 'Employee state', NULL, NULL, 1, false, '3c7a7490-7952-220b-4c87-8a6c3968c6fb');
INSERT INTO "LookUp" VALUES (151, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Inactive', 'A', '', 'Employee state', NULL, NULL, 2, false, '0b2e83da-9905-2ce0-0ee2-068094a15b4c');
INSERT INTO "LookUp" VALUES (152, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Suspended', 'A', '', 'Employee state', NULL, NULL, 3, false, '960d490f-efbe-b38b-501e-cf189ea44a23');
INSERT INTO "LookUp" VALUES (331, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, '17 inches', 'A', NULL, 'Screen size', NULL, NULL, 2, false, '401435b6-456a-d391-f21c-bd0a11657668');
INSERT INTO "LookUp" VALUES (26, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Sales', 'A', '', 'Invoice type', NULL, NULL, 1, false, '72edca53-6c9b-f913-5814-b5b6af12ab6b');
INSERT INTO "LookUp" VALUES (153, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Credit memo', 'A', '', 'Invoice type', NULL, NULL, 2, false, 'c15e77fa-f5e3-f428-4b7e-7b3d96b01131');
INSERT INTO "LookUp" VALUES (27, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Office', 'A', '', 'Room usage type', NULL, NULL, 1, false, '8d1d7b55-7eec-5e0f-85c2-ec391351b27e');
INSERT INTO "LookUp" VALUES (154, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Warehouse', 'A', '', 'Room usage type', NULL, NULL, 2, false, '948e43e1-0d1c-a222-cbcd-e0ddf5640ec6');
INSERT INTO "LookUp" VALUES (155, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Meeting room', 'A', '', 'Room usage type', NULL, NULL, 3, false, '97b5d71d-f85b-ae80-cd0f-7cbdf97221de');
INSERT INTO "LookUp" VALUES (156, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Training room', 'A', '', 'Room usage type', NULL, NULL, 4, false, 'adacdaaa-953b-f49e-ad35-b21d76b68e0e');
INSERT INTO "LookUp" VALUES (157, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Laboratory', 'A', '', 'Room usage type', NULL, NULL, 5, false, '968ffa48-726a-76a1-b3ed-33109c242b22');
INSERT INTO "LookUp" VALUES (28, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Manufacturer', 'A', '', 'Supplier type', NULL, NULL, 1, false, '0d70890f-3630-3f46-efa7-f3981b9893e9');
INSERT INTO "LookUp" VALUES (158, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Distributor', 'A', '', 'Supplier type', NULL, NULL, 2, false, '9ac7246d-3798-b231-d1ec-a5379d2a84a0');
INSERT INTO "LookUp" VALUES (32, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Hardware', 'A', '', 'Technical reference role', NULL, NULL, 1, false, 'd0147df2-262e-752f-b92a-348ad8567f1f');
INSERT INTO "LookUp" VALUES (159, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Systemistic', 'A', '', 'Technical reference role', NULL, NULL, 2, false, '460f12ae-0480-5b4c-bbe3-cf7d39fbe3da');
INSERT INTO "LookUp" VALUES (160, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Applicative', 'A', '', 'Technical reference role', NULL, NULL, 3, false, '860497ec-f48f-5d35-53d4-d4af723e44c7');
INSERT INTO "LookUp" VALUES (161, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Security', 'A', '', 'Technical reference role', NULL, NULL, 4, false, '9e3f28c1-53c2-cb2e-bb66-cc8adf45e487');
INSERT INTO "LookUp" VALUES (29, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Single user', 'A', '', 'Workplace type', NULL, NULL, 1, false, '1fcda2a8-db7c-4900-e874-67c04a9e9796');
INSERT INTO "LookUp" VALUES (162, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Multiuser', 'A', '', 'Workplace type', NULL, NULL, 2, false, 'c364705e-22aa-922f-3df7-e5b099be14d2');
INSERT INTO "LookUp" VALUES (163, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '', 'Public', 'A', '', 'Workplace type', NULL, NULL, 3, false, 'd53211c5-bc5f-bd3b-f271-6e4e1a6b69b3');
INSERT INTO "LookUp" VALUES (279, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'CA', 'Canada', 'A', NULL, 'Country', NULL, NULL, 8, false, 'b0a5bbbb-8d48-f861-e66b-7b3c777fe4a2');
INSERT INTO "LookUp" VALUES (327, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'RAID 1', 'A', NULL, 'RAID', NULL, NULL, 1, false, '3a6a867e-2269-ae9a-3d14-db6c145d0305');
INSERT INTO "LookUp" VALUES (328, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'RAID 2', 'A', NULL, 'RAID', NULL, NULL, 2, false, 'd699ce49-8fe9-c036-0146-a633ed522c9e');
INSERT INTO "LookUp" VALUES (329, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'RAID 5', 'A', NULL, 'RAID', NULL, NULL, 3, false, 'b47e3356-c3f3-521b-5a03-7c99c4af9d7c');
INSERT INTO "LookUp" VALUES (332, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, '19 inches', 'A', NULL, 'Screen size', NULL, NULL, 3, false, '8eba54f0-e1a1-64d3-18b2-524ef60bf4a9');
INSERT INTO "LookUp" VALUES (333, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, '16 inches', 'A', NULL, 'Screen size', NULL, NULL, 4, false, 'e48f08cf-2be2-cd0b-bb31-b4d48874afbb');
INSERT INTO "LookUp" VALUES (334, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, '13 inches', 'A', NULL, 'Screen size', NULL, NULL, 5, false, 'd37dbaa9-c3bb-8d24-53bd-1b734c9b936a');
INSERT INTO "LookUp" VALUES (335, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, '21 inches', 'A', NULL, 'Screen size', NULL, NULL, 6, false, 'df122985-ec9c-e62d-7748-ff1d83384373');
INSERT INTO "LookUp" VALUES (393, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '2', 'CRT', 'A', NULL, 'Monitor type', NULL, NULL, 2, false, '7a655fb0-d0e6-7e3b-9a21-9458b723ee9f');
INSERT INTO "LookUp" VALUES (395, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'A4', 'A', NULL, 'Paper size', NULL, NULL, 1, false, 'a67f8e47-fbc9-215e-96ab-0cf20ddcb3ed');
INSERT INTO "LookUp" VALUES (396, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'A3', 'A', NULL, 'Paper size', NULL, NULL, 2, false, '1eb34d55-6fe4-8cdd-1fc5-e91b612629e5');
INSERT INTO "LookUp" VALUES (397, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'A0', 'A', NULL, 'Paper size', NULL, NULL, 3, false, 'c2cdf7d7-84c5-80ec-4602-b7f2c2669328');
INSERT INTO "LookUp" VALUES (398, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Laser', 'A', NULL, 'Printer type', NULL, NULL, 1, false, 'fb7e58a8-b355-51fa-ca04-68951df68aa1');
INSERT INTO "LookUp" VALUES (399, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Inkjet', 'A', NULL, 'Printer type', NULL, NULL, 2, false, '0949a662-c06c-eb10-cb82-04250c6676d9');
INSERT INTO "LookUp" VALUES (400, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Thermal', 'A', NULL, 'Printer type', NULL, NULL, 3, false, '25866c2b-105f-f36b-77a5-2b67af474253');
INSERT INTO "LookUp" VALUES (401, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Impact', 'A', NULL, 'Printer type', NULL, NULL, 4, false, '0d2df1eb-2d8d-4bc7-3e21-06e857a0e658');
INSERT INTO "LookUp" VALUES (402, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Plotter', 'A', NULL, 'Printer type', NULL, NULL, 5, false, 'c16a46ae-e652-b372-e8e1-6895210b7be8');
INSERT INTO "LookUp" VALUES (403, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Local', 'A', NULL, 'Printer usage', NULL, NULL, 1, false, 'dd72ce56-0944-e32b-7c55-8206da45dbd9');
INSERT INTO "LookUp" VALUES (404, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Network', 'A', NULL, 'Printer usage', NULL, NULL, 2, false, 'c44b3089-6819-fa15-21b8-95bedba7081e');
INSERT INTO "LookUp" VALUES (405, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Personal productivity software', 'A', NULL, 'License category', NULL, NULL, 1, false, '829276da-537a-a987-9ac0-c756dd731fb7');
INSERT INTO "LookUp" VALUES (406, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Enterprise software', 'A', NULL, 'License category', NULL, NULL, 2, false, '7f510d2a-b229-9562-67bc-976f299b8040');
INSERT INTO "LookUp" VALUES (407, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Technical software', 'A', NULL, 'License category', NULL, NULL, 3, false, 'a49af884-cdcb-3e9c-4c99-4758125136cc');
INSERT INTO "LookUp" VALUES (408, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Router', 'A', NULL, 'Network device type', NULL, NULL, 1, false, '914450cc-2a16-4f42-a607-fa3fefd21c0d');
INSERT INTO "LookUp" VALUES (409, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Switch', 'A', NULL, 'Network device type', NULL, NULL, 2, false, '2ffb2547-d25e-5f0c-0679-6e608207b6be');
INSERT INTO "LookUp" VALUES (410, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Access point', 'A', NULL, 'Network device type', NULL, NULL, 3, false, 'a17ab276-bfd9-aa19-a373-547c06901e8c');
INSERT INTO "LookUp" VALUES (394, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '3', 'Plasma', 'A', NULL, 'Monitor type', NULL, NULL, 3, false, 'cb046e50-8403-7012-9a09-a78c9792a96c');
INSERT INTO "LookUp" VALUES (411, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Patch panel', 'A', NULL, 'Network device type', NULL, NULL, 4, false, '88f1604c-601e-5caa-724f-6176589f1cd1');
INSERT INTO "LookUp" VALUES (482, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Red', 'A', NULL, 'Cable color', NULL, NULL, 1, false, '2ff8c977-0cd6-40f4-50ae-869a928711cc');
INSERT INTO "LookUp" VALUES (483, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Black', 'A', NULL, 'Cable color', NULL, NULL, 2, false, '0bd1883f-735b-034a-d3c1-9b88e7121a0b');
INSERT INTO "LookUp" VALUES (484, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'White', 'A', NULL, 'Cable color', NULL, NULL, 3, false, '1232e2c4-17b8-00d9-f7c5-f6c957253a72');
INSERT INTO "LookUp" VALUES (485, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Yellow', 'A', NULL, 'Cable color', NULL, NULL, 4, false, 'ea5b4aea-b88e-f768-64ce-dad7b01d93ca');
INSERT INTO "LookUp" VALUES (486, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Blue', 'A', NULL, 'Cable color', NULL, NULL, 5, false, '38dc2f2a-206b-28df-7644-a35d9e4d7a44');
INSERT INTO "LookUp" VALUES (487, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Green', 'A', NULL, 'Cable color', NULL, NULL, 6, false, '76f92a48-b8df-5061-e230-58a7d5f52ae6');
INSERT INTO "LookUp" VALUES (488, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Cyan', 'A', NULL, 'Cable color', NULL, NULL, 7, false, 'e4f1e208-a8a5-140a-90c7-3690035a466b');
INSERT INTO "LookUp" VALUES (489, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Brown', 'A', NULL, 'Cable color', NULL, NULL, 8, false, '00dee871-9120-7827-045b-1322e86c7eb4');
INSERT INTO "LookUp" VALUES (490, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Gray', 'A', NULL, 'Cable color', NULL, NULL, 9, false, 'b0f39b7c-b7a3-580a-9471-c2f8c85f6290');
INSERT INTO "LookUp" VALUES (491, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Orange', 'A', NULL, 'Cable color', NULL, NULL, 10, false, 'f496a7ad-48dc-cc9c-7475-d7d9b94248ad');
INSERT INTO "LookUp" VALUES (492, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Pink', 'A', NULL, 'Cable color', NULL, NULL, 11, false, '795a55dd-e604-c74a-5c7b-37c6e072001d');
INSERT INTO "LookUp" VALUES (493, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Magenta', 'A', NULL, 'Cable color', NULL, NULL, 12, false, 'bfc70dbc-5b99-9c04-6f4f-c4c0a687a08f');
INSERT INTO "LookUp" VALUES (392, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', '1', 'LCD', 'A', NULL, 'Monitor type', NULL, NULL, 1, false, 'd0ad9eaf-e0d1-9280-6106-ec0bb6a6110d');
INSERT INTO "LookUp" VALUES (703, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Document', 'A', NULL, 'AlfrescoCategory', NULL, NULL, 1, false, 'd29b9b79-6da0-acfd-b256-6cdcfb967d89');
INSERT INTO "LookUp" VALUES (704, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', NULL, 'Image', 'A', NULL, 'AlfrescoCategory', NULL, NULL, 2, false, '607bd263-02f1-95a6-1e91-695f4007710b');
INSERT INTO "LookUp" VALUES (917, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'REC_RFC', 'Registered', 'A', NULL, 'RFC status', NULL, NULL, 1, false, '1d49b68a-2340-ab6b-f622-9d1b3632e403');
INSERT INTO "LookUp" VALUES (920, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'REQ_EXE', 'Execution requested', 'A', NULL, 'RFC status', NULL, NULL, 4, false, 'ee169dcf-b70c-c87c-dc79-99355b3dfef4');
INSERT INTO "LookUp" VALUES (921, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'IN_EXE', 'Implementation', 'A', NULL, 'RFC status', NULL, NULL, 5, false, '26f27ee8-eb2e-630c-f03c-6126f6645726');
INSERT INTO "LookUp" VALUES (922, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'OUT_EXE', 'Performed', 'A', NULL, 'RFC status', NULL, NULL, 6, false, 'fac32802-7cd4-dc94-f2dc-534a782d22ae');
INSERT INTO "LookUp" VALUES (923, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'CLOSED', 'Closed', 'A', NULL, 'RFC status', NULL, NULL, 7, false, 'cb4e6ac9-548d-7356-8844-fa33fe8783d9');
INSERT INTO "LookUp" VALUES (924, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'FPC', 'Formatting PC', 'A', NULL, 'RFC Category', NULL, NULL, 1, false, 'a1fe27e3-3a65-b105-34f4-fbc516e080b4');
INSERT INTO "LookUp" VALUES (925, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'ISE', 'External software installation', 'A', NULL, 'RFC Category', NULL, NULL, 2, false, '0e19830c-3802-fdfc-5782-11fa969e525d');
INSERT INTO "LookUp" VALUES (926, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'ARI', 'Internet access', 'A', NULL, 'RFC Category', NULL, NULL, 3, false, '6ea53ee6-fde6-ff9a-dd00-7a5c4a555bc8');
INSERT INTO "LookUp" VALUES (927, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'MIR', 'Modify IP address', 'A', NULL, 'RFC Category', NULL, NULL, 4, false, 'e56c24cb-9a4c-d2b4-8764-83f0b535e63e');
INSERT INTO "LookUp" VALUES (928, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'NU_ERP', 'Create new ERP user', 'A', NULL, 'RFC Category', NULL, NULL, 5, false, '7b760cde-b504-2144-a5b5-889e0e997d47');
INSERT INTO "LookUp" VALUES (929, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'NU_CRM', 'Create new CRM user', 'A', NULL, 'RFC Category', NULL, NULL, 6, false, 'a42aa48f-bdec-f2ca-fb51-39ff8a03b1d5');
INSERT INTO "LookUp" VALUES (930, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'NA', 'Not applicable', 'A', NULL, 'RFC Category', NULL, NULL, 7, false, '16bc6c6e-8964-687c-8095-d6a8deabe2e4');
INSERT INTO "LookUp" VALUES (931, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'HI', 'High', 'A', NULL, 'RFC priority', NULL, NULL, 1, false, '2b3b83b6-8f3a-2680-6565-f5b3b1aa0a77');
INSERT INTO "LookUp" VALUES (932, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'MID', 'Medium', 'A', NULL, 'RFC priority', NULL, NULL, 2, false, 'f656e2b4-cb6a-526b-8cc7-c94a3e03f83c');
INSERT INTO "LookUp" VALUES (933, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'LOW', 'Low', 'A', NULL, 'RFC priority', NULL, NULL, 3, false, 'b5498de0-38f9-5b52-68a6-8c109f2f5dce');
INSERT INTO "LookUp" VALUES (934, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'ACCEPTED', 'Accepted', 'A', NULL, 'RFC formal evaluation', NULL, NULL, 1, false, '84358751-9d93-4425-1feb-78437c11dd99');
INSERT INTO "LookUp" VALUES (935, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'REJECTED', 'Rejected', 'A', NULL, 'RFC formal evaluation', NULL, NULL, 2, false, '7d60b944-dcbc-0167-44eb-bfb0e6d29341');
INSERT INTO "LookUp" VALUES (936, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'APPROVED', 'Approved', 'A', NULL, 'RFC decision', NULL, NULL, 1, false, '8a02b082-a2ce-51b8-e7a4-80d0c037d68f');
INSERT INTO "LookUp" VALUES (937, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'NOT_APPROVED', 'Not approved', 'A', NULL, 'RFC decision', NULL, NULL, 2, false, '81d3aa16-1eea-7b5a-0e0e-0031b4641076');
INSERT INTO "LookUp" VALUES (938, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'POSITIVE', 'Positive', 'A', NULL, 'RFC final result', NULL, NULL, 1, false, 'c8dacdf9-723c-34a7-b595-74d766755ac9');
INSERT INTO "LookUp" VALUES (939, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'NEGATIVE', 'Negative', 'A', NULL, 'RFC final result', NULL, NULL, 2, false, 'e72ba740-f2dc-59e5-65fc-5d10b832716f');
INSERT INTO "LookUp" VALUES (918, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'REQ_DOC', 'Analysis requested', 'A', NULL, 'RFC status', NULL, NULL, 2, false, 'e9713270-4e94-7ce5-fafe-8c97e6045ba0');
INSERT INTO "LookUp" VALUES (919, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'PRE_DOC', 'Analysis in progress', 'A', NULL, 'RFC status', NULL, NULL, 3, false, '7abfa6ce-a247-d024-eb1e-675b648e74d0');
INSERT INTO "LookUp" VALUES (1, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'New', 'New', 'A', NULL, 'EmailStatus', NULL, NULL, 1, false, '46505694-ded3-2378-542d-1517d41aedff');
INSERT INTO "LookUp" VALUES (2, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'Received', 'Received', 'A', NULL, 'EmailStatus', NULL, NULL, 2, false, 'ce59a792-5f3d-6a8e-7c42-229503ee6cca');
INSERT INTO "LookUp" VALUES (3, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'Draft', 'Draft', 'A', NULL, 'EmailStatus', NULL, NULL, 3, false, '69eca148-ae9f-e1e6-1ebe-d669e0a8a4b3');
INSERT INTO "LookUp" VALUES (4, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'Outgoing', 'Outgoing', 'A', NULL, 'EmailStatus', NULL, NULL, 4, false, '736a05ff-4f54-86ba-9e0d-7b8ab0b97dfa');
INSERT INTO "LookUp" VALUES (5, '"LookUp"', NULL, '2013-05-09 12:57:48.985726', 'Sent', 'Sent', 'A', NULL, 'EmailStatus', NULL, NULL, 5, false, '2bc4ddb8-99c0-beb2-31f6-86e02c0ea8d1');












INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 120, '"PC"', 518, 'A', 'admin', '2011-08-23 17:26:13.647', NULL, 520);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 126, '"PC"', 526, 'A', 'admin', '2011-08-23 17:28:42.292', NULL, 528);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 128, '"PC"', 534, 'A', 'admin', '2011-08-23 17:29:52.21', NULL, 536);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 130, '"PC"', 542, 'A', 'admin', '2011-08-23 17:32:51.564', NULL, 544);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 134, '"Monitor"', 550, 'A', 'admin', '2011-08-23 17:34:12.416', NULL, 553);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 128, '"Monitor"', 555, 'A', 'admin', '2011-08-23 17:35:03.944', NULL, 557);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 130, '"Monitor"', 561, 'A', 'admin', '2011-08-23 17:36:00.497', NULL, 563);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 118, '"Monitor"', 567, 'A', 'admin', '2011-08-23 17:36:50.525', NULL, 569);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 132, '"Monitor"', 573, 'A', 'admin', '2011-08-23 17:37:57.173', NULL, 575);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 130, '"Printer"', 579, 'A', 'admin', '2011-08-23 17:38:55.033', NULL, 581);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 120, '"Printer"', 585, 'A', 'admin', '2011-08-23 17:39:42.706', NULL, 587);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 122, '"Printer"', 591, 'A', 'admin', '2011-08-23 17:40:48.481', NULL, 593);






INSERT INTO "Map_AssetReference" VALUES ('"Map_AssetReference"', '"Employee"', 116, '"PC"', 526, 'A', 'admin', '2011-08-23 17:28:42.292', NULL, 532, NULL);
INSERT INTO "Map_AssetReference" VALUES ('"Map_AssetReference"', '"Employee"', 134, '"PC"', 534, 'A', 'admin', '2011-08-23 17:29:52.21', NULL, 540, NULL);
INSERT INTO "Map_AssetReference" VALUES ('"Map_AssetReference"', '"Employee"', 116, '"PC"', 542, 'A', 'admin', '2011-08-23 17:32:51.564', NULL, 548, NULL);
INSERT INTO "Map_AssetReference" VALUES ('"Map_AssetReference"', '"Employee"', 116, '"PC"', 518, 'A', 'admin', '2011-08-29 12:37:39.83', NULL, 524, 32);



INSERT INTO "Map_AssetReference_history" VALUES ('"Map_AssetReference"', '"Employee"', 116, '"PC"', 518, 'U', 'admin', '2011-08-23 17:26:13.647', '2011-08-29 12:37:39.83', 524, NULL);



INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 64, '"Floor"', 79, 'A', 'admin', '2011-07-24 18:42:21.976', NULL, 81);
INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 73, '"Floor"', 83, 'A', 'admin', '2011-07-24 18:43:05.005', NULL, 85);
INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 73, '"Floor"', 87, 'A', 'admin', '2011-07-24 18:43:43.349', NULL, 89);
INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 73, '"Floor"', 92, 'A', 'admin', '2011-07-24 18:44:07.204', NULL, 94);
INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 76, '"Floor"', 96, 'A', 'admin', '2011-07-24 18:44:21.333', NULL, 98);
INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 76, '"Floor"', 100, 'A', 'admin', '2011-07-24 18:44:39.015', NULL, 102);






INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 79, '"Room"', 104, 'A', 'admin', '2011-07-24 18:45:44.718', NULL, 106);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 79, '"Room"', 200, 'A', 'admin', '2011-07-24 23:51:13.304', NULL, 202);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 83, '"Room"', 206, 'A', 'admin', '2011-07-24 23:56:14.609', NULL, 208);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 83, '"Room"', 212, 'A', 'admin', '2011-07-24 23:56:56.466', NULL, 214);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 83, '"Room"', 218, 'A', 'admin', '2011-07-24 23:57:24.774', NULL, 220);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 87, '"Room"', 224, 'A', 'admin', '2011-07-24 23:57:56.042', NULL, 226);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 87, '"Room"', 230, 'A', 'admin', '2011-07-24 23:58:29.941', NULL, 232);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 92, '"Room"', 236, 'A', 'admin', '2011-07-24 23:59:12.074', NULL, 238);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 96, '"Room"', 242, 'A', 'admin', '2011-07-24 23:59:40.137', NULL, 244);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 96, '"Room"', 248, 'A', 'admin', '2011-07-25 00:00:13.196', NULL, 250);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 96, '"Room"', 254, 'A', 'admin', '2011-07-25 00:00:42.222', NULL, 256);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 100, '"Room"', 260, 'A', 'admin', '2011-07-25 00:01:29.684', NULL, 262);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 100, '"Room"', 266, 'A', 'admin', '2011-07-25 00:01:52.818', NULL, 268);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 100, '"Room"', 272, 'A', 'admin', '2011-07-25 00:02:19.16', NULL, 274);






INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 108, '"Employee"', 134, 'A', 'admin', '2011-07-24 23:35:18.412', NULL, 166);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 108, '"Employee"', 118, 'A', 'admin', '2011-07-24 23:36:23.281', NULL, 169);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 110, '"Employee"', 132, 'A', 'admin', '2011-07-24 23:40:39.563', NULL, 173);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 110, '"Employee"', 124, 'A', 'admin', '2011-07-24 23:43:44.824', NULL, 176);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 112, '"Employee"', 122, 'A', 'admin', '2011-07-24 23:45:11.466', NULL, 179);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 110, '"Employee"', 126, 'A', 'admin', '2011-07-24 23:46:29.744', NULL, 183);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 112, '"Employee"', 130, 'A', 'admin', '2011-07-24 23:47:15.594', NULL, 186);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 108, '"Employee"', 128, 'A', 'admin', '2011-07-24 23:48:03.801', NULL, 189);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 108, '"Employee"', 120, 'A', 'admin', '2011-07-24 23:48:45.557', NULL, 192);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 112, '"Employee"', 116, 'A', 'admin', '2011-07-24 23:49:33.373', NULL, 195);






INSERT INTO "Map_NetworkDeviceConnection" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'N', 'admin', '2011-09-02 12:18:14.794', NULL, 761, 7, 490);
INSERT INTO "Map_NetworkDeviceConnection" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'N', 'admin', '2011-09-02 12:19:16.945', NULL, 765, 4, 492);
INSERT INTO "Map_NetworkDeviceConnection" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'N', 'admin', '2011-09-02 15:15:53.993', NULL, 767, 5, 492);
INSERT INTO "Map_NetworkDeviceConnection" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'N', 'admin', '2011-09-02 15:17:32.047', NULL, 769, 3, 489);
INSERT INTO "Map_NetworkDeviceConnection" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 747, 'A', 'admin', '2011-09-02 15:17:43.319', NULL, 771, 5, 490);



INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 747, 'U', 'admin', '2011-09-02 12:10:10.378', '2011-09-02 12:12:14.952', 761, 4, 487);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'U', 'admin', '2011-09-02 12:12:14.952', '2011-09-02 12:17:42.029', 761, 5, 487);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'U', 'admin', '2011-09-02 12:17:42.029', '2011-09-02 12:18:14.794', 761, 7, 490);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 747, 'U', 'admin', '2011-09-02 12:18:31.48', '2011-09-02 12:18:39.058', 765, 3, 492);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'U', 'admin', '2011-09-02 12:18:39.058', '2011-09-02 12:19:16.945', 765, 4, 492);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 747, 'U', 'admin', '2011-09-02 12:20:29.104', '2011-09-02 12:20:40.731', 767, 4, 492);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'U', 'admin', '2011-09-02 12:20:40.731', '2011-09-02 15:15:53.993', 767, 5, 492);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 747, 'U', 'admin', '2011-09-02 15:16:37.924', '2011-09-02 15:16:57.895', 769, 4, 489);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'U', 'admin', '2011-09-02 15:16:57.895', '2011-09-02 15:17:32.047', 769, 3, 489);



INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 104, 'A', 'admin', '2011-07-24 23:50:09.333', NULL, 198);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 200, 'A', 'admin', '2011-07-24 23:51:13.304', NULL, 204);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 206, 'A', 'admin', '2011-07-24 23:56:14.609', NULL, 210);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 212, 'A', 'admin', '2011-07-24 23:56:56.466', NULL, 216);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 218, 'A', 'admin', '2011-07-24 23:57:24.774', NULL, 222);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 224, 'A', 'admin', '2011-07-24 23:57:56.042', NULL, 228);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 230, 'A', 'admin', '2011-07-24 23:58:29.941', NULL, 234);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 112, '"Room"', 236, 'A', 'admin', '2011-07-24 23:59:12.074', NULL, 240);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 242, 'A', 'admin', '2011-07-24 23:59:40.137', NULL, 246);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 248, 'A', 'admin', '2011-07-25 00:00:13.196', NULL, 252);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 112, '"Room"', 266, 'A', 'admin', '2011-07-25 00:01:52.818', NULL, 270);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 260, 'A', 'admin', '2011-09-02 11:53:26.9', NULL, 264);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 272, 'A', 'admin', '2011-09-02 11:54:54.974', NULL, 276);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 254, 'A', 'admin', '2011-09-02 11:56:58.957', NULL, 258);



INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 112, '"Room"', 260, 'U', 'admin', '2011-07-25 00:01:29.684', '2011-09-02 11:53:26.9', 264);
INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 260, 'U', 'admin', '2011-09-02 11:53:26.9', '2011-09-02 11:53:26.9', 264);
INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 112, '"Room"', 272, 'U', 'admin', '2011-07-25 00:02:19.16', '2011-09-02 11:54:54.974', 276);
INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 272, 'U', 'admin', '2011-09-02 11:54:54.974', '2011-09-02 11:54:54.974', 276);
INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 254, 'U', 'admin', '2011-07-25 00:00:42.222', '2011-09-02 11:56:58.957', 258);
INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 254, 'U', 'admin', '2011-09-02 11:56:58.957', '2011-09-02 11:56:58.957', 258);





















INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 236, '"PC"', 518, 'A', 'admin', '2011-08-23 17:26:13.647', NULL, 522);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 248, '"PC"', 526, 'A', 'admin', '2011-08-23 17:28:42.292', NULL, 530);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 104, '"PC"', 534, 'A', 'admin', '2011-08-23 17:29:52.21', NULL, 538);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 272, '"PC"', 542, 'A', 'admin', '2011-08-23 17:32:51.564', NULL, 546);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 272, '"Monitor"', 555, 'A', 'admin', '2011-08-23 17:35:03.944', NULL, 559);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 242, '"Monitor"', 561, 'A', 'admin', '2011-08-23 17:36:00.497', NULL, 565);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 230, '"Monitor"', 567, 'A', 'admin', '2011-08-23 17:36:50.525', NULL, 571);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 272, '"Monitor"', 573, 'A', 'admin', '2011-08-23 17:37:57.173', NULL, 577);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 242, '"Printer"', 579, 'A', 'admin', '2011-08-23 17:38:55.033', NULL, 583);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 212, '"Printer"', 585, 'A', 'admin', '2011-08-23 17:39:42.706', NULL, 589);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 266, '"Printer"', 591, 'A', 'admin', '2011-08-23 17:40:48.481', NULL, 595);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 200, '"NetworkDevice"', 747, 'A', 'admin', '2011-09-02 12:06:33.699', NULL, 749);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 104, '"NetworkDevice"', 755, 'A', 'admin', '2011-09-02 12:08:39.585', NULL, 757);
























INSERT INTO "Map_SupplierAsset" VALUES ('"Map_SupplierAsset"', '"Supplier"', 714, '"PC"', 526, 'N', 'admin', '2011-08-29 13:07:08.776', NULL, 717);
INSERT INTO "Map_SupplierAsset" VALUES ('"Map_SupplierAsset"', '"Supplier"', 723, '"NetworkDevice"', 747, 'A', 'admin', '2011-09-02 12:06:33.699', NULL, 751);
INSERT INTO "Map_SupplierAsset" VALUES ('"Map_SupplierAsset"', '"Supplier"', 723, '"NetworkDevice"', 755, 'A', 'admin', '2011-09-02 12:08:39.585', NULL, 759);
INSERT INTO "Map_SupplierAsset" VALUES ('"Map_SupplierAsset"', '"Supplier"', 723, '"PC"', 526, 'N', 'admin', '2012-08-25 12:39:36.099', NULL, 725);
INSERT INTO "Map_SupplierAsset" VALUES ('"Map_SupplierAsset"', '"Supplier"', 723, '"PC"', 526, 'A', 'admin', '2012-08-25 12:41:15.881', NULL, 1254);



INSERT INTO "Map_SupplierAsset_history" VALUES ('"Map_SupplierAsset"', '"Supplier"', 714, '"PC"', 526, 'U', 'admin', '2011-08-29 13:03:27.919', '2011-08-29 13:07:08.776', 717);
INSERT INTO "Map_SupplierAsset_history" VALUES ('"Map_SupplierAsset"', '"Supplier"', 723, '"PC"', 526, 'U', 'admin', '2011-08-29 13:27:49.732', '2012-08-25 12:39:36.099', 725);















INSERT INTO "Map_UserRole" VALUES ('"Map_UserRole"', '"User"', 13, '"Role"', 14, 'A', 'system', '2011-03-16 11:15:37.266624', NULL, 16, NULL);
INSERT INTO "Map_UserRole" VALUES ('"Map_UserRole"', '"User"', 678, '"Role"', 677, 'A', 'admin', '2011-08-23 22:41:46.419', NULL, 681, NULL);
INSERT INTO "Map_UserRole" VALUES ('"Map_UserRole"', '"User"', 679, '"Role"', 677, 'A', 'admin', '2011-08-23 22:41:46.632', NULL, 683, NULL);
INSERT INTO "Map_UserRole" VALUES ('"Map_UserRole"', '"User"', 943, '"Role"', 942, 'A', 'admin', '2012-08-24 10:22:41.248', NULL, 945, NULL);












INSERT INTO "Menu" VALUES (1067, '"Menu"', '348fdc1c-991f-3d5c-152b-d8450732c5b6', 'Dashboard', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 0, NULL, 0, 30, 'folder', '*');
INSERT INTO "Menu" VALUES (1073, '"Menu"', '362f548e-6d49-65d3-3e84-e1b472e5bf25', 'Basic archives', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 0, NULL, 0, 33, 'folder', '*');
INSERT INTO "Menu" VALUES (1075, '"Menu"', '42c16c5e-46e6-e9a0-0a15-e591d759d997', 'Employee', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1073, '"Employee"', 0, 34, 'class', '*');
INSERT INTO "Menu" VALUES (1077, '"Menu"', 'bd71fed8-971b-3be1-0436-60eaa52a6c77', 'Office', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1073, '"Office"', 0, 35, 'class', '*');
INSERT INTO "Menu" VALUES (1079, '"Menu"', '96848206-7632-f5f5-df04-f4bda50cdb13', 'Workplace', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1073, '"Workplace"', 0, 36, 'class', '*');
INSERT INTO "Menu" VALUES (1081, '"Menu"', '5ca60188-6184-ad5b-c45a-35ef42a9e556', 'Purchases', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 0, NULL, 0, 37, 'folder', '*');
INSERT INTO "Menu" VALUES (1083, '"Menu"', '26484886-f179-f0e7-b30b-3d4dee5b53b0', 'Supplier', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1081, '"Supplier"', 0, 38, 'class', '*');
INSERT INTO "Menu" VALUES (1085, '"Menu"', 'ae8aa7b3-a312-3d5a-a8d8-d310816a939e', 'SupplierContact', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1081, '"SupplierContact"', 0, 39, 'class', '*');
INSERT INTO "Menu" VALUES (1087, '"Menu"', '3a377830-fdb3-d0b6-766e-49944cab3514', 'Invoice', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1081, '"Invoice"', 0, 40, 'class', '*');
INSERT INTO "Menu" VALUES (1089, '"Menu"', '476fd463-bece-0caa-91e2-8db21b485e81', 'Locations', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 0, NULL, 0, 41, 'folder', '*');
INSERT INTO "Menu" VALUES (1091, '"Menu"', 'a4ac5ff9-b8d8-e805-11fd-14217d1519a5', 'Building', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1089, '"Building"', 0, 42, 'class', '*');
INSERT INTO "Menu" VALUES (1093, '"Menu"', 'b31ee5ec-2187-5553-5e79-0958e1a33a69', 'Room', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1089, '"Room"', 0, 43, 'class', '*');
INSERT INTO "Menu" VALUES (1095, '"Menu"', 'c8f77c1c-1c12-0761-6ba2-90d3622a4a42', 'Floor', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1089, '"Floor"', 0, 44, 'class', '*');
INSERT INTO "Menu" VALUES (1097, '"Menu"', '4aea351e-142a-d1fd-8416-279f46826005', 'Network point', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1089, '"NetworkPoint"', 0, 45, 'class', '*');
INSERT INTO "Menu" VALUES (1099, '"Menu"', 'ff1143a7-e29e-1ca9-7bf7-4e6db37d25cc', 'Assets', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 0, NULL, 0, 46, 'folder', '*');
INSERT INTO "Menu" VALUES (1101, '"Menu"', 'ad0e8d0b-99f3-8b22-3b88-0d645f1fcc9b', 'Asset', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"Asset"', 0, 47, 'class', '*');
INSERT INTO "Menu" VALUES (1103, '"Menu"', 'a0e29c06-187b-0c0c-a4ee-90198bdc2ae7', 'Computer', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"Computer"', 0, 48, 'class', '*');
INSERT INTO "Menu" VALUES (1105, '"Menu"', '3eb83eac-fd68-91f3-2144-0f421959851e', 'PC', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"PC"', 0, 49, 'class', '*');
INSERT INTO "Menu" VALUES (1107, '"Menu"', 'd8db87b3-c979-cd35-4d37-007ee0d3aaf1', 'Notebook', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"Notebook"', 0, 50, 'class', '*');
INSERT INTO "Menu" VALUES (1109, '"Menu"', '576d5997-cc36-b400-3ee1-87adf4dadb10', 'Server', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"Server"', 0, 51, 'class', '*');
INSERT INTO "Menu" VALUES (1111, '"Menu"', '1d72017f-e3d3-e951-a01d-55465008925b', 'Monitor', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"Monitor"', 0, 52, 'class', '*');
INSERT INTO "Menu" VALUES (1113, '"Menu"', '7147560b-7d6c-7881-1967-7fd34f795c64', 'Printer', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"Printer"', 0, 53, 'class', '*');
INSERT INTO "Menu" VALUES (1115, '"Menu"', '605bf059-ea40-dfee-d9bb-c205b62b0c54', 'NetworkDevice', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"NetworkDevice"', 0, 54, 'class', '*');
INSERT INTO "Menu" VALUES (1117, '"Menu"', 'c34f9b58-506f-5322-5f67-c6eeaa04b401', 'Rack', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"Rack"', 0, 55, 'class', '*');
INSERT INTO "Menu" VALUES (1119, '"Menu"', '2f801567-38f5-4842-dab3-4c5c39a576da', 'UPS', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"UPS"', 0, 56, 'class', '*');
INSERT INTO "Menu" VALUES (1121, '"Menu"', 'd2abd49a-7131-410b-eb8b-39eb219a83e1', 'License', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1099, '"License"', 0, 57, 'class', '*');
INSERT INTO "Menu" VALUES (1123, '"Menu"', '6a123754-a810-f503-e33f-f6cc6326bdbf', 'Report', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 0, NULL, 0, 58, 'folder', '*');
INSERT INTO "Menu" VALUES (1125, '"Menu"', 'b19e215b-44f2-8f7c-2a47-abea8c570e03', 'Location list with assets', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1123, '"Report"', 597, 59, 'reportpdf', '*');
INSERT INTO "Menu" VALUES (1127, '"Menu"', 'b7c12ef3-876d-e5d2-c463-cf7ef6597c1e', 'Workflow', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 0, NULL, 0, 60, 'folder', '*');
INSERT INTO "Menu" VALUES (1129, '"Menu"', 'c7146cce-685b-8e8f-d1e8-955de390a9de', 'Request for change', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1127, '"RequestForChange"', 0, 61, 'processclass', '*');
INSERT INTO "Menu" VALUES (1069, '"Menu"', 'dfbc9d35-5e44-ccc3-e072-ede2adfbd111', 'Item situation', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1067, '"_Dashboards"', 831, 31, 'dashboard', '*');
INSERT INTO "Menu" VALUES (1071, '"Menu"', '6f2f1bb1-8227-c405-7281-1a54e500bdab', 'RfC situation', 'A', 'admin', '2014-06-12 16:52:05.948397', NULL, 1067, '"_Dashboards"', 946, 32, 'dashboard', '*');



INSERT INTO "Menu_history" VALUES (1403, '"Menu"', NULL, 'Dashboard', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 0, NULL, 0, 30, 'folder', '*', 1067, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1404, '"Menu"', NULL, 'Basic archives', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 0, NULL, 0, 33, 'folder', '*', 1073, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1405, '"Menu"', NULL, 'Employee', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1073, '"Employee"', 0, 34, 'class', '*', 1075, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1406, '"Menu"', NULL, 'Office', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1073, '"Office"', 0, 35, 'class', '*', 1077, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1407, '"Menu"', NULL, 'Workplace', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1073, '"Workplace"', 0, 36, 'class', '*', 1079, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1408, '"Menu"', NULL, 'Purchases', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 0, NULL, 0, 37, 'folder', '*', 1081, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1409, '"Menu"', NULL, 'Supplier', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1081, '"Supplier"', 0, 38, 'class', '*', 1083, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1410, '"Menu"', NULL, 'SupplierContact', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1081, '"SupplierContact"', 0, 39, 'class', '*', 1085, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1411, '"Menu"', NULL, 'Invoice', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1081, '"Invoice"', 0, 40, 'class', '*', 1087, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1412, '"Menu"', NULL, 'Locations', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 0, NULL, 0, 41, 'folder', '*', 1089, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1413, '"Menu"', NULL, 'Building', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1089, '"Building"', 0, 42, 'class', '*', 1091, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1414, '"Menu"', NULL, 'Room', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1089, '"Room"', 0, 43, 'class', '*', 1093, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1415, '"Menu"', NULL, 'Floor', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1089, '"Floor"', 0, 44, 'class', '*', 1095, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1416, '"Menu"', NULL, 'Network point', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1089, '"NetworkPoint"', 0, 45, 'class', '*', 1097, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1417, '"Menu"', NULL, 'Assets', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 0, NULL, 0, 46, 'folder', '*', 1099, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1418, '"Menu"', NULL, 'Asset', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"Asset"', 0, 47, 'class', '*', 1101, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1419, '"Menu"', NULL, 'Computer', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"Computer"', 0, 48, 'class', '*', 1103, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1420, '"Menu"', NULL, 'PC', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"PC"', 0, 49, 'class', '*', 1105, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1421, '"Menu"', NULL, 'Notebook', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"Notebook"', 0, 50, 'class', '*', 1107, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1422, '"Menu"', NULL, 'Server', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"Server"', 0, 51, 'class', '*', 1109, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1423, '"Menu"', NULL, 'Monitor', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"Monitor"', 0, 52, 'class', '*', 1111, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1424, '"Menu"', NULL, 'Printer', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"Printer"', 0, 53, 'class', '*', 1113, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1425, '"Menu"', NULL, 'NetworkDevice', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"NetworkDevice"', 0, 54, 'class', '*', 1115, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1426, '"Menu"', NULL, 'Rack', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"Rack"', 0, 55, 'class', '*', 1117, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1427, '"Menu"', NULL, 'UPS', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"UPS"', 0, 56, 'class', '*', 1119, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1428, '"Menu"', NULL, 'License', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1099, '"License"', 0, 57, 'class', '*', 1121, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1429, '"Menu"', NULL, 'Report', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 0, NULL, 0, 58, 'folder', '*', 1123, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1430, '"Menu"', NULL, 'Location list with assets', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1123, '"Report"', 597, 59, 'reportpdf', '*', 1125, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1431, '"Menu"', NULL, 'Workflow', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 0, NULL, 0, 60, 'folder', '*', 1127, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1432, '"Menu"', NULL, 'Request for change', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1127, '"RequestForChange"', 0, 61, 'processclass', '*', 1129, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1433, '"Menu"', NULL, 'Item situation', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1067, '"_Dashboards"', 831, 31, 'dashboard', '*', 1069, '2014-06-12 16:52:05.948397');
INSERT INTO "Menu_history" VALUES (1434, '"Menu"', NULL, 'RfC situation', 'U', 'admin', '2013-05-09 12:57:48.985726', NULL, 1067, '"_Dashboards"', 946, 32, 'dashboard', '*', 1071, '2014-06-12 16:52:05.948397');



INSERT INTO "Metadata" VALUES (505, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.minzoom', 'N', 'system', '2011-09-19 16:59:23.120594', '0');
INSERT INTO "Metadata" VALUES (507, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.maxzoom', 'N', 'system', '2011-09-19 16:59:23.120594', '25');
INSERT INTO "Metadata" VALUES (509, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.style', 'N', 'system', '2011-09-19 16:59:23.120594', '{"strokeDashstyle":"solid","fillColor":"#CCFFFF","externalGraphic":"upload/images/gis/Building.jpg","pointRadius":10,"strokeColor":"#CCFFCC","strokeWidth":1}');
INSERT INTO "Metadata" VALUES (511, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.visibility', 'N', 'system', '2011-09-19 16:59:23.120594', 'Building');
INSERT INTO "Metadata" VALUES (513, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.index', 'N', 'system', '2011-09-19 16:59:23.120594', '1');
INSERT INTO "Metadata" VALUES (495, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.minzoom', 'N', 'system', '2011-09-19 16:59:28.659447', '0');
INSERT INTO "Metadata" VALUES (497, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.maxzoom', 'N', 'system', '2011-09-19 16:59:28.659447', '25');
INSERT INTO "Metadata" VALUES (499, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.style', 'N', 'system', '2011-09-19 16:59:28.659447', '{"strokeDashstyle":"solid","fillColor":"#CCFFFF","externalGraphic":"upload/images/gis/Supplier.jpg","pointRadius":10,"strokeColor":"#CCFFCC","strokeWidth":1}');
INSERT INTO "Metadata" VALUES (501, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.visibility', 'N', 'system', '2011-09-19 16:59:28.659447', 'Supplier');
INSERT INTO "Metadata" VALUES (503, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.index', 'N', 'system', '2011-09-19 16:59:28.659447', '0');
INSERT INTO "Metadata" VALUES (1249, '"Metadata"', 'PC', 'system.widgets', 'N', 'system', '2013-05-09 12:57:49.745726', '[{"id":"4ea70051-9bab-436a-a5ef-5cb002a10912","label":"Ping","active":true,"alwaysenabled":true,"address":"{client:IPAddress}","count":3,"templates":{},"type":".Ping"},{"id":"06dc6599-2ad5-4d03-9262-d2dafd4277b6","label":"Warranty calendar","active":true,"alwaysenabled":true,"targetClass":"PC","startDate":"AcceptanceDate","endDate":null,"eventTitle":"SerialNumber","filter":"","defaultDate":null,"type":".Calendar"}]');



INSERT INTO "Metadata_history" VALUES (780, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.minzoom', 'U', 'system', '2011-08-23 15:41:08.854', '0', 505, '2011-09-19 16:59:23.120594');
INSERT INTO "Metadata_history" VALUES (781, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.maxzoom', 'U', 'system', '2011-08-23 15:41:08.854', '25', 507, '2011-09-19 16:59:23.120594');
INSERT INTO "Metadata_history" VALUES (782, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.style', 'U', 'system', '2011-08-23 15:41:08.854', '{"strokeDashstyle":"solid","fillColor":"#CCFFFF","externalGraphic":"upload/images/gis/Building.jpg","pointRadius":10,"strokeColor":"#CCFFCC","strokeWidth":1}', 509, '2011-09-19 16:59:23.120594');
INSERT INTO "Metadata_history" VALUES (783, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.visibility', 'U', 'system', '2011-08-23 15:41:08.854', 'Building', 511, '2011-09-19 16:59:23.120594');
INSERT INTO "Metadata_history" VALUES (784, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.index', 'U', 'system', '2011-08-23 15:41:08.854', '1', 513, '2011-09-19 16:59:23.120594');
INSERT INTO "Metadata_history" VALUES (785, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.minzoom', 'U', 'system', '2011-08-23 15:39:20.948', '0', 495, '2011-09-19 16:59:28.659447');
INSERT INTO "Metadata_history" VALUES (786, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.maxzoom', 'U', 'system', '2011-08-23 15:39:20.948', '25', 497, '2011-09-19 16:59:28.659447');
INSERT INTO "Metadata_history" VALUES (787, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.style', 'U', 'system', '2011-08-23 15:39:20.948', '{"strokeDashstyle":"solid","fillColor":"#CCFFFF","externalGraphic":"upload/images/gis/Supplier.jpg","pointRadius":10,"strokeColor":"#CCFFCC","strokeWidth":1}', 499, '2011-09-19 16:59:28.659447');
INSERT INTO "Metadata_history" VALUES (788, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.visibility', 'U', 'system', '2011-08-23 15:39:20.948', 'Supplier', 501, '2011-09-19 16:59:28.659447');
INSERT INTO "Metadata_history" VALUES (789, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.index', 'U', 'system', '2011-08-23 15:39:20.948', '0', 503, '2011-09-19 16:59:28.659447');
INSERT INTO "Metadata_history" VALUES (1251, '"Metadata"', 'PC', 'system.widgets', 'U', 'system', '2012-08-25 12:16:36.281', '[{"id":"4ea70051-9bab-436a-a5ef-5cb002a10912","label":"Ping","active":true,"alwaysenabled":true,"address":"{client:IPAddress}","count":3,"templates":{},"type":".Ping"}]', 1249, '2012-08-25 12:20:02.957');
INSERT INTO "Metadata_history" VALUES (1351, '"Metadata"', 'PC', 'system.widgets', 'U', 'system', '2012-08-25 12:20:02.957', '[{"id":"4ea70051-9bab-436a-a5ef-5cb002a10912","label":"Ping","active":true,"alwaysenabled":true,"address":"{client:IPAddress}","count":3,"templates":{},"type":".Ping"},{"id":"06dc6599-2ad5-4d03-9262-d2dafd4277b6","label":"Warranty calendar","active":true,"alwaysenabled":true,"targetClass":"PC","startDate":"AcceptanceDate","endDate":null,"eventTitle":"SerialNumber","filter":"","defaultDate":null,"type":".Calendar"}]', 1249, '2013-05-09 12:57:49.745726');



INSERT INTO "Monitor" VALUES (550, '"Monitor"', 'MON0001', 'Acer - AL1716 ', 'A', 'admin', '2011-08-23 17:34:12.416', NULL, NULL, NULL, NULL, NULL, NULL, 138, 'AL1716 ', NULL, 134, NULL, NULL, NULL, 392, NULL);
INSERT INTO "Monitor" VALUES (555, '"Monitor"', 'MON0002', 'Acer - B243WCydr', 'A', 'admin', '2011-08-23 17:35:03.944', NULL, 'PRT576', NULL, NULL, NULL, NULL, 138, 'B243WCydr', 272, 128, NULL, NULL, NULL, 392, 330);
INSERT INTO "Monitor" VALUES (561, '"Monitor"', 'MON0003', 'Acer - V193HQb', 'A', 'admin', '2011-08-23 17:36:00.497', NULL, NULL, NULL, NULL, NULL, NULL, 138, 'V193HQb', 242, 130, NULL, NULL, NULL, 392, NULL);
INSERT INTO "Monitor" VALUES (573, '"Monitor"', 'MON0004', 'Epson - W1934S-BN', 'A', 'admin', '2011-08-23 17:37:57.173', NULL, 'KR57667', NULL, NULL, NULL, NULL, 140, 'W1934S-BN', 272, 132, NULL, NULL, NULL, 393, 330);
INSERT INTO "Monitor" VALUES (567, '"Monitor"', 'MON0007', 'Hp - V220', 'A', 'admin', '2011-09-07 11:59:52.223', NULL, 'SR6576', NULL, NULL, '2011-09-06', NULL, 135, 'V220', 230, 118, NULL, NULL, NULL, 392, 330);



INSERT INTO "Monitor_history" VALUES (551, '"Monitor"', 'MON0001', 'Acer - AL1716 ', 'U', 'admin', '2011-08-23 17:34:02.111', NULL, NULL, NULL, NULL, NULL, NULL, 138, 'AL1716 ', NULL, NULL, NULL, NULL, NULL, 392, NULL, 550, '2011-08-23 17:34:12.416');
INSERT INTO "Monitor_history" VALUES (774, '"Monitor"', 'MON0007', 'Hp - V220', 'U', 'admin', '2011-08-23 17:36:50.525', NULL, 'SR6576', NULL, NULL, NULL, NULL, 135, 'V220', 230, 118, NULL, NULL, NULL, 392, 330, 567, '2011-09-07 11:59:52.223');



INSERT INTO "NetworkDevice" VALUES (747, '"NetworkDevice"', 'ND0654', 'Switch Panel CISCO Catalyst 3750 S.N. YRTU87', 'A', 'admin', '2011-09-02 12:07:44.126', NULL, 'YRTU87', 723, '2011-05-08', '2011-06-06', NULL, 137, 'Catalyst 3750', 200, NULL, NULL, NULL, NULL, 409, 32, NULL);
INSERT INTO "NetworkDevice" VALUES (755, '"NetworkDevice"', 'ND0685', 'Switch Panel CISCO Catalyst 3750 S.N. YFGE87', 'A', 'admin', '2011-09-02 12:15:10.417', NULL, 'YFGE87', 723, '2011-07-04', '2011-09-13', NULL, 137, 'Catalyst 3750', 104, NULL, NULL, NULL, NULL, 409, 32, NULL);



INSERT INTO "NetworkDevice_history" VALUES (752, '"NetworkDevice"', 'ND0654', 'Switch Panel CISCO Catalyst 3750', 'U', 'admin', '2011-09-02 12:06:33.699', NULL, 'SNYRTU87', 723, '2011-05-08', '2011-06-14', NULL, 137, 'Catalyst 3750', 200, NULL, NULL, NULL, NULL, 409, 32, NULL, 747, '2011-09-02 12:07:04.477');
INSERT INTO "NetworkDevice_history" VALUES (753, '"NetworkDevice"', 'ND0654', 'Switch Panel CISCO Catalyst 3750', 'U', 'admin', '2011-09-02 12:07:04.477', NULL, 'SNYRTU87', 723, '2011-05-08', '2011-06-06', NULL, 137, 'Catalyst 3750', 200, NULL, NULL, NULL, NULL, 409, 32, NULL, 747, '2011-09-02 12:07:44.126');
INSERT INTO "NetworkDevice_history" VALUES (762, '"NetworkDevice"', 'ND0685', 'Switch Panel CISCO Catalyst 3750 S.N. YFGE87', 'U', 'admin', '2011-09-02 12:08:39.585', NULL, 'YFGE87', 723, NULL, NULL, NULL, 137, 'Catalyst 3750', 104, NULL, NULL, NULL, NULL, 409, 32, NULL, 755, '2011-09-02 12:14:44.964');
INSERT INTO "NetworkDevice_history" VALUES (763, '"NetworkDevice"', 'ND0685', 'Switch Panel CISCO Catalyst 3750 S.N. YFGE87', 'U', 'admin', '2011-09-02 12:14:44.964', NULL, 'YFGE87', 723, '2011-07-04', NULL, NULL, 137, 'Catalyst 3750', 104, NULL, NULL, NULL, NULL, 409, 32, NULL, 755, '2011-09-02 12:15:10.417');















INSERT INTO "Office" VALUES (110, '"Office"', 'OFF03', 'Office 03 - Legal Department', 'A', 'admin', '2011-07-24 18:49:18.638', NULL, 'Legal Department', NULL);
INSERT INTO "Office" VALUES (108, '"Office"', 'OFF02', 'Office 02 - Administration', 'A', 'admin', '2011-07-24 18:49:25.82', NULL, 'Administration', NULL);
INSERT INTO "Office" VALUES (112, '"Office"', 'OFF01', 'Office 01 - Headquarters', 'A', 'admin', '2011-07-24 23:38:05.699', NULL, 'Head Office', NULL);



INSERT INTO "Office_history" VALUES (113, '"Office"', 'OFF02', 'Office 02 - Legal Department', 'U', 'admin', '2011-07-24 18:48:13.386', NULL, 'Legal Department', NULL, 110, '2011-07-24 18:49:18.638');
INSERT INTO "Office_history" VALUES (114, '"Office"', 'OFF01', 'Office 01 - Administration', 'U', 'admin', '2011-07-24 18:47:26.769', NULL, 'Administration', NULL, 108, '2011-07-24 18:49:25.82');
INSERT INTO "Office_history" VALUES (170, '"Office"', 'OFF01', 'Office 01 - Head Office', 'U', 'admin', '2011-07-24 18:49:09.575', NULL, 'Head Office', NULL, 112, '2011-07-24 23:38:05.699');



INSERT INTO "PC" VALUES (534, '"PC"', 'PC0002', 'Intel Pentium P4', 'A', 'admin', '2011-08-23 17:29:52.21', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Pentium P4', 104, 128, 134, NULL, NULL, 1, 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO "PC" VALUES (542, '"PC"', 'PC0004', 'Sony Vajo F', 'A', 'admin', '2011-08-23 17:32:51.564', NULL, 'TY747687', NULL, NULL, NULL, NULL, 136, 'Vajo F', 272, 130, 116, NULL, NULL, 8, 4, NULL, 2, NULL, NULL, NULL);
INSERT INTO "PC" VALUES (518, '"PC"', 'PC0001', 'Acer - Netbook D250', 'A', 'admin', '2012-08-25 12:17:41.034', NULL, '43434', NULL, '2011-04-03', NULL, NULL, 138, 'D250', 236, 120, 116, NULL, NULL, 4, 2, NULL, 1, '127.0.0.1', NULL, NULL);
INSERT INTO "PC" VALUES (526, '"PC"', 'PC0003', 'Hp - A6316', 'A', 'admin', '2012-08-25 12:41:15.881', NULL, NULL, 723, NULL, '2011-09-06', NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);



INSERT INTO "PC_history" VALUES (710, '"PC"', 'PC0001', 'Acer - Netbook D250', 'U', 'admin', '2011-08-23 17:26:13.647', NULL, '43434', NULL, NULL, NULL, NULL, 138, 'D250', 236, 120, 116, NULL, NULL, 4, 2, NULL, 1, NULL, NULL, NULL, 518, '2011-08-23 23:46:34.587');
INSERT INTO "PC_history" VALUES (718, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2011-08-23 17:28:42.292', NULL, NULL, NULL, NULL, NULL, NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2011-08-29 13:03:27.919');
INSERT INTO "PC_history" VALUES (719, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2011-08-29 13:03:27.919', NULL, NULL, 714, NULL, NULL, NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2011-08-29 13:07:08.776');
INSERT INTO "PC_history" VALUES (726, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2011-08-29 13:07:08.776', NULL, NULL, NULL, NULL, NULL, NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2011-08-29 13:27:49.732');
INSERT INTO "PC_history" VALUES (776, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2011-08-29 13:27:49.732', NULL, NULL, 723, NULL, NULL, NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2011-09-07 11:59:52.223');
INSERT INTO "PC_history" VALUES (1250, '"PC"', 'PC0001', 'Acer - Netbook D250', 'U', 'admin', '2011-08-23 23:46:34.587', NULL, '43434', NULL, '2011-04-03', NULL, NULL, 138, 'D250', 236, 120, 116, NULL, NULL, 4, 2, NULL, 1, NULL, NULL, NULL, 518, '2012-08-25 12:17:41.034');
INSERT INTO "PC_history" VALUES (1252, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2011-09-07 11:59:52.223', NULL, NULL, 723, NULL, '2011-09-06', NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2012-08-25 12:39:36.099');
INSERT INTO "PC_history" VALUES (1255, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2012-08-25 12:39:36.099', NULL, NULL, NULL, NULL, '2011-09-06', NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2012-08-25 12:41:15.881');



INSERT INTO "Patch" VALUES (773, '"Patch"', '1.3.1-05', 'Create database', 'A', 'system', '2011-09-05 12:01:42.544', NULL, NULL);
INSERT INTO "Patch" VALUES (818, '"Patch"', '1.4.0-01', 'Reorders tree nodes that were not properly ordered when saving them', 'A', 'system', '2012-01-31 11:29:35.93578', NULL, NULL);
INSERT INTO "Patch" VALUES (820, '"Patch"', '1.4.0-02', 'Fixes reference values filling on attribute creation', 'A', 'system', '2012-01-31 11:29:36.004394', NULL, NULL);
INSERT INTO "Patch" VALUES (822, '"Patch"', '1.5.0-01', 'Creates DB templates table', 'A', 'system', '2012-08-23 21:55:23.55', NULL, NULL);
INSERT INTO "Patch" VALUES (824, '"Patch"', '2.0.0-01', 'Dashboard base functions', 'A', 'system', '2012-08-23 21:55:23.713', NULL, NULL);
INSERT INTO "Patch" VALUES (826, '"Patch"', '2.0.0-02', 'Alter workflow tables', 'A', 'system', '2012-08-23 21:55:23.773', NULL, NULL);
INSERT INTO "Patch" VALUES (828, '"Patch"', '2.0.0-03', 'Add UI profile attributes', 'A', 'system', '2012-08-23 21:55:23.92', NULL, NULL);
INSERT INTO "Patch" VALUES (830, '"Patch"', '2.0.0-04', 'A few Dashboard Functions', 'A', 'system', '2012-08-23 21:55:23.973', NULL, NULL);
INSERT INTO "Patch" VALUES (1264, '"Patch"', '2.0.0-05', 'Support for INOUT parameters in custom functions', 'A', 'system', '2012-08-30 16:14:58.493242', NULL, NULL);
INSERT INTO "Patch" VALUES (1266, '"Patch"', '2.0.3-01', 'Add table to store the configuration of a domains based tree', 'A', NULL, '2013-05-09 12:57:48.659815', NULL, NULL);
INSERT INTO "Patch" VALUES (1268, '"Patch"', '2.0.3-02', 'Add table to store the GIS layers configuration', 'A', NULL, '2013-05-09 12:57:48.868573', NULL, NULL);
INSERT INTO "Patch" VALUES (1270, '"Patch"', '2.0.3-03', 'Fixed comments and checks for allow activity attributes sorting', 'A', NULL, '2013-05-09 12:57:48.884651', NULL, NULL);
INSERT INTO "Patch" VALUES (1272, '"Patch"', '2.0.3-04', 'Add Configuration column for Cloud Administrator', 'A', NULL, '2013-05-09 12:57:48.910141', NULL, NULL);
INSERT INTO "Patch" VALUES (1274, '"Patch"', '2.0.4-01', 'Set inconsistent processes to closed aborted', 'A', NULL, '2013-05-09 12:57:48.926717', NULL, NULL);
INSERT INTO "Patch" VALUES (1276, '"Patch"', '2.1.0-01', 'Add/Replace system functions to delete cards. Add "IdClass" attribute to simple classes. Change process system attributes MODE', 'A', NULL, '2013-05-09 12:57:48.977022', NULL, NULL);
INSERT INTO "Patch" VALUES (1344, '"Patch"', '2.1.0-02', 'Alter "Report", "Menu", "Lookup" tables for the new DAO', 'A', NULL, '2013-05-09 12:57:49.177939', NULL, NULL);
INSERT INTO "Patch" VALUES (1346, '"Patch"', '2.1.0-03', 'Changes to "User", "Role" and "Grant" tables', 'A', NULL, '2013-05-09 12:57:49.737278', NULL, NULL);
INSERT INTO "Patch" VALUES (1353, '"Patch"', '2.1.0-04', 'Create Filter, Widget, View table. Import data from Metadata table', 'A', NULL, '2013-05-09 12:57:50.836638', NULL, NULL);
INSERT INTO "Patch" VALUES (1355, '"Patch"', '2.1.2-01', 'Add table to store CMDBf MdrScopedId', 'A', 'system', '2013-06-12 14:53:30.385034', NULL, NULL);
INSERT INTO "Patch" VALUES (1357, '"Patch"', '2.1.2-02', 'Changing User and Role tables to standard classes', 'A', 'system', '2013-06-12 14:53:31.107808', NULL, NULL);
INSERT INTO "Patch" VALUES (1359, '"Patch"', '2.1.2-03', 'Increasing Widgets'' definition attribute size', 'A', 'system', '2013-06-12 14:53:31.132702', NULL, NULL);
INSERT INTO "Patch" VALUES (1361, '"Patch"', '2.1.4-01', 'Create the table to manage Email templates', 'A', 'system', '2014-06-12 16:51:42.951658', NULL, NULL);
INSERT INTO "Patch" VALUES (1363, '"Patch"', '2.1.4-02', 'Update Scheduler table to manage Email service', 'A', 'system', '2014-06-12 16:51:42.982276', NULL, NULL);
INSERT INTO "Patch" VALUES (1365, '"Patch"', '2.1.4-03', 'Update Email table to handle notify templates', 'A', 'system', '2014-06-12 16:51:43.007502', NULL, NULL);
INSERT INTO "Patch" VALUES (1367, '"Patch"', '2.1.5-01', 'Update Grant table to define attribute privileges at group level', 'A', 'system', '2014-06-12 16:51:43.023847', NULL, NULL);
INSERT INTO "Patch" VALUES (1369, '"Patch"', '2.1.5-02', 'Update column privileges to handle none value', 'A', 'system', '2014-06-12 16:51:43.040496', NULL, NULL);
INSERT INTO "Patch" VALUES (1371, '"Patch"', '2.1.5-03', 'Update User column size', 'A', 'system', '2014-06-12 16:52:02.060142', NULL, NULL);
INSERT INTO "Patch" VALUES (1374, '"Patch"', '2.1.6-01', 'Fix widget Calendar attribute name', 'A', 'system', '2014-06-12 16:52:02.082157', NULL, NULL);
INSERT INTO "Patch" VALUES (1376, '"Patch"', '2.1.6-02', 'Add indexes for all classes/tables', 'A', 'system', '2014-06-12 16:52:03.856352', NULL, NULL);
INSERT INTO "Patch" VALUES (1378, '"Patch"', '2.1.7-01', 'Fix domain tables', 'A', 'system', '2014-06-12 16:52:03.923616', NULL, NULL);
INSERT INTO "Patch" VALUES (1380, '"Patch"', '2.1.7-02', 'Add indexes for all classes/tables', 'A', 'system', '2014-06-12 16:52:03.939957', NULL, NULL);
INSERT INTO "Patch" VALUES (1382, '"Patch"', '2.1.8-01', 'Fixed issues related to backup schemas', 'A', 'system', '2014-06-12 16:52:03.95654', NULL, NULL);
INSERT INTO "Patch" VALUES (1384, '"Patch"', '2.1.9-01', 'Update Grant table to define UI card edit mode privileges', 'A', 'system', '2014-06-12 16:52:03.981675', NULL, NULL);
INSERT INTO "Patch" VALUES (1386, '"Patch"', '2.1.9-02', 'Visibility of Email class and EmailActivity domain', 'A', 'system', '2014-06-12 16:52:04.006773', NULL, NULL);
INSERT INTO "Patch" VALUES (1388, '"Patch"', '2.2.0-01', 'Create tables to manage Bim Module', 'A', 'system', '2014-06-12 16:52:04.423124', NULL, NULL);
INSERT INTO "Patch" VALUES (1390, '"Patch"', '2.2.0-02', 'Create tables for using e-mails as source event for starting workflows', 'A', 'system', '2014-06-12 16:52:05.590019', NULL, NULL);
INSERT INTO "Patch" VALUES (1392, '"Patch"', '2.2.0-03', 'Add columns to bim tables', 'A', 'system', '2014-06-12 16:52:05.614976', NULL, NULL);
INSERT INTO "Patch" VALUES (1394, '"Patch"', '2.2.0-04', 'Migrate legacy scheduler job parameters', 'A', 'system', '2014-06-12 16:52:05.639862', NULL, NULL);
INSERT INTO "Patch" VALUES (1396, '"Patch"', '2.2.0-05', 'Changing scheduler tables in task tables', 'A', 'system', '2014-06-12 16:52:05.673289', NULL, NULL);
INSERT INTO "Patch" VALUES (1398, '"Patch"', '2.2.0-06', 'Fix e-mail template table', 'A', 'system', '2014-06-12 16:52:05.740161', NULL, NULL);
INSERT INTO "Patch" VALUES (1400, '"Patch"', '2.2.0-07', 'Add columns to _DomainTreeNavigation table', 'A', 'system', '2014-06-12 16:52:05.764989', NULL, NULL);
INSERT INTO "Patch" VALUES (1402, '"Patch"', '2.2.0-08', 'Create translations table', 'A', 'system', '2014-06-12 16:52:05.940101', NULL, NULL);
INSERT INTO "Patch" VALUES (1436, '"Patch"', '2.2.0-09', 'Generate UUIDs for Menu entries', 'A', 'system', '2014-06-12 16:52:05.965405', NULL, NULL);
INSERT INTO "Patch" VALUES (1438, '"Patch"', '2.2.0-10', 'Create LastExecution column in _Task table', 'A', 'system', '2014-06-12 16:52:05.990328', NULL, NULL);
INSERT INTO "Patch" VALUES (1440, '"Patch"', '2.2.0-11', 'Update EmailStatus lookups', 'A', 'system', '2014-06-12 16:52:06.007226', NULL, NULL);
INSERT INTO "Patch" VALUES (1442, '"Patch"', '2.2.0-12', 'Create TranslationUuis column in LookUp table', 'A', 'system', '2014-06-12 16:52:06.032447', NULL, NULL);
INSERT INTO "Patch" VALUES (1444, '"Patch"', '2.2.0-13', 'Stored-procedure called by the BIM features', 'A', 'system', '2014-06-12 16:52:06.049045', NULL, NULL);
INSERT INTO "Patch" VALUES (1446, '"Patch"', '2.1.9-03', 'Create column NoSubjectPrefix to Email table', 'A', 'system', '2015-02-03 12:13:40.714192', NULL, NULL);
INSERT INTO "Patch" VALUES (1448, '"Patch"', '2.2.1-01', 'Fix _EmailAccount and _EmailTemplate inheritance and create domain between Class and Metadata', 'A', 'system', '2015-02-03 12:14:21.307906', NULL, NULL);
INSERT INTO "Patch" VALUES (1450, '"Patch"', '2.2.2-01', 'Creates AccountTemplate domain, Account attribute for _EmailTemplate class and Account attribute for Email class', 'A', 'system', '2015-02-03 12:14:21.587142', NULL, NULL);
INSERT INTO "Patch" VALUES (1452, '"Patch"', '2.2.2-02', 'Migrates task manager''s mapper keys and values', 'A', 'system', '2015-02-03 12:14:21.603609', NULL, NULL);
INSERT INTO "Patch" VALUES (1454, '"Patch"', '2.3.0-01', 'Creates Service and Privileged columns for User class', 'A', 'system', '2015-02-03 12:14:21.628616', NULL, NULL);
INSERT INTO "Patch" VALUES (1456, '"Patch"', '2.3.0-02', 'Fixes wrong inheritance for some system classes', 'A', 'system', '2015-02-03 12:14:22.137725', NULL, NULL);
INSERT INTO "Patch" VALUES (1458, '"Patch"', '2.3.0-03', 'Removes unused class "_MdrScopedId"', 'A', 'system', '2015-02-03 12:14:22.153824', NULL, NULL);
INSERT INTO "Patch" VALUES (1460, '"Patch"', '2.3.1-01', 'Creates the new attributes needed for smart e-mail regeneration within ManageEmail widget', 'A', 'system', '2016-03-24 10:34:46.081755', NULL, NULL);
INSERT INTO "Patch" VALUES (1462, '"Patch"', '2.3.1-02', 'Replaces system functions for handling user when a relation is created/updated ', 'A', 'system', '2016-03-24 10:34:46.1059', NULL, NULL);
INSERT INTO "Patch" VALUES (1464, '"Patch"', '2.3.1-03', 'Visibility of Email class and EmailActivity domain', 'A', 'system', '2016-03-24 10:34:46.130875', NULL, NULL);
INSERT INTO "Patch" VALUES (1466, '"Patch"', '2.3.1-04', 'Relation between e-mails and cards', 'A', 'system', '2016-03-24 10:34:47.276694', NULL, NULL);
INSERT INTO "Patch" VALUES (1468, '"Patch"', '2.3.1-05', 'Creates "Delay" attribute for "Email" table', 'A', 'system', '2016-03-24 10:34:47.305688', NULL, NULL);
INSERT INTO "Patch" VALUES (1470, '"Patch"', '2.3.1-06', 'Creates "Delay" attribute for "_EmailTemplate" table', 'A', 'system', '2016-03-24 10:34:47.331075', NULL, NULL);
INSERT INTO "Patch" VALUES (1472, '"Patch"', '2.3.1-07', 'Creates "OutputFolder" attribute for "_EmailAccount" table', 'A', 'system', '2016-03-24 10:34:47.355774', NULL, NULL);
INSERT INTO "Patch" VALUES (1474, '"Patch"', '2.3.2-01', 'Moves parameters from "_EmailAccount" table to "_TaskParameter" table', 'A', 'system', '2016-03-24 10:34:47.389177', NULL, NULL);
INSERT INTO "Patch" VALUES (1476, '"Patch"', '2.3.2-02', 'Moves "LastExecution" from "_Task" table to the new "_TaskRuntime" table', 'A', 'system', '2016-03-24 10:34:47.530694', NULL, NULL);
INSERT INTO "Patch" VALUES (1478, '"Patch"', '2.3.4-01', 'Creates the domain between "_Filter" and "Role" classes', 'A', 'system', '2016-03-24 10:34:47.855622', NULL, NULL);
INSERT INTO "Patch" VALUES (1480, '"Patch"', '2.3.4-02', 'Updates cm_delete_card function', 'A', 'system', '2016-03-24 10:34:47.872276', NULL, NULL);
INSERT INTO "Patch" VALUES (1482, '"Patch"', '2.3.4-03', 'Creates new functions for the management of attribute comments', 'A', 'system', '2016-03-24 10:34:47.889097', NULL, NULL);
INSERT INTO "Patch" VALUES (1484, '"Patch"', '2.3.4-04', 'Creates the _CustomPage table', 'A', 'system', '2016-03-24 10:34:48.223736', NULL, NULL);
INSERT INTO "Patch" VALUES (1486, '"Patch"', '2.3.5-01', 'Fixes "cm_modify_attribute" function', 'A', 'system', '2016-03-24 10:34:48.2389', NULL, NULL);
INSERT INTO "Patch" VALUES (1488, '"Patch"', '2.3.5-02', 'Fixes "_cm_comment_add_parts" function', 'A', 'system', '2016-03-24 10:34:48.255778', NULL, NULL);
INSERT INTO "Patch" VALUES (1490, '"Patch"', '2.3.5-03', 'Normalizes the grants of processes', 'A', 'system', '2016-03-24 10:34:48.297685', NULL, NULL);
INSERT INTO "Patch" VALUES (1492, '"Patch"', '2.3.5-04', 'Updates the table "_EmailAccount" adding the columns needed for STARTTLS management ', 'A', 'system', '2016-03-24 10:34:48.322314', NULL, NULL);
INSERT INTO "Patch" VALUES (1494, '"Patch"', '2.3.5-05', 'Fixes "_cm_comment_add_parts" function adding a custom function for the concatenation of strings', 'A', 'system', '2016-03-24 10:34:48.338994', NULL, NULL);
INSERT INTO "Patch" VALUES (1496, '"Patch"', '2.3.5-06', 'Updates the table "_TaskParameter" handling the new parameters for e-mail tasks ', 'A', 'system', '2016-03-24 10:34:48.355763', NULL, NULL);
INSERT INTO "Patch" VALUES (1498, '"Patch"', '2.4.0-01', 'Adds "EnableRecursion" column for "_DomainTreeNavigation" table.', 'A', 'system', '2016-03-24 10:34:48.380731', NULL, NULL);
INSERT INTO "Patch" VALUES (1500, '"Patch"', '2.4.0-02', 'Adds missing "TranslationUuid" values to "LookUp" table.', 'A', 'system', '2016-03-24 10:34:48.397633', NULL, NULL);
INSERT INTO "Patch" VALUES (1502, '"Patch"', '2.4.0-03', 'Creates "_Icon" table.', 'A', 'system', '2016-03-24 10:34:48.589249', NULL, NULL);






INSERT INTO "Printer" VALUES (579, '"Printer"', 'PRT0001', 'Canon - IX5000', 'A', 'admin', '2011-08-23 17:38:55.033', NULL, 'YT687', NULL, NULL, NULL, NULL, 139, 'IX5000', 242, 130, NULL, NULL, NULL, 399, 395, true, NULL);
INSERT INTO "Printer" VALUES (585, '"Printer"', 'PRT0002', 'Epson - ELP 6200L', 'A', 'admin', '2011-08-23 17:39:42.706', NULL, 'RTD575', NULL, NULL, NULL, NULL, 140, 'ELP 6200L', 212, 120, NULL, NULL, NULL, 399, 395, false, NULL);
INSERT INTO "Printer" VALUES (591, '"Printer"', 'PRT0003', 'HP DesignJet Z2100', 'A', 'admin', '2011-09-07 11:59:52.223', NULL, 'YU6874', NULL, NULL, '2011-09-06', NULL, 135, 'DesignJet Z2100', 266, 122, NULL, NULL, NULL, 399, NULL, false, NULL);



INSERT INTO "Printer_history" VALUES (775, '"Printer"', 'PRT0003', 'HP DesignJet Z2100', 'U', 'admin', '2011-08-23 17:40:48.481', NULL, 'YU6874', NULL, NULL, NULL, NULL, 135, 'DesignJet Z2100', 266, 122, NULL, NULL, NULL, 399, NULL, false, NULL, 591, '2011-09-07 11:59:52.223');









INSERT INTO "Report" VALUES (597, 'Location list with assets', 'Location list with assets', 'A', NULL, '2011-08-23 18:16:36.567', 'custom', 'SELECT
"Asset"."Code" AS "AssetCode", max("Asset"."Description") AS "AssetDescription", max("LookUp1"."Description") AS "AssetBrand",
"Workplace"."Code" AS "WorkplaceCode", max("Workplace"."Description") AS "WorkplaceDescription", max("Employee"."Description") as "Assignee", max(lower("Employee"."Email")) as "Email",
coalesce("Room"."Code", ''Not defined'') AS "RoomCode",
max(coalesce("Room"."Description",''Not defined'')) AS "RoomDescription",
max(coalesce("Floor"."Description" ,''Not defined'')) AS "FloorDescription",
max(coalesce("Building"."Description",''Not defined'')) AS "BuildingDescription"
FROM "Asset"
LEFT OUTER JOIN "Workplace" ON "Workplace"."Id"="Asset"."Workplace" AND "Workplace"."Status"=''A''
LEFT OUTER JOIN "Employee" ON "Employee"."Id"="Asset"."Assignee" AND "Employee"."Status"=''A''
LEFT OUTER JOIN "Room" ON "Room"."Id"="Asset"."Room" AND "Room"."Status"=''A''
LEFT OUTER JOIN "Floor" ON "Floor"."Id"="Room"."Floor" AND "Floor"."Status"=''A''
LEFT OUTER JOIN "Building" ON "Building"."Id"="Floor"."Building" AND "Building"."Status"=''A''
LEFT OUTER JOIN "LookUp" AS "LookUp1" ON "LookUp1"."Id"="Asset"."Brand"
WHERE "Asset"."Status"=''A''
GROUP BY "Room"."Code", "Workplace"."Code", "Asset"."Code"
ORDER BY "Room"."Code"', '\\xaced0005737200286e65742e73662e6a61737065727265706f7274732e656e67696e652e4a61737065725265706f727400000000000027d80200034c000b636f6d70696c65446174617400164c6a6176612f696f2f53657269616c697a61626c653b4c0011636f6d70696c654e616d655375666669787400124c6a6176612f6c616e672f537472696e673b4c000d636f6d70696c6572436c61737371007e00027872002d6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173655265706f727400000000000027d802002749000c626f74746f6d4d617267696e49000b636f6c756d6e436f756e7449000d636f6c756d6e53706163696e6749000b636f6c756d6e57696474685a001069676e6f7265506167696e6174696f6e5a00136973466c6f6174436f6c756d6e466f6f7465725a0010697353756d6d6172794e6577506167655a0020697353756d6d6172795769746850616765486561646572416e64466f6f7465725a000e69735469746c654e65775061676549000a6c6566744d617267696e42000b6f7269656e746174696f6e49000a7061676548656967687449000970616765576964746842000a7072696e744f7264657249000b72696768744d617267696e490009746f704d617267696e42000e7768656e4e6f44617461547970654c000a6261636b67726f756e647400244c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5242616e643b4c000c636f6c756d6e466f6f74657271007e00044c000c636f6c756d6e48656164657271007e00045b000864617461736574737400285b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52446174617365743b4c000b64656661756c74466f6e7474002a4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525265706f7274466f6e743b4c000c64656661756c745374796c657400254c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525374796c653b4c000664657461696c71007e00044c000d64657461696c53656374696f6e7400274c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5253656374696f6e3b5b0005666f6e747374002b5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525265706f7274466f6e743b4c0012666f726d6174466163746f7279436c61737371007e00024c000a696d706f72747353657474000f4c6a6176612f7574696c2f5365743b4c00086c616e677561676571007e00024c000e6c61737450616765466f6f74657271007e00044c000b6d61696e446174617365747400274c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52446174617365743b4c00046e616d6571007e00024c00066e6f4461746171007e00044c000a70616765466f6f74657271007e00044c000a7061676548656164657271007e00045b00067374796c65737400265b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525374796c653b4c000773756d6d61727971007e00045b000974656d706c6174657374002f5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525265706f727454656d706c6174653b4c00057469746c6571007e000478700000001400000001000000000000030e00000000000000001e02000002530000034a010000001e00000014017372002b6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736542616e6400000000000027d802000549001950534555444f5f53455249414c5f56455253494f4e5f5549444900066865696768745a000e697353706c6974416c6c6f7765644c00137072696e745768656e45787072657373696f6e74002a4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5245787072657373696f6e3b4c000973706c6974547970657400104c6a6176612f6c616e672f427974653b787200336e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365456c656d656e7447726f757000000000000027d80200024c00086368696c6472656e7400104c6a6176612f7574696c2f4c6973743b4c000c656c656d656e7447726f757074002c4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52456c656d656e7447726f75703b7870737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000a7870000077260000000001707372000e6a6176612e6c616e672e427974659c4e6084ee50f51c02000142000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b0200007870017371007e000f7371007e00160000000077040000000a78700000772600000005017071007e001a7371007e000f7371007e00160000000577040000000a737200306e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736552656374616e676c6500000000000027d80200014c00067261646975737400134c6a6176612f6c616e672f496e74656765723b787200356e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736547726170686963456c656d656e7400000000000027d80200034c000466696c6c71007e00114c00076c696e6550656e7400234c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5250656e3b4c000370656e71007e00117872002e6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365456c656d656e7400000000000027d80200164900066865696768745a001769735072696e74496e466972737457686f6c6542616e645a001569735072696e74526570656174656456616c7565735a001a69735072696e745768656e44657461696c4f766572666c6f77735a0015697352656d6f76654c696e655768656e426c616e6b42000c706f736974696f6e5479706542000b7374726574636854797065490005776964746849000178490001794c00096261636b636f6c6f727400104c6a6176612f6177742f436f6c6f723b4c001464656661756c745374796c6550726f76696465727400344c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5244656661756c745374796c6550726f76696465723b4c000c656c656d656e7447726f757071007e00144c0009666f7265636f6c6f7271007e00244c00036b657971007e00024c00046d6f646571007e00114c000b706172656e745374796c6571007e00074c0018706172656e745374796c654e616d655265666572656e636571007e00024c00137072696e745768656e45787072657373696f6e71007e00104c00157072696e745768656e47726f75704368616e6765737400254c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5247726f75703b4c000d70726f706572746965734d617074002d4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5250726f706572746965734d61703b5b001370726f706572747945787072657373696f6e737400335b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5250726f706572747945787072657373696f6e3b7870000000200001000002000000030a00000001000000027372000e6a6176612e6177742e436f6c6f7201a51783108f337502000546000666616c70686149000576616c75654c0002637374001b4c6a6176612f6177742f636f6c6f722f436f6c6f7253706163653b5b00096672676276616c75657400025b465b00066676616c756571007e002c787000000000fff0f0f070707071007e000e71007e001d7074000b72656374616e676c652d317371007e001801707070707070707372002a6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736550656e00000000000027d80200044c00096c696e65436f6c6f7271007e00244c00096c696e655374796c6571007e00114c00096c696e6557696474687400114c6a6176612f6c616e672f466c6f61743b4c000c70656e436f6e7461696e657274002c4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5250656e436f6e7461696e65723b7870707371007e0018007372000f6a6176612e6c616e672e466c6f6174daedc9a2db3cf0ec02000146000576616c75657871007e00190000000071007e00297070737200316e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173655374617469635465787400000000000027d80200014c00047465787471007e0002787200326e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736554657874456c656d656e7400000000000027d80200204c0006626f7264657271007e00114c000b626f72646572436f6c6f7271007e00244c000c626f74746f6d426f7264657271007e00114c0011626f74746f6d426f72646572436f6c6f7271007e00244c000d626f74746f6d50616464696e6771007e00204c0008666f6e744e616d6571007e00024c0008666f6e7453697a6571007e00204c0013686f72697a6f6e74616c416c69676e6d656e7471007e00114c00066973426f6c647400134c6a6176612f6c616e672f426f6f6c65616e3b4c000869734974616c696371007e00394c000d6973506466456d62656464656471007e00394c000f6973537472696b655468726f75676871007e00394c000c69735374796c65645465787471007e00394c000b6973556e6465726c696e6571007e00394c000a6c656674426f7264657271007e00114c000f6c656674426f72646572436f6c6f7271007e00244c000b6c65667450616464696e6771007e00204c00076c696e65426f787400274c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a524c696e65426f783b4c000b6c696e6553706163696e6771007e00114c00066d61726b757071007e00024c000770616464696e6771007e00204c000b706466456e636f64696e6771007e00024c000b706466466f6e744e616d6571007e00024c000a7265706f7274466f6e7471007e00064c000b7269676874426f7264657271007e00114c00107269676874426f72646572436f6c6f7271007e00244c000c726967687450616464696e6771007e00204c0008726f746174696f6e71007e00114c0009746f70426f7264657271007e00114c000e746f70426f72646572436f6c6f7271007e00244c000a746f7050616464696e6771007e00204c0011766572746963616c416c69676e6d656e7471007e00117871007e00230000000e0001000002000000005f0000004d000000037071007e000e71007e001d7074000c737461746963546578742d3370707070707070707070707070737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c75657871007e00190000000a70737200116a6176612e6c616e672e426f6f6c65616ecd207280d59cfaee0200015a000576616c756578700170707070707070707372002e6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173654c696e65426f7800000000000027d802000b4c000d626f74746f6d50616464696e6771007e00204c0009626f74746f6d50656e74002b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f626173652f4a52426f7850656e3b4c000c626f78436f6e7461696e657274002c4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52426f78436f6e7461696e65723b4c000b6c65667450616464696e6771007e00204c00076c65667450656e71007e00424c000770616464696e6771007e00204c000370656e71007e00424c000c726967687450616464696e6771007e00204c0008726967687450656e71007e00424c000a746f7050616464696e6771007e00204c0006746f7050656e71007e0042787070737200336e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365426f78426f74746f6d50656e00000000000027d80200007872002d6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365426f7850656e00000000000027d80200014c00076c696e65426f7871007e003a7871007e00307371007e002a00000000ff00000070707071007e00347371007e00350000000071007e004471007e004471007e003b70737200316e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365426f784c65667450656e00000000000027d80200007871007e00467371007e002a00000000ff00000070707071007e00347371007e00350000000071007e004471007e0044707371007e004670707071007e004471007e004470737200326e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365426f78526967687450656e00000000000027d80200007871007e00467371007e002a00000000ff00000070707071007e00347371007e00350000000071007e004471007e004470737200306e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365426f78546f7050656e00000000000027d80200007871007e00467371007e002a00000000ff00000070707071007e00347371007e00350000000071007e004471007e00447070707074000e48656c7665746963612d426f6c6470707070707070707074000b4173736574204272616e647371007e00370000000e0001000002000000005f0000004d000000127071007e000e71007e001d7074000d737461746963546578742d3130707070707070707070707070707371007e003d0000000a7071007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e005c71007e005c71007e0059707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e005c71007e005c707371007e004670707071007e005c71007e005c707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e005c71007e005c707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e005c71007e005c7070707074000e48656c7665746963612d426f6c6470707070707070707074000e41737365742041737369676e65657371007e00370000000e0001000002000000008b000000b4000000127071007e000e71007e001d7074000d737461746963546578742d3131707070707070707070707070707371007e003d0000000a7071007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e006f71007e006f71007e006c707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e006f71007e006f707371007e004670707071007e006f71007e006f707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e006f71007e006f707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e006f71007e006f7070707074000e48656c7665746963612d426f6c6470707070707070707074000e41737369676e656520656d61696c7371007e00370000000e0001000002000000008a000000b4000000037071007e000e71007e001d7074000d737461746963546578742d3132707070707070707070707070707371007e003d0000000a7071007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e008271007e008271007e007f707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e008271007e0082707371007e004670707071007e008271007e0082707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e008271007e0082707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e008271007e00827070707074000e48656c7665746963612d426f6c647070707070707070707400114173736574204465736372697074696f6e78700000772600000027017071007e001a707070707372002e6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736553656374696f6e00000000000027d80200015b000562616e64737400255b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5242616e643b7870757200255b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5242616e643b95dd7eec8cca85350200007870000000017371007e000f7371007e00160000000577040000000a737200306e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365546578744669656c6400000000000027d802001149000d626f6f6b6d61726b4c6576656c42000e6576616c756174696f6e54696d6542000f68797065726c696e6b54617267657442000d68797065726c696e6b547970655a0015697353747265746368576974684f766572666c6f774c0014616e63686f724e616d6545787072657373696f6e71007e00104c000f6576616c756174696f6e47726f757071007e00264c000a65787072657373696f6e71007e00104c001968797065726c696e6b416e63686f7245787072657373696f6e71007e00104c001768797065726c696e6b5061676545787072657373696f6e71007e00105b001368797065726c696e6b506172616d65746572737400335b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5248797065726c696e6b506172616d657465723b4c001c68797065726c696e6b5265666572656e636545787072657373696f6e71007e00104c001a68797065726c696e6b546f6f6c74697045787072657373696f6e71007e00104c000f6973426c616e6b5768656e4e756c6c71007e00394c000a6c696e6b54617267657471007e00024c00086c696e6b5479706571007e00024c00077061747465726e71007e00027871007e00380000000e00010000020000000100000000b30000000f7071007e000e71007e009770740009746578744669656c64707070707070707070707070707371007e003d00000009707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e009e71007e009e71007e009b707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e009e71007e009e707371007e004670707071007e009e71007e009e707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e009e71007e009e707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e009e71007e009e707070707070707070707070707000000000010100017070737200316e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736545787072657373696f6e00000000000027d802000449000269645b00066368756e6b737400305b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5245787072657373696f6e4368756e6b3b4c000e76616c7565436c6173734e616d6571007e00024c001276616c7565436c6173735265616c4e616d6571007e0002787000000018757200305b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5245787072657373696f6e4368756e6b3b6d59cfde694ba355020000787000000001737200366e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736545787072657373696f6e4368756e6b00000000000027d8020002420004747970654c00047465787471007e0002787003740005456d61696c7400106a6176612e6c616e672e537472696e6770707070707071007e00407070707372002b6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173654c696e6500000000000027d8020001420009646972656374696f6e7871007e0021000000010001000002000000030d000000010000001f7071007e000e71007e00977371007e002a00000000ffcbc7c77070707400066c696e652d3170707070707070707371007e003070707071007e00b670017371007e00990000000f0001000002000000006400000048000000007071007e000e71007e00977070707070707070707070707070707371007e003d0000000a707070707070707070707371007e0041707371007e004570707071007e00bc71007e00bc71007e00ba707371007e004a70707071007e00bc71007e00bc707371007e004670707071007e00bc71007e00bc707371007e004f70707071007e00bc71007e00bc707371007e005370707071007e00bc71007e00bc7070707070707070707070707070000000000101000070707371007e00ac000000197571007e00af000000017371007e00b10374000a41737365744272616e647400106a6176612e6c616e672e537472696e6770707070707071007e00407070707371007e00990000000e00010000020000000064000000480000000f7071007e000e71007e00977070707070707070707070707070707371007e003d0000000a707070707070707070707371007e0041707371007e004570707071007e00c971007e00c971007e00c7707371007e004a70707071007e00c971007e00c9707371007e004670707071007e00c971007e00c9707371007e004f70707071007e00c971007e00c9707371007e005370707071007e00c971007e00c97070707070707070707070707070000000000101000070707371007e00ac0000001a7571007e00af000000017371007e00b10374000841737369676e65657400106a6176612e6c616e672e537472696e6770707070707071007e00407070707371007e00990000000f00010000020000000100000000b3000000007071007e000e71007e00977070707070707070707070707070707371007e003d0000000a707070707070707070707371007e0041707371007e004570707071007e00d671007e00d671007e00d4707371007e004a70707071007e00d671007e00d6707371007e004670707071007e00d671007e00d6707371007e004f70707071007e00d671007e00d6707371007e005370707071007e00d671007e00d67070707070707070707070707070000000000101000070707371007e00ac0000001b7571007e00af000000017371007e00b10374001041737365744465736372697074696f6e7400106a6176612e6c616e672e537472696e6770707070707071007e004070707078700000772600000021017071007e001a7070737200116a6176612e7574696c2e48617368536574ba44859596b8b7340300007870770c000000043f400000000000037400226e65742e73662e6a61737065727265706f7274732e656e67696e652e646174612e2a74001d6e65742e73662e6a61737065727265706f7274732e656e67696e652e2a74000b6a6176612e7574696c2e2a787400046a6176617371007e000f7371007e00160000000477040000000a7371007e0099000000120001000002000000004d000002ac000000017071007e000e71007e00e77074000b746578744669656c642d3170707070707070707070707070707371007e0018037070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e00ec71007e00ec71007e00e9707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e00ec71007e00ec707371007e004670707071007e00ec71007e00ec707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e00ec71007e00ec707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e00ec71007e00ec7070707070707070707070707070000000000101000070707371007e00ac0000001f7571007e00af000000037371007e00b10174000a22506167652022202b207371007e00b10474000b504147455f4e554d4245527371007e00b101740009202b202220646920227400106a6176612e6c616e672e537472696e677070707070707371007e003f007070707371007e00990000001200010000020000000014000002f9000000017071007e000e71007e00e77074000b746578744669656c642d32707070707070707070707070707071007e00eb7070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e010671007e010671007e0104707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e010671007e0106707371007e004670707071007e010671007e0106707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e010671007e0106707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e010671007e01067070707070707070707070707070000000000201000070707371007e00ac000000207571007e00af000000037371007e00b1017400052222202b207371007e00b10474000b504147455f4e554d4245527371007e00b101740005202b2022227400106a6176612e6c616e672e537472696e6770707070707071007e01037070707371007e009900000012000100000200000000480000001f000000017071007e000e71007e00e77074000b746578744669656c642d337070707070707070707070707070707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e011f71007e011f71007e011d707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e011f71007e011f707371007e004670707071007e011f71007e011f707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e011f71007e011f707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e011f71007e011f7070707070707070707070707070000000000201000070707371007e00ac000000217571007e00af000000017371007e00b1017400146e6577206a6176612e7574696c2e44617465282974000e6a6176612e7574696c2e4461746570707070707071007e0103707074000a4d4d2f64642f797979797371007e0037000000120001000002000000001c00000001000000017071007e000e71007e00e77074000d737461746963546578742d32367070707070707070707070707070707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e013571007e013571007e0133707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e013571007e0135707371007e004670707071007e013571007e0135707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e013571007e0135707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e013571007e01357070707070707070707070707070740005446174653a7870000077260000001a017071007e001a7372002e6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173654461746173657400000000000027d802000e5a000669734d61696e4200177768656e5265736f757263654d697373696e67547970655b00066669656c64737400265b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a524669656c643b4c001066696c74657245787072657373696f6e71007e00105b000667726f7570737400265b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5247726f75703b4c00046e616d6571007e00025b000a706172616d657465727374002a5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52506172616d657465723b4c000d70726f706572746965734d617071007e00274c000571756572797400254c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5251756572793b4c000e7265736f7572636542756e646c6571007e00024c000e7363726970746c6574436c61737371007e00025b000a7363726970746c65747374002a5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525363726970746c65743b5b000a736f72744669656c647374002a5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52536f72744669656c643b5b00097661726961626c65737400295b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525661726961626c653b78700101757200265b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a524669656c643b023cdfc74e2af27002000078700000000b7372002c6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173654669656c6400000000000027d80200054c000b6465736372697074696f6e71007e00024c00046e616d6571007e00024c000d70726f706572746965734d617071007e00274c000e76616c7565436c6173734e616d6571007e00024c001276616c7565436c6173735265616c4e616d6571007e000278707400007400094173736574436f64657372002b6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5250726f706572746965734d617000000000000027d80200034c00046261736571007e00274c000e70726f706572746965734c69737471007e00134c000d70726f706572746965734d617074000f4c6a6176612f7574696c2f4d61703b78707070707400106a6176612e6c616e672e537472696e67707371007e014f74000074001041737365744465736372697074696f6e7371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f74000074000a41737365744272616e647371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f74000074000d576f726b706c616365436f64657371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f740000740014576f726b706c6163654465736372697074696f6e7371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f74000074000841737369676e65657371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f740000740005456d61696c7371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f740000740008526f6f6d436f64657371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f74000074000f526f6f6d4465736372697074696f6e7371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f740000740010466c6f6f724465736372697074696f6e7371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f7400007400134275696c64696e674465736372697074696f6e7371007e01537070707400106a6176612e6c616e672e537472696e677070757200265b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5247726f75703b40a35f7a4cfd78ea0200007870000000037372002c6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736547726f757000000000000027d802000e42000e666f6f746572506f736974696f6e5a0019697352657072696e744865616465724f6e45616368506167655a001169735265736574506167654e756d6265725a0010697353746172744e6577436f6c756d6e5a000e697353746172744e6577506167655a000c6b656570546f6765746865724900176d696e486569676874546f53746172744e6577506167654c000d636f756e745661726961626c657400284c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525661726961626c653b4c000a65787072657373696f6e71007e00104c000b67726f7570466f6f74657271007e00044c001267726f7570466f6f74657253656374696f6e71007e00084c000b67726f757048656164657271007e00044c001267726f757048656164657253656374696f6e71007e00084c00046e616d6571007e00027870010000000000000000007372002f6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173655661726961626c6500000000000027d802000d42000b63616c63756c6174696f6e42000d696e6372656d656e74547970655a000f697353797374656d446566696e65644200097265736574547970654c000a65787072657373696f6e71007e00104c000e696e6372656d656e7447726f757071007e00264c001b696e6372656d656e746572466163746f7279436c6173734e616d6571007e00024c001f696e6372656d656e746572466163746f7279436c6173735265616c4e616d6571007e00024c0016696e697469616c56616c756545787072657373696f6e71007e00104c00046e616d6571007e00024c000a726573657447726f757071007e00264c000e76616c7565436c6173734e616d6571007e00024c001276616c7565436c6173735265616c4e616d6571007e00027870010501047371007e00ac000000087571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e74656765722831297400116a6176612e6c616e672e496e7465676572707070707371007e00ac000000097571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000d70616c617a7a6f5f434f554e5471007e018d71007e0194707371007e00ac0000000e7571007e00af000000017371007e00b1037400134275696c64696e674465736372697074696f6e7400106a6176612e6c616e672e4f626a65637470707371007e00927571007e0095000000017371007e000f7371007e00160000000077040000000a78700000772600000000017071007e001a707371007e00927571007e0095000000017371007e000f7371007e00160000000377040000000a7371007e001f000000110001000002000000030a00000001000000067371007e002a00000000ffe0fae970707071007e000e71007e01a57074000b72656374616e676c652d3271007e002f707070707070707371007e00307071007e00347371007e00350000000071007e01a770707371007e0037000000160001000002000000003800000004000000047071007e000e71007e01a57371007e002a00000000ff00666670707074000d737461746963546578742d3139707070707070707070707070707371007e003d0000000e707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01b071007e01b071007e01ac707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01b071007e01b0707371007e004670707071007e01b071007e01b0707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01b071007e01b0707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01b071007e01b070707070707070707070707070707400094275696c64696e673a7371007e009900000014000100000200000000c40000003f000000047071007e000e71007e01a57070707070707070707070707070707371007e003d0000000e707070707070707070707371007e0041707371007e004570707071007e01c171007e01c171007e01bf707371007e004a70707071007e01c171007e01c1707371007e004670707071007e01c171007e01c1707371007e004f70707071007e01c171007e01c1707371007e005370707071007e01c171007e01c17070707070707070707070707070000000000101000070707371007e00ac0000000f7571007e00af000000017371007e00b1037400134275696c64696e674465736372697074696f6e7400106a6176612e6c616e672e537472696e67707070707070707070707870000077260000001b017071007e001a74000770616c617a7a6f7371007e018b010000000000000000007371007e018e010501047371007e00ac0000000a7571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e0194707070707371007e00ac0000000b7571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000c7461766f6c615f434f554e5471007e01cd71007e0194707371007e00ac000000107571007e00af000000017371007e00b103740010466c6f6f724465736372697074696f6e71007e019e70707371007e00927571007e0095000000017371007e000f7371007e00160000000077040000000a78700000772600000000017071007e001a707371007e00927571007e0095000000017371007e000f7371007e00160000000377040000000a7371007e001f00000013000100000200000002f900000012000000047371007e002a00000000fff5ecec70707071007e000e71007e01e27074000b72656374616e676c652d3371007e002f707070707070707371007e00307071007e00347371007e00350000000071007e01e470707371007e0037000000120001000002000000002800000017000000057071007e000e71007e01e27371007e002a00000000ff66000070707074000d737461746963546578742d3230707070707070707070707070707371007e003d0000000c707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01ed71007e01ed71007e01e9707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01ed71007e01ed707371007e004670707071007e01ed71007e01ed707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01ed71007e01ed707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01ed71007e01ed7070707070707070707070707070740006466c6f6f723a7371007e009900000012000100000200000000cc00000048000000057071007e000e71007e01e270707070707070707070707070707070707070707070707070707371007e0041707371007e004570707071007e01fd71007e01fd71007e01fc707371007e004a70707071007e01fd71007e01fd707371007e004670707071007e01fd71007e01fd707371007e004f70707071007e01fd71007e01fd707371007e005370707071007e01fd71007e01fd7070707070707070707070707070000000000101000070707371007e00ac000000117571007e00af000000017371007e00b103740010466c6f6f724465736372697074696f6e7400106a6176612e6c616e672e537472696e67707070707070707070707870000077260000001b017071007e001a7400067461766f6c617371007e018b010000000000000000007371007e018e010501047371007e00ac0000000c7571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e0194707070707371007e00ac0000000d7571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000c7374616e7a615f434f554e5471007e020971007e0194707371007e00ac000000127571007e00af000000017371007e00b103740008526f6f6d436f646571007e019e70707371007e00927571007e0095000000017371007e000f7371007e00160000000077040000000a78700000772600000000017071007e001a707371007e00927571007e0095000000017371007e000f7371007e00160000000477040000000a7371007e001f00000013000100000200000002e300000028000000057371007e002a00000000ffe2fafa70707071007e000e71007e021e7074000b72656374616e676c652d3471007e002f707070707070707371007e00307071007e00347371007e00350000000071007e022070707371007e0037000000120001000002000000002d0000002c000000057071007e000e71007e021e7371007e002a00000000ff00009970707074000d737461746963546578742d32317070707070707070707070707070707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e022871007e022871007e0225707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e022871007e0228707371007e004670707071007e022871007e0228707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e022871007e0228707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e022871007e02287070707070707070707070707070740005526f6f6d3a7371007e009900000012000100000200000000640000005c000000057071007e000e71007e021e70707070707070707070707070707070707070707070707070707371007e0041707371007e004570707071007e023871007e023871007e0237707371007e004a70707071007e023871007e0238707371007e004670707071007e023871007e0238707371007e004f70707071007e023871007e0238707371007e005370707071007e023871007e02387070707070707070707070707070000000000101000070707371007e00ac000000137571007e00af000000017371007e00b103740008526f6f6d436f64657400106a6176612e6c616e672e537472696e67707070707070707070707371007e009900000012000100000200000000d5000000ce000000057071007e000e71007e021e70707070707070707070707070707070707070707070707070707371007e0041707371007e004570707071007e024471007e024471007e0243707371007e004a70707071007e024471007e0244707371007e004670707071007e024471007e0244707371007e004f70707071007e024471007e0244707371007e005370707071007e024471007e02447070707070707070707070707070000000000101000070707371007e00ac000000147571007e00af000000017371007e00b10374000f526f6f6d4465736372697074696f6e7400106a6176612e6c616e672e537472696e67707070707070707070707870000077260000001b017071007e001a7400067374616e7a6174000941737365744c6973747572002a5b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a52506172616d657465723b22000c8d2ac36021020000787000000010737200306e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365506172616d6574657200000000000027d80200095a000e6973466f7250726f6d7074696e675a000f697353797374656d446566696e65644c001664656661756c7456616c756545787072657373696f6e71007e00104c000b6465736372697074696f6e71007e00024c00046e616d6571007e00024c000e6e6573746564547970654e616d6571007e00024c000d70726f706572746965734d617071007e00274c000e76616c7565436c6173734e616d6571007e00024c001276616c7565436c6173735265616c4e616d6571007e00027870010170707400155245504f52545f504152414d45544552535f4d4150707371007e015370707074000d6a6176612e7574696c2e4d6170707371007e02530101707074000d4a41535045525f5245504f5254707371007e01537070707400286e65742e73662e6a61737065727265706f7274732e656e67696e652e4a61737065725265706f7274707371007e0253010170707400115245504f52545f434f4e4e454354494f4e707371007e01537070707400136a6176612e73716c2e436f6e6e656374696f6e707371007e0253010170707400105245504f52545f4d41585f434f554e54707371007e015370707071007e0194707371007e0253010170707400125245504f52545f444154415f534f55524345707371007e01537070707400286e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5244617461536f75726365707371007e0253010170707400105245504f52545f5343524950544c4554707371007e015370707074002f6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5241627374726163745363726970746c6574707371007e02530101707074000d5245504f52545f4c4f43414c45707371007e01537070707400106a6176612e7574696c2e4c6f63616c65707371007e0253010170707400165245504f52545f5245534f555243455f42554e444c45707371007e01537070707400186a6176612e7574696c2e5265736f7572636542756e646c65707371007e0253010170707400105245504f52545f54494d455f5a4f4e45707371007e01537070707400126a6176612e7574696c2e54696d655a6f6e65707371007e0253010170707400155245504f52545f464f524d41545f464143544f5259707371007e015370707074002e6e65742e73662e6a61737065727265706f7274732e656e67696e652e7574696c2e466f726d6174466163746f7279707371007e0253010170707400135245504f52545f434c4153535f4c4f41444552707371007e01537070707400156a6176612e6c616e672e436c6173734c6f61646572707371007e02530101707074001a5245504f52545f55524c5f48414e444c45525f464143544f5259707371007e01537070707400206a6176612e6e65742e55524c53747265616d48616e646c6572466163746f7279707371007e0253010170707400145245504f52545f46494c455f5245534f4c564552707371007e015370707074002d6e65742e73662e6a61737065727265706f7274732e656e67696e652e7574696c2e46696c655265736f6c766572707371007e0253010170707400125245504f52545f5649525455414c495a4552707371007e01537070707400296e65742e73662e6a61737065727265706f7274732e656e67696e652e4a525669727475616c697a6572707371007e02530101707074001449535f49474e4f52455f504147494e4154494f4e707371007e01537070707400116a6176612e6c616e672e426f6f6c65616e707371007e0253010170707400105245504f52545f54454d504c41544553707371007e01537070707400146a6176612e7574696c2e436f6c6c656374696f6e707371007e0153707371007e00160000000577040000000a740019697265706f72742e7363726970746c657468616e646c696e67740010697265706f72742e656e636f64696e6774000c697265706f72742e7a6f6f6d740009697265706f72742e78740009697265706f72742e7978737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000571007e0297740003312e3071007e02967400055554462d3871007e02987400013071007e02997400013071007e029574000132787372002c6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365517565727900000000000027d80200025b00066368756e6b7374002b5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5251756572794368756e6b3b4c00086c616e677561676571007e000278707572002b5b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5251756572794368756e6b3b409f00a1e8ba34a4020000787000000001737200316e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736551756572794368756e6b00000000000027d8020003420004747970654c00047465787471007e00025b0006746f6b656e737400135b4c6a6176612f6c616e672f537472696e673b7870017404d053454c4543540a224173736574222e22436f64652220415320224173736574436f6465222c206d617828224173736574222e224465736372697074696f6e2229204153202241737365744465736372697074696f6e222c206d617828224c6f6f6b557031222e224465736372697074696f6e2229204153202241737365744272616e64222c0a22576f726b706c616365222e22436f6465222041532022576f726b706c616365436f6465222c206d61782822576f726b706c616365222e224465736372697074696f6e22292041532022576f726b706c6163654465736372697074696f6e222c206d61782822456d706c6f796565222e224465736372697074696f6e2229206173202241737369676e6565222c206d6178286c6f7765722822456d706c6f796565222e22456d61696c2229292061732022456d61696c222c0a636f616c657363652822526f6f6d222e22436f6465222c20274e6f7420646566696e656427292041532022526f6f6d436f6465222c0a6d617828636f616c657363652822526f6f6d222e224465736372697074696f6e222c274e6f7420646566696e65642729292041532022526f6f6d4465736372697074696f6e222c0a6d617828636f616c657363652822466c6f6f72222e224465736372697074696f6e22202c274e6f7420646566696e65642729292041532022466c6f6f724465736372697074696f6e222c0a6d617828636f616c6573636528224275696c64696e67222e224465736372697074696f6e222c274e6f7420646566696e656427292920415320224275696c64696e674465736372697074696f6e220a46524f4d20224173736574220a4c454654204f55544552204a4f494e2022576f726b706c61636522204f4e2022576f726b706c616365222e224964223d224173736574222e22576f726b706c6163652220414e442022576f726b706c616365222e22537461747573223d2741270a4c454654204f55544552204a4f494e2022456d706c6f79656522204f4e2022456d706c6f796565222e224964223d224173736574222e2241737369676e65652220414e442022456d706c6f796565222e22537461747573223d2741270a4c454654204f55544552204a4f494e2022526f6f6d22204f4e2022526f6f6d222e224964223d224173736574222e22526f6f6d2220414e442022526f6f6d222e22537461747573223d2741270a4c454654204f55544552204a4f494e2022466c6f6f7222204f4e2022466c6f6f72222e224964223d22526f6f6d222e22466c6f6f722220414e442022466c6f6f72222e22537461747573223d2741270a4c454654204f55544552204a4f494e20224275696c64696e6722204f4e20224275696c64696e67222e224964223d22466c6f6f72222e224275696c64696e672220414e4420224275696c64696e67222e22537461747573223d2741270a4c454654204f55544552204a4f494e20224c6f6f6b55702220415320224c6f6f6b55703122204f4e20224c6f6f6b557031222e224964223d224173736574222e224272616e64220a574845524520224173736574222e22537461747573223d2741270a47524f55502042592022526f6f6d222e22436f6465222c2022576f726b706c616365222e22436f6465222c20224173736574222e22436f6465220a4f524445522042592022526f6f6d222e22436f6465227074000373716c70707070757200295b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a525661726961626c653b62e6837c982cb7440200007870000000087371007e018e08050101707070707371007e00ac000000007571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e01947074000b504147455f4e554d4245527071007e0194707371007e018e08050102707070707371007e00ac000000017571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e01947074000d434f4c554d4e5f4e554d4245527071007e0194707371007e018e010501017371007e00ac000000027571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e0194707070707371007e00ac000000037571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000c5245504f52545f434f554e547071007e0194707371007e018e010501027371007e00ac000000047571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e0194707070707371007e00ac000000057571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000a504147455f434f554e547071007e0194707371007e018e010501037371007e00ac000000067571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e0194707070707371007e00ac000000077571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000c434f4c554d4e5f434f554e547071007e01947071007e018f71007e01ce71007e020a71007e0250707371007e000f7371007e00160000000477040000000a7371007e009900000012000100000200000000480000001f000000037071007e000e71007e02d770740009746578744669656c647070707070707070707070707070707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02db71007e02db71007e02d9707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02db71007e02db707371007e004670707071007e02db71007e02db707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02db71007e02db707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02db71007e02db7070707070707070707070707070000000000201000070707371007e00ac0000001c7571007e00af000000017371007e00b1017400146e6577206a6176612e7574696c2e44617465282974000e6a6176612e7574696c2e4461746570707070707071007e0103707074000a4d4d2f64642f797979797371007e0099000000120001000002000000004d000002ac000000017071007e000e71007e02d770740009746578744669656c64707070707070707070707070707071007e00eb7070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02f171007e02f171007e02ef707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02f171007e02f1707371007e004670707071007e02f171007e02f1707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02f171007e02f1707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02f171007e02f17070707070707070707070707070000000000101000070707371007e00ac0000001d7571007e00af000000037371007e00b10174000a22506167652022202b207371007e00b10474000b504147455f4e554d4245527371007e00b101740009202b202220646920227400106a6176612e6c616e672e537472696e6770707070707071007e01037070707371007e00990000001200010000020000000014000002f9000000017071007e000e71007e02d770740009746578744669656c64707070707070707070707070707071007e00eb7070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e030a71007e030a71007e0308707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e030a71007e030a707371007e004670707071007e030a71007e030a707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e030a71007e030a707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e030a71007e030a7070707070707070707070707070000000000201000070707371007e00ac0000001e7571007e00af000000037371007e00b1017400052222202b207371007e00b10474000b504147455f4e554d4245527371007e00b101740005202b2022227400106a6176612e6c616e672e537472696e6770707070707071007e01037070707371007e0037000000120001000002000000001c00000001000000037071007e000e71007e02d77074000d737461746963546578742d32357070707070707070707070707070707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e032371007e032371007e0321707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e032371007e0323707371007e004670707071007e032371007e0323707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e032371007e0323707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e032371007e03237070707070707070707070707070740005446174653a78700000772600000019017071007e001a7371007e000f7371007e00160000000277040000000a7371007e00370000000f000100000200000000820000028c000000017071007e000e71007e03327074000d737461746963546578742d3238707070707070707070707070707071007e00eb71007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e033671007e033671007e0334707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e033671007e0336707371007e004670707071007e033671007e0336707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e033671007e0336707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e033671007e03367070707074000e48656c7665746963612d426f6c647070707070707070707400155374616d7061746f20636f6e20434d444275696c647371007e0037000000120001010002000000018f000000c0000000017071007e000e71007e03327074000d737461746963546578742d3239707070707070707070707070707371007e003d0000000c7371007e00180271007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e034a71007e034a71007e0346707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e034a71007e034a707371007e004670707071007e034a71007e034a707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e034a71007e034a707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e034a71007e034a7070707074000e48656c7665746963612d426f6c647070707070707070707400194c6f636174696f6e206c69737420776974682061737365747378700000772600000024017371007e00ac000000177571007e00af000000037371007e00b10174000e6e657720426f6f6c65616e2028207371007e00b10474000b504147455f4e554d4245527371007e00b1017400112e696e7456616c75652829203e2031202971007e028e7071007e001a707371007e000f7371007e00160000000077040000000a78700000772600000005017071007e001a707371007e000f7371007e00160000000377040000000a7371007e00370000001a0001000002000000018f000000c0000000127071007e000e71007e03647074000c737461746963546578742d31707070707070707070707070707371007e003d0000001071007e034971007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e036971007e036971007e0366707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e036971007e0369707371007e004670707071007e036971007e0369707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e036971007e0369707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e036971007e03697070707074000e48656c7665746963612d426f6c647070707070707070707400194c6f636174696f6e206c6973742077697468206173736574737372002c6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365496d61676500000000000027d802002449000d626f6f6b6d61726b4c6576656c42000e6576616c756174696f6e54696d6542000f68797065726c696e6b54617267657442000d68797065726c696e6b547970655a000669734c617a7942000b6f6e4572726f72547970654c0014616e63686f724e616d6545787072657373696f6e71007e00104c0006626f7264657271007e00114c000b626f72646572436f6c6f7271007e00244c000c626f74746f6d426f7264657271007e00114c0011626f74746f6d426f72646572436f6c6f7271007e00244c000d626f74746f6d50616464696e6771007e00204c000f6576616c756174696f6e47726f757071007e00264c000a65787072657373696f6e71007e00104c0013686f72697a6f6e74616c416c69676e6d656e7471007e00114c001968797065726c696e6b416e63686f7245787072657373696f6e71007e00104c001768797065726c696e6b5061676545787072657373696f6e71007e00105b001368797065726c696e6b506172616d657465727371007e009a4c001c68797065726c696e6b5265666572656e636545787072657373696f6e71007e00104c001a68797065726c696e6b546f6f6c74697045787072657373696f6e71007e00104c000c69735573696e67436163686571007e00394c000a6c656674426f7264657271007e00114c000f6c656674426f72646572436f6c6f7271007e00244c000b6c65667450616464696e6771007e00204c00076c696e65426f7871007e003a4c000a6c696e6b54617267657471007e00024c00086c696e6b5479706571007e00024c000770616464696e6771007e00204c000b7269676874426f7264657271007e00114c00107269676874426f72646572436f6c6f7271007e00244c000c726967687450616464696e6771007e00204c000a7363616c65496d61676571007e00114c0009746f70426f7264657271007e00114c000e746f70426f72646572436f6c6f7271007e00244c000a746f7050616464696e6771007e00204c0011766572746963616c416c69676e6d656e7471007e00117871007e0021000000250001000002000000007100000001000000007071007e000e71007e0364707070707070707070707371007e003070707071007e037a70000000000101000002707070707070707371007e00ac000000157571007e00af000000027371007e00b1027400155245504f52545f504152414d45544552535f4d41507371007e00b10174000e2e6765742822494d4147453022297400136a6176612e696f2e496e70757453747265616d7070707070707071007e00407070707371007e0041707371007e004570707071007e038371007e038371007e037a707371007e004a70707071007e038371007e0383707371007e004670707071007e038371007e0383707371007e004f70707071007e038371007e0383707371007e005370707071007e038371007e038370707070707070707070707371007e037900000025000100000200000000710000029d000000007071007e000e71007e0364707070707070707070707371007e003070707071007e038970000000000101000002707070707070707371007e00ac000000167571007e00af000000027371007e00b1027400155245504f52545f504152414d45544552535f4d41507371007e00b10174000e2e6765742822494d41474531222971007e03827070707070707071007e00407070707371007e0041707371007e004570707071007e039171007e039171007e0389707371007e004a70707071007e039171007e0391707371007e004670707071007e039171007e0391707371007e004f70707071007e039171007e0391707371007e005370707071007e039171007e039170707070707070707070707870000077260000003c017071007e001a737200366e65742e73662e6a61737065727265706f7274732e656e67696e652e64657369676e2e4a525265706f7274436f6d70696c654461746100000000000027d80200034c001363726f7373746162436f6d70696c654461746171007e01544c001264617461736574436f6d70696c654461746171007e01544c00166d61696e44617461736574436f6d70696c654461746171007e000178707371007e029a3f4000000000000c77080000001000000000787371007e029a3f4000000000000c7708000000100000000078757200025b42acf317f8060854e0020000787000001f2bcafebabe0000002e011801001e41737365744c6973745f313331343131363331353737385f31313238343907000101002c6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a524576616c7561746f72070003010017706172616d657465725f5245504f52545f4c4f43414c450100324c6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c506172616d657465723b010017706172616d657465725f4a41535045525f5245504f525401001c706172616d657465725f5245504f52545f5649525455414c495a455201001a706172616d657465725f5245504f52545f54494d455f5a4f4e4501001e706172616d657465725f5245504f52545f46494c455f5245534f4c56455201001a706172616d657465725f5245504f52545f5343524950544c455401001f706172616d657465725f5245504f52545f504152414d45544552535f4d415001001b706172616d657465725f5245504f52545f434f4e4e454354494f4e01001d706172616d657465725f5245504f52545f434c4153535f4c4f4144455201001c706172616d657465725f5245504f52545f444154415f534f55524345010024706172616d657465725f5245504f52545f55524c5f48414e444c45525f464143544f525901001e706172616d657465725f49535f49474e4f52455f504147494e4154494f4e01001f706172616d657465725f5245504f52545f464f524d41545f464143544f525901001a706172616d657465725f5245504f52545f4d41585f434f554e5401001a706172616d657465725f5245504f52545f54454d504c41544553010020706172616d657465725f5245504f52545f5245534f555243455f42554e444c4501000f6669656c645f4173736574436f646501002e4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c4669656c643b0100156669656c645f526f6f6d4465736372697074696f6e01000e6669656c645f526f6f6d436f64650100136669656c645f576f726b706c616365436f646501000b6669656c645f456d61696c0100166669656c645f41737365744465736372697074696f6e0100106669656c645f41737365744272616e640100166669656c645f466c6f6f724465736372697074696f6e01001a6669656c645f576f726b706c6163654465736372697074696f6e0100196669656c645f4275696c64696e674465736372697074696f6e01000e6669656c645f41737369676e65650100147661726961626c655f504147455f4e554d4245520100314c6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c5661726961626c653b0100167661726961626c655f434f4c554d4e5f4e554d4245520100157661726961626c655f5245504f52545f434f554e540100137661726961626c655f504147455f434f554e540100157661726961626c655f434f4c554d4e5f434f554e540100167661726961626c655f70616c617a7a6f5f434f554e540100157661726961626c655f7461766f6c615f434f554e540100157661726961626c655f7374616e7a615f434f554e540100063c696e69743e010003282956010004436f64650c002b002c0a0004002e0c0005000609000200300c0007000609000200320c0008000609000200340c0009000609000200360c000a000609000200380c000b0006090002003a0c000c0006090002003c0c000d0006090002003e0c000e000609000200400c000f000609000200420c0010000609000200440c0011000609000200460c0012000609000200480c00130006090002004a0c00140006090002004c0c00150006090002004e0c0016001709000200500c0018001709000200520c0019001709000200540c001a001709000200560c001b001709000200580c001c0017090002005a0c001d0017090002005c0c001e0017090002005e0c001f001709000200600c0020001709000200620c0021001709000200640c0022002309000200660c0024002309000200680c00250023090002006a0c00260023090002006c0c00270023090002006e0c0028002309000200700c0029002309000200720c002a0023090002007401000f4c696e654e756d6265725461626c6501000e637573746f6d697a6564496e6974010030284c6a6176612f7574696c2f4d61703b4c6a6176612f7574696c2f4d61703b4c6a6176612f7574696c2f4d61703b295601000a696e6974506172616d73010012284c6a6176612f7574696c2f4d61703b29560c0079007a0a0002007b01000a696e69744669656c64730c007d007a0a0002007e010008696e6974566172730c0080007a0a0002008101000d5245504f52545f4c4f43414c4508008301000d6a6176612f7574696c2f4d6170070085010003676574010026284c6a6176612f6c616e672f4f626a6563743b294c6a6176612f6c616e672f4f626a6563743b0c008700880b008600890100306e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c506172616d6574657207008b01000d4a41535045525f5245504f525408008d0100125245504f52545f5649525455414c495a455208008f0100105245504f52545f54494d455f5a4f4e450800910100145245504f52545f46494c455f5245534f4c5645520800930100105245504f52545f5343524950544c45540800950100155245504f52545f504152414d45544552535f4d41500800970100115245504f52545f434f4e4e454354494f4e0800990100135245504f52545f434c4153535f4c4f4144455208009b0100125245504f52545f444154415f534f5552434508009d01001a5245504f52545f55524c5f48414e444c45525f464143544f525908009f01001449535f49474e4f52455f504147494e4154494f4e0800a10100155245504f52545f464f524d41545f464143544f52590800a30100105245504f52545f4d41585f434f554e540800a50100105245504f52545f54454d504c415445530800a70100165245504f52545f5245534f555243455f42554e444c450800a90100094173736574436f64650800ab01002c6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c4669656c640700ad01000f526f6f6d4465736372697074696f6e0800af010008526f6f6d436f64650800b101000d576f726b706c616365436f64650800b3010005456d61696c0800b501001041737365744465736372697074696f6e0800b701000a41737365744272616e640800b9010010466c6f6f724465736372697074696f6e0800bb010014576f726b706c6163654465736372697074696f6e0800bd0100134275696c64696e674465736372697074696f6e0800bf01000841737369676e65650800c101000b504147455f4e554d4245520800c301002f6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c5661726961626c650700c501000d434f4c554d4e5f4e554d4245520800c701000c5245504f52545f434f554e540800c901000a504147455f434f554e540800cb01000c434f4c554d4e5f434f554e540800cd01000d70616c617a7a6f5f434f554e540800cf01000c7461766f6c615f434f554e540800d101000c7374616e7a615f434f554e540800d30100086576616c756174650100152849294c6a6176612f6c616e672f4f626a6563743b01000a457863657074696f6e730100136a6176612f6c616e672f5468726f7761626c650700d80100116a6176612f6c616e672f496e74656765720700da010004284929560c002b00dc0a00db00dd01000867657456616c756501001428294c6a6176612f6c616e672f4f626a6563743b0c00df00e00a00ae00e10100106a6176612f6c616e672f537472696e670700e30a008c00e1010006494d414745300800e60100136a6176612f696f2f496e70757453747265616d0700e8010006494d414745310800ea0100116a6176612f6c616e672f426f6f6c65616e0700ec0a00c600e1010008696e7456616c75650100032829490c00ef00f00a00db00f1010004285a29560c002b00f30a00ed00f401000e6a6176612f7574696c2f446174650700f60a00f7002e0100166a6176612f6c616e672f537472696e674275666665720700f901000550616765200800fb010015284c6a6176612f6c616e672f537472696e673b29560c002b00fd0a00fa00fe010006617070656e6401002c284c6a6176612f6c616e672f4f626a6563743b294c6a6176612f6c616e672f537472696e674275666665723b0c010001010a00fa01020100042064692008010401002c284c6a6176612f6c616e672f537472696e673b294c6a6176612f6c616e672f537472696e674275666665723b0c010001060a00fa0107010008746f537472696e6701001428294c6a6176612f6c616e672f537472696e673b0c0109010a0a00fa010b0a00fa002e01000b6576616c756174654f6c6401000b6765744f6c6456616c75650c010f00e00a00ae01100a00c601100100116576616c75617465457374696d61746564010011676574457374696d6174656456616c75650c011400e00a00c6011501000a536f7572636546696c650021000200040000002300020005000600000002000700060000000200080006000000020009000600000002000a000600000002000b000600000002000c000600000002000d000600000002000e000600000002000f000600000002001000060000000200110006000000020012000600000002001300060000000200140006000000020015000600000002001600170000000200180017000000020019001700000002001a001700000002001b001700000002001c001700000002001d001700000002001e001700000002001f001700000002002000170000000200210017000000020022002300000002002400230000000200250023000000020026002300000002002700230000000200280023000000020029002300000002002a0023000000080001002b002c0001002d0000015c00020001000000b42ab7002f2a01b500312a01b500332a01b500352a01b500372a01b500392a01b5003b2a01b5003d2a01b5003f2a01b500412a01b500432a01b500452a01b500472a01b500492a01b5004b2a01b5004d2a01b5004f2a01b500512a01b500532a01b500552a01b500572a01b500592a01b5005b2a01b5005d2a01b5005f2a01b500612a01b500632a01b500652a01b500672a01b500692a01b5006b2a01b5006d2a01b5006f2a01b500712a01b500732a01b50075b1000000010076000000960025000000150004001c0009001d000e001e0013001f00180020001d00210022002200270023002c00240031002500360026003b00270040002800450029004a002a004f002b0054002c0059002d005e002e0063002f00680030006d00310072003200770033007c00340081003500860036008b00370090003800950039009a003a009f003b00a4003c00a9003d00ae003e00b300150001007700780001002d0000003400020004000000102a2bb7007c2a2cb7007f2a2db70082b10000000100760000001200040000004a0005004b000a004c000f004d00020079007a0001002d0000014900030002000000f12a2b1284b9008a0200c0008cb500312a2b128eb9008a0200c0008cb500332a2b1290b9008a0200c0008cb500352a2b1292b9008a0200c0008cb500372a2b1294b9008a0200c0008cb500392a2b1296b9008a0200c0008cb5003b2a2b1298b9008a0200c0008cb5003d2a2b129ab9008a0200c0008cb5003f2a2b129cb9008a0200c0008cb500412a2b129eb9008a0200c0008cb500432a2b12a0b9008a0200c0008cb500452a2b12a2b9008a0200c0008cb500472a2b12a4b9008a0200c0008cb500492a2b12a6b9008a0200c0008cb5004b2a2b12a8b9008a0200c0008cb5004d2a2b12aab9008a0200c0008cb5004fb100000001007600000046001100000055000f0056001e0057002d0058003c0059004b005a005a005b0069005c0078005d0087005e0096005f00a5006000b4006100c3006200d2006300e1006400f000650002007d007a0001002d000000ea00030002000000a62a2b12acb9008a0200c000aeb500512a2b12b0b9008a0200c000aeb500532a2b12b2b9008a0200c000aeb500552a2b12b4b9008a0200c000aeb500572a2b12b6b9008a0200c000aeb500592a2b12b8b9008a0200c000aeb5005b2a2b12bab9008a0200c000aeb5005d2a2b12bcb9008a0200c000aeb5005f2a2b12beb9008a0200c000aeb500612a2b12c0b9008a0200c000aeb500632a2b12c2b9008a0200c000aeb50065b100000001007600000032000c0000006d000f006e001e006f002d0070003c0071004b0072005a00730069007400780075008700760096007700a5007800020080007a0001002d000000b100030002000000792a2b12c4b9008a0200c000c6b500672a2b12c8b9008a0200c000c6b500692a2b12cab9008a0200c000c6b5006b2a2b12ccb9008a0200c000c6b5006d2a2b12ceb9008a0200c000c6b5006f2a2b12d0b9008a0200c000c6b500712a2b12d2b9008a0200c000c6b500732a2b12d4b9008a0200c000c6b50075b100000001007600000026000900000080000f0081001e0082002d0083003c0084004b0085005a00860069008700780088000100d500d6000200d700000004000100d9002d000003e800040003000002bc014d1baa000002b7000000000000002100000095000000a1000000ad000000b9000000c5000000d1000000dd000000e9000000f5000001010000010d0000011900000125000001310000013d0000014b00000159000001670000017500000183000001910000019f000001b7000001cf000001f0000001fe0000020c0000021a0000022800000233000002560000027100000294000002afbb00db5904b700de4da70219bb00db5904b700de4da7020dbb00db5904b700de4da70201bb00db5903b700de4da701f5bb00db5904b700de4da701e9bb00db5903b700de4da701ddbb00db5904b700de4da701d1bb00db5903b700de4da701c5bb00db5904b700de4da701b9bb00db5903b700de4da701adbb00db5904b700de4da701a1bb00db5903b700de4da70195bb00db5904b700de4da70189bb00db5903b700de4da7017d2ab40063b600e2c000e44da7016f2ab40063b600e2c000e44da701612ab4005fb600e2c000e44da701532ab4005fb600e2c000e44da701452ab40055b600e2c000e44da701372ab40055b600e2c000e44da701292ab40053b600e2c000e44da7011b2ab4003db600e5c0008612e7b9008a0200c000e94da701032ab4003db600e5c0008612ebb9008a0200c000e94da700ebbb00ed592ab40067b600eec000dbb600f204a4000704a7000403b700f54da700ca2ab40059b600e2c000e44da700bc2ab4005db600e2c000e44da700ae2ab40065b600e2c000e44da700a02ab4005bb600e2c000e44da70092bb00f759b700f84da70087bb00fa5912fcb700ff2ab40067b600eec000dbb60103130105b60108b6010c4da70064bb00fa59b7010d2ab40067b600eec000dbb60103b6010c4da70049bb00fa5912fcb700ff2ab40067b600eec000dbb60103130105b60108b6010c4da70026bb00fa59b7010d2ab40067b600eec000dbb60103b6010c4da7000bbb00f759b700f84d2cb00000000100760000011a004600000090000200920098009600a1009700a4009b00ad009c00b000a000b900a100bc00a500c500a600c800aa00d100ab00d400af00dd00b000e000b400e900b500ec00b900f500ba00f800be010100bf010400c3010d00c4011000c8011900c9011c00cd012500ce012800d2013100d3013400d7013d00d8014000dc014b00dd014e00e1015900e2015c00e6016700e7016a00eb017500ec017800f0018300f1018600f5019100f6019400fa019f00fb01a200ff01b7010001ba010401cf010501d2010901f0010a01f3010e01fe010f02010113020c0114020f0118021a0119021d011d0228011e022b01220233012302360127025601280259012c0271012d02740131029401320297013602af013702b2013b02ba01430001010e00d6000200d700000004000100d9002d000003e800040003000002bc014d1baa000002b7000000000000002100000095000000a1000000ad000000b9000000c5000000d1000000dd000000e9000000f5000001010000010d0000011900000125000001310000013d0000014b00000159000001670000017500000183000001910000019f000001b7000001cf000001f0000001fe0000020c0000021a0000022800000233000002560000027100000294000002afbb00db5904b700de4da70219bb00db5904b700de4da7020dbb00db5904b700de4da70201bb00db5903b700de4da701f5bb00db5904b700de4da701e9bb00db5903b700de4da701ddbb00db5904b700de4da701d1bb00db5903b700de4da701c5bb00db5904b700de4da701b9bb00db5903b700de4da701adbb00db5904b700de4da701a1bb00db5903b700de4da70195bb00db5904b700de4da70189bb00db5903b700de4da7017d2ab40063b60111c000e44da7016f2ab40063b60111c000e44da701612ab4005fb60111c000e44da701532ab4005fb60111c000e44da701452ab40055b60111c000e44da701372ab40055b60111c000e44da701292ab40053b60111c000e44da7011b2ab4003db600e5c0008612e7b9008a0200c000e94da701032ab4003db600e5c0008612ebb9008a0200c000e94da700ebbb00ed592ab40067b60112c000dbb600f204a4000704a7000403b700f54da700ca2ab40059b60111c000e44da700bc2ab4005db60111c000e44da700ae2ab40065b60111c000e44da700a02ab4005bb60111c000e44da70092bb00f759b700f84da70087bb00fa5912fcb700ff2ab40067b60112c000dbb60103130105b60108b6010c4da70064bb00fa59b7010d2ab40067b60112c000dbb60103b6010c4da70049bb00fa5912fcb700ff2ab40067b60112c000dbb60103130105b60108b6010c4da70026bb00fa59b7010d2ab40067b60112c000dbb60103b6010c4da7000bbb00f759b700f84d2cb00000000100760000011a00460000014c0002014e0098015200a1015300a4015700ad015800b0015c00b9015d00bc016100c5016200c8016600d1016700d4016b00dd016c00e0017000e9017100ec017500f5017600f8017a0101017b0104017f010d01800110018401190185011c01890125018a0128018e0131018f01340193013d019401400198014b0199014e019d0159019e015c01a2016701a3016a01a7017501a8017801ac018301ad018601b1019101b2019401b6019f01b701a201bb01b701bc01ba01c001cf01c101d201c501f001c601f301ca01fe01cb020101cf020c01d0020f01d4021a01d5021d01d9022801da022b01de023301df023601e3025601e4025901e8027101e9027401ed029401ee029701f202af01f302b201f702ba01ff0001011300d6000200d700000004000100d9002d000003e800040003000002bc014d1baa000002b7000000000000002100000095000000a1000000ad000000b9000000c5000000d1000000dd000000e9000000f5000001010000010d0000011900000125000001310000013d0000014b00000159000001670000017500000183000001910000019f000001b7000001cf000001f0000001fe0000020c0000021a0000022800000233000002560000027100000294000002afbb00db5904b700de4da70219bb00db5904b700de4da7020dbb00db5904b700de4da70201bb00db5903b700de4da701f5bb00db5904b700de4da701e9bb00db5903b700de4da701ddbb00db5904b700de4da701d1bb00db5903b700de4da701c5bb00db5904b700de4da701b9bb00db5903b700de4da701adbb00db5904b700de4da701a1bb00db5903b700de4da70195bb00db5904b700de4da70189bb00db5903b700de4da7017d2ab40063b600e2c000e44da7016f2ab40063b600e2c000e44da701612ab4005fb600e2c000e44da701532ab4005fb600e2c000e44da701452ab40055b600e2c000e44da701372ab40055b600e2c000e44da701292ab40053b600e2c000e44da7011b2ab4003db600e5c0008612e7b9008a0200c000e94da701032ab4003db600e5c0008612ebb9008a0200c000e94da700ebbb00ed592ab40067b60116c000dbb600f204a4000704a7000403b700f54da700ca2ab40059b600e2c000e44da700bc2ab4005db600e2c000e44da700ae2ab40065b600e2c000e44da700a02ab4005bb600e2c000e44da70092bb00f759b700f84da70087bb00fa5912fcb700ff2ab40067b60116c000dbb60103130105b60108b6010c4da70064bb00fa59b7010d2ab40067b60116c000dbb60103b6010c4da70049bb00fa5912fcb700ff2ab40067b60116c000dbb60103130105b60108b6010c4da70026bb00fa59b7010d2ab40067b60116c000dbb60103b6010c4da7000bbb00f759b700f84d2cb00000000100760000011a0046000002080002020a0098020e00a1020f00a4021300ad021400b0021800b9021900bc021d00c5021e00c8022200d1022300d4022700dd022800e0022c00e9022d00ec023100f5023200f80236010102370104023b010d023c0110024001190241011c0245012502460128024a0131024b0134024f013d025001400254014b0255014e02590159025a015c025e0167025f016a02630175026401780268018302690186026d0191026e01940272019f027301a2027701b7027801ba027c01cf027d01d2028101f0028201f3028601fe02870201028b020c028c020f0290021a0291021d029502280296022b029a0233029b0236029f025602a0025902a4027102a5027402a9029402aa029702ae02af02af02b202b302ba02bb000101170000000200017400155f313331343131363331353737385f3131323834397400326e65742e73662e6a61737065727265706f7274732e656e67696e652e64657369676e2e4a524a61766163436f6d70696c6572', '\\xaced0005737200286e65742e73662e6a61737065727265706f7274732e656e67696e652e4a61737065725265706f727400000000000027d80200034c000b636f6d70696c65446174617400164c6a6176612f696f2f53657269616c697a61626c653b4c0011636f6d70696c654e616d655375666669787400124c6a6176612f6c616e672f537472696e673b4c000d636f6d70696c6572436c61737371007e00027872002d6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173655265706f727400000000000027d802002749000c626f74746f6d4d617267696e49000b636f6c756d6e436f756e7449000d636f6c756d6e53706163696e6749000b636f6c756d6e57696474685a001069676e6f7265506167696e6174696f6e5a00136973466c6f6174436f6c756d6e466f6f7465725a0010697353756d6d6172794e6577506167655a0020697353756d6d6172795769746850616765486561646572416e64466f6f7465725a000e69735469746c654e65775061676549000a6c6566744d617267696e42000b6f7269656e746174696f6e49000a7061676548656967687449000970616765576964746842000a7072696e744f7264657249000b72696768744d617267696e490009746f704d617267696e42000e7768656e4e6f44617461547970654c000a6261636b67726f756e647400244c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5242616e643b4c000c636f6c756d6e466f6f74657271007e00044c000c636f6c756d6e48656164657271007e00045b000864617461736574737400285b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52446174617365743b4c000b64656661756c74466f6e7474002a4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525265706f7274466f6e743b4c000c64656661756c745374796c657400254c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525374796c653b4c000664657461696c71007e00044c000d64657461696c53656374696f6e7400274c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5253656374696f6e3b5b0005666f6e747374002b5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525265706f7274466f6e743b4c0012666f726d6174466163746f7279436c61737371007e00024c000a696d706f72747353657474000f4c6a6176612f7574696c2f5365743b4c00086c616e677561676571007e00024c000e6c61737450616765466f6f74657271007e00044c000b6d61696e446174617365747400274c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52446174617365743b4c00046e616d6571007e00024c00066e6f4461746171007e00044c000a70616765466f6f74657271007e00044c000a7061676548656164657271007e00045b00067374796c65737400265b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525374796c653b4c000773756d6d61727971007e00045b000974656d706c6174657374002f5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525265706f727454656d706c6174653b4c00057469746c6571007e000478700000001400000001000000000000030e00000000000000001e02000002530000034a010000001e00000014017372002b6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736542616e6400000000000027d802000549001950534555444f5f53455249414c5f56455253494f4e5f5549444900066865696768745a000e697353706c6974416c6c6f7765644c00137072696e745768656e45787072657373696f6e74002a4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5245787072657373696f6e3b4c000973706c6974547970657400104c6a6176612f6c616e672f427974653b787200336e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365456c656d656e7447726f757000000000000027d80200024c00086368696c6472656e7400104c6a6176612f7574696c2f4c6973743b4c000c656c656d656e7447726f757074002c4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52456c656d656e7447726f75703b7870737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000a7870000077260000000001707372000e6a6176612e6c616e672e427974659c4e6084ee50f51c02000142000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b0200007870017371007e000f7371007e00160000000077040000000a78700000772600000005017071007e001a7371007e000f7371007e00160000000577040000000a737200306e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736552656374616e676c6500000000000027d80200014c00067261646975737400134c6a6176612f6c616e672f496e74656765723b787200356e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736547726170686963456c656d656e7400000000000027d80200034c000466696c6c71007e00114c00076c696e6550656e7400234c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5250656e3b4c000370656e71007e00117872002e6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365456c656d656e7400000000000027d80200164900066865696768745a001769735072696e74496e466972737457686f6c6542616e645a001569735072696e74526570656174656456616c7565735a001a69735072696e745768656e44657461696c4f766572666c6f77735a0015697352656d6f76654c696e655768656e426c616e6b42000c706f736974696f6e5479706542000b7374726574636854797065490005776964746849000178490001794c00096261636b636f6c6f727400104c6a6176612f6177742f436f6c6f723b4c001464656661756c745374796c6550726f76696465727400344c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5244656661756c745374796c6550726f76696465723b4c000c656c656d656e7447726f757071007e00144c0009666f7265636f6c6f7271007e00244c00036b657971007e00024c00046d6f646571007e00114c000b706172656e745374796c6571007e00074c0018706172656e745374796c654e616d655265666572656e636571007e00024c00137072696e745768656e45787072657373696f6e71007e00104c00157072696e745768656e47726f75704368616e6765737400254c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5247726f75703b4c000d70726f706572746965734d617074002d4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5250726f706572746965734d61703b5b001370726f706572747945787072657373696f6e737400335b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5250726f706572747945787072657373696f6e3b7870000000200001000002000000030a00000001000000027372000e6a6176612e6177742e436f6c6f7201a51783108f337502000546000666616c70686149000576616c75654c0002637374001b4c6a6176612f6177742f636f6c6f722f436f6c6f7253706163653b5b00096672676276616c75657400025b465b00066676616c756571007e002c787000000000fff0f0f070707071007e000e71007e001d7074000b72656374616e676c652d317371007e001801707070707070707372002a6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736550656e00000000000027d80200044c00096c696e65436f6c6f7271007e00244c00096c696e655374796c6571007e00114c00096c696e6557696474687400114c6a6176612f6c616e672f466c6f61743b4c000c70656e436f6e7461696e657274002c4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5250656e436f6e7461696e65723b7870707371007e0018007372000f6a6176612e6c616e672e466c6f6174daedc9a2db3cf0ec02000146000576616c75657871007e00190000000071007e00297070737200316e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173655374617469635465787400000000000027d80200014c00047465787471007e0002787200326e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736554657874456c656d656e7400000000000027d80200204c0006626f7264657271007e00114c000b626f72646572436f6c6f7271007e00244c000c626f74746f6d426f7264657271007e00114c0011626f74746f6d426f72646572436f6c6f7271007e00244c000d626f74746f6d50616464696e6771007e00204c0008666f6e744e616d6571007e00024c0008666f6e7453697a6571007e00204c0013686f72697a6f6e74616c416c69676e6d656e7471007e00114c00066973426f6c647400134c6a6176612f6c616e672f426f6f6c65616e3b4c000869734974616c696371007e00394c000d6973506466456d62656464656471007e00394c000f6973537472696b655468726f75676871007e00394c000c69735374796c65645465787471007e00394c000b6973556e6465726c696e6571007e00394c000a6c656674426f7264657271007e00114c000f6c656674426f72646572436f6c6f7271007e00244c000b6c65667450616464696e6771007e00204c00076c696e65426f787400274c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a524c696e65426f783b4c000b6c696e6553706163696e6771007e00114c00066d61726b757071007e00024c000770616464696e6771007e00204c000b706466456e636f64696e6771007e00024c000b706466466f6e744e616d6571007e00024c000a7265706f7274466f6e7471007e00064c000b7269676874426f7264657271007e00114c00107269676874426f72646572436f6c6f7271007e00244c000c726967687450616464696e6771007e00204c0008726f746174696f6e71007e00114c0009746f70426f7264657271007e00114c000e746f70426f72646572436f6c6f7271007e00244c000a746f7050616464696e6771007e00204c0011766572746963616c416c69676e6d656e7471007e00117871007e00230000000e0001000002000000005f0000004d000000037071007e000e71007e001d7074000c737461746963546578742d3370707070707070707070707070737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c75657871007e00190000000a70737200116a6176612e6c616e672e426f6f6c65616ecd207280d59cfaee0200015a000576616c756578700170707070707070707372002e6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173654c696e65426f7800000000000027d802000b4c000d626f74746f6d50616464696e6771007e00204c0009626f74746f6d50656e74002b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f626173652f4a52426f7850656e3b4c000c626f78436f6e7461696e657274002c4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52426f78436f6e7461696e65723b4c000b6c65667450616464696e6771007e00204c00076c65667450656e71007e00424c000770616464696e6771007e00204c000370656e71007e00424c000c726967687450616464696e6771007e00204c0008726967687450656e71007e00424c000a746f7050616464696e6771007e00204c0006746f7050656e71007e0042787070737200336e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365426f78426f74746f6d50656e00000000000027d80200007872002d6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365426f7850656e00000000000027d80200014c00076c696e65426f7871007e003a7871007e00307371007e002a00000000ff00000070707071007e00347371007e00350000000071007e004471007e004471007e003b70737200316e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365426f784c65667450656e00000000000027d80200007871007e00467371007e002a00000000ff00000070707071007e00347371007e00350000000071007e004471007e0044707371007e004670707071007e004471007e004470737200326e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365426f78526967687450656e00000000000027d80200007871007e00467371007e002a00000000ff00000070707071007e00347371007e00350000000071007e004471007e004470737200306e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365426f78546f7050656e00000000000027d80200007871007e00467371007e002a00000000ff00000070707071007e00347371007e00350000000071007e004471007e00447070707074000e48656c7665746963612d426f6c6470707070707070707074000b4173736574204272616e647371007e00370000000e0001000002000000005f0000004d000000127071007e000e71007e001d7074000d737461746963546578742d3130707070707070707070707070707371007e003d0000000a7071007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e005c71007e005c71007e0059707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e005c71007e005c707371007e004670707071007e005c71007e005c707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e005c71007e005c707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e005c71007e005c7070707074000e48656c7665746963612d426f6c6470707070707070707074000e41737365742041737369676e65657371007e00370000000e0001000002000000008b000000b4000000127071007e000e71007e001d7074000d737461746963546578742d3131707070707070707070707070707371007e003d0000000a7071007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e006f71007e006f71007e006c707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e006f71007e006f707371007e004670707071007e006f71007e006f707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e006f71007e006f707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e006f71007e006f7070707074000e48656c7665746963612d426f6c6470707070707070707074000e41737369676e656520656d61696c7371007e00370000000e0001000002000000008a000000b4000000037071007e000e71007e001d7074000d737461746963546578742d3132707070707070707070707070707371007e003d0000000a7071007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e008271007e008271007e007f707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e008271007e0082707371007e004670707071007e008271007e0082707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e008271007e0082707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e008271007e00827070707074000e48656c7665746963612d426f6c647070707070707070707400114173736574204465736372697074696f6e78700000772600000027017071007e001a707070707372002e6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736553656374696f6e00000000000027d80200015b000562616e64737400255b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5242616e643b7870757200255b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5242616e643b95dd7eec8cca85350200007870000000017371007e000f7371007e00160000000577040000000a737200306e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365546578744669656c6400000000000027d802001149000d626f6f6b6d61726b4c6576656c42000e6576616c756174696f6e54696d6542000f68797065726c696e6b54617267657442000d68797065726c696e6b547970655a0015697353747265746368576974684f766572666c6f774c0014616e63686f724e616d6545787072657373696f6e71007e00104c000f6576616c756174696f6e47726f757071007e00264c000a65787072657373696f6e71007e00104c001968797065726c696e6b416e63686f7245787072657373696f6e71007e00104c001768797065726c696e6b5061676545787072657373696f6e71007e00105b001368797065726c696e6b506172616d65746572737400335b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5248797065726c696e6b506172616d657465723b4c001c68797065726c696e6b5265666572656e636545787072657373696f6e71007e00104c001a68797065726c696e6b546f6f6c74697045787072657373696f6e71007e00104c000f6973426c616e6b5768656e4e756c6c71007e00394c000a6c696e6b54617267657471007e00024c00086c696e6b5479706571007e00024c00077061747465726e71007e00027871007e00380000000e00010000020000000100000000b30000000f7071007e000e71007e009770740009746578744669656c64707070707070707070707070707371007e003d00000009707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e009e71007e009e71007e009b707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e009e71007e009e707371007e004670707071007e009e71007e009e707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e009e71007e009e707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e009e71007e009e707070707070707070707070707000000000010100017070737200316e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736545787072657373696f6e00000000000027d802000449000269645b00066368756e6b737400305b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5245787072657373696f6e4368756e6b3b4c000e76616c7565436c6173734e616d6571007e00024c001276616c7565436c6173735265616c4e616d6571007e0002787000000018757200305b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5245787072657373696f6e4368756e6b3b6d59cfde694ba355020000787000000001737200366e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736545787072657373696f6e4368756e6b00000000000027d8020002420004747970654c00047465787471007e0002787003740005456d61696c7400106a6176612e6c616e672e537472696e6770707070707071007e00407070707372002b6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173654c696e6500000000000027d8020001420009646972656374696f6e7871007e0021000000010001000002000000030d000000010000001f7071007e000e71007e00977371007e002a00000000ffcbc7c77070707400066c696e652d3170707070707070707371007e003070707071007e00b670017371007e00990000000f0001000002000000006400000048000000007071007e000e71007e00977070707070707070707070707070707371007e003d0000000a707070707070707070707371007e0041707371007e004570707071007e00bc71007e00bc71007e00ba707371007e004a70707071007e00bc71007e00bc707371007e004670707071007e00bc71007e00bc707371007e004f70707071007e00bc71007e00bc707371007e005370707071007e00bc71007e00bc7070707070707070707070707070000000000101000070707371007e00ac000000197571007e00af000000017371007e00b10374000a41737365744272616e647400106a6176612e6c616e672e537472696e6770707070707071007e00407070707371007e00990000000e00010000020000000064000000480000000f7071007e000e71007e00977070707070707070707070707070707371007e003d0000000a707070707070707070707371007e0041707371007e004570707071007e00c971007e00c971007e00c7707371007e004a70707071007e00c971007e00c9707371007e004670707071007e00c971007e00c9707371007e004f70707071007e00c971007e00c9707371007e005370707071007e00c971007e00c97070707070707070707070707070000000000101000070707371007e00ac0000001a7571007e00af000000017371007e00b10374000841737369676e65657400106a6176612e6c616e672e537472696e6770707070707071007e00407070707371007e00990000000f00010000020000000100000000b3000000007071007e000e71007e00977070707070707070707070707070707371007e003d0000000a707070707070707070707371007e0041707371007e004570707071007e00d671007e00d671007e00d4707371007e004a70707071007e00d671007e00d6707371007e004670707071007e00d671007e00d6707371007e004f70707071007e00d671007e00d6707371007e005370707071007e00d671007e00d67070707070707070707070707070000000000101000070707371007e00ac0000001b7571007e00af000000017371007e00b10374001041737365744465736372697074696f6e7400106a6176612e6c616e672e537472696e6770707070707071007e004070707078700000772600000021017071007e001a7070737200116a6176612e7574696c2e48617368536574ba44859596b8b7340300007870770c000000043f400000000000037400226e65742e73662e6a61737065727265706f7274732e656e67696e652e646174612e2a74001d6e65742e73662e6a61737065727265706f7274732e656e67696e652e2a74000b6a6176612e7574696c2e2a787400046a6176617371007e000f7371007e00160000000477040000000a7371007e0099000000120001000002000000004d000002ac000000017071007e000e71007e00e77074000b746578744669656c642d3170707070707070707070707070707371007e0018037070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e00ec71007e00ec71007e00e9707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e00ec71007e00ec707371007e004670707071007e00ec71007e00ec707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e00ec71007e00ec707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e00ec71007e00ec7070707070707070707070707070000000000101000070707371007e00ac0000001f7571007e00af000000037371007e00b10174000a22506167652022202b207371007e00b10474000b504147455f4e554d4245527371007e00b101740009202b202220646920227400106a6176612e6c616e672e537472696e677070707070707371007e003f007070707371007e00990000001200010000020000000014000002f9000000017071007e000e71007e00e77074000b746578744669656c642d32707070707070707070707070707071007e00eb7070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e010671007e010671007e0104707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e010671007e0106707371007e004670707071007e010671007e0106707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e010671007e0106707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e010671007e01067070707070707070707070707070000000000201000070707371007e00ac000000207571007e00af000000037371007e00b1017400052222202b207371007e00b10474000b504147455f4e554d4245527371007e00b101740005202b2022227400106a6176612e6c616e672e537472696e6770707070707071007e01037070707371007e009900000012000100000200000000480000001f000000017071007e000e71007e00e77074000b746578744669656c642d337070707070707070707070707070707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e011f71007e011f71007e011d707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e011f71007e011f707371007e004670707071007e011f71007e011f707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e011f71007e011f707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e011f71007e011f7070707070707070707070707070000000000201000070707371007e00ac000000217571007e00af000000017371007e00b1017400146e6577206a6176612e7574696c2e44617465282974000e6a6176612e7574696c2e4461746570707070707071007e0103707074000a4d4d2f64642f797979797371007e0037000000120001000002000000001c00000001000000017071007e000e71007e00e77074000d737461746963546578742d32367070707070707070707070707070707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e013571007e013571007e0133707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e013571007e0135707371007e004670707071007e013571007e0135707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e013571007e0135707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e013571007e01357070707070707070707070707070740005446174653a7870000077260000001a017071007e001a7372002e6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173654461746173657400000000000027d802000e5a000669734d61696e4200177768656e5265736f757263654d697373696e67547970655b00066669656c64737400265b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a524669656c643b4c001066696c74657245787072657373696f6e71007e00105b000667726f7570737400265b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5247726f75703b4c00046e616d6571007e00025b000a706172616d657465727374002a5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52506172616d657465723b4c000d70726f706572746965734d617071007e00274c000571756572797400254c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5251756572793b4c000e7265736f7572636542756e646c6571007e00024c000e7363726970746c6574436c61737371007e00025b000a7363726970746c65747374002a5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525363726970746c65743b5b000a736f72744669656c647374002a5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a52536f72744669656c643b5b00097661726961626c65737400295b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525661726961626c653b78700101757200265b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a524669656c643b023cdfc74e2af27002000078700000000b7372002c6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173654669656c6400000000000027d80200054c000b6465736372697074696f6e71007e00024c00046e616d6571007e00024c000d70726f706572746965734d617071007e00274c000e76616c7565436c6173734e616d6571007e00024c001276616c7565436c6173735265616c4e616d6571007e000278707400007400094173736574436f64657372002b6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5250726f706572746965734d617000000000000027d80200034c00046261736571007e00274c000e70726f706572746965734c69737471007e00134c000d70726f706572746965734d617074000f4c6a6176612f7574696c2f4d61703b78707070707400106a6176612e6c616e672e537472696e67707371007e014f74000074001041737365744465736372697074696f6e7371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f74000074000a41737365744272616e647371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f74000074000d576f726b706c616365436f64657371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f740000740014576f726b706c6163654465736372697074696f6e7371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f74000074000841737369676e65657371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f740000740005456d61696c7371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f740000740008526f6f6d436f64657371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f74000074000f526f6f6d4465736372697074696f6e7371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f740000740010466c6f6f724465736372697074696f6e7371007e01537070707400106a6176612e6c616e672e537472696e67707371007e014f7400007400134275696c64696e674465736372697074696f6e7371007e01537070707400106a6176612e6c616e672e537472696e677070757200265b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5247726f75703b40a35f7a4cfd78ea0200007870000000037372002c6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736547726f757000000000000027d802000e42000e666f6f746572506f736974696f6e5a0019697352657072696e744865616465724f6e45616368506167655a001169735265736574506167654e756d6265725a0010697353746172744e6577436f6c756d6e5a000e697353746172744e6577506167655a000c6b656570546f6765746865724900176d696e486569676874546f53746172744e6577506167654c000d636f756e745661726961626c657400284c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a525661726961626c653b4c000a65787072657373696f6e71007e00104c000b67726f7570466f6f74657271007e00044c001267726f7570466f6f74657253656374696f6e71007e00084c000b67726f757048656164657271007e00044c001267726f757048656164657253656374696f6e71007e00084c00046e616d6571007e00027870010000000000000000007372002f6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a52426173655661726961626c6500000000000027d802000d42000b63616c63756c6174696f6e42000d696e6372656d656e74547970655a000f697353797374656d446566696e65644200097265736574547970654c000a65787072657373696f6e71007e00104c000e696e6372656d656e7447726f757071007e00264c001b696e6372656d656e746572466163746f7279436c6173734e616d6571007e00024c001f696e6372656d656e746572466163746f7279436c6173735265616c4e616d6571007e00024c0016696e697469616c56616c756545787072657373696f6e71007e00104c00046e616d6571007e00024c000a726573657447726f757071007e00264c000e76616c7565436c6173734e616d6571007e00024c001276616c7565436c6173735265616c4e616d6571007e00027870010501047371007e00ac000000087571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e74656765722831297400116a6176612e6c616e672e496e7465676572707070707371007e00ac000000097571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000d70616c617a7a6f5f434f554e5471007e018d71007e0194707371007e00ac0000000e7571007e00af000000017371007e00b1037400134275696c64696e674465736372697074696f6e7400106a6176612e6c616e672e4f626a65637470707371007e00927571007e0095000000017371007e000f7371007e00160000000077040000000a78700000772600000000017071007e001a707371007e00927571007e0095000000017371007e000f7371007e00160000000377040000000a7371007e001f000000110001000002000000030a00000001000000067371007e002a00000000ffe0fae970707071007e000e71007e01a57074000b72656374616e676c652d3271007e002f707070707070707371007e00307071007e00347371007e00350000000071007e01a770707371007e0037000000160001000002000000003800000004000000047071007e000e71007e01a57371007e002a00000000ff00666670707074000d737461746963546578742d3139707070707070707070707070707371007e003d0000000e707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01b071007e01b071007e01ac707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01b071007e01b0707371007e004670707071007e01b071007e01b0707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01b071007e01b0707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01b071007e01b070707070707070707070707070707400094275696c64696e673a7371007e009900000014000100000200000000c40000003f000000047071007e000e71007e01a57070707070707070707070707070707371007e003d0000000e707070707070707070707371007e0041707371007e004570707071007e01c171007e01c171007e01bf707371007e004a70707071007e01c171007e01c1707371007e004670707071007e01c171007e01c1707371007e004f70707071007e01c171007e01c1707371007e005370707071007e01c171007e01c17070707070707070707070707070000000000101000070707371007e00ac0000000f7571007e00af000000017371007e00b1037400134275696c64696e674465736372697074696f6e7400106a6176612e6c616e672e537472696e67707070707070707070707870000077260000001b017071007e001a74000770616c617a7a6f7371007e018b010000000000000000007371007e018e010501047371007e00ac0000000a7571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e0194707070707371007e00ac0000000b7571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000c7461766f6c615f434f554e5471007e01cd71007e0194707371007e00ac000000107571007e00af000000017371007e00b103740010466c6f6f724465736372697074696f6e71007e019e70707371007e00927571007e0095000000017371007e000f7371007e00160000000077040000000a78700000772600000000017071007e001a707371007e00927571007e0095000000017371007e000f7371007e00160000000377040000000a7371007e001f00000013000100000200000002f900000012000000047371007e002a00000000fff5ecec70707071007e000e71007e01e27074000b72656374616e676c652d3371007e002f707070707070707371007e00307071007e00347371007e00350000000071007e01e470707371007e0037000000120001000002000000002800000017000000057071007e000e71007e01e27371007e002a00000000ff66000070707074000d737461746963546578742d3230707070707070707070707070707371007e003d0000000c707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01ed71007e01ed71007e01e9707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01ed71007e01ed707371007e004670707071007e01ed71007e01ed707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01ed71007e01ed707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e01ed71007e01ed7070707070707070707070707070740006466c6f6f723a7371007e009900000012000100000200000000cc00000048000000057071007e000e71007e01e270707070707070707070707070707070707070707070707070707371007e0041707371007e004570707071007e01fd71007e01fd71007e01fc707371007e004a70707071007e01fd71007e01fd707371007e004670707071007e01fd71007e01fd707371007e004f70707071007e01fd71007e01fd707371007e005370707071007e01fd71007e01fd7070707070707070707070707070000000000101000070707371007e00ac000000117571007e00af000000017371007e00b103740010466c6f6f724465736372697074696f6e7400106a6176612e6c616e672e537472696e67707070707070707070707870000077260000001b017071007e001a7400067461766f6c617371007e018b010000000000000000007371007e018e010501047371007e00ac0000000c7571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e0194707070707371007e00ac0000000d7571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000c7374616e7a615f434f554e5471007e020971007e0194707371007e00ac000000127571007e00af000000017371007e00b103740008526f6f6d436f646571007e019e70707371007e00927571007e0095000000017371007e000f7371007e00160000000077040000000a78700000772600000000017071007e001a707371007e00927571007e0095000000017371007e000f7371007e00160000000477040000000a7371007e001f00000013000100000200000002e300000028000000057371007e002a00000000ffe2fafa70707071007e000e71007e021e7074000b72656374616e676c652d3471007e002f707070707070707371007e00307071007e00347371007e00350000000071007e022070707371007e0037000000120001000002000000002d0000002c000000057071007e000e71007e021e7371007e002a00000000ff00009970707074000d737461746963546578742d32317070707070707070707070707070707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e022871007e022871007e0225707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e022871007e0228707371007e004670707071007e022871007e0228707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e022871007e0228707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e022871007e02287070707070707070707070707070740005526f6f6d3a7371007e009900000012000100000200000000640000005c000000057071007e000e71007e021e70707070707070707070707070707070707070707070707070707371007e0041707371007e004570707071007e023871007e023871007e0237707371007e004a70707071007e023871007e0238707371007e004670707071007e023871007e0238707371007e004f70707071007e023871007e0238707371007e005370707071007e023871007e02387070707070707070707070707070000000000101000070707371007e00ac000000137571007e00af000000017371007e00b103740008526f6f6d436f64657400106a6176612e6c616e672e537472696e67707070707070707070707371007e009900000012000100000200000000d5000000ce000000057071007e000e71007e021e70707070707070707070707070707070707070707070707070707371007e0041707371007e004570707071007e024471007e024471007e0243707371007e004a70707071007e024471007e0244707371007e004670707071007e024471007e0244707371007e004f70707071007e024471007e0244707371007e005370707071007e024471007e02447070707070707070707070707070000000000101000070707371007e00ac000000147571007e00af000000017371007e00b10374000f526f6f6d4465736372697074696f6e7400106a6176612e6c616e672e537472696e67707070707070707070707870000077260000001b017071007e001a7400067374616e7a6174000941737365744c6973747572002a5b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a52506172616d657465723b22000c8d2ac36021020000787000000010737200306e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365506172616d6574657200000000000027d80200095a000e6973466f7250726f6d7074696e675a000f697353797374656d446566696e65644c001664656661756c7456616c756545787072657373696f6e71007e00104c000b6465736372697074696f6e71007e00024c00046e616d6571007e00024c000e6e6573746564547970654e616d6571007e00024c000d70726f706572746965734d617071007e00274c000e76616c7565436c6173734e616d6571007e00024c001276616c7565436c6173735265616c4e616d6571007e00027870010170707400155245504f52545f504152414d45544552535f4d4150707371007e015370707074000d6a6176612e7574696c2e4d6170707371007e02530101707074000d4a41535045525f5245504f5254707371007e01537070707400286e65742e73662e6a61737065727265706f7274732e656e67696e652e4a61737065725265706f7274707371007e0253010170707400115245504f52545f434f4e4e454354494f4e707371007e01537070707400136a6176612e73716c2e436f6e6e656374696f6e707371007e0253010170707400105245504f52545f4d41585f434f554e54707371007e015370707071007e0194707371007e0253010170707400125245504f52545f444154415f534f55524345707371007e01537070707400286e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5244617461536f75726365707371007e0253010170707400105245504f52545f5343524950544c4554707371007e015370707074002f6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5241627374726163745363726970746c6574707371007e02530101707074000d5245504f52545f4c4f43414c45707371007e01537070707400106a6176612e7574696c2e4c6f63616c65707371007e0253010170707400165245504f52545f5245534f555243455f42554e444c45707371007e01537070707400186a6176612e7574696c2e5265736f7572636542756e646c65707371007e0253010170707400105245504f52545f54494d455f5a4f4e45707371007e01537070707400126a6176612e7574696c2e54696d655a6f6e65707371007e0253010170707400155245504f52545f464f524d41545f464143544f5259707371007e015370707074002e6e65742e73662e6a61737065727265706f7274732e656e67696e652e7574696c2e466f726d6174466163746f7279707371007e0253010170707400135245504f52545f434c4153535f4c4f41444552707371007e01537070707400156a6176612e6c616e672e436c6173734c6f61646572707371007e02530101707074001a5245504f52545f55524c5f48414e444c45525f464143544f5259707371007e01537070707400206a6176612e6e65742e55524c53747265616d48616e646c6572466163746f7279707371007e0253010170707400145245504f52545f46494c455f5245534f4c564552707371007e015370707074002d6e65742e73662e6a61737065727265706f7274732e656e67696e652e7574696c2e46696c655265736f6c766572707371007e0253010170707400125245504f52545f5649525455414c495a4552707371007e01537070707400296e65742e73662e6a61737065727265706f7274732e656e67696e652e4a525669727475616c697a6572707371007e02530101707074001449535f49474e4f52455f504147494e4154494f4e707371007e01537070707400116a6176612e6c616e672e426f6f6c65616e707371007e0253010170707400105245504f52545f54454d504c41544553707371007e01537070707400146a6176612e7574696c2e436f6c6c656374696f6e707371007e0153707371007e00160000000577040000000a740019697265706f72742e7363726970746c657468616e646c696e67740010697265706f72742e656e636f64696e6774000c697265706f72742e7a6f6f6d740009697265706f72742e78740009697265706f72742e7978737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000571007e0297740003312e3071007e02967400055554462d3871007e02987400013071007e02997400013071007e029574000132787372002c6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365517565727900000000000027d80200025b00066368756e6b7374002b5b4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f4a5251756572794368756e6b3b4c00086c616e677561676571007e000278707572002b5b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a5251756572794368756e6b3b409f00a1e8ba34a4020000787000000001737200316e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a524261736551756572794368756e6b00000000000027d8020003420004747970654c00047465787471007e00025b0006746f6b656e737400135b4c6a6176612f6c616e672f537472696e673b7870017404d053454c4543540a224173736574222e22436f64652220415320224173736574436f6465222c206d617828224173736574222e224465736372697074696f6e2229204153202241737365744465736372697074696f6e222c206d617828224c6f6f6b557031222e224465736372697074696f6e2229204153202241737365744272616e64222c0a22576f726b706c616365222e22436f6465222041532022576f726b706c616365436f6465222c206d61782822576f726b706c616365222e224465736372697074696f6e22292041532022576f726b706c6163654465736372697074696f6e222c206d61782822456d706c6f796565222e224465736372697074696f6e2229206173202241737369676e6565222c206d6178286c6f7765722822456d706c6f796565222e22456d61696c2229292061732022456d61696c222c0a636f616c657363652822526f6f6d222e22436f6465222c20274e6f7420646566696e656427292041532022526f6f6d436f6465222c0a6d617828636f616c657363652822526f6f6d222e224465736372697074696f6e222c274e6f7420646566696e65642729292041532022526f6f6d4465736372697074696f6e222c0a6d617828636f616c657363652822466c6f6f72222e224465736372697074696f6e22202c274e6f7420646566696e65642729292041532022466c6f6f724465736372697074696f6e222c0a6d617828636f616c6573636528224275696c64696e67222e224465736372697074696f6e222c274e6f7420646566696e656427292920415320224275696c64696e674465736372697074696f6e220a46524f4d20224173736574220a4c454654204f55544552204a4f494e2022576f726b706c61636522204f4e2022576f726b706c616365222e224964223d224173736574222e22576f726b706c6163652220414e442022576f726b706c616365222e22537461747573223d2741270a4c454654204f55544552204a4f494e2022456d706c6f79656522204f4e2022456d706c6f796565222e224964223d224173736574222e2241737369676e65652220414e442022456d706c6f796565222e22537461747573223d2741270a4c454654204f55544552204a4f494e2022526f6f6d22204f4e2022526f6f6d222e224964223d224173736574222e22526f6f6d2220414e442022526f6f6d222e22537461747573223d2741270a4c454654204f55544552204a4f494e2022466c6f6f7222204f4e2022466c6f6f72222e224964223d22526f6f6d222e22466c6f6f722220414e442022466c6f6f72222e22537461747573223d2741270a4c454654204f55544552204a4f494e20224275696c64696e6722204f4e20224275696c64696e67222e224964223d22466c6f6f72222e224275696c64696e672220414e4420224275696c64696e67222e22537461747573223d2741270a4c454654204f55544552204a4f494e20224c6f6f6b55702220415320224c6f6f6b55703122204f4e20224c6f6f6b557031222e224964223d224173736574222e224272616e64220a574845524520224173736574222e22537461747573223d2741270a47524f55502042592022526f6f6d222e22436f6465222c2022576f726b706c616365222e22436f6465222c20224173736574222e22436f6465220a4f524445522042592022526f6f6d222e22436f6465227074000373716c70707070757200295b4c6e65742e73662e6a61737065727265706f7274732e656e67696e652e4a525661726961626c653b62e6837c982cb7440200007870000000087371007e018e08050101707070707371007e00ac000000007571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e01947074000b504147455f4e554d4245527071007e0194707371007e018e08050102707070707371007e00ac000000017571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e01947074000d434f4c554d4e5f4e554d4245527071007e0194707371007e018e010501017371007e00ac000000027571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e0194707070707371007e00ac000000037571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000c5245504f52545f434f554e547071007e0194707371007e018e010501027371007e00ac000000047571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e0194707070707371007e00ac000000057571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000a504147455f434f554e547071007e0194707371007e018e010501037371007e00ac000000067571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228312971007e0194707070707371007e00ac000000077571007e00af000000017371007e00b1017400186e6577206a6176612e6c616e672e496e746567657228302971007e01947074000c434f4c554d4e5f434f554e547071007e01947071007e018f71007e01ce71007e020a71007e0250707371007e000f7371007e00160000000477040000000a7371007e009900000012000100000200000000480000001f000000037071007e000e71007e02d770740009746578744669656c647070707070707070707070707070707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02db71007e02db71007e02d9707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02db71007e02db707371007e004670707071007e02db71007e02db707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02db71007e02db707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02db71007e02db7070707070707070707070707070000000000201000070707371007e00ac0000001c7571007e00af000000017371007e00b1017400146e6577206a6176612e7574696c2e44617465282974000e6a6176612e7574696c2e4461746570707070707071007e0103707074000a4d4d2f64642f797979797371007e0099000000120001000002000000004d000002ac000000017071007e000e71007e02d770740009746578744669656c64707070707070707070707070707071007e00eb7070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02f171007e02f171007e02ef707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02f171007e02f1707371007e004670707071007e02f171007e02f1707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02f171007e02f1707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e02f171007e02f17070707070707070707070707070000000000101000070707371007e00ac0000001d7571007e00af000000037371007e00b10174000a22506167652022202b207371007e00b10474000b504147455f4e554d4245527371007e00b101740009202b202220646920227400106a6176612e6c616e672e537472696e6770707070707071007e01037070707371007e00990000001200010000020000000014000002f9000000017071007e000e71007e02d770740009746578744669656c64707070707070707070707070707071007e00eb7070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e030a71007e030a71007e0308707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e030a71007e030a707371007e004670707071007e030a71007e030a707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e030a71007e030a707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e030a71007e030a7070707070707070707070707070000000000201000070707371007e00ac0000001e7571007e00af000000037371007e00b1017400052222202b207371007e00b10474000b504147455f4e554d4245527371007e00b101740005202b2022227400106a6176612e6c616e672e537472696e6770707070707071007e01037070707371007e0037000000120001000002000000001c00000001000000037071007e000e71007e02d77074000d737461746963546578742d32357070707070707070707070707070707070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e032371007e032371007e0321707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e032371007e0323707371007e004670707071007e032371007e0323707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e032371007e0323707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e032371007e03237070707070707070707070707070740005446174653a78700000772600000019017071007e001a7371007e000f7371007e00160000000277040000000a7371007e00370000000f000100000200000000820000028c000000017071007e000e71007e03327074000d737461746963546578742d3238707070707070707070707070707071007e00eb71007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e033671007e033671007e0334707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e033671007e0336707371007e004670707071007e033671007e0336707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e033671007e0336707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e033671007e03367070707074000e48656c7665746963612d426f6c647070707070707070707400155374616d7061746f20636f6e20434d444275696c647371007e0037000000120001010002000000018f000000c0000000017071007e000e71007e03327074000d737461746963546578742d3239707070707070707070707070707371007e003d0000000c7371007e00180271007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e034a71007e034a71007e0346707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e034a71007e034a707371007e004670707071007e034a71007e034a707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e034a71007e034a707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e034a71007e034a7070707074000e48656c7665746963612d426f6c647070707070707070707400194c6f636174696f6e206c69737420776974682061737365747378700000772600000024017371007e00ac000000177571007e00af000000037371007e00b10174000e6e657720426f6f6c65616e2028207371007e00b10474000b504147455f4e554d4245527371007e00b1017400112e696e7456616c75652829203e2031202971007e028e7071007e001a707371007e000f7371007e00160000000077040000000a78700000772600000005017071007e001a707371007e000f7371007e00160000000377040000000a7371007e00370000001a0001000002000000018f000000c0000000127071007e000e71007e03647074000c737461746963546578742d31707070707070707070707070707371007e003d0000001071007e034971007e004070707070707070707371007e0041707371007e00457371007e002a00000000ff00000070707071007e00347371007e00350000000071007e036971007e036971007e0366707371007e004a7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e036971007e0369707371007e004670707071007e036971007e0369707371007e004f7371007e002a00000000ff00000070707071007e00347371007e00350000000071007e036971007e0369707371007e00537371007e002a00000000ff00000070707071007e00347371007e00350000000071007e036971007e03697070707074000e48656c7665746963612d426f6c647070707070707070707400194c6f636174696f6e206c6973742077697468206173736574737372002c6e65742e73662e6a61737065727265706f7274732e656e67696e652e626173652e4a5242617365496d61676500000000000027d802002449000d626f6f6b6d61726b4c6576656c42000e6576616c756174696f6e54696d6542000f68797065726c696e6b54617267657442000d68797065726c696e6b547970655a000669734c617a7942000b6f6e4572726f72547970654c0014616e63686f724e616d6545787072657373696f6e71007e00104c0006626f7264657271007e00114c000b626f72646572436f6c6f7271007e00244c000c626f74746f6d426f7264657271007e00114c0011626f74746f6d426f72646572436f6c6f7271007e00244c000d626f74746f6d50616464696e6771007e00204c000f6576616c756174696f6e47726f757071007e00264c000a65787072657373696f6e71007e00104c0013686f72697a6f6e74616c416c69676e6d656e7471007e00114c001968797065726c696e6b416e63686f7245787072657373696f6e71007e00104c001768797065726c696e6b5061676545787072657373696f6e71007e00105b001368797065726c696e6b506172616d657465727371007e009a4c001c68797065726c696e6b5265666572656e636545787072657373696f6e71007e00104c001a68797065726c696e6b546f6f6c74697045787072657373696f6e71007e00104c000c69735573696e67436163686571007e00394c000a6c656674426f7264657271007e00114c000f6c656674426f72646572436f6c6f7271007e00244c000b6c65667450616464696e6771007e00204c00076c696e65426f7871007e003a4c000a6c696e6b54617267657471007e00024c00086c696e6b5479706571007e00024c000770616464696e6771007e00204c000b7269676874426f7264657271007e00114c00107269676874426f72646572436f6c6f7271007e00244c000c726967687450616464696e6771007e00204c000a7363616c65496d61676571007e00114c0009746f70426f7264657271007e00114c000e746f70426f72646572436f6c6f7271007e00244c000a746f7050616464696e6771007e00204c0011766572746963616c416c69676e6d656e7471007e00117871007e0021000000250001000002000000007100000001000000007071007e000e71007e0364707070707070707070707371007e003070707071007e037a70000000000101000002707070707070707371007e00ac000000157571007e00af000000027371007e00b1027400155245504f52545f504152414d45544552535f4d41507371007e00b10174000e2e6765742822494d4147453022297400136a6176612e696f2e496e70757453747265616d7070707070707071007e00407070707371007e0041707371007e004570707071007e038371007e038371007e037a707371007e004a70707071007e038371007e0383707371007e004670707071007e038371007e0383707371007e004f70707071007e038371007e0383707371007e005370707071007e038371007e038370707070707070707070707371007e037900000025000100000200000000710000029d000000007071007e000e71007e0364707070707070707070707371007e003070707071007e038970000000000101000002707070707070707371007e00ac000000167571007e00af000000027371007e00b1027400155245504f52545f504152414d45544552535f4d41507371007e00b10174000e2e6765742822494d41474531222971007e03827070707070707071007e00407070707371007e0041707371007e004570707071007e039171007e039171007e0389707371007e004a70707071007e039171007e0391707371007e004670707071007e039171007e0391707371007e004f70707071007e039171007e0391707371007e005370707071007e039171007e039170707070707070707070707870000077260000003c017071007e001a737200366e65742e73662e6a61737065727265706f7274732e656e67696e652e64657369676e2e4a525265706f7274436f6d70696c654461746100000000000027d80200034c001363726f7373746162436f6d70696c654461746171007e01544c001264617461736574436f6d70696c654461746171007e01544c00166d61696e44617461736574436f6d70696c654461746171007e000178707371007e029a3f4000000000000c77080000001000000000787371007e029a3f4000000000000c7708000000100000000078757200025b42acf317f8060854e0020000787000001f2bcafebabe0000002e011801001e41737365744c6973745f313331343131363331353737385f31313238343907000101002c6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a524576616c7561746f72070003010017706172616d657465725f5245504f52545f4c4f43414c450100324c6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c506172616d657465723b010017706172616d657465725f4a41535045525f5245504f525401001c706172616d657465725f5245504f52545f5649525455414c495a455201001a706172616d657465725f5245504f52545f54494d455f5a4f4e4501001e706172616d657465725f5245504f52545f46494c455f5245534f4c56455201001a706172616d657465725f5245504f52545f5343524950544c455401001f706172616d657465725f5245504f52545f504152414d45544552535f4d415001001b706172616d657465725f5245504f52545f434f4e4e454354494f4e01001d706172616d657465725f5245504f52545f434c4153535f4c4f4144455201001c706172616d657465725f5245504f52545f444154415f534f55524345010024706172616d657465725f5245504f52545f55524c5f48414e444c45525f464143544f525901001e706172616d657465725f49535f49474e4f52455f504147494e4154494f4e01001f706172616d657465725f5245504f52545f464f524d41545f464143544f525901001a706172616d657465725f5245504f52545f4d41585f434f554e5401001a706172616d657465725f5245504f52545f54454d504c41544553010020706172616d657465725f5245504f52545f5245534f555243455f42554e444c4501000f6669656c645f4173736574436f646501002e4c6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c4669656c643b0100156669656c645f526f6f6d4465736372697074696f6e01000e6669656c645f526f6f6d436f64650100136669656c645f576f726b706c616365436f646501000b6669656c645f456d61696c0100166669656c645f41737365744465736372697074696f6e0100106669656c645f41737365744272616e640100166669656c645f466c6f6f724465736372697074696f6e01001a6669656c645f576f726b706c6163654465736372697074696f6e0100196669656c645f4275696c64696e674465736372697074696f6e01000e6669656c645f41737369676e65650100147661726961626c655f504147455f4e554d4245520100314c6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c5661726961626c653b0100167661726961626c655f434f4c554d4e5f4e554d4245520100157661726961626c655f5245504f52545f434f554e540100137661726961626c655f504147455f434f554e540100157661726961626c655f434f4c554d4e5f434f554e540100167661726961626c655f70616c617a7a6f5f434f554e540100157661726961626c655f7461766f6c615f434f554e540100157661726961626c655f7374616e7a615f434f554e540100063c696e69743e010003282956010004436f64650c002b002c0a0004002e0c0005000609000200300c0007000609000200320c0008000609000200340c0009000609000200360c000a000609000200380c000b0006090002003a0c000c0006090002003c0c000d0006090002003e0c000e000609000200400c000f000609000200420c0010000609000200440c0011000609000200460c0012000609000200480c00130006090002004a0c00140006090002004c0c00150006090002004e0c0016001709000200500c0018001709000200520c0019001709000200540c001a001709000200560c001b001709000200580c001c0017090002005a0c001d0017090002005c0c001e0017090002005e0c001f001709000200600c0020001709000200620c0021001709000200640c0022002309000200660c0024002309000200680c00250023090002006a0c00260023090002006c0c00270023090002006e0c0028002309000200700c0029002309000200720c002a0023090002007401000f4c696e654e756d6265725461626c6501000e637573746f6d697a6564496e6974010030284c6a6176612f7574696c2f4d61703b4c6a6176612f7574696c2f4d61703b4c6a6176612f7574696c2f4d61703b295601000a696e6974506172616d73010012284c6a6176612f7574696c2f4d61703b29560c0079007a0a0002007b01000a696e69744669656c64730c007d007a0a0002007e010008696e6974566172730c0080007a0a0002008101000d5245504f52545f4c4f43414c4508008301000d6a6176612f7574696c2f4d6170070085010003676574010026284c6a6176612f6c616e672f4f626a6563743b294c6a6176612f6c616e672f4f626a6563743b0c008700880b008600890100306e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c506172616d6574657207008b01000d4a41535045525f5245504f525408008d0100125245504f52545f5649525455414c495a455208008f0100105245504f52545f54494d455f5a4f4e450800910100145245504f52545f46494c455f5245534f4c5645520800930100105245504f52545f5343524950544c45540800950100155245504f52545f504152414d45544552535f4d41500800970100115245504f52545f434f4e4e454354494f4e0800990100135245504f52545f434c4153535f4c4f4144455208009b0100125245504f52545f444154415f534f5552434508009d01001a5245504f52545f55524c5f48414e444c45525f464143544f525908009f01001449535f49474e4f52455f504147494e4154494f4e0800a10100155245504f52545f464f524d41545f464143544f52590800a30100105245504f52545f4d41585f434f554e540800a50100105245504f52545f54454d504c415445530800a70100165245504f52545f5245534f555243455f42554e444c450800a90100094173736574436f64650800ab01002c6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c4669656c640700ad01000f526f6f6d4465736372697074696f6e0800af010008526f6f6d436f64650800b101000d576f726b706c616365436f64650800b3010005456d61696c0800b501001041737365744465736372697074696f6e0800b701000a41737365744272616e640800b9010010466c6f6f724465736372697074696f6e0800bb010014576f726b706c6163654465736372697074696f6e0800bd0100134275696c64696e674465736372697074696f6e0800bf01000841737369676e65650800c101000b504147455f4e554d4245520800c301002f6e65742f73662f6a61737065727265706f7274732f656e67696e652f66696c6c2f4a5246696c6c5661726961626c650700c501000d434f4c554d4e5f4e554d4245520800c701000c5245504f52545f434f554e540800c901000a504147455f434f554e540800cb01000c434f4c554d4e5f434f554e540800cd01000d70616c617a7a6f5f434f554e540800cf01000c7461766f6c615f434f554e540800d101000c7374616e7a615f434f554e540800d30100086576616c756174650100152849294c6a6176612f6c616e672f4f626a6563743b01000a457863657074696f6e730100136a6176612f6c616e672f5468726f7761626c650700d80100116a6176612f6c616e672f496e74656765720700da010004284929560c002b00dc0a00db00dd01000867657456616c756501001428294c6a6176612f6c616e672f4f626a6563743b0c00df00e00a00ae00e10100106a6176612f6c616e672f537472696e670700e30a008c00e1010006494d414745300800e60100136a6176612f696f2f496e70757453747265616d0700e8010006494d414745310800ea0100116a6176612f6c616e672f426f6f6c65616e0700ec0a00c600e1010008696e7456616c75650100032829490c00ef00f00a00db00f1010004285a29560c002b00f30a00ed00f401000e6a6176612f7574696c2f446174650700f60a00f7002e0100166a6176612f6c616e672f537472696e674275666665720700f901000550616765200800fb010015284c6a6176612f6c616e672f537472696e673b29560c002b00fd0a00fa00fe010006617070656e6401002c284c6a6176612f6c616e672f4f626a6563743b294c6a6176612f6c616e672f537472696e674275666665723b0c010001010a00fa01020100042064692008010401002c284c6a6176612f6c616e672f537472696e673b294c6a6176612f6c616e672f537472696e674275666665723b0c010001060a00fa0107010008746f537472696e6701001428294c6a6176612f6c616e672f537472696e673b0c0109010a0a00fa010b0a00fa002e01000b6576616c756174654f6c6401000b6765744f6c6456616c75650c010f00e00a00ae01100a00c601100100116576616c75617465457374696d61746564010011676574457374696d6174656456616c75650c011400e00a00c6011501000a536f7572636546696c650021000200040000002300020005000600000002000700060000000200080006000000020009000600000002000a000600000002000b000600000002000c000600000002000d000600000002000e000600000002000f000600000002001000060000000200110006000000020012000600000002001300060000000200140006000000020015000600000002001600170000000200180017000000020019001700000002001a001700000002001b001700000002001c001700000002001d001700000002001e001700000002001f001700000002002000170000000200210017000000020022002300000002002400230000000200250023000000020026002300000002002700230000000200280023000000020029002300000002002a0023000000080001002b002c0001002d0000015c00020001000000b42ab7002f2a01b500312a01b500332a01b500352a01b500372a01b500392a01b5003b2a01b5003d2a01b5003f2a01b500412a01b500432a01b500452a01b500472a01b500492a01b5004b2a01b5004d2a01b5004f2a01b500512a01b500532a01b500552a01b500572a01b500592a01b5005b2a01b5005d2a01b5005f2a01b500612a01b500632a01b500652a01b500672a01b500692a01b5006b2a01b5006d2a01b5006f2a01b500712a01b500732a01b50075b1000000010076000000960025000000150004001c0009001d000e001e0013001f00180020001d00210022002200270023002c00240031002500360026003b00270040002800450029004a002a004f002b0054002c0059002d005e002e0063002f00680030006d00310072003200770033007c00340081003500860036008b00370090003800950039009a003a009f003b00a4003c00a9003d00ae003e00b300150001007700780001002d0000003400020004000000102a2bb7007c2a2cb7007f2a2db70082b10000000100760000001200040000004a0005004b000a004c000f004d00020079007a0001002d0000014900030002000000f12a2b1284b9008a0200c0008cb500312a2b128eb9008a0200c0008cb500332a2b1290b9008a0200c0008cb500352a2b1292b9008a0200c0008cb500372a2b1294b9008a0200c0008cb500392a2b1296b9008a0200c0008cb5003b2a2b1298b9008a0200c0008cb5003d2a2b129ab9008a0200c0008cb5003f2a2b129cb9008a0200c0008cb500412a2b129eb9008a0200c0008cb500432a2b12a0b9008a0200c0008cb500452a2b12a2b9008a0200c0008cb500472a2b12a4b9008a0200c0008cb500492a2b12a6b9008a0200c0008cb5004b2a2b12a8b9008a0200c0008cb5004d2a2b12aab9008a0200c0008cb5004fb100000001007600000046001100000055000f0056001e0057002d0058003c0059004b005a005a005b0069005c0078005d0087005e0096005f00a5006000b4006100c3006200d2006300e1006400f000650002007d007a0001002d000000ea00030002000000a62a2b12acb9008a0200c000aeb500512a2b12b0b9008a0200c000aeb500532a2b12b2b9008a0200c000aeb500552a2b12b4b9008a0200c000aeb500572a2b12b6b9008a0200c000aeb500592a2b12b8b9008a0200c000aeb5005b2a2b12bab9008a0200c000aeb5005d2a2b12bcb9008a0200c000aeb5005f2a2b12beb9008a0200c000aeb500612a2b12c0b9008a0200c000aeb500632a2b12c2b9008a0200c000aeb50065b100000001007600000032000c0000006d000f006e001e006f002d0070003c0071004b0072005a00730069007400780075008700760096007700a5007800020080007a0001002d000000b100030002000000792a2b12c4b9008a0200c000c6b500672a2b12c8b9008a0200c000c6b500692a2b12cab9008a0200c000c6b5006b2a2b12ccb9008a0200c000c6b5006d2a2b12ceb9008a0200c000c6b5006f2a2b12d0b9008a0200c000c6b500712a2b12d2b9008a0200c000c6b500732a2b12d4b9008a0200c000c6b50075b100000001007600000026000900000080000f0081001e0082002d0083003c0084004b0085005a00860069008700780088000100d500d6000200d700000004000100d9002d000003e800040003000002bc014d1baa000002b7000000000000002100000095000000a1000000ad000000b9000000c5000000d1000000dd000000e9000000f5000001010000010d0000011900000125000001310000013d0000014b00000159000001670000017500000183000001910000019f000001b7000001cf000001f0000001fe0000020c0000021a0000022800000233000002560000027100000294000002afbb00db5904b700de4da70219bb00db5904b700de4da7020dbb00db5904b700de4da70201bb00db5903b700de4da701f5bb00db5904b700de4da701e9bb00db5903b700de4da701ddbb00db5904b700de4da701d1bb00db5903b700de4da701c5bb00db5904b700de4da701b9bb00db5903b700de4da701adbb00db5904b700de4da701a1bb00db5903b700de4da70195bb00db5904b700de4da70189bb00db5903b700de4da7017d2ab40063b600e2c000e44da7016f2ab40063b600e2c000e44da701612ab4005fb600e2c000e44da701532ab4005fb600e2c000e44da701452ab40055b600e2c000e44da701372ab40055b600e2c000e44da701292ab40053b600e2c000e44da7011b2ab4003db600e5c0008612e7b9008a0200c000e94da701032ab4003db600e5c0008612ebb9008a0200c000e94da700ebbb00ed592ab40067b600eec000dbb600f204a4000704a7000403b700f54da700ca2ab40059b600e2c000e44da700bc2ab4005db600e2c000e44da700ae2ab40065b600e2c000e44da700a02ab4005bb600e2c000e44da70092bb00f759b700f84da70087bb00fa5912fcb700ff2ab40067b600eec000dbb60103130105b60108b6010c4da70064bb00fa59b7010d2ab40067b600eec000dbb60103b6010c4da70049bb00fa5912fcb700ff2ab40067b600eec000dbb60103130105b60108b6010c4da70026bb00fa59b7010d2ab40067b600eec000dbb60103b6010c4da7000bbb00f759b700f84d2cb00000000100760000011a004600000090000200920098009600a1009700a4009b00ad009c00b000a000b900a100bc00a500c500a600c800aa00d100ab00d400af00dd00b000e000b400e900b500ec00b900f500ba00f800be010100bf010400c3010d00c4011000c8011900c9011c00cd012500ce012800d2013100d3013400d7013d00d8014000dc014b00dd014e00e1015900e2015c00e6016700e7016a00eb017500ec017800f0018300f1018600f5019100f6019400fa019f00fb01a200ff01b7010001ba010401cf010501d2010901f0010a01f3010e01fe010f02010113020c0114020f0118021a0119021d011d0228011e022b01220233012302360127025601280259012c0271012d02740131029401320297013602af013702b2013b02ba01430001010e00d6000200d700000004000100d9002d000003e800040003000002bc014d1baa000002b7000000000000002100000095000000a1000000ad000000b9000000c5000000d1000000dd000000e9000000f5000001010000010d0000011900000125000001310000013d0000014b00000159000001670000017500000183000001910000019f000001b7000001cf000001f0000001fe0000020c0000021a0000022800000233000002560000027100000294000002afbb00db5904b700de4da70219bb00db5904b700de4da7020dbb00db5904b700de4da70201bb00db5903b700de4da701f5bb00db5904b700de4da701e9bb00db5903b700de4da701ddbb00db5904b700de4da701d1bb00db5903b700de4da701c5bb00db5904b700de4da701b9bb00db5903b700de4da701adbb00db5904b700de4da701a1bb00db5903b700de4da70195bb00db5904b700de4da70189bb00db5903b700de4da7017d2ab40063b60111c000e44da7016f2ab40063b60111c000e44da701612ab4005fb60111c000e44da701532ab4005fb60111c000e44da701452ab40055b60111c000e44da701372ab40055b60111c000e44da701292ab40053b60111c000e44da7011b2ab4003db600e5c0008612e7b9008a0200c000e94da701032ab4003db600e5c0008612ebb9008a0200c000e94da700ebbb00ed592ab40067b60112c000dbb600f204a4000704a7000403b700f54da700ca2ab40059b60111c000e44da700bc2ab4005db60111c000e44da700ae2ab40065b60111c000e44da700a02ab4005bb60111c000e44da70092bb00f759b700f84da70087bb00fa5912fcb700ff2ab40067b60112c000dbb60103130105b60108b6010c4da70064bb00fa59b7010d2ab40067b60112c000dbb60103b6010c4da70049bb00fa5912fcb700ff2ab40067b60112c000dbb60103130105b60108b6010c4da70026bb00fa59b7010d2ab40067b60112c000dbb60103b6010c4da7000bbb00f759b700f84d2cb00000000100760000011a00460000014c0002014e0098015200a1015300a4015700ad015800b0015c00b9015d00bc016100c5016200c8016600d1016700d4016b00dd016c00e0017000e9017100ec017500f5017600f8017a0101017b0104017f010d01800110018401190185011c01890125018a0128018e0131018f01340193013d019401400198014b0199014e019d0159019e015c01a2016701a3016a01a7017501a8017801ac018301ad018601b1019101b2019401b6019f01b701a201bb01b701bc01ba01c001cf01c101d201c501f001c601f301ca01fe01cb020101cf020c01d0020f01d4021a01d5021d01d9022801da022b01de023301df023601e3025601e4025901e8027101e9027401ed029401ee029701f202af01f302b201f702ba01ff0001011300d6000200d700000004000100d9002d000003e800040003000002bc014d1baa000002b7000000000000002100000095000000a1000000ad000000b9000000c5000000d1000000dd000000e9000000f5000001010000010d0000011900000125000001310000013d0000014b00000159000001670000017500000183000001910000019f000001b7000001cf000001f0000001fe0000020c0000021a0000022800000233000002560000027100000294000002afbb00db5904b700de4da70219bb00db5904b700de4da7020dbb00db5904b700de4da70201bb00db5903b700de4da701f5bb00db5904b700de4da701e9bb00db5903b700de4da701ddbb00db5904b700de4da701d1bb00db5903b700de4da701c5bb00db5904b700de4da701b9bb00db5903b700de4da701adbb00db5904b700de4da701a1bb00db5903b700de4da70195bb00db5904b700de4da70189bb00db5903b700de4da7017d2ab40063b600e2c000e44da7016f2ab40063b600e2c000e44da701612ab4005fb600e2c000e44da701532ab4005fb600e2c000e44da701452ab40055b600e2c000e44da701372ab40055b600e2c000e44da701292ab40053b600e2c000e44da7011b2ab4003db600e5c0008612e7b9008a0200c000e94da701032ab4003db600e5c0008612ebb9008a0200c000e94da700ebbb00ed592ab40067b60116c000dbb600f204a4000704a7000403b700f54da700ca2ab40059b600e2c000e44da700bc2ab4005db600e2c000e44da700ae2ab40065b600e2c000e44da700a02ab4005bb600e2c000e44da70092bb00f759b700f84da70087bb00fa5912fcb700ff2ab40067b60116c000dbb60103130105b60108b6010c4da70064bb00fa59b7010d2ab40067b60116c000dbb60103b6010c4da70049bb00fa5912fcb700ff2ab40067b60116c000dbb60103130105b60108b6010c4da70026bb00fa59b7010d2ab40067b60116c000dbb60103b6010c4da7000bbb00f759b700f84d2cb00000000100760000011a0046000002080002020a0098020e00a1020f00a4021300ad021400b0021800b9021900bc021d00c5021e00c8022200d1022300d4022700dd022800e0022c00e9022d00ec023100f5023200f80236010102370104023b010d023c0110024001190241011c0245012502460128024a0131024b0134024f013d025001400254014b0255014e02590159025a015c025e0167025f016a02630175026401780268018302690186026d0191026e01940272019f027301a2027701b7027801ba027c01cf027d01d2028101f0028201f3028601fe02870201028b020c028c020f0290021a0291021d029502280296022b029a0233029b0236029f025602a0025902a4027102a5027402a9029402aa029702ae02af02af02b202b302ba02bb000101170000000200017400155f313331343131363331353737385f3131323834397400326e65742e73662e6a61737065727265706f7274732e656e67696e652e64657369676e2e4a524a61766163436f6d70696c6572', '\\xaced000570', '\\xffd8ffe000104a46494600010101006000600000ffdb00430001010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101ffdb00430101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101ffc00011080025007103012200021101031101ffc4001c00000202030101000000000000000000000008090a0506070104ffc4003a1000010402020103010603030d0000000004020305060107000812091113141516213151911922230a4158173252546181929596b1d1d3d5ffc4001b01000203010101000000000000000000000005060708040201ffc4003911000203000102040303070d0000000000020301040506111200071314153141162122081724536292a13245465152546163647191d1f0ffda000c03010002110311003f00bfc7126df1848173b01d7b86d9533132f5dabd7f4895422a7198f8ad85246c9c6bc01cf421c1090369979e3eb7888b05b72880742c20264e1dc14f1c9eebbf76d27486a9b46cac413d6572bff6432cc3b2564243e4cdcd47c18ae1a72463140c78cfc8b649a4e057d481da730db4a7149c738dd7f653fda0eadda2de140aaaf626c3997e34241f990183bad08b667ebd251925914375e19b9e8b8b2db5ac560819d43a3672b5b387dc493c8f1639187129beb8e44788ce46197e9bfd52c455f56632fc3bd2f6be98df72ebfa52ff0071245df0a95c11c328c7d29c72df8aa7f071d31c72bbdeaec8d23aa5746afa7ea7ad2735409ddf0a9544476cb20e6065a6a8b33c3d52b4c5a5e6c8b2b30312d581f6b29536f4cb60b0992710a461285e16661e579b694a17efe484e139c631b1723f7b45b4a7eeda32aaf68dd931d15759e9da295290b57b90519762622ca2b82370d1ae847b1291a5e2c12b06e98e61d095f4021ad2df4a16a42f79b39fb66b3d5d02832d70318ecb5da91274da6ca432199ab026f6746968026f09c383b24b7551d4cca5927df7470056c27cd78af95d1f0ffa46bb5dc92d601e36bd7ad5b2a8698f24722bfd9fb2cbb6ee55665d6b0bb2cbccd3a21506ddd44d015aeadbaa60e61b617e06e7ad78e8d58d1cf6b9d7ed529c65b5b3ac80ad5eb3c6f394690ac34ad1582af5991688cdf5de24b010ef972787101f4ded17da9ebdf5e7ee2f6f775ab79ed072e360991acae4aca4f39155c3b23623a0953932d332123f0bcd146e32e3496c4c1b8119ca90ce339dc765f63f60ab654bea5d07ae46d8766a88639f77969390fa28383514d21e16250b4383a5d9079b5a70acb863794bbf20ed0cf38c14b1f8fcc3e63c67cb65fafb1a4ed34d9d88c3c40e3d95afb3a1c9344c2c3928c2c6a944b5ee1b2a54b57887d92e6bd3aefb367d1528ca3a789f1cd9e60d24e6d555534509d3d266adfa19b4b22989a54c769e8d9b2146b00d8b08ac33360a5b61ca4a60d8c119733c539fcf18fdb1c3db1fa63f6c723e6cddd9928cd1b1fb123e8c3337d46c22b5d586912c614a662e5a223cd91982192064b24bc3207606718ca938f8f24a99754e299cb8accee6edfcb6b7a56959682abc44e5af6a56212d66c39451a80a1632563a1dccba950d9faaca1c96986810d4ff00b61d4b2ffbe72e379c62afb3f948f945573f5f519c8de54b138f71ee4d7da18fad2619bca2e2f3f1d4baf34c6c3349b6dab559ca8545fa325d6e213027db364f937e60bede7d11c750d9d3d7d7c5a807a34204ae61d72b7a0c26c589505204011a6f49fb5b511fa3b5bdc3d5edf6c7e98fdb9e6129c7e494e3dff3f6c631efeff9ff007710c9fecbef52f616c6a86afd435ebc01ac19affde425c9a2403d264ac3264890c6696f25b79e68864f19a69943eea70c37975195ba94ab13b2fbb7291da6350ec0d594802c376db3b118d761522c32240f88f9965a916e58670c0f03a96b14f1c06d921c4b0ca853db7de6dacab094c9f89f9bfc2399f20bbc6316dea46b539ddf4874707672e96a8718d81e3fc85b85a77a9233b6978db06ba3a139d6ac1576b57263005dd0937bcbee4dc7326b6de957a3342c465f7cd3d4cebd66816d677c5b203528d5b2cb99a7a39f076aa45c42a1cb59f6cf74444c8578a71f92718ff763878a7f4c7ed8ff0067fe31fb638826a2ed96cf277211a4bb1daa60f52d888a19db16126e36d2d48c13b5e8b2081e41e947485b8c02cb5f447b9f5cb914a1ac04f2081db42da7d6e36365eb854755a631b0293989bc1cc45d2a5316b82cc75c24ca52d0347558dc1ff4d603885b6e2581225d2c8794dad2db6aca558c5a4096b23aad4c38eeedea00451ddda47dbd4627f176011f4f9f689174e913310793019e844233d3af492889e9d607afdf3f2ee288ebfd7311f398f1baf8a7fd1c7e3f9fe18fc7fbff00eff8f0f6c7e98fdb1c84ade9ea59d87d3ed493c9d39a8e4b171efc50ba89a1db1aff008b13b6aa558c4b1973fb06d8dd5666455053d128850d0dd69ec47902ae4d9fb4074e3c739713bc7de5a1f53fadbd8adb55ab0ebabe6ccd13508b9c27562ee11ae4c352962b242552ba3d9a1620e76c51001d2f3c137f2b820ee3a95612cafc969cf24e7c2b900d8c6ac1554f6ef5a0a79b15ad22c831e6da15e21ac49985708b3a35ea9b1c4003661c992834b22150ee674aeeb49a6b0cf54bacfaaa62884201ecea026224c9955763444224897d8711da6332f8fb63f4c7ed8e1cafcff121f51dff0009da63fe7b7fff00ddc38dff00367c8ff5f81f4fe90657d7a7fa8fda8fe3e38fed4667f6343e9fcdf6febd3fcbfda8fe3e24306a96d0afd9c1967f52dcadf231d3d777b6d4c3f73ad195edab479f666c78288aa57662cce3071b16b2ebf22c424d44d447891e124a3049125720d352199d7bafe9db76e1799a858cb153b4d06544c0ca6b148c753e2ee7b0a1045267a46cb5ac321911e1410a4815a3614558f1d663e3dc2e6183c68e8e709642d1ab5168987e633b0367c065f6d86feccac5b951310d7c0de1bf9190b00bf86dc77dbcdf5fc99f91cce55ed8f7f6e532bd507bb9ea1bd04eddec4d43ae77459eb9a9e74d5df75e3b2b090b2afcb03665fd7cc12f4d1d1ab7e4ce6661d25a39c5ab0a42b2df8b4d32a693855c2f86ea73bda8c1c7b19c8be755f6d51a565b594f1ad212d524955ec91be1664e85fa7112a538e4a3b3a4f66d6cd5c1a53a17576595c5ab49cd55034d72deb026706c540ae4a203bbbbf96403d3f1758b9c99a57509f149842759d1b316da12864666b1102fd3f87b782c57861192057919c61487c775b790bc6169730ac633c526dac58343dbad7175b02f1b06d779a7e33a8ed6b189bc59287588236398b9d59cc4a909155135e6cd1ad712fb997a56c8592d444aa260a8d8bc3d46ff00e383ea6bfe2424bfe99ac7ff0033936fe89dd85eee7a81ee3bc593b05b82eb31ab352d54b62266a0068bab16cddacae082fd9e24c4747254f21510d3ef1e22b0ea53f18b9ca50bca5789cf2cf23398f0cc2b9c876ae71d1a14a502615b46d32cb5965eaaea5575333922d649b60a47d41e8b033f90cf84593cef176afa33a927466c3fd49196d650a8054b261930c6c9c88c08cc7776cfe2911f997dd652d0f39392a75e454d8ee573a047fdd8cd46d97d874c4588c9a3059472e5148f684aeb9211112b6a09d0cd7629bcb27494ac53643ecc6b786979eb25e29b459ced519799f8a83b147ed8b14ccf6250b60539c856145ac275869e5a5e2daf2c13861b1d2e2d4e3a84a51953c8f3722a1add151927645379d8d64cba32c6fa1b6da15351cdf9ad0bf9db17210fe04a7c3c50ef9e7d92b5e3c73e5ef8d1ef9d5dd1fb2ad68ba5ba9221f60ce58c9a5305180372ff004c96db63ed81c37d966414869a6d8f91f4e5d5308432b5a9b42129c77e67714e65abafc1796704fb39677b85dfdf30cce576b428e4daadc8f0ac6336d8dbcca5a36557b31869b095c55ecb754ef54f7158dc0d8bd784eef1ca3439460f29f8c272b9255ca12bb848a96afa1d8faa9d10af35eed9a893ad7405aa61faf04878d57fa4e1592e527dd1b0a177fb9a78a8081958283268fbdb61e63a64308439d44054e562c2955b0112536b14d946d3f4843ab43cfa1597b2da5394297c169293768bda5e6e5995b62cd5c34e69aab8ef67dd68ab6a6821666ee7b68ce549c0854ca5a77e442bcb391c8c2d09ce309ccc317a775c9b30dcebb5b15b398a41bae45c0ae3c206153e43cb05c40400ee36188dba95651f330ca1f4b784a12e61294e318888d05aa609ba33515576836f5c26c1f7410d9a77b4639681dc16688cf9119faa28b61d711824af95e67cf39656dabdb3ccedc87f266f30b9472a6f21dee4bc6743e217b2ed6e8a576e82f6273e8705e3b2734559ed4d54062e7f35d2a79e0f6aabecdfc75cbcc14db68b7b27ce9e238784bc8cac5d9a9ed2b5d465c9957b679d16ad727d8818b2cb6b6586169dbe354ac5b25831b9d5344e1624c5d76a83d75d9949a9573b31b86db61888ecd976b5ce59a19f3c5c499a045fcd88c0818e53b82c95bab2f2202d36dabe452b09c2bc70a561047e993f7099ea6eb726f2ad472d6a9adc3bd9bb4b8e3031150664b2cfd84733930a0196df347acb79030a259ce55208cb7fd45653c97b8fe9975be3a499954eb700c259230561b939096920dc7f0bf93cc808c35d1494f9ff00329a21a71a5fe295b6a4e729cfdfb5ba8da0b75d800b3ec6a422725e32145af47badcb4c46b024406f9440e1b01c69a288da10e98fe7c92ce14a4e508ce7286d184da1e51f953ceb8ee9718d1e77638bac38471ee579189538cddd4d23d5d6e73bd4f7b92723d8b5a3978e358c8e82eb51cdad5ed084dbbd62c5d61ca04611e6073be2faf4f6e9f1656d91726d6c1d0d37ed56a5486850e2f93632b1b1f3ebd3bda1eb0c0da375ab8e6a267dbd64a6b00faa455fbee25b8eebc6b9f5027e5b650fd889f474f9e84037014515213b542f615a61f5d05514bed4a48c58ad147dc4729f40eacb88f918561e6d4b2c75eb765453e2b727a1e7556364a14d1ba81d6b9bed26dc1002c62428497ad6928391ac4a1cb196e2042333f153f636b0e250e6433c525b565a310e66c12ff46fab24ea4d8ba41fd475f7b5e6d78b4445f631e59af9f3e10e43268097e6df25d97657152030d2712b18c67ece931d990170d948f973c7b577a5374574d64d7f5e6951a0a4e5354dbb4c4c4e22c766227666897a11e8fb3072728fcaaca2644f8e7d51cd4c6569918f8f4321473e28cc34da775f0ee5fc7f078fba95f0d59d31bdc82cd6f655ab32a33e31c6638f5473dcdba86a999836755c0015dbea1db498b55e890b335ed6368dfd007d72a9ed66be7a99ebb5a0d0f65a9f11700002182616a55540889a1db0938903ef191ab3e853e70ca27a6c4c8b18c9d66b5ecdefff00a944e4492c29d6cc3688c5a03a32a4194670e3ccad8a121cf0ce73e58925fc6e672bce7385b27587ae36de87fa71dd04385b67723ba9da3817b685dbef03f2377be475baec466f9096c6526b99221a0a4e3ea8e8e110236385389c188424d25e757704d75d0ceabeaab56adb952757871939a6752cae90d72e112b3326157f5bce12f17330688c923ca8f31c957492707c89a39120534490cbc4adb79c4ab886acf480f4f9d31b02b7b3f5e6848e83ba532fc8d8f4f95fbc3672d3579f610ac063c28654b3a1890603abc94142e5970164b4b447c6a718632d58bf9e7c08b4db55879166922e3f4aa8534538f881b2f730b49cad13f8881273913bb90644b8b5ebce718156125567446fec4e87a40a64e7591625759c4e63ff004781462a596eb0fb6283b2c8a17223ba55211620a193dec0f0e87f916a8ffaa37ff063873b070e672f7f77fbcbbf7e7ff7d23fe3c595edd1faa0fdd8ff000ffa8f0710def7fa7af5f7bf5afd8ac6e287244b0571a2dea6dfe05581acf577de479b891ddf2436702e3884b8ec719e63ad78ce7184e1c7b0e1c39e68debb996ebdfceb5628dea8d1756b751cc45843427a8b14e51098147df1d44a3ac4c8cf589989faf426d2595ec296f434641a9680b16c09f98981448947fbc7cfa4c7df1e2b694cfece9ead94daf9accc7652f25d68790f174312830a049102a1e4614c7dabf7909432b5a15947ce8032a4ff009d847bf2d69d5feac698e9fea988d3fa42acc56aad199c925baa5aca969d957538c132f3722f65441c73f9c67f99c5e50d23fa6d2538f2ca8e1c91722e79cc397aaba7926fded4454982af5dc4b556067640fad35eb2d29658ed921f70c037c0910c33a1144aecdc0c7c79633373d1558eea2c60411b483a8976431a4662beee852b12109981991ea31d189e1c387227e1b783870e1c3c1e0e1c3870f0783870e1c3c1e0e1c3870f0783870e1c3c1e3fffd9ffd8ffe000104a46494600010101006000600000ffdb00430001010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101ffdb00430101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101ffc00011080025007103012200021101031101ffc4001c00000202030101000000000000000000000008090a0506070104ffc4003a1000010402020103010603030d0000000004020305060107000812091113141516213151911922230a4158173252546181929596b1d1d3d5ffc4001b01000203010101000000000000000000000005060708040201ffc4003911000203000102040303070d0000000000020301040506111200071314153141162122081724536292a13245465152546163647191d1f0ffda000c03010002110311003f00bfc7126df1848173b01d7b86d9533132f5dabd7f4895422a7198f8ad85246c9c6bc01cf421c1090369979e3eb7888b05b72880742c20264e1dc14f1c9eebbf76d27486a9b46cac413d6572bff6432cc3b2564243e4cdcd47c18ae1a72463140c78cfc8b649a4e057d481da730db4a7149c738dd7f653fda0eadda2de140aaaf626c3997e34241f990183bad08b667ebd251925914375e19b9e8b8b2db5ac560819d43a3672b5b387dc493c8f1639187129beb8e44788ce46197e9bfd52c455f56632fc3bd2f6be98df72ebfa52ff0071245df0a95c11c328c7d29c72df8aa7f071d31c72bbdeaec8d23aa5746afa7ea7ad2735409ddf0a9544476cb20e6065a6a8b33c3d52b4c5a5e6c8b2b30312d581f6b29536f4cb60b0992710a461285e16661e579b694a17efe484e139c631b1723f7b45b4a7eeda32aaf68dd931d15759e9da295290b57b90519762622ca2b82370d1ae847b1291a5e2c12b06e98e61d095f4021ad2df4a16a42f79b39fb66b3d5d02832d70318ecb5da91274da6ca432199ab026f6746968026f09c383b24b7551d4cca5927df7470056c27cd78af95d1f0ffa46bb5dc92d601e36bd7ad5b2a8698f24722bfd9fb2cbb6ee55665d6b0bb2cbccd3a21506ddd44d015aeadbaa60e61b617e06e7ad78e8d58d1cf6b9d7ed529c65b5b3ac80ad5eb3c6f394690ac34ad1582af5991688cdf5de24b010ef972787101f4ded17da9ebdf5e7ee2f6f775ab79ed072e360991acae4aca4f39155c3b23623a0953932d332123f0bcd146e32e3496c4c1b8119ca90ce339dc765f63f60ab654bea5d07ae46d8766a88639f77969390fa28383514d21e16250b4383a5d9079b5a70acb863794bbf20ed0cf38c14b1f8fcc3e63c67cb65fafb1a4ed34d9d88c3c40e3d95afb3a1c9344c2c3928c2c6a944b5ee1b2a54b57887d92e6bd3aefb367d1528ca3a789f1cd9e60d24e6d555534509d3d266adfa19b4b22989a54c769e8d9b2146b00d8b08ac33360a5b61ca4a60d8c119733c539fcf18fdb1c3db1fa63f6c723e6cddd9928cd1b1fb123e8c3337d46c22b5d586912c614a662e5a223cd91982192064b24bc3207606718ca938f8f24a99754e299cb8accee6edfcb6b7a56959682abc44e5af6a56212d66c39451a80a1632563a1dccba950d9faaca1c96986810d4ff00b61d4b2ffbe72e379c62afb3f948f945573f5f519c8de54b138f71ee4d7da18fad2619bca2e2f3f1d4baf34c6c3349b6dab559ca8545fa325d6e213027db364f937e60bede7d11c750d9d3d7d7c5a807a34204ae61d72b7a0c26c589505204011a6f49fb5b511fa3b5bdc3d5edf6c7e98fdb9e6129c7e494e3dff3f6c631efeff9ff007710c9fecbef52f616c6a86afd435ebc01ac19affde425c9a2403d264ac3264890c6696f25b79e68864f19a69943eea70c37975195ba94ab13b2fbb7291da6350ec0d594802c376db3b118d761522c32240f88f9965a916e58670c0f03a96b14f1c06d921c4b0ca853db7de6dacab094c9f89f9bfc2399f20bbc6316dea46b539ddf4874707672e96a8718d81e3fc85b85a77a9233b6978db06ba3a139d6ac1576b57263005dd0937bcbee4dc7326b6de957a3342c465f7cd3d4cebd66816d677c5b203528d5b2cb99a7a39f076aa45c42a1cb59f6cf74444c8578a71f92718ff763878a7f4c7ed8ff0067fe31fb638826a2ed96cf277211a4bb1daa60f52d888a19db16126e36d2d48c13b5e8b2081e41e947485b8c02cb5f447b9f5cb914a1ac04f2081db42da7d6e36365eb854755a631b0293989bc1cc45d2a5316b82cc75c24ca52d0347558dc1ff4d603885b6e2581225d2c8794dad2db6aca558c5a4096b23aad4c38eeedea00451ddda47dbd4627f176011f4f9f689174e913310793019e844233d3af492889e9d607afdf3f2ee288ebfd7311f398f1baf8a7fd1c7e3f9fe18fc7fbff00eff8f0f6c7e98fdb1c84ade9ea59d87d3ed493c9d39a8e4b171efc50ba89a1db1aff008b13b6aa558c4b1973fb06d8dd5666455053d128850d0dd69ec47902ae4d9fb4074e3c739713bc7de5a1f53fadbd8adb55ab0ebabe6ccd13508b9c27562ee11ae4c352962b242552ba3d9a1620e76c51001d2f3c137f2b820ee3a95612cafc969cf24e7c2b900d8c6ac1554f6ef5a0a79b15ad22c831e6da15e21ac49985708b3a35ea9b1c4003661c992834b22150ee674aeeb49a6b0cf54bacfaaa62884201ecea026224c9955763444224897d8711da6332f8fb63f4c7ed8e1cafcff121f51dff0009da63fe7b7fff00ddc38dff00367c8ff5f81f4fe90657d7a7fa8fda8fe3e38fed4667f6343e9fcdf6febd3fcbfda8fe3e24306a96d0afd9c1967f52dcadf231d3d777b6d4c3f73ad195edab479f666c78288aa57662cce3071b16b2ebf22c424d44d447891e124a3049125720d352199d7bafe9db76e1799a858cb153b4d06544c0ca6b148c753e2ee7b0a1045267a46cb5ac321911e1410a4815a3614558f1d663e3dc2e6183c68e8e709642d1ab5168987e633b0367c065f6d86feccac5b951310d7c0de1bf9190b00bf86dc77dbcdf5fc99f91cce55ed8f7f6e532bd507bb9ea1bd04eddec4d43ae77459eb9a9e74d5df75e3b2b090b2afcb03665fd7cc12f4d1d1ab7e4ce6661d25a39c5ab0a42b2df8b4d32a693855c2f86ea73bda8c1c7b19c8be755f6d51a565b594f1ad212d524955ec91be1664e85fa7112a538e4a3b3a4f66d6cd5c1a53a17576595c5ab49cd55034d72deb026706c540ae4a203bbbbf96403d3f1758b9c99a57509f149842759d1b316da12864666b1102fd3f87b782c57861192057919c61487c775b790bc6169730ac633c526dac58343dbad7175b02f1b06d779a7e33a8ed6b189bc59287588236398b9d59cc4a909155135e6cd1ad712fb997a56c8592d444aa260a8d8bc3d46ff00e383ea6bfe2424bfe99ac7ff0033936fe89dd85eee7a81ee3bc593b05b82eb31ab352d54b62266a0068bab16cddacae082fd9e24c4747254f21510d3ef1e22b0ea53f18b9ca50bca5789cf2cf23398f0cc2b9c876ae71d1a14a502615b46d32cb5965eaaea5575333922d649b60a47d41e8b033f90cf84593cef176afa33a927466c3fd49196d650a8054b261930c6c9c88c08cc7776cfe2911f997dd652d0f39392a75e454d8ee573a047fdd8cd46d97d874c4588c9a3059472e5148f684aeb9211112b6a09d0cd7629bcb27494ac53643ecc6b786979eb25e29b459ced519799f8a83b147ed8b14ccf6250b60539c856145ac275869e5a5e2daf2c13861b1d2e2d4e3a84a51953c8f3722a1add151927645379d8d64cba32c6fa1b6da15351cdf9ad0bf9db17210fe04a7c3c50ef9e7d92b5e3c73e5ef8d1ef9d5dd1fb2ad68ba5ba9221f60ce58c9a5305180372ff004c96db63ed81c37d966414869a6d8f91f4e5d5308432b5a9b42129c77e67714e65abafc1796704fb39677b85dfdf30cce576b428e4daadc8f0ac6336d8dbcca5a36557b31869b095c55ecb754ef54f7158dc0d8bd784eef1ca3439460f29f8c272b9255ca12bb848a96afa1d8faa9d10af35eed9a893ad7405aa61faf04878d57fa4e1592e527dd1b0a177fb9a78a8081958283268fbdb61e63a64308439d44054e562c2955b0112536b14d946d3f4843ab43cfa1597b2da5394297c169293768bda5e6e5995b62cd5c34e69aab8ef67dd68ab6a6821666ee7b68ce549c0854ca5a77e442bcb391c8c2d09ce309ccc317a775c9b30dcebb5b15b398a41bae45c0ae3c206153e43cb05c40400ee36188dba95651f330ca1f4b784a12e61294e318888d05aa609ba33515576836f5c26c1f7410d9a77b4639681dc16688cf9119faa28b61d711824af95e67cf39656dabdb3ccedc87f266f30b9472a6f21dee4bc6743e217b2ed6e8a576e82f6273e8705e3b2734559ed4d54062e7f35d2a79e0f6aabecdfc75cbcc14db68b7b27ce9e238784bc8cac5d9a9ed2b5d465c9957b679d16ad727d8818b2cb6b6586169dbe354ac5b25831b9d5344e1624c5d76a83d75d9949a9573b31b86db61888ecd976b5ce59a19f3c5c499a045fcd88c0818e53b82c95bab2f2202d36dabe452b09c2bc70a561047e993f7099ea6eb726f2ad472d6a9adc3bd9bb4b8e3031150664b2cfd84733930a0196df347acb79030a259ce55208cb7fd45653c97b8fe9975be3a499954eb700c259230561b939096920dc7f0bf93cc808c35d1494f9ff00329a21a71a5fe295b6a4e729cfdfb5ba8da0b75d800b3ec6a422725e32145af47badcb4c46b024406f9440e1b01c69a288da10e98fe7c92ce14a4e508ce7286d184da1e51f953ceb8ee9718d1e77638bac38471ee579189538cddd4d23d5d6e73bd4f7b92723d8b5a3978e358c8e82eb51cdad5ed084dbbd62c5d61ca04611e6073be2faf4f6e9f1656d91726d6c1d0d37ed56a5486850e2f93632b1b1f3ebd3bda1eb0c0da375ab8e6a267dbd64a6b00faa455fbee25b8eebc6b9f5027e5b650fd889f474f9e84037014515213b542f615a61f5d05514bed4a48c58ad147dc4729f40eacb88f918561e6d4b2c75eb765453e2b727a1e7556364a14d1ba81d6b9bed26dc1002c62428497ad6928391ac4a1cb196e2042333f153f636b0e250e6433c525b565a310e66c12ff46fab24ea4d8ba41fd475f7b5e6d78b4445f631e59af9f3e10e43268097e6df25d97657152030d2712b18c67ece931d990170d948f973c7b577a5374574d64d7f5e6951a0a4e5354dbb4c4c4e22c766227666897a11e8fb3072728fcaaca2644f8e7d51cd4c6569918f8f4321473e28cc34da775f0ee5fc7f078fba95f0d59d31bdc82cd6f655ab32a33e31c6638f5473dcdba86a999836755c0015dbea1db498b55e890b335ed6368dfd007d72a9ed66be7a99ebb5a0d0f65a9f11700002182616a55540889a1db0938903ef191ab3e853e70ca27a6c4c8b18c9d66b5ecdefff00a944e4492c29d6cc3688c5a03a32a4194670e3ccad8a121cf0ce73e58925fc6e672bce7385b27587ae36de87fa71dd04385b67723ba9da3817b685dbef03f2377be475baec466f9096c6526b99221a0a4e3ea8e8e110236385389c188424d25e757704d75d0ceabeaab56adb952757871939a6752cae90d72e112b3326157f5bce12f17330688c923ca8f31c957492707c89a39120534490cbc4adb79c4ab886acf480f4f9d31b02b7b3f5e6848e83ba532fc8d8f4f95fbc3672d3579f610ac063c28654b3a1890603abc94142e5970164b4b447c6a718632d58bf9e7c08b4db55879166922e3f4aa8534538f881b2f730b49cad13f8881273913bb90644b8b5ebce718156125567446fec4e87a40a64e7591625759c4e63ff004781462a596eb0fb6283b2c8a17223ba55211620a193dec0f0e87f916a8ffaa37ff063873b070e672f7f77fbcbbf7e7ff7d23fe3c595edd1faa0fdd8ff000ffa8f0710def7fa7af5f7bf5afd8ac6e287244b0571a2dea6dfe05581acf577de479b891ddf2436702e3884b8ec719e63ad78ce7184e1c7b0e1c39e68debb996ebdfceb5628dea8d1756b751cc45843427a8b14e51098147df1d44a3ac4c8cf589989faf426d2595ec296f434641a9680b16c09f98981448947fbc7cfa4c7df1e2b694cfece9ead94daf9accc7652f25d68790f174312830a049102a1e4614c7dabf7909432b5a15947ce8032a4ff009d847bf2d69d5feac698e9fea988d3fa42acc56aad199c925baa5aca969d957538c132f3722f65441c73f9c67f99c5e50d23fa6d2538f2ca8e1c91722e79cc397aaba7926fded4454982af5dc4b556067640fad35eb2d29658ed921f70c037c0910c33a1144aecdc0c7c79633373d1558eea2c60411b483a8976431a4662beee852b12109981991ea31d189e1c387227e1b783870e1c3c1e0e1c3870f0783870e1c3c1e0e1c3870f0783870e1c3c1e3fffd9', '{4617,4617}', '{33072}', '"Report"', NULL, '{LogoCMDBuild1.jpg,LogoCMDBuild2.jpg}');









INSERT INTO "Role" VALUES (677, '"Role"', 'Helpdesk', 'Helpdesk', 'A', 'admin', '2013-05-09 12:57:49.186365', NULL, false, '"Asset"', 'helpdesk@cmdbuild.org', '{bulkupdate,importcsv,exportcsv}', NULL, NULL, false, false, false, false, false, false, true);
INSERT INTO "Role" VALUES (940, '"Role"', 'ChangeManager', 'Change manager', 'A', 'system', '2013-05-09 12:57:49.186365', NULL, false, '-', NULL, NULL, NULL, NULL, false, false, false, false, false, false, true);
INSERT INTO "Role" VALUES (941, '"Role"', 'Specialist', 'Specialist', 'A', 'system', '2013-05-09 12:57:49.186365', NULL, false, '-', NULL, NULL, NULL, NULL, false, false, false, false, false, false, true);
INSERT INTO "Role" VALUES (942, '"Role"', 'Services', 'Services', 'A', 'system', '2013-05-09 12:57:49.186365', NULL, true, '-', NULL, NULL, NULL, NULL, false, false, false, false, false, false, true);
INSERT INTO "Role" VALUES (14, '"Role"', 'SuperUser', 'SuperUser', 'A', 'system', '2013-05-09 12:57:49.186365', NULL, true, NULL, NULL, '{}', '{}', '{}', false, false, false, false, false, false, true);






INSERT INTO "Room" VALUES (104, '"Room"', 'DC01001', 'Data Center - Floor 1 - Room 001', 'A', 'admin', '2011-07-24 23:50:09.333', NULL, 79, 27, 28.00, 110);
INSERT INTO "Room" VALUES (200, '"Room"', 'DC01002', 'Data Center - Floor 1 - Room 002
', 'A', 'admin', '2011-07-24 23:51:13.304', NULL, 79, 157, 62.00, 108);
INSERT INTO "Room" VALUES (206, '"Room"', 'B101001', 'Office Building A - Floor 1 - Room 001', 'A', 'admin', '2011-07-24 23:56:14.609', NULL, 83, 27, 18.00, 110);
INSERT INTO "Room" VALUES (212, '"Room"', 'B101002', 'Office Building A - Floor 1 - Room 002', 'A', 'admin', '2011-07-24 23:56:56.466', NULL, 83, 27, 18.00, 110);
INSERT INTO "Room" VALUES (218, '"Room"', 'B101003', 'Office Building A - Floor 1 - Room 003', 'A', 'admin', '2011-07-24 23:57:24.774', NULL, 83, 27, 18.00, 110);
INSERT INTO "Room" VALUES (224, '"Room"', 'B102001', 'Office Building A - Floor 2 - Room 001', 'A', 'admin', '2011-07-24 23:57:56.042', NULL, 87, 155, 48.00, 110);
INSERT INTO "Room" VALUES (230, '"Room"', 'B102002', 'Office Building A - Floor 2 - Room 002', 'A', 'admin', '2011-07-24 23:58:29.941', NULL, 87, 156, 48.00, 110);
INSERT INTO "Room" VALUES (236, '"Room"', 'B103001', 'Office Building A - Floor 3 - Room 001', 'A', 'admin', '2011-07-24 23:59:12.074', NULL, 92, 154, 128.00, 112);
INSERT INTO "Room" VALUES (242, '"Room"', 'B201001', 'Office Building B - Floor 1 - Room 001', 'A', 'admin', '2011-07-24 23:59:40.137', NULL, 96, 27, 18.00, 108);
INSERT INTO "Room" VALUES (248, '"Room"', 'B201002', 'Office Building B - Floor 1 - Room 002', 'A', 'admin', '2011-07-25 00:00:13.196', NULL, 96, 27, 18.00, 108);
INSERT INTO "Room" VALUES (260, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'A', 'admin', '2011-09-02 11:53:26.9', 'The room is temporary used by Administration, pending the conclusion of works in the building C.<br><br>Scheduled dates:<br><br>&nbsp;&nbsp;&nbsp; * temporary use start date: 15/10/2001<br>&nbsp;&nbsp;&nbsp; * temporary use end date: 15/05/2012<br>', 100, 27, 26.00, 108);
INSERT INTO "Room" VALUES (254, '"Room"', 'B201003', 'Office Building B - Floor 1 - Room 003', 'A', 'admin', '2011-09-02 11:56:58.957', NULL, 96, 156, 18.00, 110);
INSERT INTO "Room" VALUES (266, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'A', 'admin', '2011-08-30 16:22:46.448', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the building C</span><span class="hps">.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112);
INSERT INTO "Room" VALUES (272, '"Room"', 'B202003', 'Office Building B - Floor 2 - Room 003', 'A', 'admin', '2011-09-02 11:54:54.974', '​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4<span style="color: rgb(255, 0, 0);">gh ouregou</span>regireh goreh goreg oeufg orehg oureòg yu5y uy5 u 5yu 5yu yj yu5 5yu 5yu u5yu 5 u<br><ul><li>hore goireò gierhg ierò girehg iregh iregh ireg iregie ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh o</li><li>uregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg ir</li><li>egie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oure</li><li>òghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or</li></ul>4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ', 100, 27, 24.00, 108);



INSERT INTO "Room_history" VALUES (196, '"Room"', 'DC01001', 'Data Center - Floor 1 - Room 001', 'U', 'admin', '2011-07-24 18:45:44.718', NULL, 79, NULL, 28.00, NULL, 104, '2011-07-24 23:50:09.333');
INSERT INTO "Room_history" VALUES (711, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-07-25 00:01:52.818', NULL, 100, 27, 24.00, 112, 266, '2011-08-29 12:20:03.608');
INSERT INTO "Room_history" VALUES (712, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-29 12:20:03.608', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of the work</span> <span class="hps">in the building</span> <span class="hps">C.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-29 12:20:42.359');
INSERT INTO "Room_history" VALUES (729, '"Room"', 'B202003', 'Office Building B - Floor 2 - Room 003', 'U', 'admin', '2011-07-25 00:02:19.16', NULL, 100, 27, 24.00, 112, 272, '2011-08-30 16:20:50.591');
INSERT INTO "Room_history" VALUES (730, '"Room"', 'B202003', 'Office Building B - Floor 2 - Room 003', 'U', 'admin', '2011-08-30 16:20:50.591', '​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ', 100, 27, 24.00, 112, 272, '2011-08-30 16:21:11.789');
INSERT INTO "Room_history" VALUES (731, '"Room"', 'B202003', 'Office Building B - Floor 2 - Room 003', 'U', 'admin', '2011-08-30 16:21:11.789', '​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4<span style="color: rgb(255, 0, 0);">gh ouregou</span>regireh goreh goreg oeufg orehg oureòg<br><ul><li>hore goireò gierhg ierò girehg iregh iregh ireg iregie ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh o</li><li>uregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg ir</li><li>egie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oure</li><li>òghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or</li></ul>4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ', 100, 27, 24.00, 112, 272, '2011-08-30 16:21:22.461');
INSERT INTO "Room_history" VALUES (732, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-29 12:20:42.359', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the building</span> <span class="hps">C.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:21:38.937');
INSERT INTO "Room_history" VALUES (733, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-30 16:21:38.937', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the buildingb </span><span class="hps">C.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:21:48.929');
INSERT INTO "Room_history" VALUES (745, '"Room"', 'B201003', 'Office Building B - Floor 1 - Room 003', 'U', 'admin', '2011-08-30 16:36:35.379', NULL, 96, 156, 18.00, 108, 254, '2011-09-02 11:56:58.957');
INSERT INTO "Room_history" VALUES (734, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-30 16:21:48.929', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the buildingb </span><span class="hps">ruruhf3 ir3hfg 3ihf ir3hf i3h .<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:22:03.09');
INSERT INTO "Room_history" VALUES (735, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-30 16:22:03.09', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the building C</span><span class="hps">.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:22:22.253');
INSERT INTO "Room_history" VALUES (736, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-30 16:22:22.253', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the buildingqC</span><span class="hps">.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:22:36.904');
INSERT INTO "Room_history" VALUES (737, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-30 16:22:36.904', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the_building_C</span><span class="hps">.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:22:46.448');
INSERT INTO "Room_history" VALUES (738, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'U', 'admin', '2011-07-25 00:01:29.684', NULL, 100, 27, 24.00, 112, 260, '2011-08-30 16:23:31.002');
INSERT INTO "Room_history" VALUES (739, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'U', 'admin', '2011-08-30 16:23:31.002', 'The room is temporary used by Administration, pending the conclusion of works in the building C.<br><br>Scheduled dates:<br><br>&nbsp;&nbsp;&nbsp; * temporary use start date: 15/10/2001<br>&nbsp;&nbsp;&nbsp; * temporary use end date: 15/05/2012<br>', 100, 27, 24.00, 112, 260, '2011-08-30 16:23:44.308');
INSERT INTO "Room_history" VALUES (740, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'U', 'admin', '2011-08-30 16:23:44.308', 'The room is <span style="color: rgb(255, 0, 0);">temporary </span>used by Administration, pending the conclusion of works in the building C.<br><br>Scheduled dates:<br><br>&nbsp;&nbsp;&nbsp; * temporary use start date: 15/10/2001<br>&nbsp;&nbsp;&nbsp; * temporary use end date: 15/05/2012<br>', 100, 27, 24.00, 112, 260, '2011-08-30 16:24:10.851');
INSERT INTO "Room_history" VALUES (741, '"Room"', 'B201003', 'Office Building B - Floor 1 - Room 003', 'U', 'admin', '2011-07-25 00:00:42.222', NULL, 96, 27, 18.00, 108, 254, '2011-08-30 16:36:35.379');
INSERT INTO "Room_history" VALUES (742, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'U', 'admin', '2011-08-30 16:24:10.851', 'The room is temporary used by Administration, pending the conclusion of works in the building C.<br><br>Scheduled dates:<br><br>&nbsp;&nbsp;&nbsp; * temporary use start date: 15/10/2001<br>&nbsp;&nbsp;&nbsp; * temporary use end date: 15/05/2012<br>', 100, 27, 24.00, 112, 260, '2011-09-02 11:53:03.347');
INSERT INTO "Room_history" VALUES (743, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'U', 'admin', '2011-09-02 11:53:03.347', 'The room is temporary used by Administration, pending the conclusion of works in the building C.<br><br>Scheduled dates:<br><br>&nbsp;&nbsp;&nbsp; * temporary use start date: 15/10/2001<br>&nbsp;&nbsp;&nbsp; * temporary use end date: 15/05/2012<br>', 100, 27, 26.00, 112, 260, '2011-09-02 11:53:26.9');
INSERT INTO "Room_history" VALUES (744, '"Room"', 'B202003', 'Office Building B - Floor 2 - Room 003', 'U', 'admin', '2011-08-30 16:21:22.461', '​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4<span style="color: rgb(255, 0, 0);">gh ouregou</span>regireh goreh goreg oeufg orehg oureòg yu5y uy5 u 5yu 5yu yj yu5 5yu 5yu u5yu 5 u<br><ul><li>hore goireò gierhg ierò girehg iregh iregh ireg iregie ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh o</li><li>uregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg ir</li><li>egie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oure</li><li>òghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or</li></ul>4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ', 100, 27, 24.00, 112, 272, '2011-09-02 11:54:54.974');









INSERT INTO "Supplier" VALUES (706, '"Supplier"', 'SUP001', 'Tecnoteca Srl', 'A', 'admin', '2011-08-23 23:29:19.436', 'This supplier is very <font color="#ff0000">reliable</font>.<br><span id="result_box" class="short_text" lang="en"><span class="hps">Delivery dates</span> <span class="hps">are always</span> <span class="hps">fulfilled.<br></span></span>Rating:<br><ul><li>quality: good</li><li>prices: good</li></ul><span id="result_box" class="short_text" lang="en"><span class="hps"></span></span>', 28, 'Via L''Aquila 1/B', '33010', 'Tavagnacco (UD)', '+39 0432 689094', 'tecnoteca@tecnoteca.com', 'http://www.tecnoteca.com', 65);
INSERT INTO "Supplier" VALUES (714, '"Supplier"', 'SUP02', 'HP', 'A', 'admin', '2011-08-29 12:50:04.459', NULL, 28, NULL, NULL, NULL, NULL, 'info@hp.com', 'www.hp.com', 69);
INSERT INTO "Supplier" VALUES (721, '"Supplier"', 'SUP003', 'Dell', 'A', 'admin', '2011-08-29 13:21:30.725', NULL, 28, NULL, NULL, NULL, NULL, 'info@dell.com', 'www.dell.com', 69);
INSERT INTO "Supplier" VALUES (723, '"Supplier"', 'SUP004', 'Misco', 'A', 'admin', '2011-08-29 13:23:10.823', NULL, 158, NULL, NULL, NULL, NULL, NULL, NULL, 25);









INSERT INTO "Supplier_history" VALUES (707, '"Supplier"', 'SUP001', 'Tecnoteca Srl', 'U', 'admin', '2011-08-23 23:16:41.642', NULL, 28, 'Via L''Aquila 1/B', '33010', 'Tavagnacco (UD)', '+39 0432 689094', 'tecnoteca@tecnoteca.com', 'http://www.tecnoteca.com', NULL, 706, '2011-08-23 23:18:50.004');
INSERT INTO "Supplier_history" VALUES (708, '"Supplier"', 'SUP001', 'Tecnoteca Srl', 'U', 'admin', '2011-08-23 23:18:50.004', NULL, 28, 'Via L''Aquila 1/B', '33010', 'Tavagnacco (UD)', '+39 0432 689094', 'tecnoteca@tecnoteca.com', 'http://www.tecnoteca.com', 65, 706, '2011-08-23 23:23:33.472');
INSERT INTO "Supplier_history" VALUES (709, '"Supplier"', 'SUP001', 'Tecnoteca Srl', 'U', 'admin', '2011-08-23 23:23:33.472', '​This supplier is very reliable.<br><span id="result_box" class="short_text" lang="en"><span class="hps">Delivery dates</span> <span class="hps">are always</span> <span class="hps">fulfilled.</span></span><br>', 28, 'Via L''Aquila 1/B', '33010', 'Tavagnacco (UD)', '+39 0432 689094', 'tecnoteca@tecnoteca.com', 'http://www.tecnoteca.com', 65, 706, '2011-08-23 23:29:19.436');
INSERT INTO "Supplier_history" VALUES (715, '"Supplier"', 'SUP02', 'Dell ', 'U', 'admin', '2011-08-29 12:48:58.926', NULL, 28, NULL, NULL, NULL, NULL, 'info@dell.com', 'www.dell.com', 69, 714, '2011-08-29 12:50:04.459');









INSERT INTO "User" VALUES (13, '"User"', NULL, 'Administrator', 'A', 'system', '2013-05-09 12:57:49.186365', NULL, 'admin', 'DQdKW32Mlms=', NULL, true, NULL, NULL);
INSERT INTO "User" VALUES (943, '"User"', NULL, 'workflow', 'A', 'admin', '2013-05-09 12:57:49.186365', NULL, 'workflow', 'sLPdlW/0y4msBompb4oRVw==', NULL, true, NULL, NULL);
INSERT INTO "User" VALUES (678, '"User"', NULL, 'Jones Patricia', 'A', 'admin', '2013-05-09 12:57:49.186365', NULL, 'pjones', 'Tms67HRN+qusMUAsM6xIPA==', 'patricia.jones@example.com', true, NULL, NULL);
INSERT INTO "User" VALUES (679, '"User"', NULL, 'Davis Michael', 'A', 'admin', '2013-05-09 12:57:49.186365', NULL, 'mdavis', 'Nlg70IVc7/U=', 'michael.davis@example.com', true, NULL, NULL);


















INSERT INTO "_Dashboards" VALUES (831, 'system', '2012-08-23 22:04:26.088', '{"name":"Item situation","description":"Item situation","charts":{"6172e925-4aa7-4734-a112-2dd9e33863a9":{"name":"Total number of item","description":"Total number of item","dataSourceName":"cmf_count_active_cards","type":"gauge","singleSeriesField":"Count","fgcolor":"#99CC00","bgcolor":"#C0C0C0","active":true,"autoLoad":true,"legend":false,"height":0,"maximum":50,"minimum":0,"steps":5,"dataSourceParameters":[{"name":"ClassName","type":"STRING","fieldType":"classes","defaultValue":"Asset","required":false}]},"98b4927d-6a8e-49c5-8051-b64109aeee8b":{"name":"Number of items by item brand","description":"Number of items by item brand","dataSourceName":"cmf_active_asset_for_brand","type":"pie","singleSeriesField":"Number","labelField":"Brand","active":true,"autoLoad":true,"legend":true,"height":0,"maximum":0,"minimum":0,"steps":0},"3b6bb717-b9e4-402f-b188-1d8e81135adf":{"name":"Number of items by item type","description":"Number of items by item type","dataSourceName":"cmf_active_cards_for_class","type":"bar","categoryAxisField":"Class","categoryAxisLabel":"Asset type","valueAxisLabel":"Number","chartOrientation":"vertical","active":true,"autoLoad":true,"legend":true,"height":0,"maximum":0,"minimum":0,"steps":0,"dataSourceParameters":[{"name":"ClassName","type":"STRING","fieldType":"classes","defaultValue":"Asset","required":false}],"valueAxisFields":["Number"]}},"columns":[{"width":0.3,"charts":["6172e925-4aa7-4734-a112-2dd9e33863a9"]},{"width":0.36721992,"charts":["98b4927d-6a8e-49c5-8051-b64109aeee8b"]},{"width":0.3327801,"charts":["3b6bb717-b9e4-402f-b188-1d8e81135adf"]}],"groups":["SuperUser","Helpdesk"]}', '"_Dashboards"');
INSERT INTO "_Dashboards" VALUES (946, 'system', '2012-08-24 10:25:56.862', '{"name":"RfC situation","description":"RfC situation","charts":{"07706c7e-b4cc-4873-b112-9f2a6a2b0f2f":{"name":"Open RfC by status","description":"Open RfC by status","dataSourceName":"cmf_open_rfc_for_status","type":"pie","singleSeriesField":"Number","labelField":"Status","active":true,"autoLoad":true,"legend":false,"height":0,"maximum":0,"minimum":0,"steps":0}},"groups":["SuperUser","Helpdesk","ChangeManager","Specialist"]}', '"_Dashboards"');





















INSERT INTO "_Widget" VALUES (1348, '"_Widget"', 'PC', '.Ping', 'A', NULL, '2013-05-09 12:57:49.745726', NULL, '{"id":"4ea70051-9bab-436a-a5ef-5cb002a10912","label":"Ping","active":true,"alwaysenabled":true,"address":"{client:IPAddress}","count":3,"templates":{},"type":".Ping"}');
INSERT INTO "_Widget" VALUES (1350, '"_Widget"', 'PC', '.Calendar', 'A', NULL, '2014-06-12 16:52:02.065634', NULL, '{"id":"06dc6599-2ad5-4d03-9262-d2dafd4277b6","label":"Warranty calendar","active":true,"alwaysenabled":true,"eventClass":"PC","startDate":"AcceptanceDate","endDate":null,"eventTitle":"SerialNumber","filter":"","defaultDate":null,"type":".Calendar"}');



INSERT INTO "_Widget_history" VALUES (1372, '"_Widget"', 'PC', '.Calendar', 'U', NULL, '2013-05-09 12:57:49.745726', NULL, '{"id":"06dc6599-2ad5-4d03-9262-d2dafd4277b6","label":"Warranty calendar","active":true,"alwaysenabled":true,"targetClass":"PC","startDate":"AcceptanceDate","endDate":null,"eventTitle":"SerialNumber","filter":"","defaultDate":null,"type":".Calendar"}', 1350, '2014-06-12 16:52:02.065634');



SELECT pg_catalog.setval('class_seq', 1502, true);



ALTER TABLE ONLY "Activity"
    ADD CONSTRAINT "Activity_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Asset"
    ADD CONSTRAINT "Asset_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Building_history"
    ADD CONSTRAINT "Building_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Building"
    ADD CONSTRAINT "Building_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Class"
    ADD CONSTRAINT "Class_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Computer"
    ADD CONSTRAINT "Computer_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Email_history"
    ADD CONSTRAINT "Email_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Email"
    ADD CONSTRAINT "Email_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Employee_history"
    ADD CONSTRAINT "Employee_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Employee"
    ADD CONSTRAINT "Employee_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Floor_history"
    ADD CONSTRAINT "Floor_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Floor"
    ADD CONSTRAINT "Floor_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Grant"
    ADD CONSTRAINT "Grant_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Invoice_history"
    ADD CONSTRAINT "Invoice_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Invoice"
    ADD CONSTRAINT "Invoice_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "License_history"
    ADD CONSTRAINT "License_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "License"
    ADD CONSTRAINT "License_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "LookUp"
    ADD CONSTRAINT "LookUp_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Map_AccountTemplate_history"
    ADD CONSTRAINT "Map_AccountTemplate_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_AccountTemplate"
    ADD CONSTRAINT "Map_AccountTemplate_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_AssetAssignee_history"
    ADD CONSTRAINT "Map_AssetAssignee_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_AssetAssignee"
    ADD CONSTRAINT "Map_AssetAssignee_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_AssetReference_history"
    ADD CONSTRAINT "Map_AssetReference_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_AssetReference"
    ADD CONSTRAINT "Map_AssetReference_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_BuildingFloor_history"
    ADD CONSTRAINT "Map_BuildingFloor_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_BuildingFloor"
    ADD CONSTRAINT "Map_BuildingFloor_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_ClassEmail_history"
    ADD CONSTRAINT "Map_ClassEmail_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_ClassEmail"
    ADD CONSTRAINT "Map_ClassEmail_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_ClassMetadata_history"
    ADD CONSTRAINT "Map_ClassMetadata_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_ClassMetadata"
    ADD CONSTRAINT "Map_ClassMetadata_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_FilterRole_history"
    ADD CONSTRAINT "Map_FilterRole_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_FilterRole"
    ADD CONSTRAINT "Map_FilterRole_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_FloorRoom_history"
    ADD CONSTRAINT "Map_FloorRoom_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_FloorRoom"
    ADD CONSTRAINT "Map_FloorRoom_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_Members_history"
    ADD CONSTRAINT "Map_Members_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_Members"
    ADD CONSTRAINT "Map_Members_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_NetworkDeviceConnection_history"
    ADD CONSTRAINT "Map_NetworkDeviceConnection_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_NetworkDeviceConnection"
    ADD CONSTRAINT "Map_NetworkDeviceConnection_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_OfficeRoom_history"
    ADD CONSTRAINT "Map_OfficeRoom_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_OfficeRoom"
    ADD CONSTRAINT "Map_OfficeRoom_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RFCChangeManager_history"
    ADD CONSTRAINT "Map_RFCChangeManager_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RFCChangeManager"
    ADD CONSTRAINT "Map_RFCChangeManager_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RFCExecutor_history"
    ADD CONSTRAINT "Map_RFCExecutor_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RFCExecutor"
    ADD CONSTRAINT "Map_RFCExecutor_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RFCRequester_history"
    ADD CONSTRAINT "Map_RFCRequester_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RFCRequester"
    ADD CONSTRAINT "Map_RFCRequester_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RoomAsset_history"
    ADD CONSTRAINT "Map_RoomAsset_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RoomAsset"
    ADD CONSTRAINT "Map_RoomAsset_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RoomNetworkPoint_history"
    ADD CONSTRAINT "Map_RoomNetworkPoint_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RoomNetworkPoint"
    ADD CONSTRAINT "Map_RoomNetworkPoint_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RoomWorkplace_history"
    ADD CONSTRAINT "Map_RoomWorkplace_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RoomWorkplace"
    ADD CONSTRAINT "Map_RoomWorkplace_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_Supervisor_history"
    ADD CONSTRAINT "Map_Supervisor_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_Supervisor"
    ADD CONSTRAINT "Map_Supervisor_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_SupplierAsset_history"
    ADD CONSTRAINT "Map_SupplierAsset_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_SupplierAsset"
    ADD CONSTRAINT "Map_SupplierAsset_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_SupplierContact_history"
    ADD CONSTRAINT "Map_SupplierContact_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_SupplierContact"
    ADD CONSTRAINT "Map_SupplierContact_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_SupplierInvoice_history"
    ADD CONSTRAINT "Map_SupplierInvoice_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_SupplierInvoice"
    ADD CONSTRAINT "Map_SupplierInvoice_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_UserRole_history"
    ADD CONSTRAINT "Map_UserRole_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_UserRole"
    ADD CONSTRAINT "Map_UserRole_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_WorkplaceComposition_history"
    ADD CONSTRAINT "Map_WorkplaceComposition_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_WorkplaceComposition"
    ADD CONSTRAINT "Map_WorkplaceComposition_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map"
    ADD CONSTRAINT "Map_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2");



ALTER TABLE ONLY "Menu_history"
    ADD CONSTRAINT "Menu_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Menu"
    ADD CONSTRAINT "Menu_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Metadata_history"
    ADD CONSTRAINT "Metadata_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Metadata"
    ADD CONSTRAINT "Metadata_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Monitor_history"
    ADD CONSTRAINT "Monitor_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Monitor"
    ADD CONSTRAINT "Monitor_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "NetworkDevice_history"
    ADD CONSTRAINT "NetworkDevice_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "NetworkDevice"
    ADD CONSTRAINT "NetworkDevice_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "NetworkPoint_history"
    ADD CONSTRAINT "NetworkPoint_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "NetworkPoint"
    ADD CONSTRAINT "NetworkPoint_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Notebook_history"
    ADD CONSTRAINT "Notebook_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Notebook"
    ADD CONSTRAINT "Notebook_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Office_history"
    ADD CONSTRAINT "Office_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Office"
    ADD CONSTRAINT "Office_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "PC_history"
    ADD CONSTRAINT "PC_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "PC"
    ADD CONSTRAINT "PC_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Patch_history"
    ADD CONSTRAINT "Patch_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Patch"
    ADD CONSTRAINT "Patch_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Printer_history"
    ADD CONSTRAINT "Printer_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Printer"
    ADD CONSTRAINT "Printer_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Rack_history"
    ADD CONSTRAINT "Rack_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Rack"
    ADD CONSTRAINT "Rack_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Report"
    ADD CONSTRAINT "Report_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "RequestForChange_history"
    ADD CONSTRAINT "RequestForChange_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "RequestForChange"
    ADD CONSTRAINT "RequestForChange_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Role_history"
    ADD CONSTRAINT "Role_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Role"
    ADD CONSTRAINT "Role_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Room_history"
    ADD CONSTRAINT "Room_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Room"
    ADD CONSTRAINT "Room_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Task_history"
    ADD CONSTRAINT "Scheduler_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Task"
    ADD CONSTRAINT "Scheduler_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Server_history"
    ADD CONSTRAINT "Server_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Server"
    ADD CONSTRAINT "Server_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "SupplierContact_history"
    ADD CONSTRAINT "SupplierContact_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "SupplierContact"
    ADD CONSTRAINT "SupplierContact_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Supplier_history"
    ADD CONSTRAINT "Supplier_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Supplier"
    ADD CONSTRAINT "Supplier_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "UPS_history"
    ADD CONSTRAINT "UPS_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "UPS"
    ADD CONSTRAINT "UPS_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "User_history"
    ADD CONSTRAINT "User_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "User"
    ADD CONSTRAINT "User_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Workplace_history"
    ADD CONSTRAINT "Workplace_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Workplace"
    ADD CONSTRAINT "Workplace_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_BimLayer"
    ADD CONSTRAINT "_BimLayer_ClassName_key" UNIQUE ("ClassName");



ALTER TABLE ONLY "_BimLayer"
    ADD CONSTRAINT "_BimLayer_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_BimProject"
    ADD CONSTRAINT "_BimProject_ProjectId_key" UNIQUE ("ProjectId");



ALTER TABLE ONLY "_BimProject"
    ADD CONSTRAINT "_BimProject_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_CustomPage_history"
    ADD CONSTRAINT "_CustomPage_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_CustomPage"
    ADD CONSTRAINT "_CustomPage_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Dashboards"
    ADD CONSTRAINT "_Dashboards_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_DomainTreeNavigation"
    ADD CONSTRAINT "_DomainTreeNavigation_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_EmailAccount_history"
    ADD CONSTRAINT "_EmailAccount_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_EmailAccount"
    ADD CONSTRAINT "_EmailAccount_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_EmailTemplate_history"
    ADD CONSTRAINT "_EmailTemplate_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_EmailTemplate"
    ADD CONSTRAINT "_EmailTemplate_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Filter"
    ADD CONSTRAINT "_Filter_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Icon"
    ADD CONSTRAINT "_Icon_Element_key" UNIQUE ("Element");



ALTER TABLE ONLY "_Icon"
    ADD CONSTRAINT "_Icon_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Layer"
    ADD CONSTRAINT "_Layer_FullName_key" UNIQUE ("FullName");



ALTER TABLE ONLY "_Layer"
    ADD CONSTRAINT "_Layer_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_TaskParameter_history"
    ADD CONSTRAINT "_SchedulerJobParameter_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_TaskParameter"
    ADD CONSTRAINT "_SchedulerJobParameter_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_TaskRuntime"
    ADD CONSTRAINT "_TaskRuntime_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Templates"
    ADD CONSTRAINT "_Templates_Name_key" UNIQUE ("Name");



ALTER TABLE ONLY "_Templates"
    ADD CONSTRAINT "_Templates_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Translation"
    ADD CONSTRAINT "_Translation_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_View"
    ADD CONSTRAINT "_View_Name_key" UNIQUE ("Name");



ALTER TABLE ONLY "_View"
    ADD CONSTRAINT "_View_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Widget_history"
    ADD CONSTRAINT "_Widget_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Widget"
    ADD CONSTRAINT "_Widget_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Filter"
    ADD CONSTRAINT filter_name_table_unique UNIQUE ("Code", "UserId", "ClassId");



CREATE UNIQUE INDEX "Report_unique_code" ON "Report" USING btree ((CASE WHEN ((("Code")::text = ''::text) OR (("Status")::text <> 'A'::text)) THEN NULL::text ELSE ("Code")::text END));



CREATE UNIQUE INDEX "_Unique_User_Username" ON "User" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::character varying ELSE "Username" END));



CREATE UNIQUE INDEX "_Unique__EmailAccount_Code" ON "_EmailAccount" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::character varying ELSE "Code" END));



CREATE UNIQUE INDEX "_Unique__EmailTemplate_Code" ON "_EmailTemplate" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::character varying ELSE "Code" END));



CREATE INDEX idx_activity_code ON "Activity" USING btree ("Code");



CREATE INDEX idx_activity_defaultorder ON "Activity" USING btree ("Description", "Id");



CREATE INDEX idx_activity_description ON "Activity" USING btree ("Description");



CREATE INDEX idx_activity_idclass ON "Activity" USING btree ("IdClass");



CREATE INDEX idx_asset_code ON "Asset" USING btree ("Code");



CREATE INDEX idx_asset_defaultorder ON "Asset" USING btree ("Description", "Id");



CREATE INDEX idx_asset_description ON "Asset" USING btree ("Description");



CREATE INDEX idx_asset_idclass ON "Asset" USING btree ("IdClass");



CREATE INDEX idx_bimlayer_begindate ON "_BimLayer" USING btree ("BeginDate");



CREATE INDEX idx_bimproject_begindate ON "_BimProject" USING btree ("BeginDate");



CREATE INDEX idx_building_code ON "Building" USING btree ("Code");



CREATE INDEX idx_building_defaultorder ON "Building" USING btree ("Description", "Id");



CREATE INDEX idx_building_description ON "Building" USING btree ("Description");



CREATE INDEX idx_building_idclass ON "Building" USING btree ("IdClass");



CREATE INDEX idx_buildinghistory_currentid ON "Building_history" USING btree ("CurrentId");



CREATE INDEX idx_class_code ON "Class" USING btree ("Code");



CREATE INDEX idx_class_defaultorder ON "Class" USING btree ("Description", "Id");



CREATE INDEX idx_class_description ON "Class" USING btree ("Description");



CREATE INDEX idx_computer_code ON "Computer" USING btree ("Code");



CREATE INDEX idx_computer_defaultorder ON "Computer" USING btree ("Description", "Id");



CREATE INDEX idx_computer_description ON "Computer" USING btree ("Description");



CREATE INDEX idx_computer_idclass ON "Computer" USING btree ("IdClass");



CREATE INDEX idx_custompage_code ON "_CustomPage" USING btree ("Code");



CREATE INDEX idx_custompage_description ON "_CustomPage" USING btree ("Description");



CREATE INDEX idx_custompage_idclass ON "_CustomPage" USING btree ("IdClass");



CREATE INDEX idx_custompagehistory_currentid ON "_CustomPage_history" USING btree ("CurrentId");



CREATE INDEX idx_dashboards_begindate ON "_Dashboards" USING btree ("BeginDate");



CREATE INDEX idx_domaintreenavigation_begindate ON "_DomainTreeNavigation" USING btree ("BeginDate");



CREATE INDEX idx_email_code ON "Email" USING btree ("Code");



CREATE INDEX idx_email_defaultorder ON "Email" USING btree ("Description", "Id");



CREATE INDEX idx_email_description ON "Email" USING btree ("Description");



CREATE INDEX idx_email_idclass ON "Email" USING btree ("IdClass");



CREATE INDEX idx_emailaccount_code ON "_EmailAccount" USING btree ("Code");



CREATE INDEX idx_emailaccount_description ON "_EmailAccount" USING btree ("Description");



CREATE INDEX idx_emailaccount_idclass ON "_EmailAccount" USING btree ("IdClass");



CREATE INDEX idx_emailaccounthistory_currentid ON "_EmailAccount_history" USING btree ("CurrentId");



CREATE INDEX idx_emailhistory_currentid ON "Email_history" USING btree ("CurrentId");



CREATE INDEX idx_emailtemplate_code ON "_EmailTemplate" USING btree ("Code");



CREATE INDEX idx_emailtemplate_defaultorder ON "_EmailTemplate" USING btree ("Description", "Id");



CREATE INDEX idx_emailtemplate_description ON "_EmailTemplate" USING btree ("Description");



CREATE INDEX idx_emailtemplate_idclass ON "_EmailTemplate" USING btree ("IdClass");



CREATE INDEX idx_emailtemplatehistory_currentid ON "_EmailTemplate_history" USING btree ("CurrentId");



CREATE INDEX idx_employee_code ON "Employee" USING btree ("Code");



CREATE INDEX idx_employee_defaultorder ON "Employee" USING btree ("Code", "Description" DESC, "Notes" DESC, "Surname" DESC, "Name" DESC, "Type" DESC, "Qualification" DESC, "Level" DESC, "Email" DESC, "Office" DESC, "Phone" DESC, "Mobile" DESC, "Fax" DESC, "State" DESC, "Id");



CREATE INDEX idx_employee_description ON "Employee" USING btree ("Description");



CREATE INDEX idx_employee_idclass ON "Employee" USING btree ("IdClass");



CREATE INDEX idx_employeehistory_currentid ON "Employee_history" USING btree ("CurrentId");



CREATE INDEX idx_filter_begindate ON "_Filter" USING btree ("BeginDate");



CREATE INDEX idx_floor_code ON "Floor" USING btree ("Code");



CREATE INDEX idx_floor_defaultorder ON "Floor" USING btree ("Description", "Id");



CREATE INDEX idx_floor_description ON "Floor" USING btree ("Description");



CREATE INDEX idx_floor_idclass ON "Floor" USING btree ("IdClass");



CREATE INDEX idx_floorhistory_currentid ON "Floor_history" USING btree ("CurrentId");



CREATE INDEX idx_grant_begindate ON "Grant" USING btree ("BeginDate");



CREATE INDEX idx_icon_begindate ON "_Icon" USING btree ("BeginDate");



CREATE INDEX idx_idclass_id ON "Class" USING btree ("IdClass", "Id");



CREATE INDEX idx_invoice_code ON "Invoice" USING btree ("Code");



CREATE INDEX idx_invoice_defaultorder ON "Invoice" USING btree ("Description", "Id");



CREATE INDEX idx_invoice_description ON "Invoice" USING btree ("Description");



CREATE INDEX idx_invoice_idclass ON "Invoice" USING btree ("IdClass");



CREATE INDEX idx_invoicehistory_currentid ON "Invoice_history" USING btree ("CurrentId");



CREATE INDEX idx_layer_begindate ON "_Layer" USING btree ("BeginDate");



CREATE INDEX idx_license_code ON "License" USING btree ("Code");



CREATE INDEX idx_license_defaultorder ON "License" USING btree ("Description", "Id");



CREATE INDEX idx_license_description ON "License" USING btree ("Description");



CREATE INDEX idx_license_idclass ON "License" USING btree ("IdClass");



CREATE INDEX idx_licensehistory_currentid ON "License_history" USING btree ("CurrentId");



CREATE INDEX idx_lookup_begindate ON "LookUp" USING btree ("BeginDate");



CREATE UNIQUE INDEX idx_map_accounttemplate_activerows ON "Map_AccountTemplate" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_accounttemplate_uniqueright ON "Map_AccountTemplate" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_assetassignee_activerows ON "Map_AssetAssignee" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_assetassignee_uniqueright ON "Map_AssetAssignee" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_assetreference_activerows ON "Map_AssetReference" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_assetreference_uniqueright ON "Map_AssetReference" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_buildingfloor_activerows ON "Map_BuildingFloor" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_buildingfloor_uniqueright ON "Map_BuildingFloor" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_classemail_activerows ON "Map_ClassEmail" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_classemail_uniqueright ON "Map_ClassEmail" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_classmetadata_activerows ON "Map_ClassMetadata" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_classmetadata_uniqueright ON "Map_ClassMetadata" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_filterrole_activerows ON "Map_FilterRole" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_floorroom_activerows ON "Map_FloorRoom" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_floorroom_uniqueright ON "Map_FloorRoom" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE INDEX idx_map_iddomain ON "Map" USING btree ("IdDomain");



CREATE INDEX idx_map_idobj1 ON "Map" USING btree ("IdObj1");



CREATE INDEX idx_map_idobj2 ON "Map" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_members_activerows ON "Map_Members" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_members_uniqueright ON "Map_Members" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_networkdeviceconnection_activerows ON "Map_NetworkDeviceConnection" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_officeroom_activerows ON "Map_OfficeRoom" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_officeroom_uniqueright ON "Map_OfficeRoom" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_rfcchangemanager_activerows ON "Map_RFCChangeManager" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_rfcchangemanager_uniqueleft ON "Map_RFCChangeManager" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass1" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj1" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_rfcexecutor_activerows ON "Map_RFCExecutor" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_rfcexecutor_uniqueleft ON "Map_RFCExecutor" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass1" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj1" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_rfcrequester_activerows ON "Map_RFCRequester" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_rfcrequester_uniqueleft ON "Map_RFCRequester" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass1" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj1" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_roomasset_activerows ON "Map_RoomAsset" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_roomasset_uniqueright ON "Map_RoomAsset" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_roomnetworkpoint_activerows ON "Map_RoomNetworkPoint" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_roomnetworkpoint_uniqueright ON "Map_RoomNetworkPoint" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_roomworkplace_activerows ON "Map_RoomWorkplace" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_roomworkplace_uniqueright ON "Map_RoomWorkplace" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_supervisor_activerows ON "Map_Supervisor" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_supervisor_uniqueright ON "Map_Supervisor" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_supplierasset_activerows ON "Map_SupplierAsset" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_supplierasset_uniqueright ON "Map_SupplierAsset" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_suppliercontact_activerows ON "Map_SupplierContact" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_suppliercontact_uniqueright ON "Map_SupplierContact" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_supplierinvoice_activerows ON "Map_SupplierInvoice" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_supplierinvoice_uniqueright ON "Map_SupplierInvoice" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_userrole_activerows ON "Map_UserRole" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_userrole_defaultgroup ON "Map_UserRole" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN "DefaultGroup" THEN true ELSE NULL::boolean END));



CREATE UNIQUE INDEX idx_map_workplacecomposition_activerows ON "Map_WorkplaceComposition" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_workplacecomposition_uniqueright ON "Map_WorkplaceComposition" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE INDEX idx_mapaccounttemplate_iddomain ON "Map_AccountTemplate" USING btree ("IdDomain");



CREATE INDEX idx_mapaccounttemplate_idobj1 ON "Map_AccountTemplate" USING btree ("IdObj1");



CREATE INDEX idx_mapaccounttemplate_idobj2 ON "Map_AccountTemplate" USING btree ("IdObj2");



CREATE INDEX idx_mapassetassignee_iddomain ON "Map_AssetAssignee" USING btree ("IdDomain");



CREATE INDEX idx_mapassetassignee_idobj1 ON "Map_AssetAssignee" USING btree ("IdObj1");



CREATE INDEX idx_mapassetassignee_idobj2 ON "Map_AssetAssignee" USING btree ("IdObj2");



CREATE INDEX idx_mapassetreference_iddomain ON "Map_AssetReference" USING btree ("IdDomain");



CREATE INDEX idx_mapassetreference_idobj1 ON "Map_AssetReference" USING btree ("IdObj1");



CREATE INDEX idx_mapassetreference_idobj2 ON "Map_AssetReference" USING btree ("IdObj2");



CREATE INDEX idx_mapbuildingfloor_iddomain ON "Map_BuildingFloor" USING btree ("IdDomain");



CREATE INDEX idx_mapbuildingfloor_idobj1 ON "Map_BuildingFloor" USING btree ("IdObj1");



CREATE INDEX idx_mapbuildingfloor_idobj2 ON "Map_BuildingFloor" USING btree ("IdObj2");



CREATE INDEX idx_mapclassemail_iddomain ON "Map_ClassEmail" USING btree ("IdDomain");



CREATE INDEX idx_mapclassemail_idobj1 ON "Map_ClassEmail" USING btree ("IdObj1");



CREATE INDEX idx_mapclassemail_idobj2 ON "Map_ClassEmail" USING btree ("IdObj2");



CREATE INDEX idx_mapclassmetadata_iddomain ON "Map_ClassMetadata" USING btree ("IdDomain");



CREATE INDEX idx_mapclassmetadata_idobj1 ON "Map_ClassMetadata" USING btree ("IdObj1");



CREATE INDEX idx_mapclassmetadata_idobj2 ON "Map_ClassMetadata" USING btree ("IdObj2");



CREATE INDEX idx_mapfilterrole_iddomain ON "Map_FilterRole" USING btree ("IdDomain");



CREATE INDEX idx_mapfilterrole_idobj1 ON "Map_FilterRole" USING btree ("IdObj1");



CREATE INDEX idx_mapfilterrole_idobj2 ON "Map_FilterRole" USING btree ("IdObj2");



CREATE INDEX idx_mapfloorroom_iddomain ON "Map_FloorRoom" USING btree ("IdDomain");



CREATE INDEX idx_mapfloorroom_idobj1 ON "Map_FloorRoom" USING btree ("IdObj1");



CREATE INDEX idx_mapfloorroom_idobj2 ON "Map_FloorRoom" USING btree ("IdObj2");



CREATE INDEX idx_mapmembers_iddomain ON "Map_Members" USING btree ("IdDomain");



CREATE INDEX idx_mapmembers_idobj1 ON "Map_Members" USING btree ("IdObj1");



CREATE INDEX idx_mapmembers_idobj2 ON "Map_Members" USING btree ("IdObj2");



CREATE INDEX idx_mapnetworkdeviceconnection_iddomain ON "Map_NetworkDeviceConnection" USING btree ("IdDomain");



CREATE INDEX idx_mapnetworkdeviceconnection_idobj1 ON "Map_NetworkDeviceConnection" USING btree ("IdObj1");



CREATE INDEX idx_mapnetworkdeviceconnection_idobj2 ON "Map_NetworkDeviceConnection" USING btree ("IdObj2");



CREATE INDEX idx_mapofficeroom_iddomain ON "Map_OfficeRoom" USING btree ("IdDomain");



CREATE INDEX idx_mapofficeroom_idobj1 ON "Map_OfficeRoom" USING btree ("IdObj1");



CREATE INDEX idx_mapofficeroom_idobj2 ON "Map_OfficeRoom" USING btree ("IdObj2");



CREATE INDEX idx_maprfcchangemanager_iddomain ON "Map_RFCChangeManager" USING btree ("IdDomain");



CREATE INDEX idx_maprfcchangemanager_idobj1 ON "Map_RFCChangeManager" USING btree ("IdObj1");



CREATE INDEX idx_maprfcchangemanager_idobj2 ON "Map_RFCChangeManager" USING btree ("IdObj2");



CREATE INDEX idx_maprfcexecutor_iddomain ON "Map_RFCExecutor" USING btree ("IdDomain");



CREATE INDEX idx_maprfcexecutor_idobj1 ON "Map_RFCExecutor" USING btree ("IdObj1");



CREATE INDEX idx_maprfcexecutor_idobj2 ON "Map_RFCExecutor" USING btree ("IdObj2");



CREATE INDEX idx_maprfcrequester_iddomain ON "Map_RFCRequester" USING btree ("IdDomain");



CREATE INDEX idx_maprfcrequester_idobj1 ON "Map_RFCRequester" USING btree ("IdObj1");



CREATE INDEX idx_maprfcrequester_idobj2 ON "Map_RFCRequester" USING btree ("IdObj2");



CREATE INDEX idx_maproomasset_iddomain ON "Map_RoomAsset" USING btree ("IdDomain");



CREATE INDEX idx_maproomasset_idobj1 ON "Map_RoomAsset" USING btree ("IdObj1");



CREATE INDEX idx_maproomasset_idobj2 ON "Map_RoomAsset" USING btree ("IdObj2");



CREATE INDEX idx_maproomnetworkpoint_iddomain ON "Map_RoomNetworkPoint" USING btree ("IdDomain");



CREATE INDEX idx_maproomnetworkpoint_idobj1 ON "Map_RoomNetworkPoint" USING btree ("IdObj1");



CREATE INDEX idx_maproomnetworkpoint_idobj2 ON "Map_RoomNetworkPoint" USING btree ("IdObj2");



CREATE INDEX idx_maproomworkplace_iddomain ON "Map_RoomWorkplace" USING btree ("IdDomain");



CREATE INDEX idx_maproomworkplace_idobj1 ON "Map_RoomWorkplace" USING btree ("IdObj1");



CREATE INDEX idx_maproomworkplace_idobj2 ON "Map_RoomWorkplace" USING btree ("IdObj2");



CREATE INDEX idx_mapsupervisor_iddomain ON "Map_Supervisor" USING btree ("IdDomain");



CREATE INDEX idx_mapsupervisor_idobj1 ON "Map_Supervisor" USING btree ("IdObj1");



CREATE INDEX idx_mapsupervisor_idobj2 ON "Map_Supervisor" USING btree ("IdObj2");



CREATE INDEX idx_mapsupplierasset_iddomain ON "Map_SupplierAsset" USING btree ("IdDomain");



CREATE INDEX idx_mapsupplierasset_idobj1 ON "Map_SupplierAsset" USING btree ("IdObj1");



CREATE INDEX idx_mapsupplierasset_idobj2 ON "Map_SupplierAsset" USING btree ("IdObj2");



CREATE INDEX idx_mapsuppliercontact_iddomain ON "Map_SupplierContact" USING btree ("IdDomain");



CREATE INDEX idx_mapsuppliercontact_idobj1 ON "Map_SupplierContact" USING btree ("IdObj1");



CREATE INDEX idx_mapsuppliercontact_idobj2 ON "Map_SupplierContact" USING btree ("IdObj2");



CREATE INDEX idx_mapsupplierinvoice_iddomain ON "Map_SupplierInvoice" USING btree ("IdDomain");



CREATE INDEX idx_mapsupplierinvoice_idobj1 ON "Map_SupplierInvoice" USING btree ("IdObj1");



CREATE INDEX idx_mapsupplierinvoice_idobj2 ON "Map_SupplierInvoice" USING btree ("IdObj2");



CREATE INDEX idx_mapuserrole_iddomain ON "Map_UserRole" USING btree ("IdDomain");



CREATE INDEX idx_mapuserrole_idobj1 ON "Map_UserRole" USING btree ("IdObj1");



CREATE INDEX idx_mapuserrole_idobj2 ON "Map_UserRole" USING btree ("IdObj2");



CREATE INDEX idx_mapworkplacecomposition_iddomain ON "Map_WorkplaceComposition" USING btree ("IdDomain");



CREATE INDEX idx_mapworkplacecomposition_idobj1 ON "Map_WorkplaceComposition" USING btree ("IdObj1");



CREATE INDEX idx_mapworkplacecomposition_idobj2 ON "Map_WorkplaceComposition" USING btree ("IdObj2");



CREATE INDEX idx_menu_code ON "Menu" USING btree ("Code");



CREATE INDEX idx_menu_defaultorder ON "Menu" USING btree ("Description", "Id");



CREATE INDEX idx_menu_description ON "Menu" USING btree ("Description");



CREATE INDEX idx_menu_idclass ON "Menu" USING btree ("IdClass");



CREATE INDEX idx_menuhistory_currentid ON "Menu_history" USING btree ("CurrentId");



CREATE INDEX idx_metadata_code ON "Metadata" USING btree ("Code");



CREATE INDEX idx_metadata_defaultorder ON "Metadata" USING btree ("Description", "Id");



CREATE INDEX idx_metadata_description ON "Metadata" USING btree ("Description");



CREATE INDEX idx_metadata_idclass ON "Metadata" USING btree ("IdClass");



CREATE INDEX idx_metadatahistory_currentid ON "Metadata_history" USING btree ("CurrentId");



CREATE INDEX idx_monitor_code ON "Monitor" USING btree ("Code");



CREATE INDEX idx_monitor_defaultorder ON "Monitor" USING btree ("Description", "Id");



CREATE INDEX idx_monitor_description ON "Monitor" USING btree ("Description");



CREATE INDEX idx_monitor_idclass ON "Monitor" USING btree ("IdClass");



CREATE INDEX idx_monitorhistory_currentid ON "Monitor_history" USING btree ("CurrentId");



CREATE INDEX idx_networkdevice_code ON "NetworkDevice" USING btree ("Code");



CREATE INDEX idx_networkdevice_defaultorder ON "NetworkDevice" USING btree ("Description", "Id");



CREATE INDEX idx_networkdevice_description ON "NetworkDevice" USING btree ("Description");



CREATE INDEX idx_networkdevice_idclass ON "NetworkDevice" USING btree ("IdClass");



CREATE INDEX idx_networkdevicehistory_currentid ON "NetworkDevice_history" USING btree ("CurrentId");



CREATE INDEX idx_networkpoint_code ON "NetworkPoint" USING btree ("Code");



CREATE INDEX idx_networkpoint_defaultorder ON "NetworkPoint" USING btree ("Description", "Id");



CREATE INDEX idx_networkpoint_description ON "NetworkPoint" USING btree ("Description");



CREATE INDEX idx_networkpoint_idclass ON "NetworkPoint" USING btree ("IdClass");



CREATE INDEX idx_networkpointhistory_currentid ON "NetworkPoint_history" USING btree ("CurrentId");



CREATE INDEX idx_notebook_code ON "Notebook" USING btree ("Code");



CREATE INDEX idx_notebook_defaultorder ON "Notebook" USING btree ("Description", "Id");



CREATE INDEX idx_notebook_description ON "Notebook" USING btree ("Description");



CREATE INDEX idx_notebook_idclass ON "Notebook" USING btree ("IdClass");



CREATE INDEX idx_notebookhistory_currentid ON "Notebook_history" USING btree ("CurrentId");



CREATE INDEX idx_office_code ON "Office" USING btree ("Code");



CREATE INDEX idx_office_defaultorder ON "Office" USING btree ("Description", "Id");



CREATE INDEX idx_office_description ON "Office" USING btree ("Description");



CREATE INDEX idx_office_idclass ON "Office" USING btree ("IdClass");



CREATE INDEX idx_officehistory_currentid ON "Office_history" USING btree ("CurrentId");



CREATE INDEX idx_patch_code ON "Patch" USING btree ("Code");



CREATE INDEX idx_patch_defaultorder ON "Patch" USING btree ("Description", "Id");



CREATE INDEX idx_patch_description ON "Patch" USING btree ("Description");



CREATE INDEX idx_patch_idclass ON "Patch" USING btree ("IdClass");



CREATE INDEX idx_patchhistory_currentid ON "Patch_history" USING btree ("CurrentId");



CREATE INDEX idx_pc_code ON "PC" USING btree ("Code");



CREATE INDEX idx_pc_defaultorder ON "PC" USING btree ("Description", "Id");



CREATE INDEX idx_pc_description ON "PC" USING btree ("Description");



CREATE INDEX idx_pc_idclass ON "PC" USING btree ("IdClass");



CREATE INDEX idx_pchistory_currentid ON "PC_history" USING btree ("CurrentId");



CREATE INDEX idx_printer_code ON "Printer" USING btree ("Code");



CREATE INDEX idx_printer_defaultorder ON "Printer" USING btree ("Description", "Id");



CREATE INDEX idx_printer_description ON "Printer" USING btree ("Description");



CREATE INDEX idx_printer_idclass ON "Printer" USING btree ("IdClass");



CREATE INDEX idx_printerhistory_currentid ON "Printer_history" USING btree ("CurrentId");



CREATE INDEX idx_rack_code ON "Rack" USING btree ("Code");



CREATE INDEX idx_rack_defaultorder ON "Rack" USING btree ("Description", "Id");



CREATE INDEX idx_rack_description ON "Rack" USING btree ("Description");



CREATE INDEX idx_rack_idclass ON "Rack" USING btree ("IdClass");



CREATE INDEX idx_rackhistory_currentid ON "Rack_history" USING btree ("CurrentId");



CREATE INDEX idx_report_defaultorder ON "Report" USING btree ("Description", "Id");



CREATE INDEX idx_requestforchange_code ON "RequestForChange" USING btree ("Code");



CREATE INDEX idx_requestforchange_defaultorder ON "RequestForChange" USING btree ("Description", "Id");



CREATE INDEX idx_requestforchange_description ON "RequestForChange" USING btree ("Description");



CREATE INDEX idx_requestforchange_idclass ON "RequestForChange" USING btree ("IdClass");



CREATE INDEX idx_requestforchangehistory_currentid ON "RequestForChange_history" USING btree ("CurrentId");



CREATE INDEX idx_role_code ON "Role" USING btree ("Code");



CREATE INDEX idx_role_defaultorder ON "Role" USING btree ("Description", "Id");



CREATE INDEX idx_role_description ON "Role" USING btree ("Description");



CREATE INDEX idx_role_idclass ON "Role" USING btree ("IdClass");



CREATE INDEX idx_rolehistory_currentid ON "Role_history" USING btree ("CurrentId");



CREATE INDEX idx_room_code ON "Room" USING btree ("Code");



CREATE INDEX idx_room_defaultorder ON "Room" USING btree ("Description", "Id");



CREATE INDEX idx_room_description ON "Room" USING btree ("Description");



CREATE INDEX idx_room_idclass ON "Room" USING btree ("IdClass");



CREATE INDEX idx_roomhistory_currentid ON "Room_history" USING btree ("CurrentId");



CREATE INDEX idx_scheduler_code ON "_Task" USING btree ("Code");



CREATE INDEX idx_scheduler_defaultorder ON "_Task" USING btree ("Description", "Id");



CREATE INDEX idx_scheduler_description ON "_Task" USING btree ("Description");



CREATE INDEX idx_scheduler_idclass ON "_Task" USING btree ("IdClass");



CREATE INDEX idx_schedulerhistory_currentid ON "_Task_history" USING btree ("CurrentId");



CREATE INDEX idx_schedulerjobparameter_code ON "_TaskParameter" USING btree ("Code");



CREATE INDEX idx_schedulerjobparameter_description ON "_TaskParameter" USING btree ("Description");



CREATE INDEX idx_schedulerjobparameter_idclass ON "_TaskParameter" USING btree ("IdClass");



CREATE INDEX idx_schedulerjobparameterhistory_currentid ON "_TaskParameter_history" USING btree ("CurrentId");



CREATE INDEX idx_server_code ON "Server" USING btree ("Code");



CREATE INDEX idx_server_defaultorder ON "Server" USING btree ("Description", "Id");



CREATE INDEX idx_server_description ON "Server" USING btree ("Description");



CREATE INDEX idx_server_idclass ON "Server" USING btree ("IdClass");



CREATE INDEX idx_serverhistory_currentid ON "Server_history" USING btree ("CurrentId");



CREATE INDEX idx_supplier_code ON "Supplier" USING btree ("Code");



CREATE INDEX idx_supplier_defaultorder ON "Supplier" USING btree ("Description", "Id");



CREATE INDEX idx_supplier_description ON "Supplier" USING btree ("Description");



CREATE INDEX idx_supplier_idclass ON "Supplier" USING btree ("IdClass");



CREATE INDEX idx_suppliercontact_code ON "SupplierContact" USING btree ("Code");



CREATE INDEX idx_suppliercontact_defaultorder ON "SupplierContact" USING btree ("Description", "Id");



CREATE INDEX idx_suppliercontact_description ON "SupplierContact" USING btree ("Description");



CREATE INDEX idx_suppliercontact_idclass ON "SupplierContact" USING btree ("IdClass");



CREATE INDEX idx_suppliercontacthistory_currentid ON "SupplierContact_history" USING btree ("CurrentId");



CREATE INDEX idx_supplierhistory_currentid ON "Supplier_history" USING btree ("CurrentId");



CREATE INDEX idx_taskruntime_begindate ON "_TaskRuntime" USING btree ("BeginDate");



CREATE INDEX idx_templates_begindate ON "_Templates" USING btree ("BeginDate");



CREATE INDEX idx_translation_begindate ON "_Translation" USING btree ("BeginDate");



CREATE INDEX idx_ups_code ON "UPS" USING btree ("Code");



CREATE INDEX idx_ups_defaultorder ON "UPS" USING btree ("Description", "Id");



CREATE INDEX idx_ups_description ON "UPS" USING btree ("Description");



CREATE INDEX idx_ups_idclass ON "UPS" USING btree ("IdClass");



CREATE INDEX idx_upshistory_currentid ON "UPS_history" USING btree ("CurrentId");



CREATE INDEX idx_user_code ON "User" USING btree ("Code");



CREATE INDEX idx_user_defaultorder ON "User" USING btree ("Description", "Id");



CREATE INDEX idx_user_description ON "User" USING btree ("Description");



CREATE INDEX idx_user_idclass ON "User" USING btree ("IdClass");



CREATE INDEX idx_userhistory_currentid ON "User_history" USING btree ("CurrentId");



CREATE INDEX idx_view_begindate ON "_View" USING btree ("BeginDate");



CREATE INDEX idx_widget_code ON "_Widget" USING btree ("Code");



CREATE INDEX idx_widget_defaultorder ON "_Widget" USING btree ("Description", "Id");



CREATE INDEX idx_widget_description ON "_Widget" USING btree ("Description");



CREATE INDEX idx_widget_idclass ON "_Widget" USING btree ("IdClass");



CREATE INDEX idx_widgethistory_currentid ON "_Widget_history" USING btree ("CurrentId");



CREATE INDEX idx_workplace_code ON "Workplace" USING btree ("Code");



CREATE INDEX idx_workplace_defaultorder ON "Workplace" USING btree ("Description", "Id");



CREATE INDEX idx_workplace_description ON "Workplace" USING btree ("Description");



CREATE INDEX idx_workplace_idclass ON "Workplace" USING btree ("IdClass");



CREATE INDEX idx_workplacehistory_currentid ON "Workplace_history" USING btree ("CurrentId");



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"', '');



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey" BEFORE INSERT OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"', '');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey" BEFORE INSERT OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"', '');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey" BEFORE INSERT OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"', '');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey" BEFORE INSERT OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"', '');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey" BEFORE INSERT OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Email_Card_fkey" BEFORE INSERT OR UPDATE ON "Email" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Card', '"Class"', '');



CREATE TRIGGER "Employee_Office_fkey" BEFORE INSERT OR UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Office', '"Office"', '');



CREATE TRIGGER "Floor_Building_fkey" BEFORE INSERT OR UPDATE ON "Floor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Building', '"Building"', '');



CREATE TRIGGER "Invoice_Supplier_fkey" BEFORE INSERT OR UPDATE ON "Invoice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"', '');



CREATE TRIGGER "NetworkPoint_Room_fkey" BEFORE INSERT OR UPDATE ON "NetworkPoint" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"', '');



CREATE TRIGGER "Office_Supervisor_fkey" BEFORE INSERT OR UPDATE ON "Office" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supervisor', '"Employee"', '');



CREATE TRIGGER "RequestForChange_Requester_fkey" BEFORE INSERT OR UPDATE ON "RequestForChange" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Requester', '"Employee"', '');



CREATE TRIGGER "Room_Floor_fkey" BEFORE INSERT OR UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Floor', '"Floor"', '');



CREATE TRIGGER "Room_Office_fkey" BEFORE INSERT OR UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Office', '"Office"', '');



CREATE TRIGGER "SupplierContact_Supplier_fkey" BEFORE INSERT OR UPDATE ON "SupplierContact" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"', '');



CREATE TRIGGER "Workplace_Room_fkey" BEFORE INSERT OR UPDATE ON "Workplace" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"', '');



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Menu" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Email" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Metadata" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "_Task" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Patch" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Office" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Building" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Floor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Invoice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Supplier" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Workplace" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "SupplierContact" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "NetworkPoint" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "RequestForChange" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "_Widget" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "User" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "Role" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "_EmailTemplate" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "_EmailAccount" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "_TaskParameter" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations" AFTER UPDATE ON "_CustomPage" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_Constr_Asset_Assignee" BEFORE DELETE OR UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Asset"', 'Assignee');



CREATE TRIGGER "_Constr_Asset_Room" BEFORE DELETE OR UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Asset"', 'Room');



CREATE TRIGGER "_Constr_Asset_Supplier" BEFORE DELETE OR UPDATE ON "Supplier" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Asset"', 'Supplier');



CREATE TRIGGER "_Constr_Asset_TechnicalReference" BEFORE DELETE OR UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Asset"', 'TechnicalReference');



CREATE TRIGGER "_Constr_Asset_Workplace" BEFORE DELETE OR UPDATE ON "Workplace" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Asset"', 'Workplace');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Metadata" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "SupplierContact" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Floor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Supplier" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "_TaskParameter" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Office" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Activity" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Building" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Email" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Patch" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Workplace" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Role" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "_Widget" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "User" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "_Task" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Class" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "NetworkPoint" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "RequestForChange" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "_EmailAccount" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Menu" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Invoice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "_EmailTemplate" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Email_Card" BEFORE DELETE OR UPDATE ON "_CustomPage" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Card');



CREATE TRIGGER "_Constr_Employee_Office" BEFORE DELETE OR UPDATE ON "Office" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Employee"', 'Office');



CREATE TRIGGER "_Constr_Floor_Building" BEFORE DELETE OR UPDATE ON "Building" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Floor"', 'Building');



CREATE TRIGGER "_Constr_Invoice_Supplier" BEFORE DELETE OR UPDATE ON "Supplier" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Invoice"', 'Supplier');



CREATE TRIGGER "_Constr_NetworkPoint_Room" BEFORE DELETE OR UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"NetworkPoint"', 'Room');



CREATE TRIGGER "_Constr_Office_Supervisor" BEFORE DELETE OR UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Office"', 'Supervisor');



CREATE TRIGGER "_Constr_RequestForChange_Requester" BEFORE DELETE OR UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"RequestForChange"', 'Requester');



CREATE TRIGGER "_Constr_Room_Floor" BEFORE DELETE OR UPDATE ON "Floor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Room"', 'Floor');



CREATE TRIGGER "_Constr_Room_Office" BEFORE DELETE OR UPDATE ON "Office" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Room"', 'Office');



CREATE TRIGGER "_Constr_SupplierContact_Supplier" BEFORE DELETE OR UPDATE ON "Supplier" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"SupplierContact"', 'Supplier');



CREATE TRIGGER "_Constr_Workplace_Room" BEFORE DELETE OR UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"Workplace"', 'Room');



CREATE TRIGGER "_Constr__EmailTemplate_Account" BEFORE DELETE OR UPDATE ON "_EmailAccount" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"_EmailTemplate"', 'Account');



CREATE TRIGGER "_Constr__TaskRuntime_Owner" BEFORE DELETE OR UPDATE ON "_Task" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_restrict('"_TaskRuntime"', 'Owner');



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Menu" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Email" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Metadata" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "_Task" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_UserRole" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Patch" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Office" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_Members" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Building" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Floor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Invoice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Supplier" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Workplace" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "SupplierContact" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_BuildingFloor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_SupplierInvoice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_FloorRoom" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_OfficeRoom" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_SupplierContact" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_RoomWorkplace" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_WorkplaceComposition" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_SupplierAsset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_AssetAssignee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_AssetReference" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_RoomAsset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "NetworkPoint" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_RoomNetworkPoint" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_NetworkDeviceConnection" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "RequestForChange" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_RFCChangeManager" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_RFCExecutor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_RFCRequester" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_Supervisor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "_Widget" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "User" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Role" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "_EmailTemplate" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "_EmailAccount" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "_TaskParameter" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_ClassMetadata" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_AccountTemplate" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_ClassEmail" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "Map_FilterRole" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow" AFTER DELETE OR UPDATE ON "_CustomPage" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_EmailTemplate_Account_fkey" BEFORE INSERT OR UPDATE ON "_EmailTemplate" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Account', '"_EmailAccount"', '');



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Menu" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Email" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Metadata" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_Task" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_UserRole" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Patch" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Office" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_Members" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Building" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Floor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Invoice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Supplier" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Workplace" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "SupplierContact" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_BuildingFloor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_SupplierInvoice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_FloorRoom" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_OfficeRoom" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_SupplierContact" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_RoomWorkplace" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_WorkplaceComposition" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_SupplierAsset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_AssetAssignee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_AssetReference" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_RoomAsset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "NetworkPoint" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_RoomNetworkPoint" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_NetworkDeviceConnection" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_Templates" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_Dashboards" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "RequestForChange" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_RFCChangeManager" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_RFCExecutor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_RFCRequester" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_Supervisor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_DomainTreeNavigation" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_Layer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "LookUp" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Grant" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_Filter" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_Widget" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_View" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "User" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Role" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_EmailTemplate" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_BimProject" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_BimLayer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_EmailAccount" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_TaskParameter" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_Translation" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_ClassMetadata" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_AccountTemplate" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_ClassEmail" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_TaskRuntime" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "Map_FilterRole" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_CustomPage" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck" BEFORE INSERT OR DELETE OR UPDATE ON "_Icon" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_TaskRuntime_Owner_fkey" BEFORE INSERT OR UPDATE ON "_TaskRuntime" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_fk('Owner', '"_Task"', 'simple');



CREATE TRIGGER "_UpdRef_Asset_Assignee" AFTER INSERT OR UPDATE ON "Map_AssetAssignee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Assignee', '"Asset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Asset_Room" AFTER INSERT OR UPDATE ON "Map_RoomAsset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Room', '"Asset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Asset_Supplier" AFTER INSERT OR UPDATE ON "Map_SupplierAsset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Supplier', '"Asset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "Map_AssetReference" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('TechnicalReference', '"Asset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Asset_Workplace" AFTER INSERT OR UPDATE ON "Map_WorkplaceComposition" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Workplace', '"Asset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Email_Card" AFTER INSERT OR UPDATE ON "Map_ClassEmail" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Card', '"Email"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Employee_Office" AFTER INSERT OR UPDATE ON "Map_Members" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Office', '"Employee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Floor_Building" AFTER INSERT OR UPDATE ON "Map_BuildingFloor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Building', '"Floor"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Invoice_Supplier" AFTER INSERT OR UPDATE ON "Map_SupplierInvoice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Supplier', '"Invoice"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_NetworkPoint_Room" AFTER INSERT OR UPDATE ON "Map_RoomNetworkPoint" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Room', '"NetworkPoint"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Office_Supervisor" AFTER INSERT OR UPDATE ON "Map_Supervisor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Supervisor', '"Office"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_RequestForChange_Requester" AFTER INSERT OR UPDATE ON "Map_RFCRequester" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Requester', '"RequestForChange"', 'IdObj1', 'IdObj2');



CREATE TRIGGER "_UpdRef_Room_Floor" AFTER INSERT OR UPDATE ON "Map_FloorRoom" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Floor', '"Room"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Room_Office" AFTER INSERT OR UPDATE ON "Map_OfficeRoom" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Office', '"Room"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_SupplierContact_Supplier" AFTER INSERT OR UPDATE ON "Map_SupplierContact" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Supplier', '"SupplierContact"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Workplace_Room" AFTER INSERT OR UPDATE ON "Map_RoomWorkplace" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Room', '"Workplace"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef__EmailTemplate_Account" AFTER INSERT OR UPDATE ON "Map_AccountTemplate" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_reference('Account', '"_EmailTemplate"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee" AFTER INSERT OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room" AFTER INSERT OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier" AFTER INSERT OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference" AFTER INSERT OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "Asset" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "Rack" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "Computer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "PC" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "Server" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "Notebook" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "Monitor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "Printer" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "UPS" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "License" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace" AFTER INSERT OR UPDATE ON "NetworkDevice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Email_Card" AFTER INSERT OR UPDATE ON "Email" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Card', '"Map_ClassEmail"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Employee_Office" AFTER INSERT OR UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Office', '"Map_Members"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Floor_Building" AFTER INSERT OR UPDATE ON "Floor" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Building', '"Map_BuildingFloor"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Invoice_Supplier" AFTER INSERT OR UPDATE ON "Invoice" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierInvoice"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_NetworkPoint_Room" AFTER INSERT OR UPDATE ON "NetworkPoint" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomNetworkPoint"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Office_Supervisor" AFTER INSERT OR UPDATE ON "Office" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supervisor', '"Map_Supervisor"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_RequestForChange_Requester" AFTER INSERT OR UPDATE ON "RequestForChange" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Requester', '"Map_RFCRequester"', 'IdObj1', 'IdObj2');



CREATE TRIGGER "_UpdRel_Room_Floor" AFTER INSERT OR UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Floor', '"Map_FloorRoom"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Room_Office" AFTER INSERT OR UPDATE ON "Room" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Office', '"Map_OfficeRoom"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_SupplierContact_Supplier" AFTER INSERT OR UPDATE ON "SupplierContact" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierContact"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Workplace_Room" AFTER INSERT OR UPDATE ON "Workplace" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomWorkplace"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel__EmailTemplate_Account" AFTER INSERT OR UPDATE ON "_EmailTemplate" FOR EACH ROW EXECUTE PROCEDURE _cm_trigger_update_relation('Account', '"Map_AccountTemplate"', 'IdObj2', 'IdObj1');



CREATE TRIGGER set_data_employee BEFORE INSERT OR UPDATE ON "Employee" FOR EACH ROW EXECUTE PROCEDURE set_data_employee();



CREATE TRIGGER set_data_suppliercontact BEFORE INSERT OR UPDATE ON "SupplierContact" FOR EACH ROW EXECUTE PROCEDURE set_data_suppliercontact();



ALTER TABLE ONLY "Building_history"
    ADD CONSTRAINT "Building_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Building"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Email_history"
    ADD CONSTRAINT "Email_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Email"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Employee_history"
    ADD CONSTRAINT "Employee_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Employee"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Floor_history"
    ADD CONSTRAINT "Floor_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Floor"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Invoice_history"
    ADD CONSTRAINT "Invoice_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Invoice"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "License_history"
    ADD CONSTRAINT "License_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "License"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Menu_history"
    ADD CONSTRAINT "Menu_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Menu"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Metadata_history"
    ADD CONSTRAINT "Metadata_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Metadata"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Monitor_history"
    ADD CONSTRAINT "Monitor_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Monitor"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "NetworkDevice_history"
    ADD CONSTRAINT "NetworkDevice_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "NetworkDevice"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "NetworkPoint_history"
    ADD CONSTRAINT "NetworkPoint_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "NetworkPoint"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Notebook_history"
    ADD CONSTRAINT "Notebook_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Notebook"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Office_history"
    ADD CONSTRAINT "Office_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Office"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "PC_history"
    ADD CONSTRAINT "PC_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "PC"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Patch_history"
    ADD CONSTRAINT "Patch_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Patch"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Printer_history"
    ADD CONSTRAINT "Printer_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Printer"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Rack_history"
    ADD CONSTRAINT "Rack_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Rack"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "RequestForChange_history"
    ADD CONSTRAINT "RequestForChange_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "RequestForChange"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Role_history"
    ADD CONSTRAINT "Role_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Role"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Room_history"
    ADD CONSTRAINT "Room_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Room"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "_Task_history"
    ADD CONSTRAINT "Scheduler_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "_Task"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Server_history"
    ADD CONSTRAINT "Server_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Server"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "SupplierContact_history"
    ADD CONSTRAINT "SupplierContact_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "SupplierContact"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Supplier_history"
    ADD CONSTRAINT "Supplier_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Supplier"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "UPS_history"
    ADD CONSTRAINT "UPS_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "UPS"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "User_history"
    ADD CONSTRAINT "User_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "User"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Workplace_history"
    ADD CONSTRAINT "Workplace_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Workplace"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "_CustomPage_history"
    ADD CONSTRAINT "_CustomPage_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "_CustomPage"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "_EmailAccount_history"
    ADD CONSTRAINT "_EmailAccount_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "_EmailAccount"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "_EmailTemplate_history"
    ADD CONSTRAINT "_EmailTemplate_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "_EmailTemplate"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "_TaskParameter_history"
    ADD CONSTRAINT "_SchedulerJobParameter_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "_TaskParameter"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "_Widget_history"
    ADD CONSTRAINT "_Widget_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "_Widget"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;
