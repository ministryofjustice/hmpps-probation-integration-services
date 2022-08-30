package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.custody.Custody
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table

@Immutable
@Entity
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id", updatable = false)
    val type: DisposalType,

    @Column(name = "disposal_date", nullable = false)
    val date: ZonedDateTime,

    @Column
    val lengthInDays: Long? = null,

    @Column
    val notionalEndDate: ZonedDateTime? = null,

    @OneToOne
    @JoinColumn(name = "event_id", updatable = false)
    val event: Event,

    @OneToOne(mappedBy = "disposal")
    var custody: Custody? = null,

    @Column(name = "active_flag", updatable = false, columnDefinition = "NUMBER")
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,
) {
    fun isLongerThan20Months(): Boolean? {
        val endDate = if (lengthInDays != null) date.plusDays(lengthInDays) else notionalEndDate ?: return null
        return endDate > date.plusMonths(20)
    }
}

@Immutable
@Entity
@Table(name = "r_disposal_type")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column
    val sentenceType: String,
)
