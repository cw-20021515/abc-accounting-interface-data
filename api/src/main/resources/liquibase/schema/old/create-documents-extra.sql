--- 전표 시퀀스 테이블
CREATE TABLE IF NOT EXISTS "custom_sequence"
(
    "sequence_name" VARCHAR PRIMARY KEY,
    "current_value" BIGINT NOT NULL,
    "version" BIGINT NOT NULL
);

-- 환율정보 테이블
CREATE TABLE IF NOT EXISTS "exchange_rate"
(
    "id" SERIAL PRIMARY KEY,
    "from_currency" VARCHAR(3) NOT NULL,
    "to_currency" VARCHAR(3) NOT NULL,
    "exchange_rate_date" DATE NOT NULL,
    "exchange_rate" DECIMAL(38, 12) NOT NULL,
    "create_time" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "update_time" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "exchange_rate" IS '환율정보';
COMMENT ON COLUMN "exchange_rate"."id" IS '환율ID';
COMMENT ON COLUMN "exchange_rate"."from_currency" IS '기준통화';
COMMENT ON COLUMN "exchange_rate"."to_currency" IS '변환통화';
COMMENT ON COLUMN "exchange_rate"."exchange_rate_date" IS '환율일';
COMMENT ON COLUMN "exchange_rate"."exchange_rate" IS '환율';
COMMENT ON COLUMN "exchange_rate"."create_time" IS '생성일시';
COMMENT ON COLUMN "exchange_rate"."update_time" IS '수정일시';




-- 테이블 생성
CREATE TABLE IF NOT EXISTS "document_item_attribute_master"
(
    "id" SERIAL PRIMARY KEY,
    "account_type" varchar NOT NULL,
    "attribute_category" varchar NOT NULL,
    "attribute_type" varchar NOT NULL,
    "field_requirement" varchar NOT NULL,
    "condition_logic" varchar,
    "is_active" char NOT NULL,
    "create_time" timestamp DEFAULT CURRENT_TIMESTAMP,
    "update_time" timestamp DEFAULT CURRENT_TIMESTAMP,

    -- 복합 유니크 제약조건
    CONSTRAINT unique_account_type_check UNIQUE (account_type, attribute_category, attribute_type, field_requirement)
);

COMMENT ON COLUMN "document_item_attribute_master"."account_type" IS '계정유형코드';
COMMENT ON COLUMN "document_item_attribute_master"."attribute_category" IS '속성 카테고리';
COMMENT ON COLUMN "document_item_attribute_master"."attribute_type" IS '속성 유형';
COMMENT ON COLUMN "document_item_attribute_master"."field_requirement" IS '필드요구사항';
COMMENT ON COLUMN "document_item_attribute_master"."condition_logic" IS '조건';
COMMENT ON COLUMN "document_item_attribute_master"."is_active" IS 'Active 여부';
COMMENT ON COLUMN "document_item_attribute_master"."create_time" IS '생성 일시';
COMMENT ON COLUMN "document_item_attribute_master"."update_time" IS '수정 일시';


-- Document Template 테이블 생성
CREATE TABLE IF NOT EXISTS "document_template"
(
    "doc_template_code" varchar PRIMARY KEY,
    "biz_category" varchar NOT NULL,
    "biz_system" varchar NOT NULL,
    "sales_type" varchar,
    "biz_process" varchar NOT NULL,
    "biz_event" varchar NOT NULL,
    "account_event_category" varchar,
    "account_event" varchar,
    "text_kor" varchar NOT NULL,
    "text_eng" varchar,
    "is_active" char,
    "biz_event_order" integer,
    "document_type" varchar NOT NULL,
    "processing_type" varchar,
    "order_item_status" varchar,
    "charge_status" varchar,
    "contract_status" varchar,
    "service_flow_status" varchar,
    "logistics_status" varchar,
    "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -- 인덱스 추가
-- CREATE INDEX idx_document_template_category ON document_template(category);
-- CREATE INDEX idx_document_template_sales_type ON document_template(sales_type);
-- CREATE INDEX idx_document_template_biz_process ON document_template(biz_process);
-- CREATE INDEX idx_document_template_biz_event ON document_template(biz_event);
-- CREATE INDEX idx_document_template_create_time ON document_template(create_time);

-- 코멘트 추가
COMMENT ON TABLE "document_template" IS '전표 템플릿';
COMMENT ON COLUMN "document_template"."doc_template_code" IS '전표템플릿코드';
COMMENT ON COLUMN "document_template"."biz_category" IS '카테고리';
COMMENT ON COLUMN "document_template"."biz_system" IS '비즈시스템';
COMMENT ON COLUMN "document_template"."sales_type" IS '판매유형';
COMMENT ON COLUMN "document_template"."biz_process" IS '비즈니스 프로세스';
COMMENT ON COLUMN "document_template"."biz_event" IS '비즈니스 이벤트';
COMMENT ON COLUMN "document_template"."account_event_category" IS '회계 이벤트 카테고리';
COMMENT ON COLUMN "document_template"."account_event" IS '회계 이벤트';
COMMENT ON COLUMN "document_template"."text_kor" IS '한글 텍스트';
COMMENT ON COLUMN "document_template"."text_eng" IS '영문 텍스트';
COMMENT ON COLUMN "document_template"."is_active" IS '사용여부';
COMMENT ON COLUMN "document_template"."biz_event_order" IS '비즈니스 이벤트 순서';
COMMENT ON COLUMN "document_template"."document_type" IS '전표유형';
COMMENT ON COLUMN "document_template"."processing_type" IS '처리유형';
COMMENT ON COLUMN "document_template"."order_item_status" IS '주문항목 상태';
COMMENT ON COLUMN "document_template"."charge_status" IS '청구 상태';
COMMENT ON COLUMN "document_template"."contract_status" IS '계약 상태';
COMMENT ON COLUMN "document_template"."service_flow_status" IS '서비스 플로우 상태';
COMMENT ON COLUMN "document_template"."logistics_status" IS '물류 상태';
COMMENT ON COLUMN "document_template"."create_time" IS '생성 일시';
COMMENT ON COLUMN "document_template"."update_time" IS '수정 일시';


CREATE TABLE IF NOT EXISTS "document_template_item"
(
    "id" SERIAL PRIMARY KEY,
    "doc_template_code" VARCHAR NOT NULL,
    "line_number" INTEGER NOT NULL,
    "account_code" VARCHAR NOT NULL,
    "ref_doc_template_code" VARCHAR,
    "posting_type" VARCHAR NOT NULL,
    "requirement_type" VARCHAR NOT NULL,
    "item_text_kor" TEXT,
    "item_text_eng" TEXT,
    "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
-- CREATE INDEX idx_doc_template_item_template_id ON document_template_item(doc_template_id);
-- CREATE INDEX idx_doc_template_item_create_time ON document_template_item(create_time);

-- 코멘트 추가
COMMENT ON TABLE "document_template_item" IS '전표 템플릿 항목';
COMMENT ON COLUMN "document_template_item"."id" IS 'id';
COMMENT ON COLUMN "document_template_item"."doc_template_code" IS '전표템플릿코드';
COMMENT ON COLUMN "document_template_item"."line_number" IS '전표항목번호';
COMMENT ON COLUMN "document_template_item"."posting_type" IS '포스팅유형(차/대)';
COMMENT ON COLUMN "document_template_item"."account_code" IS '계정코드';
COMMENT ON COLUMN "document_template_item"."ref_doc_template_code" IS '참조 전표템플릿코드';
COMMENT ON COLUMN "document_template_item"."item_text_kor" IS '한글 텍스트';
COMMENT ON COLUMN "document_template_item"."item_text_eng" IS '영문 텍스트';
COMMENT ON COLUMN "document_template_item"."requirement_type" IS '필수여부';
COMMENT ON COLUMN "document_template_item"."create_time" IS '생성 일시';
COMMENT ON COLUMN "document_template_item"."update_time" IS '수정 일시';
