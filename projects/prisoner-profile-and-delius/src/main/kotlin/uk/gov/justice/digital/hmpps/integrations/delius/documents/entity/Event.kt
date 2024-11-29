package uk.gov.justice.digital.hmpps.integrations.delius.documents.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Event(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column
    val referralDate: LocalDate,

    @OneToOne(mappedBy = "event")
    val mainOffence: MainOffence,

    @OneToMany(mappedBy = "event")
    val courtAppearances: List<CourtAppearance> = listOf(),

    @OneToOne(mappedBy = "event")
    val disposal: Disposal?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "event_id")
    val id: Long,
)

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Disposal(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "entry_length")
    val length: Long?,

    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val lengthUnits: ReferenceData?,

    @OneToOne(mappedBy = "disposal")
    val custody: Custody?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "disposal_id")
    val id: Long
)

@Entity
@Immutable
@Table(name = "r_disposal_type")
class DisposalType(
    @Column
    val description: String,

    @Id
    @Column(name = "disposal_type_id")
    val id: Long
)

@Entity
@Immutable
class Custody(

    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "institution_id", referencedColumnName = "institution_id"),
        JoinColumn(name = "establishment", referencedColumnName = "establishment")
    )
    val institution: Institution,

    @Id
    @Column(name = "custody_id")
    val id: Long
)

@Entity
@Immutable
@Table(name = "r_institution")
class Institution(

    @Column(name = "institution_name")
    val name: String,

    @Column
    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean,

    @Id
    @Column(name = "institution_id")
    val id: Long
)
