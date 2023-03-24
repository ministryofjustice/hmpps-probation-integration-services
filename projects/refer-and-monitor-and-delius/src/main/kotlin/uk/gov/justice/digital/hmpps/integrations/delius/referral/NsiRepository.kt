package uk.gov.justice.digital.hmpps.integrations.delius.referral

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Dataset.Code.NSI_OUTCOME
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiManager
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatusHistory
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiType

interface NsiRepository : JpaRepository<Nsi, Long> {
    @EntityGraph(attributePaths = ["person", "type", "status", "managers"])
    fun findByPersonCrnAndExternalReference(crn: String, ref: String): Nsi?

    @Query(
        """
        select nsi from Nsi nsi
        join fetch nsi.person p
        join fetch nsi.type t
        join fetch nsi.status
        join fetch nsi.managers
        join Provider pr on nsi.intendedProviderId = pr.id
        where p.crn = :crn
        and nsi.eventId = :eventId
        and t.code in :types
        and pr.code = 'CRS'
        """
    )
    fun fuzzySearch(crn: String, eventId: Long, types: Set<String>): List<Nsi>
}

interface NsiStatusHistoryRepository : JpaRepository<NsiStatusHistory, Long>

interface NsiTypeRepository : JpaRepository<NsiType, Long> {
    fun findByCode(code: String): NsiType?
}

fun NsiTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("NsiType", "code", code)

interface NsiStatusRepository : JpaRepository<NsiStatus, Long> {
    fun findByCode(code: String): NsiStatus?
}

fun NsiStatusRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("NsiStatus", "code", code)

interface NsiOutcomeRepository : JpaRepository<NsiOutcome, Long> {
    @Query(
        """
        select oc from NsiOutcome oc
        join Dataset ds on oc.datasetId = ds.id
        where oc.code = :code
        and ds.name = :datasetName
    """
    )
    fun findByCode(code: String, datasetName: String): NsiOutcome?
}

fun NsiOutcomeRepository.nsiOutcome(code: String) =
    findByCode(code, NSI_OUTCOME.value) ?: throw NotFoundException("NsiOutcome", "code", code)

interface NsiManagerRepository : JpaRepository<NsiManager, Long>
