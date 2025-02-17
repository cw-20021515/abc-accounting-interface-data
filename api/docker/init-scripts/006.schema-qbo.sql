CREATE TABLE IF NOT EXISTS "qbo_company"
(
    "code"        varchar(255) not null primary key,
    "name"        varchar(255),
    "description" text,
    "country"     varchar(255),
    "currency"    varchar(255),
    "eng_name"    varchar(255),
    "kor_name"    varchar(255),
    "fiscal_year"    integer,
    "fiscal_month"    integer,
    "create_time" timestamp(6) with time zone,
    "update_time" timestamp(6) with time zone,
    "is_active"   char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
);

CREATE TABLE IF NOT EXISTS "qbo_credential"
(
    "realm_id"                  varchar not null primary key,
    "company_code"              varchar,
    "active_profile"            varchar,
    "target_name"               varchar,
    "client_id"                 varchar,
    "client_secret"             varchar,
    "scope"                     varchar,
    "access_token"              text,
    "refresh_token"             text,
    "id_token"                  text,
    "token_type"                varchar,
    "access_token_issued_time"  timestamp(6) with time zone,
    "access_token_expire_time"  timestamp(6) with time zone,
    "refresh_token_issued_time" timestamp(6) with time zone,
    "refresh_token_expire_time" timestamp(6) with time zone,
    "basic_token"               text,
    "sub"                       varchar,
    "given_name"                varchar,
    "email"                     varchar,
    "create_time"               timestamp(6) with time zone,
    "update_time"               timestamp(6) with time zone,
    "is_active"   char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
);

CREATE TABLE IF NOT EXISTS "qbo_account"
(
    "qbo_id"       varchar(255),
    "display_name"    varchar(255),
    "account_code"    varchar(255),
    "account_name"    varchar(255),
    "realm_id"        varchar(255),
    "company_code"    varchar(255),
    "sync_token"      varchar(255),
    "submit_result"        json,
    "create_time"     timestamp(6) with time zone,
   "update_time"     timestamp(6) with time zone,
   "is_active"       char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_qbo_account_id PRIMARY KEY (qbo_id, account_code,account_name)
    );

CREATE TABLE IF NOT EXISTS "qbo_customer"
(
    "qbo_id"               varchar,
    "customer_id"          varchar,
    "company_code"         varchar,
    "customer_type"        varchar,
    "customer_status"      varchar,
    "channel_type"         varchar,
    "title_name"           varchar,
    "first_name"           varchar,
    "middle_name"          varchar,
    "last_name"            varchar,
    "family_name"          varchar,
    "name_suffix"          varchar,
    "fully_qualified_name" varchar,
    "company_name"         varchar,
    "display_name"         varchar,
    "user_id"              varchar,
    "primary_phone"        varchar,
    "alternate_phone"      varchar,
    "mobile"               varchar,
    "fax"                  varchar,
    "primary_email"        varchar,
    "alternate_email"      varchar,
    "web_addr"             varchar,
    "sync_token"           varchar(255),
    "submit_result"        json,
    "create_time"          timestamp(6) with time zone,
    "update_time"          timestamp(6) with time zone,
    "is_active"            char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_qbo_customer_id PRIMARY KEY (qbo_id, customer_id,company_code)
    );

CREATE TABLE IF NOT EXISTS "qbo_journal_entry"
(
    "qbo_id"                    varchar(255),
    "doc_id"                    varchar(255),
    "doc_hash"                  varchar(32),
    "company_code"              varchar(255),
    "sync_token"                varchar(255),
    "rounding_difference"       NUMERIC(38,2),
    "posting_date"              DATE,
    "submit_result"             json,
    "create_time"               timestamp(6) with time zone,
   "update_time"                timestamp(6) with time zone,
   "is_active"                  char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_qbo_journal_entry_id PRIMARY KEY (qbo_id, doc_id,company_code)
);


CREATE TABLE IF NOT EXISTS "qbo_item"
(
    "qbo_id"                varchar(255),
    "company_code"          varchar(255),
    "material_id"           varchar(255),
    "display_name"          varchar(255),
    "template_id"           varchar(255),
    "create_category"       varchar(255),
    "create_type"           varchar(255),
    "associated_type"       varchar(255),
    "asset_account_code"    varchar(255),
    "asset_account_name"    varchar(255),
    "income_account_code"   varchar(255),
    "income_account_name"   varchar(255),
    "expense_account_code"  varchar(255),
    "expense_account_name"  varchar(255),
    "document_type"         varchar(255),
    "management_unit"       varchar(255),
    "description"           varchar(255),
    "unit_price"            NUMERIC(38,2),
    "rate_percent"          NUMERIC(38,2),
    "sync_token"            varchar(255),
    "submit_result"        json,
    "create_time"           timestamp(6) with time zone,
    "update_time"           timestamp(6) with time zone,
    "is_active"             char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_qbo_item_id PRIMARY KEY (qbo_id, company_code)
    );

CREATE TABLE IF NOT EXISTS "qbo_class"
(
    "qbo_id"                varchar(255),
    "class_id"              varchar(255),
    "company_code"          varchar(255),
    "code"                  varchar(255),
    "type"                  varchar(255),
    "submit_result"         json,
    "create_time"           timestamp(6) with time zone,
    "update_time"           timestamp(6) with time zone,
    "is_active"             char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_qbo_classid PRIMARY KEY (qbo_id, class_id,company_code)
);

CREATE TABLE IF NOT EXISTS "qbo_vendor"
(
    "qbo_id"                varchar(255),
    "vendor_id"              varchar(255),
    "company_code"          varchar(255),
    "type"                  varchar(255),
    "submit_result"         json,
    "create_time"           timestamp(6) with time zone,
    "update_time"           timestamp(6) with time zone,
    "is_active"             char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_qbo_vendorid PRIMARY KEY (qbo_id, vendor_id,company_code)
);

CREATE TABLE IF NOT EXISTS "qbo_department"
(
    "qbo_id"                varchar(255),
    "company_code"          varchar(255),
    "name"                   varchar(255),
    "branch_id"              varchar(255),
    "warehouse_id"              varchar(255),
    "type"                  varchar(255),
    "submit_result"         json,
    "create_time"           timestamp(6) with time zone,
     "update_time"           timestamp(6) with time zone,
     "is_active"             char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_qbo_departmentid PRIMARY KEY (qbo_id, company_code)
);



CREATE TABLE IF NOT EXISTS "qbo_item_template"
(
    "template_id"          varchar not null primary key,
    "company_code"         varchar,
    "create_category"      varchar,
    "create_type"          varchar,
    "associated_type"      varchar,
    "asset_account_code"   varchar,
    "asset_account_name"   varchar,
    "expense_account_code" varchar,
    "expense_account_name" varchar,
    "income_account_code"  varchar,
    "income_account_name"  varchar,
    "management_unit"      varchar,
    "document_type"        varchar
);
