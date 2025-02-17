CREATE TABLE IF NOT EXISTS  "if_order_item"
(
    "id" varchar PRIMARY KEY,
    "order_item_id" varchar NOT NULL,
    "order_item_status" varchar NOT NULL,
    "last_order_item_status" varchar,
    "order_product_type" varchar NOT NULL,
    "order_item_type" varchar NOT NULL,
    "order_id" varchar NOT NULL,
    "channel_id" varchar NOT NULL,
    "customer_id" varchar NOT NULL,
    "referrer_code" varchar,
    "contract_id" varchar,
    "material_id" varchar NOT NULL,
    "quantity" integer NOT NULL,
    "address" json NOT NULL,
    "tax" NUMERIC(38,2) NOT NULL,
    "tax_lines" json,
    "subtotal_price" NUMERIC(38,2) NOT NULL,
    "item_price" NUMERIC(38,2) NOT NULL,
    "discount_price" NUMERIC(38,2) NOT NULL,
    "registration_price" NUMERIC(38,2),
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
    );

CREATE UNIQUE INDEX ON "if_order_item" ("order_item_id", "order_item_status", "last_order_item_status");
COMMENT ON TABLE "if_order_item" IS '주문 정보';
COMMENT ON COLUMN "if_order_item"."id" IS 'ID';
COMMENT ON COLUMN "if_order_item"."order_item_id" IS '주문항목ID';
COMMENT ON COLUMN "if_order_item"."order_item_status" IS '주문항목상태';
COMMENT ON COLUMN "if_order_item"."last_order_item_status" IS '직전주문항목상태';
COMMENT ON COLUMN "if_order_item"."order_product_type" IS '주문 상품 유형(설치/배송 구분)';
COMMENT ON COLUMN "if_order_item"."order_item_type" IS '주문항목유형(일시불/렌탈 구분)';
COMMENT ON COLUMN "if_order_item"."order_id" IS '주문번호';
COMMENT ON COLUMN "if_order_item"."channel_id" IS '채널ID';
COMMENT ON COLUMN "if_order_item"."customer_id" IS '고객ID';
COMMENT ON COLUMN "if_order_item"."referrer_code" IS '레퍼럴코드';
COMMENT ON COLUMN "if_order_item"."contract_id" IS '계약ID(렌탈 주문만 해당)';
COMMENT ON COLUMN "if_order_item"."material_id" IS '자재ID';
COMMENT ON COLUMN "if_order_item"."quantity" IS '수량';
COMMENT ON COLUMN "if_order_item"."address" IS '주소정보';
COMMENT ON COLUMN "if_order_item"."tax" IS '판매세';
COMMENT ON COLUMN "if_order_item"."tax_lines" IS '판매세 정보';
COMMENT ON COLUMN "if_order_item"."subtotal_price" IS '결제금액(세금미포함)';
COMMENT ON COLUMN "if_order_item"."item_price" IS '개별상품가격';
COMMENT ON COLUMN "if_order_item"."discount_price" IS '할인가격';
COMMENT ON COLUMN "if_order_item"."registration_price" IS '등록비';
COMMENT ON COLUMN "if_order_item"."create_time" IS '생성일시';
COMMENT ON COLUMN "if_order_item"."update_time" IS '수정일시';

CREATE TABLE IF NOT EXISTS  "if_onetime_payment"
(
    "id" varchar PRIMARY KEY,
    "payment_id" varchar  NOT NULL,
    "transaction_type" varchar NOT NULL,
    "order_id" varchar NOT NULL,
    "transaction_id" varchar NOT NULL,
    "payment_method" varchar NOT NULL,
    "payment_time" timestamp(6) NOT NULL,
    "currency" varchar NOT NULL,
    "total_price" NUMERIC(38,2) NOT NULL,
    "tax" NUMERIC(38,2) NOT NULL,
    "subtotal_price" NUMERIC(38,2) NOT NULL,
    "item_price" NUMERIC(38,2) NOT NULL,
    "discount_price" NUMERIC(38,2) NOT NULL,
    "prepaid_amount" NUMERIC(38,2),
    "registration_price" NUMERIC(38,2),
    "promotions" json,
    "tax_lines" json,
    "address" json,
    "refund" jsonb,
    "update_time" timestamp(6) NOT NULL
    );
CREATE UNIQUE INDEX ON "if_onetime_payment" ("payment_id", "update_time");
COMMENT ON TABLE "if_onetime_payment" IS '일시불 결제정보';
COMMENT ON COLUMN "if_onetime_payment"."id" IS 'ID';
COMMENT ON COLUMN "if_onetime_payment"."payment_id" IS '결제ID';
COMMENT ON COLUMN "if_onetime_payment"."transaction_type" IS '거래유형(Shopify Payments의 거래유형(결제))';
COMMENT ON COLUMN "if_onetime_payment"."order_id" IS '주문ID';
COMMENT ON COLUMN "if_onetime_payment"."transaction_id" IS '채널 거래ID';
COMMENT ON COLUMN "if_onetime_payment"."payment_method" IS '결제수단';
COMMENT ON COLUMN "if_onetime_payment"."payment_time" IS '결제시간';
COMMENT ON COLUMN "if_onetime_payment"."currency" IS '거래통화';
COMMENT ON COLUMN "if_onetime_payment"."total_price" IS '결제금액(세금포함)';
COMMENT ON COLUMN "if_onetime_payment"."tax" IS '판매세';
COMMENT ON COLUMN "if_onetime_payment"."subtotal_price" IS '결제금액(세금미포함)';
COMMENT ON COLUMN "if_onetime_payment"."item_price" IS '개별상품가격';
COMMENT ON COLUMN "if_onetime_payment"."discount_price" IS '할인가격';
COMMENT ON COLUMN "if_onetime_payment"."prepaid_amount" IS '선결제금액';
COMMENT ON COLUMN "if_onetime_payment"."registration_price" IS '등록비';
COMMENT ON COLUMN "if_onetime_payment"."promotions" IS '프로모션 정보';
COMMENT ON COLUMN "if_onetime_payment"."tax_lines" IS '판매세 정보';
COMMENT ON COLUMN "if_onetime_payment"."address" IS '주소정보';
COMMENT ON COLUMN "if_onetime_payment"."refund" IS '환불정보';
COMMENT ON COLUMN "if_onetime_payment"."update_time" IS '수정시간';


CREATE TABLE IF NOT EXISTS  "if_service_flow"
(
    "id" varchar PRIMARY KEY,
    "service_flow_id" varchar NOT NULL,
    "service_status" varchar NOT NULL,
    "last_service_status" varchar,
    "service_type" varchar NOT NULL,
    "install_id" varchar,
    "serial_number" varchar,
    "branch_id" varchar,
    "warehouse_id" varchar,
    "technician_id" varchar,
    "order_item_id" varchar NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
);

CREATE UNIQUE INDEX ON "if_service_flow" ("service_flow_id", "service_status", "last_service_status");
COMMENT ON TABLE "if_service_flow" IS '서비스 플로우 정보(HISTORY)';
COMMENT ON COLUMN "if_service_flow"."id" IS 'ID';
COMMENT ON COLUMN "if_service_flow"."service_flow_id" IS '서비스플로우ID';
COMMENT ON COLUMN "if_service_flow"."service_status" IS '서비스플로우상태';
COMMENT ON COLUMN "if_service_flow"."last_service_status" IS '직전 서비스플로우상태';
COMMENT ON COLUMN "if_service_flow"."service_type" IS '서비스유형';
COMMENT ON COLUMN "if_service_flow"."install_id" IS '설치ID';
COMMENT ON COLUMN "if_service_flow"."serial_number" IS '시리얼번호';
COMMENT ON COLUMN "if_service_flow"."branch_id" IS '브랜치 ID';
COMMENT ON COLUMN "if_service_flow"."warehouse_id" IS '창고 ID';
COMMENT ON COLUMN "if_service_flow"."technician_id" IS '테크니션ID';
COMMENT ON COLUMN "if_service_flow"."order_item_id" IS '주문항목ID';
COMMENT ON COLUMN "if_service_flow"."create_time" IS '생성시간';
COMMENT ON COLUMN "if_service_flow"."update_time" IS '수정시간';

CREATE TABLE IF NOT EXISTS  "if_contract"
(
    "id" varchar PRIMARY KEY,
    "contract_id" varchar NOT NULL,
    "contract_status" varchar NOT NULL,
    "last_contract_status" varchar,
    "rental_code" varchar(10) NOT NULL,
    "order_item_id" varchar NOT NULL,
    "customer_id" varchar NOT NULL,
    "start_date" date,
    "end_date" date,
    "duration_in_months" integer,
    "payment_day" integer NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
    );
CREATE UNIQUE INDEX ON "if_contract" ("contract_id", "contract_status", "last_contract_status");
COMMENT ON TABLE "if_contract" IS '계약 정보(HISTORY)';
COMMENT ON COLUMN "if_contract"."id" IS 'ID';
COMMENT ON COLUMN "if_contract"."contract_id" IS '계약ID';
COMMENT ON COLUMN "if_contract"."contract_status" IS '계약상태';
COMMENT ON COLUMN "if_contract"."last_contract_status" IS '직전 계약상태';
COMMENT ON COLUMN "if_contract"."rental_code" IS '렌탈코드';
COMMENT ON COLUMN "if_contract"."order_item_id" IS '주문항목ID';
COMMENT ON COLUMN "if_contract"."customer_id" IS '고객ID';
COMMENT ON COLUMN "if_contract"."start_date" IS '계약시작일';
COMMENT ON COLUMN "if_contract"."end_date" IS '계약종료일';
COMMENT ON COLUMN "if_contract"."duration_in_months" IS '계약기간';
COMMENT ON COLUMN "if_contract"."payment_day" IS '결제일';
COMMENT ON COLUMN "if_contract"."create_time" IS '생성일시';
COMMENT ON COLUMN "if_contract"."update_time" IS '수정일시';

CREATE TABLE IF NOT EXISTS  "if_charge"
(
    "id" varchar PRIMARY KEY,
    "charge_id" varchar NOT NULL,
    "charge_status" varchar NOT NULL,
    "last_charge_status" varchar,
    "billing_cycle" integer NOT NULL,
    "target_month" varchar,
    "contract_id" varchar NOT NULL,
    "start_date" date,
    "end_date" date,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
);
CREATE UNIQUE INDEX ON "if_charge" ("charge_id", "charge_status", "last_charge_status", "end_date");

COMMENT ON TABLE "if_charge" IS '청구 정보(HISTORY)';
COMMENT ON COLUMN "if_charge"."id" IS 'ID';
COMMENT ON COLUMN "if_charge"."charge_id" IS '청구ID';
COMMENT ON COLUMN "if_charge"."charge_status" IS '청구상태';
COMMENT ON COLUMN "if_charge"."last_charge_status" IS '직전 청구상태';
COMMENT ON COLUMN "if_charge"."billing_cycle" IS '청구회차';
COMMENT ON COLUMN "if_charge"."target_month" IS '대상월';
COMMENT ON COLUMN "if_charge"."contract_id" IS '계약ID';
COMMENT ON COLUMN "if_charge"."start_date" IS '시작일';
COMMENT ON COLUMN "if_charge"."end_date" IS '종료일';
COMMENT ON COLUMN "if_charge"."create_time" IS '생성일시';
COMMENT ON COLUMN "if_charge"."update_time" IS '수정일시';


CREATE TABLE IF NOT EXISTS  "if_charge_item"
(
    "charge_item_id" varchar PRIMARY KEY,
    "charge_item_type" varchar NOT NULL,
    "quantity" integer NOT NULL,
    "subtotal_price" numeric(10,4) NOT NULL,
    "item_price" numeric(10,4) NOT NULL,
    "discount_price" numeric(10,4) NOT NULL,
    "prepaid_amount" numeric(10,4) NOT NULL,
    "promotions" json,
    "currency" varchar NOT NULL,
    "is_tax_exempt" boolean NOT NULL,
    "charge_id" varchar NOT NULL,
    "service_flow_id" varchar,
    "material_id" varchar,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
);
CREATE UNIQUE INDEX ON "if_charge_item" ("charge_item_id", "charge_item_type");
COMMENT ON TABLE "if_charge_item" IS '요금 청구 항목 정보';
COMMENT ON COLUMN "if_charge_item"."charge_item_id" IS '청구항목ID';
COMMENT ON COLUMN "if_charge_item"."charge_item_type" IS '청구항목유형';
COMMENT ON COLUMN "if_charge_item"."quantity" IS '수량';
COMMENT ON COLUMN "if_charge_item"."subtotal_price" IS '전체금액(세금미포함)';
COMMENT ON COLUMN "if_charge_item"."item_price" IS '아이템금액';
COMMENT ON COLUMN "if_charge_item"."discount_price" IS '할인금액';
COMMENT ON COLUMN "if_charge_item"."prepaid_amount" IS '선급금액';
COMMENT ON COLUMN "if_charge_item"."promotions" IS '프로모션';
COMMENT ON COLUMN "if_charge_item"."currency" IS '거래통화';
COMMENT ON COLUMN "if_charge_item"."is_tax_exempt" IS '면세여부';
COMMENT ON COLUMN "if_charge_item"."charge_id" IS '청구ID';
COMMENT ON COLUMN "if_charge_item"."service_flow_id" IS '서비스플로우ID';
COMMENT ON COLUMN "if_charge_item"."material_id" IS '자재ID';
COMMENT ON COLUMN "if_charge_item"."create_time" IS '생성일시';
COMMENT ON COLUMN "if_charge_item"."update_time" IS '수정일시';



CREATE TABLE IF NOT EXISTS  "if_charge_invoice"
(
    "id" varchar PRIMARY KEY,
    "charge_id" varchar NOT NULL,
    "invoice_id" varchar NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
);
COMMENT ON TABLE "if_charge_invoice" IS '청구서 관계정보(M:N)';
COMMENT ON COLUMN "if_charge_invoice"."id" IS 'ID';
COMMENT ON COLUMN "if_charge_invoice"."charge_id" IS '청구ID';
COMMENT ON COLUMN "if_charge_invoice"."invoice_id" IS '청구서ID';
COMMENT ON COLUMN "if_charge_invoice"."create_time" IS '생성시간';
COMMENT ON COLUMN "if_charge_invoice"."update_time" IS '수정시간';

CREATE TABLE IF NOT EXISTS  "if_invoice"
(
    "id" varchar PRIMARY KEY,
    "invoice_id" varchar,
    "contract_id" varchar NOT NULL,
    "invoice_status" varchar NOT NULL,
    "billing_month" varchar NOT NULL,
    "payment_due_date" date NOT NULL,
    "total_price" NUMERIC(38,2) NOT NULL,
    "charges" jsonb NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
    );
CREATE UNIQUE INDEX ON "if_invoice" ("invoice_id", "invoice_status");
COMMENT ON TABLE "if_invoice" IS '청구서 정보(HISTORY)';
COMMENT ON COLUMN "if_invoice"."id" IS 'ID';
COMMENT ON COLUMN "if_invoice"."invoice_id" IS '인보이스ID';
COMMENT ON COLUMN "if_invoice"."contract_id" IS '계약ID';
COMMENT ON COLUMN "if_invoice"."invoice_status" IS '인보이스상태';
COMMENT ON COLUMN "if_invoice"."billing_month" IS '청구월';
COMMENT ON COLUMN "if_invoice"."payment_due_date" IS '납부일자';
COMMENT ON COLUMN "if_invoice"."total_price" IS '전체금액';
COMMENT ON COLUMN "if_invoice"."charges" IS '청구정보';
COMMENT ON COLUMN "if_invoice"."create_time" IS '생성일시';
COMMENT ON COLUMN "if_invoice"."update_time" IS '수정일시';

CREATE TABLE IF NOT EXISTS  "if_charge_payment"
(
    "id" varchar PRIMARY KEY,
    "payment_id" varchar NOT NULL,
    "transaction_type" varchar NOT NULL,
    "charge_id" varchar NOT NULL,
    "invoice_id" varchar,
    "transaction_id" varchar,
    "payment_method" varchar,
    "payment_time" timestamp,
    "total_price" NUMERIC(38,2) NOT NULL,
    "tax" NUMERIC(38,2) NOT NULL,
    "subtotal_price" NUMERIC(38,2) NOT NULL,
    "charge_items" json NOT NULL,
    "tax_lines" json NOT NULL,
    "installment_months" integer,
    "currency" varchar NOT NULL,
    "address" json NOT NULL
);

CREATE UNIQUE INDEX ON "if_charge_payment" ("payment_id", "transaction_type");
COMMENT ON TABLE "if_charge_payment" IS '청구 결제 정보(HISTORY)';
COMMENT ON COLUMN "if_charge_payment"."id" IS 'ID';
COMMENT ON COLUMN "if_charge_payment"."payment_id" IS '결제ID';
COMMENT ON COLUMN "if_charge_payment"."transaction_type" IS '거래유형(Shopify Payments의 거래유형(결제))';
COMMENT ON COLUMN "if_charge_payment"."charge_id" IS '청구ID';
COMMENT ON COLUMN "if_charge_payment"."invoice_id" IS '인보이스ID';
COMMENT ON COLUMN "if_charge_payment"."transaction_id" IS '거래ID(Shopify)';
COMMENT ON COLUMN "if_charge_payment"."payment_method" IS '결제수단';
COMMENT ON COLUMN "if_charge_payment"."payment_time" IS '결제시간';
COMMENT ON COLUMN "if_charge_payment"."total_price" IS '결제금액(세금포함)';
COMMENT ON COLUMN "if_charge_payment"."tax" IS '판매세';
COMMENT ON COLUMN "if_charge_payment"."subtotal_price" IS '결제금액(세금미포함)';
COMMENT ON COLUMN "if_charge_payment"."charge_items" IS '청구항목';
COMMENT ON COLUMN "if_charge_payment"."tax_lines" IS '판매세';
COMMENT ON COLUMN "if_charge_payment"."installment_months" IS '할부개월수';
COMMENT ON COLUMN "if_charge_payment"."currency" IS '거래통화';
COMMENT ON COLUMN "if_charge_payment"."address" IS '주소정보';

CREATE TABLE IF NOT EXISTS  "if_service_charge"
(
    "id" varchar PRIMARY KEY,
    "service_charge_id" varchar NOT NULL,
    "charge_status" varchar NOT NULL,
    "last_charge_status" varchar,
    "service_flow_id" varchar NOT NULL,
    "service_billing_type" varchar NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
);
CREATE UNIQUE INDEX ON "if_service_charge" ("service_charge_id", "charge_status", "last_charge_status");
COMMENT ON TABLE "if_service_charge" IS '서비스 청구(HISTORY)';
COMMENT ON COLUMN "if_service_charge"."id" IS 'ID';
COMMENT ON COLUMN "if_service_charge"."service_charge_id" IS '서비스 청구 ID';
COMMENT ON COLUMN "if_service_charge"."charge_status" IS '서비스 청구상태';
COMMENT ON COLUMN "if_service_charge"."last_charge_status" IS '직전 서비스 청구상태';
COMMENT ON COLUMN "if_service_charge"."service_flow_id" IS '서비스 플로우 ID';
COMMENT ON COLUMN "if_service_charge"."service_billing_type" IS '서비스 빌링유형';
COMMENT ON COLUMN "if_service_charge"."create_time" IS '생성시간';
COMMENT ON COLUMN "if_service_charge"."update_time" IS '수정시간';

CREATE TABLE IF NOT EXISTS  "if_service_charge_item"
(
    "id" varchar PRIMARY KEY,
    "charge_item_id" varchar NOT NULL,
    "charge_item_type" varchar NOT NULL,
    "quantity" varchar NOT NULL,
    "subtotal_price" NUMERIC(38,2) NOT NULL,
    "item_price" NUMERIC(38,2) NOT NULL,
    "discount_price" NUMERIC(38,2) NOT NULL,
    "prepaid_amount" NUMERIC(38,2) NOT NULL,
    "promotions" json,
    "currency" varchar NOT NULL,
    "is_tax_exempt" boolean NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL,
    "service_charge_id" varchar NOT NULL,
    "material_id" varchar
    );

CREATE UNIQUE INDEX ON "if_service_charge_item" ("charge_item_id", "charge_item_type");
COMMENT ON TABLE "if_service_charge_item" IS '서비스 청구 항목(HISTORY)';
COMMENT ON COLUMN "if_service_charge_item"."id" IS 'ID';
COMMENT ON COLUMN "if_service_charge_item"."charge_item_id" IS '서비스 청구항목 ID';
COMMENT ON COLUMN "if_service_charge_item"."charge_item_type" IS '청구아이템 유형';
COMMENT ON COLUMN "if_service_charge_item"."quantity" IS '수량';
COMMENT ON COLUMN "if_service_charge_item"."subtotal_price" IS '전체금액(세금제외)';
COMMENT ON COLUMN "if_service_charge_item"."item_price" IS '아이템금액';
COMMENT ON COLUMN "if_service_charge_item"."discount_price" IS '아이템 할인금액';
COMMENT ON COLUMN "if_service_charge_item"."prepaid_amount" IS '선지급 금액';
COMMENT ON COLUMN "if_service_charge_item"."promotions" IS '프로모션 정보';
COMMENT ON COLUMN "if_service_charge_item"."currency" IS '통화';
COMMENT ON COLUMN "if_service_charge_item"."is_tax_exempt" IS '면세여부';
COMMENT ON COLUMN "if_service_charge_item"."create_time" IS '생성시간';
COMMENT ON COLUMN "if_service_charge_item"."update_time" IS '수정시간';
COMMENT ON COLUMN "if_service_charge_item"."service_charge_id" IS '서비스청구 ID';
COMMENT ON COLUMN "if_service_charge_item"."material_id" IS '자재ID';

CREATE TABLE IF NOT EXISTS  "if_service_charge_payment"
(
    "id" varchar PRIMARY KEY,
    "payment_id" varchar NOT NULL,
    "transaction_type" varchar NOT NULL,
    "transaction_id" varchar NOT NULL,
    "service_charge_id" varchar NOT NULL,
    "payment_method" varchar NOT NULL,
    "payment_time" timestamp,
    "currency" varchar,
    "total_price" NUMERIC(38,2) NOT NULL,
    "tax" NUMERIC(38,2) NOT NULL,
    "subtotal_price" NUMERIC(38,2) NOT NULL,
    "item_price" NUMERIC(38,2) NOT NULL,
    "discount_price" NUMERIC(38,2) NOT NULL,
    "charge_items" json NOT NULL,
    "tax_lines" json NOT NULL,
    "address" json NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
);

CREATE UNIQUE INDEX ON "if_service_charge_payment" ("payment_id", "transaction_type");
COMMENT ON TABLE "if_service_charge_payment" IS '서비스 결제정보(HISTORY)';
COMMENT ON COLUMN "if_service_charge_payment"."id" IS 'ID';
COMMENT ON COLUMN "if_service_charge_payment"."payment_id" IS '결제';
COMMENT ON COLUMN "if_service_charge_payment"."transaction_type" IS '거래유형';
COMMENT ON COLUMN "if_service_charge_payment"."transaction_id" IS '거래ID';
COMMENT ON COLUMN "if_service_charge_payment"."service_charge_id" IS '서비스 청구 ID';
COMMENT ON COLUMN "if_service_charge_payment"."payment_method" IS '결제방법 (렌탈/현장수납?)';
COMMENT ON COLUMN "if_service_charge_payment"."payment_time" IS '결제시간';
COMMENT ON COLUMN "if_service_charge_payment"."total_price" IS '결제금액(세금포함)';
COMMENT ON COLUMN "if_service_charge_payment"."tax" IS '판매세';
COMMENT ON COLUMN "if_service_charge_payment"."subtotal_price" IS '결제금액(세금미포함)';
COMMENT ON COLUMN "if_service_charge_payment"."item_price" IS '개별상품가격';
COMMENT ON COLUMN "if_service_charge_payment"."discount_price" IS '할인가격';
COMMENT ON COLUMN "if_service_charge_payment"."charge_items" IS '청구항목';
COMMENT ON COLUMN "if_service_charge_payment"."tax_lines" IS '판매세정보';
COMMENT ON COLUMN "if_service_charge_payment"."address" IS '주소';
COMMENT ON COLUMN "if_service_charge_payment"."create_time" IS '생성시간';
COMMENT ON COLUMN "if_service_charge_payment"."update_time" IS '수정시간';



CREATE TABLE IF NOT EXISTS  "if_customer"
(
    "customer_id" varchar PRIMARY KEY,
    "email" varchar NOT NULL,
    "phone" varchar,
    "user_id" varchar,
    "last_name" varchar,
    "first_name" varchar,
    "account_type" varchar NOT NULL,
    "customer_status" varchar NOT NULL,
    "referrer_code" varchar NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
);

COMMENT ON TABLE "if_customer" IS '고객 정보';
COMMENT ON COLUMN "if_customer"."customer_id" IS '고객ID';
COMMENT ON COLUMN "if_customer"."email" IS '이메일';
COMMENT ON COLUMN "if_customer"."phone" IS '전화번호';
COMMENT ON COLUMN "if_customer"."user_id" IS 'AMS ID';
COMMENT ON COLUMN "if_customer"."last_name" IS '성';
COMMENT ON COLUMN "if_customer"."first_name" IS '이름';
COMMENT ON COLUMN "if_customer"."account_type" IS '고객유형(CORPORATE/INDIVIDUAL/STAFF/ACADEMY/OTHERS)';
COMMENT ON COLUMN "if_customer"."customer_status" IS '고객상태(ACTIVE/BANKRUPT/DECEASED)';
COMMENT ON COLUMN "if_customer"."referrer_code" IS '레퍼럴코드';
COMMENT ON COLUMN "if_customer"."create_time" IS '생성일시';
COMMENT ON COLUMN "if_customer"."update_time" IS '수정일시';

CREATE TABLE IF NOT EXISTS  "if_channel"
(
    "channel_id" varchar PRIMARY KEY,
    "channel_type" varchar NOT NULL,
    "channel_name" varchar NOT NULL,
    "channel_detail" varchar NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
);

COMMENT ON TABLE "if_channel" IS '채널 정보';
COMMENT ON COLUMN "if_channel"."channel_id" IS '채널ID';
COMMENT ON COLUMN "if_channel"."channel_type" IS '채널타입';
COMMENT ON COLUMN "if_channel"."channel_name" IS '채널이름';
COMMENT ON COLUMN "if_channel"."channel_detail" IS '채널상세';
COMMENT ON COLUMN "if_channel"."create_time" IS '생성시간';
COMMENT ON COLUMN "if_channel"."update_time" IS '수정시간';


CREATE TABLE IF NOT EXISTS  "if_material"
(
    "material_id" varchar PRIMARY KEY,
    "material_type" varchar NOT NULL,
    "material_model_name" varchar,
    "material_model_name_prefix" varchar,
    "material_name" varchar NOT NULL,
    "material_series_code" varchar,
    "material_series_name" varchar,
    "material_category_code" varchar NOT NULL,
    "material_category_name" varchar,
    "manufacturer_code" varchar NOT NULL,
    "brand_name" varchar,
    "product_type" varchar NOT NULL,
    "feature_code" varchar,
    "filter_type" varchar,
    "installation_type" varchar,
    "shipping_method_type" varchar NOT NULL,
    "description" varchar NOT NULL,
    "create_time" timestamp NOT NULL,
    "update_time" timestamp NOT NULL
);
COMMENT ON TABLE "if_material" IS '자재 정보';
COMMENT ON COLUMN "if_material"."material_id" IS '자재ID';
COMMENT ON COLUMN "if_material"."material_type" IS '자재유형';
COMMENT ON COLUMN "if_material"."material_model_name" IS '모델이름';
COMMENT ON COLUMN "if_material"."material_model_name_prefix" IS '모델이름(prefix)';
COMMENT ON COLUMN "if_material"."material_name" IS '자재이름';
COMMENT ON COLUMN "if_material"."material_series_code" IS '시리즈코드';
COMMENT ON COLUMN "if_material"."material_series_name" IS '시리즈명';
COMMENT ON COLUMN "if_material"."material_category_code" IS '카테고리코드';
COMMENT ON COLUMN "if_material"."material_category_name" IS '카테고리명';
COMMENT ON COLUMN "if_material"."manufacturer_code" IS '제조사 생산코드 (SAP)';
COMMENT ON COLUMN "if_material"."brand_name" IS '브랜드명';
COMMENT ON COLUMN "if_material"."product_type" IS '제품유형';
COMMENT ON COLUMN "if_material"."feature_code" IS '특징';
COMMENT ON COLUMN "if_material"."filter_type" IS '필터유형';
COMMENT ON COLUMN "if_material"."installation_type" IS '설치유형';
COMMENT ON COLUMN "if_material"."shipping_method_type" IS '배송유형';
COMMENT ON COLUMN "if_material"."description" IS '설명';
COMMENT ON COLUMN "if_material"."create_time" IS '생성시간';
COMMENT ON COLUMN "if_material"."update_time" IS '수정시간';
