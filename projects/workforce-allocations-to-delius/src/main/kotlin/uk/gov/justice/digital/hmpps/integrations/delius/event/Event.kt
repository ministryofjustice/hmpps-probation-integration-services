package uk.gov.justice.digital.hmpps.integrations.delius.event

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate

@Immutable
@Entity
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Column(name = "event_number", nullable = false)
    val number: String,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    val referralDate: LocalDate,

    @Column(name = "ftc_count", nullable = false)
    val failureToComplyCount: Int,

    @Column(name = "breach_end")
    val breachEnd: LocalDate?,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)
