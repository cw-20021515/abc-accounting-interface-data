------------------------------------------------------------------------------------------------------------------------
--- IMPORT DOCUMENTS CONFIG
------------------------------------------------------------------------------------------------------------------------
COPY document_item_attribute_master
    (id,account_type,attribute_category,attribute_type,field_requirement,is_active)
    FROM '/data/documents/document_item_attribute_master.csv'
    DELIMITER ','
    CSV HEADER;

COPY document_template
    (company_code,doc_template_code,symbol,kor_text,eng_text,biz_category,biz_system,biz_process,biz_event,account_event_category,account_event,is_active,biz_event_order,document_type,processing_type,order_item_status,service_flow_type,service_flow_status,charge_status,contract_status,logistics_status)
    FROM '/data/documents/document_template.csv'
    DELIMITER ','
    CSV HEADER;

COPY document_template
    (company_code,doc_template_code,symbol,kor_text,eng_text,biz_category,biz_system,biz_process,biz_event,account_event_category,account_event,is_active,biz_event_order,document_type,processing_type,order_item_status,service_flow_type,service_flow_status,charge_status,contract_status,logistics_status)
    FROM '/data/documents/document_template_T200.csv'
    DELIMITER ','
    CSV HEADER;

COPY document_template_item
    (company_code,doc_template_code,line_order,account_code,ref_doc_template_code,account_side,requirement_type,cost_center,profit_center,segment,project,item_text_kor,item_text_eng)
    FROM '/data/documents/document_template_item.csv'
    DELIMITER ','
    CSV HEADER;

COPY document_template_item
    (company_code,doc_template_code,line_order,account_code,ref_doc_template_code,account_side,requirement_type,cost_center,profit_center,segment,project,item_text_kor,item_text_eng)
    FROM '/data/documents/document_template_item_T200.csv'
    DELIMITER ','
    CSV HEADER;


COPY exchange_rate
    (id,from_currency,to_currency,exchange_rate_date,exchange_rate)
    FROM '/data/documents/exchange_rate.csv'
    DELIMITER ','
    CSV HEADER;
------------------------------------------------------------------------------------------------------------------------
--- IMPORT DOCUMENTS DATA
------------------------------------------------------------------------------------------------------------------------
-- COPY custom_sequence
--     (sequence_name,current_value,version)
--     FROM '/data/documents/custom_sequence.csv'
--     DELIMITER ','
--     CSV HEADER;
-- COPY document
--     (id,version,doc_hash,doc_type,doc_status,workflow_status,workflow_id,document_date,posting_date,entry_date,fiscal_year,fiscal_month,company_code,tx_currency,tx_amount,currency,amount,reference,text,is_deleted,create_time,created_by,update_time,updated_by)
--     FROM '/data/documents/document.csv'
--     DELIMITER ','
--     CSV HEADER;
-- COPY document_history
--     (id,doc_id,version,doc_hash,doc_type,doc_status,workflow_status,workflow_id,document_date,posting_date,entry_date,fiscal_year,fiscal_month,company_code,tx_currency,tx_amount,currency,amount,reference,text,is_deleted,create_time,created_by,update_time,updated_by)
--     FROM '/data/documents/document_history.csv'
--     DELIMITER ','
--     CSV HEADER;
-- COPY document_item
--     (id,version,doc_item_status,doc_id,line_number,account_code,account_side,company_code,tx_currency,tx_amount,currency,amount,exchange_rate_id,text,doc_template_code,cost_center,profit_center,segment,project,customer_id,vendor_id,create_time,created_by,update_time,updated_by)
--     FROM '/data/documents/document_item.csv'
--     DELIMITER ','
--     CSV HEADER;
-- COPY document_item_attribute
--     (doc_item_id,attribute_type,value,create_time)
--     FROM '/data/documents/document_item_attribute.csv'
--     DELIMITER ','
--     CSV HEADER;
-- COPY document_item_history
--     (id,doc_item_id,version,doc_item_status,doc_id,line_number,account_code,account_side,company_code,tx_currency,tx_amount,currency,amount,exchange_rate_id,text,cost_center,profit_center,segment,project,customer_id,vendor_id,create_time,created_by,update_time,updated_by)
--     FROM '/data/documents/document_item_history.csv'
--     DELIMITER ','
--     CSV HEADER;
-- COPY document_origin
--     (doc_id,doc_template_code,biz_system,biz_tx_id,biz_process,biz_event,accounting_event)
--     FROM '/data/documents/document_origin.csv'
--     DELIMITER ','
--     CSV HEADER;