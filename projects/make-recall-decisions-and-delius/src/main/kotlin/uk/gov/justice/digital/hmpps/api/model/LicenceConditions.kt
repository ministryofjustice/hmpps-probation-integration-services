package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.api.model.LicenceConditions.ConvictionWithLicenceConditions
import uk.gov.justice.digital.hmpps.api.model.LicenceConditions.LicenceCondition
import uk.gov.justice.digital.hmpps.api.model.LicenceConditions.LicenceConditionCategory
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Event
import java.time.LocalDate

data class LicenceConditions(
    val personalDetails: PersonalDetailsOverview,
    val activeConvictions: List<ConvictionWithLicenceConditions>,
) {
    data class ConvictionWithLicenceConditions(
        val number: String,
        val sentence: Sentence?,
        val mainOffence: Offence,
        val additionalOffences: List<Offence>,
        val licenceConditions: List<LicenceCondition>,
    )

    data class LicenceCondition(
        val startDate: LocalDate,
        val mainCategory: LicenceConditionCategory,
        val subCategory: LicenceConditionCategory?,
        val notes: String?,
    )

    data class LicenceConditionCategory(
        val code: String,
        val description: String,
    )
}

fun Event.toConvictionWithLicenceConditions() =
    toConviction().let {
        ConvictionWithLicenceConditions(
            number = it.number,
            mainOffence = it.mainOffence,
            additionalOffences = it.additionalOffences,
            sentence = it.sentence,
            licenceConditions =
                disposal?.licenceConditions?.map { lc ->
                    LicenceCondition(
                        startDate = lc.startDate,
                        mainCategory = LicenceConditionCategory(lc.mainCategory.code, lc.mainCategory.description),
                        subCategory = lc.subCategory?.let { subCategory -> LicenceConditionCategory(subCategory.code, subCategory.description) },
                        notes = lc.notes,
                    )
                } ?: emptyList(),
        )
    }
