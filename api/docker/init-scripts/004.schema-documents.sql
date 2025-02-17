CREATE TABLE IF NOT EXISTS "document"
(
    "id" VARCHAR(20) PRIMARY KEY,
    "version" BIGINT NOT NULL,
    "doc_hash" VARCHAR(255) NOT NULL UNIQUE,
    "doc_type" VARCHAR(2) NOT NULL,
    "doc_status" VARCHAR(2) NOT NULL,
    "workflow_status" VARCHAR(2) NOT NULL,
    "workflow_id" VARCHAR(20),
    "document_date" DATE,
    "posting_date" DATE,
    "entry_date" DATE,
    "fiscal_year" INT NOT NULL,
    "fiscal_month" INT NOT NULL,
    "company_code" VARCHAR(4) NOT NULL,
    "tx_currency" VARCHAR(3) NOT NULL,
    "tx_amount" NUMERIC(38,2) NOT NULL,
    "currency" VARCHAR(3) NOT NULL,
    "amount" NUMERIC(38,2),
    "reference" VARCHAR(255),
    "text" VARCHAR(255),
    "is_deleted" char(1) DEFAULT 'N',
    "create_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "created_by" VARCHAR(50) NOT NULL,
    "update_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" VARCHAR(50) NOT NULL
);

COMMENT ON TABLE "document" IS '전표';
COMMENT ON COLUMN "document"."id" IS '전표ID';
COMMENT ON COLUMN "document"."version" IS '버전';
COMMENT ON COLUMN "document"."doc_hash" IS '전표해시';
COMMENT ON COLUMN "document"."doc_type" IS '전표유형';
COMMENT ON COLUMN "document"."doc_status" IS '전표상태';
COMMENT ON COLUMN "document"."workflow_status" IS '전자결제상태';
COMMENT ON COLUMN "document"."workflow_id" IS '전자결재ID';
COMMENT ON COLUMN "document"."document_date" IS '증빙일';
COMMENT ON COLUMN "document"."posting_date" IS '전기일';
COMMENT ON COLUMN "document"."entry_date" IS '발행일';
COMMENT ON COLUMN "document"."fiscal_year" IS '회계연도';
COMMENT ON COLUMN "document"."fiscal_month" IS '회계월';
COMMENT ON COLUMN "document"."company_code" IS '회사코드';
COMMENT ON COLUMN "document"."tx_currency" IS '거래통화';
COMMENT ON COLUMN "document"."tx_amount" IS '거래금액';
COMMENT ON COLUMN "document"."currency" IS '회계통화';
COMMENT ON COLUMN "document"."amount" IS '회계금액';
COMMENT ON COLUMN "document"."reference" IS '참조';
COMMENT ON COLUMN "document"."text" IS '텍스트';
COMMENT ON COLUMN "document"."is_deleted" IS '삭제여부';
COMMENT ON COLUMN "document"."create_time" IS '생성시간';
COMMENT ON COLUMN "document"."created_by" IS '생성자';
COMMENT ON COLUMN "document"."update_time" IS '수정시간';
COMMENT ON COLUMN "document"."updated_by" IS '수정자';


CREATE TABLE IF NOT EXISTS "document_origin"
(
    "doc_id" VARCHAR(20) PRIMARY KEY ,
    "doc_template_code" VARCHAR(255) NOT NULL,
    "biz_system" VARCHAR(10) NOT NULL,
    "biz_tx_id" VARCHAR(50) NOT NULL,
    "biz_process" VARCHAR(50) NOT NULL,
    "biz_event" VARCHAR(50) NOT NULL,
    "accounting_event" VARCHAR(255) NOT NULL
);

COMMENT ON TABLE "document_origin" IS '전표비즈니스';
COMMENT ON COLUMN "document_origin"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_origin"."doc_template_code" IS '전표템플릿코드';
COMMENT ON COLUMN "document_origin"."biz_system" IS '비즈니스시스템';
COMMENT ON COLUMN "document_origin"."biz_tx_id" IS '비즈니스 거래ID';
COMMENT ON COLUMN "document_origin"."biz_process" IS '비즈니스 프로세스';
COMMENT ON COLUMN "document_origin"."biz_event" IS '비즈니스 이벤트';
COMMENT ON COLUMN "document_origin"."accounting_event" IS '회계 이벤트';


CREATE TABLE IF NOT EXISTS "document_relation"
(
    "id" VARCHAR(32) PRIMARY KEY,
    "doc_id" VARCHAR(20) NOT NULL,
    "ref_doc_id" VARCHAR(20) NOT NULL,
    "relation_type" VARCHAR(2) NOT NULL,
    "reason" VARCHAR(255),
    "create_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP)
);

CREATE INDEX idx_document_relation_doc_id ON document_relation(doc_id);
CREATE INDEX idx_document_relation_ref_doc_id ON document_relation(ref_doc_id);

COMMENT ON TABLE "document_relation" IS '전표관계';
COMMENT ON COLUMN "document_relation"."id" IS 'ID';
COMMENT ON COLUMN "document_relation"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_relation"."relation_type" IS '관계유형';
COMMENT ON COLUMN "document_relation"."ref_doc_id" IS '참조전표ID';
COMMENT ON COLUMN "document_relation"."reason" IS '사유';
COMMENT ON COLUMN "document_relation"."create_time" IS '생성시간';


CREATE TABLE IF NOT EXISTS "document_item"
(
    "id" VARCHAR(20) PRIMARY KEY,
    "version" BIGINT NOT NULL,
    "doc_item_status" VARCHAR(2) NOT NULL,
    "doc_id" VARCHAR(20) NOT NULL,
    "line_number" INTEGER NOT NULL,
    "account_code" VARCHAR(10) NOT NULL,
    "account_side" CHAR(1) NOT NULL,
    "company_code" VARCHAR(4) NOT NULL,
    "tx_currency" VARCHAR(3) NOT NULL,
    "tx_amount" NUMERIC(38,2) NOT NULL,
    "currency" VARCHAR(3) NOT NULL,
    "amount" NUMERIC(38,2) NOT NULL,
    "exchange_rate_id" VARCHAR(20),
    "text" VARCHAR(255) NOT NULL,
    "doc_template_code" VARCHAR(255),
    "cost_center" VARCHAR(50) NOT NULL,
    "profit_center" VARCHAR(50),
    "segment" VARCHAR(50),
    "project" VARCHAR(50),
    "customer_id" VARCHAR(50),
    "vendor_id" VARCHAR(50),
    "create_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "created_by" VARCHAR(50) NOT NULL,
    "update_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" VARCHAR(50) NOT NULL
);

COMMENT ON TABLE "document_item" IS '전표항목';
COMMENT ON COLUMN "document_item"."id" IS '전표항목ID';
COMMENT ON COLUMN "document_item"."version" IS '버전';
COMMENT ON COLUMN "document_item"."doc_item_status" IS '전표항목상태';
COMMENT ON COLUMN "document_item"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_item"."line_number" IS '라인번호';
COMMENT ON COLUMN "document_item"."account_code" IS '계정코드';
COMMENT ON COLUMN "document_item"."account_side" IS '차/대 구분';
COMMENT ON COLUMN "document_item"."company_code" IS '회사코드';
COMMENT ON COLUMN "document_item"."tx_currency" IS '거래통화';
COMMENT ON COLUMN "document_item"."tx_amount" IS '거래금액';
COMMENT ON COLUMN "document_item"."currency" IS '회계통화';
COMMENT ON COLUMN "document_item"."amount" IS '회계금액';
COMMENT ON COLUMN "document_item"."exchange_rate_id" IS '환율ID';
COMMENT ON COLUMN "document_item"."text" IS '텍스트';
COMMENT ON COLUMN "document_item"."doc_template_code" IS '전표템플릿코드';
COMMENT ON COLUMN "document_item"."cost_center" IS '코스트센터';
COMMENT ON COLUMN "document_item"."profit_center" IS '손익센터';
COMMENT ON COLUMN "document_item"."segment" IS '세그먼트';
COMMENT ON COLUMN "document_item"."project" IS '프로젝트';
COMMENT ON COLUMN "document_item"."customer_id" IS '고객ID';
COMMENT ON COLUMN "document_item"."vendor_id" IS '거래처ID';
COMMENT ON COLUMN "document_item"."create_time" IS '생성시간';
COMMENT ON COLUMN "document_item"."created_by" IS '생성자';
COMMENT ON COLUMN "document_item"."update_time" IS '수정시간';
COMMENT ON COLUMN "document_item"."updated_by" IS '수정자';



CREATE TABLE IF NOT EXISTS "document_item_attribute"
(
    "doc_item_id" varchar,
    "attribute_type" varchar,
    "value" varchar,
    "create_time" timestamp DEFAULT (CURRENT_TIMESTAMP),

    CONSTRAINT pk_attribute_id PRIMARY KEY (doc_item_id, attribute_type)
);

COMMENT ON TABLE "document_item_attribute" IS '전표항목속성';
COMMENT ON COLUMN "document_item_attribute"."doc_item_id" IS '전표항목ID';
COMMENT ON COLUMN "document_item_attribute"."attribute_type" IS '카테고리';
COMMENT ON COLUMN "document_item_attribute"."value" IS '값';
COMMENT ON COLUMN "document_item_attribute"."create_time" IS '생성시간';


CREATE TABLE IF NOT EXISTS "document_history"
(
    "id" BIGSERIAL PRIMARY KEY,
    "doc_id" VARCHAR(20) NOT NULL,
    "version" BIGINT NOT NULL,
    "doc_hash" VARCHAR(255) NOT NULL,
    "doc_type" VARCHAR(2) NOT NULL,
    "doc_status" VARCHAR(2) NOT NULL,
    "workflow_status" VARCHAR(2) NOT NULL,
    "workflow_id" VARCHAR(20),
    "document_date" DATE,
    "posting_date" DATE,
    "entry_date" DATE,
    "fiscal_year" INT NOT NULL,
    "fiscal_month" INT NOT NULL,
    "company_code" VARCHAR(4) NOT NULL,
    "tx_currency" VARCHAR(3) NOT NULL,
    "tx_amount" NUMERIC(38,2) NOT NULL,
    "currency" VARCHAR(3) NOT NULL,
    "amount" NUMERIC(38,2),
    "reference" VARCHAR(255),
    "text" VARCHAR(255),
    "is_deleted" char(1) DEFAULT 'N',
    "create_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "created_by" VARCHAR(50) NOT NULL,
    "update_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" VARCHAR(50) NOT NULL
);

COMMENT ON TABLE "document_history" IS '전표이력';
COMMENT ON COLUMN "document_history"."id" IS 'ID';
COMMENT ON COLUMN "document_history"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_history"."version" IS '버전';
COMMENT ON COLUMN "document_history"."doc_hash" IS '전표해시';
COMMENT ON COLUMN "document_history"."doc_type" IS '전표유형';
COMMENT ON COLUMN "document_history"."doc_status" IS '전표상태';
COMMENT ON COLUMN "document_history"."workflow_status" IS '전자결제상태';
COMMENT ON COLUMN "document_history"."workflow_id" IS '전자결재ID';
COMMENT ON COLUMN "document_history"."document_date" IS '증빙일';
COMMENT ON COLUMN "document_history"."posting_date" IS '전기일';
COMMENT ON COLUMN "document_history"."entry_date" IS '발행일';
COMMENT ON COLUMN "document_history"."fiscal_year" IS '회계연도';
COMMENT ON COLUMN "document_history"."fiscal_month" IS '회계기간';
COMMENT ON COLUMN "document_history"."company_code" IS '회사코드';
COMMENT ON COLUMN "document_history"."tx_currency" IS '거래통화';
COMMENT ON COLUMN "document_history"."tx_amount" IS '거래금액';
COMMENT ON COLUMN "document_history"."currency" IS '회계통화';
COMMENT ON COLUMN "document_history"."amount" IS '회계금액';
COMMENT ON COLUMN "document_history"."reference" IS '참조';
COMMENT ON COLUMN "document_history"."text" IS '텍스트';
COMMENT ON COLUMN "document_history"."is_deleted" IS '삭제여부';
COMMENT ON COLUMN "document_history"."create_time" IS '생성시간';
COMMENT ON COLUMN "document_history"."created_by" IS '생성자';
COMMENT ON COLUMN "document_history"."update_time" IS '수정시간';
COMMENT ON COLUMN "document_history"."updated_by" IS '수정자';


CREATE TABLE IF NOT EXISTS "document_item_history"
(
    "id" BIGSERIAL PRIMARY KEY,
    "doc_item_id" VARCHAR(20),
    "version" BIGINT NOT NULL,
    "doc_item_status" VARCHAR(2) NOT NULL,
    "doc_id" VARCHAR(20) NOT NULL,
    "line_number" INTEGER NOT NULL,
    "account_code" VARCHAR(10) NOT NULL,
    "account_side" CHAR(1) NOT NULL,
    "company_code" VARCHAR(4) NOT NULL,
    "tx_currency" VARCHAR(3) NOT NULL,
    "tx_amount" NUMERIC(38,2) NOT NULL,
    "currency" VARCHAR(3) NOT NULL,
    "amount" NUMERIC(38,2) NOT NULL,
    "exchange_rate_id" VARCHAR(20) NOT NULL,
    "text" VARCHAR(255) NOT NULL,
    "doc_template_code" VARCHAR(255),
    "cost_center" VARCHAR(50) NOT NULL,
    "profit_center" VARCHAR(50),
    "segment" VARCHAR(50),
    "project" VARCHAR(50),
    "customer_id" VARCHAR(50),
    "vendor_id" VARCHAR(50),
    "create_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "created_by" VARCHAR(50) NOT NULL,
    "update_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" VARCHAR(50) NOT NULL
);

COMMENT ON TABLE "document_item_history" IS '전표항목이력';
COMMENT ON COLUMN "document_item_history"."id" IS 'ID';
COMMENT ON COLUMN "document_item_history"."doc_item_id" IS '전표항목ID';
COMMENT ON COLUMN "document_item_history"."version" IS '버전';
COMMENT ON COLUMN "document_item_history"."doc_item_status" IS '전표항목상태';
COMMENT ON COLUMN "document_item_history"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_item_history"."line_number" IS '라인번호';
COMMENT ON COLUMN "document_item_history"."account_code" IS '계정코드';
COMMENT ON COLUMN "document_item_history"."account_side" IS '차/대 구분)';
COMMENT ON COLUMN "document_item_history"."company_code" IS '회사코드';
COMMENT ON COLUMN "document_item_history"."tx_currency" IS '거래통화';
COMMENT ON COLUMN "document_item_history"."tx_amount" IS '거래금액';
COMMENT ON COLUMN "document_item_history"."currency" IS '회계통화';
COMMENT ON COLUMN "document_item_history"."amount" IS '회계금액';
COMMENT ON COLUMN "document_item_history"."exchange_rate_id" IS '환율ID';
COMMENT ON COLUMN "document_item_history"."text" IS '텍스트';
COMMENT ON COLUMN "document_item_history"."doc_template_code" IS '전표템플릿코드';
COMMENT ON COLUMN "document_item_history"."cost_center" IS '코스트센터';
COMMENT ON COLUMN "document_item_history"."profit_center" IS '손익센터';
COMMENT ON COLUMN "document_item_history"."segment" IS '세그먼트';
COMMENT ON COLUMN "document_item_history"."project" IS '프로젝트';
COMMENT ON COLUMN "document_item_history"."customer_id" IS '고객ID';
COMMENT ON COLUMN "document_item_history"."vendor_id" IS '거래처ID';
COMMENT ON COLUMN "document_item_history"."create_time" IS '생성시간';
COMMENT ON COLUMN "document_item_history"."update_time" IS '수정시간';


CREATE TABLE IF NOT EXISTS "document_item_relation"
(
    "id" VARCHAR(20) PRIMARY KEY,
    "doc_item_id" VARCHAR(20) NOT NULL,
    "ref_doc_item_id" VARCHAR(20) NOT NULL,
    "relation_type" VARCHAR(2) NOT NULL,
    "reason" VARCHAR(255),
    "ref_amount" NUMERIC(38,2),
    "amount" NUMERIC(38,2),
    "create_time" timestamp DEFAULT (CURRENT_TIMESTAMP)
);

CREATE INDEX idx_document_item_relation_doc_item_id ON document_item_relation(doc_item_id);
CREATE INDEX idx_document_item_relation_ref_doc_item_id ON document_item_relation(ref_doc_item_id);

COMMENT ON TABLE "document_item_relation" IS '전표항목관계';
COMMENT ON COLUMN "document_item_relation"."id" IS 'ID';
COMMENT ON COLUMN "document_item_relation"."doc_item_id" IS '전표항목ID';
COMMENT ON COLUMN "document_item_relation"."ref_doc_item_id" IS '참조전표항목ID';
COMMENT ON COLUMN "document_item_relation"."relation_type" IS '관계유형';
COMMENT ON COLUMN "document_item_relation"."reason" IS '사유';
COMMENT ON COLUMN "document_item_relation"."ref_amount" IS '참조금액';
COMMENT ON COLUMN "document_item_relation"."amount" IS '금액';
COMMENT ON COLUMN "document_item_relation"."create_time" IS '생성시간';


CREATE TABLE IF NOT EXISTS "document_note"
(
    "id" BIGSERIAL PRIMARY KEY,
    "doc_id" VARCHAR(20) NOT NULL ,
    "is_deleted" CHAR(1) DEFAULT 'N',
    "contents" TEXT,
    "create_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "created_by" VARCHAR(50) NOT NULL,
    "update_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" VARCHAR(50) NOT NULL
);

COMMENT ON COLUMN "document_note"."id" IS 'ID';
COMMENT ON COLUMN "document_note"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_note"."is_deleted" IS '삭제여부';
COMMENT ON COLUMN "document_note"."contents" IS '내용';
COMMENT ON COLUMN "document_note"."create_time" IS '생성시간';
COMMENT ON COLUMN "document_note"."created_by" IS '생성자';
COMMENT ON COLUMN "document_note"."update_time" IS '수정시간';
COMMENT ON COLUMN "document_note"."updated_by" IS '수정자';


CREATE TABLE IF NOT EXISTS "document_attachment"
(
    "id" BIGSERIAL PRIMARY KEY,
    "doc_id" VARCHAR(20) NOT NULL,
    "file_name" VARCHAR(255) NOT NULL,
    "internal_path" VARCHAR(255) NOT NULL,
    "create_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "created_by" VARCHAR(20) NOT NULL,
    "update_time" TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" VARCHAR(20) NOT NULL
);

COMMENT ON COLUMN "document_attachment"."id" IS 'ID';
COMMENT ON COLUMN "document_attachment"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_attachment"."file_name" IS '파일명';
COMMENT ON COLUMN "document_attachment"."internal_path" IS '파일내부경로';
COMMENT ON COLUMN "document_attachment"."create_time" IS '생성시간';
COMMENT ON COLUMN "document_attachment"."created_by" IS '생성자';
COMMENT ON COLUMN "document_attachment"."update_time" IS '수정시간';
COMMENT ON COLUMN "document_attachment"."updated_by" IS '수정자';

-- ALTER TABLE "document_process" ADD FOREIGN KEY ("doc_id") REFERENCES "document" ("id");
-- ALTER TABLE "document_relation" ADD FOREIGN KEY ("doc_id") REFERENCES "document" ("id");
-- ALTER TABLE "document_item" ADD FOREIGN KEY ("doc_id") REFERENCES "document" ("id");
-- ALTER TABLE "document_item_attribute" ADD FOREIGN KEY ("doc_item_id") REFERENCES "document_item" ("id");
-- ALTER TABLE "document_history" ADD FOREIGN KEY ("doc_id") REFERENCES "document" ("id");
-- ALTER TABLE "document_item_history" ADD FOREIGN KEY ("doc_item_id") REFERENCES "document_item" ("id");
-- ALTER TABLE "document_item_history" ADD FOREIGN KEY ("doc_id") REFERENCES "document" ("id");
-- ALTER TABLE "document_item_relation" ADD FOREIGN KEY ("doc_item_id") REFERENCES "document_item" ("id");
-- ALTER TABLE "document_note" ADD FOREIGN KEY ("doc_id") REFERENCES "document" ("id");
-- ALTER TABLE "document_attachment" ADD FOREIGN KEY ("doc_id") REFERENCES "document" ("id");

        --- 전표 시퀀스 테이블
CREATE TABLE IF NOT EXISTS "custom_sequence"
(
    sequence_name VARCHAR(20) PRIMARY KEY,
    current_value BIGINT NOT NULL,
    version BIGINT NOT NULL
);

-- 환율정보 테이블
CREATE TABLE IF NOT EXISTS "exchange_rate"
(
    id BIGSERIAL PRIMARY KEY,
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    exchange_rate_date DATE NOT NULL,
    exchange_rate NUMERIC(38,12) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

COMMENT ON TABLE exchange_rate IS '환율정보';
COMMENT ON COLUMN exchange_rate.id IS '환율ID';
COMMENT ON COLUMN exchange_rate.from_currency IS '기준통화';
COMMENT ON COLUMN exchange_rate.to_currency IS '변환통화';
COMMENT ON COLUMN exchange_rate.exchange_rate_date IS '환율일';
COMMENT ON COLUMN exchange_rate.exchange_rate IS '환율';
COMMENT ON COLUMN exchange_rate.create_time IS '생성일시';
COMMENT ON COLUMN exchange_rate.update_time IS '수정일시';




-- 테이블 생성
CREATE TABLE IF NOT EXISTS "document_item_attribute_master"
(
    "id" BIGSERIAL PRIMARY KEY,
    "account_type" VARCHAR(20) NOT NULL,
    "attribute_category" VARCHAR(20) NOT NULL,
    "attribute_type" VARCHAR(50) NOT NULL,
    "field_requirement" VARCHAR(20) NOT NULL,
    "is_active" CHAR(1) NOT NULL,
    "condition_logic" VARCHAR(50),
    "create_time" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "update_time" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 복합 유니크 제약조건
    CONSTRAINT unique_account_type_check UNIQUE (account_type, attribute_category, attribute_type, field_requirement)
    );

COMMENT ON COLUMN document_item_attribute_master.account_type IS '계정유형코드';
COMMENT ON COLUMN document_item_attribute_master.attribute_category IS '속성 카테고리';
COMMENT ON COLUMN document_item_attribute_master.attribute_type IS '속성 유형';
COMMENT ON COLUMN document_item_attribute_master.field_requirement IS '필드요구사항';
COMMENT ON COLUMN document_item_attribute_master.condition_logic IS '조건';
COMMENT ON COLUMN document_item_attribute_master.is_active IS 'Active 여부';
COMMENT ON COLUMN document_item_attribute_master.create_time IS '생성 일시';
COMMENT ON COLUMN document_item_attribute_master.update_time IS '수정 일시';


-- Document Template 테이블 생성
CREATE TABLE IF NOT EXISTS "document_template"
(
    company_code VARCHAR(4) NOT NULL, -- 임시
    doc_template_code VARCHAR(255) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    kor_text VARCHAR(255) NOT NULL,
    eng_text VARCHAR(255),
    biz_category VARCHAR(50) NOT NULL,
    biz_system VARCHAR(50) NOT NULL,
--     sales_type VARCHAR(50),
    biz_process VARCHAR(50) NOT NULL,
    biz_event VARCHAR(50) NOT NULL,
    account_event_category VARCHAR(50),
    account_event VARCHAR(50),
    is_active CHAR(1) NOT NULL DEFAULT 'Y',
    biz_event_order INTEGER NOT NULL,
    document_type VARCHAR(20) NOT NULL,
    processing_type VARCHAR(20),
    order_item_status VARCHAR(50),
    service_flow_type VARCHAR(50),
    service_flow_status VARCHAR(50),
    charge_status VARCHAR(50),
    contract_status VARCHAR(50),
    logistics_status VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_doc_template_key PRIMARY KEY (company_code, doc_template_code)
);

-- -- 인덱스 추가
-- CREATE INDEX idx_document_template_category ON document_template(category);
-- CREATE INDEX idx_document_template_sales_type ON document_template(sales_type);
-- CREATE INDEX idx_document_template_biz_process ON document_template(biz_process);
-- CREATE INDEX idx_document_template_biz_event ON document_template(biz_event);
-- CREATE INDEX idx_document_template_create_time ON document_template(create_time);

-- 코멘트 추가
COMMENT ON TABLE document_template IS '전표 템플릿';
COMMENT ON COLUMN document_template.company_code IS '회사코드';
COMMENT ON COLUMN document_template.doc_template_code IS '전표템플릿코드';
COMMENT ON COLUMN document_template.symbol IS '심볼';
COMMENT ON COLUMN document_template.biz_category IS '카테고리';
COMMENT ON COLUMN document_template.biz_system IS '비즈시스템';
COMMENT ON COLUMN document_template.biz_process IS '비즈니스 프로세스';
COMMENT ON COLUMN document_template.biz_event IS '비즈니스 이벤트';
COMMENT ON COLUMN document_template.account_event_category IS '회계 이벤트 카테고리';
COMMENT ON COLUMN document_template.account_event IS '회계 이벤트';
COMMENT ON COLUMN document_template.kor_text IS '한글 텍스트';
COMMENT ON COLUMN document_template.eng_text IS '영문 텍스트';
COMMENT ON COLUMN document_template.is_active IS '사용여부';
COMMENT ON COLUMN document_template.biz_event_order IS '비즈니스 이벤트 순서';
COMMENT ON COLUMN document_template.document_type IS '전표유형';
COMMENT ON COLUMN document_template.processing_type IS '처리유형';
COMMENT ON COLUMN document_template.order_item_status IS '주문항목 상태';
COMMENT ON COLUMN document_template.service_flow_type IS '서비스 플로우 타입';
COMMENT ON COLUMN document_template.service_flow_status IS '서비스 플로우 상태';
COMMENT ON COLUMN document_template.charge_status IS '청구 상태';
COMMENT ON COLUMN document_template.contract_status IS '계약 상태';
COMMENT ON COLUMN document_template.logistics_status IS '물류 상태';
COMMENT ON COLUMN document_template.create_time IS '생성 일시';


CREATE TABLE IF NOT EXISTS "document_template_item"
(
    id BIGSERIAL PRIMARY KEY,
    company_code VARCHAR(4) NOT NULL,
    doc_template_code VARCHAR(255) NOT NULL,
    line_order INTEGER NOT NULL,
    account_code VARCHAR(10) NOT NULL,
    ref_doc_template_code VARCHAR(255),
    account_side VARCHAR(10) NOT NULL,
    requirement_type VARCHAR(20) NOT NULL,
    cost_center VARCHAR(20) NOT NULL,
    profit_center VARCHAR(20),
    segment VARCHAR(50),
    project VARCHAR(50),
    item_text_kor TEXT NOT NULL,
    item_text_eng TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_doc_template_item_doc_template_key ON document_template_item(company_code, doc_template_code);

-- 코멘트 추가
COMMENT ON TABLE document_template_item IS '전표 템플릿 항목';
COMMENT ON COLUMN document_template_item.id IS 'id';
COMMENT ON COLUMN document_template_item.company_code IS '회사코드';
COMMENT ON COLUMN document_template_item.doc_template_code IS '전표템플릿코드';
COMMENT ON COLUMN document_template_item.line_order IS '전표항목순서';
COMMENT ON COLUMN document_template_item.account_side IS '차대구분';
COMMENT ON COLUMN document_template_item.account_code IS '계정코드';
COMMENT ON COLUMN document_template_item.ref_doc_template_code IS '참조 전표템플릿코드';
COMMENT ON COLUMN document_template_item.cost_center IS '비용센터';
COMMENT ON COLUMN document_template_item.profit_center IS '수익센터';
COMMENT ON COLUMN document_template_item.segment IS '세그먼트';
COMMENT ON COLUMN document_template_item.project IS '프로젝트';
COMMENT ON COLUMN document_template_item.item_text_kor IS '한글 텍스트';
COMMENT ON COLUMN document_template_item.item_text_eng IS '영문 텍스트';
COMMENT ON COLUMN document_template_item.requirement_type IS '필수여부';
COMMENT ON COLUMN document_template_item.create_time IS '생성 일시';
--
-- CREATE TABLE IF NOT EXISTS "document_template_item_ref"
-- (
--     id BIGSERIAL PRIMARY KEY,
--     doc_template_item_id BIGINT NOT NULL,
--     ref_doc_template_code VARCHAR(255) NOT NULL,
-- --     FOREIGN KEY (doc_template_item_id) REFERENCES document_template_item(id)
-- );


CREATE TABLE IF NOT EXISTS "document_approval_rule"
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority INT NOT NULL,
    company_code VARCHAR(10),
    conditions JSONB NOT NULL,
    requires_approval CHAR(1) NOT NULL,
    is_active CHAR(1) NOT NULL
);