package com.eferraz.database.converters

import androidx.room.TypeConverter
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.entities.liquidity.Liquidity
import kotlinx.datetime.LocalDate

/**
 * Type converters para Room Database.
 * Converte tipos do domínio para tipos compatíveis com SQLite.
 */
internal class Converters {

    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }

//    // FixedIncomeAssetType converter
//    @TypeConverter
//    fun fromFixedIncomeAssetType(type: FixedIncomeAssetType?): String? {
//        return type?.name
//    }
//
//    @TypeConverter
//    fun toFixedIncomeAssetType(typeString: String?): FixedIncomeAssetType? {
//        return typeString?.let { FixedIncomeAssetType.valueOf(it) }
//    }
//
//    // FixedIncomeSubType converter
//    @TypeConverter
//    fun fromFixedIncomeSubType(type: FixedIncomeSubType?): String? {
//        return type?.name
//    }
//
//    @TypeConverter
//    fun toFixedIncomeSubType(typeString: String?): FixedIncomeSubType? {
//        return typeString?.let { FixedIncomeSubType.valueOf(it) }
//    }
//
//    // VariableIncomeAssetType converter
//    @TypeConverter
//    fun fromVariableIncomeAssetType(type: VariableIncomeAssetType?): String? {
//        return type?.name
//    }
//
//    @TypeConverter
//    fun toVariableIncomeAssetType(typeString: String?): VariableIncomeAssetType? {
//        return typeString?.let { VariableIncomeAssetType.valueOf(it) }
//    }
//
//    // InvestmentFundAssetType converter
//    @TypeConverter
//    fun fromInvestmentFundAssetType(type: InvestmentFundAssetType?): String? {
//        return type?.name
//    }
//
//    @TypeConverter
//    fun toInvestmentFundAssetType(typeString: String?): InvestmentFundAssetType? {
//        return typeString?.let { InvestmentFundAssetType.valueOf(it) }
//    }
//
//    // Liquidity converter
//    @TypeConverter
//    fun fromLiquidity(liquidity: Liquidity?): String? {
//        return liquidity?.name
//    }
//
//    @TypeConverter
//    fun toLiquidity(liquidityString: String?): Liquidity? {
//        return liquidityString?.let { Liquidity.valueOf(it) }
//    }
}

