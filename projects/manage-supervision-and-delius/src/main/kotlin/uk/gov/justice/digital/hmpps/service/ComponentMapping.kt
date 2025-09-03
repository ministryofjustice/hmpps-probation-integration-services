package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import uk.gov.justice.digital.hmpps.api.model.sentence.MinimalLicenceCondition
import uk.gov.justice.digital.hmpps.api.model.sentence.MinimalRequirement
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceCondition

fun List<LicenceCondition>.lcSort() = sortedWith(
    compareByDescending(LicenceCondition::active)
        .thenByDescending(LicenceCondition::imposedReleasedDate)
        .thenBy { it.mainCategory.description })

fun List<LicenceCondition>.asMinimals(): List<MinimalLicenceCondition> =
    lcSort().map { it.toMinimalLicenceCondition() }

fun List<Requirement>.rSort() = sortedWith(
    compareByDescending(Requirement::active)
        .thenByDescending(Requirement::startDate)
        .thenBy { it.mainCategory!!.description })

fun List<Requirement>.asMinimals(rar: (Requirement) -> Rar?): List<MinimalRequirement> =
    rSort().map { it.toMinimalRequirement(rar) }

fun Requirement.toMinimalRequirement(rar: (Requirement) -> Rar?): MinimalRequirement {
    return MinimalRequirement(
        id,
        populateRequirementDescription(mainCategory!!.description, subCategory?.description, length, rar(this)),
        active
    )
}