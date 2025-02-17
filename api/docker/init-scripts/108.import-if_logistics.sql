-- data import from csv

COPY if_warehouse
    (id,warehouse_id,parent_warehouse_id,name,warehouse_type,time_zone,is_active,create_time)
    FROM '/data/inventory/if_warehouse.csv'
    DELIMITER ','
    CSV HEADER;

-- COPY if_material
--     (material_id,material_type,material_model_name,material_model_name_prefix,material_name,material_series_code,material_series_name,material_category_code,material_category_name,manufacturer_code,brand_name,product_type,feature_code,filter_type,installation_type,shipping_method_type,description,create_time,update_time)
--     FROM '/data/inventory/if_material.csv'
--     DELIMITER ','
--     CSV HEADER;

COPY if_purchase_order
    (id,purchase_order_id,purchase_order_status,vendor_id,customer_id,create_time)
    FROM '/data/inventory/if_purchase_order.csv'
    DELIMITER ','
    CSV HEADER;

COPY if_purchase_order_item
    (id,purchase_order_id,purchase_order_item_id,material_id,quantity,unit_price,currency,create_time)
    FROM '/data/inventory/if_purchase_order_item.csv'
    DELIMITER ','
    CSV HEADER;

COPY if_inventory_movement
    (id,source_warehouse_id,destination_warehouse_id,warehouse_transfer_id,transfer_status,movement_category,movement_group,movement_type,service_flow_id,inbound_purchase_order_item_id,inbound_bl_no,material_id,grade,quantity,create_time)
    FROM '/data/inventory/if_inventory_movement.csv'
    DELIMITER ','
    CSV HEADER;

COPY if_aging_range
    (id,range_id,label,min_aging_days,max_aging_days,is_active,create_time)
    FROM '/data/inventory/if_aging_range.csv'
    DELIMITER ','
    CSV HEADER;

-- COPY accounts_payable
--     (id,tx_id,accounting_id,remark,title,description,payment_id,payment_type,transaction_type,payment_date_time,payment_sub_total_amount,payment_total_amount,payment_status,payment_blocking_reason,payment_retry,payment_currency,payment_balance,payout_amount,tax_amount,local_currency,local_amount,create_time,document_time,entry_time,posting_time,process_time,due_time,is_expired,is_completed,supplier_id,customer_id,drafter_id,cost_center,invoice_id,purchase_order_id,bill_of_lading_id,approval_tx_id,attachments_tx_id,company_id)
--     FROM '/data/inventory/accounts_payable.csv'
--     DELIMITER ','
--     CSV HEADER;
--
-- COPY inventory_closing_stock
--     (id,company_code,closing_date,warehouse_id,material_id,grade,quantity,is_active,create_time)
--     FROM '/data/inventory/inventory_closing_stock.csv'
--     DELIMITER ','
--     CSV HEADER;
--
-- COPY if_warehouse_inventory_age_snapshot
--     (id,warehouse_id,material_id,grade,min_aging_days,max_aging_days,quantity,create_time)
--     FROM '/data/inventory/if_warehouse_inventory_age_snapshot.csv'
--     DELIMITER ','
--     CSV HEADER;
--
-- COPY inventory_aging_loss_rule
--     (id,company_code,material_id,rule_name,grade,min_aging_days,max_aging_days,adjustment_method,adjustment_value,currency,create_user,create_time,update_user,update_time,is_active)
--     FROM '/data/inventory/inventory_aging_loss_rule.csv'
--     DELIMITER ','
--     CSV HEADER;
--
-- COPY inventory_costing
--     (id,company_code,costing_date,method,start_date,end_date,warehouse_id,material_id,grade,currency,unit_cost,is_active,create_time)
--     FROM '/data/inventory/inventory_costing.csv'
--     DELIMITER ','
--     CSV HEADER;
--
-- COPY inventory_valuation
--     (id,company_code,costing_id,closing_date,warehouse_id,material_id,grade,currency,unit_cost,quantity,total_value,is_active,create_time)
--     FROM '/data/inventory/inventory_valuation.csv'
--     DELIMITER ','
--     CSV HEADER;
