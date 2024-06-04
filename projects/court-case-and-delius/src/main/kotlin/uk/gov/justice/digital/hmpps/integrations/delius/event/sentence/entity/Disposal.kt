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
import java.time.ZonedDateTime

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
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

    val length2: Long? = null,

    val length: Long? = null,

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

interface DisposalRepository : JpaRepository<Disposal, Long> {

    @Query(
        """ 
        select d from Disposal d where d.event.person.crn = :crn
        and d.softDeleted = false
    """
    )
    fun getByCrn(crn: String): List<Disposal>
}

@Entity(name = "conviction_upw_details")
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

@Entity(name = "conviction_upw_appointment")
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

    @JoinColumn(name = "upw_details_id")
    @ManyToOne
    val upwDetails: UpwDetails,
)

interface UpwAppointmentRepository : JpaRepository<UpwAppointment, Long> {

    @Query(
        """
            SELECT COALESCE(SUM(u.minutesCredited), 0) as value, "sum_minutes" as type
            FROM conviction_upw_appointment u 
            JOIN  u.upwDetails upd 
            JOIN  upd.disposal d 
            WHERE d.id = :id
            UNION 
            SELECT count(u.id) as value, "total_appointments" as type
            FROM conviction_upw_appointment u 
            JOIN  u.upwDetails upd 
            JOIN  upd.disposal d 
            WHERE d.id = :id
            UNION 
            SELECT count(u.id) as value, "attended" as type
            FROM conviction_upw_appointment u 
            JOIN  u.upwDetails upd 
            JOIN  upd.disposal d 
            WHERE d.id = :id
            AND u.attended = 'Y'
            UNION 
            SELECT count(u.id) as value, "acceptable_absence" as type
            FROM conviction_upw_appointment u 
            JOIN  u.upwDetails upd 
            JOIN  upd.disposal d 
            WHERE d.id = :id
            AND u.attended = 'N'
            AND u.complied = 'Y'
            UNION 
            SELECT count(u.id) as value, "unacceptable_absence" as type
            FROM conviction_upw_appointment u 
            JOIN  u.upwDetails upd 
            JOIN  upd.disposal d 
            WHERE d.id = :id
            AND u.attended = 'N'
            AND u.complied = 'N'  
            UNION 
            SELECT count(u.id) as value, "no_outcome_recorded" as type
            FROM conviction_upw_appointment u 
            JOIN  u.upwDetails upd 
            JOIN  upd.disposal d 
            WHERE d.id = :id
            AND u.attended IS NULL 
            AND u.complied IS NULL                 
         """
    )
    fun getUnpaidTimeWorked(id: Long): List<UnpaidData>

    @Query(
        """
            SELECT COALESCE(SUM(u.minutesCredited), 0) AS sum_minutes,
            COUNT (u.id) AS total_appointments,
            COUNT (CASE WHEN u.attended = 'Y' THEN 1 END) AS attended
            FROM conviction_upw_appointment u 
            JOIN  u.upwDetails upd 
            JOIN  upd.disposal d 
            WHERE d.id = :id           
        """
    )
    fun getUnpaid(id: Long): Unpaid
}

interface Unpaid {
    val sumMinutes: Long
    val totalAppointments: Long
    val attended: Long
    val acceptableAbsence: Long
    val unacceptableAbsence: Long
    val noOutcomeRecorded: Long
}
interface UnpaidData {
    val value: Long
    val type: String
}


@Entity
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

    val prisonerNumber: String,

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "institution_id", referencedColumnName = "institution_id"),
        JoinColumn(name = "establishment", referencedColumnName = "establishment")
    )
    val institution: Institution,

    @OneToMany(mappedBy = "custody")
    val keyDates: List<KeyDate> = listOf(),

    @Id
    @Column(name = "custody_id")
    val id: Long
)

@Entity
@Immutable
@Table(name = "pss_rqmnt")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
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