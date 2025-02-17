CREATE TABLE IF NOT EXISTS "audit_target_entity"
(
    "id"                      bigserial primary key,
    "entity_name"             varchar not null,
    "company_id"              varchar not null,
    "audit_action_type"       varchar,
    "is_active"               char check (is_active = ANY (ARRAY ['N'::bpchar, 'Y'::bpchar]))
);


CREATE TABLE IF NOT EXISTS "audit_entity_log"
(
    "id"               bigserial primary key,
    "action_type"      varchar not null,
    "entity_id"        varchar not null,
    "company_id"       varchar not null,
    "timestamp"        timestamp(6) not null,
    "event_table_name" varchar not null,
    "event_table_id"   varchar not null,    
    "processed"        boolean      not null
);