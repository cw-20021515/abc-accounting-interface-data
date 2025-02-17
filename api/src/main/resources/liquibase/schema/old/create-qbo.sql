CREATE TABLE IF NOT EXISTS "company"
(
    "code"        varchar(255) not null primary key,
    "name"        varchar(255),
    "description" text,
    "country"     varchar(255),
    "currency"    varchar(255),
    "create_time" timestamp(6) with time zone,
    "is_active"   char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
);

comment on column "company"."code" is '사업장 ID';
comment on column "company"."name" is '사업장 이름';
comment on column "company"."description" is '설명';
comment on column "company"."country" is '사업장 소속 국가';
comment on column "company"."currency" is '사업장에서 사용하는 통화';
comment on column "company"."create_time" is '생성 일시';
comment on column "company"."is_active" is 'Active 여부';


CREATE TABLE IF NOT EXISTS "credential"
(
    "realm_id"                  varchar not null primary key,
    "client_id"                 varchar,
    "client_secret"             varchar,
    "company_code"              varchar,
    "email"                     varchar,
    "given_name"                varchar,
    "sandbox_name"              varchar,
    "scope"                     varchar,
    "sub"                       varchar,
    "token_type"                varchar,
    "access_token"              text,
    "basic_token"               text,
    "id_token"                  text,
    "refresh_token"             text,
    "access_token_expire_time"  timestamp(6) with time zone,
    "access_token_issued_time"  timestamp(6) with time zone,
    "create_time"               timestamp(6) with time zone,
    "refresh_token_expire_time" timestamp(6) with time zone,
    "refresh_token_issued_time" timestamp(6) with time zone,
    "update_time"               timestamp(6) with time zone
);

comment on column "credential"."access_token_expire_time" is '액세스 토큰 만료 일시';
comment on column "credential"."access_token_issued_time" is '액세스 토큰 생성 일시';
comment on column "credential"."create_time" is '생성 일시';
comment on column "credential"."refresh_token_expire_time" is '리프레시 토큰 만료 일시';
comment on column "credential"."refresh_token_issued_time" is '리프레시 토큰 만료 일시';
comment on column "credential"."update_time" is '업데이트 일시';
comment on column "credential"."access_token" is '액세스 토큰';
comment on column "credential"."basic_token" is 'BASE64 code';
comment on column "credential"."client_id" is 'oauth2 인증 client id';
comment on column "credential"."client_secret" is 'oauth2 인증 secret key';
comment on column "credential"."company_code" is '사업장 코드';
comment on column "credential"."email" is 'email';
comment on column "credential"."given_name" is 'givenName';
comment on column "credential"."id_token" is 'unknown';
comment on column "credential"."realm_id" is '회사 고유 식별자';
comment on column "credential"."refresh_token" is '리프레시 토큰';
comment on column "credential"."sandbox_name" is 'credential 식별 이름';
comment on column "credential"."scope" is '권한 범위';
comment on column "credential"."sub" is 'sub';
comment on column "credential"."token_type" is '토큰 유형';


CREATE TABLE IF NOT EXISTS "item_create_template"
(
    "template_id"          varchar not null primary key,
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

comment on column "item_create_template"."asset_account_code" is '자산 계정 과목 코드';
comment on column "item_create_template"."asset_account_name" is '자산 계정 과목 이름';
comment on column "item_create_template"."associated_type" is 'QBO 에 등록될 경우 생성된 연관 유형(예 : item.NonInventory';
comment on column "item_create_template"."create_category" is '아이템 카테고리';
comment on column "item_create_template"."create_type" is '아이템 세부 항목';
comment on column "item_create_template"."document_type" is '전표 유형';
comment on column "item_create_template"."expense_account_code" is '매출원가 계정 과목 코드';
comment on column "item_create_template"."expense_account_name" is '매출원가 계정 과목 이름';
comment on column "item_create_template"."income_account_code" is '매출 계정 과목 코드';
comment on column "item_create_template"."income_account_name" is '매출 계정 과목 이름';
comment on column "item_create_template"."management_unit" is '관리 단위 (SKU)';



CREATE TABLE IF NOT EXISTS "associated_item"
(
    "itemId"                varchar not null primary key,
    "template_id"           varchar,
    "company_code"          varchar,
    "association_map_id"    varchar,
    "material_id"          varchar,
    "description"          varchar,
    "rate_percent"         NUMERIC(38,2),
    "unit_price"           NUMERIC(38,2),
    "create_time"          timestamp(6) with time zone,
    "update_time"          timestamp(6) with time zone,
    "is_active"   char not null check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
);

-- comment on column item.asset_account_code is '자산 계정 코드';
-- comment on column item.asset_account_name is '자산 계정 이름';
-- comment on column item.associated_name is 'QBO 에 등록될 Unique 이름';
-- comment on column item.associated_type is 'QBO 에 등록될 경우 생성된 연관 유형(예 : NonInventory';
-- comment on column item.company_code is '사업장 코드';
-- comment on column item.create_category is '아이템 카테고리';
-- comment on column item.create_time is '생성 시간';
-- comment on column item.create_type is '아이템 세부 항목';
-- comment on column item.document_type is '전표 유형';
-- comment on column item.expense_account_code is '매출원가 계정 코드';
-- comment on column item.expense_account_name is '매출원가 계정 이름';
-- comment on column item.income_account_code is '매출 계정 코드';
-- comment on column item.income_account_name is '매출 계정 이름';
-- comment on column item.management_unit is '관리 단위 (SKU)';
-- comment on column item.material_id is 'QBO 의 SKU에 할당될 ID';
-- comment on column item.update_time is '갱신 시간';



CREATE TABLE IF NOT EXISTS "association_map"
(
    "association_id"     varchar(255) not null primary key,
    "associated_entity" varchar(255),
    "associated_id"     varchar(255),
    "associated_name"   varchar(255),
    "associated_type"   varchar(255),
    "associated_value"  varchar(255),
    "company_code"      varchar(255),
    "create_time"       timestamp(6) with time zone,
    "is_active"         char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar])),
    "realm_id"          varchar(255),
    "sandbox_name"      varchar(255),
    "sync_token"        varchar(255),
    "update_time"       timestamp(6) with time zone
);


-- comment on column association_map.association_id is 'id';
-- comment on column association_map.associated_id is 'QBO 에 등록될 경우 생성된 ID';
-- comment on column association_map.associated_entity is 'QBO 에 등록될 경우 생성된 객체의 이름 (예 : Vendor)';
-- comment on column association_map.associated_name is 'QBO 에 등록될 경우 생성된 이름 (검색의 키값이 될수 있으므로 associatedType + associatedEntity 조합에서 Unique 해야 함 ';
-- comment on column association_map.associated_type is 'QBO 에 등록될 경우 생성된 연관 유형(예 : ItemTypeEnum::FIXED_ASSET, ';
-- comment on column association_map.associated_value is 'QBO 에 등록된 value';
-- comment on column association_map.company_code is '외부 시스템에서 생성된 회사(이외에도 여럿 작업공간들)의 이름';
-- comment on column association_map.create_time is '생성 일시';
-- comment on column association_map.is_active is '사용 여부';
-- comment on column association_map.realm_id is '외부 시스템에서 발행된 workspace 의 ID (회사 또는 환경별 추적 ID)';
-- comment on column association_map.sandbox_name is 'QBO workspace name';
-- comment on column association_map.sync_token is 'QBO 에 등록될 경우 생성된 동기화 번호';
-- comment on column association_map.update_time is '생성 일시';
