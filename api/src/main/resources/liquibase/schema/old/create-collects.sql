CREATE TABLE IF NOT EXISTS "audit_target_entity"
(
    "id"                      bigserial primary key,
    "audit_action_type"       varchar,
    "entity_name"             varchar not null,
    "company_id"              varchar not null,
    "is_active"               char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
);


CREATE TABLE IF NOT EXISTS "audit_entity_log"
(
    "id"               bigserial primary key,
    "action_type"      varchar not null,
    "company_id"       varchar not null,
    "entity_id"        varchar not null,
    "event_table_id"   varchar not null,
    "event_table_name" varchar not null,
    "processed"        boolean      not null,
    "timestamp"        timestamp(6) not null
);


CREATE TABLE IF NOT EXISTS "collect_channel"
(
    "hash_code"      varchar not null primary key,
    "is_active"      char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "create_time"    timestamp(6) with time zone,
    "update_time"    timestamp(6) with time zone,
    "channel_detail" varchar,
    "channel_id"     varchar,
    "channel_type"   varchar,
    "entity"         varchar,
    "field"          varchar,
    "value"          varchar
);

CREATE TABLE IF NOT EXISTS "collect_charge"
(
    "hash_code"     varchar not null primary key,
    "charge_id"     varchar,
    "billing_cycle" integer,
    "end_date"      date,
    "is_active"     char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "start_date"    date,
    "total_price"   NUMERIC(38,2),
    "create_time"   timestamp(6) with time zone,
    "update_time"   timestamp(6) with time zone,
    "charge_status" varchar,
    "contract_id"   varchar,
    "receipt_id"    varchar,
    "target_month"  varchar
);

CREATE TABLE IF NOT EXISTS "collect_charge_item"
(
    "hash_code"        varchar not null primary key,
    "charge_id"        varchar,
    "charge_item_id"   varchar,
    "is_active"        char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "is_tax_exempt"    boolean,
    "quantity"         integer,
    "total_price"      NUMERIC(38,2),
    "create_time"      timestamp(6) with time zone,
    "update_time"      timestamp(6) with time zone,
    "charge_item_type" varchar,
    "entity"           varchar,
    "field"            varchar,
    "receipt_id"       varchar,
    "service_flow_id"  varchar,
    "value"            varchar
);


CREATE TABLE IF NOT EXISTS "collect_location"
(
    "hash_code"            varchar not null primary key,
    "is_active"            char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "entity"               varchar,
    "field"                varchar,
    "value"                varchar,
    "branch_id"            varchar,
    "warehouse_id"         varchar,
    "latitude"             double precision,
    "longitude"            double precision,
    "create_time"          timestamp(6) with time zone,
    "update_time"          timestamp(6) with time zone,
    "address1"             varchar,
    "address2"             varchar,
    "alternate_email"      varchar,
    "alternate_phone"      varchar,
    "city"                 varchar,
    "company_name"         varchar,
    "country"              varchar,
    "country_code"         varchar,
    "county"               varchar,
    "display_name"         varchar,
    "family_name"          varchar,
    "fax"                  varchar,
    "first_name"           varchar,
    "fully_qualified_name" varchar,
    "last_name"            varchar,
    "location_remark"      varchar,
    "middle_name"          varchar,
    "mobile"               varchar,
    "name_suffix"          varchar,
    "primary_email"        varchar,
    "primary_phone"        varchar,
    "state"                varchar,
    "title_name"           varchar,
    "user_id"              varchar,
    "web_addr"             varchar,
    "zip_code"             varchar
);

CREATE TABLE IF NOT EXISTS "collect_contract"
(
    "hash_code"             varchar not null primary key,
    "entity"                varchar,
    "field"                 varchar,
    "value"                 varchar,
    "duration_in_months"    integer,
    "end_date"              date,
    "is_active"             char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "is_signed"             boolean,
    "revision"              integer,
    "start_date"            date,
    "create_time"           timestamp(6) with time zone,
    "signed_time"           timestamp(6) with time zone,
    "update_time"           timestamp(6) with time zone,
    "channel_contract_id"   varchar,
    "channel_order_item_id" varchar,
    "contract_id"           varchar,
    "contract_status"       varchar,
    "customer_id"           varchar,
    "form_id"               varchar,
    "material_id"           varchar,
    "order_id"              varchar,
    "order_item_id"         varchar,
    "rental_code"           varchar
);


CREATE TABLE IF NOT EXISTS "collect_customer"
(
    "hash_code"            varchar not null primary key,
    "entity"               varchar,
    "field"                varchar,
    "value"                varchar,
    "currency"             varchar(3),
    "is_active"            char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "is_member"            char not null check (is_member = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "is_tax_liability"     char not null check (is_tax_liability = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "create_time"          timestamp(6) with time zone,
    "update_time"          timestamp(6) with time zone,
    "alternate_email"      varchar,
    "alternate_phone"      varchar,
    "channel_customer_id"  varchar,
    "channel_type"         varchar,
    "company_name"         varchar,
    "customer_id"          varchar,
    "order_item_id"        varchar,
    "customer_status"      varchar,
    "customer_type"        varchar,
    "display_name"         varchar,
    "family_name"          varchar,
    "fax"                  varchar,
    "first_name"           varchar,
    "fully_qualified_name" varchar,
    "installation_id"      varchar,
    "last_name"            varchar,
    "middle_name"          varchar,
    "mobile"               varchar,
    "name_suffix"          varchar,
    "primary_email"        varchar,
    "primary_phone"        varchar,
    "title_name"           varchar,
    "user_id"              varchar,
    "web_addr"             varchar
);

CREATE TABLE IF NOT EXISTS "collect_bank_reconcil"
(
    "hash_code"                varchar not null primary key,
    "create_time"              timestamp(6) with time zone,
    "update_time"              timestamp(6) with time zone,
    "is_active"                char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "reconcil_id"              varchar,
    "reconcil_line_number"     integer not null,
    "currency"                 varchar,
    "reconcil_to_account_code" varchar,
    "total_amount"             NUMERIC(38,2),
    "line_amount"              NUMERIC(38,2),
    "detail_type"              varchar,
    "payment_method"           varchar,
    "account_code"             varchar,
    "entity_name"              varchar,
    "entity_value"             varchar,
    "txn_id"                   varchar,
    "txn_type"                 varchar,
    "txn_date"                 date,
    "tax_code"                 varchar,
    "total_tax"                NUMERIC(38,2),
    "private_note"             varchar
);


CREATE TABLE IF NOT EXISTS "collect_installation"
(
    "hash_code"            varchar not null primary key,
    "is_active"            char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "latitude"             double precision,
    "longitude"            double precision,
    "create_time"          timestamp(6) with time zone,
    "installation_time"    timestamp(6) with time zone,
    "update_time"          timestamp(6) with time zone,
    "warranty_end_time"    timestamp(6) with time zone,
    "warranty_start_time"  timestamp(6) with time zone,
    "address1"             varchar,
    "address2"             varchar,
    "alternate_email"      varchar,
    "alternate_phone"      varchar,
    "branch_id"            varchar,
    "city"                 varchar,
    "company_name"         varchar,
    "country"              varchar,
    "country_code"         varchar,
    "county"               varchar,
    "display_name"         varchar,
    "entity"               varchar,
    "family_name"          varchar,
    "fax"                  varchar,
    "field"                varchar,
    "first_name"           varchar,
    "fully_qualified_name" varchar,
    "install_id"           varchar,
    "last_name"            varchar,
    "location_remark"      varchar,
    "middle_name"          varchar,
    "mobile"               varchar,
    "name_suffix"          varchar,
    "order_item_id"        varchar,
    "primary_email"        varchar,
    "primary_phone"        varchar,
    "serial_number"        varchar,
    "service_flow_id"      varchar,
    "state"                varchar,
    "technician_id"        varchar,
    "title_name"           varchar,
    "user_id"              varchar,
    "value"                varchar,
    "warehouse_id"         varchar,
    "water_type"           varchar,
    "web_addr"             varchar,
    "zip_code"             varchar
);

CREATE TABLE IF NOT EXISTS "collect_material"
(
    "hash_code"              varchar not null primary key,
    "is_active"              char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "create_time"            timestamp(6) with time zone,
    "update_time"            timestamp(6) with time zone,
    "description"            varchar,
    "entity"                 varchar,
    "feature_code"           varchar,
    "field"                  varchar,
    "filter_type"            varchar,
    "installation_type"      varchar,
    "material_brand_name"    varchar,
    "material_category_code" varchar,
    "material_id"            varchar not null,
    "material_model_name"    varchar,
    "material_name"          varchar,
    "material_series_code"   varchar,
    "material_type"          varchar,
    "product_type"           varchar,
    "value"                  varchar
);
CREATE TABLE IF NOT EXISTS "collect_order"
(
    "hash_code"          varchar not null primary key,
    "is_active"          char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "create_time"        timestamp(6) with time zone,
    "order_create_time"  timestamp(6) with time zone,
    "order_update_time"  timestamp(6) with time zone,
    "update_time"        timestamp(6) with time zone,
    "channel_id"         varchar,
    "channel_order_id"   varchar,
    "customer_id"        varchar,
    "entity"             varchar,
    "field"              varchar,
    "order_id"           varchar,
    "order_product_type" varchar,
    "payment_id"         varchar,
    "referrer_code"      varchar,
    "value"              varchar
);


CREATE TABLE IF NOT EXISTS "collect_order_item"
(
    "hash_code"             varchar not null primary key,
    "discount_price"        NUMERIC(38,2),
    "is_active"             char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "item_price"            NUMERIC(38,2),
    "quantity"              integer,
    "registration_price"    NUMERIC(38,2),
    "tax"                   NUMERIC(38,2),
    "total_price"           NUMERIC(38,2),
    "create_time"           timestamp(6) with time zone,
    "update_time"           timestamp(6) with time zone,
    "channel_order_id"      varchar,
    "channel_order_item_id" varchar,
    "contract_id"           varchar,
    "currency"              varchar,
    "entity"                varchar,
    "field"                 varchar,
    "install_id"            varchar,
    "material_id"           varchar,
    "order_id"              varchar,
    "order_item_id"         varchar,
    "order_item_status"     varchar,
    "order_item_type"       varchar,
    "value"                 varchar
);

CREATE TABLE IF NOT EXISTS "collect_receipt"
(
    "hash_code"                varchar not null primary key,
    "entity"                   varchar,
    "field"                    varchar,
    "value"                    varchar,
    "create_time"              timestamp(6) with time zone,
    "update_time"              timestamp(6) with time zone,
    "receipt_id"               varchar,
    "charge_id"                varchar,
    "deposit_id"               varchar,
    "transaction_id"           varchar,
    "discount_price"           NUMERIC(38,2),
    "installment_months"       integer,
    "item_monthly_price"       NUMERIC(38,2),
    "item_price"               NUMERIC(38,2),
    "monthly_discount_price"   NUMERIC(38,2),
    "monthly_tax"              NUMERIC(38,2),
    "monthly_total_price"      NUMERIC(38,2),
    "registration_price"       NUMERIC(38,2),
    "subscription_receipt_day" integer,
    "tax"                      NUMERIC(38,2),
    "total_price"              NUMERIC(38,2),
    "receipt_time"             timestamp(6) with time zone,
    "billing_type"             varchar,
    "card_number"              varchar,
    "card_type"                varchar,
    "currency"                 varchar,
    "receipt_method"           varchar,
    "is_active"                char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
);


CREATE TABLE IF NOT EXISTS "collect_deposit"
(
    "hash_code"                       varchar not null primary key,
    "create_time"                     timestamp(6) with time zone,
    "update_time"                     timestamp(6) with time zone,
    "is_active"                       char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "entity"                          varchar,
    "field"                           varchar,
    "value"                           varchar,
    "transaction_id"                  varchar,
    "deposit_id"                      varchar,
    "currency"                        varchar,
    "deposit_date"                    date,
    "amount"                          varchar,
    "adjustments_fee_amount"          varchar,
    "adjustments_gross_amount"        varchar,
    "charges_fee_amount"              varchar,
    "charges_gross_amount"            varchar,
    "refunds_fee_amount"              varchar,
    "refunds_gross_amount"            varchar,
    "reserved_funds_fee_amount"       varchar,
    "reserved_funds_gross_amount"     varchar,
    "retried_deposits_fee_amount"     varchar,
    "retried_deposits_gross_aamount"  varchar,
    "sales_fee_amount"                varchar,
    "sales_gross_amount"              varchar,
    "fees"                            varchar,
    "gross"                           varchar,
    "net"                             varchar
);

-- CREATE TABLE IF NOT EXISTS "collect_price"
-- (
--     "hash_code"      varchar not null primary key,
--     "create_time"    timestamp(6) with time zone,
--     "update_time"    timestamp(6) with time zone,
--     "is_active"      char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
--     "entity"         varchar,
--     "field"          varchar,
--     "value"          varchar,
--     "discount_price" NUMERIC(38,2),
--     "item_price"     NUMERIC(38,2),
--     "prepaid_amount" NUMERIC(38,2),
--     "tax"            NUMERIC(38,2),
--     "currency"       varchar
-- );

CREATE TABLE IF NOT EXISTS "collect_service_flow"
(
    "is_active"                  char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "cancel_time"                timestamp(6) with time zone,
    "create_time"                timestamp(6) with time zone,
    "update_time"                timestamp(6) with time zone,
    "billing_id"                 varchar,
    "customer_service_id"        varchar,
    "customer_service_ticket_id" varchar,
    "hash_code"                  varchar not null primary key,
    "order_id"                   varchar,
    "order_item_id"              varchar,
    "service_flow_id"            varchar,
    "service_status"             varchar,
    "service_type"               varchar,
    "work_id"                    varchar
);

CREATE TABLE IF NOT EXISTS "collect_tax_line"
(
    "hash_code"   varchar not null primary key,
    "is_active"   char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "price"       NUMERIC(38,2),
    "rate"        NUMERIC(38,2),
    "create_time" timestamp(6) with time zone,
    "update_time" timestamp(6) with time zone,
    "entity"      varchar,
    "field"       varchar,
    "title"       varchar,
    "value"       varchar
);

CREATE TABLE IF NOT EXISTS "collect_vendor"
(
    "hash_code"            varchar not null primary key,
    "is_active"           char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "latitude"             double precision,
    "longitude"            double precision,
    "create_time"          timestamp(6) with time zone,
    "update_time"          timestamp(6) with time zone,
    "acct_num"             varchar,
    "address1"             varchar,
    "address2"             varchar,
    "alternate_email"      varchar,
    "alternate_phone"      varchar,
    "branch_id"            varchar,
    "business_number"      varchar,
    "city"                 varchar,
    "company_id"           varchar,
    "company_name"         varchar,
    "country"              varchar,
    "country_code"         varchar,
    "county"               varchar,
    "currency"             varchar,
    "department_code"      varchar,
    "department_name"      varchar,
    "description"          varchar,
    "display_name"         varchar,
    "employer_code"        varchar,
    "entity"               varchar,
    "family_name"          varchar,
    "fax"                  varchar,
    "field"                varchar,
    "first_name"           varchar,
    "fully_qualified_name" varchar,
    "last_name"            varchar,
    "location_remark"      varchar,
    "middle_name"          varchar,
    "mobile"               varchar,
    "name_suffix"          varchar,
    "primary_email"        varchar,
    "primary_phone"        varchar,
    "remark"               varchar,
    "state"                varchar,
    "tax_identifier"       varchar,
    "terms"                varchar,
    "title_name"           varchar,
    "user_id"              varchar,
    "value"                varchar,
    "vendor_id"            varchar,
    "warehouse_id"         varchar,
    "web_addr"             varchar,
    "zip_code"             varchar
);


CREATE TABLE IF NOT EXISTS "inventory_valuation_entity"
(
   "id"                    bigint not null PRIMARY KEY,
   "base_time"             timestamp(6) with time zone,
   "currency"              varchar(3),
   "entry_id"              varchar,
   "grade_type"            varchar,
   "issued_time"           timestamp(6) with time zone,
   "material_id"           varchar,
   "material_name"         varchar,
   "material_product_type" varchar,
   "model_name"            varchar,
   "movement_type"         varchar ,
   "moving_avg_method"     varchar,
   "record_time"           timestamp(6) with time zone,
   "remark"                varchar,
   "stock_avg_unit_price"  numeric(38, 8),
   "trigger_entity_id"     varchar
);