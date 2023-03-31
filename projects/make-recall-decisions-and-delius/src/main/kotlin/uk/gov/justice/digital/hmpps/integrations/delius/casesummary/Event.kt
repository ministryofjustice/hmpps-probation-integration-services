package uk.gov.justice.digital.hmpps.integrations.delius.casesummary

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Table(name = "event")
@Entity(name = "CaseSummaryEvent")
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "event_number")
    val number: String,

    @OneToOne(mappedBy = "event", cascade = [CascadeType.PERSIST])
    val mainOffence: MainOffence,

    @OneToMany(mappedBy = "event", cascade = [CascadeType.PERSIST])
    val additionalOffences: List<AdditionalOffence>,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "disposal")
@Entity(name = "CaseSummaryDisposal")
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @Column
    val entryLength: Long?,

    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val entryLengthUnit: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @OneToOne(mappedBy = "disposal")
    val custody: Custody? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "r_disposal_type")
@Entity(name = "CaseSummaryDisposalType")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column
    val description: String
)

@Immutable
@Table(name = "custody")
@Entity(name = "CaseSummaryCustody")
@Where(clause = "soft_deleted = 0")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    val status: ReferenceData,

    @JoinColumn
    @OneToOne(mappedBy = "custody")
    @Where(clause = "key_date_type_id = (select standard_reference_list_id from r_standard_reference_list where code_value = 'SED')")
    val sentenceExpiryDate: KeyDate? = null,

    @JoinColumn
    @OneToOne(mappedBy = "custody")
    @Where(clause = "key_date_type_id = (select standard_reference_list_id from r_standard_reference_list where code_value = 'LED')")
    val licenceExpiryDate: KeyDate? = null,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "key_date")
@Entity(name = "CaseSummaryKeyDate")
@Where(clause = "soft_deleted = 0")
class KeyDate(
    @Id
    @Column(name = "key_date_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    var date: LocalDate,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "main_offence")
@Entity(name = "CaseSummaryMainOffence")
@Where(clause = "soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event?,

    @Column(name = "offence_date")
    val date: LocalDate,

    @JoinColumn(name = "offence_id")
    @ManyToOne(cascade = [CascadeType.PERSIST])
    val offence: Offence,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "additional_offence")
@Entity(name = "CaseSummaryAdditionalOffence")
@Where(clause = "soft_deleted = 0")
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event?,

    @Column(name = "offence_date")
    val date: LocalDate,

    @JoinColumn(name = "offence_id")
    @ManyToOne(cascade = [CascadeType.PERSIST])
    val offence: Offence,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "r_offence")
@Entity(name = "CaseSummaryOffence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column
    val code: String,

    @Column
    val description: String
)

interface CaseSummaryEventRepository : JpaRepository<Event, Long> {
    @EntityGraph(
        attributePaths = [
            "mainOffence.offence",
            "additionalOffences.offence",
            "disposal.entryLengthUnit",
            "disposal.type",
            "disposal.custody.status",
            "disposal.custody.sentenceExpiryDate.type",
            "disposal.custody.licenceExpiryDate.type"
        ]
    )
    fun findByPersonId(personId: Long): List<Event>
}
