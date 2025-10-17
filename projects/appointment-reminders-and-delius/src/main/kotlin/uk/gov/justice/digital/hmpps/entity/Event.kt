package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
