COPY collect_channel
    (hash_code,entity,field,value,channel_id,channel_name,channel_type,channel_detail,create_time,update_time,is_active)
    FROM '/data/collects/collect_channel.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_charge
    (hash_code,total_price,discount_price,item_price,prepaid_amount,tax,registration_price,currency,is_tax_exempt,invoice_id,charge_id,charge_status,billing_cycle,target_month,contract_id,receipt_id,start_date,end_date,create_time,update_time,is_active)
    FROM '/data/collects/collect_charge.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_charge_item
    (hash_code,entity,field,value,total_price,discount_price,item_price,prepaid_amount,tax,registration_price,currency,is_tax_exempt,invoice_id,charge_id,charge_item_id,charge_item_type,service_flow_id,quantity,receipt_id,create_time,update_time,is_active)
    FROM '/data/collects/collect_charge_item.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_contract
    (hash_code,entity,field,value,contract_id,channel_contract_id,channel_order_item_id,is_signed,signed_time,form_id,revision,rental_code,customer_id,order_id,order_item_id,material_id,start_date,end_date,duration_in_months,contract_status,create_time,update_time,is_active)
    FROM '/data/collects/collect_contract.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_customer
    (hash_code,customer_id,title_name,first_name,middle_name,last_name,family_name,name_suffix,fully_qualified_name,company_name,display_name,user_id,primary_phone,alternate_phone,mobile,fax,primary_email,alternate_email,web_addr,entity,field,value,channel_customer_id,customer_status,channel_type,customer_type,is_tax_liability,currency,installation_id,referrer_code,create_time,update_time,is_active)
    FROM '/data/collects/collect_customer.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_deposit
    (hash_code,create_time,update_time,is_active,entity,field,value,transaction_id,deposit_id,currency,deposit_date,amount,adjustments_fee_amount,adjustments_gross_amount,charges_fee_amount,charges_gross_amount,refunds_fee_amount,refunds_gross_amount,reserved_funds_fee_amount,reserved_funds_gross_amount,retried_deposits_fee_amount,retried_deposits_gross_aamount,sales_fee_amount,sales_gross_amount,fees,gross,net)
    FROM '/data/collects/collect_deposit.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_installation
    (hash_code,entity,field,value,latitude,longitude,installation_time,warranty_end_time,warranty_start_time,address1,address2,alternate_email,alternate_phone,branch_id,city,company_name,country,country_code,county,display_name,family_name,fax,first_name,fully_qualified_name,install_id,last_name,location_remark,middle_name,mobile,name_suffix,order_item_id,primary_email,primary_phone,serial_number,service_flow_id,state,technician_id,title_name,user_id,warehouse_id,water_type,web_addr,zip_code,create_time,update_time,is_active)
    FROM '/data/collects/collect_installation.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_inventory_valuation
    (hash_code,issued_time,material_id,currency,movement_type,grade_type,stock_avg_unit_price,moving_avg_method,remark,create_time,update_time,is_active)
    FROM '/data/collects/collect_inventory_valuation.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_location
    (hash_code,entity,field,value,location_id,branch_id,warehouse_id,latitude,longitude,address1,address2,alternate_email,alternate_phone,city,company_name,country,country_code,county,display_name,family_name,fax,first_name,fully_qualified_name,last_name,location_remark,middle_name,mobile,name_suffix,primary_email,primary_phone,state,title_name,user_id,web_addr,zip_code,create_time,update_time,is_active)
    FROM '/data/collects/collect_location.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_material
    (hash_code,entity,field,value,feature_code,description,filter_type,installation_type,material_brand_name,material_category_code,material_id,material_model_name,material_name,material_series_code,material_type,product_type,create_time,update_time,is_active)
    FROM '/data/collects/collect_material.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_order
    (hash_code,entity,field,value,channel_id,channel_order_id,customer_id,order_id,order_product_type,receipt_id,shipping_id,referrer_code,order_create_time,order_update_time,update_time,create_time,is_active)
    FROM '/data/collects/collect_order.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_order_item
    (hash_code,entity,field,value,total_price,discount_price,item_price,prepaid_amount,tax,registration_price,currency,is_tax_exempt,quantity,channel_order_id,channel_order_item_id,contract_id,install_id,shipping_id,material_id,order_id,order_item_id,order_item_status,order_item_type,create_time,update_time,is_active)
    FROM '/data/collects/collect_order_item.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_receipt
    (hash_code,entity,field,value,title_name,first_name,middle_name,last_name,family_name,name_suffix,fully_qualified_name,company_name,display_name,user_id,primary_phone,alternate_phone,mobile,fax,primary_email,alternate_email,web_addr,branch_id,warehouse_id,latitude,longitude,city,country,country_code,zip_code,state,county,address1,address2,location_remark,total_price,discount_price,item_price,prepaid_amount,tax,registration_price,currency,is_tax_exempt,invoice_id,receipt_id,charge_id,deposit_id,transaction_id,installment_months,item_monthly_price,monthly_discount_price,monthly_tax,monthly_total_price,subscription_receipt_day,receipt_time,billing_type,card_number,card_type,receipt_method,remark,create_time,update_time,is_active)
    FROM '/data/collects/collect_receipt.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_service_flow
    (hash_code,billing_id,customer_service_id,customer_service_ticket_id,order_id,order_item_id,service_flow_id,service_status,service_type,work_id,cancel_time,create_time,update_time,is_active)
    FROM '/data/collects/collect_service_flow.csv'
    DELIMITER ','
    CSV HEADER;

COPY collect_tax_line
    (hash_code,entity,field,value,price,rate,title,sales_tax_type,create_time,update_time,is_active)
    FROM '/data/collects/collect_tax_line.csv'
    DELIMITER ','
    CSV HEADER;