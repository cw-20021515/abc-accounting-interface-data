CREATE TABLE IF NOT EXISTS  "department"
(
    "id" varchar PRIMARY KEY,
    "company_code" varchar NOT NULL,
    "code" varchar NOT NULL,
    "parent_id" varchar,
    "kor_name" varchar NOT NULL,
    "eng_name" varchar NOT NULL,
    "level" int,
    "description" varchar NOT NULL,
    "is_active" varchar(1) DEFAULT 'Y',
    "create_time" timestamp,
    "update_time" timestamp
);
COMMENT ON COLUMN "department"."id" IS '부서 ID';
COMMENT ON COLUMN "department"."company_code" IS '부서가 속한 회사코드';
COMMENT ON COLUMN "department"."code" IS '식별 코드';
COMMENT ON COLUMN "department"."parent_id" IS '상위 부서 ID';
COMMENT ON COLUMN "department"."kor_name" IS '부서 이름';
COMMENT ON COLUMN "department"."eng_name" IS '부서 이름(영문)';
COMMENT ON COLUMN "department"."level" IS '조직 레벨';
COMMENT ON COLUMN "department"."description" IS '부서 설명';
COMMENT ON COLUMN "department"."is_active" IS '활성화 여부';
COMMENT ON COLUMN "department"."create_time" IS '생성 날짜';
COMMENT ON COLUMN "department"."update_time" IS '갱신 날짜';


CREATE TABLE IF NOT EXISTS "cost_center"
(
    "id" varchar PRIMARY KEY,
    "company_code" varchar NOT NULL,
    "code" varchar NOT NULL,
    "category" varchar NOT NULL,
    "parent_id" varchar,
    "profit_center_id" varchar,
    "name" varchar NOT NULL,
    "department_id" varchar NOT NULL,
    "description" varchar NOT NULL,
    "valid_from_time" timestamp,
    "valid_to_time" timestamp,
    "create_time" timestamp,
    "update_time" timestamp
);

COMMENT ON COLUMN "cost_center"."id" IS 'cost-center ID';
COMMENT ON COLUMN "cost_center"."company_code" IS '회사코드';
COMMENT ON COLUMN "cost_center"."code" IS 'cost_center 식별 코드';
COMMENT ON COLUMN "cost_center"."category" IS 'cost center 유형';
COMMENT ON COLUMN "cost_center"."parent_id" IS 'cost_center 부모 id';
COMMENT ON COLUMN "cost_center"."profit_center_id" IS '이익 센터 코드';
COMMENT ON COLUMN "cost_center"."name" IS 'cost center 이름';
COMMENT ON COLUMN "cost_center"."department_id" IS '소속 부서';
COMMENT ON COLUMN "cost_center"."description" IS '상세 설명';
COMMENT ON COLUMN "cost_center"."valid_from_time" IS '유효 시작일';
COMMENT ON COLUMN "cost_center"."valid_to_time" IS '유효 종료일';
COMMENT ON COLUMN "cost_center"."create_time" IS '생성 날짜';
COMMENT ON COLUMN "cost_center"."update_time" IS '갱신 날짜';

CREATE TABLE IF NOT EXISTS "profit_center"
(
    "id" varchar PRIMARY KEY,
    "company_code" varchar NOT NULL,
    "code" varchar NOT NULL,
    "parent_id" varchar,
    "name" varchar NOT NULL,
    "description" varchar NOT NULL,
    "segment_id" varchar,
    "valid_from_time" timestamp,
    "valid_to_time" timestamp,
    "create_time" timestamp,
    "update_time" timestamp
);
COMMENT ON COLUMN "profit_center"."id" IS 'profit_center ID';
COMMENT ON COLUMN "profit_center"."company_code" IS '회사코드';
COMMENT ON COLUMN "profit_center"."code" IS '식별 코드';
COMMENT ON COLUMN "profit_center"."parent_id" IS 'profit_center 관리 회사코드';
COMMENT ON COLUMN "profit_center"."name" IS 'profit_center center 이름';
COMMENT ON COLUMN "profit_center"."description" IS '상세 설명';
COMMENT ON COLUMN "profit_center"."segment_id" IS 'segment ID';
COMMENT ON COLUMN "profit_center"."valid_from_time" IS '유효 시작일';
COMMENT ON COLUMN "profit_center"."valid_to_time" IS '유효 종료일';
COMMENT ON COLUMN "profit_center"."create_time" IS '생성 날짜';
COMMENT ON COLUMN "profit_center"."update_time" IS '갱신 날짜';

CREATE TABLE IF NOT EXISTS "segment"
(
    "id" varchar PRIMARY KEY,
    "company_code" varchar NOT NULL,
    "code" varchar NOT NULL,
    "description" varchar NOT NULL,
    "valid_from_time" timestamp,
    "valid_to_time" timestamp,
    "create_time" timestamp,
    "update_time" timestamp
);
COMMENT ON COLUMN "segment"."id" IS 'segment ID';
COMMENT ON COLUMN "segment"."company_code" IS '회사코드';
COMMENT ON COLUMN "segment"."code" IS 'segment 식별 코드';
COMMENT ON COLUMN "segment"."description" IS '상세 설명';
COMMENT ON COLUMN "segment"."valid_from_time" IS '유효 시작일';
COMMENT ON COLUMN "segment"."valid_to_time" IS '유효 종료일';
COMMENT ON COLUMN "segment"."create_time" IS '생성 날짜';
COMMENT ON COLUMN "segment"."update_time" IS '갱신 날짜';

CREATE TABLE IF NOT EXISTS  "employee"
(
    "id" varchar NOT NULL,
    "company_code" varchar NOT NULL,
    "department_id" varchar NOT NULL,

    "role_code" varchar,
    "role_name" varchar,
    "grade_code" varchar,
    "grade_name" varchar,

    "status" varchar,

    "first_name" varchar,
    "middle_name" varchar,
    "last_name" varchar,
    "family_name" varchar,
    "name_suffix" varchar,

    "phone" varchar,
    "mobile" varchar,
    "fax" varchar,
    "email" varchar,

    "address_jsonb" jsonb,
    "create_time"           timestamp(6) with time zone,
    "update_time"           timestamp(6) with time zone,
    "is_active" varchar(1) DEFAULT 'Y',
    "remark" varchar,
    CONSTRAINT pk_employee_id PRIMARY KEY (id, company_code,department_id)
);

COMMENT ON COLUMN "employee"."id" IS '직원 사번';
COMMENT ON COLUMN "employee"."company_code" IS '회사코드';
COMMENT ON COLUMN "employee"."department_id" IS '부서 id';
COMMENT ON COLUMN "employee"."role_code" IS '직책 코드';
COMMENT ON COLUMN "employee"."role_name" IS '직책 이름(팀장,팀원,파트장등)';
COMMENT ON COLUMN "employee"."grade_code" IS '직급 코드';
COMMENT ON COLUMN "employee"."grade_name" IS '직급 이름(대리,과장,차장,부장)';
COMMENT ON COLUMN "employee"."status" IS '직원 상태(재직중 | 휴직중 | 퇴사';
COMMENT ON COLUMN "employee"."first_name" IS '이름';
COMMENT ON COLUMN "employee"."middle_name" IS 'middle name';
COMMENT ON COLUMN "employee"."last_name" IS '성';
COMMENT ON COLUMN "employee"."family_name" IS '가족 이름';
COMMENT ON COLUMN "employee"."name_suffix" IS '이름 앞에 붙는 수식 (예 : sir)';
COMMENT ON COLUMN "employee"."phone" IS '유선 연락처';
COMMENT ON COLUMN "employee"."mobile" IS '휴대폰 정보(SP(개인사업자)일 경우의 개인 휴대폰 번호)
    E.164 표준 형식의 휴대폰 번호 ( +[국가코드][지역코드][가입자 번호]
국가 : +1 (미국)
지역코드 : 5555
가입자번호 : 1234 5678
실제 입력 : +1 5555 1234 4567
';

COMMENT ON COLUMN "employee"."fax" IS 'fax 번호';
COMMENT ON COLUMN "employee"."email" IS '첫번째 이메일 주소';
COMMENT ON COLUMN "employee"."address_jsonb" IS '주소';
COMMENT ON COLUMN "employee"."is_active" IS '활성화 여부';
COMMENT ON COLUMN "employee"."remark" IS '비고';

CREATE TABLE IF NOT EXISTS "vendor"
(
    "id" varchar PRIMARY KEY,
    "company_code" varchar NOT NULL,
    "vendor_id" varchar NOT NULL,
    "category" varchar,
    "first_name" varchar,
    "middle_name" varchar,
    "last_name" varchar,
    "family_name" varchar,
    "name_suffix" varchar,
    "phone" varchar,
    "mobile" varchar,
    "fax" varchar,
    "email" varchar,
    "web_addr" varchar,
    "address_jsonb" jsonb,
    "description" varchar,
    "create_time" timestamp,
    "update_time" timestamp,
    "is_active" varchar(1) DEFAULT 'Y',
    "remark" varchar
    );
COMMENT ON COLUMN "vendor"."id" IS '객체 식별 ID';
COMMENT ON COLUMN "vendor"."category" IS '거래처 유형';
COMMENT ON COLUMN "vendor"."company_code" IS '회사코드';
COMMENT ON COLUMN "vendor"."first_name" IS '이름';
COMMENT ON COLUMN "vendor"."middle_name" IS 'vendor name';
COMMENT ON COLUMN "vendor"."last_name" IS '성';
COMMENT ON COLUMN "vendor"."family_name" IS '가족 이름';
COMMENT ON COLUMN "vendor"."name_suffix" IS '이름 앞에 붙는 수식 (예 : sir)';
COMMENT ON COLUMN "vendor"."phone" IS '유선 연락처';
COMMENT ON COLUMN "vendor"."mobile" IS '휴대폰 정보(SP(개인사업자)일 경우의 개인 휴대폰 번호)
    E.164 표준 형식의 휴대폰 번호 ( +[국가코드][지역코드][가입자 번호]
국가 : +1 (미국)
지역코드 : 5555
가입자번호 : 1234 5678
실제 입력 : +1 5555 1234 4567
';

COMMENT ON COLUMN "vendor"."fax" IS 'fax 번호';
COMMENT ON COLUMN "vendor"."email" IS '첫번째 이메일 주소';
COMMENT ON COLUMN "vendor"."web_addr" IS 'web site 주소';
COMMENT ON COLUMN "vendor"."address_jsonb" IS '주소';
COMMENT ON COLUMN "vendor"."description" IS 'vendor 상세 설명';
COMMENT ON COLUMN "vendor"."create_time" IS 'vendor 생성일시';
COMMENT ON COLUMN "vendor"."is_active" IS '활성화 여부';
COMMENT ON COLUMN "vendor"."remark" IS '추가 코멘트 작성용';

CREATE TABLE IF NOT EXISTS  "branch"
(
    "id" varchar PRIMARY KEY,
    "company_code" varchar NOT NULL,
    "name" varchar NOT NULL,
    "description" varchar NOT NULL,
    "warehouse_id" varchar,
    "phone" varchar NOT NULL,
    "time_zone" varchar NOT NULL,
    "address_jsonb" jsonb,
    "is_active" varchar(1) DEFAULT 'Y'
);


CREATE TABLE IF NOT EXISTS "payout"
(
    "id" varchar PRIMARY KEY,
    "company_code" varchar NOT NULL,
    "title" varchar,
    "description" varchar,
    "type" varchar DEFAULT 'VENDOR',
    "currency" varchar,
    "amount" decimal,
    "tax_amount" decimal,
    "total_amount" decimal,
    "document_time" timestamp,
    "entry_time" timestamp,
    "posting_time" timestamp,
    "due_date" date,
    "vendor_id" varchar,
    "employee_id" varchar,
    "department_id" varchar,

    "invoice_id" varchar,
    "purchase_order_id" varchar,
    "bill_of_lading_id" varchar,
    "approval_id" varchar,
    "approval_status" varchar,
    "create_time" timestamp,
    "remark" varchar NOT NULL,
    "is_active" varchar(1) DEFAULT 'Y'
    );
COMMENT ON COLUMN "payout"."id" IS '미지급금 ID';
COMMENT ON COLUMN "payout"."company_code" IS '회사코드';
COMMENT ON COLUMN "payout"."title" IS '미지급금 제목';
COMMENT ON COLUMN "payout"."description" IS '미지급금 상세 설명';
COMMENT ON COLUMN "payout"."type" IS '미지급금 유형
- VENDOR(업체 비용)
- EMPLOYEE(개인비용)
';

COMMENT ON COLUMN "payout"."currency" IS '통화';
COMMENT ON COLUMN "payout"."amount" IS '공급가액(세금 미포함)';
COMMENT ON COLUMN "payout"."tax_amount" IS '세액(순수 세금) 총합';
COMMENT ON COLUMN "payout"."total_amount" IS '공급가액(세금 포함)';
COMMENT ON COLUMN "payout"."document_time" IS '증빙일';
COMMENT ON COLUMN "payout"."entry_time" IS '발행일';
COMMENT ON COLUMN "payout"."posting_time" IS '전기일';
COMMENT ON COLUMN "payout"."due_date" IS '지급 기일';
COMMENT ON COLUMN "payout"."vendor_id" IS '공급 업체 코드';
COMMENT ON COLUMN "payout"."employee_id" IS '지급 요청 문서 작성자 코드';
COMMENT ON COLUMN "payout"."department_id" IS '귀속부서 코드';
COMMENT ON COLUMN "payout"."invoice_id" IS 'invoice id';
COMMENT ON COLUMN "payout"."purchase_order_id" IS '구매 주문 id';
COMMENT ON COLUMN "payout"."bill_of_lading_id" IS '선하증권 id';
COMMENT ON COLUMN "payout"."approval_id" IS '지급 승인 요청 id';
COMMENT ON COLUMN "payout"."approval_status" IS '지급 결재 상태
  - INIT (초안)
  - SUBMITTED (지급 승인 요청 상태--> 예산 차감 용도)
  - REJECTED(예산 회복 필요)
  - APPROVED(퀵북 연동 가능 상태)
';

COMMENT ON COLUMN "payout"."create_time" IS '생성 시간';
COMMENT ON COLUMN "payout"."remark" IS '적요';
COMMENT ON COLUMN "payout"."is_active" IS '활성화 여부';


CREATE TABLE IF NOT EXISTS "payout_item"
(
    "id" varchar PRIMARY KEY,
    "company_code" varchar NOT NULL,
    "payout_id" varchar,
    "name" varchar,
    "description" varchar,
    "quantity" decimal,
    "type" varchar,
    "unit_measure" decimal,
    "unit_price" decimal,
    "amount" decimal,
    "tax_amount" decimal,
    "total_amount" decimal,
    "line_number" integer,
    "cost_center_id" varchar,
    "invoice_item_id" varchar,
    "purchase_order_item_id" varchar,
    "material_id" varchar,
    "account_code" varchar,
    "budget_usage_time" timestamp,
    "budget_allocation" varchar,
    "remark" varchar
);
COMMENT ON COLUMN "payout_item"."id" IS '미지급금 아이템 ID';
COMMENT ON COLUMN "payout_item"."company_code" IS '회사코드';
COMMENT ON COLUMN "payout_item"."payout_id" IS '미지급금 ID';
COMMENT ON COLUMN "payout_item"."name" IS '미지급금 항목의 이름';
COMMENT ON COLUMN "payout_item"."description" IS '미지급금 항목의 상세 설명';
COMMENT ON COLUMN "payout_item"."quantity" IS '수량';
COMMENT ON COLUMN "payout_item"."cost_center_id" IS '비용 센터의 ID';
COMMENT ON COLUMN "payout_item"."type" IS '거래처 지급 유형
- MATERIAL(자재 대금 지급)
- EMPLOYEE(직원 비용 청구 지급)
- GENERAL(일반 비용 청구 지급)
';

COMMENT ON COLUMN "payout_item"."unit_measure" IS '품목의 단위
- PIECE (개)
- BOX(박스)
';

COMMENT ON COLUMN "payout_item"."unit_price" IS '단가';
COMMENT ON COLUMN "payout_item"."amount" IS '공급가액(세금 미포함)';
COMMENT ON COLUMN "payout_item"."tax_amount" IS '세액(순수 세금) 총합';
COMMENT ON COLUMN "payout_item"."total_amount" IS '공급가액(세금 포함)';
COMMENT ON COLUMN "payout_item"."line_number" IS '거래라인 아이템번호';
COMMENT ON COLUMN "payout_item"."invoice_item_id" IS 'invoice item id';
COMMENT ON COLUMN "payout_item"."purchase_order_item_id" IS '구매 주문 item id';
COMMENT ON COLUMN "payout_item"."material_id" IS '비용항목으로 포함된 상품 또는 제품에 대한 상세 확인용 ID';
COMMENT ON COLUMN "payout_item"."account_code" IS '계정코드';
COMMENT ON COLUMN "payout_item"."budget_usage_time" IS '예산 사용 일자';
COMMENT ON COLUMN "payout_item"."budget_allocation" IS '예산의 사용처';
COMMENT ON COLUMN "payout_item"."remark" IS '추가 코멘트 작성용';

CREATE TABLE IF NOT EXISTS "payout_result"
(
    "id" varchar PRIMARY KEY,
    "company_code" varchar NOT NULL,
    "payout_id" varchar,
    "type" varchar,
    "status" varchar,
    "block_reason" varchar,
    "amount" decimal,
    "tax_amount" decimal,
    "total_amount" decimal,
    "balance" decimal,
    "transaction_id" varchar,
    "transaction_retry" integer,
    "currency" varchar,
    "due_date" date,
    "complete_time" timestamp,
    "process_time" timestamp,
    "method" varchar,
    "description" varchar,
    "remark" varchar,
    "is_expired" varchar(1) DEFAULT 'Y',
    "is_completed" varchar(1) DEFAULT 'Y'
    );
COMMENT ON COLUMN "payout_result"."id" IS '객체 식별 ID';
COMMENT ON COLUMN "payout_result"."company_code" IS '회사코드';
COMMENT ON COLUMN "payout_result"."payout_id" IS '미지급금 ID';
COMMENT ON COLUMN "payout_result"."type" IS '지급 유형
- MATERIAL(자재 대금 지급)
- EMPLOYEE(직원 비용 청구 지급)
- GENERAL(일반 비용 청구 지급)
';

COMMENT ON COLUMN "payout_result"."status" IS '지급 상태';
COMMENT ON COLUMN "payout_result"."block_reason" IS '지불 차단 사유';
COMMENT ON COLUMN "payout_result"."amount" IS '지급총액(세금 미포함)';
COMMENT ON COLUMN "payout_result"."tax_amount" IS '지급 세액(순수 세금) 총합';
COMMENT ON COLUMN "payout_result"."total_amount" IS '지급 총액(세금 포함)';
COMMENT ON COLUMN "payout_result"."balance" IS '지급 잔액';
COMMENT ON COLUMN "payout_result"."transaction_id" IS '실제 지급 ID';
COMMENT ON COLUMN "payout_result"."transaction_retry" IS '지급 시도 횟수';
COMMENT ON COLUMN "payout_result"."currency" IS '지급 통화';
COMMENT ON COLUMN "payout_result"."due_date" IS '지급 기일';
COMMENT ON COLUMN "payout_result"."complete_time" IS '지급 완료일';
COMMENT ON COLUMN "payout_result"."process_time" IS '지급 처리 일시';
COMMENT ON COLUMN "payout_result"."method" IS '지불 수단';
COMMENT ON COLUMN "payout_result"."description" IS '미지급금 항목의 상세 설명';
COMMENT ON COLUMN "payout_result"."remark" IS '추가 코멘트 작성용';
COMMENT ON COLUMN "payout_result"."is_expired" IS '만료 여부';
COMMENT ON COLUMN "payout_result"."is_completed" IS '완료 여부';

CREATE TABLE IF NOT EXISTS "payout_attachment"
(
    "id" varchar PRIMARY KEY,
    "payout_id" varchar,
    "origin_file_name" varchar,
    "modified_file_name" varchar,
    "resource_path" varchar,
    "resource_size" bigint,
    "mime_type" varchar,
    "create_time" timestamp,
    "expire_time" timestamp,
    "is_deleted" varchar(1) DEFAULT 'Y',
    "remark" varchar
    );

COMMENT ON COLUMN "payout_attachment"."id" IS '증빙자료 식별자';
COMMENT ON COLUMN "payout_attachment"."payout_id" IS '트랜잭션 ID';
COMMENT ON COLUMN "payout_attachment"."origin_file_name" IS '원본 파일 이름';
COMMENT ON COLUMN "payout_attachment"."modified_file_name" IS '수정된 파일 이름';
COMMENT ON COLUMN "payout_attachment"."resource_path" IS 'resource 저장 경로';
COMMENT ON COLUMN "payout_attachment"."resource_size" IS 'resource size';
COMMENT ON COLUMN "payout_attachment"."mime_type" IS 'resource mime 유형';
COMMENT ON COLUMN "payout_attachment"."create_time" IS 'resource 생성일';
COMMENT ON COLUMN "payout_attachment"."expire_time" IS '파일 만료 일시';
COMMENT ON COLUMN "payout_attachment"."is_deleted" IS '삭제 여부';
COMMENT ON COLUMN "payout_attachment"."remark" IS '비고';

-----------------------------------------------------------------------------------------------------------------------
--- 향후 삭제 대상
------------------------------------------------------------------------------------------------------------------------
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

CREATE TABLE IF NOT EXISTS accounts_account_info
(
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

