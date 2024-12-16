package uk.gov.justice.digital.hmpps.integrations.delius.casesummary

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Table(name = "event")
@Entity(name = "CaseSummaryEvent")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
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
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "disposal")
@Entity(name = "CaseSummaryDisposal")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @Column(name = "disposal_date")
    val startDate: LocalDate,

    @Column
    val entryLength: Long?,

    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val entryLengthUnit: ReferenceData?,

    @Column(name = "length_2")
    val secondEntryLength: Long?,

    @ManyToOne
    @JoinColumn(name = "entry_length_2_units_id")
    val secondEntryLengthUnit: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @OneToOne(mappedBy = "disposal")
    val custody: Custody? = null,

    @OneToMany(mappedBy = "disposal")
    val licenceConditions: List<LicenceCondition> = emptyList(),

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
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
@SQLRestriction("soft_deleted = 0")
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

    @OneToMany(mappedBy = "custody")
    @SQLRestriction("key_date_type_id = (select sed.standard_reference_list_id from r_standard_reference_list sed where sed.code_value = 'SED')")
    val sentenceExpiryDate: Set<KeyDate> = emptySet(),

    @OneToMany(mappedBy = "custody")
    @SQLRestriction("key_date_type_id = (select led.standard_reference_list_id from r_standard_reference_list led where led.code_value = 'LED')")
    val licenceExpiryDate: Set<KeyDate> = emptySet(),

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "key_date")
@Entity(name = "CaseSummaryKeyDate")
@SQLRestriction("soft_deleted = 0")
class KeyDate(
    @Id
    @Column(name = "key_date_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    var date: LocalDate,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "main_offence")
@Entity(name = "CaseSummaryMainOffence")
@SQLRestriction("soft_deleted = 0")
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
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "additional_offence")
@Entity(name = "CaseSummaryAdditionalOffence")
@SQLRestriction("soft_deleted = 0")
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event?,

    @Column(name = "offence_date")
    val date: LocalDate?,

    @JoinColumn(name = "offence_id")
    @ManyToOne(cascade = [CascadeType.PERSIST])
    val offence: Offence,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "r_offence")
@Entity(name = "CaseSummaryOffence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column(columnDefinition = "char(5)")
    val code: String,

    @Column
    val description: String
)

@Immutable
@Table(name = "lic_condition")
@Entity(name = "CaseSummaryLicenceCondition")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class LicenceCondition(
    @Id
    @Column(name = "lic_condition_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @Column
    val startDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: LicenceConditionMainCategory,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData?,

    @Lob
    @Column(name = "lic_condition_notes")
    val notes: String?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "r_lic_cond_type_main_cat")
@Entity(name = "CaseSummaryLicenceConditionMainCategory")
class LicenceConditionMainCategory(
    @Id
    @Column(name = "lic_cond_type_main_cat_id")
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
            "disposal.secondEntryLengthUnit",
            "disposal.type",
            "disposal.custody.status",
            "disposal.custody.sentenceExpiryDate.type",
            "disposal.custody.licenceExpiryDate.type"
        ]
    )
    fun findByPersonId(personId: Long): List<Event>
}
