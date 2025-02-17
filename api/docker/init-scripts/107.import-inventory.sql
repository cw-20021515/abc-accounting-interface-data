
COPY inventory_costing
    (company_code,costing_date,method,start_date,end_date,warehouse_id,material_id,grade,currency,unit_cost,is_active)
    FROM '/data/inventory/inventory_costing.csv'
    DELIMITER ','
    CSV HEADER;