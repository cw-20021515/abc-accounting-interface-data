COPY audit_target_entity
    (id,entity_name,company_id,audit_action_type,is_active)
    FROM '/data/audit/audit_target_entity.csv'
    DELIMITER ','
    CSV HEADER;