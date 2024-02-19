package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
data class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "event_number")
    val number: String,

    @Column
    val convictionDate: LocalDate?,

    @OneToOne(mappedBy = "event")
    val mainOffence: MainOffence,

    @OneToMany(mappedBy = "event")
    val additionalOffences: List<AdditionalOffence>,

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
@SQLRestriction("soft_deleted = 0")
data class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "disposal_date")
    val date: LocalDate,

    @Column(name = "entry_length")
    val length: Long?,

    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val lengthUnits: ReferenceData?,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Entity
@Immutable
@Table(name = "r_disposal_type")
data class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column(name = "description")
    val description: String,

    val sentenceType: String
) {
    fun isCustodial() = sentenceType in listOf("NC", "SC")
}
