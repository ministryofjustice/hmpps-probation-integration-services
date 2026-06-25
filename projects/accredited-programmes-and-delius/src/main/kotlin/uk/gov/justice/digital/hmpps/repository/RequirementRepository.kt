package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.RequirementMainCategory.Companion.COURT_ACCREDITED_PROGRAMMES
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.RequirementMainCategory.Companion.RAR_GAR_ACCREDITED_PROGRAMMES
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.service.reportMissingIds

interface RequirementRepository : JpaRepository<Requirement, Long> {
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
        requirementIds: Set<Long>,
        mainCategoryCodes: Set<String> = setOf(RAR_GAR_ACCREDITED_PROGRAMMES, COURT_ACCREDITED_PROGRAMMES)
    ): List<Requirement>

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
    fun findAllByIdAndMainCategoryCodeIn(
        requirementId: Long,
        mainCategoryCodes: Set<String> = setOf(RAR_GAR_ACCREDITED_PROGRAMMES, COURT_ACCREDITED_PROGRAMMES)
    ): Requirement?

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
        mainCategoryCodes: Set<String> = setOf(RAR_GAR_ACCREDITED_PROGRAMMES, COURT_ACCREDITED_PROGRAMMES)
    ): List<Requirement>

    fun findByIdOrNotFound(id: Long) = findAllByIdAndMainCategoryCodeIn(id).orNotFoundBy("id", id)
    fun getAllByIdIn(ids: List<Long>) = ids.toSet().let { ids ->
        findAllByIdInAndActiveTrueAndMainCategoryCodeIn(ids).associateBy { it.id }.reportMissingIds(ids)
    }
}
