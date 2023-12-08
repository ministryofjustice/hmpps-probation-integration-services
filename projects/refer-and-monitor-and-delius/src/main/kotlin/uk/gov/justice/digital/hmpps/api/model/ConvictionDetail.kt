package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class CaseConvictions(
    val caseDetail: CaseDetail,
    val convictions: List<Conviction>,
)

data class CaseConviction(
    val caseDetail: CaseDetail,
    val conviction: Conviction,
)

data class Conviction(
    val id: Long,
    val date: LocalDate?,
    val sentence: Sentence,
    val mainOffence: Offence,
    val active: Boolean,
)

data class Sentence(val description: String, val expectedEndDate: LocalDate?)

data class Offence(val category: String, val subCategory: String)
