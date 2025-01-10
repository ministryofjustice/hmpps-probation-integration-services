package uk.gov.justice.digital.hmpps.integrations.delius.caseview

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "disposal")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class CaseViewDisposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: CaseViewEvent,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class CaseViewEvent(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @Column(name = "event_number", nullable = false)
    val number: String,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

interface SentenceSummary {
    val eventId: Long
    val description: String
    val startDate: LocalDate
    val length: String
    val endDate: LocalDate
    val offenceMainCategory: String
    val offenceSubCategory: String
}
