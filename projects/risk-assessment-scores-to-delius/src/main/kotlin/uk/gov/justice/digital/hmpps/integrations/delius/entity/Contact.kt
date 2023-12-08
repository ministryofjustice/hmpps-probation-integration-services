package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
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
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Contact(
    @Id
    @SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_generator")
    @Column(name = "contact_id", nullable = false)
    val id: Long = 0,
    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime? = null,
    @ManyToOne
    @JoinColumn(name = "contact_type_id", nullable = false)
    val type: ContactType,
    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,
    @Lob
    @Column
    val notes: String? = null,
    @Column
    val staffId: Long,
    @Column
    val teamId: Long,
    @Convert(converter = YesNoConverter::class)
    @Column(name = "alert_active")
    val alert: Boolean? = false,
    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),
    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,
    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),
    @Column(nullable = false)
    @CreatedBy
    var createdByUserId: Long = 0,
    @Column(nullable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
    @Column(nullable = false)
    val partitionAreaId: Long = 0,
    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,
    @Column(name = "contact_date", nullable = false)
    val date: ZonedDateTime,
)

const val OGRS_ASSESSMENT_CT = "EOGR"

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id", nullable = false)
    val id: Long,
    @Column(nullable = false)
    val code: String,
)

interface ContactRepository : JpaRepository<Contact, Long>

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String): ContactType =
    findByCode(code) ?: throw NotFoundException("ContactType", "code", code)
