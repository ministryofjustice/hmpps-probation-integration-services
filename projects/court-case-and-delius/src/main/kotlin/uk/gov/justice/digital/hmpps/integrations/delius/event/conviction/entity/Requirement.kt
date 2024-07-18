package uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.AdRequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.RequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Custody
import java.time.LocalDate
import java.time.ZonedDateTime
import uk.gov.justice.digital.hmpps.api.model.conviction.Requirement as RequirementModel

@Immutable
@Entity(name = "conviction_rqmnt")
@Table(name = "rqmnt")
class Requirement(

    @Id
    @Column(name = "rqmnt_id")
    val id: Long,

    @Column(name = "disposal_id")
    val disposalId: Long,

    @Column(name = "rqmnt_notes", columnDefinition = "clob")
    val notes: String? = null,

    @Column(name = "commencement_date")
    val commencementDate: LocalDate? = null,

    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Column(name = "termination_date")
    val terminationDate: LocalDate? = null,

    @Column(name = "expected_start_date")
    val expectedStartDate: LocalDate? = null,

    @Column(name = "expected_end_date")
    val expectedEndDate: LocalDate? = null,

    @Column(name = "created_datetime")
    val createdDatetime: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val subCategory: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory? = null,

    @ManyToOne
    @JoinColumn(name = "ad_rqmnt_type_main_category_id")
    val adMainCategory: AdRequirementMainCategory? = null,

    @ManyToOne
    @JoinColumn(name = "ad_rqmnt_type_sub_category_id")
    val adSubCategory: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_termination_reason_id")
    val terminationReason: ReferenceData? = null,

    @Column(name = "length")
    val length: Long? = null,

    @Column(name = "rar_count")
    val rarCount: Long? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "disposal_id", updatable = false, insertable = false)
    val disposal: Disposal? = null
) {
    fun toRequirementModel(): RequirementModel = RequirementModel(
        id,
        notes,
        commencementDate,
        startDate,
        terminationDate,
        expectedStartDate,
        expectedEndDate,
        createdDatetime,
        active,
        subCategory?.let { KeyValue(it.code, it.description) },
        mainCategory?.let { KeyValue(it.code, it.description) },
        adMainCategory?.let { KeyValue(it.code, it.description) },
        adSubCategory?.let { KeyValue(it.code, it.description) },
        terminationReason?.let { KeyValue(it.code, it.description) },
        length,
        mainCategory?.let { it.units?.description },
        mainCategory?.let {
            when (it.restrictive) {
                "Y" -> true
                else -> false
            }
        },
        softDeleted,
        rarCount
    )
}

@Immutable
@Entity(name = "conviction_event")
@Table(name = "event")
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Column(name = "offender_id")
    val offenderId: Long,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,
)

interface ConvictionEventRepository : JpaRepository<Event, Long> {
    fun findEventByIdAndOffenderId(eventId: Long, personId: Long): Event?
}

fun ConvictionEventRepository.getByEventId(eventId: Long, personId: Long): Event =
    findEventByIdAndOffenderId(eventId, personId)
        ?: throw NotFoundException("Conviction with convictionId $eventId not found")

@Immutable
@Entity(name = "conviction_disposal")
@Table(name = "disposal")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @OneToOne(mappedBy = "disposal")
    val custody: Custody?,
)

@Entity(name = "conviction_custody")
@Table(name = "custody")
@Immutable
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @Column(name = "disposal_id")
    val disposalId: Long,

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false, insertable = false)
    val disposal: Disposal,
)

interface ConvictionRequirementRepository : JpaRepository<Requirement, Long> {
    @Query(
        """
        SELECT r FROM conviction_rqmnt r
        JOIN r.disposal.event e 
        WHERE e.id = :eventId
        AND r.active = :active
        AND r.softDeleted = :softDeleted
    """
    )
    fun getRequirements(eventId: Long, active: Boolean, softDeleted: Boolean): List<Requirement>
}
