package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.hibernate.type.YesNoConverter
import java.time.LocalDate

@Entity
@Immutable
@Where(clause = "soft_deleted = 0")
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column
    val referralDate: LocalDate,

    @OneToOne(mappedBy = "event")
    val mainOffence: MainOffence,

    @OneToMany(mappedBy = "event")
    val courtAppearances: List<CourtAppearance>,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Entity
@Immutable
@Where(clause = "soft_deleted = 0")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

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
    val custody: Custody? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Entity
@Immutable
@Table(name = "r_disposal_type")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column
    val description: String
)

@Entity
@Immutable
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "institution_id", referencedColumnName = "institution_id"),
        JoinColumn(name = "establishment", referencedColumnName = "establishment")
    )
    val institution: Institution
)

@Entity
@Immutable
@Table(name = "r_institution")
class Institution(
    @Id
    @Column(name = "institution_id")
    val id: Long,

    @Column(name = "institution_name")
    val name: String,

    @Column
    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean
)
