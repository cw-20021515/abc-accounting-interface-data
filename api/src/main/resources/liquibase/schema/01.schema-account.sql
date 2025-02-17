CREATE TABLE IF NOT EXISTS "account"
(
    "code" varchar PRIMARY KEY,
    "name" varchar,
    "description" varchar,
    "account_type" varchar,
    "account_class" varchar,
    "is_active" char,
    "is_open_item_mgmt" char,
    "qbo_account_type" varchar,
    "qbo_account_subtype" varchar,
    "system_source" varchar,
    "create_time" timestamp DEFAULT CURRENT_TIMESTAMP,
    "update_time" timestamp DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "account" IS 'Account table for managing financial accounts';
COMMENT ON COLUMN "account"."code" IS '계정코드';
COMMENT ON COLUMN "account"."name" IS '계정이름(영어)';
COMMENT ON COLUMN "account"."description" IS '계정설명(한글)';
COMMENT ON COLUMN "account"."account_type" IS '계정유형';
COMMENT ON COLUMN "account"."account_class" IS '계정유형 (예: 자산, 부채, 수익, 비용 등)';
COMMENT ON COLUMN "account"."is_open_item_mgmt" IS '미결관리 여부';
COMMENT ON COLUMN "account"."is_active" IS 'Active 여부에 활용';
COMMENT ON COLUMN "account"."system_source" IS '시스템 소스 유형';
COMMENT ON COLUMN "account"."qbo_account_type" IS 'Quickbook 계정유형';
COMMENT ON COLUMN "account"."qbo_account_subtype" IS 'Quickbook 계정서브유형';
COMMENT ON COLUMN "account"."create_time" IS '생성 일시';
COMMENT ON COLUMN "account"."update_time" IS '수정 일시';


CREATE TABLE IF NOT EXISTS "account_group"
(
    "id" varchar PRIMARY KEY,
    "parent_id" varchar,
    "code" varchar,
    "level" integer,
    "name" varchar,
    "description" varchar,
    "start_account_code" varchar,
    "end_account_code" varchar,
    "is_active" char,
    "create_time" timestamp DEFAULT CURRENT_TIMESTAMP,
    "update_time" timestamp DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "account_group" IS 'Account grouping table for hierarchical account structure';
COMMENT ON COLUMN "account_group"."id" IS '계정그룹ID';
COMMENT ON COLUMN "account_group"."parent_id" IS '부모 그룹ID';
COMMENT ON COLUMN "account_group"."code" IS '계정그룹코드';
COMMENT ON COLUMN "account_group"."level" IS '계정그룹 레벨';
COMMENT ON COLUMN "account_group"."name" IS '계정그룹 이름';
COMMENT ON COLUMN "account_group"."description" IS '그룹 설명';
COMMENT ON COLUMN "account_group"."start_account_code" IS '계정코드 시작값';
COMMENT ON COLUMN "account_group"."end_account_code" IS '계정코드 종료값';
COMMENT ON COLUMN "account_group"."is_active" IS 'Active 여부';
COMMENT ON COLUMN "account_group"."create_time" IS '생성 일시';
COMMENT ON COLUMN "account_group"."update_time" IS '수정 일시';

--
-- CREATE TABLE IF NOT EXISTS "account_balance"
-- (
--     account_code VARCHAR(10) PRIMARY KEY ,
--     account_nature CHAR NOT NULL,
--     balance NUMERIC(38,2) DEFAULT 0.00,
--     accumulated_debit NUMERIC(38,2) DEFAULT 0.00,
--     accumulated_credit NUMERIC(38,2) DEFAULT 0.00,
--     update_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
--     version BIGINT DEFAULT 0
-- );
--
-- COMMENT ON TABLE "account_balance" IS 'Account balance table for managing account balance';
-- COMMENT ON COLUMN "account_balance"."account_code" IS '계정코드';
-- COMMENT ON COLUMN "account_balance"."account_nature" IS '계정 차대구분';
-- COMMENT ON COLUMN "account_balance"."balance" IS '계정잔액';
-- COMMENT ON COLUMN "account_balance"."accumulated_debit" IS '차변 누계액';
-- COMMENT ON COLUMN "account_balance"."accumulated_credit" IS '대변 누계액';
-- COMMENT ON COLUMN "account_balance"."version" IS '데이터 버전';

-- Account Balance Record Table
CREATE TABLE account_balance_record (
    -- Primary key
    id BIGINT PRIMARY KEY,

    -- Account information
    company_code VARCHAR(4) NOT NULL,
    account_code VARCHAR(255) NOT NULL,
    account_nature VARCHAR(50) NOT NULL,

    -- Document information
    doc_item_id VARCHAR(255) NOT NULL,
    document_date DATE NOT NULL,
    posting_date DATE NOT NULL,
    entry_date DATE NOT NULL,
    record_type VARCHAR(50) NOT NULL,

    -- Balance amounts with high precision
    change_amount NUMERIC(38,2) NOT NULL,
    balance_after_change NUMERIC(38,2) NOT NULL,
    accumulated_debit_after_change NUMERIC(38,2) NOT NULL,
    accumulated_credit_after_change NUMERIC(38,2) NOT NULL,

    -- Timestamp
    record_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add table comment
COMMENT ON TABLE account_balance_record IS '계정 잔액 변경 기록';

-- Add column comments
COMMENT ON COLUMN account_balance_record.id IS 'ID';
COMMENT ON COLUMN account_balance_record.company_code IS '회사 코드';
COMMENT ON COLUMN account_balance_record.account_code IS '계정 코드';
COMMENT ON COLUMN account_balance_record.account_nature IS '계정 차대구분';
COMMENT ON COLUMN account_balance_record.doc_item_id IS '전표 항목 ID';
COMMENT ON COLUMN account_balance_record.document_date IS '증빙일';
COMMENT ON COLUMN account_balance_record.posting_date IS '전기일';
COMMENT ON COLUMN account_balance_record.entry_date IS '발행일';
COMMENT ON COLUMN account_balance_record.record_type IS '기록 유형';
COMMENT ON COLUMN account_balance_record.change_amount IS '변경 금액';
COMMENT ON COLUMN account_balance_record.balance_after_change IS '변경 후 잔액';
COMMENT ON COLUMN account_balance_record.accumulated_debit_after_change IS '변경 후 차변 누적액';
COMMENT ON COLUMN account_balance_record.accumulated_credit_after_change IS '변경 후 대변 누적액';
COMMENT ON COLUMN account_balance_record.record_time IS '기록 시간';

-- Add indexes for common queries
CREATE INDEX idx_account_balance_record_account
    ON account_balance_record (company_code, account_code);

CREATE INDEX idx_account_balance_record_doc_item
    ON account_balance_record (doc_item_id);

CREATE INDEX idx_account_balance_record_time
    ON account_balance_record (record_time);

-- Monthly Closing Table
CREATE TABLE IF NOT EXISTS "monthly_closing"
(
    -- Composite key columns from FiscalYearMonth
    fiscal_year INTEGER NOT NULL,
    fiscal_month INTEGER NOT NULL,

    -- Status column with enum
    status VARCHAR(50) NOT NULL,

    -- Timestamps
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),

    -- Version for optimistic locking
    version BIGINT NOT NULL DEFAULT 0,

    -- Primary key constraint
    CONSTRAINT pk_monthly_closing PRIMARY KEY (fiscal_year, fiscal_month)
);

-- Comments
COMMENT ON TABLE "monthly_closing" IS '월별 마감 정보';
COMMENT ON COLUMN "monthly_closing".fiscal_year IS '회계연도';
COMMENT ON COLUMN "monthly_closing".fiscal_month IS '회계월';
COMMENT ON COLUMN "monthly_closing".status IS '마감 상태';
COMMENT ON COLUMN "monthly_closing".create_time IS '생성일시';
COMMENT ON COLUMN "monthly_closing".created_by IS '생성자';
COMMENT ON COLUMN "monthly_closing".update_time IS '수정일시';
COMMENT ON COLUMN "monthly_closing".updated_by IS '수정자';



-- Monthly Closing Table
CREATE TABLE IF NOT EXISTS "monthly_closing_history"
(
    id BIGINT PRIMARY KEY,
    -- Composite key columns from FiscalYearMonth
    fiscal_year INTEGER NOT NULL,
    fiscal_month INTEGER NOT NULL,

    -- Status column with enum
    status VARCHAR(50) NOT NULL,

    -- Timestamps
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),

    -- Version for optimistic locking
    version BIGINT NOT NULL DEFAULT 0
);

-- Comments
COMMENT ON TABLE "monthly_closing_history" IS '월별 마감 정보 이력';
COMMENT ON COLUMN "monthly_closing_history".fiscal_year IS '회계연도';
COMMENT ON COLUMN "monthly_closing_history".fiscal_month IS '회계월';
COMMENT ON COLUMN "monthly_closing_history".status IS '마감 상태';
COMMENT ON COLUMN "monthly_closing_history".create_time IS '생성일시';
COMMENT ON COLUMN "monthly_closing_history".created_by IS '생성자';
COMMENT ON COLUMN "monthly_closing_history".update_time IS '수정일시';
COMMENT ON COLUMN "monthly_closing_history".updated_by IS '수정자';

-- Monthly Closing Balance Snapshot Table
CREATE TABLE IF NOT EXISTS monthly_closing_balance_snapshot
(
    -- Primary key
    id BIGINT PRIMARY KEY,

    -- Account information
    company_code VARCHAR(4) NOT NULL,
    account_code VARCHAR(255) NOT NULL,
    account_nature VARCHAR(50) NOT NULL,

    -- Fiscal period
    fiscal_year INTEGER NOT NULL,
    fiscal_month INTEGER NOT NULL,

    -- Balance amounts with high precision
    balance NUMERIC(38, 6) NOT NULL,
    accumulated_debit NUMERIC(38, 6) NOT NULL,
    accumulated_credit NUMERIC(38, 6) NOT NULL,

    -- Snapshot information
    snapshot_date DATE NOT NULL,
    snapshot_type VARCHAR(50) NOT NULL,

    -- Timestamp
    created_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add table comment
COMMENT ON TABLE monthly_closing_balance_snapshot IS '월별 마감 잔액 스냅샷';

-- Add column comments
COMMENT ON COLUMN monthly_closing_balance_snapshot.id IS 'ID';
COMMENT ON COLUMN monthly_closing_balance_snapshot.company_code IS '회사 코드';
COMMENT ON COLUMN monthly_closing_balance_snapshot.account_code IS '계정 코드';
COMMENT ON COLUMN monthly_closing_balance_snapshot.account_nature IS '계정 차대구분';
COMMENT ON COLUMN monthly_closing_balance_snapshot.balance IS '잔액';
COMMENT ON COLUMN monthly_closing_balance_snapshot.accumulated_debit IS '차변 누계액';
COMMENT ON COLUMN monthly_closing_balance_snapshot.accumulated_credit IS '대변 누계액';
COMMENT ON COLUMN monthly_closing_balance_snapshot.snapshot_date IS '스냅샷 날짜';
COMMENT ON COLUMN monthly_closing_balance_snapshot.snapshot_type IS '스냅샷 유형';
COMMENT ON COLUMN monthly_closing_balance_snapshot.created_time IS '생성 시간';

-- Add index for common queries
CREATE INDEX idx_monthly_closing_balance_snapshot_fiscal
    ON monthly_closing_balance_snapshot (fiscal_year, fiscal_month);

CREATE INDEX idx_monthly_closing_balance_snapshot_account
    ON monthly_closing_balance_snapshot (account_code);