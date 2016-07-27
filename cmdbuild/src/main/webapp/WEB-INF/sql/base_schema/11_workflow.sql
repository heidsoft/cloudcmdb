--
-- FlowStatus lookup
--

INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'open.running','FlowStatus', 1, 'Running', true, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'open.not_running.suspended','FlowStatus', 2, 'Suspended', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.completed','FlowStatus', 3, 'Completed', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.terminated','FlowStatus', 4, 'Terminated', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.aborted','FlowStatus', 5, 'Aborted', false, 'A');

--
-- Activity class
--

CREATE TABLE "Activity"
(
  "FlowStatus" integer,
  "ActivityDefinitionId" character varying[],
  "ProcessCode" text,
  "NextExecutor" character varying[],
  "ActivityInstanceId" character varying[],
  "PrevExecutors" character varying[],
  "UniqueProcessDefinition" text,
  CONSTRAINT "Activity_pkey" PRIMARY KEY ("Id")
)
INHERITS ("Class");
COMMENT ON TABLE "Activity" IS 'MODE: baseclass|TYPE: class|DESCR: Activity|SUPERCLASS: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."IdClass" IS 'MODE: reserved|DESCR: Class';
COMMENT ON COLUMN "Activity"."Code" IS 'MODE: read|DESCR: Activity Name|INDEX: 0|DATEEXPIRE: false|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Description" IS 'MODE: read|DESCR: Description|INDEX: 1|DATEEXPIRE: false|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."Notes" IS 'MODE: read|DESCR: Notes';
COMMENT ON COLUMN "Activity"."FlowStatus" IS 'MODE: system|DESCR: Process Status|INDEX: 2|LOOKUP: FlowStatus';
COMMENT ON COLUMN "Activity"."ActivityDefinitionId" IS 'MODE: system|DESCR: Activity Definition Ids (for speed)';
COMMENT ON COLUMN "Activity"."ProcessCode" IS 'MODE: system|DESCR: Process Instance Id';
COMMENT ON COLUMN "Activity"."NextExecutor" IS 'MODE: system|DESCR: Activity Instance performers';
COMMENT ON COLUMN "Activity"."ActivityInstanceId" IS 'MODE: system|DESCR: Activity Instance Ids';
COMMENT ON COLUMN "Activity"."PrevExecutors" IS 'MODE: system|DESCR: Process Instance performers up to now';
COMMENT ON COLUMN "Activity"."UniqueProcessDefinition" IS 'MODE: system|DESCR: Unique Process Definition (for speed)';

CREATE INDEX idx_activity_code
  ON "Activity"
  USING btree
  ("Code");

CREATE INDEX idx_activity_description
  ON "Activity"
  USING btree
  ("Description");

CREATE INDEX idx_activity_idclass
  ON "Activity"
  USING btree
  ("IdClass");