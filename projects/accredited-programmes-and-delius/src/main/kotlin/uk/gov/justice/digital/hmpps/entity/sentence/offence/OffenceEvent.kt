package uk.gov.justice.digital.hmpps.entity.sentence.offence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.PersonCrn

@Entity
@Immutable
@Table(name = "event")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class OffenceEvent(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(name = "event_number")
    val number: String,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: PersonCrn,

    @OneToMany(mappedBy = "event")
    val mainOffence: List<MainOffence>,

    @OneToMany(mappedBy = "event")
    val additionalOffences: List<AdditionalOffence>,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
)