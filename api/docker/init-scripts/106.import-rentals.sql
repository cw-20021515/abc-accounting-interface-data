------------------------------------------------------------------------------------------------------------------------
--- IMPORT RENTALS CONFIG
------------------------------------------------------------------------------------------------------------------------
COPY rental_code_master
    (rental_code,rental_code_name,rental_code_description,current_term,term1_period,term2_period,term3_period,term4_period,term5_period,contract_pricing_type,contract_duration,commitment_duration,lease_type,remark,is_active)
    FROM '/data/rentals/rental_code_master.csv'
    DELIMITER ','
    CSV HEADER;

COPY rental_distribution_master
    (id,material_model_name_prefix,rental_distribution_type,onetime_price,membership_price,membership_dcprice_c24,free_service_duration,start_date)
    FROM '/data/rentals/rental_distribution_master.csv'
    DELIMITER ','
    CSV HEADER;

COPY rental_pricing_master
    (id,material_model_name_prefix,rental_code,material_care_type,price,currency,tax_included,period_type,start_date)
    FROM '/data/rentals/rental_pricing_master.csv'
    DELIMITER ','
    CSV HEADER;


------------------------------------------------------------------------------------------------------------------------
--- IMPORT RENTALS DATA
------------------------------------------------------------------------------------------------------------------------
COPY rental_asset_depreciation_master
    (id,material_id,useful_life,salvage_value,currency,depreciation_method,start_date)
    FROM '/data/rentals/rental_asset_depreciation_master.csv'
    DELIMITER ','
    CSV HEADER;

COPY rental_distribution_rule
    (id,material_id,material_model_name_prefix,rental_code,material_care_type,lease_type,commitment_duration,adjusted_commitment_duration,dist_value_m01,dist_value_r01,dist_value_r02,dist_value_r03,dist_value_s01,dist_value_t01,dist_ratio_m01,dist_ratio_r01,dist_ratio_r02,dist_ratio_r03,dist_ratio_s01,dist_ratio_t01,dist_price_m01,dist_price_r01,dist_price_r02,dist_price_r03,dist_price_s01,dist_price_t01,start_date)
    FROM '/data/rentals/rental_distribution_rule.csv'
    DELIMITER ','
    CSV HEADER;

COPY rental_financial_interest_master
    (target_month,interest_rate,create_time)
    FROM '/data/rentals/rental_financial_interest_master.csv'
    DELIMITER ','
    CSV HEADER;