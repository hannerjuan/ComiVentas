package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandDao {

    @Query("SELECT * FROM brands ORDER BY name ASC")
    fun getAllBrands(): Flow<List<BrandEntity>>

    @Query("SELECT * FROM brands WHERE isEnabled = 1 ORDER BY name ASC")
    fun getActiveBrands(): Flow<List<BrandEntity>>

    @Query("SELECT * FROM brands")
    suspend fun getAllBrandsSnapshot(): List<BrandEntity>

    @Query("SELECT * FROM brands WHERE name = :name LIMIT 1")
    suspend fun getBrandByName(name: String): BrandEntity?

    @Query("SELECT COUNT(*) FROM brands")
    suspend fun getBrandCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(brand: BrandEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrands(brands: List<BrandEntity>)

    @Update
    suspend fun updateBrand(brand: BrandEntity)

    @Delete
    suspend fun deleteBrand(brand: BrandEntity)

    @Query("DELETE FROM brands WHERE id = :id")
    suspend fun deleteBrandById(id: Long)

    @Query("DELETE FROM brands")
    suspend fun deleteAllBrands()
}
