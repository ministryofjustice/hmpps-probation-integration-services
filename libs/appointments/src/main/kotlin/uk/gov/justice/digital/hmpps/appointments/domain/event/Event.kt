package uk.gov.justice.digital.hmpps.appointments.domain.event

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Table(name = "event")
@Entity
@SQLRestriction("soft_deleted = 0")
open class Event(

    @OneToOne(mappedBy = "event")
    val disposal: Disposal?,

    @Column(name = "ftc_count")
    var ftcCount: Long,

    @Column(name = "breach_end")
    val breachEnd: LocalDate?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "event_id")
    val id: Long = 0,
)

@Entity
@Immutable
@Table(name = "disposal")
@SQLRestriction("soft_deleted = 0")
open class Disposal(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "disposal_date")
    val date: LocalDate,

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
open class DisposalType(

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
}

interface EventRepository : JpaRepository<Event, Long>