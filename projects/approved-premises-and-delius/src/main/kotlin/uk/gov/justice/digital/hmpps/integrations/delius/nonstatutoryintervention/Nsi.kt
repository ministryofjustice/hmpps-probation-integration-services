package uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention

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
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "nsi")
@EntityListeners(AuditingEntityListener::class)
class Nsi(
    @Id
    @SequenceGenerator(name = "nsi_id_generator", sequenceName = "nsi_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nsi_id_generator")
    @Column(name = "nsi_id", nullable = false)
    val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id", nullable = false)
    val type: NsiType,

    @ManyToOne
    @JoinColumn(name = "nsi_status_id")
    val status: NsiStatus,

    @Column(name = "nsi_status_date")
    val statusDate: ZonedDateTime = ZonedDateTime.now(),

    @Column
    val referralDate: LocalDate,

    @Column
    val expectedStartDate: LocalDate? = null,

    @Column
    val expectedEndDate: LocalDate? = null,

    @Column
    val actualStartDate: ZonedDateTime? = null,

    actualEndDate: ZonedDateTime? = null,

    @Lob
    @Column
    val notes: String? = null,

    val externalReference: String? = null,

    @Column(columnDefinition = "number")
    val pendingTransfer: Boolean = false,

    @Column
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(name = "active_flag", columnDefinition = "number")
    var active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
) {

    var actualEndDate: ZonedDateTime? = actualEndDate
        set(value) {
            field = value
            active = value == null
        }

    companion object {
        const val EXT_REF_BOOKING_PREFIX = "urn:uk:gov:hmpps:approved-premises-service:booking:"
    }
}

interface NsiRepository : JpaRepository<Nsi, Long> {
    fun findByExternalReference(externalReference: String): Nsi?
}
