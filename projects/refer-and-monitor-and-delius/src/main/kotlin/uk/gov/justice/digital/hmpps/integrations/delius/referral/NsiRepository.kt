package uk.gov.justice.digital.hmpps.integrations.delius.referral

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Dataset
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

fun NsiStatusRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("NsiStatus", "code", code)

interface NsiOutcomeRepository : JpaRepository<NsiOutcome, Long> {
    @Query(
        """
        select oc from NsiOutcome oc
        join Dataset ds on oc.datasetId = ds.id
        where ds.name = :dataset
        and oc.code = :code
    """
    )
    fun findByCode(code: String, dataset: String): NsiOutcome?
}

fun NsiOutcomeRepository.getByCode(code: String, datasetCode: Dataset.Code = Dataset.Code.NSI_OUTCOME) =
    findByCode(code, datasetCode.value) ?: throw NotFoundException("NsiOutcome", "code", code)
