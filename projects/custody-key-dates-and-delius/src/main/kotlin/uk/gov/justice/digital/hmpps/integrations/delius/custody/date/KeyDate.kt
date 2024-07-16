package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.custody.BaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceData
import java.time.LocalDate

@Entity
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "key_date_id_seq", sequenceName = "key_date_id_seq", allocationSize = 1)
@Table(name = "key_date", uniqueConstraints = [UniqueConstraint(columnNames = ["custody_id", "key_date_type_id", "key_date"])])
class KeyDate(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "key_date_id_seq")
    @Column(name = "key_date_id")
    val id: Long?,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody? = null,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    val date: LocalDate

) : BaseEntity() {
    fun changeDate(date: LocalDate): KeyDate? = if (this.date == date) {
        null
    } else {
        // create new entity to allow dry run to not make changes
        KeyDate(id, custody, type, date).also {
            it.createdDateTime = createdDateTime
            it.createdUserId = createdUserId
            it.version = version
        }
    }
}
