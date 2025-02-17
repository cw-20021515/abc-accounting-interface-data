CREATE TABLE IF NOT EXISTS "company"
(
    "code" VARCHAR(4) PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL,
    "description" VARCHAR(255) NOT NULL,
    "country" VARCHAR(2) NOT NULL,
    "currency" VARCHAR(3) NOT NULL,
    "timezone" VARCHAR(20) NOT NULL,
    "fiscal_start_month" int NOT NULL DEFAULT 1,
    "is_active" char(1) NOT NULL DEFAULT 'Y',
    "create_time" timestamp DEFAULT CURRENT_TIMESTAMP,
    "created_by" VARCHAR(255),
    "update_time" timestamp DEFAULT CURRENT_TIMESTAMP,
    "updated_by" VARCHAR(255)
);

COMMENT ON TABLE "company" IS 'Account table for managing financial accounts';
COMMENT ON COLUMN "company"."code" IS '회사코드';
COMMENT ON COLUMN "company"."name" IS '회사명';
COMMENT ON COLUMN "company"."description" IS '회사 설명';
COMMENT ON COLUMN "company"."country" IS '국가';
COMMENT ON COLUMN "company"."currency" IS '통화';
COMMENT ON COLUMN "company"."timezone" IS '시간대';
COMMENT ON COLUMN "company"."fiscal_start_month" IS '회계시작월';
COMMENT ON COLUMN "company"."is_active" IS '사용여부';
COMMENT ON COLUMN "company"."create_time" IS '생성 일시';
COMMENT ON COLUMN "company"."created_by" IS '생성자';
COMMENT ON COLUMN "company"."update_time" IS '수정 일시';
COMMENT ON COLUMN "company"."updated_by" IS '수정자';


CREATE TABLE IF NOT EXISTS "account"
(
    "company_code" VARCHAR(4) NOT NULL,
    "account_code" VARCHAR(10) NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "description" VARCHAR(255) NOT NULL,
    "account_type" VARCHAR(10) NOT NULL,
    "account_class" VARCHAR(10) NOT NULL,
    "is_active" CHAR(1) NOT NULL,
    "is_open_item_mgmt" CHAR(1) NOT NULL,
    "system_source" VARCHAR(10) NOT NULL,
    "qbo_parent_account_code" VARCHAR(10),
    "qbo_account_type" VARCHAR(50),
    "qbo_account_subtype" VARCHAR(50),
    "consolidation_account_code" VARCHAR(20),
    "create_time" timestamp DEFAULT CURRENT_TIMESTAMP,
    "update_time" timestamp DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_account_id PRIMARY KEY (company_code, account_code)
);

COMMENT ON TABLE "account" IS 'Account table for managing financial accounts';
COMMENT ON COLUMN "account"."company_code" IS '회사코드';
COMMENT ON COLUMN "account"."account_code" IS '계정코드';
COMMENT ON COLUMN "account"."name" IS '계정이름(영어)';
COMMENT ON COLUMN "account"."description" IS '계정설명(한글)';
COMMENT ON COLUMN "account"."account_type" IS '계정유형';
COMMENT ON COLUMN "account"."account_class" IS '계정유형 (예: 자산, 부채, 수익, 비용 등)';
COMMENT ON COLUMN "account"."consolidation_account_code" IS '연결계정과목';
COMMENT ON COLUMN "account"."is_open_item_mgmt" IS '미결관리 여부';
COMMENT ON COLUMN "account"."system_source" IS '시스템 소스 유형';
COMMENT ON COLUMN "account"."qbo_parent_account_code" IS '퀵북 부모 계정코드';
COMMENT ON COLUMN "account"."qbo_account_type" IS 'Quickbook 계정유형';
COMMENT ON COLUMN "account"."qbo_account_subtype" IS 'Quickbook 계정서브유형';
COMMENT ON COLUMN "account"."is_active" IS 'Active 여부에 활용';
COMMENT ON COLUMN "account"."create_time" IS '생성 일시';
COMMENT ON COLUMN "account"."update_time" IS '수정 일시';


-- 연결계정을 관리하는 코드
CREATE TABLE IF NOT EXISTS "consolidation_account"
(
    "code" VARCHAR(10) PRIMARY KEY,
    "level" INTEGER NOT NULL,
    "parent_code" VARCHAR(10),
    "name" VARCHAR(255) NOT NULL,
    "eng_name" VARCHAR(255) NOT NULL,
    "description" VARCHAR(255),
    "is_postable" CHAR(1) NOT NULL,
    "is_active" CHAR(1) NOT NULL,
    "system_source" VARCHAR(255) NOT NULL,
    "create_time" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "created_by" VARCHAR(255),
    "update_time" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_by" VARCHAR(255)
);

COMMENT ON TABLE "consolidation_account" IS '연결계정(ECM)';
COMMENT ON COLUMN "consolidation_account"."code" IS '연결계정코드';
COMMENT ON COLUMN "consolidation_account"."level" IS '연결계정레벨';
COMMENT ON COLUMN "consolidation_account"."parent_code" IS '상위연결계정코드';
COMMENT ON COLUMN "consolidation_account"."name" IS '계정명';
COMMENT ON COLUMN "consolidation_account"."eng_name" IS '계정명(영어)';
COMMENT ON COLUMN "consolidation_account"."description" IS '계정설명';
COMMENT ON COLUMN "consolidation_account"."is_postable" IS '기표가능여부';
COMMENT ON COLUMN "consolidation_account"."is_active" IS '사용여부';
COMMENT ON COLUMN "consolidation_account"."create_time" IS '생성일시';
COMMENT ON COLUMN "consolidation_account"."created_by" IS '생성자';
COMMENT ON COLUMN "consolidation_account"."update_time" IS '수정일시';
COMMENT ON COLUMN "consolidation_account"."updated_by" IS '수정자';



-- Account Balance Table
CREATE TABLE IF NOT EXISTS "account_balance"
(
    company_code VARCHAR(4) NOT NULL,
    account_code VARCHAR(255) NOT NULL,
--     version BIGINT NOT NULL,
    account_nature VARCHAR(50) NOT NULL,

    -- Document information
    doc_item_id VARCHAR(255) NOT NULL,
    document_date DATE NOT NULL,
    posting_date DATE NOT NULL,
    entry_date DATE NOT NULL,

    -- Balance amounts with high precision
    balance NUMERIC(38,2) NOT NULL,
    accumulated_debit NUMERIC(38,2) NOT NULL,
    accumulated_credit NUMERIC(38,2) NOT NULL,

    -- Timestamp
    record_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_account_balance_key PRIMARY KEY (company_code, account_code)
);

-- Add table comment
COMMENT ON TABLE account_balance IS '계정 잔액';

-- Add column comments
COMMENT ON COLUMN account_balance.company_code IS '회사 코드';
COMMENT ON COLUMN account_balance.account_code IS '계정 코드';
COMMENT ON COLUMN account_balance.account_nature IS '계정 차대구분';
COMMENT ON COLUMN account_balance.doc_item_id IS '전표 항목 ID';
COMMENT ON COLUMN account_balance.document_date IS '증빙일';
COMMENT ON COLUMN account_balance.posting_date IS '전기일';
COMMENT ON COLUMN account_balance.entry_date IS '발행일';
COMMENT ON COLUMN account_balance.balance IS '잔액';
COMMENT ON COLUMN account_balance.accumulated_debit IS '차변 누적액';
COMMENT ON COLUMN account_balance.accumulated_credit IS '대변 누적액';
COMMENT ON COLUMN account_balance.record_time IS '기록 시간';

-- Account Balance Record Table
CREATE TABLE IF NOT EXISTS "account_balance_record"
(
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
    ON account_balance_record (company_code, account_code, id desc);

CREATE INDEX idx_account_balance_record_time
    ON account_balance_record (record_time desc);

-- Monthly Closing Table
CREATE TABLE IF NOT EXISTS "fiscal_closing"
(
    -- Composite key columns from FiscalYearMonth
    company_code VARCHAR(4) NOT NULL,
    fiscal_year INTEGER NOT NULL,
    fiscal_month INTEGER NOT NULL,

    -- Status column with enum
    status VARCHAR(10) NOT NULL,

    reason VARCHAR(255) NOT NULL,

    -- Timestamps
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),

    -- Version for optimistic locking
    version BIGINT NOT NULL DEFAULT 0,

    -- Primary key constraint
    CONSTRAINT pk_fiscal_closing PRIMARY KEY (company_code, fiscal_year, fiscal_month)
);

-- Comments
COMMENT ON TABLE "fiscal_closing" IS '회계 월 마감 정보';
COMMENT ON COLUMN "fiscal_closing".company_code IS '회사 코드';
COMMENT ON COLUMN "fiscal_closing".fiscal_year IS '회계연도';
COMMENT ON COLUMN "fiscal_closing".fiscal_month IS '회계월';
COMMENT ON COLUMN "fiscal_closing".status IS '상태';
COMMENT ON COLUMN "fiscal_closing".reason IS '사유';
COMMENT ON COLUMN "fiscal_closing".create_time IS '생성일시';
COMMENT ON COLUMN "fiscal_closing".created_by IS '생성자';
COMMENT ON COLUMN "fiscal_closing".update_time IS '수정일시';
COMMENT ON COLUMN "fiscal_closing".updated_by IS '수정자';



-- Monthly Closing Table
CREATE TABLE IF NOT EXISTS "fiscal_closing_history"
(
    id BIGINT PRIMARY KEY,
    -- Composite key columns from FiscalYearMonth
    company_code VARCHAR(4) NOT NULL,
    fiscal_year INTEGER NOT NULL,
    fiscal_month INTEGER NOT NULL,

    -- Status column with enum
    status VARCHAR(10) NOT NULL,

    reason VARCHAR(255) NOT NULL,

    -- Timestamps
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);

-- Comments
COMMENT ON TABLE "fiscal_closing_history" IS '회계 월 마감 정보 이력';
COMMENT ON COLUMN "fiscal_closing_history".id IS 'ID';
COMMENT ON COLUMN "fiscal_closing_history".company_code IS '회사 코드';
COMMENT ON COLUMN "fiscal_closing_history".fiscal_year IS '회계연도';
COMMENT ON COLUMN "fiscal_closing_history".fiscal_month IS '회계월';
COMMENT ON COLUMN "fiscal_closing_history".status IS '상태';
COMMENT ON COLUMN "fiscal_closing_history".reason IS '사유';
COMMENT ON COLUMN "fiscal_closing_history".create_time IS '생성일시';
COMMENT ON COLUMN "fiscal_closing_history".created_by IS '생성자';
COMMENT ON COLUMN "fiscal_closing_history".update_time IS '수정일시';
COMMENT ON COLUMN "fiscal_closing_history".updated_by IS '수정자';

-- Monthly Closing Balance Snapshot Table
CREATE TABLE IF NOT EXISTS "fiscal_closing_balance_snapshot"
(
    -- Primary key
    id BIGINT PRIMARY KEY,

    -- Account information
    company_code VARCHAR(4) NOT NULL,
    -- Fiscal period
    fiscal_year INTEGER NOT NULL,
    fiscal_month INTEGER NOT NULL,

    account_code VARCHAR(255) NOT NULL,
    account_nature VARCHAR(50) NOT NULL,


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
COMMENT ON TABLE fiscal_closing_balance_snapshot IS '회계 월별 마감 잔액 스냅샷';

-- Add column comments
COMMENT ON COLUMN fiscal_closing_balance_snapshot.id IS 'ID';
COMMENT ON COLUMN fiscal_closing_balance_snapshot.company_code IS '회사 코드';
COMMENT ON COLUMN fiscal_closing_balance_snapshot.account_code IS '계정 코드';
COMMENT ON COLUMN fiscal_closing_balance_snapshot.account_nature IS '계정 차대구분';
COMMENT ON COLUMN fiscal_closing_balance_snapshot.balance IS '잔액';
COMMENT ON COLUMN fiscal_closing_balance_snapshot.accumulated_debit IS '차변 누계액';
COMMENT ON COLUMN fiscal_closing_balance_snapshot.accumulated_credit IS '대변 누계액';
COMMENT ON COLUMN fiscal_closing_balance_snapshot.snapshot_date IS '스냅샷 날짜';
COMMENT ON COLUMN fiscal_closing_balance_snapshot.snapshot_type IS '스냅샷 유형';
COMMENT ON COLUMN fiscal_closing_balance_snapshot.created_time IS '생성 시간';

-- Add index for common queries
CREATE INDEX idx_fiscal_closing_balance_snapshot_fiscal_key
    ON fiscal_closing_balance_snapshot (company_code, fiscal_year, fiscal_month);

CREATE INDEX idx_fiscal_closing_balance_snapshot_account
    ON fiscal_closing_balance_snapshot (company_code, account_code);

