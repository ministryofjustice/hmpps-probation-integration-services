package uk.gov.justice.digital.hmpps.integrations.delius.referral

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.projections.NsiNotFoundReason
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiManager
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatusHistory
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiType

interface NsiRepository : JpaRepository<Nsi, Long> {
    @EntityGraph(attributePaths = ["person", "type", "status", "managers"])
    fun findByPersonCrnAndExternalReference(
        crn: String,
        ref: String,
    ): Nsi?

    @Query(
        """
        select nsi from Nsi nsi
        join Requirement r on r.id = nsi.requirementId
        join r.mainCategory rt
        where nsi.id = :id and rt.code = 'F'
    """,
    )
    fun findByIdIfRar(id: Long): Nsi?

    @Query(
        """
        select case when rt.code = 'F' then true else false end
        from Nsi nsi
        left join Requirement r on r.id = nsi.requirementId
        left join r.mainCategory rt
        where nsi.id = :id
        """,
    )
    fun isRar(id: Long): Boolean?

    @Query(
        """
            select 
                nsi.soft_deleted as nsiSoftDeleted,
                nsi.active_flag as nsiActive,
                last_updated_by.distinguished_name as nsiLastUpdatedBy
            from nsi
            join offender nsi_offender on nsi_offender.offender_id = nsi.offender_id 
            left join user_ last_updated_by on last_updated_by.user_id = nsi.last_updated_user_id
            where nsi_offender.crn = :crn and nsi.external_reference = :nsiExternalReference
        """,
        nativeQuery = true,
    )
    fun getNotFoundReason(
        crn: String,
        nsiExternalReference: String,
    ): NsiNotFoundReason?
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

interface NsiManagerRepository : JpaRepository<NsiManager, Long>
