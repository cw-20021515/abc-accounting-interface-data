CREATE TABLE IF NOT EXISTS "document"
(
    "id" varchar PRIMARY KEY,
    "version" bigint,
    "doc_hash" varchar UNIQUE,
    "doc_type" varchar,
    "doc_status" varchar,
    "workflow_status" varchar,
    "workflow_id" varchar,
    "document_date" date,
    "posting_date" date,
    "entry_date" date,
    "fiscal_year" int,
    "fiscal_month" int,
    "company_code" varchar,
    "tx_currency" varchar,
    "tx_amount" NUMERIC(38,2),
    "currency" varchar,
    "amount" NUMERIC(38,2),
    "reference" varchar,
    "text" varchar,
    "is_deleted" char,
    "create_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "created_by" varchar,
    "update_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" varchar
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
COMMENT ON COLUMN "document"."fiscal_month" IS '회계기간';
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


CREATE TABLE IF NOT EXISTS "document_origin" (
    "id" serial PRIMARY KEY,
    "doc_id" varchar,
    "doc_template_code" varchar,
    "biz_system" varchar,
    "biz_tx_id" varchar,
    "biz_process" varchar,
    "biz_event" varchar,
    "accounting_event" varchar
);

COMMENT ON TABLE "document_origin" IS '전표비즈니스';
COMMENT ON COLUMN "document_origin"."id" IS 'ID';
COMMENT ON COLUMN "document_origin"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_origin"."doc_template_code" IS '전표템플릿코드';
COMMENT ON COLUMN "document_origin"."biz_system" IS '비즈니스시스템';
COMMENT ON COLUMN "document_origin"."biz_tx_id" IS '비즈니스 거래ID';
COMMENT ON COLUMN "document_origin"."biz_process" IS '비즈니스 프로세스';
COMMENT ON COLUMN "document_origin"."biz_event" IS '비즈니스 이벤트';
COMMENT ON COLUMN "document_origin"."accounting_event" IS '회계 이벤트';


CREATE TABLE IF NOT EXISTS "document_relation" (
    "id" varchar PRIMARY KEY,
    "doc_id" varchar,
    "ref_doc_id" varchar,
    "relation_type" varchar,
    "reason" varchar,
    "create_time" timestamp DEFAULT (CURRENT_TIMESTAMP)
);

COMMENT ON TABLE "document_relation" IS '전표관계';
COMMENT ON COLUMN "document_relation"."id" IS 'ID';
COMMENT ON COLUMN "document_relation"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_relation"."relation_type" IS '관계유형';
COMMENT ON COLUMN "document_relation"."ref_doc_id" IS '참조전표ID';
COMMENT ON COLUMN "document_relation"."reason" IS '사유';
COMMENT ON COLUMN "document_relation"."create_time" IS '생성시간';


CREATE TABLE IF NOT EXISTS "document_item" (
    "id" varchar PRIMARY KEY,
    "version" bigint,
    "doc_item_status" varchar,
    "doc_id" varchar,
    "line_number" integer,
    "account_code" varchar,
    "posting_type" char,
    "company_code" varchar,
    "tx_currency" varchar,
    "tx_amount" NUMERIC(38,2),
    "currency" varchar,
    "amount" NUMERIC(38,2),
    "exchange_rate_id" varchar,
    "text" varchar,
    "doc_template_code" varchar,
    "cost_center" varchar,
    "profit_center" varchar,
    "segment" varchar,
    "project" varchar,
    "customer_id" varchar,
    "vendor_id" varchar,
    "create_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "created_by" varchar,
    "update_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" varchar
);

COMMENT ON TABLE "document_item" IS '전표항목';
COMMENT ON COLUMN "document_item"."id" IS '전표항목ID';
COMMENT ON COLUMN "document_item"."version" IS '버전';
COMMENT ON COLUMN "document_item"."doc_item_status" IS '전표항목상태';
COMMENT ON COLUMN "document_item"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_item"."line_number" IS '라인번호';
COMMENT ON COLUMN "document_item"."account_code" IS '계정코드';
COMMENT ON COLUMN "document_item"."posting_type" IS '전기유형(차/대)';
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



CREATE TABLE IF NOT EXISTS "document_item_attribute" (
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


CREATE TABLE IF NOT EXISTS "document_history" (
    "id" serial PRIMARY KEY,
    "doc_id" varchar,
    "version" bigint,
    "doc_hash" varchar,
    "doc_type" varchar,
    "doc_status" varchar,
    "workflow_status" varchar,
    "workflow_id" varchar,
    "document_date" date,
    "posting_date" date,
    "entry_date" date,
    "fiscal_year" int,
    "fiscal_month" int,
    "company_code" varchar,
    "tx_currency" varchar,
    "tx_amount" NUMERIC(38,2),
    "currency" varchar,
    "amount" NUMERIC(38,2),
    "reference" varchar,
    "text" varchar,
    "is_deleted" char,
    "create_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "created_by" varchar,
    "update_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" varchar
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


CREATE TABLE IF NOT EXISTS "document_item_history" (
    "id" serial PRIMARY KEY,
    "doc_item_id" varchar,
    "version" bigint,
    "doc_item_status" varchar,
    "doc_id" varchar,
    "line_number" integer,
    "account_code" varchar,
    "posting_type" char,
    "company_code" varchar,
    "tx_currency" varchar,
    "tx_amount" NUMERIC(38,2),
    "currency" varchar,
    "amount" NUMERIC(38,2),
    "exchange_rate_id" varchar,
    "text" varchar,
    "cost_center" varchar,
    "profit_center" varchar,
    "segment" varchar,
    "project" varchar,
    "customer_id" varchar,
    "vendor_id" varchar,
    "create_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "created_by" varchar,
    "update_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" varchar
);

COMMENT ON TABLE "document_item_history" IS '전표항목이력';
COMMENT ON COLUMN "document_item_history"."id" IS 'ID';
COMMENT ON COLUMN "document_item_history"."doc_item_id" IS '전표항목ID';
COMMENT ON COLUMN "document_item_history"."version" IS '버전';
COMMENT ON COLUMN "document_item_history"."doc_item_status" IS '전표항목상태';
COMMENT ON COLUMN "document_item_history"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_item_history"."line_number" IS '라인번호';
COMMENT ON COLUMN "document_item_history"."account_code" IS '계정코드';
COMMENT ON COLUMN "document_item_history"."posting_type" IS '전기유형(차/대)';
COMMENT ON COLUMN "document_item_history"."company_code" IS '회사코드';
COMMENT ON COLUMN "document_item_history"."tx_currency" IS '거래통화';
COMMENT ON COLUMN "document_item_history"."tx_amount" IS '거래금액';
COMMENT ON COLUMN "document_item_history"."currency" IS '회계통화';
COMMENT ON COLUMN "document_item_history"."amount" IS '회계금액';
COMMENT ON COLUMN "document_item_history"."exchange_rate_id" IS '환율ID';
COMMENT ON COLUMN "document_item_history"."text" IS '텍스트';
COMMENT ON COLUMN "document_item_history"."cost_center" IS '코스트센터';
COMMENT ON COLUMN "document_item_history"."profit_center" IS '손익센터';
COMMENT ON COLUMN "document_item_history"."segment" IS '세그먼트';
COMMENT ON COLUMN "document_item_history"."project" IS '프로젝트';
COMMENT ON COLUMN "document_item_history"."customer_id" IS '고객ID';
COMMENT ON COLUMN "document_item_history"."vendor_id" IS '거래처ID';
COMMENT ON COLUMN "document_item_history"."create_time" IS '생성시간';
COMMENT ON COLUMN "document_item_history"."update_time" IS '수정시간';


CREATE TABLE IF NOT EXISTS "document_item_relation" (
    "id" varchar PRIMARY KEY,
    "doc_item_id" varchar,
    "ref_doc_item_id" varchar,
    "relation_type" varchar,
    "reason" varchar,
    "ref_amount" NUMERIC(38,2),
    "amount" NUMERIC(38,2),
    "create_time" timestamp DEFAULT (CURRENT_TIMESTAMP)
);

COMMENT ON TABLE "document_item_relation" IS '전표항목관계';
COMMENT ON COLUMN "document_item_relation"."id" IS 'ID';
COMMENT ON COLUMN "document_item_relation"."doc_item_id" IS '전표항목ID';
COMMENT ON COLUMN "document_item_relation"."ref_doc_item_id" IS '참조전표항목ID';
COMMENT ON COLUMN "document_item_relation"."relation_type" IS '관계유형';
COMMENT ON COLUMN "document_item_relation"."reason" IS '사유';
COMMENT ON COLUMN "document_item_relation"."ref_amount" IS '참조금액';
COMMENT ON COLUMN "document_item_relation"."amount" IS '금액';
COMMENT ON COLUMN "document_item_relation"."create_time" IS '생성시간';


CREATE TABLE IF NOT EXISTS "document_note" (
    "id" serial PRIMARY KEY,
    "doc_id" varchar,
    "is_deleted" char,
    "contents" text,
    "create_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "created_by" varchar,
    "update_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" varchar
);

COMMENT ON COLUMN "document_note"."id" IS 'ID';
COMMENT ON COLUMN "document_note"."doc_id" IS '전표ID';
COMMENT ON COLUMN "document_note"."is_deleted" IS '삭제여부';
COMMENT ON COLUMN "document_note"."contents" IS '내용';
COMMENT ON COLUMN "document_note"."create_time" IS '생성시간';
COMMENT ON COLUMN "document_note"."created_by" IS '생성자';
COMMENT ON COLUMN "document_note"."update_time" IS '수정시간';
COMMENT ON COLUMN "document_note"."updated_by" IS '수정자';


CREATE TABLE IF NOT EXISTS "document_attachment" (
    "id" serial PRIMARY KEY,
    "doc_id" varchar,
    "file_name" varchar,
    "internal_path" varchar,
    "create_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "created_by" varchar,
    "update_time" timestamp DEFAULT (CURRENT_TIMESTAMP),
    "updated_by" varchar
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
