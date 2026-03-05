package uk.gov.justice.digital.hmpps.entity.event.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.event.EventEntity
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @Column(name = "disposal_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: EventEntity? = null,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
