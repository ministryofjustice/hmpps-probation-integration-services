package uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
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
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "approved_premises_referral")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "ap_referral_id_seq", sequenceName = "ap_referral_id_seq", allocationSize = 1)
class Referral(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "approved_premises_id")
    val approvedPremises: ApprovedPremises,

    val referralDate: LocalDate,
    val expectedArrivalDate: LocalDate?,
    val expectedDepartureDate: LocalDate?,
    val decisionDate: ZonedDateTime?,
    @Lob
    val referralNotes: String?,
    @ManyToOne
    @JoinColumn(name = "referral_date_type_id")
    val referralDateType: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "referral_category_id")
    val category: ReferenceData,

    val referringTeamId: Long,
    val referringStaffId: Long,

    @Lob
    val reasonForReferral: String?,

    @ManyToOne
    @JoinColumn(name = "referral_source_id")
    val referralSource: ReferralSource,
    @ManyToOne
    @JoinColumn(name = "source_type_id")
    val sourceType: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "referral_decision_id")
    val decision: ReferenceData,
    val decisionTeamId: Long,
    val decisionStaffId: Long,
    @Lob
    val decisionNotes: String?,

    @ManyToOne
    @JoinColumn(name = "active_arson_risk_id")
    val activeArsonRisk: ReferenceData,
    @Lob
    val arsonRiskDetails: String?,

    @ManyToOne
    @JoinColumn(name = "disability_issues_id")
    val disabilityIssues: ReferenceData,
    @Lob
    val disabilityDetails: String?,

    @ManyToOne
    @JoinColumn(name = "single_room_id")
    val singleRoom: ReferenceData,
    @Lob
    val singleRoomDetails: String?,

    @Convert(converter = YesNoConverter::class)
    val sexOffender: Boolean,
    @Convert(converter = YesNoConverter::class)
    val gangAffiliated: Boolean,

    @ManyToOne
    @JoinColumn(name = "roh_public_id")
    val rohPublic: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "roh_staff_id")
    val rohStaff: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "roh_known_person_id")
    val rohKnownPerson: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "roh_children_id")
    val rohChildren: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "roh_residents_id")
    val rohResidents: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "roh_self_id")
    val rohSelf: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "roh_others_id")
    val rohOthers: ReferenceData,
    @Lob
    val riskInformation: String?
) {
    @Column(name = "original_ap_admit_date")
    var admissionDate: LocalDate? = null
    var nonArrivalDate: LocalDate? = null

    @ManyToOne
    @JoinColumn(name = "non_arrival_reason_id")
    var nonArrivalReason: ReferenceData? = null

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

interface ReferralRepository : JpaRepository<Referral, Long>
interface ReferralSourceRepository : JpaRepository<ReferralSource, Long> {
    fun findByCode(code: String): ReferralSource?
}
fun ReferralSourceRepository.getByCode(code: String) = findByCode(code)
    ?: throw NotFoundException("ReferralSource", "code", code)

interface EventRepository : JpaRepository<Event, Long> {
    fun findByPersonIdAndNumber(personId: Long, number: String): Event?
}
fun EventRepository.getByEventNumber(personId: Long, number: String) =
    findByPersonIdAndNumber(personId, number) ?: throw NotFoundException("Event Not Found")
