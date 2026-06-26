package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.LicenceConditionMainCategory.Companion.LICENCE_ACCREDITED_PROGRAMMES
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.service.reportMissingIds

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long> {
    @EntityGraph(
        attributePaths = [
            "mainCategory",
            "subCategory",
            "manager",
            "terminationReason",
            "disposal.type",
            "disposal.lengthUnits",
            "disposal.event.person.gender.dataset",
            "disposal.event.person.ethnicity.dataset",
            "disposal.event.person.manager.staff.user",
            "disposal.event.person.manager.team.localAdminUnit.probationDeliveryUnit",
            "disposal.event.person.manager.team.provider",
            "disposal.custody",
        ]
    )
    fun findAllByIdInAndActiveTrueAndMainCategoryCodeIn(
        licenceConditionIds: Set<Long>,
        mainCategoryCodes: Set<String> = setOf(LICENCE_ACCREDITED_PROGRAMMES)
    ): List<LicenceCondition>

    @EntityGraph(
        attributePaths = [
            "mainCategory",
            "subCategory",
            "manager",
            "terminationReason",
            "disposal.type",
            "disposal.lengthUnits",
            "disposal.event.person.gender.dataset",
            "disposal.event.person.ethnicity.dataset",
            "disposal.event.person.manager.staff.user",
            "disposal.event.person.manager.team.localAdminUnit.probationDeliveryUnit",
            "disposal.event.person.manager.team.provider",
            "disposal.custody",
        ]
    )
    fun findByIdAndMainCategoryCodeIn(
        licenceConditionId: Long,
        mainCategoryCodes: Set<String> = setOf(LICENCE_ACCREDITED_PROGRAMMES)
    ): LicenceCondition?

    @EntityGraph(
        attributePaths = [
            "mainCategory",
            "subCategory",
            "manager",
            "terminationReason",
            "disposal.type",
            "disposal.lengthUnits",
            "disposal.event.person.gender.dataset",
            "disposal.event.person.ethnicity.dataset",
            "disposal.event.person.manager.staff.user",
            "disposal.event.person.manager.team.localAdminUnit.probationDeliveryUnit",
            "disposal.event.person.manager.team.provider",
            "disposal.custody",
        ]
    )
    fun findAllByDisposalEventPersonCrnAndMainCategoryCodeIn(
        crn: String,
        mainCategoryCodes: Set<String> = setOf(LICENCE_ACCREDITED_PROGRAMMES)
    ): List<LicenceCondition>

    fun findByIdOrNotFound(id: Long) = findByIdAndMainCategoryCodeIn(id).orNotFoundBy("id", id)
    fun getAllByIdIn(ids: List<Long>) = ids.toSet().let { ids ->
        findAllByIdInAndActiveTrueAndMainCategoryCodeIn(ids).associateBy { it.id }.reportMissingIds(ids)
    }
}
