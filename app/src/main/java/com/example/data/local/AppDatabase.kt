package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SaleEntity::class, CommissionRuleEntity::class, AdvisorProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun salesDao(): SalesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "comiventas_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.salesDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: SalesDao) {
                dao.setAdvisorProfile(
                    AdvisorProfileEntity(
                        id = 1,
                        advisorName = "Carlos Rodríguez",
                        campaignName = "Ventas Celulares Postpago",
                        monthlyTargetAmount = 3000000.0,
                        monthlyTargetUnits = 25
                    )
                )

                val defaultRules = listOf(
                    CommissionRuleEntity(brand = "Samsung", fixedCommission = 35000.0, percentageCommission = 2.5),
                    CommissionRuleEntity(brand = "Xiaomi", fixedCommission = 28000.0, percentageCommission = 2.0),
                    CommissionRuleEntity(brand = "Apple", fixedCommission = 55000.0, percentageCommission = 3.0),
                    CommissionRuleEntity(brand = "Motorola", fixedCommission = 25000.0, percentageCommission = 2.0),
                    CommissionRuleEntity(brand = "Honor", fixedCommission = 30000.0, percentageCommission = 2.5),
                    CommissionRuleEntity(brand = "Oppo", fixedCommission = 27000.0, percentageCommission = 2.0),
                    CommissionRuleEntity(brand = "Infinix / Tecno", fixedCommission = 22000.0, percentageCommission = 1.5)
                )
                dao.insertAllRules(defaultRules)

                // Initial sample sales for current month
                val currentMonth = "2026-10"
                val initialSales = listOf(
                    SaleEntity(
                        brand = "Samsung",
                        model = "Galaxy S24 FE 256GB",
                        price = 2899000.0,
                        commission = 107475.0,
                        saleDate = "2026-10-04",
                        monthKey = currentMonth,
                        customerName = "María Fernanda Gómez",
                        imeiOrContract = "864201061234567",
                        status = "APROBADA",
                        notes = "Portabilidad Plan Black"
                    ),
                    SaleEntity(
                        brand = "Xiaomi",
                        model = "Redmi Note 13 Pro+ 5G",
                        price = 1650000.0,
                        commission = 61000.0,
                        saleDate = "2026-10-03",
                        monthKey = currentMonth,
                        customerName = "Juan Pablo Vargas",
                        imeiOrContract = "865512069876543",
                        status = "APROBADA",
                        notes = "Línea nueva adicional"
                    ),
                    SaleEntity(
                        brand = "Apple",
                        model = "iPhone 15 128GB",
                        price = 3799000.0,
                        commission = 168970.0,
                        saleDate = "2026-10-02",
                        monthKey = currentMonth,
                        customerName = "Diana Patricia Silva",
                        imeiOrContract = "359123087654321",
                        status = "APROBADA",
                        notes = "Renovación anticipada"
                    ),
                    SaleEntity(
                        brand = "Motorola",
                        model = "Moto Edge 50 Pro 512GB",
                        price = 1999000.0,
                        commission = 64980.0,
                        saleDate = "2026-10-01",
                        monthKey = currentMonth,
                        customerName = "Andrés Felipe Ruiz",
                        imeiOrContract = "861992051122334",
                        status = "EN_VALIDACION",
                        notes = "Pendiente firma digital"
                    ),
                    SaleEntity(
                        brand = "Honor",
                        model = "Honor Magic 6 Lite",
                        price = 1299000.0,
                        commission = 62475.0,
                        saleDate = "2026-10-01",
                        monthKey = currentMonth,
                        customerName = "Laura Sofía Mendoza",
                        imeiOrContract = "867701049988776",
                        status = "APROBADA",
                        notes = "Migración Prepago a Postpago"
                    )
                )
                for (s in initialSales) {
                    dao.insertSale(s)
                }

                // Sample sales for previous month
                val prevMonth = "2026-09"
                val prevSales = listOf(
                    SaleEntity(
                        brand = "Samsung",
                        model = "Galaxy A55 5G",
                        price = 1450000.0,
                        commission = 71250.0,
                        saleDate = "2026-09-28",
                        monthKey = prevMonth,
                        customerName = "Camilo Torres",
                        imeiOrContract = "864201061111222",
                        status = "APROBADA"
                    ),
                    SaleEntity(
                        brand = "Apple",
                        model = "iPhone 13 128GB",
                        price = 2499000.0,
                        commission = 129970.0,
                        saleDate = "2026-09-25",
                        monthKey = prevMonth,
                        customerName = "Esteban Morales",
                        imeiOrContract = "359123087333444",
                        status = "APROBADA"
                    ),
                    SaleEntity(
                        brand = "Xiaomi",
                        model = "Redmi 13C 256GB",
                        price = 599000.0,
                        commission = 39980.0,
                        saleDate = "2026-09-20",
                        monthKey = prevMonth,
                        customerName = "Gloria Ortiz",
                        imeiOrContract = "865512069555666",
                        status = "APROBADA"
                    )
                )
                for (s in prevSales) {
                    dao.insertSale(s)
                }
            }
        }
    }
}
