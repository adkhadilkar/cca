package com.architectprep.app.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.content.ChoiceDto
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.domain.MockExamScoring
import com.architectprep.app.domain.PerDomainScore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class ExamQuestion(
    val id: String,
    val domainId: String,
    val domainCode: String,
    val stem: String,
    val choices: List<ChoiceDto>
)

data class ExamSessionUiState(
    val index: Int,
    val total: Int,
    val question: ExamQuestion?,
    val selectedChoiceId: String?,
    val flagged: Boolean,
    val answeredCount: Int,
    val flaggedCount: Int,
    val remainingMs: Long,
    val submitted: Boolean
)

class ExamSessionViewModel(
    private val db: AppDatabase,
    private val attemptId: String
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }
    private var questions: List<ExamQuestion> = emptyList()
    private var answers: MutableMap<String, String> = mutableMapOf()
    private var flagged: MutableSet<String> = mutableSetOf()
    private var index = 0
    private var startedAt = 0L
    private var timeLimitMs = 0L
    private var trackCode = "CCAR-F"
    private var scoreScale = 1000
    private var passScore = 720

    private val _uiState = MutableStateFlow<ExamSessionUiState?>(null)
    val uiState: StateFlow<ExamSessionUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val attempt = db.mockAttemptDao().get(attemptId) ?: return@launch
            trackCode = attempt.trackCode
            val track = db.trackDao().get(trackCode) ?: return@launch
            scoreScale = track.scoreScale
            passScore = track.passScore
            startedAt = attempt.startedAt
            timeLimitMs = track.timeLimitMin * 60_000L

            val domains = db.domainDao().observeByTrack(trackCode)
            val domainList = domains.first()
            val allQuestions = domainList.flatMap { d -> db.questionDao().getByDomain(d.id).map { d to it } }
            val target = minOf(track.questionCount, allQuestions.size)
            val chosen = allQuestions.shuffled().take(target)
            questions = chosen.map { (d, q) ->
                ExamQuestion(
                    id = q.id,
                    domainId = q.domainId,
                    domainCode = d.code,
                    stem = q.stem,
                    choices = json.decodeFromString(q.choicesJson)
                )
            }

            answers = json.decodeFromString<Map<String, String>>(attempt.answersJson).toMutableMap()
            flagged = json.decodeFromString<List<String>>(attempt.flaggedJson).toMutableSet()

            if (attempt.status == "submitted") {
                emit(submitted = true)
                return@launch
            }
            tickLoop()
        }
    }

    private suspend fun tickLoop() {
        while (true) {
            val remaining = timeLimitMs - (System.currentTimeMillis() - startedAt)
            if (remaining <= 0) {
                submit()
                return
            }
            emit(submitted = false, remainingMsOverride = remaining)
            delay(1000)
        }
    }

    private fun emit(submitted: Boolean, remainingMsOverride: Long? = null) {
        val q = questions.getOrNull(index)
        val remaining = remainingMsOverride ?: (timeLimitMs - (System.currentTimeMillis() - startedAt)).coerceAtLeast(0)
        _uiState.value = ExamSessionUiState(
            index = index,
            total = questions.size,
            question = q,
            selectedChoiceId = q?.let { answers[it.id] },
            flagged = q?.let { it.id in flagged } ?: false,
            answeredCount = answers.size,
            flaggedCount = flagged.size,
            remainingMs = remaining,
            submitted = submitted
        )
    }

    fun selectChoice(choiceId: String) {
        val q = questions.getOrNull(index) ?: return
        answers[q.id] = choiceId
        persist()
        emit(submitted = false)
    }

    fun toggleFlag() {
        val q = questions.getOrNull(index) ?: return
        if (q.id in flagged) flagged.remove(q.id) else flagged.add(q.id)
        persist()
        emit(submitted = false)
    }

    fun goTo(newIndex: Int) {
        if (newIndex !in questions.indices) return
        index = newIndex
        emit(submitted = false)
    }

    private fun persist() {
        viewModelScope.launch {
            db.mockAttemptDao().upsert(
                db.mockAttemptDao().get(attemptId)!!.copy(
                    answersJson = json.encodeToString(answers.toMap()),
                    flaggedJson = json.encodeToString(flagged.toList())
                )
            )
        }
    }

    fun submit() {
        viewModelScope.launch {
            val domains = db.domainDao().observeByTrack(trackCode)
            val domainList = domains.first()
            val correctByQuestion = questions.associate { q ->
                q.id to (db.questionDao().get(q.id)?.correctJson?.let { json.decodeFromString<List<String>>(it) } ?: emptyList())
            }
            var totalCorrect = 0
            val perDomain = domainList.map { d ->
                val domainQs = questions.filter { it.domainId == d.id }
                val correct = domainQs.count { q -> answers[q.id] in (correctByQuestion[q.id] ?: emptyList()) }
                totalCorrect += correct
                PerDomainScore(d.code, d.title, correct, domainQs.size)
            }
            val score = MockExamScoring.score(totalCorrect, questions.size, scoreScale)

            db.mockAttemptDao().upsert(
                db.mockAttemptDao().get(attemptId)!!.copy(
                    submittedAt = System.currentTimeMillis(),
                    score = score,
                    perDomainJson = json.encodeToString(perDomain),
                    answersJson = json.encodeToString(answers.toMap()),
                    flaggedJson = json.encodeToString(flagged.toList()),
                    status = "submitted"
                )
            )
            emit(submitted = true)
        }
    }

    class Factory(private val app: PrepApplication, private val attemptId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExamSessionViewModel(app.database, attemptId) as T
        }
    }
}
