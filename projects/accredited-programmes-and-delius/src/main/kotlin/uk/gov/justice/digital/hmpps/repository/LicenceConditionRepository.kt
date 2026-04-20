package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
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
    fun findAllByIdInAndActiveTrue(licenceConditionIds: Set<Long>): List<LicenceCondition>

    fun findByIdOrNotFound(id: Long) = findByIdOrNull(id).orNotFoundBy("id", id)
    fun getAllByCodeIn(ids: List<Long>) =
        ids.toSet().let { ids -> findAllByIdInAndActiveTrue(ids).associateBy { it.id }.reportMissingIds(ids) }
}
