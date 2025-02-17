-- COPY accounts_account_info
--     (id,account_code,account_description,account_name,account_type,classification,create_time,group_code,group_description,group_name,is_group_account,level,parent_account_code,system_source)
--     FROM '/data/payout/accounts_account_info.csv'
--     DELIMITER ','
--     CSV HEADER;
--
--
-- COPY cost_center_info
--     (id,account_type,center_id,center_name,center_sub_type,center_type,company_id,create_time,description,is_active,parent_center_id,system_source)
--     FROM '/data/payout/cost_center_info.csv'
--     DELIMITER ','
--     CSV HEADER;


COPY branch
    (id,company_code,name,description,warehouse_id,phone,time_zone,address_jsonb,is_active)
    FROM '/data/payout/branch.csv'
    DELIMITER ','
    CSV HEADER;

COPY cost_center
    (id,company_code,code,category,parent_id,profit_center_id,name,department_id,description,valid_from_time,valid_to_time,create_time,update_time)
    FROM '/data/payout/cost_center.csv'
    DELIMITER ','
    CSV HEADER;

COPY department
    (id,company_code,code,parent_id,kor_name,eng_name,level,description,create_time,update_time,is_active)
    FROM '/data/payout/department.csv'
    DELIMITER ','
    CSV HEADER;
COPY employee
    (id,company_code,department_id,role_code,role_name,grade_code,grade_name,status,first_name,middle_name,last_name,family_name,name_suffix,phone,mobile,fax,email,address_jsonb,create_time,update_time,is_active)
    FROM '/data/payout/employee.csv'
    DELIMITER ','
    CSV HEADER;
COPY vendor
    (id,company_code,vendor_id,category,first_name,middle_name,last_name,family_name,name_suffix,phone,mobile,fax,email,web_addr,address_jsonb,description,create_time,update_time,is_active,remark)
    FROM '/data/payout/vendor.csv'
    DELIMITER ','
    CSV HEADER;