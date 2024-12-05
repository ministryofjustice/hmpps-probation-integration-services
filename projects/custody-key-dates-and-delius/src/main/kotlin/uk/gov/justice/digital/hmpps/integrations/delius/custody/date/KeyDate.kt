package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.custody.BaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceData
import java.time.LocalDate

@Entity
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "key_date_id_seq", sequenceName = "key_date_id_seq", allocationSize = 1)
@Table(
    name = "key_date",
    uniqueConstraints = [UniqueConstraint(columnNames = ["custody_id", "key_date_type_id", "key_date"])]
)
class KeyDate(

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody? = null,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    val date: LocalDate,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "key_date_id_seq")
    @Column(name = "key_date_id")
    val id: Long = 0,
) : BaseEntity() {
    fun changeDate(date: LocalDate): KeyDate? = if (this.date == date && !this.softDeleted) {
        null
    } else {
        // create new entity to allow dry run to not make changes
        KeyDate(custody, type, date, id).also {
            it.createdDateTime = createdDateTime
            it.createdUserId = createdUserId
            it.version = version
            it.softDeleted = false
        }
    }
}
