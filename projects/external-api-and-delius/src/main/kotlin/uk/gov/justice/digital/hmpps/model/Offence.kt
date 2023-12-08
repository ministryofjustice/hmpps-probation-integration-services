package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integration.delius.entity.Offence
import java.time.LocalDate

data class Offence(
    val date: LocalDate?,
    val count: Int?,
    val code: String,
    val description: String,
    val mainCategory: OffenceCategory?,
    val subCategory: OffenceCategory?,
    val schedule15SexualOffence: Boolean?,
    val schedule15ViolentOffence: Boolean?,
) {
    companion object {
        fun of(
            date: LocalDate?,
            count: Int?,
            offence: Offence,
        ) = Offence(
            date = date,
            count = count,
            code = offence.code,
            description = offence.description,
            mainCategory =
                OffenceCategory(
                    code = offence.mainCategoryCode,
                    description = offence.mainCategoryDescription,
                ),
            subCategory =
                OffenceCategory(
                    code = offence.subCategoryCode,
                    description = offence.subCategoryDescription,
                ),
            schedule15SexualOffence = offence.schedule15SexualOffence,
            schedule15ViolentOffence = offence.schedule15ViolentOffence,
        )
    }
}

data class OffenceCategory(
    val code: String,
    val description: String,
)
