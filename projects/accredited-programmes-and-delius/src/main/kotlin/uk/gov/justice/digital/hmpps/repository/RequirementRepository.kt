package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.service.reportMissingIds

interface RequirementRepository : JpaRepository<Requirement, Long> {
    @EntityGraph(
        attributePaths = [
            "mainCategory",
            "subCategory",
            "disposal.type",
            "disposal.lengthUnits",
            "disposal.event.person.gender",
            "disposal.event.person.ethnicity",
            "disposal.event.person.manager.staff.user",
            "disposal.event.person.manager.team.localAdminUnit.probationDeliveryUnit",
            "disposal.event.person.manager.team.provider",
            "disposal.custody"
        ]
    )
    fun findAllByIdIn(requirementIds: Set<Long>): List<Requirement>
}

fun RequirementRepository.getAllByCodeIn(ids: List<Long>) =
    ids.toSet().let { ids -> findAllByIdIn(ids).associateBy { it.id }.reportMissingIds(ids) }
