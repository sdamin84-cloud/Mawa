package com.example.mawa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mawa.data.local.dao.CashTallyDao
import com.example.mawa.data.local.dao.CompanyOrderDao
import com.example.mawa.data.local.dao.CustomerDao
import com.example.mawa.data.local.dao.DailyCashDao
import com.example.mawa.data.local.dao.FordiDao
import com.example.mawa.data.local.dao.PersonalTransactionDao
import com.example.mawa.data.local.dao.ProductDao
import com.example.mawa.data.local.dao.ShopSettingsDao
import com.example.mawa.data.local.dao.SupplierCompanyDao
import com.example.mawa.data.local.dao.TransactionDao
import com.example.mawa.data.local.entity.CashTallyEntity
import com.example.mawa.data.local.entity.CompanyOrderEntity
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.data.local.entity.DailyCashEntity
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.PersonalTransactionEntity
import com.example.mawa.data.local.entity.PersonalTransactionType
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return try {
            TransactionType.valueOf(value)
        } catch (e: Exception) {
            TransactionType.EXPENSE_SHOP
        }
    }

    @TypeConverter
    fun fromPersonalTransactionType(value: PersonalTransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toPersonalTransactionType(value: String): PersonalTransactionType {
        return try {
            PersonalTransactionType.valueOf(value)
        } catch (e: Exception) {
            PersonalTransactionType.EXPENSE
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create personal_transactions table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `personal_transactions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `type` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `title` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `note` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL
            )
        """.trimIndent())

        // 2. Add appMode and isModeConfigured columns to shop_settings safely
        try {
            db.execSQL("ALTER TABLE `shop_settings` ADD COLUMN `appMode` TEXT NOT NULL DEFAULT 'BOTH'")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `shop_settings` ADD COLUMN `isModeConfigured` INTEGER NOT NULL DEFAULT 0")
        } catch (_: Exception) {}
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `creditLimit` REAL NOT NULL DEFAULT 0.0")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `promisedPaymentDate` INTEGER NOT NULL DEFAULT 0")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `categoryTag` TEXT NOT NULL DEFAULT 'REGULAR'")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `customers` ADD COLUMN `nidOrGuarantor` TEXT NOT NULL DEFAULT ''")
        } catch (_: Exception) {}
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE `products` ADD COLUMN `barcode` TEXT NOT NULL DEFAULT ''")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `products` ADD COLUMN `stockQuantity` REAL NOT NULL DEFAULT 0.0")
        } catch (_: Exception) {}
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `daily_cash_records` (
                `dateKey` TEXT PRIMARY KEY NOT NULL,
                `dateMillis` INTEGER NOT NULL,
                `sabekCash` REAL NOT NULL DEFAULT 0.0,
                `closingCash` REAL NOT NULL DEFAULT 0.0,
                `isClosed` INTEGER NOT NULL DEFAULT 0,
                `updatedAt` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create supplier_companies table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `supplier_companies` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `companyName` TEXT NOT NULL,
                `srName` TEXT NOT NULL DEFAULT '',
                `srPhone` TEXT NOT NULL DEFAULT '',
                `orderDay` TEXT NOT NULL DEFAULT '',
                `deliveryDay` TEXT NOT NULL DEFAULT '',
                `address` TEXT NOT NULL DEFAULT '',
                `note` TEXT NOT NULL DEFAULT '',
                `createdAt` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // 2. Add paymentStatus & promisedPaymentDate to transactions
        try {
            db.execSQL("ALTER TABLE `transactions` ADD COLUMN `paymentStatus` TEXT NOT NULL DEFAULT 'PAID'")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `transactions` ADD COLUMN `promisedPaymentDate` INTEGER NOT NULL DEFAULT 0")
        } catch (_: Exception) {}

        // 3. Add companyName, orderDay, deliveryDay to fordi_items
        try {
            db.execSQL("ALTER TABLE `fordi_items` ADD COLUMN `companyName` TEXT NOT NULL DEFAULT ''")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `fordi_items` ADD COLUMN `orderDay` TEXT NOT NULL DEFAULT ''")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `fordi_items` ADD COLUMN `deliveryDay` TEXT NOT NULL DEFAULT ''")
        } catch (_: Exception) {}
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `cash_tally_records` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `dateKey` TEXT NOT NULL,
                `dateMillis` INTEGER NOT NULL,
                `count1000` INTEGER NOT NULL DEFAULT 0,
                `count500` INTEGER NOT NULL DEFAULT 0,
                `count200` INTEGER NOT NULL DEFAULT 0,
                `count100` INTEGER NOT NULL DEFAULT 0,
                `count50` INTEGER NOT NULL DEFAULT 0,
                `count20` INTEGER NOT NULL DEFAULT 0,
                `count10` INTEGER NOT NULL DEFAULT 0,
                `count5` INTEGER NOT NULL DEFAULT 0,
                `count2` INTEGER NOT NULL DEFAULT 0,
                `count1` INTEGER NOT NULL DEFAULT 0,
                `totalAmount` REAL NOT NULL DEFAULT 0.0,
                `systemBalance` REAL NOT NULL DEFAULT 0.0,
                `difference` REAL NOT NULL DEFAULT 0.0,
                `note` TEXT NOT NULL DEFAULT '',
                `timestamp` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE `shop_settings` ADD COLUMN `bkashNumber` TEXT NOT NULL DEFAULT ''")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `shop_settings` ADD COLUMN `nagadNumber` TEXT NOT NULL DEFAULT ''")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `shop_settings` ADD COLUMN `bankDetails` TEXT NOT NULL DEFAULT ''")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE `shop_settings` ADD COLUMN `qrCodeData` TEXT NOT NULL DEFAULT ''")
        } catch (_: Exception) {}
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `company_orders` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `companyName` TEXT NOT NULL,
                `totalAmount` REAL NOT NULL,
                `orderDate` INTEGER NOT NULL,
                `orderDateString` TEXT NOT NULL,
                `note` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `confirmedAt` INTEGER,
                `createdAt` INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

@Database(
    entities = [
        TransactionEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        FordiItemEntity::class,
        ShopSettingsEntity::class,
        PersonalTransactionEntity::class,
        DailyCashEntity::class,
        SupplierCompanyEntity::class,
        CashTallyEntity::class,
        CompanyOrderEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MawaDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun fordiDao(): FordiDao
    abstract fun shopSettingsDao(): ShopSettingsDao
    abstract fun personalTransactionDao(): PersonalTransactionDao
    abstract fun dailyCashDao(): DailyCashDao
    abstract fun supplierCompanyDao(): SupplierCompanyDao
    abstract fun cashTallyDao(): CashTallyDao
    abstract fun companyOrderDao(): CompanyOrderDao

    companion object {
        @Volatile
        private var INSTANCE: MawaDatabase? = null

        fun getDatabase(context: Context): MawaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MawaDatabase::class.java,
                    "mawa_khata_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
