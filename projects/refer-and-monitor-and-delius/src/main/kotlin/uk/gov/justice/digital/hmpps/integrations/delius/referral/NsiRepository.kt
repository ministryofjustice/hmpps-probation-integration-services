package uk.gov.justice.digital.hmpps.integrations.delius.referral

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Dataset.Code.NSI_OUTCOME
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatusHistory

interface NsiRepository : JpaRepository<Nsi, Long> {
    @EntityGraph(attributePaths = ["person", "status", "managers"])
    fun findByPersonCrnAndExternalReference(crn: String, ref: String): Nsi?
}

fun NsiRepository.getByCrnAndExternalReference(crn: String, ref: String) =
    findByPersonCrnAndExternalReference(crn, ref)
        ?: throw NotFoundException("NSI with reference $ref for CRN $crn not found")

interface NsiStatusHistoryRepository : JpaRepository<NsiStatusHistory, Long>

interface NsiStatusRepository : JpaRepository<NsiStatus, Long> {
    fun findByCode(code: String): NsiStatus?
}

fun NsiStatusRepository.nsiOutcome(code: String) =
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
