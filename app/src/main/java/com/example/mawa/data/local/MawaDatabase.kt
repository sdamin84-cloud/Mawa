package com.example.mawa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mawa.data.local.dao.CustomerDao
import com.example.mawa.data.local.dao.FordiDao
import com.example.mawa.data.local.dao.PersonalTransactionDao
import com.example.mawa.data.local.dao.ProductDao
import com.example.mawa.data.local.dao.ShopSettingsDao
import com.example.mawa.data.local.dao.TransactionDao
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.PersonalTransactionEntity
import com.example.mawa.data.local.entity.PersonalTransactionType
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.ShopSettingsEntity
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

@Database(
    entities = [
        TransactionEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        FordiItemEntity::class,
        ShopSettingsEntity::class,
        PersonalTransactionEntity::class
    ],
    version = 4,
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
