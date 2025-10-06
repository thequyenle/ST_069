package net.android.st069_fakecallphoneprank.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.android.st069_fakecallphoneprank.data.dao.FakeCallDao
import net.android.st069_fakecallphoneprank.data.entity.FakeCall

@Database(
    entities = [FakeCall::class],
    version = 1,
    exportSchema = false
)
abstract class FakeCallDatabase : RoomDatabase() {

    abstract fun fakeCallDao(): FakeCallDao

    companion object {
        @Volatile
        private var INSTANCE: FakeCallDatabase? = null

        fun getDatabase(context: Context): FakeCallDatabase {
            // Return existing instance or create new one
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FakeCallDatabase::class.java,
                    "fake_call_database"
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}