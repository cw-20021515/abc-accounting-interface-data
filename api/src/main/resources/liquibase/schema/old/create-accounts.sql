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

