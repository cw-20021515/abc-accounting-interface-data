COPY qbo_item_template
    (template_id,company_code,create_category,create_type,associated_type,asset_account_code,asset_account_name,income_account_code,income_account_name,expense_account_code,expense_account_name,document_type,management_unit)
    FROM '/data/qbo/qbo_item_template.csv'
    DELIMITER ','
    CSV HEADER;


COPY qbo_company
    (code,name,description,country,currency,eng_name,kor_name,fiscal_year,fiscal_month,create_time,update_time,is_active)
    FROM '/data/qbo/qbo_company.csv'
    DELIMITER ','
    CSV HEADER;

-- COPY qbo_credential
--     (realm_id,company_code,active_profile,target_name,client_id,client_secret,scope,access_token,refresh_token,id_token,token_type,access_token_issued_time,access_token_expire_time,refresh_token_issued_time,refresh_token_expire_time,basic_token,sub,given_name,email,create_time,update_time,is_active)
--     FROM '/data/qbo/qbo_credential-local.csv'
--     DELIMITER ','
--     CSV HEADER;
--
-- COPY qbo_credential
--     (realm_id,company_code,active_profile,target_name,client_id,client_secret,scope,access_token,refresh_token,id_token,token_type,access_token_issued_time,access_token_expire_time,refresh_token_issued_time,refresh_token_expire_time,basic_token,sub,given_name,email,create_time,update_time,is_active)
--     FROM '/data/qbo/qbo_credential-dev.csv'
--     DELIMITER ','
--     CSV HEADER;

COPY qbo_credential
    (realm_id,company_code,active_profile,target_name,client_id,client_secret,scope,access_token,refresh_token,id_token,token_type,access_token_issued_time,access_token_expire_time,refresh_token_issued_time,refresh_token_expire_time,basic_token,sub,given_name,email,create_time,update_time,is_active)
    FROM '/data/qbo/qbo_credential-local.csv'
    DELIMITER ','
    CSV HEADER;

-- COPY qbo_credential
--     (realm_id,company_code,active_profile,target_name,client_id,client_secret,scope,access_token,refresh_token,id_token,token_type,access_token_issued_time,access_token_expire_time,refresh_token_issued_time,refresh_token_expire_time,basic_token,sub,given_name,email,create_time,update_time,is_active)
--     FROM '/data/qbo/qbo_credential-prd.csv'
--     DELIMITER ','
--     CSV HEADER;

COPY qbo_account
    (qbo_id,display_name,account_code,account_name,realm_id,company_code,sync_token,submit_result,create_time,update_time,is_active)
    FROM '/data/qbo/qbo_account.csv'
    DELIMITER ','
    CSV HEADER;

-- COPY qbo_customer
--     (qbo_id,customer_id,company_code,customer_type,customer_status,channel_type,title_name,first_name,middle_name,last_name,family_name,name_suffix,fully_qualified_name,company_name,display_name,user_id,primary_phone,alternate_phone,mobile,fax,primary_email,alternate_email,web_addr,sync_token,submit_result,create_time,update_time,is_active)
--     FROM '/data/qbo/qbo_customer.csv'
--     DELIMITER ','
--     CSV HEADER;

-- COPY qbo_item
--     (submit_id,material_id,company_code,template_id,create_category,create_type,associated_type,asset_account_code,asset_account_name,income_account_code,income_account_name,expense_account_code,expense_account_name,document_type,management_unit,description,unit_price,rate_percent,sync_token,create_time,update_time,is_active)
--     FROM '/data/qbo/qbo_item.csv'
--     DELIMITER ','
--     CSV HEADER;

-- COPY qbo_journal_entry
--     (qbo_id,doc_id,company_code,sync_token,submit_result,create_time,update_time,is_active)
--     FROM '/data/qbo/qbo_journal_entry.csv'
--     DELIMITER ','
--     CSV HEADER;