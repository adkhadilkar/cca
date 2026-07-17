package com.architectprep.app.domain

import kotlin.math.max

enum class Grade { AGAIN, HARD, GOOD, EASY }

data class SchedulerState(
    val ease: Double,
    val intervalDays: Double,
    val reps: Int,
    val lapses: Int
) {
    companion object {
        val NEW = SchedulerState(ease = 2.5, intervalDays = 0.0, reps = 0, lapses = 0)
    }
}

/**
 * Lightweight SM-2 variant per docs/DEVELOPMENT_DESIGN.md §9.1. First-step
 * intervals on a new or just-reset card are fixed to match the design's
 * displayed values (Again 1 min, Hard 2 d, Good 4 d, Easy 8 d); once a card
 * has graduated (reps > 0), intervals grow multiplicatively off `ease`.
 */
object SpacedRepetitionScheduler {
    private const val MIN_EASE = 1.3

    fun grade(state: SchedulerState, grade: Grade): SchedulerState {
        if (state.reps == 0) {
            return when (grade) {
                Grade.AGAIN -> state.copy(intervalDays = 1.0 / 1440.0, lapses = state.lapses + 1, ease = max(MIN_EASE, state.ease - 0.2))
                Grade.HARD -> state.copy(intervalDays = 2.0, reps = 1, ease = max(MIN_EASE, state.ease - 0.15))
                Grade.GOOD -> state.copy(intervalDays = 4.0, reps = 1)
                Grade.EASY -> state.copy(intervalDays = 8.0, reps = 1, ease = state.ease + 0.15)
            }
        }
        return when (grade) {
            Grade.AGAIN -> state.copy(
                reps = 0,
                intervalDays = 1.0 / 1440.0,
                lapses = state.lapses + 1,
                ease = max(MIN_EASE, state.ease - 0.2)
            )
            Grade.HARD -> state.copy(
                intervalDays = state.intervalDays * 1.2,
                reps = state.reps + 1,
                ease = max(MIN_EASE, state.ease - 0.15)
            )
            Grade.GOOD -> state.copy(
                intervalDays = state.intervalDays * state.ease,
                reps = state.reps + 1
            )
            Grade.EASY -> state.copy(
                intervalDays = state.intervalDays * state.ease * 1.3,
                reps = state.reps + 1,
                ease = state.ease + 0.15
            )
        }
    }

    fun dueAt(nowMillis: Long, intervalDays: Double): Long =
        nowMillis + (intervalDays * 24.0 * 60.0 * 60.0 * 1000.0).toLong()
}
