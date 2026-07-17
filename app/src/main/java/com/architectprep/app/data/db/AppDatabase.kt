package com.architectprep.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.architectprep.app.data.db.dao.DomainDao
import com.architectprep.app.data.db.dao.FlashcardStateDao
import com.architectprep.app.data.db.dao.GlossaryDao
import com.architectprep.app.data.db.dao.LessonDao
import com.architectprep.app.data.db.dao.LessonProgressDao
import com.architectprep.app.data.db.dao.MockAttemptDao
import com.architectprep.app.data.db.dao.QuestionAttemptDao
import com.architectprep.app.data.db.dao.QuestionDao
import com.architectprep.app.data.db.dao.StreakDayDao
import com.architectprep.app.data.db.dao.TrackDao
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

@Database(
    entities = [
        TrackEntity::class,
        DomainEntity::class,
        LessonEntity::class,
        QuestionEntity::class,
        GlossaryTermEntity::class,
        LessonProgressEntity::class,
        QuestionAttemptEntity::class,
        FlashcardStateEntity::class,
        MockAttemptEntity::class,
        StreakDayEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun domainDao(): DomainDao
    abstract fun lessonDao(): LessonDao
    abstract fun questionDao(): QuestionDao
    abstract fun glossaryDao(): GlossaryDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun questionAttemptDao(): QuestionAttemptDao
    abstract fun flashcardStateDao(): FlashcardStateDao
    abstract fun mockAttemptDao(): MockAttemptDao
    abstract fun streakDayDao(): StreakDayDao

    companion object {
        const val DB_NAME = "architect_prep.db"
    }
}
