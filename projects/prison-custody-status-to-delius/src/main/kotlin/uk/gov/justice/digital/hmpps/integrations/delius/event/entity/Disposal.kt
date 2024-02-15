package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import java.time.ZonedDateTime

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
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_disposal_type")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column(name = "disposal_type_code")
    val code: String,

    @Column
    val sentenceType: String
) {
    enum class Code(val value: String) {
        COMMITTAL_PSSR_BREACH("326")
    }
}
