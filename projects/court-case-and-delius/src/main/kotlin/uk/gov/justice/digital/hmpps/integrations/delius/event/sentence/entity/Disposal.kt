package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Disposal(
    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @OneToOne(mappedBy = "disposal")
    val custody: Custody?,

    @Column(name = "disposal_date")
    val startDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id", nullable = false)
    val disposalType: DisposalType,

    @Column(name = "notional_end_date")
    val endDate: ZonedDateTime? = null,

    @Column
    val terminationDate: LocalDate? = null,

    @Column(name = "entry_length")
    val entryLength: Long? = null,

    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val entryLengthUnit: ReferenceData? = null,

    @Column(name = "length_in_days")
    val lengthInDays: Long? = null,

    @ManyToOne
    @JoinColumn(name = "disposal_termination_reason_id")
    val terminationReason: ReferenceData?,

    @Column(name = "upw", columnDefinition = "number")
    val upw: Boolean = false,

    val effectiveLength: Long? = null,

    @ManyToOne
    @JoinColumn(name = "entry_length_2_units_id")
    val entryLength2Unit: ReferenceData? = null,

    @Column(name = "length_2")
    val length2: Long? = null,

    val length: Long? = null,

    @Column(name = "entered_notional_end_date")
    val enteredSentenceEndDate: LocalDate? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @OneToOne(mappedBy = "disposal")
    val unpaidWorkDetails: UpwDetails? = null,

    @Id
    @Column(name = "disposal_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "release")
class Release(
    @Id
    @Column(name = "release_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @Column(name = "actual_release_date")
    val date: LocalDateTime,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Entity
@Table(name = "upw_details")
@Immutable
class UpwDetails(
    @Id
    @Column(name = "upw_details_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    val upwLengthMinutes: Long,

    @ManyToOne
    @JoinColumn(name = "upw_status_id")
    val status: ReferenceData
)

@Entity
@Table(name = "upw_appointment")
@Immutable
class UpwAppointment(
    @Id
    @Column(name = "upw_appointment_id")
    val id: Long,

    val minutesCredited: Long?,

    @Column(columnDefinition = "char(1)")
    val attended: String?,

    @Column(columnDefinition = "char(1)")
    val complied: String?,

    val softDeleted: Long,

    val appointmentDate: LocalDate,

    val upwProjectId: Long,

    @JoinColumn(name = "upw_details_id")
    @ManyToOne
    val upwDetails: UpwDetails,
)

interface UpwAppointmentRepository : JpaRepository<UpwAppointment, Long> {

    @Query(
        """
            SELECT COALESCE(SUM(u.minutesCredited), 0) AS sumMinutes,
            COUNT (u.id) AS totalAppointments,
            COUNT (CASE WHEN u.attended = 'Y' THEN 1 END) AS attended,
            COUNT (CASE WHEN u.attended = 'N' AND u.complied = 'Y' THEN 1 END) AS acceptableAbsence,
            COUNT (CASE WHEN u.attended = 'N' AND u.complied = 'N' THEN 1 END) AS unacceptableAbsence,
            COUNT (CASE WHEN u.attended IS NULL AND u.complied is NULL THEN 1 END) AS noOutcomeRecorded
            FROM UpwAppointment u 
            JOIN  u.upwDetails upd 
            JOIN  upd.disposal d 
            WHERE d.id = :id           
        """
    )
    fun getUnpaidTimeWorked(id: Long): Unpaid
}

interface Unpaid {
    val sumMinutes: Long
    val totalAppointments: Long
    val attended: Long
    val acceptableAbsence: Long
    val unacceptableAbsence: Long
    val noOutcomeRecorded: Long
}

@Entity
@Table(name = "r_disposal_type")
@Immutable
class DisposalType(

    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    val description: String,

    val sentenceType: String,

    @Column(name = "cja2003")
    @Convert(converter = YesNoConverter::class)
    val cja2003Order: Boolean = false,

    @Column(name = "pre_cja2003")
    @Convert(converter = YesNoConverter::class)
    val legacyOrder: Boolean = false,

    @Column(name = "ftc_limit")
    val failureToComplyLimit: Long? = null

)

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Custody(
    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    val status: ReferenceData,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    val prisonerNumber: String?,

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "institution_id", referencedColumnName = "institution_id"),
        JoinColumn(name = "establishment", referencedColumnName = "establishment")
    )
    val institution: Institution,

    @OneToMany(mappedBy = "custody")
    val keyDates: List<KeyDate> = listOf(),

    @OneToMany(mappedBy = "custody")
    val releases: List<Release> = emptyList(),

    @Column(name = "pss_start_date")
    val pssStartDate: LocalDate? = null,

    @Id
    @Column(name = "custody_id")
    val id: Long
)

@Entity
@Immutable
@Table(name = "pss_rqmnt")
@SQLRestriction("soft_deleted = 0")
class PssRequirement(

    @Column(name = "custody_id")
    val custodyId: Long,

    @ManyToOne
    @JoinColumn(name = "pss_rqmnt_type_main_cat_id")
    val mainCategory: PssRequirementMainCat?,

    @ManyToOne
    @JoinColumn(name = "pss_rqmnt_type_sub_cat_id")
    val subCategory: PssRequirementSubCat?,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "pss_rqmnt_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_pss_rqmnt_type_main_category")
class PssRequirementMainCat(
    val code: String,
    val description: String,
    @Id
    @Column(name = "pss_rqmnt_type_main_cat_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_pss_rqmnt_type_sub_category")
class PssRequirementSubCat(
    val code: String,
    val description: String,
    @Id
    @Column(name = "pss_rqmnt_type_sub_cat_id")
    val id: Long
)

interface CustodyRepository : JpaRepository<Custody, Long>
interface PssRequirementRepository : JpaRepository<PssRequirement, Long> {
    fun findAllByCustodyId(custodyId: Long): List<PssRequirement>
}

@Immutable
@Entity
@Table(name = "additional_sentence")
@SQLRestriction("soft_deleted = 0")
class AdditionalSentence(
    @Id
    @Column(name = "additional_sentence_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "additional_sentence_type_id", nullable = false)
    val type: ReferenceData,

    @Column(name = "amount")
    val amount: BigDecimal? = null,

    @Column(name = "length")
    val length: Long? = null,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,
)

interface AdditionalSentenceRepository : JpaRepository<AdditionalSentence, Long> {
    fun getAllByEventId(id: Long): List<AdditionalSentence>
}