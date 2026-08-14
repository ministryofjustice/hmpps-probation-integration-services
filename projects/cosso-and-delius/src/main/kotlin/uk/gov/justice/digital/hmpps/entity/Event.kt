package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Table(name = "event")
@SQLRestriction("soft_deleted = 0")
class EventEntity(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "event_number")
    val number: String,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val activeFlag: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
)

interface EventRepository : JpaRepository<EventEntity, Long> {
    fun findByPersonCrnAndNumber(crn: String, number: String): EventEntity?
}

fun EventRepository.getByCrnAndNumber(crn: String, number: String): EventEntity =
    findByPersonCrnAndNumber(crn, number) ?: throw NotFoundException("Event", "event number", number)

