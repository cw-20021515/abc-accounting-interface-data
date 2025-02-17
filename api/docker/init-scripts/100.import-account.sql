-- data import from csv
-- CSV 파일에서 데이터를 users 테이블로 import
-- CONFIG DATA --
-- COPY account
--     (company_code,account_code,name,description,account_type,account_class,is_active,is_open_item_mgmt,qbo_account_type,qbo_account_subtype,system_source,create_time,update_time)
--     FROM '/data/account/account.csv'
--     DELIMITER ','
--     CSV HEADER;

COPY account
    (company_code,account_code,name,description,account_class,account_type,is_open_item_mgmt,is_active,qbo_account_type,qbo_account_subtype,system_source)
    FROM '/data/account/account_N100.csv'
    DELIMITER ','
    CSV HEADER;

COPY account
    (company_code,account_code,name,description,account_class,account_type,is_open_item_mgmt,is_active,qbo_account_type,qbo_account_subtype,system_source)
    FROM '/data/account/account_N200.csv'
    DELIMITER ','
    CSV HEADER;

COPY account
    (company_code,account_code,name,description,account_class,account_type,is_open_item_mgmt,is_active,qbo_account_type,qbo_account_subtype,system_source)
    FROM '/data/account/account_N300.csv'
    DELIMITER ','
    CSV HEADER;

COPY account
    (company_code,account_code,name,description,account_class,account_type,is_open_item_mgmt,consolidation_account_code,qbo_parent_account_code,qbo_account_type,qbo_account_subtype,system_source,is_active)
    FROM '/data/account/account_T100.csv'
    DELIMITER ','
    CSV HEADER;
--
COPY account
    (company_code,account_code,name,description,account_class,account_type,is_open_item_mgmt,consolidation_account_code,qbo_parent_account_code,qbo_account_type,qbo_account_subtype,system_source,is_active)
    FROM '/data/account/account_T200.csv'
    DELIMITER ','
    CSV HEADER;

COPY account
    (company_code,account_code,name,description,account_class,account_type,is_open_item_mgmt,consolidation_account_code,qbo_parent_account_code,qbo_account_type,qbo_account_subtype,system_source,is_active)
    FROM '/data/account/account_T300.csv'
    DELIMITER ','
    CSV HEADER;

COPY consolidation_account
    (code,level,parent_code,name,eng_name,description,is_postable,is_active,system_source, created_by, updated_by)
    FROM '/data/account/consolidation_account.csv'
    DELIMITER ','
    CSV HEADER;

COPY company
    (code,name,description,country,currency,timezone,fiscal_start_month,is_active)
    FROM '/data/account/company.csv'
    DELIMITER ','
    CSV HEADER;