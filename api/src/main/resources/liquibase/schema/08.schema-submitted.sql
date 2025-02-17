CREATE TABLE IF NOT EXISTS "submitted_account"
(
    "submit_id"       varchar(255),
    "account_code"    varchar(255),
    "account_name"    varchar(255),
    "realm_id"        varchar(255),
    "company_code"    varchar(255),
    "sync_token"      varchar(255),
    "create_time"     timestamp(6) with time zone,
    "update_time"     timestamp(6) with time zone,
    "is_active"       char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_submitted_account_id PRIMARY KEY (submit_id, account_code,account_name)
);

CREATE TABLE IF NOT EXISTS "submitted_customer"
(
    "submit_id"            varchar,
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
    "create_time"          timestamp(6) with time zone,
    "update_time"          timestamp(6) with time zone,
    "is_active"            char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_submitted_customer_id PRIMARY KEY (submit_id, customer_id,company_code)
);

CREATE TABLE IF NOT EXISTS "submitted_item"
(
    "submit_id"             varchar(255),
    "material_id"           varchar(255),
    "company_code"          varchar(255),
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
    "create_time"           timestamp(6) with time zone,
    "update_time"           timestamp(6) with time zone,
    "is_active"             char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_submitted_item_id PRIMARY KEY (submit_id, material_id,company_code)
);

CREATE TABLE IF NOT EXISTS "submitted_journal_entry"
(
    "submit_id"       varchar(255),
    "doc_id"          varchar(255),
    "company_code"    varchar(255),
    "sync_token"      varchar(255),
    "create_time"     timestamp(6) with time zone,
    "update_time"     timestamp(6) with time zone,
    "is_active"       char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    CONSTRAINT pk_submitted_journal_entry_id PRIMARY KEY (submit_id, doc_id,company_code)
);