package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ClanSnapshotEntity
import com.example.data.local.entity.PersonalNoteEntity
import com.example.data.local.entity.PlayerSnapshotEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.TrackedClanEntity
import com.example.data.local.entity.TrackedPlayerEntity
import com.example.data.local.entity.WarAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)
}

@Dao
interface TrackedPlayerDao {
    @Query("SELECT * FROM tracked_players ORDER BY trophies DESC")
    fun getAllTrackedPlayers(): Flow<List<TrackedPlayerEntity>>

    @Query("SELECT * FROM tracked_players WHERE tag = :tag LIMIT 1")
    suspend fun getPlayerByTag(tag: String): TrackedPlayerEntity?

    @Query("SELECT * FROM tracked_players WHERE tag = :tag LIMIT 1")
    fun observePlayerByTag(tag: String): Flow<TrackedPlayerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(player: TrackedPlayerEntity)

    @Query("DELETE FROM tracked_players WHERE tag = :tag")
    suspend fun deleteByTag(tag: String)

    @Query("SELECT COUNT(*) FROM tracked_players")
    fun getCount(): Flow<Int>
}

@Dao
interface TrackedClanDao {
    @Query("SELECT * FROM tracked_clans ORDER BY clanPoints DESC")
    fun getAllTrackedClans(): Flow<List<TrackedClanEntity>>

    @Query("SELECT * FROM tracked_clans WHERE tag = :tag LIMIT 1")
    suspend fun getClanByTag(tag: String): TrackedClanEntity?

    @Query("SELECT * FROM tracked_clans WHERE tag = :tag LIMIT 1")
    fun observeClanByTag(tag: String): Flow<TrackedClanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(clan: TrackedClanEntity)

    @Query("DELETE FROM tracked_clans WHERE tag = :tag")
    suspend fun deleteByTag(tag: String)

    @Query("SELECT COUNT(*) FROM tracked_clans")
    fun getCount(): Flow<Int>
}

@Dao
interface SnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerSnapshot(snapshot: PlayerSnapshotEntity)

    @Query("SELECT * FROM player_snapshots WHERE playerTag = :playerTag ORDER BY timestamp ASC")
    fun getPlayerSnapshots(playerTag: String): Flow<List<PlayerSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClanSnapshot(snapshot: ClanSnapshotEntity)

    @Query("SELECT * FROM clan_snapshots WHERE clanTag = :clanTag ORDER BY timestamp ASC")
    fun getClanSnapshots(clanTag: String): Flow<List<ClanSnapshotEntity>>

    @Query("SELECT * FROM player_snapshots ORDER BY timestamp DESC LIMIT 20")
    fun getRecentPlayerSnapshots(): Flow<List<PlayerSnapshotEntity>>
}

@Dao
interface WarAssignmentDao {
    @Query("SELECT * FROM war_assignments WHERE clanTag = :clanTag ORDER BY targetBaseNumber ASC")
    fun getAssignmentsForClan(clanTag: String): Flow<List<WarAssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: WarAssignmentEntity)

    @Update
    suspend fun updateAssignment(assignment: WarAssignmentEntity)

    @Query("DELETE FROM war_assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: Long)

    @Query("DELETE FROM war_assignments WHERE clanTag = :clanTag")
    suspend fun clearClanAssignments(clanTag: String)
}

@Dao
interface PersonalNoteDao {
    @Query("SELECT * FROM personal_notes WHERE targetType = :type AND targetTag = :tag ORDER BY updatedAt DESC")
    fun getNotes(type: String, tag: String): Flow<List<PersonalNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PersonalNoteEntity)

    @Query("DELETE FROM personal_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}
