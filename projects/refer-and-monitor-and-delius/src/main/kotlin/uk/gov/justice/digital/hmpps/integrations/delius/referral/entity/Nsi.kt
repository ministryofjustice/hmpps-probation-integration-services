package uk.gov.justice.digital.hmpps.integrations.delius.referral.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "nsi")
@Where(clause = "soft_deleted = 0")
class Nsi(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "nsi_status_id")
    val status: NsiStatus,

    @Column(name = "nsi_status_date")
    val statusDate: ZonedDateTime = ZonedDateTime.now(),

    val referralDate: ZonedDateTime,

    val actualStartDate: ZonedDateTime? = null,

    var actualEndDate: ZonedDateTime? = null,

    @Lob
    val notes: String? = null,

    val externalReference: String? = null,

    @Id
    @SequenceGenerator(name = "nsi_id_generator", sequenceName = "nsi_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nsi_id_generator")
    @Column(name = "nsi_id", nullable = false)
    val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(name = "active_flag", columnDefinition = "number")
    var active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Entity
@Immutable
@Table(name = "r_nsi_status")
class NsiStatus(
    val code: String,
    @Id
    @Column(name = "nsi_status_id")
    val id: Long
) {
    enum class Code(val value: String) {
        END("COMP")
    }
}