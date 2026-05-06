package com.kaizen.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KaizenDao {

    // ── Habits ────────────────────────────────────────────────────────────
    @Transaction
    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun allHabitsWithCompletions(): Flow<List<HabitWithCompletions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update  suspend fun updateHabit(habit: Habit)
    @Delete  suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habits WHERE remoteId = :id LIMIT 1")
    suspend fun habitByRemoteId(id: String): Habit?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabitCompletion(c: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun removeHabitCompletion(habitId: Long, date: String)

    // ── Workout logs ──────────────────────────────────────────────────────
    @Query("SELECT * FROM workout_logs ORDER BY completedAt DESC LIMIT 90")
    fun recentWorkouts(): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs ORDER BY completedAt DESC")
    suspend fun allWorkouts(): List<WorkoutLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLog): Long

    @Update  suspend fun updateWorkoutLog(log: WorkoutLog)

    // ── Personal records ──────────────────────────────────────────────────
    @Query("SELECT * FROM personal_records ORDER BY setAt DESC")
    fun allPersonalRecords(): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE exerciseName = :name ORDER BY setAt DESC LIMIT 1")
    suspend fun latestPRForExercise(name: String): PersonalRecord?

    @Query("SELECT * FROM personal_records WHERE workoutType = :type ORDER BY setAt DESC")
    suspend fun prsForWorkoutType(type: WorkoutType): List<PersonalRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalRecord(pr: PersonalRecord): Long

    @Delete  suspend fun deletePersonalRecord(pr: PersonalRecord)

    // ── Bodyweight ────────────────────────────────────────────────────────
    @Query("SELECT * FROM bodyweight_entries ORDER BY date DESC LIMIT 90")
    fun bodyweightEntries(): Flow<List<BodyweightEntry>>

    @Query("SELECT * FROM bodyweight_entries ORDER BY date DESC LIMIT 1")
    suspend fun latestBodyweight(): BodyweightEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyweight(entry: BodyweightEntry): Long

    @Delete  suspend fun deleteBodyweight(entry: BodyweightEntry)

    // ── Sleep + HRV ───────────────────────────────────────────────────────
    @Query("SELECT * FROM sleep_entries ORDER BY date DESC LIMIT 30")
    fun sleepEntries(): Flow<List<SleepEntry>>

    @Query("SELECT * FROM sleep_entries WHERE date = :date LIMIT 1")
    suspend fun sleepForDate(date: String): SleepEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepEntry(entry: SleepEntry): Long

    @Update  suspend fun updateSleepEntry(entry: SleepEntry)

    // ── Injuries ──────────────────────────────────────────────────────────
    @Query("SELECT * FROM injury_logs WHERE resolved = 0 ORDER BY loggedAt DESC")
    fun activeInjuries(): Flow<List<InjuryLog>>

    @Query("SELECT * FROM injury_logs ORDER BY loggedAt DESC")
    fun allInjuries(): Flow<List<InjuryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInjury(log: InjuryLog): Long

    @Update  suspend fun updateInjury(log: InjuryLog)
    @Delete  suspend fun deleteInjury(log: InjuryLog)

    // ── Journal ───────────────────────────────────────────────────────────
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun journalEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(entry: JournalEntry): Long

    @Update  suspend fun updateJournal(entry: JournalEntry)
    @Delete  suspend fun deleteJournal(entry: JournalEntry)

    // ── Goals ─────────────────────────────────────────────────────────────
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun goals(): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update  suspend fun updateGoal(goal: Goal)
    @Delete  suspend fun deleteGoal(goal: Goal)

    // ── Wins ──────────────────────────────────────────────────────────────
    @Query("SELECT * FROM wins ORDER BY date DESC")
    fun wins(): Flow<List<Win>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWin(win: Win): Long

    @Delete  suspend fun deleteWin(win: Win)
    @Update  suspend fun updateWin(win: Win)

    @Query("SELECT * FROM journal_entries WHERE remoteId = :id LIMIT 1")
    suspend fun journalByRemoteId(id: String): JournalEntry?

    @Query("SELECT * FROM goals WHERE remoteId = :id LIMIT 1")
    suspend fun goalByRemoteId(id: String): Goal?

    @Query("SELECT * FROM wins WHERE remoteId = :id LIMIT 1")
    suspend fun winByRemoteId(id: String): Win?

    // ── Garmin ────────────────────────────────────────────────────────────
    @Query("SELECT * FROM garmin_entries WHERE date = :date LIMIT 1")
    fun garminForDate(date: String): Flow<GarminEntry?>

    @Query("SELECT * FROM garmin_entries WHERE date = :date LIMIT 1")
    suspend fun garminForDateOnce(date: String): GarminEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGarminEntry(entry: GarminEntry)

    // ── Cloud restore counts ───────────────────────────────────────────────
    @Query("SELECT COUNT(*) FROM journal_entries")
    suspend fun journalCount(): Int

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun goalCount(): Int

    @Query("SELECT COUNT(*) FROM wins")
    suspend fun winCount(): Int
}
