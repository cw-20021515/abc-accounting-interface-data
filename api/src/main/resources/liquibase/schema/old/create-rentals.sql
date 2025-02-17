CREATE TABLE IF NOT EXISTS "material" (
    "material_id" varchar PRIMARY KEY,
    "material_series_code" varchar,
    "material_model_name" varchar,
    "material_type" varchar,
    "material_category_code" varchar,
    "installation_type" varchar,
    "filter_type" varchar,
    "feature_code" varchar,
    "create_time" timestamp(6) with time zone
);
COMMENT ON TABLE "material" IS 'Material Master table ';
COMMENT ON COLUMN "material"."material_id" IS '자재ID';
COMMENT ON COLUMN "material"."material_series_code" IS '품목코드';
COMMENT ON COLUMN "material"."material_model_name" IS '모델명';
COMMENT ON COLUMN "material"."material_type" IS '자재유형';
COMMENT ON COLUMN "material"."material_category_code" IS '제품군';
COMMENT ON COLUMN "material"."installation_type" IS '설치유형';
COMMENT ON COLUMN "material"."filter_type" IS '필터유형';
COMMENT ON COLUMN "material"."feature_code" IS '필터코드';
COMMENT ON COLUMN "material"."create_time" IS '생성시간';


CREATE TABLE IF NOT EXISTS "rental_code_master" (
    "rental_code" varchar PRIMARY KEY,
    "rental_code_name" varchar,
    "rental_code_description" varchar,
    "current_term" integer,
    "term1_period" integer,
    "term2_period" integer,
    "term3_period" integer,
    "term4_period" integer,
    "term5_period" integer,
    "contract_pricing_type" varchar,
    "contract_duration" integer,
    "commitment_duration" integer,
    "lease_type" varchar,
    "remark" varchar,
    "is_active" boolean,
    "create_time" timestamp(6) with time zone,
    "update_time" timestamp(6) with time zone
);

COMMENT ON TABLE "rental_code_master" IS 'Master table for rental information';
COMMENT ON COLUMN "rental_code_master"."rental_code" IS '렌탈 코드';
COMMENT ON COLUMN "rental_code_master"."rental_code_name" IS '렌탈코드 명';
COMMENT ON COLUMN "rental_code_master"."rental_code_description" IS '렌탈코드 명';
COMMENT ON COLUMN "rental_code_master"."current_term" IS '계약회차';
COMMENT ON COLUMN "rental_code_master"."term1_period" IS '1회차 계약';
COMMENT ON COLUMN "rental_code_master"."term2_period" IS '2회차 계약';
COMMENT ON COLUMN "rental_code_master"."term3_period" IS '3회차 계약';
COMMENT ON COLUMN "rental_code_master"."term4_period" IS '4회차 계약';
COMMENT ON COLUMN "rental_code_master"."term5_period" IS '5회차 계약';
COMMENT ON COLUMN "rental_code_master"."contract_pricing_type" IS '요금체계';
COMMENT ON COLUMN "rental_code_master"."contract_duration" IS '렌탈기간';
COMMENT ON COLUMN "rental_code_master"."commitment_duration" IS '약정기간';
COMMENT ON COLUMN "rental_code_master"."lease_type" IS '리스유형';
COMMENT ON COLUMN "rental_code_master"."remark" IS '비고';
COMMENT ON COLUMN "rental_code_master"."is_active" IS '사용여부';
COMMENT ON COLUMN "rental_code_master"."create_time" IS '생성 일시';
COMMENT ON COLUMN "rental_code_master"."update_time" IS '생성 일시';


CREATE TABLE IF NOT EXISTS "rental_distribution_master" (
    "id" SERIAL PRIMARY KEY,
    "material_series_code" varchar,
    "rental_distribution_type" varchar,
    "onetime_price" NUMERIC(38,2),
    "membership_price" NUMERIC(38,2),
    "membership_dcprice_c24" NUMERIC(38,2),
    "free_service_duration" integer,
    "start_date" date
);
COMMENT ON TABLE "rental_distribution_master" IS 'Master table for rental distribution information';
COMMENT ON COLUMN "rental_distribution_master"."id" IS 'ID';
COMMENT ON COLUMN "rental_distribution_master"."material_series_code" IS '품목코드';
COMMENT ON COLUMN "rental_distribution_master"."rental_distribution_type" IS '렌탈 분할(안분)유형';
COMMENT ON COLUMN "rental_distribution_master"."onetime_price" IS '일시불 가격';
COMMENT ON COLUMN "rental_distribution_master"."membership_price" IS '멤버십 가격';
COMMENT ON COLUMN "rental_distribution_master"."free_service_duration" IS '무상 서비스 기간';
COMMENT ON COLUMN "rental_distribution_master"."membership_dcprice_c24" IS '멤버십 2년 약정시 할인';
COMMENT ON COLUMN "rental_distribution_master"."start_date" IS '효력 시작일';



CREATE TABLE IF NOT EXISTS "rental_pricing_master" (
    "id" integer PRIMARY KEY,
    "material_series_code" varchar,
    "rental_code" varchar,
    "material_care_type" varchar,
    "price" NUMERIC(38,2),
    "currency" varchar(3),
    "tax_included" char,
    "period_type" varchar,
    "start_date" date
);

COMMENT ON TABLE "rental_pricing_master" IS 'Master table for rental pricing information';
COMMENT ON COLUMN "rental_pricing_master"."material_series_code" IS '품목코드';
COMMENT ON COLUMN "rental_pricing_master"."rental_code" IS '렌탈코드';
COMMENT ON COLUMN "rental_pricing_master"."material_care_type" IS '관리방식';
COMMENT ON COLUMN "rental_pricing_master"."price" IS '가격';
COMMENT ON COLUMN "rental_pricing_master"."currency" IS '통화';
COMMENT ON COLUMN "rental_pricing_master"."tax_included" IS 'includeTax';
COMMENT ON COLUMN "rental_pricing_master"."period_type" IS '기준';
COMMENT ON COLUMN "rental_pricing_master"."start_date" IS '생성 일시';



CREATE TABLE IF NOT EXISTS "rental_distribution_rule" (
    "id" BIGSERIAL PRIMARY KEY,
    "material_id" varchar,
    "material_series_code" varchar,
    "rental_code" varchar,
    "material_care_type" varchar,
    "lease_type" varchar,
    "commitment_duration" integer,
    "adjusted_commitment_duration" integer,
    "dist_value_m01" NUMERIC(38,2),
    "dist_value_r01" NUMERIC(38,2),
    "dist_value_r02" NUMERIC(38,2),
    "dist_value_r03" NUMERIC(38,2),
    "dist_value_s01" NUMERIC(38,2),
    "dist_ratio_m01" NUMERIC(38,2),
    "dist_ratio_r01" NUMERIC(38,2),
    "dist_ratio_r02" NUMERIC(38,2),
    "dist_ratio_r03" NUMERIC(38,2),
    "dist_ratio_s01" NUMERIC(38,2),
    "dist_price_m01" NUMERIC(38,2),
    "dist_price_r01" NUMERIC(38,2),
    "dist_price_r02" NUMERIC(38,2),
    "dist_price_r03" NUMERIC(38,2),
    "dist_price_s01" NUMERIC(38,2),
    "start_date" date,
    "create_time" timestamp(6) with time zone,
    "update_time" timestamp(6) with time zone,

    CONSTRAINT unique_distribution_rule UNIQUE (material_id, rental_code, material_care_type, start_date)
);

COMMENT ON TABLE "rental_distribution_rule" IS 'Rules table for rental distribution';
COMMENT ON COLUMN "rental_distribution_rule"."id" IS '아이디';
COMMENT ON COLUMN "rental_distribution_rule"."material_id" IS '자재ID';
COMMENT ON COLUMN "rental_distribution_rule"."material_series_code" IS '품목코드';
COMMENT ON COLUMN "rental_distribution_rule"."rental_code" IS '렌탈 코드';
COMMENT ON COLUMN "rental_distribution_rule"."material_care_type" IS '제품 관리방식';
COMMENT ON COLUMN "rental_distribution_rule"."lease_type" IS '회계처리';
COMMENT ON COLUMN "rental_distribution_rule"."commitment_duration" IS '약정기간';
COMMENT ON COLUMN "rental_distribution_rule"."adjusted_commitment_duration" IS '조정 약정기간';
COMMENT ON COLUMN "rental_distribution_rule"."start_date" IS '효력 시작일';
COMMENT ON COLUMN rental_distribution_rule.create_time IS '생성 일시';
COMMENT ON COLUMN rental_distribution_rule.update_time IS '수정 일시';



CREATE TABLE IF NOT EXISTS "rental_asset_depreciation_master" (
    "id" SERIAL PRIMARY KEY,
    "material_id" VARCHAR NOT NULL,
    "useful_life" INT,
    "salvage_value" NUMERIC(38,2) NOT NULL,
    "currency" VARCHAR NOT NULL,
    "depreciation_method" VARCHAR NOT NULL,
    "start_date" DATE
);
CREATE SEQUENCE IF NOT EXISTS "rental_asset_depreciation_master_seq"
    START WITH 1
    INCREMENT BY 50
    MINVALUE 1
    CACHE 1;

COMMENT ON TABLE "rental_asset_depreciation_master" IS '렌탈자산 상각 마스터';
COMMENT ON COLUMN "rental_asset_depreciation_master"."id" IS 'id';
COMMENT ON COLUMN "rental_asset_depreciation_master"."material_id" IS '자재ID';
COMMENT ON COLUMN "rental_asset_depreciation_master"."useful_life" IS '내용연수';
COMMENT ON COLUMN "rental_asset_depreciation_master"."salvage_value" IS '잔존가치';
COMMENT ON COLUMN "rental_asset_depreciation_master"."currency" IS '통화';
COMMENT ON COLUMN "rental_asset_depreciation_master"."depreciation_method" IS '상각방법';
COMMENT ON COLUMN "rental_asset_depreciation_master"."start_date" IS '시작일';


CREATE TABLE IF NOT EXISTS "rental_asset_history" (
    "id" SERIAL PRIMARY KEY,
    "serial_number" VARCHAR NOT NULL,
    "material_id" VARCHAR NOT NULL,
    "depreciation_count" INT,
    "depreciation_date" DATE,
    "acquisition_cost" NUMERIC(38,2) NOT NULL,
    "depreciation_expense" NUMERIC(38,2),
    "accumulated_depreciation" NUMERIC(38,2),
    "book_value" NUMERIC(38,2) NOT NULL,
    "contract_id" VARCHAR NOT NULL,
    "contract_date" DATE NOT NULL,
    "contract_status" VARCHAR NOT NULL,
    "order_id" VARCHAR NOT NULL,
    "order_item_id" VARCHAR NOT NULL,
    "customer_id" VARCHAR NOT NULL,
    "event_type" VARCHAR NOT NULL,
    "hash" VARCHAR NOT NULL,
    "create_time" timestamp(6) with time zone
);
CREATE SEQUENCE IF NOT EXISTS "rental_asset_history_seq"
    START WITH 1
    INCREMENT BY 50
    MINVALUE 1
    CACHE 1;

COMMENT ON TABLE "rental_asset_history" IS '렌탈자산 이력';
COMMENT ON COLUMN "rental_asset_history"."id" IS 'id';
COMMENT ON COLUMN "rental_asset_history"."serial_number" IS '시리얼번호';
COMMENT ON COLUMN "rental_asset_history"."material_id" IS '자재ID';
COMMENT ON COLUMN "rental_asset_history"."depreciation_count" IS '감가상각회차';
COMMENT ON COLUMN "rental_asset_history"."depreciation_date" IS '감가상각일';
COMMENT ON COLUMN "rental_asset_history"."acquisition_cost" IS '취득원가';
COMMENT ON COLUMN "rental_asset_history"."depreciation_expense" IS '감가상각비';
COMMENT ON COLUMN "rental_asset_history"."accumulated_depreciation" IS '감가상각누계액';
COMMENT ON COLUMN "rental_asset_history"."book_value" IS '장부가액';
COMMENT ON COLUMN "rental_asset_history"."contract_id" IS '계약ID';
COMMENT ON COLUMN "rental_asset_history"."contract_date" IS '계약일';
COMMENT ON COLUMN "rental_asset_history"."contract_status" IS '계약상태';
COMMENT ON COLUMN "rental_asset_history"."order_id" IS '주문ID';
COMMENT ON COLUMN "rental_asset_history"."order_item_id" IS '주문아이템ID';
COMMENT ON COLUMN "rental_asset_history"."customer_id" IS '고객ID';
COMMENT ON COLUMN "rental_asset_history"."event_type" IS '이벤트 구분';
COMMENT ON COLUMN "rental_asset_history"."hash" IS '해시 값(중복체크)';
COMMENT ON COLUMN "rental_asset_history"."create_time" IS '생성 일시';


CREATE TABLE IF NOT EXISTS "rental_asset_depreciation_schedule" (
    "id" SERIAL PRIMARY KEY,
    "serial_number" VARCHAR NOT NULL,
    "depreciation_count" INT NOT NULL,
    "depreciation_date" DATE NOT NULL,
    "currency" VARCHAR NOT NULL,
    "beginning_book_value" NUMERIC(38,2) NOT NULL,
    "depreciation_expense" NUMERIC(38,2) NOT NULL,
    "ending_book_value" NUMERIC(38,2) NOT NULL,
    "accumulated_depreciation" NUMERIC(38,2) NOT NULL,
    "create_time" timestamp(6) with time zone
);
CREATE SEQUENCE IF NOT EXISTS "rental_asset_depreciation_schedule_seq"
    START WITH 1
    INCREMENT BY 50
    MINVALUE 1
    CACHE 1;

COMMENT ON TABLE "rental_asset_depreciation_schedule" IS '렌탈자산 이력';
COMMENT ON COLUMN "rental_asset_depreciation_schedule"."id" IS 'id';
COMMENT ON COLUMN "rental_asset_depreciation_schedule"."serial_number" IS '시리얼번호';
COMMENT ON COLUMN "rental_asset_depreciation_schedule"."depreciation_count" IS '감가상각회차';
COMMENT ON COLUMN "rental_asset_depreciation_schedule"."depreciation_date" IS '감가상각일';
COMMENT ON COLUMN "rental_asset_depreciation_schedule"."currency" IS '통화';
COMMENT ON COLUMN "rental_asset_depreciation_schedule"."beginning_book_value" IS '기초 장부가액';
COMMENT ON COLUMN "rental_asset_depreciation_schedule"."depreciation_expense" IS '감가상각비';
COMMENT ON COLUMN "rental_asset_depreciation_schedule"."ending_book_value" IS '기말 장부가액';
COMMENT ON COLUMN "rental_asset_depreciation_schedule"."accumulated_depreciation" IS '감가상각누계액';
COMMENT ON COLUMN "rental_asset_depreciation_schedule"."create_time" IS '생성 일시';

-- 금융 렌탈자산 상각 스케줄 테이블 생성 및 주석 추가
create table if not exists "rental_financial_depreciation_schedule" (
    "id" varchar(50) primary key,                             -- 테이블 id
    "tx_id" varchar(50) not null,                        -- 그룹 id
    "order_item_id" varchar(50) not null,                -- 주문 아이템 id
    "contract_id" varchar(50) not null,                -- 계약 id
    "depreciation_count" int,                            -- 감가삼각 회차
    "depreciation_year_month" varchar(10),                -- 년월
    "depreciation_bill_year_month" varchar(10),           -- 청구 년월
    "currency" varchar(10),                               -- 통화
    "depreciation_rental_amount" numeric(15, 4),         -- 렌탈료
    "depreciation_book_value" numeric(15, 4),            -- 장부 금액
    "depreciation_present_value" numeric(15, 4),         -- 현재 가치(pv)
    "depreciation_current_difference" numeric(15, 4),    -- 현 할차
    "depreciation_interest_income" numeric(15, 4),       -- 이자 수익
    "create_time" timestamp(6) with time zone    -- 생성 시간
);

comment on table "rental_financial_depreciation_schedule" is '금융 렌탈자산 상각 스케줄';
comment on column "rental_financial_depreciation_schedule"."id" is '테이블 id';
comment on column "rental_financial_depreciation_schedule"."tx_id" is '그룹 id';
comment on column "rental_financial_depreciation_schedule"."order_item_id" is '주문 아이템 id';
comment on column "rental_financial_depreciation_schedule"."contract_id" is '계약 id';
comment on column "rental_financial_depreciation_schedule"."depreciation_count" is '감가삼각 회차';
comment on column "rental_financial_depreciation_schedule"."depreciation_year_month" is '년월';
comment on column "rental_financial_depreciation_schedule"."depreciation_bill_year_month" is '청구 년월';
comment on column "rental_financial_depreciation_schedule"."currency" is '통화';
comment on column "rental_financial_depreciation_schedule"."depreciation_rental_amount" is '렌탈료';
comment on column "rental_financial_depreciation_schedule"."depreciation_book_value" is '장부 금액';
comment on column "rental_financial_depreciation_schedule"."depreciation_present_value" is '현재 가치(pv)';
comment on column "rental_financial_depreciation_schedule"."depreciation_current_difference" is '현 할차';
comment on column "rental_financial_depreciation_schedule"."depreciation_interest_income" is '이자 수익';
comment on column "rental_financial_depreciation_schedule"."create_time" is '생성 시간';

-- 렌탈 금융자산 이력 테이블 생성 및 주석 추가
create table if not exists "rental_financial_depreciation_history" (
    "id" varchar(50) primary key,                        -- 테이블 id
    "tx_id" varchar(50) null,                            -- 그룹 id
    "doc_hash_code" varchar(64),                         -- 해시 코드
    "order_id" varchar(50) not null,                     -- 주문 id
    "order_item_id" varchar(50) not null,                -- 주문 아이템 id
    "customer_id" varchar(50) not null,                  -- 고객 id
    "serial_number" varchar(100),                        -- 시리얼 번호
    "contract_id" varchar(50),                           -- 계약 id
    "material_id" varchar(50),                           -- 자재 id
    "base_date" date not null,                           -- 기준일
    "contract_date" date,                                -- 계약일
    "material_series_code" varchar(50),                  -- 품목 코드
    "contract_end_date" date,                            -- 만기일
    "contract_period" int,                               -- 약정 개월
    "interest_rate" numeric(5, 2),                       -- 이자율
    "rental_amount" numeric(15, 4),                      -- 렌탈료
    "rental_amount_for_goods" numeric(15, 4),            -- 렌탈료(재화)
    "depreciation_count" int,                            -- 감가삼각 회차
    "depreciation_year_month" varchar(10),                -- 년월
    "depreciation_bill_year_month" varchar(10),           -- 청구 년월
    "currency" varchar(10),                               -- 통화
    "depreciation_book_value" numeric(15, 4),            -- 장부 금액
    "depreciation_present_value" numeric(15, 4),         -- 현재 가치(pv)
    "depreciation_current_difference" numeric(15, 4),    -- 현 할차
    "depreciation_interest_income" numeric(15, 4),       -- 이자 수익
    "cumulative_interest_income" numeric(15, 4),         -- 이자 수익(누계)
    "initial_book_value" numeric(15, 4),                 -- 최초 장부 금액
    "initial_present_value" numeric(15, 4),              -- 최초 현재 가치(pv)
    "initial_current_difference" numeric(15, 4),          -- 최초 현 할차
    "update_time" timestamp(6) with time zone,                             -- 수정일
    "create_time" timestamp(6) with time zone,   -- 생성일
    "rental_event_type" varchar(50)                        -- 이벤트 구분
);


comment on table "rental_financial_depreciation_history" is '렌탈 금융자산 이력';
comment on column "rental_financial_depreciation_history"."id" is '테이블 id';
comment on column "rental_financial_depreciation_history"."tx_id" is '그룹 id';
comment on column "rental_financial_depreciation_history"."doc_hash_code" is '해시 코드';
comment on column "rental_financial_depreciation_history"."order_id" is '주문 id';
comment on column "rental_financial_depreciation_history"."order_item_id" is '주문 아이템 id';
comment on column "rental_financial_depreciation_history"."customer_id" is '고객 id';
comment on column "rental_financial_depreciation_history"."serial_number" is '시리얼 번호';
comment on column "rental_financial_depreciation_history"."contract_id" is '계약 id';
comment on column "rental_financial_depreciation_history"."material_id" is '자재 id';
comment on column "rental_financial_depreciation_history"."base_date" is '기준일';
comment on column "rental_financial_depreciation_history"."contract_date" is '계약일';
comment on column "rental_financial_depreciation_history"."material_series_code" is '품목 코드';
comment on column "rental_financial_depreciation_history"."contract_end_date" is '만기일';
comment on column "rental_financial_depreciation_history"."contract_period" is '약정 개월';
comment on column "rental_financial_depreciation_history"."initial_book_value" is '최초 장부 금액';
comment on column "rental_financial_depreciation_history"."interest_rate" is '이자율';
comment on column "rental_financial_depreciation_history"."rental_amount" is '렌탈료';
comment on column "rental_financial_depreciation_history"."depreciation_count" is '감가삼각 회차';
comment on column "rental_financial_depreciation_history"."depreciation_year_month" is '년월';
comment on column "rental_financial_depreciation_history"."depreciation_bill_year_month" is '청구 년월';
comment on column "rental_financial_depreciation_history"."currency" is '통화';
comment on column "rental_financial_depreciation_history"."depreciation_book_value" is '장부 금액';
comment on column "rental_financial_depreciation_history"."depreciation_present_value" is '현재 가치(pv)';
comment on column "rental_financial_depreciation_history"."depreciation_current_difference" is '현 할차';
comment on column "rental_financial_depreciation_history"."depreciation_interest_income" is '이자 수익';
comment on column "rental_financial_depreciation_history"."cumulative_interest_income" is '이자 수익(누계)';
comment on column "rental_financial_depreciation_history"."initial_present_value" is '최초 현재 가치(pv)';
comment on column "rental_financial_depreciation_history"."initial_current_difference" is '최초 현 할차';
comment on column "rental_financial_depreciation_history"."rental_amount_for_goods" is '렌탈료(재화)';
comment on column "rental_financial_depreciation_history"."update_time" is '수정일';
comment on column "rental_financial_depreciation_history"."create_time" is '생성일';
comment on column "rental_financial_depreciation_history"."rental_event_type" is '이벤트 구분';

create table if not exists "rental_financial_interest_master" (
    "id" bigserial primary key,                      -- 고유 식별자
    "target_month" varchar(7) not null,           -- 기준월 (예: '2024-07')
    "interest_rate" numeric(5, 2) not null,          -- 이자율 (소수점 두 자리까지)
    "create_time" timestamp(6) with time zone -- 등록일시 (자동 생성)
);

-- comment 추가
comment on table "rental_financial_interest_master" is '금융리스 이자율 관리 테이블';
comment on column "rental_financial_interest_master"."id" is '고유 식별자';
comment on column "rental_financial_interest_master"."target_month" is '기준월 (예: 2024-07)';
comment on column "rental_financial_interest_master"."interest_rate" is '이자율 (소수점 두 자리까지)';
comment on column "rental_financial_interest_master"."create_time" is '등록일시 (데이터 생성 시간)';