package com.architectprep.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.architectprep.app.data.db.entity.DomainEntity
import com.architectprep.app.data.db.entity.GlossaryTermEntity
import com.architectprep.app.data.db.entity.LessonEntity
import com.architectprep.app.data.db.entity.LessonProgressEntity
import com.architectprep.app.data.db.entity.QuestionEntity
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
}
