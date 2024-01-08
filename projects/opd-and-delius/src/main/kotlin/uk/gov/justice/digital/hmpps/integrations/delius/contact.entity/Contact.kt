package uk.gov.justice.digital.hmpps.integrations.delius.contact.entity

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
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "contact")
@SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
class Contact(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime?,

    @Lob
    @Column
    val notes: String?,

    val teamId: Long,
    val staffId: Long,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Id
    @Column(name = "contact_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_generator")
    val id: Long = 0
) {
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    var createdByUserId: Long = 0

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0

    val partitionAreaId: Long = 0
}

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(

    val code: String,

    @Id
    @Column(name = "contact_type_id")
    val id: Long
) {
    enum class Code(val value: String) {
        PENDING_CONSULTATION("OPD025")
    }
}

interface ContactRepository : JpaRepository<Contact, Long>

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String): ContactType =
    findByCode(code) ?: throw NotFoundException("ContactType", "code", code)
