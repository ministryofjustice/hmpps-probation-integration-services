package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "disposal")
class Disposal(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column(name = "disposal_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "entered_notional_end_date")
    val enteredEndDate: LocalDate? = null,

    @Column(name = "notional_end_date")
    val notionalEndDate: LocalDate? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "disposal_id")
    val id: Long
) {
    fun expectedEndDate() = enteredEndDate ?: notionalEndDate
}

@Entity
@Immutable
@Table(name = "r_disposal_type")
class DisposalType(

    @Column(name = "disposal_type_code")
    val code: String,

    val description: String,

    @Column(name = "sentence_type")
    val sentenceType: String? = null,

    @Column(name = "ftc_limit")
    val ftcLimit: Long? = null,

    @Id
    @Column(name = "disposal_type_id")
    val id: Long
) {
    fun overLimit(count: Long): Boolean = sentenceType != null && ftcLimit != null && count > ftcLimit

    enum class Code(val value: String) {
        COMMITTAL_PSSR_BREACH("326")
    }
}

interface DisposalRepository : JpaRepository<Disposal, Long> {
    @EntityGraph(attributePaths = ["type", "event"])
    fun findByEventPersonIdAndEventId(personId: Long, eventId: Long): Disposal?
}

fun DisposalRepository.getByPersonIdAndEventId(personId: Long, eventId: Long) =
    findByEventPersonIdAndEventId(personId, eventId) ?: throw NotFoundException("Event", "id", eventId)
