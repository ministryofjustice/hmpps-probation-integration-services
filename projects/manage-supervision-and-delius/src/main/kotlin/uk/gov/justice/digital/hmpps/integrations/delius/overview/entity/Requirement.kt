package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0")
class Requirement(
    @Id
    @Column(name = "rqmnt_id", nullable = false)
    val id: Long,

    val length: Long?,

    @Column(name = "rqmnt_notes", columnDefinition = "clob")
    val notes: String?,

    val expectedStartDate: LocalDate?,

    val startDate: LocalDate,

    val commencementDate: LocalDate?,

    val expectedEndDate: LocalDate?,

    val terminationDate: LocalDate?,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory?,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val subCategory: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "rqmnt_termination_reason_id")
    val terminationDetails: ReferenceData?,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface RarDays {
    val days: Int
    val type: String
}

interface RequirementDetails {
    val id: Long
    val disposalId: Long
    val expectedStartDate: LocalDate?
    val startDate: LocalDate
    val commencementDate: LocalDate?
    val expectedEndDate: LocalDate?
    val terminationDate: LocalDate?
    val terminationReason: String?
    val length: Long?
    val lengthUnitValue: String?
    val code: String
    val description: String
    val codeDescription: String?
    val notes: String?
}

interface RequirementRepository : JpaRepository<Requirement, Long> {

    @Query(
        """
        select count(r.rqmnt_id) as days, 'SCHEDULED' as type from contact c
        join rqmnt r on r.rqmnt_id = c.rqmnt_id
        join r_rqmnt_type_main_category mc on r.rqmnt_type_main_category_id = mc.rqmnt_type_main_category_id
        where c.rar_activity = 'Y' and c.soft_deleted = 0
        and (c.attended is null)
        and (c.complied is null or c.complied = 'Y')
        and mc.code = 'F' and r.active_flag = 1 and r.soft_deleted = 0
        and r.disposal_id = :disposalId
        union
        select count(r.rqmnt_id) as days, 'COMPLETED' as type from contact c
        join rqmnt r on r.rqmnt_id = c.rqmnt_id
        join r_rqmnt_type_main_category mc on r.rqmnt_type_main_category_id = mc.rqmnt_type_main_category_id
        where c.rar_activity = 'Y' and c.soft_deleted = 0
        and (c.attended = 'Y')
        and (c.complied is null or c.complied = 'Y')
        and mc.code = 'F' and r.active_flag = 1 and r.soft_deleted = 0
        and r.disposal_id = :disposalId
        """, nativeQuery = true
    )
    fun getRarDays(disposalId: Long): List<RarDays>

    @Query(
        """
        select count(r.rqmnt_id) as days, 'SCHEDULED' as type from contact c
        join rqmnt r on r.rqmnt_id = c.rqmnt_id
        join r_rqmnt_type_main_category mc on r.rqmnt_type_main_category_id = mc.rqmnt_type_main_category_id
        where c.rar_activity = 'Y' and c.soft_deleted = 0
        and (c.attended is null)
        and (c.complied is null or c.complied = 'Y')
        and mc.code = 'F' and r.active_flag = 1 and r.soft_deleted = 0
        and r.disposal_id = :disposalId
        union
        select count(r.rqmnt_id) as days, 'COMPLETED' as type from contact c
        join rqmnt r on r.rqmnt_id = c.rqmnt_id
        join r_rqmnt_type_main_category mc on r.rqmnt_type_main_category_id = mc.rqmnt_type_main_category_id
        where c.rar_activity = 'Y' and c.soft_deleted = 0
        and (c.attended = 'Y')
        and (c.complied is null or c.complied = 'Y')
        and mc.code = 'F' and r.active_flag = 1 and r.soft_deleted = 0
        and r.disposal_id = :disposalId
        union 
        SELECT count(r.rqmnt_id) as days, 'NSI_COMPLETED' FROM contact c
        JOIN nsi n ON n.nsi_id = c.nsi_id
        JOIN rqmnt r on r.rqmnt_id = n.rqmnt_id 
        join r_rqmnt_type_main_category mc on r.rqmnt_type_main_category_id = mc.rqmnt_type_main_category_id
        where c.rar_activity = 'Y' and c.soft_deleted = 0
        and (c.attended  is null or c.attended = 'Y')
        and (c.complied is null or c.complied = 'Y')
        and mc.code = 'F' and r.active_flag = 1 and r.soft_deleted = 0
        and r.disposal_id = :disposalId
        """, nativeQuery = true
    )
    fun getRarDaysByDisposalId(disposalId: Long): List<RarDays>

    @Query(
        """
            SELECT r, m, d, sc, td, ud
            FROM Requirement r
            JOIN r.mainCategory m
            JOIN r.disposal d 
            JOIN d.event e
            LEFT JOIN r.subCategory sc
            LEFT JOIN r.terminationDetails td
            LEFT JOIN m.unitDetails ud
            WHERE e.id = :id
            AND e.eventNumber = :eventNumber
            AND (:includeRar = true OR m.code <> 'F')
            ORDER BY m.description
        """
    )
    fun getRequirements(id: Long, eventNumber: String, includeRar: Boolean = true): List<Requirement>

    @Query(
        """
            SELECT r, m, d, sc, td, ud
            FROM Requirement r
            LEFT JOIN r.mainCategory m
            JOIN r.disposal d
            JOIN d.event e
            LEFT JOIN r.subCategory sc
            LEFT JOIN r.terminationDetails td
            LEFT JOIN m.unitDetails ud
            WHERE r.id = :id
        """
    )
    fun getRequirement(id: Long): Requirement?

    @Query(
        """
            SELECT COALESCE(SUM(r.length), 0) 
            FROM Requirement r 
            JOIN  r.mainCategory mc 
            JOIN  r.disposal 
            WHERE r.disposal.id = :id 
            AND mc.code = 'W' 
        """
    )
    fun sumTotalUnpaidWorkHoursByDisposal(id: Long): Long
}

fun RequirementRepository.getRar(disposalId: Long): Rar {
    val rarDays = getRarDaysByDisposalId(disposalId)
    val scheduledDays = rarDays.find { it.type == "SCHEDULED" }?.days ?: 0
    val completedDays = rarDays.find { it.type == "COMPLETED" }?.days ?: 0
    val nsiCompletedDays = rarDays.find { it.type == "NSI_COMPLETED" }?.days ?: 0
    return Rar(completed = completedDays, nsiCompleted = nsiCompletedDays, scheduled = scheduledDays)
}

@Immutable
@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementMainCategory(
    @Id
    @Column(name = "rqmnt_type_main_category_id", nullable = false)
    val id: Long,
    val code: String,
    val description: String,

    @ManyToOne
    @JoinColumn(name = "units_id")
    val unitDetails: ReferenceData? = null,
)

