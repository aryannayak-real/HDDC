package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ActiveBuilderDao
import com.example.data.local.dao.PersonalNoteDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.SnapshotDao
import com.example.data.local.dao.TrackedClanDao
import com.example.data.local.dao.TrackedPlayerDao
import com.example.data.local.dao.WarAssignmentDao
import com.example.data.local.entity.ActiveBuilderEntity
import com.example.data.local.entity.ClanSnapshotEntity
import com.example.data.local.entity.PersonalNoteEntity
import com.example.data.local.entity.PlayerSnapshotEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.TrackedClanEntity
import com.example.data.local.entity.TrackedPlayerEntity
import com.example.data.local.entity.WarAssignmentEntity

@Database(
    entities = [
        SettingsEntity::class,
        TrackedPlayerEntity::class,
        TrackedClanEntity::class,
        PlayerSnapshotEntity::class,
        ClanSnapshotEntity::class,
        WarAssignmentEntity::class,
        PersonalNoteEntity::class,
        ActiveBuilderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun trackedPlayerDao(): TrackedPlayerDao
    abstract fun trackedClanDao(): TrackedClanDao
    abstract fun snapshotDao(): SnapshotDao
    abstract fun warAssignmentDao(): WarAssignmentDao
    abstract fun personalNoteDao(): PersonalNoteDao
    abstract fun activeBuilderDao(): ActiveBuilderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hddc_clash_intel.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
