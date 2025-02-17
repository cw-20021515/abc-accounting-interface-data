CREATE TABLE IF NOT EXISTS "company"
(
    "code"                  varchar(255) not null primary key,
    "name"                  varchar(255),
    "description"           text,
    "country"               varchar(255),
    "currency"              varchar(255),
    "eng_name"              varchar(255),
    "kor_name"              varchar(255),
    "fiscal_year"           varchar(255),
    "fiscal_month"          varchar(255),
    "create_time"           timestamp(6) with time zone,
    "update_time"           timestamp(6) with time zone,
    "is_active"               char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
);

CREATE TABLE IF NOT EXISTS "credential"
(
    "realm_id"                  varchar not null primary key,
    "company_code"              varchar,
    "active_profile"            varchar,
    "target_name"               varchar,
    "client_id"                 varchar,
    "client_secret"             varchar,
    "scope"                     varchar,
    "email"                     varchar,
    "given_name"                varchar,
    "sub"                       varchar,
    "token_type"                varchar,
    "access_token"              text,
    "basic_token"               text,
    "id_token"                  text,
    "refresh_token"             text,
    "access_token_expire_time"  timestamp(6) with time zone,
    "access_token_issued_time"  timestamp(6) with time zone,
    "refresh_token_expire_time" timestamp(6) with time zone,
    "refresh_token_issued_time" timestamp(6) with time zone,
    "create_time"               timestamp(6) with time zone,
    "update_time"               timestamp(6) with time zone,
    "is_active"                 char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
);

CREATE TABLE IF NOT EXISTS "item_create_template"
(
    "template_id"          varchar not null primary key,
    "company_code"         varchar,
    "asset_account_code"   varchar,
    "asset_account_name"   varchar,
    "associated_type"      varchar,
    "create_category"      varchar,
    "create_type"          varchar,
    "document_type"        varchar,
    "expense_account_code" varchar,
    "expense_account_name" varchar,
    "income_account_code"  varchar,
    "income_account_name"  varchar,
    "management_unit"      varchar
);

-- CREATE TABLE IF NOT EXISTS "associated_item"
-- (
--     "itemId"                varchar not null primary key,
--     "template_id"           varchar,
--     "company_code"          varchar,
--     "association_map_id"    varchar,
--     "material_id"           varchar,
--     "description"           varchar,
--     "rate_percent"          NUMERIC(38,2),
--     "unit_price"            NUMERIC(38,2),
--     "create_time"           timestamp(6) with time zone,
--     "update_time"           timestamp(6) with time zone,
--     "is_active"             char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
-- );

-- CREATE TABLE IF NOT EXISTS "association_map"
-- (
--     "association_id"        varchar(255) not null primary key,
--     "associated_entity"     varchar(255),
--     "associated_id"         varchar(255),
--     "associated_name"       varchar(255),
--     "associated_type"       varchar(255),
--     "associated_value"      varchar(255),
--     "company_code"          varchar(255),
--     "realm_id"              varchar(255),
--     "sandbox_name"          varchar(255),
--     "sync_token"            varchar(255),
--     "create_time"           timestamp(6) with time zone,
--     "update_time"           timestamp(6) with time zone,
--     "is_active"             char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
-- );