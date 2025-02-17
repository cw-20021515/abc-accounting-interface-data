CREATE TABLE IF NOT EXISTS logistics_material
(
    id                      varchar                                             not null primary key,
    options                 varchar,
    type                    varchar                                             not null,
    name                    varchar                                             not null,
    model_name              varchar,
    description             text,
    create_time             timestamp                                           not null,
    create_user             varchar,
    update_time             timestamp,
    update_user             varchar,
    manufacturer_code       varchar                                             not null,
    is_data_setup_completed boolean,
    shipping_method_type    varchar                                             not null,
    category_code           varchar default 'WATER_PURIFIER'::character varying not null,
    series_code             varchar,
    series_name             varchar,
    resource_id             varchar,
    content_url             varchar,
    brand_name              varchar default 'NECOA'::character varying          not null,
    is_used                 boolean
);


CREATE TABLE IF NOT EXISTS logistics_warehouse
(
    id                  varchar(32)  not null
        primary key,
    parent_warehouse_id varchar(32),
    name                varchar      not null,
    warehouse_type      varchar(30)  not null,
    region              varchar,
    address_id          varchar,
    is_active           boolean      not null,
    manager_id          varchar      not null,
    description         text,
    is_edi_used         boolean      not null,
    is_used             boolean      not null,
    create_user         varchar      not null,
    create_time         timestamp(6) not null,
    update_user         varchar,
    update_time         timestamp(6),
    time_zone           varchar,
    manager_name        varchar,
    manager_email       varchar
);



CREATE TABLE IF NOT EXISTS logistics_location
(
    id                     varchar      not null
        primary key,
    warehouse_id           varchar(32)  not null,
    zone                   varchar      not null,
    rack                   varchar      not null,
    bin                    varchar      not null,
    description            text,
    is_used                boolean      not null,
    location_type          varchar      not null,
    is_deletable           boolean      not null,
    is_active              boolean      not null,
    is_updatable           boolean      not null,
    is_available_inventory boolean      not null,
    create_user            varchar      not null,
    create_time            timestamp(6) not null,
    update_user            varchar,
    update_time            timestamp(6)
);


CREATE TABLE IF NOT EXISTS logistics_location_inventory
(
    id                     varchar           not null
        primary key,
    location_id            varchar           not null,
    material_id            varchar           not null,
    quantity               integer           not null,
    allocated_quantity     integer default 0 not null,
    create_user            varchar           not null,
    create_time            timestamp(6)      not null,
    update_user            varchar,
    update_time            timestamp(6),
    manufacture_year_month varchar,
    receive_year_month     varchar
);


CREATE TABLE IF NOT EXISTS logistics_location_inventory_history
(
    id                    varchar      not null
        primary key,
    location_inventory_id varchar      not null,
    previous_quantity     integer      not null,
    update_quantity       integer      not null,
    change_type           varchar(20)  not null,
    previous_location_id  varchar,
    update_location_id    varchar,
    inbound_delivery_id   varchar,
    outbound_delivery_id  varchar,
    remark                text,
    create_user           varchar      not null,
    create_time           timestamp(6) not null,
    update_user           varchar,
    update_time           timestamp(6)
);

CREATE TABLE IF NOT EXISTS logistics_inventory_transfer
(
    id                        varchar                                               not null
        primary key,
    to_location_id            varchar                                               not null,
    inventory_transfer_type   varchar                                               not null,
    inventory_transfer_status varchar                                               not null,
    create_user               varchar                                               not null,
    create_time               timestamp(6)                                          not null,
    update_user               varchar,
    update_time               timestamp(6),
    from_location_id          varchar default 'default_location'::character varying not null,
    warehouse_worker_id       varchar,
    work_start_time           timestamp(6),
    work_end_time             timestamp(6),
    cancel_time               timestamp(6),
    cancel_reason             varchar,
    inbound_warehouse_id      varchar
);


CREATE TABLE IF NOT EXISTS logistics_inventory_transfer_serial_number
(
    id                         varchar               not null
        constraint inventory_transfer_serial_number_pk primary key,
    serial_number_id           varchar               not null,
    create_time                timestamp             not null,
    create_user                varchar               not null,
    update_time                timestamp,
    update_user                varchar,
    inventory_transfer_item_id varchar               not null,
    is_picked                  boolean,
    is_initially_assigned      boolean               not null,
    is_placed                  boolean default false not null
);


CREATE TABLE IF NOT EXISTS logistics_safety_inventory
(
    id                         varchar      not null
        primary key,
    warehouse_id               varchar      not null,
    material_id                varchar      not null,
    create_user                varchar      not null,
    create_time                timestamp(6) not null,
    update_user                varchar,
    update_time                timestamp(6),
    forecast_quantity          integer,
    managed_quantity           integer      not null,
    remark                     text,
    max_inventory_quantity     integer,
    near_safety_inventory_rate integer      not null
);


CREATE TABLE IF NOT EXISTS logistics_safety_inventory_history
(
    id                varchar      not null
        primary key,
    warehouse_id      varchar      not null,
    material_id       varchar      not null,
    material_name     varchar      not null,
    remark            text,
    create_user       varchar      not null,
    create_time       timestamp(6) not null,
    update_user       varchar,
    update_time       timestamp(6),
    modify_time       timestamp    not null,
    modify_user       varchar      not null,
    material_type     varchar      not null,
    forecast_quantity integer,
    managed_quantity  integer      not null,
    modification_type varchar      not null
);

CREATE TABLE IF NOT EXISTS logistics_warehouse_transfer
(
    id                      varchar                                            not null
        primary key,
    outbound_delivery_id    varchar,
    inbound_delivery_id     varchar,
    from_warehouse_id       varchar,
    to_warehouse_id         varchar,
    approval_status         varchar                                            not null,
    request_reason          varchar,
    comment                 varchar,
    inquire_user_id         varchar,
    inquire_time            timestamp(6),
    approve_user_id         varchar,
    approve_time            timestamp(6),
    create_user             varchar                                            not null,
    create_time             timestamp(6)                                       not null,
    update_user             varchar,
    update_time             timestamp(6),
    requested_arrival_date  date,
    reject_user_id          varchar,
    reject_time             timestamp,
    reject_reason           varchar,
    warehouse_transfer_type varchar default 'REPLENISHMENT'::character varying not null,
    cancel_user_id          varchar,
    cancel_time             timestamp,
    cancel_reason           varchar
);


CREATE TABLE IF NOT EXISTS logistics_warehouse_transfer_item
(
    id                    varchar      not null
        primary key,
    warehouse_transfer_id varchar      not null,
    material_id           varchar      not null,
    requested_quantity    integer,
    approve_quantity      integer,
    remark                text,
    create_user           varchar      not null,
    create_time           timestamp(6) not null,
    update_user           varchar,
    update_time           timestamp(6)
);


CREATE TABLE IF NOT EXISTS logistics_technician_hold
(
    id                           varchar      not null
        primary key,
    outbound_delivery_id         varchar,
    inbound_delivery_id          varchar,
    outbound_warehouse_id        varchar      not null,
    inbound_warehouse_id         varchar      not null,
    approval_status              varchar      not null,
    remark                       varchar,
    comment                      varchar,
    delete_user                  varchar,
    delete_time                  timestamp(6),
    inquire_user                 varchar,
    inquire_time                 timestamp(6),
    approve_user                 varchar,
    approve_time                 timestamp(6),
    cancel_user                  varchar,
    cancel_time                  timestamp(6),
    reject_user                  varchar,
    reject_time                  timestamp(6),
    create_user                  varchar      not null,
    create_time                  timestamp(6) not null,
    update_user                  varchar,
    update_time                  timestamp(6),
    material_id                  varchar      not null,
    quantity                     integer      not null,
    approve_quantity             integer,
    technician_hold_request_type varchar      not null
);


CREATE TABLE IF NOT EXISTS logistics_address
(
    id         varchar not null
        primary key,
    phone      varchar,
    mobile     varchar,
    city       varchar not null,
    state      varchar not null,
    zipcode    varchar not null,
    address1   varchar not null,
    address2   varchar not null,
    first_name varchar not null,
    last_name  varchar not null
);

CREATE TABLE IF NOT EXISTS logistics_serial_number
(
    id                     varchar                                                 not null constraint serial_number_pk primary key,
    material_id            varchar                                                 not null,
    factory                varchar                                                 not null,
    line                   varchar                                                 not null,
    code                   varchar                                                 not null,
    manufacture_date_code  varchar                                                 not null,
    manufacture_date       date                                                    not null,
    number                 varchar                                                 not null,
    create_time            timestamp(6)                                            not null,
    update_time            timestamp(6),
    customer_address_id    varchar,
    allocation_status      varchar(255) default 'NOT_ALLOCATED'::character varying not null,
    first_inbound_time     timestamp(6) default now()                              not null,
    current_warehouse_id   varchar,
    current_location_id    varchar,
    current_material_grade varchar      default 'GRADE_B'::character varying       not null,
    last_movement_time     timestamp(6)
);

CREATE TABLE IF NOT EXISTS logistics_serial_number_history
(
    id                           varchar      not null constraint serial_number_history_pk primary key,
    warehouse_id                 varchar,
    location_id                  varchar,
    material_grade               varchar      not null,
    customer_address_id          varchar,
    serial_number_id             varchar      not null,
    create_time                  timestamp(6) not null,
    warehouse_first_inbound_time timestamp(6),
    last_inbound_time            timestamp(6)
);


CREATE TABLE IF NOT EXISTS logistics_purchase_order
(
    id                    varchar                                                  not null
        constraint purchase_order_pk
            primary key,
    vendor_id             varchar                                                  not null,
    inbound_warehouse_id  varchar                                                  not null,
    remark                text,
    inbound_delivery_id   varchar,
    create_user           varchar                                                  not null,
    create_time           timestamp(6)                                             not null,
    update_user           varchar,
    update_time           timestamp(6),
    cancel_reason         varchar,
    cancel_time           timestamp(6),
    cancel_user           varchar,
    purchase_order_status varchar                                                  not null,
    send_user             varchar,
    send_time             timestamp(6),
    vendor_name           varchar default 'coway'::character varying               not null,
    vendor_address_id     varchar default 'address'::character varying             not null,
    recipient_email       varchar default 'example@example.com'::character varying not null
);

CREATE TABLE IF NOT EXISTS logistics_purchase_order_item
(
    id                varchar                                  not null
        constraint purchase_order_item_pk
            primary key,
    material_id       varchar                                  not null,
    quantity          integer                                  not null,
    remark            text,
    purchase_order_id varchar                                  not null,
    create_user       varchar                                  not null,
    create_time       timestamp(6)                             not null,
    update_user       varchar,
    update_time       timestamp(6),
    unit_price        numeric default 0                        not null,
    currency          varchar default 'USD'::character varying not null
);

CREATE TABLE IF NOT EXISTS logistics_inbound_delivery
(
    id                               varchar               not null
        constraint inbound_delivery_pk
            primary key,
    inbound_warehouse_id             varchar,
    remark                           text,
    estimated_arrival_date           date                  not null,
    inbound_delivery_status          varchar               not null,
    inbound_delivery_type            varchar               not null,
    tracking_number                  varchar,
    complete_time                    timestamp(6),
    cancel_time                      timestamp(6),
    create_user                      varchar               not null,
    create_time                      timestamp(6)          not null,
    update_user                      varchar,
    update_time                      timestamp(6),
    service_flow_id                  varchar,
    outbound_warehouse_id            varchar,
    from_address_id                  varchar,
    purchase_order_id                varchar,
    shipping_time                    timestamp,
    technician_hold_request_type     varchar,
    cancel_user                      varchar,
    signature_resource_id            varchar,
    complete_user                    varchar,
    signature_content_url            varchar,
    is_overall_inspection_successful boolean default false not null,
    bill_of_lading_number            varchar
);


CREATE TABLE IF NOT EXISTS logistics_inbound_delivery_item
(
    id                           varchar                                      not null
        constraint inbound_delivery_item_pk
            primary key,
    material_id                  varchar                                      not null,
    quantity                     integer                                      not null,
    total_pallet                 integer,
    boxes_per_pallet             integer,
    units_per_box                integer,
    remark                       text,
    receive_quantity             integer                                      not null,
    inbound_delivery_item_status varchar                                      not null,
    inbound_delivery_id          varchar                                      not null,
    inspect_time                 timestamp(6),
    inspect_user                 varchar,
    complete_time                timestamp(6),
    complete_user                varchar,
    cancel_time                  timestamp(6),
    create_user                  varchar                                      not null,
    create_time                  timestamp(6)                                 not null,
    update_user                  varchar,
    update_time                  timestamp(6),
    inspection_status            varchar default 'INVALID'::character varying not null,
    inspection_status_detail     jsonb
);

CREATE TABLE IF NOT EXISTS logistics_inbound_delivery_serial_number
(
    id                       varchar not null
        constraint inbound_delivery_serial_number_pk
            primary key,
    serial_number            varchar not null,
    inbound_delivery_item_id varchar not null,
    constraint unique_serial_and_inbound
        unique (serial_number, inbound_delivery_item_id)
);


CREATE TABLE IF NOT EXISTS logistics_receive_serial_number
(
    id                       varchar not null
        constraint receive_serial_number_pk
            primary key,
    serial_number            varchar not null,
    inbound_delivery_item_id varchar not null,
    constraint unique_serial_and_receive
        unique (serial_number, inbound_delivery_item_id)
);


CREATE TABLE IF NOT EXISTS logistics_purchase_order_inbound_delivery
(
    id                  varchar      not null
        constraint purchase_order_inbound_delivery_pk
            primary key,
    purchase_order_id   varchar      not null,
    inbound_delivery_id varchar      not null,
    from_address_id     varchar      not null,
    to_address_id       varchar      not null,
    create_user         varchar      not null,
    create_time         timestamp(6) not null,
    update_user         varchar,
    update_time         timestamp(6)
);


CREATE TABLE IF NOT EXISTS logistics_outbound_delivery
(
    id                       varchar   not null
        constraint outbound_delivery_pk
            primary key,
    outbound_warehouse_id    varchar   not null,
    outbound_delivery_type   varchar   not null,
    estimated_shipping_date  date,
    shipping_time            timestamp,
    cancel_time              timestamp,
    cancel_user_id           varchar,
    tracking_number          varchar,
    remark                   text,
    outbound_delivery_status varchar   not null,
    create_time              timestamp not null,
    create_user              varchar   not null,
    update_time              timestamp,
    update_user              varchar,
    service_flow_id          varchar,
    to_address_id            varchar,
    signature_resource_id    varchar,
    signature_content_url    varchar,
    reference_id             varchar,
    desired_shipping_date    date
);

CREATE TABLE IF NOT EXISTS logistics_outbound_delivery_item
(
    id                            varchar                                        not null
        constraint outbound_delivery_item_pk
            primary key,
    outbound_delivery_id          varchar                                        not null,
    material_id                   varchar                                        not null,
    quantity                      integer                                        not null,
    remark                        text,
    create_time                   timestamp                                      not null,
    create_user                   varchar                                        not null,
    update_time                   timestamp,
    update_user                   varchar,
    outbound_delivery_item_status varchar default 'SCHEDULED'::character varying not null,
    shipping_time                 timestamp,
    cancel_time                   timestamp
);

CREATE TABLE IF NOT EXISTS logistics_outbound_delivery_serial_number
(
    id                        varchar               not null
        constraint outbound_delivery_serial_number_pk
            primary key,
    outbound_delivery_item_id varchar               not null,
    serial_number_id          varchar               not null,
    create_time               timestamp             not null,
    create_user               varchar               not null,
    update_time               timestamp,
    update_user               varchar,
    is_shipped                boolean default false not null,
    is_opened                 boolean default false
);

CREATE TABLE IF NOT EXISTS logistics_inventory_adjustment
(
    id                          varchar   not null
        primary key,
    inquire_user_id             varchar,
    inquire_time                timestamp(6),
    remark                      text,
    approve_user_id             varchar,
    approve_time                timestamp(6),
    reject_user_id              varchar,
    reject_time                 timestamp(6),
    inventory_adjustment_status varchar   not null,
    create_time                 timestamp not null,
    create_user                 varchar   not null,
    update_time                 timestamp,
    update_user                 varchar,
    inventory_audit_id          varchar   not null,
    approve_comment             text,
    reject_reason               text
);


CREATE TABLE IF NOT EXISTS logistics_material_master
(
    id                                  varchar(16)           not null
        primary key,
    material_id                         varchar               not null
        unique,
    is_serial_managed                   boolean               not null,
    boxes_per_pallet                    integer default 0,
    units_per_box                       integer default 0,
    create_user                         varchar               not null,
    create_time                         timestamp(6)          not null,
    update_user                         varchar,
    update_time                         timestamp(6),
    is_technician_hold_allowed          boolean default false not null,
    is_technician_hold_quantity_managed boolean default true  not null,
    is_technician_hold_auto_approve     boolean default false not null,
    pallets_per_container               integer default 0,
    package_box_weight                  double precision,
    moq_unit                            integer,
    moq_pallet                          integer,
    package_box_dimensions              varchar,
    purchase_order_unit_price           numeric(38, 4),
    currency                            varchar
);


CREATE TABLE IF NOT EXISTS logistics_daily_inventory_data
(
    id                  varchar(16)       not null
        primary key,
    snapshot_date       timestamp         not null,
    warehouse_id        varchar           not null,
    material_id         varchar           not null,
    total_inventory     integer default 0 not null,
    available_inventory integer default 0 not null,
    safety_inventory    integer default 0 not null,
    create_user         varchar           not null,
    create_time         timestamp(6)      not null,
    update_user         varchar,
    update_time         timestamp(6),
    outbound_quantity   integer default 0 not null
);

CREATE TABLE IF NOT EXISTS logistics_warehouse_worker
(
    id                  varchar                               not null
        primary key,
    email               varchar                               not null
        unique,
    first_name          varchar                               not null,
    last_name           varchar                               not null,
    is_used             boolean                               not null,
    create_user         varchar                               not null,
    create_time         timestamp(6)                          not null,
    update_user         varchar,
    update_time         timestamp(6),
    delete_user         varchar,
    delete_time         timestamp(6),
    mobile              varchar default ''::character varying not null,
    phone               varchar,
    picture_resource_id varchar default ''::character varying not null,
    picture_content_url varchar default ''::character varying not null
);


CREATE TABLE IF NOT EXISTS logistics_inventory_transfer_item
(
    id                             varchar           not null
        primary key,
    inventory_transfer_id          varchar           not null,
    material_id                    varchar           not null,
    planned_quantity               integer           not null,
    manufacture_year_month         varchar,
    receive_year_month             varchar,
    picked_quantity                integer,
    picked_time                    timestamp(6),
    placed_time                    timestamp(6),
    inventory_transfer_item_status varchar,
    create_user                    varchar           not null,
    create_time                    timestamp(6)      not null,
    update_user                    varchar,
    update_time                    timestamp(6),
    placed_quantity                integer default 0 not null
);


CREATE TABLE IF NOT EXISTS logistics_notification_log
(
    id          varchar      not null
        primary key,
    type        varchar      not null,
    template_id varchar      not null,
    recipient   varchar      not null,
    status_code integer,
    code        varchar,
    message     varchar,
    message_id  varchar,
    error       varchar,
    create_time timestamp(6) not null
);

CREATE TABLE IF NOT EXISTS logistics_outbound_item_transfer_mapping
(
    id                         varchar   not null
        constraint outbound_item_transfer_mapping_pk
            primary key,
    outbound_delivery_item_id  varchar   not null,
    inventory_transfer_item_id varchar   not null,
    allocated_quantity         integer   not null,
    create_time                timestamp not null,
    create_user                varchar   not null,
    update_time                timestamp,
    update_user                varchar
);

CREATE TABLE IF NOT EXISTS logistics_organization
(
    id          varchar(16)  not null
        primary key,
    name        varchar(30)  not null
        unique,
    is_used     boolean      not null,
    create_user varchar      not null,
    create_time timestamp(6) not null,
    update_user varchar,
    update_time timestamp(6)
);

CREATE TABLE IF NOT EXISTS logistics_employee
(
    id              varchar(16)  not null
        primary key,
    organization_id varchar(16)  not null,
    name            varchar(30)  not null,
    is_used         boolean      not null,
    create_user     varchar      not null,
    create_time     timestamp(6) not null,
    update_user     varchar,
    update_time     timestamp(6)
);


CREATE TABLE IF NOT EXISTS logistics_aging_range
(
    id          varchar      not null
        primary key,
    label       varchar      not null,
    start_day   integer      not null,
    end_day     integer,
    description text,
    is_used     boolean      not null,
    create_time timestamp(6) not null,
    create_user varchar,
    update_time timestamp(6),
    update_user varchar,
    delete_time timestamp(6),
    delete_user varchar
);

CREATE TABLE IF NOT EXISTS logistics_daily_inventory_aging
(
    id                  varchar      not null
        primary key,
    material_id         varchar      not null,
    warehouse_id        varchar      not null,
    in_transit_quantity integer,
    record_date         date         not null,
    create_time         timestamp(6) not null
);



CREATE TABLE IF NOT EXISTS logistics_inventory_grade_aging_summary
(
    id                       varchar      not null
        primary key,
    daily_inventory_aging_id varchar      not null,
    material_grade           varchar      not null,
    total_quantity           integer      not null,
    create_time              timestamp(6) not null
);


CREATE TABLE IF NOT EXISTS logistics_inventory_grade_aging_detail
(
    id                               varchar      not null
        primary key,
    inventory_grade_aging_summary_id varchar      not null,
    aging_range_id                   varchar      not null,
    quantity                         integer      not null,
    create_time                      timestamp(6) not null
);


CREATE TABLE IF NOT EXISTS logistics_inventory_adjustment_item
(
    id                      varchar           not null
        primary key,
    inventory_adjustment_id varchar           not null,
    location_id             varchar           not null,
    material_id             varchar           not null,
    system_quantity         integer default 0 not null,
    counted_quantity        integer default 0 not null,
    adjustment_reason       varchar,
    remark                  text,
    create_time             timestamp         not null,
    create_user             varchar           not null,
    update_time             timestamp,
    update_user             varchar
);


CREATE TABLE IF NOT EXISTS logistics_inventory_audit
(
    id                         varchar(16)  not null
        primary key,
    warehouse_id               varchar      not null,
    inventory_audit_date       date         not null,
    inventory_audit_start_time timestamp(6),
    complete_time              timestamp(6),
    cancel_time                timestamp(6),
    cancel_user_id             varchar,
    cancel_reason              varchar,
    inventory_audit_status     varchar      not null,
    remark                     text,
    create_user                varchar      not null,
    create_time                timestamp(6) not null,
    update_user                varchar,
    update_time                timestamp(6)
);

CREATE TABLE IF NOT EXISTS logistics_inventory_audit_item
(
    id                          varchar(16)       not null
        primary key,
    inventory_audit_id          varchar           not null,
    location_id                 varchar           not null,
    material_id                 varchar           not null,
    warehouse_worker_id         varchar,
    system_quantity             integer default 0 not null,
    counted_quantity            integer default 0 not null,
    remark                      text,
    inventory_audit_item_status varchar           not null,
    create_user                 varchar           not null,
    create_time                 timestamp(6)      not null,
    update_user                 varchar,
    update_time                 timestamp(6),
    discrepancy_reason          varchar,
    count_time                  timestamp
);

CREATE TABLE IF NOT EXISTS logistics_inventory_audit_serial_number
(
    id                        varchar(16)                                       not null
        primary key,
    inventory_audit_item_id   varchar                                           not null,
    serial_number             varchar                                           not null,
    create_user               varchar                                           not null,
    create_time               timestamp(6)                                      not null,
    update_user               varchar,
    update_time               timestamp(6),
    serial_number_audit_state varchar default 'NOT_IN_AUDIT'::character varying not null,
    current_warehouse_id      varchar,
    current_location_id       varchar
);


CREATE TABLE IF NOT EXISTS logistics_daily_snapshot
(
    id                                            varchar(16)       not null
        primary key,
    snapshot_date                                 date              not null,
    warehouse_id                                  varchar           not null,
    material_id                                   varchar           not null,
    courier_request_quantity                      integer default 0 not null,
    courier_canceled_quantity                     integer default 0 not null,
    courier_completed_quantity                    integer default 0 not null,
    install_request_quantity                      integer default 0 not null,
    install_canceled_quantity                     integer default 0 not null,
    install_completed_quantity                    integer default 0 not null,
    as_request_quantity                           integer default 0 not null,
    as_canceled_quantity                          integer default 0 not null,
    as_completed_quantity                         integer default 0 not null,
    technician_hold_request_quantity              integer default 0 not null,
    technician_hold_canceled_quantity             integer default 0 not null,
    technician_hold_completed_quantity            integer default 0 not null,
    warehouse_transfer_request_quantity           integer default 0 not null,
    warehouse_transfer_canceled_quantity          integer default 0 not null,
    warehouse_transfer_completed_quantity         integer default 0 not null,
    purchase_order_request_quantity               integer default 0 not null,
    purchase_order_canceled_quantity              integer default 0 not null,
    purchase_order_completed_quantity             integer default 0 not null,
    technician_hold_return_request_quantity       integer default 0 not null,
    technician_hold_return_canceled_quantity      integer default 0 not null,
    technician_hold_return_completed_quantity     integer default 0 not null,
    courier_return_request_quantity               integer default 0 not null,
    courier_return_canceled_quantity              integer default 0 not null,
    courier_return_completed_quantity             integer default 0 not null,
    install_cancel_return_request_quantity        integer default 0 not null,
    install_cancel_return_canceled_quantity       integer default 0 not null,
    install_cancel_return_completed_quantity      integer default 0 not null,
    uninstall_return_request_quantity             integer default 0 not null,
    uninstall_return_canceled_quantity            integer default 0 not null,
    uninstall_return_completed_quantity           integer default 0 not null,
    as_cancel_return_request_quantity             integer default 0 not null,
    as_cancel_return_canceled_quantity            integer default 0 not null,
    as_cancel_return_completed_quantity           integer default 0 not null,
    warehouse_transfer_inbound_request_quantity   integer default 0 not null,
    warehouse_transfer_inbound_canceled_quantity  integer default 0 not null,
    warehouse_transfer_inbound_completed_quantity integer default 0 not null,
    grade_a_quantity                              integer default 0 not null,
    grade_b_quantity                              integer default 0 not null,
    total_quantity                                integer default 0 not null,
    create_user                                   varchar           not null,
    create_time                                   timestamp(6)      not null,
    update_user                                   varchar,
    update_time                                   timestamp(6)
);

CREATE TABLE IF NOT EXISTS logistics_worker_assigned_warehouse
(
    id                    varchar                    not null
        primary key,
    warehouse_worker_id   varchar                    not null,
    assigned_warehouse_id varchar                    not null,
    create_time           timestamp(6) default now() not null,
    constraint unique_warehouse_worker_and_assigned_warehouse
        unique (warehouse_worker_id, assigned_warehouse_id)
);

CREATE TABLE IF NOT EXISTS logistics_bill_of_lading
(
    bill_of_lading_number    varchar      not null
        primary key,
    purchase_order_id        varchar      not null,
    incoterms                varchar      not null,
    notify_party_description varchar      not null,
    notify_party_street      varchar,
    notify_party_city        varchar,
    notify_party_zip_code    varchar,
    notify_party_name        varchar,
    notify_party_email       varchar(241),
    notify_party_phone       varchar(30),
    port_of_loading          varchar(25)  not null,
    port_of_discharge        varchar(25)  not null,
    shipper_name             varchar(55)  not null,
    shipper_street           varchar(100),
    shipper_city             varchar(70),
    shipper_zip_code         varchar(10),
    shipper_contact_name     varchar(40),
    shipper_email            varchar(241),
    shipper_phone            varchar(30),
    consignee_name           varchar(55)  not null,
    consignee_street         varchar(100),
    consignee_city           varchar(70),
    consignee_zip_code       varchar(10),
    consignee_contact_name   varchar(40),
    consignee_email          varchar(241),
    consignee_phone          varchar(30),
    vessel_name              varchar(25),
    voyage_number            varchar(10),
    shipping_time            timestamp    not null,
    is_new                   boolean      not null,
    is_update                boolean      not null,
    is_used                  boolean      not null,
    create_time              timestamp(6) not null,
    update_time              timestamp(6),
    delete_time              timestamp(6)
);


CREATE TABLE IF NOT EXISTS logistics_bill_of_lading_item
(
    id                    varchar      not null
        primary key,
    bill_of_lading_number varchar      not null,
    delivery_note_id      varchar      not null,
    delivery_note_item_id varchar      not null,
    manufacturer_code     varchar      not null,
    quantity              integer      not null,
    unit                  varchar(10)  not null,
    create_time           timestamp(6) not null,
    update_time           timestamp(6)
);


CREATE TABLE IF NOT EXISTS logistics_inventory_adjustment_serial_number
(
    id                           varchar                                           not null
        primary key,
    inventory_adjustment_item_id varchar                                           not null,
    serial_number                varchar                                           not null,
    adjustment_reason            varchar,
    remark                       text,
    create_user                  varchar                                           not null,
    create_time                  timestamp(6)                                      not null,
    update_user                  varchar,
    update_time                  timestamp(6),
    serial_number_audit_state    varchar default 'NOT_IN_AUDIT'::character varying not null,
    current_warehouse_id         varchar,
    current_location_id          varchar
);


CREATE TABLE IF NOT EXISTS logistics_dispatch
(
    id                   varchar   not null primary key,
    outbound_delivery_id varchar   not null,
    assignee_type        varchar   not null,
    assignee_id          varchar,
    assignee_name        varchar,
    vehicle_number       varchar,
    assignee_contact     varchar,
    vendor_id            varchar,
    create_time          timestamp not null,
    create_user          varchar   not null,
    update_time          timestamp,
    update_user          varchar,
    tracking_number      varchar
);
