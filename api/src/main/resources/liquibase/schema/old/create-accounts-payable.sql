CREATE TABLE IF NOT EXISTS "accounts_payable"
(
    "id" VARCHAR(255) PRIMARY KEY,
    "tx_id" VARCHAR(255),
    "accounting_id" VARCHAR(255),
    "remark" VARCHAR(255),
    "title" VARCHAR(255),
    "description" VARCHAR(255),
    "payment_id" VARCHAR(255),
    "payment_type" VARCHAR(255),
    "transaction_type" VARCHAR(255),
    "payment_date_time" TIMESTAMPTZ,
    "payment_sub_total_amount" DOUBLE PRECISION,
    "payment_total_amount" DOUBLE PRECISION,
    "payment_status" VARCHAR(255),
    "payment_blocking_reason" VARCHAR(255),
    "payment_retry" INT,
    "payment_currency" VARCHAR(255),
    "payment_balance" INT,
    "payout_amount" DOUBLE PRECISION,
    "tax_amount" DOUBLE PRECISION,
    "local_currency" VARCHAR(255),
    "local_amount" DOUBLE PRECISION,
    "create_time" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    "document_time" TIMESTAMPTZ,
    "entry_time" TIMESTAMPTZ,
    "posting_time" TIMESTAMPTZ,
    "process_time" TIMESTAMPTZ,
    "due_time" TIMESTAMPTZ,
    "is_expired" CHAR(1) DEFAULT 'N',
    "is_completed" CHAR(1) DEFAULT 'N',
    "supplier_id" VARCHAR(255),
    "customer_id" VARCHAR(255),
    "drafter_id" VARCHAR(255),
    "cost_center" VARCHAR(255),
    "invoice_id" VARCHAR(255),
    "purchase_order_id" VARCHAR(255),
    "bill_of_lading_id" VARCHAR(255),
    "approval_tx_id" VARCHAR(255),
    "attachments_tx_id" VARCHAR(255),
    "company_id" VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS "accounts_payable_item"
(
    "id" VARCHAR(255) PRIMARY KEY,
    "account_code" VARCHAR(255),
    "account_name" VARCHAR(255),
    "amount" DOUBLE PRECISION,
    "budget_allocation" VARCHAR(255),
    "budget_amount" DOUBLE PRECISION,
    "budget_usage_time" TIMESTAMPTZ,
    "cost_center" VARCHAR(255),
    "description" VARCHAR(255),
    "line_number" VARCHAR(255),
    "material_category_code" VARCHAR(255),
    "material_id" VARCHAR(255),
    "material_name" VARCHAR(255),
    "material_type" VARCHAR(255),
    "name" VARCHAR(255),
    "payout_case_type" VARCHAR(255),
    "posting_key" VARCHAR(255),
    "quantity" INT,
    "remark" VARCHAR(255),
    "tax" DOUBLE PRECISION,
    "tx_id" VARCHAR(255),
    "unit_measure" VARCHAR(255),
    "unit_price" DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS "payout_attachment"
(
    "id"                 varchar not null primary key,
    "tx_id"              varchar,
    "resource_path"      varchar,
    "resource_size"      bigint,
    "mime_type"          varchar,
    "origin_file_name"   varchar,
    "modified_file_name" varchar,
    "remark"             varchar,
    "is_deleted"         char check (is_deleted = any (array['N', 'Y'])),
    "expire_datetime"    timestamp(6) with time zone,
    "create_datetime"    timestamp(6) with time zone
);

CREATE TABLE IF NOT EXISTS "cost_center_info"
(
    "id" VARCHAR NOT NULL PRIMARY KEY,
    "account_type" VARCHAR,
    "center_id" VARCHAR,
    "center_name" VARCHAR,
    "center_sub_type" VARCHAR,
    "center_type" VARCHAR,
    "company_id" VARCHAR,
    "create_time" TIMESTAMPTZ,
    "description" VARCHAR,
    "is_active" CHAR,
    "parent_center_id" VARCHAR,
    "system_source" VARCHAR
);

CREATE TABLE IF NOT EXISTS accounts_account_info (
    "id" VARCHAR NOT NULL PRIMARY KEY,
    "account_code" VARCHAR,
    "account_description" VARCHAR,
    "account_name" VARCHAR,
    "account_type" VARCHAR,
    "classification" VARCHAR,
    "create_time" TIMESTAMPTZ,
    "group_code" VARCHAR,
    "group_description" VARCHAR,
    "group_name" VARCHAR,
    "is_group_account" CHAR,
    "level" INT,
    "parent_account_code" VARCHAR,
    "system_source" VARCHAR
);