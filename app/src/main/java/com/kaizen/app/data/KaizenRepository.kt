package com.kaizen.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Database(
    entities = [
        Habit::class, HabitCompletion::class,
        WorkoutLog::class,
        PersonalRecord::class,
        BodyweightEntry::class,
        SleepEntry::class,
        InjuryLog::class,
        JournalEntry::class,
        Goal::class,
        Win::class,
        GarminEntry::class,
    ],
    version = 6,
    exportSchema = false,
)
@TypeConverters(KaizenConverters::class)
abstract class KaizenDatabase : RoomDatabase() {
    abstract fun dao(): KaizenDao
    companion object {
        @Volatile private var INSTANCE: KaizenDatabase? = null
        fun getInstance(ctx: Context): KaizenDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(ctx.applicationContext, KaizenDatabase::class.java, "kaizen.db")
                    .fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

class KaizenRepository(private val dao: KaizenDao) {

    val habits: Flow<List<HabitWithCompletions>> = dao.allHabitsWithCompletions()
    val recentWorkouts: Flow<List<WorkoutLog>>   = dao.recentWorkouts()
    val personalRecords: Flow<List<PersonalRecord>> = dao.allPersonalRecords()
    val bodyweightEntries: Flow<List<BodyweightEntry>> = dao.bodyweightEntries()
    val sleepEntries: Flow<List<SleepEntry>>     = dao.sleepEntries()
    val activeInjuries: Flow<List<InjuryLog>>    = dao.activeInjuries()
    val allInjuries: Flow<List<InjuryLog>>       = dao.allInjuries()
    val journalEntries: Flow<List<JournalEntry>> = dao.journalEntries()
    val goals: Flow<List<Goal>>                  = dao.goals()
    val wins: Flow<List<Win>>                    = dao.wins()

    suspend fun addHabit(name: String, category: HabitCategory, slot: TimeSlot) =
        dao.insertHabit(Habit(name = name, category = category, timeSlot = slot))
    suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = dao.deleteHabit(habit)
    suspend fun markHabitDone(habit: Habit, date: String) {
        dao.insertHabitCompletion(HabitCompletion(habit.id, date))
        dao.updateHabit(habit.copy(streak = habit.streak + 1))
    }
    suspend fun markHabitUndone(habit: Habit, date: String) {
        dao.removeHabitCompletion(habit.id, date)
        dao.updateHabit(habit.copy(streak = maxOf(0, habit.streak - 1)))
    }

    suspend fun allWorkouts(): List<WorkoutLog> = dao.allWorkouts()
    suspend fun logWorkout(date: String, type: WorkoutType, whoopRecovery: Int?) =
        dao.insertWorkoutLog(WorkoutLog(date = date, workoutType = type, whoopRecovery = whoopRecovery, strainScore = null, durationMinutes = null))
    suspend fun updateWorkoutLog(log: WorkoutLog) = dao.updateWorkoutLog(log)
    suspend fun updateCoreSets(log: WorkoutLog, coreSetsJson: String) =
        dao.updateWorkoutLog(log.copy(coreSetsJson = coreSetsJson))

    suspend fun logPR(exerciseName: String, workoutType: WorkoutType, difficulty: Difficulty, repsOrDuration: String, notes: String = "") =
        dao.insertPersonalRecord(PersonalRecord(exerciseName = exerciseName, workoutType = workoutType,
            difficulty = difficulty, repsOrDuration = repsOrDuration, notes = notes, date = LocalDate.now().toString()))
    suspend fun latestPR(name: String): PersonalRecord? = dao.latestPRForExercise(name)
    suspend fun deletePR(pr: PersonalRecord) = dao.deletePersonalRecord(pr)

    suspend fun logBodyweight(weightKg: Float, notes: String = "") =
        dao.insertBodyweight(BodyweightEntry(date = LocalDate.now().toString(), weightKg = weightKg, notes = notes))
    suspend fun latestBodyweight(): BodyweightEntry? = dao.latestBodyweight()
    suspend fun deleteBodyweight(entry: BodyweightEntry) = dao.deleteBodyweight(entry)

    suspend fun logSleep(hoursSlept: Float, hrv: Int? = null, quality: Int = 3, notes: String = "") {
        val date = LocalDate.now().toString()
        val existing = dao.sleepForDate(date)
        if (existing != null) dao.updateSleepEntry(existing.copy(hoursSlept = hoursSlept, hrv = hrv, quality = quality, notes = notes))
        else dao.insertSleepEntry(SleepEntry(date = date, hoursSlept = hoursSlept, hrv = hrv, quality = quality, notes = notes))
    }

    suspend fun logInjury(bodyPart: BodyPart, side: InjurySide, type: InjuryType, severity: Int, notes: String, date: String = LocalDate.now().toString()) =
        dao.insertInjury(InjuryLog(date = date, bodyPart = bodyPart, side = side, type = type, severity = severity, notes = notes))
    suspend fun resolveInjury(injury: InjuryLog) =
        dao.updateInjury(injury.copy(resolved = true, resolvedDate = LocalDate.now().toString()))
    suspend fun updateInjury(injury: InjuryLog) = dao.updateInjury(injury)
    suspend fun deleteInjury(injury: InjuryLog) = dao.deleteInjury(injury)

    suspend fun addJournal(text: String, mood: Int, tags: String) {
        val entry = JournalEntry(date = LocalDate.now().toString(), text = text, mood = mood, tags = tags)
        dao.insertJournal(entry)
        runCatching { SupabaseSync.upsertJournal(entry) }
    }
    suspend fun updateJournal(entry: JournalEntry) {
        dao.updateJournal(entry)
        runCatching { SupabaseSync.upsertJournal(entry) }
    }
    suspend fun deleteJournal(entry: JournalEntry) {
        dao.deleteJournal(entry)
        runCatching { SupabaseSync.deleteJournal(entry.remoteId) }
    }

    suspend fun addGoal(title: String, description: String, targetDate: String) {
        val goal = Goal(title = title, description = description, targetDate = targetDate)
        dao.insertGoal(goal)
        runCatching { SupabaseSync.upsertGoal(goal) }
    }
    suspend fun updateGoal(goal: Goal) {
        dao.updateGoal(goal)
        runCatching { SupabaseSync.upsertGoal(goal) }
    }
    suspend fun completeGoal(goal: Goal) {
        val updated = goal.copy(status = GoalStatus.COMPLETED, updatedAt = System.currentTimeMillis())
        dao.updateGoal(updated)
        runCatching { SupabaseSync.upsertGoal(updated) }
    }
    suspend fun deleteGoal(goal: Goal) {
        dao.deleteGoal(goal)
        runCatching { SupabaseSync.deleteGoal(goal.remoteId) }
    }

    suspend fun addWin(title: String, description: String, type: WinType = WinType.WIN) {
        val win = Win(title = title, description = description, date = LocalDate.now().toString(), type = type)
        dao.insertWin(win)
        runCatching { SupabaseSync.upsertWin(win) }
    }
    suspend fun deleteWin(win: Win) {
        dao.deleteWin(win)
        runCatching { SupabaseSync.deleteWin(win.remoteId) }
    }

    val garminEntryToday: Flow<GarminEntry?> = dao.garminForDate(LocalDate.now().toString())

    suspend fun saveGarminEntry(entry: GarminEntry) = dao.upsertGarminEntry(entry)
    suspend fun garminEntryOnce(date: String): GarminEntry? = dao.garminForDateOnce(date)

    suspend fun syncToCloud(journals: List<JournalEntry>, goals: List<Goal>, wins: List<Win>): Boolean =
        runCatching {
            journals.forEach { SupabaseSync.upsertJournal(it) }
            goals.forEach   { SupabaseSync.upsertGoal(it)    }
            wins.forEach    { SupabaseSync.upsertWin(it)     }
        }.isSuccess

    suspend fun restoreFromSupabase() {
        runCatching {
            if (dao.journalCount() + dao.goalCount() + dao.winCount() > 0) return
            SupabaseSync.fetchJournalEntries().forEach { j ->
                dao.insertJournal(JournalEntry(
                    remoteId  = j.getString("remote_id"),
                    date      = j.getString("date"),
                    text      = j.getString("text"),
                    mood      = j.optInt("mood", 3),
                    tags      = j.optString("tags", ""),
                    updatedAt = j.optLong("updated_at", System.currentTimeMillis()),
                ))
            }
            SupabaseSync.fetchGoals().forEach { g ->
                dao.insertGoal(Goal(
                    remoteId    = g.getString("remote_id"),
                    title       = g.getString("title"),
                    description = g.optString("description", ""),
                    targetDate  = g.optString("target_date", ""),
                    status      = runCatching { GoalStatus.valueOf(g.getString("status")) }.getOrElse { GoalStatus.ACTIVE },
                    updatedAt   = g.optLong("updated_at", System.currentTimeMillis()),
                ))
            }
            SupabaseSync.fetchWins().forEach { w ->
                dao.insertWin(Win(
                    remoteId    = w.getString("remote_id"),
                    title       = w.getString("title"),
                    description = w.optString("description", ""),
                    date        = w.getString("date"),
                    type        = runCatching { WinType.valueOf(w.optString("type", "WIN")) }.getOrElse { WinType.WIN },
                    updatedAt   = w.optLong("updated_at", System.currentTimeMillis()),
                ))
            }
        }
    }
}
