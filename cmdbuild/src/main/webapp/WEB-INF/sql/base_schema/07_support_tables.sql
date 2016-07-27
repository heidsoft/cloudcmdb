---------------------------------------------
-- Menu
---------------------------------------------

SELECT cm_create_class('Menu', 'Class', 'MODE: reserved|TYPE: class|DESCR: Menu|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('Menu', 'IdParent', 'integer', '0', false, false, 'MODE: read|DESCR: Parent Item, 0 means no parent');
SELECT cm_create_class_attribute('Menu', 'IdElementClass', 'regclass', null, false, false, 'MODE: read|DESCR: Class connect to this item');
SELECT cm_create_class_attribute('Menu', 'IdElementObj', 'integer', '0', true, false, 'MODE: read|DESCR: Object connected to this item, 0 means no object');
SELECT cm_create_class_attribute('Menu', 'Number', 'integer', '0', true, false, 'MODE: read|DESCR: Ordering');
SELECT cm_create_class_attribute('Menu', 'GroupName', 'text', null, true, false, 'MODE: read');
SELECT cm_create_class_attribute('Menu', 'Type', 'varchar (70)', '', true, false, 'MODE: read');

---------------------------------------------
-- Report
---------------------------------------------

CREATE TABLE "Report"
(
  "Id" integer NOT NULL DEFAULT _cm_new_card_id(),
  "Code" varchar(40),
  "Description" varchar(100),
  "Status" varchar(1),
  "User" varchar(100),
  "BeginDate" timestamp without time zone NOT NULL DEFAULT now(),
  "Type" varchar(20),
  "Query" text,
  "SimpleReport" bytea,
  "RichReport" bytea,
  "Wizard" bytea,
  "Images" bytea,
  "ImagesLength" integer[],
  "ReportLength" integer[],
  "IdClass" regclass,
  "Groups" varchar[],
  "ImagesName" varchar[],
  CONSTRAINT "Report_pkey" PRIMARY KEY ("Id")
);
COMMENT ON TABLE "Report" IS 'MODE: reserved|TYPE: class|DESCR: Report|SUPERCLASS: false|STATUS: active';
COMMENT ON COLUMN "Report"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Code" IS 'MODE: read|DESCR: Code';
COMMENT ON COLUMN "Report"."Description" IS 'MODE: read|DESCR: Description';
COMMENT ON COLUMN "Report"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Type" IS 'MODE: read|DESCR: Type';
COMMENT ON COLUMN "Report"."Query" IS 'MODE: read|DESCR: Query';
COMMENT ON COLUMN "Report"."SimpleReport" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."RichReport" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Wizard" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Images" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."ImagesLength" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."ReportLength" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Groups" IS 'MODE: read';
COMMENT ON COLUMN "Report"."ImagesName" IS 'MODE: read';

CREATE UNIQUE INDEX "Report_unique_code"
  ON "Report"
  USING btree
  ((
CASE
    WHEN "Code"::text = ''::text OR "Status"::text <> 'A'::text THEN NULL::text
    ELSE "Code"::text
END));

---------------------------------------------
-- Create Metadata class
---------------------------------------------

SELECT cm_create_class('Metadata', 'Class', 'MODE: reserved|TYPE: class|DESCR: Metadata|SUPERCLASS: false|STATUS: active');

COMMENT ON COLUMN "Metadata"."Code" IS 'MODE: read|DESCR: Schema|INDEX: 1';
COMMENT ON COLUMN "Metadata"."Description" IS 'MODE: read|DESCR: Key|INDEX: 2';
COMMENT ON COLUMN "Metadata"."Notes" IS 'MODE: read|DESCR: Value|INDEX: 3';

SELECT cm_create_domain('ClassMetadata', 'MODE: reserved|TYPE: domain|CLASS1: Class|CLASS2: Metadata|DESCRDIR: |DESCRINV: |CARDIN: 1:N|STATUS: active');

---------------------------------------------
-- Create Task class
---------------------------------------------

SELECT cm_create_class('_Task', 'Class', 'MODE: reserved|TYPE: class|DESCR: Scheduler|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_Task', 'CronExpression', 'text', null, false, false, 'MODE: write|DESCR: Cron Expression|STATUS: active');
SELECT cm_create_class_attribute('_Task', 'Type', 'text', null, false, false, 'MODE: write|DESCR: Type|STATUS: active');
SELECT cm_create_class_attribute('_Task', 'Running', 'boolean', null, false, false, 'MODE: write|DESCR: Running|STATUS: active');

---------------------------------------------
-- Create Task Parameters class
---------------------------------------------

SELECT cm_create_class('_TaskParameter', 'Class', 'MODE: reserved|TYPE: class|DESCR: Email Accounts|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_TaskParameter', 'Owner', 'int4', null, false, false, 'MODE: write|DESCR: Owner|INDEX: 1|STATUS: active');
SELECT cm_create_class_attribute('_TaskParameter', 'Key', 'text', null, true, false, 'MODE: write|DESCR: Key|INDEX: 2|STATUS: active');
SELECT cm_create_class_attribute('_TaskParameter', 'Value', 'text', null, false, false, 'MODE: write|DESCR: Value|INDEX: 3|STATUS: active');

---------------------------------------------
-- Create Task Runtime class
---------------------------------------------

SELECT cm_create_class('_TaskRuntime', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: _TaskRuntime|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_TaskRuntime', 'Owner', 'integer', null, false, false, 'MODE: write|DESCR: Owner|STATUS: active|FKTARGETCLASS: _Task');
SELECT cm_create_class_attribute('_TaskRuntime', 'LastExecution', 'timestamp', null, false, false, 'MODE: write|DESCR: Last Execution|STATUS: active');

---------------------------------------------
-- Create Data Store Templates class
---------------------------------------------

SELECT cm_create_class('_Templates', NULL, 'DESCR: Templates|MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_Templates', 'Name', 'text', NULL, TRUE, TRUE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Templates', 'Template', 'text', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Create Dashboard class
---------------------------------------------

SELECT cm_create_class('_Dashboards', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_Dashboards', 'Definition', 'text', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Create DomainTreeNavigation class
---------------------------------------------

SELECT cm_create_class('_DomainTreeNavigation', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'IdParent', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'IdGroup', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'Type', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'Description', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'DomainName', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'Direct', 'boolean', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'BaseNode', 'boolean', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'TargetClassName', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'TargetClassDescription', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'TargetFilter', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'EnableRecursion', 'boolean', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
---------------------------------------------
-- Create Layer class
---------------------------------------------

SELECT cm_create_class('_Layer', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_Layer', 'Description', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'FullName', 'character varying', NULL, FALSE, TRUE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'Index', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'MinimumZoom', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'MaximumZoom', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'MapStyle', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'Name', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'GeoServerName', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'Type', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'Visibility', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'CardsBinding', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Create Widget class
---------------------------------------------

SELECT cm_create_class('_Widget', 'Class', 'MODE: reserved|TYPE: class|DESCR: Widget|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_Widget', 'Definition', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Create Views class
---------------------------------------------

SELECT cm_create_class('_View', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_View', 'Name', 'character varying', NULL, TRUE, TRUE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_View', 'Description', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_View', 'Filter', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_View', 'IdSourceClass', 'regclass', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_View', 'SourceFunction', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_View', 'Type', 'character varying', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Filter
---------------------------------------------

SELECT cm_create_class('_Filter', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Filter|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Code', 'varchar', null, true, false, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Description', 'varchar', null, false, false, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'UserId', 'int', null, false, false, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Filter', 'text', null, false, false, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'ClassId', 'regclass', null, true, false, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Shared', 'boolean', 'false', true, false, 'MODE: write|STATUS: active');

ALTER TABLE "_Filter" ADD CONSTRAINT filter_name_table_unique UNIQUE ("Code", "UserId", "ClassId");

SELECT cm_create_domain('FilterRole', 'MODE: reserved|TYPE: domain|CLASS1: _Filter|CLASS2: Role|DESCRDIR: |DESCRINV: |CARDIN: N:N|STATUS: active');


---------------------------------------------
-- Email Accounts
---------------------------------------------

SELECT cm_create_class('_EmailAccount', 'Class', 'MODE: reserved|TYPE: class|DESCR: Email Accounts|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'IsDefault', 'boolean', null, false, false, 'MODE: write|DESCR: Is default|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'Address', 'varchar(100)', null, false, false, 'MODE: write|DESCR: Address|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'Username', 'varchar(100)', null, false, false, 'MODE: write|DESCR: Username|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'Password', 'varchar(100)', null, false, false, 'MODE: write|DESCR: Password|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'SmtpServer', 'varchar(100)', null, false, false, 'MODE: write|DESCR: SMTP server|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'SmtpPort', 'int4', null, false, false, 'MODE: write|DESCR: SMTP port|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'SmtpSsl', 'boolean', null, false, false, 'MODE: write|DESCR: SMTP SSL|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'ImapServer', 'varchar(100)', null, false, false, 'MODE: write|DESCR: IMAP server|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'ImapPort', 'int4', null, false, false, 'MODE: write|DESCR: IMAP port|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'ImapSsl', 'boolean', null, false, false, 'MODE: write|DESCR: IMAP SSL|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'InputFolder', 'varchar(50)', null, false, false, 'MODE: write|DESCR: Input folder|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'ProcessedFolder', 'varchar(50)', null, false, false, 'MODE: write|DESCR: Processed folder|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'RejectedFolder', 'varchar(50)', null, false, false, 'MODE: write|DESCR: Rejected folder|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'RejectNotMatching', 'boolean', null, false, false, 'MODE: write|DESCR: Reject not matching|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'OutputFolder', 'varchar(100)', null, false, false, 'MODE: write|DESCR: Output Folder|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'SmtpStartTls', 'boolean', null, false, false, 'MODE: write|DESCR: SMTP STARTTLS|STATUS: active');
SELECT cm_create_class_attribute('_EmailAccount', 'ImapStartTls', 'boolean', null, false, false, 'MODE: write|DESCR: IMAP STARTTLS|STATUS: active');
SELECT _cm_attribute_set_uniqueness('"_EmailAccount"'::regclass::oid, 'Code', TRUE);

---------------------------------------------
-- Email Templates
---------------------------------------------

SELECT cm_create_class('_EmailTemplate', 'Class', 'MODE: reserved|TYPE: class|DESCR: Email Templates|SUPERCLASS: false|STATUS: active');
SELECT _cm_attribute_set_uniqueness('"_EmailTemplate"'::regclass::oid, 'Code', TRUE);
SELECT cm_create_class_attribute('_EmailTemplate', 'From', 'text', null, false, false, 'MODE: user|DESCR: From|INDEX: 2|STATUS: active');
SELECT cm_create_class_attribute('_EmailTemplate', 'To', 'text', null, false, false, 'MODE: user|DESCR: To|INDEX: 3|STATUS: active');
SELECT cm_create_class_attribute('_EmailTemplate', 'CC', 'text', null, false, false, 'MODE: user|DESCR: Cc|INDEX: 4|STATUS: active');
SELECT cm_create_class_attribute('_EmailTemplate', 'BCC', 'text', null, false, false, 'MODE: user|DESCR: Bcc|INDEX: 5|STATUS: active');
SELECT cm_create_class_attribute('_EmailTemplate', 'Subject', 'text', null, false, false, 'MODE: user|DESCR: Subject|INDEX: 6|STATUS: active');
SELECT cm_create_class_attribute('_EmailTemplate', 'Body', 'text', null, false, false, 'MODE: user|DESCR: Body|INDEX: 7|STATUS: active');

SELECT cm_create_domain('AccountTemplate', 'MODE: reserved|TYPE: domain|CLASS1: _EmailAccount|CLASS2: _EmailTemplate|DESCRDIR: is default|DESCRINV: has default|CARDIN: 1:N|STATUS: active');
SELECT cm_create_class_attribute('_EmailTemplate', 'Account', 'integer', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Account|INDEX: 8|REFERENCEDOM: AccountTemplate|REFERENCEDIRECT: false|REFERENCETYPE: restrict|STATUS: active');

SELECT cm_create_class_attribute('_EmailTemplate', 'KeepSynchronization', 'boolean', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Keep synchronization|INDEX: 9|STATUS: active');
SELECT cm_create_class_attribute('_EmailTemplate', 'PromptSynchronization', 'boolean', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Prompt synchronization|INDEX: 10|STATUS: active');
SELECT cm_create_class_attribute('_EmailTemplate', 'Delay', 'int4', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Delay|INDEX: 11|STATUS: active');

---------------------------------------------
-- Bim Projects
---------------------------------------------

SELECT cm_create_class('_BimProject', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: BIM Project|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_BimProject', 'Code', 'varchar', null, true, false, 'MODE: write|DESCR: Name|INDEX: 1|STATUS: active');
SELECT cm_create_class_attribute('_BimProject', 'Description', 'varchar', null, false, false, 'MODE: write|DESCR: Description|INDEX: 2|STATUS: active');
SELECT cm_create_class_attribute('_BimProject', 'ProjectId', 'varchar', null, true, true, 'MODE: write|DESCR: Project ID|INDEX: 3|STATUS: active');
SELECT cm_create_class_attribute('_BimProject', 'Active', 'boolean', 'TRUE', true, false, 'MODE: write|DESCR: Active|INDEX: 4|STATUS: active');
SELECT cm_create_class_attribute('_BimProject', 'LastCheckin', 'timestamp', null, false, false, 'MODE: write|DESCR: Last Checkin|INDEX: 5|STATUS: active');
SELECT cm_create_class_attribute('_BimProject', 'Synchronized', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Synchronized|INDEX: 6|STATUS: active');
SELECT cm_create_class_attribute('_BimProject', 'ImportMapping', 'text', null, false, false, 'MODE: write|DESCR: ImportMapping|INDEX: 7|STATUS: active');
SELECT cm_create_class_attribute('_BimProject', 'ExportMapping', 'text', null, false, false, 'MODE: write|DESCR: ImportMapping|INDEX: 8|STATUS: active');
SELECT cm_create_class_attribute('_BimProject', 'ExportProjectId', 'varchar', null, false, false, 'MODE: write|DESCR: ExportProjectId|INDEX: 9|STATUS: active');
SELECT cm_create_class_attribute('_BimProject', 'ShapesProjectId', 'varchar', null, false, false, 'MODE: write|DESCR: ExportProjectId|INDEX: 10|STATUS: active');

---------------------------------------------
-- Bim Layers Configuration
---------------------------------------------

SELECT cm_create_class('_BimLayer', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: BIM Project|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_BimLayer', 'ClassName', 'varchar', null, true, true, 'MODE: write|DESCR: ClassName|INDEX: 1|STATUS: active');
SELECT cm_create_class_attribute('_BimLayer', 'Root', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Root|INDEX: 2|STATUS: active');
SELECT cm_create_class_attribute('_BimLayer', 'Active', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Active|INDEX: 3|STATUS: active');
SELECT cm_create_class_attribute('_BimLayer', 'Export', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Export|INDEX: 4|STATUS: active');
SELECT cm_create_class_attribute('_BimLayer', 'Container', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Container|INDEX: 5|STATUS: active');
SELECT cm_create_class_attribute('_BimLayer', 'RootReference', 'varchar', null, false, false, 'MODE: write|DESCR: RootReference|INDEX: 6|STATUS: active');

---------------------------------------------
-- Translations
---------------------------------------------

SELECT cm_create_class('_Translation', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Translations|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_Translation', 'Element', 'text', null, true, false, 'MODE: write|DESCR: Element|INDEX: 1|STATUS: active');
SELECT cm_create_class_attribute('_Translation', 'Lang', 'text', null, true, false, 'MODE: write|DESCR: Lang|INDEX: 2|STATUS: active');
SELECT cm_create_class_attribute('_Translation', 'Value', 'text', null, true, false, 'MODE: write|DESCR: Value|INDEX: 3|STATUS: active');

---------------------------------------------
-- EmailStatus lookup
---------------------------------------------
INSERT INTO "LookUp" ("IdClass", "Code", "Description", "Status", "Type", "Number", "IsDefault")
    VALUES ('"LookUp"'::regclass, 'New', 'New', 'A', 'EmailStatus', 1, false);
INSERT INTO "LookUp" ("IdClass", "Code", "Description", "Status", "Type", "Number", "IsDefault")
    VALUES ('"LookUp"'::regclass, 'Received', 'Received', 'A', 'EmailStatus', 2, false);
INSERT INTO "LookUp" ("IdClass", "Code", "Description", "Status", "Type", "Number", "IsDefault")
    VALUES ('"LookUp"'::regclass, 'Draft', 'Draft', 'A', 'EmailStatus', 3, false);
INSERT INTO "LookUp" ("IdClass", "Code", "Description", "Status", "Type", "Number", "IsDefault")
    VALUES ('"LookUp"'::regclass, 'Outgoing', 'Outgoing', 'A', 'EmailStatus', 4, false);
INSERT INTO "LookUp" ("IdClass", "Code", "Description", "Status", "Type", "Number", "IsDefault")
    VALUES ('"LookUp"'::regclass, 'Sent', 'Sent', 'A', 'EmailStatus', 5, false);

---------------------------------------------
-- Email (class only)
---------------------------------------------
SELECT cm_create_class('Email', 'Class', 'MODE: reserved|TYPE: class|DESCR: Email|SUPERCLASS: false|STATUS: active');

---------------------------------------------
-- ClassEmail
---------------------------------------------
SELECT cm_create_domain('ClassEmail', 'MODE: reserved|TYPE: domain|CLASS1: Class|CLASS2: Email|DESCRDIR: |DESCRINV: |CARDIN: 1:N|STATUS: active');

---------------------------------------------
-- Email (attributes)
---------------------------------------------
SELECT cm_create_class_attribute('Email', 'Card', 'integer', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Card|INDEX: 4|REFERENCEDOM: ClassEmail|REFERENCEDIRECT: false|REFERENCETYPE: restrict|STATUS: active');
SELECT cm_create_class_attribute('Email', 'EmailStatus', 'integer', null, true, false, 'MODE: user|FIELDMODE: write|DESCR: EmailStatus|INDEX: 5|LOOKUP: EmailStatus|STATUS: active');
SELECT cm_create_class_attribute('Email', 'FromAddress', 'text', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: From|INDEX: 6|STATUS: active');
SELECT cm_create_class_attribute('Email', 'ToAddresses', 'text', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: To|INDEX: 7|STATUS: active');
SELECT cm_create_class_attribute('Email', 'CcAddresses', 'text', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Cc|INDEX: 8|STATUS: active');
SELECT cm_create_class_attribute('Email', 'BccAddresses', 'text', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Bcc|INDEX: 9|STATUS: active');
SELECT cm_create_class_attribute('Email', 'Subject', 'text', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Subject|INDEX: 10|STATUS: active');
SELECT cm_create_class_attribute('Email', 'Content', 'text', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Body|INDEX: 11|STATUS: active');
SELECT cm_create_class_attribute('Email', 'NotifyWith', 'text', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: NotifyWith|INDEX: 12|STATUS: active');
SELECT cm_create_class_attribute('Email', 'NoSubjectPrefix', 'boolean', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: No subject prefix|INDEX: 13|STATUS: active');
SELECT cm_create_class_attribute('Email', 'Account', 'text', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Account|INDEX: 14|STATUS: active');
SELECT cm_create_class_attribute('Email', 'Template', 'text', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Template|INDEX: 15|STATUS: active');
SELECT cm_create_class_attribute('Email', 'KeepSynchronization', 'boolean', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Keep synchronization|INDEX: 16|STATUS: active');
SELECT cm_create_class_attribute('Email', 'PromptSynchronization', 'boolean', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Prompt synchronization|INDEX: 17|STATUS: active');
SELECT cm_create_class_attribute('Email', 'Delay', 'int4', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Delay|INDEX: 18|STATUS: active');

---------------------------------------------
-- Custom Pages
---------------------------------------------
SELECT cm_create_class('_CustomPage', 'Class', 'MODE: reserved|TYPE: class|DESCR: CustomPage|SUPERCLASS: false|STATUS: active');

---------------------------------------------
-- Icons
---------------------------------------------
SELECT cm_create_class('_Icon', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: _Icon|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_Icon', 'Element', 'text', null, true, true, 'MODE: write|DESCR: Element|STATUS: active');
SELECT cm_create_class_attribute('_Icon', 'Path', 'text', null, true, false, 'MODE: write|DESCR: Path|STATUS: active');