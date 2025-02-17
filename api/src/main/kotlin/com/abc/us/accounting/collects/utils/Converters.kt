package com.abc.us.accounting.collects.utils

import com.abc.us.accounting.collects.domain.type.*
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class MaterialTypeConverter : AttributeConverter<MaterialType, String> {
    override fun convertToDatabaseColumn(attribute: MaterialType?): String? {
        return attribute?.symbol
    }

    override fun convertToEntityAttribute(dbData: String?): MaterialType? {
        return dbData?.let { symbol ->
            MaterialType.values().find { it.symbol == symbol }
        }
    }
}

@Converter
class MaterialCategoryCodeConverter : AttributeConverter<MaterialCategoryCode, String> {
    override fun convertToDatabaseColumn(attribute: MaterialCategoryCode?): String? {
        return attribute?.symbol
    }

    override fun convertToEntityAttribute(dbData: String?): MaterialCategoryCode? {
        return dbData?.let { symbol ->
            MaterialCategoryCode.values().find { it.symbol == symbol }
        }
    }
}

@Converter
class InstallationTypeConverter : AttributeConverter<InstallationType, String> {
    override fun convertToDatabaseColumn(attribute: InstallationType?): String? {
        return attribute?.symbol
    }

    override fun convertToEntityAttribute(dbData: String?): InstallationType? {
        return dbData?.let { symbol ->
            InstallationType.values().find { it.symbol == symbol }
        }
    }
}

@Converter
class MaterialProductTypeConverter : AttributeConverter<ProductType, String> {
    override fun convertToDatabaseColumn(attribute: ProductType?): String? {
        return attribute?.symbol
    }

    override fun convertToEntityAttribute(dbData: String?): ProductType? {
        return dbData?.let { symbol ->
            ProductType.values().find { it.symbol == symbol }
        }
    }
}



@Converter
class FilterTypeConverter : AttributeConverter<FilterType, String> {
    override fun convertToDatabaseColumn(attribute: FilterType?): String? {
        return attribute?.symbol
    }

    override fun convertToEntityAttribute(dbData: String?): FilterType? {
        return dbData?.let { symbol ->
            FilterType.values().find { it.symbol == symbol }
        }
    }
}



@Converter
class FeatureCodeConverter : AttributeConverter<FeatureCode, String> {
    override fun convertToDatabaseColumn(attribute: FeatureCode?): String? {
        return attribute?.symbol
    }

    override fun convertToEntityAttribute(dbData: String?): FeatureCode? {
        return dbData?.let { symbol ->
            FeatureCode.values().find { it.symbol == symbol }
        }
    }
}
