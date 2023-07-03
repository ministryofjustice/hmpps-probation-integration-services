package uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.LockModeType
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.Nsi
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "approved_premises_referral")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "ap_referral_id_seq", sequenceName = "ap_referral_id_seq", allocationSize = 1)
class Referral(

    @Column(name = "offender_id")
    val personId: Long,

    val eventId: Long,
    val approvedPremisesId: Long,

    val referralDate: LocalDate,
    val expectedArrivalDate: LocalDate?,
    val expectedDepartureDate: LocalDate?,
    val decisionDate: ZonedDateTime?,
    @Lob
    val referralNotes: String?,
    val referralDateTypeId: Long?,
    @Column(name = "referral_category_id")
    val categoryId: Long,

    val referringTeamId: Long,
    val referringStaffId: Long,
    @Column(columnDefinition = "varchar2(400)")
    val reasonForReferral: String?,
    val referralSourceId: Long,
    val sourceTypeId: Long,

    @Column(name = "referral_decision_id")
    val decisionId: Long,
    @Column(name = "decision_by_team_id")
    val decisionTeamId: Long,
    @Column(name = "decision_by_staff_id")
    val decisionStaffId: Long,
    @Lob
    val decisionNotes: String?,

    val activeArsonRiskId: Long,
    @Column(columnDefinition = "varchar2(400)")
    val arsonRiskDetails: String?,
    val disabilityIssuesId: Long,
    @Column(columnDefinition = "varchar2(400)")
    val disabilityDetails: String?,
    val singleRoomId: Long,
    @Column(columnDefinition = "varchar2(400)")
    val singleRoomDetails: String?,

    @Convert(converter = YesNoConverter::class)
    val sexOffender: Boolean,
    @Convert(converter = YesNoConverter::class)
    val gangAffiliated: Boolean,

    val rohPublicId: Long,
    val rohStaffId: Long,
    val rohKnownPersonId: Long,
    val rohChildrenId: Long,
    val rohResidentsId: Long,
    val rohSelfId: Long,
    val rohOthersId: Long,
    @Lob
    val riskInformation: String?
) {
    @Column(name = "original_ap_admit_date")
    var admissionDate: LocalDate? = null

    var nonArrivalDate: LocalDate? = null
    var nonArrivalReasonId: Long? = null

    @Lob
    var nonArrivalNotes: String? = null

    @Id
    @Column(name = "approved_premises_referral_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ap_referral_id_seq")
    val id: Long = 0

    val reservationStartDate: LocalDate? = expectedArrivalDate
    val reservationLength: Long? = if (expectedArrivalDate != null && expectedDepartureDate != null) {
        ChronoUnit.DAYS.between(expectedArrivalDate, expectedDepartureDate)
    } else {
        null
    }

    @Column(columnDefinition = "number")
    var softDeleted: Boolean = false

    @Version
    var rowVersion: Long = 0

    @CreatedDate
    @Column
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @Column
    @CreatedBy
    var createdByUserId: Long = 0

    @Column
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    @Column
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0

    @Column
    val partitionAreaId: Long = 0

    fun isForBooking(bookingId: String): Boolean =
        referralNotes?.contains(Nsi.EXT_REF_BOOKING_PREFIX + bookingId) == true
}

@Entity
@Immutable
@Table(name = "r_referral_source")
class ReferralSource(
    @Id
    @Column(name = "referral_source_id")
    val id: Long,

    val code: String
)

@Entity
@Immutable
@Table
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(name = "event_number")
    val number: String,

    @Column(name = "offender_id")
    val personId: Long
)

interface ReferralRepository : JpaRepository<Referral, Long> {
    fun findByPersonIdAndCreatedByUserIdAndReferralNotesContains(
        personId: Long,
        createdByUserId: Long,
        externalRef: String
    ): Referral?
}

interface ReferralSourceRepository : JpaRepository<ReferralSource, Long> {
    fun findByCode(code: String): ReferralSource?
}

fun ReferralSourceRepository.getByCode(code: String) = findByCode(code)
    ?: throw NotFoundException("ReferralSource", "code", code)

interface EventRepository : JpaRepository<Event, Long> {
    fun findByPersonIdAndNumber(personId: Long, number: String): Event?

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select e.id from Event e where e.id = :id")
    fun findForUpdate(id: Long): Long
}

fun EventRepository.getByEventNumber(personId: Long, number: String) =
    findByPersonIdAndNumber(personId, number) ?: throw NotFoundException("Event Not Found")
