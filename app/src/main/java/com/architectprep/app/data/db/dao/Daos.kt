package com.architectprep.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.architectprep.app.data.db.entity.DomainEntity
import com.architectprep.app.data.db.entity.FlashcardStateEntity
import com.architectprep.app.data.db.entity.GlossaryTermEntity
import com.architectprep.app.data.db.entity.LessonEntity
import com.architectprep.app.data.db.entity.LessonProgressEntity
import com.architectprep.app.data.db.entity.MockAttemptEntity
import com.architectprep.app.data.db.entity.QuestionAttemptEntity
import com.architectprep.app.data.db.entity.QuestionEntity
import com.architectprep.app.data.db.entity.StreakDayEntity
import com.architectprep.app.data.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(track: TrackEntity)

    @Query("SELECT * FROM tracks WHERE code = :code LIMIT 1")
    suspend fun get(code: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE code = :code LIMIT 1")
    fun observe(code: String): Flow<TrackEntity?>
}

@Dao
interface DomainDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(domains: List<DomainEntity>)

    @Query("SELECT * FROM domains WHERE trackCode = :trackCode ORDER BY orderIndex")
    fun observeByTrack(trackCode: String): Flow<List<DomainEntity>>

    @Query("SELECT * FROM domains WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<DomainEntity?>
}

@Dao
interface LessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(lessons: List<LessonEntity>)

    @Query("SELECT * FROM lessons WHERE domainId = :domainId ORDER BY orderIndex")
    fun observeByDomain(domainId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun get(id: String): LessonEntity?

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun count(): Int
}

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions WHERE domainId = :domainId")
    suspend fun getByDomain(domainId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun get(id: String): QuestionEntity?

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun randomSample(limit: Int): List<QuestionEntity>
}

@Dao
interface GlossaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(terms: List<GlossaryTermEntity>)

    @Query(
        "SELECT * FROM glossary_terms WHERE term LIKE '%' || :query || '%' " +
            "OR definition LIKE '%' || :query || '%' ORDER BY term"
    )
    fun search(query: String): Flow<List<GlossaryTermEntity>>
}

@Dao
interface LessonProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LessonProgressEntity)

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId LIMIT 1")
    suspend fun get(lessonId: String): LessonProgressEntity?

    @Query(
        "SELECT COUNT(*) FROM lesson_progress p JOIN lessons l ON l.id = p.lessonId " +
            "WHERE l.domainId = :domainId AND p.status = 'done'"
    )
    fun observeDoneCountForDomain(domainId: String): Flow<Int>

    @Query("SELECT lessonId FROM lesson_progress WHERE status = 'done'")
    fun observeDoneLessonIds(): Flow<List<String>>
}

@Dao
interface QuestionAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: QuestionAttemptEntity)

    @Query("SELECT COUNT(*) FROM question_attempts WHERE domainId = :domainId AND correct = 1")
    fun observeCorrectCountForDomain(domainId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM question_attempts WHERE domainId = :domainId")
    fun observeAttemptCountForDomain(domainId: String): Flow<Int>
}

@Dao
interface FlashcardStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: FlashcardStateEntity)

    @Query("SELECT * FROM flashcard_state WHERE cardId = :cardId LIMIT 1")
    suspend fun get(cardId: String): FlashcardStateEntity?

    @Query("SELECT * FROM flashcard_state WHERE dueAt <= :now ORDER BY dueAt LIMIT :limit")
    suspend fun dueCards(now: Long, limit: Int): List<FlashcardStateEntity>

    @Query("SELECT COUNT(*) FROM flashcard_state WHERE dueAt <= :now")
    suspend fun dueCount(now: Long): Int

    @Query("SELECT id FROM questions WHERE id NOT IN (SELECT cardId FROM flashcard_state) LIMIT :limit")
    suspend fun newCardIds(limit: Int): List<String>
}

@Dao
interface MockAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attempt: MockAttemptEntity)

    @Query("SELECT * FROM mock_attempts WHERE id = :id LIMIT 1")
    suspend fun get(id: String): MockAttemptEntity?

    @Query("SELECT * FROM mock_attempts WHERE id = :id LIMIT 1")
    fun observe(id: String): Flow<MockAttemptEntity?>

    @Query("SELECT * FROM mock_attempts WHERE trackCode = :trackCode AND status = 'in_progress' ORDER BY startedAt DESC LIMIT 1")
    suspend fun latestInProgress(trackCode: String): MockAttemptEntity?

    @Query("SELECT * FROM mock_attempts WHERE trackCode = :trackCode AND status = 'submitted' ORDER BY submittedAt DESC")
    fun observeSubmitted(trackCode: String): Flow<List<MockAttemptEntity>>
}

@Dao
interface StreakDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: StreakDayEntity)

    @Query("SELECT * FROM streak_days WHERE date = :date LIMIT 1")
    suspend fun get(date: String): StreakDayEntity?

    @Query("SELECT * FROM streak_days ORDER BY date DESC LIMIT :days")
    fun observeRecent(days: Int): Flow<List<StreakDayEntity>>
}
