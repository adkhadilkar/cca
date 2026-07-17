package com.architectprep.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Material3's DatePickerState.selectedDateMillis is UTC-midnight for the
 * selected calendar date, not local-midnight — converting it through
 * ZoneId.systemDefault() shifts the date back a day in any timezone behind
 * UTC. Always go through ZoneOffset.UTC to recover the LocalDate the user
 * actually picked.
 */
object ExamDate {
    fun toLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
}
