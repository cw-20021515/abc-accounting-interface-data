CREATE TABLE IF NOT EXISTS "oms_resource_history"
(
    "id"                varchar(255)            not null primary key,
    "resource_id"       varchar(255)            not null,
    "entity_class_name" varchar(255)            not null,
    "operation"         varchar(50)             not null,
    "new_value"         json,
    "difference"        json,
    "create_time"       timestamp default now() not null,
    "http_method"       varchar,
    "uri"               varchar,
    "create_user"       varchar
    );

create table oms_charge
(
    id            varchar not null primary key,
    billing_cycle integer,
    target_month  varchar,
    charge_status varchar,
    update_time   timestamp(6),
    create_time   timestamp(6),
    contract_id   varchar,
    start_date    date,
    end_date      date
);

create table oms_charge_invoice
(
    id          varchar not null primary key,
    charge_id   varchar not null,
    invoice_id  varchar not null,
    create_time timestamp(6) default now(),
    update_time timestamp(6) default now()
);

create table oms_charge_item
(
    id               varchar not null primary key,
    charge_item_type varchar,
    service_flow_id  varchar,
    quantity         integer,
    total_price      numeric(10, 4),
    item_price       numeric(10, 4),
    discount_price   numeric(10, 4),
    prepaid_amount   numeric(10, 4),
    promotions       json,
    currency         varchar,
    is_tax_exempt    boolean,
    create_time      timestamp(6),
    update_time      timestamp(6),
    charge_id        varchar,
    material_id      varchar,
    is_excluded      boolean default false,
    remark           varchar
);



CREATE TABLE IF NOT EXISTS oms_contract
(
    id                   varchar not null primary key,
    is_signed            boolean,
    signed_time          timestamp(3),
    form_id              varchar,
    revision             integer,
    customer_id          varchar,
    order_item_id        varchar,
    start_date           timestamp(3),
    end_date             timestamp(3),
    duration_in_months   integer,
    contract_status_code varchar,
    update_time          timestamp,
    create_time          timestamp,
    channel_contract_id  varchar,
    rental_code          varchar(10)
    );

CREATE TABLE IF NOT EXISTS oms_contract_history
(
    id            varchar                 not null primary key,
    contract_id   varchar(255)            not null,
    source_status varchar(255)            not null,
    target_status varchar(255)            not null,
    event         varchar(255)            not null,
    error_log     text,
    create_time   timestamp default now() not null
    );


CREATE TABLE IF NOT EXISTS oms_charge_payment
(
    id                 varchar not null primary key,
    charge_id          varchar constraint uk_charge_payment_charge_id unique,
    invoice_id         varchar,
    payment_method     varchar,
    transaction_id     varchar,
    payment_time       timestamp(6),
    last_name          varchar,
    first_name         varchar,
    address1           varchar,
    address2           varchar,
    zipcode            varchar,
    city               varchar,
    state              varchar,
    phone              varchar,
    email              varchar,
    remark             varchar,
    total_price        numeric(10, 4),
    charge_items       json,
    card_number        varchar,
    card_type          varchar,
    installment_months integer,
    currency           varchar,
    payout_id          varchar,
    tax                numeric(10, 4),
    tax_lines          json
    );

CREATE TABLE IF NOT EXISTS oms_contract
(
    id                   varchar not null primary key,
    is_signed            boolean,
    signed_time          timestamp(3),
    form_id              varchar,
    revision             integer,
    customer_id          varchar,
    order_item_id        varchar,
    start_date           timestamp(3),
    end_date             timestamp(3),
    duration_in_months   integer,
    contract_status_code varchar,
    update_time          timestamp,
    create_time          timestamp,
    channel_contract_id  varchar,
    rental_code          varchar(10)
    );

CREATE TABLE IF NOT EXISTS oms_contract_charge
(
    id          varchar(255)            not null primary key,
    contract_id varchar(255)            not null,
    data        json                    not null,
    create_time timestamp default now() not null,
    update_time timestamp default now() not null
    );

CREATE TABLE IF NOT EXISTS oms_contract_document
(
    id                  varchar(255)            not null primary key,
    contract_id         varchar(255)            not null,
    channel_contract_id varchar(255)            not null,
    customer_id         varchar(255)            not null,
    order_item_id       varchar(255)            not null,
    revision            integer,
    file_url            varchar,
    create_time         timestamp default now() not null,
    update_time         timestamp default now() not null,
    mapping_data        text
    );

CREATE TABLE IF NOT EXISTS oms_contract_payment_information
(
    id                       varchar not null primary key,
    contract_id              varchar,
    payment_method           varchar,
    transaction_id           varchar,
    monthly_total_price      numeric(10, 2),
    monthly_discount_price   numeric(10, 2),
    subscription_payment_day integer,
    last_name                varchar,
    first_name               varchar,
    address1                 varchar,
    address2                 varchar,
    zipcode                  varchar,
    city                     varchar,
    state                    varchar,
    phone                    varchar,
    email                    varchar,
    card_number              varchar,
    card_type                varchar,
    item_monthly_price       numeric(10, 2),
    monthly_tax              numeric(10, 2) default 0,
    currency                 varchar
    );

CREATE TABLE IF NOT EXISTS oms_customer
(
    id                  varchar   not null constraint customer_primary_key primary key,
    email               varchar   not null,
    phone               varchar   not null,
    user_id             varchar,
    last_name           varchar,
    first_name          varchar,
    create_time         timestamp not null,
    create_user         varchar   not null,
    update_time         timestamp not null,
    update_user         varchar   not null,
    account_type        varchar default 'PRIVATE'::character varying,
    customer_status     varchar default 'CONTRACT'::character varying,
    channel_customer_id varchar(255),
    referrer_code       varchar(255),
    member_id           varchar(255)
    );

CREATE TABLE IF NOT EXISTS oms_delivery_address
(
    id         varchar not null primary key,
    order_id   varchar unique,
    last_name  varchar,
    first_name varchar,
    address1   varchar,
    address2   varchar,
    zipcode    varchar,
    city       varchar,
    state      varchar,
    phone      varchar,
    remark     varchar,
    email      varchar,
    latitude   double precision,
    longitude  double precision
);

CREATE TABLE IF NOT EXISTS oms_installation_information
(
    id                  varchar not null primary key,
    order_item_id       varchar unique,
    address1            varchar,
    address2            varchar,
    zipcode             varchar,
    city                varchar,
    state               varchar,
    serial_number       varchar constraint installation_information_serial_number unique,
    latitude            double precision,
    longitude           double precision,
    installation_time   timestamp(3),
    create_time         timestamp(3),
    update_time         timestamp(3),
    technician_id       varchar,
    service_flow_id     varchar,
    warranty_start_time timestamp(3),
    warranty_end_time   timestamp(3),
    branch_id           varchar,
    warehouse_id        varchar,
    water_type          varchar
    );

CREATE TABLE IF NOT EXISTS oms_invoice
(
    id               varchar not null primary key,
    contract_id      varchar,
    invoice_status   varchar,
    billing_month    varchar,
    payment_due_date date,
    total_price      numeric(10, 4),
    charges          jsonb,
    create_time      timestamp(6),
    update_time      timestamp(6)
    );

CREATE TABLE IF NOT EXISTS oms_order
(
    id                 varchar not null primary key,
    channel_order_id   varchar not null,
    channel_id         varchar not null,
    customer_id        varchar,
    order_product_type varchar,
    order_create_time  timestamp(3),
    order_update_time  timestamp(3),
    create_time        timestamp(3),
    update_time        timestamp(3),
    referrer_code      varchar
    );

CREATE TABLE IF NOT EXISTS oms_order_item
(
    id                      varchar not null primary key,
    channel_order_item_id   varchar,
    order_id                varchar,
    channel_order_id        varchar,
    sequence                integer,
    order_item_status_code  varchar,
    order_item_type         varchar,
    material_id             varchar,
    quantity                integer,
    item_price              numeric(10, 2),
    registration_price      numeric(10, 2),
    create_time             timestamp(3),
    update_time             timestamp(3),
    discount_price          numeric(10, 2),
    total_price             numeric(10, 2),
    tax                     numeric(10, 2),
    currency                varchar,
    shipping_information_id varchar(255)
    );
CREATE TABLE IF NOT EXISTS oms_order_item_history
(
    id            varchar(255) not null constraint pk_order_item_history primary key,
    create_time   timestamp(3) not null,
    update_time   timestamp(3),
    order_item_id varchar(255),
    source_status varchar(255),
    target_status varchar(255),
    event         varchar(255),
    error_log     text
    );

CREATE TABLE IF NOT EXISTS oms_order_item_promotion
(
    id             varchar(255) not null constraint pk_order_item_promotion primary key,
    order_item_id  varchar(255),
    promotion_id   varchar(255),
    create_time    timestamp    not null,
    update_time    timestamp,
    discount_price varchar(255)
    );

CREATE TABLE IF NOT EXISTS oms_payment
(
    id                 varchar not null primary key,
    order_id           varchar,
    payment_method     varchar,
    transaction_id     varchar,
    item_price         numeric(10, 2),
    payment_time       timestamp(3),
    last_name          varchar,
    first_name         varchar,
    address1           varchar,
    address2           varchar,
    zipcode            varchar,
    city               varchar,
    state              varchar,
    phone              varchar,
    email              varchar,
    remark             varchar,
    discount_price     numeric(10, 2),
    registration_price numeric(10, 2),
    total_price        numeric(10, 2),
    tax                numeric(10, 2),
    card_number        varchar,
    card_type          varchar,
    installment_months integer,
    currency           varchar,
    prepaid_amount     numeric(10, 2),
    payout_id          varchar,
    refund             jsonb
    );

CREATE TABLE IF NOT EXISTS oms_promotion
(
    id                varchar(255) not null constraint pk_promotion_info primary key,
    create_time       timestamp    not null,
    update_time       timestamp,
    promotion_type    varchar(255),
    promotion_cycles  character varying[],
    promotion_plan_id varchar,
    is_active         boolean,
    promotion_name    varchar
    );

CREATE TABLE IF NOT EXISTS oms_service_charge
(
    id                    varchar not null primary key,
    service_flow_id       varchar,
    service_billing_type  varchar,
    service_charge_status varchar,
    update_time           timestamp(6),
    create_time           timestamp(6),
    shopify_order_id      varchar
    );

CREATE TABLE IF NOT EXISTS oms_service_charge_item
(
    id                varchar not null primary key,
    charge_item_type  varchar,
    quantity          integer,
    total_price       numeric(10, 4),
    item_price        numeric(10, 4),
    discount_price    numeric(10, 4),
    prepaid_amount    numeric(10, 4),
    promotions        json,
    currency          varchar,
    is_tax_exempt     boolean,
    create_time       timestamp(6),
    update_time       timestamp(6),
    service_charge_id varchar,
    material_id       varchar,
    is_excluded       boolean default false,
    remark            varchar
    );

CREATE TABLE IF NOT EXISTS oms_service_charge_payment
(
    id                 varchar not null primary key,
    service_charge_id  varchar,
    payment_method     varchar,
    transaction_id     varchar,
    payment_time       timestamp(6),
    last_name          varchar,
    first_name         varchar,
    address1           varchar,
    address2           varchar,
    zipcode            varchar,
    city               varchar,
    state              varchar,
    phone              varchar,
    email              varchar,
    remark             varchar,
    total_price        numeric(10, 4),
    charge_items       json,
    card_number        varchar,
    card_type          varchar,
    installment_months integer,
    currency           varchar,
    payout_id          varchar,
    tax                numeric(10, 4),
    tax_lines          json,
    create_time        timestamp(6),
    update_time        timestamp(6),
    item_price         numeric(38, 4),
    discount_price     numeric(38, 4)
    );

CREATE TABLE IF NOT EXISTS oms_service_flow
(
    id                         varchar(255) not null
    primary key,
    service_type               varchar(255),
    order_id                   varchar(255),
    order_item_id              varchar(255),
    customer_service_ticket_id varchar,
    booking_id                 varchar(255),
    outbound_delivery_id       varchar(255),
    billing_id                 varchar(255),
    work_id                    varchar(255),
    service_status_code        varchar(255),
    service_create_time        timestamp(3),
    service_update_time        timestamp(3),
    service_cancel_time        timestamp(3),
    create_time                timestamp(3),
    update_time                timestamp(3),
    customer_service_id        varchar,
    service_complete_time      timestamp(6)
    );
CREATE TABLE IF NOT EXISTS oms_service_history
(
    id              varchar not null primary key,
    service_flow_id varchar,
    data            json,
    create_time     timestamp(3),
    event           varchar,
    source_status   varchar,
    target_status   varchar,
    service_type    varchar,
    error_log       text
    );

CREATE TABLE IF NOT EXISTS oms_service_location
(
    id              varchar(255) not null primary key,
    service_flow_id varchar(255),
    branch_id       varchar(255),
    warehouse_id    varchar(255),
    last_name       varchar(255),
    first_name      varchar(255),
    address1        varchar(255),
    address2        varchar(255),
    zipcode         varchar(255),
    city            varchar(255),
    state           varchar(255),
    phone           varchar(255),
    email           varchar,
    latitude        double precision,
    longitude       double precision
    );

CREATE TABLE IF NOT EXISTS oms_service_payment
(
    id                   varchar(255) not null primary key,
    service_flow_id      varchar(255),
    payment_method       varchar(255),
    transaction_id       varchar(255),
    item_price           numeric(10, 2),
    payment_time         timestamp(3),
    last_name            varchar,
    first_name           varchar,
    address1             varchar,
    address2             varchar,
    zipcode              varchar,
    city                 varchar,
    state                varchar,
    phone                varchar,
    email                varchar,
    remark               varchar,
    card_number          varchar,
    card_type            varchar,
    installment_months   integer,
    discount_price       numeric(10, 2) default 0.0,
    total_price          numeric(10, 2) default 0,
    tax                  numeric(10, 2) default 0,
    currency             varchar,
    registration_price   numeric(10, 2),
    service_billing_type varchar,
    prepaid_amount       numeric(10, 2),
    create_time          timestamp(3),
    update_time          timestamp(3),
    payout_id            varchar
    );

CREATE TABLE IF NOT EXISTS oms_service_payment_charge_item
(
    id                 varchar not null primary key,
    charge_item_type   varchar,
    service_flow_id    varchar,
    service_payment_id varchar,
    quantity           integer,
    total_price        numeric(10, 4),
    item_price         numeric(10, 4),
    discount_price     numeric(10, 4),
    prepaid_amount     numeric(10, 4),
    promotions         json,
    currency           varchar,
    is_tax_exempt      boolean,
    create_time        timestamp(6),
    update_time        timestamp(6),
    charge_id          varchar
    );

CREATE TABLE IF NOT EXISTS oms_tax_line
(
    id                              varchar        not null primary key,
    title                           varchar(255)   not null,
    rate                            numeric(5, 4)  not null,
    price                           numeric(10, 2) not null,
    payment_id                      varchar,
    service_payment_id              varchar,
    contract_payment_information_id varchar,
    order_item_id                   varchar
    );