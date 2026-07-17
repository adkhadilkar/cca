package com.architectprep.app.data.content

import android.content.Context
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.db.entity.DomainEntity
import com.architectprep.app.data.db.entity.GlossaryTermEntity
import com.architectprep.app.data.db.entity.LessonEntity
import com.architectprep.app.data.db.entity.QuestionEntity
import com.architectprep.app.data.db.entity.TrackEntity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Loads a bundled content pack from assets/content/<packId>/ into Room.
 *
 * Idempotent by design (see docs/DEVELOPMENT_DESIGN.md §5.2 "golden rule"):
 * content rows are safe to re-upsert on every launch or content update: only
 * user/progress tables must never be touched here.
 */
class ContentImporter(
    private val context: Context,
    private val db: AppDatabase
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun importIfNeeded(packId: String = "ccar-f") {
        val domainsFile = readAsset<DomainsFile>("$packId/domains.json")
        val existing = db.trackDao().get(domainsFile.track.code)
        if (existing != null && existing.contentVersion >= domainsFile.track.contentVersion) return
        import(packId, domainsFile)
    }

    private suspend fun import(packId: String, domainsFile: DomainsFile) {
        val lessonsFile = readAsset<LessonsFile>("$packId/lessons.json")
        val questionsFile = readAsset<QuestionsFile>("$packId/questions.json")
        val glossaryFile = readAsset<GlossaryFile>("$packId/glossary.json")

        db.trackDao().upsert(
            TrackEntity(
                code = domainsFile.track.code,
                title = domainsFile.track.title,
                questionCount = domainsFile.track.questionCount,
                timeLimitMin = domainsFile.track.timeLimitMin,
                passScore = domainsFile.track.passScore,
                scoreScale = domainsFile.track.scoreScale,
                contentVersion = domainsFile.track.contentVersion
            )
        )

        db.domainDao().upsertAll(
            domainsFile.domains.map { d ->
                DomainEntity(
                    id = d.id,
                    trackCode = domainsFile.track.code,
                    code = d.code,
                    title = d.title,
                    weightPct = d.weightPct,
                    orderIndex = d.orderIndex,
                    summary = d.summary
                )
            }
        )

        db.lessonDao().upsertAll(
            lessonsFile.lessons.map { l ->
                LessonEntity(
                    id = l.id,
                    domainId = l.domainId,
                    orderIndex = l.orderIndex,
                    title = l.title,
                    estMinutes = l.estMinutes,
                    bodyJson = json.encodeToString(l.body)
                )
            }
        )

        db.questionDao().upsertAll(
            questionsFile.questions.map { q ->
                QuestionEntity(
                    id = q.id,
                    domainId = q.domainId,
                    type = q.type,
                    difficulty = q.difficulty,
                    stem = q.stem,
                    choicesJson = json.encodeToString(q.choices),
                    correctJson = json.encodeToString(q.correct),
                    explanation = q.explanation,
                    sourceRef = q.sourceRef
                )
            }
        )

        db.glossaryDao().upsertAll(
            glossaryFile.terms.map { t ->
                GlossaryTermEntity(
                    id = t.id,
                    term = t.term,
                    category = t.category,
                    definition = t.definition
                )
            }
        )
    }

    private inline fun <reified T> readAsset(path: String): T {
        val text = context.assets.open("content/$path").bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }
}
