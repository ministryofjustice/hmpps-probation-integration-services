package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence

import java.time.LocalDate

interface SentenceSummary {
    val eventId: Long
    val description: String
    val startDate: LocalDate
    val length: String
    val endDate: LocalDate
    val offenceMainCategory: String
    val offenceSubCategory: String
}
